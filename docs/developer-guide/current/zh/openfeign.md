# OpenFeign 模块 —— 开发者指南

模块：`microsphere-i18n-openfeign`
Java 包：`io.microsphere.i18n.feign`
规模：一个生产类 + 一个单元测试。

```
microsphere-i18n-openfeign/
├── pom.xml
└── src/
    ├── main/java/io/microsphere/i18n/feign/
    │   └── AcceptLanguageHeaderRequestInterceptor.java
    └── test/java/io/microsphere/i18n/feign/
        └── AcceptLanguageHeaderRequestInterceptorTest.java
```

模块没有 `src/main/resources`：不提供 `spring.factories`，不提供 `AutoConfiguration.imports`，
也不提供元数据文件。注册完全交由应用方完成。

## 1. 源码解析

### 类声明

```java
public class AcceptLanguageHeaderRequestInterceptor implements RequestInterceptor {

    private static final Logger logger = getLogger(AcceptLanguageHeaderRequestInterceptor.class);

    public static final String HEADER_NAME = ACCEPT_LANGUAGE;
```

值得注意的细节：

- `RequestInterceptor` 即 `feign.RequestInterceptor`，是 Feign 在构造请求过程中调用的 SPI。
- `HEADER_NAME` 通过静态导入复用 `org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE`，因此该常量
  必然与 Spring 自身使用的字面量 `Accept-Language` 一致。
- `Logger` 是 `io.microsphere.logging.Logger`，配合静态导入
  `io.microsphere.logging.LoggerFactory.getLogger(...)`——即 Microsphere 的日志门面，与框架其他模块
  保持一致，而不是直接使用 SLF4J 或 Logback。
- 类是无状态的：除静态 logger 外没有字段，因此单个实例可以被所有 Feign 客户端、所有线程共享。
- Javadoc 标注 `@since 1.0.0` 并 `@see AcceptHeaderLocaleResolver`，那只是文档层面的关联——本类从不
  实例化任何 locale resolver。

### `apply(RequestTemplate)` 逐段解析

```java
RequestAttributes requestAttributes = getRequestAttributes();
if (!(requestAttributes instanceof ServletRequestAttributes)) {
    ... return;
}
ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
HttpServletRequest request = servletRequestAttributes.getRequest();
String acceptLanguage = request.getHeader(HEADER_NAME);
if (hasText(acceptLanguage)) {
    template.header(HEADER_NAME, acceptLanguage);
    ...
} else {
    ...
}
```

| 步骤 | 代码                                                | 失败情形与行为                                                                        |
|------|-----------------------------------------------------|----------------------------------------------------------------------------------------|
| 1    | `RequestContextHolder.getRequestAttributes()`       | 不在请求线程上（定时任务、MQ 监听、裸线程）时返回 `null`，由下面的 `instanceof` 判断兜住。 |
| 2    | `instanceof ServletRequestAttributes`               | 同时过滤掉非 Servlet 的上下文绑定以及其它 `RequestAttributes` 实现。输出 TRACE 日志后静默 `return`。 |
| 3    | `servletRequestAttributes.getRequest()`             | 第 2 步不通过时根本不会执行到这里，因此这里不存在 `NullPointerException` 的可能。        |
| 4    | `request.getHeader(HEADER_NAME)`                    | 客户端未发送时返回 `null`；否则返回原始字符串，包含 `q=` 权重。                          |
| 5    | `StringUtils.hasText(acceptLanguage)`               | `null`、`""` 与纯空白值同样处理：不写入任何内容。                                       |
| 6    | `template.header(HEADER_NAME, acceptLanguage)`      | 原样复制值；不解析、不重排序、不做受支持语言环境过滤。                                  |

所有日志语句都包在 `logger.isTraceEnabled()` 判断里，因此在 DEBUG 及以上级别没有任何字符串格式化开销。
任何路径都不会抛异常：该拦截器最多是空操作，永远不会成为 Feign 调用失败的原因。

### 边界情形

- **入站请求没有该头。** 出站 Feign 请求也就不带 `Accept-Language`。接收方服务通过自身的
  `AcceptHeaderLocaleResolver` 解析语言环境，并回退到默认语言环境——该默认值由
  `microsphere-i18n-spring` 的 `AcceptHeaderLocaleResolverBeanPostProcessor` 配置。本拦截器**刻意
  不做** `LocaleContextHolder` 回退。
- **`Accept-Language: `（空或纯空白）。** `hasText` 判定不通过，结果与缺失一致。
- **非 MVC 请求线程。** 任何不是 `ServletRequestAttributes` 的 `RequestAttributes` 都会被忽略。这包括
  从未绑定过请求属性的线程上发起的异步 Feign 调用；只有子线程启动前用
  `setRequestAttributes(attributes, true)` 绑定了可继承线程局部变量，才能跨越线程边界。
- **Servlet 3.x 异步派发。** 拦截器读取的是 `apply` 执行那一刻绑定的请求；若请求已完成、属性已被重置，
  则该头被跳过。
