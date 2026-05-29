package io.microsphere.i18n.spring.beans.factory;

import io.microsphere.annotation.Nonnull;
import io.microsphere.i18n.AbstractServiceMessageSource;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
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
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
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
 * {@link ServiceMessageSource} {@link FactoryBean} Implementation that creates and manages
 * {@link ServiceMessageSource} beans based on Spring Factories configuration.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Typically auto-registered via @EnableI18n
 *   @Bean
 *   public ServiceMessageSourceFactoryBean myServiceMessageSource() {
 *       return new ServiceMessageSourceFactoryBean("myapp");
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableI18n
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

    /**
     * Constructs with the given source identifier and lowest precedence order.
     *
     * @param source the source identifier
     */
    public ServiceMessageSourceFactoryBean(String source) {
        this(source, LOWEST_PRECEDENCE);
    }

    /**
     * Constructs with the given source identifier and order.
     *
     * @param source the source identifier
     * @param order  the order for bean sorting
     */
    public ServiceMessageSourceFactoryBean(String source, int order) {
        this.source = source;
        setOrder(order);
    }

    @Override
    public ReloadableResourceServiceMessageSource getObject() {
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

    @Nonnull
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
        this.environment = asConfigurableEnvironment(environment);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order for this factory bean.
     *
     * @param order the order value
     */
    public void setOrder(int order) {
        this.order = order;
    }

    protected List<AbstractServiceMessageSource> initServiceMessageSources() {
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

    protected Locale resolveDefaultLocale(ConfigurableEnvironment environment) {
        String propertyName = DEFAULT_LOCALE_PROPERTY_NAME;
        String localeValue = environment.getProperty(propertyName);
        final Locale locale;
        if (!hasText(localeValue)) {
            locale = getDefaultLocale();
            if (logger.isTraceEnabled()) {
                logger.trace("Default Locale configuration property [name : '{}'] not found, use default value: '{}'", propertyName, locale);
            }
        } else {
            locale = parseLocale(localeValue);
            if (logger.isTraceEnabled()) {
                logger.trace("Default Locale : '{}' parsed by configuration properties [name : '{}']", propertyName, locale);
            }
        }
        return locale;
    }

    protected Set<Locale> resolveSupportedLocales(ConfigurableEnvironment environment) {
        final Set<Locale> supportedLocales;
        String propertyName = SUPPORTED_LOCALES_PROPERTY_NAME;
        List<String> locales = environment.getProperty(propertyName, List.class, emptyList());
        if (locales.isEmpty()) {
            supportedLocales = getSupportedLocales();
            if (logger.isTraceEnabled()) {
                logger.trace("Support Locale set configuration property [name : '{}'] not found, use default value: {}", propertyName, supportedLocales);
            }
        } else {
            supportedLocales = locales.stream()
                    .map(StringUtils::parseLocale)
                    .collect(toSet());
            if (logger.isTraceEnabled()) {
                logger.trace("The set of supported Locales parsed by configuration property [name : '{}']: {}", propertyName, supportedLocales);
            }
        }
        return unmodifiableSet(supportedLocales);
    }

    @Override
    public void onApplicationEvent(ResourceServiceMessageSourceChangedEvent event) {
        Iterable<String> changedResources = event.getChangedResources();
        if (logger.isTraceEnabled()) {
            logger.trace("Receive event change resource: {}", changedResources);
        }
        super.reload(changedResources);
    }
}
