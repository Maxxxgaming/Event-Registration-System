# Event Registration System

A full-stack event registration system monorepo containing a Spring Boot API and a React/Vite frontend. The backend currently includes the event and registration API, JPA domain model, and Flyway-managed MySQL schema.

## Tech stack

- **Backend:** Java 17, Spring Boot 3, Maven, Spring Data JPA, MySQL
- **Frontend:** React, TypeScript, Vite, React Router

## Prerequisites

- JDK 17
- Node.js 20.19+ or 22.12+, and npm
- MySQL 8+
- Docker for backend integration tests

## Run the backend

Create a MySQL database named `event_registration_db` and set `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` (at least 32 characters) in your environment. The development JDBC URL defaults to `jdbc:mysql://localhost:3306/event_registration_db`; override it with `DB_URL` if needed. Flyway applies schema migrations on startup, and Hibernate validates the mapped schema.

From the repository root:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The health endpoint is available at <http://localhost:8080/api/health>. Set `SERVER_PORT` to change the port.

Run backend tests from `backend` with `.\mvnw.cmd clean verify`. Integration tests use Testcontainers and require Docker; they do not use your local MySQL database.

The event API is rooted at `/api/events`; registrations are available under `/api/events` and `/api/registrations/me`. Authentication endpoints are `/api/auth/register`, `/api/auth/login`, and `/api/auth/me`. Use the returned bearer token for protected endpoints. The `/api/admin/**` routes and event management routes require the `ADMIN` role.

Set `JWT_SECRET` to a secret of at least 32 characters (and at least 32 UTF-8 bytes) before starting the backend. `JWT_EXPIRATION_MS` optionally overrides the default 24-hour token lifetime. `CORS_ALLOWED_ORIGINS` accepts a comma-separated origin list and defaults to `http://localhost:5173`. To seed an administrator, optionally set `ADMIN_EMAIL` and `ADMIN_PASSWORD`; both must be set or seeding is skipped.

## Run the frontend

From the repository root:

```powershell
cd frontend
npm install
Copy-Item .env.example .env
npm run dev
```

The Vite development server prints its local URL (normally <http://localhost:5173>). Configure the API URL in `frontend/.env`.

## Profiles and API documentation

The backend defaults to the `dev` profile. Set `SPRING_PROFILES_ACTIVE=prod` for production configuration and provide `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` through the deployment environment. Swagger UI is available at `/swagger-ui.html` when the backend is running.
