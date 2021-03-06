package org.elegadro.neo4j.util;

import org.elegadro.iota.legal.number.LegalNumber;
import org.neo4j.driver.v1.Value;

import static org.elegadro.iota.legal.number.LegalNumber.LEGAL_NUMBER_KEY;
import static org.elegadro.iota.legal.number.LegalNumber.LEGAL_SUPER_KEY;
import static org.elegadro.iota.legal.number.LegalNumber.LEGAL_ROMAN_KEY;

/**
 * @author Taimo Peelo
 */
public final class GraphNodeUtil {
    private GraphNodeUtil() {}

    /**
     * Appends the legal number (if it has content!) to supplied Neo4J graph node,
     * and returns the same node, modified. */
    public static org.neo4j.graphdb.Node appendLegalNumberToNode(LegalNumber pn, org.neo4j.graphdb.Node node) {
        if (pn == null || (pn.getNum() == null && pn.getSup() == null))
            return node;

        if (pn.getNum() != null)
            node.setProperty(LEGAL_NUMBER_KEY, pn.getNum());

        if (pn.getSup() != null)
            node.setProperty(LEGAL_SUPER_KEY, pn.getSup());

        node.setProperty(LEGAL_ROMAN_KEY, pn.isRoman());

        return node;
    }

    /** Returns legal number from Neo4J graph driver node. */
    public static LegalNumber legalNumberFromNode(org.neo4j.driver.v1.types.Node node) {
        Value numVal = node.get(LEGAL_NUMBER_KEY);
        Integer iNum = numVal.isNull() ? null : numVal.asInt();
        Value supVal = node.get(LEGAL_SUPER_KEY);
        String sSup = supVal.isNull() ? null : supVal.asString();
        Value romanVal = node.get(LEGAL_ROMAN_KEY);

        boolean bRoman = romanVal.isNull() ? false : romanVal.asBoolean();

        return new LegalNumber(bRoman, iNum, sSup, true); // Assume present node unexpired
    }

    /** Returns legal number from Neo4J graph node. */
    public static LegalNumber legalNumberFromNode(org.neo4j.graphdb.Node node) {
        Integer numProp = (Integer) node.getProperty(LEGAL_NUMBER_KEY, null);
        String supProp = (String) node.getProperty(LEGAL_SUPER_KEY, null);
        Boolean isRomanProp = (Boolean) node.getProperty(LEGAL_ROMAN_KEY, Boolean.FALSE);

        return new LegalNumber(isRomanProp.booleanValue(), numProp, supProp, true); // Assume present node unexpired
    }
}
