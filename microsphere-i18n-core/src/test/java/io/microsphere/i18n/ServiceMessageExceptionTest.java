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
package io.microsphere.i18n;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.i18n.util.I18nUtils.destroyServiceMessageSource;
import static io.microsphere.i18n.util.I18nUtils.setServiceMessageSource;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static java.lang.String.format;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link ServiceMessageException} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ServiceMessageExceptionTest {

    @Before
    public void before() {
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("test");
        serviceMessageSource.setDefaultLocale(SIMPLIFIED_CHINESE);
        serviceMessageSource.setSupportedLocales(ofList(SIMPLIFIED_CHINESE));
        serviceMessageSource.init();
        setServiceMessageSource(serviceMessageSource);
    }

    @After
    public void after() {
        destroyServiceMessageSource();
    }

    @Test
    public void test() {
        assertServiceMessageException("测试-a", "{a}");
        assertServiceMessageException("您好,World", "{hello}", "World");
    }

    private void assertServiceMessageException(String localizedMessage, String message, Object... args) {
        ServiceMessageException exception = new ServiceMessageException(message, args);
        assertTrue(exception instanceof RuntimeException);
        assertEquals(message, exception.getMessage());
        assertEquals(localizedMessage, exception.getLocalizedMessage());
        assertEquals(format("ServiceMessageException[message='%s', args=%s, localized message='%s']", message, arrayToString(args), localizedMessage), exception.toString());
    }
}
