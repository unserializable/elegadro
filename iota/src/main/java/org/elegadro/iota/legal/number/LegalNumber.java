package org.elegadro.iota.legal.number;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.elegadro.iota.legal.number.util.DecimalArabicSupUtil;
import org.elegadro.iota.legal.number.util.RomanNumeralUtil;

import java.io.Serializable;

/**
 * @author Taimo Peelo
 */
@Getter
@EqualsAndHashCode
public final class LegalNumber implements Serializable, Comparable<LegalNumber> {
    public static final String LEGAL_NUMBER_KEY = "#";
    public static final String LEGAL_SUPER_KEY = "#^";
    public static final String LEGAL_ROMAN_KEY = "#R?";

    private final boolean isRoman;
    private final Integer num;
    // In laws:
    //  grep ylaIndeks= *.xml |   sed -e "s/\t//g" -re "s/.*ylaIndeks=\"([a-zA-Z0-9]*)\".*/\1/"  | sort -n | uniq
    // superscript seems always numeric or empty (NOT 'Roman'!). Not sure about other types of acts yet.
    private final String sup;

    public LegalNumber(Integer num, String sup) {
        this(false, num, sup);
    }

    public LegalNumber(boolean isRoman, Integer num, String sup) {
        this.isRoman = isRoman;
        this.num = num;
        this.sup = sup;
    }

    private void requireNullSup() {
        if (sup != null)
            throw new IllegalStateException("No num, but sup '" + sup + "'");
    }

    private String supToString() {
        return  (sup != null && !sup.isEmpty() ? DecimalArabicSupUtil.int2sup(Integer.valueOf(sup)) : "");
    }

    private String toArabicString() {
        if (num == null) {
            requireNullSup();
            return "";
        }
        return num + supToString();
    }

    private String toRomanString() {
        if (num == null) {
            requireNullSup();
            return "";
        }

        return RomanNumeralUtil.decimalToRoman(num) + supToString();
    }

    @Override
    public String toString() {
        return !isRoman ? toArabicString() : toRomanString();
    }

    @Override
    public int compareTo(LegalNumber o) {
        Integer oNum = o.getNum();
        if (this.num == null) {
            return (oNum == null) ? 0 : -1;
        } else if (oNum == null) {
            return -1;
        }

        int dn = this.num - oNum;
        if (dn != 0)
            return dn;

        String oSup = o.getSup();
        if (this.sup == null) {
            return (oSup == null) ? 0 : -1;
        } else if (oSup == null) {
            return 1;
        }

        return Integer.valueOf(this.sup) - Integer.valueOf(oSup);
    }

    public String toDebugString() {
        final StringBuilder sb = new StringBuilder("LegalNumber{");
        sb.append("isRoman=").append(isRoman);
        sb.append(", num=").append(num);
        sb.append(", sup='").append(sup).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
