package com.github.buzluk.d2anki.service;

import com.github.buzluk.d2anki.client.AsyncHttpClient;
import com.github.buzluk.d2anki.client.request.SearchingWordRequest;
import com.github.buzluk.d2anki.exception.WordFetchException;
import com.github.buzluk.d2anki.model.Word;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class WordFetcher {

    private final AsyncHttpClient client;

    public Collection<Word> fetchFromFile(Path inputFilePath) {
        Collection<Word> fetchedWords = new ConcurrentLinkedQueue<>();

        log.info("Reading words from '{}'...", inputFilePath);
        try (Stream<String> lines = Files.lines(inputFilePath, StandardCharsets.UTF_8)) {
            lines
                    .filter(line -> !line.isBlank())
                    .map(String::trim)
                    .distinct()
                    .forEach(word -> client.sendRequest(
                            SearchingWordRequest.forWord(word, fetchedWords::add)
                    ));
        } catch (IOException e) {
            throw new WordFetchException("Failed to read input file: " + inputFilePath, e);
        }

        log.info("Waiting for definitions...");
        client.waitForFinish();

        return fetchedWords;
    }
    
}

