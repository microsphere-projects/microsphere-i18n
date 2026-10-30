package io.microsphere.i18n;

/**
 * Reloadable {@link ResourceServiceMessageSource} that supports dynamic reloading
 * of internationalized message resources at runtime.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
 *   source.init();
 *   if (source.canReload("META-INF/i18n/test/i18n_messages_en.properties")) {
 *       source.reload("META-INF/i18n/test/i18n_messages_en.properties");
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ReloadableResourceServiceMessageSource extends ResourceServiceMessageSource {

    /**
     * Whether the specified resource can be reloaded at runtime.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
     *   source.init();
     *   boolean reloadable = source.canReload("META-INF/i18n/test/i18n_messages_en.properties");
     *   // returns true if the resource exists
     * }</pre>
     *
     * @param changedResource the changed resource path
     * @return {@code true} if reloadable, default returns {@code true}
     */
    boolean canReload(String changedResource);

    /**
     * Whether the specified resource list can be reloaded at runtime.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
     *   source.init();
     *   boolean reloadable = source.canReload(Arrays.asList(
     *       "i18n_messages_en.properties",
     *       "i18n_messages_zh_CN.properties"
     * ));
     * }</pre>
     *
     * @param changedResources the list of changed resources
     * @return {@code true} if any resource is reloadable, default returns {@code true}
     */
    default boolean canReload(Iterable<String> changedResources) {
        boolean reloadable = false;
        for (String changedResource : changedResources) {
            if (reloadable = canReload(changedResource)) {
                break;
            }
        }
        return reloadable;
    }

    /**
     * Reloads the specified resource if {@link #canReload(String)} returns {@code true}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
     *   source.init();
     *   source.reload("i18n_messages_en.properties");
     * }</pre>
     *
     * @param changedResource the changed resource path
     */
    default void reload(String changedResource) {
        initializeResource(changedResource);
    }

    /**
     * Reloads the specified resources if {@link #canReload(Iterable)} returns {@code true}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
     *   source.init();
     *   source.reload(Arrays.asList(
     *       "i18n_messages_en.properties",
     *       "i18n_messages_zh_CN.properties"
     * ));
     * }</pre>
     *
     * @param changedResources the list of changed resources
     */
    default void reload(Iterable<String> changedResources) {
        initializeResources(changedResources);
    }
}
