package com.github.buzluk.d2anki.anki.render;

import com.github.buzluk.d2anki.model.Accent;
import com.github.buzluk.d2anki.model.Pronunciation;
import com.github.buzluk.d2anki.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardFrontRendererTest {

    @Test
    void shouldRenderWordName() {
        Word word = createTestWord();
        CardFrontRenderer renderer = CardFrontRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertNotNull(html);
        assertTrue(html.contains("detect"));
    }

    @Test
    void shouldRenderCategory() {
        Word word = createTestWord();
        CardFrontRenderer renderer = CardFrontRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertTrue(html.contains("verb"));
        assertTrue(html.contains("category"));
    }

    @Test
    void shouldRenderPronunciations() {
        Word word = createTestWord();
        CardFrontRenderer renderer = CardFrontRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertTrue(html.contains("/dɪˈtekt/"));
        assertTrue(html.contains("US"));
        assertTrue(html.contains("UK"));
    }

    @Test
    void shouldRenderSoundReferences() {
        Word word = createTestWord();
        CardFrontRenderer renderer = CardFrontRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertTrue(html.contains("[sound:"));
        assertTrue(html.contains(".mp3]"));
    }

    @Test
    void shouldIncludeCssStyles() {
        Word word = createTestWord();
        CardFrontRenderer renderer = CardFrontRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertTrue(html.contains("<style>"));
        assertTrue(html.contains(".name"));
        assertTrue(html.contains(".category"));
        assertTrue(html.contains(".pron-container"));
    }

    @Test
    void shouldHandleEmptyPronunciations() {
        Word word = new Word("test", "noun", Collections.emptyList(), Collections.emptyList());
        CardFrontRenderer renderer = CardFrontRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertNotNull(html);
        assertTrue(html.contains("test"));
    }

    @Test
    void shouldHandleNullName() {
        Word word = new Word(null, "noun", Collections.emptyList(), Collections.emptyList());
        CardFrontRenderer renderer = CardFrontRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertNotNull(html);
    }

    @Test
    void shouldHandleNullCategory() {
        Word word = new Word("test", null, Collections.emptyList(), Collections.emptyList());
        CardFrontRenderer renderer = CardFrontRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertNotNull(html);
    }

    @Test
    void shouldThrowExceptionForNullWord() {
        assertThrows(NullPointerException.class, () -> CardFrontRenderer.create(null));
    }

    private Word createTestWord() {
        List<Pronunciation> pronunciations = List.of(
                new Pronunciation(Accent.US, "/dɪˈtekt/", "https://example.com/detect_us.mp3"),
                new Pronunciation(Accent.UK, "/dɪˈtekt/", "https://example.com/detect_uk.mp3")
        );
        return new Word("detect", "verb", pronunciations, Collections.emptyList());
    }
}
