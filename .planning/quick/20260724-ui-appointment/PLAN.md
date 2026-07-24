---
status: complete
slug: ui-appointment
created: 2026-07-24
---

# UI Appointment

Implemented the user appointment UI against the ClinicQ appointment APIs.

## Scope
- Added appointment API service for doctors, slots, my appointments, booking, cancel, and doctor appointments.
- Added user appointment page with doctor/category selection, date and available slots, booking form, and my appointment cancellation.
- Added doctor appointments page for DOCTOR role.
- Updated guards, routes, layout navigation, and login landing routes for USER/DOCTOR/ADMIN separation.
- Adjusted Angular production build config to avoid network font inlining and match current Material bundle/style sizes.

## Verification
- `npm run build` passed.
