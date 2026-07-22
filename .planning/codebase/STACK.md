# Technology Stack

**Analysis Date:** 2026-07-22

## Languages

**Primary:**
- Java 17 - backend API in `backend/src/main/java/com/training/starter`.
- TypeScript 5.4 - Angular frontend in `frontend/src/app`.

**Secondary:**
- SQL - Flyway migrations in `backend/src/main/resources/db/migration`.
- SCSS - frontend styles in `frontend/src/styles.scss` and component styles.
- YAML - Spring configuration in `backend/src/main/resources/application.yml` and `backend/src/main/resources/application-test.yml`.

## Runtime

**Environment:**
- JVM 17+ for Spring Boot.
- Node.js 18+ required by `README.md`; current local Node is v24.18.0.

**Package Manager:**
- Maven Wrapper for backend: `backend/mvnw`, `backend/mvnw.cmd`, `backend/pom.xml`.
- npm 11.16.0 for frontend.
- Lockfile: `frontend/package-lock.json` present.

## Frameworks

**Core:**
- Spring Boot 3.2.5 - backend application parent in `backend/pom.xml`.
- Spring Web MVC - REST controllers in `backend/src/main/java/com/training/starter/controller`.
- Spring Security - stateless JWT security in `backend/src/main/java/com/training/starter/security`.
- Spring Data JPA - repositories in `backend/src/main/java/com/training/starter/repository`.
- Angular 17.3 - standalone-component frontend configured by `frontend/angular.json`.
- Angular Material 17.3 - UI dependency in `frontend/package.json`.

**Testing:**
- Spring Boot Test - backend unit/integration tests in `backend/src/test/java`.
- Spring Security Test - security test support in `backend/pom.xml`.
- Testcontainers 1.19.7 - PostgreSQL and RabbitMQ integration support in `backend/pom.xml`.
- Jasmine/Karma - Angular test runner dependencies in `frontend/package.json`.

**Build/Dev:**
- Maven Compiler Plugin with Lombok and MapStruct processors in `backend/pom.xml`.
- Spring Boot Maven Plugin in `backend/pom.xml`.
- Angular CLI build system in `frontend/angular.json`.

## Key Dependencies

**Critical:**
- `io.jsonwebtoken:jjwt-*` 0.12.5 - JWT creation and parsing in `backend/src/main/java/com/training/starter/security/JwtTokenProvider.java`.
- Lombok 1.18.32 - reduces boilerplate across backend DTOs, services, entities, and config.
- MapStruct 1.5.5.Final - DTO mapping in `backend/src/main/java/com/training/starter/mapper`.
- PostgreSQL JDBC driver - runtime database driver in `backend/pom.xml`.
- Flyway - database migration engine configured in `backend/src/main/resources/application.yml`.
- RxJS 7.8 - Angular async streams in frontend services/components.

**Infrastructure:**
- PostgreSQL 16 - relational database service in `backend/docker-compose.yml`.
- Redis 7 - cache/configured data service through `backend/src/main/java/com/training/starter/config/RedisConfig.java`.
- RabbitMQ 3 - messaging through `backend/src/main/java/com/training/starter/config/RabbitMQConfig.java`.
- MailHog - local mail testing service in `README.md`.
- SpringDoc OpenAPI 2.3.0 - Swagger/OpenAPI UI configured in `backend/src/main/java/com/training/starter/config/OpenApiConfig.java`.

## Configuration

**Environment:**
- Backend uses Spring YAML with environment variable placeholders in `backend/src/main/resources/application.yml`.
- Test profile overrides live in `backend/src/main/resources/application-test.yml`.
- Frontend environments live in `frontend/src/environments/environment.ts` and `frontend/src/environments/environment.prod.ts`.
- Secrets must be supplied through environment variables or local ignored files, not committed.

**Build:**
- Backend build config: `backend/pom.xml`.
- Frontend build config: `frontend/angular.json`, `frontend/tsconfig.json`, `frontend/tsconfig.app.json`, `frontend/tsconfig.spec.json`.
- Frontend dev proxy: `frontend/proxy.conf.json`.

## Platform Requirements

**Development:**
- JDK 17+.
- Maven Wrapper or Maven 3.9+.
- Node.js 18+ and npm.
- Docker and Docker Compose for PostgreSQL, Redis, RabbitMQ, and MailHog.

**Production:**
- Deployment target is not defined in the repository. Needs confirmation.

---

*Stack analysis: 2026-07-22*
