package com.github.buzluk.d2anki.config;

import java.nio.file.Path;

public record AppConfig(
        Path mediaOutputDir,
        Path outputFilePath,
        Path failedLogFilePath,
        String defaultInputFile
) {

    public static AppConfig defaults() {
        return new AppConfig(
                Path.of("collection.media"),
                Path.of("output.tsv"),
                Path.of("failed_request.txt"),
                "words.txt"
        );
    }
}

