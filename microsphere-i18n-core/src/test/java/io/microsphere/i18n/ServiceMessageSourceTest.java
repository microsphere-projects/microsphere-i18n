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


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.i18n.ServiceMessageSource.COMMON_SOURCE;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.getDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServiceMessageSource
 * @since 1.0.0
 */
class ServiceMessageSourceTest {

    private ServiceMessageSource serviceMessageSource;

    protected ServiceMessageSource createServiceMessageSource() {
        return new TestServiceMessageSource();
    }

    protected <T extends ServiceMessageSource> T getServiceMessageSource() {
        return (T) serviceMessageSource;
    }

    @BeforeEach
    void setUp() {
        this.serviceMessageSource = createServiceMessageSource();
        this.serviceMessageSource.init();
    }

    @AfterEach
    void tearDown() {
        this.serviceMessageSource.destroy();
    }

    @Test
    void testConstants() {
        assertSame("common", COMMON_SOURCE);
    }

    @Test
    void testGetMessage() {
        assertSame(EMPTY_STRING, this.serviceMessageSource.getMessage("code"));
        assertSame(EMPTY_STRING, this.serviceMessageSource.getMessage("code", Locale.ENGLISH));
    }


    @Test
    void testGetLocale() {
        assertSame(getDefault(), this.serviceMessageSource.getLocale());
    }

    @Test
    void testGetDefaultLocale() {
        assertSame(getDefault(), this.serviceMessageSource.getDefaultLocale());
    }

    @Test
    void testGetSupportedLocales() {
        assertEquals(ofSet(getDefault(), ENGLISH), this.serviceMessageSource.getSupportedLocales());
    }

    @Test
    void testGetSource() {
        assertEquals(COMMON_SOURCE, this.serviceMessageSource.getSource());
    }
}