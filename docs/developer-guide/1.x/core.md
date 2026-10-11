# microsphere-i18n Core — Developer Guide (1.x line)

This guide describes the internal design of the `microsphere-i18n-core` module on the
**`release-1.x` branch**: its package structure, class hierarchy, the design patterns it applies, the
resolution/loading algorithms in the key classes, and its testing strategy. Every statement here was
verified against the sources under `microsphere-i18n-core/src/main/java` and the tests under
`microsphere-i18n-core/src/test` on that branch.

> Documentation for the `1.x` line (`0.1.x` artifacts, `javax.*` namespace, Spring Boot 2.x). The
> 0.2.x equivalent is [here](../current/core.md).

The core module is framework-neutral plain Java compiled at source level 8; it has no Spring
dependency and no upstream microsphere-i18n module. Downstream Spring and OpenFeign integrations
build on top of it. Its production sources are identical to the `release` (0.2.x) branch — only the
POM differs — so the design described below applies unchanged to both lines.

## Architecture and package structure

The module exposes two packages:

| Package | Contents | Responsibility |
|---------|----------|----------------|
| `io.microsphere.i18n` | `ServiceMessageSource`, `ResourceServiceMessageSource`, `ReloadableResourceServiceMessageSource`, `AbstractServiceMessageSource`, `AbstractResourceServiceMessageSource`, `PropertiesResourceServiceMessageSource`, `DefaultServiceMessageSource`, `CompositeServiceMessageSource`, `EmptyServiceMessageSource`, `ServiceMessageException` | Message-source contract, implementations, and the i18n exception. |
| `io.microsphere.i18n.util` | `I18nUtils`, `MessageUtils` | Global source access (holder) and `{code}` message-pattern resolution. |

The design separates a **contract layer** (the interfaces), an **abstract base layer** that owns the
shared algorithms (`AbstractServiceMessageSource`, `AbstractResourceServiceMessageSource`,
`PropertiesResourceServiceMessageSource`), and a **concrete leaf layer** that supplies the only missing
piece — where the bytes come from (`DefaultServiceMessageSource`) — plus two structural collaborators
(`CompositeServiceMessageSource`, `EmptyServiceMessageSource`).

Capability is layered through interface specialization, so consumers depend on the smallest interface they
need: `ServiceMessageSource` → `ResourceServiceMessageSource` → `ReloadableResourceServiceMessageSource`.

The data model of a resource-backed source is a two-level map held in
`AbstractResourceServiceMessageSource`:

```
Map<String /* resource path */, Map<String /* message code */, String /* message pattern */>>
```

For source `test` with English and Simplified-Chinese locales that resolves to:

```
{
  "META-INF/i18n/test/i18n_messages_zh_CN.properties": { "test.a": "测试-a", "test.hello": "您好,{}" },
  "META-INF/i18n/test/i18n_messages_en.properties":     { "test.a": "test-a", "test.hello": "Hello,{}" }
}
```

## Class hierarchy

