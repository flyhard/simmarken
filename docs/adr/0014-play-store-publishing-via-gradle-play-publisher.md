# ADR-0014: Play Store publishing via Gradle Play Publisher

- **Status:** Accepted (not yet implemented)
- **Date:** 2026-07-25 (recorded retroactively 2026-09-25)
- **Related:** PRD-0002 (CI-02, RELE-02 … RELE-04), ADR-0012, ADR-0013, ADR-0015

## Context

Signed release bundles need to reach the Play internal testing track without
manual steps. The project has one app and one maintainer.

## Decision

- Use the **Gradle Play Publisher** plugin (4.x) driven from a GitHub Actions
  release workflow, triggered by a tag or manual dispatch.
- Publish to the **internal testing** track only; promotion to production is manual.
- Authenticate with a GCP service account (Release Manager) whose JSON key is
  the GitHub Secret `PLAY_SERVICE_ACCOUNT_JSON` (ADR-0015), decoded to an
  ephemeral path outside the checkout and removed after the job. The keystore
  secrets from ADR-0012 are added to the repository with this workflow.
- `versionCode` is incremented automatically for each upload.

## Alternatives considered

- **Fastlane** — Ruby toolchain overhead unnecessary for a single app.
- **Manual uploads** — error-prone and doesn't scale to contributors.

## Consequences

- Requires Play Console setup first (ADR-0015): the Play Developer API cannot
  create an app, so the first release is uploaded by hand.
- Revisit this ADR when the release workflow is implemented; supersede it if the
  approach changes.

## Origin

GSD v1.1 milestone decisions (`STATE.md`), `research/STACK.md` and `research/PITFALLS.md`.
