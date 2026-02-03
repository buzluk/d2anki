package com.github.buzluk.d2anki.client.request;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class AsyncHttpRequestTest {

    @Test
    void shouldDecrementRetryCount() {
        TestAsyncHttpRequest request = new TestAsyncHttpRequest(3);

        assertEquals(3, request.getRetryCount());
        request.decrementRetryCount();
        assertEquals(2, request.getRetryCount());
        request.decrementRetryCount();
        assertEquals(1, request.getRetryCount());
    }

    @Test
    void shouldNotDecrementBelowZero() {
        TestAsyncHttpRequest request = new TestAsyncHttpRequest(1);

        request.decrementRetryCount();
        assertEquals(0, request.getRetryCount());
        request.decrementRetryCount();
        assertEquals(0, request.getRetryCount());
    }

    @Test
    void shouldRetryWhenCountGreaterThanZero() {
        TestAsyncHttpRequest request = new TestAsyncHttpRequest(2);

        assertTrue(request.shouldRetry());
        request.decrementRetryCount();
        assertTrue(request.shouldRetry());
        request.decrementRetryCount();
        assertFalse(request.shouldRetry());
    }

    @Test
    void shouldNotRetryWhenCountIsZero() {
        TestAsyncHttpRequest request = new TestAsyncHttpRequest(0);

        assertFalse(request.shouldRetry());
    }

    @Test
    void shouldGetHttpRequest() {
        TestAsyncHttpRequest request = new TestAsyncHttpRequest(3);

        assertNotNull(request.getHttpRequest());
        assertEquals("https://example.com/test", request.getHttpRequest().uri().toString());
    }

    private static class TestAsyncHttpRequest extends AsyncHttpRequest<String> {
        public TestAsyncHttpRequest(int maxRetries) {
            super(HttpRequest.newBuilder()
                    .uri(URI.create("https://example.com/test"))
                    .GET()
                    .build(), maxRetries);
        }

        @Override
        public HttpResponse.BodyHandler<String> getBodyHandler() {
            return HttpResponse.BodyHandlers.ofString();
        }

        @Override
        public void handleHttpResponse(HttpResponse<String> response) {
            //ignore
        }
    }
}
