# Spring Module — Developer Guide

This document describes the internal design of `microsphere-i18n-spring`: how `@EnableI18n` wires the
container, how message-source implementations are discovered, how the Spring `MessageSource` façade is
adapted, which events drive runtime reloading, and how the module is tested.

> Audience: contributors to the module and advanced users who need to extend the bean graph. For
> day-to-day usage, see the Spring user guide.

## Architecture and bean wiring overview

The module contains no runtime message-resolution logic of its own; it is a **wiring layer** that
adapts the core abstraction (`io.microsphere.i18n.ServiceMessageSource`) to the Spring container. All
resolution, locale fallback and message-formatting behaviour is inherited from `microsphere-i18n-core`.

Four cooperating patterns are used:

| Pattern                       | Type in this module                                       | Role                                                                        |
|-------------------------------|------------------------------------------------------------|-------------------------------------------------------------------------------|
| Annotation-driven registration | `EnableI18n` + `I18nImportBeanDefinitionRegistrar`        | Turns one annotation into the complete bean graph.                          |
| Factory + service discovery   | `ServiceMessageSourceFactoryBean`                          | Builds one composite message source per `source`, from `spring.factories`.  |
| Adapter                       | `MessageSourceAdapter`                                     | Exposes `ServiceMessageSource` through the Spring `MessageSource` contract.  |
| Observer / lifecycle hooks    | `I18nApplicationListener`, `ResourceServiceMessageSourceChangedEvent`, the three `BeanPostProcessor`s | Publish the global source, reload on change, and integrate with Validation and MVC. |

The resulting runtime object graph is a two-level composite:

```
DelegatingServiceMessageSource (bean name: serviceMessageSource, primary)
└── ServiceMessageSourceFactoryBean("common")   (bean name: commonServiceMessageSource)
│   ├── DefaultServiceMessageSource("common")
│   └── PropertySourcesServiceMessageSource("common")
└── ServiceMessageSourceFactoryBean("test")     (bean name: testServiceMessageSource)
    ├── DefaultServiceMessageSource("test")
    └── PropertySourcesServiceMessageSource("test")

MessageSourceAdapter (bean name: messageSource) ──► serviceMessageSource
```

Both levels sort their members before use: `ServiceMessageSourceFactoryBean` sorts the discovered
implementations, and `DelegatingServiceMessageSource` sorts the `ServiceMessageSource` beans found in
the `BeanFactory` (excluding itself).

## `@EnableI18n` registration flow

`I18nImportBeanDefinitionRegistrar` is package-private and extends
`io.microsphere.spring.context.annotation.AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableI18n>`,
which supplies the `Environment` and `Logger` fields, resolves the annotation attributes with
placeholder support (`ResolvablePlaceholderAnnotationAttributes<EnableI18n>`), and short-circuits the
whole registration when `isEnabled(AnnotationMetadata)` returns `false`.

```mermaid
flowchart TD
    A["Configuration class annotated @EnableI18n"] --> B["@Import(I18nImportBeanDefinitionRegistrar.class)"]
    B --> C{"isEnabled: microsphere.i18n.enabled != false"}
    C -- "false" --> D["No bean definition is registered"]
    C -- "true" --> E["resolveSources: union of microsphere.i18n.sources and sources attribute, placeholders resolved"]
    E --> F["For each source: register ServiceMessageSourceFactoryBean bean definition, constructor argument = source"]
    F --> G["Register DelegatingServiceMessageSource as serviceMessageSource with setPrimary(true)"]
    G --> H{"exposeMessageSource == true"}
    H -- "yes" --> I["Register MessageSourceAdapter under bean name messageSource"]
    H -- "no" --> J["Skip adapter; Spring falls back to DelegatingMessageSource"]
    I --> K["Register I18nApplicationListener"]
    J --> K
    K --> L["Register ServiceMessageSourceBeanLifecyclePostProcessor"]
    L --> M{"isValidatorFactoryPresent(classLoader)"}
    M -- "yes" --> N["Register I18nLocalValidatorFactoryBeanPostProcessor"]
    M -- "no" --> O["Skip validation post-processor"]
    N --> P{"isAcceptHeaderLocaleResolverPresent(classLoader)"}
    O --> P
    P -- "yes" --> Q["Register AcceptHeaderLocaleResolverBeanPostProcessor"]
    P -- "no" --> R["Skip MVC locale resolver post-processor"]
```

