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

import io.microsphere.i18n.spring.PropertySourcesServiceMessageSource;
import io.microsphere.i18n.spring.boot.condition.ConditionalOnI18nEnabled;
import io.microsphere.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener;
import io.microsphere.spring.cloud.client.condition.ConditionalOnFeaturesEnabled;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.i18n.spring.PropertySourcesServiceMessageSource.findAllPropertySourcesServiceMessageSources;
import static java.util.Collections.emptyList;

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
@ConditionalOnI18nEnabled
@ConditionalOnClass(name = {
        "org.springframework.cloud.context.environment.EnvironmentChangeEvent", // spring-cloud-context
})
@Import(value = {
        I18nCloudAutoConfiguration.FeaturesConfiguration.class,
        ReloadableResourceServiceMessageSourceListener.class
})
public class I18nCloudAutoConfiguration {

    @ConditionalOnFeaturesEnabled
    public static class FeaturesConfiguration {

        /**
         * The bean name of {@link HasFeatures}
         *
         * @see #i18nFeatures(ListableBeanFactory)
         */
        public final static String I18N_FEATURES_BEAN_NAME = "i18nFeatures";

        @Bean(name = I18N_FEATURES_BEAN_NAME)
        public HasFeatures i18nFeatures(ListableBeanFactory beanFactory) {
            List<PropertySourcesServiceMessageSource> propertySourcesServiceMessageSources = findAllPropertySourcesServiceMessageSources(beanFactory);
            int size = propertySourcesServiceMessageSources.size();
            List<NamedFeature> namedFeatures = newArrayList(size);
            for (int i = 0; i < size; i++) {
                PropertySourcesServiceMessageSource source = propertySourcesServiceMessageSources.get(i);
                String name = "Source : " + source.getSource();
                namedFeatures.add(new NamedFeature(name, source.getClass()));
            }
            return new HasFeatures(emptyList(), namedFeatures);
        }
    }
