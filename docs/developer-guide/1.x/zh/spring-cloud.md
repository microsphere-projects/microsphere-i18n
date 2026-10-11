# Spring Cloud 集成 - 开发者指南（1.x）

> 适用于 `0.1.x` 版本线（`release-1.x` / `1.x` 分支）：Spring Boot 2.3 - 2.7、
> Spring Cloud Hoxton.SR12 - 2021.0.9、`javax.*` 命名空间，自动配置通过 `META-INF/spring.factories` 注册。
> Spring Boot 3 / `0.2.x` 版本线请参见
> [../../current/zh/spring-cloud.md](../../current/zh/spring-cloud.md)。

## 架构与关键类

`microsphere-i18n-spring-cloud` 是 Spring Cloud 环境刷新机制与 `microsphere-i18n-spring` 中定义的、由
`PropertySources` 支撑的消息源之间的一个轻量、事件驱动的桥接层。它包含两个生产类。

### `I18nCloudAutoConfiguration`

`io.microsphere.i18n.spring.cloud.autoconfigure.I18nCloudAutoConfiguration` 是一个
`@Configuration(proxyBeanMethods = false)` 类，声明在本模块的 `META-INF/spring.factories` 中（见下文的注册机制一节）。
它只贡献一个 Bean，并带有三个激活守卫：

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnI18nAvailable
@ConditionalOnClass(name = {
        "org.springframework.cloud.context.environment.EnvironmentChangeEvent",
})
public class I18nCloudAutoConfiguration {
    @Bean
    public ReloadableResourceServiceMessageSourceListener reloadableResourceServiceMessageSourceListener() {
        return new ReloadableResourceServiceMessageSourceListener();
    }
}
```

- `@ConditionalOnI18nAvailable`（来自 `microsphere-i18n-spring-boot`）展开为
  `@ConditionalOnClass(ServiceMessageSource, EnableI18n)` 加上
  `@ConditionalOnProperty(name = "microsphere.i18n.enabled", matchIfMissing = true)`。因此 `core`/`spring` 必须在场，
  且 i18n 没有被显式禁用。
- `@ConditionalOnClass(EnvironmentChangeEvent)` 要求 `spring-cloud-context`；没有它整个模块就是惰性的，这正是能够把
  Spring Cloud 依赖安全地设为 optional 的原因。

### `ReloadableResourceServiceMessageSourceListener`

`io.microsphere.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener` 实现了
`ApplicationListener<EnvironmentChangeEvent>` 与 `BeanFactoryAware`。它的全部逻辑就是一行两语句的委托：

```java
@Override
public void onApplicationEvent(EnvironmentChangeEvent event) {
    Set<String> changedPropertyNames = event.getKeys();
    reloadAll(this.beanFactory, changedPropertyNames);
}
```

`BeanFactory` 通过 `setBeanFactory(...)` 注入，因为 reload 辅助方法是 `PropertySourcesServiceMessageSource` 上的一个静态
方法，它需要的是 Bean 查找能力，而不是对主消息源的实时引用。

### 重新加载算法（`PropertySourcesServiceMessageSource.reloadAll`）

真正的逻辑位于 `microsphere-i18n-spring`。`reloadAll(BeanFactory, Set<String>)`：

1. `findAllPropertySourcesServiceMessageSources(beanFactory)` 解析出主 `serviceMessageSource`
   （`DelegatingServiceMessageSource`），并通过 `I18nUtils.findAllServiceMessageSources(...)` 遍历其组合体图，只保留
   `PropertySourcesServiceMessageSource` 实例。
2. 对每个源遍历 `getResources()`。在此设计中，一个资源字符串就等于它所加载的 Spring 属性名
   （`getPropertyName(resource)` 是恒等映射），形如 `<source>.i18n_messages_<locale>.properties`（source 与文件用
   `MessageUtils.SOURCE_SEPARATOR` 连接，而该分隔符是 `.`）。
3. 只有当 `changedPropertyNames.contains(propertyName)` 时资源才被记为“已变化”；只有当 `canReload(changedResources)`
   返回 `true` 时源才被重新加载。`canReload(String)` 检查 `environment.containsProperty(propertyName)`。
4. `reload(changedResources)` -> `initializeResources(...)` 会从 `Environment` 重新读取每个属性的值（该值是作为
   `String` 保存的 `.properties` 文件内容，经由 `loadAllPropertiesResources` 中的 `StringReader` 加载）并重新解析。

正是这一点让监听器做到精确，而不是“全量重载”：一次改变 `server.port` 的刷新所发布的 `EnvironmentChangeEvent`，其键与任何
i18n 资源属性都不匹配，因此没有任何消息源会被重建。

## 自动配置注册机制

这是与 `0.2.x` 线在结构上唯一的差别。在 `release-1.x` 上，本模块使用 Spring Boot 2.x 的 `spring.factories` 机制注册其
自动配置。文件 `microsphere-i18n-spring-cloud/src/main/resources/META-INF/spring.factories` 的全部内容就是：

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
io.microsphere.i18n.spring.cloud.autoconfigure.I18nCloudAutoConfiguration
```

