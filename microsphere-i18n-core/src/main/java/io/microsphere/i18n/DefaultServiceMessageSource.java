package io.microsphere.i18n;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;

/**
 * Default {@link ServiceMessageSource} Class that loads i18n messages from classpath resources
 * under {@code META-INF/i18n/{source}/}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
 *   source.init();
 *   assertEquals("测试-a", source.getMessage("a"));
 *   assertEquals("Hello,World", source.getMessage("hello", Locale.ENGLISH, "World"));
 *   source.destroy();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class DefaultServiceMessageSource extends PropertiesResourceServiceMessageSource implements
        ReloadableResourceServiceMessageSource {

    /**
     * Resource path pattern
     */
    public static final String RESOURCE_LOCATION_PATTERN = "META-INF/i18n/{}/{}";

    @Nonnull
    private final ClassLoader classLoader;

    /**
     * Constructs with the given source using the default class loader.
     *
     * @param source the source identifier
     */
    public DefaultServiceMessageSource(String source) {
        this(source, null);
    }

    /**
     * Constructs with the given source and optional class loader.
     *
     * @param source      the source identifier
     * @param classLoader the class loader to use, or {@code null} for the default
     */
    public DefaultServiceMessageSource(String source, @Nullable ClassLoader classLoader) {
        super(source);
        this.classLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
    }

    @Override
    public boolean canReload(String changedResource) {
        return this.classLoader.getResource(changedResource) != null;
    }

    protected String getResource(String resourceName) {
        return format(RESOURCE_LOCATION_PATTERN, getSource(), resourceName);
    }

    @Override
    protected List<Reader> loadAllPropertiesResources(String resource) throws IOException {
        Enumeration<URL> resources = getResources(resource);
        Charset encoding = getEncoding();
        List<Reader> propertiesResources = new LinkedList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            propertiesResources.add(new InputStreamReader(url.openStream(), encoding));
        }
        return propertiesResources;
    }

    protected Enumeration<URL> getResources(String resource) throws IOException {
        return this.classLoader.getResources(resource);
    }
}
