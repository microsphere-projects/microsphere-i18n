# Spring Cloud i18n Server (1.x)

> Applies to the `0.1.x` version line (the `release-1.x` / `1.x` branch), the line that targets Spring Boot 2.x
> and the javax.* namespace. For the Spring Boot 3 / `0.2.x` line see
> [../current/spring-cloud-server.md](../current/spring-cloud-server.md).

## Overview

`microsphere-i18n-spring-cloud-server` turns an application into a centralized i18n server. It discovers
every registered service through Spring Cloud's `DiscoveryClient`, builds a message source per service
from the Spring `Environment`, aggregates them into one `CompositeServiceMessageSource`, and exposes the
combined messages over REST so a client (or gateway) can read all services' translations from a single
place.

The module contains exactly three production classes:

- `@EnableI18nServer` - the activation annotation (imports an internal `Marker` class).
- `I18nServerAutoConfiguration` - the auto-configuration that performs discovery and aggregation.
- `I18nServerController` - the `@RestController` that exposes the aggregated messages.

It also relies on `I18nEndpoint` (the Spring Boot Actuator endpoint from `microsphere-i18n-spring-boot`)
as the data source for the REST response.

> Class-name note: the root-level developer guide historically referred to a class called
> `I18nServerConfiguration`. The real class in the source is `I18nServerAutoConfiguration`
> (`io.microsphere.i18n.spring.cloud.server.autoconfigure.I18nServerAutoConfiguration`). There is no
> `I18nServerConfiguration`.

Dependency chain:

```
spring-cloud-server -> spring-cloud -> spring-boot -> spring -> core
```

## Maven Coordinates

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-i18n-spring-cloud-server</artifactId>
    <version>0.1.21</version>
</dependency>
```

Managing the version through the `microsphere-i18n-dependencies` BOM works the same way, with `0.1.21`
as the BOM version.

`spring-cloud-commons` (for `DiscoveryClient`) and `spring-cloud-context`, together with
`microsphere-spring-cloud-commons`, `microsphere-spring-boot-core` and `microsphere-spring-boot-actuator`,
are declared optional in the POM. Bring the Spring Cloud artifacts through your own BOM.

## Version & Compatibility

| Component                | Value                                                              |
|--------------------------|--------------------------------------------------------------------|
| microsphere-i18n line    | `0.1.x` (latest release `0.1.21`, in-development `0.1.22-SNAPSHOT`)|
| groupId                  | `io.github.microsphere-projects`                                   |
| Java                     | No `java.version` override in this line; the CI matrix builds Java 8, 11, 17, 21 and 25 |
| Namespace                | `javax.*` (e.g. `javax.servlet-api`)                               |
| Spring Boot              | 2.3 - 2.7 generation (parent profiles pin 2.3.12.RELEASE / 2.4.13 / 2.6.15) |
| Spring Cloud             | Hoxton.SR12 - 2021.0.9                                             |
| Requires                 | a blocking `DiscoveryClient` and Spring Boot Actuator on classpath |
| Auto-configuration       | `META-INF/spring.factories` under the `EnableAutoConfiguration` key |

The auto-configuration is registered the Spring Boot 2.x way, through `META-INF/spring.factories`:

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.i18n.spring.cloud.server.autoconfigure.I18nServerAutoConfiguration
```

The module also ships the newer imports file
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` with the same single
entry, which Spring Boot 2.7+ reads in addition to `spring.factories`.

## Enabling the Server: `@EnableI18nServer`

`@EnableI18nServer` is the on-switch. It is `@Target(TYPE)`, `@Inherited` and `@Import`s the nested
`EnableI18nServer.Marker` class. Registering that marker bean is what `I18nServerAutoConfiguration`
requires through `@ConditionalOnBean(Marker.class)`, so the annotation both documents intent and gates
the auto-configuration.

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableI18nServer
public class I18nServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(I18nServerApplication.class, args);
    }
}
```

The test bootstrap `I18nServerBootstrap` uses precisely this combination
(`@SpringBootApplication @EnableDiscoveryClient @EnableI18nServer`) and starts as a `SERVLET` application.

If you prefer not to use the annotation, the same beans come from the auto-configuration as long as a
`Marker` bean, a blocking `DiscoveryClient`, Actuator and a web application are all present - but
`@EnableI18nServer` is the supported, declarative entry point.

