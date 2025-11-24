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

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.i18n.ServiceMessageSource.COMMON_SOURCE;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.getDefault;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * {@link ServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServiceMessageSource
 * @since 1.0.0
 */
public class ServiceMessageSourceTest extends AbstractI18nTest {

    private ServiceMessageSource serviceMessageSource;

    protected ServiceMessageSource createServiceMessageSource() {
        return new TestServiceMessageSource();
    }

    protected <T extends ServiceMessageSource> T getServiceMessageSource() {
        return (T) serviceMessageSource;
    }

    @Before
    public void setUp() {
        this.serviceMessageSource = createServiceMessageSource();
        this.serviceMessageSource.init();
    }

    @After
    public void tearDown() {
        this.serviceMessageSource.destroy();
    }

    @Test
    public void testConstants() {
        assertSame("common", COMMON_SOURCE);
    }

    @Test
    public void testGetMessage() {
        assertNull(this.serviceMessageSource.getMessage("code"));
        assertNull(this.serviceMessageSource.getMessage("code", ENGLISH));
    }

    @Test
    public void testGetLocale() {
        assertSame(getDefault(), this.serviceMessageSource.getLocale());
    }

    @Test
    public void testGetDefaultLocale() {
        assertSame(getDefault(), this.serviceMessageSource.getDefaultLocale());
    }

    @Test
    public void testGetSupportedLocales() {
        assertEquals(ofSet(getDefault(), ENGLISH), this.serviceMessageSource.getSupportedLocales());
    }

    @Test
    public void testGetSource() {
        assertEquals(COMMON_SOURCE, this.serviceMessageSource.getSource());
    }

    @Test
    public void testToString() {
        assertNotNull(this.serviceMessageSource.toString());
    }
}