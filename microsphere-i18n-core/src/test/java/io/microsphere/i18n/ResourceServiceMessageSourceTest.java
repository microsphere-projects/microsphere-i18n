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


import org.junit.jupiter.api.Test;

import static io.microsphere.collection.Sets.ofSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ResourceServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourceServiceMessageSource
 * @since 1.0.0
 */
class ResourceServiceMessageSourceTest extends ServiceMessageSourceTest {

    @Override
    protected ResourceServiceMessageSource createServiceMessageSource() {
        return new TestResourceServiceMessageSource();
    }

    @Test
    void testResources() {
        ResourceServiceMessageSource serviceMessageSource = getServiceMessageSource();
        Iterable<String> resources = ofSet(TEST_SOURCE);
        serviceMessageSource.initializeResource(TEST_SOURCE);
        assertEquals(resources, serviceMessageSource.getInitializedResources());
    }

    @Test
    void testGetEncoding() {
        ResourceServiceMessageSource serviceMessageSource = getServiceMessageSource();
        assertEquals(UTF_8, serviceMessageSource.getEncoding());
    }
}