package com.github.buzluk.d2anki.model;

public record Pronunciation(Accent accent,
                            String phonetic,
                            String soundSrc) {

    public String soundFileName() {
        return soundSrc.substring(soundSrc.lastIndexOf('/') + 1);
    }
}
