# Codebase Structure

**Analysis Date:** 2026-07-22

## Directory Layout

```text
ClinicQ/
|-- backend/              # Spring Boot backend
|   |-- pom.xml           # Maven project configuration
|   |-- docker-compose.yml # Local infrastructure
|   |-- src/main/java/com/training/starter/
|   |-- src/main/resources/
|   `-- src/test/java/com/training/starter/
|-- frontend/             # Angular frontend
|   |-- package.json      # npm scripts and dependencies
|   |-- angular.json      # Angular CLI config
|   `-- src/app/
|-- .codex/               # Local GSD Core Codex runtime files
|-- .planning/            # GSD planning and codebase context
|-- AGENTS.md             # Long-lived Codex guidance
|-- .gitignore
`-- README.md
```

## Directory Purposes

**`backend/src/main/java/com/training/starter/controller`:**
- Purpose: REST API endpoints.
- Contains: `AuthController.java`, `UserController.java`, `PatientController.java`.
- Key files: `backend/src/main/java/com/training/starter/controller/UserController.java`.

**`backend/src/main/java/com/training/starter/service`:**
- Purpose: Service contracts.
- Contains: `AuthService.java`, `UserService.java`, `PatientService.java`.
- Key files: `backend/src/main/java/com/training/starter/service/UserService.java`.

**`backend/src/main/java/com/training/starter/service/impl`:**
- Purpose: Business logic implementations.
- Contains: `AuthServiceImpl.java`, `UserServiceImpl.java`, `PatientServiceImpl.java`.
- Key files: `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`.

**`backend/src/main/java/com/training/starter/entity`:**
- Purpose: JPA persistence entities.
- Contains: `BaseEntity.java`, `User.java`, `Patient.java`.
- Key files: `backend/src/main/java/com/training/starter/entity/BaseEntity.java`.

**`backend/src/main/resources/db/migration`:**
- Purpose: Flyway schema migrations.
- Contains: versioned SQL migration files.
- Key files: `backend/src/main/resources/db/migration/V1__create_users_table.sql`, `backend/src/main/resources/db/migration/V2__create_patients_tables.sql`.

**`frontend/src/app/core`:**
- Purpose: Cross-cutting Angular code.
- Contains: guards, interceptors, services, models.
- Key files: `frontend/src/app/core/interceptors/jwt.interceptor.ts`, `frontend/src/app/core/guards/auth.guard.ts`.

**`frontend/src/app/features`:**
- Purpose: Feature screens and feature-specific API services.
- Contains: `auth`, `users`, `dashboard`.
- Key files: `frontend/src/app/features/users/user.service.ts`.

**`frontend/src/app/shared`:**
- Purpose: Reusable UI components.
- Contains: loading spinner, pagination, confirm dialog.
- Key files: `frontend/src/app/shared/components/pagination/pagination.component.ts`.

## Key File Locations

**Entry Points:**
- `backend/src/main/java/com/training/starter/StarterApplication.java`: Spring Boot entry point.
- `frontend/src/main.ts`: Angular bootstrap entry point.
- `frontend/src/app/app.routes.ts`: Angular route tree.

**Configuration:**
- `backend/pom.xml`: Maven dependencies and build plugins.
- `backend/src/main/resources/application.yml`: backend runtime configuration.
- `backend/src/main/resources/application-test.yml`: backend test configuration.
- `backend/docker-compose.yml`: local infrastructure.
- `frontend/package.json`: frontend npm scripts and dependencies.
- `frontend/angular.json`: Angular CLI build/test/serve configuration.
- `frontend/proxy.conf.json`: Angular dev proxy.

**Core Logic:**
- `backend/src/main/java/com/training/starter/service/impl`: backend business logic.
- `backend/src/main/java/com/training/starter/security`: JWT and Spring Security logic.
- `frontend/src/app/core/services`: client-side auth and notifications.
- `frontend/src/app/features`: user-facing feature behavior.

**Testing:**
- `backend/src/test/java/com/training/starter/service/UserServiceTest.java`: backend unit test reference.
- `backend/src/test/java/com/training/starter/BaseIntegrationTest.java`: Testcontainers base integration test.
- `frontend/src/app/**/*.spec.ts`: Angular test file convention.

## Naming Conventions

**Files:**
- Backend Java classes use PascalCase by role, such as `UserController.java`, `UserServiceImpl.java`, `UserRepository.java`.
- Backend migrations use Flyway names such as `V1__create_users_table.sql`.
- Frontend Angular files use kebab-case plus role suffix, such as `user-list.component.ts`, `auth.service.ts`, `jwt.interceptor.ts`.

**Directories:**
- Backend directories map to package/layer names: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`.
- Frontend feature directories group UI by user-facing feature: `features/auth`, `features/users`, `features/dashboard`.

## Where to Add New Code

**New Backend Entity Feature:**
- Entity: `backend/src/main/java/com/training/starter/entity`.
- Migration: `backend/src/main/resources/db/migration`.
- Repository: `backend/src/main/java/com/training/starter/repository`.
- DTOs: `backend/src/main/java/com/training/starter/dto/request` and `backend/src/main/java/com/training/starter/dto/response`.
- Mapper: `backend/src/main/java/com/training/starter/mapper`.
- Service contract: `backend/src/main/java/com/training/starter/service`.
- Service implementation: `backend/src/main/java/com/training/starter/service/impl`.
- Controller: `backend/src/main/java/com/training/starter/controller`.
- Tests: `backend/src/test/java/com/training/starter`.

**New Frontend Feature:**
- Primary code: `frontend/src/app/features/<feature-name>`.
- Shared reusable UI: `frontend/src/app/shared/components`.
- Cross-cutting service/guard/interceptor/model: `frontend/src/app/core`.
- Routes: `frontend/src/app/app.routes.ts`.

**Utilities:**
- Backend common response helpers: `backend/src/main/java/com/training/starter/common`.
- Frontend shared models/services: `frontend/src/app/core`.

## Special Directories

**`.codex`:**
- Purpose: Local GSD Core runtime files for Codex.
- Generated: Yes, by official GSD installer.
- Committed: Intended for project-local GSD setup unless team policy says otherwise.

**`.planning`:**
- Purpose: GSD project planning and codebase context.
- Generated: Yes, by GSD onboarding/mapping workflows.
- Committed: Intended documentation context.

**`backend/target`, `frontend/node_modules`, `frontend/dist`, `frontend/.angular/cache`:**
- Purpose: generated dependency/build/cache output.
- Generated: Yes.
- Committed: No.

---

*Structure analysis: 2026-07-22*
