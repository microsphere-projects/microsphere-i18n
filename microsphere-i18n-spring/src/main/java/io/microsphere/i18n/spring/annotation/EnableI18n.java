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
package io.microsphere.i18n.spring.annotation;

import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.microsphere.i18n.spring.constants.I18nConstants;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.microsphere.i18n.ServiceMessageSource.COMMON_SOURCE;

/**
 * Enables the extension for Spring Internationalisation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nImportBeanDefinitionRegistrar
 * @see ServiceMessageSourceFactoryBean
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
@Import(I18nImportBeanDefinitionRegistrar.class)
public @interface EnableI18n {

    /**
     * Declares the sources of the {@link ServiceMessageSource} as the {@link ServiceMessageSourceFactoryBean} Spring Beans
     * to be registered.
     * <p>
     * The attribute value will be merged from the Spring property whose name is {@link I18nConstants#SOURCES_PROPERTY_NAME}
     *
     * @return {@link ServiceMessageSource#COMMON_SOURCE} as the default
     * @see I18nConstants#SOURCES_PROPERTY_NAME
     */
    String[] sources() default {COMMON_SOURCE};

    /**
     * Whether to expose {@link I18nConstants#SERVICE_MESSAGE_SOURCE_BEAN_NAME the primary Spring Bean}
     * {@link ServiceMessageSource} as the {@link MessageSource}
     *
     * @return <code>true</code> as default
     * @see AbstractApplicationContext#MESSAGE_SOURCE_BEAN_NAME
     * @see MessageSource
     */
    boolean exposeMessageSource() default true;
}
