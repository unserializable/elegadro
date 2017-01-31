package org.elegadro.parser.util.rt;

/**
 * @author Taimo Peelo
 */
public final class DecimalArabicSupUtil {
    private DecimalArabicSupUtil() {};

    // https://en.wikipedia.org/wiki/Unicode_subscripts_and_superscripts
    private static final char UNICODE_SUPER_MINUS = '\u207B';
    private static final char[] UNICODE_SUPER = new char[] {
        '\u2070', /* ^0 */
        '\u00B9', /* ^1 */
        '\u00B2', /* ^2 */
        '\u00B3', /* ^3 */
        '\u2074', /* ^4 */
        '\u2075', /* ^5 */
        '\u2076', /* ^6 */
        '\u2077', /* ^7 */
        '\u2078', /* ^8 */
        '\u2079'  /* ^9 */
    };

    /** This only works with _positive_ integers, as it stands. */
    public static String int2sup(int i) {
        StringBuilder sb = new StringBuilder(11).append(i);
        int l = sb.length();

        for (int c = (i >= 0 ? 0 : 1); c < l; c++)
            sb.setCharAt(c, UNICODE_SUPER[sb.charAt(c)-'0']);

        if (i < 0)
            sb.setCharAt(0, UNICODE_SUPER_MINUS);

        return sb.toString();
    }
}