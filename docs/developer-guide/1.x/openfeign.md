# OpenFeign Module — Developer Guide (1.x line)

Module: `microsphere-i18n-openfeign` on the **`release-1.x` branch**
Java package: `io.microsphere.i18n.feign`
Size: one production class and one unit test.

> Documentation for the `1.x` line (`0.1.x` artifacts, `javax.*` namespace, Spring Boot 2.x). The
> 0.2.x equivalent is [here](../current/openfeign.md).

```
microsphere-i18n-openfeign/
├── pom.xml
└── src/
    ├── main/java/io/microsphere/i18n/feign/
    │   └── AcceptLanguageHeaderRequestInterceptor.java
    └── test/java/io/microsphere/i18n/feign/
        └── AcceptLanguageHeaderRequestInterceptorTest.java
```

There is no `src/main/resources`: the module contributes no `spring.factories`, no
`AutoConfiguration.imports`, and no metadata file. Registration is left to the application. This is
the same on both lines, but note that on `release-1.x` the *sibling* Spring modules register their
auto-configurations through `META-INF/spring.factories` (the Spring Boot 2.x mechanism) — so even if
this module ever gained auto-configuration here, it would be declared with the
`org.springframework.boot.autoconfigure.EnableAutoConfiguration` key, not only in
`AutoConfiguration.imports`.

## 1. Source Analysis

### Class Declaration

```java
public class AcceptLanguageHeaderRequestInterceptor implements RequestInterceptor {

    private static final Logger logger = getLogger(AcceptLanguageHeaderRequestInterceptor.class);

    public static final String HEADER_NAME = ACCEPT_LANGUAGE;
```

Notable details:

- `RequestInterceptor` is `feign.RequestInterceptor`, the SPI Feign calls while building a request.
  On this line the resolved artifact is OpenFeign 10.x/11.x depending on the active generation
  profile (11.10 in the default `spring-cloud-2021` build); the interface shape used here is
  unchanged across those versions.
- `HEADER_NAME` reuses `org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE` via a static import,
  so the constant is guaranteed to be the literal `Accept-Language` Spring itself uses.
- `HttpServletRequest` is imported from **`javax.servlet.http`**, which is the one line of source
  that differs from the 0.2.x branch (there it is `jakarta.servlet.http`).
- `Logger` is `io.microsphere.logging.Logger` with
  `io.microsphere.logging.LoggerFactory.getLogger(...)` — the Microsphere logging facade, consistent
  with the rest of the framework, not SLF4J or Logback directly.
- The class is stateless: no fields besides the static logger, therefore a single instance shared by
  all Feign clients and all threads is safe.
- The module compiles at Java 8 source level (inherited from the microsphere parent chain), which is
  why the class uses no post-8 language features.
- Javadoc declares `@since 1.0.0` and `@see AcceptHeaderLocaleResolver`, a documentation link only —
  the class never instantiates a locale resolver.

### `apply(RequestTemplate)` Walkthrough

```java
RequestAttributes requestAttributes = getRequestAttributes();
if (!(requestAttributes instanceof ServletRequestAttributes)) {
    ... return;
}
ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
HttpServletRequest request = servletRequestAttributes.getRequest();
String acceptLanguage = request.getHeader(HEADER_NAME);
if (hasText(acceptLanguage)) {
    template.header(HEADER_NAME, acceptLanguage);
    ...
} else {
    ...
}
```

| Step | Code                                                | Failure mode and behavior                                                                  |
|------|-----------------------------------------------------|---------------------------------------------------------------------------------------------|
| 1    | `RequestContextHolder.getRequestAttributes()`       | Returns `null` outside a request thread (scheduled job, MQ listener, raw thread). Guarded by the `instanceof` check below. |
| 2    | `instanceof ServletRequestAttributes`               | Also filters out non-servlet `RequestAttributes` implementations and any non-servlet binding. TRACE log, then silent `return`. |
| 3    | `servletRequestAttributes.getRequest()`             | Never reached when step 2 fails, so no `NullPointerException` is possible here. Returns the `javax.servlet.http.HttpServletRequest` bound by Spring 5.3. |
| 4    | `request.getHeader(HEADER_NAME)`                    | `null` when the client sent no header; the raw string otherwise, including `q=` weights.     |
| 5    | `StringUtils.hasText(acceptLanguage)`               | Treats `null`, `""` and whitespace-only values the same way: nothing is written.             |
| 6    | `template.header(HEADER_NAME, acceptLanguage)`      | Copies the value verbatim; no parsing, no re-ordering, no supported-locale filtering.        |

Every log statement is wrapped in `logger.isTraceEnabled()`, so there is no formatting cost at
DEBUG and above. There is no exception thrown on any path: the interceptor can only ever be a
no-op, never a failure source for a Feign call.

