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

package io.microsphere.i18n.spring.beans.factory.config;


import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AcceptHeaderLocaleResolverBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AcceptHeaderLocaleResolverBeanPostProcessor
 * @since 1.0.0
 */
@EnableI18n
class AcceptHeaderLocaleResolverBeanPostProcessorTest {

    @Test
    void test() {
        testInSpringContainer(context -> {
            ServiceMessageSource serviceMessageSource = context.getBean(SERVICE_MESSAGE_SOURCE_BEAN_NAME, ServiceMessageSource.class);
            AcceptHeaderLocaleResolver localeResolver = context.getBean(AcceptHeaderLocaleResolver.class);
            assertEquals(ofList(serviceMessageSource.getSupportedLocales()), localeResolver.getSupportedLocales());
        }, AcceptHeaderLocaleResolverBeanPostProcessorTest.class);
    }

    @Bean
    public AcceptHeaderLocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver();
    }

}