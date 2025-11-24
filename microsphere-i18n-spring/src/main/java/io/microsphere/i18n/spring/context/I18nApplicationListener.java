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
package io.microsphere.i18n.spring.context;

import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static io.microsphere.i18n.util.I18nUtils.destroyServiceMessageSource;
import static io.microsphere.i18n.util.I18nUtils.setServiceMessageSource;

/**
 * Internationalization {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SmartApplicationListener
 * @see AcceptHeaderLocaleResolver
 * @see EnableI18n
 * @since 1.0.0
 */
public class I18nApplicationListener {

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        initializeServiceMessageSource(context);
    }

    @EventListener(ContextClosedEvent.class)
    public void onContextClosedEvent() {
        destroyServiceMessageSource();
    }

    private void initializeServiceMessageSource(ApplicationContext context) {
        ServiceMessageSource serviceMessageSource = context.getBean(SERVICE_MESSAGE_SOURCE_BEAN_NAME, ServiceMessageSource.class);
        setServiceMessageSource(serviceMessageSource);
    }
}