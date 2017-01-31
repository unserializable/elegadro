package org.elegadro.parser;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.joor.Reflect;
import org.springframework.core.io.InputStreamSource;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import static org.elegadro.parser.util.HtmlParserUtil.htmlParseToDocument;
import static java.util.stream.Collectors.joining;

/**
 * @author Taimo Peelo
 */
public abstract class Parser<I, O> {
    protected I parseSource;
    private boolean isParsed = false;

    public Parser(I parseSource) {
        requireNonNull("parseSource", parseSource);
        this.parseSource = parseSource;
    }

    public abstract Optional<? extends O> parse();

    public static <FI, FO, SO> Optional<SO> combinedResult(Parser<FI, FO> p1, Class<? extends Parser<FO, SO>> pClazz) {
        Optional<? extends FO> p1parsed = p1.parse();
        if (!p1parsed.isPresent())
            return Optional.empty();

        FO fo = p1parsed.get();

        Parser<FO, SO> p2 = Reflect.on(pClazz).create(fo).get();
        Optional<? extends SO> p2parsed = p2.parse();

        return p2parsed.isPresent() ? Optional.of(p2parsed.get()) : Optional.empty();
    }

    // STATE MANAGEMENT

    public void setAsParsed() {
        isParsed = true;
    }

    // SUBCLASS HELPERS

    protected final String parseToString() {
        return parseToString("UTF-8");
    }

    protected final Document parseToDocument() {
        requireUnparsedState();

        return htmlParseToDocument(parseToString());
    }

    protected final String parseToString(String charsetName) {
        requireUnparsedState();

        if (parseSource instanceof InputStreamSource) {
            return parseInputStreamSourceToString((InputStreamSource) parseSource, charsetName);
        }

        return parseToStringInternal(charsetName);
    }

    protected String parseToStringInternal(String charsetName) {
        throw new UnsupportedOperationException("parseToStringInternal not implemented");
    }

    // FINALS
    @SneakyThrows(IOException.class)
    protected static final String parseInputStreamSourceToString(InputStreamSource iss, String charsetName) {
        @Cleanup InputStream is = iss.getInputStream();
        @Cleanup InputStreamReader isr = new InputStreamReader(is, charsetName);
        @Cleanup BufferedReader reader = new BufferedReader(isr);

        return reader.lines().collect(joining("\n"));
    }

    protected static final void requireNonNull(String param, Object o) throws IllegalStateException {
        if (o == null) {
            throw new IllegalStateException("Variable '" + param + "' must not be null.");
        }
    }

    protected final void requireParsedState() throws IllegalStateException {
        if (!isParsed)
            throw new IllegalStateException("Parsed state required");
    }

    protected final void requireUnparsedState() throws IllegalStateException {
        if (isParsed)
            throw new IllegalStateException("Unparsed state required.");
    }
}
