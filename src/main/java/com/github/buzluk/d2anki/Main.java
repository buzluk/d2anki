package com.github.buzluk.d2anki;

import com.github.buzluk.d2anki.anki.AnkiCard;
import com.github.buzluk.d2anki.client.AsyncHttpClient;
import com.github.buzluk.d2anki.client.request.AsyncHttpRequest;
import com.github.buzluk.d2anki.client.request.FileDownloadRequest;
import com.github.buzluk.d2anki.client.request.SearchingWordRequest;
import com.github.buzluk.d2anki.model.Pronunciation;
import com.github.buzluk.d2anki.model.Word;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Main {

    private static final Path MEDIA_OUTPUT_DIR = Path.of("collection.media");
    private static final String DEFAULT_INPUT_FILE = "words.txt";
    private static final String OUTPUT_FILE = "output.tsv";
    private static final String FAILED_LOG_FILE = "failed_request.txt";

    private static final AsyncHttpClient client = new AsyncHttpClient();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        String inputFilePath = resolveInputFile(args);

        if (!Files.exists(Path.of(inputFilePath))) {
            log.error("ERROR: Specified file not found: {}", inputFilePath);
            System.exit(1);
        }

        Collection<Word> fetchedWords = new ConcurrentLinkedQueue<>();

        fetchDefinitions(inputFilePath, fetchedWords);

        reportFailures();

        if (fetchedWords.isEmpty()) {
            log.warn("No words fetched. Terminating program.");
            System.exit(0);
        }

        log.info("Starting post-processing for {} words...", fetchedWords.size());

        CompletableFuture<Void> tsvTask = CompletableFuture.runAsync(() -> generateTsv(fetchedWords));

        queueAudioDownloads(fetchedWords);
        log.info("Waiting for audio downloads to complete...");
        client.waitForFinish();
        tsvTask.join();

        long duration = System.currentTimeMillis() - start;
        log.info("All operations completed in {} ms.", duration);

        System.exit(0);
    }

    private static String resolveInputFile(String[] args) {
        if (args != null && args.length > 0) {
            String path = args[0];
            log.info("User argument detected. Processing file: {}", path);
            return path;
        } else {
            log.warn("No arguments provided. Using default file: {}", DEFAULT_INPUT_FILE);
            return DEFAULT_INPUT_FILE;
        }
    }

    private static void fetchDefinitions(String fileName, Collection<Word> resultCollection) {
        log.info("Reading words from '{}'...", fileName);
        try (Stream<String> lines = Files.lines(Path.of(fileName), StandardCharsets.UTF_8)) {
            lines
                    .filter(line -> !line.isBlank())
                    .map(String::trim)
                    .distinct()
                    .forEach(word -> client.sendRequest(SearchingWordRequest.forWord(word, resultCollection::add)));
        } catch (IOException e) {
            log.error("Failed to read input file", e);
            System.exit(1);
        }

        log.info("Waiting for definitions...");
        client.waitForFinish();
    }


    private static void reportFailures() {
        var failedRequests = client.getFailedRequests();
        if (failedRequests.isEmpty()) return;

        log.warn("{} requests failed even after retries.", failedRequests.size());

        List<String> failedUrls = failedRequests.stream()
                .map(AsyncHttpRequest::getHttpRequest)
                .map(HttpRequest::uri)
                .map(Object::toString)
                .toList();

        try {
            Path logPath = Path.of(FAILED_LOG_FILE);
            Files.write(logPath, failedUrls);
            log.info("Failed requests logged to: {}", logPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write failure log", e);
        }
    }

    private static void generateTsv(Collection<Word> words) {
        log.info("TSV generation started.");
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(OUTPUT_FILE), StandardCharsets.UTF_8)) {
            for (Word word : words) {
                AnkiCard card = AnkiCard.fromWord(word);
                writer.append(card.printAsTsvFormat()).append("\n");
            }
            log.info("TSV generation completed.");
        } catch (IOException e) {
            log.error("Failed to write TSV file", e);
        }
    }

    private static void queueAudioDownloads(Collection<Word> words) {
        log.info("Queuing audio download requests...");

        try {
            Files.createDirectories(MEDIA_OUTPUT_DIR);
        } catch (IOException e) {
            log.error("Fatal: Could not create media directory.", e);
            return;
        }

        int count = 0;
        for (Word word : words) {
            for (Pronunciation pronunciation : word.pronunciations()) {
                String src = pronunciation.soundSrc();
                if (src != null && !src.isEmpty()) {
                    try {
                        client.sendRequest(FileDownloadRequest.of(src, MEDIA_OUTPUT_DIR));
                        count++;
                    } catch (IllegalArgumentException e) {
                        log.warn("Skipping invalid audio URL for word '{}': {}", word.name(), e.getMessage());
                    }
                }
            }
        }
        log.info("Queued {} audio download requests.", count);
    }
}