### Bean definitions created by the registrar

| Bean name                                    | Bean class                                       | Constructor arguments | Role (`BeanDefinition.getRole()`) | Notes                                                            |
|----------------------------------------------|---------------------------------------------------|------------------------|-----------------------------------|-------------------------------------------------------------------|
| `{source}ServiceMessageSource` (for example `commonServiceMessageSource`, `testServiceMessageSource`) | `ServiceMessageSourceFactoryBean`                | the `source` string    | `ROLE_APPLICATION`                | One definition per resolved source; `FactoryBean`, so the exposed type is `ReloadableResourceServiceMessageSource`. |
| `serviceMessageSource`                        | `DelegatingServiceMessageSource`                  | none                   | `ROLE_APPLICATION`                | `setPrimary(true)`; the type used for `@Autowired ServiceMessageSource`. |
| `messageSource`                               | `MessageSourceAdapter`                            | none in the definition; Spring resolves the single `ServiceMessageSource` constructor argument by type (the primary bean) | `ROLE_APPLICATION` | Only when `exposeMessageSource = true`. Name equals `AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME`. |
| generated name                                | `I18nApplicationListener`                         | none                   | `ROLE_APPLICATION`                | Registered without an explicit name; resolve it **by type**.        |
| generated name                                | `ServiceMessageSourceBeanLifecyclePostProcessor`  | none                   | `ROLE_APPLICATION`                | `MergedBeanDefinitionPostProcessor`.                                |
| generated name                                | `I18nLocalValidatorFactoryBeanPostProcessor`       | `ObjectProvider<MessageSourceAdapter>` (resolved by type) | `ROLE_APPLICATION` | Conditional on `jakarta.validation.ValidatorFactory`.               |
| generated name                                | `AcceptHeaderLocaleResolverBeanPostProcessor`      | none                   | `ROLE_APPLICATION`                | Conditional on `AcceptHeaderLocaleResolver`.                        |

Names marked "generated" come from
`org.springframework.beans.factory.support.BeanDefinitionReaderUtils#generateBeanName`, because
`io.microsphere.spring.beans.factory.support.BeanRegistrar` passes `null` for the name. Since the
names are therefore not part of the module contract, every test and internal lookup resolves these
beans **by type** (`context.getBean(MessageSourceAdapter.class)`,
`isBeanPresent(context, I18nApplicationListener.class)`).

`BeanRegistrar` also refuses to overwrite an existing definition: it logs a warning and returns
`false`. Consequences: declaring your own `messageSource` bean keeps yours, and applying `@EnableI18n`
twice (for example directly and through Boot auto-configuration) registers each source only once.

## FactoryBean plus `spring.factories` discovery

`ServiceMessageSourceFactoryBean` is a `final` class that is simultaneously a `FactoryBean`, a
`CompositeServiceMessageSource`, an `Ordered` bean and an event listener. `getObject()` returns `this`,
and `getObjectType()` returns `ReloadableResourceServiceMessageSource.class`, so the container
publishes the composite itself rather than a separate product object.

Discovery is **Spring Factories based, not `java.util.ServiceLoader`**: the module reads
`META-INF/spring.factories` with
`org.springframework.core.io.support.SpringFactoriesLoader#loadFactoryNames` and therefore no
`META-INF/services` directory is needed. The resource shipped with the module is:

```properties
# microsphere-i18n-spring/src/main/resources/META-INF/spring.factories
io.microsphere.i18n.AbstractServiceMessageSource=\
io.microsphere.i18n.spring.PropertySourcesServiceMessageSource,\
io.microsphere.i18n.DefaultServiceMessageSource
```

