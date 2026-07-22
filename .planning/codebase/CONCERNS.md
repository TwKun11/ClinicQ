# Codebase Concerns

**Analysis Date:** 2026-07-22

## Tech Debt

**Role and authorization model:**
- Issue: Authenticated users can reach all non-public endpoints, but controller methods do not enforce role-specific access for user administration or patient records.
- Files: `backend/src/main/java/com/training/starter/security/SecurityConfig.java`, `backend/src/main/java/com/training/starter/controller/UserController.java`, `backend/src/main/java/com/training/starter/controller/PatientController.java`, `backend/src/main/java/com/training/starter/enums/Role.java`
- Impact: Any valid account can list, create, edit, and delete users and patients once it has a JWT.
- Fix approach: Add method-level authorization such as `@PreAuthorize` on controller/service methods, define admin/clinical roles, and cover role behavior with security tests.

**Refresh tokens are stateless and indistinguishable from access tokens:**
- Issue: Refresh tokens are generated with the same claims shape as access tokens and only differ by expiration duration.
- Files: `backend/src/main/java/com/training/starter/security/JwtTokenProvider.java`, `backend/src/main/java/com/training/starter/service/impl/AuthServiceImpl.java`, `backend/src/main/java/com/training/starter/dto/request/RefreshTokenRequest.java`
- Impact: Tokens cannot be revoked, rotated safely, or invalidated on logout; stolen refresh tokens remain usable until expiry.
- Fix approach: Add token type claims, store refresh token identifiers or hashes server-side, rotate refresh tokens, and reject reused or revoked tokens.

**Frontend auth state is token-presence based:**
- Issue: The route guard treats any `access_token` in local storage as authenticated without checking expiry or token validity.
- Files: `frontend/src/app/core/guards/auth.guard.ts`, `frontend/src/app/core/services/auth.service.ts`, `frontend/src/app/core/interceptors/jwt.interceptor.ts`
- Impact: Expired or malformed tokens can unlock protected UI until an API call fails; user experience depends on reactive 401 handling.
- Fix approach: Decode and validate token expiry client-side for routing, add a refresh flow before expiry, and keep server-side authorization authoritative.

**Inline component templates and styles limit maintainability:**
- Issue: Several Angular components contain substantial inline HTML and inline styles.
- Files: `frontend/src/app/features/users/user-list/user-list.component.ts`, `frontend/src/app/features/users/user-form/user-form.component.ts`, `frontend/src/app/features/auth/login/login.component.ts`, `frontend/src/app/features/auth/register/register.component.ts`
- Impact: UI changes are harder to review, test, and reuse as feature screens grow.
- Fix approach: Move larger templates/styles into component template and SCSS files when adding behavior, while keeping standalone component structure.

## Known Bugs

**Delete endpoints return a raw empty 204 instead of the API response envelope:**
- Symptoms: Delete operations do not use `ApiResponse`, unlike create/update/get endpoints.
- Files: `backend/src/main/java/com/training/starter/controller/UserController.java`, `backend/src/main/java/com/training/starter/controller/PatientController.java`, `frontend/src/app/features/users/user-list/user-list.component.ts`
- Trigger: Call `DELETE /api/v1/users/{id}` or `DELETE /api/v1/patients/{id}`.
- Workaround: Frontend currently treats a successful empty response as success; API consumers must special-case delete responses.

**Invalid pagination or sort parameters surface as generic errors:**
- Symptoms: `Sort.Direction.fromString(sortDir)` and `PageRequest.of(page, size, sort)` can throw runtime exceptions that are handled by the generic 500 handler.
- Files: `backend/src/main/java/com/training/starter/controller/UserController.java`, `backend/src/main/java/com/training/starter/controller/PatientController.java`, `backend/src/main/java/com/training/starter/exception/GlobalExceptionHandler.java`
- Trigger: Pass invalid `sortDir`, negative `page`, negative `size`, or an unknown `sortBy` to list endpoints.
- Workaround: Clients must send known-good pagination and sort values.

