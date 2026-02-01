package com.github.buzluk.d2anki.model;

import java.util.List;

public record Word(String name,
                   String category,
                   List<Pronunciation> pronunciations,
                   List<Meaning> meanings) {
}
