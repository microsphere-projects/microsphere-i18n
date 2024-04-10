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

import org.springframework.context.i18n.LocaleContext;
import org.springframework.lang.Nullable;

import java.util.Locale;

import static org.springframework.context.i18n.LocaleContextHolder.getLocaleContext;

/**
 * The utilities class for {@link Locale} in the Spring
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see LocaleContext
 * @see Locale
 * @since 1.0.0
 */
public abstract class LocaleUtils {

    /**
     * Get the {@link Locale} from Thread-Locale scoped {@link LocaleContext}
     *
     * @return <code>null</code> if can't be found
     */
    @Nullable
    public static Locale getLocaleFromLocaleContext() {
        LocaleContext localeContext = getLocaleContext();
        return localeContext == null ? null : localeContext.getLocale();
    }
}
