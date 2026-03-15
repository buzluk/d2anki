package com.github.buzluk.d2anki;

import com.github.buzluk.d2anki.client.D2AnkiHttpClient;
import com.github.buzluk.d2anki.exporter.TsvExporter;
import com.github.buzluk.d2anki.exporter.WordExporter;
import com.github.buzluk.d2anki.model.Word;
import com.github.buzluk.d2anki.service.AudioDownloader;
import com.github.buzluk.d2anki.service.FailureReporter;
import com.github.buzluk.d2anki.service.WordProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class DefaultFileD2AnkiApplication implements D2AnkiApplication {

    private final WordProvider wordProvider;
    private final WordExporter wordExporter;
    private final AudioDownloader audioDownloader;
    private final FailureReporter failureReporter;
    private final D2AnkiHttpClient client;



    @Override
    public void run() {
        long start = System.currentTimeMillis();

        Collection<Word> fetchedWords = wordProvider.fetchWords();

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

