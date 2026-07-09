#!/usr/bin/env python3
"""Generate layers.json from the structural analysis results."""

import json

# Read the input to get all file node IDs
with open('.understand-anything/tmp/ua-arch-input.json') as f:
    input_data = json.load(f)

all_file_ids = set(fn['id'] for fn in input_data['fileNodes'])
all_file_count = len(all_file_ids)
print(f"Total file nodes: {all_file_count}")

# Read the structural analysis results
with open('.understand-anything/tmp/ua-arch-results.json') as f:
    results = json.load(f)

directory_groups = results['directoryGroups']

# ===========================================================================
# Layer definitions
# ===========================================================================
# 1. Core Framework - microsphere-i18n-core module
core_ids = directory_groups['microsphere-i18n-core']
print(f"Core: {len(core_ids)} files")

# 2. Spring Integration - microsphere-i18n-spring module
spring_ids = directory_groups['microsphere-i18n-spring']
print(f"Spring: {len(spring_ids)} files")

# 3. Spring Boot - microsphere-i18n-spring-boot module
spring_boot_ids = directory_groups['microsphere-i18n-spring-boot']
print(f"Spring Boot: {len(spring_boot_ids)} files")

# 4. Spring Cloud - microsphere-i18n-spring-cloud module
spring_cloud_ids = directory_groups['microsphere-i18n-spring-cloud']
print(f"Spring Cloud: {len(spring_cloud_ids)} files")

# 5. Spring Cloud Server - microsphere-i18n-spring-cloud-server module
spring_cloud_server_ids = directory_groups['microsphere-i18n-spring-cloud-server']
print(f"Spring Cloud Server: {len(spring_cloud_server_ids)} files")

# 6. OpenFeign - microsphere-i18n-openfeign module
openfeign_ids = directory_groups['microsphere-i18n-openfeign']
print(f"OpenFeign: {len(openfeign_ids)} files")

# 7. CI/CD - .github directory (workflows, steps, scripts, dependabot)
github_ids = directory_groups['.github']
print(f".github total: {len(github_ids)} files")
# Split into CI/CD (pipelines, steps, scripts, dependabot) vs Documentation (prompts)
ci_cd_ids = []
doc_prompt_ids = []
for nid in github_ids:
    if 'prompts' in nid:
        doc_prompt_ids.append(nid)
    else:
        ci_cd_ids.append(nid)
print(f"  CI/CD: {len(ci_cd_ids)}, Prompts: {len(doc_prompt_ids)}")

# 8. Project Configuration - root pom.xml, parent, dependencies, maven wrapper, mvnw scripts, .mvn, .understand-anything
project_config_ids = []
# root group files
root_ids = directory_groups['root']
print(f"Root total: {len(root_ids)} files")
doc_ids = []
for nid in root_ids:
    fn = next(f for f in input_data['fileNodes'] if f['id'] == nid)
    # Documents go to Documentation layer
    if fn['type'] == 'document':
        doc_ids.append(nid)
    else:
        project_config_ids.append(nid)
print(f"  Root config: {len(project_config_ids)}, Root docs: {len(doc_ids)}")

# .mvn wrapper
mvn_ids = directory_groups['.mvn']
project_config_ids.extend(mvn_ids)
print(f"  .mvn: {len(mvn_ids)}")

# microsphere-i18n-parent
parent_ids = directory_groups['microsphere-i18n-parent']
project_config_ids.extend(parent_ids)
print(f"  Parent POM: {len(parent_ids)}")

# microsphere-i18n-dependencies
deps_ids = directory_groups['microsphere-i18n-dependencies']
project_config_ids.extend(deps_ids)
print(f"  Dependencies POM: {len(deps_ids)}")

# .understand-anything
understand_ids = directory_groups['.understand-anything']
project_config_ids.extend(understand_ids)
print(f"  .understand-anything: {len(understand_ids)}")

print(f"Total Project Configuration: {len(project_config_ids)}")

# 9. Documentation
# Root docs (README, CODE_OF_CONDUCT, developer-guide, release-notes)
# + .github prompts
documentation_ids = doc_ids + doc_prompt_ids
print(f"Documentation: {len(documentation_ids)} files")

