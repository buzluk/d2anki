package com.github.buzluk.d2anki.client.request;

import com.github.buzluk.d2anki.model.OxfordWord;
import com.github.buzluk.d2anki.model.Word;
import com.github.buzluk.d2anki.parser.OxfordHtmlParser;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class OxfordWordRequest extends AsyncHttpRequest<String> {
    private final Consumer<Word> responseHandler;

    private OxfordWordRequest(HttpRequest req, Consumer<Word> responseHandler, int maxRetries) {
        super(req, maxRetries);
        this.responseHandler = Objects.requireNonNull(responseHandler);
    }

    public static OxfordWordRequest forWord(OxfordWord word, Consumer<Word> responseHandler) {
        String url = "https://www.oxfordlearnersdictionaries.com" + word.definitionUrl();
        HttpRequest req = HttpRequest
                .newBuilder(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .GET()
                .build();

        return new OxfordWordRequest(req, responseHandler, 5);
    }

    @Override
    public HttpResponse.BodyHandler<String> getBodyHandler() {
        return HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    }

    @Override
    public void handleHttpResponse(HttpResponse<String> response) {
        Word word = OxfordHtmlParser.parseWord(response.body());
        responseHandler.accept(word);
    }
}
