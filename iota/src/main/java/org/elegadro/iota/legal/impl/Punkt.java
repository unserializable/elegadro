package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LawParticleEnum;
import org.elegadro.iota.legal.number.LegalNumber;

/**
 * @author Taimo Peelo
 */
public class Punkt extends AbstractLegalParticle {
    public Punkt(String legalText) {
        super(legalText);
    }

    public Punkt(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    @Override
    public String getParticleName() {
        return LawParticleEnum.PUNKT.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Punkt{")
            .append(getParticleNumber())
            .append(") ")
            .append(getLegalText())
            .append('}');
        return sb.toString();
    }
}