# ===========================================================================
# Verify no overlap and all files assigned
# ===========================================================================
all_assigned = set()
for id_list in [core_ids, spring_ids, spring_boot_ids, spring_cloud_ids,
                spring_cloud_server_ids, openfeign_ids, ci_cd_ids,
                project_config_ids, documentation_ids]:
    all_assigned.update(id_list)

missing = all_file_ids - all_assigned
extra = all_assigned - all_file_ids

if missing:
    print(f"ERROR: {len(missing)} files not assigned!")
    for m in sorted(missing):
        print(f"  MISSING: {m}")
if extra:
    print(f"ERROR: {len(extra)} extra files assigned!")
    for e in sorted(extra):
        print(f"  EXTRA: {e}")

print(f"Assigned: {len(all_assigned)} / {all_file_count}")

if not missing and not extra:
    print("All files accounted for!")
else:
    exit(1)

# ===========================================================================
# Build layers output
# ===========================================================================
layers = [
    {
        "id": "layer:core",
        "name": "Core Framework",
        "description": "Foundational i18n API and implementations including ServiceMessageSource interface, abstract base classes, resource-backed message sources, composite delegation, and utility classes for message resolution",
        "nodeIds": sorted(core_ids)
    },
    {
        "id": "layer:spring",
        "name": "Spring Integration",
        "description": "Spring framework integration layer providing IoC-aware message source adapters, bean factory support, annotation-driven enablement, locale resolution, and BeanValidation integration",
        "nodeIds": sorted(spring_ids)
    },
    {
        "id": "layer:spring-boot",
        "name": "Spring Boot",
        "description": "Spring Boot auto-configuration, conditional activation, actuator endpoint for runtime i18n message management, and auto-configuration registration via spring.factories",
        "nodeIds": sorted(spring_boot_ids)
    },
    {
        "id": "layer:spring-cloud",
        "name": "Spring Cloud",
        "description": "Spring Cloud integration for dynamic i18n message reloading on environment changes through EnvironmentChangeEvent listeners",
        "nodeIds": sorted(spring_cloud_ids)
    },
    {
        "id": "layer:spring-cloud-server",
        "name": "Spring Cloud Server",
        "description": "Spring Cloud REST server exposing i18n messages via HTTP endpoints for distributed service discovery and remote message source resolution",
        "nodeIds": sorted(spring_cloud_server_ids)
    },
    {
        "id": "layer:openfeign",
        "name": "OpenFeign",
        "description": "OpenFeign request interceptor that propagates Accept-Language HTTP headers from incoming requests to outgoing Feign client calls for consistent locale handling",
        "nodeIds": sorted(openfeign_ids)
    },
    {
        "id": "layer:ci-cd",
        "name": "CI/CD",
        "description": "GitHub Actions workflow definitions for Maven build, publishing, branch synchronization, wiki generation, and dependency management automation",
        "nodeIds": sorted(ci_cd_ids)
    },
    {
        "id": "layer:project-config",
        "name": "Project Configuration",
        "description": "Maven project build configuration including parent POM, dependency BOM, Maven wrapper scripts and properties, and internal analysis tool configuration",
        "nodeIds": sorted(project_config_ids)
    },
    {
        "id": "layer:documentation",
        "name": "Documentation",
        "description": "Project documentation including README, developer guide, code of conduct, release notes, and AI assistant prompts for code review and onboarding",
        "nodeIds": sorted(documentation_ids)
    }
]

# Verify total count
total_assigned = sum(len(layer['nodeIds']) for layer in layers)
print(f"\nTotal assigned in layers: {total_assigned}")
assert total_assigned == all_file_count, f"Count mismatch: {total_assigned} != {all_file_count}"

# Write output
output_path = '.understand-anything/intermediate/layers.json'
with open(output_path, 'w') as f:
    json.dump(layers, f, indent=2)

print(f"Written to {output_path}")
print(f"Number of layers: {len(layers)}")
for layer in layers:
    print(f"  {layer['id']}: {len(layer['nodeIds'])} files")
