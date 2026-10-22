# Source Diff (src/test): 0.2.21 vs 0.1.21

Comparison of the `src/test` content of every Maven module between the two latest tags:

| Item | Value |
|------|-------|
| Tag (newer) | `0.2.21` — `release` branch line, 0.2.x |
| Tag (older) | `0.1.21` — `release-1.x` branch line, 0.1.x |
| Compared scope | `*/src/test` of matched Maven modules only |
| Exclusions applied | 1) Java 17+ vs Java 8 syntax/API differences; 2) Java EE (`javax.*`) vs Jakarta EE (`jakarta.*`) API differences |

## Module matching

Both tags declare the same 8 Maven modules; all modules are compared pairwise and none had to be ignored as unmatched.
`microsphere-i18n-parent` and `microsphere-i18n-dependencies` are POM-only artifacts without a `src` directory.

## Overall result

| Maven module | src/test files (0.1.21) | src/test files (0.2.21) | Diff |
|--------------|-------------------------|-------------------------|------|
| `microsphere-i18n-parent` | — | — | no test sources (POM only) |
| `microsphere-i18n-dependencies` | — | — | no test sources (POM only) |
| `microsphere-i18n-core` | 19 | 19 | None — byte-for-byte identical |
| `microsphere-i18n-spring` | 21 | 21 | None — byte-for-byte identical |
| `microsphere-i18n-openfeign` | 1 | 1 | None — byte-for-byte identical |
| `microsphere-i18n-spring-boot` | 10 | 10 | None — byte-for-byte identical |
| `microsphere-i18n-spring-cloud` | 7 | 7 | None — byte-for-byte identical |
| `microsphere-i18n-spring-cloud-server` | 5 | 5 | None — byte-for-byte identical |

## Substantive differences (per module)

None. `git diff 0.1.21 0.2.21 -- '*/src/test'` returns an empty changeset: every test source, test resource, and test-scoped configuration file is identical between the two tags — no raw diff even needed the exclusion filters.

## Why the tests are identical

The two release lines share the same test suite because:

- Test code is compiled and run with the JDK/Spring stack available on each branch, but the assertions and fixtures only exercise module APIs (`ServiceMessageSource`, auto-configuration, endpoints), whose contracts are intentionally kept identical across the 0.1.x/0.2.x lines.
- The only production-code divergence (`javax` → `jakarta` package names and the `spring.factories` registration) is invisible to the tests: no test imports Servlet/Validation types directly, and auto-configuration tests load contexts through Spring's `ApplicationContextRunner`, which works with whichever registration file the branch ships.

## Conclusion

`src/test` is 100% identical between `0.1.21` and `0.2.21`: no added, removed, or modified test files, and therefore no differences requiring exclusion or explanation.
