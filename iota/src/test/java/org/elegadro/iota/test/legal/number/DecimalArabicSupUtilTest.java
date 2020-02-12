package org.elegadro.iota.test.legal.number;

import org.elegadro.iota.legal.number.util.DecimalArabicSupUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taimo Peelo (taimo@timedrops.us).
 */
public class DecimalArabicSupUtilTest {
    @Test
    public void plus1_to_sup() {
        String s = DecimalArabicSupUtil.int2sup(1);
        System.out.println("1 to superscript -> :" + s);
        Assert.assertEquals("\u00B9", s);
    }

    @Test
    public void minus1_to_sup() {
        String s = DecimalArabicSupUtil.int2sup(-1);
        System.out.println("1 to superscript -> :" + s);
        Assert.assertEquals("\u207B\u00B9", s);
    }

    @Test
    public void plus12_to_sup() {
        String s = DecimalArabicSupUtil.int2sup(12);
        System.out.println("12 to superscript -> :" + s);
        Assert.assertEquals("\u00B9\u00B2", s);
    }

    @Test
    public void minus34_to_sup() {
        String s = DecimalArabicSupUtil.int2sup(-34);
        System.out.println("-34 to superscript -> :" + s);
        Assert.assertEquals("\u207B\u00B3\u2074", s);
    }

    @Test
    public void posMAXint_to_sup() {
        String s = DecimalArabicSupUtil.int2sup(Integer.MAX_VALUE);
        System.out.println(Integer.MAX_VALUE + " to superscript -> :" + s);
        Assert.assertEquals( "\u00B2\u00B9\u2074\u2077\u2074\u2078\u00B3\u2076\u2074\u2077", s);
    }

    @Test
    public void negMAXint_to_sup() {
        String s = DecimalArabicSupUtil.int2sup(Integer.MIN_VALUE);
        System.out.println(Integer.MIN_VALUE + " to superscript -> :" + s);
        Assert.assertEquals( "\u207B\u00B2\u00B9\u2074\u2077\u2074\u2078\u00B3\u2076\u2074\u2078", s);
    }
    // 1234 678

    @Test
    public void plus950_to_sup() {
        String s = DecimalArabicSupUtil.int2sup(950);
        System.out.println("950 to superscript -> :" + s);
        Assert.assertEquals( "\u2079\u2075\u2070", s);
    }
}
