# AGENTS.md

Long-lived guidance for Codex and other AI coding agents working in this repository.

## Project Overview

This is a full-stack training starter application. The backend is a Java 17 Spring Boot 3.2.5 API with JWT authentication, Spring Security, Spring Data JPA, PostgreSQL, Redis, RabbitMQ, Flyway migrations, MapStruct, Lombok, and SpringDoc OpenAPI. The frontend is an Angular 17 standalone-component application using Angular Material, RxJS, TypeScript, and SCSS.

The current domain includes authentication, users, patients, and a dashboard shell. The `User` CRUD implementation is the reference pattern for extending new domain entities.

## Architecture

- Backend code lives in `backend/src/main/java/com/training/starter`.
- Frontend code lives in `frontend/src/app`.
- Backend dependency direction should remain controller -> service interface -> service implementation -> repository/entity, with DTOs and mappers at the API boundary.
- Frontend feature code should stay under `frontend/src/app/features`, shared reusable UI under `frontend/src/app/shared`, cross-cutting services/guards/interceptors/models under `frontend/src/app/core`, and layout shells under `frontend/src/app/layout`.
- Do not introduce a new architecture, module system, or framework unless the user asks for it and a plan has been approved.

## Backend Layout

- `config`: Redis, RabbitMQ, OpenAPI, and web/CORS configuration.
- `security`: JWT token provider, JWT filter, user details service, and Spring Security configuration.
- `controller`: REST controllers for auth, users, and patients.
- `dto/request` and `dto/response`: API request and response records/classes.
- `entity`: JPA entities such as `BaseEntity`, `User`, and `Patient`.
- `exception`: custom exceptions and `GlobalExceptionHandler`.
- `mapper`: MapStruct mapper interfaces.
- `repository`: Spring Data JPA repositories.
- `service` and `service/impl`: service contracts and business logic.
- `common`: response wrappers such as `ApiResponse` and `PageResponse`.
- `backend/src/main/resources/db/migration`: Flyway SQL migrations.

## Frontend Layout

- `core/guards`: route guards.
- `core/interceptors`: HTTP interceptors, including JWT attachment.
- `core/models`: shared API response models.
- `core/services`: auth and notification services.
- `features/auth`: login and register pages.
- `features/users`: user list/form components and user API service.
- `features/dashboard`: dashboard page.
- `layout`: auth and main layout components.
- `shared/components`: confirm dialog, loading spinner, and pagination.

## Commands

Install backend dependencies through Maven wrapper commands in `backend`:

```bash
cd backend
./mvnw test
```

Run backend development server:

```bash
cd backend
./mvnw spring-boot:run
```

Start local infrastructure:

```bash
cd backend
docker compose up -d
```

Install frontend dependencies:

```bash
cd frontend
npm install
```

Run frontend development server:

```bash
cd frontend
npm start
```

Build frontend:

```bash
cd frontend
npm run build
```

Run frontend tests:

```bash
cd frontend
npm test
```

No dedicated lint script is currently defined in `frontend/package.json`, and no backend lint plugin is configured in `backend/pom.xml`.

## Database And Migrations

Database changes are managed with Flyway SQL files in `backend/src/main/resources/db/migration`.

- Name new migrations with the next version number, for example `V3__describe_change.sql`.
- Do not edit existing migrations after they have been applied unless the user explicitly approves a reset/migration rewrite.
- Do not change schema or seed data as part of unrelated work.

## API And Error Conventions

- REST responses use `ApiResponse` and paginated endpoints use `PageResponse`.
- Validation errors are handled by `GlobalExceptionHandler` and returned as field-message maps.
- Known error types include `ResourceNotFoundException`, `DuplicateResourceException`, `BadRequestException`, bad credentials, access denied, and generic internal errors.
- Keep authentication stateless and JWT-based unless a planned change says otherwise.
- Public routes currently include `/api/v1/auth/**`, Swagger/OpenAPI routes, and `/actuator/**`; other routes require authentication.

## Coding Conventions

- Follow the existing package structure and naming style.
- Prefer service interfaces plus `service/impl` implementations for backend business logic.
- Use DTOs and MapStruct mappers instead of exposing entities directly through controllers.
- Use Lombok consistently where the existing backend code does.
- Use Angular standalone components and SCSS, matching the existing Angular 17 setup.
- Keep frontend API response handling aligned with `core/models`.

## Testing Expectations

- Add focused backend tests under `backend/src/test/java` for service behavior and new business rules.
- Use the existing `UserServiceTest` style as the reference for unit tests.
- Use `BaseIntegrationTest` and Testcontainers only when integration coverage is needed.
- For frontend changes, add or update Angular/Karma tests when behavior is changed.
- Run relevant tests and builds after changes. If a command is unavailable or blocked by local prerequisites, report that clearly.

## Secrets And Environment

- Do not commit `.env`, `.env.*`, machine-local config, generated logs, dependency folders, build outputs, or cache directories.
- Do not print secret values from `application.yml`, environment files, or local shell variables.
- Treat `backend/src/main/resources/application.yml` as sensitive configuration. Preserve user edits in that file unless the task explicitly requires a config change.
- Use `application-test.yml` for test-specific configuration.

## Files And Folders To Avoid Editing Automatically

- Do not modify `.git` metadata except for explicitly requested git operations.
- Do not edit generated dependency/build folders such as `node_modules`, `target`, `dist`, `.angular/cache`, or `coverage`.
- Do not rewrite existing Flyway migrations casually.
- Do not change `backend/src/main/resources/application.yml` without explicit user confirmation because it currently has uncommitted local changes.

## GSD Workflow Guidance

- This repository is intended to use GSD Core with Codex for the loop: Discuss -> Plan -> Execute -> Verify -> Ship.
- Before large features or multi-module changes, read the GSD project context files if they exist, especially `.planning/PROJECT.md`, `.planning/REQUIREMENTS.md`, `.planning/ROADMAP.md`, `.planning/STATE.md`, and `.planning/codebase/`.
- For larger changes, create or update a plan before editing multiple modules.
- Do not run greenfield project initialization over this existing codebase if it risks overwriting source code.
- Do not fabricate GSD planning artifacts. If GSD is not installed yet, run the official installer first:

```bash
scripts/install-gsd-codex.cmd
```

This installs Codex runtime files locally through the official installer using `--codex --local`.
Generated runtime directories such as `.codex/`, `.claude/`, and `.gsd/` are intentionally ignored and should not be pushed.

## Git Rules

- Check `git status` before editing.
- Do not overwrite uncommitted user changes.
- Do not commit, push, or create a pull request unless the user explicitly asks.
- Keep review diffs focused on the requested task and avoid unrelated formatting churn.
