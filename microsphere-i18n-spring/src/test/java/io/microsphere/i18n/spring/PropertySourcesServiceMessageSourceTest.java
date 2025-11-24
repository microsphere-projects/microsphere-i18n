package io.microsphere.i18n.spring;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.DefaultServiceMessageSource;
import io.microsphere.io.StringBuilderWriter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.i18n.spring.PropertySourcesServiceMessageSource.findAllPropertySourcesServiceMessageSources;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link PropertySourcesServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertySourcesServiceMessageSource
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

    @Test
    public void testFindAllPropertySourcesServiceMessageSourcesOnNoBeanDefinition() {
        testInSpringContainer(context -> {
            assertTrue(findAllPropertySourcesServiceMessageSources(context).isEmpty());
        });
    }

    @Test
    public void testFindAllPropertySourcesServiceMessageSourcesOnSingleBeanDefinition() {
        testInSpringContainer(context -> {
            List<PropertySourcesServiceMessageSource> allPropertySourcesServiceMessageSources = findAllPropertySourcesServiceMessageSources(context);
            assertEquals(1, allPropertySourcesServiceMessageSources.size());
            PropertySourcesServiceMessageSource propertySourcesServiceMessageSource = allPropertySourcesServiceMessageSources.get(0);
            assertEquals(TEST_SOURCE, propertySourcesServiceMessageSource.getSource());
        }, SingleBeanConfig.class);
    }

    @Test
    public void testFindAllPropertySourcesServiceMessageSourcesOnDelegatingBeanDefinition() {
        testInSpringContainer(context -> {
            List<PropertySourcesServiceMessageSource> allPropertySourcesServiceMessageSources = findAllPropertySourcesServiceMessageSources(context);
            assertEquals(1, allPropertySourcesServiceMessageSources.size());
            PropertySourcesServiceMessageSource propertySourcesServiceMessageSource = allPropertySourcesServiceMessageSources.get(0);
            assertEquals(TEST_SOURCE, propertySourcesServiceMessageSource.getSource());
        }, DelegatingBeanConfig.class);
    }

    @Test
    public void testFindAllPropertySourcesServiceMessageSourcesOnEmptyCollection() {
        assertSame(emptyList(), findAllPropertySourcesServiceMessageSources(emptyList()));
    }

    @Test
    public void testGetResource() {
        assertEquals(this.testPropertyName, this.propertySourcesServiceMessageSource.getResource("i18n_messages_zh_CN.properties"));
    }
    
    @Test
    public void testGetPropertyName() {
        assertEquals(this.testPropertyName, this.propertySourcesServiceMessageSource.getPropertyName(SIMPLIFIED_CHINESE));
    }

    static class SingleBeanConfig {

        @Bean
        public PropertySourcesServiceMessageSource propertySourcesServiceMessageSource() {
            return new PropertySourcesServiceMessageSource(TEST_SOURCE);
        }
    }

    @Import(SingleBeanConfig.class)
    static class DelegatingBeanConfig {

        @Bean
        @Primary
        public DelegatingServiceMessageSource delegatingServiceMessageSource() {
            return new DelegatingServiceMessageSource();
        }
    }
}