```mermaid
classDiagram
    class Prioritized {
        <<interface>>
        +getPriority() int
        +compareTo(Prioritized) int
    }
    class ServiceMessageSource {
        <<interface>>
        +COMMON_SOURCE String
        +init() void
        +destroy() void
        +getMessage(code, locale, args) String
        +getMessage(code, args) String
        +getLocale() Locale
        +getDefaultLocale() Locale
        +getSupportedLocales() Set~Locale~
        +getSource() String
    }
    class ResourceServiceMessageSource {
        <<interface>>
        +initializeResource(resource) void
        +initializeResources(resources) void
        +getInitializedResources() Set~String~
        +getEncoding() Charset
    }
    class ReloadableResourceServiceMessageSource {
        <<interface>>
        +canReload(resource) boolean
        +canReload(resources) boolean
        +reload(resource) void
        +reload(resources) void
    }
    class AbstractServiceMessageSource {
        <<abstract>>
        #source String
        #codePrefix String
        -defaultLocale Locale
        -supportedLocales Set~Locale~
        #resolveMessageCode(code)* String
        #getInternalMessage(...)* String
    }
    class AbstractResourceServiceMessageSource {
        <<abstract>>
        -localizedResourceMessages Map
        #initialize() void
        +getResource(locale) String
        #loadMessages(resource)* Map
    }
    class PropertiesResourceServiceMessageSource {
        <<abstract>>
        +loadAllProperties(locale) Properties
        +loadAllProperties(resource) Properties
        #loadAllPropertiesResources(resource)* List~Reader~
    }
    class DefaultServiceMessageSource {
        +RESOURCE_LOCATION_PATTERN String
        -classLoader ClassLoader
        #getResource(resourceName) String
        #loadAllPropertiesResources(resource) List~Reader~
    }
    class CompositeServiceMessageSource {
        -serviceMessageSources List
        +getMessage(code, locale, args) String
        +setServiceMessageSources(list) void
    }
    class EmptyServiceMessageSource {
        +INSTANCE EmptyServiceMessageSource
        +getMessage(code, locale, args) String
    }
    class ServiceMessageException {
        <<RuntimeException>>
        -message String
        -args Object[]
        +getLocalizedMessage() String
    }

    Prioritized <|-- ServiceMessageSource
    ServiceMessageSource <|-- ResourceServiceMessageSource
    ResourceServiceMessageSource <|-- ReloadableResourceServiceMessageSource
    ServiceMessageSource <|-- AbstractServiceMessageSource
    AbstractServiceMessageSource <|-- AbstractResourceServiceMessageSource
    ResourceServiceMessageSource <|.. AbstractResourceServiceMessageSource
    AbstractResourceServiceMessageSource <|-- PropertiesResourceServiceMessageSource
    PropertiesResourceServiceMessageSource <|-- DefaultServiceMessageSource
    ReloadableResourceServiceMessageSource <|.. DefaultServiceMessageSource
    ReloadableResourceServiceMessageSource <|.. CompositeServiceMessageSource
    ServiceMessageSource <|.. EmptyServiceMessageSource
    ServiceMessageException ..> MessageUtils : resolves via
    MessageUtils ..> I18nUtils : uses global source
    I18nUtils ..> ServiceMessageSource : holds
```

`ServiceMessageSource` extends `io.microsphere.lang.Prioritized`, which extends `Comparable<Prioritized>`.
This is what lets `CompositeServiceMessageSource.setServiceMessageSources(...)` sort children with
`Collections.sort` — the smallest priority value sorts first and therefore has the highest precedence
during `getMessage` iteration.

## Design patterns

The patterns below are located to the exact class and, where useful, the exact method.

### Template Method — `AbstractServiceMessageSource` and `AbstractResourceServiceMessageSource`

`AbstractServiceMessageSource.getMessage(String, Locale, Object...)` is declared `final` and defines the
skeleton of resolution: resolve code, resolve locale, call the abstract `getInternalMessage(...)`, then
run the language-only fallback. Subclasses never override the algorithm; they fill in the hooks
`resolveMessageCode(String)` and `getInternalMessage(...)` (and `getInternalLocale()` to influence
`getLocale()`).

`AbstractResourceServiceMessageSource` applies the same pattern for initialization: `init()` (final-ish
public entry) calls `initialize()`, which iterates `getResources()` and calls the private per-resource
routine that ultimately delegates to the abstract `loadMessages(String)` supplied by subclasses.
`PropertiesResourceServiceMessageSource` narrows the template further: `loadMessages(...)` is `final` and
calls the abstract `loadAllPropertiesResources(String)`, so a concrete leaf like
`DefaultServiceMessageSource` only decides *where* readers come from (a `ClassLoader`).

### Composite — `CompositeServiceMessageSource`

