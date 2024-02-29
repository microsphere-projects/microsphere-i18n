package io.microsphere.i18n.spring.beans.factory;

import io.microsphere.i18n.AbstractServiceMessageSource;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.context.ResourceServiceMessageSourceChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static io.microsphere.i18n.spring.constants.I18nConstants.DEFAULT_LOCALE_PROPERTY_NAME;
import static io.microsphere.i18n.spring.constants.I18nConstants.SUPPORTED_LOCALES_PROPERTY_NAME;
import static io.microsphere.spring.util.BeanUtils.getSortedBeans;
import static io.microsphere.spring.util.BeanUtils.invokeAwareInterfaces;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.getConstructorIfAvailable;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link ServiceMessageSource} {@link FactoryBean} Implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class ServiceMessageSourceFactoryBean implements ReloadableResourceServiceMessageSource,
        InitializingBean, DisposableBean, EnvironmentAware, BeanClassLoaderAware, ApplicationContextAware,
        FactoryBean<ReloadableResourceServiceMessageSource>, ApplicationListener<ResourceServiceMessageSourceChangedEvent>,
        Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ServiceMessageSourceFactoryBean.class);

    private final String source;

    private CompositeServiceMessageSource delegate;

    private ClassLoader classLoader;

    private ConfigurableEnvironment environment;

    private ApplicationContext context;

    private int order;

    public ServiceMessageSourceFactoryBean(String source) {
        this.source = source;
        this.delegate = new CompositeServiceMessageSource();
    }

    @Override
    public ReloadableResourceServiceMessageSource getObject() throws Exception {
        return this;
    }

    @Override
    public Class<ReloadableResourceServiceMessageSource> getObjectType() {
        return ReloadableResourceServiceMessageSource.class;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void init() {
        this.delegate.setServiceMessageSources(initServiceMessageSources());
    }

    @Override
    public void destroy() {
        this.delegate.destroy();
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
        if (locale == null) {
            locale = this.delegate.getLocale();
        }
        return locale;
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
        return source;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "The 'environment' parameter must be of type ConfigurableEnvironment");
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    private List<AbstractServiceMessageSource> initServiceMessageSources() {
        List<String> factoryNames = loadFactoryNames(AbstractServiceMessageSource.class, classLoader);

        Locale defaultLocale = resolveDefaultLocale(environment);
        List<Locale> supportedLocales = resolveSupportedLocales(environment);

        List<AbstractServiceMessageSource> serviceMessageSources = new ArrayList<>(factoryNames.size());

        for (String factoryName : factoryNames) {
            Class<?> factoryClass = resolveClassName(factoryName, classLoader);
            Constructor constructor = getConstructorIfAvailable(factoryClass, String.class);
            AbstractServiceMessageSource serviceMessageSource = (AbstractServiceMessageSource) instantiateClass(constructor, source);
            serviceMessageSources.add(serviceMessageSource);

            invokeAwareInterfaces(serviceMessageSource, context);

            serviceMessageSource.setDefaultLocale(defaultLocale);
            serviceMessageSource.setSupportedLocales(supportedLocales);
            serviceMessageSource.init();
        }

        sort(serviceMessageSources);

        return serviceMessageSources;
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
    public String toString() {
        return "ServiceMessageSourceFactoryBean{" +
                "delegate=" + delegate +
                ", order=" + order +
                '}';
    }

    private Locale resolveDefaultLocale(ConfigurableEnvironment environment) {
        String propertyName = DEFAULT_LOCALE_PROPERTY_NAME;
        String localeValue = environment.getProperty(propertyName);
        final Locale locale;
        if (!hasText(localeValue)) {
            locale = ReloadableResourceServiceMessageSource.super.getDefaultLocale();
            logger.debug("Default Locale configuration property [name : '{}'] not found, use default value: '{}'", propertyName, locale);
        } else {
            locale = StringUtils.parseLocale(localeValue);
            logger.debug("Default Locale : '{}' parsed by configuration properties [name : '{}']", propertyName, locale);
        }
        return locale;
    }

    private List<Locale> resolveSupportedLocales(ConfigurableEnvironment environment) {
        final List<Locale> supportedLocales;
        String propertyName = SUPPORTED_LOCALES_PROPERTY_NAME;
        List<String> locales = environment.getProperty(propertyName, List.class, emptyList());
        if (locales.isEmpty()) {
            supportedLocales = ReloadableResourceServiceMessageSource.super.getSupportedLocales();
            logger.debug("Support Locale list configuration property [name : '{}'] not found, use default value: {}", propertyName, supportedLocales);
        } else {
            supportedLocales = locales.stream().map(StringUtils::parseLocale).collect(Collectors.toList());
            logger.debug("List of supported Locales parsed by configuration property [name : '{}']: {}", propertyName, supportedLocales);
        }
        return unmodifiableList(supportedLocales);
    }

    @Override
    public void onApplicationEvent(ResourceServiceMessageSourceChangedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Iterable<String> changedResources = event.getChangedResources();
        logger.debug("Receive event change resource: {}", changedResources);
        for (ReloadableResourceServiceMessageSource reloadableResourceServiceMessageSource : getSortedBeans(context, ReloadableResourceServiceMessageSource.class)) {
            if (reloadableResourceServiceMessageSource.canReload(changedResources)) {
                reloadableResourceServiceMessageSource.reload(changedResources);
                logger.debug("change resource [{}] activate {} reloaded", changedResources, reloadableResourceServiceMessageSource);
            }
        }
    }
}
