# Phase 8: Gradle Signing Configuration - Context

**Gathered:** 2026-07-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Configure Gradle release signing so `bundleRelease` produces AABs signed with the upload key — locally via gitignored `keystore.properties` and in CI via environment variables. Deliverables: conditional `signingConfig` in `app/build.gradle.kts`, keystore bootstrap script, committed `keystore.properties.example`, gitignored private fingerprint doc, and fail-hard behavior when credentials are absent. Does not include Gitleaks/CI lint workflows (Phases 7/9), Play Console enrollment (Phase 10), or automated release upload (Phase 11).

</domain>

<decisions>
## Implementation Decisions

### Missing-Keystore Behavior
- **D-01:** Fail hard on release — `./gradlew bundleRelease` (and `assembleRelease`) must error with a clear message when signing credentials are absent; no silent debug-keystore fallback.
- **D-02:** Debug builds unaffected — `assembleDebug`, `testDebugUnitTest`, and other debug tasks work without any signing configuration.
- **D-03:** Actionable error message — Release signing failure must point maintainers to `keystore.properties.example` and keystore creation docs/script.
- **D-04:** Same rule in CI — Phase 11 release jobs must have signing secrets configured; no unsigned release fallback when secrets are absent.

### Keystore Bootstrap
- **D-05:** Committed generation script — `scripts/generate-upload-keystore.sh` runs `keytool` with documented parameters; maintainer executes once locally.
- **D-06:** Google-default key parameters — RSA 2048, 10 000-day validity, alias `upload` (script defaults; override only if documented).
- **D-07:** Keystore at repo root — Generated `upload.jks` lives at project root (already covered by Phase 7 `.gitignore` `*.jks` pattern).
- **D-08:** Interactive password prompts — Generation script prompts for store/key passwords interactively; never echoes or writes passwords to disk.

### Local Credential Layout
- **D-09:** `keystore.properties` at repo root — Standard Android convention; gitignored per Phase 7 SECU-03.
- **D-10:** Committed example template — `keystore.properties.example` with placeholder values committed to repo.
- **D-11:** Standard four properties — `storeFile`, `storePassword`, `keyAlias`, `keyPassword` in `keystore.properties`.
- **D-12:** Parallel CI env vars — When `keystore.properties` is absent, Gradle reads `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` from environment (CI decodes base64 keystore to ephemeral path).

### Private Fingerprint Documentation
- **D-13:** Gitignored local fingerprint file — `keystore-fingerprint.md` (or equivalent) at repo root, added to `.gitignore`; never committed.
- **D-14:** Fingerprint + metadata — Document contains SHA-256 fingerprint, key alias, creation date, and `keytool` command to re-verify.
- **D-15:** Script captures fingerprint — Keystore generation script prints and writes SHA-256 to the gitignored fingerprint file.
- **D-16:** Capture in Phase 8, register in Phase 10 — Fingerprint documented during keystore creation; Play Console enrollment is Phase 10's responsibility.

### Claude's Discretion
- Exact Gradle `signingConfigs` block structure and env-var decode logic in `app/build.gradle.kts`.
- Whether to add a small `signing.gradle.kts` include vs inline in `app/build.gradle.kts`.
- `keystore-fingerprint.md` exact filename and `.gitignore` entry placement.
- Base64 decode location (Gradle vs CI workflow step) — as long as D-12 env var contract is honored.
- Error message wording and link targets within repo docs.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requirements & Roadmap
- `.planning/REQUIREMENTS.md` — CI-02 (signed release AAB via secrets), RELE-01 (upload keystore); SECU-03 gitignore patterns
- `.planning/ROADMAP.md` — Phase 8 goal and success criteria; dependencies on Phase 7; downstream Phases 10–11
- `.planning/PROJECT.md` — v1.1 infrastructure-only; GitHub Actions + no Fastlane; solo-maintainer workflow
- `.planning/STATE.md` — Current milestone position

### Prior Phase Context
- `.planning/phases/07-security-git-hygiene/07-CONTEXT.md` — D-13 gitignore patterns for `*.jks`, `keystore.properties`; zero-tolerance secrets policy

### Security & Build
- `.gitignore` — SECU-03 signing/credential patterns (extend for `keystore-fingerprint.md`)
- `app/build.gradle.kts` — Current release `buildType` (no `signingConfig` yet; `isMinifyEnabled = false`)
- `docs/SECURITY-CHECKLIST.md` — Item 2 evidence for gitignore coverage of signing files

### Out of Scope (later phases)
- Phase 9: General lint/test CI workflow
- Phase 10: Play Console enrollment and upload-key registration
- Phase 11: Release pipeline workflow using these signing credentials

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `.gitignore`: Already blocks `*.jks`, `*.keystore`, `keystore.properties`, Play credential JSON — extend only for fingerprint doc.
- `app/build.gradle.kts`: Release `buildType` exists; greenfield `signingConfigs` block needed.
- No `keystore.properties`, `keystore.properties.example`, or `scripts/` signing tooling yet.

### Established Patterns
- v1.1 is infrastructure-only — Gradle/build file changes only; no app code changes.
- Solo-maintainer workflow — fail-hard and actionable errors over contributor-friendly unsigned fallbacks.
- Secrets never in source — local file gitignored; CI via GitHub Secrets env vars (Phase 7/11 alignment).
- `isMinifyEnabled = false` acceptable for internal track (per REQUIREMENTS.md out-of-scope note).

### Integration Points
- `app/build.gradle.kts` `signingConfigs` + `buildTypes.release.signingConfig` — core deliverable.
- `scripts/generate-upload-keystore.sh` — creates `upload.jks` + `keystore-fingerprint.md`.
- `keystore.properties.example` — copied to `keystore.properties` for local signing.
- Phase 11 release workflow will consume D-12 env var contract (`ANDROID_KEYSTORE_BASE64`, etc.).
- Phase 10 Play Console setup reads SHA-256 from maintainer's private `keystore-fingerprint.md`.

</code_context>

<specifics>
## Specific Ideas

- User wants zero tolerance for debug-keystore fallback on release builds — aligns with ROADMAP success criterion #1.
- Keystore generation should be repeatable via committed script, not ad-hoc manual `keytool` without repo artifact.
- Fingerprint capture is part of keystore creation (not deferred to Play Console setup), but registration waits for Phase 10.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 8-Gradle Signing Configuration*
*Context gathered: 2026-07-26*