`CompositeServiceMessageSource implements ReloadableResourceServiceMessageSource` and holds a
`List<? extends ServiceMessageSource>`. `getMessage` iterates children and returns the first non-null
result; `init`/`destroy` fan out to every child; `getSupportedLocales` and `getInitializedResources`
aggregate (union) child results; `reload`/`initializeResources` use the private
`iterate(Class<T>, Consumer<T>)` helper to filter children by capability type and forward the call. The
composite itself answers `getSource() = "common"`, `canReload(...) = true`, and `getEncoding() = UTF-8`.
Its Javadoc states it is **not thread-safe** for mutation.

### Null Object — `EmptyServiceMessageSource`

`EmptyServiceMessageSource` implements the contract with a shared `INSTANCE`; `init`/`destroy` are no-ops
and `getMessage(...)` always returns `null`, while `getSource()` reports `"Empty"`. It is the safe fallback
returned by `I18nUtils.serviceMessageSource()` when nothing has been configured, and a harmless member to
include in a composite (consulting it always yields a miss).

### Global-singleton holder — `I18nUtils`

`I18nUtils` keeps the JVM-wide active source in a `private static volatile ServiceMessageSource` field.
`serviceMessageSource()`, `setServiceMessageSource(...)` and `destroyServiceMessageSource()` form the
accessor trio; `serviceMessageSource()` substitutes `EmptyServiceMessageSource.INSTANCE` (with a WARN log)
when the field is null. The class is an abstract utility with a private constructor.

### Copy-on-Write — `AbstractResourceServiceMessageSource.initializeResources(Iterable)`

Reloads and incremental initialization mutate the message map through a copy-on-write protocol guarded by
`synchronized (this)` — see *Thread-safety of `initializeResources`* below.

### Progressive role interfaces (Interface Segregation)

`ResourceServiceMessageSource` and `ReloadableResourceServiceMessageSource` extend the base contract with
narrow, opt-in capabilities so that `EmptyServiceMessageSource` (base only), `CompositeServiceMessageSource`
(resource + reload without `AbstractServiceMessageSource`), and the classpath chain can each implement only
what they support.

### Facade utilities — `MessageUtils`

`MessageUtils` is a static facade that hides the `{code}` parsing plus the global-source lookup behind
`getLocalizedMessage(...)`, with a documented not-found fallback (strip up to the first `.`).

### Composite tree flattening — `I18nUtils.doFindAllServiceMessageSources`

`initServiceMessageSources(...)` recursively expands nested composites into their leaf sources; a
non-composite is collected as a leaf of itself.

## Key class analysis

### `AbstractServiceMessageSource`

Fields:

```java
protected final Logger logger;                       // per-subclass logger via getLogger(getClass())
protected final String source;                       // immutable source name
protected final String codePrefix;                   // source + MessageUtils.SOURCE_SEPARATOR ("." )
private   Set<Locale> supportedLocales;              // lazily the hierarchical expansion
private   Locale defaultLocale;                      // configured default locale (nullable)
```

The constructor asserts `source != null` (via `io.microsphere.util.Assert.assertNotNull`) and derives
`codePrefix`. The `getMessage` overloads are `final`, funneling all lookups through the same algorithm and
forcing subclasses to implement `getInternalMessage`. `getLocale()` is `final` and is the seam that lets
subclasses influence the runtime locale through the protected `getInternalLocale()` hook (which returns
`null` by default, meaning "use the default locale").

Resolution algorithm (`getMessage(String code, Locale locale, Object... args)`):

```java
String message = null;
if (code != null) {
    String resolvedCode = resolveMessageCode(code);          // add source prefix if missing
    Locale resolvedLocale = resolveLocale(locale);           // normalize language + region
    message = getInternalMessage(code, resolvedCode, locale, resolvedLocale, args);   // exact locale
    if (message == null) {
        resolvedLocale = new Locale(locale.getLanguage());   // language-only fallback
        if (!resolvedLocale.equals(locale)) {
            message = getInternalMessage(code, resolvedCode, locale, resolvedLocale, args);
        }
    }
}
return message;
```

