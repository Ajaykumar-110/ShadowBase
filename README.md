# ShadowBase

### Zero-Downtime Schema Migration Sandbox

ShadowBase is an individual software development project focused on exploring and validating database schema migrations with minimal downtime using Java, Spring Boot, PostgreSQL, Docker, Apache Kafka, and Debezium.

The project aims to provide a controlled environment to experiment with database schema changes, migration planning, change data capture (CDC), and migration execution.


## Tech Stack

- Java 21
- Spring Boot
- PostgreSQL
- Docker & Testcontainers
- Apache Kafka & Debezium
- Maven
- React (Dashboard)

## Key Features

- Database schema comparison
- Automated migration planning
- Migration execution
- Migration history tracking
- Database integration testing
- Change Data Capture (CDC) exploration

## Project Structure

```text
ShadowBase/
├── src/
├── frontend/
├── docs/
├── infra/
├── pom.xml
└── README.md
```

## Run Locally

```bash
git clone https://github.com/Ajaykumar-110/shadowbase.git
cd shadowbase
.\mvnw.cmd spring-boot:run
```
## 📅 Week 1 Progress

- Initialized Java 21 and Spring Boot project.
- Implemented schema comparison service.
- Developed migration planning and execution services.
- Added migration history tracking.
- Created PostgreSQL integration tests using Testcontainers.
- Added backend service and application tests.
- Organized project structure and GitHub repository.

**Status:** Backend foundation and core migration services developed. Further integration and dashboard development are ongoing.