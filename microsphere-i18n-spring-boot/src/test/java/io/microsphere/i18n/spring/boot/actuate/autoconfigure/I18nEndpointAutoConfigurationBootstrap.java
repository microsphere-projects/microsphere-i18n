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
package io.microsphere.i18n.spring.boot.actuate.autoconfigure;

import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

/**
 * TODO Comment
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since TODO
 */
@EnableAutoConfiguration
public class I18nEndpointAutoConfigurationBootstrap {

    public static void main(String[] args) {
        new SpringApplicationBuilder(I18nEndpointAutoConfigurationBootstrap.class)
                .web(WebApplicationType.SERVLET)
                .properties("test.i18n_messages_en.properties=test.a = test-a-2024")
                .run(args);
    }

    @Bean
    public static ServiceMessageSourceFactoryBean testServiceMessageSource() {
        return new ServiceMessageSourceFactoryBean("test");
    }
}
