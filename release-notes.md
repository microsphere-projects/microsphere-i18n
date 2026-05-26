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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-i18n/compare/0.2.6...0.2.7