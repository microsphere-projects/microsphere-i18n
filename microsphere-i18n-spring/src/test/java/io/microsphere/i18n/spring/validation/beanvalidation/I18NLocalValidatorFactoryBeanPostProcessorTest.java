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

package io.microsphere.i18n.spring.validation.beanvalidation;


import io.microsphere.i18n.spring.config.TestSourceEnableI18nConfiguration;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link I18nLocalValidatorFactoryBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see I18nLocalValidatorFactoryBeanPostProcessor
 * @since 1.0.0
 */
class I18NLocalValidatorFactoryBeanPostProcessorTest {

    @Test
    void testOnNoLocalValidatorFactoryBean() {
        testInSpringContainer(context -> {
            assertFalse(isBeanPresent(context, LocalValidatorFactoryBean.class));
        }, TestSourceEnableI18nConfiguration.class);
    }

    @Test
    void testOnNoMessageSourceAdapterBean() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, LocalValidatorFactoryBean.class));
        }, TestSourceEnableI18nConfiguration.class, LocalValidatorFactoryBean.class);
    }

    @Test
    void test() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, MessageSourceAdapter.class));
            LocalValidatorFactoryBean localValidatorFactoryBean = context.getBean(LocalValidatorFactoryBean.class);
            assertTrue(localValidatorFactoryBean.getMessageInterpolator() instanceof LocaleContextMessageInterpolator);
        }, TestSourceEnableI18nConfiguration.class, LocalValidatorFactoryBean.class, this.getClass());
    }
}