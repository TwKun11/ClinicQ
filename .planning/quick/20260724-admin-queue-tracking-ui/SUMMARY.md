---
status: complete
completed: 2026-07-24
branch: feature/admin-queue-tracking-ui
---

# Summary

Built the admin queue tracking UI from the provided HTML as a new `/queues` route.

## Changes

- Added `QueueTrackingComponent` with search, specialty/doctor/date filters, KPI cards, queue table, pagination, and patient detail drawer.
- Added admin sidebar navigation for “Theo dõi hàng đợi”.
- Added global SCSS import for the queue page to avoid Angular component style budget errors.

## Verification

- `npm run build` passed.

