# Microsphere I18n - User Guide

## Brief Overview

**microsphere-i18n** is a Java internationalization (i18n) framework that provides a modular, extensible system for managing translated messages across microservices. Instead of using Java's built-in `ResourceBundle`, it introduces a `ServiceMessageSource` abstraction that loads messages from `.properties` files, Spring Environment properties, or remote servers — and supports **runtime reloading** without restarting the application.

The project is organized into **6 modules**, each building on the previous:

| Module | Purpose |
|--------|---------|
| `microsphere-i18n-core` | Core interfaces and classpath-based message loading |
| `microsphere-i18n-spring` | Spring Framework integration (beans, annotations, adapters) |
| `microsphere-i18n-spring-boot` | Spring Boot auto-configuration and Actuator endpoint |
| `microsphere-i18n-spring-cloud` | Dynamic reloading via Spring Cloud config changes |
| `microsphere-i18n-spring-cloud-server` | Centralized i18n server exposing messages via REST |
| `microsphere-i18n-openfeign` | Propagates `Accept-Language` header across Feign calls |

---

## Module 1: `microsphere-i18n-core` (12 files)

This is the foundation. It defines the core abstractions and default implementations for loading and resolving internationalized messages.

### Key Concepts

- **Source**: A logical group of messages (e.g., `"common"`, `"user-service"`). Each source has its own set of `.properties` files.
- **Message Code**: A dot-prefixed key like `test.hello`. The prefix matches the source name.
- **Message Pattern**: A template string like `"Hello,{}"` that gets arguments substituted in.
- **Locale**: Java's `java.util.Locale` — represents a language/region (e.g., `en`, `zh_CN`).

### Class-by-Class Breakdown

#### 1. `ServiceMessageSource` (Interface)

**What it does:** The central contract for the entire framework. Think of it as "the thing that gives you translated messages."

**Key parts:**

- `init()` / `destroy()` — Lifecycle methods (start up and shut down)
- `getMessage(code, locale, args)` — The core method: "give me the message for this code in this language"
- `getMessage(code, args)` — Convenience overload that uses the current runtime locale
- `getLocale()` — Returns the current locale being used
- `getSupportedLocales()` — Returns which languages are available (defaults to system default + English)
- `getSource()` — Returns the source name (defaults to `"common"`)

**Example:**

```java
ServiceMessageSource source = new DefaultServiceMessageSource("test");
source.init();
String msg = source.getMessage("hello", Locale.ENGLISH, "World"); // "Hello,World"
source.destroy();
```

**When to use:** You interact with this interface whenever you need to retrieve a translated message anywhere in your application.

---

#### 2. `AbstractServiceMessageSource` (Abstract Class)

**What it does:** Provides the base implementation that all concrete message sources inherit from. Handles locale resolution, message code resolution, and hierarchical locale fallback.

**Key parts:**

- **Constructor** takes a `source` name and builds a `codePrefix` (e.g., `"test."`)
- `getMessage()` resolves the code, resolves the locale, and delegates to `getInternalMessage()`. If no match found with the full locale (`zh_CN`), it falls back to just the language (`zh`).
- `resolveMessageCode()` — Abstract method that subclasses implement to add the source prefix to codes
- `resolveHierarchicalLocales()` — Expands `zh_CN` into both `zh_CN` and `zh` for fallback
- `setSupportedLocales()` / `setDefaultLocale()` — Configuration methods

**Key concept — Locale Fallback:** If you request `zh_CN` (Simplified Chinese) and no messages exist for it, the system automatically tries `zh` (generic Chinese). This is the "hierarchical locale" resolution.

---

#### 3. `ResourceServiceMessageSource` (Interface)

**What it does:** Extends `ServiceMessageSource` to add the concept of "resources" — named files or data sources that contain messages.

**Key parts:**

- `initializeResource(resource)` — Load messages from a specific resource path
- `getInitializedResources()` — Returns all resources that have been loaded
- `getEncoding()` — Character encoding (defaults to UTF-8)

**When to use:** When you need to know which resource files were loaded, or manually load a new resource.

---

#### 4. `ReloadableResourceServiceMessageSource` (Interface)

**What it does:** Adds hot-reload capability. Resources can be reloaded at runtime without restarting the application.

**Key parts:**

- `canReload(changedResource)` — Checks if a changed resource can be reloaded
- `reload(changedResource)` — Reloads the messages from the changed resource (delegates to `initializeResource`)

**When to use:** In dynamic environments (like Spring Cloud) where configuration changes at runtime.

