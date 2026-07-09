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
package io.microsphere.i18n.spring.cloud.autoconfigure;

import io.microsphere.i18n.spring.boot.condition.ConditionalOnI18nAvailable;
import io.microsphere.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * I18n Auto-Configuration for Spring Cloud that enables dynamic reloading of
 * i18n messages when Spring Cloud environment changes occur.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Auto-configured when spring-cloud-context is on classpath and i18n is enabled
 *   // Listens for EnvironmentChangeEvent to trigger message source reloading
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnI18nAvailable
@ConditionalOnClass(name = {
        "org.springframework.cloud.context.environment.EnvironmentChangeEvent", // Spring Cloud Context API
})
public class I18nCloudAutoConfiguration {

    @Bean
    public ReloadableResourceServiceMessageSourceListener reloadableResourceServiceMessageSourceListener() {
        return new ReloadableResourceServiceMessageSourceListener();
    }
}