---
phase: 9
slug: ci-build-test
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-27
---

# Phase 9 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 + Robolectric 4.14.1 + kotlinx-coroutines-test |
| **Config file** | none — standard Android Gradle test setup |
| **Quick run command** | `./gradlew testDebugUnitTest --no-daemon` |
| **Full suite command** | `./gradlew lintDebug testDebugUnitTest --no-daemon` |
| **Estimated runtime** | ~3.5 min cold; ~30s warm unit tests |

Phase 9 is CI infrastructure; behavioral verification is **Gradle command-based** locally and **GitHub Actions integration** on push.

---

## Sampling Rate

- **After every task commit:** `./gradlew lintDebug testDebugUnitTest --no-daemon` (when touching app or workflow files)
- **After Plan 09-01:** Full local lint+test must pass before creating `ci.yml`
- **After Plan 09-02:** YAML structure grep + local Gradle command
- **After Plan 09-03:** Green `lint-and-test` GitHub Actions run on pushed branch
- **Max feedback latency:** 60 seconds per-task locally; workflow run ~5–10 min on GitHub

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 09-01-01 | 01 | 1 | CI-01 | — | SettingsScreen lint errors fixed | command | `./gradlew lintDebug --no-daemon` exits 0 | ❌ W0 | ⬜ pending |
| 09-01-02 | 01 | 1 | CI-01 | — | Combined lint+test green locally | command | `./gradlew lintDebug testDebugUnitTest --no-daemon` exits 0 | ❌ W0 | ⬜ pending |
| 09-02-01 | 02 | 2 | CI-01,CI-03,D-01..D-16 | T-09-01 | ci.yml with locked workflow decisions | inspection | grep checks on `.github/workflows/ci.yml` | ❌ W0 | ⬜ pending |
| 09-02-02 | 02 | 2 | CI-01,CI-03 | — | Local Gradle still passes after workflow add | command | `./gradlew lintDebug testDebugUnitTest --no-daemon` | ❌ W0 | ⬜ pending |
| 09-03-01 | 03 | 3 | CI-01,CI-03 | — | GitHub workflow succeeds on push | integration | `gh run list` / `gh run view` success | ❌ W0 | ⬜ pending |
| 09-03-02 | 03 | 3 | CI-01,CI-03 | — | Maintainer confirms green check + cache | checkpoint | Human verifies Actions UI | — | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] Fix `SettingsScreen.kt` Compose lint (`LocalContextGetResourceValueCall`) — Plan 09-01
- [ ] `.github/workflows/ci.yml` — Plan 09-02
- [ ] Green GitHub Actions run — Plan 09-03

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| CI cache restore on second run | CI-03 | Requires two GitHub workflow runs | Push twice; check setup-gradle cache hit in logs |
| Workflow under 10 minutes | CI-03 | Wall-clock on GitHub runner | Record duration from Actions UI in SUMMARY |
| Lint failure fails workflow | CI-01 | Destructive test | Optional: temporary lint break → red X |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] CI-01 and CI-03 mapped to automated or integration checks
