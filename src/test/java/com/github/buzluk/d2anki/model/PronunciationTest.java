package com.github.buzluk.d2anki.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PronunciationTest {

    @Test
    void shouldCreatePronunciationWithAllFields() {
        Pronunciation pron = new Pronunciation(Accent.US, "/dɪˈtekt/", "https://example.com/audio/detect__us_1.mp3");

        assertEquals(Accent.US, pron.accent());
        assertEquals("/dɪˈtekt/", pron.phonetic());
        assertEquals("https://example.com/audio/detect__us_1.mp3", pron.soundSrc());
    }

    @Test
    void shouldExtractSoundFileName() {
        Pronunciation pron = new Pronunciation(Accent.UK, "/dɪˈtekt/", "https://example.com/audio/detect__uk_1.mp3");

        assertEquals("detect__uk_1.mp3", pron.soundFileName());
    }

    @Test
    void shouldExtractSoundFileNameWithComplexUrl() {
        Pronunciation pron = new Pronunciation(Accent.US, "/test/", "https://www.oxfordlearnersdictionaries.com/media/english/us_pron/c/con/consu/consumer__us_1.mp3");

        assertEquals("consumer__us_1.mp3", pron.soundFileName());
    }

    @Test
    void shouldBeEqualWhenSameData() {
        Pronunciation pron1 = new Pronunciation(Accent.UK, "/test/", "https://example.com/test.mp3");
        Pronunciation pron2 = new Pronunciation(Accent.UK, "/test/", "https://example.com/test.mp3");

        assertEquals(pron1, pron2);
        assertEquals(pron1.hashCode(), pron2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentAccent() {
        Pronunciation pron1 = new Pronunciation(Accent.UK, "/test/", "https://example.com/test.mp3");
        Pronunciation pron2 = new Pronunciation(Accent.US, "/test/", "https://example.com/test.mp3");

        assertNotEquals(pron1, pron2);
    }
}