- **多值情形。** `HttpServletRequest.getHeader` 返回第一行头部，因此客户端发送多行
  `Accept-Language` 时只转发第一行。

## 2. 请求头解析流程

```mermaid
flowchart TD
    A["Feign 构造 RequestTemplate"] --> B["AcceptLanguageHeaderRequestInterceptor.apply(template)"]
    B --> C["RequestContextHolder.getRequestAttributes()"]
    C --> D{"是否为 ServletRequestAttributes？"}
    D -- "否（null 或非 Servlet）" --> E["TRACE：非 Spring WebMVC 场景，忽略"]
    E --> Z["模板保持不变并返回"]
    D -- "是" --> F["getRequest() 取得 HttpServletRequest"]
    F --> G["request.getHeader('Accept-Language')"]
    G --> H{"StringUtils.hasText(value)？"}
    H -- "是" --> I["template.header('Accept-Language', value)"]
    I --> J["TRACE：Feign 已设置 HTTP 请求头"]
    H -- "否（null / 空 / 空白）" --> K["TRACE：请求方未传递该头"]
    J --> Y["出站 Feign 请求"]
    K --> Z
    Z --> Y
```

## 3. 依赖结构

来自 `microsphere-i18n-openfeign/pom.xml` 与该模块解析出的依赖树：

```mermaid
graph TD
    OF["microsphere-i18n-openfeign (jar)"]
    OF -->|"compile，必需"| CORE["microsphere-i18n-core"]
    OF -.->|"optional"| FEIGN["io.github.openfeign:feign-core 13.6.1"]
    OF -.->|"optional"| MVC["org.springframework:spring-webmvc 6.2.19"]
    OF -.->|"optional"| SERVLET["jakarta.servlet:jakarta.servlet-api 6.0.0"]
    OF -.->|"optional，构建期"| AP["microsphere-annotation-processor 0.3.19"]
    MVC --> WEB["org.springframework:spring-web 6.2.19（可选的传递依赖）"]
    MVC --> SPRINGCORE["org.springframework:spring-core 6.2.19（传递依赖，非可选）"]
    AP -.-> LM["microsphere-lang-model 0.3.19"]
    LM -.-> JCORE["microsphere-java-core 0.3.19 —— 提供 io.microsphere.logging"]
    CORE -.-> JCORE2["microsphere-java-core（core 中同样声明为 optional）"]
    TESTS["test 作用域：junit-jupiter 6.1.3、spring-test 6.2.19、microsphere-spring-test 0.2.39、microsphere-logback 0.2.2、logback-classic 1.5.34"] -.-> OF
```

对维护者而言的含义：

- 只有 `microsphere-i18n-core` 会传递给使用方。其余全部需要应用自己具备；这正是本模块针对
  `feign-core`、`spring-webmvc` 与 Servlet API 编译却不导出它们的原因。
- 上面的版本号**不写在本 POM 里**。它们来自 parent 链：`microsphere-i18n-parent` 导入
  `microsphere-spring-cloud-dependencies:0.2.26`，而后者自己的 parent 又导入
  `spring-boot-dependencies`，其版本由激活的代际 profile 决定。切换构建 profile，`spring-webmvc` 与
  `feign-core` 的版本会随之变化。
- `io.microsphere.logging` 之所以进入编译类路径，是通过可选的注解处理器链
  （`microsphere-annotation-processor` -> `microsphere-lang-model` -> `microsphere-java-core`），在
  测试期则由 `microsphere-spring-test` 提供。
- `spring-core` 是唯一被拉进来的非可选第三方构件，它作为可选 `spring-webmvc` 的传递依赖出现。

构建图位置：必须先构建 `microsphere-i18n-core`（`-pl microsphere-i18n-openfeign -am`）。

## 4. 设计说明

**拦截器 / 中介者模式。** 该类是一个 Feign `RequestInterceptor`，也就是插在 Feign 请求构建管线中的钩子，
而不是对客户端做装饰。它在两个原本互相看不见的对象之间充当中介：`HttpServletRequest`（入站，Servlet
世界）与 `RequestTemplate`（出站，Feign 世界）。因此本模块对 Bean 容器零耦合，可以手工 `new` 出来使用。

**复制而不是协商。** 拦截器转发的是一个头，不是一个 `Locale`。这个决定使模块不依赖
`ServiceMessageSource`，也不依赖任何配置属性；协商交由**下游**服务完成，因为只有下游才知道自己支持哪些
语言环境。同时，这让拦截器对完全不使用 microsphere-i18n 其他部分的应用同样正确。

**静默降级。** 所有不适用场景都是直接返回，既不写头也不抛异常。语言环境传递的失败绝不能演变成业务调用
的失败。

