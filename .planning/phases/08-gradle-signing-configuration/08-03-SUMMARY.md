---
phase: 08-gradle-signing-configuration
plan: 03
subsystem: infra
tags: [android, signing, jarsigner, keytool, bash, release-verification]

requires:
  - phase: 08-gradle-signing-configuration
    provides: Gradle signing resolver, keystore bootstrap script, keystore.properties.example
provides:
  - scripts/verify-release-signature.sh post-build AAB signature validation
  - Human-confirmed signed release AAB end-to-end (RELE-01, SC-1/SC-4)
affects: [phase-10-play-console, phase-11-release-pipeline]

tech-stack:
  added: []
  patterns:
    - "Post-build verification: jarsigner -verify + keytool -printcert -jarfile"
    - "Optional SHA-256 compare against gitignored keystore-fingerprint.md"

key-files:
  created:
    - scripts/verify-release-signature.sh
  modified: []

key-decisions:
  - "Fingerprint mismatch prints MISMATCH but does not fail script when doc present (CI without local doc)"
  - "Human end-to-end verification completed by maintainer — upload.jks, bundleRelease, verify script, fail-hard, debug, gitleaks all confirmed"

patterns-established:
  - "verify-release-signature.sh: default AAB path, positional override, prerequisite tool checks"

requirements-completed: [CI-02, RELE-01]

coverage:
  - id: D1
    description: "verify-release-signature.sh wraps jarsigner and keytool post-build checks"
    requirement: CI-02
    verification:
      - kind: other
        ref: "bash -n scripts/verify-release-signature.sh && grep jarsigner/keytool checks"
        status: pass
    human_judgment: false
  - id: D2
    description: "Signed release AAB produced locally and verified end-to-end"
    requirement: RELE-01
    verification:
      - kind: manual_procedural
        ref: "Maintainer: generate-upload-keystore.sh, bundleRelease, verify-release-signature.sh, fail-hard, assembleDebug, gitleaks"
        status: pass
    human_judgment: true
    rationale: "Requires maintainer-local upload.jks, keystore.properties, and interactive keytool bootstrap — approved 2026-07-26"

duration: 12min
completed: 2026-07-26
status: complete
---

# Phase 8 Plan 03: Release Signature Verification Summary

**Post-build AAB verification script plus maintainer-confirmed signed release bundle end-to-end**

## Performance

- **Duration:** 12 min
- **Started:** 2026-07-26T08:44:55Z
- **Completed:** 2026-07-26T19:17:00Z
- **Tasks:** 2/2 complete
- **Files modified:** 1

## Accomplishments

- Created `scripts/verify-release-signature.sh` with jarsigner -verify, keytool -printcert, and optional fingerprint compare
- Script passes `bash -n`, is executable, and follows repo bash conventions
- Maintainer approved human checkpoint: local keystore setup, signed `bundleRelease`, verification script, fail-hard guard, debug build, and gitleaks all confirmed

## Task Commits

1. **Task 1: Release signature verification script** - `23056e4` (feat)
2. **Task 2: Human verify signed release AAB end-to-end** - user approved (no commit)

## Files Created/Modified

- `scripts/verify-release-signature.sh` - Post-build signed AAB validation wrapper

## Decisions Made

- Fingerprint MISMATCH is reported but non-fatal (supports CI without local fingerprint doc)
- End-to-end signed bundleRelease verification completed by maintainer on 2026-07-26

## Deviations from Plan

None — plan executed exactly as written.

## Issues Encountered

None after maintainer completed local keystore bootstrap and verification steps.

## Human Verification (Task 2)

**Approved:** 2026-07-26 — maintainer confirmed:

1. `upload.jks` generated via `generate-upload-keystore.sh`
2. `keystore.properties` configured from example
3. `./gradlew :app:bundleRelease` produced signed `app-release.aab`
4. `./scripts/verify-release-signature.sh` passed (jarsigner verify + SHA-256 match)
5. Fail-hard guard confirmed (bundleRelease fails without keystore.properties)
6. Debug build unaffected (`assembleDebug` succeeds without signing credentials)
7. `gitleaks detect` — no secrets staged

## Next Phase Readiness

- Verification script ready for CI and release pipeline use
- Phase 8 signing path validated end-to-end (SC-1/SC-4, RELE-01)
- Phase 10 can consume SHA-256 from `keystore-fingerprint.md` for Play Console registration

## Self-Check: PASSED

- FOUND: scripts/verify-release-signature.sh
- FOUND: commit 23056e4

---
*Phase: 08-gradle-signing-configuration*
*Completed: 2026-07-26*
