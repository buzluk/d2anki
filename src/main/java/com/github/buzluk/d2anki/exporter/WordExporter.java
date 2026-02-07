package com.github.buzluk.d2anki.exporter;

import com.github.buzluk.d2anki.model.Word;

import java.util.Collection;

public interface WordExporter {

    void export(Collection<Word> words);

}

