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

import io.microsphere.i18n.spring.annotation.EnableI18n;
import io.microsphere.i18n.spring.constants.I18nConstants;
import io.microsphere.spring.config.context.annotation.ResourcePropertySource;
import org.springframework.context.annotation.Configuration;

/**
 * The Spring {@link Configuration} class to disable {@link EnableI18n}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableI18n
 * @see I18nConstants#ENABLED_PROPERTY_NAME
 * @since 1.0.0
 */
@EnableI18n
@ResourcePropertySource("classpath:META-INF/config/disabled-enable-i18n.properties")
public class DisabledEnableI18nConfiguration {
}