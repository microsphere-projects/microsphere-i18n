package io.microsphere.i18n;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.ListUtils.forEach;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * The Composite {@link ServiceMessageSource} class, No thread safe.
 * Delegates to a list of child {@link ServiceMessageSource} instances.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
 *   CompositeServiceMessageSource composite = new CompositeServiceMessageSource(
 *       Arrays.asList(EmptyServiceMessageSource.INSTANCE, source));
 *   composite.init();
 *   String msg = composite.getMessage("a"); // "测试-a" (from the DefaultServiceMessageSource)
 *   composite.destroy();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractServiceMessageSource
 * @see ServiceMessageSource
 * @see ResourceServiceMessageSource
 * @see ReloadableResourceServiceMessageSource
 * @since 1.0.0
 */
public class CompositeServiceMessageSource implements ReloadableResourceServiceMessageSource {

    private static final Logger logger = getLogger(CompositeServiceMessageSource.class);

    private List<? extends ServiceMessageSource> serviceMessageSources;

    /**
     * Constructs an empty {@link CompositeServiceMessageSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   CompositeServiceMessageSource composite = new CompositeServiceMessageSource();
     * }</pre>
     */
    public CompositeServiceMessageSource() {
        this.serviceMessageSources = emptyList();
    }

    /**
     * Constructs a {@link CompositeServiceMessageSource} with the given list of message sources.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   CompositeServiceMessageSource composite = new CompositeServiceMessageSource(
     *       Arrays.asList(EmptyServiceMessageSource.INSTANCE, new DefaultServiceMessageSource("test")));
     * }</pre>
     *
     * @param serviceMessageSources the list of {@link ServiceMessageSource} instances
     */
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
        for (ServiceMessageSource serviceMessageSource : this.serviceMessageSources) {
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
    public Set<Locale> getSupportedLocales() {
        Set<Locale> supportedLocales = new LinkedHashSet<>();
        iterate(serviceMessageSource -> {
            for (Locale locale : serviceMessageSource.getSupportedLocales()) {
                supportedLocales.add(locale);
            }
        });

        return supportedLocales.isEmpty() ? getDefaultSupportedLocales() :
                unmodifiableSet(supportedLocales);
    }

    /**
     * Gets the default supported locales from the parent interface.
     *
     * @return default supported locales set
     */
    public Set<Locale> getDefaultSupportedLocales() {
        return ReloadableResourceServiceMessageSource.super.getSupportedLocales();
    }

    @Override
    public String getSource() {
        return ReloadableResourceServiceMessageSource.super.getSource();
    }

    /**
     * Sets and sorts the list of composited {@link ServiceMessageSource} instances.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   composite.setServiceMessageSources(Arrays.asList(source1, source2));
     * }</pre>
     *
     * @param serviceMessageSources the list of {@link ServiceMessageSource} instances
     */
    public void setServiceMessageSources(List<? extends ServiceMessageSource> serviceMessageSources) {
        List<ServiceMessageSource> newServiceMessageSources = new ArrayList<>(serviceMessageSources);
        sort(newServiceMessageSources);

        List<? extends ServiceMessageSource> oldServiceMessageSources = this.serviceMessageSources;
        this.serviceMessageSources = newServiceMessageSources;
        if (logger.isTraceEnabled()) {
            logger.trace("The ServiceMessageSource original: '{}' , sorted : '{}'", serviceMessageSources, newServiceMessageSources);
        }

        if (oldServiceMessageSources != null) {
            oldServiceMessageSources.clear();
        }
    }

    @Override
    public void reload(Iterable<String> changedResources) {
        iterate(ReloadableResourceServiceMessageSource.class, reloadableResourceServiceMessageSource -> {
            if (reloadableResourceServiceMessageSource.canReload(changedResources)) {
                reloadableResourceServiceMessageSource.reload(changedResources);
                if (logger.isTraceEnabled()) {
                    logger.trace("The '{}' reloaded the changed resources: {}", reloadableResourceServiceMessageSource, changedResources);
                }
            }
        });
    }

    @Override
    public void reload(String changedResource) {
        reload(ofSet(changedResource));
    }

    @Override
    public boolean canReload(String changedResource) {
        return true;
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
    public Set<String> getInitializedResources() {
        Set<String> resources = new LinkedHashSet<>();
        iterate(ResourceServiceMessageSource.class, resourceServiceMessageSource -> {
            resources.addAll(resourceServiceMessageSource.getInitializedResources());
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
        return this.getClass().getSimpleName() + " - serviceMessageSources = " + serviceMessageSources + '}';
    }

    private ServiceMessageSource getFirstServiceMessageSource() {
        return first(this.serviceMessageSources);
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
