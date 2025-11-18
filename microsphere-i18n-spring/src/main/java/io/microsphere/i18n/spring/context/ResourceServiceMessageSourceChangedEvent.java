package io.microsphere.i18n.spring.context;

import io.microsphere.i18n.ResourceServiceMessageSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * {@link ResourceServiceMessageSource} Changed {@link ApplicationEvent Event}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ApplicationEvent
 * @since 1.0.0
 */
public class ResourceServiceMessageSourceChangedEvent extends ApplicationContextEvent {

    private final Iterable<String> changedResources;

    public ResourceServiceMessageSourceChangedEvent(ApplicationContext source, Iterable<String> changedResources) {
        super(source);
        this.changedResources = changedResources;
    }

    public Iterable<String> getChangedResources() {
        return changedResources;
    }
}