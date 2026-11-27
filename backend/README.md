# Event Registration System API

Spring Boot 3 / Java 17 REST API with MySQL persistence, Flyway migrations, JWT authentication, and role-based authorization.

## Prerequisites

- JDK 17
- Docker Desktop or another Docker-compatible runtime for integration tests
- MySQL 8+ for running the application locally

The Maven wrapper is included, so a separate Maven installation is not required.

## Configuration

The default profile is `dev`. Set the following environment variables before starting the API:

| Variable | Required | Development behavior |
| --- | --- | --- |
| `DB_URL` | Yes for local database use | Defaults to `jdbc:mysql://localhost:3306/event_registration_db` |
| `DB_USERNAME` | Yes | No default credential |
| `DB_PASSWORD` | Yes | No default credential |
| `JWT_SECRET` | Yes | Must contain at least 32 characters and 32 UTF-8 bytes |
| `ADMIN_EMAIL` | No | If set with `ADMIN_PASSWORD`, seeds an administrator at startup |
| `ADMIN_PASSWORD` | No | If set with `ADMIN_EMAIL`, seeds an administrator at startup |
| `CORS_ALLOWED_ORIGINS` | No | Defaults to `http://localhost:5173`; accepts comma-separated origins |

`JWT_EXPIRATION_MS` optionally changes the token lifetime (default: 86400000 ms). Set `SPRING_PROFILES_ACTIVE=prod` for production; production requires explicit database credentials and does not enable SQL output.

## Run locally

Create the `event_registration_db` schema in MySQL, configure the required environment variables, then run from this directory:

```powershell
.\mvnw.cmd spring-boot:run
```

In Bash-compatible terminals, use `./mvnw spring-boot:run`.

The service listens on port 8080 by default. Health check: <http://localhost:8080/api/health>.

## Tests and coverage

Run unit tests:

```powershell
.\mvnw.cmd test
```

Run unit and full-stack integration tests, apply Flyway migrations to a real MySQL Testcontainer, and generate coverage:

```powershell
.\mvnw.cmd clean verify
```

Docker must be running for integration tests. They use the isolated `test` profile and a test-only JWT secret; no local MySQL credentials are used. The JaCoCo report is written to `target/site/jacoco/index.html`.

## API documentation

With the API running, Swagger UI is at <http://localhost:8080/swagger-ui.html> and the OpenAPI document is at <http://localhost:8080/v3/api-docs>.
