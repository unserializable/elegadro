package org.elegadro.poc.web.riik.tree.display;

import org.elegadro.rt.legal.LegalParticle;

/**
 * @author Taimo Peelo
 */
public interface LegalTreeDisplay<T extends LegalParticle> {
    T getDisplayParticle();
}
