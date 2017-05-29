package org.elegadro.iota.parser.rt.util;

import _2010._02.tyviseadus_1_10.*;
import org.elegadro.iota.legal.number.LegalNumber;
import org.elegadro.iota.legal.number.util.RomanNumeralUtil;
import org.jooq.lambda.Unchecked;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.elegadro.iota.parser.rt.util.LawParseUtil.stringIDToString;

/**
 * @author Taimo Peelo
 */
public final class LegalNumberUtil {
    private LegalNumberUtil() {}

    public static LegalNumber legalNumberFor(OsaType osaType) {
        return legalNumberForType(osaType, osaType.getOsaNr());
    }

    public static LegalNumber legalNumberFor(PeatykkType peatykkType) {
        List<PeatykkType.PeatykkNr> peatykkNrs = peatykkType.getPeatykkNr();
        List<LegalNumber> legalNumbers = peatykkNrs.stream()
                .map(pnr -> legalNumberForType(peatykkType, pnr))
                .collect(Collectors.toList());

        // not quite certain yet, what to do with multiple ones...
        if (legalNumbers.size() != 1) {
            String lnStrings = legalNumbers.stream()
                .map(ln -> ln.toDebugString())
                .collect(Collectors.joining(","));
            throw new IllegalStateException("Cannot handle many legalNumbers, were " + lnStrings);
        }

        return legalNumbers.get(0);
    }

    public static LegalNumber legalNumberFor(JaguType jaguType) {
        return legalNumberForType(jaguType, jaguType.getJaguNr());
    }

    public static LegalNumber legalNumberFor(JaotisType jaotisType) {
        return legalNumberForType(jaotisType, jaotisType.getJaotisNr());
    }

    public static LegalNumber legalNumberFor(AlljaotisType alljaotisType) {
        return legalNumberForType(alljaotisType, alljaotisType.getAlljaotisNr());
    }

    public static LegalNumber legalNumberFor(ParagrahvType paragrahvType) {
        return legalNumberForType(paragrahvType, paragrahvType.getParagrahvNr());
    }

    public static LegalNumber legalNumberFor(LoigeType loigeType) {
        return legalNumberForType(loigeType, loigeType.getLoigeNr());
    }

    public static LegalNumber legalNumberFor(AlampunktType alampunktType) {
        return legalNumberForType(alampunktType, alampunktType.getAlampunktNr());
    }

    /*---------------------------------------------------------------------
      PRIVATE fuss!
      ********************************************************************* */

    private static LegalNumber legalNumberForType(TypeID type, StringID specificNr) {
        String nrFormat = specificNr != null ? getVormingFor(specificNr) : null;
        boolean expectedRoman = isRoman(nrFormat);
        // For laws, never true ^^ unfortunately, even though "I. peatükk"/"I" is used.
        String sup = specificNr != null ? getYlaindexFor(specificNr) : null;
        return getLegalNumberFor(expectedRoman, type, specificNr, sup);
    }

    /** Extracts the legal particle or legal molecul number from {@code type}. */
    private static LegalNumber getLegalNumberFor(boolean expectedRoman, TypeID type, StringID str, String sup) {
        StringID kstrID = C_KUVATAV_NR.apply(type);
        String knr = (kstrID != null) ? stringIDToString(kstrID) : null;
        String nr = (str != null) ? str.getValue() : null;

        if (knr != null) knr = knr.trim();
        if (nr != null) nr = nr.trim();
        if (sup != null) sup = sup.trim();

        if (!Objects.equals(knr, nr)) {
            if (!(consistentNumbering(knr, nr) || consistentSuplessNumbering(knr, nr, sup))) {
                // Allow one case as 'consistent': where display number (knr) is empty and loige nr
                // is equal to 1 -- such discrepancy arises because it is customary to not write lg
                // nr when the whole pg (§) consists of just one loige.
                if (!(type instanceof LoigeType && "1".equals(nr) && knr.isEmpty()))
                    throw new IllegalStateException("Unhappystance: '" + knr + "' !!=  '" + nr + "'");
            }
        }

        // matches are good, but the number input could still be "roman" ...
        if (nr != null && !nr.isEmpty() && !nr.matches("\\d+")) {
            Optional<Integer> optArabic = RomanNumeralUtil.romanToDecimal(nr);
            if (optArabic.isPresent())
                return new LegalNumber(true, optArabic.get(), sup);
        }

        Integer pgNr = (nr != null && !nr.isEmpty()) ? Integer.parseInt(nr) : null;
        return new LegalNumber(expectedRoman, pgNr, sup);
    }

