# Project State

**Updated:** 2026-07-23

## Current Status

GSD Core is installed locally for Codex in `.codex/`.

The existing codebase has been mapped into `.planning/codebase/`.

Planning baseline files are present:

- `.planning/PROJECT.md`
- `.planning/REQUIREMENTS.md`
- `.planning/ROADMAP.md`
- `.planning/STATE.md`

## Current Branch

`feature/gsd-core-integration`

## Known Local Changes

- `backend/src/main/resources/application.yml` had an uncommitted change before GSD onboarding work. Preserve it unless the user explicitly asks to edit backend configuration.

## Next Recommended GSD Step

Run:

```text
$gsd-discuss-phase 1
```

Phase 1 should clarify verification blockers and decide whether to fix the Maven wrapper/dependency setup in this branch or leave it as environment documentation.

## Verification Notes

- GSD Core local install succeeded with version 1.8.0.
- Backend Maven wrapper test command currently fails to start Maven in this environment.
- Frontend build currently requires `npm install` because dependencies are not installed.
- Backend search/filter quick task verified with `mvn test` on 2026-07-23: 26 tests passed/loaded with 2 Docker-dependent PostgreSQL integration tests skipped because Docker was unavailable.

### Quick Tasks Completed

| # | Description | Date | Commit | Directory |
|---|-------------|------|--------|-----------|
| 260723-m1m | Add backend search and filter APIs for patients and appointments | 2026-07-23 | uncommitted | [260723-m1m-add-backend-search-and-filter-apis-for-p](./quick/260723-m1m-add-backend-search-and-filter-apis-for-p/) |
| 20260724-auth | Add email verification auth and password flows | 2026-07-24 | uncommitted | [20260724-auth-email-verification-passwords](./quick/20260724-auth-email-verification-passwords/) |
| 20260724-google-auth | Add Google login and role-based auth routing | 2026-07-24 | uncommitted | [20260724-google-login-role-routing](./quick/20260724-google-login-role-routing/) |

