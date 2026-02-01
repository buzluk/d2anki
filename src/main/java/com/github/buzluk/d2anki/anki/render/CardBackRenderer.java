package com.github.buzluk.d2anki.anki.render;

import com.github.buzluk.d2anki.model.Meaning;
import com.github.buzluk.d2anki.model.Word;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CardBackRenderer {
    private final Word word;

    public static CardBackRenderer create(Word word) {
        return new CardBackRenderer(requireNonNull(word, "word must not be null"));
    }

    public String renderAsHtml() {
        Document doc = Document.createShell("");
        doc.charset(StandardCharsets.UTF_8);
        doc.outputSettings().prettyPrint(false).charset(StandardCharsets.UTF_8);

        Element head = doc.head();
        head.appendElement("meta").attr("charset", "UTF-8");

        Element body = doc.body();
        body.appendChild(cssStyle());

        Element definitionList = body.appendElement("ol").addClass("definition-list");
        for (Meaning meaning : word.meanings()) {
            Element definitionListItem = definitionList.appendChild(li(meaning.definition()));
            List<String> examples = meaning.examples();
            if (examples == null) continue;
            Element exampleListItem = exampleListSection(examples);
            definitionListItem.appendChild(exampleListItem);
        }
        return doc.outerHtml();
    }

    private Element exampleListSection(List<String> examples) {
        Element exampleListItem = new Element("ul").addClass("example-list");
        for (String example : examples) {
            exampleListItem.appendChild(li(example));
        }
        return exampleListItem;
    }


    private Element li(String text) {
        return new Element("li").text(text);
    }

    private Element cssStyle() {
        return new Element("style").appendText(
                ".definition-list { font-size: 18px; }" +
                        ".example-list { font-size: 15px; }"
        );
    }
}
