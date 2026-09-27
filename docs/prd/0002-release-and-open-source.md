# PRD-0002: Release & open source (v1.1)

- **Status:** In progress
- **Owner:** Maintainer
- **Created:** 2026-07-25
- **Last updated:** 2026-09-27 (store graphics, #6; migrated from GSD `.planning/REQUIREMENTS.md`, `ROADMAP.md`, `STATE.md` and phase 9–10 context)

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
  - Evidence: PR-triggered run green in ~6 min (flyhard/simmmarken#1); a
    deliberately failing unit test turned the check red, then was reverted.
- [ ] **CI-02** (must): A release workflow builds a signed release AAB using
  GitHub Secrets (no secrets in source).
  - Done: Gradle reads signing credentials from environment variables and fails
    hard without them ([ADR-0012](../adr/0012-release-signing-configuration.md)).
  - Remaining: the release workflow itself.
- [x] **CI-03** (must): CI uses Gradle dependency caching; feedback loop under
  10 minutes.
- [x] **CI-07** (should): Claude Code cloud sessions can run the CI checks
  (`./gradlew lintDebug testDebugUnitTest`) with no manual setup
  ([ADR-0016](../adr/0016-android-sdk-in-cloud-agent-sessions.md)).
  - Evidence: flyhard/simmmarken#11 — SessionStart hook installs the Android SDK;
    the CI command passed in a cloud session.

### Release & Play Store (RELE)

- [ ] **RELE-01** (must): Upload keystore generated and Play App Signing enrolled
  in Play Console.
  - Done: `scripts/generate-upload-keystore.sh`; keystore and SHA-256 fingerprint
    exist locally (gitignored); end-to-end signed AAB verified with
    `scripts/verify-release-signature.sh`.
  - Remaining: enrol Play App Signing and register the upload key; the
    upload-key certificate SHA-256 in Play Console must match the local
    `keystore-fingerprint.md`.
- [ ] **RELE-02** (must): Play Console app record exists for application ID
  `se.simmarken`, and a GCP service account with the Release Manager role has
  its JSON key stored in the GitHub Secret `PLAY_SERVICE_ACCOUNT_JSON`
  ([ADR-0015](../adr/0015-play-console-bootstrap.md)).
  - At least one signed AAB (`versionCode` 1, `versionName` "1.0", built
    locally and checked with `scripts/verify-release-signature.sh`) is accepted
    on the internal testing track.
  - Steps and evidence (dates, release IDs, screenshots) are recorded in a
    committed runbook, `docs/PLAY-CONSOLE-SETUP.md`.
  - Done: runbook written with evidence placeholders.
  - Remaining: carry out the steps and fill in the evidence.
- [ ] **RELE-03** (must): The release pipeline uploads the signed AAB to the Play
  internal testing track on tag or manual dispatch.
  - Keystore and service-account credentials are decoded to ephemeral runner
    paths outside the checkout and removed after the build.
- [ ] **RELE-04** (must): `versionCode` is incremented before each Play upload —
  no duplicate-version rejection.
- [ ] **RELE-05** (must): Minimum store presence required by Play for internal
  testing.
  - App name **Simmärken**, default store language Swedish (`sv-SE`).
  - A privacy policy at a stable public HTTPS URL (e.g. GitHub Pages)
    stating that the app works offline, collects no data, stores everything
    locally and only exports JSON when the user asks. Consistent with
    `docs/SECURITY-CHECKLIST.md`.
  - IARC content rating questionnaire completed honestly (no ads, no data
    collection, no in-app purchases); expected rating Everyone / PEGI 3.
  - Internal testers: maintainer only; the runbook explains how to add family
    testers later.
  - Store graphics: 512×512 app icon and 1024×500 feature graphic generated
    from the launcher icon, and at least 2 phone screenshots that meet Play's
    size and ratio rules, taken with made-up child names only
    ([ADR-0017](../adr/0017-store-graphics-from-launcher-icon.md)).
  - Done: policy text drafted in `docs/privacy-policy.md` (Swedish + English);
    runbook `docs/PLAY-CONSOLE-SETUP.md` covers listing, IARC and data safety;
    `scripts/store-graphics.py` generates the icon and feature graphic and
    checks screenshots (flyhard/simmmarken#6).
  - Remaining: host the policy at a public URL, take the screenshots and
    complete the Console steps.

- [ ] **RELE-06** (must): Adaptive launcher icon (foreground, solid brand-colour
  background, monochrome layer for themed icons, round variant) lands on `main`
  before the first manual upload of `versionCode` 1 (RELE-02); store graphics
  are derived from it.
  - Done: swimming-badge vector in `res/drawable/ic_launcher_foreground.xml`,
    adaptive icons in `res/mipmap-anydpi/`, all artwork inside the 66dp safe
    zone (flyhard/simmmarken#5).
  - Remaining: check the icon on a real launcher (light and themed-icon modes).

### Open source (OSS)

- [x] **OSS-01** (must): README with project description, prerequisites, and
  build/test instructions that match the CI commands.
  - Evidence: `README.md`.
- [x] **OSS-02** (must): MIT `LICENSE` at the repo root, with a carve-out for
  badge assets (`NOTICE` or `docs/ASSETS.md`) consistent with `docs/SOURCES.md`.
  - Evidence: `LICENSE`, `NOTICE`; `docs/SOURCES.md` *Project license* updated.
- [x] **OSS-03** (must): `CONTRIBUTING.md` with PR expectations and local
  verification steps (including the PRD/ADR process).
  - Evidence: `CONTRIBUTING.md`.
- [x] **OSS-04** (must): `CODEOWNERS` routes all reviews to the maintainer.
  - Evidence: `.github/CODEOWNERS` (`* @flyhard`).
- [ ] **OSS-05** (must): Repository made public **only after** SECU-01 and
  SECU-02 pass on `main`. This is the final step of the milestone.

## Delivery order

Remaining work, in dependency order:

1. **Play Console setup** (RELE-01 remainder, RELE-02, RELE-05) — manual, per
   [ADR-0015](../adr/0015-play-console-bootstrap.md): app record and store
   listing, privacy policy, Play App Signing with the existing upload key,
   first internal-testing release uploaded by hand, service account and
   `PLAY_SERVICE_ACCOUNT_JSON`.
2. **Release pipeline** (CI-02, RELE-03, RELE-04) — needs 1.
3. **Open-source publish** (OSS-01 … OSS-04 done; OSS-05 last) — re-run the
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
- [ADR-0015](../adr/0015-play-console-bootstrap.md) Play Console bootstrap
- [ADR-0016](../adr/0016-android-sdk-in-cloud-agent-sessions.md) Android SDK in Claude Code cloud sessions
- [ADR-0017](../adr/0017-store-graphics-from-launcher-icon.md) Store graphics generated from the launcher icon
