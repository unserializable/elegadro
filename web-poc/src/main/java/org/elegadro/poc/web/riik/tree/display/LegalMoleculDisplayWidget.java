package org.elegadro.poc.web.riik.tree.display;

import org.elegadro.rt.legal.LegalMolecul;

/**
 * @author Taimo Peelo
 */
public class LegalMoleculDisplayWidget extends LegalParticleDisplayWidget<LegalMolecul> {
    public LegalMoleculDisplayWidget(LegalMolecul displayMolecul) {
        super(displayMolecul);
    }

    @Override
    protected void init() throws Exception {
        setViewSelector("web/riik/legal/tree/legalMolecul");
    }
}
