package org.elegadro.rt.legal.impl;

import org.elegadro.parser.rt.number.LegalNumber;
import org.elegadro.rt.legal.LawParticleEnum;

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
