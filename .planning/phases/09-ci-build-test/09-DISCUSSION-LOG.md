# Phase 9: CI Build & Test - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-27
**Phase:** 9-CI Build & Test
**Areas discussed:** Triggers, Job layout, Android runner setup, PR behavior

---

## Triggers

| Option | Description | Selected |
|--------|-------------|----------|
| All branches | Every push to any branch + all PRs | ✓ |
| main + PRs only | Push CI only on main; feature branches via PR | |
| You decide | Pick what fits solo-maintainer workflow | |

**User's choice:** All branches

| Option | Description | Selected |
|--------|-------------|----------|
| push + pull_request | Standard: runs on direct pushes and PRs | ✓ |
| push only | Simpler, but PRs from forks won't run until merge | |
| pull_request only | No CI on direct pushes to branches | |

**User's choice:** push + pull_request

| Option | Description | Selected |
|--------|-------------|----------|
| No path filter | Every push/PR runs full lint + test | ✓ |
| Code paths only | Skip when only docs/.planning/README change | |
| You decide | | |

**User's choice:** No path filter

| Option | Description | Selected |
|--------|-------------|----------|
| ci.yml | Standard name, separate from gitleaks.yml | ✓ |
| build-test.yml | More descriptive filename | |
| You decide | | |

**User's choice:** ci.yml

---

## Job Layout

| Option | Description | Selected |
|--------|-------------|----------|
| Single job | One job runs lintDebug then testDebugUnitTest sequentially | ✓ |
| Parallel jobs | Separate lint and test jobs run simultaneously | |

**User's choice:** Single job

| Option | Description | Selected |
|--------|-------------|----------|
| Combined Gradle invocation | `./gradlew lintDebug testDebugUnitTest` single step | ✓ |
| Two separate steps | lint first, then test (clearer step failure) | |
| You decide | | |

**User's choice:** Combined Gradle invocation

| Option | Description | Selected |
|--------|-------------|----------|
| lint-and-test | Explicit about what ran | ✓ |
| build-test | Descriptive status check name | |
| ci | Short, matches workflow filename | |

**User's choice:** lint-and-test

| Option | Description | Selected |
|--------|-------------|----------|
| Fail fast | Stop on first failing task | ✓ |
| Run all | Always run both even if lint fails | |
| You decide | | |

**User's choice:** Fail fast

---

## Android Runner Setup

| Option | Description | Selected |
|--------|-------------|----------|
| ubuntu-latest | Standard GitHub-hosted runner | ✓ |
| ubuntu-24.04 | Pin to specific Ubuntu version | |
| You decide | | |

**User's choice:** ubuntu-latest

| Option | Description | Selected |
|--------|-------------|----------|
| JDK 17 | Matches app/build.gradle.kts jvmTarget | ✓ |
| JDK 21 | Newer LTS, mismatches Java 17 target | |
| You decide | | |

**User's choice:** JDK 17

| Option | Description | Selected |
|--------|-------------|----------|
| android-actions/setup-android | Community action for SDK + licenses | ✓ |
| Manual sdkmanager | Explicit sdkmanager commands | |
| You decide | | |

**User's choice:** android-actions/setup-android

| Option | Description | Selected |
|--------|-------------|----------|
| gradle/actions/setup-gradle | Standard dependency caching | ✓ |
| actions/cache manually | Hand-rolled ~/.gradle/caches paths | |
| No caching | Simpler YAML, slower runs | |

**User's choice:** gradle/actions/setup-gradle

---

## PR Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Cancel in-progress | New push cancels stale run | ✓ |
| No cancellation | Let all runs complete | |
| You decide | | |

**User's choice:** Cancel in-progress

| Option | Description | Selected |
|--------|-------------|----------|
| contents: read only | Minimal permissions | ✓ |
| contents: read + pull-requests: write | Enable future PR comment bots | |
| You decide | | |

**User's choice:** contents: read only

| Option | Description | Selected |
|--------|-------------|----------|
| Red X only | Standard GitHub check failure | ✓ |
| JUnit test report upload | Publish test results as annotations | |
| You decide | | |

**User's choice:** Red X only

| Option | Description | Selected |
|--------|-------------|----------|
| No manual trigger | push + pull_request only | ✓ |
| workflow_dispatch | Manual re-run from Actions tab | |
| You decide | | |

**User's choice:** No manual trigger

---

## Claude's Discretion

- Action version pins for setup-android and setup-gradle
- Gradle CI flags and concurrency group naming
- Step ordering details

## Deferred Ideas

None — discussion stayed within phase scope.
