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

package io.microsphere.i18n.spring.cloud.server.autoconfigure;


import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import io.microsphere.i18n.spring.boot.actuate.autoconfigure.I18nEndpointAutoConfiguration;
import io.microsphere.i18n.spring.cloud.server.annotation.EnableI18nServer.Marker;
import io.microsphere.i18n.spring.cloud.server.controller.I18nServerController;
import io.microsphere.spring.boot.test.WebAutoConfigurationTest;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration;

import java.util.Set;

/**
 * {@link I18nServerAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see I18nServerAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                Marker.class,
                I18nServerAutoConfigurationTest.class,
                I18nEndpointAutoConfiguration.class,
                CompositeDiscoveryClientAutoConfiguration.class
        },
        properties = {
                "management.endpoints.web.exposure.include=i18n",
                "management.endpoint.i18n.enabled=true"
        }
)
class I18nServerAutoConfigurationTest extends WebAutoConfigurationTest<I18nServerAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(I18nServerController.class);
        autoConfiguredClasses.add(CompositeServiceMessageSource.class);
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.i18n.enabled=false");
        globalDisabledPropertyValues.add("spring.cloud.discovery.enabled=false");
        globalDisabledPropertyValues.add("spring.cloud.discovery.blocking.enabled=false");
        globalDisabledPropertyValues.add("management.endpoints.web.exposure.include=none");
        globalDisabledPropertyValues.add("management.endpoint.i18n.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(ServiceMessageSource.class);
        globalMissingClasses.add(EnableI18n.class);
        globalMissingClasses.add(DiscoveryClient.class);
        globalMissingClasses.add(Endpoint.class);
    }
}