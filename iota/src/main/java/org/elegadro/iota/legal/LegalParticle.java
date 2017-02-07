package org.elegadro.iota.legal;

import org.elegadro.iota.legal.number.LegalNumber;

import java.io.Serializable;

/**
 * @author Taimo Peelo
 */
public interface LegalParticle extends Serializable {
    LegalNumber getParticleNumber();
    String getLegalText();
    String getParticleName();
    default int particleCount() { return 1; };
}
