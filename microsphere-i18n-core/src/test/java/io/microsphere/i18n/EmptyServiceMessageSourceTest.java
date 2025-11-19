package io.microsphere.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.i18n.EmptyServiceMessageSource.INSTANCE;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link EmptyServiceMessageSource} 测试
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class EmptyServiceMessageSourceTest extends AbstractI18nTest {

    private EmptyServiceMessageSource serviceMessageSource = INSTANCE;

    @BeforeEach
    protected void before() {
        super.before();
        serviceMessageSource.init();
    }

    @AfterEach
    protected void after() {
        super.after();
        serviceMessageSource.destroy();
    }

    @Test
    void testGetMessage() {
        assertNull(serviceMessageSource.getMessage("test"));
        assertNull(serviceMessageSource.getMessage("test", "a"));
        assertNull(serviceMessageSource.getMessage("test", ENGLISH, "a"));
    }

    @Test
    void testGetSource() {
        assertEquals("Empty", serviceMessageSource.getSource());
    }

    @Test
    void testGetDefaultLocale() {
        assertEquals(SIMPLIFIED_CHINESE, serviceMessageSource.getDefaultLocale());
    }

    @Test
    void testGetLocale() {
        assertEquals(SIMPLIFIED_CHINESE, serviceMessageSource.getLocale());
    }

    @Test
    void testGetSupportedLocales() {
        assertEquals(ofList(SIMPLIFIED_CHINESE, ENGLISH), serviceMessageSource.getSupportedLocales());
    }
}
