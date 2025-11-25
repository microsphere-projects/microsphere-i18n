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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static io.microsphere.i18n.util.I18nUtils.destroyServiceMessageSource;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static java.util.Locale.setDefault;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext;

/**
 * Abstract Spring Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractSpringTest {

    public static final String TEST_SOURCE = "test";

    @BeforeClass
    public static void beforeClass() {
        // Set the simplified Chinese as the default Locale
        setDefault(SIMPLIFIED_CHINESE);
    }

    @Before
    public void before() throws Throwable {
        resetLocaleContext();
    }

    @AfterClass
    public static void afterClass() throws Throwable {
        destroyServiceMessageSource();
        resetLocaleContext();
    }

    protected void assertGetMessage(ServiceMessageSource serviceMessageSource) {
        // Testing Simplified Chinese
        // If the Message Code is "a"
        assertEquals("测试-a", serviceMessageSource.getMessage("a"));

        // The same is true for overloaded methods with Message Pattern arguments
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));

        // Returns null if code does not exist
        assertNull(serviceMessageSource.getMessage("code-not-found"));
    }
}