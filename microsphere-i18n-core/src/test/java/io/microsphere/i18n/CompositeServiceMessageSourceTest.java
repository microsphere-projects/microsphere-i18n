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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.i18n.AbstractI18nTest.TEST_SOURCE;
import static io.microsphere.i18n.EmptyServiceMessageSource.INSTANCE;
import static io.microsphere.i18n.ServiceMessageSource.COMMON_SOURCE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.getDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CompositeServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see CompositeServiceMessageSource
 * @since 1.0.0
 */
class CompositeServiceMessageSourceTest {

    private CompositeServiceMessageSource compositeServiceMessageSource;

    private CompositeServiceMessageSource emptyCompositeServiceMessageSource;

    private List<ServiceMessageSource> serviceMessageSources;

    private DefaultServiceMessageSource defaultServiceMessageSource;

    private TestReloadableResourceServiceMessageSource testReloadableResourceServiceMessageSource;

    private List<String> resources;

    private Set<Locale> locales = ofSet(getDefault(), ENGLISH);

    @BeforeEach
    void setUp() {
        this.defaultServiceMessageSource = new DefaultServiceMessageSource(TEST_SOURCE);
        this.testReloadableResourceServiceMessageSource = new TestReloadableResourceServiceMessageSource();
        this.serviceMessageSources = ofList(INSTANCE, this.defaultServiceMessageSource, this.testReloadableResourceServiceMessageSource);
        this.compositeServiceMessageSource = new CompositeServiceMessageSource(serviceMessageSources);
        this.emptyCompositeServiceMessageSource = new CompositeServiceMessageSource();

        this.compositeServiceMessageSource.init();
        this.emptyCompositeServiceMessageSource.init();

        this.resources = ofList(this.defaultServiceMessageSource.getInitializedResources());
    }

    @AfterEach
    void tearDown() {
        this.compositeServiceMessageSource.destroy();
    }

    @Test
    void testGetMessage() {
        assertGetMessage(TEST_SOURCE);
        assertGetMessage("a");
        assertGetMessage("hello", "world");
        assertGetMessage(null);
    }

    @Test
    void testGetLocale() {
        assertEquals(getDefault(), this.compositeServiceMessageSource.getLocale());
        assertEquals(getDefault(), this.emptyCompositeServiceMessageSource.getLocale());
    }

    @Test
    void testGetDefaultLocale() {
        assertEquals(getDefault(), this.compositeServiceMessageSource.getDefaultLocale());
        assertEquals(getDefault(), this.emptyCompositeServiceMessageSource.getDefaultLocale());
    }

    @Test
    void testGetSupportedLocales() {
        assertSupportedLocales(this.compositeServiceMessageSource::getSupportedLocales);
        assertSupportedLocales(this.emptyCompositeServiceMessageSource::getSupportedLocales);
    }

    @Test
    void testGetDefaultSupportedLocales() {
        assertSupportedLocales(this.compositeServiceMessageSource::getDefaultSupportedLocales);
        assertSupportedLocales(this.emptyCompositeServiceMessageSource::getDefaultSupportedLocales);
    }

    @Test
    void testGetSource() {
        assertSame(COMMON_SOURCE, this.compositeServiceMessageSource.getSource());
        assertSame(COMMON_SOURCE, this.emptyCompositeServiceMessageSource.getSource());
    }

    @Test
    void testSetServiceMessageSources() {
        this.compositeServiceMessageSource.setServiceMessageSources(emptyList());
        this.emptyCompositeServiceMessageSource.setServiceMessageSources(emptyList());
    }

    @Test
    void testReload() {
        this.compositeServiceMessageSource.reload(this.resources);
        this.compositeServiceMessageSource.reload(this.resources.get(0));
        this.emptyCompositeServiceMessageSource.reload(this.resources);
        this.emptyCompositeServiceMessageSource.reload(this.resources.get(0));

        assertReload(this.compositeServiceMessageSource);
        assertReload(this.emptyCompositeServiceMessageSource);
    }

    @Test
    void testCanReload() {
        assertTrue(this.compositeServiceMessageSource.canReload(this.resources));
        assertTrue(this.compositeServiceMessageSource.canReload(this.resources.get(0)));
        assertTrue(this.emptyCompositeServiceMessageSource.canReload(this.resources));
        assertTrue(this.emptyCompositeServiceMessageSource.canReload(this.resources.get(0)));
    }

    @Test
    void testInitializeResource() {
        this.compositeServiceMessageSource.initializeResource(this.resources.get(0));
        this.emptyCompositeServiceMessageSource.initializeResource(this.resources.get(0));
    }

    @Test
    void testInitializeResources() {
        this.compositeServiceMessageSource.initializeResources(this.resources);
        this.emptyCompositeServiceMessageSource.initializeResources(this.resources);
    }

    @Test
    void testGetInitializedResources() {
        this.compositeServiceMessageSource.initializeResources(this.resources);
        this.emptyCompositeServiceMessageSource.initializeResources(this.resources);
        assertTrue(this.compositeServiceMessageSource.getInitializedResources().containsAll(this.defaultServiceMessageSource.getInitializedResources()));
        assertTrue(this.emptyCompositeServiceMessageSource.getInitializedResources().isEmpty());
    }

    @Test
    void testGetEncoding() {
        assertEquals(UTF_8, this.compositeServiceMessageSource.getEncoding());
        assertEquals(UTF_8, this.emptyCompositeServiceMessageSource.getEncoding());
    }

    @Test
    void testGetServiceMessageSources() {
        assertEquals(ofList(INSTANCE, this.defaultServiceMessageSource, this.testReloadableResourceServiceMessageSource), this.compositeServiceMessageSource.getServiceMessageSources());
        assertEquals(emptyList(), this.emptyCompositeServiceMessageSource.getServiceMessageSources());
    }

    @Test
    void testToString() {
        assertNotNull(this.compositeServiceMessageSource.toString());
        assertNotNull(this.emptyCompositeServiceMessageSource.toString());
    }

    void assertGetMessage(String code, Object... args) {
        assertEquals(this.defaultServiceMessageSource.getMessage(code, args),
                this.compositeServiceMessageSource.getMessage(code, args));
        assertNull(this.emptyCompositeServiceMessageSource.getMessage(code, args));
    }

    void assertSupportedLocales(Supplier<Collection<Locale>> localsSupplier) {
        Collection<Locale> supportedLocales = localsSupplier.get();
        assertEquals(this.locales.size(), supportedLocales.size());
        assertTrue(this.locales.containsAll(supportedLocales));
        assertTrue(supportedLocales.containsAll(this.locales));
    }

    void assertReload(CompositeServiceMessageSource compositeServiceMessageSource) {
        Set<String> initializedResources = compositeServiceMessageSource.getInitializedResources();
        compositeServiceMessageSource.reload(initializedResources);
    }
}