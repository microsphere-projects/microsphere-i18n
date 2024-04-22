package io.microsphere.i18n;

import io.microsphere.collection.MapUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static io.microsphere.collection.CollectionUtils.isEmpty;
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

    @Override
    protected final Map<String, String> loadMessages(String resource) {
        Map<String, String> messages = emptyMap();
        try {
            Properties properties = loadAllProperties(resource);
            if (!MapUtils.isEmpty(properties)) {
                messages = new HashMap<>(properties.size());
                messages.putAll((Map) properties);
            }
        } catch (IOException e) {
            throw new RuntimeException(format("Source '{}' Messages Properties Resource[name : {}] loading is failed", source, resource), e);
        }
        return unmodifiableMap(messages);
    }

    private Properties loadAllProperties(String resource) throws IOException {
        List<Reader> propertiesResources = loadAllPropertiesResources(resource);
        logger.debug("Source '{}' loads {} Properties Resources['{}']", source, propertiesResources.size(), resource);
        if (isEmpty(propertiesResources)) {
            return null;
        }
        Properties properties = new Properties();
        for (Reader propertiesResource : propertiesResources) {
            try (Reader reader = propertiesResource) {
                properties.load(reader);
            }
        }
        logger.debug("Source '{}' loads all Properties Resources[name :{}] : {}", source, resource, properties);
        return properties;
    }

    protected abstract String getResource(String resourceName);

    protected abstract List<Reader> loadAllPropertiesResources(String resource) throws IOException;
}