---

#### 5. `AbstractResourceServiceMessageSource` (Abstract Class)

**What it does:** The workhorse of the framework. Manages a `Map<String, Map<String, String>>` — a map from resource paths to their key-value message pairs.

**Key parts:**

- `initialize()` — Iterates over all supported locales, builds resource paths (like `META-INF/i18n/test/i18n_messages_en.properties`), loads and validates messages
- `getInternalMessage()` — Looks up a message by finding the right locale's map and resolving the pattern with arguments
- `initializeResources()` — Thread-safe method (synchronized) to add new resources at runtime
- `validateMessageCode()` — Ensures every message code starts with the correct source prefix (e.g., `test.`)
- `buildResourceName()` — Creates names like `i18n_messages_en.properties` from a locale

**Example of the data structure:**

```
{
  "META-INF/i18n/test/i18n_messages_zh_CN.properties": {
    "test.a": "测试-a",
    "test.hello": "您好,{}"
  },
  "META-INF/i18n/test/i18n_messages_en.properties": {
    "test.a": "test-a", 
    "test.hello": "Hello,{}"
  }
}
```

---

#### 6. `PropertiesResourceServiceMessageSource` (Abstract Class)

**What it does:** Specializes `AbstractResourceServiceMessageSource` to load messages from Java `.properties` files specifically.

**Key parts:**

- `loadMessages()` — Reads `.properties` files via `Reader` objects and converts them into `Map<String, String>`
- `loadAllProperties()` — Aggregates properties from multiple readers (supports classpath scanning where the same resource path exists in multiple JARs)

---

#### 7. `DefaultServiceMessageSource` (Concrete Class)

**What it does:** The ready-to-use implementation that loads messages from classpath resources under `META-INF/i18n/{source}/`.

**Key parts:**

- Resource path pattern: `META-INF/i18n/{source}/i18n_messages_{locale}.properties`
- Uses the `ClassLoader` to find resources (supports JARs and classpath scanning)
- Implements `ReloadableResourceServiceMessageSource` — can reload if the resource exists on the classpath

**Example:**

```java
// Place files at:
//   META-INF/i18n/test/i18n_messages_en.properties  → test.hello=Hello,{}
//   META-INF/i18n/test/i18n_messages_zh_CN.properties → test.hello=您好,{}

DefaultServiceMessageSource source = new DefaultServiceMessageSource("test");
source.init();
source.getMessage("hello", Locale.ENGLISH, "World");    // "Hello,World"
source.getMessage("hello", Locale.SIMPLIFIED_CHINESE, "World"); // "您好,World"
```

---

#### 8. `CompositeServiceMessageSource`

**What it does:** Combines multiple `ServiceMessageSource` instances into one, delegating to each in priority order until a message is found. Uses the **Composite design pattern**.

**Key parts:**

- `getMessage()` — Iterates through child sources; returns the first non-null result
- `setServiceMessageSources()` — Sorts children by priority (using the `Prioritized` interface)
- `reload()` — Propagates reload to all children that support reloading
- **Not thread-safe** for mutation (by design)

**When to use:** When you have messages split across multiple sources (e.g., `common` + `user-service`) and want a single entry point.

---

#### 9. `EmptyServiceMessageSource`

**What it does:** A Null Object pattern implementation — always returns `null` for any message code. Used as a safe default when no real source is configured.

```java
ServiceMessageSource empty = EmptyServiceMessageSource.INSTANCE;
empty.getMessage("anything"); // null
```

---

#### 10. `ServiceMessageException`

**What it does:** A `RuntimeException` that supports i18n message patterns. The `getMessage()` returns the raw pattern, while `getLocalizedMessage()` resolves it through the global `ServiceMessageSource`.

```java
throw new ServiceMessageException("{hello}", "World");
// getMessage()          → "{hello}"
// getLocalizedMessage() → "您好,World" (resolved based on current locale)
```

---

#### 11. `I18nUtils` (Utility Class)

**What it does:** Provides global static access to the `ServiceMessageSource` singleton. Also provides utility methods to recursively find all leaf sources within a composite hierarchy.

**Key parts:**

- `serviceMessageSource()` — Returns the global instance (or `EmptyServiceMessageSource` if not set)
- `setServiceMessageSource()` / `destroyServiceMessageSource()` — Lifecycle management
- `findAllServiceMessageSources()` — Recursively flattens a composite hierarchy into a list of leaf sources

---

#### 12. `MessageUtils` (Utility Class)

