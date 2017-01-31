package org.elegadro.parser.util;

import org.cyberneko.html.parsers.DOMParser;
import org.jooq.lambda.tuple.Tuple2;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Taimo Peelo
 */
public final class HtmlParserUtil {
    /** Creates X(HT)ML document from SAX InputSource. Neko Parser features can be supplied */
    public static Document htmlParseToDocument(InputSource inputSource) {
        return htmlParseToDocument(inputSource, new Tuple2[]{});
    }

    /** Creates X(HT)ML document from SAX InputSource. */
    public static Document htmlParseToDocument(InputSource inputSource, Tuple2<String, Boolean>... nekoFeats) {
        DOMParser nekoParser = new DOMParser();

        try {
            /*  Possible to switch on recovery reporting with:
                nekoParser.setFeature("http://cyberneko.org/html/features/report-errors", true); */

            // parser feats
            nekoParser.setFeature("http://cyberneko.org/html/features/scanner/normalize-attrs", true);
            nekoParser.setFeature("http://cyberneko.org/html/features/balance-tags", true);

            if (nekoFeats != null && nekoFeats.length > 0) {
                for (Tuple2<String, Boolean> feat: nekoFeats) {
                    if (feat != null)
                        nekoParser.setFeature(feat.v1, feat.v2.booleanValue());
                }
            }

            // parser props
            nekoParser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");
        } catch (SAXException axe) {
            throw new RuntimeException(axe);
        }

        try {
            nekoParser.parse(inputSource);
        } catch (SAXException | IOException axe) {
            throw new RuntimeException(axe);
        }

        return nekoParser.getDocument();
    }

    /** Creates X(HT)ML document from SAX InputSource. */
    public static Document htmlParseToDocument(String string) {
        return htmlParseToDocument(new InputSource(new StringReader(string)));
    }

    /** Creates X(HT)ML document from SAX InputSource. */
    public static Document htmlParseToDocument(String string, Tuple2<String, Boolean>... nekoFeats) {
        return htmlParseToDocument(new InputSource(new StringReader(string)), nekoFeats);
    }

    /** Necessarily <i>synchronized</i> entrypoint for XPath instance creation in the application! */
    public static synchronized XPath newXPath() {
        return XPathFactory.newInstance().newXPath();
    }
}
