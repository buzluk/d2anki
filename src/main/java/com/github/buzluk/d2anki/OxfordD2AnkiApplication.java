package com.github.buzluk.d2anki;

import com.github.buzluk.d2anki.client.D2AnkiHttpClient;
import com.github.buzluk.d2anki.exporter.WordExporter;
import com.github.buzluk.d2anki.service.AudioDownloader;
import com.github.buzluk.d2anki.service.FailureReporter;
import com.github.buzluk.d2anki.service.WordProvider;
import com.github.buzluk.d2anki.model.Word;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class OxfordD2AnkiApplication implements D2AnkiApplication {
    private final WordProvider wordProvider;
    private final WordExporter wordExporter;
    private final AudioDownloader audioDownloader;
    private final FailureReporter failureReporter;
    private final D2AnkiHttpClient client;

    public OxfordD2AnkiApplication(WordProvider wordProvider,
                                   WordExporter wordExporter,
                                   AudioDownloader audioDownloader,
                                   FailureReporter failureReporter,
                                   D2AnkiHttpClient client) {
        this.wordProvider = wordProvider;
        this.wordExporter = wordExporter;
        this.audioDownloader = audioDownloader;
        this.failureReporter = failureReporter;
        this.client = client;
    }

    @Override
    public void run() {
        Collection<Word> fetchedWords = wordProvider.fetchWords();

        if (fetchedWords.isEmpty()) {
            log.warn("No words fetched. Terminating program.");
            return;
        }

        failureReporter.reportFailures(client.getFailedRequests());

        CompletableFuture<Void> exportTask = CompletableFuture.runAsync(() -> wordExporter.export(fetchedWords));
        audioDownloader.downloadAudioFiles(fetchedWords);
        exportTask.join();
    }
}
