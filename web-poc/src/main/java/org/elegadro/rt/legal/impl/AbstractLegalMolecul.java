package org.elegadro.rt.legal.impl;

import org.elegadro.parser.rt.number.LegalNumber;
import org.elegadro.rt.legal.LegalMolecul;
import org.elegadro.rt.legal.LegalParticle;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;

/**
 * @author Taimo Peelo
 */
abstract class AbstractLegalMolecul extends AbstractLegalParticle implements LegalMolecul {
    protected Set<LegalParticle> legalParticles = null;

    public AbstractLegalMolecul(String legalText) {
        super(legalText);
    }

    public AbstractLegalMolecul(String legalText, LegalNumber particleNumber) {
        super(legalText, particleNumber);
    }

    public AbstractLegalMolecul(String legalText, List<LegalParticle> legalParticles) {
        super(legalText);
        this.legalParticles = new LinkedHashSet<>(legalParticles);
    }

    public AbstractLegalMolecul(String legalText, LegalNumber particleNumber, List<LegalParticle> legalParticles) {
        super(legalText, particleNumber);
        this.legalParticles = new LinkedHashSet<>(legalParticles);
    }

    @Override
    public LegalMolecul addLegalParticle(LegalParticle legalParticle) {
        if (legalParticles == null) {
            legalParticles = makeParticleList();
        }
        legalParticles.add(legalParticle);

        return this;
    }

    @Override
    public Iterable<LegalParticle> getLegalParticles() {
        return legalParticles != null ? unmodifiableSet(legalParticles) : emptyList();
    }

    protected Set<LegalParticle> makeParticleList() {
        return new LinkedHashSet<>();
    }

    public String childrenToString() {
        if (legalParticles == null)
            return "";
        return legalParticles.stream().map(p -> p.toString()).collect(Collectors.joining("\n"));
    }
}
