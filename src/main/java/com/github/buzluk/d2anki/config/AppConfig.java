package com.github.buzluk.d2anki.config;

import java.nio.file.Path;

public record AppConfig(
        Path mediaOutputDir,
        Path outputFilePath,
        Path failedLogFilePath,
        String defaultInputFile,
        int maxConcurrentRequests
) {

    public static AppConfig defaults() {
        return new AppConfig(
                Path.of("collection.media"),
                Path.of("output.tsv"),
                Path.of("failed_request.txt"),
                "words.txt",
                10 // Default value for maxConcurrentRequests
        );
    }
}

