package io.microsphere.i18n;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRANCE;
import static java.util.Locale.getDefault;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class DefaultServiceMessageSourceTest extends ResourceServiceMessageSourceTest {

    @Override
    protected DefaultServiceMessageSource createServiceMessageSource() {
        return new DefaultServiceMessageSource(TEST_SOURCE);
    }

    @Test
    @Override
    void testGetMessage() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();

        assertEquals("测试-a", serviceMessageSource.getMessage("a"));
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));
    }

    @Test
    void testResources() {
        ResourceServiceMessageSource serviceMessageSource = getServiceMessageSource();
        serviceMessageSource.initializeResource(ERROR_SOURCE);
        assertFalse(serviceMessageSource.getInitializedResources().isEmpty());
    }

    @Test
    @Override
    void testGetSource() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();
        assertEquals(TEST_SOURCE, serviceMessageSource.getSource());
    }

    @Test
    void testValidateMessageCode() {
        assertThrows(IllegalStateException.class, () -> {
            DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource(ERROR_SOURCE);
            serviceMessageSource.initialize();
        });
    }

    @Test
    void testGetInternalLocale() {
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource(TEST_SOURCE) {

            @Override
            protected Locale getInternalLocale() {
                return getDefaultLocale();
            }
        };
        assertSame(serviceMessageSource.getLocale(), serviceMessageSource.getDefaultLocale());
    }

    @Test
    void testSupports() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();

        assertTrue(serviceMessageSource.supports(getDefault()));
        assertTrue(serviceMessageSource.supports(ENGLISH));
    }

    @Test
    void testResolveMessageCode() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();

        String code = "test.code";
        assertEquals(code, serviceMessageSource.resolveMessageCode(code));
        assertEquals(code, serviceMessageSource.resolveMessageCode("code"));
    }

    @Test
    void testGetLocalizedResourceMessages() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();

        Map<String, Map<String, String>> localizedResourceMessages = serviceMessageSource.getLocalizedResourceMessages();
        assertEquals(2, localizedResourceMessages.size());
    }

    @Test
    void testLoadAllProperties() throws IOException {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();
        for (Locale locale : serviceMessageSource.getSupportedLocales()) {
            assertNotNull(serviceMessageSource.loadAllProperties(locale));
        }

        assertNull(serviceMessageSource.loadAllProperties(FRANCE));
    }
}