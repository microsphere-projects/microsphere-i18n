package io.microsphere.i18n;

import org.junit.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRANCE;
import static java.util.Locale.getDefault;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link DefaultServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class DefaultServiceMessageSourceTest extends ResourceServiceMessageSourceTest {

    @Override
    protected DefaultServiceMessageSource createServiceMessageSource() {
        return new DefaultServiceMessageSource(TEST_SOURCE);
    }

    @Test
    @Override
    public void testGetMessage() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();

        assertEquals("测试-a", serviceMessageSource.getMessage("a"));
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));
    }

    @Test
    public void testResources() {
        ResourceServiceMessageSource serviceMessageSource = getServiceMessageSource();
        serviceMessageSource.initializeResource(ERROR_SOURCE);
        assertFalse(serviceMessageSource.getInitializedResources().isEmpty());
    }

    @Test
    @Override
    public void testGetSource() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();
        assertEquals(TEST_SOURCE, serviceMessageSource.getSource());
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateMessageCode() {
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("error");
        serviceMessageSource.init();
    }

    @Test
    public void testGetInternalLocale() {
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource(TEST_SOURCE) {

            @Override
            protected Locale getInternalLocale() {
                return getDefaultLocale();
            }
        };
        assertSame(serviceMessageSource.getLocale(), serviceMessageSource.getDefaultLocale());
    }

    @Test
    public void testSupports() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();

        assertTrue(serviceMessageSource.supports(getDefault()));
        assertTrue(serviceMessageSource.supports(ENGLISH));
    }

    @Test
    public void testResolveMessageCode() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();

        String code = "test.code";
        assertEquals(code, serviceMessageSource.resolveMessageCode(code));
        assertEquals(code, serviceMessageSource.resolveMessageCode("code"));
    }

    @Test
    public void testGetLocalizedResourceMessages() {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();

        Map<String, Map<String, String>> localizedResourceMessages = serviceMessageSource.getLocalizedResourceMessages();
        assertEquals(2, localizedResourceMessages.size());
    }

    @Test
    public void testLoadAllProperties() throws IOException {
        DefaultServiceMessageSource serviceMessageSource = getServiceMessageSource();
        for (Locale locale : serviceMessageSource.getSupportedLocales()) {
            assertNotNull(serviceMessageSource.loadAllProperties(locale));
        }

        assertNull(serviceMessageSource.loadAllProperties(FRANCE));
    }
}