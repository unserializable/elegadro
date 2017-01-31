package org.elegadro.rt.legal;

/**
 * @author Taimo Peelo
 */
public interface LegalMolecul extends LegalParticle {
    LegalMolecul addLegalParticle(LegalParticle legalParticle);
    Iterable<LegalParticle> getLegalParticles();
}
