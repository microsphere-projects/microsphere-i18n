package io.microsphere.i18n.spring.boot.autoconfigure;

import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.microsphere.i18n.spring.beans.factory.config.I18nBeanPostProcessor;
import io.microsphere.i18n.spring.beans.factory.support.ServiceMessageSourceBeanLifecyclePostProcessor;
import io.microsphere.i18n.spring.context.I18nApplicationListener;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import org.junit.Test;
import org.junit.Before;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class I18nAutoConfigurationTests {

    ApplicationContextRunner applicationContextRunner;

    @Before
    void setup() {
        applicationContextRunner = new ApplicationContextRunner();
    }

    @Test
    void shouldContainServiceMessageSourceFactoryBean() {
        applicationContextRunner.withPropertyValues("spring.application.name=I18nAutoConfigurationTests")
                .withConfiguration(AutoConfigurations.of(I18nAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class))
                .run(context -> {
                    assertThat(context)
                            .hasBean("applicationServiceMessageSource")
                            .getBean("applicationServiceMessageSource")
                            .hasFieldOrPropertyWithValue("source", "I18nAutoConfigurationTests");


                    assertThat(context).getBeans(ServiceMessageSourceFactoryBean.class).hasSizeGreaterThanOrEqualTo(1);

                    assertThat(context).getBean("serviceMessageSource").isInstanceOf(DelegatingServiceMessageSource.class);
                    assertThat(context).getBean("messageSource").isInstanceOf(MessageSourceAdapter.class);

                    assertThat(context).hasSingleBean(I18nApplicationListener.class)
                            .hasSingleBean(I18nBeanPostProcessor.class)
                            .hasSingleBean(ServiceMessageSourceBeanLifecyclePostProcessor.class);
                });
    }
}
