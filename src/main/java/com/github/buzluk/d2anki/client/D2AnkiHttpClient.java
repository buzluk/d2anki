package com.github.buzluk.d2anki.client;

import com.github.buzluk.d2anki.client.request.AsyncHttpRequest;

import java.util.List;

/**
 * Interface for a custom asynchronous HTTP client.
 */
public interface D2AnkiHttpClient extends AutoCloseable {

    /**
     * Sends an asynchronous HTTP request.
     *
     * @param request The request to send.
     */
    void sendRequest(AsyncHttpRequest<?> request);

    /**
     * Waits for all active and queued requests to finish.
     */
    void waitForFinish();

    /**
     * Returns a list of requests that failed after exhausting all retries.
     *
     * @return A list of failed requests.
     */
    List<AsyncHttpRequest<?>> getFailedRequests();

    /**
     * Shuts down the client, interrupting ongoing operations and releasing resources.
     *
     * @throws Exception if an error occurs during shutdown.
     */
    @Override
    void close();
}
