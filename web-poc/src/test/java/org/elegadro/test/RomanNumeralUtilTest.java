package org.elegadro.test;

import org.junit.Test;

import static org.elegadro.parser.util.rt.RomanNumeralUtil.decimalToRoman;
import static org.elegadro.parser.util.rt.RomanNumeralUtil.romanToDecimal;
import static org.junit.Assert.assertEquals;

/**
 * @author Taimo Peelo
 */
public class RomanNumeralUtilTest {
    @Test
    public void testArabicToRomanSingleChars() {
        assertEquals("Arabic 1 to Roman", "I", decimalToRoman(1));
        assertEquals("Arabic 5 to Roman", "V", decimalToRoman(5));
        assertEquals("Arabic 10 to Roman", "X", decimalToRoman(10));
        assertEquals("Arabic 50 to Roman", "L", decimalToRoman(50));
        assertEquals("Arabic 100 to Roman", "C", decimalToRoman(100));
        assertEquals("Arabic 500 to Roman", "D", decimalToRoman(500));
        assertEquals("Arabic 1000 to Roman", "M", decimalToRoman(1000));
    }

    @Test
    public void testArabicToRomanNumbers() {
        assertEquals("Arabic 1954 to Roman", "MCMLIV", decimalToRoman(1954));
        assertEquals("Arabic 1990 to Roman", "MCMXC", decimalToRoman(1990));
        assertEquals("Arabic 2014 to Roman", "MMXIV", decimalToRoman(2014));
    }
    
    @Test
    public void testRomanSingleCharsToArabic() {
        assertEquals("Roman I to Arabic",1, romanToDecimal("I").get().intValue());
        assertEquals("Roman V to Arabic", 5, romanToDecimal("V").get().intValue());
        assertEquals("Roman X to Arabic", 10, romanToDecimal("X").get().intValue());
        assertEquals("Roman L to Arabic", 50, romanToDecimal("L").get().intValue());
        assertEquals("Roman C to Arabic", 100, romanToDecimal("C").get().intValue());
        assertEquals("Roman D to Arabic", 500, romanToDecimal("D").get().intValue());
        assertEquals("Roman M to Arabic", 1000, romanToDecimal("M").get().intValue());
    }

    @Test
    public void testRomanToArabicNumbers() {
        assertEquals("Roman MCMLIV to Arabic",1954, romanToDecimal("MCMLIV").get().intValue());
        assertEquals("Roman MCMXC to Arabic", 1990, romanToDecimal("MCMXC").get().intValue());
        assertEquals("Roman MMXIV to Arabic", 2014, romanToDecimal("MMXIV").get().intValue());
    }
}
