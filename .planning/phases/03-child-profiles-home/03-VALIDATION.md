---
phase: 3
slug: child-profiles-home
status: approved
nyquist_compliant: true
wave_0_complete: false
created: 2026-07-23
---

# Phase 3 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 (unit), AndroidJUnit4 + Room Testing 2.8.4 (instrumented) |
| **Config file** | `app/build.gradle.kts` test deps |
| **Quick run command** | `./gradlew :app:testDebugUnitTest --tests "se.simmarken.domain.*"` |
| **Full suite command** | `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest` |
| **Estimated runtime** | ~30s unit / ~2min instrumented |

---

## Sampling Rate

- **After every task commit:** `./gradlew :app:testDebugUnitTest`
- **After every plan wave:** `./gradlew :app:connectedDebugAndroidTest`
- **Before `/gsd-verify-work`:** Full suite must be green + manual UAT checklist
- **Max feedback latency:** 30 seconds (unit tests)

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 03-01-01 | 01 | 1 | KIDS-02 | — | N/A | unit | `./gradlew :app:testDebugUnitTest --tests "*.KidAvatarInitialsTest"` | ❌ W0 | ⬜ pending |
| 03-01-02 | 01 | 1 | KIDS-02 | — | N/A | unit | `./gradlew :app:testDebugUnitTest --tests "*.KidAvatarColorsTest"` | ❌ W0 | ⬜ pending |
| 03-01-03 | 01 | 1 | KIDS-01 | T-03-01 | Trim/reject empty name before persist | unit | `./gradlew :app:testDebugUnitTest --tests "*.KidNameValidationTest"` | ❌ W0 | ⬜ pending |
| 03-01-04 | 01 | 2 | KIDS-01/03/04 | T-03-02 | Parameterized Room queries | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.KidDaoTest"` | ❌ W0 | ⬜ pending |
| 03-02-01 | 02 | 2 | UI-01 | — | N/A | manual | Home FAB + cards on device | — | ⬜ pending |
| 03-03-01 | 03 | 3 | KIDS-01/04 | T-03-03 | Delete only after confirmation | manual | Bottom sheet + delete dialog UAT | — | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/se/simmarken/domain/util/KidAvatarInitialsTest.kt` — KIDS-02 initials logic
- [ ] `app/src/test/java/se/simmarken/ui/theme/KidAvatarColorsTest.kt` — KIDS-02 default color hash
- [ ] `app/src/test/java/se/simmarken/domain/validation/KidNameValidationTest.kt` — KIDS-01/D-06
- [ ] `app/src/androidTest/java/se/simmarken/data/local/KidDaoTest.kt` — KIDS-01/03/04 CRUD, ordering, cascade
- [ ] Fix `KidDao.observeAll()` ORDER BY to `sortOrder ASC, createdAtEpochMillis ASC` (D-04 prerequisite)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Home FAB + child cards layout | UI-01 | Compose layout + touch targets | Launch app, verify FAB visible, cards full-width, tap navigates |
| Bottom sheet add/edit flow | KIDS-01/04 | Modal interaction | FAB → fill name → pick color → save; overflow → edit |
| Delete confirmation | KIDS-04 | Destructive dialog UX | Overflow → delete → confirm removes card |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 30s (unit)
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-07-23
