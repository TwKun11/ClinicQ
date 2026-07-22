# Requirements

**Analysis Date:** 2026-07-22
**Source:** Existing repository behavior and README. Unconfirmed product goals are marked `Needs confirmation`.

## v1 Existing Capabilities

### Authentication

- [ ] **AUTH-01**: User can register through `POST /api/v1/auth/register`.
- [ ] **AUTH-02**: User can log in through `POST /api/v1/auth/login`.
- [ ] **AUTH-03**: User can refresh an access token through `POST /api/v1/auth/refresh`.
- [ ] **AUTH-04**: Frontend can protect application routes with `authGuard`.
- [ ] **AUTH-05**: Frontend can attach JWT credentials to API calls with `jwt.interceptor.ts`.

### Users

- [ ] **USER-01**: Authenticated user can list users with pagination and sorting.
- [ ] **USER-02**: Authenticated user can get a user by ID.
- [ ] **USER-03**: Authenticated user can create a user.
- [ ] **USER-04**: Authenticated user can update a user.
- [ ] **USER-05**: Authenticated user can delete a user.
- [ ] **USER-06**: Frontend provides user list and create/edit screens.

### Patients

- [ ] **PAT-01**: Authenticated user can list patients with pagination and sorting.
- [ ] **PAT-02**: Authenticated user can get a patient by ID.
- [ ] **PAT-03**: Authenticated user can create a patient.
- [ ] **PAT-04**: Authenticated user can update a patient.
- [ ] **PAT-05**: Authenticated user can delete a patient.

### Infrastructure

- [ ] **INFRA-01**: Developer can start local PostgreSQL, Redis, RabbitMQ, and MailHog with Docker Compose.
- [ ] **INFRA-02**: Backend schema changes run through Flyway migrations.
- [ ] **INFRA-03**: Backend API docs are available through Swagger UI.

## Needs Confirmation

- [ ] **AUTH-NC-01**: Role-based authorization requirements for admin, clinician, or other roles.
- [ ] **PAT-NC-01**: Patient workflows beyond CRUD.
- [ ] **OPS-NC-01**: Production deployment, CI/CD, monitoring, and backup requirements.
- [ ] **SEC-NC-01**: Compliance and privacy expectations for patient data.

## Out Of Scope For GSD Onboarding

- New product features.
- Database schema changes.
- Framework upgrades.
- Business logic changes.
- Secret rotation or environment rewrites.

## Traceability

Roadmap traceability is initialized in `.planning/ROADMAP.md` and should be updated when the user confirms the next feature phase.

