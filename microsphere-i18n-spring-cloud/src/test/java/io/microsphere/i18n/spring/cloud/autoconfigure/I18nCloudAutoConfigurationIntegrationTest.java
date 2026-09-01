package io.microsphere.i18n.spring.cloud.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.actuator.FeaturesEndpoint;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link I18nCloudAutoConfiguration} Integration Test
 *
 * @author li.qi
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nCloudAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                I18nCloudAutoConfigurationIntegrationTest.class
        },
        properties = {
                "spring.cloud.service-registry.auto-registration.enabled=false",
                "management.endpoints.web.exposure.include=features",
                "management.endpoint.features.enabled=true"
        }
)
@EnableAutoConfiguration
class I18nCloudAutoConfigurationIntegrationTest {

    @Autowired
    private FeaturesEndpoint featuresEndpoint;

    @Test
    void testHasFeatures() {
        Object features = featuresEndpoint.features();
        assertNotNull(features);
    }
}