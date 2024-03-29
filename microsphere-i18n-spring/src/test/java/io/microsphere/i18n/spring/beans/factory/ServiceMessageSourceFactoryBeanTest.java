package io.microsphere.i18n.spring.beans.factory;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import io.microsphere.i18n.spring.context.ResourceServiceMessageSourceChangedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link ServiceMessageSourceFactoryBean} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
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
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ConfigurableEnvironment environment;

    private MockPropertySource propertySource;

    @Before
    public void before() {
        super.before();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        propertySource = new MockPropertySource("mock");
        environment.getPropertySources().addFirst(propertySource);
    }

    @Test
    public void testGetMessage() {
        assertEquals("test-a", serviceMessageSource.getMessage("a"));
        assertEquals("Hello,World", serviceMessageSource.getMessage("hello", "World"));

        // Test FRANCE
        assertNull(serviceMessageSource.getMessage("a", Locale.FRANCE));

        ResourceServiceMessageSourceChangedEvent event = new ResourceServiceMessageSourceChangedEvent(context, Arrays.asList("test.i18n_messages_en.properties"));
        propertySource.setProperty("test.i18n_messages_en.properties", "test.a=1");
        eventPublisher.publishEvent(event);
        assertEquals("1", serviceMessageSource.getMessage("a"));
    }

    @Test
    public void testGetLocale() {
        assertEquals(Locale.ENGLISH, serviceMessageSource.getLocale());

        // Test US
        LocaleContextHolder.setLocale(Locale.US);
        assertEquals(Locale.US, serviceMessageSource.getLocale());
    }

    @Test
    public void testGetDefaultLocale() {
        assertEquals(Locale.ENGLISH, serviceMessageSource.getDefaultLocale());
    }

    @Test
    public void testGetSupportedLocales() {
        assertEquals(asList(Locale.ENGLISH), serviceMessageSource.getSupportedLocales());
    }

    @Test
    public void testGetSource() {
        assertEquals("test", serviceMessageSource.getSource());
    }

}
