/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.i18n.spring.boot.actuate;

import io.microsphere.i18n.AbstractResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.PropertySourcesServiceMessageSource;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.MapUtils.newFixedLinkedHashMap;
import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.i18n.spring.PropertySourcesServiceMessageSource.findAllPropertySourcesServiceMessageSources;
import static io.microsphere.i18n.spring.util.I18nBeanUtils.getServiceMessageSource;
import static io.microsphere.i18n.util.I18nUtils.findAllServiceMessageSources;
import static java.util.Collections.singleton;

/**
 * I18n Spring Boot Actuator Endpoint
 * <pre>{@code
 * {
 * "test.i18n_messages_zh.properties": {
 *
 * },
 * "META-INF/i18n/test/i18n_messages_zh_CN.properties": {
 * "test.a": "测试-a",
 * "test.hello": "您好,{}"
 * },
 * "META-INF/i18n/test/i18n_messages_en.properties": {
 * "test.a": "test-a",
 * "test.hello": "Hello,{}"
 * },
 * "META-INF/i18n/common/i18n_messages_zh_CN.properties": {
 * "common.a": "a"
 * }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Endpoint(id = "i18n")
public class I18nEndpoint {

    public static final String PROPERTY_SOURCE_NAME = "i18nEndpointPropertySource";

    private ServiceMessageSource primaryServiceMessageSource;

    private List<AbstractResourceServiceMessageSource> resourceServiceMessageSources;

    private ConfigurableEnvironment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        init(event.getApplicationContext());
    }

    private void init(ConfigurableApplicationContext context) {
        this.primaryServiceMessageSource = getServiceMessageSource(context);
        this.resourceServiceMessageSources = findAllServiceMessageSources(this.primaryServiceMessageSource, AbstractResourceServiceMessageSource.class);
        this.environment = context.getEnvironment();
    }

    @ReadOperation
    public Map<String, Map<String, String>> invoke() {
        List<AbstractResourceServiceMessageSource> resourceServiceMessageSources = this.resourceServiceMessageSources;
        int size = resourceServiceMessageSources.size();
        Map<String, Map<String, String>> allLocalizedResourceMessages = newFixedLinkedHashMap(size);
        for (int i = 0; i < size; i++) {
            AbstractResourceServiceMessageSource resourceServiceMessageSource = resourceServiceMessageSources.get(i);
            Map<String, Map<String, String>> localizedResourceMessages = resourceServiceMessageSource.getLocalizedResourceMessages();
            allLocalizedResourceMessages.putAll(localizedResourceMessages);
        }
        return allLocalizedResourceMessages;
    }

    @ReadOperation
    public List<Map<String, String>> getMessage(@Selector String code) {
        return getMessage(code, null);
    }

    @ReadOperation
    public List<Map<String, String>> getMessage(@Selector String code, @Selector Locale locale) {
        Set<Locale> supportedLocales = getSupportedLocales(locale);
        int size = resourceServiceMessageSources.size();

        List<Map<String, String>> messageMaps = newArrayList(size * supportedLocales.size());

        List<AbstractResourceServiceMessageSource> resourceServiceMessageSources = this.resourceServiceMessageSources;
        for (int i = 0; i < size; i++) {
            AbstractResourceServiceMessageSource resourceServiceMessageSource = resourceServiceMessageSources.get(i);
            for (Locale supportedLocale : supportedLocales) {
                String source = resourceServiceMessageSource.getSource();
                String resource = resourceServiceMessageSource.getResource(supportedLocale);
                String message = resourceServiceMessageSource.getMessage(code, supportedLocale);

                Map<String, String> messageMap = ofMap(
                        "code", code,
                        "locale", supportedLocale.toString(),
                        "source", source,
                        "resource", resource,
                        "message", message
                );
                messageMaps.add(messageMap);
            }
        }
        return messageMaps;
    }

    @WriteOperation
    public Map<String, Object> addMessage(String source, Locale locale, String code, String message) throws IOException {
        PropertySourcesServiceMessageSource serviceMessageSource = getPropertySourcesServiceMessageSource(source);
        Properties properties = loadProperties(serviceMessageSource, locale);
        // Add a new code with message
        properties.setProperty(code, message);

        String propertyName = serviceMessageSource.getPropertyName(locale);
        StringWriter stringWriter = new StringWriter();
        // Properties -> StringWriter
        properties.store(stringWriter, null);
        // StringWriter -> String
        String propertyValue = stringWriter.toString();

        MapPropertySource propertySource = getPropertySource();
        Map<String, Object> newProperties = propertySource.getSource();
        newProperties.put(propertyName, propertyValue);

        serviceMessageSource.init();
        return newProperties;
    }

    private Properties loadProperties(PropertySourcesServiceMessageSource serviceMessageSource, Locale locale) throws IOException {
        Properties properties = serviceMessageSource.loadAllProperties(locale);
        return properties == null ? new Properties() : properties;
    }

    private MapPropertySource getPropertySource() {
        MutablePropertySources propertySources = environment.getPropertySources();
        String name = PROPERTY_SOURCE_NAME;
        MapPropertySource propertySource = (MapPropertySource) propertySources.get(name);
        if (propertySource == null) {
            Map<String, Object> properties = new HashMap<>();
            propertySource = new MapPropertySource(name, properties);
            propertySources.addFirst(propertySource);
        }
        return propertySource;
    }

    private PropertySourcesServiceMessageSource getPropertySourcesServiceMessageSource(String source) {
        List<PropertySourcesServiceMessageSource> propertySourcesServiceMessageSources = findAllPropertySourcesServiceMessageSources(
                this.resourceServiceMessageSources, serviceMessageSource -> Objects.equals(source, serviceMessageSource.getSource()));
        return first(propertySourcesServiceMessageSources);
    }

    private Set<Locale> getSupportedLocales(Locale locale) {
        if (locale == null) {
            return primaryServiceMessageSource.getSupportedLocales();
        } else {
            return singleton(locale);
        }
    }
}