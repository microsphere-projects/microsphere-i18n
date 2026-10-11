# Parent POM (`microsphere-i18n-parent`) — 1.x

`microsphere-i18n-parent` is a POM-only module (`<packaging>pom</packaging>`). It publishes no
classes and no artifacts beyond its POM; its job is to give every other module of the project the
same set of managed third-party versions.

This document describes the **`1.x` line** (`0.1.x` version series), the Spring Boot 2.x /
`javax.*` generation. For the current `0.2.x` line see
[docs/user-guide/current/parent.md](../current/parent.md).

## Purpose

The POM does exactly three things (see `microsphere-i18n-parent/pom.xml` on the `1.x` branch):

1. It inherits from the project's root aggregator POM `microsphere-i18n`
   (`<relativePath>../pom.xml</relativePath>`), which in turn inherits from
   `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.1.26`.
2. It declares one property:

   ```xml
   <microsphere-spring-cloud.version>0.1.26</microsphere-spring-cloud.version>
   ```

3. It imports one BOM in `dependencyManagement`:

   ```xml

   <dependency>
       <groupId>io.github.microsphere-projects</groupId>
       <artifactId>microsphere-spring-cloud-dependencies</artifactId>
       <version>${microsphere-spring-cloud.version}</version>
       <type>pom</type>
       <scope>import</scope>
   </dependency>
   ```

That single import is what pins Spring Boot, Spring Cloud, Spring Cloud Alibaba, JUnit,
Testcontainers and the Microsphere Spring Boot artifacts to a coherent set of versions for the
whole reactor. It defines no plugins, no profiles, and no dependencies of its own.

## When to Use It

| Situation                                             | Use                                                       |
|-------------------------------------------------------|-------------------------------------------------------------|
| Consuming microsphere-i18n from an application        | Import the [`microsphere-i18n-dependencies` BOM](dependencies.md) — not this parent |
| Building or contributing to microsphere-i18n          | This parent is already wired in; nothing to add in your POM |
| Adding a new module to the microsphere-i18n reactor   | Set `microsphere-i18n-parent` as the module's `<parent>`    |
| Building a private extension module alongside         | You may set it as `<parent>`, but you must then also align `${revision}` and the source repository |

The framework modules all follow the same pattern — `core`, `openfeign`, `spring`, `spring-boot`,
`spring-cloud`, `spring-cloud-server` and even the `dependencies` BOM declare
`microsphere-i18n-parent` as their `<parent>`, with `<version>${revision}</version>`.

## Using It as a Parent

```xml

<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-i18n-parent</artifactId>
    <version>0.1.21</version>
    <relativePath/>
</parent>

<groupId>com.example</groupId>
<artifactId>my-i18n-extension</artifactId>
<version>1.0.0-SNAPSHOT</version>
```

An empty `<relativePath/>` forces resolution from the repository instead of the file system. After
this, dependencies from the managed set can be declared without a version, for example:

```xml

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
</dependency>
```

Do not confuse this with the consumer-facing BOM: `microsphere-i18n-parent` does **not** manage the
versions of the `microsphere-i18n-*` artifacts themselves. That is the role of
`microsphere-i18n-dependencies`.

## What Gets Managed

`microsphere-i18n-parent` contributes one entry to `dependencyManagement` — the import of
`microsphere-spring-cloud-dependencies:0.1.26`. Through that chain, the reactor resolves versions
for:

| Managed group                                                        | Provided by                                             |
|----------------------------------------------------------------------|-----------------------------------------------------------|
| `org.springframework.boot:*` (and everything `spring-boot-dependencies` manages, including `spring-*`, `javax.servlet-api`, Logback, Tomcat) | `spring-boot-dependencies`, version selected by the active Spring Boot 2.x profile |
| `org.springframework.cloud:*`                                        | `spring-cloud-dependencies`                               |
| `com.alibaba.cloud:*`                                                | `spring-cloud-alibaba-dependencies`                       |
| `io.github.microsphere-projects:*` (Microsphere Spring Boot line)    | `microsphere-spring-boot-dependencies`, pinned by the `0.1.26` chain |
| `io.github.microsphere-projects:microsphere-spring-cloud-commons`, `microsphere-spring-cloud-openfeign` | `microsphere-spring-cloud-dependencies:0.1.26`            |
| `org.junit:*`, `org.junit.jupiter:*`                                 | `junit-bom`, selected by the upstream chain               |
| `org.testcontainers:*`                                               | `testcontainers-bom`, selected by the upstream chain      |

