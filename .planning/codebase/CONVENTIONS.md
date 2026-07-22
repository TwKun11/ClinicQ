# Coding Conventions

**Analysis Date:** 2026-07-22

## Naming Patterns

**Files:**
- Backend Java files use one public type per file under package folders matching `com.training.starter`, such as `backend/src/main/java/com/training/starter/controller/UserController.java` and `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`.
- Backend domain types use suffixes that describe their layer: `Controller`, `Service`, `ServiceImpl`, `Repository`, `Mapper`, `Request`, `Response`, and `Exception`, as shown by `backend/src/main/java/com/training/starter/controller/PatientController.java`, `backend/src/main/java/com/training/starter/service/PatientService.java`, and `backend/src/main/java/com/training/starter/dto/request/CreatePatientRequest.java`.
- Frontend Angular files use kebab-case with Angular role suffixes, including `frontend/src/app/features/users/user-list/user-list.component.ts`, `frontend/src/app/features/users/user-form/user-form.component.ts`, and `frontend/src/app/core/interceptors/jwt.interceptor.ts`.
- Frontend services use `*.service.ts` and colocate feature API services with the feature, as in `frontend/src/app/features/users/user.service.ts`; cross-cutting services live under `frontend/src/app/core/services/auth.service.ts` and `frontend/src/app/core/services/notification.service.ts`.

**Functions:**
- Backend service and controller methods use lower camelCase verbs matching CRUD actions: `getAll`, `getById`, `create`, `update`, and `delete` in `backend/src/main/java/com/training/starter/service/UserService.java`.
- Backend mapper methods name conversion direction explicitly: `toResponse`, `toEntity`, and `updateEntity` in `backend/src/main/java/com/training/starter/mapper/UserMapper.java`.
- Frontend component methods use lower camelCase event/action names such as `ngOnInit`, `loadUsers`, `onPageChange`, `onDelete`, `onSubmit`, and `onCancel` in `frontend/src/app/features/users/user-list/user-list.component.ts` and `frontend/src/app/features/users/user-form/user-form.component.ts`.

**Variables:**
- Backend fields and local variables use lower camelCase: `userRepository`, `passwordEncoder`, `sortBy`, and `sortDir` in `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java` and `backend/src/main/java/com/training/starter/controller/UserController.java`.
- Frontend component state uses explicit lower camelCase properties: `users`, `displayedColumns`, `totalElements`, `pageSize`, `currentPage`, `isEdit`, `loading`, and `userId` in `frontend/src/app/features/users/user-list/user-list.component.ts` and `frontend/src/app/features/users/user-form/user-form.component.ts`.
- Frontend service URLs are private lower camelCase fields built from environment config, for example `private apiUrl = \`${environment.apiUrl}/users\`` in `frontend/src/app/features/users/user.service.ts`.

**Types:**
- Backend request/response DTOs are Java records with noun-based names: `CreateUserRequest`, `UpdateUserRequest`, `UserResponse`, and `AuthResponse` in `backend/src/main/java/com/training/starter/dto/request/CreateUserRequest.java` and `backend/src/main/java/com/training/starter/dto/response/UserResponse.java`.
- Backend entities are singular nouns extending `BaseEntity`, such as `User` in `backend/src/main/java/com/training/starter/entity/User.java` and `Patient` in `backend/src/main/java/com/training/starter/entity/Patient.java`.
- Frontend models and API payload types are TypeScript interfaces exported from service/model files, including `User`, `CreateUserRequest`, and `UpdateUserRequest` in `frontend/src/app/features/users/user.service.ts` and `ApiResponse<T>` in `frontend/src/app/core/models/api-response.model.ts`.

## Code Style

**Formatting:**
- No Prettier, ESLint, Biome, Checkstyle, Spotless, or frontend lint config is detected in the repository.
- Backend formatting follows common Spring Java style with 4-space indentation, annotation blocks above declarations, grouped imports, blank lines between methods, and line-wrapped fluent calls, as shown in `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`.
- Frontend formatting follows Angular CLI defaults: 2-space indentation, single quotes, semicolons, inline templates for existing standalone components, and trailing commas only where TypeScript formatting naturally uses them, as shown in `frontend/src/app/features/users/user-form/user-form.component.ts`.
- Angular component metadata keeps `standalone: true` and lists required Angular Material/modules in `imports`, as in `frontend/src/app/features/users/user-list/user-list.component.ts`.

**Linting:**
- Not detected for backend or frontend; `backend/pom.xml` has no lint plugin and `frontend/package.json` has no lint script.
- Use the compiler and tests as the current quality gate: `backend/mvnw test` for Java and `npm run build` or `npm test` in `frontend` for Angular.

