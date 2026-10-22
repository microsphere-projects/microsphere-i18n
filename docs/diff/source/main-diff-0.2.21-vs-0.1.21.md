# Source Diff (src/main): 0.2.21 vs 0.1.21

Comparison of the `src/main` content of every Maven module between the two latest tags:

| Item | Value |
|------|-------|
| Tag (newer) | `0.2.21` — `release` branch line, 0.2.x |
| Tag (older) | `0.1.21` — `release-1.x` branch line, 0.1.x |
| Compared scope | `*/src/main` of matched Maven modules only |
| Exclusions applied | 1) Java 17+ vs Java 8 syntax/API differences; 2) Java EE (`javax.*`) vs Jakarta EE (`jakarta.*`) API differences |

## Module matching

Both tags declare the same 8 Maven modules, so all modules are compared pairwise; no unmatched module was ignored.
`microsphere-i18n-parent` and `microsphere-i18n-dependencies` are POM-only artifacts without a `src` directory and are therefore reported as "no source to compare".

## Overall result

| Maven module | src/main files (0.1.21 → 0.2.21) | Substantive differences (after exclusions) |
|--------------|----------------------------------|--------------------------------------------|
| `microsphere-i18n-parent` | no `src` | — (POM only) |
| `microsphere-i18n-dependencies` | no `src` | — (POM only) |
| `microsphere-i18n-core` | 12 → 12 | Identical |
| `microsphere-i18n-spring` | 15 → 15 | Identical (only Jakarta EE namespace change, excluded) |
| `microsphere-i18n-openfeign` | 1 → 1 | Identical (only Jakarta EE namespace change, excluded) |
| `microsphere-i18n-spring-boot` | 6 → 5 | `META-INF/spring.factories` removed |
| `microsphere-i18n-spring-cloud` | 5 → 4 | `META-INF/spring.factories` removed |
| `microsphere-i18n-spring-cloud-server` | 5 → 4 | `META-INF/spring.factories` removed |

## Substantive differences (per module)

### `microsphere-i18n-spring-boot` / `microsphere-i18n-spring-cloud` / `microsphere-i18n-spring-cloud-server`

| **File** | **Change type** | **0.1.21** | **0.2.21** | **Reason** |
|----------|-----------------|------------|------------|------------|
| `src/main/resources/META-INF/spring.factories` (all three modules) | Deleted | Registers auto-configurations via the legacy `org.springframework.boot.autoconfigure.EnableAutoConfiguration=` key, **in addition to** the same classes already listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` | File removed; `AutoConfiguration.imports` retained unchanged | Spring Boot version support. The 0.1.x line must support Spring Boot 2.3–2.7, where `spring.factories` is the only auto-configuration registration mechanism, so both registration files are shipped. The 0.2.x line targets Spring Boot 3+ only, where `spring.factories`-based auto-configuration is ignored (replaced by `AutoConfiguration.imports` since Boot 3.0), making the file dead configuration. |

The registered classes are byte-for-byte the same in both tags (`I18nAutoConfiguration`, `I18nEndpointAutoConfiguration`, `I18nCloudAutoConfiguration`, `I18nServerAutoConfiguration`), so this is a packaging/compatibility cleanup, not a behavior change for Spring Boot 3 consumers.

## Excluded differences (for transparency)

These raw diffs were filtered out per the stated rules but are listed for traceability.

### Java EE → Jakarta EE API migration (excluded)

| **Module** | **File** | **Change** | **Category** |
|------------|----------|------------|--------------|
| `microsphere-i18n-openfeign` | `io/microsphere/i18n/feign/AcceptLanguageHeaderRequestInterceptor.java` | Import `javax.servlet.http.HttpServletRequest` → `jakarta.servlet.http.HttpServletRequest` | Java EE vs Jakarta EE |
| `microsphere-i18n-spring` | `io/microsphere/i18n/spring/validation/beanvalidation/I18nLocalValidatorFactoryBeanPostProcessor.java` | Class-presence probe constant `"javax.validation.ValidatorFactory"` → `"jakarta.validation.ValidatorFactory"` | Java EE vs Jakarta EE |

### Java 17+ vs Java 8 syntax/API (excluded)

No additional differences found beyond the namespace changes above — after excluding the `javax` → `jakarta` substitutions, the Java sources of both tags are otherwise identical, so no JDK-level syntax/API filtering was needed in practice.

## Conclusion

The two release lines have fully converged at source level: apart from the `javax`/`jakarta` namespace migration, the only real `src/main` difference between `0.1.21` and `0.2.21` is the removal of the legacy Spring Boot 2 `spring.factories` auto-configuration registration from the three Spring Boot modules, driven by the 0.2.x line dropping Spring Boot 2.x support.
