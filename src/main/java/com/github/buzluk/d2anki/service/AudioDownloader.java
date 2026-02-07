package com.github.buzluk.d2anki.service;

import com.github.buzluk.d2anki.client.AsyncHttpClient;
import com.github.buzluk.d2anki.client.request.FileDownloadRequest;
import com.github.buzluk.d2anki.model.Pronunciation;
import com.github.buzluk.d2anki.model.Word;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class AudioDownloader {

    private final AsyncHttpClient client;
    private final Path mediaOutputDir;

    public void downloadAudioFiles(Collection<Word> words) {
        log.info("Queuing audio download requests...");

        if (!ensureDirectoryExists()) {
            return;
        }

        int count = queueDownloads(words);
        log.info("Queued {} audio download requests.", count);

        log.info("Waiting for audio downloads to complete...");
        client.waitForFinish();
    }

    private boolean ensureDirectoryExists() {
        try {
            Files.createDirectories(mediaOutputDir);
            return true;
        } catch (IOException e) {
            log.error("Fatal: Could not create media directory.", e);
            return false;
        }
    }

    private int queueDownloads(Collection<Word> words) {
        int count = 0;
        for (Word word : words) {
            for (Pronunciation pronunciation : word.pronunciations()) {
                if (queueSingleDownload(word, pronunciation)) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean queueSingleDownload(Word word, Pronunciation pronunciation) {
        String src = pronunciation.soundSrc();
        if (src == null || src.isEmpty()) {
            return false;
        }

        try {
            client.sendRequest(FileDownloadRequest.of(src, mediaOutputDir));
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Skipping invalid audio URL for word '{}': {}", word.name(), e.getMessage());
            return false;
        }
    }
}

