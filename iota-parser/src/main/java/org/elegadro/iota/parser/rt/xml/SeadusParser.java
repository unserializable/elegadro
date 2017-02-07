package org.elegadro.iota.parser.rt.xml;

import _2010._02.juurakt_1_10.DepthFirstTraverserImpl;
import _2010._02.juurakt_1_10.TraversingVisitor;
import _2010._02.tyviseadus_1_10.Oigusakt;
import org.elegadro.iota.parser.Parser;
import org.elegadro.iota.parser.rt.xml.visitor.SeadusVisitor;
import org.elegadro.iota.legal.LegalParticle;
import org.elegadro.iota.legal.impl.Seadus;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @author Taimo Peelo
 */
@Slf4j
public class SeadusParser extends Parser<_2010._02.tyviseadus_1_10.Oigusakt, Seadus> {
    public SeadusParser(Oigusakt parseSource) {
        super(parseSource);
    }

    @Override
    public Optional<Seadus> parse() {
        try {
            SeadusVisitor seadusVisitor = new SeadusVisitor();

            TraversingVisitor<LegalParticle, RuntimeException> traverser =
                    new TraversingVisitor<>(new DepthFirstTraverserImpl<>(), seadusVisitor);
            traverser.setProgressMonitor(seadusVisitor);
            traverser.setTraverseFirst(false);

            Seadus result = (Seadus) parseSource.accept(traverser);
            return Optional.of(result);
        } catch (Exception ex) {
            log.warn("Unparsed ... ", ex);
            return Optional.empty();
        }
    }
}
