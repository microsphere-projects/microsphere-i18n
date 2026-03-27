package io.microsphere.i18n;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.i18n.util.MessageUtils.SOURCE_SEPARATOR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNoNullElements;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.StringUtils.isBlank;

/**
 * Abstract {@link ServiceMessageSource} providing base implementation for message resolution,
 * locale management, and message code resolution.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Typically used via concrete subclass such as DefaultServiceMessageSource
 *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
 *   source.setSupportedLocales(Arrays.asList(Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH));
 *   source.init();
 *   String msg = source.getMessage("hello", Locale.ENGLISH, "World"); // "Hello,World"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractServiceMessageSource implements ServiceMessageSource {

    protected final Logger logger = getLogger(getClass());

    protected final String source;

    protected final String codePrefix;

    private Set<Locale> supportedLocales;

    private Locale defaultLocale;

    /**
     * Constructs an {@link AbstractServiceMessageSource} with the given source name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
     * }</pre>
     *
     * @param source the source identifier, must not be null
     */
    public AbstractServiceMessageSource(String source) {
        assertNotNull(source, () -> "'source' argument must not be null");
        this.source = source;
        this.codePrefix = source + SOURCE_SEPARATOR;
    }

    @Override
    public final String getMessage(String code, Object... args) {
        return ServiceMessageSource.super.getMessage(code, args);
    }

    @Override
    public final String getMessage(String code, Locale locale, Object... args) {
        String message = null;
        if (code != null) {
            String resolvedCode = resolveMessageCode(code);
            Locale resolvedLocale = resolveLocale(locale);
            message = getInternalMessage(code, resolvedCode, locale, resolvedLocale, args);
            if (message == null) {
                resolvedLocale = new Locale(locale.getLanguage());
                if (!resolvedLocale.equals(locale)) {
                    message = getInternalMessage(code, resolvedCode, locale, resolvedLocale, args);
                }
            }
        }
        return message;
    }

    @Nonnull
    @Override
    public final Locale getLocale() {
        Locale locale = getInternalLocale();
        return locale == null ? getDefaultLocale() : locale;
    }

    /**
     * Get the internal {@link Locale}
     *
     * @return the internal {@link Locale}
     */
    @Nullable
    protected Locale getInternalLocale() {
        return null;
    }

    @Nonnull
    @Override
    public final Locale getDefaultLocale() {
        if (this.defaultLocale != null) {
            return this.defaultLocale;
        }
        return ServiceMessageSource.super.getDefaultLocale();
    }

    @Nonnull
    @Override
    public final Set<Locale> getSupportedLocales() {
        if (this.supportedLocales != null) {
            return this.supportedLocales;
        }
        return ServiceMessageSource.super.getSupportedLocales();
    }

    @Override
    public final String getSource() {
        return this.source;
    }

    /**
     * Sets the default {@link Locale} for this message source.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   source.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
     * }</pre>
     *
     * @param defaultLocale the default {@link Locale}
     */
    public final void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        this.logger.trace("Source '{}' sets the default Locale : '{}'", source, defaultLocale);
    }

    /**
     * Sets the supported {@link Locale locales} for this message source.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   source.setSupportedLocales(Arrays.asList(Locale.SIMPLIFIED_CHINESE, Locale.ENGLISH));
     * }</pre>
     *
     * @param supportedLocales the collection of supported {@link Locale locales}
     */
    public final void setSupportedLocales(Collection<Locale> supportedLocales) {
        this.supportedLocales = resolveHierarchicalLocales(supportedLocales);
        this.logger.trace("Source '{}' sets the supported Locales : {}", source, supportedLocales);
    }

    /**
     * Asserts that the given collection of supported locales is valid (non-empty, no null elements).
     *
     * @param supportedLocales the locales to validate
     * @throws IllegalArgumentException if the collection is empty or contains null elements
     */
    protected void assertSupportedLocales(Collection<Locale> supportedLocales) {
        assertNotEmpty(supportedLocales, () -> "The 'supportedLocales' must not be empty!");
        assertNoNullElements(supportedLocales, () -> "Any element of 'supportedLocales' must not be null!");
    }

    @Nonnull
    protected abstract String resolveMessageCode(@Nonnull String code);

    @Nullable
    protected abstract String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale,
                                                 Object... args);

    /**
     * Checks whether the given {@link Locale} is supported by this message source.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
     *   source.init();
     *   boolean supported = source.supports(Locale.ENGLISH); // true
     * }</pre>
     *
     * @param locale the {@link Locale} to check
     * @return {@code true} if the locale is supported
     */
    protected boolean supports(Locale locale) {
        Set<Locale> hierarchicalLocales = resolveHierarchicalLocales(locale);
        return hierarchicalLocales.contains(locale);
    }

    @Nonnull
    protected Set<Locale> resolveHierarchicalLocales(Collection<Locale> supportedLocales) {
        assertSupportedLocales(supportedLocales);
        Set<Locale> hierarchicalLocales = newFixedLinkedHashSet(supportedLocales.size() * 2);
        for (Locale supportedLocale : supportedLocales) {
            addLocale(hierarchicalLocales, supportedLocale);
            for (Locale locale : resolveHierarchicalLocales(supportedLocale)) {
                addLocale(hierarchicalLocales, locale);
            }
        }
        return hierarchicalLocales;
    }

    /**
     * Adds a {@link Locale} to the collection if not already present.
     *
     * @param locales the target collection
     * @param locale  the {@link Locale} to add
     */
    protected void addLocale(Collection<Locale> locales, Locale locale) {
        if (!locales.contains(locale)) {
            locales.add(locale);
        }
    }

    @Nonnull
    protected Set<Locale> resolveHierarchicalLocales(Locale locale) {
        Locale resolvedLocale = resolveLocale(locale);
        String country = resolvedLocale.getCountry();
        if (isBlank(country)) {
            return ofSet(locale);
        }
        return ofSet(resolvedLocale, new Locale(resolvedLocale.getLanguage()));
    }

    @Nonnull
    protected Locale resolveLocale(Locale locale) {
        String language = locale.getLanguage();
        String region = locale.getCountry();
        if (isBlank(region)) {
            return locale;
        }
        return new Locale(language, region);
    }

    @Nonnull
    protected String resolveMessage(String message, Object... args) {
        // Using FormatUtils#format, future subclasses may re-implement formatting
        return format(message, args);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " - " +
                "source = '" + this.source + '\'' +
                ", codePrefix = '" + this.codePrefix + '\'' +
                ", supportedLocales = " + getSupportedLocales() +
                ", defaultLocale = " + getDefaultLocale();
    }
}