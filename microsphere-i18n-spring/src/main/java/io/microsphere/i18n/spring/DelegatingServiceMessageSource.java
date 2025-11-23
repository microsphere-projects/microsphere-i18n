package io.microsphere.i18n.spring;

import io.microsphere.i18n.AbstractServiceMessageSource;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.logging.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedList;
import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * The delegating {@link ServiceMessageSource} class is composited by the Spring {@link ServiceMessageSource} beans
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractServiceMessageSource
 * @see ServiceMessageSource
 * @since 1.0.0
 */
public class DelegatingServiceMessageSource extends CompositeServiceMessageSource
        implements ReloadableResourceServiceMessageSource, InitializingBean, DisposableBean {

    private static final Logger logger = getLogger(DelegatingServiceMessageSource.class);

    private final ObjectProvider<ServiceMessageSource> serviceMessageSourcesProvider;

    public DelegatingServiceMessageSource(ObjectProvider<ServiceMessageSource> serviceMessageSourcesProvider) {
        this.serviceMessageSourcesProvider = serviceMessageSourcesProvider;
    }

    @Override
    public void init() {
        this.setServiceMessageSources(findServiceMessageSourceBeans());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * Get the {@link CompositeServiceMessageSource}
     *
     * @return the {@link CompositeServiceMessageSource}
     */
    public CompositeServiceMessageSource getDelegate() {
        return this;
    }

    private List<ServiceMessageSource> findServiceMessageSourceBeans() {
        List<ServiceMessageSource> serviceMessageSources = new LinkedList<>();
        for (ServiceMessageSource serviceMessageSource : serviceMessageSourcesProvider) {
            if(serviceMessageSource!=this) {
                serviceMessageSources.add(serviceMessageSource);
            }
        }
        sort(serviceMessageSources);
        logger.trace("Initializes the ServiceMessageSource Bean list : {}", serviceMessageSources);
        return serviceMessageSources;
    }
}