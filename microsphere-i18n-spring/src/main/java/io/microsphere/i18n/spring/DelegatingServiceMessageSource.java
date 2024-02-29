package io.microsphere.i18n.spring;

import io.microsphere.i18n.AbstractServiceMessageSource;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * The delegating {@link ServiceMessageSource} class is composited by the Spring {@link ServiceMessageSource} beans
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractServiceMessageSource
 * @see ServiceMessageSource
 * @since 1.0.0
 */
public class DelegatingServiceMessageSource implements ReloadableResourceServiceMessageSource, InitializingBean,
        DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(DelegatingServiceMessageSource.class);

    private final ObjectProvider<ServiceMessageSource> serviceMessageSourcesProvider;

    private CompositeServiceMessageSource delegate;

    private ListableBeanFactory beanFactory;

    public DelegatingServiceMessageSource(ObjectProvider<ServiceMessageSource> serviceMessageSourcesProvider) {
        this.serviceMessageSourcesProvider = serviceMessageSourcesProvider;
    }

    @Override
    public void init() {
        CompositeServiceMessageSource delegate = this.delegate;
        if (delegate == null) {
            delegate = new CompositeServiceMessageSource();
            delegate.setServiceMessageSources(getServiceMessageSourceBeans());
            this.delegate = delegate;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Nonnull
    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return this.delegate.getMessage(code, locale, args);
    }

    @Override
    public String getMessage(String code, Object... args) {
        return this.delegate.getMessage(code, args);
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return locale == null ? this.delegate.getLocale() : locale;
    }

    @Nonnull
    @Override
    public Locale getDefaultLocale() {
        return this.delegate.getDefaultLocale();
    }

    @Nonnull
    @Override
    public List<Locale> getSupportedLocales() {
        return this.delegate.getSupportedLocales();
    }

    @Override
    public String getSource() {
        return this.delegate.getSource();
    }

    @Override
    public void reload(Iterable<String> changedResources) {
        this.delegate.reload(changedResources);
    }

    @Override
    public boolean canReload(Iterable<String> changedResources) {
        return this.delegate.canReload(changedResources);
    }

    @Override
    public void initializeResource(String resource) {
        this.delegate.initializeResource(resource);
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        this.delegate.initializeResources(resources);
    }

    @Override
    public Set<String> getInitializeResources() {
        return this.delegate.getInitializeResources();
    }

    @Override
    public Charset getEncoding() {
        return this.delegate.getEncoding();
    }

    @Override
    public void destroy() {
        this.delegate.destroy();
    }

    @Override
    public String toString() {
        return "ServiceMessageSources{" + "delegate=" + delegate + '}';
    }

    private List<ServiceMessageSource> getServiceMessageSourceBeans() {
        List<ServiceMessageSource> serviceMessageSources = new LinkedList<>();
        serviceMessageSourcesProvider.forEach(serviceMessageSources::add);
        sort(serviceMessageSources);
        logger.debug("Initializes the ServiceMessageSource Bean list : {}", serviceMessageSources);
        return serviceMessageSources;
    }

}
