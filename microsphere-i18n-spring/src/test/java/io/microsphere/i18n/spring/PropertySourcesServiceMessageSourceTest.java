package io.microsphere.i18n.spring;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.DefaultServiceMessageSource;
import io.microsphere.io.StringBuilderWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PropertySourcesServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class PropertySourcesServiceMessageSourceTest extends AbstractSpringTest {

    private MockEnvironment environment;

    private PropertySourcesServiceMessageSource propertySourcesServiceMessageSource;

    private String testResource = "META-INF/i18n/test/i18n_messages_zh_CN.properties";

    private String testPropertyName = "test.i18n_messages_zh_CN.properties";

    @BeforeEach
    protected void before() throws Throwable {
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
    void test() throws IOException {
        assertEquals("测试-a", this.propertySourcesServiceMessageSource.getMessage("a"));
        assertEquals("您好,World", this.propertySourcesServiceMessageSource.getMessage("hello", "World"));
        assertNull(this.propertySourcesServiceMessageSource.getMessage("not-found-code"));
    }

    @Test
    void testCanReload() {
        assertTrue(this.propertySourcesServiceMessageSource.canReload(this.testPropertyName));
        assertTrue(this.propertySourcesServiceMessageSource.canReload(ofSet(this.testPropertyName)));

        assertFalse(this.propertySourcesServiceMessageSource.canReload(emptySet()));

        String notFoundResource = "not-found.properties";
        assertFalse(this.propertySourcesServiceMessageSource.canReload(notFoundResource));
        assertFalse(this.propertySourcesServiceMessageSource.canReload(ofSet(notFoundResource)));
    }

    @Test
    void testFindAllPropertySourcesServiceMessageSourcesOnNoBeanDefinition() {
        testInSpringContainer(context -> {
            assertTrue(findAllPropertySourcesServiceMessageSources(context).isEmpty());
        });
    }

    @Test
    void testFindAllPropertySourcesServiceMessageSourcesOnSingleBeanDefinition() {
        testInSpringContainer(context -> {
            List<PropertySourcesServiceMessageSource> allPropertySourcesServiceMessageSources = findAllPropertySourcesServiceMessageSources(context);
            assertEquals(1, allPropertySourcesServiceMessageSources.size());
            PropertySourcesServiceMessageSource propertySourcesServiceMessageSource = allPropertySourcesServiceMessageSources.get(0);
            assertEquals(TEST_SOURCE, propertySourcesServiceMessageSource.getSource());
        }, SingleBeanConfig.class);
    }

    @Test
    void testFindAllPropertySourcesServiceMessageSourcesOnDelegatingBeanDefinition() {
        testInSpringContainer(context -> {
            List<PropertySourcesServiceMessageSource> allPropertySourcesServiceMessageSources = findAllPropertySourcesServiceMessageSources(context);
            assertEquals(1, allPropertySourcesServiceMessageSources.size());
            PropertySourcesServiceMessageSource propertySourcesServiceMessageSource = allPropertySourcesServiceMessageSources.get(0);
            assertEquals(TEST_SOURCE, propertySourcesServiceMessageSource.getSource());
        }, DelegatingBeanConfig.class);
    }

    @Test
    void testFindAllPropertySourcesServiceMessageSourcesOnEmptyCollection() {
        assertSame(emptyList(), findAllPropertySourcesServiceMessageSources(emptyList()));
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