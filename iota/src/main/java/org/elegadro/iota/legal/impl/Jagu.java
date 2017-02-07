package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LawParticleEnum;
import org.elegadro.iota.legal.number.LegalNumber;

/**
 * @author Taimo Peelo
 */
public class Jagu extends AbstractLegalMolecul {
    public Jagu(String legalText) {
        super(legalText);
    }

    public Jagu(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    public String getParticleName() {
        return LawParticleEnum.JAGU.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(String.valueOf(getParticleNumber())).append(" Jagu{");
        sb.append(childrenToString());
        sb.append('}');
        return sb.toString();
    }
}
