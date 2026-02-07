package com.github.buzluk.d2anki;

import com.github.buzluk.d2anki.client.AsyncHttpClient;
import com.github.buzluk.d2anki.config.AppConfig;
import com.github.buzluk.d2anki.exporter.TsvExporter;
import com.github.buzluk.d2anki.exporter.WordExporter;
import com.github.buzluk.d2anki.model.Word;
import com.github.buzluk.d2anki.service.AudioDownloader;
import com.github.buzluk.d2anki.service.FailureReporter;
import com.github.buzluk.d2anki.service.WordFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class D2AnkiApplication {

    private final WordFetcher wordFetcher;
    private final WordExporter wordExporter;
    private final AudioDownloader audioDownloader;
    private final FailureReporter failureReporter;
    private final AsyncHttpClient client;

    public static D2AnkiApplication create(AppConfig config) {
        AsyncHttpClient client = new AsyncHttpClient();

        WordFetcher wordFetcher = new WordFetcher(client);
        WordExporter wordExporter = new TsvExporter(config.outputFilePath());
        AudioDownloader audioDownloader = new AudioDownloader(client, config.mediaOutputDir());
        FailureReporter failureReporter = new FailureReporter(config.failedLogFilePath());

        return new D2AnkiApplication(wordFetcher, wordExporter, audioDownloader, failureReporter, client);
    }

    public void run(Path inputFilePath) {
        long start = System.currentTimeMillis();

        Collection<Word> fetchedWords = wordFetcher.fetchFromFile(inputFilePath);

        failureReporter.reportFailures(client.getFailedRequests());

        if (fetchedWords.isEmpty()) {
            log.warn("No words fetched. Terminating program.");
            return;
        }

        log.info("Starting post-processing for {} words...", fetchedWords.size());
        
        CompletableFuture<Void> exportTask = CompletableFuture.runAsync(() -> wordExporter.export(fetchedWords));
        audioDownloader.downloadAudioFiles(fetchedWords);
        exportTask.join();

        long duration = System.currentTimeMillis() - start;
        log.info("All operations completed in {} ms.", duration);
    }
}

