---
status: completed
created: 2026-07-24
completed: 2026-07-24
---

# Google login and role routing

Implement quick auth refinements:
- Redirect ADMIN users to `/dashboard` after login.
- Redirect USER and STAFF users to `/home` after login.
- Show Change Password menu only for USER and STAFF.
- Add Google login on the current auth branch using the provided Google client ID.

## Verification

- `backend/.\\mvnw.cmd test` passed: 54 tests, 0 failures, 2 skipped.
- `frontend/npm run build` passed.
