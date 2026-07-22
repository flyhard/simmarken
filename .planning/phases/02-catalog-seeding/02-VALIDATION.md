---
phase: 2
slug: catalog-seeding
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-22
---

# Phase 2 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4 + AndroidX Test (instrumented); Kotlin test (unit) |
| **Config file** | `app/build.gradle.kts` (existing from Phase 1) |
| **Quick run command** | `./gradlew :app:testDebugUnitTest` |
| **Full suite command** | `./gradlew :app:connectedDebugAndroidTest` |
| **Estimated runtime** | ~30 seconds (unit) / ~90 seconds (instrumented, with emulator) |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew :app:testDebugUnitTest`
- **After every plan wave:** Run `./gradlew :app:connectedDebugAndroidTest`
- **Before `/gsd-verify-work`:** Full suite must be green + manual D-12 verification checklist for all badges
- **Max feedback latency:** 90 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 02-01 | 1 | CATA-01 | — | N/A | unit | `./gradlew :app:testDebugUnitTest --tests "*.SimidrottRequirementAccuracyTest"` | ❌ W0 | ⬜ pending |
| 02-02-01 | 02-02 | 1 | CATA-02 | — | N/A | unit | `./gradlew :app:testDebugUnitTest --tests "*.SlsRequirementAccuracyTest"` | ❌ W0 | ⬜ pending |
| 02-03-01 | 02-03 | 2 | CATA-01, CATA-02, CATA-03 | T-2-01 | Strict JSON parse; reject malformed seed | unit | `./gradlew :app:testDebugUnitTest --tests "*.CatalogSeedParserTest"` | ❌ W0 | ⬜ pending |
| 02-03-02 | 02-03 | 2 | CATA-01, CATA-02 | — | N/A | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.CatalogSeedLoaderTest"` | ❌ W0 | ⬜ pending |
| 02-03-03 | 02-03 | 3 | D-13, D-14, D-15 | — | N/A | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.CatalogSeedMergeTest"` | ❌ W0 | ⬜ pending |
| 02-03-04 | 02-03 | 2 | CATA-03 | — | N/A | unit | `./gradlew :app:testDebugUnitTest --tests "*.CatalogVersionTest"` | ❌ W0 | ⬜ pending |
| 02-04-01 | 02-04 | 2 | CATA-05 | — | N/A | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.CatalogSeedLoaderTest.badgeImagesResolvable"` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/se/simmarken/data/seed/CatalogVersionTest.kt` — version comparison logic
- [ ] `app/src/test/java/se/simmarken/data/seed/CatalogSeedParserTest.kt` — JSON → DTO from test fixture
- [ ] `app/src/test/java/se/simmarken/data/seed/SimidrottRequirementAccuracyTest.kt` — golden files for Baddaren, Hajen
- [ ] `app/src/test/java/se/simmarken/data/seed/SlsRequirementAccuracyTest.kt` — golden files for SLS basics
- [ ] `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderTest.kt` — first-run population
- [ ] `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt` — progress preservation on version bump
- [ ] `app/src/test/resources/seed/simidrott_sample.json` — minimal fixture for unit tests
- [ ] `CatalogDao` lookup-by-code queries + transactional merge support

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| All seeded badges verified against official sources | D-12 | Human verification against PDF protocols | Walk affisch + protocol PDFs; confirm every badge requirement bullet matches `textSv` |
| Image licensing documented | D-03 | Legal review | Confirm `docs/SOURCES.md` lists URLs, extraction date, promotional-use statement |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 90s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