    private static <T extends StringID> String getVormingFor(T t) {
        String ret = C_VORMING.apply(t);
        if (ret != null && !(V_ARAABIA.equals(ret) || V_ROOMA.equals(ret))) {
            throw new IllegalStateException("Unhappystance: vorming '" + ret + "'");
        }
        return ret;
    }

    private static <T extends StringID> String getYlaindexFor(T t) {
        return C_YLAINDEX.apply(t);
    }

    private static boolean isRoman(String vorming) {
        return V_ROOMA.equals(vorming);
    }

    // Do ALLOW for cases like ('1. peatükk' vs '1'), ('§ 1.' vs '1'), ...
    // Additionally ALLOW some ENGLISH translation specific prefixes, e..g 'Chapter 1' vs '1' etc
    private static final String SECTION_NR_PREFIXES_EN = "(Chapter|Part|Division|Subdivision|Sub-subdivision)";
    private static boolean consistentNumbering(String kuvatavNr, String xmlNr) {
        kuvatavNr = kuvatavNr.trim();

        String j2rgArvNr = "" + String.valueOf(xmlNr) + ".";
        if (kuvatavNr.startsWith(j2rgArvNr) || kuvatavNr.endsWith(j2rgArvNr))
            return true;

        String lgArvNr = "(" + String.valueOf(xmlNr) + ")";
        if (kuvatavNr.startsWith(lgArvNr) || kuvatavNr.endsWith(lgArvNr))
            return true;

        String punktNr = String.valueOf(xmlNr) + ")";
        if (kuvatavNr.equals(punktNr))
            return true;

        String nrENprefixed = SECTION_NR_PREFIXES_EN + "\\s+" + Pattern.quote(String.valueOf(xmlNr));
        if (kuvatavNr.matches(nrENprefixed))
            return true;

        // Be more lenient, English translators have been happy to mix and match Arabic and Roman numerals.
        if (xmlNr.matches("\\d+")) {
            Integer arabic = Integer.valueOf(xmlNr);
            String roman = RomanNumeralUtil.decimalToRoman(arabic);
            String romanNrENprefixed = SECTION_NR_PREFIXES_EN + "\\s+" + Pattern.quote(String.valueOf(roman));
            if (kuvatavNr.matches(romanNrENprefixed))
                return true;
        }

        return false;
    }

    /* In numbering department, when superscript is present, sometimes:
         '§ 6<sup>1</sup>.' vs  '6'
       and alike arise. In such case, we make sure that:
         a) numbers are equal when superscript is not considered
         b) if a) is true, extracted superscripts must be equal */
    private static boolean consistentSuplessNumbering(String kuvatavNr, String xmlNr, String xmlSup) {
        String suplessKuvaNr = kuvatavNr.trim();
        String expectedKuvaSup = "<sup>" + xmlSup + "</sup>";
        suplessKuvaNr = suplessKuvaNr.replaceAll(Pattern.quote(expectedKuvaSup), "").trim();

        if (Objects.equals(suplessKuvaNr, xmlNr))
            return true;

        return consistentNumbering(suplessKuvaNr, xmlNr);
    }

    /** Returns getKuvatavNr() method for given TypeID. */
    private static Function<Class<? extends TypeID>, Method> M_KUVATAV_NR = memoize(
        Unchecked.function(c -> c.getMethod("getKuvatavNr"))
    );

    /** Returns getVorming() method for given StringID. */
    private static Function<Class<? extends StringID>, Method> M_VORMING = memoize(
        Unchecked.function(c -> c.getMethod("getVorming"))
    );

    /** Returns getYlaIndeks() method for given StringID. */
    private static Function<Class<? extends StringID>, Method> M_YLAINDEX = memoize(
        Unchecked.function(c -> c.getMethod("getYlaIndeks"))
    );

    private static Function<TypeID, StringID> C_KUVATAV_NR =
        Unchecked.function(t -> (StringID) M_KUVATAV_NR.apply(t.getClass()).invoke(t));

    private static Function<StringID, String> C_VORMING =
        Unchecked.function(t -> (String) M_VORMING.apply(t.getClass()).invoke(t));

    private static Function<StringID, String> C_YLAINDEX =
        Unchecked.function(t -> (String) M_YLAINDEX.apply(t.getClass()).invoke(t));

    private static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return input -> cache.computeIfAbsent(input, function::apply);
    }

    private static final String V_ARAABIA = "araabia";
    private static final String V_ROOMA = "rooma";
}
