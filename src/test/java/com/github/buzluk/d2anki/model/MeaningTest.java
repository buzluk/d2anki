package com.github.buzluk.d2anki.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeaningTest {

    @Test
    void shouldCreateMeaningWithDefinitionAndExamples() {
        String definition = "to find or discover something";
        List<String> examples = List.of("The tests detected high levels of lead.", "The disease is difficult to detect.");

        Meaning meaning = new Meaning(definition, examples);

        assertEquals(definition, meaning.definition());
        assertEquals(examples, meaning.examples());
        assertEquals(2, meaning.examples().size());
    }

    @Test
    void shouldCreateMeaningWithEmptyExamples() {
        String definition = "a simple definition";
        List<String> examples = Collections.emptyList();

        Meaning meaning = new Meaning(definition, examples);

        assertEquals(definition, meaning.definition());
        assertTrue(meaning.examples().isEmpty());
    }

    @Test
    void shouldCreateMeaningWithNullExamples() {
        String definition = "a definition without examples";

        Meaning meaning = new Meaning(definition, null);

        assertEquals(definition, meaning.definition());
        assertNull(meaning.examples());
    }

    @Test
    void shouldBeEqualWhenSameData() {
        Meaning meaning1 = new Meaning("definition", List.of("example"));
        Meaning meaning2 = new Meaning("definition", List.of("example"));

        assertEquals(meaning1, meaning2);
        assertEquals(meaning1.hashCode(), meaning2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentData() {
        Meaning meaning1 = new Meaning("definition1", List.of("example"));
        Meaning meaning2 = new Meaning("definition2", List.of("example"));

        assertNotEquals(meaning1, meaning2);
    }
}
