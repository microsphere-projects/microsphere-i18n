package io.microsphere.i18n.spring.boot.actuate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import java.util.Map;

import static com.jayway.jsonpath.JsonPath.using;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class I18nEndpointTest {
    ApplicationContextRunner applicationContextRunner;

    @BeforeEach
    void setup() {
        applicationContextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(EndpointConfiguration.class));
    }

    @Test
    void shouldReturnCommonI18nMessages() {
        applicationContextRunner.run(context -> {
            Map<String, Map<String, String>> invoke = context.getBean(I18nEndpoint.class).invoke();
            DocumentContext parse = using(Configuration.defaultConfiguration()).parse(invoke);
            assertThat(parse, hasJsonPath("$.['common.i18n_messages_en.properties']"));
            assertThat(parse, hasJsonPath("$.['common.i18n_messages_zh.properties']"));
            assertThat(parse, hasJsonPath("$.['common.i18n_messages_zh_CN.properties']"));
            assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_en.properties']"));
            assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_en.properties'].['common.a']", equalTo("a")));
            assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_zh.properties']"));
            assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_zh_CN.properties']"));
            assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_zh_CN.properties'].['common.a']", equalTo("啊")));
        });
    }

    @EnableI18n
    static class EndpointConfiguration {
        @Bean
        I18nEndpoint i18nEndpoint() {
            return new I18nEndpoint();
        }
    }
}
