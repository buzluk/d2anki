package com.github.buzluk.d2anki.anki;

import com.github.buzluk.d2anki.anki.render.CardBackRenderer;
import com.github.buzluk.d2anki.anki.render.CardFrontRenderer;
import com.github.buzluk.d2anki.model.Word;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AnkiCard {
    private final Word word;

    public static AnkiCard fromWord(Word word) {
        return new AnkiCard(word);
    }

    public String getFrontAsHtml() {
        return CardFrontRenderer.create(word).renderAsHtml();
    }

    public String getBackAsHtml() {
        return CardBackRenderer.create(word).renderAsHtml();
    }

    public String getTag() {
        return word.category();
    }

    public String getName() {
        return word.name();
    }

    public String printAsTsvFormat() {
        return getFrontAsHtml() + "\t" + getBackAsHtml() + "\t" + getTag();
    }

}
