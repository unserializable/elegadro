package org.elegadro.iota.test.legal.number;

import org.elegadro.iota.legal.number.LegalNumber;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Taimo Peelo (taimo@timedrops.us).
 */
public class LegalNumberOrderTest {
    @Test
    public void _1lt_1sup1() {
        LegalNumber earlier = new LegalNumber(1, null,true);
        LegalNumber following = new LegalNumber(1, "1", true);

        Assert.assertTrue("1 must be less than 1\u00B9", earlier.compareTo(following) < 0);
        Assert.assertTrue("1\u00B9 must be more than 1", following.compareTo(earlier) > 0);
    }

    @Test
    public void identicalEqual() {
        LegalNumber earlier = new LegalNumber(1, "2", true);
        LegalNumber following = new LegalNumber(1, "2", true);

        Assert.assertTrue(earlier.compareTo(following) == 0);
        Assert.assertTrue(following.compareTo(earlier) == 0);
    }

    @Test
    public void naturalOrdered2ElementTreeSetCorrect() {
        Set<LegalNumber> numberSet = new TreeSet<>(Comparator.naturalOrder());

        LegalNumber earlier = new LegalNumber(1, null, true);
        LegalNumber following = new LegalNumber(1, "1", true);
        numberSet.add(following);
        numberSet.add(earlier);

        Iterator<LegalNumber> it = numberSet.iterator();

        Assert.assertEquals("First set element must be 1", earlier, it.next());
        Assert.assertEquals("Second set element must be 1^1", following, it.next());
    }

    @Test
    public void naturalOrdered4ElementTreeSetCorrect() {
        Set<LegalNumber> numberSet = new TreeSet<>();

        LegalNumber b4finita = new LegalNumber(1, "2", true);
        LegalNumber finita = new LegalNumber(2, null, true);
        LegalNumber earlier = new LegalNumber(1, null, true);
        LegalNumber following = new LegalNumber(1, "1", true);
        numberSet.add(finita);
        numberSet.add(earlier);
        numberSet.add(following);
        numberSet.add(b4finita);

        Iterator<LegalNumber> it = numberSet.iterator();

        Assert.assertEquals("First number", earlier, it.next());
        Assert.assertEquals("Second number", following, it.next());
        Assert.assertEquals("Third number", b4finita, it.next());
        Assert.assertEquals("Fourth number", finita, it.next());
    }
}
