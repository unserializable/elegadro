package org.elegadro.poc.servlet;

import org.elegadro.poc.config.PersistenceConfiguration;
import org.elegadro.poc.config.SpringConfiguration;
import org.elegadro.poc.util.AraneaAutowireUtil;
import org.araneaframework.core.AraneaRuntimeException;
import org.araneaframework.http.ServletServiceAdapterComponent;
import org.araneaframework.http.core.BaseAraneaDispatcherServlet;
import org.araneaframework.integration.spring.AraneaSpringDispatcherServlet;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Mixes together Aranea and Spring in more modern manner than Aranea 1.2.2
 * {@link org.araneaframework.integration.spring.AraneaSpringDispatcherServlet} does.
 *
 * @author Taimo Peelo
 */
public class ApplicationDispatcherServlet extends BaseAraneaDispatcherServlet {
    private static final long serialVersionUID = 1L;

    protected ConfigurableListableBeanFactory beanFactory;

    public void init() throws ServletException {
        final String araneaCustomConfXml = AraneaSpringDispatcherServlet.DEFAULT_ARANEA_CUSTOM_CONF_XML;
        final String araneaCustomConfProperties = AraneaSpringDispatcherServlet.DEFAULT_ARANEA_CUSTOM_CONF_PROPERTIES;

        AnnotationConfigApplicationContext annotationConfigAppCtx = new AnnotationConfigApplicationContext(
            SpringConfiguration.class,
            PersistenceConfiguration.class
        );

        this.beanFactory = annotationConfigAppCtx.getBeanFactory();

        XmlBeanDefinitionReader
                xmlBeanDefinitionReader = new XmlBeanDefinitionReader(annotationConfigAppCtx);

        xmlBeanDefinitionReader.loadBeanDefinitions(new ClassPathResource(AraneaSpringDispatcherServlet.ARANEA_DEFAULT_CONF_XML));

        PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
        cfg.setLocation(new ClassPathResource(AraneaSpringDispatcherServlet.ARANEA_DEFAULT_CONF_PROPERTIES));
        cfg.setIgnoreUnresolvablePlaceholders(true);

        // Loading custom properties
        Properties localConf = new Properties();
        try {
            if (getServletContext().getResource(araneaCustomConfProperties) != null) {
                localConf.load(getServletContext().getResourceAsStream(araneaCustomConfProperties));
            }
        } catch (IOException e) {
            throw new ServletException(e);
        }

        cfg.setProperties(localConf);
        cfg.setLocalOverride(true);

        // Loading custom XML config
        try {
            if (getServletContext().getResource(araneaCustomConfXml) != null) {
                xmlBeanDefinitionReader.loadBeanDefinitions(new ServletContextResource(getServletContext(), araneaCustomConfXml));
            }
        } catch (MalformedURLException e) {
            throw new AraneaRuntimeException(e);
        }

        cfg.postProcessBeanFactory(this.beanFactory);
        AraneaAutowireUtil.INSTANCE.setBeanFactory(beanFactory);
        beanFactory.registerSingleton(AraneaAutowireUtil.class.getSimpleName(), AraneaAutowireUtil.INSTANCE);

        super.init();
    }

    protected ServletServiceAdapterComponent buildRootComponent() {
        return (ServletServiceAdapterComponent) this.beanFactory.getBean(AraneaSpringDispatcherServlet.DEFAULT_ARANEA_ROOT);
    }

    protected Map getEnvironmentEntries() {
        return Collections.singletonMap(BeanFactory.class, this.beanFactory);
    }
}
