# curie-clean (1.0.2)

## Skye Goetz (ISB) 03/26/2025

A Clojure utility for analyzing and resolving duplicate nodes in knowledge graph exchange **(KGX)** formatted knowledge graphs, with [**Tablassert**](https://github.com/SkyeAv/Tablassert) integration and **Biolink Model** compliance. It **updates** Tablassert-integreated **"table_configs"** through a **CLI**. 

## Features

- **Duplicate Detection**: Identifies duplicate entries in TSV files
- **Interactive Resolution**: CLI prompts for handling conflicts
- **YAML Configuration**: Generates config files to prevent future duplicates

## Requirements

- **Clojure 1.10+**
- Java JDK 8+
- Leiningen

## Usage

For arguments...

```
# With Leiningen
lein run -h
```

**To resolve duplicates...**

```bash
# With Leiningen
lein run -n nodes.tsv -e edges.tsv
```

To test the application...

```bash
# With Leiningen
lein test
```

## Directory Structure

```txt
curie-clean/
├── src/
│   └── duplicate_utility/
│       ├── io/
│       │   ├── tsv.clj
│       │   └── yaml.clj
│       ├── processing/
│       │   ├── duplicates.clj
│       │   └── resolution.clj
│       ├── core.clj
│       └── validation.clj
└── test/
    └── duplicate_utility/
        ├── io/
        │   ├── tsv_test.clj
        │   └── yaml_test.clj
        ├── processing/
        │   ├── duplicates_test.clj
        │   └── resolution_test.clj
        ├── core_test.clj
        ├── test_utils.clj
        └── validation_test.clj
```