The key is the abstract type `io.microsphere.i18n.AbstractServiceMessageSource`, so any library can add
its own implementation by contributing a `spring.factories` entry with the same key.

```mermaid
sequenceDiagram
    participant C as Spring container
    participant FB as ServiceMessageSourceFactoryBean
    participant SF as SpringFactoriesLoader
    participant MS as AbstractServiceMessageSource member
    C->>FB: setBeanClassLoader / setEnvironment / setApplicationContext
    C->>FB: afterPropertiesSet
    FB->>FB: init
    FB->>SF: loadFactoryNames(AbstractServiceMessageSource, classLoader)
    SF-->>FB: PropertySourcesServiceMessageSource, DefaultServiceMessageSource
    FB->>FB: resolveDefaultLocale and resolveSupportedLocales from Environment
    loop every factory class
        FB->>FB: resolveClassName, getConstructorIfAvailable(Class, String.class)
        FB->>MS: instantiateClass(constructor, source)
        FB->>MS: invokeAwareInterfaces(member, applicationContext)
        FB->>MS: setDefaultLocale / setSupportedLocales / init
    end
    FB->>FB: sort(members)
    FB-->>C: getObject returns this composite
```

Implementation requirements and behaviours worth noting:

- An implementation must expose a constructor that takes a single `String` (the source);
  `ClassUtils.getConstructorIfAvailable` returns `null` otherwise and `instantiateClass` fails.
- Members are ordered by their natural `Prioritized` ordering (`java.util.Collections.sort`), which
  determines lookup precedence inside the composite.
- `Aware` callbacks are pushed manually through
  `io.microsphere.spring.beans.BeanUtils#invokeAwareInterfaces(member, context)`, so a discovered
  implementation receives `BeanFactory`/`ApplicationContext`/`Environment` treatment even though it is
  not itself a container-managed bean.
- `resolveDefaultLocale` falls back to the composite's current default locale when
  `microsphere.i18n.default-locale` is absent; `resolveSupportedLocales` falls back to
  `getSupportedLocales()` (default locale plus English) when `microsphere.i18n.supported-locales` is
  absent. Both log at `TRACE`.
- `getLocale()` prefers `LocaleUtils.getLocaleFromLocaleContext()` and only then defers to the
  composite, which is what makes a single JVM-wide bean respond per request.

## Adapter pattern: `MessageSourceAdapter`

`MessageSourceAdapter` implements the Spring `MessageSource` contract on top of
`ServiceMessageSource` and is installed under the `messageSource` bean name, so every Spring
mechanism that consumes messages (validation, `MessageSourceResolvable`, view layers) goes through the
i18n stack without knowing about it.

```mermaid
flowchart TD
    A["getMessage(code, args, defaultMessage, locale)"] --> B["serviceMessageSource.getMessage(code, locale, args)"]
    B --> C{"result is null"}
    C -- "no" --> D["Return localized message"]
    C -- "yes" --> E["getDefaultMessage: iterate defaultMessageSources"]
    E --> F{"any fallback MessageSource returned non-null"}
    F -- "yes" --> G["Return fallback message"]
    F -- "no" --> H["Return defaultMessage argument"]
    H --> I["Note: getMessage(code, args, locale) passes null default, so it can return null instead of throwing NoSuchMessageException"]
```

Key implementation facts:

- The list `defaultMessageSources` is filled in `afterSingletonsInstantiated()` from
  `getSortedBeans(beanFactory, MessageSource.class)`, skipping `this`. Populating it that late is
  deliberate: other `MessageSource` beans (for example a `ResourceBundleMessageSource`) may not exist
  when the adapter itself is created.
- The constructor receives the primary `DelegatingServiceMessageSource` by type.
- `getMessage(MessageSourceResolvable, Locale)` loops over `resolvable.getCodes()` and returns the
  first non-`null` answer, so an empty code array (`() -> new String[0]`) yields `null`.
