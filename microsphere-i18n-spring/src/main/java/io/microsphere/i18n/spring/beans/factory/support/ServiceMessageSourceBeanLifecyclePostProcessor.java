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
package io.microsphere.i18n.spring.beans.factory.support;

import io.microsphere.i18n.ServiceMessageSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.StringUtils;

import static org.springframework.beans.factory.support.AbstractBeanDefinition.INFER_METHOD;

/**
 * The PostProcessor processes the lifecycle of {@link ServiceMessageSource} Beans automatically.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ServiceMessageSource
 * @see MergedBeanDefinitionPostProcessor
 * @since 1.0.0
 */
public class ServiceMessageSourceBeanLifecyclePostProcessor implements MergedBeanDefinitionPostProcessor {

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (ServiceMessageSource.class.isAssignableFrom(beanType)) {
            setInitMethodName(beanDefinition, beanType);
            setDestroyMethodName(beanDefinition, beanType);
        }
    }

    private void setInitMethodName(RootBeanDefinition beanDefinition, Class<?> beanType) {
        if (InitializingBean.class.isAssignableFrom(beanType)) {
            // If ServiceMessageSource bean implements the interface InitializingBean,
            // it's ignored immediately.
            return;
        }
        String initMethodName = beanDefinition.getInitMethodName();
        if (StringUtils.isEmpty(initMethodName)) {
            // If The BeanDefinition does not declare the initialization method,
            // ServiceMessageSource#init() method should be a candidate.
            beanDefinition.setInitMethodName("init");
        }
    }

    private void setDestroyMethodName(RootBeanDefinition beanDefinition, Class<?> beanType) {
        if (DisposableBean.class.isAssignableFrom(beanType)) {
            // If ServiceMessageSource bean implements the interface DisposableBean,
            // it's ignored immediately.
            return;
        }
        String destroyMethodName = beanDefinition.getDestroyMethodName();

        if (INFER_METHOD.equals(destroyMethodName)) {
            // If the "(inferred)" method was found, return immediately.
            return;
        }
        if (StringUtils.isEmpty(destroyMethodName)) {
            // If The BeanDefinition does not declare the destroy method,
            // ServiceMessageSource#destroy() method should be a candidate.
            beanDefinition.setDestroyMethodName("destroy");
        }
    }
}
