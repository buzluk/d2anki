package com.github.buzluk.d2anki.service;

import com.github.buzluk.d2anki.client.request.AsyncHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

@Slf4j
public class FailureReporter {

    private final Path logFilePath;

    public FailureReporter(Path logFilePath) {
        this.logFilePath = logFilePath;
    }
    
    public void reportFailures(Collection<AsyncHttpRequest<?>> failedRequests) {
        if (failedRequests.isEmpty()) {
            log.info("No failed requests to report.");
            return;
        }

        log.warn("{} requests failed even after retries.", failedRequests.size());

        List<String> failedUrls = failedRequests.stream()
                .map(AsyncHttpRequest::getHttpRequest)
                .map(HttpRequest::uri)
                .map(Object::toString)
                .toList();

        writeFailureLog(failedUrls);
    }

    private void writeFailureLog(List<String> failedUrls) {
        try {
            Files.write(logFilePath, failedUrls);
            log.info("Failed requests logged to: {}", logFilePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write failure log to: {}", logFilePath, e);
        }
    }
}

