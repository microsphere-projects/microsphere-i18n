package io.microsphere.i18n.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.logging.Logger;

import java.util.LinkedList;
import java.util.List;

import static io.microsphere.i18n.EmptyServiceMessageSource.INSTANCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * Internationalization Utilities class providing global access to the
 * {@link ServiceMessageSource} and utility methods for service message source discovery.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Set the global ServiceMessageSource
 *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
 *   I18nUtils.setServiceMessageSource(source);
 *   assertSame(source, I18nUtils.serviceMessageSource());
 *
 *   // Find all leaf ServiceMessageSources in a composite
 *   CompositeServiceMessageSource composite = new CompositeServiceMessageSource(
 *       Arrays.asList(EmptyServiceMessageSource.INSTANCE));
 *   List<ServiceMessageSource> all = I18nUtils.findAllServiceMessageSources(composite);
 *
 *   I18nUtils.destroyServiceMessageSource();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class I18nUtils {

    private static final Logger logger = getLogger(I18nUtils.class);

    private static volatile ServiceMessageSource serviceMessageSource;

    /**
     * Returns the global {@link ServiceMessageSource}, or {@link EmptyServiceMessageSource#INSTANCE}
     * if not initialized.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ServiceMessageSource source = I18nUtils.serviceMessageSource();
     * }</pre>
     *
     * @return the current {@link ServiceMessageSource}, never null
     */
    @Nonnull
    public static ServiceMessageSource serviceMessageSource() {
        if (serviceMessageSource == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("serviceMessageSource is not initialized, EmptyServiceMessageSource will be used");
            }
            return INSTANCE;
        }
        return serviceMessageSource;
    }

    /**
     * Sets the global {@link ServiceMessageSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   I18nUtils.setServiceMessageSource(new DefaultServiceMessageSource("test"));
     * }</pre>
     *
     * @param serviceMessageSource the {@link ServiceMessageSource} to set
     */
    public static void setServiceMessageSource(ServiceMessageSource serviceMessageSource) {
        I18nUtils.serviceMessageSource = serviceMessageSource;
        if (logger.isTraceEnabled()) {
            logger.trace("I18nUtils.serviceMessageSource is initialized : {}", serviceMessageSource);
        }
    }

    /**
     * Destroys (nullifies) the global {@link ServiceMessageSource}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   I18nUtils.destroyServiceMessageSource();
     * }</pre>
     */
    public static void destroyServiceMessageSource() {
        serviceMessageSource = null;
        if (logger.isTraceEnabled()) {
            logger.trace("serviceMessageSource is destroyed");
        }
    }

    /**
     * Recursively finds all leaf {@link ServiceMessageSource} instances within a composite hierarchy.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   CompositeServiceMessageSource composite = new CompositeServiceMessageSource(
     *       Arrays.asList(EmptyServiceMessageSource.INSTANCE));
     *   List<ServiceMessageSource> all = I18nUtils.findAllServiceMessageSources(composite);
     *   // returns [EmptyServiceMessageSource.INSTANCE]
     * }</pre>
     *
     * @param serviceMessageSource the root {@link ServiceMessageSource}
     * @return unmodifiable list of all leaf sources
     */
    public static List<ServiceMessageSource> findAllServiceMessageSources(ServiceMessageSource serviceMessageSource) {
        return unmodifiableList(doFindAllServiceMessageSources(serviceMessageSource));
    }

    /**
     * Recursively finds all leaf {@link ServiceMessageSource} instances of a specific type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   List<EmptyServiceMessageSource> sources =
     *       I18nUtils.findAllServiceMessageSources(composite, EmptyServiceMessageSource.class);
     * }</pre>
     *
     * @param serviceMessageSource     the root {@link ServiceMessageSource}
     * @param serviceMessageSourceType the type to filter by
     * @param <S>                      the target type
     * @return unmodifiable list of matching sources
     */
    public static <S extends ServiceMessageSource> List<S> findAllServiceMessageSources(ServiceMessageSource serviceMessageSource, Class<S> serviceMessageSourceType) {
        return unmodifiableList(doFindAllServiceMessageSources(serviceMessageSource)
                .stream()
                .filter(serviceMessageSourceType::isInstance)
                .map(serviceMessageSourceType::cast)
                .collect(toList()));
    }

    static List<ServiceMessageSource> doFindAllServiceMessageSources(ServiceMessageSource serviceMessageSource) {
        List<ServiceMessageSource> allServiceMessageSources = new LinkedList<>();
        initServiceMessageSources(serviceMessageSource, allServiceMessageSources);
        return allServiceMessageSources;
    }

    static void initServiceMessageSources(ServiceMessageSource serviceMessageSource,
                                          List<ServiceMessageSource> allServiceMessageSources) {
        if (serviceMessageSource instanceof CompositeServiceMessageSource) {
            CompositeServiceMessageSource compositeServiceMessageSource = (CompositeServiceMessageSource) serviceMessageSource;
            for (ServiceMessageSource subServiceMessageSource : compositeServiceMessageSource.getServiceMessageSources()) {
                initServiceMessageSources(subServiceMessageSource, allServiceMessageSources);
            }
        } else {
            allServiceMessageSources.add(serviceMessageSource);
        }
    }

    private I18nUtils() {
    }
}
