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

package io.microsphere.i18n.spring.config;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import org.springframework.context.annotation.Configuration;

import static io.microsphere.i18n.AbstractSpringTest.TEST_SOURCE;

/**
 * The Spring {@link Configuration} class annotated {@link EnableI18n} for {@link AbstractSpringTest#TEST_SOURCE "test"}
 * source.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableI18n
 * @since 1.0.0
 */
@EnableI18n(sources = TEST_SOURCE)
public class TestSourceEnableI18nConfiguration {
}