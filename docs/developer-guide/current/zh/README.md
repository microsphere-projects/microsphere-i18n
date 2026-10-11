# 开发者文档 — 当前线（0.2.x，release 分支）

0.2.x 发布线（`release` 分支）的内部设计文档：架构、源码分析、设计模式与流程图。

- [English](../README.md) · [1.x 发布线](../../1.x/zh/README.md) · [开发者文档总入口](../zh/README.md)

## 技术分析范围

Java 17、Spring Framework 6.x、Spring Boot 3.x（自动装配通过
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册）、
`jakarta.*` 命名空间。

## 模块文档

| **模块** | **英文** | **中文** | **主题** |
|----------|----------|----------|----------|
| core | [core.md](../core.md) | [core.md](./core.md) | 类层次、解析算法、Locale 回退、模板方法 / 组合 / 空对象模式 |
| spring | [spring.md](../spring.md) | [spring.md](./spring.md) | `@EnableI18n` 注册管线、FactoryBean 发现、适配器、观察者、后置处理器 |
| spring-boot | [spring-boot.md](../spring-boot.md) | [spring-boot.md](./spring-boot.md) | 条件评估链、Actuator 端点写入路径 |
| spring-cloud | [spring-cloud.md](../spring-cloud.md) | [spring-cloud.md](./spring-cloud.md) | `EnvironmentChangeEvent` → 重载时序 |
| spring-cloud-server | [spring-cloud-server.md](../spring-cloud-server.md) | [spring-cloud-server.md](./spring-cloud-server.md) | 服务发现驱动的聚合、组合源惰性初始化 |
| openfeign | [openfeign.md](../openfeign.md) | [openfeign.md](./openfeign.md) | 请求头解析流程图 |
| parent | [parent.md](../parent.md) | [parent.md](./parent.md) | POM 继承链 |
| dependencies | [dependencies.md](../dependencies.md) | [dependencies.md](./dependencies.md) | BOM 导入链 |

## 模块依赖关系

```mermaid
flowchart TD
    subgraph POM["构建结构"]
        PARENT[microsphere-i18n-parent]
        DEPS[microsphere-i18n-dependencies (BOM)]
    end
    CORE[core] --> SPRING[spring] --> BOOT[spring-boot] --> CLOUD[spring-cloud] --> SERVER[spring-cloud-server]
    CORE --> FEIGN[openfeign]
    PARENT -. 被继承 .- CORE
    DEPS -. 版本管理 .- CLOUD
```

建议阅读顺序：[core.md](./core.md) → [spring.md](./spring.md) → [spring-boot.md](./spring-boot.md)
→ [spring-cloud.md](./spring-cloud.md) → [spring-cloud-server.md](./spring-cloud-server.md)。
