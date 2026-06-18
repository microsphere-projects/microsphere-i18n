#!/usr/bin/env node
"use strict";

const fs = require("fs");

if (process.argv.length < 4) {
  console.error("Usage: node ua-arch-analyze.js <input.json> <output.json>");
  process.exit(1);
}

const inputPath = process.argv[2];
const outputPath = process.argv[3];

let input;
try {
  input = JSON.parse(fs.readFileSync(inputPath, "utf-8"));
} catch (e) {
  console.error("Failed to read input JSON:", e.message);
  process.exit(1);
}

const { fileNodes, importEdges, allEdges } = input;

// ---------------------------------------------------------------------------
// A. Directory Grouping
// ---------------------------------------------------------------------------
// Compute common path prefix
function commonPathPrefix(paths) {
  if (paths.length === 0) return "";
  const parts = paths.map((p) => p.split("/"));
  let prefix = parts[0].slice(0, -1);
  for (const p of parts.slice(1)) {
    let i = 0;
    while (i < prefix.length && i < p.length && prefix[i] === p[i]) i++;
    prefix = prefix.slice(0, i);
  }
  return prefix.join("/") + (prefix.length > 0 ? "/" : "");
}

// Get all file paths for file-type nodes
const filePaths = fileNodes
  .filter((n) => n.type === "file")
  .map((n) => n.filePath || n.id.replace(/^file:/, ""));

const prefix = commonPathPrefix(filePaths);
function getGroup(filePath) {
  if (!filePath) return "root";
  // Strip common prefix
  let rel = filePath.startsWith(prefix) ? filePath.slice(prefix.length) : filePath;
  rel = rel.replace(/^\/+/, "");
  const parts = rel.split("/");
  if (parts.length === 0 || (parts.length === 1 && parts[0] === "")) return "root";
  // Check if it's a root-level file (no subdirectory)
  if (parts.length === 1) return "root";
  return parts[0];
}

const directoryGroups = {};
for (const fn of fileNodes) {
  const path = fn.filePath || "";
  const grp = getGroup(path);
  if (!directoryGroups[grp]) directoryGroups[grp] = [];
  directoryGroups[grp].push(fn.id);
}

// ---------------------------------------------------------------------------
// B. Node Type Grouping
// ---------------------------------------------------------------------------
const nodeTypeGroups = {};
for (const fn of fileNodes) {
  const t = fn.type;
  if (!nodeTypeGroups[t]) nodeTypeGroups[t] = [];
  nodeTypeGroups[t].push(fn.id);
}

// ---------------------------------------------------------------------------
// C. Import Adjacency Matrix
// ---------------------------------------------------------------------------
// Build fan-in/fan-out for each file
const fanOut = {};
const fanIn = {};
const importSet = new Set();
for (const e of importEdges) {
  const src = e.source;
  const tgt = e.target;
  fanOut[src] = (fanOut[src] || 0) + 1;
  fanIn[tgt] = (fanIn[tgt] || 0) + 1;
  importSet.add(JSON.stringify({ source: src, target: tgt }));
}

// Per-file fan-in and fan-out (only files that have imports)
const fileFanIn = {};
const fileFanOut = {};
for (const fn of fileNodes) {
  const id = fn.id;
  if (fanIn[id]) fileFanIn[id] = fanIn[id];
  if (fanOut[id]) fileFanOut[id] = fanOut[id];
}

// Build mapping from file ID to group
const idToGroup = {};
for (const fn of fileNodes) {
  idToGroup[fn.id] = getGroup(fn.filePath || "");
}

// Per-group: which groups it imports from and is imported by
const groupImportsFrom = {};
const groupImportedBy = {};
for (const e of importEdges) {
  const srcGroup = idToGroup[e.source] || "root";
  const tgtGroup = idToGroup[e.target] || "root";
  if (srcGroup !== tgtGroup) {
    if (!groupImportsFrom[srcGroup]) groupImportsFrom[srcGroup] = new Set();
    groupImportsFrom[srcGroup].add(tgtGroup);
    if (!groupImportedBy[tgtGroup]) groupImportedBy[tgtGroup] = new Set();
    groupImportedBy[tgtGroup].add(srcGroup);
  }
}
const groupImportsFromArr = {};
const groupImportedByArr = {};
for (const [g, s] of Object.entries(groupImportsFrom)) groupImportsFromArr[g] = [...s];
for (const [g, s] of Object.entries(groupImportedBy)) groupImportedByArr[g] = [...s];

