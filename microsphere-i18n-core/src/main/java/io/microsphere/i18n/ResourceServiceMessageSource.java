package io.microsphere.i18n;

import io.microsphere.annotation.Nonnull;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Resource {@link ServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ResourceServiceMessageSource extends ServiceMessageSource {

    /**
     * Initializes the resource
     *
     * @param resource the resource to be loaded for messages
     */
    void initializeResource(String resource);

    /**
     * Initializes the multiple resources
     *
     * @param resources the resources to be loaded for messages
     */
    default void initializeResources(Iterable<String> resources) {
        resources.forEach(this::initializeResource);
    }

    /**
     * Gets all initialized {@link Locale} resources
     *
     * @return all initialized {@link Locale} resources
     */
    @Nonnull
    Set<String> getInitializedResources();

    /**
     * Gets the resource content character encoding
     *
     * @return The default is utf-8
     */
    default Charset getEncoding() {
        return UTF_8;
    }
}