- Because the adapter is registered with the same name Spring reserves, a user-provided `messageSource`
  bean definition blocks it (see the registration refusal above).

## Observer and event flows

### Context lifecycle: publishing the global source

`I18nApplicationListener` uses `@EventListener` methods rather than the `ApplicationListener`
interface, and is registered unconditionally whenever i18n is enabled.

```mermaid
sequenceDiagram
    participant CTX as ApplicationContext
    participant LIS as I18nApplicationListener
    participant IU as I18nUtils (core static holder)
    participant DS as DelegatingServiceMessageSource
    CTX->>LIS: ContextRefreshedEvent
    LIS->>DS: I18nBeanUtils.getServiceMessageSource(context)
    LIS->>IU: setServiceMessageSource(serviceMessageSource)
    Note over IU: MessageUtils.getLocalizedMessage and I18nUtils.serviceMessageSource now work anywhere
    CTX->>LIS: ContextClosedEvent
    LIS->>IU: destroyServiceMessageSource()
    Note over IU: subsequent lookups return EmptyServiceMessageSource.INSTANCE
```

### Environment-driven reload: `PropertySourcesChangedEvent`

```mermaid
sequenceDiagram
    participant CFG as Microsphere config layer
    participant CTX as ApplicationContext
    participant LIS as I18nApplicationListener
    participant PSS as PropertySourcesServiceMessageSource.reloadAll
    participant SRC as PropertySourcesServiceMessageSource members
    CFG->>CTX: publish PropertySourcesChangedEvent(changedProperties)
    CTX->>LIS: onPropertySourcesChangedEvent
    LIS->>PSS: reloadAll(context, changedProperties.keySet)
    PSS->>PSS: getServiceMessageSource(context) then I18nUtils.findAllServiceMessageSources (flatten composites)
    PSS->>SRC: filter instances of PropertySourcesServiceMessageSource
    loop every such source
        SRC->>SRC: map each known resource to its property name, keep the changed ones
        SRC->>SRC: canReload(changedResources)
        SRC->>SRC: reload(changedResources) -> initializeResources -> loadMessages from Environment
    end
```

`reloadAll` deliberately translates *property names* back into *resource names* per source before
filtering, so an unrelated configuration change never triggers a reload (`canReload` requires the
property to exist in the `Environment`). `I18nApplicationListenerTest` asserts both halves of this
behaviour: an event without changed properties leaves the messages untouched, while an event carrying
`test.i18n_messages_zh_CN.properties` replaces `test.a` with the new value.

### Resource reload event: `ResourceServiceMessageSourceChangedEvent`

`ServiceMessageSourceFactoryBean` implements `ApplicationListener<ResourceServiceMessageSourceChangedEvent>`
and forwards `event.getChangedResources()` to `CompositeServiceMessageSource.reload`, which asks each
member `ReloadableResourceServiceMessageSource` whether it `canReload` before reloading it.

```mermaid
sequenceDiagram
    participant APP as Application code
    participant CTX as ApplicationContext
    participant FB as ServiceMessageSourceFactoryBean
    participant PS as PropertySourcesServiceMessageSource
    participant DF as DefaultServiceMessageSource
    APP->>CTX: publishEvent(ResourceServiceMessageSourceChangedEvent(context, "test.i18n_messages_en.properties"))
    CTX->>FB: onApplicationEvent
    FB->>FB: CompositeServiceMessageSource.reload(changedResources)
    FB->>PS: canReload -> environment.containsProperty(propertyName)
    PS->>PS: reload -> re-read the properties text from Environment
    FB->>DF: canReload -> classLoader.getResource(resource) != null
    Note over DF: for a property-name resource the classpath lookup fails, so the classpath source is left untouched
```

This is the flow exercised by `ServiceMessageSourceFactoryBeanTest#testGetMessage`: a
`MockPropertySource` added first to the `Environment` supplies
`test.i18n_messages_en.properties = test.a=1`, the event is published, and `getMessage("a")` returns
`"1"`.

