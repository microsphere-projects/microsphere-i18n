# Release Notes

## v0.2.4

# Release Notes - Version 0.2.4

## New Features
- Added support for `spring-cloud-2025.1` through a new Maven profile. ([2e331a5](#))
- Introduced a workflow to sync fork branches from the upstream repository. ([46fc32d](#))
- Added Copilot-assisted release notes generation tool. ([c33ea18](#))

## Other Changes
- Updated Maven wrapper to version 3.9.15. ([b42cb1e](#))
- Updated README branch references to reflect the latest versions. ([00e4cce](#))
- Bumped `microsphere-spring-cloud` dependency to version 0.2.10. ([1a149c2](#))
- Minor documentation update to generalize script project references. ([f6ca50b](#))

---

For a full list of changes, refer to the commit history.

## v0.2.5

# Release Notes for v0.2.5

## Dependency Updates
- Bumped `microsphere-spring-cloud` to version `0.2.11`.

## Build and Workflow Enhancements
- Implemented matrix job for merging `main` into branches for streamlined workflows.
- Fixed indentation issues in `dependabot.yml`.
- Updated branch-specific latest versions in `README`.

## Other Changes
- Improved release notes and release creation process.
- Removed final newline in `generate-wiki-docs.py`.

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.4...0.2.5## v0.2.6

# Release Notes: Version 0.2.6

## Build and Workflow Enhancements
- Added OSSRH credentials to `maven-publish` workflow. ([6dcf8d0](https://github.com/microsphere-projects/microsphere-i18n/commit/6dcf8d0))
- Updated Maven CI workflows and fixed newline issues. ([1ee2439](https://github.com/microsphere-projects/microsphere-i18n/commit/1ee2439))
- Removed `testcontainers` profile from Maven build. ([7e9fabc](https://github.com/microsphere-projects/microsphere-i18n/commit/7e9fabc))

## Documentation
- Updated README with corrected branch names and versions. ([8f28f21](https://github.com/microsphere-projects/microsphere-i18n/commit/8f28f21))

## Dependency Updates
- Bumped microsphere Spring Cloud to version `0.2.12`. ([492e5ef](https://github.com/microsphere-projects/microsphere-i18n/commit/492e5ef))

## Other Changes
- Merged changes from `main` into `release` multiple times for synchronization. ([a2734bd](https://github.com/microsphere-projects/microsphere-i18n/commit/a2734bd), [7d555b6](https://github.com/microsphere-projects/microsphere-i18n/commit/7d555b6), [4edebd4](https://github.com/microsphere-projects/microsphere-i18n/commit/4edebd4), [fa79d29](https://github.com/microsphere-projects/microsphere-i18n/commit/fa79d29))
- Merged `release` into `main` after publishing version `0.2.5`. ([2885192](https://github.com/microsphere-projects/microsphere-i18n/commit/2885192))
- Bumped version to the next patch after publishing `0.2.5`. ([e7bda6a](https://github.com/microsphere-projects/microsphere-i18n/commit/e7bda6a))  

**[Full Changelog](https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.5...0.2.6)**  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.5...0.2.6## v0.2.7

# Release Notes - Version 0.2.7  

## New Features  
- **I18n Enhancements**: Registered `I18n` sources as `HasFeatures` for improved functionality.  

## Test Improvements  
- Updated tests to use `LoggingLevelsClass`.  
- Removed test-specific logback configuration file to streamline test setup.  

## Documentation  
- Updated version numbers in `README.md` for consistency with the latest release.  

## Other Changes  
- Maintenance merges from `main` into `release` and vice-versa.  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.6...0.2.7## v0.2.8

# Release Notes - Version 0.2.8

## New Features
- Introduced `.github` AI prompt templates for enhanced project guidance. [da82e88]

## Documentation
- Renamed `user-guide.md` to `developer-guide.md` and updated references accordingly. [7b8e067, 622e25b]
- Added a comprehensive `developer-guide.md` on code explanations. [f423335]
- Updated README:
  - Linked `developer-guide.md` in the documentation section. [9e21571]
  - Normalized markdown spacing and wrapping. [b683c7f]
  - Expanded README with comprehensive details. [73f3dd9]

## Dependency Updates
- Bumped `microsphere-spring-cloud` version to `0.2.13`. [4e2bec0]

## Other Changes
- Updated default configuration for the explain-code prompt. [6895881]  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.7...0.2.8## v0.2.9

# Release Notes - Version 0.2.9

## New Features
- **Spring Cloud Dependency Update:** Bumped Microsphere Spring Cloud version to `0.2.14` for enhanced compatibility. ([ca34a40](#))

## Documentation
- **README Improvements:** Updated README versions and fixed formatting issues for better clarity. ([e619c68](#))

## Bug Fixes
- **Code Cleanup:** Removed duplicated line separators and trailing whitespace for improved code cleanliness. ([035c5bd](#))

## Other Changes
- Routine merges from `main` to `release` and vice versa.  
- Version bumped post `0.2.8` release for patch development readiness. ([b491da2](#))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.8...0.2.9## v0.2.10

# Release Notes for v0.2.10

## Dependency Updates
- Bumped Microsphere Parent and BOM to `0.2.15`. ([a53db73](https://github.com/microsphere-projects/microsphere-i18n/commit/a53db73))

## Build and Workflow Enhancements
- Merged `main` branch into `release`. ([1e75ee8](https://github.com/microsphere-projects/microsphere-i18n/commit/1e75ee8))
- Merged `release` branch into `main`. ([1796009](https://github.com/microsphere-projects/microsphere-i18n/commit/1796009))
- Updated version for the next patch development after publishing `v0.2.9`. ([6afe653](https://github.com/microsphere-projects/microsphere-i18n/commit/6afe653))

## Other Changes
- Synced `main` branch. ([8bfda2d](https://github.com/microsphere-projects/microsphere-i18n/commit/8bfda2d)) 

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.9...0.2.10## v0.2.11

# Release Notes - Version 0.2.11

## Dependency Updates
- Bumped Microsphere Spring Cloud to `0.2.16`.

## Documentation
- Updated README versions to `0.2.11` and `0.1.11`.

## Build and Workflow Enhancements
- Prepared for new release by merging `main` into `release` and vice versa.  
- Incremented version to prepare for patch release after `0.2.10`.  

---  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.10...0.2.11## v0.2.12

# Release Notes: Version 0.2.12

## New Features
- Add **Understand-Anything** analysis artifacts. (b1a4c73)
- Introduce `ConditionalOnFeaturesAvailable` for feature-based configuration. (c5f6583)

## Dependency Updates
- Bump Microsphere Spring Cloud parent to `0.2.18`. (39bd6d2)
- Update parent/BOM to `0.2.17` in alignment with README updates. (972868d)

## Test Improvements
- Reorder annotations in test configuration for better organization. (e5a304f)

## Other Changes
- Version increment post-release. (8f40da3)
- Routine merges from `main` to `release`. [skip ci] (9a2673c, 7e96588, 4376b20, 4fba44c)
- Merge `release` back into `main`. [skip ci] (6569b75)

---

**Note:** For a full list of changes, refer to the complete changelog in the repository. 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.11...0.2.12## v0.2.13

# Release Notes for Version 0.2.13

## Dependency Updates
- Upgraded `microsphere-spring-cloud` dependency to `0.2.19`. (#65febc5)

## Documentation
- Updated `README` to reflect versions `0.2.13` and `0.1.13`. (#f89995a)
- Improved documentation and added missing `EmptyServiceMessageSource` import. (#1c46e8d)

## Code Improvements
- Made `FeaturesConfiguration` package-private for better encapsulation. (#46a587d)
- Switched to using `AnnotatedBeanCapableImportBeanDefinitionRegistrar` for improved annotation handling. (#7d08d0b)

## Other Changes
- Routine merges between `main` and `release` branches. [skip ci] (#bff3a1b, #b6ef048, #659e6ce, #67f79bc, #a0062a6)
- Bumped project version to prepare for the next patch after publishing `0.2.12`. (#24756e1)

---

Full Changelog: [0.2.12...0.2.13](URL_TO_CHANGELOG_IF_AVAILABLE)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.12...0.2.13## v0.2.14

# Release Notes - Version 0.2.14

## Build and Workflow Enhancements
- Aligned i18n auto-configuration with upcoming version 0.2.20 for consistency. (#903222d)
- Merged branch updates between `main` and `release` to streamline workflows. ([#8dc4407](skip ci), [#f2dc146](skip ci), [#c62cad5](skip ci))

## Documentation
- Updated README to reflect accurate version numbers in branch references. (#39f29b3)

## Other Changes
- Bumped version to prepare for the next patch release. (#82d9849)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.13...0.2.14## v0.2.15

# Release Notes for v0.2.15

## Dependency Updates
- Bumped `microsphere-spring-cloud` to `0.2.21`. [(c417abc)](https://github.com/user/repo/commit/c417abc)

## Documentation
- Updated README with latest branch versions. [(ce37980)](https://github.com/user/repo/commit/ce37980)

## Build and Workflow Enhancements
- Merged `main` into `release`. [(bd155ed)](https://github.com/user/repo/commit/bd155ed)
- Merged `release` into `main`. [(0ffe51a)](https://github.com/user/repo/commit/0ffe51a)
- Bumped version to the next patch after publishing `v0.2.14`. [(f7968a9)](https://github.com/user/repo/commit/f7968a9) 

---

**Note**: No new features or bug fixes in this release.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.14...0.2.15## v0.2.16

# Release Notes for Version 0.2.16

## Bug Fixes
- Corrected typo in `ConditionalOnI18nAvailable`. (#60b79b8)

## Documentation
- Updated README with the latest release versions. (#86563b7)

## Other Changes
- Prepared repository for versioning and publishing: 
  - Merged `main` into `release`. (#eafbd2c, #9548079)
  - Bumped version post 0.2.15 release. (#e8f2942) 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.15...0.2.16## v0.2.17

# Release Notes - Version 0.2.17

## New Features
- **Internationalization (i18n)**:  
  - Auto-configure Spring Cloud i18n server to streamline setup.  
  - Gate i18n server auto-configuration by endpoint availability.  
  - Introduced `ConditionalOnI18nAvailable` annotation for enhanced conditional auto-configuration.  

## Bug Fixes
- Fixed actuator auto-configuration registration issue.  

## Dependency Updates
- Bumped `microsphere-spring-cloud` to version `0.2.22`.  
- Refined Spring Boot, Spring Cloud, and OpenFeign POM dependencies.  

## Documentation
- Updated README with the latest version numbers.  

## Build and Workflow Enhancements
- Added annotation processors in core and server modules for improved development workflows.  
- Cleaned up unused Spring metadata files.  
- Tidy up test dependency management across modules.  

## Other Changes
- Marked several auto-configurations with `@Configuration` for better Spring context handling.  
- Enhanced i18n auto-config test setup for improved robustness.  

---

For more details, refer to the [Full Changelog](https://github.com/your-repo/compare/v0.2.16...v0.2.17).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.16...0.2.17