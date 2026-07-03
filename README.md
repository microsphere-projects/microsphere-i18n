# Microsphere Internationalisation

> Microsphere Projects for Internationalisation

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-i18n)
[![Maven Build](https://github.com/microsphere-projects/microsphere-i18n/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-i18n/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-i18n/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-i18n)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-i18n.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-i18n.svg)

Microsphere Internationalisation is a comprehensive i18n framework for Java applications, with deep integration into
the Spring ecosystem. It provides a modular, extensible architecture for managing localized messages across deployment
scenarios ranging from standalone Spring applications to distributed, cloud-native microservices.

## Table of Contents

- [Features](#features)
- [Modules](#modules)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
    - [Add the BOM](#add-the-bom)
    - [Spring Applications](#spring-applications)
    - [Spring Boot Applications](#spring-boot-applications)
    - [Spring Cloud Applications](#spring-cloud-applications)
    - [OpenFeign Integration](#openfeign-integration)
    - [Centralized I18n Server](#centralized-i18n-server)
- [Message Files](#message-files)
- [Configuration Properties](#configuration-properties)
- [Actuator Endpoint](#actuator-endpoint)
- [Building from Source](#building-from-source)
- [Getting Help](#getting-help)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Layered message sources** — load localized messages from classpath properties files, Spring `Environment`, or
  external configuration systems, all through a unified `ServiceMessageSource` abstraction.
- **Composite sources with priority ordering** — combine multiple message sources via `CompositeServiceMessageSource`;
  higher-priority sources shadow lower-priority ones.
- **Dynamic configuration** — Spring Cloud integration enables runtime message updates without application restarts.
- **Spring MessageSource bridge** — the primary `ServiceMessageSource` bean is automatically exposed as Spring's
  `MessageSource`, so it works transparently with `@Value`, Thymeleaf, and other Spring facilities.
- **Bean Validation integration** — i18n-aware `LocalValidatorFactoryBean` translates constraint violation messages
  through the same source.
- **OpenFeign propagation** — an `Accept-Language` request interceptor automatically forwards the current locale to
  downstream Feign clients.
- **Actuator endpoint** — read and write i18n messages at runtime through `/actuator/i18n`.
- **Centralized i18n server** — serve consolidated message bundles across microservices via a dedicated Spring Cloud
  server module.

## Modules

| **Module**                               | **Purpose**                                                                         |
|------------------------------------------|-------------------------------------------------------------------------------------|
| **microsphere-i18n-parent**              | Defines the parent POM with dependency management and Spring Cloud version profiles |
| **microsphere-i18n-dependencies**        | Centralizes dependency management for all project modules                           |
| **microsphere-i18n-core**                | Provides fundamental i18n abstractions and message source implementations           |
| **microsphere-i18n-openfeign**           | Propagates the current locale to downstream OpenFeign clients                       |
| **microsphere-i18n-spring**              | Integrates with Spring Framework's `MessageSource` and `Environment`                |
| **microsphere-i18n-spring-boot**         | Auto-configuration and Actuator endpoint                                            |
| **microsphere-i18n-spring-cloud**        | Dynamic configuration updates via Spring Cloud config                               |
| **microsphere-i18n-spring-cloud-server** | Centralized i18n management server                                                  |

## Requirements

- **Java** 17 or later
- **Spring Framework** 6.x (for the `microsphere-i18n-spring` module and above)
- **Spring Boot** 3.x (for the `microsphere-i18n-spring-boot` module and above)
- **Spring Cloud** 2022.0.x – 2025.0.x (main branch) or Hoxton – 2021.0.x (1.x branch)

## Getting Started

### Add the BOM

Import the Bill of Materials into your `pom.xml` to manage all module versions centrally:

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
```

Choose the version that matches your Spring Cloud generation:

| **Branch** | **Compatible Spring Cloud** | **Latest Version** |
|------------|-----------------------------|--------------------|
| **main**   | 2022.0.x – 2025.0.x         | `0.2.16`           |
| **1.x**    | Hoxton – 2021.0.x           | `0.1.16`           |

### Spring Applications

Add the Spring integration module and enable i18n support with `@EnableI18n`:

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-i18n-spring</artifactId>
</dependency>
```

```java

@EnableI18n(sources = {"common", "myapp"})
@Configuration
public class I18nConfig {
}
```

Inject and use `ServiceMessageSource` anywhere in your Spring beans:

```java

@Service
public class GreetingService {

    @Autowired
    private ServiceMessageSource messageSource;

    public String greet(String name) {
        // Resolves "hello" from the message files for the current request locale
        return messageSource.getMessage("hello", name);
    }
}
```

### Spring Boot Applications

Add the auto-configuration module — no `@EnableI18n` annotation required:

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-i18n-spring-boot</artifactId>
</dependency>
```

The auto-configuration activates when `microsphere.i18n.enabled=true` (the default). Set
`spring.application.name` to automatically register an application-specific message source alongside
the default `common` source:

```properties
spring.application.name=myapp
microsphere.i18n.enabled=true
```

### Spring Cloud Applications

Add the Spring Cloud module for dynamic message refresh without application restarts:

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-i18n-spring-cloud</artifactId>
</dependency>
```

Message sources are automatically refreshed when Spring Cloud configuration change events are
received, so updating messages in a config server takes effect immediately across all instances.

### OpenFeign Integration

Add the OpenFeign module to propagate the current request locale to downstream services:

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-i18n-openfeign</artifactId>
</dependency>
```

`AcceptLanguageHeaderRequestInterceptor` is registered automatically. It adds an `Accept-Language`
header to every Feign request so that downstream services resolve messages in the caller's locale.

### Centralized I18n Server

Stand up a dedicated server that aggregates and serves message bundles for all microservices:

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-i18n-spring-cloud-server</artifactId>
</dependency>
```

Annotate your Spring Boot application class:

```java

@EnableI18nServer
@SpringBootApplication
public class I18nServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(I18nServerApplication.class, args);
    }
}
```

The server exposes a `GET /messages` endpoint that returns all registered message bundles as JSON.

## Message Files

Place message properties files under `META-INF/i18n/<source>/` on the classpath. The naming
convention is `i18n_messages_<locale>.properties`:

```
src/main/resources/
└── META-INF/
    └── i18n/
        ├── common/
        │   ├── i18n_messages_en.properties
        │   └── i18n_messages_zh_CN.properties
        └── myapp/
            ├── i18n_messages_en.properties
            └── i18n_messages_zh_CN.properties
```

Message values use `{}` as the positional placeholder:

```properties
# i18n_messages_en.properties
hello=Hello, {}
error.notFound=Resource '{}' was not found
```

```properties
# i18n_messages_zh_CN.properties
hello=您好，{}
error.notFound=资源"{}"未找到
```

Retrieve a message with arguments at runtime:

```java
// Resolves to "Hello, World" or "您好，World" based on the current locale
String msg = messageSource.getMessage("hello", "World");
```

## Configuration Properties

All properties are prefixed with `microsphere.i18n.`:

| **Property**                         | **Type**   | **Default** | **Description**                                                    |
|--------------------------------------|------------|-------------|--------------------------------------------------------------------|
| `microsphere.i18n.enabled`           | `boolean`  | `true`      | Enables or disables the i18n framework                             |
| `microsphere.i18n.sources`           | `String[]` | —           | Additional message source names to register (e.g., `common,myapp`) |
| `microsphere.i18n.default-locale`    | `Locale`   | JVM default | Default locale used when no request locale is available            |
| `microsphere.i18n.supported-locales` | `String[]` | —           | Comma-separated list of supported locales (e.g., `zh_CN,en`)       |

## Actuator Endpoint

When Spring Boot Actuator is on the classpath, the `/actuator/i18n` endpoint is registered
automatically (requires `management.endpoints.web.exposure.include=i18n` or `*`).

| **Method** | **Path**                | **Description**                                      |
|------------|-------------------------|------------------------------------------------------|
| `GET`      | `/actuator/i18n`        | List all localized messages grouped by resource file |
| `GET`      | `/actuator/i18n/{code}` | Get all locale variants of a single message code     |
| `POST`     | `/actuator/i18n`        | Add or overwrite a message at runtime                |

## Building from Source

You don't need to build from source to use the project. To build for development or to try the
latest unreleased changes:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-i18n.git
cd microsphere-i18n
```

2. Build and run tests:

- Linux / macOS:

```bash
./mvnw verify
```

- Windows:

```powershell
mvnw.cmd verify
```

## Getting Help

- **Documentation**:
    - [DeepWiki](https://deepwiki.com/microsphere-projects/microsphere-i18n)
    - [ZRead](https://zread.ai/microsphere-projects/microsphere-i18n)
    - [GitHub Wiki](https://github.com/microsphere-projects/microsphere-i18n/wiki)
    - [Developer Guide](./developer-guide.md)
- **JavaDoc**:
    - [microsphere-i18n-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-core)
    - [microsphere-i18n-openfeign](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-openfeign)
    - [microsphere-i18n-spring](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-spring)
    - [microsphere-i18n-spring-boot](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-spring-boot)
    - [microsphere-i18n-spring-cloud](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-spring-cloud)
    - [microsphere-i18n-spring-cloud-server](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-spring-cloud-server)
- **Bug reports / feature requests**: [GitHub Issues](https://github.com/microsphere-projects/microsphere-i18n/issues) —
  please search existing issues before opening a new one and include as much detail as possible.

## Contributing

We welcome contributions of all kinds. Please read the [Code of Conduct](./CODE_OF_CONDUCT.md) before
submitting a pull request.

**Maintainer**: [Mercy Ma](mailto:mercyblitz@gmail.com) ([@mercyblitz](https://github.com/mercyblitz)) — lead architect
and primary developer.

## License

Microsphere Internationalisation is released under the [Apache License 2.0](./LICENSE).
