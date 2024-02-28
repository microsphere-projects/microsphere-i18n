package io.microsphere.i18n;

import io.microsphere.lang.Prioritized;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

/**
 * Service internationalization message source
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ServiceMessageSource extends Prioritized {

    /**
     * Common internationalizing message sources
     */
    String COMMON_SOURCE = "common";

    /**
     * Initialize the life cycle
     */
    void init();

    /**
     * Destruction life cycle
     */
    void destroy();

    /**
     * Getting international Messages
     *
     * @param code   message Code
     * @param locale {@link Locale}
     * @param args   the argument of message pattern
     * @return <code>null</code> if message can't be found
     */
    @Nonnull
    String getMessage(String code, Locale locale, Object... args);

    default String getMessage(String code, Object... args) {
        return getMessage(code, getLocale(), args);
    }

    /**
     * Get the runtime {@link Locale}
     *
     * @return {@link Locale}
     */
    @Nonnull
    Locale getLocale();

    /**
     * Get the default {@link Locale}
     *
     * @return {@link Locale#SIMPLIFIED_CHINESE} as default
     */
    @Nonnull
    default Locale getDefaultLocale() {
        return Locale.SIMPLIFIED_CHINESE;
    }

    /**
     * Gets a list of supported {@link Locale}
     *
     * @return Non-null {@link List}, simplified Chinese and English by default
     */
    @Nonnull
    default List<Locale> getSupportedLocales() {
        return asList(getDefaultLocale(), Locale.ENGLISH);
    }

    /**
     * Message service source
     *
     * @return The application name or {@link #COMMON_SOURCE}
     */
    default String getSource() {
        return COMMON_SOURCE;
    }
}
