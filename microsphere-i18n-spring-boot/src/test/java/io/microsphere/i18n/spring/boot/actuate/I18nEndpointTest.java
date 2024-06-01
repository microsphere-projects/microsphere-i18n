package io.microsphere.i18n.spring.boot.actuate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import io.microsphere.i18n.spring.boot.actuate.autoconfigure.I18nEndpointAutoConfiguration;
import io.microsphere.i18n.spring.boot.autoconfigure.I18nAutoConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Locale;
import java.util.Map;

import static com.jayway.jsonpath.JsonPath.using;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(
        classes = I18nEndpointTest.EndpointConfiguration.class,
        properties = {
                "management.endpoint.i18n.enabled=true",
                "management.endpoints.web.exposure.include=*"
        }
)
class I18nEndpointTest {

    @BeforeAll
    static void init() {
        Locale.setDefault(Locale.CHINA);
    }

    @Autowired
    private ApplicationContext context;

    @Test
    void shouldReturnCommonI18nMessages() {
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
    }

    @ImportAutoConfiguration(classes = {
            I18nAutoConfiguration.class,
            I18nEndpointAutoConfiguration.class,
            PropertyPlaceholderAutoConfiguration.class
    })
    static class EndpointConfiguration {
        @Bean
        I18nEndpoint i18nEndpoint() {
            return new I18nEndpoint();
        }
    }
}
