package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LegalParticle;
import org.elegadro.iota.legal.number.LegalNumber;

/**
 * @author Taimo Peelo
 */
public abstract class AbstractLegalParticle implements LegalParticle {
    private final String legalText;
    private final LegalNumber particleNumber;

    public AbstractLegalParticle(String legalText) {
        this(legalText, null);
    }

    public AbstractLegalParticle(String legalText, LegalNumber particleNumber) {
        this.legalText = legalText;
        this.particleNumber = particleNumber;
    }

    @Override
    public String getLegalText() {
        return legalText;
    }

    @Override
    public LegalNumber getParticleNumber() {
        return particleNumber;
    }
}
