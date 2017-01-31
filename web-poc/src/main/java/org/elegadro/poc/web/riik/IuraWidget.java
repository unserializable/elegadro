package org.elegadro.poc.web.riik;

import org.elegadro.parser.util.rt.neo4j.GraphPathUtil;
import org.elegadro.rt.legal.LegalMolecul;
import org.elegadro.rt.legal.LegalParticle;
import org.elegadro.rt.legal.impl.Seadus;
import org.elegadro.rt.search.LawParagraphSearch;
import org.elegadro.rt.search.SearchUtil;
import org.elegadro.rt.service.law.RawIuraSearchService;
import org.elegadro.poc.web.base.BaseAppUIWidget;
import org.elegadro.poc.web.riik.tree.display.LegalMoleculDisplayWidget;
import org.elegadro.poc.web.riik.tree.display.LegalParticleDisplayWidget;
import org.elegadro.poc.web.riik.tree.display.LegalTreeDisplay;
import lombok.extern.slf4j.Slf4j;
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
import org.neo4j.driver.v1.types.Path;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Taimo Peelo
 */
@Slf4j
public class IuraWidget extends BaseAppUIWidget {
    @Autowired
    private RawIuraSearchService rawIuraSS;

    private FormWidget searchForm;

    @Override
    protected void init() throws Exception {
        setViewSelector("web/riik/iura");

        searchForm = new FormWidget();
        searchForm.addElement("s", "#Iura otsing", new TextControl(TextType.TEXT), new StringData(), null, false);

        addWidget("sf", searchForm);
        addEventListener("ds", new SearchListener());
    }

    private class SearchListener extends StandardEventListener {
        @Override
        public void processEvent(String eventId, String eventParam, InputData input) throws Exception {
            if (searchForm.convertAndValidate()) {
                String s = (String) searchForm.getValueByFullName("s");
                s = s.trim();

                log.debug(" search string was '" + s + "'");

                List<LawParagraphSearch> lawParagraphSearches = SearchUtil.toLawParagraphSearch(s);
                List<Path> resultPaths;
                if (!lawParagraphSearches.isEmpty()) {
                    log.debug("Concrete law paragraph search to be executed ");
                    if (log.isTraceEnabled()) {
                        log.trace(String.valueOf(lawParagraphSearches));
                    }

                    resultPaths = rawIuraSS.lawParagraphSearch(lawParagraphSearches);
                } else {
                    resultPaths = rawIuraSS.textSearch(s);
                }

                List<Seadus> matches = pathsAsLaw(resultPaths);
                // TODO: conditional (none if no matches)
                TreeWidget child = new TreeWidget(new SearchResultProvider(matches));
                child.setUseActions(true);
                child.setCollapsed(true);
                child.setRemoveChildrenOnCollapse(false);
                addWidget("srt", child);
            }
        }

        private List<Seadus> pathsAsLaw(List<Path> paths) {
            Set<?> legalParticles = GraphPathUtil.pathsToParticles(paths);
            for (Object lp: legalParticles) {
                if (!(lp instanceof Seadus))
                    throw new IllegalStateException();
            }

            return new ArrayList(legalParticles);
        }
    }

    private static class SearchResultProvider implements TreeDataProvider {
        private Collection<? extends LegalParticle> root;

        public SearchResultProvider(Collection<? extends LegalParticle> root) {
            this.root = root;
        }

        @Override
        public List getChildren(TreeNodeContext parent) {
            if (parent instanceof TreeWidget) // root!
                return root
                    .stream()
                    .map(SearchResultProvider::treeNodeWidgetFor)
                    .collect(Collectors.toCollection(() -> new ArrayList<>(root.size())));

            LegalTreeDisplay ldp = (LegalTreeDisplay) parent.getDisplayWidget();
            LegalParticle parentLegalMatter = ldp.getDisplayParticle();
            if (!(parentLegalMatter instanceof LegalMolecul))
                return null;

            LegalMolecul parentMolecul = (LegalMolecul) parentLegalMatter;

            return StreamSupport.stream(parentMolecul.getLegalParticles().spliterator(), false)
                .map(SearchResultProvider::treeNodeWidgetFor)
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

        private static TreeNodeWidget treeNodeWidgetFor(LegalParticle lp) {
            TreeNodeWidget treeNodeWidget = new TreeNodeWidget(displayWidgetFor(lp));
            return treeNodeWidget;
        }

        private static LegalParticleDisplayWidget displayWidgetFor(LegalParticle lp) {
            if (lp instanceof LegalMolecul)
                return new LegalMoleculDisplayWidget((LegalMolecul)lp);

            return new LegalParticleDisplayWidget(lp);
        }
    }
}
