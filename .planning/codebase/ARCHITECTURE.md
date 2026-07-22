<!-- refreshed: 2026-07-22 -->
# Architecture

**Analysis Date:** 2026-07-22

## System Overview

```text
Browser Angular SPA
`frontend/src/app`
        |
        v
Spring Boot REST API
`backend/src/main/java/com/training/starter/controller`
        |
        v
Service layer
`backend/src/main/java/com/training/starter/service`
`backend/src/main/java/com/training/starter/service/impl`
        |
        v
Persistence and integrations
`backend/src/main/java/com/training/starter/repository`
`backend/src/main/resources/db/migration`
PostgreSQL / Redis / RabbitMQ / Mail
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| Application bootstrap | Starts Spring Boot backend | `backend/src/main/java/com/training/starter/StarterApplication.java` |
| Auth API | Register, login, refresh token endpoints | `backend/src/main/java/com/training/starter/controller/AuthController.java` |
| User API | Paginated CRUD reference implementation | `backend/src/main/java/com/training/starter/controller/UserController.java` |
| Patient API | Paginated CRUD for patients | `backend/src/main/java/com/training/starter/controller/PatientController.java` |
| Auth service | Authentication business logic | `backend/src/main/java/com/training/starter/service/impl/AuthServiceImpl.java` |
| User service | User CRUD business logic | `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java` |
| Patient service | Patient CRUD business logic | `backend/src/main/java/com/training/starter/service/impl/PatientServiceImpl.java` |
| Security config | Stateless JWT security chain | `backend/src/main/java/com/training/starter/security/SecurityConfig.java` |
| Frontend routing | Lazy standalone routes and auth guard | `frontend/src/app/app.routes.ts` |
| Frontend auth | Login/register/logout client logic | `frontend/src/app/core/services/auth.service.ts` |

## Pattern Overview

**Overall:** Layered full-stack application.

**Key Characteristics:**
- Backend controllers return `ApiResponse` wrappers and delegate business logic to service interfaces.
- Backend services use repositories, mappers, DTOs, and custom exceptions.
- Database schema is managed by Flyway migrations, not Hibernate auto-generation in the main profile.
- Frontend uses Angular standalone components with lazy `loadComponent` routes.
- Frontend HTTP auth is centralized in an interceptor.

## Layers

**Backend API Layer:**
- Purpose: Expose REST endpoints and validate request DTOs.
- Location: `backend/src/main/java/com/training/starter/controller`.
- Contains: `AuthController`, `UserController`, `PatientController`.
- Depends on: service interfaces, request/response DTOs, Spring MVC annotations.
- Used by: Angular frontend and external HTTP clients.

**Backend Service Layer:**
- Purpose: Own business rules and transactional workflows.
- Location: `backend/src/main/java/com/training/starter/service` and `backend/src/main/java/com/training/starter/service/impl`.
- Contains: service interfaces and implementations.
- Depends on: repositories, mappers, security utilities, exceptions.
- Used by: controllers.

**Persistence Layer:**
- Purpose: Store and retrieve JPA entities.
- Location: `backend/src/main/java/com/training/starter/repository`, `backend/src/main/java/com/training/starter/entity`.
- Contains: Spring Data repositories and JPA entities.
- Depends on: Spring Data JPA and PostgreSQL runtime driver.
- Used by: service implementations.

**Frontend Core Layer:**
- Purpose: Cross-cutting client behavior.
- Location: `frontend/src/app/core`.
- Contains: guards, interceptors, models, auth and notification services.
- Depends on: Angular Router, Angular HttpClient, Angular Material snackbar.
- Used by: layouts and feature components.

**Frontend Feature Layer:**
- Purpose: User-facing screens and feature API services.
- Location: `frontend/src/app/features`.
- Contains: auth pages, user list/form, dashboard.
- Depends on: core services/models and shared components.
- Used by: route configuration in `frontend/src/app/app.routes.ts`.

## Data Flow

### Auth Request Path

1. Login/register request enters `AuthController` at `/api/v1/auth/*` (`backend/src/main/java/com/training/starter/controller/AuthController.java`).
2. Controller validates DTOs with `@Valid` and delegates to `AuthService` (`backend/src/main/java/com/training/starter/service/AuthService.java`).
3. `AuthServiceImpl` authenticates, hashes or verifies passwords, and creates JWT responses (`backend/src/main/java/com/training/starter/service/impl/AuthServiceImpl.java`).
4. Response is wrapped in `ApiResponse` (`backend/src/main/java/com/training/starter/common/ApiResponse.java`).
5. Angular stores and attaches the token through `frontend/src/app/core/services/auth.service.ts` and `frontend/src/app/core/interceptors/jwt.interceptor.ts`.

### CRUD Request Path

1. Angular feature service calls `/api/v1/users` or `/api/v1/patients`.
2. JWT interceptor attaches bearer token (`frontend/src/app/core/interceptors/jwt.interceptor.ts`).
3. Spring Security validates JWT (`backend/src/main/java/com/training/starter/security/JwtAuthenticationFilter.java`).
4. Controller delegates to service implementation.
5. Service uses repository and mapper to read/write entities and return response DTOs.
6. Paginated responses use `PageResponse` (`backend/src/main/java/com/training/starter/common/PageResponse.java`).

**State Management:**
- Backend remains stateless for HTTP sessions with JWT.
- Frontend auth state is held in client-side auth service/storage. See `frontend/src/app/core/services/auth.service.ts`.

## Key Abstractions

**ApiResponse:**
- Purpose: Standard response envelope.
- Examples: `backend/src/main/java/com/training/starter/common/ApiResponse.java`.
- Pattern: static success/error builders and generic payload.

**PageResponse:**
- Purpose: Standard paginated payload.
- Examples: `backend/src/main/java/com/training/starter/common/PageResponse.java`.
- Pattern: maps Spring `Page<T>` content to response DTO lists.

**BaseEntity:**
- Purpose: Shared entity identity and timestamps.
- Examples: `backend/src/main/java/com/training/starter/entity/BaseEntity.java`.
- Pattern: inherited JPA mapped superclass/base class.

**Mappers:**
- Purpose: Entity-to-DTO and DTO-to-entity conversion.
- Examples: `backend/src/main/java/com/training/starter/mapper/UserMapper.java`, `backend/src/main/java/com/training/starter/mapper/PatientMapper.java`.
- Pattern: MapStruct interfaces.

## Entry Points

**Backend Application:**
- Location: `backend/src/main/java/com/training/starter/StarterApplication.java`.
- Triggers: `./mvnw spring-boot:run` or packaged Spring Boot launch.
- Responsibilities: Bootstraps Spring context.

**Frontend Application:**
- Location: `frontend/src/main.ts`.
- Triggers: Angular CLI dev server/build.
- Responsibilities: Bootstraps Angular app config.

**Frontend Routes:**
- Location: `frontend/src/app/app.routes.ts`.
- Triggers: Browser navigation.
- Responsibilities: Lazy-load layouts and feature components.

## Architectural Constraints

- **Threading:** Standard Spring MVC request-per-thread backend and Angular browser runtime.
- **Global state:** Security and infrastructure beans are Spring-managed singletons under `backend/src/main/java/com/training/starter/config` and `backend/src/main/java/com/training/starter/security`.
- **Circular imports:** Not detected.
- **Database schema:** Main profile uses Hibernate `ddl-auto: validate`; schema changes must be Flyway migrations.

## Anti-Patterns

### Exposing Entities Directly

**What happens:** Returning JPA entities from controllers would bypass DTO contracts.
**Why it's wrong:** It couples API shape to persistence and can leak fields.
**Do this instead:** Return response DTOs through mappers, as in `backend/src/main/java/com/training/starter/controller/UserController.java`.

### Bypassing Service Interfaces

**What happens:** Controllers using repositories directly would skip business rules.
**Why it's wrong:** It fragments validation, exception handling, and transaction boundaries.
**Do this instead:** Follow controller -> service interface -> implementation -> repository.

## Error Handling

**Strategy:** Centralized exception mapping in `GlobalExceptionHandler`.

**Patterns:**
- Throw custom exceptions from services.
- Use `@Valid` on controller request DTOs.
- Return consistent `ApiResponse` error payloads.

## Cross-Cutting Concerns

**Logging:** SLF4J via Lombok `@Slf4j`.
**Validation:** Jakarta Bean Validation on request DTOs with `MethodArgumentNotValidException` handling.
**Authentication:** Stateless JWT through Spring Security filter chain and Angular JWT interceptor.

---

*Architecture analysis: 2026-07-22*
