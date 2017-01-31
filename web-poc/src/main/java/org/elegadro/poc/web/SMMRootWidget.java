package org.elegadro.poc.web;

import org.elegadro.poc.web.base.AppMenuWidget;
import org.elegadro.poc.web.riik.IuraWidget;
import org.araneaframework.framework.container.StandardFlowContainerWidget;
import org.araneaframework.uilib.core.BaseUIWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Taimo Peelo
 */
public class SMMRootWidget extends BaseUIWidget {
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(SMMRootWidget.class);

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
