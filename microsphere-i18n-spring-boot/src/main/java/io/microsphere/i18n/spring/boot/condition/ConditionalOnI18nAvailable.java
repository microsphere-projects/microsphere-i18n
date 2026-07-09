package io.microsphere.i18n.spring.boot.condition;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.i18n.spring.constants.I18nConstants.ENABLED_PROPERTY_NAME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link Conditional @Conditional} that checks whether I18n is available.
 * Combines class presence checks and property condition ({@code microsphere.i18n.enabled}).
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @ConditionalOnI18nAvailable
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
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Documented
@ConditionalOnClass(name = {
        "io.microsphere.i18n.ServiceMessageSource",         // Microsphere i18n Core
        "io.microsphere.i18n.spring.annotation.EnableI18n", // Microsphere i18n Spring
})
@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)
public @interface ConditionalOnI18nAvailable {
}