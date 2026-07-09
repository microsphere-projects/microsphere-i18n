package io.microsphere.i18n.spring.cloud.autoconfigure;

import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import io.microsphere.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener;
import io.microsphere.spring.boot.test.AutoConfigurationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.actuator.FeaturesEndpoint;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link I18nCloudAutoConfiguration} Test
 *
 * @author li.qi
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nCloudAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {I18nCloudAutoConfigurationTest.class},
        webEnvironment = NONE
)
class I18nCloudAutoConfigurationTest extends AutoConfigurationTest<I18nCloudAutoConfiguration> {


    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(ReloadableResourceServiceMessageSourceListener.class);
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.i18n.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(ServiceMessageSource.class);
        globalMissingClasses.add(EnableI18n.class);
        globalMissingClasses.add(EnvironmentChangeEvent.class);
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