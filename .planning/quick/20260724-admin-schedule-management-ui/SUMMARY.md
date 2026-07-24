# Summary

Implemented an admin schedule management UI.

## Changed

- Added `/schedules` admin route.
- Added "Quản lý lịch khám" sidebar navigation.
- Created `ScheduleManagementComponent` as an admin-level schedule dashboard with doctor search, specialty filtering, doctor selection cards, selected-doctor timeline, status legend, schedule stats, view toggle, and create-slot drawer.

## Verification

- `npm run build` passed in `frontend`.
- Angular still reports existing CSS budget warnings for rich UI components; build succeeds.