The Spring Cloud module reuses the same entry point from a different trigger: its
`ReloadableResourceServiceMessageSourceListener` calls
`PropertySourcesServiceMessageSource.reloadAll(beanFactory, event.getKeys())` on
`EnvironmentChangeEvent`.

## Bean post-processors and their activation conditions

| Post-processor                                     | Kind                              | Guarded by                                                                                          | Effect when it matches                                          |
|----------------------------------------------------|-----------------------------------|--------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| `ServiceMessageSourceBeanLifecyclePostProcessor`    | `MergedBeanDefinitionPostProcessor` | `io.microsphere.util.ClassUtils.isAssignableFrom(ServiceMessageSource.class, beanType)`               | Defaults `initMethodName` to `init` and `destroyMethodName` to `destroy`. |
| `I18nLocalValidatorFactoryBeanPostProcessor`        | `GenericBeanPostProcessorAdapter<LocalValidatorFactoryBean>` | class-path: `ClassUtils.isPresent("jakarta.validation.ValidatorFactory", classLoader)`; per-bean: generic type match; per-call: `ObjectProvider.ifAvailable` | `setValidationMessageSource(messageSourceAdapter)` before initialization. |
| `AcceptHeaderLocaleResolverBeanPostProcessor`       | `GenericBeanPostProcessorAdapter<AcceptHeaderLocaleResolver>` | class-path: `ClassUtils.isPresent("org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver", classLoader)`; per-bean: generic type match | `setDefaultLocale` and `setSupportedLocales` after initialization. |

Lifecycle defaults are applied defensively:

- A bean that already implements `InitializingBean` or `DisposableBean` is left alone, otherwise the
  callback would run twice (once through the interface, once through the bean definition). This is why
  `DelegatingServiceMessageSource` bean definitions keep a `null` init/destroy method.
- An explicitly declared method name wins (`init`/`destroy` are only set when the current value is
  blank).
- The destroy value `AbstractBeanDefinition.INFER_METHOD` (`"(inferred)"`) is preserved, so Boot-style
  inferred shutdown methods are not replaced.

The class-path guards use `ClassLoaderUtils.getDefaultClassLoader()` at registration time, and both
guards are exposed as public static methods so the registrar can call them without loading the
optional types eagerly. That is what keeps the module usable without Spring MVC or Bean Validation: the
`I18nImportBeanDefinitionRegistrarTest#testOnBeanClassAbsent` case swaps the thread context class loader
for the platform loader and asserts that the two optional post-processors are gone while the core
`ServiceMessageSourceFactoryBean`, `MessageSourceAdapter`, `ServiceMessageSource` and
`I18nApplicationListener` beans remain.

## Class hierarchy

