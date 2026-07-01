package io.microsphere.i18n.spring.boot.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.microsphere.i18n.spring.constants.I18nConstants.ENABLED_PROPERTY_NAME;

/**
 * {@link Conditional @Conditional} that checks whether I18n is availiable.
 * Combines class presence checks and property condition ({@code microsphere.i18n.enabled}).
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @ConditionalOnI18nAvailiable
 *   @Configuration
 *   public class MyI18nConfig {
 *       // Only activated when i18n core and spring modules are on classpath
 *       // and microsphere.i18n.enabled is true (default)
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@ConditionalOnClass(name = {
        "io.microsphere.i18n.ServiceMessageSource", // microsphere-i18n-core
        "io.microsphere.i18n.spring.annotation.EnableI18n", // microsphere-i18n-spring
})
@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)
public @interface ConditionalOnI18nAvailiable {
}