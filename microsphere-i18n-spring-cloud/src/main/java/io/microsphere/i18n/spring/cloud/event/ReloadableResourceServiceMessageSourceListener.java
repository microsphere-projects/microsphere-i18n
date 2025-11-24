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
package io.microsphere.i18n.spring.cloud.event;

import io.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.PropertySourcesServiceMessageSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

import java.util.Set;

import static io.microsphere.i18n.spring.PropertySourcesServiceMessageSource.reloadAll;

/**
 * * An {@link ApplicationListener} of {@link EnvironmentChangeEvent} to reload
 * {@link ServiceMessageSource} dynamically at the runtime.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourcesServiceMessageSource
 * @see DelegatingServiceMessageSource
 * @see ReloadableResourceServiceMessageSource
 * @see ServiceMessageSource
 * @see EnvironmentChangeEvent
 * @since 1.0.0
 */
public class ReloadableResourceServiceMessageSourceListener implements ApplicationListener<EnvironmentChangeEvent>,
        BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> changedPropertyNames = event.getKeys();
        reloadAll(this.beanFactory, changedPropertyNames);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}