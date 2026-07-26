---
phase: 08-gradle-signing-configuration
plan: 02
subsystem: infra
tags: [android, keytool, signing, bash, gitignore, secu-03]

requires:
  - phase: 07-security-git-hygiene
    provides: SECU-03 gitignore patterns for *.jks and keystore.properties
provides:
  - scripts/generate-upload-keystore.sh one-time keystore bootstrap
  - keystore-fingerprint.md gitignore coverage
  - SECURITY-CHECKLIST item 2 evidence for fingerprint doc
affects: [08-03, phase-10-play-console, phase-11-release-pipeline]

tech-stack:
  added: []
  patterns:
    - "Bash script conventions from extract-sls.sh (set -euo pipefail, --help, stderr errors)"
    - "Interactive keytool only — no passwords on disk (D-08)"
    - "Private SHA-256 capture to gitignored keystore-fingerprint.md (D-13–D-16)"

key-files:
  created:
    - scripts/generate-upload-keystore.sh
  modified:
    - .gitignore
    - docs/SECURITY-CHECKLIST.md

key-decisions:
  - "Auto-selected proceed — generate new upload keystore (decision checkpoint; upload.jks absent)"
  - "Fingerprint doc gitignored under existing SECU-03 block; Play Console registration deferred to Phase 10"

patterns-established:
  - "Keystore bootstrap: REPO_ROOT resolution, refuse overwrite, keytool RSA 2048 / 10000-day / alias upload"
  - "Fingerprint capture: keytool -list -v piped through awk; written to keystore-fingerprint.md with re-verify command"

requirements-completed: [RELE-01, SECU-03]

coverage:
  - id: D1
    description: "Committed generate-upload-keystore.sh with Google-default parameters and interactive prompts"
    requirement: RELE-01
    verification:
      - kind: other
        ref: "bash -n scripts/generate-upload-keystore.sh && grep checks per plan verify"
        status: pass
    human_judgment: false
  - id: D2
    description: "keystore-fingerprint.md gitignored and documented in SECURITY-CHECKLIST item 2"
    requirement: SECU-03
    verification:
      - kind: other
        ref: "git check-ignore -v keystore-fingerprint.md"
        status: pass
    human_judgment: false
  - id: D3
    description: "Maintainer runs script once to create upload.jks and fingerprint file locally"
    requirement: RELE-01
    verification: []
    human_judgment: true
    rationale: "Keystore generation is interactive one-time maintainer action; not run in CI"

duration: 5min
completed: 2026-07-26
status: complete
---

# Phase 8 Plan 02: Keystore Bootstrap Summary

**Upload keystore bootstrap script with interactive keytool, SHA-256 fingerprint capture, and SECU-03 gitignore extension**

## Performance

- **Duration:** 5 min
- **Started:** 2026-07-26T08:44:20Z
- **Completed:** 2026-07-26T08:49:00Z
- **Tasks:** 3 (1 decision auto-approved + 2 implementation)
- **Files modified:** 3

## Accomplishments

- Decision checkpoint auto-selected **proceed** (AUTO_CHAIN; `upload.jks` absent)
- `scripts/generate-upload-keystore.sh` creates repo-root `upload.jks` with RSA 2048, 10000-day validity, alias `upload`
- Script refuses overwrite, prompts passwords interactively, writes SHA-256 to `keystore-fingerprint.md`
- `.gitignore` extended with `keystore-fingerprint.md`; SECURITY-CHECKLIST item 2 updated with Phase 10 note

## Task Commits

1. **Task 2: Keystore bootstrap script with fingerprint capture** - `cd3bae1` (feat)
2. **Task 3: Extend gitignore and SECURITY-CHECKLIST** - `5d7ced4` (chore)

**Plan metadata:** `b94465f` (docs: complete plan)

## Files Created/Modified

- `scripts/generate-upload-keystore.sh` - One-time keytool wrapper; fingerprint capture
- `.gitignore` - Added `keystore-fingerprint.md` under SECU-03 block
- `docs/SECURITY-CHECKLIST.md` - Item 2 evidence includes fingerprint doc and Phase 10 deferral

## Decisions Made

- Auto-selected **proceed** at decision checkpoint (no existing `upload.jks`)
- Play Console SHA-256 registration remains Phase 10; Phase 8 only captures locally (D-16)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

Maintainer must run once locally (not automated in CI):

```bash
scripts/generate-upload-keystore.sh
cp keystore.properties.example keystore.properties   # after Plan 08-01 / 08-03 delivers example
# Edit keystore.properties with passwords chosen during keytool prompts
```

## Next Phase Readiness

- Bootstrap script ready for one-time keystore creation
- Plan 08-03 can wire Gradle signing to consume `upload.jks` + `keystore.properties`
- Phase 10 will read SHA-256 from private `keystore-fingerprint.md`

## Self-Check: PASSED

- FOUND: scripts/generate-upload-keystore.sh
- FOUND: .gitignore (keystore-fingerprint.md entry)
- FOUND: docs/SECURITY-CHECKLIST.md (updated item 2)
- FOUND: commit cd3bae1
- FOUND: commit 5d7ced4

---
*Phase: 08-gradle-signing-configuration*
*Completed: 2026-07-26*
