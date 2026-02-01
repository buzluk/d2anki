package com.github.buzluk.d2anki.client.request;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileDownloadRequest extends AsyncHttpRequest<Path> {
    private final Path targetPath;

    private FileDownloadRequest(HttpRequest httpRequest, Path targetPath) {
        super(httpRequest, 5);
        this.targetPath = targetPath;
    }

    public static FileDownloadRequest of(String url, Path outputDirectory) {
        String fileName = extractFileName(url);
        Path targetPath = outputDirectory.resolve(fileName);
        final HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .GET().build();
        return new FileDownloadRequest(req, targetPath);
    }


    public static String extractFileName(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        int queryIndex = url.indexOf('?');
        String cleanUrl = (queryIndex != -1) ? url.substring(0, queryIndex) : url;

        int lastSlashIndex = cleanUrl.lastIndexOf('/');
        String filename = cleanUrl.substring(lastSlashIndex + 1);

        if (filename.length() > 4 && filename.toLowerCase().endsWith(".mp3")) {
            return filename;
        }

        throw new IllegalArgumentException("URL does not point to a valid MP3 file: " + url);
    }

    @Override
    public HttpResponse.BodyHandler<Path> getBodyHandler() {
        return HttpResponse.BodyHandlers.ofFile(targetPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void handleHttpResponse(HttpResponse<Path> response) {
        if (response.statusCode() == 200) {
            log.info("File downloaded successfully: {}", targetPath);
        } else {
            try {
                java.nio.file.Files.deleteIfExists(targetPath);
            } catch (Exception e) {
                log.warn("Could not cleanup failed download: {}", targetPath, e);
            }
            log.warn("Download failed with status: {} for {}", response.statusCode(), targetPath);
        }
    }

}