Because the `1.x` line resolves the Spring Boot 2.x generation, the concrete versions are the
Spring Framework 5.x / `javax.*` family (for example `spring-webmvc` from the Spring Boot 2.x
release train selected by the active profile). The exact numbers are not written in the module POMs;
they come entirely from this parent chain.

## Version and Compatibility

The parent is published in the same release train as the rest of the project, so it always carries
the version `${revision}` of the reactor (for example `0.1.21`).

| microsphere-i18n line | Parent POM version | Upstream `microsphere-spring-cloud` version | Java | Spring Boot | Spring Cloud |
|-----------------------|--------------------|---------------------------------------------|------|-------------|--------------|
| `1.x` (this line)     | `0.1.x`, e.g. `0.1.21` (working `0.1.22-SNAPSHOT`) | `0.1.26`                                    | not declared in the reactor POM | 2.x | Hoxton – 2021.0.x |
| `0.2.x` (current)     | `0.2.x`, e.g. `0.2.21` | `0.2.26`                                    | 17 (`java.version` property in the reactor POM) | 3.x | 2022.0.x – 2025.0.x |

Inside the `1.x` line, the Spring Boot / Spring Cloud pair actually used by a build is chosen by a
Maven profile inherited from `microsphere-spring-cloud-parent:0.1.26`
(see the [Developer Guide](../../developer-guide/1.x/parent.md) for the profile table and how CI
uses it).

## Notes

- Because it is a POM with no code, there is nothing to test at runtime; the "test" of this module
  is that the reactor resolves and compiles.
- Upgrading `microsphere-spring-cloud.version` is the single-lever change that moves every
  third-party dependency of the project; it is the property to touch when adding support for a new
  Spring Cloud generation.
- The published POMs are flattened: `${revision}` is replaced by the concrete release version, so
  a POM that references `microsphere-i18n-parent` never needs `${revision}` itself.

## Differences from the 0.2.x line

The `1.x` parent has the identical shape (one property, one BOM import) and the same role in the
reactor; only the version numbers change:

| Aspect                                   | `1.x` (`0.1.x`) line                             | `0.2.x` line                            |
|------------------------------------------|--------------------------------------------------|-----------------------------------------|
| Root parent version                      | `microsphere-spring-cloud-parent:0.1.26`         | `microsphere-spring-cloud-parent:0.2.26` |
| `microsphere-spring-cloud.version`       | `0.1.26`                                         | `0.2.26`                                |
| BOM import target                        | `microsphere-spring-cloud-dependencies:0.1.26`   | `microsphere-spring-cloud-dependencies:0.2.26` |
| `${revision}` (working / published)      | `0.1.22-SNAPSHOT` / `0.1.21`                     | `0.2.22-SNAPSHOT` / `0.2.21`            |
| Generation reached through the chain     | Spring Boot 2.x / Spring Cloud Hoxton – 2021.0.x | Spring Boot 3.x / Spring Cloud 2022.0.x – 2025.0.x |
| Servlet/validation namespace pinned      | `javax.*`                                        | `jakarta.*`                             |
| `java.version` property in reactor POM   | not declared                                     | `17`                                    |

`microsphere-i18n-parent/pom.xml` differs between the two release branches by exactly one line:
the `microsphere-spring-cloud.version` property value (`0.1.26` vs `0.2.26`).

## Related Documentation

- [Dependencies BOM](dependencies.md) — what applications should import
- [Spring Boot](spring-boot.md) and the other module guides
- Developer Guide: [Parent POM](../../developer-guide/1.x/parent.md)
- 0.2.x counterpart: [current Parent guide](../current/parent.md)

## Module dependencies

POM structure:

```mermaid
flowchart TD
    ROOT["microsphere-i18n (aggregator)"] --> PARENT["microsphere-i18n-parent"]
    PARENT -. "inherited by all modules" .- CORE["core / spring / spring-boot / spring-cloud / server / openfeign"]
    PARENT -. "parent of" .- DEPS["microsphere-i18n-dependencies (BOM)"]
    DEPS -. "manages versions of" .- CORE
```
