package org.elegadro.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author Taimo Peelo
 */
public class ArithUtil {
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public static final BigDecimal percentage(Number total, Number part) {
        BigDecimal bigDone = new BigDecimal(String.valueOf(part));
        BigDecimal bigTotal = new BigDecimal(String.valueOf(total));
        BigDecimal donePart = bigDone.divide(bigTotal, 2, RoundingMode.HALF_DOWN);
        BigDecimal percentage = HUNDRED.multiply(donePart).setScale(2, RoundingMode.HALF_DOWN);

        return percentage;
    }
}
