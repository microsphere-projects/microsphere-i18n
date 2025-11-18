package io.microsphere.i18n.spring.annotation;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.beans.TestServiceMessageSourceConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.microsphere.i18n.util.I18nUtils.serviceMessageSource;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.US;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * {@link EnableI18n} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        EnableI18nTest.class,
        TestServiceMessageSourceConfiguration.class
})
@EnableI18n
public class EnableI18nTest extends AbstractSpringTest {

    @Autowired
    private ServiceMessageSource serviceMessageSource;

    @Test
    public void testGetMessage() {
        // Testing Simplified Chinese
        // If the Message Code is "a"
        assertEquals("测试-a", serviceMessageSource.getMessage("a"));

        // The same is true for overloaded methods with Message Pattern arguments
        assertEquals("您好,World", serviceMessageSource.getMessage("hello", "World"));

        // Returns null if code does not exist
        assertNull(serviceMessageSource.getMessage("code-not-found"));

        // Test English, because the English Message resource does not exist
        assertEquals("Hello,World", serviceMessageSource.getMessage("hello", ENGLISH, "World"));

        // Returns null if code does not exist
        assertNull(serviceMessageSource.getMessage("code-not-found", US));
    }

    @Test
    public void testCommonServiceMessageSource() {
        assertSame(serviceMessageSource(), serviceMessageSource);
    }
}