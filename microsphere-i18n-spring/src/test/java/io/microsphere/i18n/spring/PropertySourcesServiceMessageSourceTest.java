package io.microsphere.i18n.spring;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.DefaultServiceMessageSource;
import io.microsphere.io.StringBuilderWriter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static io.microsphere.collection.Sets.ofSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.StreamUtils.copyToString;

/**
 * {@link PropertySourcesServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class PropertySourcesServiceMessageSourceTest extends AbstractSpringTest {

    private MockEnvironment environment;

    private PropertySourcesServiceMessageSource propertySourcesServiceMessageSource;

    private String testResource = "META-INF/i18n/test/i18n_messages_zh_CN.properties";

    private String testPropertyName = "test.i18n_messages_zh_CN.properties";

    @Before
    public void before() throws Throwable {
        super.before();
        this.environment = new MockEnvironment();
        this.initEnvironment();
        this.propertySourcesServiceMessageSource = new PropertySourcesServiceMessageSource(TEST_SOURCE);
        this.propertySourcesServiceMessageSource.setEnvironment(environment);
        this.propertySourcesServiceMessageSource.init();
    }

    void initEnvironment() throws IOException {
        DefaultServiceMessageSource defaultServiceMessageSource = new DefaultServiceMessageSource(TEST_SOURCE);

        Properties properties = defaultServiceMessageSource.loadAllProperties(testResource);
        StringBuilderWriter writer = new StringBuilderWriter();
        properties.store(writer, "");
        this.environment.setProperty(testPropertyName, writer.toString());
    }

    @Test
    public void test() throws IOException {
        assertEquals("测试-a", this.propertySourcesServiceMessageSource.getMessage("a"));
        assertEquals("您好,World", this.propertySourcesServiceMessageSource.getMessage("hello", "World"));
        assertNull(this.propertySourcesServiceMessageSource.getMessage("not-found-code"));
    }

    @Test
    public void testCanReload() {
        assertTrue(this.propertySourcesServiceMessageSource.canReload(this.testPropertyName));
        assertTrue(this.propertySourcesServiceMessageSource.canReload(ofSet(this.testPropertyName)));

        assertFalse(this.propertySourcesServiceMessageSource.canReload(emptySet()));

        String notFoundResource = "not-found.properties";
        assertFalse(this.propertySourcesServiceMessageSource.canReload(notFoundResource));
        assertFalse(this.propertySourcesServiceMessageSource.canReload(ofSet(notFoundResource)));
    }
}