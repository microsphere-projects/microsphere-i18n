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
 * Spring {@link MessageSource} Adapter that delegates message resolution to the
 * underlying {@link ServiceMessageSource}, falling back to other {@link MessageSource} beans.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Automatically registered when @EnableI18n(exposeMessageSource = true)
 *   @Autowired
 *   private MessageSource messageSource; // This is the MessageSourceAdapter
 *   String msg = messageSource.getMessage("hello", new Object[]{"World"}, Locale.ENGLISH);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableI18n#exposeMessageSource()
 * @since 1.0.0
 */
public class MessageSourceAdapter implements MessageSource, SmartInitializingSingleton, BeanFactoryAware {

    private final ServiceMessageSource serviceMessageSource;

    private final List<MessageSource> defaultMessageSources;

    private BeanFactory beanFactory;

    /**
     * Constructs the adapter with the given {@link ServiceMessageSource}.
     *
     * @param serviceMessageSource the underlying {@link ServiceMessageSource}
     */
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

    /**
     * Resolves a default message by delegating to fallback {@link MessageSource} beans.
     *
     * @param code           the message code
     * @param args           the message arguments
     * @param defaultMessage the default message
     * @param locale         the target locale
     * @return the resolved message, or the default message if not found
     */
    protected String getDefaultMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        for (MessageSource defaultMessageSource : this.defaultMessageSources) {
            String message = defaultMessageSource.getMessage(code, args, defaultMessage, locale);
            if (message != null) {
                return message;
            }
        }
        return defaultMessage;
    }