## REST Endpoints

### `I18nServerController`

`I18nServerController` is a plain `@RestController` that autowires `I18nEndpoint` and exposes a single
method:

| Method | Path        | Returns                                | Delegates to |
|--------|-------------|----------------------------------------|--------------|
| GET    | `/messages` | `Map<String, Map<String, String>>`     | `i18nEndpoint.invoke()` |

```java
@GetMapping("/messages")
public Map<String, Map<String, String>> getMessages() {
    return i18nEndpoint.invoke();
}
```

The response is a map keyed by resource name, each holding the `code -> message` pairs for that resource,
across every discovered service.

### `I18nEndpoint` (Actuator)

Because the server module also depends on the Actuator `I18nEndpoint`, the standard operations are
available alongside `/messages` once the endpoint is exposed:

| Method   | Path                       | Operation (annotation) | Purpose |
|----------|----------------------------|------------------------|---------|
| GET      | `/actuator/i18n`           | `@ReadOperation invoke()` | All messages, same payload as `/messages` |
| GET      | `/actuator/i18n/{code}`    | `@ReadOperation getMessage(@Selector code)` | One code across all supported locales |
| GET      | `/actuator/i18n/{code}/{locale}` | `@ReadOperation getMessage(code, @Selector locale)` | One code in one locale |
| POST     | `/actuator/i18n`           | `@WriteOperation addMessage(...)` | Add/update a message in an `Environment`-backed source |

The controller path `/messages` is intentionally free of the `/actuator` prefix; the endpoint paths sit
under Actuator's base path (`management.endpoints.web.base-path`, `/actuator` by default in Spring Boot
2.x as well).

## Aggregating Messages Across Services

`I18nServerAutoConfiguration` performs aggregation in two phases.

Phase 1 - a lazy composite bean. On startup the context has an empty `CompositeServiceMessageSource`
registered as `lazyCompositeServiceMessageSource` (`@Lazy`):

```java
@Lazy
@Bean(name = LAZY_COMPOSITE_SERVICEMESSAGE_SOURCE_BEAN_NAME)
public CompositeServiceMessageSource lazyCompositeServiceMessageSource() {
    return new CompositeServiceMessageSource();
}
```

Phase 2 - populate on `ApplicationStartedEvent`. When Spring Boot fires `ApplicationStartedEvent`, the
listener grabs the lazy composite, asks the `DiscoveryClient` for the service list, builds one message
source per service and installs them into the composite:

```java
@EventListener(ApplicationStartedEvent.class)
public void onApplicationStartedEvent(ApplicationStartedEvent event) {
    ConfigurableApplicationContext context = event.getApplicationContext();
    CompositeServiceMessageSource composite =
            context.getBean(LAZY_COMPOSITE_SERVICEMESSAGE_SOURCE_BEAN_NAME, CompositeServiceMessageSource.class);
    List<String> services = discoveryClient.getServices();
    List<ServiceMessageSource> serviceMessageSources = services.stream()
            .map(this::buildServiceMessageSource)
            .collect(Collectors.toList());
    composite.setServiceMessageSources(serviceMessageSources);
}

private ServiceMessageSource buildServiceMessageSource(String service) {
    PropertySourcesServiceMessageSource serviceMessageSource = new PropertySourcesServiceMessageSource(service);
    serviceMessageSource.setEnvironment(environment);
    serviceMessageSource.setDefaultLocale(getDefault());
    serviceMessageSource.setSupportedLocales(ofList(getDefault(), ENGLISH));
    serviceMessageSource.init();
    return serviceMessageSource;
}
```

So a service registered as `order-service` yields a `PropertySourcesServiceMessageSource` whose messages
are read from `Environment` properties named `order-service.i18n_messages_en.properties`,
`order-service.i18n_messages_zh_CN.properties`, and so on. `CompositeServiceMessageSource` merges them,
and `I18nServerController` returns the merged view.

