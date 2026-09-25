# ADR-0015: Play Console bootstrap

- **Status:** Accepted (not yet implemented)
- **Date:** 2026-08-09 (recorded retroactively 2026-09-25)
- **Related:** PRD-0002 (RELE-01, RELE-02, RELE-05), ADR-0012, ADR-0014

## Context

Automated publishing (ADR-0014) needs an existing Play Console app, Play App
Signing enrolment and API credentials. The Play Developer API cannot create a
new app, so part of the setup is necessarily manual and happens once.

## Decision

- **First upload is manual.** Build `bundleRelease` locally with
  `keystore.properties`, check it with `scripts/verify-release-signature.sh`,
  and upload it via the Play Console UI to the internal testing track, with
  `versionCode` 1 / `versionName` "1.0" and short Swedish release notes.
  Automated uploads start with the release pipeline.
- **Play App Signing** is enrolled with the existing upload key (ADR-0012). The
  upload certificate fingerprint in the Console must match `keystore-fingerprint.md`.
- **Service account:** created in the GCP project linked from Play Console
  (Setup → API access), with the Play Developer API enabled. The account is
  invited in Play Console with **Release Manager** only (least privilege).
- **Secret name:** the full JSON key is stored as the GitHub Secret
  `PLAY_SERVICE_ACCOUNT_JSON`. This is the only secret added at this step; the
  keystore secrets are added with the release workflow.
- **Track:** Play's default internal testing track; testers are the maintainer only.
- **Runbook:** the steps and evidence (dates, release IDs, screenshots) go in a
  committed `docs/PLAY-CONSOLE-SETUP.md`, not in private notes.

## Consequences

- The secret name `PLAY_SERVICE_ACCOUNT_JSON` is referenced by the release
  workflow; renaming it later means updating the workflow and the runbook.
- The privacy-policy URL is stored in Play Console, so moving where the policy
  is hosted means updating the Console too.

## Origin

GSD Phase 10 D-01…D-16 (`.planning/phases/10-play-console-setup/10-CONTEXT.md`
at commit `dcf12df`).
