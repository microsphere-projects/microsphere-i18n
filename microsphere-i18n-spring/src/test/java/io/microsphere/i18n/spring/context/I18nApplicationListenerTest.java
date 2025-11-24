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


import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.config.TestSourceEnableI18nConfiguration;
import io.microsphere.spring.config.env.event.PropertySourcesChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockPropertySource;

import static io.microsphere.i18n.EmptyServiceMessageSource.INSTANCE;
import static io.microsphere.i18n.spring.util.I18nBeanUtils.getServiceMessageSource;
import static io.microsphere.i18n.util.I18nUtils.serviceMessageSource;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.added;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link I18nApplicationListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see I18nApplicationListener
 * @since 1.0.0
 */
class I18nApplicationListenerTest {

    @Test
    void test() {
        testInSpringContainer((context, environment) -> {
            ServiceMessageSource serviceMessageSource = getServiceMessageSource(context);
            assertSame(serviceMessageSource(), serviceMessageSource);

            assertEquals("测试-a", serviceMessageSource.getMessage("a"));
            assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));

            MockPropertySource mockPropertySource = new MockPropertySource("test");
            mockPropertySource.setProperty("test.i18n_messages_zh_CN.properties", "test.a = 测试-test");
            environment.getPropertySources().addLast(mockPropertySource);

            context.publishEvent(new PropertySourcesChangedEvent(context));

            PropertySourcesChangedEvent event = new PropertySourcesChangedEvent(context, added(context, mockPropertySource));
            context.publishEvent(event);
            assertEquals("测试-test", serviceMessageSource.getMessage("a"));
        }, TestSourceEnableI18nConfiguration.class);
        assertSame(INSTANCE, serviceMessageSource());
    }
}