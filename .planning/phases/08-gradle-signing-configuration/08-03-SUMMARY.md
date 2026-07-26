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
  - "Human end-to-end verification deferred — upload.jks and keystore.properties absent in executor environment"

patterns-established:
  - "verify-release-signature.sh: default AAB path, positional override, prerequisite tool checks"

requirements-completed: []

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
    verification: []
    human_judgment: true
    rationale: "Requires maintainer-local upload.jks, keystore.properties, and interactive keytool bootstrap — not present in CI/executor environment"

duration: 4min
completed: 2026-07-26
status: checkpoint-pending
---

# Phase 8 Plan 03: Release Signature Verification Summary

**Post-build AAB verification script with jarsigner/keytool checks; human end-to-end signing validation pending maintainer keystore setup**

## Performance

- **Duration:** 4 min
- **Started:** 2026-07-26T08:44:55Z
- **Completed:** 2026-07-26T08:49:00Z (task 1 only; checkpoint pending)
- **Tasks:** 1/2 complete
- **Files modified:** 1

## Accomplishments

- Created `scripts/verify-release-signature.sh` with jarsigner -verify, keytool -printcert, and optional fingerprint compare
- Script passes `bash -n`, is executable, and follows repo bash conventions
- Human checkpoint emitted: local `upload.jks` / `keystore.properties` not present — maintainer must bootstrap before signed AAB verification

## Task Commits

1. **Task 1: Release signature verification script** - `23056e4` (feat)

## Files Created/Modified

- `scripts/verify-release-signature.sh` - Post-build signed AAB validation wrapper

## Decisions Made

- Fingerprint MISMATCH is reported but non-fatal (supports CI without local fingerprint doc)
- End-to-end signed bundleRelease verification requires maintainer action (keystore absent)

## Deviations from Plan

None for task 1 — script implemented as specified.

## Issues Encountered

- `upload.jks`, `keystore.properties`, and `keystore-fingerprint.md` absent locally — cannot run `bundleRelease` or full verification in executor environment

## User Setup Required

Maintainer must complete before approving checkpoint:

```bash
# 1. Generate upload keystore (interactive keytool prompts)
./scripts/generate-upload-keystore.sh

# 2. Configure signing credentials
cp keystore.properties.example keystore.properties
# Edit storePassword and keyPassword with values chosen during step 1

# 3. Build signed release bundle
./gradlew :app:bundleRelease

# 4. Verify signature and fingerprint match
./scripts/verify-release-signature.sh

# 5. Confirm fail-hard (rename keystore.properties temporarily)
./gradlew :app:bundleRelease   # must fail with keystore.properties.example message

# 6. Confirm debug unaffected
./gradlew :app:assembleDebug   # must succeed without keystore.properties

# 7. Confirm no secrets staged
gitleaks detect --source . --config .gitleaks.toml
```

## Next Phase Readiness

- Verification script ready for use after first signed `bundleRelease`
- Phase 8 cannot sign off until human checkpoint approved
- Phase 10 can consume SHA-256 from `keystore-fingerprint.md` after maintainer bootstrap

## Self-Check: PASSED

- FOUND: scripts/verify-release-signature.sh
- FOUND: commit 23056e4

---
*Phase: 08-gradle-signing-configuration*
*Checkpoint pending: 2026-07-26*
