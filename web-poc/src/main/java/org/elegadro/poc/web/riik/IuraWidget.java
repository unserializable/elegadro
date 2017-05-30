package org.elegadro.poc.web.riik;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.araneaframework.InputData;
import org.araneaframework.core.StandardEventListener;
import org.araneaframework.uilib.event.OnChangeEventListener;
import org.araneaframework.uilib.form.FormWidget;
import org.araneaframework.uilib.form.control.SelectControl;
import org.araneaframework.uilib.form.control.TextControl;
import org.araneaframework.uilib.form.data.StringData;
import org.araneaframework.uilib.support.DisplayItem;
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

    private static final String D_ET_ET="et_et", D_ET_EN="et_en", D_EN_EN="en_en", D_EN_ET="en_et";

    @Autowired
    private RawIuraSearchService rawIuraSS;

    private FormWidget searchForm;
    private String searchString;

    @Override
    protected void init() throws Exception {
        setViewSelector("web/riik/iura");

        searchForm = new FormWidget();
        SelectControl langDirSelect = buildSearchDirSelect();
        searchForm.addElement("sd", "#", langDirSelect, new StringData(), true);
        searchForm.addElement("s", "#", new TextControl(TextType.TEXT), new StringData(), null, false);

        langDirSelect.addOnChangeEventListener(new OnChangeEventListener() {
            @Override
            public void onChange() throws Exception {
                if (searchForm.getElementByFullName("sd").convertAndValidate()) {
                    String slangDir = (String) searchForm.getValueByFullName("sd");

                    String s = (String) searchForm.getValueByFullName("s");
                    if (s == null)
                        s = "";

                    s = s.trim();

                    log.debug("received onchange to... " + slangDir + " search string to use is " + s);

                    performSearch(s, slangDir);
                }
            }
        });

        addWidget("sf", searchForm);
        addEventListener("ds", new SearchListener());
    }

    private SelectControl buildSearchDirSelect() {
        SelectControl selectControl = new SelectControl();
        selectControl.addItem(new DisplayItem(D_ET_ET, "ET->ET"));
        selectControl.addItem(new DisplayItem(D_ET_EN, "ET->EN"));
        selectControl.addItem(new DisplayItem(D_EN_EN, "EN->EN"));
        selectControl.addItem(new DisplayItem(D_EN_ET, "EN->ET"));
        return selectControl;
    }

    public String getSearchString() {
        return StringEscapeUtils.escapeJavaScript(searchString);
    }

    private class SearchListener extends StandardEventListener {
        @Override
        public void processEvent(String eventId, String eventParam, InputData input) throws Exception {
            boolean conversionValid = searchForm.convertAndValidate();
            String s = null;
            if (conversionValid) {
                if (null == (s = (String) searchForm.getValueByFullName("s")))
                    s = "";

                s = s.trim();
                String slangDir = (String) searchForm.getValueByFullName("sd");
                if (log.isDebugEnabled()) {
                    log.debug(" search string was '" + s + "' search language direction " + slangDir);
                }

                performSearch(s, slangDir);
            }
        }
    }

    private void performSearch(String s, String langDir) {
        List<LawParagraphSearch> pgSearches = SearchUtil.toLawParagraphSearch(s);
        List<Path> resultPaths = pgSearches.isEmpty() ?
            pathsFromTextualSearch(s, langDir) : pathsFromPgSearch(pgSearches);
        List<Seadus> matches = pathsAsLaw(resultPaths, !pgSearches.isEmpty(), langDir);
        showSearchResults(matches, !pgSearches.isEmpty());
    }

    private List<Path> pathsFromPgSearch(List<LawParagraphSearch> lpSearches) {
        if (lpSearches.isEmpty())
            throw new IllegalStateException("Empty paragraph search provided.");

        searchString = null;
        log.debug("Concrete law paragraph search to be executed ");
        if (log.isTraceEnabled()) {
            log.trace(String.valueOf(lpSearches));
        }

        return rawIuraSS.lawParagraphSearch(lpSearches);
    }

    private List<Path> pathsFromTextualSearch(String s, String langDir) {
        searchString = s;
        return rawIuraSS.textSearch(s, langDir);
    }

    private void showSearchResults(List<Seadus> matches, boolean concretePgSearch) {
        if (matches.isEmpty()) {
            removeWidget("srt");
            return;
        }

        TreeWidget child = new TreeWidget(new SearchResultProvider(matches, concretePgSearch));
        child.setUseActions(true);
        child.setCollapsed(true);
        child.setRemoveChildrenOnCollapse(false);
        addWidget("srt", child);
    }

    private static List<Seadus> pathsAsLaw(List<Path> paths, boolean concretePgSearch, String langDir) {
        Set<?> legalParticles = GraphPathUtil.pathsToParticles(paths, langDir);
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
