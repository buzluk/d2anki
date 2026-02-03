package com.github.buzluk.d2anki.anki;

import com.github.buzluk.d2anki.model.Accent;
import com.github.buzluk.d2anki.model.Meaning;
import com.github.buzluk.d2anki.model.Pronunciation;
import com.github.buzluk.d2anki.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnkiCardTest {

    @Test
    void shouldCreateAnkiCardFromWord() {
        Word word = createTestWord();
        AnkiCard card = AnkiCard.fromWord(word);

        assertNotNull(card);
        assertEquals("detect", card.getName());
        assertEquals("verb", card.getTag());
    }

    @Test
    void shouldGenerateFrontHtml() {
        Word word = createTestWord();
        AnkiCard card = AnkiCard.fromWord(word);

        String frontHtml = card.getFrontAsHtml();

        assertNotNull(frontHtml);
        assertTrue(frontHtml.contains("detect"));
        assertTrue(frontHtml.contains("verb"));
    }

    @Test
    void shouldGenerateBackHtml() {
        Word word = createTestWord();
        AnkiCard card = AnkiCard.fromWord(word);

        String backHtml = card.getBackAsHtml();

        assertNotNull(backHtml);
        assertTrue(backHtml.contains("to discover something"));
    }

    @Test
    void shouldGenerateTsvFormat() {
        Word word = createTestWord();
        AnkiCard card = AnkiCard.fromWord(word);

        String tsv = card.printAsTsvFormat();

        assertNotNull(tsv);
        assertTrue(tsv.contains("\t"));
        String[] parts = tsv.split("\t");
        assertEquals(3, parts.length);
    }

    @Test
    void shouldHandleWordWithEmptyCategory() {
        Word word = new Word("test", "", Collections.emptyList(), Collections.emptyList());
        AnkiCard card = AnkiCard.fromWord(word);

        assertEquals("", card.getTag());
    }

    @Test
    void shouldHandleWordWithNullCategory() {
        Word word = new Word("test", null, Collections.emptyList(), Collections.emptyList());
        AnkiCard card = AnkiCard.fromWord(word);

        assertNull(card.getTag());
    }

    private Word createTestWord() {
        List<Pronunciation> pronunciations = List.of(
                new Pronunciation(Accent.US, "/dɪˈtekt/", "https://example.com/detect_us.mp3"),
                new Pronunciation(Accent.UK, "/dɪˈtekt/", "https://example.com/detect_uk.mp3")
        );
        List<Meaning> meanings = List.of(
                new Meaning("to discover something", List.of("The test detected a problem."))
        );
        return new Word("detect", "verb", pronunciations, meanings);
    }
}
