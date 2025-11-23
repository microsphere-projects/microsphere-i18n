package io.microsphere.i18n.spring.boot.autoconfigure;

import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.microsphere.i18n.spring.validation.beanvalidation.I18nLocalValidatorFactoryBeanPostProcessor;
import io.microsphere.i18n.spring.beans.factory.support.ServiceMessageSourceBeanLifecyclePostProcessor;
import io.microsphere.i18n.spring.context.I18nApplicationListener;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

/**
 * {@link I18nAutoConfiguration} Test
 *
 * @author li.qi
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nAutoConfiguration
 * @since 1.0.0
 */
class I18nAutoConfigurationTests {

    ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void setup() {
        applicationContextRunner = new ApplicationContextRunner();
    }

    @Test
    void shouldContainServiceMessageSourceFactoryBean() {
        applicationContextRunner.withPropertyValues("spring.application.name=I18nAutoConfigurationTests")
                .withConfiguration(of(I18nAutoConfiguration.class, PropertyPlaceholderAutoConfiguration.class))
                .run(context -> {
                    assertThat(context)
                            .hasBean("applicationServiceMessageSource")
                            .getBean("applicationServiceMessageSource")
                            .hasFieldOrPropertyWithValue("source", "I18nAutoConfigurationTests");

                    assertThat(context).getBeans(ServiceMessageSourceFactoryBean.class).hasSizeGreaterThanOrEqualTo(1);

                    assertThat(context).getBean("serviceMessageSource").isInstanceOf(DelegatingServiceMessageSource.class);
                    assertThat(context).getBean("messageSource").isInstanceOf(MessageSourceAdapter.class);

                    assertThat(context).hasSingleBean(I18nApplicationListener.class)
                            .hasSingleBean(I18nLocalValidatorFactoryBeanPostProcessor.class)
                            .hasSingleBean(ServiceMessageSourceBeanLifecyclePostProcessor.class);
                });
    }
}