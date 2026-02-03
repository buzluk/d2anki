package com.github.bbuzluk.d2anki.parser;

import com.github.bbuzluk.d2anki.model.Accent;
import com.github.bbuzluk.d2anki.model.Meaning;
import com.github.bbuzluk.d2anki.model.Pronunciation;
import com.github.bbuzluk.d2anki.model.Word;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class OxfordHtmlParser {
    private static final String TOP_CONTENT_CSS_QUERY = "div.top-container > div.top-g > div.webtop";
    private static final String PHONETICS_CSS_QUERY = "> span.phonetics";
    private static final String MAIN_CONTENT_CSS_QUERY = "div#main-container > div#entryContent > div.entry";

    private final Element mainContent;
    private final Element topContent;
    private final Element phonetics;

    OxfordHtmlParser(String htmlContent) {
        Document doc = org.jsoup.Jsoup.parse(htmlContent);
        Element body = doc.body();
        this.mainContent = body.selectFirst(MAIN_CONTENT_CSS_QUERY);
        this.topContent = selectFirstSafely(this.mainContent, TOP_CONTENT_CSS_QUERY);
        this.phonetics = selectFirstSafely(this.topContent, PHONETICS_CSS_QUERY);
    }

    public static Word parseWord(String htmlContent) {
        return new OxfordHtmlParser(htmlContent).parse();
    }

    private Word parse() {
        List<Pronunciation> pronunciations = new ArrayList<>();
        pronunciations.addAll(parseAmericanPronunciation());
        pronunciations.addAll(parseBritishPronunciation());
        return new Word(getHeadWord(), getPos(), pronunciations, getMeanings());
    }

    private String getHeadWord() {
        Element headword = selectFirstSafely(topContent, "h1.headword");
        String result = textSafely(headword);
        return result == null ? "" : result;
    }

    private String getPos() {
        Element pos = selectFirstSafely(topContent, "span.pos");
        String result = textSafely(pos);
        return result == null ? "" : result;
    }

    private List<Pronunciation> parseAmericanPronunciation() {
        List<String> amPhon = getPhon("div.phons_n_am");
        List<String> amAudioSrc = getAudioSrc("div.phons_n_am");
        List<Pronunciation> result = new ArrayList<>();
        for (int i = 0; i < amPhon.size(); i++) {
            String phonetic = amPhon.get(i);
            String soundSrc = amAudioSrc.get(i);
            result.add(new Pronunciation(Accent.US, phonetic, soundSrc));
        }
        return result;
    }

    private List<Pronunciation> parseBritishPronunciation() {
        List<String> brPhon = getPhon("div.phons_br");
        List<String> brAudioSrc = getAudioSrc("div.phons_br");
        List<Pronunciation> result = new ArrayList<>();
        for (int i = 0; i < brPhon.size(); i++) {
            String phonetic = brPhon.get(i);
            String soundSrc = brAudioSrc.get(i);
            result.add(new Pronunciation(Accent.UK, phonetic, soundSrc));
        }
        return result;
    }

    private List<Meaning> getMeanings() {
        Meaning singleMeaning = parseSingleMeaning();
        if (singleMeaning != null) return List.of(singleMeaning);
        List<Meaning> multiMeanings = parseMultiMeanings();
        if (multiMeanings.isEmpty()) {
            log.warn("No meanings found for for headWord='{}'", getHeadWord());
        }
        return multiMeanings;
    }

    private List<String> getAudioSrc(String phonsCssQuery) {
        Element phonetic = selectFirstSafely(phonetics, phonsCssQuery);
        Elements dataSrcMp3s = selectSafely(phonetic, "> div[data-src-mp3]");
        if (dataSrcMp3s == null) {
            log.warn("No audio sources found for headWord='{}' with CSS query='{}'", getHeadWord(), phonsCssQuery);
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        for (Element src : dataSrcMp3s) {
            result.add(src.attr("data-src-mp3"));
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    private List<String> getPhon(String phonsCssQuery) {
        Element phonetic = selectFirstSafely(phonetics, phonsCssQuery);
        Elements phons = selectSafely(phonetic, "> span.phon");
        if (phons == null) {

            return Collections.emptyList();
        }
        List<String> result = phons.stream().map(Element::text).toList();
        return result.isEmpty() ? Collections.emptyList() : result;
    }


    private Meaning parseSingleMeaning() {
        Element senseSingle = selectFirstSafely(mainContent, "div.entry > ol.sense_single  li.sense");
        Element def = selectFirstSafely(senseSingle, "span.def");
        if (def == null) return null;
        String definition = textSafely(def);
        Element examples = selectFirstSafely(senseSingle, "> ul.examples");
        List<String> examplesAsText = getTextOfChildren(examples);
        return new Meaning(definition, examplesAsText);
    }

    private List<Meaning> parseMultiMeanings() {
        List<Meaning> result = new ArrayList<>();
        Elements sensesMultiple = selectSafely(mainContent, "div.entry > ol.senses_multiple li.sense");
        for (Element sense : sensesMultiple) {
            Element def = selectFirstSafely(sense, "span.def");
            if (def == null) continue;
            String definition = textSafely(def);
            Element examples = selectFirstSafely(sense, "> ul.examples");
            List<String> examplesAsText = getTextOfChildren(examples);
            Meaning meaning = new Meaning(definition, examplesAsText);
            result.add(meaning);
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    private List<String> getTextOfChildren(Element parent) {
        if (parent == null) return Collections.emptyList();
        List<String> result = parent.children().stream().map(Element::text).toList();
        return result.isEmpty() ? Collections.emptyList() : result;
    }

    private Element selectFirstSafely(Element parent, String cssQuery) {
        return parent == null ? null : parent.selectFirst(cssQuery);
    }

    private Elements selectSafely(Element parent, String cssQuery) {
        return parent == null ? null : parent.select(cssQuery);
    }

    private String textSafely(Element parent) {
        return parent == null ? null : parent.text();
    }
}
