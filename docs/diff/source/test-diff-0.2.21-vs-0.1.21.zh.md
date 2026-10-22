# 源码差异（src/test）：0.2.21 vs 0.1.21

对比最新两个 tag 之间每个 Maven 模块的 `src/test` 内容：

| 项目 | 值 |
|------|-----|
| Tag（较新） | `0.2.21` —— `release` 分支线，0.2.x |
| Tag（较旧） | `0.1.21` —— `release-1.x` 分支线，0.1.x |
| 对比范围 | 仅匹配成功的 Maven 模块的 `*/src/test` |
| 排除规则 | 1）Java 17+ 与 Java 8 的语法/API 差异；2）Java EE（`javax.*`）与 Jakarta EE（`jakarta.*`）API 差异 |

## 模块匹配

两个 tag 声明的 8 个 Maven 模块完全一致，全部按同名配对比较，没有需要忽略的不匹配模块。
`microsphere-i18n-parent` 与 `microsphere-i18n-dependencies` 是纯 POM 构件，没有 `src` 目录。

## 总体结果

| Maven 模块 | src/test 文件数（0.1.21） | src/test 文件数（0.2.21） | 差异 |
|------------|---------------------------|---------------------------|------|
| `microsphere-i18n-parent` | — | — | 无测试源码（纯 POM） |
| `microsphere-i18n-dependencies` | — | — | 无测试源码（纯 POM） |
| `microsphere-i18n-core` | 19 | 19 | 无 —— 逐字节一致 |
| `microsphere-i18n-spring` | 21 | 21 | 无 —— 逐字节一致 |
| `microsphere-i18n-openfeign` | 1 | 1 | 无 —— 逐字节一致 |
| `microsphere-i18n-spring-boot` | 10 | 10 | 无 —— 逐字节一致 |
| `microsphere-i18n-spring-cloud` | 7 | 7 | 无 —— 逐字节一致 |
| `microsphere-i18n-spring-cloud-server` | 5 | 5 | 无 —— 逐字节一致 |

## 逐模块实质差异

无。`git diff 0.1.21 0.2.21 -- '*/src/test'` 返回空变更集：两个 tag 之间的所有测试源码、测试资源与测试配置完全一致 —— 甚至没有任何原始 diff 需要动用排除规则过滤。

## 测试代码为何完全一致

两条发布线共享同一套测试，原因如下：

- 测试的断言与夹具只使用各模块自身的 API（`ServiceMessageSource`、自动配置、Actuator 端点等），这些契约在 0.1.x/0.2.x 两条发布线之间被有意保持一致。
- 生产代码上仅有的分歧（`javax` → `jakarta` 包名与 `spring.factories` 注册文件）对测试不可见：没有任何测试直接导入 Servlet/Validation 类型（已在两个 tag 的 `src/test` 中验证为零引用），且自动配置测试通过 Spring 的 `ApplicationContextRunner` 加载上下文，与各分支实际携带的注册文件机制均兼容。

## 结论

`0.1.21` 与 `0.2.21` 之间 `src/test` 100% 一致：无新增、删除或修改的测试文件，因此不存在需要排除或解释的差异。
