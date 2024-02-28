package io.microsphere.i18n.util;

import io.microsphere.i18n.AbstractI18nTest;
import io.microsphere.i18n.DefaultServiceMessageSource;
import io.microsphere.i18n.EmptyServiceMessageSource;
import org.junit.Test;

import static org.junit.Assert.assertSame;

/**
 * {@link I18nUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nUtilsTest extends AbstractI18nTest {

    private I18nUtils i18nUtils;

    @Test
    public void test() {
        assertSame(EmptyServiceMessageSource.INSTANCE, I18nUtils.serviceMessageSource());

        DefaultServiceMessageSource defaultServiceMessageSource = new DefaultServiceMessageSource("test");
        I18nUtils.setServiceMessageSource(defaultServiceMessageSource);

        assertSame(defaultServiceMessageSource, I18nUtils.serviceMessageSource());
    }


}
