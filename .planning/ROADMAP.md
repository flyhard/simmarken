# Roadmap: Simmärken Tracker

## Milestones

- ✅ **v1.0 MVP** — Phases 1–6 (shipped 2026-07-25)
- 🚧 **v1.1 Release & Open Source** — Phases 7–12 (in progress)

## Phases

<details>
<summary>✅ v1.0 MVP (Phases 1–6) — SHIPPED 2026-07-25</summary>

- [x] Phase 1: Android Foundation & Database (3/3 plans) — completed 2026-07-22
- [x] Phase 2: Catalog Seeding (5/5 plans) — completed 2026-07-22
- [x] Phase 3: Child Profiles & Home (3/3 plans) — completed 2026-07-23
- [x] Phase 4: Catalog View & Visual States (3/3 plans) — completed 2026-07-25
- [x] Phase 5: Progress Tracking & Badge Detail (3/3 plans) — completed 2026-07-24
- [x] Phase 6: Export/Import & i18n (4/4 plans) — completed 2026-07-25

Full phase details: [.planning/milestones/v1.0-ROADMAP.md](milestones/v1.0-ROADMAP.md)

</details>

### 🚧 v1.1 Release & Open Source (In Progress)

**Milestone Goal:** Ship Simmärken publicly — safe to open-source on GitHub, with automated builds and Play Store internal testing.

- [ ] **Phase 7: Security & Git Hygiene** — Secrets scan, hardened gitignore, pre-public review checklist
- [x] **Phase 8: Gradle Signing Configuration** — Conditional release signing for local dev and CI (completed 2026-07-26)
- [ ] **Phase 9: CI Build & Test** — Fast lint + unit test workflow on every push/PR
- [ ] **Phase 10: Play Console Setup** — Manual Play Store bootstrap and credential setup
- [ ] **Phase 11: Release Pipeline** — Signed AAB build and internal track upload automation
- [ ] **Phase 12: Open Source Publish** — OSS docs and public visibility (last step)

## Phase Details

### Phase 7: Security & Git Hygiene

**Goal**: Repository is hardened against accidental secret commits and passes full-history secrets scan before any public visibility
**Depends on**: Phase 6 (v1.0 shipped)
**Requirements**: SECU-01, SECU-02, SECU-03
**Success Criteria** (what must be TRUE):

  1. Gitleaks scan passes on full git history with zero findings
  2. `.gitignore` blocks keystores, Play credentials, and local signing files from being tracked
  3. Gitleaks workflow runs on every push and PR with `fetch-depth: 0`
  4. Pre-public security review checklist (`docs/SECURITY-CHECKLIST.md`) is completed and signed off

**Plans**: 3/3 plans executed

Plans:
**Wave 1**

- [x] 07-01-PLAN.md — Local security baseline: hardened gitignore, gitleaks config, full-history local scan (SECU-03, SECU-01 local)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 07-02-PLAN.md — Gitleaks GitHub Actions workflow on push/PR with fetch-depth 0 (SECU-01 CI)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 07-03-PLAN.md — Pre-public security checklist with evidence and maintainer sign-off (SECU-02)

### Phase 8: Gradle Signing Configuration

**Goal**: Release builds produce correctly signed AABs locally and in CI — no debug keystore fallback
**Depends on**: Phase 7
**Requirements**: *(supports CI-02, RELE-01 — no direct requirement IDs)*
**Success Criteria** (what must be TRUE):

  1. `bundleRelease` produces an AAB signed with the upload key, not the debug keystore
  2. Maintainer can sign locally via gitignored `keystore.properties` without committing secrets
  3. CI signing reads credentials from environment variables with no secrets in source
  4. Upload keystore SHA-256 fingerprint is documented privately for Play Console registration

**Plans**: 3/3 plans executed

Plans:
**Wave 1** *(parallel — no file overlap)*

- [x] 08-01-PLAN.md — Gradle signing resolver, fail-hard guard, keystore.properties.example (CI-02, D-01–D-04, D-09–D-12)
- [x] 08-02-PLAN.md — Keystore bootstrap script, fingerprint gitignore, SECURITY-CHECKLIST update (RELE-01, SECU-03, D-05–D-08, D-13–D-16)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 08-03-PLAN.md — Release signature verification script and end-to-end signed AAB human checkpoint (CI-02, RELE-01)

