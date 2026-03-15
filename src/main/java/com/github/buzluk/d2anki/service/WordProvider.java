package com.github.buzluk.d2anki.service;

import com.github.buzluk.d2anki.model.Word;

import java.util.Collection;

/**
 * Interface for providing words from various sources.
 */
public interface WordProvider {
    /**
     * Fetches a collection of words.
     *
     * @return A collection of words.
     */
    Collection<Word> fetchWords();
}
