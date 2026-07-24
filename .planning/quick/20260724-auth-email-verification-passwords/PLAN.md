---
status: completed
created: 2026-07-24
completed: 2026-07-24
---

# Auth email verification and password flows

Implement realistic auth in ClinicQ:
- login by email instead of username
- register creates PENDING account and sends verification email
- verify email activates account and redirects to login
- statuses: PENDING, ACTIVE, INACTIVE
- reset password, change password, and /me endpoint
- minimal Angular UI/API support

## Verification

- `backend/.\\mvnw.cmd test` passed: 54 tests, 0 failures, 2 skipped.
- `frontend/npm run build` passed.
