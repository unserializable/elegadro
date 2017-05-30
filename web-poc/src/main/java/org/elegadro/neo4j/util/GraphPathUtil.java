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
    public static Set<LegalParticle> pathsToParticles(List<Path> paths, String langDir) {
        Map<Node, LegalParticle> nodeToParticle = new LinkedHashMap<>(paths.size()*4);

        for (Path path: paths) {
            Node prev = null;
            for (Node node: path.nodes()) {
                if (!nodeToParticle.containsKey(node)) {
                    LegalParticle nodeAsParticle = nodeToLegalParticle(node, langDir);
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

    private static LegalParticle nodeToLegalParticle(org.neo4j.driver.v1.types.Node node, String langDir) {
        LegalParticle result = null;
        Iterable<String> labels = node.labels();
        for (String label: labels) {
            try {
                LawParticleEnum lpe = LawParticleEnum.valueOf(label.toUpperCase());
                result = nodeToLegalParticle(lawParticleEnumClass(lpe), node, langDir);
            } catch (IllegalArgumentException iae) {
                continue;
            }
        }
        return result;
    }

    private static LegalParticle nodeToLegalParticle(Class<? extends LegalParticle> clazz, org.neo4j.driver.v1.types.Node node, String langDir) {
        String lang = langDir.split("_")[1];
        if (Punkt.class.equals(clazz))
            return nodeToPunkt(node, lang);
        else if (Loige.class.equals(clazz))
            return nodeToLoige(node, lang);
        else if (Paragrahv.class.equals(clazz)) {
            return nodeToParagrahv(node, lang);
        } else if (AllJaotis.class.equals(clazz)) {
            return nodeToAllJaotis(node, lang);
        } else if (Jaotis.class.equals(clazz)) {
            return nodeToJaotis(node, lang);
        } else if (Jagu.class.equals(clazz)) {
            return nodeToJagu(node, lang);
        } else if (Peatykk.class.equals(clazz)) {
            return nodeToPeatykk(node, lang);
        } else if (Osa.class.equals(clazz)) {
            return nodeToOsa(node, lang);
        } else if (Seadus.class.equals(clazz)) {
            return nodeToSeadus(node, lang);
        } else
            throw new IllegalStateException("What clazz is " + clazz.getName() + " for node " + node.labels());
    }

    private static Seadus nodeToSeadus(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(SEADUS.name().toLowerCase());
        String property = "tr_" + lang;
        return new Seadus(node.get(property).asString());
    }

    private static Osa nodeToOsa(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(OSA.name().toLowerCase());
        String property = "tr_" + lang;
        return new Osa(node.get(property).asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Peatykk nodeToPeatykk(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(PEATYKK.name().toLowerCase());
        String property = "tr_" + lang;
        return new Peatykk(node.get(property).asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Jagu nodeToJagu(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(JAGU.name().toLowerCase());
        String property = "tr_" + lang;
        return new Jagu(node.get(property).asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Jaotis nodeToJaotis(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(JAOTIS.name().toLowerCase());
        String property = "tr_" + lang;
        return new Jaotis(node.get(property).asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static AllJaotis nodeToAllJaotis(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(ALLJAOTIS.name().toLowerCase());
        String property = "tr_" + lang;
        return new AllJaotis(node.get(property).asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Paragrahv nodeToParagrahv(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(PARAGRAHV.name().toLowerCase());
        String property = "tr_" + lang;
        return new Paragrahv(node.get(property).asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Loige nodeToLoige(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(LOIGE.name().toLowerCase());
        String property = "tr_" + lang;
        return new Loige(node.get(property).asString(), GraphNodeUtil.legalNumberFromNode(node));
    }

    private static Punkt nodeToPunkt(org.neo4j.driver.v1.types.Node node, String lang) {
        assert node.hasLabel(PUNKT.name().toLowerCase());
        String property = "tr_" + lang;
        return new Punkt(node.get(property).asString(), GraphNodeUtil.legalNumberFromNode(node));
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
