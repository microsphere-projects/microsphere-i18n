package io.microsphere.i18n.spring;

import io.microsphere.i18n.PropertiesResourceServiceMessageSource;
import io.microsphere.i18n.ReloadableResourceServiceMessageSource;
import io.microsphere.i18n.ServiceMessageSource;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySources;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.springframework.util.StringUtils.hasText;

/**
 * Spring {@link PropertySources} {@link ServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class PropertySourcesServiceMessageSource extends PropertiesResourceServiceMessageSource implements ReloadableResourceServiceMessageSource, EnvironmentAware {

    private Environment environment;

    public PropertySourcesServiceMessageSource(String source) {
        super(source);
    }

    @Override
    protected String getResource(String resourceName) {
        return getSource() + "." + resourceName;
    }

    @Nullable
    @Override
    protected Locale getInternalLocale() {
        return LocaleContextHolder.getLocale();
    }

    @Override
    protected List<Reader> loadAllPropertiesResources(String resource) throws IOException {
        String propertiesContent = getPropertiesContent(resource);
        return hasText(propertiesContent) ? asList(new StringReader(propertiesContent)) : emptyList();
    }

    protected String getPropertiesContent(String resource) {
        String propertyName = getPropertyName(resource);
        String propertiesContent = environment.getProperty(propertyName);
        return propertiesContent;
    }

    public String getPropertyName(Locale locale) {
        String resource = getResource(locale);
        String propertyName = getPropertyName(resource);
        return propertyName;
    }

    protected String getPropertyName(String resource) {
        String propertyName = resource;
        return propertyName;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
