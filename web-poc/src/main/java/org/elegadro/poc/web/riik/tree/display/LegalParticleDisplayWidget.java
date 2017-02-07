package org.elegadro.poc.web.riik.tree.display;

import org.araneaframework.OutputData;
import org.araneaframework.http.util.ServletUtil;
import org.elegadro.iota.legal.LegalParticle;
import org.elegadro.poc.web.base.BaseAppUIWidget;

import javax.servlet.http.HttpServletResponse;

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
    protected void render(OutputData output) throws Exception {
        HttpServletResponse response = ServletUtil.getResponse(output);
        LegalParticle p = this.getDisplayParticle();
        response.getWriter().write(p.getParticleNumber() + ") " + p.getLegalText());
    }
}