Because the fallback dereferences `locale.getLanguage()`, the `locale` argument must be non-null when
`code` is non-null.

`resolveLocale(Locale)` returns the locale unchanged when it has no country, otherwise it rebuilds
`new Locale(language, region)` — a normalization step. `resolveMessage(String, Object...)` delegates to
`io.microsphere.text.FormatUtils.format`, the `{}`-placeholder formatter.

Maintenance note for the 1.x line: the `new Locale(String)` constructor used here and in `resolveLocale`
has been deprecated since JDK 19 in favour of `Locale.of(...)`. It stays because the module compiles at
source level 8; under the Java 21/25 CI legs the compiler emits a deprecation warning only.

### `AbstractResourceServiceMessageSource`

Field: `private volatile Map<String,Map<String,String>> localizedResourceMessages` initialized to
`Collections.emptyMap()`. `volatile` + whole-map replacement is what makes concurrent readers see a
consistent snapshot.

Constants: `DEFAULT_RESOURCE_NAME_PREFIX = "i18n_messages_"`, `DEFAULT_RESOURCE_NAME_SUFFIX = ".properties"`.

Locale-keyed lookup: `getMessages(Locale)` computes `getResource(locale)` = `buildResourceName(locale)`
= `"i18n_messages_" + locale.toString() + ".properties"`, then `localizedResourceMessages.get(resource)`.
`getResource(Locale)` is public and prepends the location pattern in the concrete subclass.

Message code prefix validation:

```java
private void validateMessageCodePrefix(String code, String resourceName) {
    if (!code.startsWith(codePrefix)) {
        throw new IllegalStateException(format(
            "Source '{}' Message Resource[name : '{}'] code '{}' must start with '{}'",
            source, resourceName, code, codePrefix));
    }
}
```

`validateMessages(...)` runs this for every entry immediately after `loadMessages` returns, during
`initializeResource`. This is why a properties file that declares a foreign-prefixed key (for example
`test.b` inside the `error` source, whose prefix is `error.`) aborts initialization with an
`IllegalStateException` (asserted by `DefaultServiceMessageSourceTest.testValidateMessageCode`).

Note the two exception types on purpose: `IllegalArgumentException` from `Assert.assertNotNull` when
`loadMessages` returns null (asserted by `AbstractResourceServiceMessageSourceTest.testInitialize`) versus
`IllegalStateException` from the prefix check.

`resolveMessageCode(String)` here implements the abstract hook:

```java
if (code.startsWith(codePrefix)) return code;   // already fully-qualified
return codePrefix + code;                        // "test." + "a" -> "test.a"
```

`destroy()` calls `clearAllMessages()` which clears the map and then nulls the field.

### Locale hierarchical fallback — `resolveHierarchicalLocales`

Two overloads cooperate:

```java
// Per-locale: zh_CN -> {zh_CN, zh}; a language-only locale stays as {locale}
protected Set<Locale> resolveHierarchicalLocales(Locale locale) {
    Locale resolvedLocale = resolveLocale(locale);
    String country = resolvedLocale.getCountry();
    if (isBlank(country)) {
        return ofSet(locale);
    }
    return ofSet(resolvedLocale, new Locale(resolvedLocale.getLanguage()));
}

// Collection: expands each supported locale, preserving order, de-duplicated
protected Set<Locale> resolveHierarchicalLocales(Collection<Locale> supportedLocales) {
    assertSupportedLocales(supportedLocales);                 // non-empty, no null elements
    Set<Locale> hierarchicalLocales = newFixedLinkedHashSet(supportedLocales.size() * 2);
    for (Locale supportedLocale : supportedLocales) {
        addLocale(hierarchicalLocales, supportedLocale);
        for (Locale locale : resolveHierarchicalLocales(supportedLocale)) {
            addLocale(hierarchicalLocales, locale);
        }
    }
    return hierarchicalLocales;
}
```

