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
 * Internationalization {@link BeanPostProcessor} for {@link LocalValidatorFactoryBean}.
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

    public I18nLocalValidatorFactoryBeanPostProcessor(ObjectProvider<MessageSourceAdapter> messageSourceAdapterProvider) {
        this.messageSourceAdapterProvider = messageSourceAdapterProvider;
    }

    @Override
    protected void processBeforeInitialization(LocalValidatorFactoryBean localValidatorFactoryBean, String beanName) throws BeansException {
        messageSourceAdapterProvider.ifAvailable(messageSourceAdapter -> {
            localValidatorFactoryBean.setValidationMessageSource(messageSourceAdapter);
            logger.trace("LocalValidatorFactoryBean[name : '{}'] is associated with MessageSource : {}", beanName, messageSourceAdapter);
        });
    }

    public static boolean isValidatorFactoryPresent(ClassLoader classLoader) {
        return isPresent(VALIDATOR_FACTORY_CLASS_NAME, classLoader);
    }
}