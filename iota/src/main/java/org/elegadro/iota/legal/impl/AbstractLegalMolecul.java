package org.elegadro.iota.legal.impl;

import org.elegadro.iota.legal.LegalMolecul;
import org.elegadro.iota.legal.LegalParticle;
import org.elegadro.iota.legal.number.LegalNumber;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
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
        this(legalText, null, legalParticles);
    }

    public AbstractLegalMolecul(String legalText, LegalNumber particleNumber, List<LegalParticle> legalParticles) {
        super(legalText, particleNumber);
        this.legalParticles = makeParticleList(legalParticles);
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

    @Override
    public int particleCount() {
        int r = super.particleCount();
        if (legalParticles != null)
            r += legalParticles.stream().mapToInt(LegalParticle::particleCount).sum();
        return r;
    }

    public String childrenToString() {
        if (legalParticles == null)
            return "";
        return legalParticles.stream().map(p -> p.toString()).collect(Collectors.joining("\n"));
    }

    protected Set<LegalParticle> makeParticleList() {
        return new TreeSet<>(Comparator.comparing(new ComparatorKeyExtractor()));
    }

    protected Set<LegalParticle> makeParticleList(List<LegalParticle> legalParticles) {
        Set<LegalParticle> result = makeParticleList();
        result.addAll(legalParticles);
        return result;
    }

    protected static class ComparatorKeyExtractor implements Function<LegalParticle, LegalNumber>, Serializable {
        public LegalNumber apply(LegalParticle legalParticle) {
            return legalParticle.getParticleNumber();
        }
    }
}
