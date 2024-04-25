package io.microsphere.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import static io.microsphere.collection.ListUtils.forEach;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

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
public class CompositeServiceMessageSource implements ReloadableResourceServiceMessageSource {

    private static final Logger logger = LoggerFactory.getLogger(CompositeServiceMessageSource.class);

    private List<? extends ServiceMessageSource> serviceMessageSources;

    public CompositeServiceMessageSource() {
        this.serviceMessageSources = emptyList();
    }

    public CompositeServiceMessageSource(List<? extends ServiceMessageSource> serviceMessageSources) {
        setServiceMessageSources(serviceMessageSources);
    }

    @Override
    public void init() {
        forEach(this.serviceMessageSources, ServiceMessageSource::init);
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        for (ServiceMessageSource serviceMessageSource : serviceMessageSources) {
            message = serviceMessageSource.getMessage(code, locale, args);
            if (message != null) {
                break;
            }
        }
        return message;
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        ServiceMessageSource serviceMessageSource = getFirstServiceMessageSource();
        return serviceMessageSource == null ? getDefaultLocale() : serviceMessageSource.getLocale();
    }

    @Nonnull
    @Override
    public Locale getDefaultLocale() {
        ServiceMessageSource serviceMessageSource = getFirstServiceMessageSource();
        return serviceMessageSource == null ? ReloadableResourceServiceMessageSource.super.getDefaultLocale() : serviceMessageSource.getLocale();
    }

    @Nonnull
    @Override
    public List<Locale> getSupportedLocales() {
        List<Locale> supportedLocales = new LinkedList<>();
        iterate(serviceMessageSource -> {
            for (Locale locale : serviceMessageSource.getSupportedLocales()) {
                if (!supportedLocales.contains(locale)) {
                    supportedLocales.add(locale);
                }
            }
        });

        return supportedLocales.isEmpty() ? getDefaultSupportedLocales() :
                unmodifiableList(supportedLocales);
    }

    public List<Locale> getDefaultSupportedLocales() {
        return ReloadableResourceServiceMessageSource.super.getSupportedLocales();
    }

    @Override
    public String getSource() {
        return ReloadableResourceServiceMessageSource.super.getSource();
    }

    public void setServiceMessageSources(List<? extends ServiceMessageSource> serviceMessageSources) {
        List<? extends ServiceMessageSource> oldServiceMessageSources = this.serviceMessageSources;
        List<ServiceMessageSource> newServiceMessageSources = new ArrayList<>(serviceMessageSources);
        sort(newServiceMessageSources);
        if (oldServiceMessageSources != null) {
            oldServiceMessageSources.clear();
        }
        this.serviceMessageSources = newServiceMessageSources;
        logger.debug("Source '{}' sets ServiceMessageSource list, sorted : {}", serviceMessageSources, newServiceMessageSources);
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
        return unmodifiableSet(resources);
    }

    @Override
    public Charset getEncoding() {
        return ReloadableResourceServiceMessageSource.super.getEncoding();
    }

    /**
     * Get the read-only list of the composited {@link ServiceMessageSource}
     *
     * @return non-null
     */
    @Nonnull
    public List<ServiceMessageSource> getServiceMessageSources() {
        return unmodifiableList(serviceMessageSources);
    }

    @Override
    public void destroy() {
        List<? extends ServiceMessageSource> serviceMessageSources = this.serviceMessageSources;
        forEach(serviceMessageSources, ServiceMessageSource::destroy);
        serviceMessageSources.clear();
    }

    @Override
    public String toString() {
        return "CompositeServiceMessageSource{" +
                "serviceMessageSources=" + serviceMessageSources +
                '}';
    }

    private ServiceMessageSource getFirstServiceMessageSource() {
        return this.serviceMessageSources.isEmpty() ? null : this.serviceMessageSources.get(0);
    }

    private <T> void iterate(Class<T> serviceMessageSourceType, Consumer<T> consumer) {
        this.serviceMessageSources.stream()
                .filter(serviceMessageSourceType::isInstance)
                .map(serviceMessageSourceType::cast)
                .forEach(consumer);
    }

    private <T> void iterate(Consumer<ServiceMessageSource> consumer) {
        this.serviceMessageSources.forEach(consumer);
    }
}