`setSupportedLocales(...)` stores the expansion of the input, so `getSupportedLocales()` returns the
ordered, de-duplicated hierarchical set. `supports(Locale)` checks membership via the per-locale expansion.

The `ofSet(...)` and `newFixedLinkedHashSet(...)` helpers come from `microsphere-java-core`
(`io.microsphere.collection.Sets`, `io.microsphere.util.*`) rather than from the JDK, which is precisely
what keeps the module compilable at Java 8 source level (there is no `Set.of` before Java 9).

### Thread-safety of `initializeResources`

```java
@Override
public void initializeResources(Iterable<String> resources) {
    synchronized (this) {                                  // guarded mutation
        Map<String, Map<String, String>> copy = new HashMap<>(this.localizedResourceMessages);
        initializeResources(resources, copy);              // load into the private copy
        this.localizedResourceMessages = copy;             // atomic field exchange (volatile write)
    }
}
```

Readers never observe a half-updated map: the incoming resources are loaded into a private copy and only
then is the `volatile` field replaced in one assignment. Because the field is `volatile`, the publish is
visible across threads and readers that grabbed the previous reference keep a consistent snapshot. `init()`
(and the internal `initialize()`) build a fresh map and publish it the same way. Note `initialize()` does
not take the monitor, but it fully constructs the local map before the single volatile write, so the same
"no partial publication" property holds.

The composite's `setServiceMessageSources` is *not* synchronized (matching the class's documented
"not thread-safe" contract), unlike the resource class's copy-on-write path.

### `PropertiesResourceServiceMessageSource` and `DefaultServiceMessageSource`

`loadMessages` is final and turns a `Properties` object into an unmodifiable `Map<String,String>`, wrapping
any `IOException` into a `RuntimeException`. `loadAllProperties(String)` opens every classpath resource at
the same path (multi-JAR scanning) and merges them; it returns `null` only when no reader is available,
which `loadMessages` converts into an empty map.

`DefaultServiceMessageSource` supplies the two remaining abstract hooks: `getResource(resourceName)`
formats `RESOURCE_LOCATION_PATTERN` (the literal value `META-INF/i18n/{}/{}`) with `getSource()`, and
`loadAllPropertiesResources(resource)` enumerates `classLoader.getResources(resource)` and wraps each URL
in an `InputStreamReader` using `getEncoding()` (UTF-8). `canReload(resource)` is simply
`classLoader.getResource(resource) != null` — the classpath leaf can reload only files that still resolve.

## Flowchart — `getMessage` resolution

```mermaid
flowchart TD
    A[getMessage code, locale, args] --> B{code != null?}
    B -->|no| Z[return null]
    B -->|yes| C[resolvedCode = resolveMessageCode code]
    C --> D[resolvedLocale = resolveLocale locale]
    D --> E[getInternalMessage exact locale]
    E --> F{message found?}
    F -->|yes| Y[return message]
    F -->|no| G[resolvedLocale2 = new Locale locale.getLanguage]
    G --> H{resolvedLocale2 equals locale?}
    H -->|yes| Z
    H -->|no| I[getInternalMessage language-only locale]
    I --> F

    subgraph GIM [getInternalMessage in AbstractResourceServiceMessageSource]
        E1[messages = getMessages resolvedLocale] --> E2{messages != null?}
        E2 -->|no| EN[return null]
        E2 -->|yes| E3[pattern = messages.get resolvedCode]
        E3 --> E4{pattern != null?}
        E4 -->|no| EN
        E4 -->|yes| E5[message = resolveMessage pattern args]
        E5 --> E6[logMessage + return message]
    end
```

## Flowchart — `init()` resource loading

