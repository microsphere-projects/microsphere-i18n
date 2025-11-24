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
import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.INFER_METHOD;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * {@link ServiceMessageSourceBeanLifecyclePostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServiceMessageSourceBeanLifecyclePostProcessor
 * @since 1.0.0
 */
public class ServiceMessageSourceBeanLifecyclePostProcessorTest {

    private ServiceMessageSourceBeanLifecyclePostProcessor postProcessor;

    @Before
    public void setUp() {
        this.postProcessor = new ServiceMessageSourceBeanLifecyclePostProcessor();
    }

    @Test
    public void testPostProcessMergedBeanDefinition() {
        testPostProcessMergedBeanDefinition(ServiceMessageSource.class, beanDefinition -> {
            assertEquals("init", beanDefinition.getInitMethodName());
            assertEquals("destroy", beanDefinition.getDestroyMethodName());
        });

        testPostProcessMergedBeanDefinition(ServiceMessageSource.class, beanDefinitionBuilder -> {
            beanDefinitionBuilder.setDestroyMethodName(INFER_METHOD);
        }, beanDefinition -> {
            assertEquals("init", beanDefinition.getInitMethodName());
            assertEquals(INFER_METHOD, beanDefinition.getDestroyMethodName());
        });

        testPostProcessMergedBeanDefinition(ServiceMessageSource.class, beanDefinitionBuilder -> {
            beanDefinitionBuilder.setInitMethodName("start");
            beanDefinitionBuilder.setDestroyMethodName("stop");
        }, beanDefinition -> {
            assertEquals("start", beanDefinition.getInitMethodName());
            assertEquals("stop", beanDefinition.getDestroyMethodName());
        });

        testPostProcessMergedBeanDefinition(DelegatingServiceMessageSource.class, beanDefinition -> {
            assertNull(beanDefinition.getInitMethodName());
            assertNull(beanDefinition.getDestroyMethodName());
        });

        testPostProcessMergedBeanDefinition(Object.class, beanDefinition -> {
            assertNull(beanDefinition.getInitMethodName());
            assertNull(beanDefinition.getDestroyMethodName());
        });
    }

    void testPostProcessMergedBeanDefinition(Class<?> beanType, Consumer<RootBeanDefinition> beanDefinitionConsumer) {
        testPostProcessMergedBeanDefinition(beanType, beanDefinitionBuilder -> {
        }, beanDefinitionConsumer);
    }

    void testPostProcessMergedBeanDefinition(Class<?> beanType, Consumer<BeanDefinitionBuilder> builderConsumer,
                                             Consumer<RootBeanDefinition> beanDefinitionConsumer) {
        BeanDefinitionBuilder beanDefinitionBuilder = rootBeanDefinition(beanType);
        builderConsumer.accept(beanDefinitionBuilder);
        RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinitionBuilder.getBeanDefinition();
        postProcessor.postProcessMergedBeanDefinition(rootBeanDefinition, beanType, "");
        beanDefinitionConsumer.accept(rootBeanDefinition);
    }
}