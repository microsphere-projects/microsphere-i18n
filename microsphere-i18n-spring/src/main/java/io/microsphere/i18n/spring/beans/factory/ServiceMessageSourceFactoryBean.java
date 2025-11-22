package io.microsphere.i18n.spring.beans.factory;

import io.microsphere.i18n.AbstractServiceMessageSource;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.context.ResourceServiceMessageSourceChangedEvent;
import io.microsphere.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static io.microsphere.i18n.spring.constants.I18nConstants.DEFAULT_LOCALE_PROPERTY_NAME;
import static io.microsphere.i18n.spring.constants.I18nConstants.SUPPORTED_LOCALES_PROPERTY_NAME;
import static io.microsphere.i18n.spring.util.LocaleUtils.getLocaleFromLocaleContext;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.invokeAwareInterfaces;
import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.getConstructorIfAvailable;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.parseLocale;

/**
 * {@link ServiceMessageSource} {@link FactoryBean} Implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class ServiceMessageSourceFactoryBean extends CompositeServiceMessageSource implements
        ReloadableResourceServiceMessageSource, InitializingBean, DisposableBean, EnvironmentAware, BeanClassLoaderAware,
        ApplicationContextAware, FactoryBean<ReloadableResourceServiceMessageSource>,
        ApplicationListener<ResourceServiceMessageSourceChangedEvent>, Ordered {

    private static final Logger logger = getLogger(ServiceMessageSourceFactoryBean.class);

    private final String source;

    private ClassLoader classLoader;

    private ConfigurableEnvironment environment;

    private ApplicationContext context;

    private int order;

    public ServiceMessageSourceFactoryBean(String source) {
        this(source, Ordered.LOWEST_PRECEDENCE);
    }

    public ServiceMessageSourceFactoryBean(String source, int order) {
        this.source = source;
        this.order = order;
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
        this.setServiceMessageSources(initServiceMessageSources());
    }

    @NonNull
    @Override
    public Locale getLocale() {
        Locale locale = getLocaleFromLocaleContext();
        if (locale == null) {
            locale = super.getLocale();
        }
        return locale;
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
        Set<Locale> supportedLocales = resolveSupportedLocales(environment);

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
    public String toString() {
        return "ServiceMessageSourceFactoryBean{" +
                "source='" + this.source + '\'' +
                ", supportedLocales=" + getSupportedLocales() +
                ", defaultLocale=" + getDefaultLocale() +
                ", order=" + order +
                ", serviceMessageSources=" + getServiceMessageSources() +
                '}';
    }

    private Locale resolveDefaultLocale(ConfigurableEnvironment environment) {
        String propertyName = DEFAULT_LOCALE_PROPERTY_NAME;
        String localeValue = environment.getProperty(propertyName);
        final Locale locale;
        if (!hasText(localeValue)) {
            locale = getDefaultLocale();
            logger.trace("Default Locale configuration property [name : '{}'] not found, use default value: '{}'", propertyName, locale);
        } else {
            locale = parseLocale(localeValue);
            logger.trace("Default Locale : '{}' parsed by configuration properties [name : '{}']", propertyName, locale);
        }
        return locale;
    }

    private Set<Locale> resolveSupportedLocales(ConfigurableEnvironment environment) {
        final Set<Locale> supportedLocales;
        String propertyName = SUPPORTED_LOCALES_PROPERTY_NAME;
        List<String> locales = environment.getProperty(propertyName, List.class, emptyList());
        if (locales.isEmpty()) {
            supportedLocales = getSupportedLocales();
            logger.trace("Support Locale set configuration property [name : '{}'] not found, use default value: {}", propertyName, supportedLocales);
        } else {
            supportedLocales = locales.stream()
                    .map(StringUtils::parseLocale)
                    .collect(toSet());
            logger.trace("The set of supported Locales parsed by configuration property [name : '{}']: {}", propertyName, supportedLocales);
        }
        return unmodifiableSet(supportedLocales);
    }

    @Override
    public void onApplicationEvent(ResourceServiceMessageSourceChangedEvent event) {
        Iterable<String> changedResources = event.getChangedResources();
        logger.trace("Receive event change resource: {}", changedResources);
        super.reload(changedResources);
    }
}