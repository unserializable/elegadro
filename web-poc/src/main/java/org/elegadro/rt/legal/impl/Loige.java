package org.elegadro.rt.legal.impl;

import org.elegadro.parser.rt.number.LegalNumber;
import org.elegadro.rt.legal.LawParticleEnum;

/**
 * @author Taimo Peelo
 */
public class Loige extends AbstractLegalMolecul {
    public Loige(String legalText) {
        super(legalText);
    }

    public Loige(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    @Override
    public String getParticleName() {
        return LawParticleEnum.LOIGE.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Loige{");
        sb.append('(');
        sb.append(getParticleNumber());
        sb.append(')');
        sb.append(getLegalText());
        sb.append(childrenToString());
        sb.append('}');
        return sb.toString();
    }
}
