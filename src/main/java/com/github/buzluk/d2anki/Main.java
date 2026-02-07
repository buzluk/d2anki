package com.github.buzluk.d2anki;

import com.github.buzluk.d2anki.config.AppConfig;
import com.github.buzluk.d2anki.exception.WordFetchException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Main {

    public static void main(String[] args) {
        AppConfig config = AppConfig.defaults();
        Path inputFilePath = resolveInputFile(args, config.defaultInputFile());

        if (!validateInputFile(inputFilePath)) {
            System.exit(1);
        }

        try {
            D2AnkiApplication application = D2AnkiApplication.create(config);
            application.run(inputFilePath);
            System.exit(0);
        } catch (WordFetchException e) {
            log.error("Failed to fetch word definitions: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            System.exit(1);
        }
    }

    private static Path resolveInputFile(String[] args, String defaultInputFile) {
        if (args != null && args.length > 0) {
            String path = args[0];
            log.info("User argument detected. Processing file: {}", path);
            return Path.of(path);
        } else {
            log.warn("No arguments provided. Using default file: {}", defaultInputFile);
            return Path.of(defaultInputFile);
        }
    }

    private static boolean validateInputFile(Path inputFilePath) {
        if (!Files.exists(inputFilePath)) {
            log.error("ERROR: Specified file not found: {}", inputFilePath);
            return false;
        }
        return true;
    }
}