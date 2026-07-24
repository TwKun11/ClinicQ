---
status: complete
slug: backend-appointment-business
created: 2026-07-24
---

# Backend Appointment Business

Implemented the ClinicQ appointment slice after rebasing feature/appointment-backend from origin/develop.

## Scope
- Align appointment model with ClinicQ: patient is authenticated User, appointment links Doctor and ScheduleSlot.
- Add schedule slot model, pessimistic lock repository query, generation/list/block APIs.
- Add booking/cancel business rules: slot must be AVAILABLE, doctor active, doctor/slot match, patient cannot overlap active appointments, cancel reopens slot.
- Align route authorization: USER books/views/cancels own appointments, DOCTOR views doctor appointments, ADMIN manages doctor slots.
- Fix duplicate Flyway V5 by renaming doctor migration to V6 and adding V7 for schedule slots + appointment schema update.
- Update focused backend tests for service rules and API authorization.

## Verification
- Backend tests passed via cached Maven: 59 tests, 0 failures, 0 errors, 1 skipped.
