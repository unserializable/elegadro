package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LawParticleEnum;
import org.elegadro.iota.legal.number.LegalNumber;

/**
 * @author Taimo Peelo
 */
public class AllJaotis extends AbstractLegalMolecul {
    public AllJaotis(String legalText) {
        super(legalText);
    }

    public AllJaotis(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    @Override
    public String getParticleName() {
        return LawParticleEnum.ALLJAOTIS.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AllJaotis{");
        sb.append(getParticleNumber()).append("\n");
        sb.append(childrenToString());
        sb.append('}');
        return sb.toString();
    }
}