**不做自动配置。** `0.2.x` 版本线没有为该拦截器注册任何 Bean。自动装配一个全局的请求头写入者会改变应用
中所有 Feign 客户端的行为，这类决定应当留给使用者。（README 中“自动注册”的表述与代码并不一致——本模块
没有发布任何自动配置元数据，`microsphere-i18n-spring-boot` 与 `microsphere-i18n-spring-cloud` 中也没有
任何地方引用这个类。）

**无状态与线程安全。** 全部状态都从线程绑定的 `RequestContextHolder` 读取，因此单个共享 Bean 即可服务
所有客户端与所有线程。

## 5. 测试策略

`AcceptLanguageHeaderRequestInterceptorTest` 是纯粹的 JUnit 5 测试——不启动任何 Spring 上下文，这恰恰
是重点：该类只依赖 `RequestContextHolder` 就必须能工作。

```java
@SpringLoggingTest
class AcceptLanguageHeaderRequestInterceptorTest {
```

`@SpringLoggingTest`（来自测试作用域依赖 `microsphere-spring-test`）是项目级的统一约定，用于需要开启
框架日志的测试；`microsphere-i18n-spring` 与 `microsphere-i18n-spring-boot` 的测试同样使用该注解。

夹具（`@BeforeEach`）把 `MockHttpServletRequest` 绑定到当前线程，并准备一个全新的 `RequestTemplate`；
`@AfterEach` 调用 `resetRequestAttributes()`，保证线程局部变量不会泄漏到下一个测试。

| 测试方法                                | 前置布置                                              | 断言                                                             |
|-----------------------------------------|--------------------------------------------------------|--------------------------------------------------------------------|
| `testApply`                             | `request.addHeader(HEADER_NAME, "en")`                 | 模板头集合初始为空，之后 `Accept-Language` 的值为 `ofList("en")` |
| `testApplyWithoutAcceptLanguageHeader`  | 已绑定 Servlet 属性，但不添加头                        | `requestTemplate.headers()` 保持为空                             |
| `testApplyNoWebMvc`                     | 在 `apply` 之前调用 `resetRequestAttributes()`         | 传入一个**全新**的 `RequestTemplate`，仍然没有任何头              |

这三个用例与上文流程图的三条分支（命中、未命中、守卫）一一对应。运行方式：

```bash
# Linux / macOS
./mvnw -pl microsphere-i18n-openfeign -am test

# Windows
mvnw.cmd -pl microsphere-i18n-openfeign -am test
```

CI 会带着代际 profile 跑整个 Reactor，例如
`mvn test --activate-profiles test,coverage,spring-cloud-2025 -Drevision=0.0.1-SNAPSHOT`，因此本模块
会在每一对受支持的 Spring Boot / Spring Cloud 组合下被验证。

贡献者可以补齐的测试空白：

- 断言 TRACE 日志内容（项目其他地方使用了 `microsphere-logging-test`）；
- 覆盖纯空白的头（`Accept-Language:   `）；
- 覆盖带权重（`q=`）的入站头，以固化“原样转发”的行为；
- 覆盖非 Servlet 的 `RequestAttributes` 实现，而不仅仅是 `null`。

## 6. 扩展本模块

在本模块开展新工作时的约定：

1. 保持类无状态、不做任何 Spring Bean 查找；当前设计唯一的耦合点是 `RequestContextHolder`。
2. 若新的拦截器需要的是已解析的语言环境而非原始头，请从 `LocaleContextHolder.getLocale()` 或
   `ServiceMessageSource#getLocale()` 获取——并说明其回退链为何与本类不同（本类没有回退链）。
3. 新增的第三方依赖继续声明为 `optional`，让模块对外只导出 `microsphere-i18n-core`。
4. 自动配置一律放在 `microsphere-i18n-spring-boot`（该模块持有
   `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`），并且必须是有条件的，
   例如 `@ConditionalOnClass(RequestInterceptor.class)`，不要无条件装配。
5. 日志保持 TRACE 级别，并且包在 `isTraceEnabled()` 判断内。

## 7. 维护者排障表

| 现象                                                          | 可能原因                                                                     |
|----------------------------------------------------------------|-------------------------------------------------------------------------------|
| 下游始终使用默认语言环境                                       | 拦截器 Bean 未声明；或 Feign 调用发生在请求线程之外                          |
| `NoClassDefFoundError: feign/RequestInterceptor`               | 缺少 `feign-core`——该依赖是 optional，不会传递导出                          |
| `NoClassDefFoundError: jakarta/servlet/http/HttpServletRequest` | 缺少 Servlet API，或运行在 `javax.*` 容器（即 `1.x` 版本线）中            |
| 入站调用带有该头但 Feign 调用没有                              | `RequestContextHolder` 已被重置（异步派发、`@Async`、手工清理）             |
| `spring-webmvc` 版本不符合预期                                 | 构建使用了不同的 `spring-cloud-*` 代际 profile                              |

## 相关文档

- 用户指南：[OpenFeign](../../../user-guide/current/zh/openfeign.md)
- 开发者指南：[Core](core.md)、[Parent POM](parent.md)、[Dependencies BOM](dependencies.md)
