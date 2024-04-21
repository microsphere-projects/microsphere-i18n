package io.microsphere.i18n;

import io.microsphere.collection.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.text.FormatUtils.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

/**
 * Abstract Resource {@link ServiceMessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractResourceServiceMessageSource extends AbstractServiceMessageSource implements ResourceServiceMessageSource {

    /**
     * Message Resource name prefix
     */
    protected static final String RESOURCE_NAME_PREFIX = "i18n_messages_";

    /**
     * Message Resource Name Suffix
     */
    protected static final String RESOURCE_NAME_SUFFIX = ".properties";

    private volatile Map<Locale, Map<String, String>> localizedMessages = emptyMap();

    private volatile Set<String> initializedResources = emptySet();

    public AbstractResourceServiceMessageSource(String source) {
        super(source);
    }

    @Override
    public void init() {
        requireNonNull(this.source, "The 'source' attribute must be assigned before initialization!");
        initialize();
    }

    @Override
    public void initializeResource(String resource) {
        initializeResources(singleton(resource));
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        List<Locale> supportedLocales = getSupportedLocales();
        assertSupportedLocales(supportedLocales);

        // Copy the current messages and initialized resources
        Map<Locale, Map<String, String>> localizedMessages = new HashMap<>(this.localizedMessages);
        Set<String> initializedResources = new LinkedHashSet<>(this.initializedResources);

        initializeResources(resources, supportedLocales, localizedMessages, initializedResources);

        // Exchange the field
        synchronized (this) {
            this.localizedMessages = localizedMessages;
            this.initializedResources = initializedResources;
        }
    }

    @Override
    public void destroy() {
        clearAllMessages();
    }

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
        String messagePattern = messages.get(resolvedCode);
        if (messagePattern != null) {
            message = formatMessage(messagePattern, args);
            logMessage(code, resolvedCode, locale, resolvedLocale, args, messagePattern, message);
        }
        return message;
    }

    protected void logMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object[] args,
                              String messagePattern, String message) {
        if (logger.isDebugEnabled()) {
            logger.debug("Source '{}' gets Message[code : '{}' , resolvedCode : '{}' , locale : '{}' , resolvedLocale : '{}', args : '{}' , pattern : '{}'] : '{}'",
                    source, code, resolvedCode, locale, resolvedLocale, ArrayUtils.toString(args), messagePattern, message);
        }
    }

    @Override
    public Set<String> getInitializeResources() {
        return unmodifiableSet(initializedResources);
    }

    /**
     * Initialization
     */
    protected final void initialize() {
        List<Locale> supportedLocales = getSupportedLocales();
        assertSupportedLocales(supportedLocales);
        Map<Locale, Map<String, String>> localizedMessages = new HashMap<>(supportedLocales.size());
        Set<String> initializedResources = new LinkedHashSet<>();
        for (Locale resolveLocale : supportedLocales) {
            String resource = getResource(resolveLocale);
            initializeResource(resource, resolveLocale, localizedMessages, initializedResources);
        }
        // Exchange the field
        synchronized (this) {
            this.localizedMessages = localizedMessages;
            this.initializedResources = initializedResources;
        }
        logger.debug("Source '{}' Initialization is completed , resources : {} , localizedMessages : {}", source, initializedResources, localizedMessages);
    }

    private void assertSupportedLocales(List<Locale> supportedLocales) {
        if (CollectionUtils.isEmpty(supportedLocales)) {
            throw new IllegalStateException(format("{}.getSupportedLocales() Methods cannot return an empty list of locales!", this.getClass()));
        }
    }

    protected final void clearAllMessages() {
        this.localizedMessages.clear();
        this.initializedResources.clear();
        this.localizedMessages = null;
        this.initializedResources = null;
    }

    private void validateMessages(Map<String, String> messages, String resourceName) {
        messages.forEach((code, message) -> validateMessageCode(code, resourceName));
    }

    protected void validateMessageCode(String code, String resourceName) {
        validateMessageCodePrefix(code, resourceName);
    }

    private void validateMessageCodePrefix(String code, String resourceName) {
        if (!code.startsWith(codePrefix)) {
            throw new IllegalStateException(format("Source '{}' Message Resource[name : '{}'] code '{}' must start with '{}'",
                    source, resourceName, code, codePrefix));
        }
    }

    private String getResource(Locale locale) {
        String resourceName = getResourceName(locale);
        return getResource(resourceName);
    }

    private String getResourceName(Locale locale) {
        return RESOURCE_NAME_PREFIX + locale + RESOURCE_NAME_SUFFIX;
    }

    protected abstract String getResource(String resourceName);

    protected abstract Map<String, String> loadMessages(Locale locale, String resource);

    public final Map<String, String> getMessages(Locale locale) {
        return localizedMessages.getOrDefault(locale, emptyMap());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(getClass().getSimpleName())
                .append("{source='").append(source).append('\'')
                .append(", initializedResources=").append(initializedResources)
                .append(", defaultLocale=").append(getDefaultLocale())
                .append(", supportedLocales=").append(getSupportedLocales())
                .append(", localizedMessages=").append(localizedMessages)
                .append('}');
        return sb.toString();
    }

    private void initializeResources(Iterable<String> resources, List<Locale> supportedLocales,
                                     Map<Locale, Map<String, String>> localizedMessages, Set<String> initializedResources) {

        for (Locale locale : supportedLocales) {
            initializeResources(resources, locale, localizedMessages, initializedResources);
        }
    }

    private void initializeResources(Iterable<String> resources, Locale locale, Map<Locale, Map<String, String>> localizedMessages,
                                     Set<String> initializedResources) {
        for (String resource : resources) {
            initializeResource(resource, locale, localizedMessages, initializedResources);
        }
    }

    private void initializeResource(String resource, Locale locale, Map<Locale, Map<String, String>> localizedMessages,
                                    Set<String> initializedResources) {
        Map<String, String> messages = loadMessages(locale, resource);
        validateMessages(messages, resource);
        if (!isEmpty(messages)) {
            logger.debug("Source '{}' loads the Locale '{}' resource['{}'] messages : {}", source, locale, resource, messages);
        } else {
            logger.debug("Source '{}' Locale '{}' resource not found['{}'] messages", source, locale, resource);
        }
        initializedResources.add(resource);
        // Override the localized message if present
        localizedMessages.put(locale, messages);
    }
}
