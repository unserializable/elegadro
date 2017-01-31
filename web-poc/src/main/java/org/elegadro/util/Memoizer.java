package org.elegadro.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Inspiration from functional prograaming and
 * <a href="https://dzone.com/articles/java-8-automatic-memoization">Java 8 Memoization</a>
 * article + comments.
 *
 * @author Taimo Peelo
 */
public class Memoizer {
    private static <T, R> Function<T, R> doMemoize(final Function<T, R> function) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return input -> cache.computeIfAbsent(input, function::apply);
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        return doMemoize(function);
    }
}