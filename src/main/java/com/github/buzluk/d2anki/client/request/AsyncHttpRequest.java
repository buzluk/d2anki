package com.github.buzluk.d2anki.client.request;

import lombok.Getter;
import lombok.ToString;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@ToString(onlyExplicitlyIncluded = true)
public abstract class AsyncHttpRequest<T> {

    @ToString.Include
    private final HttpRequest httpRequest;

    private final AtomicInteger retryCount;

    protected AsyncHttpRequest(HttpRequest httpRequest, int maxRetries) {
        this.httpRequest = httpRequest;
        this.retryCount = new AtomicInteger(maxRetries);
    }

    public abstract HttpResponse.BodyHandler<T> getBodyHandler();

    public abstract void handleHttpResponse(HttpResponse<T> response);

    public final int decrementRetryCount() {
        return retryCount.updateAndGet(val -> val > 0 ? val - 1 : 0);
    }

    public final boolean shouldRetry() {
        return retryCount.get() > 0;
    }

    public int getRetryCount() {
        return retryCount.get();
    }


}