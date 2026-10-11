# Parent POM 模块 —— 开发者指南

模块：`microsphere-i18n-parent`
打包方式：`pom`
源码：无——该模块就是 `microsphere-i18n-parent/pom.xml`，别无一物。

## 1. 在 Maven Reactor 中的角色

根 POM（`microsphere-i18n`，`<packaging>pom</packaging>`）列出八个模块，它自身则是
`io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26` 的子 POM：

```
<modules>
    <module>microsphere-i18n-parent</module>       <!-- 1：必须最先构建 -->
    <module>microsphere-i18n-dependencies</module>
    <module>microsphere-i18n-core</module>
    <module>microsphere-i18n-openfeign</module>
    <module>microsphere-i18n-spring</module>
    <module>microsphere-i18n-spring-boot</module>
    <module>microsphere-i18n-spring-cloud</module>
    <module>microsphere-i18n-spring-cloud-server</module>
</modules>
```

`microsphere-i18n-parent` 排在第一位，因为其余七个模块——包括 `microsphere-i18n-dependencies` BOM
在内——都把它声明为自己的 `<parent>`，并带有
`<relativePath>../microsphere-i18n-parent/pom.xml</relativePath>`。Maven 必须先构建并安装该 parent，
继承它的模块才能解析到它；因此对单个模块执行 Reactor 构建时需要 `-am`（also-make）把它一并带上。

继承链与管理链是两条不同的边，两者都必须理解：

```mermaid
graph TD
    SCP["microsphere-spring-cloud-parent : 0.2.26（外部，上游）"] -->|"继承"| ROOT["microsphere-i18n（根聚合 POM，${revision}）"]
    ROOT -->|"继承（relativePath ../pom.xml）"| PARENT["microsphere-i18n-parent（pom）"]
    PARENT -->|"继承"| DEPS["microsphere-i18n-dependencies（BOM）"]
    PARENT -->|"继承"| CORE["microsphere-i18n-core"]
    PARENT -->|"继承"| FEIGN["microsphere-i18n-openfeign"]
    PARENT -->|"继承"| SPRING["microsphere-i18n-spring"]
    PARENT -->|"继承"| SB["microsphere-i18n-spring-boot"]
    PARENT -->|"继承"| SCLOUD["microsphere-i18n-spring-cloud"]
    PARENT -->|"继承"| SRV["microsphere-i18n-spring-cloud-server"]
    SCD["microsphere-spring-cloud-dependencies : 0.2.26（BOM）"] -.->|"在 microsphere-i18n-parent 中以 dependencyManagement import 引入"| PARENT
    SCD -->|"它自己的 parent"| SCP
```

```mermaid
graph LR
    P["microsphere-i18n-parent 属性：microsphere-spring-cloud.version = 0.2.26"]
    P --> I["dependencyManagement：import scope pom microsphere-spring-cloud-dependencies"]
    I --> B1["spring-boot-dependencies（${spring-boot.version} 由激活的 profile 决定）"]
    I --> B2["spring-cloud-dependencies（${spring-cloud.version}）"]
    I --> B3["spring-cloud-alibaba-dependencies（${spring-cloud-alibaba.version}）"]
    I --> B4["microsphere-spring-boot-dependencies : 0.2.34"]
    I --> B5["junit-bom : 6.1.3 / testcontainers-bom : 2.0.5"]
    B1 --> M["Reactor 中所有模块的第三方版本都在这里解析"]
    B2 --> M
    B3 --> M
    B4 --> M
    B5 --> M
```

## 2. Parent 声明了什么

`microsphere-i18n-parent/pom.xml` 的完整内容：

| 段落                   | 内容                                                                                                                          |
|------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `<parent>`             | `io.github.microsphere-projects:microsphere-i18n:${revision}`，带 `<relativePath>../pom.xml</relativePath>`                 |
| `<artifactId>`         | `microsphere-i18n-parent`，`<packaging>pom</packaging>`，`<version>${revision}</version>`                                   |
| `<properties>`         | 只有一条：`<microsphere-spring-cloud.version>0.2.26</microsphere-spring-cloud.version>`（注释为 “BOM versions”）          |
| `<dependencyManagement>` | 只有一条 `<scope>import</scope>` + `<type>pom</type>` 条目：`microsphere-spring-cloud-dependencies:${microsphere-spring-cloud.version}` |
| `<dependencies>`       | 无——每个模块各自声明                                                                                                       |
| `<build>` / `<profiles>` | 本仓库内没有定义，全部内容来自继承                                                                                          |

