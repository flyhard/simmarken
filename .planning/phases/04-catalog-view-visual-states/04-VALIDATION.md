---
phase: 4
slug: catalog-view-visual-states
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-23
---

# Phase 4 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 (JVM unit tests) |
| **Config file** | none — standard Android `app/src/test` |
| **Quick run command** | `./gradlew :app:testDebugUnitTest --tests "se.simmarken.domain.BadgeStateCalculatorTest"` |
| **Full suite command** | `./gradlew :app:testDebugUnitTest` |
| **Estimated runtime** | ~30 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew :app:testDebugUnitTest --tests "*BadgeStateCalculatorTest*"`
- **After every plan wave:** Run `./gradlew :app:testDebugUnitTest`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 04-01-01 | 01 | 1 | PROG-04 | — | N/A | unit | `./gradlew :app:testDebugUnitTest --tests "*BadgeStateCalculatorTest*"` | ❌ W0 | ⬜ pending |
| 04-01-02 | 01 | 1 | PROG-04 | — | N/A | unit | same as above | ❌ W0 | ⬜ pending |
| 04-02-01 | 02 | 2 | UI-02 | — | N/A | manual | Human verify category grouping | — | ⬜ pending |
| 04-03-01 | 03 | 3 | UI-04 | — | N/A | manual | Human verify 4-tier visuals | — | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/se/simmarken/domain/BadgeStateCalculatorTest.kt` — stubs for PROG-04
- [ ] `app/src/main/java/se/simmarken/domain/BadgeStateCalculator.kt` — domain object under test
- [ ] `app/src/main/java/se/simmarken/domain/model/BadgeVisualState.kt` — enum
- [ ] Coil dependency in `gradle/libs.versions.toml` + `app/build.gradle.kts`
- [ ] Optional: `CatalogDao.observeAllRequirements()` + repository passthrough

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Category grouping + sortOrder | UI-02 | Compose layout visual | Open child catalog; verify categories in affisch order |
| 4-tier visual states | UI-04 | Visual distinction | Verify grayscale/ring/cart/checkmark on sample badges |
| Badge detail navigation | UI-02 | Navigation flow | Tap badge; confirm detail placeholder with stub text |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
