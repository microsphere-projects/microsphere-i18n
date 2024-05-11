package io.microsphere.i18n.spring.boot.autoconfigure;

import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.microsphere.i18n.spring.beans.factory.config.I18nBeanPostProcessor;
import io.microsphere.i18n.spring.beans.factory.support.ServiceMessageSourceBeanLifecyclePostProcessor;
import io.microsphere.i18n.spring.context.I18nApplicationListener;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nAutoConfigurationTests {

    ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void setup() {
        applicationContextRunner = new ApplicationContextRunner();
    }

    @Test
    void shouldContainServiceMessageSourceFactoryBean() {
        applicationContextRunner.withPropertyValues("spring.application.name=I18nAutoConfigurationTests")
                .withConfiguration(AutoConfigurations.of(I18nAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class))
                .run(context -> {
                    assertTrue(context.containsBean("applicationServiceMessageSource"));
                    assertEquals("I18nAutoConfigurationTests",
                            context.getBean("applicationServiceMessageSource", ServiceMessageSourceFactoryBean.class).getSource());

                    assertFalse(context.getBeansOfType(ServiceMessageSourceFactoryBean.class).isEmpty());
                    assertInstanceOf(DelegatingServiceMessageSource.class, context.getBean("serviceMessageSource"));

                    assertTrue(context.containsBean("messageSource"));
                    assertInstanceOf(MessageSourceAdapter.class, context.getBean("messageSource"));

                    assertNotNull(context.getBean(I18nApplicationListener.class));
                    assertNotNull(context.getBean(I18nBeanPostProcessor.class));
                    assertNotNull(context.getBean(ServiceMessageSourceBeanLifecyclePostProcessor.class));
                });
    }
}