根 POM 提供了塑造构建的另外两个属性：

| 属性          | 取值              | 作用                                                                     |
|---------------|-------------------|---------------------------------------------------------------------------|
| `revision`    | `0.2.22-SNAPSHOT` | 所有模块的 CI 友好版本；由 CI 与发布任务在命令行覆盖                    |
| `java.version`| `17`              | 该版本线的 Java 级别；`0.2.x` 线使用 `jakarta.*` / Spring Boot 3        |

## 3. 版本 Profile 与 Spring Cloud 代际锁定

一个必须说清楚的事实：**本仓库中没有任何 POM 定义 `<profiles>` 块。** 代际锁定是通过根 POM 自身的
`<parent>` 从上游 `microsphere-spring-cloud-parent:0.2.26` 继承而来的。该 POM 导入
`spring-boot-dependencies:${spring-boot.version}`、
`spring-cloud-dependencies:${spring-cloud.version}` 与
`spring-cloud-alibaba-dependencies:${spring-cloud-alibaba.version}`，并为每一代定义一个 profile：

| Profile ID            | `spring-boot.version` | `spring-cloud.version` | `spring-cloud-alibaba.version` | 激活方式                     |
|-----------------------|-----------------------|------------------------|--------------------------------|------------------------------|
| `spring-cloud-2022`   | `3.0.13`              | `2022.0.5`             | `2022.0.0.2`                   | 需显式 `-P`                  |
| `spring-cloud-2023`   | `3.3.13`              | `2023.0.6`             | `2023.0.3.4`                   | 需显式 `-P`                  |
| `spring-cloud-2024`   | `3.4.13`              | `2024.0.3`             | `2023.0.3.4`                   | 需显式 `-P`                  |
| `spring-cloud-2025`   | `3.5.15`              | `2025.0.3`             | `2025.0.0.0`                   | `activeByDefault`（隐式）    |
| `spring-cloud-2025.1` | `4.0.8`               | `2025.1.3`             | `2025.1.0.0`                   | 需显式 `-P`                  |

实际影响：

- 不加 `-P` 构建 Reactor 时，编译目标是 Spring Boot 3.5.x / Spring Cloud 2025.0.x，此时
  `microsphere-i18n-openfeign` 解析到 `spring-webmvc:6.2.19` 与 `feign-core:13.6.1`。
- 加上 `-Pspring-cloud-2022`，同一份源码就改到 Boot 3.0 / Spring Cloud 2022 上编译，这正是 README 中
  “支持 2022.0.x – 2025.0.x” 说法的实现机制。
- `spring-cloud-2025.1` 对应 Spring Boot 4.x，且**不在**本项目的 CI 矩阵里，因此对 microsphere-i18n
  而言它不是被验证过的组合。

构建矩阵位于 `.github/workflows/maven-build.yml`：

```yaml
strategy:
  matrix:
    java: [ '17' , '21' , '25' ]
    maven-profile-spring-cloud: [ 'spring-cloud-2022' , 'spring-cloud-2023' ,
                                  'spring-cloud-2024' , 'spring-cloud-2025' ]
```

它执行的命令：

```bash
mvn --batch-mode --update-snapshots --file pom.xml \
    -Drevision=0.0.1-SNAPSHOT test \
    --activate-profiles test,coverage,${{ matrix.maven-profile-spring-cloud }}
```

其中 `test` 与 `coverage` 这两个 profile 由继承来的构建配置提供，并非本仓库的 POM 所定义。

## 4. `${revision}` 与发布出去的 POM

每个模块的版本都写作 `${revision}`。由于下游项目无法直接使用 `${revision}`，构建在安装与发布时会使用
**flatten 后的** POM：每个模块目录下都会生成 `.flattened-pom.xml`，进入仓库的 POM 携带的是字面量版本。
例如，已发布的 `microsphere-i18n-dependencies:0.2.21` POM 中，六个被管理构件以及它的 parent 都写着
`0.2.21`，不再残留 `${revision}`；消费者看到的 `microsphere-i18n-parent` POM 同样带有字面量
`<microsphere-spring-cloud.version>0.2.26</microsphere-spring-cloud.version>` 与 `0.2.26` 的导入。

