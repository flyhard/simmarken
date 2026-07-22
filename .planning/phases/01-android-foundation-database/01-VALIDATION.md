---
phase: 1
slug: android-foundation-database
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-22
---

# Phase 1 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4 + AndroidX Test 1.6+ + Room Testing 2.8.4 |
| **Config file** | `app/build.gradle.kts` (`testInstrumentationRunner`, `androidTestImplementation`) |
| **Quick run command** | `./gradlew :app:assembleDebug` |
| **Full suite command** | `./gradlew :app:connectedDebugAndroidTest` |
| **Estimated runtime** | ~120 seconds (build) / ~60 seconds (instrumented, with emulator) |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew :app:assembleDebug`
- **After every plan wave:** Run `./gradlew :app:connectedDebugAndroidTest` (emulator required)
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 01-01-01 | 01-01 | 1 | DATA-01 | — | N/A | build | `./gradlew :app:assembleDebug` | ❌ W0 | ⬜ pending |
| 01-02-01 | 01-02 | 2 | DATA-01 | T-1-01 | Room parameterized queries only | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "se.simmarken.data.local.DaoInstrumentedTest.catalogChainInsertsWithForeignKeys"` | ❌ W0 | ⬜ pending |
| 01-03-01 | 01-03 | 3 | DATA-01 | — | N/A | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "se.simmarken.data.local.DatabasePersistenceTest.kidSurvivesCloseAndReopen"` | ❌ W0 | ⬜ pending |
| 01-03-02 | 01-03 | 3 | DATA-01 | — | N/A | smoke | `./gradlew :app:assembleDebug` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/build.gradle.kts` — Android application module with KSP, Room plugin, Compose, Navigation
- [ ] `gradle/libs.versions.toml` — version catalog pins
- [ ] `app/src/androidTest/.../DaoInstrumentedTest.kt` — inserts catalog chain + kid + progress rows; verifies FK integrity
- [ ] `app/src/androidTest/.../DatabasePersistenceTest.kt` — disk DB close/reopen persistence
- [ ] `app/schemas/` — committed schema JSON after first compile
- [ ] `app/src/main/AndroidManifest.xml` — `application android:name=".SimmarkenApplication"`
- [ ] Framework install: Android project scaffold (Empty Activity Compose template or equivalent)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| App launches on emulator/device | DATA-01 | Visual smoke beyond CI | Install debug APK, tap through placeholder Home screen |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
