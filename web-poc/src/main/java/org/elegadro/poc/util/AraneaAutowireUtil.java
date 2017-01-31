package org.elegadro.poc.util;

import lombok.SneakyThrows;
import org.araneaframework.Component;
import org.araneaframework.Environment;
import org.araneaframework.core.util.ClassLoaderUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;

/**
 * @author Taimo Peelo
 */
public final class AraneaAutowireUtil implements BeanFactoryAware {
    public static final AraneaAutowireUtil INSTANCE = new AraneaAutowireUtil();

    public static BeanFactory BEAN_FACTORY_INSTANCE;

    private AraneaAutowireUtil() {}

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        BEAN_FACTORY_INSTANCE = beanFactory;
    }

    @SneakyThrows
    public static final void inject(Environment env, Component component) {
        Field[] declaredFields = component.getClass().getDeclaredFields();
        for (Field f: declaredFields) {
            Annotation[] annotations = f.getAnnotations();
            for (Annotation a: annotations) {
                if (Autowired.class.equals(a.annotationType())) {
                    wire(env, f,component);
                }
            }
        }
    }

    private static final void wire(Environment env, Field f, Component o) throws IllegalAccessException, InstantiationException {
        Class<?> type = f.getType();

        if (!f.isAccessible())
            f.setAccessible(true);

        Object proxy = Proxy.newProxyInstance(
                ClassLoaderUtil.getDefaultClassLoader(), new Class[]{type},
                new SpringBeanInvocationHandler(env, type));

        f.set(o, proxy);
    }

    private static class SpringBeanInvocationHandler implements InvocationHandler, Serializable {
        private Environment env;
        private Class<?> clazz;

        public SpringBeanInvocationHandler(Environment env, Class<?> clazz) {
            this.env = env;
            this.clazz = clazz;
            env.requireEntry(BeanFactory.class);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            BeanFactory bf = (BeanFactory) env.getEntry(BeanFactory.class);

            if (bf == null) {
                bf = AraneaAutowireUtil.BEAN_FACTORY_INSTANCE;
            }

            if (bf == null) {
                throw new IllegalStateException("BeanFactory not acquired. If the proxy is used outside request, then "
                        + AraneaAutowireUtil.class.getName() + " needs to have correct BeanFactory wired explicitly.");
            }

            Object bean = bf.getBean(clazz);

            try {
                return method.invoke(bean, args);
            } catch (InvocationTargetException ex){
                throw ex.getTargetException();
            }
        }
    }
}