### Edge Cases

- **Header absent on the inbound request.** Outbound Feign request has no `Accept-Language`. The
  receiving service resolves the locale through its own `AcceptHeaderLocaleResolver`, which then
  falls back to the default locale configured by `AcceptHeaderLocaleResolverBeanPostProcessor`
  (module `microsphere-i18n-spring`). There is deliberately **no** `LocaleContextHolder` fallback in
  this interceptor.
- **`Accept-Language: ` (empty or whitespace).** `hasText` rejects it; same outcome as absent.
- **Non-MVC request thread.** Any `RequestAttributes` that is not a `ServletRequestAttributes` is
  ignored. This includes async Feign usage where the calling thread never had request attributes, and
  inheritable thread-local setups only work if the attributes were bound with
  `setRequestAttributes(attributes, true)` before the child thread started.
- **Servlet 3.1/4.0 async dispatch.** The interceptor reads whatever request is bound at the moment
  `apply` runs; if the request has been completed and attributes reset, the header is skipped.
- **Multiple values.** `HttpServletRequest.getHeader` returns the first header line, so a client
  sending several `Accept-Language` lines forwards only the first one.
- **Wrong container namespace.** In a `jakarta.servlet` container (Spring Boot 3 / Servlet 5+),
  `ServletRequestAttributes.getRequest()` returns a `jakarta.servlet.http.HttpServletRequest`, and
  this class fails at link time with `NoClassDefFoundError: javax/servlet/http/HttpServletRequest`.
  That is a line-selection error, not a bug: use the 0.2.x artifacts there.

## 2. Header Resolution Flow

```mermaid
flowchart TD
    A["Feign builds a RequestTemplate"] --> B["AcceptLanguageHeaderRequestInterceptor.apply(template)"]
    B --> C["RequestContextHolder.getRequestAttributes()"]
    C --> D{"instanceof ServletRequestAttributes?"}
    D -- "no (null or non-servlet)" --> E["TRACE: non-Spring WebMVC scenario, ignoring"]
    E --> Z["template returned unchanged"]
    D -- "yes" --> F["getRequest() -> javax HttpServletRequest"]
    F --> G["request.getHeader('Accept-Language')"]
    G --> H{"StringUtils.hasText(value)?"}
    H -- "yes" --> I["template.header('Accept-Language', value)"]
    I --> J["TRACE: Feign has set HTTP request header"]
    H -- "no (null / empty / blank)" --> K["TRACE: requester did not pass the header"]
    J --> Y["outgoing Feign request"]
    K --> Z
    Z --> Y
```

## 3. Dependency Structure

From `microsphere-i18n-openfeign/pom.xml` on `release-1.x` and the parent-chain versions resolved by
the default `spring-cloud-2021` profile:

```mermaid
graph TD
    OF["microsphere-i18n-openfeign (jar)"]
    OF -->|"compile, required"| CORE["microsphere-i18n-core 0.1.x"]
    OF -.->|"optional"| FEIGN["io.github.openfeign:feign-core 11.10"]
    OF -.->|"optional"| MVC["org.springframework:spring-webmvc 5.3.27"]
    OF -.->|"optional"| SERVLET["javax.servlet:javax.servlet-api 4.0.1"]
    OF -.->|"optional, build-time"| AP["microsphere-annotation-processor (version from the 0.1.x parent chain)"]
    MVC --> WEB["org.springframework:spring-web 5.3.27 (optional transitive)"]
    MVC --> SPRINGCORE["org.springframework:spring-core 5.3.27 (transitive, non-optional)"]
    AP -.-> LM["microsphere-lang-model"]
    LM -.-> JCORE["microsphere-java-core - provides io.microsphere.logging"]
    CORE -.-> JCORE2["microsphere-java-core (declared optional by core)"]
    TESTS["test scope: junit-jupiter 5.14.4, spring-test 5.3.27, microsphere-spring-test / microsphere-logback (0.1.x line), logback-classic 1.2.12"] -.-> OF
```

Consequences for maintainers:

- Only `microsphere-i18n-core` is transitive to consumers. Everything else must be present in the
  application; that is why the module compiles against `feign-core`, `spring-webmvc` and the
  `javax.servlet` API without exporting them.
- The version numbers above are **not** written in this POM. They come from the parent chain:
  `microsphere-i18n-parent` imports `microsphere-spring-cloud-dependencies:0.1.26`, whose own parent
  imports `spring-boot-dependencies` at the version selected by the active generation profile
  (`spring-cloud-hoxton` → Boot 2.3.12.RELEASE, `spring-cloud-2020` → Boot 2.4.13,
  `spring-cloud-2021` → Boot 2.6.15 and active by default). Switching the build profile changes
  `spring-webmvc`, `spring-test` and `feign-core` versions automatically — under Hoxton the Feign
  line moves to 10.x.
