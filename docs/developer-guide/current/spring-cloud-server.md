# Spring Cloud i18n Server - Developer Guide

## Architecture and Key Classes

`microsphere-i18n-spring-cloud-server` builds on the reload-capable stack (`spring-cloud` -> `spring-boot` ->
`spring` -> `core`) and adds discovery-driven aggregation plus a REST surface. Three production classes carry
all the behavior.

### `@EnableI18nServer` and its `Marker`

`io.microsphere.i18n.spring.cloud.server.annotation.EnableI18nServer` is an annotation with
`@Target(TYPE)`, `@Retention(RUNTIME)`, `@Documented`, `@Inherited` and `@Import(EnableI18nServer.Marker.class)`.
`Marker` is an empty nested class. Registering `Marker` as a bean is the sole side effect of the annotation; it
exists purely to be detected by the auto-configuration's `@ConditionalOnBean(Marker.class)`. This is the standard
`@Enable...` marker pattern: the annotation opts the application in without the auto-configuration having to scan
for the annotation itself.

### `I18nServerAutoConfiguration`

`io.microsphere.i18n.spring.cloud.server.autoconfigure.I18nServerAutoConfiguration` is the core class. Its guard
stack is unusually tall, and each guard maps to a failure mode:

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnI18nAvailable
@ConditionalOnBlockingDiscoveryAvailable
@ConditionalOnActuatorEndpointPresent
@ConditionalOnWebApplication
@ConditionalOnAvailableEndpoint(endpoint = I18nEndpoint.class)
@ConditionalOnBean(Marker.class)
@Import(I18nServerController.class)
@AutoConfigureAfter(name = { COMMONS_CLIENT_AUTO_CONFIGURATION_CLASS_NAME }, value = {
        I18nAutoConfiguration.class,
        I18nEndpointAutoConfiguration.class,
        I18nCloudAutoConfiguration.class })
public class I18nServerAutoConfiguration { ... }
```

- `@ConditionalOnI18nAvailable` - i18n core/spring present and `microsphere.i18n.enabled` not `false`.
- `@ConditionalOnBlockingDiscoveryAvailable` (microsphere-spring-cloud-commons) - only run with a blocking
  `DiscoveryClient`.
- `@ConditionalOnActuatorEndpointPresent` + `@ConditionalOnAvailableEndpoint(endpoint = I18nEndpoint.class)` -
  the aggregation feeds `I18nEndpoint`, so the endpoint must be on the classpath and actually exposed.
- `@ConditionalOnWebApplication` - the REST controller needs a servlet/reactive web context.
- `@ConditionalOnBean(Marker.class)` - activated only when `@EnableI18nServer` was used.
- `@Import(I18nServerController.class)` - pulls the controller in when the config matches.
- `@AutoConfigureAfter(...)` - ordered after discovery, i18n boot, endpoint and cloud auto-configurations so that
  `DiscoveryClient` and `I18nEndpoint` beans already exist.

Two collaborators are autowired (`ConfigurableEnvironment`, `DiscoveryClient`) and two things happen:

1. A `@Lazy` `CompositeServiceMessageSource` bean named by the constant
   `LAZY_COMPOSITE_SERVICEMESSAGE_SOURCE_BEAN_NAME = "lazyCompositeServiceMessageSource"` is created empty.
   `@Lazy` prevents it from being built during context refresh; it is materialized only when the started event
   resolves it.
2. `@EventListener(ApplicationStartedEvent.class)` populates the composite. It reads
   `discoveryClient.getServices()`, maps each id through `buildServiceMessageSource(service)`, and calls
   `setServiceMessageSources(...)` on the composite. Each `buildServiceMessageSource` creates a
   `PropertySourcesServiceMessageSource(service)`, injects the shared `Environment`, sets the default locale to
   `Locale.getDefault()` and supported locales to `{default, ENGLISH}` (the code carries a `// FIXME Hardcode`
   marker here), then `init()`.

The per-service message source reads its content from `Environment` properties named
`<service>.i18n_messages_<locale>.properties`, so aggregation is really a naming convention applied over the
discovered service list.

### `I18nServerController`

`io.microsphere.i18n.spring.cloud.server.controller.I18nServerController` is a `@RestController` autowiring
`I18nEndpoint` and exposing exactly one method, `@GetMapping("/messages")`, which returns
`i18nEndpoint.invoke()` typed as `Map<String, Map<String, String>>`. The controller deliberately reuses the
endpoint's read model instead of duplicating traversal logic, giving the same payload under a friendly path.

## Design Patterns

- **Marker / enable-annotation** - `@EnableI18nServer` imports `Marker`; the auto-configuration gates on
  `@ConditionalOnBean(Marker.class)`. The annotation and the config stay decoupled.
- **Event-driven initialization** - aggregation runs from `@EventListener(ApplicationStartedEvent.class)` rather
  than `@PostConstruct`, guaranteeing the `DiscoveryClient` has its service registry by the time
  `getServices()` is called.
- **Lazy holder** - the composite is `@Lazy`, so it costs nothing until the started event forces its creation.
- **Composite** - `CompositeServiceMessageSource` (core) merges one `ServiceMessageSource` per discovered service;
  `setServiceMessageSources` also sorts them and clears the previous list.
- **Adapter / reuse** - `I18nServerController` adapts the Actuator `I18nEndpoint` read operation to a plain REST
  path.
