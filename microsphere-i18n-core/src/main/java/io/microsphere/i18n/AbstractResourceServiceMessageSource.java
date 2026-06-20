package io.microsphere.i18n;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Abstract Resource {@link ServiceMessageSource} Class that loads messages from resources
 * keyed by {@link Locale}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Typically used through DefaultServiceMessageSource
 *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
 *   source.init();
 *   Map<String, Map<String, String>> messages = source.getLocalizedResourceMessages();
 *   // messages keyed by resource name, e.g. "META-INF/i18n/test/i18n_messages_zh_CN.properties"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractResourceServiceMessageSource extends AbstractServiceMessageSource implements
        ResourceServiceMessageSource {

    /**
     * The default prefix of of message resource name
     */
    public static final String DEFAULT_RESOURCE_NAME_PREFIX = "i18n_messages_";

    /**
     * The default suffix of message resource name
     */
    public static final String DEFAULT_RESOURCE_NAME_SUFFIX = ".properties";

    private volatile Map<String, Map<String, String>> localizedResourceMessages = emptyMap();

    /**
     * Constructs with the given source identifier.
     *
     * @param source the source identifier
     */
    public AbstractResourceServiceMessageSource(String source) {
        super(source);
    }

    @Override
    public void init() {
        assertNotNull(this.source, () -> "The 'source' attribute must be assigned before initialization!");
        initialize();
    }

    @Override
    public void initializeResource(String resource) {
        initializeResources(singleton(resource));
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        synchronized (this) {
            // Copy the current messages and initialized resources
            Map<String, Map<String, String>> localizedResourceMessages = new HashMap<>(this.localizedResourceMessages);
            initializeResources(resources, localizedResourceMessages);
            // Exchange the field
            this.localizedResourceMessages = localizedResourceMessages;
        }
    }

    @Override
    public void destroy() {
        clearAllMessages();
    }

    @Override
    protected String resolveMessageCode(String code) {
        if (code.startsWith(codePrefix)) { // The complete Message code
            return code;
        }
        return codePrefix + code;
    }

    @Override
    protected String getInternalMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object[] args) {
        String message = null;
        Map<String, String> messages = getMessages(resolvedLocale);
        if (messages != null) {
            String messagePattern = messages.get(resolvedCode);
            if (messagePattern != null) {
                message = resolveMessage(messagePattern, args);
                logMessage(code, resolvedCode, locale, resolvedLocale, args, messagePattern, message);
            }
        }
        return message;
    }

    /**
     * Initialization
     */
    protected final void initialize() {
        Set<String> resources = getResources();
        Map<String, Map<String, String>> localizedResourceMessages = new HashMap<>(resources.size());
        for (String resource : resources) {
            initializeResource(resource, localizedResourceMessages);
        }
        // Exchange the field
        this.localizedResourceMessages = localizedResourceMessages;
        logger.trace("The initialization[Source '{}'] is completed , localizedResourceMessages : {}", source, localizedResourceMessages);
    }

    protected final void clearAllMessages() {
        this.localizedResourceMessages.clear();
        this.localizedResourceMessages = null;
    }

    private void validateMessages(Map<String, String> messages, String resourceName) {
        messages.forEach((code, message) -> validateMessageCode(code, resourceName));
    }

    /**
     * Validates a message code for the given resource.
     *
     * @param code         the message code
     * @param resourceName the resource name
     * @throws IllegalStateException if the code is invalid
     */
    protected void validateMessageCode(String code, String resourceName) {
        validateMessageCodePrefix(code, resourceName);
    }

    private void validateMessageCodePrefix(String code, String resourceName) {
        if (!code.startsWith(codePrefix)) {
            throw new IllegalStateException(format("Source '{}' Message Resource[name : '{}'] code '{}' must start with '{}'",
                    source, resourceName, code, codePrefix));
        }
    }

    /**
     * Gets the resource path for the specified {@link Locale}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   String resource = source.getResource(Locale.ENGLISH);
     *   // e.g. "META-INF/i18n/test/i18n_messages_en.properties"
     * }</pre>
     *
     * @param locale the target {@link Locale}
     * @return the resource path
     */
    public String getResource(Locale locale) {
        String resourceName = buildResourceName(locale);
        return getResource(resourceName);
    }

    /**
     * Builds the resource name for the given {@link Locale}.
     *
     * @param locale the target {@link Locale}
     * @return the resource name, e.g. "i18n_messages_en.properties"
     */
    protected String buildResourceName(Locale locale) {
        return DEFAULT_RESOURCE_NAME_PREFIX + locale + DEFAULT_RESOURCE_NAME_SUFFIX;
    }

    protected abstract String getResource(String resourceName);

    @Nonnull
    protected abstract Map<String, String> loadMessages(String resource);

    /**
     * Gets the message map for the specified {@link Locale}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Map<String, String> messages = source.getMessages(Locale.SIMPLIFIED_CHINESE);
     *   // e.g. {"test.a": "测试-a", "test.hello": "您好,{}"}
     * }</pre>
     *
     * @param locale the target {@link Locale}
     * @return the message map, or {@code null} if no messages for the locale
     */
    @Nullable
    public final Map<String, String> getMessages(Locale locale) {
        String resource = getResource(locale);
        return localizedResourceMessages.get(resource);
    }

    private void initializeResources(Iterable<String> resources, Map<String, Map<String, String>> localizedResourceMessages) {
        for (String resource : resources) {
            initializeResource(resource, localizedResourceMessages);
        }
    }

    private void initializeResource(String resource, Map<String, Map<String, String>> localizedResourceMessages) {
        Map<String, String> messages = loadMessages(resource);
        logger.trace("The loaded resource[name : '{}' ,source : '{}'] messages : {}", resource, this.source, messages);
        assertNotNull(messages, () -> format("The loaded resource[name : '{}' ,source : '{}'] messages must not be null", resource, this.source));
        validateMessages(messages, resource);
        // Override the localized message if present
        localizedResourceMessages.put(resource, messages);
    }

    /**
     * Logs a resolved message for debugging purposes.
     *
     * @param code           the original message code
     * @param resolvedCode   the resolved message code
     * @param locale         the requested locale
     * @param resolvedLocale the resolved locale
     * @param args           the message arguments
     * @param messagePattern the raw message pattern
     * @param message        the resolved message
     */
    protected void logMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object[] args,
                              String messagePattern, String message) {
        logger.trace("Source '{}' gets Message[code : '{}' , resolvedCode : '{}' , locale : '{}' , resolvedLocale : '{}', args : '{}' , pattern : '{}'] : '{}'",
                source, code, resolvedCode, locale, resolvedLocale, arrayToString(args), messagePattern, message);
    }

    /**
     * Gets all localized resource messages as an unmodifiable map.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
     *   source.init();
     *   Map<String, Map<String, String>> all = source.getLocalizedResourceMessages();
     *   // 2 entries for zh_CN and en locales
     * }</pre>
     *
     * @return unmodifiable map of resource name to message code-value pairs
     */
    public Map<String, Map<String, String>> getLocalizedResourceMessages() {
        return unmodifiableMap(this.localizedResourceMessages);
    }

    @Override
    public Set<String> getInitializedResources() {
        return localizedResourceMessages.keySet();
    }

    /**
     * Get the resources around {@link #getSupportedLocales() supported locales}.
     *
     * @return non-null
     * @see #getSupportedLocales()
     */
    @Nonnull
    public final Set<String> getResources() {
        Set<Locale> supportedLocales = getSupportedLocales();
        Set<String> resources = newFixedLinkedHashSet(supportedLocales.size());
        for (Locale supportedLocale : supportedLocales) {
            String resource = getResource(supportedLocale);
            resources.add(resource);
        }
        return unmodifiableSet(resources);
    }

    @Override
    public String toString() {
        return super.toString() + ", localizedResourceMessages = " + this.localizedResourceMessages;
    }
}
