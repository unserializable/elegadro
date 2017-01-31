package org.elegadro.poc.tag;

import org.araneaframework.http.util.FileImportUtil;
import org.araneaframework.jsp.UiEvent;
import org.araneaframework.jsp.UiUpdateEvent;
import org.araneaframework.jsp.tag.layout.LayoutRowHtmlTag;
import org.araneaframework.jsp.tag.uilib.list.ListTag;
import org.araneaframework.jsp.util.JspUpdateRegionUtil;
import org.araneaframework.jsp.util.JspUtil;
import org.araneaframework.jsp.util.JspWidgetCallUtil;
import org.araneaframework.uilib.list.ListWidget;
import org.araneaframework.uilib.list.OrderInfo;
import org.araneaframework.uilib.list.OrderInfoField;
import org.araneaframework.uilib.list.structure.ListField;
import org.araneaframework.uilib.list.structure.ListStructure;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

/**
 * Tag for list header, very closely c/p from Aranea Framework 1.2.2 release examples ComponentListHeaderTag class!
 *
 * @author Taimo Peelo
 */
public class SMMListHeaderTag extends LayoutRowHtmlTag {
    public final static String ORDER_EVENT_ID = "order";
    public final static String COMPONENT_LIST_STYLE_CLASS = "xdata";

    private String updateRegions;
    private String globalUpdateRegions;

    private List updateRegionNames;

    public SMMListHeaderTag() {
        styleClass = COMPONENT_LIST_STYLE_CLASS;
    }

    protected int doStartTag(Writer out) throws Exception {
        this.updateRegionNames = JspUpdateRegionUtil.getUpdateRegionNames(pageContext, updateRegions, globalUpdateRegions);

        super.doStartTag(out);

        writeHeader(out);
        return EVAL_BODY_INCLUDE;
    }

    protected void writeHeader(Writer out) throws Exception {
        // Get list data
        String listId = (String)requireContextEntry(ListTag.LIST_FULL_ID_KEY);
        ListWidget.ViewModel viewModel = (ListWidget.ViewModel)requireContextEntry(ListTag.LIST_VIEW_MODEL_KEY);

        // Get order data
        ListStructure.ViewModel listStructureViewModel = viewModel.getListStructure();
        OrderInfo.ViewModel orderInfoViewModel = viewModel.getOrderInfo();

        for(Iterator i = listStructureViewModel.getColumnList().iterator(); i.hasNext();) {
            ListField.ViewModel columnViewModel = (ListField.ViewModel)i.next();

            // Write cell
            JspUtil.writeOpenStartTag(out, "th");
            JspUtil.writeCloseStartTag(out);

            // Write link if needed
            if (listStructureViewModel.isColumnOrdered(columnViewModel.getId())) {
                // Draw column ordering if needed
                for(Iterator j = orderInfoViewModel.getFields().iterator(); j.hasNext();) {
                    OrderInfoField.ViewModel orderInfoFieldViewModel = (OrderInfoField.ViewModel)j.next();

                    if (orderInfoFieldViewModel.getId().equals(columnViewModel.getId())) {
                        StringBuffer url = ((HttpServletRequest)pageContext.getRequest()).getRequestURL();
                        // Found
                        if (orderInfoFieldViewModel.isAscending()) {
                            JspUtil.writeOpenStartTag(out, "img");
                            JspUtil.writeAttribute(out, "src",
                                    url.append(FileImportUtil.getImportString("gfx/ico_sortup.gif")));
                            JspUtil.writeCloseStartEndTag(out);
                        }
                        else {
                            JspUtil.writeOpenStartTag(out, "img");
                            JspUtil.writeAttribute(out, "src",
                                    url.append(FileImportUtil.getImportString("gfx/ico_sortdown.gif")));
                            JspUtil.writeCloseStartEndTag(out);
                        }
                        out.write("&nbsp;");
                        break;
                    }
                }

                UiEvent orderEvent = new UiUpdateEvent(ORDER_EVENT_ID, listId, columnViewModel.getId(), updateRegionNames);

                JspUtil.writeOpenStartTag(out, "a");
                JspUtil.writeAttribute(out, "class", "aranea-link-button");
                JspUtil.writeEventAttributes(out, orderEvent);
                JspWidgetCallUtil.writeSubmitScriptForEvent(out, "onclick");

                JspUtil.writeCloseStartTag_SS(out);
            }
            if (columnViewModel.getLabel() != null)
                JspUtil.writeEscaped(out, JspUtil.getResourceString(pageContext, columnViewModel.getLabel()));

            // Write link if needed
            if (listStructureViewModel.isColumnOrdered(columnViewModel.getId()))
                JspUtil.writeEndTag(out, "a");

            // Write cell
            JspUtil.writeEndTag(out, "th");
        }
    }

    public void setUpdateRegions(String updateRegions) throws JspException {
        this.updateRegions = (String) evaluate("updateRegions", updateRegions, String.class);
    }

    public void setGlobalUpdateRegions(String globalUpdateRegions) throws JspException {
        this.globalUpdateRegions = (String) evaluate("globalUpdateRegions", globalUpdateRegions, String.class);
    }
}
