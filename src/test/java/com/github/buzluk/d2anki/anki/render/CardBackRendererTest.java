package com.github.buzluk.d2anki.anki.render;

import com.github.buzluk.d2anki.model.Meaning;
import com.github.buzluk.d2anki.model.Word;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CardBackRendererTest {

    @Test
    void shouldRenderDefinitions() {
        Word word = createWordWithMeanings();
        CardBackRenderer renderer = CardBackRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertNotNull(html);
        assertTrue(html.contains("to discover something"));
        assertTrue(html.contains("definition-list"));
    }

    @Test
    void shouldRenderExamples() {
        List<Meaning> meanings = List.of(
                new Meaning("to discover something", List.of("Example sentence 1", "Example sentence 2"))
        );
        Word word = new Word("detect", "verb", Collections.emptyList(), meanings);
        CardBackRenderer renderer = CardBackRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertTrue(html.contains("Example sentence 1"));
        assertTrue(html.contains("Example sentence 2"));
        assertTrue(html.contains("example-list"));
    }

    @Test
    void shouldRenderMultipleMeanings() {
        List<Meaning> meanings = List.of(
                new Meaning("first definition", Collections.emptyList()),
                new Meaning("second definition", Collections.emptyList())
        );
        Word word = new Word("test", "noun", Collections.emptyList(), meanings);
        CardBackRenderer renderer = CardBackRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertTrue(html.contains("first definition"));
        assertTrue(html.contains("second definition"));
    }

    @Test
    void shouldRenderEmptyMeanings() {
        Word word = new Word("test", "noun", Collections.emptyList(), Collections.emptyList());
        CardBackRenderer renderer = CardBackRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertNotNull(html);
    }

    @Test
    void shouldIncludeCssStyles() {
        Word word = createWordWithMeanings();
        CardBackRenderer renderer = CardBackRenderer.create(word);

        String html = renderer.renderAsHtml();

        assertTrue(html.contains("<style>"));
        assertTrue(html.contains(".definition-list"));
        assertTrue(html.contains(".example-list"));
    }

    @Test
    void shouldThrowExceptionForNullWord() {
        assertThrows(NullPointerException.class, () -> CardBackRenderer.create(null));
    }

    private Word createWordWithMeanings() {
        List<Meaning> meanings = List.of(
                new Meaning("to discover something", List.of("The test detected a problem."))
        );
        return new Word("detect", "verb", Collections.emptyList(), meanings);
    }
}
