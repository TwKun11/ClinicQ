# Google login and role routing

Implemented role-aware routing and Google login.

- Email/password login now redirects ADMIN to `/dashboard`, USER/STAFF to `/home`.
- Added `/home` route and a simple home page.
- Dashboard and user management routes are guarded for ADMIN only.
- Change password route and sidebar item are available only for USER and STAFF.
- Added Google login button on the login screen using Google Identity Services.
- Added backend `/api/v1/auth/google` endpoint that verifies Google ID tokens against the configured client ID.
- Google login creates a new ACTIVE USER account when the email does not exist.

Verification:

- `backend/.\\mvnw.cmd test`
- `frontend/npm run build`
