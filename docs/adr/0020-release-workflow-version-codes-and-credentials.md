# ADR-0020: Release workflow: versionCode from Play, credentials outside the checkout

- **Status:** Proposed
- **Date:** 2026-09-29
- **Related:** PRD-0002 (CI-02, RELE-03, RELE-04), amends ADR-0014; ADR-0015, ADR-0019

## Context

ADR-0014 says the release workflow publishes with Gradle Play Publisher (GPP)
and that `versionCode` "is incremented automatically for each upload", without
saying how. Play rejects an upload whose `versionCode` it has seen before, on
any track. `versionCode` 1 is uploaded by hand (ADR-0015), and the maintainer
may upload by hand again later (a hotfix from a laptop, a re-do of the
bootstrap), so the pipeline can't assume it is the only uploader.

Issue #8 also fixes that the keystore and the service-account JSON are written
only outside the checkout. ADR-0019's CI path (`ANDROID_KEYSTORE_BASE64`) has
Gradle decode the keystore into `app/build/signing/`, which is inside it.

## Decision

**versionCode.** GPP's `resolutionStrategy = AUTO`. Before bundling, GPP asks
the Play Developer API for the highest `versionCode` on any track and builds
the bundle with the next one. `versionCode` in `app/build.gradle.kts` stays 1
and only applies to local builds and the manual first upload. The workflow
reads the resolved code from the bundle's manifest and refuses to upload unless
it is above 1, so an automated upload can never take the manual upload's slot
(for example if Play has no release yet). Runs are serialised with a
`concurrency` group, because two parallel runs would resolve the same code.

**GPP only with credentials.** The `play {}` block is enabled only when
`PLAY_SERVICE_ACCOUNT_FILE` is set. Otherwise AUTO would make every local
`bundleRelease` call Play, and the manual first upload would need API
credentials that don't exist yet.

**Credential files.** `.github/workflows/release.yml` checks that all five
secrets are non-empty before anything else runs, then writes the decoded
keystore and the service-account JSON to `$RUNNER_TEMP/release-credentials/`
(mode 0600) and passes their paths to Gradle as `ANDROID_KEYSTORE_FILE`
(ADR-0019's file path, which takes precedence over the base64 path) and
`PLAY_SERVICE_ACCOUNT_FILE`. An `if: always()` step deletes the directory.
The secret names are unchanged: `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`, `PLAY_SERVICE_ACCOUNT_JSON`.

The "release signing is not configured" guard in `app/build.gradle.kts` now also
matches `*ReleaseBundle` tasks, because `publishReleaseBundle` goes through
`signReleaseBundle` without the `bundleRelease` lifecycle task.

## Alternatives considered

- **`github.run_number` (plus an offset)** — predictable, and needs no API
  call to build. But it knows nothing about manual uploads, so the first
  pipeline run after one can collide, and a re-run attempt reuses the same
  number. The offset would have to be bumped by hand after every manual upload.
- **Commit count or a timestamp** — same blindness to manual uploads; a
  timestamp also burns through the 2 100 000 000 limit quickly.
- **Bumping `versionCode` in git from CI** — needs `contents: write` and a
  bot commit on every release, against least privilege.
- **GPP reading the JSON from `ANDROID_PUBLISHER_CREDENTIALS`** — avoids the
  file entirely, but the issue's contract is an ephemeral file, and a file with
  0600 permissions in the runner's temp directory is easier to reason about than
  a large secret in every Gradle process's environment.

## Consequences

- A tag or dispatch always gets a fresh `versionCode`; manual uploads in between
  are fine.
- The `versionCode` of a build is only known once the run has talked to Play; it
  is shown in the run's summary and in Play Console, not in git.
- A release build in CI needs the Play API to be reachable even to build.
- `versionName` still comes from `app/build.gradle.kts`; bump it there when a
  release should show a new version to users.
