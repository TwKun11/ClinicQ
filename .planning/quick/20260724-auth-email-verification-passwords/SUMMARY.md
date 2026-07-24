# Auth email verification and password flows

Implemented email-based authentication for ClinicQ.

- Login now uses email and password.
- Register creates a `PENDING` account, sends a verification email, and does not issue tokens.
- Email verification consumes a short-lived token, activates the user, and redirects to the login page.
- Added user statuses: `PENDING`, `ACTIVE`, `INACTIVE`.
- Admin create/update keeps account activation mapped to `ACTIVE`/`INACTIVE`.
- Added `/api/v1/auth/me`, forgot password, reset password, and change password flows.
- Added Angular screens for forgot password, reset password, and change password.
- Tightened auth route security so protected auth endpoints require JWT.

Verification:

- `backend/.\\mvnw.cmd test`
- `frontend/npm run build`
