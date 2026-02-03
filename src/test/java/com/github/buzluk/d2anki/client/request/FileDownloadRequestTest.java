package com.github.buzluk.d2anki.client.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.http.HttpResponse;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloadRequestTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCreateRequestWithValidUrl() {
        String url = "https://example.com/audio/test.mp3";

        FileDownloadRequest request = FileDownloadRequest.of(url, tempDir);

        assertNotNull(request);
        assertNotNull(request.getHttpRequest());
        assertEquals("https://example.com/audio/test.mp3", request.getHttpRequest().uri().toString());
    }

    @Test
    void shouldExtractFileNameFromUrl() {
        String fileName = FileDownloadRequest.extractFileName("https://example.com/audio/test_file.mp3");

        assertEquals("test_file.mp3", fileName);
    }

    @Test
    void shouldExtractFileNameFromUrlWithQueryParams() {
        String fileName = FileDownloadRequest.extractFileName("https://example.com/audio/test.mp3?version=1");

        assertEquals("test.mp3", fileName);
    }

    @Test
    void shouldExtractFileNameFromComplexUrl() {
        String fileName = FileDownloadRequest.extractFileName(
                "https://www.oxfordlearnersdictionaries.com/media/english/us_pron/c/con/consu/consumer__us_1.mp3"
        );

        assertEquals("consumer__us_1.mp3", fileName);
    }

    @Test
    void shouldThrowExceptionForNullUrl() {
        assertThrows(IllegalArgumentException.class, () -> FileDownloadRequest.extractFileName(null));
    }

    @Test
    void shouldThrowExceptionForEmptyUrl() {
        assertThrows(IllegalArgumentException.class, () -> FileDownloadRequest.extractFileName(""));
    }

    @Test
    void shouldThrowExceptionForNonMp3Url() {
        assertThrows(IllegalArgumentException.class, () -> FileDownloadRequest.extractFileName("https://example.com/file.txt"));
    }

    @Test
    void shouldThrowExceptionForUrlWithoutFile() {
        assertThrows(IllegalArgumentException.class, () -> FileDownloadRequest.extractFileName("https://example.com/"));
    }

    @Test
    void shouldHaveBodyHandler() {
        FileDownloadRequest request = FileDownloadRequest.of("https://example.com/test.mp3", tempDir);

        HttpResponse.BodyHandler<Path> handler = request.getBodyHandler();

        assertNotNull(handler);
    }

    @Test
    void shouldHaveUserAgentHeader() {
        FileDownloadRequest request = FileDownloadRequest.of("https://example.com/test.mp3", tempDir);

        assertTrue(request.getHttpRequest().headers().firstValue("User-Agent").isPresent());
    }

    @Test
    void shouldUseGetMethod() {
        FileDownloadRequest request = FileDownloadRequest.of("https://example.com/test.mp3", tempDir);

        assertEquals("GET", request.getHttpRequest().method());
    }

    @Test
    void shouldHaveRetryCount() {
        FileDownloadRequest request = FileDownloadRequest.of("https://example.com/test.mp3", tempDir);

        assertTrue(request.getRetryCount() > 0);
    }
}
