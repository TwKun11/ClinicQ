# External Integrations

**Analysis Date:** 2026-07-22

## APIs & External Services

**OpenAPI Documentation:**
- Swagger UI - documents backend REST APIs.
  - SDK/Client: `org.springdoc:springdoc-openapi-starter-webmvc-ui` in `backend/pom.xml`.
  - Auth: JWT bearer scheme configured in `backend/src/main/java/com/training/starter/config/OpenApiConfig.java`.

**Frontend API Calls:**
- Angular frontend calls the Spring Boot API through services such as `frontend/src/app/core/services/auth.service.ts` and `frontend/src/app/features/users/user.service.ts`.
  - SDK/Client: Angular `HttpClient`.
  - Auth: JWT token attached by `frontend/src/app/core/interceptors/jwt.interceptor.ts`.

## Data Storage

**Databases:**
- PostgreSQL.
  - Connection: environment variables configured through `backend/src/main/resources/application.yml`.
  - Client: Spring Data JPA repositories in `backend/src/main/java/com/training/starter/repository`.
  - Migrations: Flyway SQL files in `backend/src/main/resources/db/migration`.

**File Storage:**
- Not detected.

**Caching:**
- Redis.
  - Connection: environment variables configured through `backend/src/main/resources/application.yml`.
  - Client/configuration: `backend/src/main/java/com/training/starter/config/RedisConfig.java`.

## Messaging

**Queues:**
- RabbitMQ.
  - Connection: environment variables configured through `backend/src/main/resources/application.yml`.
  - Configuration: `backend/src/main/java/com/training/starter/config/RabbitMQConfig.java`.

## Authentication & Identity

**Auth Provider:**
- Custom JWT authentication.
  - Login/register/refresh endpoints: `backend/src/main/java/com/training/starter/controller/AuthController.java`.
  - Implementation: `backend/src/main/java/com/training/starter/service/impl/AuthServiceImpl.java`.
  - Security filter: `backend/src/main/java/com/training/starter/security/JwtAuthenticationFilter.java`.
  - Token provider: `backend/src/main/java/com/training/starter/security/JwtTokenProvider.java`.
  - Password hashing: BCrypt bean in `backend/src/main/java/com/training/starter/security/SecurityConfig.java`.

## Monitoring & Observability

**Error Tracking:**
- Not detected.

**Logs:**
- Backend uses SLF4J via Lombok `@Slf4j`, including `backend/src/main/java/com/training/starter/exception/GlobalExceptionHandler.java`.
- Logging levels are configured in `backend/src/main/resources/application.yml`.

## CI/CD & Deployment

**Hosting:**
- Not detected.

**CI Pipeline:**
- Not detected.

## Environment Configuration

**Required env vars:**
- Database, Redis, RabbitMQ, Mail, and JWT settings are referenced by name in `backend/src/main/resources/application.yml`.
- Frontend API base URL is configured in `frontend/src/environments/environment.ts` and `frontend/src/environments/environment.prod.ts`.

**Secrets location:**
- Committed secrets files are not expected. `.env` and `.env.*` are ignored by `.gitignore`.
- Do not print or commit actual secret values.

## Webhooks & Callbacks

**Incoming:**
- REST API routes under `/api/v1/auth`, `/api/v1/users`, and `/api/v1/patients`.

**Outgoing:**
- Not detected.

---

*Integration audit: 2026-07-22*
