package io.microsphere.i18n.spring;

import io.microsphere.i18n.AbstractServiceMessageSource;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;

/**
 * The delegating {@link ServiceMessageSource} class is composited by the Spring {@link ServiceMessageSource} beans
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractServiceMessageSource
 * @see ServiceMessageSource
 * @since 1.0.0
 */
public class DelegatingServiceMessageSource extends CompositeServiceMessageSource
        implements ReloadableResourceServiceMessageSource, InitializingBean, DisposableBean, BeanFactoryAware {

    private static final Logger logger = getLogger(DelegatingServiceMessageSource.class);

    private BeanFactory beanFactory;

    @Override
    public void afterPropertiesSet() {
        super.init();
        this.setServiceMessageSources(findServiceMessageSourceBeans());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private List<ServiceMessageSource> findServiceMessageSourceBeans() {
        List<ServiceMessageSource> serviceMessageSources = new ArrayList<>(getSortedBeans(this.beanFactory, ServiceMessageSource.class));
        serviceMessageSources.remove(this);
        logger.trace("Initializes the ServiceMessageSource Bean list : {}", serviceMessageSources);
        return serviceMessageSources;
    }

}