### Phase 9: CI Build & Test

**Goal**: Every code change gets fast automated feedback via lint and unit tests
**Depends on**: Phase 7
**Requirements**: CI-01, CI-03
**Success Criteria** (what must be TRUE):

  1. Every push and pull request triggers `lintDebug` and `testDebugUnitTest` via GitHub Actions
  2. CI workflow completes in under 10 minutes with Gradle dependency caching enabled
  3. Failed lint or unit tests cause the workflow to fail (visible red check on PR)

**Plans**: 3/3 plans executed

Plans:
**Wave 1**

- [x] 09-01-PLAN.md — Fix SettingsScreen Compose lint errors; local `lintDebug testDebugUnitTest` green (CI-01 prerequisite)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 09-02-PLAN.md — Add `.github/workflows/ci.yml` with lint-and-test job, Gradle cache, locked CONTEXT decisions (CI-01, CI-03)

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 09-03-PLAN.md — Push to GitHub, confirm green CI run and cache behavior; maintainer checkpoint (CI-01, CI-03)

### Phase 10: Play Console Setup

**Goal**: Play Store app record exists with Play App Signing enrolled and credentials ready for automation
**Depends on**: Phase 8
**Requirements**: RELE-01, RELE-02
**Success Criteria** (what must be TRUE):

  1. Play Developer account is active with app record for `se.simmarken` application ID
  2. Play App Signing is enrolled and upload keystore from Phase 8 is registered
  3. GCP service account with Release Manager role is created and JSON key stored in GitHub Secrets
  4. At least one signed AAB is accepted on the internal testing track (manual first upload if required)

**Plans**: TBD

### Phase 11: Release Pipeline

**Goal**: Maintainer can trigger automated signed release builds that upload to Play Store internal testing
**Depends on**: Phase 8, Phase 9, Phase 10
**Requirements**: CI-02, RELE-03, RELE-04
**Success Criteria** (what must be TRUE):

  1. Release workflow builds a signed release AAB using GitHub Secrets (no secrets in source)
  2. Pipeline uploads signed AAB to Play Store internal testing track on tag or manual dispatch
  3. `versionCode` is incremented before each Play upload — no duplicate-version rejection
  4. Keystore and service account credentials are decoded to ephemeral runner paths and cleaned up after build

**Plans**: TBD

### Phase 12: Open Source Publish

**Goal**: Repository is contributor-ready and made public only after security gates pass
**Depends on**: Phase 7, Phase 11
**Requirements**: OSS-01, OSS-02, OSS-03, OSS-04, OSS-05
**Success Criteria** (what must be TRUE):

  1. README describes the project with prerequisites and build/test instructions matching CI commands
  2. MIT LICENSE at repo root with badge asset licensing carve-out in NOTICE or `docs/ASSETS.md`
  3. CONTRIBUTING.md documents PR expectations and local verification steps
  4. CODEOWNERS routes all reviews to the maintainer
  5. Repository visibility changed to public only after SECU-01 and SECU-02 pass on `main` — this is the final step

**Plans**: TBD

## Progress

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Android Foundation & Database | v1.0 | 3/3 | Complete | 2026-07-22 |
| 2. Catalog Seeding | v1.0 | 5/5 | Complete | 2026-07-22 |
| 3. Child Profiles & Home | v1.0 | 3/3 | Complete | 2026-07-23 |
| 4. Catalog View & Visual States | v1.0 | 3/3 | Complete | 2026-07-25 |
| 5. Progress Tracking & Badge Detail | v1.0 | 3/3 | Complete | 2026-07-24 |
| 6. Export/Import & i18n | v1.0 | 4/4 | Complete | 2026-07-25 |
| 7. Security & Git Hygiene | v1.1 | 3/3 | In Progress|  |
| 8. Gradle Signing Configuration | v1.1 | 3/3 | Complete    | 2026-07-26 |
| 9. CI Build & Test | v1.1 | 3/3 | In Progress|  |
| 10. Play Console Setup | v1.1 | 0/? | Not started | - |
| 11. Release Pipeline | v1.1 | 0/? | Not started | - |
| 12. Open Source Publish | v1.1 | 0/? | Not started | - |
