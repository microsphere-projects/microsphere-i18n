package io.microsphere.i18n;

import io.microsphere.annotation.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.collection.MapUtils.isNotEmpty;
import static io.microsphere.text.FormatUtils.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * {@link Properties} Resource {@link ServiceMessageSource} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class PropertiesResourceServiceMessageSource extends AbstractResourceServiceMessageSource {

    public PropertiesResourceServiceMessageSource(String source) {
        super(source);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected final Map<String, String> loadMessages(String resource) {
        Map<String, String> messages = null;
        try {
            Properties properties = loadAllProperties(resource);
            if (isNotEmpty(properties)) {
                messages = new HashMap<>(properties.size());
                messages.putAll((Map) properties);
            }
        } catch (IOException e) {
            throw new RuntimeException(format("Source '{}' Messages Properties Resource[name : {}] loading is failed", this.source, resource), e);
        }
        return messages == null ? emptyMap() : unmodifiableMap(messages);
    }

    @Nullable
    public Properties loadAllProperties(Locale locale) throws IOException {
        String resource = getResource(locale);
        return loadAllProperties(resource);
    }

    @Nullable
    public Properties loadAllProperties(String resource) throws IOException {
        List<Reader> propertiesResources = loadAllPropertiesResources(resource);
        logger.trace("Source '{}' loads {} Properties Resources['{}']", this.source, propertiesResources.size(), resource);
        if (isEmpty(propertiesResources)) {
            return null;
        }
        Properties properties = new Properties();
        for (Reader propertiesResource : propertiesResources) {
            try (Reader reader = propertiesResource) {
                properties.load(reader);
            }
        }
        logger.trace("Source '{}' loads all Properties Resources[name :{}] : {}", this.source, resource, properties);
        return properties;
    }

    protected abstract String getResource(String resourceName);

    protected abstract List<Reader> loadAllPropertiesResources(String resource) throws IOException;
}