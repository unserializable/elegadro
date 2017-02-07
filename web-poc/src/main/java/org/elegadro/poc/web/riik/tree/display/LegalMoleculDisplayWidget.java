package org.elegadro.poc.web.riik.tree.display;

import org.araneaframework.OutputData;
import org.araneaframework.http.util.ServletUtil;
import org.elegadro.iota.legal.LegalMolecul;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @author Taimo Peelo
 */
public class LegalMoleculDisplayWidget extends LegalParticleDisplayWidget<LegalMolecul> {
    public LegalMoleculDisplayWidget(LegalMolecul displayMolecul) {
        super(displayMolecul);
    }

    @Override
    protected void render(OutputData output) throws Exception {
        HttpServletResponse response = ServletUtil.getResponse(output);
        PrintWriter wr = response.getWriter();
        LegalMolecul m = this.getDisplayParticle();
        String pn = m.getParticleName();

        if (pn.charAt(0) == 'L') { // Loige
            boolean hasNum = m.getParticleNumber().getNum() != null;
            if (hasNum) wr.write("(");
            if (hasNum) {
                wr.write(m.getParticleNumber().toString());

            }
            if (hasNum) wr.write(") ");
            wr.write(m.getLegalText());
            return;
        } else if (pn.startsWith("Pa")) { // Paragrahv
            wr.write("§ ");
            wr.write(m.getParticleNumber().toString());
            wr.write(" ");
            wr.write(m.getLegalText());
            return;
        } else if (pn.charAt(0) == 'O') { // Osa
            wr.write(m.getParticleNumber().toString());
            wr.write(". OSA ");
            wr.write(m.getLegalText());
            return;
        } else if (pn.charAt(0) == 'S') {
            wr.write("<b>");
            wr.write(m.getLegalText());
            wr.write("</b>");
            return;
        }

        wr.write(m.getParticleNumber().toString());
        wr.write(". ");
        if (m.getParticleName().charAt(0) != 'P')
            wr.write(m.getParticleName().toLowerCase());
        else
            wr.write("peatükk");
        wr.write(" ");
        wr.write(m.getLegalText());
    }
}