// ---------------------------------------------------------------------------
// D. Cross-Category Dependency Analysis
// ---------------------------------------------------------------------------
const crossCategoryEdges = [];
const crossCatCounts = {};
for (const e of allEdges) {
  const srcNode = fileNodes.find((n) => n.id === e.source);
  const tgtNode = fileNodes.find((n) => n.id === e.target);
  if (srcNode && tgtNode && srcNode.type !== tgtNode.type) {
    const key = `${srcNode.type}->${tgtNode.type}:${e.type}`;
    crossCatCounts[key] = (crossCatCounts[key] || 0) + 1;
  }
}
for (const [key, count] of Object.entries(crossCatCounts)) {
  const [fromTo, edgeType] = key.split(":");
  const [fromType, toType] = fromTo.split("->");
  crossCategoryEdges.push({ fromType, toType, edgeType, count });
}

// ---------------------------------------------------------------------------
// E. Inter-Group Import Frequency
// ---------------------------------------------------------------------------
const interGroupImports = [];
const interGroupCounts = {};
for (const e of importEdges) {
  const srcGroup = idToGroup[e.source] || "root";
  const tgtGroup = idToGroup[e.target] || "root";
  if (srcGroup !== tgtGroup) {
    const key = `${srcGroup}->${tgtGroup}`;
    interGroupCounts[key] = (interGroupCounts[key] || 0) + 1;
  }
}
for (const [key, count] of Object.entries(interGroupCounts)) {
  const [from, to] = key.split("->");
  interGroupImports.push({ from, to, count });
}

// ---------------------------------------------------------------------------
// F. Intra-Group Import Density
// ---------------------------------------------------------------------------
const intraGroupDensity = {};
for (const [grp, ids] of Object.entries(directoryGroups)) {
  let internalEdges = 0;
  let totalEdges = 0;
  for (const e of importEdges) {
    const sg = idToGroup[e.source] || "root";
    const tg = idToGroup[e.target] || "root";
    if (sg === grp || tg === grp) {
      totalEdges++;
      if (sg === grp && tg === grp) internalEdges++;
    }
  }
  intraGroupDensity[grp] = {
    internalEdges,
    totalEdges,
    density: totalEdges > 0 ? internalEdges / totalEdges : 0,
  };
}

// ---------------------------------------------------------------------------
// G. Directory Pattern Matching
// ---------------------------------------------------------------------------
const dirPatterns = {
  routes: "api",
  api: "api",
  controllers: "api",
  endpoints: "api",
  handlers: "api",
  services: "service",
  core: "service",
  domain: "service",
  logic: "service",
  models: "data",
  db: "data",
  data: "data",
  persistence: "data",
  repository: "data",
  entities: "data",
  components: "ui",
  views: "ui",
  pages: "ui",
  ui: "ui",
  middleware: "middleware",
  plugins: "middleware",
  utils: "utility",
  helpers: "utility",
  common: "utility",
  shared: "utility",
  tools: "utility",
  config: "config",
  constants: "config",
  env: "config",
  settings: "config",
  test: "test",
  tests: "test",
  __tests__: "test",
  spec: "test",
  types: "types",
  interfaces: "types",
  schemas: "types",
  contracts: "types",
  dtos: "types",
  hooks: "hooks",
  store: "state",
  state: "state",
  assets: "assets",
  static: "assets",
  public: "assets",
  migrations: "data",
  management: "config",
  commands: "config",
  cmd: "entry",
  internal: "service",
  pkg: "utility",
  entity: "data",
  controller: "api",
  routers: "api",
  composables: "service",
  blueprints: "api",
  bin: "entry",
  docs: "documentation",
  documentation: "documentation",
  deploy: "infrastructure",
  deployment: "infrastructure",
  infra: "infrastructure",
  infrastructure: "infrastructure",
  ".github": "ci-cd",
  github: "ci-cd",
  workflows: "ci-cd",
  k8s: "infrastructure",
  kubernetes: "infrastructure",
  terraform: "infrastructure",
  docker: "infrastructure",
  sql: "data",
  database: "data",
  schema: "data",
  autoconfigure: "config",
  annotation: "config",
  condition: "config",
  validation: "service",
  servlet: "api",
  context: "service",
  event: "service",
  integration: "service",
  feign: "api",
  actuator: "api",
  bootstrap: "entry",
};

