package org.elegadro.iota.parser;

import org.joor.Reflect;

import java.util.Optional;

/**
 * @author Taimo Peelo
 */
public abstract class Parser<I, O> {
    protected I parseSource;

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

    protected static final void requireNonNull(String param, Object o) throws IllegalStateException {
        if (o == null) {
            throw new IllegalStateException("Variable '" + param + "' must not be null.");
        }
    }
}
