package io.microsphere.i18n.util;

import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.EmptyServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Internationalization Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class I18nUtils {

    private static final Logger logger = LoggerFactory.getLogger(I18nUtils.class);

    private static volatile ServiceMessageSource serviceMessageSource;

    public static ServiceMessageSource serviceMessageSource() {
        if (serviceMessageSource == null) {
            logger.warn("serviceMessageSource is not initialized, EmptyServiceMessageSource will be used");
            return EmptyServiceMessageSource.INSTANCE;
        }
        return serviceMessageSource;
    }

    public static void setServiceMessageSource(ServiceMessageSource serviceMessageSource) {
        I18nUtils.serviceMessageSource = serviceMessageSource;
        logger.debug("I18nUtils.serviceMessageSource is initialized : {}", serviceMessageSource);
    }

    public static void destroyServiceMessageSource() {
        serviceMessageSource = null;
        logger.debug("serviceMessageSource is destroyed");
    }

    public static List<ServiceMessageSource> findAllServiceMessageSources(ServiceMessageSource serviceMessageSource) {
        List<ServiceMessageSource> allServiceMessageSources = new LinkedList<>();
        initServiceMessageSources(serviceMessageSource, allServiceMessageSources);
        return unmodifiableList(allServiceMessageSources);
    }

    public static void initServiceMessageSources(ServiceMessageSource serviceMessageSource,
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
}
