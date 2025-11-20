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

import org.junit.Test;

import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.collection.Sets.ofSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link ReloadableResourceServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ReloadableResourceServiceMessageSource
 * @see TestReloadableResourceServiceMessageSource
 * @since 1.0.0
 */
public class ReloadableResourceServiceMessageSourceTest extends ResourceServiceMessageSourceTest {

    @Override
    protected TestReloadableResourceServiceMessageSource createServiceMessageSource() {
        return new TestReloadableResourceServiceMessageSource();
    }

    @Test
    public void testReload() {
        TestReloadableResourceServiceMessageSource serviceMessageSource = getServiceMessageSource();
        serviceMessageSource.reload("test");
        assertEquals(ofSet("test"), serviceMessageSource.getInitializedResources());
    }

    @Test
    public void testReloadWithIterable() {
        TestReloadableResourceServiceMessageSource serviceMessageSource = getServiceMessageSource();
        Iterable<String> resources = ofSet("test");
        serviceMessageSource.reload(resources);
        assertEquals(ofSet(resources), serviceMessageSource.getInitializedResources());
    }

    @Test
    public void testCanReload() {
        TestReloadableResourceServiceMessageSource serviceMessageSource = getServiceMessageSource();
        assertFalse(serviceMessageSource.canReload("test"));
        serviceMessageSource.reload("test");
        assertTrue(serviceMessageSource.canReload("test"));
    }

    @Test
    public void testCanReloadWithIterable() {
        TestReloadableResourceServiceMessageSource serviceMessageSource = getServiceMessageSource();
        Iterable<String> resources = ofSet("test");
        assertFalse(serviceMessageSource.canReload(resources));
        serviceMessageSource.reload(resources);
        assertTrue(serviceMessageSource.canReload(resources));
    }
}