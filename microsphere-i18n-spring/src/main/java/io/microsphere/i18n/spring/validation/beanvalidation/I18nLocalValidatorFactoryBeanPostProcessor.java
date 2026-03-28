package io.microsphere.i18n.spring.validation.beanvalidation;

import io.microsphere.i18n.spring.annotation.EnableI18n;
import io.microsphere.i18n.spring.context.MessageSourceAdapter;
import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.config.GenericBeanPostProcessorAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.MessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.springframework.util.ClassUtils.isPresent;


/**
 * Internationalization {@link BeanPostProcessor} for {@link LocalValidatorFactoryBean}
 * that sets the i18n {@link MessageSourceAdapter} as the validation message source.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Automatically registered via @EnableI18n when Jakarta Validation API is on classpath
 *   // Associates LocalValidatorFactoryBean with the i18n MessageSourceAdapter
 *   @EnableI18n
 *   @Configuration
 *   public class AppConfig { }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see LocalValidatorFactoryBean#setValidationMessageSource(MessageSource)
 * @see MessageSourceAdapter
 * @see EnableI18n
 * @since 1.0.0
 */
public class I18nLocalValidatorFactoryBeanPostProcessor extends GenericBeanPostProcessorAdapter<LocalValidatorFactoryBean> {

    private static final String VALIDATOR_FACTORY_CLASS_NAME = "javax.validation.ValidatorFactory";

    private static final Logger logger = getLogger(I18nLocalValidatorFactoryBeanPostProcessor.class);

    private final ObjectProvider<MessageSourceAdapter> messageSourceAdapterProvider;

    /**
     * Constructs with the given {@link MessageSourceAdapter} provider.
     *
     * @param messageSourceAdapterProvider the {@link MessageSourceAdapter} provider
     */
    public I18nLocalValidatorFactoryBeanPostProcessor(ObjectProvider<MessageSourceAdapter> messageSourceAdapterProvider) {
        this.messageSourceAdapterProvider = messageSourceAdapterProvider;
    }

    @Override
    protected void processBeforeInitialization(LocalValidatorFactoryBean localValidatorFactoryBean, String beanName) throws BeansException {
        messageSourceAdapterProvider.ifAvailable(messageSourceAdapter -> {
            localValidatorFactoryBean.setValidationMessageSource(messageSourceAdapter);
            if (logger.isTraceEnabled()) {
                logger.trace("LocalValidatorFactoryBean[name : '{}'] is associated with MessageSource : {}", beanName, messageSourceAdapter);
            }
        });
    }

    /**
     * Checks whether Jakarta Validation API is present on the classpath.
     *
     * @param classLoader the class loader to check
     * @return {@code true} if the ValidatorFactory class is available
     */
    public static boolean isValidatorFactoryPresent(ClassLoader classLoader) {
        return isPresent(VALIDATOR_FACTORY_CLASS_NAME, classLoader);
    }
}