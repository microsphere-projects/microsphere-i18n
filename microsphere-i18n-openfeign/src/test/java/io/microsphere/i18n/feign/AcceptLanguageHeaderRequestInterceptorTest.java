package io.microsphere.i18n.feign;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletRequestAttributes;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.i18n.feign.AcceptLanguageHeaderRequestInterceptor.HEADER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes;
import static org.springframework.web.context.request.RequestContextHolder.setRequestAttributes;

/**
 * {@link AcceptLanguageHeaderRequestInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class AcceptLanguageHeaderRequestInterceptorTest {

    private AcceptLanguageHeaderRequestInterceptor requestInterceptor;

    private RequestTemplate requestTemplate;

    @BeforeEach
    void before() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER_NAME, "en");
        setRequestAttributes(new ServletRequestAttributes(request));

        this.requestInterceptor = new AcceptLanguageHeaderRequestInterceptor();
        this.requestTemplate = new RequestTemplate();
    }

    @AfterEach
    public void after(){
        resetRequestAttributes();
    }

    @Test
    public void testApply() {
        assertTrue(requestTemplate.headers().isEmpty());
        requestInterceptor.apply(requestTemplate);
        assertEquals(ofList("en"), requestTemplate.headers().get("Accept-Language"));
    }

    @Test
    public void testApplyNoWebMvc() {
        resetRequestAttributes();
        assertTrue(requestTemplate.headers().isEmpty());
        requestInterceptor.apply(new RequestTemplate());
        assertTrue(requestTemplate.headers().isEmpty());
    }
}