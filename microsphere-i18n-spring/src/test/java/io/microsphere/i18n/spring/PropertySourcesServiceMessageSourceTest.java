package io.microsphere.i18n.spring;

import io.microsphere.i18n.AbstractSpringTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.context.i18n.LocaleContextHolder.setLocale;
import static org.springframework.util.StreamUtils.copyToString;

/**
 * {@link PropertySourcesServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class PropertySourcesServiceMessageSourceTest extends AbstractSpringTest {

    @BeforeEach
    protected void before() {
        super.before();
        setLocale(SIMPLIFIED_CHINESE);
    }

    @Test
    public void test() throws IOException {
        MockEnvironment environment = new MockEnvironment();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("META-INF/i18n/test/i18n_messages_zh_CN.properties");
        String propertiesContent = copyToString(inputStream, UTF_8);
        environment.setProperty("test.i18n_messages_zh_CN.properties", propertiesContent);

        PropertySourcesServiceMessageSource serviceMessageSource = new PropertySourcesServiceMessageSource("test");
        serviceMessageSource.setEnvironment(environment);
        serviceMessageSource.init();

        assertEquals("测试-a", serviceMessageSource.getMessage("a"));
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));
    }

    @Test
    public void testNotFound() {
    }
}