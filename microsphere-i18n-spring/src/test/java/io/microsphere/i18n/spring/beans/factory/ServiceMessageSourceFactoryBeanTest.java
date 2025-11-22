package io.microsphere.i18n.spring.beans.factory;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import io.microsphere.i18n.spring.context.ResourceServiceMessageSourceChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Sets.ofSet;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRANCE;
import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.context.i18n.LocaleContextHolder.setLocale;

/**
 * {@link ServiceMessageSourceFactoryBean} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ServiceMessageSourceFactoryBean
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ServiceMessageSourceFactoryBeanTest.class,
        TestServiceMessageSourceConfiguration.class
})
@TestPropertySource(properties = {
        "microsphere.i18n.default-locale=en",
        "microsphere.i18n.supported-locales=en",
})
public class ServiceMessageSourceFactoryBeanTest extends AbstractSpringTest {

    @Autowired
    private ServiceMessageSource serviceMessageSource;

    @Autowired
    private ServiceMessageSourceFactoryBean serviceMessageSourceFactoryBean;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigurableEnvironment environment;

    private MockPropertySource propertySource;

    @BeforeEach
    protected void before() throws Throwable {
        super.before();
        setLocale(ENGLISH);
        propertySource = new MockPropertySource("mock");
        environment.getPropertySources().addFirst(propertySource);
    }

    @Test
    void testGetMessage() {
        assertEquals("test-a", serviceMessageSource.getMessage("a"));
        assertEquals("Hello,World", serviceMessageSource.getMessage("hello", "World"));

        // Test FRANCE
        assertNull(serviceMessageSource.getMessage("a", FRANCE));

        ResourceServiceMessageSourceChangedEvent event = new ResourceServiceMessageSourceChangedEvent(context, ofList("test.i18n_messages_en.properties"));
        propertySource.setProperty("test.i18n_messages_en.properties", "test.a=1");
        eventPublisher.publishEvent(event);
        assertEquals("1", serviceMessageSource.getMessage("a"));
    }

    @Test
    void testGetLocale() {
        assertEquals(ENGLISH, serviceMessageSource.getLocale());

        // Test US
        setLocale(US);
        assertEquals(US, serviceMessageSource.getLocale());
    }

    @Test
    void testGetDefaultLocale() {
        assertEquals(ENGLISH, serviceMessageSource.getDefaultLocale());
    }

    @Test
    void testGetSupportedLocales() {
        assertEquals(ofSet(ENGLISH), serviceMessageSource.getSupportedLocales());
    }

    @Test
    void testGetSource() {
        assertEquals("test", serviceMessageSource.getSource());
    }

    @Test
    void testSetOrder() {
        serviceMessageSourceFactoryBean.setOrder(1);
        assertEquals(1, serviceMessageSourceFactoryBean.getOrder());
    }

    @Test
    void testToString() {
        assertTrue(serviceMessageSource.toString().startsWith("ServiceMessageSourceFactoryBean"));
    }
}