**Inactive users can still authenticate unless user details disables them:**
- Symptoms: The service layer exposes an `active` flag, but auth service token generation does not explicitly reject inactive users.
- Files: `backend/src/main/java/com/training/starter/entity/User.java`, `backend/src/main/java/com/training/starter/service/impl/AuthServiceImpl.java`, `backend/src/main/java/com/training/starter/security/UserDetailsServiceImpl.java`, `backend/src/main/java/com/training/starter/dto/request/UpdateUserRequest.java`
- Trigger: Mark a user inactive, then attempt login or refresh-token use for that account.
- Workaround: Confirm `UserDetailsServiceImpl` enforces `active` before relying on the flag; add explicit auth tests for inactive users.

## Security Considerations

**Patient data lacks access controls and audit trail:**
- Risk: Patient records include contact details, date of birth, address, and medical notes but have only broad authenticated access.
- Files: `backend/src/main/java/com/training/starter/entity/Patient.java`, `backend/src/main/resources/db/migration/V2__create_patients_tables.sql`, `backend/src/main/java/com/training/starter/controller/PatientController.java`
- Current mitigation: Endpoints require a JWT through `backend/src/main/java/com/training/starter/security/SecurityConfig.java`.
- Recommendations: Add role/ownership authorization, audit logging for read/write/delete events, and define data retention and masking rules before production use.

**JWTs stored in localStorage:**
- Risk: Access and refresh tokens are readable by any injected JavaScript running in the browser context.
- Files: `frontend/src/app/core/services/auth.service.ts`, `frontend/src/app/core/interceptors/jwt.interceptor.ts`
- Current mitigation: Backend uses stateless bearer tokens and clears local storage on 401.
- Recommendations: Harden XSS defenses, avoid rendering untrusted HTML, consider HttpOnly secure cookies for refresh tokens, and add token refresh/revocation.

**Actuator endpoints are publicly permitted:**
- Risk: Operational endpoints can disclose health, configuration shape, or runtime information if exposed by configuration.
- Files: `backend/src/main/java/com/training/starter/security/SecurityConfig.java`
- Current mitigation: Only the routes are permitted here; actual exposed actuator endpoints depend on runtime configuration.
- Recommendations: Restrict `/actuator/**` to admin/internal networks or permit only the minimum health endpoint required by deployment.

**CSRF is disabled globally:**
- Risk: Safe for pure bearer-token APIs, but unsafe if browser cookies are later introduced without revisiting security configuration.
- Files: `backend/src/main/java/com/training/starter/security/SecurityConfig.java`
- Current mitigation: Sessions are stateless and frontend sends bearer tokens.
- Recommendations: Reassess CSRF if cookie auth, server-side sessions, or browser credentials are introduced.

## Performance Bottlenecks

**Unbounded or weakly bounded page size:**
- Problem: List endpoints accept arbitrary `size` values.
- Files: `backend/src/main/java/com/training/starter/controller/UserController.java`, `backend/src/main/java/com/training/starter/controller/PatientController.java`
- Cause: Controllers pass request parameters directly to `PageRequest.of`.
- Improvement path: Enforce maximum page sizes through validation, shared pageable helpers, or Spring Data web pageable configuration.

**List screens refetch full page after deletes:**
- Problem: User delete success reloads the current page from the server.
- Files: `frontend/src/app/features/users/user-list/user-list.component.ts`, `frontend/src/app/features/users/user.service.ts`
- Cause: The component has no local optimistic update or cache strategy.
- Improvement path: Keep refetch for correctness at small scale, but add local removal or query invalidation patterns when list screens become high-traffic.

