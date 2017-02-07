package org.elegadro.poc.web;

import org.araneaframework.framework.container.StandardFlowContainerWidget;
import org.araneaframework.uilib.core.BaseUIWidget;
import org.elegadro.poc.web.base.AppMenuWidget;
import org.elegadro.poc.web.riik.IuraWidget;

/**
 * @author Taimo Peelo
 */
public class SMMRootWidget extends BaseUIWidget {
    private static final long serialVersionUID = 1L;

    public StandardFlowContainerWidget appFlow;

    public SMMRootWidget() {}

    @Override
    protected void init() throws Exception {
        setViewSelector("root");
        appFlow = new AppMenuWidget(new IuraWidget());
        appFlow.setFinishable(false);
        addWidget("wContent", appFlow);
    }
}
