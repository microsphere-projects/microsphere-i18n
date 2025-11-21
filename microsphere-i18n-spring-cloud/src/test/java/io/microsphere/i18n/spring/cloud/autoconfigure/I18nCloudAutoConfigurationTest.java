package io.microsphere.i18n.spring.cloud.autoconfigure;

import io.microsphere.i18n.spring.boot.autoconfigure.I18nAutoConfiguration;
import io.microsphere.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
        applicationContextRunner = new ApplicationContextRunner()
                .withConfiguration(of(I18nAutoConfiguration.class, I18nCloudAutoConfiguration.class));
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
}