- **Strategy (inherited)** - the per-service sources are `PropertySourcesServiceMessageSource`, whose
  `canReload`/`reload` behavior (from the `spring-cloud` layer) still lets a refresh update an individual
  service's messages via `EnvironmentChangeEvent`.

## Sequence: request, discovery, aggregated response

Discovery/aggregation happens once at startup; the `/messages` request reads the already-aggregated composite.

```mermaid
sequenceDiagram
    participant Start as Spring Boot (ApplicationStartedEvent)
    participant Cfg as I18nServerAutoConfiguration
    participant Disc as DiscoveryClient
    participant Composite as CompositeServiceMessageSource
    participant Client as HTTP Client
    participant Ctrl as I18nServerController
    participant Endpoint as I18nEndpoint

    Start->>Cfg: onApplicationStartedEvent(event)
    Cfg->>Composite: getBean("lazyCompositeServiceMessageSource")  [forces @Lazy init]
    Cfg->>Disc: getServices()
    Disc-->>Cfg: [service-a, service-b, ...]
    loop each service id
        Cfg->>Cfg: buildServiceMessageSource(id) -> PropertySourcesServiceMessageSource
        Cfg->>Cfg: setEnvironment / setSupportedLocales / init()
    end
    Cfg->>Composite: setServiceMessageSources(list)

    Client->>Ctrl: GET /messages
    Ctrl->>Endpoint: invoke()
    Endpoint->>Composite: collect localized resource messages
    Composite-->>Endpoint: Map(resource -> code -> message)
    Endpoint-->>Ctrl: Map<String, Map<String,String>>
    Ctrl-->>Client: JSON aggregated messages
```

## Flowchart: `I18nServerAutoConfiguration` conditions

```mermaid
flowchart TD
    A[Auto-configuration load] --> B{ConditionalOnI18nAvailable}
    B -- missing/disabled --> Z[Back off]
    B -- ok --> C{ConditionalOnBlockingDiscoveryAvailable}
    C -- no blocking client --> Z
    C -- ok --> D{ConditionalOnActuatorEndpointPresent}
    D -- actuator absent --> Z
    D -- ok --> E{ConditionalOnWebApplication}
    E -- non-web --> Z
    E -- web --> F{ConditionalOnAvailableEndpoint I18nEndpoint}
    F -- endpoint not exposed --> Z
    F -- exposed --> G{ConditionalOnBean Marker.class}
    G -- no EnableI18nServer --> Z
    G -- marker present --> H[Order after discovery/boot/endpoint/cloud]
    H --> I[Import I18nServerController]
    I --> J[Register lazy CompositeServiceMessageSource]
    J --> K[On ApplicationStartedEvent: discover + aggregate]
```

## Module Dependency

```mermaid
flowchart LR
    server[microsphere-i18n-spring-cloud-server]
    cloud[microsphere-i18n-spring-cloud]
    boot[microsphere-i18n-spring-boot]
    spring[microsphere-i18n-spring]
    core[microsphere-i18n-core]
    openfeign[microsphere-i18n-openfeign]

    server --> cloud
    cloud --> boot
    boot --> spring
    spring --> core
    openfeign --> core

    server -.optional.-> commons[org.springframework.cloud:spring-cloud-commons]
    commons -.provides.-> dc[DiscoveryClient]
```

The server depends on `microsphere-i18n-spring-cloud` (which transitively brings `spring-boot`, `spring`,
`core`); `microsphere-i18n-openfeign` is a sibling that depends only on `microsphere-i18n-core`.

## Testing Strategy

- `I18nServerAutoConfigurationTest` extends `WebAutoConfigurationTest<I18nServerAutoConfiguration>` and boots a
  web context with `Marker`, `I18nEndpointAutoConfiguration` and `CompositeDiscoveryClientAutoConfiguration`,
  exposing the `i18n` endpoint. It asserts the auto-config contributes `I18nServerController` and
  `CompositeServiceMessageSource`, and backs off under every guard:
  - disabled properties tested: `microsphere.i18n.enabled=false`, `spring.cloud.discovery.enabled=false`,
    `spring.cloud.discovery.blocking.enabled=false`, `management.endpoints.web.exposure.include=none`,
    `management.endpoint.i18n.enabled=false` - each corresponds to one of the `@Conditional*` guards.
  - missing classes tested: `ServiceMessageSource`, `EnableI18n`, `DiscoveryClient`, `Endpoint` - covering the
    i18n, discovery and actuator class conditions.
- `application.properties` (test resources) drives a real end-to-end discovery with no external registry: it
  registers a single service through Spring Cloud's simple discovery client
  (`spring.cloud.discovery.client.simple.instances.test-application[0]...`) so
  `discoveryClient.getServices()` returns `test-application`, letting the aggregation path run in a test.
  It also sets `spring.application.name=i18n-server`, exposes all endpoints and enables
  `management.endpoint.env.post.enabled`.
- `I18nServerBootstrap` is the runnable `@SpringBootApplication @EnableDiscoveryClient @EnableI18nServer` main
  (servlet) used for manual verification against a live registry and `GET /messages`.

Together these give condition-level coverage (`I18nServerAutoConfigurationTest`), an aggregation smoke path via
simple discovery in test config, and a manual bootstrap for real deployments.
