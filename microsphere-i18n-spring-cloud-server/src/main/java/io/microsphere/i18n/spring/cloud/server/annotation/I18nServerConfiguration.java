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
package io.microsphere.i18n.spring.cloud.server.annotation;

import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.PropertySourcesServiceMessageSource;
import io.microsphere.i18n.spring.cloud.server.controller.I18nServerController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.getDefault;

/**
 * The Configuration Class for I18n Server
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourcesServiceMessageSource
 * @since 1.0.0
 */
@Import(I18nServerController.class)
public class I18nServerConfiguration {

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Bean
    @Lazy
    public CompositeServiceMessageSource lazyCompositeServiceMessageSource() {
        return new CompositeServiceMessageSource();
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStartedEvent(ApplicationStartedEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        CompositeServiceMessageSource lazyCompositeServiceMessageSource =
                context.getBean("lazyCompositeServiceMessageSource", CompositeServiceMessageSource.class);
        List<String> services = discoveryClient.getServices();
        List<ServiceMessageSource> serviceMessageSources = services.stream()
                .map(this::buildServiceMessageSource)
                .collect(Collectors.toList());
        lazyCompositeServiceMessageSource.setServiceMessageSources(serviceMessageSources);
    }

    private ServiceMessageSource buildServiceMessageSource(String service) {
        PropertySourcesServiceMessageSource serviceMessageSource = new PropertySourcesServiceMessageSource(service);
        serviceMessageSource.setEnvironment(environment);
        // FIXME Hardcode
        serviceMessageSource.setDefaultLocale(getDefault());
        serviceMessageSource.setSupportedLocales(asList(getDefault(), ENGLISH));
        serviceMessageSource.init();
        return serviceMessageSource;
    }
}