这就是为什么调整 `microsphere-spring-cloud.version` 属于源码级变更、必须发版才能生效——消费者无法用
属性把它改掉。

发布时 `revision` 的值究竟从哪里来？不是来自 POM：发布任务在命令行上传入 `-Drevision=<x.y.z>`（见
`.github/workflows/maven-publish.yml`，它会先校验版本是否符合 `major.minor.patch` 形式），紧接着的
步骤会把根 POM 中的 `<revision>` 改写为下一个补丁版本并推送，也就是提交历史里那条
`chore: bump version to next patch after publishing 0.2.21`。因此 POM 里写的 `revision` 永远是
*下一个* 开发版本（当前工作树中为 `0.2.22-SNAPSHOT`），而不是仓库中已发布的那个版本。

## 5. 维护流程

**升级到新的上游 Spring Cloud 版本列车**

1. 修改 `microsphere-i18n-parent/pom.xml` 中的 `<microsphere-spring-cloud.version>`（一行）。
2. 用矩阵支持的所有代际 profile 完整构建一次 Reactor。
3. 修复各模块的 API 适配问题——parent 本身除这一行外不应有其他改动。

**新增一个 Spring Cloud 代际支持**

如果上游缺少所需的 profile，必须先在 `microsphere-spring-cloud-parent` 中添加；本仓库只能激活继承来的
profile。上游发布后，把新的 profile ID 加入 `.github/workflows/maven-build.yml` 的矩阵。

**新增一个模块**

```xml

<project>
    <parent>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-i18n-parent</artifactId>
        <version>${revision}</version>
        <relativePath>../microsphere-i18n-parent/pom.xml</relativePath>
    </parent>
    <artifactId>microsphere-i18n-example</artifactId>
    <packaging>jar</packaging>
</project>
```

随后必须同时把它登记到根 POM 的 `<modules>` 列表**和** `microsphere-i18n-dependencies` 的
`dependencyManagement` 中，否则该构件会以“无人管理”的状态发布。

**查看生效的版本管理**

```bash
# parent 贡献了什么？
./mvnw -pl microsphere-i18n-parent help:effective-pom

# 某个模块解析到的第三方版本是多少？
./mvnw -pl microsphere-i18n-openfeign dependency:tree

# 换成较旧的代际再看一次
./mvnw -Pspring-cloud-2022 -pl microsphere-i18n-openfeign dependency:tree
```

## 6. 设计说明

- **两个 POM 各司其职。** `microsphere-i18n-parent` 回答“我拿什么编译”（继承：插件、BOM 导入）；
  `microsphere-i18n-dependencies` 回答“我用哪个 i18n 版本”（可导入的版本管理）。二者分离，使应用可以只
  导入 BOM 而不继承构建插件。
- **parent 链通向外部项目。** Reactor 继承自 `microsphere-spring-cloud` 而不是
  `spring-boot-starter-parent`，因此 Microsphere 可以在一个上游 POM 中统一掌控整个矩阵（Boot + Cloud +
  Alibaba + Microsphere 自身构件），让所有 Microsphere 项目保持相互一致。
- **看似环状的结构是有意为之。** 根聚合 POM 是 `microsphere-i18n-parent` 的 parent，而后者又是所有叶子
  模块以及 BOM 的 parent。Maven 允许这样，因为“聚合”与“继承”两种角色是分别解析的；Reactor 构建顺序仍然
  把 `microsphere-i18n-parent` 放在最前。
- **面积最小化。** parent 中只有一个属性和一个 import；写在这里的任何东西都会被放大到八个模块，因此新增的
  版本管理应放到上游 BOM，或者放到 `microsphere-i18n-dependencies` 里。

## 相关文档

- 用户指南：[Parent POM](../../../user-guide/current/zh/parent.md)
- 开发者指南：[Dependencies BOM](dependencies.md)、[OpenFeign](openfeign.md)
