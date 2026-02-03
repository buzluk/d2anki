package com.github.buzluk.d2anki.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordTest {

    @Test
    void shouldCreateWordWithAllFields() {
        List<Pronunciation> pronunciations = List.of(
                new Pronunciation(Accent.US, "/dɪˈtekt/", "https://example.com/detect_us.mp3"),
                new Pronunciation(Accent.UK, "/dɪˈtekt/", "https://example.com/detect_uk.mp3")
        );
        List<Meaning> meanings = List.of(
                new Meaning("to discover or notice something", List.of("The test can detect early signs of disease."))
        );

        Word word = new Word("detect", "verb", pronunciations, meanings);

        assertEquals("detect", word.name());
        assertEquals("verb", word.category());
        assertEquals(2, word.pronunciations().size());
        assertEquals(1, word.meanings().size());
    }

    @Test
    void shouldCreateWordWithEmptyPronunciationsAndMeanings() {
        Word word = new Word("test", "noun", Collections.emptyList(), Collections.emptyList());

        assertEquals("test", word.name());
        assertEquals("noun", word.category());
        assertTrue(word.pronunciations().isEmpty());
        assertTrue(word.meanings().isEmpty());
    }

    @Test
    void shouldCreateWordWithNullValues() {
        Word word = new Word(null, null, null, null);

        assertNull(word.name());
        assertNull(word.category());
        assertNull(word.pronunciations());
        assertNull(word.meanings());
    }

    @Test
    void shouldBeEqualWhenSameData() {
        List<Pronunciation> pronunciations = List.of(new Pronunciation(Accent.US, "/test/", "url"));
        List<Meaning> meanings = List.of(new Meaning("definition", List.of("example")));

        Word word1 = new Word("test", "noun", pronunciations, meanings);
        Word word2 = new Word("test", "noun", pronunciations, meanings);

        assertEquals(word1, word2);
        assertEquals(word1.hashCode(), word2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentName() {
        Word word1 = new Word("test1", "noun", Collections.emptyList(), Collections.emptyList());
        Word word2 = new Word("test2", "noun", Collections.emptyList(), Collections.emptyList());

        assertNotEquals(word1, word2);
    }
}
