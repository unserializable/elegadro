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
    private static final Map<String, Actronym> ACRONYM_UC_2_ACTRONYM =
        Arrays.stream(Actronym.values())
            .collect(
                Collectors.toMap(
                    v -> v.getActronym().toUpperCase(),
                    v -> v,
                    (v1, v2) -> {
                        throw new IllegalStateException("Impossible happened: Actronyms must be duplicated, received " + v1  + " and " + v2);
                    },
                    () -> new LinkedHashMap<>(Actronym.values().length))
            );

    private static final String PATS =
        Arrays.stream(Actronym.values())
            .map(ae -> {
                String actronym = ae.getActronym();
                StringBuilder ulab = new StringBuilder("(");
                for (int i = 0; i < actronym.length(); i++) {
                    char c = actronym.charAt(i);
                    ulab.append("[")
                        .append(Pattern.quote("" + Character.toLowerCase(c)))
                        .append(Pattern.quote("" + Character.toUpperCase(c)))
                    .append("]");
                }
                ulab.append(")");
                return ulab.toString();
            })
            .collect(Collectors.joining("|", "(?<law>", "){1}"));

    private static final String NUMS = "(?<num>\\d+)";
    private static final String RANGES = "(?<r>(?<rs>\\d+)\\s*(-)+\\s*(?<re>\\d+))";
    private static final String S_WS = "(\\s+)";

    private static final String NUMS_OR_RANGES = "(?<seq>(" + RANGES + "|" + NUMS + "\\s*,?)+)";

    private static final Pattern LAW_ACTRONYM_PATTERN = Pattern.compile(PATS + S_WS + NUMS_OR_RANGES);
    private static final Pattern PG_SINGLE_PATTERN = Pattern.compile("(\\s*)" + NUMS + "(\\s*)");
    private static final Pattern PG_RANGE_PATTERN = Pattern.compile("(\\s*)" + RANGES + "(\\s*)");

    private static final String G_SEQ = "seq";
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
                .append("AND s.tr_et='")
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
            String law = m.group(G_LAW);
            Actronym ae = ACRONYM_UC_2_ACTRONYM.get(law.toUpperCase());
            if (ae == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Could not parse actronym for group '" + law + "'");
                }
                continue;
            }

            String pgSeq = m.group(G_SEQ);
            String[] pgQs = pgSeq.split(",");
            for (int i = 0; i < pgQs.length; i++) {
                String that = pgQs[i];
                Matcher singleMatcher = PG_SINGLE_PATTERN.matcher(that);
                if (singleMatcher.matches()) {
                    cssList.add(new LawParagraphSearch(ae, Integer.valueOf(singleMatcher.group(G_NUM))));
                    continue;
                }
                Matcher rangeMatcher = PG_RANGE_PATTERN.matcher(that);
                if (rangeMatcher.matches()) {
                    String rangeStart = rangeMatcher.group(G_RANGE_START);
                    String rangeEnd = rangeMatcher.group(G_RANGE_END);
                    LawParagraphSearch lprs = new LawParagraphSearch(ae, Integer.valueOf(rangeStart), Integer.valueOf(rangeEnd));
                    cssList.add(lprs);
                    continue;
                }
            }
        }

        return cssList.isEmpty() ? Collections.emptyList() : cssList;
    }
}
