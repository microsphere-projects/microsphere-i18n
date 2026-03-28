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


import io.microsphere.logging.test.jupiter.LoggingLevelsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.i18n.AbstractI18nTest.TEST_SOURCE;
import static java.util.Locale.CHINESE;
import static java.util.Locale.FRANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link PropertiesResourceServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertiesResourceServiceMessageSource
 * @since 1.0.0
 */
class PropertiesResourceServiceMessageSourceTest {

    private PropertiesResourceServiceMessageSource propertiesResourceServiceMessageSource;

    @BeforeEach
    void setUp() {
        this.propertiesResourceServiceMessageSource = new TestPropertiesResourceServiceMessageSource(TEST_SOURCE);
    }

    @Test
    void testLoadMessages() {
        assertThrows(RuntimeException.class, () -> this.propertiesResourceServiceMessageSource.loadMessages("test"));
    }

    @Test
    @LoggingLevelsTest(levels = "ERROR")
    void testSetDefaultLocale() {
        this.propertiesResourceServiceMessageSource.setDefaultLocale(FRANCE);
        assertEquals(FRANCE, this.propertiesResourceServiceMessageSource.getDefaultLocale());
    }

    @Test
    @LoggingLevelsTest(levels = "ERROR")
    void testSetSupportedLocales() {
        this.propertiesResourceServiceMessageSource.setSupportedLocales(ofList(CHINESE));
        assertEquals(ofSet(CHINESE), this.propertiesResourceServiceMessageSource.getSupportedLocales());
    }
}