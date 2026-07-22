# Roadmap

**Analysis Date:** 2026-07-22
**Mode:** Brownfield onboarding baseline.

This roadmap documents safe next phases for an existing codebase. Product priorities need confirmation before feature execution.

## Phase 1: Stabilize Local Verification

**Goal:** Make the existing backend and frontend validation commands reliable for reviewers.
**Mode:** mvp

**Requirements:** `INFRA-01`, `INFRA-02`, `INFRA-03`

**Success Criteria:**
1. Backend test command can run from a clean checkout or its blocker is documented.
2. Frontend dependency installation and build command are documented and reproducible.
3. No secrets or machine-local files are required for basic verification.

## Phase 2: Confirm Authorization Model

**Goal:** Clarify and implement role boundaries for user and patient management if required.
**Mode:** mvp

**Requirements:** `AUTH-NC-01`, `SEC-NC-01`

**Success Criteria:**
1. User roles and allowed actions are documented.
2. Backend endpoints enforce confirmed role rules.
3. Tests cover allowed and denied access paths.

## Phase 3: Complete Patient Frontend Workflow

**Goal:** Add or confirm frontend workflows for patient management.
**Mode:** mvp

**Requirements:** `PAT-01`, `PAT-02`, `PAT-03`, `PAT-04`, `PAT-05`, `PAT-NC-01`

**Success Criteria:**
1. User can list patients in the frontend if product scope requires it.
2. User can create and edit patient records in the frontend if product scope requires it.
3. API and UI validation errors are displayed consistently.

## Phase 4: Production Readiness Decisions

**Goal:** Decide operational requirements before production-like use.
**Mode:** mvp

**Requirements:** `OPS-NC-01`, `SEC-NC-01`

**Success Criteria:**
1. Deployment target is confirmed.
2. CI/CD and environment strategy are documented.
3. Logging, monitoring, and data protection expectations are documented.

## Requirement Coverage

- `AUTH-01` to `AUTH-05`: Existing capability; future changes depend on confirmed auth scope.
- `USER-01` to `USER-06`: Existing capability; role hardening tracked in Phase 2.
- `PAT-01` to `PAT-05`: Backend existing capability; frontend scope tracked in Phase 3.
- `INFRA-01` to `INFRA-03`: Phase 1.
- `AUTH-NC-01`, `SEC-NC-01`: Phase 2 and Phase 4.
- `PAT-NC-01`: Phase 3.
- `OPS-NC-01`: Phase 4.

