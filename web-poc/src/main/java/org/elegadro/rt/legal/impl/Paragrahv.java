package org.elegadro.rt.legal.impl;

import org.elegadro.parser.rt.number.LegalNumber;
import org.elegadro.rt.legal.LawParticleEnum;

/**
 * @author Taimo Peelo
 */
public class Paragrahv extends AbstractLegalMolecul {
    public Paragrahv(String legalText) {
        super(legalText);
    }

    public Paragrahv(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    @Override
    public String getParticleName() {
        return LawParticleEnum.PARAGRAHV.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("§{");
        sb.append(getParticleNumber());
        sb.append(' ');
        sb.append(getLegalText());
        sb.append(' ');
        sb.append(childrenToString());
        sb.append('}');
        return sb.toString();
    }
}
