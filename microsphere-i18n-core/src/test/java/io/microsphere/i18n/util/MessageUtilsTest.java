package io.microsphere.i18n.util;

import io.microsphere.i18n.AbstractI18nTest;
import io.microsphere.i18n.DefaultServiceMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.i18n.util.I18nUtils.setServiceMessageSource;
import static io.microsphere.i18n.util.MessageUtils.MESSAGE_PATTERN_PREFIX;
import static io.microsphere.i18n.util.MessageUtils.MESSAGE_PATTERN_SUFFIX;
import static io.microsphere.i18n.util.MessageUtils.SOURCE_SEPARATOR;
import static io.microsphere.i18n.util.MessageUtils.getLocalizedMessage;
import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link MessageUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class MessageUtilsTest extends AbstractI18nTest {

    @BeforeEach
    protected void before() {
        super.before();
        DefaultServiceMessageSource serviceMessageSource = new DefaultServiceMessageSource("test");
        serviceMessageSource.init();
        setServiceMessageSource(serviceMessageSource);
    }

    @Test
    void testConstants() {
        assertEquals("{", MESSAGE_PATTERN_PREFIX);
        assertEquals("}", MESSAGE_PATTERN_SUFFIX);
        assertEquals(".", SOURCE_SEPARATOR);
    }

    @Test
    void testGetLocalizedMessage() {
        // Testing Simplified Chinese
        // null
        assertEquals(null, getLocalizedMessage(null));
        // If the message argument is "a", the pattern "{" "}" is not included, and the original content is returned
        assertEquals("a", getLocalizedMessage("a"));
        // "{a}" is the Message Code template, where "a" is Message Code
        assertEquals("测试-a", getLocalizedMessage("{a}"));

        // The same is true for overloaded methods with Message Pattern arguments
        assertEquals("hello", getLocalizedMessage("hello", "World"));
        assertEquals("您好,World", getLocalizedMessage("{hello}", "World"));

        // If message code does not exist, return the original content of message
        assertEquals("{code-not-found}", getLocalizedMessage("{code-not-found}"));
        assertEquals("code-not-found", getLocalizedMessage("{microsphere-test.code-not-found}"));
        assertEquals("code-not-found", getLocalizedMessage("{common.code-not-found}"));

        // The test of English
        assertEquals("hello", getLocalizedMessage("hello", ENGLISH, "World"));
        assertEquals("Hello,World", getLocalizedMessage("{hello}", ENGLISH, "World"));


        assertEquals("{a.}", getLocalizedMessage("{a.}", ENGLISH, "World"));
    }
}