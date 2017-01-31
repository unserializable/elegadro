package org.elegadro.rt.legal;

import org.elegadro.parser.rt.number.LegalNumber;

import java.io.Serializable;

/**
 * @author Taimo Peelo
 */
public interface LegalParticle extends Serializable {
    LegalNumber getParticleNumber();
    String getLegalText();
    String getParticleName();
}
