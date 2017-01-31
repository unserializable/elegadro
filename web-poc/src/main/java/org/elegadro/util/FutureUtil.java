package org.elegadro.util;

import java.math.BigDecimal;
import java.util.concurrent.Future;

/**
 * @author Taimo Peelo
 */
public final class FutureUtil {
    private FutureUtil() {}

    public static final BigDecimal doneFuturesPercentage(Future[] futures) {
        return ArithUtil.percentage(futures.length, doneFuturesCount(futures));
    }

    public static final BigDecimal doneFuturesPercentage(Iterable<? extends Future> iterable) {
        int total = 0, done = 0;
        for (Future f: iterable) {
            total++;
            if (f.isDone())
                done++;
        }

        return ArithUtil.percentage(total, done);
    }

    public static final int doneFuturesCount(Iterable<? extends Future> iterable) {
        int done = 0;
        for (Future f: iterable) {
            if (f.isDone())
                done++;
        }
        return done;
    }

    public static final int doneFuturesCount(Future[] futures) {
        int total = futures.length, done = 0;
        for (int i = 0; i < total; i++) {
            if (futures[i].isDone())
                done++;
        }
        return done;
    }
}
