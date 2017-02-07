package org.elegadro.rt.search;

import lombok.Getter;
import org.elegadro.iota.rt.actronym.Actronym;

/**
 * @author Taimo Peelo
 */
@Getter
public class LawParagraphSearch {
    private Actronym ae;
    private int numStart;
    private Integer numEnd;

    public LawParagraphSearch(Actronym ae, int numStart) {
        this.ae = ae;
        this.numStart = numStart;
    }

    public LawParagraphSearch(Actronym ae, int numStart, Integer numEnd) {
        this.ae = ae;
        if (numEnd != null) {
            this.numStart = numStart < numEnd ? numStart : numEnd;
            this.numEnd = numStart < numEnd ? numEnd : numStart;
        }
    }

    /** Returns true if this is paragraph search. */
    public boolean isRange() {
        return numEnd != null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConcreteSearch{");
        sb.append("ae=").append(ae).append(":").append(ae.getExpanym());
        sb.append(", numStart=").append(numStart);
        sb.append(", numEnd=").append(numEnd);
        sb.append('}');
        return sb.toString();
    }
}
