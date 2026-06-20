# Release Notes

## v0.1.5

# Release Notes - Version 0.1.5  

## Dependency Updates  
- Bumped `microsphere-spring-cloud` to version `0.1.11`.  

## Documentation  
- Updated `README` branch versions to `0.2.5`/`0.1.5`.  

## Build and Workflow Enhancements  
- Added CI release-notes generation and permissions.  
- Granted workflow `contents: read` permission.  
- Fixed `dependabot.yml` update list formatting.  

## Other Changes  
- Enhanced release notes and release creation process.  
- Merged `release-1.x` changes into `dev-1.x`.  

---  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.4...0.1.5## v0.1.6

# Release Notes - Version 0.1.6

## Build and Workflow Enhancements
- Added Maven credentials to CI workflow for improved build automation. ([5c72717](#))
- Adjusted Java setup and integrated Maven Wrapper in CI pipeline. ([8616a76](#))
- Updated `maven-build.yml` for better CI configuration. ([7e261c3](#))

## Dependency Updates
- Upgraded Microsphere parent to version `0.1.12`. ([dbf5f23](#))

## Documentation
- Updated README to reflect branch structure and versioning changes. ([155afde](#))

## Other Changes
- Merged release-1.x into dev-1.x branch for alignment. ([85f38fb](#))
- Bumped version to prepare for the next patch after `0.1.5`. ([2a359ad](#))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.5...0.1.6## v0.1.7

# Release Notes: Version 0.1.7

## New Features
- Added `logging-test` and `I18n cloud features` bean. ([3c07ed1](https://example.com/commit/3c07ed1))

## Test Improvements
- Removed test logback configuration. ([4e44a75](https://example.com/commit/4e44a75))

## Documentation
- Updated version numbers in `README.md`. ([ec9dfd3](https://example.com/commit/ec9dfd3))

## Build and Workflow Enhancements
- Merged `release-1.x` branch into `dev-1.x`. ([947b033](https://example.com/commit/947b033))
- Bumped version to the next patch after publishing v0.1.6. ([e70beb7](https://example.com/commit/e70beb7))

---

**Note:** This release includes incremental improvements and updates to prepare for future enhancements.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.6...0.1.7## v0.1.8

# Release Notes: Version 0.1.8

## New Features
- Set default inputs in the explain-code prompt to enhance usability. (#058b590)
- Add `.github` prompt templates for improved assistant task automation. (#f9f1a9e)

## Bug Fixes
- Fix formatting issues in README documentation lists. (#208edef)

## Documentation
- Revamp README with detailed documentation and examples. (#d343cb1)
- Add a developer guide and update README for clarity. (#a6fb703)

## Dependency Updates
- Bump `microsphere-spring-cloud` BOM to version 0.1.13. (#c3ff895)

## Build and Workflow Enhancements
- Merge `release-1.x` into `dev-1.x` to sync branches. (#df5dd1f)
- Bump version to prepare for patch release after publishing 0.1.7. (#d29c6d6)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.7...0.1.8## v0.1.9

# Release Notes - Version 0.1.9

## Dependency Updates
- **microsphere-spring-cloud:** Upgraded to version `0.1.14`. ([75b046f](#))

## Documentation
- Improved version references and formatting in `README.md`. ([66d00f0](#))

## Code Cleanup
- Removed duplicated line separators in code. ([0baad45](#))
- Cleaned up unnecessary whitespace in Java source files. ([faa4ab2](#))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` for branch consistency. ([dcf8cb3](#))
- Updated version to next patch after publishing `0.1.8`. ([c79c599](#))

--- 

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.8...0.1.9## v0.1.10

# Release Notes - Version 0.1.10

## Dependency Updates
- **Microsphere Spring Cloud**: Upgraded to version `0.1.15`. ([#52a0067](https://example.com))
- **Parent POM**: Bumped version to `0.1.15`. ([#b4a4bbe](https://example.com))

## Documentation
- Updated README versions to include details for `0.2.10` and `0.1.10`. ([#17d1569](https://example.com))

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x`. ([#e1aeb9e](https://example.com))
- Prepared codebase for next patch cycle post `0.1.9`. ([#4b36084](https://example.com))

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.9...0.1.10## v0.1.11

# Release Notes: Version 0.1.11

## Dependency Updates
- Bumped Microsphere Spring Cloud dependency to `0.1.16`. ([5e6c6dc](https://github.com/mercyblitz/microsphere-i18n/commit/5e6c6dc))

## Documentation
- Updated README to reflect the latest versions `0.2.11` and `0.1.11`. ([d991358](https://github.com/mercyblitz/microsphere-i18n/commit/d991358))

## Build and Workflow Enhancements
- Bumped version to next patch after publishing `0.1.10`. ([ce32e13](https://github.com/mercyblitz/microsphere-i18n/commit/ce32e13))
- Merged `release-1.x` into `dev-1.x`. ([a5eec3b](https://github.com/mercyblitz/microsphere-i18n/commit/a5eec3b))
- Synced `dev-1.x` with merged changes from the main repository. ([160d147](https://github.com/mercyblitz/microsphere-i18n/commit/160d147))

## Other Changes
- Various codebase synchronizations and internal improvements. ([b3200be](https://github.com/mercyblitz/microsphere-i18n/commit/b3200be)) 

---

For more details, view the [Full Changelog](https://github.com/mercyblitz/microsphere-i18n/compare/0.1.10...0.1.11).

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.10...0.1.11## v0.1.12

# Release Notes - Version 0.1.12

## New Features
- Introduced `@ConditionalOnFeaturesAvailable` for feature-based conditions. (#610475c)

## Bug Fixes
- Fixed issue by ensuring properties are loaded before `@EnableI18n` in test configuration. (#99e13fc)

## Dependency Updates
- Updated BOM and refactored `HasFeatures` test. (#5e022b8)
- Bumped versions of `microsphere-spring-cloud` and documentation dependencies. (#f2d8656)

## Test Improvements
- Enhanced test structure and refactored related configurations. (#5e022b8)

## Build and Workflow Enhancements
- Merged `release-1.x` into `dev-1.x` to sync branches. (#1228cc8)
- Incremented patch version post `0.1.11` release. (#45e267b)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.11...0.1.12## v0.1.13

# Release Notes - Version 0.1.13

## New Features
- Adopt `AnnotatedBeanCapableImportBeanDefinitionRegistrar` for enhanced bean registration. ([d87390b](https://link-to-commit))
- Import `EmptyServiceMessageSource` in `I18nUtils` to support improved internationalization. ([e379b80](https://link-to-commit))

## Bug Fixes
- Remove duplicate imports and add proper nullability annotations. ([6fe1edd](https://link-to-commit))

## Documentation
- Update README to include reference to latest versions. ([ca890dc](https://link-to-commit))
- Align Javadoc `@param` spacing for consistency. ([193ea1a](https://link-to-commit))

## Dependency Updates
- Upgrade `microsphere-spring-cloud` to version `0.1.19`. ([10eecca](https://link-to-commit))

## Test Improvements
- Reorder annotations in test config files for better readability. ([2787903](https://link-to-commit))

## Code Quality
- Reorder imports in `LocaleUtils` for better maintainability. ([cdc528d](https://link-to-commit))

---

Full Changelog: [v0.1.12...v0.1.13](https://link-to-changelog)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.1.12...0.1.13