function classifyDirName(name) {
  return dirPatterns[name] || null;
}

// Check file-level patterns
const filePatterns = {
  // Test files
  test: (fn) => {
    const name = fn.name || "";
    const path = fn.filePath || "";
    return /\.test\./.test(name) || /\.spec\./.test(name) || /Test\.java$/.test(name) || /Tests\.java$/.test(name) || /_test\.py$/.test(name) || path.includes("/test/") || path.includes("/tests/") || path.startsWith("src/test/");
  },
  // TypeScript declaration files
  types: (fn) => {
    return /\.d\.ts$/.test(fn.name || "");
  },
  // Entry points
  entry: (fn) => {
    const name = fn.name || "";
    const path = fn.filePath || "";
    if (name === "index.ts" || name === "index.js" || name === "__init__.py") return true;
    if (name === "manage.py") return true;
    if (name === "wsgi.py" || name === "asgi.py") return true;
    if (/main\.go$/.test(name) && path.includes("/cmd/")) return true;
    if (name === "main.rs" || name === "lib.rs") return true;
    if (/Application\.java$/.test(name) || /Program\.cs$/.test(name)) return true;
    if (name === "config.ru") return true;
    if (/Bootstrap\.java$/.test(name)) return true;
    return false;
  },
  // Config files
  config: (fn) => {
    const name = fn.name || "";
    if (/Cargo\.toml$/.test(name) || name === "go.mod" || name === "Gemfile" || /pom\.xml$/.test(name) || /build\.gradle$/.test(name) || name === "composer.json") return true;
    return false;
  },
  // Infrastructure files
  infrastructure: (fn) => {
    const name = fn.name || "";
    if (/^Dockerfile/.test(name) || /docker-compose/.test(name)) return true;
    if (/\.tf$/.test(name) || /\.tfvars$/.test(name)) return true;
    if (name === "Makefile") return true;
    return false;
  },
  // CI-CD
  "ci-cd": (fn) => {
    const name = fn.name || "";
    if (/\.github\/workflows/.test(fn.filePath || "")) return true;
    if (name === ".gitlab-ci.yml" || name === "Jenkinsfile") return true;
    return false;
  },
  // Data files
  data: (fn) => {
    const name = fn.name || "";
    if (/\.sql$/.test(name)) return true;
    return false;
  },
  // Types
  types: (fn) => {
    const name = fn.name || "";
    if (/\.graphql$/.test(name) || /\.gql$/.test(name) || /\.proto$/.test(name)) return true;
    return false;
  },
  // Documentation
  documentation: (fn) => {
    const name = fn.name || "";
    if (/\.md$/.test(name) || /\.rst$/.test(name)) return true;
    return false;
  },
};

const patternMatches = {};
for (const [grp, ids] of Object.entries(directoryGroups)) {
  // Check directory name pattern first
  const dirLabel = classifyDirName(grp);
  if (dirLabel) {
    patternMatches[grp] = dirLabel;
    continue;
  }
  // Check file-level patterns for files in this group
  for (const id of ids) {
    const fn = fileNodes.find((n) => n.id === id);
    if (!fn) continue;
    // Check test pattern
    if (filePatterns.test(fn)) {
      patternMatches[grp] = "test";
      break;
    }
    if (filePatterns.entry(fn)) {
      patternMatches[grp] = "entry";
      break;
    }
    if (filePatterns.documentation(fn)) {
      patternMatches[grp] = "documentation";
      break;
    }
    if (filePatterns.infrastructure(fn)) {
      patternMatches[grp] = "infrastructure";
      break;
    }
    if (fn.type === "config") {
      patternMatches[grp] = "config";
      break;
    }
  }
}