**What it does:** Resolves message patterns like `{hello}` into localized messages by extracting the code from `{}` braces and looking it up via the global `ServiceMessageSource`.

**Key parts:**

- `getLocalizedMessage("{hello}", Locale.ENGLISH, "World")` → extracts code `hello`, resolves via `ServiceMessageSource` → `"Hello,World"`
- `getLocalizedMessage("plain text")` → returns `"plain text"` unchanged (no `{}` pattern)
- If a code is not found, returns the part after the `.` separator (fallback behavior)

---

## Module 2: `microsphere-i18n-spring` (14 files)

This module integrates the core framework with the **Spring Framework**.

### Key Files

#### `@EnableI18n` (Annotation)

**What it does:** The entry point annotation to activate i18n in a Spring application. Place it on a `@Configuration` class.

```java
@EnableI18n(sources = {"common", "myapp"}, exposeMessageSource = true)
@Configuration
public class AppConfig { }
```

- `sources` — Which message sources to register (each becomes a Spring bean)
- `exposeMessageSource` — Whether to replace Spring's `MessageSource` with this framework's adapter

---

#### `I18nImportBeanDefinitionRegistrar`

**What it does:** The machinery behind `@EnableI18n`. When Spring processes the annotation, this registrar creates all the necessary beans: a `ServiceMessageSourceFactoryBean` for each source, a `DelegatingServiceMessageSource` as the primary bean, a `MessageSourceAdapter`, lifecycle processors, and application listeners.

---

#### `DelegatingServiceMessageSource`

**What it does:** The **primary** `ServiceMessageSource` bean in a Spring application. It auto-discovers all other `ServiceMessageSource` beans in the application context and delegates to them. It extends `CompositeServiceMessageSource` with Spring lifecycle integration (`InitializingBean`, `DisposableBean`).

---

#### `PropertySourcesServiceMessageSource`

**What it does:** A `ServiceMessageSource` that reads messages from **Spring's Environment properties** instead of classpath files. This enables storing messages in application.yml, config server, etc.

**Key concept:** The resource name (like `test.i18n_messages_en.properties`) becomes a **property name** in Spring's Environment. The value is the entire `.properties` file content as a string.

```yaml
# application.yml
test.i18n_messages_en.properties: |
  test.hello=Hello,{}
  test.a=test-a
```

---

#### `MessageSourceAdapter`

**What it does:** Bridges the framework's `ServiceMessageSource` to Spring's standard `MessageSource` interface. This means Spring features like `@Validated` bean validation messages, Thymeleaf templates, etc., automatically use your i18n messages.

- First tries `ServiceMessageSource`
- Falls back to any other `MessageSource` beans in the context

---

#### `I18nApplicationListener`

**What it does:** Listens for Spring application events to initialize the global `I18nUtils.serviceMessageSource` and to handle property changes that might require message reloading.

---

#### `ServiceMessageSourceFactoryBean`

**What it does:** A Spring `FactoryBean` that creates `ServiceMessageSource` instances. It uses Java's `ServiceLoader` mechanism (`META-INF/services`) to discover which implementation class to use (e.g., `DefaultServiceMessageSource` or `PropertySourcesServiceMessageSource`).

---

#### `ServiceMessageSourceBeanLifecyclePostProcessor`

**What it does:** A `BeanPostProcessor` that automatically sets `init-method` and `destroy-method` on `ServiceMessageSource` beans, so Spring manages their lifecycle correctly.

---

#### `I18nConstants`

**What it does:** Defines the configuration property names:

- `microsphere.i18n.enabled` — Enable/disable the feature
- `microsphere.i18n.sources` — Comma-separated list of message sources
- `microsphere.i18n.default-locale` — Default locale
- `microsphere.i18n.supported-locales` — Supported locales list

---

#### `LocaleUtils`

**What it does:** Retrieves the current `Locale` from Spring's `LocaleContextHolder` (which gets it from the current web request's `Accept-Language` header).

---

#### `ResourceServiceMessageSourceChangedEvent`

**What it does:** A Spring `ApplicationEvent` published when i18n resource files are modified at runtime. Other components can listen for this event to react to changes.

---

#### `I18nLocalValidatorFactoryBeanPostProcessor`

**What it does:** Configures Spring's `LocalValidatorFactoryBean` to use the i18n `MessageSourceAdapter` for Bean Validation messages (like `@NotNull`, `@Size`, etc.).

---

#### `AcceptHeaderLocaleResolverBeanPostProcessor`

