# Summary

Implemented the ClinicQ admin overview page and seeded a demo admin account.

## Changed

- Replaced the starter dashboard with a Vietnamese ClinicQ admin overview page at `/dashboard`.
- Kept admin login redirect behavior unchanged because `AuthService.landingRouteForRole()` already sends `ADMIN` to `/dashboard`.
- Added Flyway migration `V6__seed_admin_user.sql` to create or update an active admin account.

## Demo Admin

- Email: `admin@clinicq.local`
- Password: `11111111`
- Role: `ADMIN`
- Status: `ACTIVE`

## Verification

- Frontend build passed.
- Backend test suite passed.
