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

package io.microsphere.i18n.spring.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractApplicationContext;

import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static io.microsphere.spring.beans.BeanUtils.getBeanIfAvailable;
import static io.microsphere.util.ClassUtils.cast;
import static org.springframework.context.support.AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME;

/**
 * The utilities class for i18n Beans
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableI18n
 * @since 1.0.0
 */
public abstract class I18nBeanUtils {

    /**
     * Get the {@link MessageSource} bean from the specified {@link BeanFactory}.
     *
     * @param beanFactory the specified {@link BeanFactory}
     * @return <code>null</code> if bean was not found
     * @see AbstractApplicationContext#MESSAGE_SOURCE_BEAN_NAME
     * @see AbstractApplicationContext#initMessageSource()
     * @see #getMessageSourceAdapter(BeanFactory)
     */
    @Nonnull
    public static MessageSource getMessageSource(@Nonnull BeanFactory beanFactory) {
        return beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
    }

    /**
     * Get the {@link MessageSourceAdapter} bean from the specified {@link BeanFactory}.
     *
     * @param beanFactory the specified {@link BeanFactory}
     * @return <code>null</code> if {@link EnableI18n#exposeMessageSource()} was <code>false</code>
     * @see EnableI18n#exposeMessageSource()
     * @see MessageSourceAdapter
     */
    @Nullable
    public static MessageSourceAdapter getMessageSourceAdapter(@Nonnull BeanFactory beanFactory) {
        MessageSource messageSource = getMessageSource(beanFactory);
        return cast(messageSource, MessageSourceAdapter.class);
    }

    /**
     * Get the {@link ServiceMessageSource} primary bean from the specified {@link BeanFactory}.
     *
     * @param beanFactory the specified {@link BeanFactory}
     * @return <code>null</code> if bean was not found
     * @see EnableI18n
     * @see DelegatingServiceMessageSource
     * @see ServiceMessageSourceFactoryBean
     */
    @Nullable
    public static ServiceMessageSource getServiceMessageSource(@Nonnull BeanFactory beanFactory) {
        return getBeanIfAvailable(beanFactory, SERVICE_MESSAGE_SOURCE_BEAN_NAME, ServiceMessageSource.class);
    }

    private I18nBeanUtils() {
    }
}
