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
    default boolean isInForce() {
        LegalNumber pNr = getParticleNumber();
        /* Numberless particles ATM the top level ones which by pure existence are unexpired. */
        return (pNr != null) ? pNr.isUnexpired() : true;
    }
}
