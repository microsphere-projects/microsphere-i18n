package io.microsphere.i18n.spring.context;

import io.microsphere.i18n.ServiceMessageSource;
import io.microsphere.i18n.spring.annotation.EnableI18n;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;

/**
 * Spring {@link MessageSource} Adapter
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableI18n#exposeMessageSource()
 * @since 1.0.0
 */
public class MessageSourceAdapter implements MessageSource, SmartInitializingSingleton, BeanFactoryAware {

    private final ServiceMessageSource serviceMessageSource;

    private final List<MessageSource> defaultMessageSources;

    private BeanFactory beanFactory;

    public MessageSourceAdapter(ServiceMessageSource serviceMessageSource) {
        this.serviceMessageSource = serviceMessageSource;
        this.defaultMessageSources = new ArrayList<>(2);
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        String message = this.serviceMessageSource.getMessage(code, locale, args);
        if (message == null) {
            message = getDefaultMessage(code, args, defaultMessage, locale);
        }
        return message;
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
        return getMessage(code, args, null, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
        String message = null;
        for (String code : resolvable.getCodes()) {
            message = getMessage(code, resolvable.getArguments(), resolvable.getDefaultMessage(), locale);
            if (message != null) {
                break;
            }
        }
        return message;
    }

    @Override
    public void afterSingletonsInstantiated() {
        for (MessageSource messageSource : getSortedBeans(this.beanFactory, MessageSource.class)) {
            if (messageSource != this) {
                this.defaultMessageSources.add(messageSource);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public String toString() {
        return "MessageSourceAdapter{" +
                "serviceMessageSource=" + serviceMessageSource +
                ", defaultMessageSources=" + defaultMessageSources +
                '}';
    }

    private String getDefaultMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        for (MessageSource defaultMessageSource : this.defaultMessageSources) {
            String message = defaultMessageSource.getMessage(code, args, defaultMessage, locale);
            if (message != null) {
                return message;
            }
        }
        return defaultMessage;
    }
}