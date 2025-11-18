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
package io.microsphere.i18n.spring.annotation;

import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.microsphere.i18n.spring.beans.factory.config.I18nBeanPostProcessor;
import io.microsphere.i18n.spring.beans.factory.support.ServiceMessageSourceBeanLifecyclePostProcessor;
import io.microsphere.i18n.spring.context.I18nApplicationListener;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import static io.microsphere.i18n.spring.constants.I18nConstants.DEFAULT_ENABLED;
import static io.microsphere.i18n.spring.constants.I18nConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static io.microsphere.i18n.spring.constants.I18nConstants.SOURCES_PROPERTY_NAME;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.context.support.AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * I18n {@link ImportBeanDefinitionRegistrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ImportBeanDefinitionRegistrar
 * @see EnableI18n
 * @since 1.0.0
 */
public class I18nImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = EnableI18n.class;

    private static Logger logger = LoggerFactory.getLogger(ANNOTATION_TYPE);

    private Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (isEnabled()) {
            AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
            registerServiceMessageSourceBeanDefinitions(attributes, registry);
            registerMessageSourceAdapterBeanDefinition(attributes, registry);
            registerI18nApplicationListenerBeanDefinition(registry);
            registerBeanPostProcessorBeanDefinitions(registry);
        }
    }

    private void registerServiceMessageSourceBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        Set<String> sources = resolveSources(attributes);

        for (String source : sources) {
            String beanName = source + "ServiceMessageSource";
            registerBeanDefinition(registry, beanName, ServiceMessageSourceFactoryBean.class, source);
        }

        // Register DelegatingServiceMessageSource as the Spring Primary Bean
        BeanDefinition primaryBeanDefinition =
                rootBeanDefinition(DelegatingServiceMessageSource.class)
                .setPrimary(true)
                .getBeanDefinition();
        registry.registerBeanDefinition(SERVICE_MESSAGE_SOURCE_BEAN_NAME, primaryBeanDefinition);
    }

    private Set<String> resolveSources(AnnotationAttributes attributes) {
        Set<String> sources = new LinkedHashSet<>();
        initSources(sources, () -> environment.getProperty(SOURCES_PROPERTY_NAME, String[].class, EMPTY_STRING_ARRAY));
        initSources(sources, () -> attributes.getStringArray("sources"));
        return sources;
    }

    private void initSources(Set<String> sources, Supplier<String[]> sourcesSupplier) {
        for (String source : sourcesSupplier.get()) {
            sources.add(environment.resolvePlaceholders(source));
        }
    }

    private void registerMessageSourceAdapterBeanDefinition(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        boolean exposeMessageSource = attributes.getBoolean("exposeMessageSource");
        if (exposeMessageSource) {
            registerBeanDefinition(registry, MESSAGE_SOURCE_BEAN_NAME, MessageSourceAdapter.class);
        }
    }

    private void registerI18nApplicationListenerBeanDefinition(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, I18nApplicationListener.class);
    }

    private void registerBeanPostProcessorBeanDefinitions(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, I18nBeanPostProcessor.class);
        registerBeanDefinition(registry, ServiceMessageSourceBeanLifecyclePostProcessor.class);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private boolean isEnabled() {
        String propertyName = ENABLED_PROPERTY_NAME;
        boolean enabled = environment.getProperty(propertyName, boolean.class, DEFAULT_ENABLED);
        logger.debug("Microsphere i18n is {} , cased by the Spring property[name : '{}', default value : '{}']",
                enabled ? "enabled" : "disabled",
                propertyName,
                DEFAULT_ENABLED);
        return enabled;
    }
}