```mermaid
classDiagram
    class ServiceMessageSource {
        <<interface>>
        +init()
        +destroy()
        +getMessage(code, locale, args)
        +getLocale()
        +getSource()
    }
    class ResourceServiceMessageSource {
        <<interface>>
        +initializeResource(resource)
        +getInitializedResources()
        +getEncoding()
    }
    class ReloadableResourceServiceMessageSource {
        <<interface>>
        +canReload(changedResource)
        +reload(changedResources)
    }
    class AbstractServiceMessageSource {
        <<abstract>>
        #resolveMessageCode(code)
        #getInternalMessage(...)
        #getInternalLocale()
    }
    class AbstractResourceServiceMessageSource {
        <<abstract>>
        +getResource(locale)
        +getResources()
        #loadMessages(resource)
    }
    class PropertiesResourceServiceMessageSource {
        <<abstract>>
        +loadAllProperties(resource)
        #loadAllPropertiesResources(resource)
    }
    class DefaultServiceMessageSource {
        +getResource(resourceName)
    }
    class PropertySourcesServiceMessageSource {
        -Environment environment
        +getPropertyName(locale)
        +canReload(changedResource)
        +reloadAll(beanFactory, changedPropertyNames)$
    }
    class CompositeServiceMessageSource
    class ServiceMessageSourceFactoryBean {
        -String source
        -int order
        +getObject()
        +initServiceMessageSources()
    }
    class DelegatingServiceMessageSource {
        +afterPropertiesSet()
    }
    class MessageSourceAdapter {
        <<adapts>>
        +getMessage(code, args, defaultMessage, locale)
        +afterSingletonsInstantiated()
    }
    class I18nApplicationListener
    class ResourceServiceMessageSourceChangedEvent {
        <<ApplicationContextEvent>>
        +getChangedResources()
    }

    ServiceMessageSource <|-- ResourceServiceMessageSource
    ResourceServiceMessageSource <|-- ReloadableResourceServiceMessageSource
    ServiceMessageSource <|-- AbstractServiceMessageSource
    AbstractServiceMessageSource <|-- AbstractResourceServiceMessageSource
    ResourceServiceMessageSource <|.. AbstractResourceServiceMessageSource
    AbstractResourceServiceMessageSource <|-- PropertiesResourceServiceMessageSource
    PropertiesResourceServiceMessageSource <|-- DefaultServiceMessageSource
    PropertiesResourceServiceMessageSource <|-- PropertySourcesServiceMessageSource
    ReloadableResourceServiceMessageSource <|.. DefaultServiceMessageSource
    ReloadableResourceServiceMessageSource <|.. PropertySourcesServiceMessageSource
    ReloadableResourceServiceMessageSource <|.. CompositeServiceMessageSource
    CompositeServiceMessageSource <|-- ServiceMessageSourceFactoryBean
    CompositeServiceMessageSource <|-- DelegatingServiceMessageSource
    ReloadableResourceServiceMessageSource <|.. ServiceMessageSourceFactoryBean
    ReloadableResourceServiceMessageSource <|.. DelegatingServiceMessageSource
    ServiceMessageSource ..> MessageSourceAdapter : delegates to
    ResourceServiceMessageSourceChangedEvent ..> ServiceMessageSourceFactoryBean : consumed by
```

Spring lifecycle interfaces implemented by the Spring-facing classes:

| Class                                        | Spring contracts                                                                                                              |
|----------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `ServiceMessageSourceFactoryBean`             | `FactoryBean`, `InitializingBean`, `DisposableBean`, `EnvironmentAware`, `BeanClassLoaderAware`, `ApplicationContextAware`, `Ordered`, `ApplicationListener` |
| `DelegatingServiceMessageSource`              | `InitializingBean`, `DisposableBean`, `BeanFactoryAware`                                                                        |
| `PropertySourcesServiceMessageSource`         | `EnvironmentAware`                                                                                                              |
| `MessageSourceAdapter`                        | `MessageSource`, `SmartInitializingSingleton`, `BeanFactoryAware`                                                               |
| `AcceptHeaderLocaleResolverBeanPostProcessor` | `BeanPostProcessor` (through `GenericBeanPostProcessorAdapter`), `ApplicationContextAware`                                       |
| `I18nLocalValidatorFactoryBeanPostProcessor`  | `BeanPostProcessor` (through `GenericBeanPostProcessorAdapter`)                                                                  |
| `ServiceMessageSourceBeanLifecyclePostProcessor` | `MergedBeanDefinitionPostProcessor`                                                                                          |
| `I18nApplicationListener`                     | `@EventListener` methods only                                                                                                    |

## Module dependencies

```mermaid
graph LR
    core["microsphere-i18n-core"] --> spring["microsphere-i18n-spring"]
    spring --> boot["microsphere-i18n-spring-boot"]
    boot --> cloud["microsphere-i18n-spring-cloud"]
    cloud --> server["microsphere-i18n-spring-cloud-server"]
    core --> openfeign["microsphere-i18n-openfeign"]
    parent["microsphere-i18n-parent"] -. parent POM .-> spring
    bom["microsphere-i18n-dependencies (BOM)"] -. dependency management .-> spring
    ctx["microsphere-spring-context (optional)"] -. runtime requirement .-> spring
    webmvc["spring-webmvc (optional)"] -. AcceptHeaderLocaleResolver .-> spring
    validation["jakarta.validation-api (optional)"] -. ValidatorFactory .-> spring
```