（该文件末尾没有换行符；唯一的取值在反斜杠续行之后继续。）

它被消费的方式：

- `@EnableAutoConfiguration`（`I18nBootstrap` 与 `@SpringBootApplication` 都会用到）会触发
  `AutoConfigurationImportSelector`，它加载 classpath 上所有 `META-INF/spring.factories` 中
  `EnableAutoConfiguration` 键下的条目。Spring Boot 2.0 - 2.6 只读取这个文件。
- 真正的过滤仍由 `@ConditionalOnClass`、`@ConditionalOnProperty`、`@ConditionalOnBean` 完成，因此被列出但条件不成立的
  条目只会退避。
- Spring Boot 2.7 会额外读取
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，本模块同样附带了该文件，且条目与上面一致。
  在 2.7 上两个来源会被合并并去重，因此该类只会被注册一次；在 2.3 - 2.6 上 imports 文件不起任何作用。
- 相对于其他自动配置的顺序并不是由注册文件决定的，而来自
  `@AutoConfigureAfter` / `@AutoConfigureBefore` / `@AutoConfigureOrder` —— `I18nServerAutoConfiguration` 正是借此排在
  `I18nCloudAutoConfiguration` 之后。
- 同级的 `microsphere-i18n-spring-boot` 模块遵循同样的模式，在同一键下列出 `I18nAutoConfiguration` 与
  `I18nEndpointAutoConfiguration`。

由于自动配置类是按类名注册的，模块 POM 中生成条件元数据的注解处理器依赖
（`spring-boot-autoconfigure-processor`）被声明为 optional，同时 `microsphere-annotation-processor` 也在场，用于
`@ConditionalOnI18nAvailable` 这类别名条件。

## 设计模式

- **观察者（Observer）** - `ReloadableResourceServiceMessageSourceListener` 是一个具体的观察者，注册为 Spring 的
  `ApplicationListener<EnvironmentChangeEvent>`。Spring Cloud 的上下文刷新是被通知方，它发出携带变化键的
  `EnvironmentChangeEvent`（`event.getKeys()`）；监听器作出响应，而发出方对 i18n 一无所知。这种解耦正是本模块可以把 Spring
  Cloud 依赖设为 optional 的原因。
- **组合（Composite）** - 重新加载的目标集合是通过 `DelegatingServiceMessageSource` 发现的，后者继承自
  `CompositeServiceMessageSource`。`reloadAll` 遍历那棵组合体树以找到叶子级的 `PropertySourcesServiceMessageSource`
  Bean，因此由 `@EnableI18n(sources = ...)` 添加的、按 source 划分的消息源会被自动覆盖。
- **模板方法（Template Method）** - `canReload` / `reload` / `initializeResources` 在
  `ReloadableResourceServiceMessageSource` 接口（core）上定义，并由 `PropertySourcesServiceMessageSource`（spring）特化为
  从 `Environment` 读取。
- **静态辅助 / 服务定位** - 监听器持有一个 `BeanFactory` 并调用静态的 `reloadAll(...)`，以无状态监听器 Bean 换取对协作者的
  注入。
- **只在打包层面分叉的版本线** - 两个生产类与 `0.2.x` 线完全一致；Spring Boot 2 与 3 的差异被注册文件和 POM 中的
  servlet-API 坐标完全吸收，这也是本模块源码里看不到任何 `jakarta`/`javax` 导入的原因。

## 序列：配置变更到重新加载

