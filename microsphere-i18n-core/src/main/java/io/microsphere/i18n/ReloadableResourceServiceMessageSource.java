package io.microsphere.i18n;

import java.util.Set;

/**
 * Reloadable {@link ResourceServiceMessageSource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface ReloadableResourceServiceMessageSource extends ResourceServiceMessageSource {

    /**
     * Reload if {@link #canReload(String)} returns <code>true</code>,
     * The calling {@link #initializeResource(String)} as default
     *
     * @param changedResource Changes in the resource
     */
    default void reload(String changedResource) {
        initializeResource(changedResource);
    }

    /**
     * Reload if {@link #canReload(Iterable)} returns <code>true</code>,
     * The calling {@link #initializeResources(Iterable)} as default
     *
     * @param changedResources Changes in the resources
     */
    default void reload(Iterable<String> changedResources) {
        initializeResources(changedResources);
    }

    /**
     * Whether the specified resource can be overloaded
     *
     * @param changedResource Changes in the resource
     * @return Supported by default, returning <code>true<code>
     */
    default boolean canReload(String changedResource) {
        Set<String> resources = getInitializedResources();
        return resources.contains(changedResource);
    }

    /**
     * Whether the specified resource list can be overloaded
     *
     * @param changedResources Changes in the resource
     * @return Supported by default, returning <code>true<code>
     */
    default boolean canReload(Iterable<String> changedResources) {
        Set<String> resources = getInitializedResources();
        boolean reloadable = false;
        for (String changedResource : changedResources) {
            if (reloadable = resources.contains(changedResource)) {
                break;
            }
        }
        return reloadable;
    }
}