package io.microsphere.i18n.spring.cloud.autoconfigure;

import io.microsphere.i18n.spring.boot.autoconfigure.I18nAutoConfiguration;
import io.microsphere.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.actuator.FeaturesEndpoint;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

/**
 * {@link I18nCloudAutoConfiguration} Test
 *
 * @author li.qi
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nCloudAutoConfiguration
 * @since 1.0.0
 */
class I18nCloudAutoConfigurationTest {

    ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void setup() {
        applicationContextRunner = new ApplicationContextRunner().withConfiguration(
                of(I18nAutoConfiguration.class, I18nCloudAutoConfiguration.class)
        );
    }

    @Test
    void shouldContainReloadableResourceServiceMessageSourceListenerBean() {
        applicationContextRunner.run(context ->
                assertThat(context).hasSingleBean(ReloadableResourceServiceMessageSourceListener.class));
    }

    @Test
    void shouldNotContainReloadableResourceServiceMessageSourceListenerBeanWhenMissingEnvironmentChangeEvent() {
        applicationContextRunner.withClassLoader(new FilteredClassLoader("org.springframework.cloud.context.environment.EnvironmentChangeEvent"))
                .run(context -> assertThat(context).doesNotHaveBean(ReloadableResourceServiceMessageSourceListener.class));
    }

    @Nested
    @SpringBootTest(
            classes = {
                    FeaturesConfigurationTest.class
            },
            properties = {
                    "spring.cloud.service-registry.auto-registration.enabled=false",
                    "management.endpoints.web.exposure.include=features",
                    "management.endpoint.features.enabled=true"
            }
    )
    @EnableAutoConfiguration
    class FeaturesConfigurationTest {

        @Autowired
        private FeaturesEndpoint featuresEndpoint;

        @Test
        void testHasFeatures() {
            Object features = featuresEndpoint.features();
            assertNotNull(features);
        }
    }
}