# Admin Dashboard UI and Seed Account

**Date:** 2026-07-24
**Branch:** `feature/admin-doctor-management-ui`
**Status:** Completed

## Scope

- Replace the starter dashboard with a ClinicQ admin overview page based on the provided HTML.
- Keep the page under the existing authenticated admin shell and route `/dashboard`.
- Add a Flyway migration to seed an active admin account for demo login.

## Verification

- `npm run build` passed in `frontend`.
- `.\mvnw.cmd test` passed in `backend`: 54 tests, 0 failures, 0 errors, 2 skipped.
