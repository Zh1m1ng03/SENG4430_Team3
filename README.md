# SENG4430 Team3 Project

## Trading Application

### Prerequisites

- **Java 17**
- **MySQL** (running locally on port 3306)
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
