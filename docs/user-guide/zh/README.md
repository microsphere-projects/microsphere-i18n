# 用户文档

microsphere-i18n 的使用类文档：安装、配置、API 说明与代码示例，覆盖全部 Maven 模块。

- [English](./README.md) · [返回文档总入口](../zh/README.md)

## 选择发布线

| **发布线** | **目录** | **版本** | **技术栈** |
|------------|----------|----------|------------|
| 当前线（`release` 分支） | [`current/`](../current/zh/README.md) | 0.2.x（最新 `0.2.21`） | Java 17、Spring Boot 3.x、Spring Cloud 2022.0.x – 2025.0.x、`jakarta.*` |
| 1.x 维护线（`release-1.x` 分支） | [`1.x/`](../1.x/zh/README.md) | 0.1.x（最新 `0.1.21`） | Java 8+、Spring Boot 2.3.x – 2.7.x、Spring Cloud Hoxton – 2021.0.x、`javax.*` |

## 文档列表（按 Maven 模块）

每个发布线目录内均按模块提供独立文档：

| **文档** | **内容** |
|----------|----------|
| core.md | `ServiceMessageSource` API、消息文件布局、Locale 回退、组合消息源 |
| spring.md | `@EnableI18n`、Spring `MessageSource` 桥接、配置属性 |
| spring-boot.md | 自动装配、Actuator `/actuator/i18n` 端点 |
| spring-cloud.md | Spring Cloud 配置变更时的消息动态刷新 |
| spring-cloud-server.md | 集中式 i18n 服务器（`@EnableI18nServer`、`GET /messages`） |
| openfeign.md | Feign 调用传播 `Accept-Language` |
| parent.md | 父 POM 的使用 |
| dependencies.md | 导入项目 BOM |

中文版路径在对应英文路径中加入 `zh/`，例如 `current/zh/core.md`、`1.x/zh/spring-boot.md`。

## 建议阅读顺序

1. 在 `pom.xml` 中导入 [BOM](../current/zh/dependencies.md)。
2. 按集成层次选择：原生 Spring → [spring.md](../current/zh/spring.md)；Spring Boot →
   [spring-boot.md](../current/zh/spring-boot.md)；Spring Cloud →
   [spring-cloud.md](../current/zh/spring-cloud.md)。
3. 通过 [core.md](../current/zh/core.md) 掌握消息文件约定。
4. 微服务场景再阅读 [openfeign.md](../current/zh/openfeign.md) 与
   [spring-cloud-server.md](../current/zh/spring-cloud-server.md)。
