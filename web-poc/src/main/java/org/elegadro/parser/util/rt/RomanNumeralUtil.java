package org.elegadro.parser.util.rt;

import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Oh how many years,
 * since Romans ruled the world.
 * Still we make machines,
 * parse their numerals.
 *
 * @author Taimo Peelo
 */
public final class RomanNumeralUtil {
    private static final short[] VALUES = new short[]
        {  1,    4,    5,   9,   10,   40,  50,   90, 100,  400, 500,  900, 1000};
    private static final String[] RNS = new String[]
        { "I", "IV", "V", "IX", "X", "XL", "L", "XC", "C", "CD", "D", "CM", "M" };
    private static final String RN_PAT = stream("IVXLCDM".split("")).collect(joining("|", "(", ")+"));
    private static final Pattern ROMAN_NUMBER_CHARS = Pattern.compile(RN_PAT);

    private RomanNumeralUtil() {}

    public static String decimalToRoman(Integer d) {
        if (d == null)
            return null;

        int id = d; // unbox d
        short i = (short)(VALUES.length-1); // index to Roman values

        StringBuilder sb = new StringBuilder();
        while (i >= 0) {
            if (id < VALUES[i--])
                continue;

            sb.append(RNS[++i]);
            id -= VALUES[i];
        }

        assert id == 0;

        return sb.toString();
    }

    /** A "lenient" Roman numeral parser: e.g. IIII returns 4 :) */
    public static Optional<Integer> romanToDecimal(String maybeRoman) {
        if (maybeRoman == null)
            return Optional.empty();

        String tru = maybeRoman.trim().toUpperCase();
        if (tru.isEmpty() || !ROMAN_NUMBER_CHARS.matcher(maybeRoman).matches())
            return Optional.empty();

        short res = arabic(maybeRoman.charAt(tru.length()-1));
        short last = res;
        for (int i = tru.length()-2; i >= 0; i--) {
            short prev = arabic(maybeRoman.charAt(i));
            res += ((prev < last) ? -prev : prev);
            if (prev > last)
                last = prev;
        }

        return Optional.of((int)res);
    }

    private static final short[] _CD__ILM = new short[] {0, 100, 500, 0, 0, 1, 50, 1000};
    private static short arabic(char roman) {
        if (0 < (roman & 16) >> 4)
            return 0 < (roman & 8) ? (short)10 : (short)5;

        return _CD__ILM[((roman & 12) >> 1) | (roman & 1)];
    }
}
