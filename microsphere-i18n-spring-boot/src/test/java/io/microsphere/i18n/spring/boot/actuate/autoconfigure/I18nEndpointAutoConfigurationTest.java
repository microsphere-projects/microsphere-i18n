package io.microsphere.i18n.spring.boot.actuate.autoconfigure;

import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import io.microsphere.i18n.spring.boot.actuate.I18nEndpoint;
import io.microsphere.spring.boot.test.AutoConfigurationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Set;

/**
 * {@link I18nEndpointAutoConfiguration} Test
 *
 * @author li.qi
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nEndpointAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                I18nEndpointAutoConfigurationTest.class
        },
        properties = {
                "management.endpoints.web.exposure.include=i18n",
                "management.endpoint.i18n.enabled=true"
        }
)
class I18nEndpointAutoConfigurationTest extends AutoConfigurationTest<I18nEndpointAutoConfiguration> {

    ApplicationContextRunner applicationContextRunner;

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(I18nEndpoint.class);
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
