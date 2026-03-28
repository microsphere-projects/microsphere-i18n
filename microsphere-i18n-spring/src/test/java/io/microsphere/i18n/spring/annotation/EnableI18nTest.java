package io.microsphere.i18n.spring.annotation;

import io.microsphere.i18n.AbstractSpringTest;
import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.config.TestSourceEnableI18nConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.microsphere.i18n.util.I18nUtils.serviceMessageSource;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link EnableI18n} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TestSourceEnableI18nConfiguration.class
})
class EnableI18nTest extends AbstractSpringTest {

    @Autowired
    private ServiceMessageSource serviceMessageSource;

    @Test
    void testGetMessage() {
        assertGetMessage(this.serviceMessageSource);

        // Test English, because the English Message resource does not exist
        assertEquals("Hello,World", serviceMessageSource.getMessage("hello", ENGLISH, "World"));

        // Returns null if code does not exist
        assertNull(serviceMessageSource.getMessage("code-not-found", US));
    }

    @Test
    void testCommonServiceMessageSource() {
        assertSame(serviceMessageSource(), serviceMessageSource);
    }
}