- `io.microsphere.logging` reaches the compile classpath through the optional
  annotation-processor chain (`microsphere-annotation-processor` -> `microsphere-lang-model` ->
  `microsphere-java-core`, all version-managed by the `0.1.x` parent chain) and through
  `microsphere-spring-test` at test scope.
- `spring-core` is the only non-optional third-party artifact pulled in, as a transitive dependency
  of the optional `spring-webmvc`.

Build graph position: `microsphere-i18n-core` must be built first (`-pl microsphere-i18n-openfeign
-am`).

## 4. Design Notes

**Interceptor / mediator pattern.** The class is a Feign `RequestInterceptor`, i.e. a hook in
Feign's request-building pipeline rather than a decorator around the client. It mediates between two
objects that never see each other otherwise: `javax.servlet.http.HttpServletRequest` (inbound,
servlet world) and `RequestTemplate` (outbound, Feign world). The module therefore has zero coupling
to any bean container and can be instantiated by hand.

**Copy, do not negotiate.** The interceptor forwards a header, not a `Locale`. That decision keeps
the module independent of `ServiceMessageSource` and of any configuration property: the downstream
service — where the supported locales of that service are known — performs the negotiation. It also
means the interceptor is correct for applications that do not use the rest of microsphere-i18n at
all.

**Silent degradation.** Every non-applicable case returns without writing the header and without
throwing. A locale-propagation failure must never turn into a business-call failure.

**No auto-configuration.** Neither the `0.1.x` nor the `0.2.x` line registers a bean for this
interceptor. Auto-wiring a global header writer would change the behavior of every Feign client in an
application, which is a decision left to the user. (The README's wording about automatic registration
does not match the code — the module ships no auto-configuration metadata on either branch; nothing in
`microsphere-i18n-spring-boot` or `microsphere-i18n-spring-cloud` references this class, and those
modules are precisely the ones that on this branch expose their own auto-configurations through
`META-INF/spring.factories`.)

**Statelessness and thread-safety.** All state is read from thread-bound `RequestContextHolder`, so
one shared bean instance serves every client and thread.

## 5. Testing Strategy

`AcceptLanguageHeaderRequestInterceptorTest` is a plain JUnit 5 test — no Spring context is started,
which is exactly the point: the class must work with nothing but `RequestContextHolder`. The file is
identical to the one on the `release` branch.

```java
@SpringLoggingTest
class AcceptLanguageHeaderRequestInterceptorTest {
```

`@SpringLoggingTest` (from `microsphere-spring-test` `0.1.39`, a test-scope dependency) is the
project-wide convention for tests that need the framework's logging active; the same annotation is
used by the tests in `microsphere-i18n-spring` and `microsphere-i18n-spring-boot`.

Fixture (`@BeforeEach`) binds a `MockHttpServletRequest` to the current thread and prepares a fresh
`RequestTemplate`; `@AfterEach` calls `resetRequestAttributes()` so the thread-local never leaks into
the next test.

| Test method                             | Arrangement                                          | Assertion                                                        |
|-----------------------------------------|--------------------------------------------------------|-------------------------------------------------------------------|
| `testApply`                             | `request.addHeader(HEADER_NAME, "en")`                 | template headers were empty before, then `ofList("en")` for `Accept-Language` |
| `testApplyWithoutAcceptLanguageHeader`  | servlet attributes bound, no header added              | `requestTemplate.headers()` stays empty                            |
| `testApplyNoWebMvc`                     | `resetRequestAttributes()` before `apply`              | a **new** `RequestTemplate` still has no headers                   |

The three cases map one-to-one onto the branches of the flowchart above (hit, miss, guard). Running
them:

```bash
# Linux / macOS
./mvnw -pl microsphere-i18n-openfeign -am test

# Windows
mvnw.cmd -pl microsphere-i18n-openfeign -am test
```

CI on this branch runs the full reactor over a matrix of JDKs (8, 11, 17, 21, 25) crossed with the
generation profiles, for example
`mvn test --activate-profiles test,coverage,spring-cloud-2021 -Drevision=0.0.1-SNAPSHOT`, so this
module is exercised against every supported Spring Boot / Spring Cloud pair of the 2.x generation.

Gaps a contributor may want to close:

- assert the TRACE log content (the project uses `microsphere-logging-test` elsewhere);
- cover a whitespace-only header (`Accept-Language:   `);
- cover an inbound header with quality values to document verbatim forwarding;
- cover a non-servlet `RequestAttributes` implementation rather than `null`.

