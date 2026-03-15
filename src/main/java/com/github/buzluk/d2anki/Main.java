package com.github.buzluk.d2anki;

import com.github.buzluk.d2anki.client.AsyncHttpClient;
import com.github.buzluk.d2anki.client.D2AnkiHttpClient;
import com.github.buzluk.d2anki.config.AppConfig;
import com.github.buzluk.d2anki.exception.WordFetchException;
import com.github.buzluk.d2anki.exporter.TsvExporter;
import com.github.buzluk.d2anki.exporter.WordExporter;
import com.github.buzluk.d2anki.service.AudioDownloader;
import com.github.buzluk.d2anki.service.FailureReporter;
import com.github.buzluk.d2anki.service.FileWordProvider;
import com.github.buzluk.d2anki.service.ResourceCsvWordProvider;
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

        if (!validateInputFile(inputFilePath) && args != null && args.length > 0) { // Only validate if a file was explicitly provided
            System.exit(1);
        }

        try (D2AnkiHttpClient client = new AsyncHttpClient(config.maxConcurrentRequests())) { // Use D2AnkiHttpClient interface here
            AudioDownloader audioDownloader = new AudioDownloader(client, config.mediaOutputDir());
            WordExporter wordExporter = new TsvExporter(config.outputFilePath());
            FailureReporter failureReporter = new FailureReporter(config.failedLogFilePath()); // Correct constructor for FailureReporter

            D2AnkiApplication application;
            if (args != null && args.length > 0) {
                FileWordProvider fileWordProvider = new FileWordProvider(client, inputFilePath);
                application = new DefaultFileD2AnkiApplication(fileWordProvider, wordExporter, audioDownloader, failureReporter, client);
            } else {
                log.info("Using default Oxford 5000 CEFR word list.");
                ResourceCsvWordProvider resourceCsvWordProvider = new ResourceCsvWordProvider(client);
                application = new OxfordD2AnkiApplication(resourceCsvWordProvider, wordExporter, audioDownloader, failureReporter, client);
            }
            application.run(); // No inputFilePath parameter anymore
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