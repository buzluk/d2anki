package com.github.buzluk.d2anki.exporter;

import com.github.buzluk.d2anki.anki.AnkiCard;
import com.github.buzluk.d2anki.exception.ExportException;
import com.github.buzluk.d2anki.model.Word;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class TsvExporter implements WordExporter {

    private final Path outputPath;

    @Override
    public void export(Collection<Word> words) {
        log.info("TSV generation started.");
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            for (Word word : words) {
                AnkiCard card = AnkiCard.fromWord(word);
                writer.append(card.printAsTsvFormat()).append("\n");
            }
            log.info("TSV generation completed. Output: {}", outputPath.toAbsolutePath());
        } catch (IOException e) {
            throw new ExportException("Failed to write TSV file: " + outputPath, e);
        }
    }
}

