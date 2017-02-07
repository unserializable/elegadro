package org.elegadro.iota.parser.rt.xml.visitor;

import _2010._02.juurakt_1_10.BaseVisitor;
import _2010._02.juurakt_1_10.TraversingVisitorProgressMonitor;
import _2010._02.juurakt_1_10.Visitable;
import _2010._02.tyviseadus_1_10.Aktinimi;
import _2010._02.tyviseadus_1_10.ParagrahvType;
import _2010._02.tyviseadus_1_10.PealkiriType;
import _2010._02.tyviseadus_1_10.TavatekstID;
import org.elegadro.iota.parser.rt.util.LawParseUtil;
import org.elegadro.iota.legal.LegalMolecul;
import org.elegadro.iota.legal.LegalParticle;
import org.elegadro.iota.legal.impl.*;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static org.elegadro.iota.parser.rt.util.LegalNumberUtil.legalNumberFor;

/**
 * @author Taimo Peelo
 */
public class SeadusVisitor extends BaseVisitor<LegalParticle, RuntimeException> implements TraversingVisitorProgressMonitor {
    private Deque<LegalParticle> particleStack = new LinkedList<>();

    private Function<Class<? extends Visitable>, Boolean> hasOwnV4clazz = memoize(
        (clazz) -> clazzHasOwnVisitForVisitableClazz(this.getClass(), clazz)
    );

    @Override // TraversingVisitorProgressMonitor
    public void visited(Visitable visitable) {
        // do nothing, Particle corresponding to node is put into stack from visit
    }

    @Override // TraversingVisitorProgressMonitor
    public void traversed(Visitable visitable) {
        if (hasOwnVisitFor(visitable))
            particleStack.pop();
    }

    @Override
    public Seadus visit(_2010._02.tyviseadus_1_10.Oigusakt oigusakt) {
        StringBuilder sb = new StringBuilder();
        Aktinimi aktinimi = oigusakt.getAktinimi();

        PealkiriType pealkiriType = aktinimi != null ? aktinimi.getNimi() : null;
        if (pealkiriType != null) {
            TavatekstID pealkiri = pealkiriType.getPealkiri();
            sb.append(LawParseUtil.tavatekstIDToString(pealkiri));
        }

        return integrate(new Seadus(sb.toString()));
    }

    @Override
    public Osa visit(_2010._02.tyviseadus_1_10.OsaType osaType) throws RuntimeException {
        TavatekstID osaPealkiri = osaType.getOsaPealkiri();
        StringBuilder sb = new StringBuilder()
            .append(LawParseUtil.tavatekstIDToString(osaPealkiri));

        return integrate(new Osa(sb.toString(), legalNumberFor(osaType)));
    }

    @Override
    public Peatykk visit(_2010._02.tyviseadus_1_10.PeatykkType peatykkType) throws RuntimeException {
        TavatekstID peatykkPealkiri = peatykkType.getPeatykkPealkiri();
        StringBuilder sb = new StringBuilder()
            .append(LawParseUtil.tavatekstIDToString(peatykkPealkiri));

        return integrate(new Peatykk(sb.toString(), legalNumberFor(peatykkType)));
    }

    @Override
    public Jagu visit(_2010._02.tyviseadus_1_10.JaguType jaguType) throws RuntimeException {
        StringBuilder sb = new StringBuilder()
            .append(LawParseUtil.tavatekstIDToString(jaguType.getJaguPealkiri()));

        return integrate(new Jagu(sb.toString(), legalNumberFor(jaguType)));
    }

    @Override
    public Jaotis visit(_2010._02.tyviseadus_1_10.JaotisType jaotisType) throws RuntimeException {
        StringBuilder sb = new StringBuilder()
            .append(jaotisType.getJaotisPealkiri().getContent());

        return integrate(new Jaotis(sb.toString(), legalNumberFor(jaotisType)));
    }

    @Override
    public AllJaotis visit(_2010._02.tyviseadus_1_10.AlljaotisType alljaotisType) throws RuntimeException {
        StringBuilder sb = new StringBuilder()
            .append(LawParseUtil.tavatekstIDToString(alljaotisType.getAlljaotisPealkiri()));

        return integrate(new AllJaotis(sb.toString(), legalNumberFor(alljaotisType)));
    }

    @Override
    public Paragrahv visit(ParagrahvType paragrahvType) throws RuntimeException {
        StringBuilder sb = new StringBuilder();
        // pealkiri can be missing, e.g for cancelled paragrahvs like VÕS 666
        if (paragrahvType.getParagrahvPealkiri() != null) {
            TavatekstID paragrahvPealkiri = paragrahvType.getParagrahvPealkiri();
            sb.append(LawParseUtil.tavatekstIDToString(paragrahvPealkiri));
        }

        return integrate(new Paragrahv(sb.toString(), legalNumberFor(paragrahvType)));
    }

    @Override
    public Loige visit(_2010._02.tyviseadus_1_10.LoigeType loigeType) throws RuntimeException {
        StringBuilder sb = new StringBuilder();
        loigeType.getSisuTekst().forEach(tt -> sb.append(LawParseUtil.textTypeToString(tt)));

        return integrate(new Loige(sb.toString(), legalNumberFor(loigeType)));
    }

    @Override
    public Punkt visit(_2010._02.tyviseadus_1_10.AlampunktType punktType) throws RuntimeException {
        StringBuilder sb = new StringBuilder();

        punktType.getSisuTekst().stream().forEach(tt ->
            sb.append(LawParseUtil.textTypeToString(tt))
        );

        return integrate(new Punkt(sb.toString(), legalNumberFor(punktType)));
    }

    // ***********************************************************************
    // INNER PRIVATES
    // ***********************************************************************
    private boolean hasOwnVisitFor(Visitable visitable) {
        return hasOwnV4clazz.apply(visitable.getClass());
    }

    private LegalMolecul predecessor() {
        return (LegalMolecul) particleStack.peek();
    }

    private <T extends LegalParticle> T integrate(T particle) {
        LegalMolecul predecessor = predecessor();
        if (predecessor != null)
            predecessor.addLegalParticle(particle);
        particleStack.push(particle);
        return particle;
    }

    private static boolean clazzHasOwnVisitForVisitableClazz(Class clazz, Class<? extends Visitable> visitableClazz) {
        try {
            clazz.getDeclaredMethod("visit", visitableClazz);
        } catch (NoSuchMethodException e) {
            return false;
        }

        return true;
    }

    private static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        Map<T, R> cache = new ConcurrentHashMap<>();
        return input -> cache.computeIfAbsent(input, function::apply);
    }
}