```mermaid
sequenceDiagram
    participant Ops as Operator / Config Server
    participant Spring as Spring Cloud Context
    participant Bus as ApplicationContext
    participant Listener as ReloadableResourceServiceMessageSourceListener
    participant PSS as PropertySourcesServiceMessageSource
    participant Env as Environment

    Ops->>Spring: refresh (POST /actuator/refresh or env change)
    Spring->>Env: rebind property sources
    Spring->>Bus: publish EnvironmentChangeEvent(keys)
    Bus->>Listener: onApplicationEvent(event)
    Listener->>PSS: reloadAll(beanFactory, event.getKeys())
    PSS->>PSS: findAllPropertySourcesServiceMessageSources(beanFactory)
    loop each source
        PSS->>Env: canReload? environment.containsProperty(propertyName)
        Env-->>PSS: true / false
        alt property present and key changed
            PSS->>Env: reload(changedResources) -> getProperty(name)
            Env-->>PSS: new .properties content
            PSS->>PSS: re-parse resources
        else not matching
            PSS->>PSS: skip
        end
    end
    Note over PSS: getMessage(code, locale) now returns the reloaded value
```

## 流程图：`I18nCloudAutoConfiguration` 条件

```mermaid
flowchart TD
    A[Auto-configuration load] --> B{ConditionalOnI18nAvailable}
    B -- ServiceMessageSource / EnableI18n missing --> Z[Back off]
    B -- present --> C{microsphere.i18n.enabled != false<br/>matchIfMissing=true}
    C -- false --> Z
    C -- true/absent --> D{ConditionalOnClass<br/>EnvironmentChangeEvent}
    D -- spring-cloud-context absent --> Z
    D -- present --> E[Register ReloadableResourceServiceMessageSourceListener]
    E --> F[Listener reacts to EnvironmentChangeEvent]
```

## 模块依赖

```mermaid
flowchart LR
    server[microsphere-i18n-spring-cloud-server]
    cloud[microsphere-i18n-spring-cloud]
    boot[microsphere-i18n-spring-boot]
    spring[microsphere-i18n-spring]
    core[microsphere-i18n-core]
    openfeign[microsphere-i18n-openfeign]

    server --> cloud
    cloud --> boot
    boot --> spring
    spring --> core
    openfeign --> core

    cloud -.optional.-> scc[org.springframework.cloud:spring-cloud-context]
    scc -.provides.-> evn[EnvironmentChangeEvent]
```

`microsphere-i18n-spring-cloud` 的 POM 只非 optional 地依赖 `microsphere-i18n-spring-boot`
（版本由 `${revision}` 决定）；其余全部是 `optional`：`microsphere-spring-cloud-commons`、`spring-cloud-commons`、
`spring-cloud-context`、`spring-boot-autoconfigure`、`spring-boot-starter-web`、actuator 的 autoconfigure 模块、
configuration/autoconfigure 处理器以及 `javax.servlet:javax.servlet-api`。

## 测试策略

测试位于 `src/test/java` 之下，分为条件覆盖与行为集成两类。

- `I18nCloudAutoConfigurationTest` 继承 `AutoConfigurationTest<I18nCloudAutoConfiguration>`。它断言该自动配置会贡献
  `ReloadableResourceServiceMessageSourceListener`，并在负例下正确退避。各 override 枚举了负例：
  - `configureGlobalDisabledPropertyValues` 添加 `microsphere.i18n.enabled=false`。
  - `configureGlobalMissingClasses` 添加 `ServiceMessageSource`、`EnableI18n` 与 `EnvironmentChangeEvent` —— 每一个单独都应
    禁用本模块，覆盖两个 `@Conditional*` 守卫。
- `I18nCloudAutoConfigurationIntegrationTest` 启动一个真实的 `@EnableAutoConfiguration` 上下文
  （`spring.cloud.service-registry.auto-registration.enabled=false`，并暴露 `features` 端点），断言 `FeaturesEndpoint`
  能成功自动注入。其目的在于证明本模块的存在不会破坏一个 Spring Cloud 运行时上下文。
