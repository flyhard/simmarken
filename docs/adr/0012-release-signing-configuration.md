# ADR-0012: Release signing configuration

- **Status:** Accepted
- **Date:** 2026-07-26 (recorded retroactively 2026-09-25)
- **Related:** PRD-0002 (CI-02, RELE-01), ADR-0011

## Context

Play requires release bundles signed with the registered upload key. A release
build that silently falls back to the debug keystore would be rejected — or worse,
go unnoticed until upload.

## Decision

- `app/build.gradle.kts` resolves the release `signingConfig` from, in order:
  1. `keystore.properties` at the repo root (gitignored; template in
     `keystore.properties.example`) with `storeFile`, `storePassword`,
     `keyAlias`, `keyPassword`;
  2. environment variables `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
     `KEY_ALIAS`, `KEY_PASSWORD` — the keystore is decoded by Gradle into
     `build/signing/` (ephemeral, never a tracked path).
- If neither is present, **release tasks fail hard** with an actionable message.
  There is no debug-keystore fallback. Debug builds and tests need no signing setup.
- The upload key is created once with `scripts/generate-upload-keystore.sh`
  (RSA 2048, 10 000 days, alias `upload`, passwords prompted interactively and
  never written to disk). The script records the SHA-256 fingerprint in the
  gitignored `keystore-fingerprint.md`.
- `scripts/verify-release-signature.sh` checks a built AAB against the expected
  fingerprint.

## Consequences

- CI release jobs must have all four secrets configured or they fail.
- Losing the upload key requires a Play upload-key reset (Play App Signing holds
  the app signing key).

## Origin

GSD Phase 8 D-01…D-16.
