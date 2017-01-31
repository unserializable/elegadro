package org.elegadro.poc.web.base;

import org.elegadro.poc.web.riik.IuraWidget;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.araneaframework.OutputData;
import org.araneaframework.Widget;
import org.araneaframework.http.util.ServletUtil;
import org.araneaframework.uilib.core.BaseMenuWidget;
import org.araneaframework.uilib.core.MenuItem;

/**
 * @author Taimo Peelo
 */
@Slf4j
public class AppMenuWidget extends BaseMenuWidget {
    public AppMenuWidget(Widget topWidget) throws Exception {
        super(topWidget);
    }

    @Override
    protected MenuItem buildMenu() throws Exception {
        MenuItem root = new MenuItem();

        root.addMenuItem(new MenuItem("#Iura", IuraWidget.class));

        return root;
    }

    @Override
    protected void renderExceptionHandler(OutputData outputData, Exception e) throws Exception {
        Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause != null) {
            putViewDataOnce(
                "rootStackTrace",
                ExceptionUtils.getFullStackTrace(rootCause)
            );
        }
        putViewDataOnce("fullStackTrace", ExceptionUtils.getFullStackTrace(e));
        ServletUtil.include("/WEB-INF/jsp/error.jspx", this, outputData);
    }
}