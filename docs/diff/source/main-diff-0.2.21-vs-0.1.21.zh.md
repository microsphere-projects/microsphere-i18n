# 源码差异（src/main）：0.2.21 vs 0.1.21

对比最新两个 tag 之间每个 Maven 模块的 `src/main` 内容：

| 项目 | 值 |
|------|-----|
| Tag（较新） | `0.2.21` —— `release` 分支线，0.2.x |
| Tag（较旧） | `0.1.21` —— `release-1.x` 分支线，0.1.x |
| 对比范围 | 仅匹配成功的 Maven 模块的 `*/src/main` |
| 排除规则 | 1）Java 17+ 与 Java 8 的语法/API 差异；2）Java EE（`javax.*`）与 Jakarta EE（`jakarta.*`）API 差异 |

## 模块匹配

两个 tag 声明的 8 个 Maven 模块完全一致，全部按同名配对比较，没有需要忽略的不匹配模块。
`microsphere-i18n-parent` 与 `microsphere-i18n-dependencies` 是纯 POM 构件，没有 `src` 目录，标记为“无源码可比较”。

## 总体结果

| Maven 模块 | src/main 文件数（0.1.21 → 0.2.21） | 排除规则后的实质差异 |
|------------|------------------------------------|----------------------|
| `microsphere-i18n-parent` | 无 `src` | —（纯 POM） |
| `microsphere-i18n-dependencies` | 无 `src` | —（纯 POM） |
| `microsphere-i18n-core` | 12 → 12 | 完全一致 |
| `microsphere-i18n-spring` | 15 → 15 | 完全一致（仅 Jakarta EE 命名空间替换，已排除） |
| `microsphere-i18n-openfeign` | 1 → 1 | 完全一致（仅 Jakarta EE 命名空间替换，已排除） |
| `microsphere-i18n-spring-boot` | 6 → 5 | 删除 `META-INF/spring.factories` |
| `microsphere-i18n-spring-cloud` | 5 → 4 | 删除 `META-INF/spring.factories` |
| `microsphere-i18n-spring-cloud-server` | 5 → 4 | 删除 `META-INF/spring.factories` |

## 逐模块实质差异

### `microsphere-i18n-spring-boot` / `microsphere-i18n-spring-cloud` / `microsphere-i18n-spring-cloud-server`

| **文件** | **变更类型** | **0.1.21** | **0.2.21** | **原因解释** |
|----------|--------------|------------|------------|--------------|
| 三个模块的 `src/main/resources/META-INF/spring.factories` | 删除 | 通过旧式 `org.springframework.boot.autoconfigure.EnableAutoConfiguration=` 键注册自动配置类，**同时**这些类已列在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中 | 删除该文件，`AutoConfiguration.imports` 保持不变 | Spring Boot 版本支持策略。0.1.x 线需兼容 Spring Boot 2.3–2.7，`spring.factories` 是该版本唯一的自动配置注册机制，因此两种注册文件同时保留；0.2.x 线仅面向 Spring Boot 3+，而 Boot 3.0 起不再读取 `spring.factories` 中的自动配置（改由 `AutoConfiguration.imports` 承担），该文件已成为无效配置。 |

两个 tag 注册的自动配置类完全相同（`I18nAutoConfiguration`、`I18nEndpointAutoConfiguration`、`I18nCloudAutoConfiguration`、`I18nServerAutoConfiguration`），因此这是打包层面的兼容性清理，对 Spring Boot 3 使用者没有任何行为差异。

## 已排除差异（备查）

以下原始 diff 按排除规则被过滤，此处列出以便追溯。

### Java EE → Jakarta EE 迁移（已排除）

| **模块** | **文件** | **变更内容** | **类别** |
|----------|----------|--------------|----------|
| `microsphere-i18n-openfeign` | `io/microsphere/i18n/feign/AcceptLanguageHeaderRequestInterceptor.java` | 导入 `javax.servlet.http.HttpServletRequest` → `jakarta.servlet.http.HttpServletRequest` | Java EE vs Jakarta EE |
| `microsphere-i18n-spring` | `io/microsphere/i18n/spring/validation/beanvalidation/I18nLocalValidatorFactoryBeanPostProcessor.java` | 类探测常量 `"javax.validation.ValidatorFactory"` → `"jakarta.validation.ValidatorFactory"` | Java EE vs Jakarta EE |

### Java 17+ 与 Java 8 语法/API 差异（已排除）

除上述命名空间替换外未发现其他差异 —— 排除 `javax` → `jakarta` 替换后，两个 tag 的 Java 源码完全相同，实际无需额外过滤 JDK 层面的语法/API 差异。

## 结论

两条发布线在源码层面已完全收敛：除 `javax`/`jakarta` 命名空间迁移外，`0.1.21` 与 `0.2.21` 之间唯一的真实 `src/main` 差异，是三个 Spring Boot 模块删除了面向 Spring Boot 2 的旧式 `spring.factories` 自动配置注册，其原因是 0.2.x 线不再支持 Spring Boot 2.x。
