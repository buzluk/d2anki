package com.github.buzluk.d2anki.client.request;

import com.github.buzluk.d2anki.model.Word;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class SearchingWordRequestTest {

    @Test
    void shouldCreateRequestForWord() {
        AtomicReference<Word> result = new AtomicReference<>();

        SearchingWordRequest request = SearchingWordRequest.forWord("detect", result::set);

        assertNotNull(request);
        assertNotNull(request.getHttpRequest());
    }

    @Test
    void shouldFormatUrlCorrectly() {
        SearchingWordRequest request = SearchingWordRequest.forWord("detect", word -> {
        });

        String uri = request.getHttpRequest().uri().toString();

        assertTrue(uri.contains("detect_1"));
        assertTrue(uri.startsWith("https://www.oxfordlearnersdictionaries.com/definition/english/"));
    }

    @Test
    void shouldConvertToLowercase() {
        SearchingWordRequest request = SearchingWordRequest.forWord("DETECT", word -> {
        });

        String uri = request.getHttpRequest().uri().toString();

        assertTrue(uri.contains("detect"));
        assertFalse(uri.contains("DETECT"));
    }

    @Test
    void shouldReplaceSpacesWithDashes() {
        SearchingWordRequest request = SearchingWordRequest.forWord("ice cream", word -> {
        });

        String uri = request.getHttpRequest().uri().toString();

        assertTrue(uri.contains("ice-cream"));
    }

    @Test
    void shouldTrimWhitespace() {
        SearchingWordRequest request = SearchingWordRequest.forWord("  detect  ", word -> {
        });

        String uri = request.getHttpRequest().uri().toString();

        assertTrue(uri.contains("detect_1"));
        assertFalse(uri.contains(" "));
    }

    @Test
    void shouldHaveBodyHandler() {
        SearchingWordRequest request = SearchingWordRequest.forWord("test", word -> {
        });

        HttpResponse.BodyHandler<String> handler = request.getBodyHandler();

        assertNotNull(handler);
    }

    @Test
    void shouldHaveUserAgentHeader() {
        SearchingWordRequest request = SearchingWordRequest.forWord("test", word -> {
        });

        assertTrue(request.getHttpRequest().headers().firstValue("User-Agent").isPresent());
    }

    @Test
    void shouldUseGetMethod() {
        SearchingWordRequest request = SearchingWordRequest.forWord("test", word -> {
        });

        assertEquals("GET", request.getHttpRequest().method());
    }

    @Test
    void shouldHaveRetryCount() {
        SearchingWordRequest request = SearchingWordRequest.forWord("test", word -> {
        });

        assertTrue(request.getRetryCount() > 0);
    }

    @Test
    void shouldThrowExceptionForNullWord() {
        assertThrows(NullPointerException.class, () -> SearchingWordRequest.forWord(null, word -> {
        }));
    }

    @Test
    void shouldThrowExceptionForNullHandler() {
        assertThrows(NullPointerException.class, () -> SearchingWordRequest.forWord("test", null));
    }
}
