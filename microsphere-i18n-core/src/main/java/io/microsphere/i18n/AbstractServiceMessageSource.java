package io.microsphere.i18n;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static io.microsphere.i18n.util.MessageUtils.SOURCE_SEPARATOR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.StringUtils.isNotBlank;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

/**
 * Abstract {@link ServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractServiceMessageSource implements ServiceMessageSource {

    protected final Logger logger = getLogger(getClass());

    protected final String source;

    protected final String codePrefix;

    private List<Locale> supportedLocales;

    private Locale defaultLocale;

    public AbstractServiceMessageSource(String source) {
        assertNotNull(source, () -> "'source' argument must not be null");
        this.source = source;
        this.codePrefix = source + SOURCE_SEPARATOR;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
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
            if (resolvedCode != null) {
                Locale resolvedLocale = resolveLocale(locale);
                message = getInternalMessage(code, resolvedCode, locale, resolvedLocale, args);
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
        if (defaultLocale != null) {
            return defaultLocale;
        }
        return ServiceMessageSource.super.getDefaultLocale();
    }

    @Nonnull
    @Override
    public final List<Locale> getSupportedLocales() {
        if (supportedLocales != null) {
            return supportedLocales;
        }
        return ServiceMessageSource.super.getSupportedLocales();
    }

    @Override
    public final String getSource() {
        return source;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        logger.trace("Source '{}' sets the default Locale : '{}'", source, defaultLocale);
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = resolveLocales(supportedLocales);
        logger.trace("Source '{}' sets the supported Locales : {}", source, supportedLocales);
    }

    protected String resolveMessageCode(String code) {
        return code;
    }

    protected Locale resolveLocale(Locale locale) {
        return locale;
    }

    protected abstract String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object... args);

    protected boolean supports(Locale locale) {
        return getSupportedLocales().contains(locale);
    }

    protected static List<Locale> resolveLocales(List<Locale> supportedLocales) {
        List<Locale> resolvedLocales = new LinkedList<>();
        for (Locale supportedLocale : supportedLocales) {
            addLocale(resolvedLocales, supportedLocale);
            for (Locale derivedLocale : resolveDerivedLocales(supportedLocale)) {
                addLocale(resolvedLocales, derivedLocale);
            }
        }
        return unmodifiableList(resolvedLocales);
    }

    protected static void addLocale(List<Locale> locales, Locale locale) {
        if (!locales.contains(locale)) {
            locales.add(locale);
        }
    }

    protected static List<Locale> resolveDerivedLocales(Locale locale) {
        String language = locale.getLanguage();
        String region = locale.getCountry();
        String variant = locale.getVariant();

        boolean hasRegion = isNotBlank(region);
        boolean hasVariant = isNotBlank(variant);

        if (!hasRegion && !hasVariant) {
            return emptyList();
        }

        List<Locale> derivedLocales = new LinkedList<>();

        if (hasVariant) {
            derivedLocales.add(new Locale(language, region));
        }

        if (hasRegion) {
            derivedLocales.add(new Locale(language));
        }

        return derivedLocales;
    }


    protected String resolveMessage(String message, Object... args) {
        // Using FormatUtils#format, future subclasses may re-implement formatting
        return format(message, args);
    }

}
