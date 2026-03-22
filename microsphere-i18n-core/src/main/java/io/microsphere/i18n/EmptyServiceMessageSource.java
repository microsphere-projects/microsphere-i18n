package io.microsphere.i18n;

import io.microsphere.annotation.Nonnull;

import java.util.Locale;

/**
 * Empty {@link ServiceMessageSource} that always returns {@code null} for any message code.
 * Used as a no-op fallback.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   ServiceMessageSource empty = EmptyServiceMessageSource.INSTANCE;
 *   empty.init();
 *   assertNull(empty.getMessage("any-code"));
 *   assertEquals("Empty", empty.getSource());
 *   empty.destroy();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class EmptyServiceMessageSource implements ServiceMessageSource {

    public static final EmptyServiceMessageSource INSTANCE = new EmptyServiceMessageSource();

    /**
     * Default constructor.
     */
    public EmptyServiceMessageSource() {
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return null;
    }

    @Nonnull
    @Override
    public Locale getLocale() {
        return getDefaultLocale();
    }

    @Override
    public String getSource() {
        return "Empty";
    }
}
