package com.github.buzluk.d2anki.client;

import com.github.buzluk.d2anki.client.request.AsyncHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class AsyncHttpClient {
    private static final int MAX_CONCURRENT_REQUESTS = 10;

    private final HttpClient client;
    private final Semaphore semaphore;
    private final Queue<AsyncHttpRequest<?>> requestQueue = new ConcurrentLinkedQueue<>();
    private final Collection<AsyncHttpRequest<?>> failedRequests = new ConcurrentLinkedQueue<>();

    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final Object termination = new Object();

    private final AtomicInteger consecutiveCriticalFailures = new AtomicInteger(0);
    private volatile boolean isThrottled = false;

    public AsyncHttpClient() {
        log.info("Initializing AsyncHttpClient. Max Concurrent: {}, Timeout: 10s", MAX_CONCURRENT_REQUESTS);

        ExecutorService executor = Executors.newCachedThreadPool();
        this.client = HttpClient.newBuilder()
                .executor(executor)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();

        this.semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

        Thread dispatcherThread = new Thread(this::processQueueLoop);
        dispatcherThread.setName("AsyncHttpClient-Dispatcher");
        dispatcherThread.start();

        log.debug("Dispatcher thread started.");
    }

    public void sendRequest(AsyncHttpRequest<?> request) {
        log.trace("Entering sendRequest for request: '{}'", request);
        int currentActive = activeTaskCount.incrementAndGet();
        requestQueue.add(request);
        log.debug("Enqueued request for '{}'. Total active tasks: {}", request, currentActive);
        synchronized (requestQueue) {
            requestQueue.notifyAll();
        }
    }

    private void processQueueLoop() {
        log.info("Dispatcher loop started processing.");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (isThrottled) {
                    log.trace("Throttling is active. Applying backoff delay.");
                    applyBackoffDelay();
                }

                log.trace("Dispatcher waiting for semaphore permit. Available: {}", semaphore.availablePermits());
                semaphore.acquire();

                AsyncHttpRequest<?> request = requestQueue.poll();
                if (request == null) {
                    log.trace("Request queue is empty. Releasing permit and waiting.");
                    semaphore.release();
                    synchronized (requestQueue) {
                        requestQueue.wait();
                    }
                    continue;
                }

                log.debug("Dispatching request for '{}'. Retry count remaining: {}", request, request.getRetryCount());
                sendAsync(request);

            } catch (InterruptedException _) {
                log.warn("Dispatcher thread interrupted. Stopping loop.");
                Thread.currentThread().interrupt();
            }
        }
        log.info("Dispatcher loop terminated.");
    }

    private <T> void sendAsync(AsyncHttpRequest<T> request) {
        log.trace("Building async HTTP request for '{}'", request);

        client.sendAsync(request.getHttpRequest(), request.getBodyHandler()).thenApply(response -> {
            log.trace("Received response for '{}'", request);
            processResponse(request, response);
            return null;
        }).exceptionally(ex -> {
            log.error("Async exception occurred for '{}': {}", request, ex.getMessage());
            handleFailure(request, ex);
            return null;
        }).whenComplete((result, ex) -> {
            semaphore.release();
            int remaining = activeTaskCount.decrementAndGet();

            log.trace("Task completed/failed for '{}'. Permit released. Remaining active tasks: {}", request, remaining);

            if (remaining == 0 && requestQueue.isEmpty()) {
                log.debug("Queue empty and no active tasks. Notifying termination lock.");
                synchronized (termination) {
                    termination.notifyAll();
                }
            }
        });
    }

    private <T> void processResponse(AsyncHttpRequest<T> request, HttpResponse<T> response) {
        int status = response.statusCode();
        log.debug("Processing response for '{}'. Status Code: {}", request, status);

        if (status == 200) {
            resetThrottle();
            log.trace("Successful response (200) for '{}'. Calling handler.", request);
            request.handleHttpResponse(response);
        } else if (status == 404) {
            resetThrottle();
            log.warn("Word not found (404): '{}'. Skipping retry.", request);
        } else if (status == 429 || status >= 500) {
            log.warn("Critical Server Error/Rate Limit ({}) for '{}'. Triggering throttle.", status, request);
            triggerThrottle();
            handleRetryLogic(request);
        } else {
            log.warn("Unexpected status code ({}) for '{}'. Attempting retry.", status, request);
            handleRetryLogic(request);
        }
    }

    private void handleFailure(AsyncHttpRequest<?> request, Throwable ex) {
        log.warn("Handling failure for '{}'. Exception: {}", request, ex.getClass().getSimpleName());
        handleRetryLogic(request);
    }

    private void handleRetryLogic(AsyncHttpRequest<?> request) {
        if (request.shouldRetry()) {
            int retriesLeft = request.decrementRetryCount();
            log.info("Retrying request for '{}'. Retries left: {}", request, retriesLeft);
            sendRequest(request);
        } else {
            log.error("Max retries reached for '{}'. Moving to failedRequests list.", request);
            failedRequests.add(request);
        }
    }

    private void triggerThrottle() {
        int fails = consecutiveCriticalFailures.incrementAndGet();
        isThrottled = true;
        log.warn("Throttle triggered. Consecutive critical failures: {}", fails);
    }

    private void resetThrottle() {
        if (consecutiveCriticalFailures.get() > 0) {
            log.info("Resetting throttle. System recovered.");
            consecutiveCriticalFailures.set(0);
            isThrottled = false;
        }
    }

    private void applyBackoffDelay() {
        try {
            int fails = consecutiveCriticalFailures.get();
            long backoff = Math.min(5000, 200 * (long) Math.pow(2, fails));

            log.warn("Applying dynamic backoff: {}ms due to {} consecutive failures.", backoff, fails);
            Thread.sleep(backoff);
        } catch (InterruptedException _) {
            log.error("Interrupted during backoff sleep.");
            Thread.currentThread().interrupt();
        }
    }

    public void waitForFinish() {
        log.info("Waiting for all requests to finish...");
        synchronized (termination) {
            while (activeTaskCount.get() > 0 || !requestQueue.isEmpty()) {
                try {
                    log.debug("Waiting... Active: {}, Queue: {}", activeTaskCount.get(), requestQueue.size());
                    termination.wait(2000);
                } catch (InterruptedException _) {
                    log.error("WaitForFinish interrupted.");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("All requests finished. Total failed requests: {}", failedRequests.size());
    }

    public List<AsyncHttpRequest<?>> getFailedRequests() {
        return new ArrayList<>(failedRequests);
    }
}