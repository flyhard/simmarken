# Phase 8: Gradle Signing Configuration - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-26
**Phase:** 8-Gradle Signing Configuration
**Areas discussed:** Missing-keystore behavior, Keystore bootstrap, Local credential layout, Private fingerprint docs

---

## Missing-Keystore Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Fail hard | `bundleRelease` errors with clear message if no signing config; no silent debug fallback | ✓ |
| Skip signing | Release build completes unsigned (CI smoke tests without secrets) | |
| You decide | Match Android/Google convention for solo-maintainer OSS repos | |

**User's choice:** Fail hard
**Notes:** Debug builds (`assembleDebug`, `testDebugUnitTest`) remain unaffected. Error message must be actionable — point to `keystore.properties.example` and keystore creation docs. Same fail-hard rule applies in CI (Phase 11) when signing secrets are absent.

---

## Keystore Bootstrap

| Option | Description | Selected |
|--------|-------------|----------|
| Script in repo | `scripts/generate-upload-keystore.sh` with documented keytool command | ✓ |
| Docs only | Document keytool steps in markdown; no committed script | |
| Fully manual | Maintainer creates keystore independently; Gradle wiring only | |

**User's choice:** Script in repo
**Notes:** Google-default key parameters (RSA 2048, 10 000-day validity, alias `upload`). Keystore file at repo root (`upload.jks`). Interactive password prompts — never echo or write passwords to disk.

---

## Local Credential Layout

| Option | Description | Selected |
|--------|-------------|----------|
| Repo root | `keystore.properties` at project root; standard Android convention | ✓ |
| App module | `app/keystore.properties` | |
| You decide | | |

**User's choice:** Repo root
**Notes:** Commit `keystore.properties.example` with placeholder values. Standard four properties: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. CI uses parallel env vars (`ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) when properties file absent.

---

## Private Fingerprint Docs

| Option | Description | Selected |
|--------|-------------|----------|
| Gitignored local file | `keystore-fingerprint.md` at repo root, added to `.gitignore` | ✓ |
| Password manager | Document extraction command only; fingerprint stored externally | |
| You decide | | |

**User's choice:** Gitignored local file
**Notes:** Document contains SHA-256 fingerprint, alias, creation date, and re-verify command. Keystore generation script prints and writes fingerprint. Capture in Phase 8; Play Console registration deferred to Phase 10.

---

## Claude's Discretion

- Gradle `signingConfigs` structure and env-var decode implementation details
- `signing.gradle.kts` include vs inline configuration
- Exact fingerprint filename and `.gitignore` entry
- Base64 decode location (Gradle vs CI workflow)

## Deferred Ideas

None.
