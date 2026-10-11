# Developer Guide — Current (0.2.x, release branch)

Internal design documentation for the 0.2.x release line (`release` branch): architecture,
source analysis, design patterns and flow diagrams.

- [中文](./zh/README.md) · [1.x line](../1.x/README.md) · [Developer Guide index](../README.md)

## Stack analyzed

Java 17, Spring Framework 6.x, Spring Boot 3.x (auto-configuration via
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`),
`jakarta.*` namespace.

## Module documents

| **Module** | **English** | **中文** | **Key topics** |
|------------|-------------|----------|----------------|
| core | [core.md](./core.md) | [core.md](./zh/core.md) | Class hierarchy, resolution algorithm, locale fallback, Template Method / Composite / Null Object |
| spring | [spring.md](./spring.md) | [spring.md](./zh/spring.md) | `@EnableI18n` registration pipeline, FactoryBean discovery, Adapter, Observer, post-processors |
| spring-boot | [spring-boot.md](./spring-boot.md) | [spring-boot.md](./zh/spring-boot.md) | Conditional evaluation chain, Actuator endpoint write path |
| spring-cloud | [spring-cloud.md](./spring-cloud.md) | [spring-cloud.md](./zh/spring-cloud.md) | `EnvironmentChangeEvent` → reload sequence |
| spring-cloud-server | [spring-cloud-server.md](./spring-cloud-server.md) | [spring-cloud-server.md](./zh/spring-cloud-server.md) | Discovery-driven aggregation, lazy composite init |
| openfeign | [openfeign.md](./openfeign.md) | [openfeign.md](./zh/openfeign.md) | Header resolution flowchart |
| parent | [parent.md](./parent.md) | [parent.md](./zh/parent.md) | POM inheritance chain |
| dependencies | [dependencies.md](./dependencies.md) | [dependencies.md](./zh/dependencies.md) | BOM import chain |

## Module dependency graph

```mermaid
flowchart TD
    subgraph POM["Build structure"]
        PARENT[microsphere-i18n-parent]
        DEPS[microsphere-i18n-dependencies (BOM)]
    end
    CORE[core] --> SPRING[spring] --> BOOT[spring-boot] --> CLOUD[spring-cloud] --> SERVER[spring-cloud-server]
    CORE --> FEIGN[openfeign]
    PARENT -. inherited by .- CORE
    DEPS -. manages versions of .- CLOUD
```

Reading order: [core.md](./core.md) → [spring.md](./spring.md) → [spring-boot.md](./spring-boot.md)
→ [spring-cloud.md](./spring-cloud.md) → [spring-cloud-server.md](./spring-cloud-server.md).
