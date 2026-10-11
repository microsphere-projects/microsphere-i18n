# OpenFeign Integration (`microsphere-i18n-openfeign`, 1.x line)

The `microsphere-i18n-openfeign` module propagates the caller's language preference across
OpenFeign service-to-service calls, so that a downstream microservice resolves its messages in the
same locale the end user requested from the entry-point service.

> This is the documentation for the `1.x` line (`0.1.x` artifacts, `javax.*` namespace, Spring Boot
> 2.x / Spring Cloud Hoxton–2021.0.x). For Spring Boot 3.x / `jakarta.*` applications, read the
> [current (0.2.x) OpenFeign guide](../current/openfeign.md) instead.

## Overview

In a microservice topology, the browser or mobile client sends an `Accept-Language` header to the
first service only. Every subsequent hop is a new HTTP request built by a Feign client, and Feign
does not copy headers by default. Without help, the downstream service sees no `Accept-Language`
and falls back to its own default locale.

This module contributes a single class,
`io.microsphere.i18n.feign.AcceptLanguageHeaderRequestInterceptor`, which implements Feign's
`feign.RequestInterceptor` SPI. For every outgoing Feign request, it looks at the HTTP request the
current thread is serving and copies its `Accept-Language` header value verbatim onto the Feign
`RequestTemplate`. On this branch the request type it reads is `javax.servlet.http.HttpServletRequest`.

The module is deliberately small and passive:

- it depends on `microsphere-i18n-core`, but does not call `ServiceMessageSource` itself;
- it does not parse, negotiate, or filter the locale — it forwards the raw header;
- locale negotiation still happens in the **receiving** service, through Spring MVC's
  `AcceptHeaderLocaleResolver` (which the Spring integration module reconfigures with the i18n
  default and supported locales, see [Spring](spring.md)).

```
client (Accept-Language: zh_CN)
   |
   v
service A  -- Feign call -->  service B
   AcceptLanguageHeaderRequestInterceptor copies the header      service B resolves
   on the current request thread                                 messages in zh_CN
```

## Maven Coordinates

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-i18n-openfeign</artifactId>
</dependency>
```

No `<version>` is needed when the `microsphere-i18n-dependencies` BOM (version `0.1.21` on this
line) is imported; see [Dependencies BOM](dependencies.md).

The module declares only `microsphere-i18n-core` as a required dependency. `feign-core`,
`spring-webmvc` and `javax.servlet:javax.servlet-api` are declared **optional**, so they are used to
compile the module but are not exported: your application must already provide them. A Spring Cloud
web application on Boot 2.x normally does, through `spring-cloud-starter-openfeign` and
`spring-boot-starter-web` (which bring the Servlet 4.x `javax.*` API via Tomcat).

## Version and Compatibility

| Item                  | Value                                                                     |
|-----------------------|---------------------------------------------------------------------------|
| Artifact              | `io.github.microsphere-projects:microsphere-i18n-openfeign`               |
| Version line          | `0.1.x` (latest published release `0.1.21`; working `0.1.22-SNAPSHOT` on `release-1.x`) |
| Minimum Java          | 8 (source level inherited from the microsphere parent chain)              |
| Namespace             | `javax.*` (not `jakarta.*`)                                               |
| Spring Boot           | 2.x — generation profiles pin 2.3.12.RELEASE / 2.4.13 / 2.6.15            |
| Spring Cloud          | Hoxton.SR12 – 2021.0.x                                                    |
| Compile-time Feign    | `io.github.openfeign:feign-core` (optional; `11.10` under the default `spring-cloud-2021` profile) |
| Compile-time Web MVC  | `org.springframework:spring-webmvc` (optional; `5.3.27` in the same default build) |
| Servlet API           | `javax.servlet:javax.servlet-api` (optional; `4.0.1` in the same default build) |
| Class `@since`        | `1.0.0`                                                                   |

The exact third-party versions come from the Spring Cloud generation profile selected for the build,
not from this module's POM: `spring-cloud-hoxton` (Boot 2.3.12.RELEASE / Hoxton.SR12),
`spring-cloud-2020` (Boot 2.4.13 / 2020.0.5) and `spring-cloud-2021` (Boot 2.6.15 / 2021.0.9, the
default). Under Hoxton the Feign line is 10.x; the module's code compiles against either. See
[Parent POM](parent.md) for the generation-to-version mapping.

For applications on Spring Boot 3.x / Spring Cloud 2022.0.x and later (`jakarta.*` namespace), use
the `current` (0.2.x) documentation set instead.

## API: `AcceptLanguageHeaderRequestInterceptor`

| Element                        | Description                                                                                     |
|--------------------------------|-------------------------------------------------------------------------------------------------|
| Class                          | `io.microsphere.i18n.feign.AcceptLanguageHeaderRequestInterceptor`                              |
| Implements                     | `feign.RequestInterceptor`                                                                      |
| `HEADER_NAME`                  | `public static final String`, value `Accept-Language` (from `org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE`) |
| `apply(RequestTemplate)`        | The only behavior method: copies the incoming request's `Accept-Language` header onto the template |
| Constructors                   | public no-argument constructor only                                                              |

### Where the `Accept-Language` value comes from

`apply(RequestTemplate)` resolves the header from the **current HTTP request**, not from
`LocaleContextHolder`:

1. `RequestContextHolder.getRequestAttributes()` is read for the current thread.
2. If the result is not a `ServletRequestAttributes` — no request bound to the thread, a
   non-servlet context, or a background thread — the interceptor logs a TRACE message and returns
   without touching the template.
3. Otherwise `ServletRequestAttributes.getRequest()` gives the `javax.servlet.http.HttpServletRequest`,
   and `request.getHeader("Accept-Language")` is read.
4. If the value has text (`StringUtils.hasText`), it is written to the template with
   `template.header("Accept-Language", value)`.
5. If the value is `null` or blank, nothing is written; a TRACE message records that the requester
   did not pass the header.

Key consequences:

- **There is no fallback to the JVM default locale, to `LocaleContextHolder`, or to
  `ServiceMessageSource.getLocale()`.** If the incoming request carried no `Accept-Language`, the
  Feign request carries none either, and the downstream service applies its own default.
- The header value is forwarded **verbatim**, including quality values, e.g.
  `zh-CN,zh;q=0.9,en;q=0.8`. Negotiation is left to the downstream `AcceptHeaderLocaleResolver`.
- Requests issued outside a servlet request thread (scheduled tasks, message listeners, async work
  that does not inherit request attributes) are not enriched.

## Registering the Interceptor

The module ships no `META-INF/spring.factories` and no `AutoConfiguration.imports` entry — it has no
`src/main/resources` at all — and none of the Spring Boot / Spring Cloud auto-configurations on this
branch reference this class. You must declare the bean yourself:

```java

