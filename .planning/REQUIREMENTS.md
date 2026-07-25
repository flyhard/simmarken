# Requirements: Simmärken Tracker

**Defined:** 2026-07-25
**Milestone:** v1.1 Release & Open Source
**Core Value:** At the swim hall, a parent can immediately answer: "Did they pass this badge, and did we buy the physical pin?"

## v1.1 Requirements

Release engineering and open-source publishing. No app feature changes — additive infrastructure only.

### Security & Secrets (SECU)

- [ ] **SECU-01**: Automated secrets scan (gitleaks) passes with zero findings on full git history
- [ ] **SECU-02**: Manual pre-public security review checklist completed and signed off
- [ ] **SECU-03**: `.gitignore` hardened for keystores, Play credentials, and local signing files

### CI/CD (CI)

- [ ] **CI-01**: GitHub Actions runs `lintDebug` and `testDebugUnitTest` on every push and pull request
- [ ] **CI-02**: Release workflow builds a signed release AAB using GitHub Secrets (no secrets in source)
- [ ] **CI-03**: CI uses Gradle dependency caching to keep feedback loop under 10 minutes

### Release & Play Store (RELE)

- [ ] **RELE-01**: Upload keystore generated and Play App Signing enrolled in Play Console
- [ ] **RELE-02**: Play Console app record created with `se.simmarken` application ID
- [ ] **RELE-03**: Release pipeline uploads signed AAB to Play Store internal testing track
- [ ] **RELE-04**: `versionCode` incremented before each Play upload (no duplicate rejection)

### Open Source (OSS)

- [ ] **OSS-01**: README with project description, prerequisites, and build/test instructions matching CI commands
- [ ] **OSS-02**: MIT LICENSE at repo root with badge asset licensing carve-out (NOTICE or `docs/ASSETS.md`)
- [ ] **OSS-03**: CONTRIBUTING.md with PR expectations and local verification steps
- [ ] **OSS-04**: CODEOWNERS file routing all reviews to maintainer
- [ ] **OSS-05**: Repository made public only after SECU-01 and SECU-02 pass on `main`

## v2 Requirements

Deferred to future milestone. Tracked but not in v1.1 roadmap.

### Sharing

- **SHAR-01**: Parent can share child progress as formatted text
- **SHAR-02**: Parent can share child progress as image

### Catalogs

- **CATA-06**: Parent can add custom swim club badge catalogs
- **CATA-07**: Catalog version updates when official requirements change

### CI Enhancements (deferred)

- **CI-04**: Instrumented tests in CI (emulator)
- **CI-05**: Branch protection requiring CI green before merge
- **CI-06**: Dependabot for Gradle and GitHub Actions

## Out of Scope

| Feature | Reason |
|---------|--------|
| Auto-deploy to production Play track | v1.1 targets internal testing only; production promotion is manual |
| Fastlane | Ruby overhead unnecessary for solo-maintainer single-app pipeline |
| Firebase Crashlytics / analytics | Adds network dependency and privacy policy; contradicts offline-first design |
| Cloud sync or backend | Out of scope per v1.0 architecture; JSON export/import handles migration |
| R8/ProGuard minification | `isMinifyEnabled = false` acceptable for internal track; defer to v2 |
| APK sideload distribution | Play Store requires AAB; APK uploads not supported for new apps |
| Committing keystores or service account JSON | Security risk on public repo; GitHub Secrets only |
| Making repo public before secrets scan | Irreversible git history exposure; SECU gates must pass first |
| App feature changes | v1.1 is infrastructure-only; share/custom catalogs deferred to v2 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| SECU-01 | — | Pending |
| SECU-02 | — | Pending |
| SECU-03 | — | Pending |
| CI-01 | — | Pending |
| CI-02 | — | Pending |
| CI-03 | — | Pending |
| RELE-01 | — | Pending |
| RELE-02 | — | Pending |
| RELE-03 | — | Pending |
| RELE-04 | — | Pending |
| OSS-01 | — | Pending |
| OSS-02 | — | Pending |
| OSS-03 | — | Pending |
| OSS-04 | — | Pending |
| OSS-05 | — | Pending |

**Coverage:**
- v1.1 requirements: 15 total
- Mapped to phases: 0
- Unmapped: 15 ⚠️

---
*Requirements defined: 2026-07-25*
*Last updated: 2026-07-25 after v1.1 milestone definition*
