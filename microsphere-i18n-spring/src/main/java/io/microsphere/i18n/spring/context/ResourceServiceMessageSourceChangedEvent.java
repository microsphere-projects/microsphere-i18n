package io.microsphere.i18n.spring.context;

import io.microsphere.i18n.ResourceServiceMessageSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * {@link ResourceServiceMessageSource} Changed {@link ApplicationEvent Event},
 * published when i18n resource files are modified at runtime.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   ApplicationContext context = ...;
 *   context.publishEvent(new ResourceServiceMessageSourceChangedEvent(
 *       context, Arrays.asList("META-INF/i18n/test/i18n_messages_en.properties")));
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ApplicationEvent
 * @since 1.0.0
 */
public class ResourceServiceMessageSourceChangedEvent extends ApplicationContextEvent {

    private final Iterable<String> changedResources;

    /**
     * Constructs the event.
     *
     * @param source           the {@link ApplicationContext} that publishes this event
     * @param changedResources the changed resource paths
     */
    public ResourceServiceMessageSourceChangedEvent(ApplicationContext source, Iterable<String> changedResources) {
        super(source);
        this.changedResources = changedResources;
    }

    /**
     * Gets the changed resource paths.
     *
     * @return the changed resource paths
     */
    public Iterable<String> getChangedResources() {
        return changedResources;
    }
}