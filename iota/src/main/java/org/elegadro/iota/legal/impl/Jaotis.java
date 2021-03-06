package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LawParticleEnum;
import org.elegadro.iota.legal.number.LegalNumber;

/**
 * @author Taimo Peelo
 */
public class Jaotis extends AbstractLegalMolecul {
    public Jaotis(String legalText) {
        super(legalText);
    }

    public Jaotis(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    @Override
    public String getParticleName() {
        return LawParticleEnum.JAOTIS.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Jaotis{");
        sb.append(getParticleNumber()).append("\n");
        sb.append(childrenToString());
        sb.append('}');
        return sb.toString();
    }
}