`microsphere-i18n-spring` depends on `microsphere-i18n-core` with `${revision}` and is in turn a
required dependency of `microsphere-i18n-spring-boot`, whose `I18nAutoConfiguration` is annotated with
`@EnableI18n`. Nothing in this module depends on Spring Boot, which is why it can be used in a plain
`AnnotationConfigApplicationContext`.

## Testing strategy

The tests run against a plain Spring test context (`spring-test`, JUnit 5); there is no Boot
dependency in this module's test scope.

### Shared fixture: `io.microsphere.i18n.AbstractSpringTest`

Located in the test root package and reused by every message-resolution test:

- `TEST_SOURCE = "test"`, matching the resources under
  `src/test/resources/META-INF/i18n/test/`.
- `@BeforeAll` pins `Locale.setDefault(Locale.SIMPLIFIED_CHINESE)`, so the JVM default locale is the
  expected fallback everywhere.
- `@BeforeEach` calls `resetLocaleContext()`; `@AfterAll` calls `destroyServiceMessageSource()` and
  resets the context again, keeping the core static holder clean between test classes.
- `assertGetMessage(ServiceMessageSource)` centralizes the expectations against
  `META-INF/i18n/test/i18n_messages_zh_CN.properties` (`test.a = 测试-a`, `test.hello = 您好,{}`):
  `getMessage("a")` is `"测试-a"`, `getMessage("hello", "World")` is `"您好,World"`, and an unknown code
  returns `null`.

### Test configurations

| Class                                            | Purpose                                                                                                 |
|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `TestSourceEnableI18nConfiguration`               | `@EnableI18n(sources = TEST_SOURCE)`; the baseline context for most assertions (`testServiceMessageSource`). |
| `DisabledEnableI18nConfiguration`                 | `@EnableI18n` plus `@ResourcePropertySource("classpath:META-INF/config/disabled-enable-i18n.properties")`, whose single line sets `microsphere.i18n.enabled = false`. |
| `UnexposedMessageSourceEnableI18nConfiguration`   | `@EnableI18n(exposeMessageSource = false)`; proves the adapter is the only bean suppressed.                 |

### Test resources

- `META-INF/i18n/test/i18n_messages_{en,zh_CN}.properties` — the happy-path bundles, exercised both from
  the classpath and through the `Environment`.
- `META-INF/i18n/error/i18n_messages_zh_CN.properties` — a deliberately mixed bundle (`error.a`, `test.b`)
  kept in the test resources; no test in this module reads it, the invalid-prefix case is asserted in
  the core module (`DefaultServiceMessageSourceTest#testValidateMessageCode`).
- `messages_{en,zh_CN}.properties` (`test.value`) — the classic `ResourceBundleMessageSource` basename
  that acts as the `MessageSourceAdapter` fallback.
- `META-INF/config/disabled-enable-i18n.properties` — the kill switch.

### Per-class coverage

