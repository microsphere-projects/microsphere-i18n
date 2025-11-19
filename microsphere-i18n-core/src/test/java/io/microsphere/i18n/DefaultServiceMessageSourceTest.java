package io.microsphere.i18n;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link DefaultServiceMessageSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class DefaultServiceMessageSourceTest extends AbstractI18nTest {

    @Test
    void test() {
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("test");
        serviceMessageSource.init();

        assertEquals("测试-a", serviceMessageSource.getMessage("a"));
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));

        serviceMessageSource.destroy();
    }

    @Test
    void testValidateMessageCode() {
        assertThrows(IllegalStateException.class, () -> {
            DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("error");
            serviceMessageSource.init();
        });
    }
}