package io.microsphere.i18n;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.Prioritized;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.getDefault;

/**
 * Service internationalization message source.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   ServiceMessageSource source = new DefaultServiceMessageSource("test");
 *   source.init();
 *   // Get message in default locale (e.g. Simplified Chinese)
 *   String msg = source.getMessage("a"); // "测试-a"
 *   // Get message in a specific locale
 *   String engMsg = source.getMessage("hello", Locale.ENGLISH, "World"); // "Hello,World"
 *   source.destroy();
 * }</pre>
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
    @Nullable
    String getMessage(String code, Locale locale, Object... args);

    /**
     * Getting international Messages using the runtime {@link Locale}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ServiceMessageSource source = new DefaultServiceMessageSource("test");
     *   source.init();
     *   String msg = source.getMessage("a"); // "测试-a"
     *   String hello = source.getMessage("hello", "World"); // "您好,World"
     * }</pre>
     *
     * @param code message Code
     * @param args the argument of message pattern
     * @return {@code null} if message can't be found
     */
    @Nullable
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
     * @return {@link Locale#getDefault()} as default
     */
    @Nonnull
    default Locale getDefaultLocale() {
        return getDefault();
    }

    /**
     * Gets the supported {@link Locale locales}
     *
     * @return Non-null {@link List}, simplified Chinese and English by default
     */
    @Nonnull
    default Set<Locale> getSupportedLocales() {
        return ofSet(getDefaultLocale(), ENGLISH);
    }

    /**
     * Message service source
     *
     * @return The application name or {@link #COMMON_SOURCE}
     */
    default String getSource() {
        return COMMON_SOURCE;
    }

    /**
     * The {@link String} representation of this message service source
     *
     * @return non-null
     */
    @Override
    @Nonnull
    String toString();
}
