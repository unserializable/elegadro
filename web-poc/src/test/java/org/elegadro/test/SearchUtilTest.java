package org.elegadro.test;

import org.elegadro.rt.search.LawParagraphSearch;
import org.junit.Assert;
import org.junit.Test;
import org.elegadro.actronym.Actronym;

import java.util.List;

import static org.elegadro.rt.search.SearchUtil.toLawParagraphQueryString;
import static org.elegadro.rt.search.SearchUtil.toLawParagraphSearch;

/**
 * @author Taimo Peelo
 */
public class SearchUtilTest {
    @Test
    public void simpleAVRS13() {
        List<LawParagraphSearch> lpss = toLawParagraphSearch("AVRS 13");
        Assert.assertFalse("Concrete law paragraph search expected", lpss.isEmpty());
        Assert.assertTrue("Expected one concrete law paragraph search", lpss.size() == 1);

        Assert.assertEquals("AVRS type expected", Actronym.AVRS, lpss.iterator().next().getAe());
        Assert.assertEquals("Not a range ", Boolean.FALSE, lpss.iterator().next().isRange());
        Assert.assertEquals("Paragraph # ", 13, lpss.iterator().next().getNumStart());

        String x = toLawParagraphQueryString(lpss);
        Assert.assertTrue("Query string must not be empty", !x.isEmpty());
        Assert.assertTrue("Query string must be terminated with semicolon", x.endsWith(";"));
    }

    /*
        More samples for tests:

        String[] sampleSearches = {
            "AVRS 13", "AVRS 17-36", "NONmatch", "AVRS 17-36", "AVRS 36-17", "AVRS 36--17",
            "AVRS 13 AVRS 40--42"
        };
     */
}
