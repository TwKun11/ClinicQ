# Testing Patterns

**Analysis Date:** 2026-07-22

## Test Framework

**Runner:**
- Backend: JUnit Jupiter through `spring-boot-starter-test` in `backend/pom.xml`.
- Backend unit mocking: Mockito JUnit Jupiter extension in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Backend integration support: Spring Boot Test plus Testcontainers in `backend/src/test/java/com/training/starter/BaseIntegrationTest.java`.
- Frontend: Angular CLI Karma/Jasmine builder configured in `frontend/angular.json`.
- Config: `backend/pom.xml`, `frontend/angular.json`, and `frontend/tsconfig.spec.json`.

**Assertion Library:**
- Backend: AssertJ static assertions, including `assertThat` and `assertThatThrownBy`, in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Backend mocks/verifications: Mockito static helpers including `when`, `verify`, `never`, and `any` in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Frontend: Jasmine is configured through `@types/jasmine`, `jasmine-core`, `karma-jasmine`, and `karma-jasmine-html-reporter` in `frontend/package.json`; no committed `*.spec.ts` tests are detected.

**Run Commands:**
```bash
cd backend && ./mvnw test      # Run all backend tests
cd frontend && npm test        # Run Angular Karma/Jasmine tests
cd frontend && npm run build   # Compile Angular app; no lint script is detected
```

## Test File Organization

**Location:**
- Backend tests live under `backend/src/test/java/com/training/starter`, mirroring main package structure.
- Backend service unit tests live in subpackages such as `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Shared backend integration setup lives at `backend/src/test/java/com/training/starter/BaseIntegrationTest.java`.
- Frontend test discovery is configured for `frontend/src/**/*.spec.ts` in `frontend/tsconfig.spec.json`, but no committed frontend spec files are detected.

**Naming:**
- Backend test classes use `{Subject}Test`, as in `UserServiceTest` in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Backend test methods use `method_condition_expectedResult`, such as `create_validRequest_returnsUserResponse`, `create_duplicateUsername_throwsDuplicateResourceException`, and `getById_notFound_throwsResourceNotFoundException` in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Frontend specs should use Angular CLI naming, `*.spec.ts`, because `frontend/tsconfig.spec.json` includes `src/**/*.spec.ts`.

**Structure:**
```text
backend/src/test/java/com/training/starter/
|-- BaseIntegrationTest.java
`-- service/
    `-- UserServiceTest.java

frontend/src/
`-- **/*.spec.ts
```

## Test Structure

**Suite Organization:**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void create_validRequest_returnsUserResponse() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        // When
        var result = userService.create(request);

        // Then
        assertThat(result.username()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
    }
}
```

**Patterns:**
- Use `@ExtendWith(MockitoExtension.class)` for focused backend service unit tests, as in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Use `@Mock` for repository, mapper, and infrastructure collaborators; use `@InjectMocks` for the service implementation under test in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Use Given/When/Then blocks inside test methods in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Use private helper methods for repeated entity construction, such as `buildUser` in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Use `BaseIntegrationTest` only when Spring context and real backing services are needed.

## Mocking

**Framework:** Mockito for backend unit tests; Angular/Jasmine test utilities are configured but no frontend mocking examples are committed.

**Patterns:**
```java
@Mock
private UserRepository userRepository;

@Mock
private UserMapper userMapper;

@Mock
private PasswordEncoder passwordEncoder;

@InjectMocks
private UserServiceImpl userService;

when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
verify(userMapper).updateEntity(entity, request);
verify(userRepository, never()).save(any());
```

**What to Mock:**
- Mock repositories, mappers, encoders, and external infrastructure collaborators in service unit tests, following `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Mock duplicate checks and lookup outcomes explicitly with `when(...)` so service branches are deterministic.
- For frontend tests, mock HTTP-facing services such as `UserService` or use Angular HTTP testing utilities when adding specs for components under `frontend/src/app/features`.

**What NOT to Mock:**
- Do not mock the service implementation under test; instantiate it through `@InjectMocks` as in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Do not mock DTO records or simple entities; build them directly, as with `CreateUserRequest`, `UpdateUserRequest`, `UserResponse`, and `User` in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Do not use Mockito unit tests when the behavior depends on Spring wiring, Flyway migrations, database behavior, Redis, or RabbitMQ; extend `BaseIntegrationTest` instead.

## Fixtures and Factories

**Test Data:**
```java
private User buildUser(Long id, String username, String email) {
    User user = User.builder()
            .username(username)
            .email(email)
            .password("encoded")
            .fullName("Test User")
            .role(Role.USER)
            .active(true)
            .build();
    user.setId(id);
    return user;
}
```

**Location:**
- Local helper methods inside test classes are the current fixture pattern, as in `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- No shared fixture/factory directory is detected.
- Testcontainers fixture setup is centralized in `backend/src/test/java/com/training/starter/BaseIntegrationTest.java`.

## Coverage

**Requirements:** None enforced. No JaCoCo, frontend coverage threshold, or coverage gate is detected in `backend/pom.xml` or `frontend/angular.json`.

**View Coverage:**
```bash
cd frontend && npm test -- --code-coverage    # Angular/Karma coverage output when needed
cd backend && ./mvnw test                     # Backend tests only; no coverage report plugin is configured
```

## Test Types

**Unit Tests:**
- Backend service unit tests are the established pattern. Use Mockito to isolate service logic and AssertJ to assert returned DTO values and exception branches, following `backend/src/test/java/com/training/starter/service/UserServiceTest.java`.
- Cover success paths, duplicate/resource-not-found branches, update behavior, delete behavior, and collaborator calls with `verify`.

**Integration Tests:**
- Backend integration tests should extend `BaseIntegrationTest`, which starts PostgreSQL, Redis, and RabbitMQ containers and exposes a `TestRestTemplate` in `backend/src/test/java/com/training/starter/BaseIntegrationTest.java`.
- Use integration tests for controller/security/repository behavior that needs the Spring context, HTTP boundary, database schema, or infrastructure properties.

**E2E Tests:**
- Not used. No Playwright, Cypress, Selenium, or Angular e2e project is detected.

## Common Patterns

**Async Testing:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }
}
```

**Error Testing:**
```java
assertThatThrownBy(() -> userService.create(request))
        .isInstanceOf(DuplicateResourceException.class);
verify(userRepository, never()).save(any());
```

---

*Testing analysis: 2026-07-22*
