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
package io.microsphere.i18n.spring.boot.actuate;

import io.microsphere.i18n.AbstractResourceServiceMessageSource;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * I18n Spring Boot Actuator Endpoint
 * <prev>
 * {
 * "common" : {
 * "zh_CN" : {
 * "error.a" : "a"
 * }
 * },
 * "test" : {
 * "test.i18n_messages_zh_CN.properties" : {
 *
 * },
 * "META-INF/i18n/test/i18n_messages_en.properties":{
 * "test.a" : "test-a"
 * "test.hello" : "Hello,{}"
 * }
 * ...
 * }
 * }
 * </prev>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Endpoint(id = "i18n")
public class I18nEndpoint {

    private List<ServiceMessageSource> serviceMessageSources;

    @Autowired
    @Qualifier(SERVICE_MESSAGE_SOURCE_BEAN_NAME)
    public void initServiceMessageSources(ServiceMessageSource serviceMessageSource) {
        List<ServiceMessageSource> serviceMessageSources = emptyList();
        if (serviceMessageSource instanceof DelegatingServiceMessageSource) {
            DelegatingServiceMessageSource delegatingServiceMessageSource = (DelegatingServiceMessageSource) serviceMessageSource;
            serviceMessageSources = delegatingServiceMessageSource.getDelegate().getServiceMessageSources();
        }
        this.serviceMessageSources = serviceMessageSources;
    }

    @ReadOperation
    public Map<String, Object> invoke() {
        List<ServiceMessageSource> serviceMessageSources = this.serviceMessageSources;
        int size = serviceMessageSources.size();
        Map<String, Object> result = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            ServiceMessageSource serviceMessageSource = serviceMessageSources.get(i);
            if (serviceMessageSource instanceof CompositeServiceMessageSource) {
                CompositeServiceMessageSource compositeServiceMessageSource = (CompositeServiceMessageSource) serviceMessageSource;
                for (ServiceMessageSource sms : compositeServiceMessageSource.getServiceMessageSources()) {
                    if (sms instanceof AbstractResourceServiceMessageSource) {
                        AbstractResourceServiceMessageSource resourceServiceMessageSource = (AbstractResourceServiceMessageSource) sms;
                        String source = serviceMessageSource.getSource();
                        List<Locale> supportedLocales = resourceServiceMessageSource.getSupportedLocales();
                        Map<String, Map<String, String>> localizedMessages = new HashMap<>(supportedLocales.size());


                        for (Locale supportedLocale : supportedLocales) {
                            Map<String, String> messages = resourceServiceMessageSource.getMessages(supportedLocale);
                            if (!isEmpty(messages)) {
                                localizedMessages.put(supportedLocale.toString(), messages);
                            }
                        }

                        result.put(source, localizedMessages);
                    }
                }
            }

        }
        return result;
    }

    private AbstractResourceServiceMessageSource getResourceServiceMessageSource(ServiceMessageSource serviceMessageSource) {
        if (serviceMessageSource instanceof ServiceMessageSourceFactoryBean) {
            ServiceMessageSourceFactoryBean smffb = (ServiceMessageSourceFactoryBean) serviceMessageSource;
            CompositeServiceMessageSource compositeServiceMessageSource = smffb.getDelegate();
        }
        return null;
    }
}
