package org.elegadro.poc.web.base;

import org.elegadro.poc.util.AraneaAutowireUtil;
import org.araneaframework.Component;
import org.araneaframework.Environment;
import org.araneaframework.Scope;
import org.araneaframework.Widget;
import org.araneaframework.uilib.core.BaseUIWidget;
import org.araneaframework.uilib.list.ListWidget;
import org.araneaframework.uilib.tab.TabContainerContext;

/**
 * @author Taimo Peelo
 */
public class BaseAppUIWidget extends BaseUIWidget {
    protected String name;

    protected TabContainerContext getTabContainerContext() {
        return (TabContainerContext) getEnvironment().getEntry(TabContainerContext.class);
    }

    public Component.Interface _getComponent() {
        return new ComponentImpl();
    }

    protected class ComponentImpl extends BaseUIWidget.ComponentImpl {
        private static final long serialVersionUID = 1L;

        public void init(Scope scope, Environment env) {
            AraneaAutowireUtil.inject(env, BaseAppUIWidget.this);
            super.init(scope, env);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /*
        START OF HACK #1 -- do not bother subclassing both ListWidget and BeanListWidget to
        remove the list event listeners that do not sense in the application at hand. Instead
        override the addWidget* methods. Imperfect but fine -- as long as only BaseAppUIWidget
        subclasses handle adding of list widgets.
     ****************************************************************************************/
    @Override
    public void addWidget(Object key, Widget child, Environment env) {
        if (child instanceof ListWidget) {
            clearUnhandledListEventListeners((ListWidget) child);
        }
        super.addWidget(key, child, env);
    }

    @Override
    public void addWidget(Object key, Widget child) {
        if (child instanceof ListWidget) {
            clearUnhandledListEventListeners((ListWidget) child);
        }
        super.addWidget(key, child);
    }

    private void clearUnhandledListEventListeners(ListWidget lw) {
        lw.clearEventlisteners("showAll");
        lw.clearEventlisteners("showSlice");
        lw.clearEventlisteners("nextBlock");
        lw.clearEventlisteners("previousBlock");
    }

    /*
      END OF HACK #1
    ****************************************************************************************/
}
