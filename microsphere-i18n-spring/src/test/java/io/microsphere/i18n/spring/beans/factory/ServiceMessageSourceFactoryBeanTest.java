package io.microsphere.i18n.spring.beans.factory;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.config.TestSourceEnableI18nConfiguration;
import io.microsphere.i18n.spring.context.ResourceServiceMessageSourceChangedEvent;
import io.microsphere.logging.test.jupiter.LoggingLevelsTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;
import java.util.Set;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Sets.ofSet;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRANCE;
import static java.util.Locale.US;
import static java.util.Locale.getDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.context.i18n.LocaleContextHolder.resetLocaleContext;
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
        TestSourceEnableI18nConfiguration.class
})
@TestPropertySource(properties = {
        "microsphere.i18n.default-locale=en",
        "microsphere.i18n.supported-locales=en",
})
class ServiceMessageSourceFactoryBeanTest extends AbstractSpringTest {

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
        assertEquals("test-a", this.serviceMessageSource.getMessage("a"));
        assertEquals("Hello,World", this.serviceMessageSource.getMessage("hello", "World"));

        // Test FRANCE
        assertNull(this.serviceMessageSource.getMessage("a", FRANCE));

        ResourceServiceMessageSourceChangedEvent event = new ResourceServiceMessageSourceChangedEvent(context, ofList("test.i18n_messages_en.properties"));
        propertySource.setProperty("test.i18n_messages_en.properties", "test.a=1");
        eventPublisher.publishEvent(event);
        assertEquals("1", this.serviceMessageSource.getMessage("a"));
    }

    @Test
    void testGetLocale() {
        assertEquals(ENGLISH, this.serviceMessageSource.getLocale());
        assertEquals(ENGLISH, this.serviceMessageSourceFactoryBean.getLocale());

        // Test US
        setLocale(US);
        assertEquals(US, this.serviceMessageSource.getLocale());
        assertEquals(US, this.serviceMessageSourceFactoryBean.getLocale());

        // Reset
        resetLocaleContext();
        assertEquals(getDefault(), this.serviceMessageSource.getLocale());
        assertEquals(getDefault(), this.serviceMessageSourceFactoryBean.getLocale());
    }

    @Test
    void testGetDefaultLocale() {
        assertEquals(ENGLISH, this.serviceMessageSource.getDefaultLocale());
        assertEquals(ENGLISH, this.serviceMessageSourceFactoryBean.getDefaultLocale());
    }

    @Test
    void testGetSupportedLocales() {
        assertEquals(ofSet(ENGLISH), this.serviceMessageSource.getSupportedLocales());
        assertEquals(ofSet(ENGLISH), this.serviceMessageSourceFactoryBean.getSupportedLocales());
    }

    @Test
    void testGetSource() {
        assertEquals(TEST_SOURCE, this.serviceMessageSourceFactoryBean.getSource());
    }

    @Test
    void testSetOrder() {
        this.serviceMessageSourceFactoryBean.setOrder(1);
        assertEquals(1, this.serviceMessageSourceFactoryBean.getOrder());
    }

    @Test
    @LoggingLevelsTest(levels = "ERROR")
    void testResolveSupportedLocale() {
        Locale locale = this.serviceMessageSourceFactoryBean.resolveDefaultLocale(this.environment);
        assertEquals(ENGLISH, locale);

        MockEnvironment environment = new MockEnvironment();
        locale = this.serviceMessageSourceFactoryBean.resolveDefaultLocale(environment);
        assertEquals(this.serviceMessageSourceFactoryBean.getDefaultLocale(), locale);
    }

    @Test
    @LoggingLevelsTest(levels = "ERROR")
    void testResolveSupportedLocales() {
        Set<Locale> locales = this.serviceMessageSourceFactoryBean.resolveSupportedLocales(this.environment);
        assertEquals(ofSet(ENGLISH), locales);

        MockEnvironment environment = new MockEnvironment();
        locales = this.serviceMessageSourceFactoryBean.resolveSupportedLocales(environment);
        assertEquals(this.serviceMessageSourceFactoryBean.getDefaultSupportedLocales(), locales);
    }

    @Test
    @LoggingLevelsTest(levels = "ERROR")
    void testOnApplicationEvent() {
        ResourceServiceMessageSourceChangedEvent event = new ResourceServiceMessageSourceChangedEvent(this.context, ofList("test.i18n_messages_en.properties"));
        this.serviceMessageSourceFactoryBean.onApplicationEvent(event);
    }

    @Test
    void testToString() {
        assertNotNull(this.serviceMessageSource.toString());
    }
