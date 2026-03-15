package com.github.buzluk.d2anki.model;

public record OxfordWord(String word,
                         String definitionUrl,
                         String partOfSpeech,
                         String cefrLevel,
                         String americanPronUrl,
                         String britishPronUrl) {
}
