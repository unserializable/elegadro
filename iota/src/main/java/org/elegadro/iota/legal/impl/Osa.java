package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LawParticleEnum;
import org.elegadro.iota.legal.number.LegalNumber;

/**
 * @author Taimo Peelo
 */
public class Osa extends AbstractLegalMolecul {
    public Osa(String legalText) {
        super(legalText);
    }

    public Osa(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    @Override
    public String getParticleName() {
        return LawParticleEnum.OSA.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(String.valueOf(getParticleNumber())).append(" Osa{");
        sb.append(childrenToString());
        sb.append('}');
        return sb.toString();
    }
}