**No search or filtered indexes for patient/user lists:**
- Problem: CRUD list endpoints only expose full-table pagination sorted by client-selected fields.
- Files: `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`, `backend/src/main/java/com/training/starter/service/impl/PatientServiceImpl.java`, `backend/src/main/resources/db/migration/V1__create_users_table.sql`, `backend/src/main/resources/db/migration/V2__create_patients_tables.sql`
- Cause: Repositories use `findAll(pageable)` with only username/email/phone indexes.
- Improvement path: Add use-case-specific search endpoints, validated sort fields, and indexes for common patient lookup fields.

## Fragile Areas

**Duplicate checks depend on application-level preflight:**
- Files: `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`, `backend/src/main/java/com/training/starter/service/impl/AuthServiceImpl.java`, `backend/src/main/java/com/training/starter/service/impl/PatientServiceImpl.java`, `backend/src/main/resources/db/migration/V1__create_users_table.sql`, `backend/src/main/resources/db/migration/V2__create_patients_tables.sql`
- Why fragile: Concurrent creates can pass `existsBy...` checks and then fail on database unique constraints.
- Safe modification: Keep database uniqueness, but catch `DataIntegrityViolationException` and map it to the same conflict response.
- Test coverage: No concurrent create or database constraint integration tests are present.

**Dynamic sort fields are not allowlisted:**
- Files: `backend/src/main/java/com/training/starter/controller/UserController.java`, `backend/src/main/java/com/training/starter/controller/PatientController.java`
- Why fragile: Renaming entity fields or accepting invalid sort fields can break endpoints at runtime.
- Safe modification: Define endpoint-specific allowed sort field sets and map API sort names to entity properties.
- Test coverage: No controller tests cover invalid sorting.

**MapStruct partial updates rely on mapper null-handling configuration:**
- Files: `backend/src/main/java/com/training/starter/mapper/UserMapper.java`, `backend/src/main/java/com/training/starter/mapper/PatientMapper.java`, `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`, `backend/src/main/java/com/training/starter/service/impl/PatientServiceImpl.java`
- Why fragile: Update DTOs use nullable fields, so mapper behavior determines whether omitted fields preserve or erase existing data.
- Safe modification: Keep `NullValuePropertyMappingStrategy.IGNORE` explicit on update mappers and test each nullable update field.
- Test coverage: User update has one service test; patient update and null-preservation paths are not covered.

## Scaling Limits

**Single Spring Boot API with synchronous CRUD flow:**
- Current capacity: Not defined in repo; request handling is conventional synchronous Spring MVC.
- Limit: Long-running workflows, notifications, or reporting will tie up request threads unless moved off the request path.
- Scaling path: Use the existing RabbitMQ dependency/configuration for asynchronous workflows and add observability around queue latency.

**Database schema has basic indexes only:**
- Current capacity: Indexes exist for username, email, and phone uniqueness/lookup.
- Limit: Patient lookup by name, date of birth, status, or medical workflow fields will scan or sort inefficiently as data grows.
- Scaling path: Add fields and indexes based on real query patterns, and introduce migrations only for active use cases.

**Frontend has no state/query abstraction:**
- Current capacity: Each component calls services directly and owns its own loading/error state.
- Limit: Cross-screen cache coherence, refresh-on-login, optimistic updates, and shared error handling become repetitive.
- Scaling path: Add a lightweight shared data-access pattern only when multiple feature modules need it.

## Dependencies at Risk

**Spring Boot 3.2.5 and springdoc 2.3.0:**
- Risk: Framework and OpenAPI dependencies are pinned to older minor versions for the current date.
- Impact: Security fixes, compatibility fixes, and Spring ecosystem support can lag.
- Migration plan: Review official Spring Boot and springdoc release notes before upgrade, then run backend tests and OpenAPI smoke checks.

**Angular 17.3 toolchain:**
- Risk: Angular dependencies are pinned to the 17.x generation.
- Impact: Browser tooling, TypeScript compatibility, and security fixes can drift from current ecosystem support.
- Migration plan: Use `ng update` through supported major-version steps, then run `npm test` and `npm run build`.

