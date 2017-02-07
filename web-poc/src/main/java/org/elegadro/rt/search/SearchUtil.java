package org.elegadro.rt.search;

import lombok.extern.slf4j.Slf4j;
import org.elegadro.iota.rt.actronym.Actronym;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Taimo Peelo
 */
@Slf4j
public class SearchUtil {
    private static final String PATS =
        Arrays.stream(Actronym.values())
            .map(ae -> Pattern.quote(ae.getActronym()))
            .collect(Collectors.joining("|", "(?<law>", ")"));

    private static final String NUMS = "(?<num>\\d+)";
    private static final String RANGES = "(?<r>(?<rs>\\d+)\\s*(-)+\\s*(?<re>\\d+))";
    private static final String S_WS = "(\\s+)";

    private static final String NUMS_OR_RANGES = "(" + RANGES + "|" + NUMS + ")";

    private static final Pattern LAW_ACTRONYM_PATTERN = Pattern.compile(PATS + S_WS + NUMS_OR_RANGES);

    private static final String G_LAW = "law";
    private static final String G_NUM = "num";
    private static final String G_RANGE = "r";
    private static final String G_RANGE_START = "rs";
    private static final String G_RANGE_END = "re";

    public static String toLawParagraphQueryString(List<LawParagraphSearch> lpss) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<LawParagraphSearch> it = lpss.iterator(); it.hasNext(); ) {
            LawParagraphSearch lps = it.next();

            sb
                .append("MATCH rada=(s:Seadus)-[:HAS*..]->(p:Paragrahv)-[:HAS*..]->() ")
                .append("WHERE exists(p.`#`) ")
                .append("AND s.text='")
                .append(lps.getAe().getExpanym())
                .append("' ");

            if (!lps.isRange()) {
                sb  .append("AND p.`#`=")
                    .append(lps.getNumStart())
                    .append(' ');
            } else {
                sb  .append("AND ")
                    .append(lps.getNumStart())
                    .append(" <= ")
                    .append("p.`#`<= ")
                    .append(lps.getNumEnd())
                    .append(' ');
            }

            sb.append("RETURN rada");

            if (it.hasNext())
                sb.append(" UNION ");
            else
                sb.append(";");
        }

        return sb.toString();
    }

    /** Converts input e.g. "AVRS 17-36 AVRS 40" to suitable law paragraph search. */
    public static List<LawParagraphSearch> toLawParagraphSearch(String s) {
        if (s == null)
            return Collections.emptyList();
        s = s.trim();

        List cssList = new LinkedList();
        Matcher m = LAW_ACTRONYM_PATTERN.matcher(s);
        while (m.find()) {
            LawParagraphSearch cs;
            String law = m.group(G_LAW);
            Actronym ae;
            try {
                ae = Actronym.valueOf(law);
            } catch (IllegalArgumentException iae) {
                if (log.isTraceEnabled()) {
                    log.trace("Could not parse actronym for group '" + law + "'");
                }
                continue;
            }

            String num = m.group(G_NUM);
            if (null == num) {
                // look for range
                String rangeStart = m.group(G_RANGE_START);
                String rangeEnd = m.group(G_RANGE_END);
                cs = new LawParagraphSearch(ae, Integer.valueOf(rangeStart), Integer.valueOf(rangeEnd));
            } else {
                cs = new LawParagraphSearch(ae, Integer.valueOf(num));
            }

            cssList.add(cs);
        }

        return cssList.isEmpty() ? Collections.emptyList() : cssList;
    }
}
