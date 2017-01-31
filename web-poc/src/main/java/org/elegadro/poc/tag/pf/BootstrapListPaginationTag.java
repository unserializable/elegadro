package org.elegadro.poc.tag.pf;

import org.araneaframework.jsp.UiEvent;
import org.araneaframework.jsp.UiUpdateEvent;
import org.araneaframework.jsp.tag.PresentationTag;
import org.araneaframework.jsp.tag.uilib.list.ListTag;
import org.araneaframework.jsp.util.JspScriptUtil;
import org.araneaframework.jsp.util.JspUpdateRegionUtil;
import org.araneaframework.jsp.util.JspUtil;
import org.araneaframework.jsp.util.JspWidgetCallUtil;
import org.araneaframework.uilib.list.ListWidget;
import org.araneaframework.uilib.list.SequenceHelper;
import org.araneaframework.uilib.util.MessageUtil;
import org.springframework.util.StringUtils;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Tag for list header, very closely c/p from Aranea Framework 1.2.2 release examples ComponentListFooterTag class!
 *
 * @author Taimo Peelo
 */
public class BootstrapListPaginationTag extends PresentationTag {
    public final static String PREVIOUS_PAGE_EVENT_ID = "previousPage";
    public final static String NEXT_PAGE_EVENT_ID = "nextPage";
    public final static String FIRST_PAGE_EVENT_ID = "firstPage";
    public final static String LAST_PAGE_EVENT_ID = "lastPage";
    public final static String JUMP_TO_PAGE_EVENT_ID = "jumpToPage";

    public final static String DEFAULT_NO_DATA_STRING_ID = "list.empty.recordset";

    protected String listId;

    protected String numberStyleClass = "nr";
    protected String infoStyleClass = "info";
    protected String firstClass = "first";
    protected String prevClass = "prev";
    protected String nextClass = "next";
    protected String lastClass = "last";

    protected String noDataStringId = DEFAULT_NO_DATA_STRING_ID;

    // update regions
    private String updateRegions;
    private String globalUpdateRegions;

    private List updateRegionNames;

    public BootstrapListPaginationTag() {
        styleClass = "pagination";
    }

    public int doStartTag(Writer out) throws Exception {
        super.doStartTag(out);

        this.updateRegionNames = JspUpdateRegionUtil.getUpdateRegionNames(pageContext, updateRegions, globalUpdateRegions);

        // Get list data
        listId = (String)requireContextEntry(ListTag.LIST_FULL_ID_KEY);
        ListWidget.ViewModel viewModel = (ListWidget.ViewModel)requireContextEntry(ListTag.LIST_VIEW_MODEL_KEY);

        // Get sequnce data
        SequenceHelper.ViewModel sequenceViewModel = viewModel.getSequence();

        Encapsulated __ = new Encapsulated(sequenceViewModel);

        JspUtil.writeOpenStartTag(out, "ul");
        JspUtil.writeAttribute(out, "class", getStyleClass());
        JspUtil.writeAttribute(out, "style", getStyle());
        JspUtil.writeCloseStartTag(out);

        if (__.totalItemCount > 0 && !__.allItemsShown) {
            writePagination(out, __);
        } else {
            writeItemlessPagination(out, __);
        }

        return EVAL_BODY_INCLUDE;
    }


    @Override
    protected int doEndTag(Writer out) throws Exception {
        JspUtil.writeEndTag(out, "ul");
        return super.doEndTag(out);
    }

    private void writePagination(Writer out, Encapsulated __) throws Exception {
        boolean jumpToFirstEnabled = __.firstPage != __.currentPage;
        writeStartPageLI(out, false, !jumpToFirstEnabled);
        writeOpenEventLink(out, FIRST_PAGE_EVENT_ID, null, jumpToFirstEnabled, firstClass);
        out.write("&laquo;&nbsp;");
        JspUtil.writeEndTag_SS(out, "a");
        writeEndPageLI(out);

        boolean jumpToPrevEnabled = __.firstPage < __.currentPage;
        writeStartPageLI(out, false, !jumpToPrevEnabled);
        writeOpenEventLink(out, PREVIOUS_PAGE_EVENT_ID, null, jumpToPrevEnabled, prevClass);
        out.write("&lt;&nbsp;");
        JspUtil.writeEndTag_SS(out, "a");
        writeEndPageLI(out);

        for(long page = __.blockFirstPage; page  <= __.blockLastPage; page++) {
            boolean activePage = page == __.currentPage;
            writeStartPageLI(out, activePage, activePage);
            // Jump to page
            writeOpenEventLink(out, JUMP_TO_PAGE_EVENT_ID, String.valueOf(page), !activePage, activePage ? "active" : null);
            JspUtil.writeEscaped(out, String.valueOf((page - __.firstPage) + 1));
            JspUtil.writeEndTag_SS(out, "a");
            writeEndPageLI(out);
        }

        boolean jumpToNextEnabled = __.currentPage < __.lastPage;
        writeStartPageLI(out, false, !jumpToNextEnabled);
        writeOpenEventLink(out, NEXT_PAGE_EVENT_ID, null, jumpToNextEnabled, nextClass);
        out.write("&nbsp;&gt;");
        JspUtil.writeEndTag_SS(out, "a");
        writeEndPageLI(out);

        boolean jumpToLastEnabled = __.lastPage != __.currentPage;
        writeStartPageLI(out, false, !jumpToLastEnabled);
        writeOpenEventLink(out, LAST_PAGE_EVENT_ID, null, jumpToLastEnabled, lastClass);
        out.write("&nbsp;&raquo;");
        JspUtil.writeEndTag_SS(out, "a");
        writeEndPageLI(out);

        writeInfo(out, __.totalItemCount, __.allItemsShown, __.firstShown, __.lastShown);
    }

