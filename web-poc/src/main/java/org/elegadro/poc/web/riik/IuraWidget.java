package org.elegadro.poc.web.riik;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.araneaframework.InputData;
import org.araneaframework.core.StandardEventListener;
import org.araneaframework.uilib.form.FormWidget;
import org.araneaframework.uilib.form.control.TextControl;
import org.araneaframework.uilib.form.data.StringData;
import org.araneaframework.uilib.support.TextType;
import org.araneaframework.uilib.tree.TreeDataProvider;
import org.araneaframework.uilib.tree.TreeNodeContext;
import org.araneaframework.uilib.tree.TreeNodeWidget;
import org.araneaframework.uilib.tree.TreeWidget;
import org.elegadro.iota.legal.LegalMolecul;
import org.elegadro.iota.legal.LegalParticle;
import org.elegadro.iota.legal.impl.Seadus;
import org.elegadro.neo4j.util.GraphPathUtil;
import org.elegadro.poc.web.base.BaseAppUIWidget;
import org.elegadro.poc.web.riik.tree.display.LegalMoleculDisplayWidget;
import org.elegadro.poc.web.riik.tree.display.LegalParticleDisplayWidget;
import org.elegadro.poc.web.riik.tree.display.LegalTreeDisplay;
import org.elegadro.rt.search.LawParagraphSearch;
import org.elegadro.rt.search.SearchUtil;
import org.elegadro.rt.service.law.RawIuraSearchService;
import org.neo4j.driver.v1.types.Path;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Taimo Peelo
 */
@Slf4j
public class IuraWidget extends BaseAppUIWidget {
    private static final Comparator<Map.Entry<Seadus, Integer>>
        DESCENDING = Comparator.<Map.Entry<Seadus, Integer>>comparingInt(e -> e.getValue()).reversed();

    @Autowired
    private RawIuraSearchService rawIuraSS;

    private FormWidget searchForm;
    private String searchString;

    @Override
    protected void init() throws Exception {
        setViewSelector("web/riik/iura");

        searchForm = new FormWidget();
        searchForm.addElement("s", "#Iura otsing", new TextControl(TextType.TEXT), new StringData(), null, false);

        addWidget("sf", searchForm);
        addEventListener("ds", new SearchListener());
    }

    public String getSearchString() {
        return StringEscapeUtils.escapeJavaScript(searchString);
    }

    private class SearchListener extends StandardEventListener {
        @Override
        public void processEvent(String eventId, String eventParam, InputData input) throws Exception {
            if (searchForm.convertAndValidate()) {
                String s = (String) searchForm.getValueByFullName("s");
                s = s.trim();

                if (log.isDebugEnabled()) {
                    log.debug(" search string was '" + s + "'");
                }

                List<LawParagraphSearch> lawParagraphSearches = SearchUtil.toLawParagraphSearch(s);
                List<Path> resultPaths;
                if (!lawParagraphSearches.isEmpty()) {
                    searchString = null;
                    log.debug("Concrete law paragraph search to be executed ");
                    if (log.isTraceEnabled()) {
                        log.trace(String.valueOf(lawParagraphSearches));
                    }

                    resultPaths = rawIuraSS.lawParagraphSearch(lawParagraphSearches);
                } else {
                    searchString = s;
                    resultPaths = rawIuraSS.textSearch(s);
                }

                List<Seadus> matches = pathsAsLaw(resultPaths, !lawParagraphSearches.isEmpty());
                // TODO: conditional (none if no matches)
                TreeWidget child = new TreeWidget(new SearchResultProvider(matches, !lawParagraphSearches.isEmpty()));
                child.setUseActions(true);
                child.setCollapsed(true);
                child.setRemoveChildrenOnCollapse(false);
                addWidget("srt", child);
            }
        }

        private List<Seadus> pathsAsLaw(List<Path> paths, boolean concretePgSearch) {
            Set<?> legalParticles = GraphPathUtil.pathsToParticles(paths);
            for (Object lp: legalParticles) {
                if (!(lp instanceof Seadus))
                    throw new IllegalStateException();
            }

            if (concretePgSearch)
                return new ArrayList(legalParticles);

            List<Map.Entry<Seadus, Integer>> results = legalParticles.stream()
                .map(lp -> (Seadus) lp)
                .map(lp -> singletonMap(lp, lp.particleCount()).entrySet().iterator().next())
                .collect(toList());

            // sort the results so that laws with more matches for search are shown first
            results.sort(DESCENDING);

            return new ArrayList(results.stream().map(e -> e.getKey()).collect(toList()));
        }
    }

    private static class SearchResultProvider implements TreeDataProvider {
        private Collection<? extends LegalParticle> root;
        private boolean concretePgSearch;

        public SearchResultProvider(Collection<? extends LegalParticle> root, boolean concretePgSearch) {
            this.root = root;
            this.concretePgSearch = concretePgSearch;
        }

        @Override
        public List getChildren(TreeNodeContext parent) {
            if (parent instanceof TreeWidget) // root!
                return root
                    .stream()
                    .map(x -> SearchResultProvider.treeNodeWidgetFor(x, concretePgSearch, root.size() == 1))
                    .collect(Collectors.toCollection(() -> new ArrayList<>(root.size())));

            LegalTreeDisplay ldp = (LegalTreeDisplay) parent.getDisplayWidget();
            LegalParticle parentLegalMatter = ldp.getDisplayParticle();
            if (!(parentLegalMatter instanceof LegalMolecul))
                return null;

            LegalMolecul parentMolecul = (LegalMolecul) parentLegalMatter;

            return StreamSupport.stream(parentMolecul.getLegalParticles().spliterator(), false)
                .map(x -> SearchResultProvider.treeNodeWidgetFor(x, concretePgSearch, root.size() == 1))
                .collect(Collectors.toCollection(() -> new ArrayList<>()));
        }

        @Override
        public boolean hasChildren(TreeNodeContext parent) {
            if (parent == null)
                return !root.isEmpty();

            LegalTreeDisplay ldp = (LegalTreeDisplay) parent.getDisplayWidget();
            LegalParticle parentLegalMatter = ldp.getDisplayParticle();
            if (!(parentLegalMatter instanceof LegalMolecul))
                return false;

            LegalMolecul parentMolecul = (LegalMolecul) parentLegalMatter;

            return parentMolecul.getLegalParticles().iterator().hasNext();
        }

        private static TreeNodeWidget treeNodeWidgetFor(LegalParticle lp, boolean concretePgSearch, boolean singleResult) {
            TreeNodeWidget treeNodeWidget = new TreeNodeWidget(displayWidgetFor(lp)) {
                @Override
                protected void init() throws Exception {
                    super.init();
                    boolean hasChildren = (lp instanceof LegalMolecul && ((LegalMolecul) lp).getLegalParticles().iterator().hasNext());
                    boolean isSeadus = lp.getParticleName().charAt(0) == 'S';
                    boolean expanded = (hasChildren && (concretePgSearch || singleResult || !isSeadus));
                    setCollapsed(!expanded);
                }
            };
            return treeNodeWidget;
        }

        private static LegalParticleDisplayWidget displayWidgetFor(LegalParticle lp) {
            if (lp instanceof LegalMolecul)
                return new LegalMoleculDisplayWidget((LegalMolecul)lp);

            return new LegalParticleDisplayWidget(lp);
        }
    }
}
