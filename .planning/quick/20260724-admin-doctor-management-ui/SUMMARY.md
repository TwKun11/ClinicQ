# Summary

Implemented a frontend-only admin doctor management page for ClinicQ.

## Changed

- Added admin route `/doctors`.
- Added "Quản lý bác sĩ" to the admin sidebar.
- Created `DoctorManagementComponent` with API-backed search, specialty/status filters, pagination, summary cards, CSV export, and activate/deactivate action.
- Added frontend-only doctor form and detail pages wired to the expected Doctor API contract. No doctor or schedule backend code is implemented in this branch.
- Updated the add/edit doctor form to match the provided admin HTML: account selection panel, professional information panel, status toggle, and sticky action buttons.
- Renamed the main admin sidebar brand from "Training Starter" to "ClinicQ".

## Verification

- `npm run build` passed in `frontend`.
