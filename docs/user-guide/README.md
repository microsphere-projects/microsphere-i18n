# User Guide

Usage-oriented documentation for microsphere-i18n: installation, configuration, API reference and
code examples for every Maven module.

- [中文版](./zh/README.md) · [Back to docs index](../README.md)

## Choose your release line

| **Release line** | **Directory** | **Versions** | **Stack** |
|------------------|---------------|--------------|-----------|
| Current (`release` branch) | [`current/`](./current/README.md) | 0.2.x (latest `0.2.21`) | Java 17, Spring Boot 3.x, Spring Cloud 2022.0.x – 2025.0.x, `jakarta.*` |
| Legacy 1.x (`release-1.x` branch) | [`1.x/`](./1.x/README.md) | 0.1.x (latest `0.1.21`) | Java 8+, Spring Boot 2.3.x – 2.7.x, Spring Cloud Hoxton – 2021.0.x, `javax.*` |

## Documents (per module)

Each release-line directory contains one document per Maven module:

| **Document** | **Covers** |
|--------------|------------|
| [core.md](./current/core.md) | `ServiceMessageSource` API, message file layout, locale fallback, composite sources |
| [spring.md](./current/spring.md) | `@EnableI18n`, Spring `MessageSource` bridge, configuration properties |
| [spring-boot.md](./current/spring-boot.md) | Auto-configuration, Actuator `/actuator/i18n` endpoint |
| [spring-cloud.md](./current/spring-cloud.md) | Dynamic message reload with Spring Cloud config |
| [spring-cloud-server.md](./current/spring-cloud-server.md) | Centralized i18n server (`@EnableI18nServer`, `GET /messages`) |
| [openfeign.md](./current/openfeign.md) | `Accept-Language` propagation for Feign clients |
| [parent.md](./current/parent.md) | Parent POM usage |
| [dependencies.md](./current/dependencies.md) | Importing the project BOM |

Replace `current/` with `1.x/` for the 0.1.x line, or insert `zh/` for Chinese
(e.g. `current/zh/core.md`).

## Recommended reading order

1. Add the [BOM](./current/dependencies.md) to your `pom.xml`.
2. Pick your integration level: plain Spring → [spring.md](./current/spring.md), Spring Boot →
   [spring-boot.md](./current/spring-boot.md), Spring Cloud →
   [spring-cloud.md](./current/spring-cloud.md).
3. Learn the message-file convention in [core.md](./current/core.md).
4. For microservice scenarios add [openfeign.md](./current/openfeign.md) and
   [spring-cloud-server.md](./current/spring-cloud-server.md).