```mermaid
flowchart TD
    A[init] --> B[assertNotNull source]
    B --> C[initialize]
    C --> D[resources = getResources for getSupportedLocales]
    D --> E[new HashMap localizedResourceMessages]
    E --> F{for each resource}
    F -->|next| G[loadMessages resource]
    G --> H{messages != null?}
    H -->|no| X[throw IllegalArgumentException]
    H -->|yes| I[validateMessages: codePrefix check]
    I --> J{all codes prefixed?}
    J -->|no| Y[throw IllegalStateException]
    J -->|yes| K[put resource -> messages into map]
    K --> F
    F -->|done| L[volatile exchange localizedResourceMessages]
    L --> M[log TRACE initialization complete]
```

## Sequence diagram — composite delegation

```mermaid
sequenceDiagram
    participant Caller
    participant Composite as CompositeServiceMessageSource
    participant Empty as EmptyServiceMessageSource
    participant Real as DefaultServiceMessageSource
    participant Reload as TestReloadableResourceServiceMessageSource

    Caller->>Composite: getMessage("hello", locale, args)
    Composite->>Empty: getMessage (priority 1)
    Empty-->>Composite: null
    Composite->>Real: getMessage (priority 2)
    Real-->>Composite: "Hello,World"
    Note over Composite: first non-null short-circuits the loop
    Composite-->>Caller: "Hello,World"

    Caller->>Composite: reload(changedResources)
    Composite->>Composite: iterate children of type ReloadableResourceServiceMessageSource
    Composite->>Real: canReload? reload?
    Real->>Real: initializeResources -> synchronized copy-on-write
    Composite->>Reload: canReload? reload?
    Reload-->>Composite: reloaded
```

## Module dependency