    private void writeItemlessPagination(Writer out, Encapsulated __) throws IOException {
        if (__.totalItemCount == 0)
            JspUtil.writeEscaped(out, JspUtil.getResourceString(pageContext, noDataStringId));
        else if (__.totalItemCount != 0 && __.allItemsShown) {
            writeInfo(out, __.totalItemCount, __.allItemsShown, __.firstShown, __.lastShown);
        }
    }

    private void writeStartPageLI(Writer out, boolean active, boolean disabled) throws IOException {
        StringBuilder classBuilder = new StringBuilder();
        if (active) {
            classBuilder.append("active");
            disabled = false; // does not make sense...
        }
        if (disabled) {
            if (classBuilder.length() > 0)
                classBuilder.append(' ');
            classBuilder.append("disabled");
        }
        if (!StringUtils.isEmpty(numberStyleClass)) {
            if (classBuilder.length() > 0)
                classBuilder.append(' ');
            classBuilder.append(numberStyleClass);
        }
        JspUtil.writeOpenStartTag(out, "li");
        JspUtil.writeAttribute(out, "class", classBuilder.toString());
        JspUtil.writeCloseStartTag(out);
    }

    private void writeEndPageLI(Writer out) throws IOException {
        JspUtil.writeEndTag(out, "li"); // numbers
    }

    
  /* ***********************************************************************************
   * Helper functions
   * ***********************************************************************************/

    protected void writeInfo(Writer out, long totalItemCount, boolean allItemsShown, long firstShown, long lastShown) throws IOException {
        JspUtil.writeOpenStartTag(out, "li");
        JspUtil.writeAttribute(out, "class", infoStyleClass);
        JspUtil.writeCloseStartTag(out);

        out.write(MessageUtil.localize("list.visible.records", getEnvironment()) + " [");
        out.write(new Long(firstShown).toString());
        out.write("-");
        out.write(new Long(lastShown).toString());
        out.write("]. " + MessageUtil.localize("list.total.records", getEnvironment()) + " ");
        JspUtil.writeEscaped(out, new Long(totalItemCount).toString());
        out.write(". ");

        JspUtil.writeEndTag(out, "li"); //info
    }

    protected void writeOpenEventLink(Writer out, String eventId, String eventParam, boolean enabled, String styleClass) throws Exception {
        UiEvent event = createListEvent(eventId, eventParam);

        JspUtil.writeOpenStartTag(out, "a");
        if (enabled) {
            JspUtil.writeAttribute(out, "class", "aranea-link-button " + styleClass);
            JspUtil.writeAttribute(out, "href", "#");
        }
        else {
            JspUtil.writeAttribute(out, "class", styleClass);
            JspUtil.writeAttribute(out, "href", "javascript:return false;");
        }

        JspUtil.writeEventAttributes(out, event);

        if (enabled)
            JspWidgetCallUtil.writeSubmitScriptForEvent(out, "onclick");
        else
            JspScriptUtil.writeEmptyEventAttribute(out, "onclick");
        JspUtil.writeCloseStartTag_SS(out);
    }

    protected UiEvent createListEvent(String eventId, String eventParam) {
        UiUpdateEvent event = new UiUpdateEvent();
        event.setId(eventId);
        event.setParam(eventParam);
        event.setTarget(listId);
        event.setUpdateRegionNames(updateRegionNames);
        return event;
    }

   /* ***********************************************************************************
   * Tag attributes
   * ***********************************************************************************/

    public void setNoDataStringId(String noDataStringId) throws JspException {
        this.noDataStringId = (String)evaluateNotNull("noDataStringId", noDataStringId, String.class);
    }

    public void setInfoStyleClass(String infoStyleClass) {
        this.infoStyleClass = infoStyleClass;
    }

    public void setNumberStyleClass(String numberStyleClass) {
        this.numberStyleClass = numberStyleClass;
    }

    public void setUpdateRegions(String updateRegions) throws JspException {
        this.updateRegions = (String) evaluate("updateRegions", updateRegions, String.class);
    }

    public void setGlobalUpdateRegions(String globalUpdateRegions) throws JspException {
        this.globalUpdateRegions = (String) evaluate("globalUpdateRegions", globalUpdateRegions, String.class);
    }

   /* ***********************************************************************************
   * Private helper classe
   * ***********************************************************************************/
    private final static class Encapsulated {
        public final long firstPage ;
        public final long lastPage ;

        public final long blockFirstPage ;
        public final long blockLastPage ;

        public final long currentPage ;
        public final long totalItemCount ;
        public final boolean allItemsShown ;

        public final long firstShown ;
        public final long lastShown ;

        public Encapsulated(SequenceHelper.ViewModel sequenceViewModel) {
            firstPage = sequenceViewModel.getFirstPage().longValue();
            lastPage = sequenceViewModel.getLastPage().longValue();

            blockFirstPage = sequenceViewModel.getBlockFirstPage().longValue();
            blockLastPage = sequenceViewModel.getBlockLastPage().longValue();

            currentPage = sequenceViewModel.getCurrentPage().longValue();
            totalItemCount = sequenceViewModel.getTotalItemCount().longValue();
            allItemsShown = sequenceViewModel.getAllItemsShown().booleanValue();

            firstShown = sequenceViewModel.getPageFirstItem().longValue();
            lastShown = sequenceViewModel.getPageLastItem().longValue();
        }
    }
}
