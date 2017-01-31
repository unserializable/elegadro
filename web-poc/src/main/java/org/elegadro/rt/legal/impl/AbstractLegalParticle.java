package org.elegadro.rt.legal.impl;

import org.elegadro.parser.rt.number.LegalNumber;
import org.elegadro.rt.legal.LegalParticle;

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
