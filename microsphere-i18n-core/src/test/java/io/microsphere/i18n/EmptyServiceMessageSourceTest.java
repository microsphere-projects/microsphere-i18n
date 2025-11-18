package io.microsphere.i18n;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.i18n.EmptyServiceMessageSource.INSTANCE;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link EmptyServiceMessageSource} 测试
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class EmptyServiceMessageSourceTest extends AbstractI18nTest {

    private EmptyServiceMessageSource serviceMessageSource = INSTANCE;

    @Before
    public void before() {
        super.before();
        serviceMessageSource.init();
    }

    @After
    public void after() {
        super.after();
        serviceMessageSource.destroy();
    }

    @Test
    public void testGetMessage() {
        assertNull(serviceMessageSource.getMessage("test"));
        assertNull(serviceMessageSource.getMessage("test", "a"));
        assertNull(serviceMessageSource.getMessage("test", ENGLISH, "a"));
    }

    @Test
    public void testGetSource() {
        assertEquals("Empty", serviceMessageSource.getSource());
    }

    @Test
    public void testGetDefaultLocale() {
        assertEquals(SIMPLIFIED_CHINESE, serviceMessageSource.getDefaultLocale());
    }

    @Test
    public void testGetLocale() {
        assertEquals(SIMPLIFIED_CHINESE, serviceMessageSource.getLocale());
    }

    @Test
    public void testGetSupportedLocales() {
        assertEquals(ofList(SIMPLIFIED_CHINESE, ENGLISH), serviceMessageSource.getSupportedLocales());
    }
}
