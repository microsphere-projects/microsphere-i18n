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

package io.microsphere.i18n.spring.context;


import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.DefaultServiceMessageSource;
import io.microsphere.i18n.EmptyServiceMessageSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Locale.getDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MessageSourceAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MessageSourceAdapter
 * @since 1.0.0
 */
class MessageSourceAdapterTest extends AbstractSpringTest {

    private static final String testCode = "test.value";

    private static final String defaultMessage = "default";

    private Locale locale = getDefault();

    private Object[] args = EMPTY_OBJECT_ARRAY;

    private MessageSourceResolvable testMessageSourceResolvable = new MessageSourceResolvable() {

        @Override
        public String[] getCodes() {
            return ofArray(testCode);
        }

        @Override
        public Object[] getArguments() {
            return args;
        }

        @Override
        public String getDefaultMessage() {
            return defaultMessage;
        }
    };

    private MessageSourceResolvable emptyMessageSourceResolvable = () -> new String[0];

    @Test
    void testOnNoMessageSourceBean() {
        testInSpringContainer(context -> {
            MessageSourceAdapter messageSourceAdapter = context.getBean(MessageSourceAdapter.class);
            assertNull(messageSourceAdapter.getMessage(testCode, args, locale));
            assertEquals(defaultMessage, messageSourceAdapter.getMessage(testCode, args, defaultMessage, locale));
            assertEquals(defaultMessage, messageSourceAdapter.getMessage(testMessageSourceResolvable, locale));
            assertNotNull(messageSourceAdapter.toString());
        }, EmptyServiceMessageSource.class, MessageSourceAdapter.class);
    }

    @Test
    void test() {
        testInSpringContainer(context -> {
            MessageSourceAdapter messageSourceAdapter = context.getBean(MessageSourceAdapter.class);
            assertEquals("测试-a", messageSourceAdapter.getMessage("a", args, locale));
            assertEquals("测试", messageSourceAdapter.getMessage(testCode, args, locale));
            assertEquals("测试", messageSourceAdapter.getMessage(testCode, args, defaultMessage, locale));
            assertEquals("测试", messageSourceAdapter.getMessage(testMessageSourceResolvable, locale));
            assertNull(messageSourceAdapter.getMessage(emptyMessageSourceResolvable, locale));
            assertNull(messageSourceAdapter.getMessage(() -> ofArray("not-found-code"), locale));
            assertNotNull(messageSourceAdapter.toString());
        }, MessageSourceAdapter.class, MessageSourceAdapterTest.class);
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public DefaultServiceMessageSource defaultServiceMessageSource() {
        return new DefaultServiceMessageSource(TEST_SOURCE);
    }

    @Bean
    public ResourceBundleMessageSource resourceBundleMessageSource() {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setBasename("messages");
        resourceBundleMessageSource.setDefaultEncoding("UTF-8");
        return resourceBundleMessageSource;
    }
}