package org.elegadro.iota.legal.number;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.elegadro.iota.legal.number.util.DecimalArabicSupUtil;
import org.elegadro.iota.legal.number.util.RomanNumeralUtil;

import java.io.Serializable;

/**
 * NB! This class has a natural ordering that is inconsistent with equals!
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
    private final boolean unexpired;

    public LegalNumber(Integer num, String sup, boolean unexpired) {
        this(false, num, sup, unexpired);
    }

    public LegalNumber(boolean isRoman, Integer num, String sup, boolean unexpired) {
        this.isRoman = isRoman;
        this.num = num;
        this.sup = sup;
        this.unexpired = unexpired;
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
        if (o == null) {
            return 1;
        }

        Integer oNum = o.getNum();
        if (this.num == null) {
            return (oNum == null) ? 0 : -1;
        } else if (oNum == null) {
            return 1;
        }

        int dn = this.num - oNum;
        if (dn != 0)
            return dn;

        String thisSup = this.sup;
        if (thisSup != null && thisSup.isEmpty())
            thisSup = null;

        String oSup = o.getSup();
        if (oSup != null && oSup.isEmpty())
            oSup = null;
        if (thisSup == null) {
            return (oSup == null) ? 0 : -1;
        } else if (oSup == null) {
            return 1;
        }

        return Integer.valueOf(this.sup) - Integer.valueOf(oSup);
    }

    public String toDebugString() {
        final StringBuilder sb = new StringBuilder("LegalNumber{");
        sb.append("isRoman=").append(isRoman ? 'T' : 'F');
        sb.append(", unexpired=").append(unexpired ? 'T' : 'F');
        sb.append(", num=").append(num);
        sb.append(", sup=");
        if (sup == null)
            sb.append("null");
        else
            sb.append('\'').append(sup).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
