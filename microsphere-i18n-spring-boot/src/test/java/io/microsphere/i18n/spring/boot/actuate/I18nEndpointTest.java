package io.microsphere.i18n.spring.boot.actuate;

import com.jayway.jsonpath.DocumentContext;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.boot.actuate.autoconfigure.I18nEndpointAutoConfiguration;
import io.microsphere.i18n.spring.boot.autoconfigure.I18nAutoConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static com.jayway.jsonpath.JsonPath.using;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static io.microsphere.i18n.ServiceMessageSource.COMMON_SOURCE;
import static io.microsphere.i18n.spring.constants.I18nConstants.SERVICE_MESSAGE_SOURCE_BEAN_NAME;
import static java.util.Collections.emptyMap;
import static java.util.Locale.FRANCE;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static java.util.Locale.getDefault;
import static java.util.Locale.setDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link I18nEndpoint} Test
 *
 * @author li.qi
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see I18nEndpoint
 * @since 1.0.0
 */
@SpringBootTest(
        classes = I18nEndpointTest.EndpointConfiguration.class,
        properties = {
                "management.endpoint.i18n.enabled=true",
                "management.endpoints.web.exposure.include=*"
        },
        webEnvironment = NONE
)
class I18nEndpointTest {

    private static final Locale defaultLocale = getDefault();

    @BeforeAll
    static void beforeAll() {
        setDefault(SIMPLIFIED_CHINESE);
    }

    @AfterAll
    static void afterAll() {
        setDefault(defaultLocale);
    }

    @Autowired
    private ApplicationContext context;

    @Autowired
    @Qualifier(SERVICE_MESSAGE_SOURCE_BEAN_NAME)
    private ServiceMessageSource primaryServiceMessageSource;

    @Autowired
    private I18nEndpoint i18nEndpoint;

    @Test
    void testInvoke() {
        Map<String, Map<String, String>> invoke = this.i18nEndpoint.invoke();
        DocumentContext parse = using(defaultConfiguration()).parse(invoke);
        assertThat(parse, hasJsonPath("$.['common.i18n_messages_en.properties']"));
        assertThat(parse, hasJsonPath("$.['common.i18n_messages_zh.properties']"));
        assertThat(parse, hasJsonPath("$.['common.i18n_messages_zh_CN.properties']"));
        assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_en.properties']"));
        assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_en.properties'].['common.a']", equalTo("a")));
        assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_zh.properties']"));
        assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_zh_CN.properties']"));
        assertThat(parse, hasJsonPath("$.['META-INF/i18n/common/i18n_messages_zh_CN.properties'].['common.a']", equalTo("啊")));
    }

    @Test
    void testGetMessageWithCode() {
        List<Map<String, String>> messages = this.i18nEndpoint.getMessage("a");
        assertGetMessage(messages, 6);
    }

    @Test
    void testGetMessageWithCodeAndLocale() {
        List<Map<String, String>> messages = this.i18nEndpoint.getMessage("a", SIMPLIFIED_CHINESE);
        assertGetMessage(messages, 2);
    }

    @Test
    void testAddMessage() throws IOException {
        Map<String, Object> properties = this.i18nEndpoint.addMessage(COMMON_SOURCE, FRANCE, "a", "$a");
        assertEquals(1, properties.size());

        properties = this.i18nEndpoint.addMessage(COMMON_SOURCE, FRANCE, "a", "$a");
        assertEquals(1, properties.size());

        properties = this.i18nEndpoint.addMessage(COMMON_SOURCE, FRANCE, "b", "$b");
        assertEquals(1, properties.size());

        properties = this.i18nEndpoint.addMessage("not-found-source", FRANCE, "b", "$b");
        assertSame(emptyMap(), properties);
    }

    void assertGetMessage(List<Map<String, String>> messagesList, int expectedSize) {
        assertEquals(expectedSize, messagesList.size());
        for (int i = 0; i < expectedSize; i++) {
            Map<String, String> messages = messagesList.get(i);
            assertEquals(5, messages.size());

            String code = messages.get("code");
            String locale = messages.get("locale");

            assertEquals("a", code);
            assertNotNull(locale);
            assertNotNull(messages.get("source"));
            assertNotNull(messages.get("resource"));
            assertTrue(messages.containsKey("message"));
        }
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