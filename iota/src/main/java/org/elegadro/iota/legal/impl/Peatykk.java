package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LawParticleEnum;
import org.elegadro.iota.legal.number.LegalNumber;

/**
 * @author Taimo Peelo
 */
public class Peatykk extends AbstractLegalMolecul {
    public Peatykk(String legalText) {
        super(legalText);
    }

    public Peatykk(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    @Override
    public String getParticleName() {
        return LawParticleEnum.PEATYKK.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb =
            new StringBuilder(String.valueOf(getParticleNumber())).append(" Peatykk{");
        sb.append(' ');
        sb.append(getLegalText());
        sb.append(childrenToString());
        sb.append('}');
        return sb.toString();
    }
}
