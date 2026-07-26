---
phase: 08-gradle-signing-configuration
plan: 01
subsystem: infra
tags: [gradle, android, signing, agp, keystore]

requires:
  - phase: 07-security-git-hygiene
    provides: SECU-03 gitignore patterns for *.jks and keystore.properties
provides:
  - Release signing credential resolver (local keystore.properties + CI env vars)
  - Conditional signingConfigs.release with fail-hard whenReady guard
  - keystore.properties.example template at repo root
affects: [08-02, 08-03, phase-10, phase-11]

tech-stack:
  added: []
  patterns:
    - "Guarded credential resolution returning null when incomplete (D-02)"
    - "gradle.taskGraph.whenReady fail-hard for bundleRelease/assembleRelease (D-01)"
    - "CI env-var contract ANDROID_KEYSTORE_BASE64 + KEYSTORE_PASSWORD + KEY_ALIAS + KEY_PASSWORD (D-12)"

key-files:
  created:
    - keystore.properties.example
  modified:
    - app/build.gradle.kts

key-decisions:
  - "Inline signing logic in app/build.gradle.kts (no signing.gradle.kts include)"
  - "Gradle-side base64 decode to app/build/signing/ci-upload.jks for CI path"
  - "Nullable release signingConfig via findByName — no debug keystore fallback"

patterns-established:
  - "resolveReleaseSigningCredentials() returns null for partial/missing credentials"
  - "whenReady guard throws GradleException referencing keystore.properties.example and generate-upload-keystore.sh"

requirements-completed: [CI-02]

coverage:
  - id: D1
    description: "Release signing resolver with local keystore.properties and CI env-var paths"
    requirement: CI-02
    verification:
      - kind: other
        ref: "grep resolveReleaseSigningCredentials app/build.gradle.kts"
        status: pass
    human_judgment: false
  - id: D2
    description: "Fail-hard guard on bundleRelease/assembleRelease without credentials"
    requirement: CI-02
    verification:
      - kind: other
        ref: "./gradlew :app:bundleRelease (no credentials) → exit 1 with keystore.properties.example and generate-upload-keystore.sh in output"
        status: pass
    human_judgment: false
  - id: D3
    description: "Debug builds unaffected without signing configuration"
    requirement: CI-02
    verification:
      - kind: other
        ref: "./gradlew :app:assembleDebug && ./gradlew :app:testDebugUnitTest"
        status: pass
    human_judgment: false
  - id: D4
    description: "keystore.properties.example template with four standard properties"
    requirement: CI-02
    verification:
      - kind: other
        ref: "test -f keystore.properties.example && grep storeFile/storePassword/keyAlias/keyPassword"
        status: pass
    human_judgment: false

duration: 12min
completed: 2026-07-26
status: complete
---

# Phase 8 Plan 1: Release Signing Resolver Summary

**Conditional release signing with guarded credential resolution, CI env-var contract, and fail-hard whenReady enforcement — debug builds unchanged**

## Performance

- **Duration:** 12 min
- **Started:** 2026-07-26T08:44:00Z
- **Completed:** 2026-07-26T08:56:00Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments

- Added `resolveReleaseSigningCredentials()` supporting gitignored `keystore.properties` and D-12 CI env vars
- Wired conditional `signingConfigs.release` with nullable `buildTypes.release.signingConfig` (no debug fallback)
- Added `gradle.taskGraph.whenReady` guard that fails release tasks with actionable setup instructions
- Committed `keystore.properties.example` template at repo root

## Task Commits

Each task was committed atomically:

1. **Task 1: End-to-end release signing resolver with fail-hard guard** - `bbd4875` (feat)
2. **Task 2: Commit keystore.properties.example template** - `66482f4` (feat)

## Files Created/Modified

- `app/build.gradle.kts` - Release signing resolver, conditional signingConfigs, whenReady fail-hard guard
- `keystore.properties.example` - Committed template for local signing credentials

## Decisions Made

- Inline signing logic in `app/build.gradle.kts` per solo-maintainer discretion (no separate `signing.gradle.kts`)
- Gradle decodes `ANDROID_KEYSTORE_BASE64` to ephemeral `app/build/signing/ci-upload.jks`
- Resolver returns null (not throws) when credentials incomplete to preserve debug build path (D-02)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- Gradle verification required running outside sandbox (`all` permissions) due to `FileLockContentionHandler` / wildcard IP restriction in sandboxed environment

## User Setup Required

None for this plan. Maintainer keystore bootstrap (`scripts/generate-upload-keystore.sh`) is delivered in Plan 08-02.

## Next Phase Readiness

- Plan 08-02 can add keystore bootstrap script and fingerprint capture
- Plan 08-03 can extend verification scripts
- Phase 11 release workflow can consume D-12 env-var contract

## Self-Check: PASSED

- FOUND: app/build.gradle.kts
- FOUND: keystore.properties.example
- FOUND: bbd4875
- FOUND: 66482f4

---
*Phase: 08-gradle-signing-configuration*
*Completed: 2026-07-26*
