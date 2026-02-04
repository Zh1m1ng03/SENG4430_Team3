# SENG4430 Team3 Project

This repository contains the broker backend application built with Quarkus.

## Running the Broker Backend with Docker

The easiest way to run the broker backend is using Docker Compose. It will build the images and start both PostgreSQL and the application.

### Quick Start

1. **Navigate to the broker-back-end directory**:
   ```bash
   cd broker-back-end
   ```

2. **Build and start all services**:
   ```bash
   docker compose up --build
   ```

   This single command will:
   - Build the application Docker image
   - Pull the PostgreSQL image (if needed)
   - Start both PostgreSQL and the application containers

3. **Start in detached mode** (runs in background):
   ```bash
   docker compose up --build -d
   ```

### Useful Commands

- **View logs**:
  ```bash
  docker compose logs -f app
  ```

- **Stop services**:
  ```bash
  docker compose down
  ```

- **Stop services and remove volumes** (clean up database data):
  ```bash
  docker compose down -v
  ```

The application will be available at:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/q/swagger-ui/
- **Dev UI**: http://localhost:8080/q/dev/ (dev mode only)
- **PostgreSQL**: localhost:5432

## Project Structure

- `broker-back-end/` - Quarkus-based broker backend application
- `quality-analysis-tool/` - Code quality analysis tool

For more details about running the broker backend locally or building the application, see the [broker-back-end README](broker-back-end/README.md).