**JJWT 0.12.5:**
- Risk: JWT security libraries need close version monitoring.
- Impact: Token parsing/signing behavior and vulnerability fixes affect authentication directly.
- Migration plan: Track JJWT releases, keep token tests around valid/expired/tampered/type-specific tokens, and upgrade deliberately.

## Missing Critical Features

**Password reset and account recovery:**
- Problem: Users can register and log in, but cannot recover accounts or reset compromised passwords.
- Blocks: Production authentication workflows and user support.

**Refresh flow and logout invalidation:**
- Problem: Frontend stores refresh tokens but never uses them, and backend has no logout/revocation endpoint.
- Blocks: Smooth session renewal, forced logout, incident response, and device/session management.

**Patient feature frontend:**
- Problem: Backend exposes patient CRUD, but no patient Angular feature is present.
- Blocks: Users cannot manage patients through the UI despite available API endpoints.

**Authorization policy for roles:**
- Problem: Roles exist in the backend response/model, but no endpoint policy differentiates admin, clinician, or normal user capabilities.
- Blocks: Safe multi-user clinic workflows and least-privilege access.

**Operational readiness:**
- Problem: No CI config, coverage threshold, deployment config, or production observability integration is detected.
- Blocks: Repeatable release quality gates and production incident visibility.

## Test Coverage Gaps

**Authentication service and JWT behavior:**
- What's not tested: Register/login/refresh flows, invalid refresh tokens, inactive users, expired tokens, tampered tokens, token type separation, and JWT filter behavior.
- Files: `backend/src/main/java/com/training/starter/service/impl/AuthServiceImpl.java`, `backend/src/main/java/com/training/starter/security/JwtTokenProvider.java`, `backend/src/main/java/com/training/starter/security/JwtAuthenticationFilter.java`
- Risk: Authentication regressions can ship without detection.
- Priority: High

**Authorization and controller security:**
- What's not tested: Which roles can access user and patient endpoints.
- Files: `backend/src/main/java/com/training/starter/security/SecurityConfig.java`, `backend/src/main/java/com/training/starter/controller/UserController.java`, `backend/src/main/java/com/training/starter/controller/PatientController.java`
- Risk: Privilege escalation and accidental public access can go unnoticed.
- Priority: High

**Patient service behavior:**
- What's not tested: Create/update/delete patient logic, duplicate phone/email handling, nullable email handling, and not-found paths.
- Files: `backend/src/main/java/com/training/starter/service/impl/PatientServiceImpl.java`, `backend/src/main/java/com/training/starter/mapper/PatientMapper.java`
- Risk: Patient CRUD regressions affect sensitive domain data.
- Priority: High

**Controller validation and API envelope consistency:**
- What's not tested: Validation error payloads, invalid pagination/sort handling, and delete response shape.
- Files: `backend/src/main/java/com/training/starter/controller/AuthController.java`, `backend/src/main/java/com/training/starter/controller/UserController.java`, `backend/src/main/java/com/training/starter/controller/PatientController.java`, `backend/src/main/java/com/training/starter/exception/GlobalExceptionHandler.java`
- Risk: Clients can break on untested API contract changes.
- Priority: Medium

**Frontend auth and user UI:**
- What's not tested: Login/register token storage, route guard behavior, JWT interceptor 401 handling, user list pagination, delete confirmation flow, and user form create/update behavior.
- Files: `frontend/src/app/core/services/auth.service.ts`, `frontend/src/app/core/guards/auth.guard.ts`, `frontend/src/app/core/interceptors/jwt.interceptor.ts`, `frontend/src/app/features/users/user-list/user-list.component.ts`, `frontend/src/app/features/users/user-form/user-form.component.ts`
- Risk: Frontend auth and CRUD workflows can regress without test failures.
- Priority: High

---

*Concerns audit: 2026-07-22*
