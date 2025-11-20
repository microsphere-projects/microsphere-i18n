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

package io.microsphere.i18n;

import java.util.Locale;
import java.util.Set;

import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static java.util.Locale.getDefault;

/**
 * {@link ServiceMessageSource} for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServiceMessageSource
 * @since 1.0.0
 */
class TestServiceMessageSource implements ServiceMessageSource {

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getMessage(String code, Locale locale, Object... args) {
        return EMPTY_STRING;
    }

    @Override
    public String getMessage(String code, Object... args) {
        return ServiceMessageSource.super.getMessage(code, args);
    }

    @Override
    public Locale getLocale() {
        return getDefault();
    }

    @Override
    public Locale getDefaultLocale() {
        return ServiceMessageSource.super.getDefaultLocale();
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return ServiceMessageSource.super.getSupportedLocales();
    }

    @Override
    public String getSource() {
        return ServiceMessageSource.super.getSource();
    }
}