`microsphere-i18n-core` is the leaf of the i18n dependency chain: it depends on no other
microsphere-i18n module. Its `io.microsphere.*` and `io.microsphere.logging.*` references are satisfied
by `microsphere-java-core` (declared `optional`, its version resolved from the `0.1.x` parent chain,
so it is not a number written in this module's POM).
Within the project, `microsphere-i18n-spring` and `microsphere-i18n-openfeign` are the direct downstream
consumers of core.

```mermaid
graph TD
    subgraph i18n["microsphere-i18n project (1.x line)"]
        CORE[microsphere-i18n-core]
        SPRING[microsphere-i18n-spring]
        BOOT[microsphere-i18n-spring-boot]
        CLOUD[microsphere-i18n-spring-cloud]
        SERVER[microsphere-i18n-spring-cloud-server]
        FEIGN[microsphere-i18n-openfeign]
        DEPS[microsphere-i18n-dependencies BOM]
    end

    JAVA[microsphere-java-core optional - version from the 0.1.x parent chain]
    SPRINGFW[Spring Framework 5.3.x]

    CORE -->|optional| JAVA
    SPRING --> CORE
    SPRING --> SPRINGFW
    BOOT --> SPRING
    CLOUD --> BOOT
    SERVER --> CLOUD
    FEIGN --> CORE

    DEPS -.manages versions.-> CORE
```

Full chain summary: `spring → core`, `spring-boot → spring`, `spring-cloud → spring-boot`,
`spring-cloud-server → spring-cloud`, `openfeign → core`.

## Testing strategy

Tests live under `microsphere-i18n-core/src/test/java` (JUnit 5 — `junit-jupiter` `5.14.4` in the 1.x
parent chain, versus the 6.x line used by 0.2.x) with fixtures in
`src/test/resources/META-INF/i18n/`. The test sources are the same files as on the `release` branch. They
are organised to mirror the class hierarchy so shared contract behaviour is inherited and re-verified per
implementation.

### Shared fixtures and base classes

- `AbstractI18nTest` is the base for the i18n tests. A `@BeforeAll` forces `Locale.setDefault(Locale.SIMPLIFIED_CHINESE)`
  so assertions like `"测试-a"` are deterministic, and it `destroyServiceMessageSource()` before and after each
  test to keep the `I18nUtils` global holder from leaking between tests. It exposes the `TEST_SOURCE = "test"` and
  `ERROR_SOURCE = "error"` constants.
- `ServiceMessageSourceTest` establishes the template-method inheritance pattern: a `createServiceMessageSource()`
  factory, a generic `getServiceMessageSource()`, `init()` in `@BeforeEach` and `destroy()` in `@AfterEach`. It
  verifies the contract defaults (`COMMON_SOURCE == "common"`, null lookups, default/supported locales).
- `ResourceServiceMessageSourceTest`, `ReloadableResourceServiceMessageSourceTest` and `DefaultServiceMessageSourceTest`
  form a subclass chain — each overrides `createServiceMessageSource()` and inherits the parent's assertions,
  so the resource and reload contracts are checked against the abstract test doubles and the real classpath
  implementation alike.

### Test doubles

`TestServiceMessageSource`, `TestResourceServiceMessageSource`, `TestReloadableResourceServiceMessageSource` and
`TestPropertiesResourceServiceMessageSource` are minimal implementations that exercise the interface default
methods. Notably `TestReloadableResourceServiceMessageSource.canReload(String)` returns whether a resource was
already initialized, letting `ReloadableResourceServiceMessageSourceTest.testCanReload` assert the false→true
transition after `reload`. `TestPropertiesResourceServiceMessageSource.loadAllPropertiesResources` throws
`IOException`, which `PropertiesResourceServiceMessageSourceTest.testLoadMessages` asserts is wrapped as a
`RuntimeException`.

### Per-implementation coverage

- `DefaultServiceMessageSourceTest` covers real classpath resolution: message lookup
  (`getMessage("a")` → `"测试-a"`), the `getLocalizedResourceMessages()` size being `2` (en + zh_CN),
  `loadAllProperties(Locale.FRANCE)` returning `null`, `supports(...)` for default and English locales,
  `resolveMessageCode` both for short and already-prefixed codes, and the invalid-prefix `error` source throwing
  `IllegalStateException` in `testValidateMessageCode`. It also overrides the inherited reload tests because the
  classpath `canReload` is resource-existence based (`canReload(TEST_SOURCE)` is asserted `false`).
- `CompositeServiceMessageSourceTest` builds a composite of `EmptyServiceMessageSource.INSTANCE`, a
  `DefaultServiceMessageSource`, and a `TestReloadableResourceServiceMessageSource`, then asserts composite lookups
  equal the direct source lookups, empty-composite behaviour, the `"common"` source name, locale union, encoding,
  and that `getServiceMessageSources()` preserves order.
- `EmptyServiceMessageSourceTest` asserts the null object always returns null and reports source `"Empty"`.
- `ServiceMessageExceptionTest` configures a `test` source with default and supported locale `zh_CN`
  (`setSupportedLocales(ofList(SIMPLIFIED_CHINESE))`), sets it as the global source, and checks `getMessage()`
  (raw pattern) versus `getLocalizedMessage()` (resolved) versus `toString()` built with
  `io.microsphere.text.FormatUtils.format`.
- `MessageUtilsTest` and `I18nUtilsTest` cover the util layer: `{code}` resolution, the "no braces returns input"
  and "code not found strips to after the first dot" fallbacks, and the global holder defaulting to
  `EmptyServiceMessageSource.INSTANCE` plus recursive composite flattening.

### Java-8 test idioms

The tests collect through `io.microsphere.collection.Lists.ofList`,
`io.microsphere.collection.Sets.ofSet` and `java.util.Collections.emptyList()` instead of `List.of` /
`Set.of`, and use anonymous subclasses rather than `var` or text blocks. Contributors working on this
branch must keep that constraint: the reactor compiles with `maven.compiler.source/target = 8` inherited
from the microsphere parent chain, and the 0.2.x branch is the place where Java 17 idioms are allowed.

### Logging-sensitive tests

Composite and message-util tests are annotated with `@LoggingLevelsClass` / `@LoggingLevelsTest` from
`microsphere-logging-test`, and `logMessage` / trace branches are exercised at TRACE, INFO and ERROR levels.
`PropertiesResourceServiceMessageSourceTest` runs locale setters at ERROR level to assert no accidental logging
is emitted for those paths.

Together the suite keeps `microsphere-i18n-core` under `src/test` coverage for the contract defaults, the
two-step locale fallback, prefix validation, copy-on-write reload semantics, composite delegation, and the
null-object and global-holder safety guarantees.

## Differences from the 0.2.x line

`git diff origin/release origin/release-1.x -- microsphere-i18n-core` shows **only `pom.xml` changes**;
every class under `src/main/java` and `src/test/java` is identical. The differences are therefore
build-time and ecosystem-level:

| Concern | 1.x (`release-1.x`) | 0.2.x (`release`) |
|---------|---------------------|-------------------|
| Artifact version | `0.1.x` (`revision` = `0.1.22-SNAPSHOT`; latest published `0.1.21`) | `0.2.x` (`0.2.22-SNAPSHOT`, published `0.2.21`) |
| Root POM parent | `microsphere-spring-cloud-parent` `0.1.26` | `0.2.26` |
| Java baseline | inherited `java.version` = 8 (`maven.compiler.source/target` from `microsphere-build`) — the 1.x root POM contains no `java.version` override | root POM explicitly sets `<java.version>17</java.version>` |
| Language level in sources | no `List.of` / `Set.of` / `var` / text blocks; JDK-8 APIs plus microsphere collection helpers | Java 17 constructs allowed |
| Servlet coordinate (core POM, optional) | `javax.servlet:javax.servlet-api` | `jakarta.servlet:jakarta.servlet-api` |
| Downstream registration | the Spring modules on this branch publish `META-INF/spring.factories` with the `org.springframework.boot.autoconfigure.EnableAutoConfiguration` key (Spring Boot 2.x mechanism), alongside `AutoConfiguration.imports` | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` only |
| Build generation profiles | `spring-cloud-hoxton` (Boot 2.3.12.RELEASE / Hoxton.SR12), `spring-cloud-2020` (Boot 2.4.13 / Cloud 2020.0.5), `spring-cloud-2021` (Boot 2.6.15 / Cloud 2021.0.9, `activeByDefault`) | Boot 3.x generations (up to `spring-cloud-2025`) |
| CI matrix | Java 8, 11, 17, 21, 25 crossed with the three generation profiles | Java 17+ legs |
| JUnit | `junit-jupiter` 5.14.4 via `junit-bom` | 6.x |

Design-level observations that follow from the above:

- The class hierarchy, the template-method skeletons, the copy-on-write reload protocol and the
  composite/null-object patterns are the same code; nothing in this developer guide needs a per-line
  rewrite.
- The namespace split is invisible inside core (it uses no Servlet types) but is what forces the whole
  project to maintain two lines: a `javax.*`-based Spring Boot 2.x application cannot load the 0.2.x
  Spring/OpenFeign modules, and a Boot 3.x application must not use this line.
- Deprecations are expected, not acted on: `new Locale(String)` is deprecated on JDK 19+, and the CI
  still runs Java 21/25 legs so the warnings are visible, but fixing them would break the Java 8 source
  level. Treat such warnings as intentional on this branch.
- Changes made on `release-1.x` normally need a matching change on `release` (and vice versa) because
  the sources are duplicated; only build descriptors are line-specific.

## Related documentation

- Developer guide: [OpenFeign](openfeign.md), [Parent POM](parent.md), [Dependencies BOM](dependencies.md)
- User guide: [Core](../../user-guide/1.x/core.md)
- 0.2.x equivalent: [current Core developer guide](../current/core.md)
