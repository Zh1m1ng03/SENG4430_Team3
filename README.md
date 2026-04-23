# SENG4430 Software Quality Tool

A Java-based software quality analysis tool built with Spring Boot. It performs static analysis on Java source code and dynamic throughput testing on a running web application, then reports results via an interactive CLI.

---

## Team
Guotai Xiao: Guotai812

---

## Prerequisites

| Requirement | Version  |
|-------------|----------|
| Java        | 21       |
| Maven       | 3.8+     |

No database is required to run this tool. The throughput tests require the [trading-project](../trading-project/README.md) to be running on `http://localhost:9000`.

---

## Build and Run

From the `quality-test-tool` directory:

```bash
# Using the Maven wrapper (no local Maven install needed)
./mvnw spring-boot:run

# Or with Maven installed globally
mvn spring-boot:run
```

The tool launches an interactive CLI menu.

---

## Configuration

All settings live in `src/main/resources/application.yaml`.

```yaml
analysis:
  cyclomatic-complexity:
    good-threshold: 3      # CC <= this is GOOD
    warning-threshold: 6   # CC <= this is WARNING; above is CRITICAL
    top-n: 10              # number of most-complex methods to list in the report
  ignore:
    file-patterns:         # file name patterns to exclude (supports * wildcard)
      - "*DAO.java"
      - "*DTO.java"
    packages:              # package prefixes to exclude (dot notation)
      - "com.chainsys.tradingapp.config"
      - "com.chainsys.tradingapp.model"
      - "com.chainsys.tradingapp.mapper"
```

Files matching `*Test.java` or `*Tests.java` are always excluded from static analysis regardless of the yaml settings.

---

## Metrics

### Static Analysis

| # | Metric | Quality Aspect | How It Works |
|---|--------|---------------|--------------|
| 1 | Cyclomatic Complexity | Maintainability | Counts decision points (if, for, while, catch, ternary, &&, \|\|, switch cases) per method. Score = average of per-method scores. |
| 2 | Duplicate Code Ratio | Maintainability | Uses PMD CPD (minTokens=30) to find cloned blocks. Score penalises redundant lines beyond the first occurrence. |
| 3 | Weak Crypto APIs | Security | AST scan for MD5, SHA-1, DES, RC4, and `java.util.Random` usage. Each violation deducts 20 points from 100. |
| 4 | Uncaught Exceptions | Reliability | Counts `throw` statements that sit outside a top-level `try` block per method. Score = percentage of safe methods. |

### Dynamic Analysis

| Metric | How It Works |
|--------|-------------|
| Throughput (QPS) | Fires 20 concurrent threads at each endpoint for 3 seconds via OkHttp. Reports queries-per-second and a level: `bad` (0 QPS), `medium` (1–1000), or `good` (>1000). Covers 30 endpoints of the trading-project. |

### Ratings

| Rating   | Meaning |
|----------|---------|
| GOOD     | Score ≥ 70 (CC) / ≥ 80 (Duplicate, Crypto) / ≥ 80 (Uncaught) |
| WARNING  | Score ≥ 40 (CC) / ≥ 50 (Duplicate) / ≥ 40 (Crypto) / ≥ 50 (Uncaught) |
| CRITICAL | Below WARNING threshold |

---

## CLI Usage

### Before loading a path

```
SENG4430 Software Quality Tool
Current Path: (none)

  1. Load Target Path
  2. Throughput (Reliability)
  0. Exit
```

### After loading a path

```
SENG4430 Software Quality Tool
Current Path: /path/to/source

  1. Change Target Path
  2. Cyclomatic Complexity (Maintainability)
  3. Duplicate Code (Maintainability)
  4. Weak Crypto APIs (Security)
  5. Uncaught Exceptions (Reliability)
  6. Throughput (Reliability)
  7. Run All Metrics
  0. Exit
```

Enter the number for the desired analysis. **Run All Metrics** runs every static analyser and prints a summary table with an overall score.

### Example: analyse the trading-project

```
Enter your choice: 1
Enter target path: ../trading-project/src/main/java
Path loaded. Found 42 Java files.
```

### Example: self-test (analyse this tool itself)

```
Enter your choice: 1
Enter target path: src/main/java
Path loaded. Found 16 Java files.
```

---

## Running Tests

```bash
# Run all unit and integration tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=CyclomaticComplexityAnalyserTest
```

> The throughput tests in `ThroughPutTest.java` require the trading-project server to be running on port 9000. They have no JUnit assertions and are run as live integration tests only.

### Test Suite Summary

| Test Class | Tests | Type |
|---|---|---|
| `CyclomaticComplexityAnalyserTest` | 23 | Unit |
| `UncaughtExceptionAnalyserTest` | 14 | Unit |
| `DuplicateCodeAnalyserTest` | 8 | Unit |
| `WeakCryptoAnalyserTest` | 6 | Unit |
| `StaticAnalysisEngineIntegrationTest` | 7 | Integration |
| `ThroughPutTest` | 30 | Dynamic (requires live server) |
| `QualityTestToolApplicationTests` | 1 | Spring context |

---

## Project Structure

```
quality-test-tool/
├── src/main/java/com/seng4430/qualitytesttool/
│   ├── QualityTestToolApplication.java         # Entry point (CommandLineRunner)
│   ├── shared/
│   │   ├── cli/MenuController.java             # Interactive CLI menu
│   │   ├── config/AnalysisConfig.java          # Binds application.yaml settings
│   │   └── report/ReportPrinter.java           # Console report formatting
│   ├── staticanalysis/
│   │   ├── engine/StaticAnalysisEngine.java    # Parses .java files, dispatches analysers
│   │   └── metric/
│   │       ├── MetricAnalyser.java             # Interface for all analysers
│   │       ├── MetricResult.java               # Immutable result: score, rating, details
│   │       ├── MetricRating.java               # Enum: GOOD / WARNING / CRITICAL
│   │       ├── MetricRegistry.java             # Returns the ordered list of analysers
│   │       └── impl/
│   │           ├── CyclomaticComplexityAnalyser.java
│   │           ├── DuplicateCodeAnalyser.java
│   │           ├── WeakCryptoAnalyser.java
│   │           └── UncaughtExceptionAnalyser.java
│   └── dynamicanalysis/
│       ├── cofig/HttpConfig.java               # OkHttpClient configuration
│       ├── cofig/ThreadPoolConfig.java         # ThreadPoolExecutor configuration
│       └── service/ThroughPutTestService.java  # 30 endpoint throughput tests
└── src/main/resources/
    └── application.yaml
```

---

## Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Spring Boot | 4.1.0-M2 | Application framework, CLI runner |
| JavaParser | 3.25.10 | AST parsing for static analysis |
| PMD (pmd-java) | 7.13.0 | CPD duplicate code detection |
| OkHttp | 4.12.0 | HTTP client for throughput tests |
| Gson | 2.10.1 | JSON body construction in tests |
