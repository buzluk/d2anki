package com.github.buzluk.d2anki.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccentTest {

    @Test
    void shouldHaveUKAccent() {
        Accent accent = Accent.UK;
        assertEquals("UK", accent.name());
    }

    @Test
    void shouldHaveUSAccent() {
        Accent accent = Accent.US;
        assertEquals("US", accent.name());
    }

    @Test
    void shouldHaveExactlyTwoValues() {
        assertEquals(2, Accent.values().length);
    }

    @Test
    void shouldParseFromString() {
        assertEquals(Accent.UK, Accent.valueOf("UK"));
        assertEquals(Accent.US, Accent.valueOf("US"));
    }
}
