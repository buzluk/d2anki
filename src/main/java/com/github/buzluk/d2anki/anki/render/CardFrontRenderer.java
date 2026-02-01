package com.github.buzluk.d2anki.anki.render;

import com.github.buzluk.d2anki.model.Accent;
import com.github.buzluk.d2anki.model.Pronunciation;
import com.github.buzluk.d2anki.model.Word;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CardFrontRenderer {
    private final Word word;

    public static CardFrontRenderer create(Word word) {
        return new CardFrontRenderer(requireNonNull(word, "word must not be null"));
    }

    public String renderAsHtml() {
        Document doc = Document.createShell("");
        doc.charset(StandardCharsets.UTF_8);
        doc.outputSettings().prettyPrint(false).charset(StandardCharsets.UTF_8);
        doc.head().appendElement("meta").attr("charset", "UTF-8");

        Element body = doc.body();
        body.appendChild(cssStyle());
        body.appendChild(createNameSection());
        body.appendChild(createCategoryElement());
        body.appendElement("br");
        body.appendElement("br");
        List<Element> pronunciationSection = createPronunciationSection();
        pronunciationSection.forEach(body::appendChild);
        return doc.outerHtml();
    }

    private Element createNameSection() {
        String wordName = word.name() == null ? "" : word.name();
        Element nameDiv = new Element("div");
        nameDiv.appendElement("span").addClass("name").appendElement("b").text(wordName);
        return nameDiv;
    }

    private Element createCategoryElement() {
        String category = word.category() == null ? "" : word.category();
        return new Element("div").appendElement("span").addClass("category").appendElement("i").text(category);
    }

    private List<Element> createPronunciationSection() {
        List<Pronunciation> ukPron = word.pronunciations().stream().filter(p -> p.accent() == Accent.UK).toList();
        List<Pronunciation> usPron = word.pronunciations().stream().filter(p -> p.accent() == Accent.US).toList();


        List<Element> pronList = new ArrayList<>();
        Element usPronContainer = new Element("div").addClass("pron-container");
        Element usAccentSpan = new Element("span").addClass("left-text").text(Accent.US.name());
        usPronContainer.appendChild(usAccentSpan);
        for (Pronunciation p : usPron) {
            usPronContainer.appendChild(createPhonDiv(p));
        }
        pronList.add(usPronContainer);

        pronList.add(new Element("br"));

        Element ukPronContainer = new Element("div").addClass("pron-container");
        Element ukAccentSpan = new Element("span").addClass("left-text").text(Accent.UK.name());
        ukPronContainer.appendChild(ukAccentSpan);
        for (Pronunciation p : ukPron) {
            ukPronContainer.appendChild(createPhonDiv(p));
        }
        pronList.add(ukPronContainer);


        return pronList;
    }


    private Element createPhonDiv(Pronunciation pron) {
        Element phonDiv = new Element("div").addClass("phon-div");
        phonDiv.appendElement("span").text("[sound:%s]".formatted(pron.soundFileName()));
        phonDiv.appendElement("span").addClass("phon-text").text(" %s ".formatted(pron.phonetic()));
        return phonDiv;
    }

    private Element cssStyle() {
        return new Element("style").appendText(
                ".name { font-size: 50px; }" +
                        ".category { font-size: 25px; font-style: italic;}" +
                        ".pron-container { display: flex; align-items: center; }" +
                        ".left-text { margin-right: 10px;}" +
                        ".phon-div { margin-left: 10px; padding: 4px; border: 1px solid #ccc; }" +
                        ".phon-text { margin-right: 5px;  }"
        );
    }


}