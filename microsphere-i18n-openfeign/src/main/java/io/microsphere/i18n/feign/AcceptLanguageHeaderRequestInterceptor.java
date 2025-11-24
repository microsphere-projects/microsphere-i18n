package io.microsphere.i18n.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.microsphere.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;

/**
 * HTTP Header "Accept-Language" {@link RequestInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AcceptHeaderLocaleResolver
 * @since 1.0.0
 */
public class AcceptLanguageHeaderRequestInterceptor implements RequestInterceptor {

    private static final Logger logger = getLogger(AcceptLanguageHeaderRequestInterceptor.class);

    public static final String HEADER_NAME = ACCEPT_LANGUAGE;

    @Override
    public void apply(RequestTemplate template) {
        RequestAttributes requestAttributes = getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes)) {
            logger.trace("Feign calls in non-Spring WebMVC scenarios, ignoring setting request headers: '{}'", HEADER_NAME);
            return;
        }

        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;

        HttpServletRequest request = servletRequestAttributes.getRequest();

        String acceptLanguage = request.getHeader(HEADER_NAME);

        if (hasText(acceptLanguage)) {
            template.header(HEADER_NAME, acceptLanguage);
            logger.trace("Feign has set HTTP request header [name : '{}' , value : '{}']", HEADER_NAME, acceptLanguage);
        } else {
            logger.trace("Feign could not set HTTP request header [name : '{}'] because the requester did not pass: '{}'", HEADER_NAME, acceptLanguage);
        }
    }
}