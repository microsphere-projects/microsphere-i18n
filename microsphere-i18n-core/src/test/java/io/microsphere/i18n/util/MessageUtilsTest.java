package io.microsphere.i18n.util;

import io.microsphere.i18n.AbstractI18nTest;
import io.microsphere.i18n.DefaultServiceMessageSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static io.microsphere.i18n.util.I18nUtils.setServiceMessageSource;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class MessageUtilsTest extends AbstractI18nTest {

    @Before
    public void before() {
        super.before();
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("test");
        serviceMessageSource.init();
        setServiceMessageSource(serviceMessageSource);
    }

    @Test
    public void testGetLocalizedMessage() {
        // Testing Simplified Chinese
        // null
        Assert.assertEquals(null, MessageUtils.getLocalizedMessage(null));
        // If the message argument is "a", the pattern "{" "}" is not included, and the original content is returned
        assertEquals("a", MessageUtils.getLocalizedMessage("a"));
        // "{a}" is the Message Code template, where "a" is Message Code
        assertEquals("测试-a", MessageUtils.getLocalizedMessage("{a}"));

        // The same is true for overloaded methods with Message Pattern arguments
        assertEquals("hello", MessageUtils.getLocalizedMessage("hello", "World"));
        assertEquals("您好,World", MessageUtils.getLocalizedMessage("{hello}", "World"));

        // If message code does not exist, return the original content of message
        assertEquals("{code-not-found}", MessageUtils.getLocalizedMessage("{code-not-found}"));
        assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{microsphere-test.code-not-found}"));
        assertEquals("code-not-found", MessageUtils.getLocalizedMessage("{common.code-not-found}"));

        // The test of English
        assertEquals("hello", MessageUtils.getLocalizedMessage("hello", Locale.ENGLISH, "World"));
        assertEquals("Hello,World", MessageUtils.getLocalizedMessage("{hello}", Locale.ENGLISH, "World"));
    }
}
