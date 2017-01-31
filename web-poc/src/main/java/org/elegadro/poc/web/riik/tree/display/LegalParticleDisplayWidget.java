package org.elegadro.poc.web.riik.tree.display;

import org.elegadro.rt.legal.LegalParticle;
import org.elegadro.poc.web.base.BaseAppUIWidget;

/**
 * @author Taimo Peelo
 */
public class LegalParticleDisplayWidget<T extends LegalParticle> extends BaseAppUIWidget implements LegalTreeDisplay<T> {
    protected T displayParticle;

    public LegalParticleDisplayWidget(T displayParticle) {
        this.displayParticle = displayParticle;
    }

    @Override
    public T getDisplayParticle() {
        return displayParticle;
    }

    @Override
    protected void init() throws Exception {
        setViewSelector("web/riik/legal/tree/legalParticle");
    }
}
