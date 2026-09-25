# PRD-0002: Release & open source (v1.1)

- **Status:** In progress
- **Owner:** Maintainer
- **Created:** 2026-07-25
- **Last updated:** 2026-09-25 (migrated from GSD `.planning/REQUIREMENTS.md`, `ROADMAP.md`, `STATE.md`)

## Problem

The v1.0 app ([PRD-0001](0001-mvp-badge-tracker.md)) only exists on the
maintainer's machine. Other parents can't install it, contributors can't see
the code, and every build and release is manual — with a real risk of leaking
signing keys or credentials once the repository becomes public.

## Goals

- The repository can be made public safely: no secrets anywhere in git history,
  and guard rails that keep it that way.
- Every push and pull request gets automated lint + unit-test feedback in under
  10 minutes.
- Signed release builds are produced by CI and delivered to the Play Store
  internal testing track without manual steps.
- The repository is contributor-ready (README, licence, contributing guide,
  code owners).

## Non-goals

- Any app feature change — this milestone is infrastructure only.
- Production Play Store rollout (promotion from internal testing stays manual).

## Requirements

Status as of 2026-09-25. Checked = done with evidence.

### Security & secrets (SECU)

- [x] **SECU-01** (must): Automated secrets scan (Gitleaks) passes with zero
  findings on the full git history, locally and in CI on every push/PR.
  - Evidence: `docs/SECURITY-CHECKLIST.md` item 1; `.github/workflows/gitleaks.yml`;
    negative-path test (fake key) failed CI as expected.
- [x] **SECU-02** (must): Manual pre-public security review checklist completed
  and signed off.
  - Evidence: `docs/SECURITY-CHECKLIST.md`, signed 2026-07-25.
- [x] **SECU-03** (must): `.gitignore` hardened for keystores, Play credentials
  and local signing files.

### CI/CD (CI)

- [x] **CI-01** (must): GitHub Actions runs `lintDebug` and `testDebugUnitTest`
  on every push and pull request (`.github/workflows/ci.yml`).
- [ ] **CI-02** (must): A release workflow builds a signed release AAB using
  GitHub Secrets (no secrets in source).
  - Done: Gradle reads signing credentials from environment variables and fails
    hard without them ([ADR-0012](../adr/0012-release-signing-configuration.md)).
  - Remaining: the release workflow itself.
- [x] **CI-03** (must): CI uses Gradle dependency caching; feedback loop under
  10 minutes.

### Release & Play Store (RELE)

- [ ] **RELE-01** (must): Upload keystore generated and Play App Signing enrolled
  in Play Console.
  - Done: `scripts/generate-upload-keystore.sh`; keystore and SHA-256 fingerprint
    exist locally (gitignored); end-to-end signed AAB verified with
    `scripts/verify-release-signature.sh`.
  - Remaining: register the upload key / enrol Play App Signing.
- [ ] **RELE-02** (must): Play Console app record exists for application ID
  `se.simmarken`, and a GCP service account with Release Manager role has its
  JSON key stored in GitHub Secrets.
- [ ] **RELE-03** (must): The release pipeline uploads the signed AAB to the Play
  internal testing track on tag or manual dispatch.
  - Keystore and service-account credentials are decoded to ephemeral runner
    paths outside the checkout and removed after the build.
- [ ] **RELE-04** (must): `versionCode` is incremented before each Play upload —
  no duplicate-version rejection.

### Open source (OSS)

- [ ] **OSS-01** (must): README with project description, prerequisites, and
  build/test instructions that match the CI commands.
- [ ] **OSS-02** (must): MIT `LICENSE` at the repo root, with a carve-out for
  badge assets (`NOTICE` or `docs/ASSETS.md`) consistent with `docs/SOURCES.md`.
- [ ] **OSS-03** (must): `CONTRIBUTING.md` with PR expectations and local
  verification steps (including the PRD/ADR process).
- [ ] **OSS-04** (must): `CODEOWNERS` routes all reviews to the maintainer.
- [ ] **OSS-05** (must): Repository made public **only after** SECU-01 and
  SECU-02 pass on `main`. This is the final step of the milestone.

## Delivery order

Remaining work, in dependency order:

1. **Play Console setup** (RELE-01 remainder, RELE-02) — manual: developer
   account, app record, Play App Signing with the existing upload key, service
   account, first manual upload to internal testing if Play requires it.
2. **Release pipeline** (CI-02, RELE-03, RELE-04) — needs 1.
3. **Open-source publish** (OSS-01 … OSS-04, then OSS-05 last) — re-run the
   full-history Gitleaks scan on `main` immediately before flipping visibility.

## Out of scope

| Item | Reason |
|------|--------|
| Auto-deploy to production track | Internal testing only; production promotion is manual |
| Fastlane | Ruby overhead unnecessary for a solo-maintainer single-app pipeline |
| Crashlytics / analytics | Adds network dependency and a privacy policy; contradicts [ADR-0003](../adr/0003-offline-only-no-backend.md) |
| R8/ProGuard minification | Acceptable to skip for internal track; revisit later |
| APK sideload distribution | Play requires AAB for new apps |
| Committing keystores or service-account JSON | Security risk on a public repo; GitHub Secrets only |
| Instrumented (emulator) tests in CI (CI-04) | Deferred — slow and flaky on hosted runners |
| Branch protection requiring green CI (CI-05) | Deferred |
| Dependabot for Gradle and Actions (CI-06) | Deferred |

## Release criteria

- All *must* requirements checked.
- A tagged or manually dispatched release lands a signed build on the Play
  internal testing track with no manual signing steps.
- Gitleaks is clean on `main` at the moment the repository is made public.

## Risks & open questions

- **Upload key mismatch** between CI secrets and the key registered in Play
  Console — verify the SHA-256 fingerprint before the first automated upload.
- **versionCode collision** on the first automated upload after any manual one.
- **Service account misconfiguration** (missing app-level permission in Play
  Console) — test with a dry run before relying on the pipeline.
- **Asset licensing:** official pin images and requirement text are not ours to
  MIT-license; the carve-out must be explicit before going public.
- Irreversible: once public, any secret in history is exposed. OSS-05 is gated.

## Related ADRs

- [ADR-0011](../adr/0011-secrets-scanning-with-gitleaks.md) Secrets scanning with Gitleaks
- [ADR-0012](../adr/0012-release-signing-configuration.md) Release signing configuration
- [ADR-0013](../adr/0013-ci-on-github-actions.md) CI on GitHub Actions
- [ADR-0014](../adr/0014-play-store-publishing-via-gradle-play-publisher.md) Play Store publishing via Gradle Play Publisher