@Configuration
public class I18nFeignConfig {

    @Bean
    public AcceptLanguageHeaderRequestInterceptor acceptLanguageHeaderRequestInterceptor() {
        return new AcceptLanguageHeaderRequestInterceptor();
    }
}
```

A `RequestInterceptor` bean in the root application context is applied to all Feign clients. To
scope it to one client, reference the configuration class from `@FeignClient`:

```java

@FeignClient(name = "user-service", configuration = I18nFeignConfig.class)
public interface UserClient {

    @GetMapping("/users/{id}")
    User getUser(@PathVariable("id") Long id);
}
```

When the configuration class is used as a per-client `configuration`, keep it out of component
scanning so it does not also become global.

The unit test of the module shows the same registration plus the thread-binding the interceptor
relies on (`microsphere-i18n-openfeign/src/test/java/io/microsphere/i18n/feign/AcceptLanguageHeaderRequestInterceptorTest.java`):

```java

@SpringLoggingTest
class AcceptLanguageHeaderRequestInterceptorTest {

    private AcceptLanguageHeaderRequestInterceptor requestInterceptor;
    private RequestTemplate requestTemplate;
    private MockHttpServletRequest request;

    @BeforeEach
    void before() {
        this.request = new MockHttpServletRequest();
        this.requestInterceptor = new AcceptLanguageHeaderRequestInterceptor();
        this.requestTemplate = new RequestTemplate();
        // bind the simulated request to the current thread
        setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void after() {
        resetRequestAttributes();
    }

    @Test
    void testApply() {
        this.request.addHeader(HEADER_NAME, "en");
        requestInterceptor.apply(requestTemplate);
        assertEquals(ofList("en"), requestTemplate.headers().get("Accept-Language"));
    }
}
```

`MockHttpServletRequest` and `ServletRequestAttributes` come from `spring-test` / `spring-web`
5.3.x, and `ofList` is `io.microsphere.collection.Lists.ofList` — the tests are written at the
Java 8 language level on this branch.

## End-to-End Usage

A typical Spring Cloud setup, combining this module with the Spring Boot integration:

`pom.xml`:

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-i18n-dependencies</artifactId>
            <version>${microsphere-i18n.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-i18n-spring-boot</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-i18n-openfeign</artifactId>
    </dependency>
</dependencies>
```

Entry-point service (`order-service`):

```java

@Configuration
public class I18nFeignConfig {

    @Bean
    public AcceptLanguageHeaderRequestInterceptor acceptLanguageHeaderRequestInterceptor() {
        return new AcceptLanguageHeaderRequestInterceptor();
    }
}

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/users/profile")
    Profile profile();
}

@RestController
public class OrderController {

    private final UserClient userClient;
    private final ServiceMessageSource messageSource;

    public OrderController(UserClient userClient, ServiceMessageSource messageSource) {
        this.userClient = userClient;
        this.messageSource = messageSource;
    }

    @GetMapping("/orders/summary")
    public String summary() {
        // Local message in the caller's locale
        String title = messageSource.getMessage("order.summary.title");
        // The downstream call carries the same Accept-Language header
        Profile profile = userClient.profile();
        return title + " - " + profile.greeting();
    }
}
```

Downstream service (`user-service`) needs no extra code: its `AcceptHeaderLocaleResolver` reads the
propagated header, Spring stores the resolved locale in `LocaleContextHolder`, and
`ServiceMessageSource.getMessage(code, args)` picks it up.

Message files, per the framework convention:

```
src/main/resources/META-INF/i18n/order-service/
├── i18n_messages_en.properties      # order.summary.title=Order summary
└── i18n_messages_zh_CN.properties   # order.summary.title=订单摘要
```

## Verifying the Behavior

The interceptor is silent unless TRACE is enabled for its logger:

```properties
logging.level.io.microsphere.i18n.feign.AcceptLanguageHeaderRequestInterceptor=TRACE
```

You will see one of:

- `Feign has set HTTP request header [name : 'Accept-Language' , value : 'zh_CN']`
- `Feign could not set HTTP request header [name : 'Accept-Language'] because the requester did not
  pass: 'null'`
- `Feign calls in non-Spring WebMVC scenarios, ignoring setting request headers:
  'Accept-Language'`

To confirm the header on the wire, inspect the request with a Feign `Logger.Level` (`FULL`) or with
the Spring Cloud OpenFeign logging configuration available on this generation
(`feign.client.config.default.loggerLevel=FULL`).

## Notes and Limitations

- Only `Accept-Language` is propagated. Other locale-related headers are untouched.
- The interceptor is not registered automatically by any microsphere auto-configuration in the
  `0.1.x` line either; the bean declaration above is required.
- Because the lookup is a thread-local `RequestContextHolder` read, Feign calls made from other
  threads (for example `@Async` methods or reactive pipelines) need explicit propagation, such as
  `RequestContextHolder.setRequestAttributes(attributes, true)` for inheritable thread locals, or
  setting the header per call with `@RequestHeader`.
- In Spring Cloud OpenFeign 2.x, a `RequestInterceptor` bean defined in the main context is applied
  to all clients unless the configuration is scoped per client.
- If several interceptors write the same header, Feign's `RequestTemplate` semantics govern the
  result; keep one `Accept-Language` writer per request to stay deterministic.
- The jar is `javax.*`-only. Deploying it into a Servlet 5/6 container (Boot 3, Undertow/Jetty with
  `jakarta.servlet`) fails with `NoClassDefFoundError: javax/servlet/http/HttpServletRequest`;
  use the 0.2.x line there.

## Differences from the 0.2.x line

The production class is the same code on both branches except for one import:

```java
// release-1.x (0.1.x)                          // release (0.2.x)
import javax.servlet.http.HttpServletRequest;   import jakarta.servlet.http.HttpServletRequest;
```

Everything else — `apply(RequestTemplate)`, the TRACE-only logging, the `HEADER_NAME` constant, the
constructor set — is identical, as is the unit test.

| Concern | 1.x (this document) | 0.2.x |
|---------|---------------------|-------|
| Version line | `0.1.x` (`0.1.21` published, `0.1.22-SNAPSHOT` working) | `0.2.x` (`0.2.21` / `0.2.22-SNAPSHOT`) |
| Java baseline | 8 | 17 |
| Servlet coordinate in the module POM | `javax.servlet:javax.servlet-api` (optional) | `jakarta.servlet:jakarta.servlet-api` (optional) |
| Servlet container generation targeted | Servlet 4.x (`javax.*`), Tomcat 9 | Servlet 6.0 (`jakarta.*`), Tomcat 10+ |
| Spring Boot / Spring Cloud | 2.3–2.7 / Hoxton.SR12–2021.0.x | 3.x / 2022.0.x–2025.0.x |
| Typical compile-time Feign | 10.x (Hoxton) or 11.x (2020/2021) | 12.x/13.x |
| Auto-configuration mechanism in sibling modules | `META-INF/spring.factories` (`EnableAutoConfiguration`) — Spring Boot 2.x style | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |
| Registration of this interceptor | manual `@Bean` (module ships no resources) | manual `@Bean` (same) |

Migration notes:

- Moving an application from `0.1.x` to `0.2.x` (or the reverse) is a namespace decision, not an API
  change: the class name, package and behaviour are the same, so the only edits are the dependency
  version and, in tests, `MockHttpServletRequest` imports that follow the container namespace.
- On this line the interceptor's `@ConditionalOnClass`-style guards are irrelevant because the module
  ships no auto-configuration at all; nothing to enable or disable.

## Related Documentation

- [Core](core.md) — `ServiceMessageSource` and locale resolution
- [Spring](spring.md) — `AcceptHeaderLocaleResolver` customization
- [Spring Boot](spring-boot.md) — auto-configuration (registered via `spring.factories` on this line)
- [Parent POM](parent.md), [Dependencies BOM](dependencies.md)
- Developer guide: [OpenFeign module](../../developer-guide/1.x/openfeign.md)
- 0.2.x equivalent: [current OpenFeign user guide](../current/openfeign.md)

## Module dependencies

Depends on `core` only; independent of the Spring chain.

```mermaid
flowchart TD
    CORE[core] --> SPRING[spring] --> BOOT[spring-boot] --> CLOUD[spring-cloud] --> SERVER[spring-cloud-server]
    CORE --> FEIGN[openfeign]
    style FEIGN stroke-width:3px
```
