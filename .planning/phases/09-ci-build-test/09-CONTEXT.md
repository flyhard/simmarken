# Phase 9: CI Build & Test - Context

**Gathered:** 2026-07-27
**Status:** Ready for planning

<domain>
## Phase Boundary

Deliver fast automated feedback on every code change via GitHub Actions running `lintDebug` and `testDebugUnitTest` (CI-01). Workflow must complete in under 10 minutes with Gradle dependency caching enabled (CI-03), and any lint or test failure must fail the workflow with a visible red check on PRs. Does not include Gitleaks scanning (Phase 7 — already in `.github/workflows/gitleaks.yml`), release signing builds (Phase 11), instrumented/emulator tests (CI-04 deferred), branch protection (CI-05 deferred), or Dependabot (CI-06 deferred).

</domain>

<decisions>
## Implementation Decisions

### Workflow Triggers
- **D-01:** All branches — CI runs on push to any branch and on all pull requests; no branch restriction.
- **D-02:** Events are `push` + `pull_request` — matches CI-01 and the existing gitleaks workflow pattern.
- **D-03:** No path filter — every push/PR runs full lint + test regardless of changed files (solo-maintainer simplicity; ~18 unit tests are fast enough).
- **D-04:** Workflow file is `.github/workflows/ci.yml` — separate from `gitleaks.yml` per Phase 7 decision.

### Job Layout
- **D-05:** Single job — one job runs lint and unit tests sequentially with one Android SDK setup (fits CI-03 <10 min goal).
- **D-06:** Combined Gradle invocation — `./gradlew lintDebug testDebugUnitTest` in a single step for shared configuration and dependency resolution.
- **D-07:** Job name is `lint-and-test` — explicit status check label on PRs for solo-maintainer clarity.
- **D-08:** Fail fast — if lint fails, unit tests do not run.

### Android Runner Setup
- **D-09:** Runner OS is `ubuntu-latest` — matches existing gitleaks workflow.
- **D-10:** JDK 17 — matches `app/build.gradle.kts` `compileOptions` and `jvmTarget`.
- **D-11:** Android SDK via `android-actions/setup-android` — handles SDK install and license acceptance.
- **D-12:** Gradle caching via `gradle/actions/setup-gradle` — satisfies CI-03 dependency caching requirement.

### PR Behavior
- **D-13:** Cancel in-progress runs — new push to same PR cancels stale workflow run via concurrency group.
- **D-14:** Minimal permissions — `contents: read` only; no `pull-requests: write`.
- **D-15:** Red X only — standard GitHub check failure; no JUnit report upload or PR comment bots.
- **D-16:** No `workflow_dispatch` — automated push/PR triggers only.

### Claude's Discretion
- Exact `android-actions/setup-android` version pin and any compileSdk/targetSdk package selection.
- `gradle/actions/setup-gradle` configuration options (e.g., cache-read-only on PRs).
- Concurrency group naming and `cancel-in-progress` YAML syntax.
- Workflow step ordering (checkout → JDK → Android SDK → Gradle setup → build).
- Whether to add `--no-daemon` or other Gradle flags for CI reliability.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requirements & Roadmap
- `.planning/REQUIREMENTS.md` — CI-01 (lint + unit test on push/PR), CI-03 (Gradle caching <10 min); CI-04/05/06 deferred
- `.planning/ROADMAP.md` — Phase 9 goal and success criteria; depends on Phase 7
- `.planning/PROJECT.md` — v1.1 infrastructure-only; GitHub Actions + no Fastlane
- `.planning/STATE.md` — Current milestone position

### Prior Phase Context
- `.planning/phases/07-security-git-hygiene/07-CONTEXT.md` — Separate Gitleaks workflow; CI-only enforcement; no local pre-commit hooks
- `.planning/phases/08-gradle-signing-configuration/08-CONTEXT.md` — D-02 debug builds/tests work without signing credentials

### Existing CI & Build
- `.github/workflows/gitleaks.yml` — Existing workflow pattern (push + PR, ubuntu-latest, checkout@v6)
- `app/build.gradle.kts` — Java 17 target; release signing guard does not block debug lint/test tasks
- `gradle/wrapper/gradle-wrapper.properties` — Gradle 9.6.1 wrapper version
- `gradle/libs.versions.toml` — AGP 9.3.0, Kotlin 2.2.10 dependency versions

### Out of Scope (later phases / deferred)
- Phase 11: Release pipeline workflow (`bundleRelease` with signing secrets)
- CI-04: Instrumented tests in CI (emulator) — `app/src/androidTest/` exists but excluded
- CI-05: Branch protection requiring CI green
- CI-06: Dependabot for Gradle and GitHub Actions

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `.github/workflows/gitleaks.yml`: Established workflow conventions — `on: [pull_request, push]`, `ubuntu-latest`, `actions/checkout@v6` with `fetch-depth: 0` (CI workflow does not need full history).
- `app/build.gradle.kts`: `testDebugUnitTest` and `lintDebug` tasks available; release signing `whenReady` guard only blocks release tasks.
- `app/src/test/`: ~18 JVM unit tests (Robolectric, coroutines test, JUnit) — no signing or emulator required.

### Established Patterns
- v1.1 is infrastructure-only — workflow YAML only; no app code changes expected.
- Solo-maintainer workflow — prefer simple, minimal-permission CI over advanced reporting.
- Separate workflows per concern — Gitleaks (Phase 7) and build/test (Phase 9) are independent files.
- Secrets never needed for debug lint/test — aligns with Phase 8 D-02.

### Integration Points
- `.github/workflows/ci.yml` — new workflow file; second workflow in repo after gitleaks.
- Gradle wrapper (`./gradlew`) — CI must use wrapper, not system Gradle.
- Phase 12 OSS-01 README will document the same `./gradlew lintDebug testDebugUnitTest` commands CI runs.

</code_context>

<specifics>
## Specific Ideas

- User wants maximum simplicity: no path filters, no manual dispatch, no test report uploads.
- Status check name `lint-and-test` should be clear enough for future branch protection (CI-05) without extra configuration later.
- Combined single-job approach prioritizes total wall-clock time over parallel job granularity.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope. CI-04 (instrumented tests), CI-05 (branch protection), and CI-06 (Dependabot) remain deferred per REQUIREMENTS.md v2 section.

</deferred>

---

*Phase: 9-CI Build & Test*
*Context gathered: 2026-07-27*
