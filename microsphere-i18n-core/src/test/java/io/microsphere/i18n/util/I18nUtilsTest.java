package io.microsphere.i18n.util;

import io.microsphere.i18n.AbstractI18nTest;
import io.microsphere.i18n.CompositeServiceMessageSource;
import io.microsphere.i18n.DefaultServiceMessageSource;
import io.microsphere.i18n.EmptyServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import org.junit.Test;

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.i18n.EmptyServiceMessageSource.INSTANCE;
import static io.microsphere.i18n.util.I18nUtils.findAllServiceMessageSources;
import static io.microsphere.i18n.util.I18nUtils.serviceMessageSource;
import static io.microsphere.i18n.util.I18nUtils.setServiceMessageSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * {@link I18nUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class I18nUtilsTest extends AbstractI18nTest {

    @Test
    public void test() {
        assertSame(INSTANCE, serviceMessageSource());

        DefaultServiceMessageSource defaultServiceMessageSource = new DefaultServiceMessageSource("test");
        setServiceMessageSource(defaultServiceMessageSource);

        assertSame(defaultServiceMessageSource, serviceMessageSource());
    }

    @Test
    public void testFindAllServiceMessageSources() {
        List<ServiceMessageSource> serviceMessageSources = ofList(INSTANCE);
        CompositeServiceMessageSource compositeServiceMessageSource = new CompositeServiceMessageSource(ofList(INSTANCE));
        assertEquals(serviceMessageSources, findAllServiceMessageSources(compositeServiceMessageSource));
        assertEquals(serviceMessageSources, findAllServiceMessageSources(compositeServiceMessageSource, ServiceMessageSource.class));
        assertEquals(serviceMessageSources, findAllServiceMessageSources(compositeServiceMessageSource, EmptyServiceMessageSource.class));
    }
}