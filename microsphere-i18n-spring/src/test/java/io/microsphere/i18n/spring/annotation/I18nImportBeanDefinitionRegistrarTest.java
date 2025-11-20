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
import io.microsphere.spring.config.context.annotation.ResourcePropertySource;
import org.junit.Test;
import org.springframework.context.MessageSource;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link I18nImportBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see I18nImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
public class I18nImportBeanDefinitionRegistrarTest {

    @Test
    public void testOnSpecifiedSources() {
        testInSpringContainer((context, environment) -> {
            assertTrue(isBeanPresent(context, ServiceMessageSource.class));
            assertTrue(isBeanPresent(context, MessageSource.class));
            assertTrue(context.containsBean("testServiceMessageSource"));
        }, SpecifiedSourcesConfig.class);
    }

    @Test
    public void testOnDisabled() {
        testInSpringContainer((context, environment) -> {
            assertFalse(isBeanPresent(context, ServiceMessageSource.class));
        }, DisabledConfig.class);
    }

    @Test
    public void testOnUnexposedMessageSource() {
        testInSpringContainer((context, environment) -> {
            assertTrue(isBeanPresent(context, ServiceMessageSource.class));
            assertTrue(isBeanPresent(context, MessageSource.class));
        }, UnexposedMessageSourceConfig.class);
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