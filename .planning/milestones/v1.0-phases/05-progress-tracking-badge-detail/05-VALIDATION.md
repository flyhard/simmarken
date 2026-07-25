---
phase: 5
slug: progress-tracking-badge-detail
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-23
---

# Phase 5 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 (JVM unit tests) |
| **Config file** | none — standard Android `app/src/test` |
| **Quick run command** | `./gradlew :app:testDebugUnitTest --tests "se.simmarken.domain.ProgressWriteLogicTest"` |
| **Full suite command** | `./gradlew :app:testDebugUnitTest` |
| **Estimated runtime** | ~45 seconds |

---

## Sampling Rate

- **After every task commit:** Run quick run command for the plan under test
- **After every plan wave:** Run `./gradlew :app:testDebugUnitTest`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 05-01-01 | 01 | 1 | PROG-01 | T-05-01 | N/A | unit | `./gradlew :app:testDebugUnitTest --tests "*ProgressWriteLogicTest*"` | ❌ W0 | ⬜ pending |
| 05-01-02 | 01 | 1 | PROG-02, PROG-03 | T-05-02 | achievedAt write-once | unit | same as above | ❌ W0 | ⬜ pending |
| 05-01-03 | 01 | 1 | PROG-01 | — | N/A | compile | `./gradlew :app:compileDebugKotlin` | ✅ | ⬜ pending |
| 05-02-01 | 02 | 2 | UI-03 | — | N/A | compile | `./gradlew :app:compileDebugKotlin` | ✅ | ⬜ pending |
| 05-02-02 | 02 | 2 | PROG-01 | — | N/A | manual | Human verify checklist toggle persists | — | ⬜ pending |
| 05-02-03 | 02 | 2 | PROG-02, PROG-03 | T-05-03 | köpt undo confirm | manual | Human verify purchase toggle + dialog | — | ⬜ pending |
| 05-03-01 | 03 | 3 | PROG-05 | T-05-06 | N/A | unit | `./gradlew :app:testDebugUnitTest --tests "*KidProgressSummaryCalculatorTest*"` | ❌ W0 | ⬜ pending |
| 05-03-02 | 03 | 3 | PROG-05 | T-05-07 | per-kid scope | compile | `./gradlew :app:compileDebugKotlin` | ✅ | ⬜ pending |
| 05-03-03 | 03 | 3 | PROG-05 | — | N/A | manual | Human verify home subtitle lines | — | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/se/simmarken/domain/ProgressWriteLogicTest.kt` — stubs for PROG-01/02/03 write rules
- [ ] `app/src/test/java/se/simmarken/domain/KidProgressSummaryCalculatorTest.kt` — stubs for PROG-05
- [ ] `ProgressRepository` write implementations (05-01 tracer)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Requirement checklist toggle | UI-03, PROG-01 | Compose interaction + Room persistence | Toggle skill; return to grid; verify state updates |
| Purchase toggle + köpt undo dialog | PROG-02, PROG-03 | Dialog UX | Complete all skills; toggle köpt; confirm undo dialog |
| Home summary subtitles | PROG-05 | Visual copy + hide-when-zero | Child with 1 in-progress badge shows `1 pågår`; zero counts hide lines |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
