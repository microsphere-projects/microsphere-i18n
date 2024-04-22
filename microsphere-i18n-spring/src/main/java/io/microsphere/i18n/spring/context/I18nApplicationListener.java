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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;

import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static io.microsphere.i18n.util.I18nUtils.destroyServiceMessageSource;
import static io.microsphere.i18n.util.I18nUtils.setServiceMessageSource;
import static org.springframework.util.ObjectUtils.containsElement;

/**
 * Internationalization {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SmartApplicationListener
 * @since 1.0.0
 */
public class I18nApplicationListener implements SmartApplicationListener {

    private static final Class<?>[] SUPPORTED_EVENT_TYPES = {
            ContextRefreshedEvent.class,
            ContextClosedEvent.class
    };

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return containsElement(SUPPORTED_EVENT_TYPES, eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        } else if (event instanceof ContextClosedEvent) {
            onContextClosedEvent((ContextClosedEvent) event);
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();

        ServiceMessageSource serviceMessageSource = context.getBean(SERVICE_MESSAGE_SOURCE_BEAN_NAME, ServiceMessageSource.class);
        setServiceMessageSource(serviceMessageSource);
    }


    private void onContextClosedEvent(ContextClosedEvent event) {
        destroyServiceMessageSource();
    }
}
