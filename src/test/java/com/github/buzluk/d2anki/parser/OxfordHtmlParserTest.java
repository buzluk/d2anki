package com.github.buzluk.d2anki.parser;

import com.github.buzluk.d2anki.model.Accent;
import com.github.buzluk.d2anki.model.Pronunciation;
import com.github.buzluk.d2anki.model.Word;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OxfordHtmlParserTest {

    @Test
    void shouldParseWordName() {
        String html = createBasicHtml("detect", "verb");

        Word word = OxfordHtmlParser.parseWord(html);

        assertEquals("detect", word.name());
    }

    @Test
    void shouldParseCategory() {
        String html = createBasicHtml("detect", "verb");

        Word word = OxfordHtmlParser.parseWord(html);

        assertEquals("verb", word.category());
    }

    @Test
    void shouldParseSingleMeaning() {
        String html = createHtmlWithSingleMeaning("detect", "verb", "to discover something");

        Word word = OxfordHtmlParser.parseWord(html);

        assertFalse(word.meanings().isEmpty());
        assertEquals("to discover something", word.meanings().get(0).definition());
    }

    @Test
    void shouldParseMultipleMeanings() {
        String html = createHtmlWithMultipleMeanings("test", "noun",
                List.of("first definition", "second definition"));

        Word word = OxfordHtmlParser.parseWord(html);

        assertEquals(2, word.meanings().size());
        assertEquals("first definition", word.meanings().get(0).definition());
        assertEquals("second definition", word.meanings().get(1).definition());
    }

    @Test
    void shouldParseExamples() {
        String html = createHtmlWithExamples("detect", "verb", "to discover something",
                List.of("The test detected a problem.", "We detected an error."));

        Word word = OxfordHtmlParser.parseWord(html);

        List<String> examples = word.meanings().get(0).examples();
        assertEquals(2, examples.size());
        assertTrue(examples.contains("The test detected a problem."));
        assertTrue(examples.contains("We detected an error."));
    }

    @Test
    void shouldParseAmericanPronunciation() {
        String html = createHtmlWithPronunciation("detect", "verb", "/dɪˈtekt/", "https://example.com/detect_us.mp3", "phons_n_am");

        Word word = OxfordHtmlParser.parseWord(html);

        List<Pronunciation> usPron = word.pronunciations().stream()
                .filter(p -> p.accent() == Accent.US)
                .toList();

        assertFalse(usPron.isEmpty());
        assertEquals("/dɪˈtekt/", usPron.get(0).phonetic());
    }

    @Test
    void shouldParseBritishPronunciation() {
        String html = createHtmlWithPronunciation("detect", "verb", "/dɪˈtekt/", "https://example.com/detect_uk.mp3", "phons_br");

        Word word = OxfordHtmlParser.parseWord(html);

        List<Pronunciation> ukPron = word.pronunciations().stream()
                .filter(p -> p.accent() == Accent.UK)
                .toList();

        assertFalse(ukPron.isEmpty());
    }

    @Test
    void shouldHandleEmptyHtml() {
        String html = createBasicHtml("", "");

        Word word = OxfordHtmlParser.parseWord(html);

        assertNotNull(word);
        assertEquals("", word.name());
        assertEquals("", word.category());
    }

    @Test
    void shouldHandleMissingMeanings() {
        String html = createBasicHtml("test", "noun");

        Word word = OxfordHtmlParser.parseWord(html);

        assertNotNull(word.meanings());
    }

    @Test
    void shouldHandleMissingPronunciations() {
        String html = createBasicHtml("test", "noun");

        Word word = OxfordHtmlParser.parseWord(html);

        assertNotNull(word.pronunciations());
    }

    private String createBasicHtml(String headword, String pos) {
        return """
                <html>
                <body>
                <div id="main-container">
                    <div id="entryContent">
                        <div class="entry">
                            <div class="top-container">
                                <div class="top-g">
                                    <div class="webtop">
                                        <h1 class="headword">%s</h1>
                                        <span class="pos">%s</span>
                                        <span class="phonetics"></span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                </body>
                </html>
                """.formatted(headword, pos);
    }

    private String createHtmlWithSingleMeaning(String headword, String pos, String definition) {
        return """
                <html>
                <body>
                <div id="main-container">
                    <div id="entryContent">
                        <div class="entry">
                            <div class="top-container">
                                <div class="top-g">
                                    <div class="webtop">
                                        <h1 class="headword">%s</h1>
                                        <span class="pos">%s</span>
                                        <span class="phonetics"></span>
                                    </div>
                                </div>
                            </div>
                            <ol class="sense_single">
                                <li class="sense">
                                    <span class="def">%s</span>
                                </li>
                            </ol>
                        </div>
                    </div>
                </div>
                </body>
                </html>
                """.formatted(headword, pos, definition);
    }

    private String createHtmlWithMultipleMeanings(String headword, String pos, List<String> definitions) {
        StringBuilder senses = new StringBuilder();
        for (String def : definitions) {
            senses.append("<li class=\"sense\"><span class=\"def\">").append(def).append("</span></li>");
        }

        return """
                <html>
                <body>
                <div id="main-container">
                    <div id="entryContent">
                        <div class="entry">
                            <div class="top-container">
                                <div class="top-g">
                                    <div class="webtop">
                                        <h1 class="headword">%s</h1>
                                        <span class="pos">%s</span>
                                        <span class="phonetics"></span>
                                    </div>
                                </div>
                            </div>
                            <ol class="senses_multiple">
                                %s
                            </ol>
                        </div>
                    </div>
                </div>
                </body>
                </html>
                """.formatted(headword, pos, senses);
    }

    private String createHtmlWithExamples(String headword, String pos, String definition, List<String> examples) {
        StringBuilder examplesHtml = new StringBuilder("<ul class=\"examples\">");
        for (String ex : examples) {
            examplesHtml.append("<li>").append(ex).append("</li>");
        }
        examplesHtml.append("</ul>");

        return """
                <html>
                <body>
                <div id="main-container">
                    <div id="entryContent">
                        <div class="entry">
                            <div class="top-container">
                                <div class="top-g">
                                    <div class="webtop">
                                        <h1 class="headword">%s</h1>
                                        <span class="pos">%s</span>
                                        <span class="phonetics"></span>
                                    </div>
                                </div>
                            </div>
                            <ol class="sense_single">
                                <li class="sense">
                                    <span class="def">%s</span>
                                    %s
                                </li>
                            </ol>
                        </div>
                    </div>
                </div>
                </body>
                </html>
                """.formatted(headword, pos, definition, examplesHtml);
    }

    private String createHtmlWithPronunciation(String headword, String pos, String phonetic, String audioSrc, String phonClass) {
        return """
                <html>
                <body>
                <div id="main-container">
                    <div id="entryContent">
                        <div class="entry">
                            <div class="top-container">
                                <div class="top-g">
                                    <div class="webtop">
                                        <h1 class="headword">%s</h1>
                                        <span class="pos">%s</span>
                                        <span class="phonetics">
                                            <div class="%s">
                                                <span class="phon">%s</span>
                                                <div data-src-mp3="%s"></div>
                                            </div>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                </body>
                </html>
                """.formatted(headword, pos, phonClass, phonetic, audioSrc);
    }
}
