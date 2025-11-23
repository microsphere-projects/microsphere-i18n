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

import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.microsphere.i18n.spring.beans.factory.support.ServiceMessageSourceBeanLifecyclePostProcessor;
import io.microsphere.i18n.spring.context.I18nApplicationListener;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import io.microsphere.i18n.spring.validation.beanvalidation.I18nLocalValidatorFactoryBeanPostProcessor;
import io.microsphere.i18n.spring.web.servlet.AcceptHeaderLocaleResolverBeanPostProcessor;
import io.microsphere.spring.config.context.annotation.ResourcePropertySource;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link I18nImportBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see I18nImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
class I18nImportBeanDefinitionRegistrarTest {

    @Test
    void testOnSpecifiedSources() {
        testInSpringContainer((context, environment) -> {
            assertTrue(isBeanPresent(context, ServiceMessageSourceFactoryBean.class));
            assertTrue(isBeanPresent(context, MessageSourceAdapter.class));
            assertTrue(isBeanPresent(context, ServiceMessageSource.class));
            assertTrue(isBeanPresent(context, I18nApplicationListener.class));
            assertTrue(isBeanPresent(context, ServiceMessageSourceBeanLifecyclePostProcessor.class));
            assertTrue(isBeanPresent(context, I18nLocalValidatorFactoryBeanPostProcessor.class));
            assertTrue(isBeanPresent(context, AcceptHeaderLocaleResolverBeanPostProcessor.class));

            assertTrue(isBeanPresent(context, MessageSource.class));
            assertTrue(context.containsBean("testServiceMessageSource"));
        }, SpecifiedSourcesConfig.class);
    }

    @Test
    void testOnDisabled() {
        testInSpringContainer((context, environment) -> {
            assertFalse(isBeanPresent(context, ServiceMessageSourceFactoryBean.class));
            assertFalse(isBeanPresent(context, MessageSourceAdapter.class));
            assertFalse(isBeanPresent(context, ServiceMessageSource.class));
            assertFalse(isBeanPresent(context, I18nApplicationListener.class));
            assertFalse(isBeanPresent(context, ServiceMessageSourceBeanLifecyclePostProcessor.class));
            assertFalse(isBeanPresent(context, I18nLocalValidatorFactoryBeanPostProcessor.class));
            assertFalse(isBeanPresent(context, AcceptHeaderLocaleResolverBeanPostProcessor.class));
        }, DisabledConfig.class);
    }

    @Test
    void testOnUnexposedMessageSource() {
        testInSpringContainer((context, environment) -> {
            assertTrue(isBeanPresent(context, ServiceMessageSourceFactoryBean.class));
            assertFalse(isBeanPresent(context, MessageSourceAdapter.class));
            assertTrue(isBeanPresent(context, ServiceMessageSource.class));
            assertTrue(isBeanPresent(context, I18nApplicationListener.class));
            assertTrue(isBeanPresent(context, ServiceMessageSourceBeanLifecyclePostProcessor.class));
            assertTrue(isBeanPresent(context, I18nLocalValidatorFactoryBeanPostProcessor.class));
            assertTrue(isBeanPresent(context, AcceptHeaderLocaleResolverBeanPostProcessor.class));
        }, UnexposedMessageSourceConfig.class);
    }

    @Test
    void testOnBeanClassAbsent() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        try {
            currentThread().setContextClassLoader(getSystemClassLoader().getParent());
            testInSpringContainer((context, environment) -> {
                assertTrue(isBeanPresent(context, ServiceMessageSourceFactoryBean.class));
                assertTrue(isBeanPresent(context, MessageSourceAdapter.class));
                assertTrue(isBeanPresent(context, ServiceMessageSource.class));
                assertTrue(isBeanPresent(context, I18nApplicationListener.class));
                assertTrue(isBeanPresent(context, ServiceMessageSourceBeanLifecyclePostProcessor.class));
                assertFalse(isBeanPresent(context, I18nLocalValidatorFactoryBeanPostProcessor.class));
                assertFalse(isBeanPresent(context, AcceptHeaderLocaleResolverBeanPostProcessor.class));
            }, SpecifiedSourcesConfig.class);
        } finally {
            currentThread().setContextClassLoader(classLoader);
        }
    }

    @EnableI18n(sources = "test")
    static class SpecifiedSourcesConfig {
    }

    @EnableI18n
    @ResourcePropertySource("classpath:META-INF/config/disabled-enable-i18n.properties")
    static class DisabledConfig {
    }

    @EnableI18n(exposeMessageSource = false)
    static class UnexposedMessageSourceConfig {
    }
}