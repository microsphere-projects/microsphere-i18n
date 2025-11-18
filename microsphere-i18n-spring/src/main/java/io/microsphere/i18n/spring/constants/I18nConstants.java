package io.microsphere.i18n.spring.constants;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.i18n.ServiceMessageSource;

import java.util.List;
import java.util.Locale;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;

/**
 * Internationalization property constants
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface I18nConstants {

    String PROPERTY_NAME_PREFIX = "microsphere.i18n.";

    /**
     * Enabled Configuration Name
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = "true",
            source = APPLICATION_SOURCE
    )
    String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "enabled";

    /**
     * Enabled By Default
     */
    boolean DEFAULT_ENABLED = true;

    /**
     * The property name of the {@link ServiceMessageSource#getSource() sources} of {@link ServiceMessageSource}
     *
     * @see ServiceMessageSource#getSource()
     */
    @ConfigurationProperty(
            type = String[].class,
            source = APPLICATION_SOURCE
    )
    String SOURCES_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "sources";

    /**
     * Default {@link Locale} property name
     */
    @ConfigurationProperty(
            type = Locale.class,
            source = APPLICATION_SOURCE
    )
    String DEFAULT_LOCALE_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "default-locale";

    /**
     * Supported {@link Locale} list property names
     */
    @ConfigurationProperty(
            type = List.class,
            source = APPLICATION_SOURCE
    )
    String SUPPORTED_LOCALES_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "supported-locales";

    /**
     * The Primary {@link ServiceMessageSource} Bean Bean
     */
    String SERVICE_MESSAGE_SOURCE_BEAN_NAME = "serviceMessageSource";
}