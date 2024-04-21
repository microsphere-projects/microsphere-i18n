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
package io.microsphere.i18n.spring.boot.autoconfigure;

import io.microsphere.i18n.spring.context.I18nConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * I18n Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see I18nConfiguration
 * @since 1.0.0
 */
@ConditionalOnClass(name = {
        "io.microsphere.i18n.ServiceMessageSource", // microsphere-i18n-core
        "io.microsphere.i18n.spring.context.I18nConfiguration", // microsphere-i18n-spring
})
@Import(I18nConfiguration.class)
public class I18nAutoConfiguration {
}
