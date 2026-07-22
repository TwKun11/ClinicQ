# Project Context

**Project:** Training Starter / ClinicQ
**Analysis Date:** 2026-07-22
**Status:** Existing brownfield codebase onboarded for GSD Core.

## Purpose

This repository is a full-stack starter/training application with a Spring Boot backend and Angular frontend. It provides JWT authentication, user CRUD, patient CRUD, shared API response patterns, database migrations, local infrastructure, and reusable frontend shell/components.

## Scope

**In scope from current source:**
- User registration, login, and token refresh.
- JWT-protected backend APIs.
- User CRUD reference implementation.
- Patient CRUD implementation.
- Angular login/register screens.
- Angular dashboard and user management screens.
- Local PostgreSQL, Redis, RabbitMQ, and MailHog infrastructure.

**Unknown / needs confirmation:**
- Whether the product target is a production clinic system or a training scaffold.
- Required patient workflows beyond CRUD.
- Compliance/privacy requirements.
- Deployment target and CI/CD.
- Role model for real clinic usage.

## Architecture

The project is a two-part application:

- Backend: Java 17 Spring Boot 3.2.5 under `backend/`.
- Frontend: Angular 17 under `frontend/`.

Backend dependency direction:

```text
controller -> service interface -> service implementation -> repository/entity
                         |
                         v
                    DTOs / mappers / exceptions
```

Frontend organization:

```text
routes/layout -> features -> core services/models/guards/interceptors
                         -> shared components
```

Detailed codebase context lives in `.planning/codebase/`.

## Main Modules

- Authentication: `backend/src/main/java/com/training/starter/controller/AuthController.java`, `backend/src/main/java/com/training/starter/service/impl/AuthServiceImpl.java`, `frontend/src/app/core/services/auth.service.ts`.
- Security: `backend/src/main/java/com/training/starter/security`, `frontend/src/app/core/interceptors/jwt.interceptor.ts`, `frontend/src/app/core/guards/auth.guard.ts`.
- Users: `backend/src/main/java/com/training/starter/controller/UserController.java`, `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`, `frontend/src/app/features/users`.
- Patients: `backend/src/main/java/com/training/starter/controller/PatientController.java`, `backend/src/main/java/com/training/starter/service/impl/PatientServiceImpl.java`.
- Database migrations: `backend/src/main/resources/db/migration`.
- Shared backend API envelopes: `backend/src/main/java/com/training/starter/common`.
- Shared frontend UI: `frontend/src/app/shared/components`.

## Technology

See `.planning/codebase/STACK.md` and `.planning/codebase/INTEGRATIONS.md`.

## Validation And Error Handling

- Request DTOs use Jakarta Bean Validation and controller `@Valid`.
- Backend errors are centralized in `backend/src/main/java/com/training/starter/exception/GlobalExceptionHandler.java`.
- API responses use `ApiResponse`; paginated responses use `PageResponse`.

## Authentication And Authorization

- Authentication is custom JWT.
- Spring Security is stateless.
- Public backend routes include `/api/v1/auth/**`, Swagger/OpenAPI routes, and `/actuator/**`.
- Role-specific authorization rules are not confirmed in current controllers.

## Database

- PostgreSQL is the primary relational database.
- Flyway migrations live in `backend/src/main/resources/db/migration`.
- Main profile uses Hibernate schema validation.
- Existing migrations: users table and patients table.

## Testing Strategy

- Backend unit tests are present for user service behavior.
- Backend integration test base uses Testcontainers.
- Frontend has Angular/Karma testing dependencies, but no committed spec files were detected in `frontend/src/app`.
- See `.planning/codebase/TESTING.md`.

## AI Change Boundaries

AI agents must not:

- Change business logic during GSD onboarding.
- Rewrite architecture without an approved plan.
- Modify secrets or print secret values.
- Rewrite existing Flyway migrations casually.
- Overwrite uncommitted user changes.
- Commit or push unless explicitly asked.

