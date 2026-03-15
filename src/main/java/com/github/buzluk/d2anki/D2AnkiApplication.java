package com.github.buzluk.d2anki;

import java.nio.file.Path;

/**
 * Interface for the main D2Anki application workflow.
 */
public interface D2AnkiApplication {
    /**
     * Executes the main application logic.
     *
     * @param inputPath Optional path to an input file, if applicable for the implementation.
     */
    void run();
}
