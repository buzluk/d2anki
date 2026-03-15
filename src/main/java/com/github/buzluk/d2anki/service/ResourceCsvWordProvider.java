package com.github.buzluk.d2anki.service;

import com.github.buzluk.d2anki.client.D2AnkiHttpClient;
import com.github.buzluk.d2anki.client.request.OxfordWordRequest;
import com.github.buzluk.d2anki.exception.WordFetchException;
import com.github.buzluk.d2anki.model.OxfordWord;
import com.github.buzluk.d2anki.model.Word;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@RequiredArgsConstructor
public class ResourceCsvWordProvider implements WordProvider {

    private final D2AnkiHttpClient client;

    @Override
    public Collection<Word> fetchWords() {
        Collection<Word> fetchedWords = new ConcurrentLinkedQueue<>();
        var words = fetchDefaultOxford5000CefrWords();
        for (OxfordWord word : words) {
            client.sendRequest(OxfordWordRequest.forWord(word, fetchedWords::add));
        }
        client.waitForFinish();
        return fetchedWords;
    }

    private List<OxfordWord> fetchDefaultOxford5000CefrWords() {
        InputStream wordListAsInputStream = getClass().getResourceAsStream("/oxford_words.csv");
        if (wordListAsInputStream == null) {
            throw new WordFetchException("Failed to load default Oxford 5000 CEFR words. Resource not found: /oxford_words.csv");
        }

        try (CSVParser parser = CSVParser.parse(wordListAsInputStream, StandardCharsets.UTF_8, getCsvFormat())) {
            List<CSVRecord> wordList = parser.getRecords();
            return wordList.stream().map(row -> new OxfordWord(
                    row.get("word"),
                    row.get("definitionUrl"),
                    row.get("partOfSpeech"),
                    row.get("cefrLevel"),
                    row.get("americanPronUrl"),
                    row.get("britishPronUrl")
            )).toList();

        } catch (IOException e) {
            throw new WordFetchException("Failed fetch default Oxford 5000 CEFR words from wordListAsInputStream", e);
        }
    }

    private CSVFormat getCsvFormat() {
        return CSVFormat.DEFAULT.builder()
                .setHeader("word", "definitionUrl", "partOfSpeech", "cefrLevel", "americanPronUrl", "britishPronUrl")
                .setSkipHeaderRecord(true).get();
    }
}
