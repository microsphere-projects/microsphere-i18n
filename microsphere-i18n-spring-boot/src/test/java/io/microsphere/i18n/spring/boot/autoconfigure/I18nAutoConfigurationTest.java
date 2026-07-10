package io.microsphere.i18n.spring.boot.autoconfigure;

import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.DelegatingServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import io.microsphere.i18n.spring.beans.factory.ServiceMessageSourceFactoryBean;
import io.microsphere.i18n.spring.beans.factory.support.ServiceMessageSourceBeanLifecyclePostProcessor;
import io.microsphere.i18n.spring.context.I18nApplicationListener;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import io.microsphere.i18n.spring.validation.beanvalidation.I18nLocalValidatorFactoryBeanPostProcessor;
import io.microsphere.spring.boot.test.AutoConfigurationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link I18nAutoConfiguration} Test
 *
 * @author li.qi
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                I18nAutoConfigurationTest.class,
                PropertyPlaceholderAutoConfiguration.class
        },
        webEnvironment = NONE,
        properties = {
                "spring.application.name=I18nAutoConfigurationTest"
        }
)
class I18nAutoConfigurationTest extends AutoConfigurationTest<I18nAutoConfiguration> {

    @Test
    void shouldContainServiceMessageSourceFactoryBean() {
        runner.run(context -> {
            assertThat(context)
                    .hasBean("applicationServiceMessageSource")
                    .getBean("applicationServiceMessageSource")
                    .hasFieldOrPropertyWithValue("source", "I18nAutoConfigurationTest");

            assertThat(context).getBeans(ServiceMessageSourceFactoryBean.class).hasSizeGreaterThanOrEqualTo(1);

            assertThat(context).getBean("serviceMessageSource").isInstanceOf(DelegatingServiceMessageSource.class);
            assertThat(context).getBean("messageSource").isInstanceOf(MessageSourceAdapter.class);

            assertThat(context).hasSingleBean(I18nApplicationListener.class)
                    .hasSingleBean(I18nLocalValidatorFactoryBeanPostProcessor.class)
                    .hasSingleBean(ServiceMessageSourceBeanLifecyclePostProcessor.class);
        });
    }

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.i18n.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(ServiceMessageSource.class);
        globalMissingClasses.add(EnableI18n.class);
    }
}
