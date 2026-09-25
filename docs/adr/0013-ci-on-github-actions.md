# ADR-0013: CI on GitHub Actions

- **Status:** Accepted
- **Date:** 2026-07-28 (recorded retroactively 2026-09-25)
- **Related:** PRD-0002 (CI-01, CI-03)

## Context

Every change needs fast automated feedback, and the code is hosted on GitHub.

## Decision

- `.github/workflows/ci.yml`, separate from the Gitleaks workflow.
- Triggers: `push` (all branches) and `pull_request`; no path filters, no
  `workflow_dispatch`.
- One job, `lint-and-test`, on `ubuntu-latest`: JDK 17 (Temurin),
  `android-actions/setup-android`, `gradle/actions/setup-gradle` for caching,
  then a single `./gradlew lintDebug testDebugUnitTest --no-daemon`.
- Concurrency group cancels superseded runs; permissions are `contents: read`.
- Failure is reported as a plain red check — no report uploads or PR bots.
- Lint issues are fixed in code rather than suppressed via a lint baseline.

## Alternatives considered

- **Separate lint and test jobs** — duplicate SDK setup and exceed the time budget
  for little gain.

## Consequences

- Instrumented Room tests (`androidTest`) are **not** run in CI; they must be run
  locally when touching the database or seed (deferred as CI-04).
- Target: under 10 minutes per run.

## Origin

GSD Phase 9 D-01…D-16.
