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

import java.nio.charset.Charset;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * {@link ResourceServiceMessageSource} for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourceServiceMessageSource
 * @since 1.0.0
 */
public class TestResourceServiceMessageSource extends TestServiceMessageSource implements ResourceServiceMessageSource {

    protected Set<String> resources = new LinkedHashSet<>();

    @Override
    public void initializeResource(String resource) {
        this.resources.add(resource);
    }

    @Override
    public void initializeResources(Iterable<String> resources) {
        ResourceServiceMessageSource.super.initializeResources(resources);
    }

    @Override
    public Set<String> getInitializeResources() {
        return unmodifiableSet(this.resources);
    }

    @Override
    public Charset getEncoding() {
        return ResourceServiceMessageSource.super.getEncoding();
    }
}
