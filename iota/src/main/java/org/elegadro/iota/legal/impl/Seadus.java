package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LawParticleEnum;

/**
 * @author Taimo Peelo
 */
public class Seadus extends AbstractLegalMolecul {
    public Seadus(String legalText) {
        super(legalText);
    }

    @Override
    public String getParticleName() {
        return LawParticleEnum.SEADUS.getLabel();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Seadus{");
        sb.append(getLegalText());
        sb.append(' ');
        sb.append(childrenToString());
        sb.append('}');
        return sb.toString();
    }
}
