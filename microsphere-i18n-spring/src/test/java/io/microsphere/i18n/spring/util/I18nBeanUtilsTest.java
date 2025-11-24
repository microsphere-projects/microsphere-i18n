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

package io.microsphere.i18n.spring.util;


import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.config.DisabledEnableI18nConfiguration;
import io.microsphere.i18n.spring.config.TestSourceEnableI18nConfiguration;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DelegatingMessageSource;

import static io.microsphere.i18n.spring.util.I18nBeanUtils.getMessageSource;
import static io.microsphere.i18n.spring.util.I18nBeanUtils.getMessageSourceAdapter;
import static io.microsphere.i18n.spring.util.I18nBeanUtils.getServiceMessageSource;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.context.support.AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME;

/**
 * {@link I18nBeanUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see I18nBeanUtils
 * @since 1.0.0
 */
public class I18nBeanUtilsTest {

    @Test
    public void testGetMessageSourceWithoutMessageSourceBean() {
        testInSpringContainer(this::assertDefaultMessageSource);
    }

    @Test
    public void testGetMessageSourceWithoutMessageSourceBeanExposure() {
        testInSpringContainer(this::assertDefaultMessageSource, DisabledEnableI18nConfiguration.class);
    }

    @Test
    public void testGetMessageSource() {
        testInSpringContainer(this::assertMessageSourceAdapter, TestSourceEnableI18nConfiguration.class);
    }

    @Test
    public void testGetServiceMessageSourceWithoutDefinition() {
        testInSpringContainer(context -> {
            ServiceMessageSource serviceMessageSource = getServiceMessageSource(context);
            assertNull(serviceMessageSource);
        });
    }

    @Test
    public void testGetServiceMessageSource() {
        testInSpringContainer(context -> {
            ServiceMessageSource serviceMessageSource = getServiceMessageSource(context);
            assertNotNull(serviceMessageSource);
            assertTrue(serviceMessageSource instanceof DelegatingServiceMessageSource);
        }, TestSourceEnableI18nConfiguration.class);
    }

    void assertDefaultMessageSource(ConfigurableApplicationContext context) {
        assertMessageSource(context, false);
    }

    void assertMessageSourceAdapter(ConfigurableApplicationContext context) {
        assertMessageSource(context, true);
    }

    void assertMessageSource(ConfigurableApplicationContext context, boolean exposed) {
        MessageSource messageSource = getMessageSource(context);
        MessageSourceAdapter messageSourceAdapter = getMessageSourceAdapter(context);
        assertNotNull(messageSource);
        assertEquals(!exposed, messageSource instanceof DelegatingMessageSource);
        assertEquals(exposed, messageSourceAdapter == messageSource);
        assertEquals(exposed, messageSource instanceof MessageSourceAdapter);
        assertEquals(exposed, context.containsBeanDefinition(MESSAGE_SOURCE_BEAN_NAME));
    }
}