## Import Organization

**Order:**
1. Backend imports place project packages first, then third-party packages, then Java standard library, then static imports in tests; see `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
2. Frontend imports place Angular framework modules first, Angular Material modules next, then RxJS/environment/core/shared/feature imports; see `frontend/src/app/features/users/user-list/user-list.component.ts`.
3. Static imports in backend tests are grouped last and split by assertion library and Mockito helpers in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.

**Path Aliases:**
- No TypeScript path aliases are configured in `frontend/tsconfig.json`; frontend code uses relative imports such as `../../../core/services/notification.service` in `frontend/src/app/features/users/user-form/user-form.component.ts`.
- Backend packages use normal Java package imports under `com.training.starter`, such as `com.training.starter.common.ApiResponse` in `backend/src/main/java/com/training/starter/controller/UserController.java`.

## Error Handling

**Patterns:**
- Backend services throw typed domain exceptions instead of returning null or error wrappers, for example `ResourceNotFoundException` and `DuplicateResourceException` in `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`.
- Backend controllers return `ApiResponse<T>` for successful JSON responses and `PageResponse<T>` for paginated data, as in `backend/src/main/java/com/training/starter/controller/UserController.java`.
- Backend validation belongs on request DTO records with Jakarta Validation annotations, as in `backend/src/main/java/com/training/starter/dto/request/CreateUserRequest.java`.
- `GlobalExceptionHandler` converts known exceptions to API responses and HTTP statuses in `backend/src/main/java/com/training/starter/exception/GlobalExceptionHandler.java`.
- Frontend components handle service errors in `subscribe({ error: ... })` callbacks and show messages through `NotificationService`, as in `frontend/src/app/features/users/user-list/user-list.component.ts`.
- Frontend form submit handlers guard invalid forms before API calls, as in `frontend/src/app/features/users/user-form/user-form.component.ts`.

## Logging

**Framework:** SLF4J via Lombok `@Slf4j` on backend exception handling; Angular Material snack bars for frontend user-facing notifications.

**Patterns:**
- Backend only logs unexpected generic exceptions in `GlobalExceptionHandler.handleGeneral` in `backend/src/main/java/com/training/starter/exception/GlobalExceptionHandler.java`.
- Known business and validation exceptions return sanitized messages without logging in `backend/src/main/java/com/training/starter/exception/GlobalExceptionHandler.java`.
- Frontend user-visible success/error notifications go through `NotificationService` in `frontend/src/app/core/services/notification.service.ts` rather than direct `console` calls.
- No application `console.log` usage is detected in `frontend/src/app`.

## Comments

**When to Comment:**
- Backend tests use short Given/When/Then comments to mark arrange, act, and assert phases in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Backend production code is mostly self-documenting through annotations and names; add comments only for non-obvious behavior.
- Frontend production code has minimal comments; prefer clear method names and typed interfaces over explanatory comments.

**JSDoc/TSDoc:**
- Not detected in frontend source. Do not introduce broad JSDoc unless a public API or complex behavior needs it.
- Backend Swagger annotations document REST operations with `@Operation` and `@Tag`, as in `backend/src/main/java/com/training/starter/controller/UserController.java`.

## Function Design

**Size:** Keep methods focused on one layer responsibility. Controllers should assemble request parameters and delegate to services, as `UserController.getAll` does in `backend/src/main/java/com/training/starter/controller/UserController.java`. Services should contain business checks and persistence coordination, as in `UserServiceImpl.create` in `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`.

**Parameters:** Backend APIs accept typed DTO records for request bodies and framework types for pagination (`Pageable`) in `backend/src/main/java/com/training/starter/service/UserService.java`. Frontend service methods accept typed interfaces and primitive IDs in `frontend/src/app/features/users/user.service.ts`.

**Return Values:** Backend services return DTOs or `Page<DTO>` and never expose entities through controllers, as shown by `UserService` and `UserController`. Frontend HTTP services return `Observable<ApiResponse<T>>` or `Observable<ApiResponse<PageResponse<T>>>` in `frontend/src/app/features/users/user.service.ts`.

## Module Design

**Exports:** Backend modules expose Spring beans through annotations (`@RestController`, `@Service`, `@Mapper`, `@Repository`) and interfaces where the service contract is separate from implementation, as in `backend/src/main/java/com/training/starter/service/UserService.java` and `backend/src/main/java/com/training/starter/service/impl/UserServiceImpl.java`.

**Barrel Files:** Not detected. Frontend imports point directly to concrete files rather than `index.ts` barrel files, as in `frontend/src/app/features/users/user-list/user-list.component.ts`.

---

*Convention analysis: 2026-07-22*