// Special handling: add pattern matches for special directories
for (const [grp, ids] of Object.entries(directoryGroups)) {
  if (patternMatches[grp]) continue;
  // Check all file paths in group
  const paths = ids.map((id) => {
    const fn = fileNodes.find((n) => n.id === id);
    return fn ? fn.filePath || "" : "";
  });
  // If all files are test files
  const allTest = ids.every((id) => {
    const fn = fileNodes.find((n) => n.id === id);
    return fn && filePatterns.test(fn);
  });
  if (allTest) {
    patternMatches[grp] = "test";
    continue;
  }
  // If all files are config
  const allDoc = ids.every((id) => {
    const fn = fileNodes.find((n) => n.id === id);
    return fn && fn.type === "document";
  });
  if (allDoc) {
    patternMatches[grp] = "documentation";
    continue;
  }
  // Default: try to infer from the subdirectory structure
  // Check for Maven module patterns
  if (grp.startsWith("microsphere-i18n-")) {
    const suffix = grp.replace("microsphere-i18n-", "");
    if (suffix === "core" || suffix === "spring" || suffix === "spring-boot" || suffix === "spring-cloud" || suffix === "spring-cloud-server" || suffix === "openfeign") {
      patternMatches[grp] = "service";
    } else if (suffix === "parent" || suffix === "dependencies") {
      patternMatches[grp] = "config";
    }
  }
}

// ---------------------------------------------------------------------------
// H. Deployment Topology Detection
// ---------------------------------------------------------------------------
const deploymentTopology = {
  hasDockerfile: false,
  hasCompose: false,
  hasK8s: false,
  hasTerraform: false,
  hasCI: false,
  infraFiles: [],
};

for (const fn of fileNodes) {
  const name = fn.name || "";
  const path = fn.filePath || "";
  if (/^Dockerfile/.test(name)) {
    deploymentTopology.hasDockerfile = true;
    deploymentTopology.infraFiles.push(path);
  }
  if (/docker-compose/.test(name)) {
    deploymentTopology.hasCompose = true;
    deploymentTopology.infraFiles.push(path);
  }
  if (path.includes(".github/workflows") || name === ".gitlab-ci.yml" || name === "Jenkinsfile") {
    deploymentTopology.hasCI = true;
    deploymentTopology.infraFiles.push(path);
  }
  if (/\.tf$/.test(name)) {
    deploymentTopology.hasTerraform = true;
    deploymentTopology.infraFiles.push(path);
  }
}

// ---------------------------------------------------------------------------
// I. Data Pipeline Detection
// ---------------------------------------------------------------------------
const dataPipeline = {
  schemaFiles: [],
  migrationFiles: [],
  dataModelFiles: [],
  apiHandlerFiles: [],
};

for (const fn of fileNodes) {
  const path = fn.filePath || "";
  const name = fn.name || "";
  if (/\.sql$/.test(name)) dataPipeline.schemaFiles.push(path);
  if (path.includes("migration")) dataPipeline.migrationFiles.push(path);
  if (path.includes("/model") || path.includes("/entity") || path.includes("/entities")) dataPipeline.dataModelFiles.push(path);
  if (path.includes("/controller") || path.includes("/endpoint") || path.includes("/routes") || path.includes("/api") || (path.includes("/feign/"))) dataPipeline.apiHandlerFiles.push(path);
}

