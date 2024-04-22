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
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static io.microsphere.i18n.util.I18nUtils.findAllServiceMessageSources;
import static java.util.Collections.emptyList;

/**
 * I18n Spring Boot Actuator Endpoint
 * <pre>
 * {
 * "test.i18n_messages_zh.properties": {
 *
 * },
 * "META-INF/i18n/test/i18n_messages_zh_CN.properties": {
 * "test.a": "测试-a",
 * "test.hello": "您好,{}"
 * },
 * "META-INF/i18n/test/i18n_messages_en.properties": {
 * "test.a": "test-a",
 * "test.hello": "Hello,{}"
 * },
 * "META-INF/i18n/common/i18n_messages_zh_CN.properties": {
 * "common.a": "a"
 * }
 * }
 * </pre>
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
    public Map<String, Map<String, String>> invoke() {
        List<ServiceMessageSource> serviceMessageSources = this.serviceMessageSources;
        int size = serviceMessageSources.size();
        Map<String, Map<String, String>> allLocalizedResourceMessages = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            ServiceMessageSource serviceMessageSource = serviceMessageSources.get(i);
            List<ServiceMessageSource> subServiceMessageSources = findAllServiceMessageSources(serviceMessageSource);
            for (ServiceMessageSource subServiceMessageSource : subServiceMessageSources) {
                if (subServiceMessageSource instanceof AbstractResourceServiceMessageSource) {
                    AbstractResourceServiceMessageSource resourceServiceMessageSource = (AbstractResourceServiceMessageSource) subServiceMessageSource;
                    Map<String, Map<String, String>> localizedResourceMessages = resourceServiceMessageSource.getLocalizedResourceMessages();
                    allLocalizedResourceMessages.putAll(localizedResourceMessages);
                }
            }
        }
        return allLocalizedResourceMessages;
    }
}
