package io.microsphere.i18n;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static io.microsphere.collection.ListUtils.forEach;
import static java.util.Collections.singleton;
import static java.util.Collections.sort;

/**
 * The Composite {@link ServiceMessageSource} class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractServiceMessageSource
 * @see ServiceMessageSource
 * @see ResourceServiceMessageSource
 * @see ReloadableResourceServiceMessageSource
 * @since 1.0.0
 */
public class CompositeServiceMessageSource extends AbstractServiceMessageSource implements ReloadableResourceServiceMessageSource {

    private List<? extends ServiceMessageSource> serviceMessageSources;

    public CompositeServiceMessageSource() {
        super("Composite");
    }

    public CompositeServiceMessageSource(List<? extends ServiceMessageSource> serviceMessageSources) {
        super("Composite");
        setServiceMessageSources(serviceMessageSources);
    }

    @Override
    public void init() {
        forEach(this.serviceMessageSources, ServiceMessageSource::init);
        this.initDefaultLocale();
        this.initSupportedLocales();
    }

    @Override
    public void destroy() {
        List<? extends ServiceMessageSource> serviceMessageSources = this.serviceMessageSources;
        forEach(serviceMessageSources, ServiceMessageSource::destroy);
        serviceMessageSources.clear();
    }

    public void setServiceMessageSources(List<? extends ServiceMessageSource> serviceMessageSources) {
        List<? extends ServiceMessageSource> oldServiceMessageSources = this.serviceMessageSources;
        List<ServiceMessageSource> newServiceMessageSources = new ArrayList<>(serviceMessageSources);
        sort(newServiceMessageSources);
        if (oldServiceMessageSources != null) {
            oldServiceMessageSources.clear();
        }
        this.serviceMessageSources = newServiceMessageSources;
        logger.debug("Source '{}' sets ServiceMessageSource list, original : {} , sorted : {}", serviceMessageSources, newServiceMessageSources);
    }

    protected Locale resolveLocale(Locale locale) {

        Locale defaultLocale = getDefaultLocale();

        if (locale == null || Objects.equals(defaultLocale, locale)) { // If it's the default Locale
            return defaultLocale;
        }

        if (supports(locale)) { // If it matches the supported Locale list
            return locale;
        }

        Locale resolvedLocale = null;

        List<Locale> derivedLocales = resolveDerivedLocales(locale);
        for (Locale derivedLocale : derivedLocales) {
            if (supports(derivedLocale)) {
                resolvedLocale = derivedLocale;
                break;
            }
        }

        return resolvedLocale == null ? defaultLocale : resolvedLocale;
    }

    @Override
    protected String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args) {
        String message = null;
        for (ServiceMessageSource serviceMessageSource : serviceMessageSources) {
            message = serviceMessageSource.getMessage(resolvedCode, resolvedLocale, args);
            if (message != null) {
                break;
            }
        }

        if (message == null) {
            Locale defaultLocale = getDefaultLocale();
            if (!Objects.equals(defaultLocale, resolvedLocale)) { // Use the default Locale as the bottom pocket
                message = getInternalMessage(resolvedCode, resolvedCode, defaultLocale, defaultLocale, args);
            }
        }

        return message;
    }

    @Override
    public String toString() {
        return "CompositeServiceMessageSource{" +
                "serviceMessageSources=" + serviceMessageSources +
                '}';
    }

    @Override
    public void reload(Iterable<String> changedResources) {
        iterate(ReloadableResourceServiceMessageSource.class, reloadableResourceServiceMessageSource -> {
            if (reloadableResourceServiceMessageSource.canReload(changedResources)) {
                reloadableResourceServiceMessageSource.reload(changedResources);
            }
        });
    }

    @Override
    public boolean canReload(Iterable<String> changedResources) {
        return true;
    }

    @Override
    public void initializeResource(String resource) {
        initializeResources(singleton(resource));
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        iterate(ResourceServiceMessageSource.class, resourceServiceMessageSource -> {
            resourceServiceMessageSource.initializeResources(resources);
        });
    }

    @Override
    public Set<String> getInitializeResources() {
        Set<String> resources = new LinkedHashSet<>();
        iterate(ResourceServiceMessageSource.class, resourceServiceMessageSource -> {
            resources.addAll(resourceServiceMessageSource.getInitializeResources());
        });
        return resources;
    }

    @Override
    public Charset getEncoding() {
        return ReloadableResourceServiceMessageSource.super.getEncoding();
    }

    private <T> void iterate(Class<T> serviceMessageSourceType, Consumer<T> consumer) {
        this.serviceMessageSources.stream()
                .filter(serviceMessageSource -> serviceMessageSourceType.isInstance(serviceMessageSource))
                .map(serviceMessageSourceType::cast)
                .forEach(consumer);
    }

    private void initDefaultLocale() {
        Locale defaultLocale = serviceMessageSources.isEmpty() ? super.getDefaultLocale() : serviceMessageSources.get(0).getDefaultLocale();
        setDefaultLocale(defaultLocale);
    }

    private void initSupportedLocales() {
        List<Locale> allSupportedLocales = new LinkedList<>();
        for (ServiceMessageSource serviceMessageSource : serviceMessageSources) {
            for (Locale supportedLocale : serviceMessageSource.getSupportedLocales()) {
                allSupportedLocales.add(supportedLocale);
            }
        }
        setSupportedLocales(allSupportedLocales);
    }
}
