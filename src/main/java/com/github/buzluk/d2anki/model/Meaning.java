package com.github.buzluk.d2anki.model;

import java.util.List;


public record Meaning(String definition,
                      List<String> examples) {
}