Example `application.properties` for a simple-discovery setup (mirrors the module's test config):

```properties
spring.application.name=i18n-server
management.endpoint.i18n.enabled=true
management.endpoints.web.exposure.include=*
management.endpoint.env.post.enabled=true

spring.cloud.discovery.client.simple.instances.test-application[0].instanceId=1
spring.cloud.discovery.client.simple.instances.test-application[0].serviceId=test-application
spring.cloud.discovery.client.simple.instances.test-application[0].host=127.0.0.1
spring.cloud.discovery.client.simple.instances.test-application[0].port=8080
```

With that single discovered service, `GET /messages` returns the resources contributed by
`test-application` (plus the server's own `common` source), each keyed by its resource name.

## How `DiscoveryClient` Is Used

Discovery is done through Spring Cloud's `org.springframework.cloud.client.discovery.DiscoveryClient`,
autowired into `I18nServerAutoConfiguration`. The only call is `discoveryClient.getServices()`, invoked
once per `ApplicationStartedEvent`. Each returned service id becomes a source name handed to
`PropertySourcesServiceMessageSource`.

The auto-configuration is guarded by `@ConditionalOnBlockingDiscoveryAvailable` (from
`microsphere-spring-cloud-commons`), so it activates only when a blocking discovery client is available,
and it is ordered after the discovery/actuator/i18n auto-configurations via `@AutoConfigureAfter`
(`COMMONS_CLIENT_AUTO_CONFIGURATION_CLASS_NAME`, `I18nAutoConfiguration`,
`I18nEndpointAutoConfiguration`, `I18nCloudAutoConfiguration`). This guarantees the `DiscoveryClient` and
the `I18nEndpoint` beans exist before the server builds its composite.

Note that the source's locale handling contains a `// FIXME Hardcode` marker: the default locale is the
JVM default and the supported locales are hardcoded to `{default, ENGLISH}` per service.

## Differences from the 0.2.x line

The three production classes and the test sources are byte-for-byte identical on the `0.2.x` and `1.x`
lines, so the enable-annotation, the condition stack, the discovery-driven aggregation and the REST
semantics above are unchanged. Only the packaging and the runtime target differ.

| Topic | `1.x` (`0.1.x`) | `0.2.x` (`current`) |
|-------|-----------------|---------------------|
| Version line | `0.1.x`, latest release `0.1.21` | `0.2.x`, latest release `0.2.21` |
| Spring Boot / Cloud | Boot 2.3 - 2.7, Cloud Hoxton.SR12 - 2021.0.9 | Boot 3.x, Cloud 2022.0.x - 2025.0.x |
| Namespace | `javax.*`; the POM depends on `javax.servlet:javax.servlet-api` | `jakarta.*`; the POM depends on `jakarta.servlet:jakarta.servlet-api` |
| Auto-configuration registration | `META-INF/spring.factories` with the `org.springframework.boot.autoconfigure.EnableAutoConfiguration` key (the imports file is shipped too, for Boot 2.7+) | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` only |
| Java baseline | no `java.version` override in the reactor; the parent chain and the CI matrix (Java 8/11/17/21/25) decide | the 0.2.x line targets Java 17 |
| Parent chain | root POM inherits `microsphere-spring-cloud-parent` `0.1.26`, whose profiles pin the Boot and Cloud BOMs (`spring-cloud-2021` is the default, i.e. Boot 2.6.15 / Cloud 2021.0.9) | the 0.2.x parent chain pins the Boot 3 / Cloud 2022+ generations |

Practical notes for this line:

- Discovery client selection is yours to make: any generation in Hoxton.SR12 - 2021.0.9 works as long as a
  blocking `DiscoveryClient` bean is present, otherwise
  `@ConditionalOnBlockingDiscoveryAvailable` makes the whole server back off.
- `spring.cloud.discovery.client.simple.instances....` (as in the test configuration above) is available in
  every supported generation, so you can run the server without a registry.
- `ApplicationStartedEvent`, `@ConditionalOnAvailableEndpoint` and the `/actuator/i18n` write operation all
  behave the same on Spring Boot 2.x; if your Boot generation predates
  `management.endpoint.env.post.enabled`, that property is ignored and `POST /actuator/env` stays enabled.

## Module dependencies

Depends on `spring-cloud` (→ `spring-boot` → `spring` → `core`).

```mermaid
flowchart TD
    CORE[core] --> SPRING[spring] --> BOOT[spring-boot] --> CLOUD[spring-cloud] --> SERVER[spring-cloud-server]
    CORE --> FEIGN[openfeign]
    style SERVER stroke-width:3px
```
