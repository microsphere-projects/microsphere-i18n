package io.microsphere.i18n.spring;

import io.microsphere.i18n.AbstractSpringTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * {@link PropertySourcesServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class PropertySourcesServiceMessageSourceTest extends AbstractSpringTest {

    @Before
    public void before() {
        super.before();
        LocaleContextHolder.setLocale(Locale.SIMPLIFIED_CHINESE);
    }

    @Test
    public void test() throws IOException {
        MockEnvironment environment = new MockEnvironment();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("META-INF/i18n/test/i18n_messages_zh_CN.properties");
        String propertiesContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
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
