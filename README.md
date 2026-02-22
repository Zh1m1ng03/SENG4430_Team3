# SENG4430 Team3 Project

## Test Tool

The **test-tool** is a Java-based static analysis CLI that analyses Java source code and computes software metrics. It uses [JavaParser](https://javaparser.org/) to parse `.java` files into ASTs and runs pluggable metric analysers (e.g. cyclomatic complexity) per file, then can aggregate results and generate JSON reports.

- **Metrics**: Implementations of `IMetricAnalyser` (e.g. average cyclomatic complexity per method, `CC_AVG`) produce `Result(metricId, target, value)` per compilation unit.
- **Reports**: Metric results can be written as JSON under `metric-report/<metricId>/` via `ReportGenerator`.
- **CLI**: The tool runs as a Spring Boot application with an interactive menu; registered test cases appear as options that run the corresponding analyses.

**Prerequisites**: Java 21, Maven.

**Run from project root:**

```bash
cd static-test-tool
./mvnw spring-boot:run
```

Or with Maven installed: `mvn spring-boot:run`. Choose an option from the menu to run the desired analysis.

**Configuration (`static-test-tool/src/main/resources/application.yaml`):**

- `report.enabled`: Set to `true` to write JSON metric reports to `static-metric-report/` after running analyses; set to `false` to disable report generation (default in the file is `false`). The setting is bound to `ReportProperties` in the report generator.

**Directory layout (main folders):**

```
static-test-tool/
├── src/main/java/com/team3/
│   ├── Application.java
│   ├── analyser/          # Metrics (e.g. cyclomatic complexity)
│   ├── io/                # Source path & Java parsing
│   ├── report/            # JSON report generation
│   ├── entity/            # TestCase, Report
│   ├── factory/           # MetricAnalyserFactory
│   └── registry/          # TestCaseRegistry
└── src/main/resources/
    └── application.yaml
```

---

## Trading Application

### Prerequisites

- **Java 17**
- **MySQL** (running locally on port "3306")
- **Maven**

### Setup Instructions

#### 1. Configure MySQL credentials

Create a `.env` file in the `trading-project` directory (copy from `.env.example`):

```bash
cp trading-project/.env.example trading-project/.env
```

Edit `.env` with your local MySQL user and password:

```
MYSQL_USER=your_mysql_username
MYSQL_PASSWORD=your_mysql_password
```

> `.env` is gitignored and must not be committed.

#### 2. Initialize the database

Run the SQL script to create the database and tables:

```bash
mysql -u your_username -p < trading-project/src/sql/tradingapp.sql
```

Or open MySQL and execute the script manually:

```bash
mysql -u your_username -p
```

```sql
source trading-project/src/sql/tradingapp.sql
```

#### 3. Run the application

```bash
cd trading-project
./mvnw spring-boot:run
```

Or with Maven installed:

```bash
cd trading-project
mvn spring-boot:run
```

The application will start at **http://localhost:9000**.