| Test                                                  | What it pins down                                                                                                              |
|--------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `I18nImportBeanDefinitionRegistrarTest`                | The full bean inventory for four scenarios: sources specified, disabled, adapter unexposed, and optional classes absent (via a swapped context class loader). |
| `EnableI18nTest`                                        | End-to-end resolution through the injected primary `ServiceMessageSource`, including `assertSame` against `I18nUtils.serviceMessageSource()` and the English fallback. |
| `ServiceMessageSourceFactoryBeanTest`                 | `@TestPropertySource` locale pinning, `LocaleContextHolder` precedence, `FRANCE` returning `null`, reload through `ResourceServiceMessageSourceChangedEvent`, `setOrder`, `resolveDefaultLocale` / `resolveSupportedLocales` fallbacks, `toString`. |
| `PropertySourcesServiceMessageSourceTest`             | Resource-to-property naming (`META-INF/i18n/test/...` becomes `test.i18n_messages_zh_CN.properties`), `canReload` variants, `findAll...` over empty/single/delegating bean graphs, and a hand-built `Config` with a `@Primary` `DelegatingServiceMessageSource`. |
| `MessageSourceAdapterTest`                             | Adapter with and without fallback `MessageSource` beans, `MessageSourceResolvable` handling including the empty-codes case, and `toString` coverage. |
| `I18nApplicationListenerTest`                          | Global publication, no-op reload on an empty `PropertySourcesChangedEvent`, effective reload when the mock property source reports an added property, and cleanup back to `EmptyServiceMessageSource.INSTANCE`. |
| `DelegatingServiceMessageSourceTest`                   | A delegating composite over `EmptyServiceMessageSource` returns `null` rather than failing.                                          |
| `ServiceMessageSourceBeanLifecyclePostProcessorTest`  | Every branch of the init/destroy defaults: unset, `INFER_METHOD`, user-declared `start`/`stop`, an `InitializingBean`/`DisposableBean` type (`DelegatingServiceMessageSource`) and a non-matching type (`Object`). |
| `I18nBeanUtilsTest`                                    | `messageSource` present versus `DelegatingMessageSource`, adapter detection, and `getServiceMessageSource` returning `null` when no definition exists. |
| `I18NLocalValidatorFactoryBeanPostProcessorTest`       | Behaviour with no validator bean, with no adapter bean, and the successful wiring that leaves `LocaleContextMessageInterpolator` as the interpolator. |
| `AcceptHeaderLocaleResolverBeanPostProcessorTest`      | The resolver's supported locales equal the primary source's supported locales.                                                       |

### Test utilities

- `SpringTestUtils.testInSpringContainer(...)` (from `microsphere-spring-test`) builds an anonymous
  context per lambda, optionally with a `ConfigurableEnvironment` argument, so each assertion gets an
  isolated bean graph.
- `@SpringLoggingTest` and `@LoggingLevelsTest` (from `microsphere-logback` / `microsphere-spring-test`)
  capture trace logs and temporarily raise levels to `ERROR` where the code path logs a lot, for
  instance the locale-resolution unit tests.
- `MockEnvironment`, `MockPropertySource` and `environment.getPropertySources().addFirst(...)` are used
  to simulate external configuration changes without Boot or a config server.

## Extension and troubleshooting notes

- **Adding a message-source implementation**: register the class under the
  `io.microsphere.i18n.AbstractServiceMessageSource` key in your own `META-INF/spring.factories`, and
  provide a `public MySource(String source)` constructor. It will be instantiated, Aware-injected,
  configured with the resolved locales, initialized and sorted for every declared `source`.
- **Adding a source without `@EnableI18n` attributes**: set `microsphere.i18n.sources`; both routes are
  merged, placeholder-resolved and deduplicated.
- **Nothing is registered**: check `microsphere.i18n.enabled` first; the registrar logs the decision at
  `TRACE` along with the property name and default value.
- **Messages resolve to `null`**: confirm the code prefix (`<source>.`), the supported-locale pruning
  (`microsphere.i18n.supported-locales` narrows which files are loaded), and the current
  `LocaleContextHolder` value.
- **Tracing**: `AbstractResourceServiceMessageSource`, `CompositeServiceMessageSource`,
  `ServiceMessageSourceFactoryBean`, `DelegatingServiceMessageSource`, `MessageSourceAdapter` and both
  optional post-processors log the resolved code, locale and pattern at `TRACE` — enabling trace for
  `io.microsphere.i18n` is the fastest way to diagnose a miss.
- **Property metadata**: the build generates
  `META-INF/microsphere/configuration-properties.json` from the `@ConfigurationProperty` annotations on
  `I18nConstants`, which is what IDEs read for completion of the four `microsphere.i18n.*` keys.