Whatever is added must stay Java 8 compatible (`ofList` from `io.microsphere.collection.Lists` rather
than `List.of`), because this branch compiles at source level 8.

## 6. Extending the Module

Guidelines for new work here:

1. Keep the class stateless and free of Spring bean lookups; the current design's only coupling is
   `RequestContextHolder`.
2. If a new interceptor needs the resolved locale rather than the raw header, obtain it from
   `LocaleContextHolder.getLocale()` or `ServiceMessageSource#getLocale()` — and document why the
   fallback chain differs from this class's, which has none.
3. New third-party dependencies stay `optional` so the module keeps exporting only
   `microsphere-i18n-core`.
4. Keep the `javax.servlet` namespace on this branch. Any new web-facing code must import
   `javax.servlet.*`, never `jakarta.servlet.*`; the Jakarta variant belongs on the `release`
   (0.2.x) branch.
5. Any auto-configuration belongs in `microsphere-i18n-spring-boot` — on this branch that module
   registers through `META-INF/spring.factories` under the
   `org.springframework.boot.autoconfigure.EnableAutoConfiguration` key (it also carries an
   `AutoConfiguration.imports` file for Boot 2.7+). Such a configuration should be conditional, e.g.
   `@ConditionalOnClass(RequestInterceptor.class)`, rather than unconditional.
6. Keep all logging at TRACE and behind `isTraceEnabled()`.
7. Behavioural fixes usually have to be mirrored on `release`, since the two branches intentionally
   carry the same sources apart from the namespace import.

## 7. Troubleshooting for Maintainers

| Symptom                                                    | Likely cause                                                                |
|-------------------------------------------------------------|------------------------------------------------------------------------------|
| Downstream always uses the default locale                    | Interceptor bean not declared, or Feign call made outside the request thread |
| `NoClassDefFoundError: feign/RequestInterceptor`             | `feign-core` missing — the dependency is optional and not exported           |
| `NoClassDefFoundError: javax/servlet/http/HttpServletRequest` | `javax.servlet-api` absent (non-servlet app), or the jar was deployed into a `jakarta.*` container — the 0.2.x line is needed there |
| `ClassNotFoundException` for the interceptor at startup     | Wrong version line for the container: `0.1.x` needs Boot 2.x, `0.2.x` needs Boot 3.x |
| Header present on inbound call but not on the Feign call     | `RequestContextHolder` was reset (async dispatch, `@Async`, manual clearing) |
| Version of `spring-webmvc` unexpected                        | Build ran with a different `spring-cloud-*` generation profile               |

## Differences from the 0.2.x line

`git diff origin/release origin/release-1.x -- microsphere-i18n-openfeign` shows exactly two files:

```diff
- import jakarta.servlet.http.HttpServletRequest;
+ import javax.servlet.http.HttpServletRequest;

  <dependency>
-     <groupId>jakarta.servlet</groupId>
-     <artifactId>jakarta.servlet-api</artifactId>
+     <groupId>javax.servlet</groupId>
+     <artifactId>javax.servlet-api</artifactId>
      <optional>true</optional>
  </dependency>
```

Summary of the consequences:

| Topic | 1.x (`release-1.x`) | 0.2.x (`release`) |
|-------|---------------------|-------------------|
| Version line | `0.1.x` — published `0.1.21`, working `0.1.22-SNAPSHOT` | `0.2.x` — published `0.2.21`, working `0.2.22-SNAPSHOT` |
| Java baseline | 8 (inherited; no `java.version` override in the 1.x root POM) | 17 |
| Servlet namespace | `javax.servlet` (Servlet 4.x, Tomcat 9) | `jakarta.servlet` (Servlet 6.0, Tomcat 10+) |
| Spring / Cloud generation | Boot 2.3–2.7, Cloud Hoxton.SR12–2021.0.x | Boot 3.x, Cloud 2022.0.x–2025.0.x |
| Interceptor logic, logging, constants, tests | unchanged | unchanged |
| Auto-configuration style in sibling modules | `META-INF/spring.factories` (`EnableAutoConfiguration`) plus `AutoConfiguration.imports` | `AutoConfiguration.imports` only |
| CI generation profiles | `spring-cloud-hoxton`, `spring-cloud-2020`, `spring-cloud-2021` | up to `spring-cloud-2025` |

The design itself — a passive, stateless header copier with no container registration — is
line-independent, which is why the module survived the namespace migration with a single import
change.

## Related Documentation

- User guide: [OpenFeign](../../user-guide/1.x/openfeign.md)
- Developer guide: [Core](core.md), [Parent POM](parent.md), [Dependencies BOM](dependencies.md)
- 0.2.x equivalent: [current OpenFeign developer guide](../current/openfeign.md)
