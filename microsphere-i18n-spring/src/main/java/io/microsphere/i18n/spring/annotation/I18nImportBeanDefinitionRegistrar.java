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
import io.microsphere.i18n.spring.beans.factory.support.ServiceMessageSourceBeanLifecyclePostProcessor;
import io.microsphere.i18n.spring.context.I18nApplicationListener;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import io.microsphere.i18n.spring.validation.beanvalidation.I18nLocalValidatorFactoryBeanPostProcessor;
import io.microsphere.i18n.spring.web.servlet.AcceptHeaderLocaleResolverBeanPostProcessor;
import io.microsphere.spring.context.annotation.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import static io.microsphere.i18n.spring.constants.I18nConstants.DEFAULT_ENABLED;
import static io.microsphere.i18n.spring.constants.I18nConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static io.microsphere.i18n.spring.constants.I18nConstants.SOURCES_PROPERTY_NAME;
import static io.microsphere.i18n.spring.validation.beanvalidation.I18nLocalValidatorFactoryBeanPostProcessor.isValidatorFactoryPresent;
import static io.microsphere.i18n.spring.web.servlet.AcceptHeaderLocaleResolverBeanPostProcessor.isAcceptHeaderLocaleResolverPresent;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.context.support.AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME;

/**
 * I18n {@link ImportBeanDefinitionRegistrar} that registers i18n-related Spring beans
 * when {@link EnableI18n} is present.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Automatically invoked via @EnableI18n annotation
 *   @EnableI18n(sources = {"common"})
 *   @Configuration
 *   public class AppConfig { }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableI18n
 * @see ImportBeanDefinitionRegistrar
 * @see ServiceMessageSourceFactoryBean
 * @see MessageSourceAdapter
 * @see I18nApplicationListener
 * @see ServiceMessageSourceBeanLifecyclePostProcessor
 * @see I18nLocalValidatorFactoryBeanPostProcessor
 * @see AcceptHeaderLocaleResolverBeanPostProcessor
 * @since 1.0.0
 */
class I18nImportBeanDefinitionRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableI18n> {

    @Override
    protected boolean isEnabled(AnnotationMetadata metadata) {
        return isEnabled();
    }

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableI18n> annotationAttributes) {
        registerServiceMessageSourceBeanDefinitions(annotationAttributes, registry);
        registerMessageSourceAdapterBeanDefinition(annotationAttributes, registry);
        registerI18nApplicationListenerBeanDefinition(registry);
        registerBeanPostProcessorBeanDefinitions(registry);
    }

    private void registerServiceMessageSourceBeanDefinitions(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        Set<String> sources = resolveSources(annotationAttributes);

        for (String source : sources) {
            String beanName = source + "ServiceMessageSource";
            registerBeanDefinition(registry, beanName, ServiceMessageSourceFactoryBean.class, source);
        }

        // Register DelegatingServiceMessageSource as the Spring Primary Bean
        BeanDefinition primaryBeanDefinition = rootBeanDefinition(DelegatingServiceMessageSource.class)
                .setPrimary(true)
                .getBeanDefinition();
        registerBeanDefinition(registry, SERVICE_MESSAGE_SOURCE_BEAN_NAME, primaryBeanDefinition);
    }

    private Set<String> resolveSources(AnnotationAttributes annotationAttributes) {
        Set<String> sources = new LinkedHashSet<>();
        initSources(sources, () -> environment.getProperty(SOURCES_PROPERTY_NAME, String[].class, EMPTY_STRING_ARRAY));
        initSources(sources, () -> annotationAttributes.getStringArray("sources"));
        return sources;
    }

    private void initSources(Set<String> sources, Supplier<String[]> sourcesSupplier) {
        for (String source : sourcesSupplier.get()) {
            sources.add(environment.resolvePlaceholders(source));
        }
    }

    private void registerMessageSourceAdapterBeanDefinition(AnnotationAttributes annotationAttributes, BeanDefinitionRegistry registry) {
        boolean exposeMessageSource = annotationAttributes.getBoolean("exposeMessageSource");
        if (exposeMessageSource) {
            registerBeanDefinition(registry, MESSAGE_SOURCE_BEAN_NAME, MessageSourceAdapter.class);
        }
    }

    private void registerI18nApplicationListenerBeanDefinition(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, I18nApplicationListener.class);
    }

    private void registerBeanPostProcessorBeanDefinitions(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, ServiceMessageSourceBeanLifecyclePostProcessor.class);
        ClassLoader classLoader = getDefaultClassLoader();
        if (isValidatorFactoryPresent(classLoader)) {
            registerBeanDefinition(registry, I18nLocalValidatorFactoryBeanPostProcessor.class);
        }
        if (isAcceptHeaderLocaleResolverPresent(classLoader)) {
            registerBeanDefinition(registry, AcceptHeaderLocaleResolverBeanPostProcessor.class);
        }
    }

    private boolean isEnabled() {
        String propertyName = ENABLED_PROPERTY_NAME;
        boolean enabled = environment.getProperty(propertyName, boolean.class, DEFAULT_ENABLED);
        if (logger.isTraceEnabled()) {
            logger.trace("Microsphere i18n is {} , cased by the Spring property[name : '{}', default value : '{}']",
                    enabled ? "enabled" : "disabled",
                    propertyName,
                    DEFAULT_ENABLED);
        }
        return enabled;
    }
}