**What it does:** Configures Spring's `AcceptHeaderLocaleResolver` with the supported locales from the i18n configuration.

---

#### `I18nBeanUtils`

**What it does:** Utility for retrieving i18n-related beans from the Spring `BeanFactory` by their well-known names.

---

## Module 3: `microsphere-i18n-spring-boot` (4 files)

### `I18nAutoConfiguration`

**What it does:** Spring Boot auto-configuration that automatically enables `@EnableI18n` when the library is on the classpath and `microsphere.i18n.enabled` is `true` (default).

### `ConditionalOnI18nEnabled`

**What it does:** A conditional annotation that activates beans only when i18n is enabled.

### `I18nEndpoint` (Actuator)

**What it does:** A Spring Boot Actuator endpoint (at `/actuator/i18n`) providing:

- **GET `/actuator/i18n`** — Lists all messages across all sources and locales
- **GET `/actuator/i18n/{code}`** — Looks up a specific message code across all locales
- **POST `/actuator/i18n`** — Dynamically adds/updates a message at runtime (writes to a `MapPropertySource` and reinitializes)

### `I18nEndpointAutoConfiguration`

**What it does:** Auto-configures the `I18nEndpoint` bean when Actuator is present.

---

## Module 4: `microsphere-i18n-spring-cloud` (2 files)

### `I18nCloudAutoConfiguration`

**What it does:** Auto-configures Spring Cloud integration, registering the `ReloadableResourceServiceMessageSourceListener`.

### `ReloadableResourceServiceMessageSourceListener`

**What it does:** Listens for Spring Cloud's `EnvironmentChangeEvent` (triggered when config properties change via Config Server, Consul, etc.) and automatically reloads affected `PropertySourcesServiceMessageSource` instances.

**When to use:** In microservice architectures where i18n messages are stored in a centralized config server and need to update without restart.

---

## Module 5: `microsphere-i18n-spring-cloud-server` (3 files)

### `@EnableI18nServer` (Annotation)

**What it does:** Activates the i18n server functionality — a centralized REST service that serves messages to other microservices.

### `I18nServerConfiguration`

**What it does:** Discovers all services (via Spring Cloud's `DiscoveryClient`) and builds `DefaultServiceMessageSource` instances for each service's messages.

### `I18nServerController`

**What it does:** A REST controller exposing a `GET /messages` endpoint that returns all i18n messages from all discovered services as JSON.

---

## Module 6: `microsphere-i18n-openfeign` (1 file)

### `AcceptLanguageHeaderRequestInterceptor`

**What it does:** A Feign `RequestInterceptor` that copies the `Accept-Language` HTTP header from the current incoming request to outgoing Feign client requests.

**Why it matters:** In microservice architectures, when Service A calls Service B via Feign, the user's language preference must be propagated. Without this interceptor, Service B wouldn't know which language to respond in.

```java
@Bean
public AcceptLanguageHeaderRequestInterceptor acceptLanguageInterceptor() {
    return new AcceptLanguageHeaderRequestInterceptor();
}
```

---

## Common Use Cases

1. **Simple application i18n**: Add `microsphere-i18n-spring-boot`, place `.properties` files under `META-INF/i18n/{source}/`, and annotate with `@EnableI18n`.

2. **Microservice i18n with centralized config**: Use `microsphere-i18n-spring-cloud` to store messages in Spring Cloud Config Server. Messages update automatically when config changes.

3. **Cross-service locale propagation**: Add `microsphere-i18n-openfeign` to ensure language preferences flow across Feign service calls.

4. **Runtime message management**: Use the Actuator endpoint (`/actuator/i18n`) to inspect or modify messages without redeployment.

5. **Centralized i18n server**: Use `microsphere-i18n-spring-cloud-server` to serve all microservices' messages from a single REST endpoint.

---

## Architecture Diagram (Class Hierarchy)

```
ServiceMessageSource (interface)
├── EmptyServiceMessageSource (null object)
├── AbstractServiceMessageSource (abstract)
│   └── AbstractResourceServiceMessageSource (abstract)
│       └── PropertiesResourceServiceMessageSource (abstract)
│           ├── DefaultServiceMessageSource (classpath-based)
│           └── PropertySourcesServiceMessageSource (Spring Environment-based)
├── CompositeServiceMessageSource (composite pattern)
│   └── DelegatingServiceMessageSource (Spring-aware composite)
├── ResourceServiceMessageSource (interface)
│   └── ReloadableResourceServiceMessageSource (interface)
```
