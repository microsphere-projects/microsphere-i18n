package io.microsphere.i18n.spring.boot.actuate.autoconfigure;

import io.microsphere.i18n.spring.boot.actuate.I18nEndpoint;
import io.microsphere.i18n.spring.boot.autoconfigure.I18nAutoConfiguration;
import org.junit.Test;
import org.junit.Before;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


class I18nEndpointAutoConfigurationTest {

    ApplicationContextRunner applicationContextRunner;

    @Before
    void setup() {
        applicationContextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        I18nAutoConfiguration.class,
                        I18nEndpointAutoConfiguration.class
                ));
    }

    @Test
    void shouldHaveEndpointBean() {
        applicationContextRunner.withPropertyValues("management.endpoints.web.exposure.include=i18n")
                .run(context -> assertThat(context).hasSingleBean(I18nEndpoint.class));
    }


    @Test
    void shouldNotHaveEndpointBean() {
        applicationContextRunner
                .run(context -> assertThat(context).doesNotHaveBean(I18nEndpoint.class));
    }

    @Test
    void shouldNotHaveEndpointBeanWhenEnablePropertyIsFalse() {
        applicationContextRunner.withPropertyValues("management.endpoint.i18n.enabled=false")
                .withPropertyValues("management.endpoints.web.exposure.include=*")
                .run(context -> assertThat(context).doesNotHaveBean(I18nEndpoint.class));
    }
}
