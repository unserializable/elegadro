package org.elegadro.neo4j.util;

import org.elegadro.iota.legal.LawParticleEnum;
import org.elegadro.iota.legal.LegalMolecul;
import org.elegadro.iota.legal.LegalParticle;
import org.elegadro.iota.legal.impl.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;

import java.util.*;

import static org.elegadro.iota.legal.LawParticleEnum.*;

/**
 * @author Taimo Peelo
 */
public final class GraphPathUtil {
    private GraphPathUtil() {}

    /** Returns legal particle representation of Paths, deduplicating. */
    public static Set<LegalParticle> pathsToParticles(List<Path> paths) {
        Map<Node, LegalParticle> nodeToParticle = new LinkedHashMap<>(paths.size()*4);

        for (Path path: paths) {
            Node prev = null;
            for (Node node: path.nodes()) {
                if (!nodeToParticle.containsKey(node)) {
                    LegalParticle nodeAsParticle = nodeToLegalParticle(node);
                    nodeToParticle.put(node, nodeAsParticle);

                    if (prev != null) {
                        LegalMolecul molecul = (LegalMolecul) nodeToParticle.get(prev);
                        molecul.addLegalParticle(nodeAsParticle);
                    }
                }

                prev = node;
            }
        }

        Set<LegalParticle> result = new LinkedHashSet<>(paths.size());
        for (Path path: paths) {
            result.add(nodeToParticle.get(path.start()));
        }
        return result;
    }

    private static LegalParticle nodeToLegalParticle(org.neo4j.driver.v1.types.Node node) {
        LegalParticle result = null;
        Iterable<String> labels = node.labels();
        for (String label: labels) {
            try {
                LawParticleEnum lpe = LawParticleEnum.valueOf(label.toUpperCase());
                result = nodeToLegalParticle(lawParticleEnumClass(lpe), node);
            } catch (IllegalArgumentException iae) {
                continue;
            }
        }
        return result;
    }

    private static LegalParticle nodeToLegalParticle(Class<? extends LegalParticle> clazz, org.neo4j.driver.v1.types.Node node) {
        if (Punkt.class.equals(clazz))
            return nodeToPunkt(node);
        else if (Loige.class.equals(clazz))
            return nodeToLoige(node);
        else if (Paragrahv.class.equals(clazz)) {
            return nodeToParagrahv(node);
        } else if (AllJaotis.class.equals(clazz)) {
            return nodeToAllJaotis(node);
        } else if (Jaotis.class.equals(clazz)) {
            return nodeToJaotis(node);
        } else if (Jagu.class.equals(clazz)) {
            return nodeToJagu(node);
        } else if (Peatykk.class.equals(clazz)) {
            return nodeToPeatykk(node);
        } else if (Osa.class.equals(clazz)) {
            return nodeToOsa(node);
        } else if (Seadus.class.equals(clazz)) {
            return nodeToSeadus(node);
        } else
            throw new IllegalStateException("What clazz is " + clazz.getName() + " for node " + node.labels());
    }

    private static Seadus nodeToSeadus(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(SEADUS.name().toLowerCase());
        return new Seadus(node.get("text").asString());
    }

    private static Osa nodeToOsa(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(OSA.name().toLowerCase());
        return new Osa(node.get("text").asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Peatykk nodeToPeatykk(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(PEATYKK.name().toLowerCase());
        return new Peatykk(node.get("text").asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Jagu nodeToJagu(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(JAGU.name().toLowerCase());
        return new Jagu(node.get("text").asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Jaotis nodeToJaotis(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(JAOTIS.name().toLowerCase());
        return new Jaotis(node.get("text").asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static AllJaotis nodeToAllJaotis(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(ALLJAOTIS.name().toLowerCase());
        return new AllJaotis(node.get("text").asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Paragrahv nodeToParagrahv(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(PARAGRAHV.name().toLowerCase());
        return new Paragrahv(node.get("text").asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Loige nodeToLoige(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(LOIGE.name().toLowerCase());
        return new Loige(node.get("text").asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Punkt nodeToPunkt(org.neo4j.driver.v1.types.Node node) {
        assert node.hasLabel(PUNKT.name().toLowerCase());
        return new Punkt(node.get("text").asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static List<Map.Entry<String, Class<? extends LegalParticle>>> NODE_ENTITIES = Arrays.asList(
        lawParticleAsEntry(SEADUS),
        lawParticleAsEntry(OSA),
        lawParticleAsEntry(PEATYKK),
        lawParticleAsEntry(JAGU),
        lawParticleAsEntry(JAOTIS),
        lawParticleAsEntry(ALLJAOTIS),
        lawParticleAsEntry(PARAGRAHV),
        lawParticleAsEntry(LOIGE),
        lawParticleAsEntry(PUNKT)
    );

    private static Map.Entry<String, Class<? extends LegalParticle>> lawParticleAsEntry(LawParticleEnum lpe) {
        return Collections.<String, Class<? extends LegalParticle>>
            singletonMap(lpe.getLabel(), lawParticleEnumClass(lpe))
            .entrySet().iterator().next();
    }

    private static Class<? extends LegalParticle> lawParticleEnumClass(LawParticleEnum lpe) {
        switch (lpe) {
            case SEADUS:
                return Seadus.class;
            case OSA:
                return Osa.class;
            case PEATYKK:
                return Peatykk.class;
            case JAGU:
                return Jagu.class;
            case JAOTIS:
                return Jaotis.class;
            case ALLJAOTIS:
                return AllJaotis.class;
            case PARAGRAHV:
                return Paragrahv.class;
            case LOIGE:
                return Loige.class;
            case PUNKT:
                return Punkt.class;

            default:
                throw new IllegalStateException("Impossible enum value " + lpe);
        }
    }
}