- `MessageSourceIntegrationTest` 是端到端跑通重新加载路径的行为测试。它导入
  `I18nAutoConfiguration`、`I18nEndpointAutoConfiguration`、`PropertyPlaceholderAutoConfiguration` 与
  `ReloadableResourceServiceMessageSourceListener`，随后：
  1. 解析 Bean `commonServiceMessageSource` 并断言 `getMessage("common.a", ENGLISH)` 为 `"a"`（来自
     `META-INF/i18n/common/i18n_messages_en.properties`），
  2. 添加一个用 `common.a=a.2024` 覆盖 `common.i18n_messages_en.properties` 的 `MapPropertySource`，
  3. 发布 `new EnvironmentChangeEvent(ofSet("common.i18n_messages_en.properties"))`，
  4. 断言 `getMessage("common.a", ENGLISH)` 变为 `"a.2024"`，
  5. 读取 `I18nEndpoint` 的 JSON 并断言 JSONPath
     `$.['common.i18n_messages_en.properties'].['common.a']` 等于 `"a.2024"`。
  这是最能说明观察者装配、`reloadAll` 过滤与基于 `Environment` 的重新加载三者协同的证据。
- `I18nBootstrap` 是一个可运行的 `main`（servlet web 应用），它预置
  `common.i18n_messages_en.properties=common.a = common-a-2024`，并加载 `i18n-bootstrap.properties`
  （`spring.application.name=i18n-bootstrap`，启用 actuator `i18n` 与 env-post），用于手动验证
  `/actuator/i18n` 与 `/actuator/env`。
- `META-INF/config/default/features.yaml` 是本模块为 `microsphere-spring-cloud-commons` 提供的特性目录：它按模块键
  列出各层所贡献的类（`microsphere-i18n-spring`、`microsphere-i18n-spring-boot`，以及本模块的
  `io.microsphere.i18n.spring.cloud.event.ReloadableResourceServiceMessageSourceListener`）。该目录以最低优先级默认值的
  形式绑定到 `microsphere.spring.cloud.features.abstract.*` 前缀之下。它与上文的 `FeaturesEndpoint` 断言无关，后者唯一的
  目的在于证明本模块位于 classpath 上时 Spring Cloud 自身的特性端点依然能够解析成功。

## 本版本线的构建与测试

反应堆通过继承自 `microsphere-spring-cloud-parent` `0.1.26` 的 profile 来锁定运行时代际，因此同一份源码会在三个 Spring
Cloud 代际上被验证：

| Profile | Spring Boot | Spring Cloud |
|---------|-------------|--------------|
| `spring-cloud-hoxton` | 2.3.12.RELEASE | Hoxton.SR12 |
| `spring-cloud-2020` | 2.4.13 | 2020.0.5 |
| `spring-cloud-2021`（默认） | 2.6.15 | 2021.0.9 |

CI 以 `mvn --activate-profiles test,coverage,<spring-cloud-profile>` 的形式，在 Java 8、11、17、21、25 上跑上述 profile
矩阵；模块没有覆写 `java.version`，因此不要往这些源码里引入仅 Java 17 可用的语法。

## 与 0.2.x 版本线的差异

`git diff origin/release origin/release-1.x -- microsphere-i18n-spring-cloud` 给出了全部差异：`pom.xml` 中 servlet-API
坐标的两行改动（`jakarta.servlet:jakarta.servlet-api` 变为 `javax.servlet:javax.servlet-api`），外加新增的
`spring.factories` 两行。`src/main/java` 与 `src/test/java` 下的每一个 `.java` 文件都完全相同。

| 方面 | `1.x`（`0.1.x`） | `0.2.x`（`current`） |
|--------|-----------------|---------------------|
| Java 源码（类、条件、Bean、监听器逻辑） | 未改动 | 未改动 |
| 注册文件 | `META-INF/spring.factories`（`EnableAutoConfiguration=...`）是 Boot 2.x 读取的那个；imports 文件为 2.7+ 保留 | 仅 imports 文件 |
| Servlet API 依赖 | `javax.servlet:javax.servlet-api`（optional） | `jakarta.servlet:jakarta.servlet-api`（optional） |
| 被测的 Boot/Cloud 代际 | Boot 2.3.12 / 2.4.13 / 2.6.15 搭配 Hoxton.SR12 / 2020.0.5 / 2021.0.9 | Boot 3.x 搭配 Cloud 2022.0.x - 2025.0.x |
| Java 目标版本 | 继承而来，未覆写；CI 覆盖 Java 8 - 25 | Java 17 |
| 版本属性 | 根 POM `<revision>0.1.22-SNAPSHOT</revision>`，最新发布 `0.1.21` | `<revision>` 为 `0.2.x` 线，最新发布 `0.2.21` |
