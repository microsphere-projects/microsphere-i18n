package io.microsphere.i18n;

import io.microsphere.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.MapUtils.isEmpty;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;

/**
 * Abstract Resource {@link ServiceMessageSource} Class
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
        Set<Locale> supportedLocales = getSupportedLocales();
        Set<Locale> hierarchicalLocales = resolveHierarchicalLocales(supportedLocales);
        Map<String, Map<String, String>> localizedResourceMessages = new HashMap<>(hierarchicalLocales.size());
        for (Locale hierarchicalLocale : hierarchicalLocales) {
            String resource = getResource(hierarchicalLocale);
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

    protected void validateMessageCode(String code, String resourceName) {
        validateMessageCodePrefix(code, resourceName);
    }

    private void validateMessageCodePrefix(String code, String resourceName) {
        if (!code.startsWith(codePrefix)) {
            throw new IllegalStateException(format("Source '{}' Message Resource[name : '{}'] code '{}' must start with '{}'",
                    source, resourceName, code, codePrefix));
        }
    }

    public String getResource(Locale locale) {
        String resourceName = buildResourceName(locale);
        return getResource(resourceName);
    }

    protected String buildResourceName(Locale locale) {
        return DEFAULT_RESOURCE_NAME_PREFIX + locale + DEFAULT_RESOURCE_NAME_SUFFIX;
    }

    protected abstract String getResource(String resourceName);

    @Nullable
    protected abstract Map<String, String> loadMessages(String resource);

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
        logger.trace("Source '{}' loads the resource['{}'] messages : {}", source, resource, messages);

        if (isEmpty(messages)) {
            return;
        }

        validateMessages(messages, resource);
        // Override the localized message if present
        localizedResourceMessages.put(resource, messages);
    }

    protected void logMessage(String code, String resolvedCode, Locale locale, Locale resolvedLocale, Object[] args,
                              String messagePattern, String message) {
        logger.trace("Source '{}' gets Message[code : '{}' , resolvedCode : '{}' , locale : '{}' , resolvedLocale : '{}', args : '{}' , pattern : '{}'] : '{}'",
                source, code, resolvedCode, locale, resolvedLocale, arrayToString(args), messagePattern, message);
    }

    public Map<String, Map<String, String>> getLocalizedResourceMessages() {
        return unmodifiableMap(this.localizedResourceMessages);
    }

    @Override
    public Set<String> getInitializedResources() {
        return localizedResourceMessages.keySet();
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "source='" + this.source + '\'' +
                ", codePrefix='" + this.codePrefix + '\'' +
                ", supportedLocales=" + getSupportedLocales() +
                ", defaultLocale=" + getDefaultLocale() +
                ", localizedResourceMessages=" + this.localizedResourceMessages +
                '}';
    }
}