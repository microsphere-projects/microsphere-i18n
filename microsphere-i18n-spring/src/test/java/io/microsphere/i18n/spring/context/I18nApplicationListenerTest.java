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
import org.junit.jupiter.api.Test;

import static io.microsphere.i18n.EmptyServiceMessageSource.INSTANCE;
import static io.microsphere.i18n.spring.util.I18nBeanUtils.getServiceMessageSource;
import static io.microsphere.i18n.util.I18nUtils.serviceMessageSource;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
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
        }, TestSourceEnableI18nConfiguration.class);
        assertSame(INSTANCE, serviceMessageSource());
    }
}