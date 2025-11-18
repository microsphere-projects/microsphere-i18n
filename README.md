# Microsphere Internationalisation

> Microsphere Projects for Internationalisation

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-i18n)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/microsphere-projects/microsphere-i18n)
[![Maven Build](https://github.com/microsphere-projects/microsphere-i18n/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-i18n/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-i18n/branch/dev/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-i18n)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-i18n.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-i18n.svg)

The Microsphere Internationalisation project is a comprehensive internationalization (i18n) framework designed for Java
applications, with particular emphasis on Spring-based ecosystems. It provides a modular, extensible architecture for
managing localized messages across different deployment scenarios, from standalone applications to distributed
cloud-native systems.

## Purpose and Scope

The Microsphere Internationalisation project extends Spring Cloud Internationalisation with enhanced features including:

- Message Source Hierarchy: The system provides a flexible hierarchy of message sources that can load localized content
  from various sources including properties files, Spring's `Environment`, and external configuration systems. The
  `ServiceMessageSource` interface serves as the primary abstraction, with specialized implementations for different
  resource types.
- Composite and Delegation Patterns: The `CompositeServiceMessageSource` allows multiple message sources to be combined
  with configurable priority ordering. The `DelegatingServiceMessageSource` provides Spring Framework integration while
  maintaining the core abstractions.
- Dynamic Configuration Support: Through Spring Cloud integration, the system supports runtime updates to message
  sources without requiring application restarts. This capability is essential for cloud-native applications where
  configuration can change dynamically.
- Spring Ecosystem Integration: Progressive integration layers provide seamless compatibility with Spring Framework,
  Spring Boot auto-configuration, and Spring Cloud distributed configuration management. The system follows Spring
  conventions and lifecycle patterns.

## Modules

| **Module**                               | **Purpose**                                                                         |
|------------------------------------------|-------------------------------------------------------------------------------------|
| **microsphere-i18n-parent**              | Defines the parent POM with dependency management and Spring Cloud version profiles |
| **microsphere-i18n-dependencies**        | Centralizes dependency management for all project modules                           |
| **microsphere-i18n-core**                | Provides fundamental i18n abstractions and message source implementations           |
| **microsphere-i18n-openfeign**           | Message resolution for OpenFeign clients                                            |
| **microsphere-i18n-spring**              | Integrates with Spring Framework's `MessageSource` and `Environment`                |
| **microsphere-i18n-spring-boot**         | Auto-configuration and actuator endpoints                                           |
| **microsphere-i18n-spring-cloud**        | Dynamic configuration updates via cloud config                                      |
| **microsphere-i18n-spring-cloud-server** | Centralized i18n management server                                                  |

## Getting Started

The easiest way to get started is by adding the Microsphere Internationalisation BOM (Bill of Materials) to your
project's pom.xml:

```xml

<dependencyManagement>
    <dependencies>
        ...
        <!-- Microsphere Internationalisation Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-i18n-dependencies</artifactId>
            <version>${microsphere-i18n.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

`${microsphere-spring-boot.version}` has two branches:

| **Branches** | **Purpose**                                      | **Latest Version** |
|--------------|--------------------------------------------------|--------------------|
| **0.2.x**    | Compatible with Spring Cloud 2022.0.x - 2025.0.x | 0.2.0              |
| **0.1.x**    | Compatible with Spring Cloud Hoxton - 2021.0.x   | 0.1.0              |

Then add the specific modules you need:

### Spring Scenarios

```xml

<dependencies>
    <dependency>
        <!-- Microsphere Internationalisation Spring -->
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-i18n-spring</artifactId>
    </dependency>
</dependencies>
```

### Spring Boot Scenarios

```xml

<dependencies>
    <dependency>
        <!-- Microsphere Internationalisation Spring Boot -->
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-i18n-spring-boot</artifactId>
    </dependency>
</dependencies>
```

### Spring Cloud Scenarios

```xml

<dependencies>
    <dependency>
        <!-- Microsphere Internationalisation Spring Cloud -->
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-i18n-spring-cloud</artifactId>
    </dependency>
</dependencies>
```

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

To build the project, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-i18n.git
```

2. Build the source:

- Linux/MacOS:

```bash
./mvnw package
```

- Windows:

```powershell
mvnw.cmd package
```

## Contributing

We welcome your contributions! Please read [Code of Conduct](./CODE_OF_CONDUCT.md) before submitting a pull request.

## Reporting Issues

* Before you log a bug, please search
  the [issues](https://github.com/microsphere-projects/microsphere-i18n/issues)
  to see if someone has already reported the problem.
* If the issue doesn't already
  exist, [create a new issue](https://github.com/microsphere-projects/microsphere-i18n/issues/new).
* Please provide as much information as possible with the issue report.

## Documentation

### User Guide

[DeepWiki Host](https://deepwiki.com/microsphere-projects/microsphere-i18n)

[ZRead Host](https://zread.ai/microsphere-projects/microsphere-i18n)

### Wiki

[Github Host](https://github.com/microsphere-projects/microsphere-i18n/wiki)

### JavaDoc

- [microsphere-i18n-core](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-core)
- [microsphere-i18n-openfeign](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-openfeign)
- [microsphere-i18n-spring](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-spring)
- [microsphere-i18n-spring-boot](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-spring-boot)
- [microsphere-i18n-spring-cloud](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-spring-cloud)
- [microsphere-i18n-spring-cloud-server](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-i18n-spring-cloud-server)

## License

The Microsphere Spring is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).