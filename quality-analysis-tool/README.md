# Software Quality Analysis Tool

A comprehensive Java-based tool for analyzing software quality metrics including maintainability, security, reliability, and usability. This tool can analyze Java projects and generate detailed quality reports in both JSON and HTML formats.

## Features

- **Maintainability Analysis**: Measures cyclomatic complexity, code smells, method/class length, coupling, and cohesion
- **Security Analysis**: Detects SQL injection risks, XSS vulnerabilities, hardcoded secrets, weak encryption usage
- **Reliability Analysis**: Evaluates exception handling coverage, null pointer risks, resource leak detection
- **Usability Analysis**: Checks documentation coverage, naming conventions, API design quality
- **Self-Testing**: Can analyze its own codebase to demonstrate self-testing capability
- **Multiple Report Formats**: Generates both JSON and HTML reports

## Requirements

- Java 11 or higher
- Maven 3.6+ (or use included Maven wrapper)

## Building the Project

```bash
mvn clean package
```

This will create an executable JAR file in the `target` directory.

## Usage

### Basic Usage

Analyze a Java project:

```bash
java -jar target/quality-analysis-tool-1.0.0.jar <path-to-project>
```

### Command Line Options

- `-o, --output <directory>`: Output directory for reports (default: `./quality-reports`)
- `-f, --format <format>`: Report format: `json`, `html`, or `both` (default: `both`)
- `-s, --self-test`: Run self-test analysis on this tool's own codebase
- `-v, --verbose`: Enable verbose output
- `-h, --help`: Show help message

### Examples

**Analyze a project and generate both JSON and HTML reports:**
```bash
java -jar target/quality-analysis-tool-1.0.0.jar ../broker-back-end/src/main/java
```

**Generate only JSON report:**
```bash
java -jar target/quality-analysis-tool-1.0.0.jar ../broker-back-end/src/main/java -f json
```

**Run self-test (analyze this tool's own code):**
```bash
java -jar target/quality-analysis-tool-1.0.0.jar . -s
```

**Verbose output:**
```bash
java -jar target/quality-analysis-tool-1.0.0.jar ../broker-back-end/src/main/java -v
```

## Output

The tool generates reports in the specified output directory (default: `./quality-reports`):

- `quality-report.json`: Detailed JSON report with all metrics
- `quality-report.html`: Visual HTML report with charts and recommendations

## Quality Metrics Explained

### Maintainability Score
- **Cyclomatic Complexity**: Measures code complexity (lower is better)
- **Code Smells**: Identifies long methods, large classes, high complexity methods
- **Coupling/Cohesion**: Measures class interdependencies

### Security Score
- **SQL Injection Risks**: Detects unsafe SQL query construction
- **XSS Risks**: Identifies potential cross-site scripting vulnerabilities
- **Hardcoded Secrets**: Finds potential hardcoded passwords/API keys
- **Weak Encryption**: Detects insecure random number generation

### Reliability Score
- **Exception Handling**: Coverage of exception handling in methods
- **Null Pointer Risks**: Potential null pointer exceptions
- **Resource Leaks**: Unclosed resources (files, connections, etc.)

### Usability Score
- **Documentation Coverage**: Percentage of methods/classes with JavaDoc
- **Naming Conventions**: Clarity of method and variable names
- **API Design**: Complexity of method signatures

## Self-Testing Capability

The tool can analyze its own codebase using the `--self-test` flag. This demonstrates the self-testing requirement for the assignment.

## Project Structure

```
quality-analysis-tool/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── quality/
│   │               └── tool/
│   │                   ├── QualityAnalysisTool.java    # Main entry point
│   │                   ├── analyzer/
│   │                   │   ├── CodeAnalyzer.java        # Core analyzer
│   │                   │   └── metrics/                 # Metric analyzers
│   │                   ├── model/                       # Data models
│   │                   └── report/                      # Report generators
│   └── test/
└── pom.xml
```

## Dependencies

- **JavaParser**: For parsing and analyzing Java source code
- **Jackson**: For JSON processing
- **Picocli**: For command-line interface
- **SLF4J**: For logging

## License

This project is created for educational purposes as part of SENG4430 Software Quality course.

## Author

Created for SENG4430 Assessment 1 - Software Quality Analysis Tool