// ---------------------------------------------------------------------------
// J. Documentation Coverage
// ---------------------------------------------------------------------------
const groupsWithDocs = new Set();
const allGroups = Object.keys(directoryGroups);
for (const fn of fileNodes) {
  const path = fn.filePath || "";
  const name = fn.name || "";
  if (fn.type === "document" || /\.md$/.test(name)) {
    // Check if this doc is in a specific group or references it
    const docGroup = getGroup(path);
    if (docGroup && docGroup !== "root") {
      groupsWithDocs.add(docGroup);
    }
    // Also check docs directory
    if (path.startsWith("docs/") || path.includes("/docs/")) {
      groupsWithDocs.add(path.split("/").filter((s) => s !== "docs" && s !== "documentation")[0] || "docs");
    }
  }
}
// Check for README files within groups
for (const grp of allGroups) {
  const ids = directoryGroups[grp];
  for (const id of ids) {
    const fn = fileNodes.find((n) => n.id === id);
    if (fn && (fn.name === "README.md" || fn.filePath?.includes("/README.md"))) {
      groupsWithDocs.add(grp);
    }
  }
}

const undocumentedGroups = allGroups.filter((g) => !groupsWithDocs.has(g) && g !== "root");
const docCoverage = {
  groupsWithDocs: groupsWithDocs.size,
  totalGroups: allGroups.length,
  coverageRatio: allGroups.length > 0 ? groupsWithDocs.size / allGroups.length : 0,
  undocumentedGroups,
};

// ---------------------------------------------------------------------------
// K. Dependency Direction
// ---------------------------------------------------------------------------
const pairCounts = {};
for (const e of importEdges) {
  const sg = idToGroup[e.source] || "root";
  const tg = idToGroup[e.target] || "root";
  if (sg === tg) continue;
  const key1 = `${sg}->${tg}`;
  const key2 = `${tg}->${sg}`;
  pairCounts[key1] = pairCounts[key1] || { a: sg, b: tg, aToB: 0, bToA: 0 };
  pairCounts[key1].aToB++;
}
// Also track reverse
for (const e of importEdges) {
  const sg = idToGroup[e.source] || "root";
  const tg = idToGroup[e.target] || "root";
  if (sg === tg) continue;
  // Already counted in both directions by checking both keys
}

// Group by unordered pair
const pairDirs = {};
for (const [key, val] of Object.entries(pairCounts)) {
  const [a, b] = key.split("->");
  const pairKey = [a, b].sort().join("|");
  if (!pairDirs[pairKey]) {
    pairDirs[pairKey] = { a, b, aToB: 0, bToA: 0 };
  }
  pairDirs[pairKey].aToB += val.aToB;
  // Check reverse
  const revKey = `${b}->${a}`;
  if (pairCounts[revKey]) {
    pairDirs[pairKey].bToA += pairCounts[revKey].aToB;
  }
}

const dependencyDirection = [];
for (const [pairKey, val] of Object.entries(pairDirs)) {
  if (val.aToB > val.bToA) {
    dependencyDirection.push({ dependent: val.a, dependsOn: val.b });
  } else if (val.bToA > val.aToB) {
    dependencyDirection.push({ dependent: val.b, dependsOn: val.a });
  }
}

// ---------------------------------------------------------------------------
// File Stats
// ---------------------------------------------------------------------------
const filesPerGroup = {};
for (const [grp, ids] of Object.entries(directoryGroups)) {
  filesPerGroup[grp] = ids.length;
}

const nodeTypeCounts = {};
for (const [t, ids] of Object.entries(nodeTypeGroups)) {
  nodeTypeCounts[t] = ids.length;
}

const fileStats = {
  totalFileNodes: fileNodes.length,
  filesPerGroup,
  nodeTypeCounts,
};

// ---------------------------------------------------------------------------
// Output
// ---------------------------------------------------------------------------
const results = {
  scriptCompleted: true,
  directoryGroups,
  nodeTypeGroups,
  crossCategoryEdges,
  interGroupImports,
  intraGroupDensity,
  patternMatches,
  deploymentTopology,
  dataPipeline,
  docCoverage,
  dependencyDirection,
  fileStats,
  fileFanIn,
  fileFanOut,
  _analysis: {
    prefix,
    groupImportsFrom: groupImportsFromArr,
    groupImportedBy: groupImportedByArr,
  },
};

fs.writeFileSync(outputPath, JSON.stringify(results, null, 2), "utf-8");
console.log(`Analysis written to ${outputPath}`);
process.exit(0);
