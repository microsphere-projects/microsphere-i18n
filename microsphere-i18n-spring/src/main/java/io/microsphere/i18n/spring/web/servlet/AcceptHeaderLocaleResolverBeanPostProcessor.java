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

package io.microsphere.i18n.spring.web.servlet;

import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.config.GenericBeanPostProcessorAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.i18n.spring.util.I18nBeanUtils.getServiceMessageSource;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.springframework.util.ClassUtils.isPresent;

/**
 * The {@link BeanPostProcessor} to set the default {@link Locale locale} and supported {@link Locale locales}
 * into {@link AcceptHeaderLocaleResolver}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AcceptHeaderLocaleResolver
 * @see AcceptHeaderLocaleResolver#setDefaultLocale(Locale)
 * @see AcceptHeaderLocaleResolver#setSupportedLocales(List)
 * @see EnableI18n
 * @since 1.0.0
 */
public class AcceptHeaderLocaleResolverBeanPostProcessor extends GenericBeanPostProcessorAdapter<AcceptHeaderLocaleResolver> implements ApplicationContextAware {

    private static final String CLASS_NAME = "org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver";

    private static final Logger logger = getLogger(AcceptHeaderLocaleResolverBeanPostProcessor.class);

    private ApplicationContext context;

    @Override
    protected void processAfterInitialization(AcceptHeaderLocaleResolver acceptHeaderLocaleResolver, String beanName) throws BeansException {
        ServiceMessageSource serviceMessageSource = getServiceMessageSource(this.context);
        Locale defaultLocale = serviceMessageSource.getDefaultLocale();
        Set<Locale> supportedLocales = serviceMessageSource.getSupportedLocales();
        acceptHeaderLocaleResolver.setDefaultLocale(defaultLocale);
        acceptHeaderLocaleResolver.setSupportedLocales(ofList(supportedLocales));
        logger.trace("AcceptHeaderLocaleResolver Bean associated with default Locale : '{}' , list of supported Locales : {}", defaultLocale, supportedLocales);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public static boolean isAcceptHeaderLocaleResolverPresent(ClassLoader classLoader) {
        return isPresent(CLASS_NAME, classLoader);
    }
}
