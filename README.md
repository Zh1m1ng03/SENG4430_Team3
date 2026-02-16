# SENG4430 Team3 Project

## Trading Application

### Prerequisites

- **Java 17**
- **MySQL** (running locally on port 3306)
- **Maven**

### Setup Instructions

#### 1. Configure MySQL credentials

Edit `trading-application/src/main/resources/application.properties` and set your local MySQL user and password:

```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

#### 2. Initialize the database

Run the SQL script to create the database and tables:

```bash
mysql -u your_username -p < trading-application/src/sql/tradingapp.sql
```

Or open MySQL and execute the script manually:

```bash
mysql -u your_username -p
```

```sql
source trading-application/src/sql/tradingapp.sql
```

#### 3. Run the application

```bash
cd trading-application
./mvnw spring-boot:run
```

Or with Maven installed:

```bash
cd trading-application
mvn spring-boot:run
```

The application will start at **http://localhost:9000**.
