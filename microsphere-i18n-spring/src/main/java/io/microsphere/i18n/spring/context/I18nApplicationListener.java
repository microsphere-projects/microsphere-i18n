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
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static io.microsphere.i18n.util.I18nUtils.destroyServiceMessageSource;
import static io.microsphere.i18n.util.I18nUtils.setServiceMessageSource;
import static io.microsphere.spring.util.BeanUtils.getOptionalBean;
import static io.microsphere.spring.util.BeanUtils.getSortedBeans;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static org.springframework.util.ObjectUtils.containsElement;

/**
 * Internationalization {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see SmartApplicationListener
 * @since 1.0.0
 */
public class I18nApplicationListener implements SmartApplicationListener {

    private static final Logger logger = LoggerFactory.getLogger(I18nApplicationListener.class);

    private static final String ACCEPT_HEADER_LOCALE_RESOLVER_CLASS_NAME = "org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver";

    private static final Class<?> ACCEPT_HEADER_LOCALE_RESOLVER_CLASS = resolveClass(ACCEPT_HEADER_LOCALE_RESOLVER_CLASS_NAME);

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

        initializeServiceMessageSource(context);

        initializeAcceptHeaderLocaleResolver(context);
    }

    private void initializeServiceMessageSource(ApplicationContext context) {
        ServiceMessageSource serviceMessageSource = context.getBean(SERVICE_MESSAGE_SOURCE_BEAN_NAME, ServiceMessageSource.class);
        setServiceMessageSource(serviceMessageSource);
    }


    @SuppressWarnings("unchecked")
    private void initializeAcceptHeaderLocaleResolver(ApplicationContext context) {
        if (ACCEPT_HEADER_LOCALE_RESOLVER_CLASS == null) {
            logger.debug("The class '{}' was not found!", ACCEPT_HEADER_LOCALE_RESOLVER_CLASS_NAME);
            return;
        }

        Class<AcceptHeaderLocaleResolver> beanClass = (Class<AcceptHeaderLocaleResolver>) ACCEPT_HEADER_LOCALE_RESOLVER_CLASS;

        List<AcceptHeaderLocaleResolver> acceptHeaderLocaleResolvers = getSortedBeans(context, beanClass);

        if (acceptHeaderLocaleResolvers.isEmpty()) {
            logger.debug("The '{}' Spring Bean was not found!", ACCEPT_HEADER_LOCALE_RESOLVER_CLASS_NAME);
            return;
        }

        ServiceMessageSource serviceMessageSource = getOptionalBean(context, ServiceMessageSource.class);

        for (AcceptHeaderLocaleResolver acceptHeaderLocaleResolver : acceptHeaderLocaleResolvers) {
            Locale defaultLocale = serviceMessageSource.getDefaultLocale();
            List<Locale> supportedLocales = serviceMessageSource.getSupportedLocales();
            acceptHeaderLocaleResolver.setDefaultLocale(defaultLocale);
            acceptHeaderLocaleResolver.setSupportedLocales(supportedLocales);
            logger.debug("AcceptHeaderLocaleResolver Bean associated with default Locale : '{}' , list of supported Locales : {}", defaultLocale, supportedLocales);
        }
    }

    private void onContextClosedEvent(ContextClosedEvent event) {
        destroyServiceMessageSource();
    }
}
