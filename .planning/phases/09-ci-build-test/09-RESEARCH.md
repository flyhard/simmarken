# Phase 9: CI Build & Test - Research

**Researched:** 2026-07-27
**Domain:** GitHub Actions CI for Android Gradle (lint + JVM unit tests)
**Confidence:** HIGH

## Summary

Phase 9 delivers a single new workflow file (`.github/workflows/ci.yml`) that runs `./gradlew lintDebug testDebugUnitTest` on every `push` and `pull_request` across all branches. All structural decisions are locked in `09-CONTEXT.md`: separate workflow from Gitleaks, one `lint-and-test` job on `ubuntu-latest`, JDK 17, `android-actions/setup-android`, `gradle/actions/setup-gradle` caching, concurrency cancel-in-progress, and `contents: read` permissions only.

The project is ready for CI mechanically: Gradle wrapper 9.6.1, AGP 9.3.0, Kotlin 2.2.10, 19 JVM unit test classes under `app/src/test/` (Robolectric + JUnit 4), and Phase 8 signing guard that blocks only release tasks — debug lint/test need no secrets. Local `testDebugUnitTest` passes in ~9s warm; full `lintDebug testDebugUnitTest` took ~3m25s cold (well under the 10-minute CI-03 budget once caches warm).

**Critical pre-flight finding:** `lintDebug` currently fails locally with 2 Compose lint errors in `SettingsScreen.kt` (`LocalContextGetResourceValueCall`). CI will show a red X on first run until those are fixed or baselined. This is outside the "workflow YAML only" ideal but required for CI-01 success criteria ("failed lint causes workflow to fail" implies lint must also pass). Planner should include a lint-fix or baseline task before or alongside workflow creation.

**Primary recommendation:** Add `.github/workflows/ci.yml` matching the locked CONTEXT decisions; pin `android-actions/setup-android@v4` and `gradle/actions/setup-gradle@v6`; install `platforms;android-36` explicitly (matching `compileSdk = 36`); do not duplicate Gradle caching via `setup-java`.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Workflow Triggers
- **D-01:** All branches — CI runs on push to any branch and on all pull requests; no branch restriction.
- **D-02:** Events are `push` + `pull_request` — matches CI-01 and the existing gitleaks workflow pattern.
- **D-03:** No path filter — every push/PR runs full lint + test regardless of changed files (solo-maintainer simplicity; ~18 unit tests are fast enough).
- **D-04:** Workflow file is `.github/workflows/ci.yml` — separate from `gitleaks.yml` per Phase 7 decision.

#### Job Layout
- **D-05:** Single job — one job runs lint and unit tests sequentially with one Android SDK setup (fits CI-03 <10 min goal).
- **D-06:** Combined Gradle invocation — `./gradlew lintDebug testDebugUnitTest` in a single step for shared configuration and dependency resolution.
- **D-07:** Job name is `lint-and-test` — explicit status check label on PRs for solo-maintainer clarity.
- **D-08:** Fail fast — if lint fails, unit tests do not run.

#### Android Runner Setup
- **D-09:** Runner OS is `ubuntu-latest` — matches existing gitleaks workflow.
- **D-10:** JDK 17 — matches `app/build.gradle.kts` `compileOptions` and `jvmTarget`.
- **D-11:** Android SDK via `android-actions/setup-android` — handles SDK install and license acceptance.
- **D-12:** Gradle caching via `gradle/actions/setup-gradle` — satisfies CI-03 dependency caching requirement.

#### PR Behavior
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

### Deferred Ideas (OUT OF SCOPE)

None — discussion stayed within phase scope. CI-04 (instrumented tests), CI-05 (branch protection), and CI-06 (Dependabot) remain deferred per REQUIREMENTS.md v2 section.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CI-01 | GitHub Actions runs `lintDebug` and `testDebugUnitTest` on every push and pull request | Workflow triggers `push` + `pull_request` on all branches; single job runs `./gradlew lintDebug testDebugUnitTest`; tasks exist and are verified locally |
| CI-03 | CI uses Gradle dependency caching to keep feedback loop under 10 minutes | `gradle/actions/setup-gradle@v6` caches Gradle User Home; cold local run ~3.5 min; warm unit tests ~9s; enhanced cache free for public repos |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Lint execution (`lintDebug`) | CI runner (GitHub Actions) | Android Gradle Plugin | Lint is a Gradle task; CI invokes it; AGP owns lint rules |
| Unit test execution (`testDebugUnitTest`) | CI runner | JVM on runner (Robolectric) | Tests run on host JVM, no emulator; Robolectric provides Android stubs |
| Dependency/SDK caching | CI runner (GitHub Actions cache) | Gradle User Home | `setup-gradle` restores `~/.gradle` between runs |
| Android SDK provisioning | CI runner | `android-actions/setup-android` | Ubuntu runners lack preinstalled SDK matching `compileSdk 36` |
| Signing credentials | Not applicable | Phase 8 release guard | Debug tasks skip release signing check per Phase 8 D-02 |
| Workflow trigger policy | GitHub (repository events) | — | `on: [push, pull_request]` at workflow level |

## Standard Stack

### Core

| Component | Version | Purpose | Why Standard |
|-----------|---------|---------|--------------|
| GitHub Actions | — | CI orchestration | Locked in PROJECT.md; solo-maintainer, no Fastlane |
| `actions/checkout` | v6 | Clone repo | Matches existing `gitleaks.yml` [VERIFIED: codebase] |
| `actions/setup-java` | v5 | JDK 17 provisioning | Official Java setup; Gradle docs recommend Temurin 17 [CITED: github.com/gradle/actions] |
| `android-actions/setup-android` | v4 | SDK cmdline-tools + license acceptance | Canonical maintained action for Android CI [CITED: github.com/android-actions/setup-android] |
| `gradle/actions/setup-gradle` | v6 | Gradle cache + wrapper validation | Gradle's official CI action; supersedes `setup-java` `cache: gradle` [CITED: docs.gradle.org/current/userguide/github-actions.html] |
| Gradle Wrapper | 9.6.1 | Build execution | `gradle/wrapper/gradle-wrapper.properties` [VERIFIED: codebase] |
| Android Gradle Plugin | 9.3.0 | Lint + Android build | `gradle/libs.versions.toml` [VERIFIED: codebase] |

### Supporting

| Component | Version | Purpose | When to Use |
|-----------|---------|---------|-------------|
| `sdkmanager` (post-setup) | cmdline-tools 20.0 default | Install `platforms;android-36` | When `compileSdk = 36` not in default packages [CITED: github.com/android-actions/setup-android] |
| `--no-daemon` | — | CI Gradle flag | Recommended for ephemeral runners [CITED: docs.gradle.org/current/userguide/github-actions.html] |
| Robolectric | 4.14.1 | JVM Android stubs in unit tests | Already in project test deps [VERIFIED: codebase] |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `gradle/actions/setup-gradle` | `actions/setup-java` with `cache: gradle` | Simpler but weaker cache; duplicate if both used; CONTEXT locks setup-gradle |
| `android-actions/setup-android` | `amyu/setup-android` / manual `sdkmanager` | More features but not locked; setup-android is CONTEXT decision |
| Combined Gradle step | Separate lint + test steps | Clearer fail-fast but violates D-06; use task order + no `--continue` instead |
| `ubuntu-latest` preinstalled SDK | Explicit setup action | Ubuntu images do not reliably include API 36 platform [CITED: github.com/android/camera-samples workflow] |

**Installation:** No new project dependencies. Workflow-only change.

**Version verification:** Action major versions verified via official GitHub repos and Gradle docs (2026-07-27).

## Package Legitimacy Audit

> No new npm/PyPI/crates packages. Phase installs pinned GitHub Actions marketplace actions only.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| — | — | — | — | — | N/A | No language-ecosystem packages |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

*GitHub Actions should be pinned to major version tags (`@v4`, `@v6`) per existing repo convention (`checkout@v6`, `gitleaks-action@v3`).*

## Architecture Patterns

### System Architecture Diagram

```
GitHub push/PR event
        │
        ▼
┌───────────────────────────────────────┐
│  .github/workflows/ci.yml             │
│  concurrency: cancel stale PR runs    │
│  permissions: contents: read          │
└───────────────────────────────────────┘
        │
        ▼
┌───────────────────────────────────────┐
│  Job: lint-and-test (ubuntu-latest)   │
│                                       │
│  1. checkout@v6 (shallow OK)        │
│  2. setup-java@v5 (JDK 17, temurin) │
│  3. setup-android@v4 (+ platform 36)  │
│  4. setup-gradle@v6 (cache restore)  │
│  5. ./gradlew lintDebug               │
│     testDebugUnitTest --no-daemon     │
│     (fail on first task failure)      │
└───────────────────────────────────────┘
        │
        ├──► lintDebug ──► AGP Lint ──► pass/fail (red X)
        │
        └──► testDebugUnitTest ──► JUnit/Robolectric ──► pass/fail
```

### Recommended Project Structure

```
.github/workflows/
├── gitleaks.yml      # existing — unchanged
└── ci.yml            # NEW — Phase 9 deliverable
```

No changes to `app/`, `gradle/`, or test sources unless fixing pre-existing lint failures.

### Pattern 1: Minimal Android CI Workflow

**What:** Single-job workflow with official setup actions and wrapper invocation.
**When to use:** Solo-maintainer OSS prep with JVM unit tests only (no emulator).

**Example:**

```yaml
# Source: github.com/gradle/actions + github.com/android-actions/setup-android
name: CI

on:
  pull_request:
  push:

concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: true

jobs:
  lint-and-test:
    name: lint-and-test
    runs-on: ubuntu-latest
    permissions:
      contents: read
    steps:
      - uses: actions/checkout@v6

      - name: Set up JDK 17
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '17'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v4

      - name: Install compileSdk platform
        run: sdkmanager "platforms;android-36"

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v6

      - name: Lint and unit test
        run: ./gradlew lintDebug testDebugUnitTest --no-daemon
```

### Pattern 2: Concurrency Group for PR Cancel

**What:** Cancel superseded runs when new commits push to the same PR/branch.
**When to use:** D-13; workflow triggers both `push` and `pull_request`.

```yaml
# Source: docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions#concurrency
concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: true
```

Use `github.event.pull_request.number || github.ref` so push events (no PR number) still get a stable group per branch [CITED: docs.github.com].

### Pattern 3: Gradle Caching (CI-03)

**What:** `setup-gradle` caches Gradle User Home (distributions, dependencies, build cache subset).
**When to use:** Every CI run; satisfies CI-03.

```yaml
# Source: docs.gradle.org/current/userguide/github-actions.html
- uses: gradle/actions/setup-gradle@v6
  # Optional discretion: cache-read-only on PRs from forks — not needed for solo-maintainer private repo
```

Do **not** also set `cache: gradle` on `setup-java` — redundant and can conflict [CITED: github.com/gradle/actions].

### Anti-Patterns to Avoid

- **Combining Gitleaks and build/test in one workflow:** Violates Phase 7/9 separation; different permissions (`pull-requests: write` vs `contents: read`).
- **`fetch-depth: 0` on CI build:** Unnecessary for lint/test; adds clone time. Reserve full history for Gitleaks only.
- **Release signing secrets in CI workflow:** Debug tasks need no `ANDROID_KEYSTORE_BASE64` per Phase 8 D-02.
- **Instrumented tests (`connectedDebugAndroidTest`):** Deferred CI-04; requires emulator and adds >10 min.
- **Path filters on workflow:** Violates D-03.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Gradle dependency caching | Custom `actions/cache` paths | `gradle/actions/setup-gradle@v6` | Optimized cache keys, wrapper validation, cleanup [CITED: gradle/actions] |
| Android SDK install | Manual wget/unzip scripts | `android-actions/setup-android@v4` | License acceptance, PATH, problem matchers |
| JDK provisioning | `apt install openjdk` | `actions/setup-java@v5` | Consistent Temurin 17 across runners |
| Lint/test reporting bots | Custom PR comment actions | GitHub check pass/fail only | Locked D-15 |
| Emulator-based CI | Custom AVD scripts | Defer to CI-04 | Out of scope; 18 JVM tests sufficient |

**Key insight:** Android CI boilerplate is well-solved by official Gradle + Android Actions; custom caching or SDK scripts add maintenance without benefit for a single-module app.

## Common Pitfalls

### Pitfall 1: Pre-existing Lint Failures Block First Green CI

**What goes wrong:** Workflow is correct but every run fails on `lintDebug` before tests matter.
**Why it happens:** `SettingsScreen.kt` triggers `LocalContextGetResourceValueCall` (2 errors confirmed locally 2026-07-27).
**How to avoid:** Fix lint errors or add `lint { baseline = ... }` before declaring phase complete; verify `./gradlew lintDebug` passes locally.
**Warning signs:** `BUILD FAILED` on `:app:lintDebug` with Compose resource lint IDs.

### Pitfall 2: Missing Android Platform for compileSdk 36

**What goes wrong:** Build fails with "Failed to install the following SDK components: platforms;android-36".
**Why it happens:** `setup-android` default packages are only `tools platform-tools` [CITED: github.com/android-actions/setup-android].
**How to avoid:** Add explicit `sdkmanager "platforms;android-36"` step (or `packages` input with platform + build-tools).
**Warning signs:** SDK component errors during `:app:compileDebugKotlin`.

### Pitfall 3: Fail-Fast Semantics with Combined Gradle Command

**What goes wrong:** Unit tests run even when lint fails, wasting CI minutes.
**Why it happens:** `lintDebug` and `testDebugUnitTest` are independent tasks; with `--parallel` they could overlap.
**How to avoid:** List `lintDebug` before `testDebugUnitTest`; omit `--parallel`; do not pass `--continue`. Project has no `org.gradle.parallel=true` in `gradle.properties` [VERIFIED: codebase]. If fail-fast must be guaranteed, document that Gradle aborts the build on first task failure without `--continue` [CITED: docs.gradle.org].
**Warning signs:** Test output appears after lint failure in CI logs.

### Pitfall 4: Duplicate Gradle Caching

**What goes wrong:** Cache key conflicts or redundant cache uploads.
**Why it happens:** Both `setup-java` `cache: gradle` and `setup-gradle` enabled.
**How to avoid:** Use `setup-gradle` only for caching (D-12).
**Warning signs:** Two cache restore steps in workflow logs.

### Pitfall 5: Accidental Release Task Invocation

**What goes wrong:** CI fails asking for signing credentials.
**Why it happens:** Wrong Gradle tasks (e.g., `build` includes release variants).
**How to avoid:** Invoke only `lintDebug testDebugUnitTest` per D-06; Phase 8 guard only blocks release tasks [VERIFIED: codebase `gradle.taskGraph.whenReady`].
**Warning signs:** `Release signing is not configured` in CI log.

## Code Examples

### Complete Recommended Workflow (Discretion Applied)

```yaml
# Sources: 09-CONTEXT.md, github.com/gradle/actions, github.com/android-actions/setup-android
name: CI

on:
  pull_request:
  push:

concurrency:
  group: ${{ github.workflow }}-${{ github.event.pull_request.number || github.ref }}
  cancel-in-progress: true

jobs:
  lint-and-test:
    name: lint-and-test
    runs-on: ubuntu-latest
    permissions:
      contents: read
    steps:
      - uses: actions/checkout@v6

      - name: Set up JDK 17
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '17'

      - name: Set up Android SDK
        uses: android-actions/setup-android@v4

      - name: Install compileSdk 36 platform
        run: sdkmanager "platforms;android-36"

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v6

      - name: Lint and unit test
        run: ./gradlew lintDebug testDebugUnitTest --no-daemon
```

### Local Verification Commands (Match CI)

```bash
./gradlew lintDebug testDebugUnitTest --no-daemon
```

Phase 12 OSS-01 will document these same commands [VERIFIED: 09-CONTEXT.md].

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `gradle/gradle-build-action` | `gradle/actions/setup-gradle` | v4+ delegation | Use `@v6` [CITED: github.com/gradle/actions] |
| `setup-java` Gradle cache only | Enhanced cache via setup-gradle | v6 default | Free for public repos; optional `cache-provider: basic` for OSS-only path |
| `android-actions/setup-android@v3` | `@v4` (Node 24) | 2024–2025 | Pin `@v4` [CITED: github.com/android-actions/setup-android] |
| kapt for tests | KSP + Robolectric JVM tests | Project v1.0 | CI runs JVM tests only, no emulator |

**Deprecated/outdated:**
- `actions/setup-java` `cache: gradle` when `setup-gradle` is present — redundant.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `sdkmanager "platforms;android-36"` is sufficient without explicit build-tools pin | Standard Stack | Build fails; add `build-tools;36.0.0` |
| A2 | Combined `./gradlew lintDebug testDebugUnitTest` stops before tests when lint fails (no `--parallel`, no `--continue`) | Pitfall 3 | Tests run after lint failure; wastes ~1–2 min |
| A3 | Pre-existing lint errors must be fixed for phase success | Summary | Phase "complete" but CI always red |
| A4 | `enhanced` cache provider (setup-gradle default) acceptable for eventual public repo | Pattern 3 | Use `cache-provider: basic` if maintainer prefers 100% OSS cache |

## Open Questions

1. **Fix lint errors vs. baseline in Phase 9?**
   - What we know: 2 `LocalContextGetResourceValueCall` errors in `SettingsScreen.kt`; infrastructure-only scope prefers YAML-only.
   - What's unclear: Whether maintainer wants lint fixes in this phase or a follow-up.
   - Recommendation: Include one small lint-fix plan task — CI-01 success requires green lint; baselining hides real issues and is not discussed in CONTEXT.

2. **Explicit build-tools version in sdkmanager?**
   - What we know: AGP 9.3.0 typically manages build-tools; amyu docs say prefer letting AGP manage [CITED: github.com/amyu/setup-android].
   - What's unclear: Whether platform-only install suffices on `ubuntu-latest`.
   - Recommendation: Start with `platforms;android-36` only; add `build-tools;36.0.0` if CI fails.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| GitHub Actions | CI execution | ✓ (hosted) | — | None — core deliverable |
| `ubuntu-latest` runner | D-09 | ✓ (hosted) | — | None |
| JDK 17 | Gradle/AGP | ✓ (via setup-java) | Temurin 17 | — |
| Android SDK cmdline-tools | lint/test compile | ✓ (via setup-android) | 20.0 default | Manual sdkmanager |
| Gradle wrapper | Build | ✓ | 9.6.1 | `./gradlew` required |
| Local Java (dev verify) | Maintainer pre-push | ✓ | OpenJDK 21 (local) | CI uses 17 via setup-java |
| `gh` CLI | Optional workflow test | ✓ | 2.96.0 | Push branch to trigger CI |
| Network (Gradle deps) | First CI run | ✓ on GHA | — | Cache on subsequent runs |

**Missing dependencies with no fallback:** none for workflow implementation.

**Missing dependencies with fallback:** none.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 + Robolectric 4.14.1 + kotlinx-coroutines-test |
| Config file | none — standard Android Gradle test setup |
| Quick run command | `./gradlew testDebugUnitTest --no-daemon` |
| Full suite command | `./gradlew lintDebug testDebugUnitTest --no-daemon` |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| CI-01 | lint runs on push/PR | integration (workflow) | Push branch / open PR; verify `lint-and-test` job runs | ❌ Wave 0 — create `ci.yml` |
| CI-01 | unit tests run on push/PR | integration (workflow) | Same workflow run shows `testDebugUnitTest` success | ❌ Wave 0 |
| CI-01 | lint failure fails workflow | integration | Introduce lint error → red check | Manual |
| CI-01 | test failure fails workflow | integration | Break unit test → red check | Manual |
| CI-03 | Gradle cache enabled | integration | Second workflow run faster; cache hit in `setup-gradle` log | Manual |
| CI-03 | Under 10 minutes | integration | Wall-clock on workflow run | Manual |

### Sampling Rate

- **Per task commit:** `./gradlew testDebugUnitTest --no-daemon` (if touching tests)
- **Per wave merge:** `./gradlew lintDebug testDebugUnitTest --no-daemon`
- **Phase gate:** Green GitHub Actions run on branch; full local command passes

### Wave 0 Gaps

- [ ] `.github/workflows/ci.yml` — covers CI-01, CI-03
- [ ] Fix `SettingsScreen.kt` lint errors (or baseline) — required for green CI-01
- [ ] No new unit test files needed — 19 existing test classes cover app logic

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|------------------|
| V2 Authentication | no | — |
| V3 Session Management | no | — |
| V4 Access Control | yes | `permissions: contents: read` only; no write tokens [locked D-14] |
| V5 Input Validation | no | CI does not accept external input |
| V6 Cryptography | no | No secrets in CI workflow; signing deferred to Phase 11 |

### Known Threat Patterns for GitHub Actions Android CI

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Secret exfiltration via `GITHUB_TOKEN` | Information Disclosure | Minimal `contents: read`; no `pull-requests: write` unlike gitleaks |
| Poisoned cache from fork PRs | Tampering | Solo-maintainer repo; optional `cache-read-only` on fork PRs if repo goes public |
| Supply-chain action compromise | Tampering | Pin major versions (`@v4`, `@v6`); Dependabot deferred CI-06 |
| Signing key exposure in CI | Information Disclosure | Do not pass keystore env vars to lint/test job |

## Project Constraints (from .cursor/rules/)

- **GSD workflow enforcement:** Phase work via `/gsd-execute-phase`; this research supports planning only.
- **Platform:** Native Android Kotlin + Jetpack Compose — CI must use Android SDK setup, not plain JVM-only workflow.
- **Offline-first app:** No network services to mock in CI; unit tests use Robolectric/fixtures.
- **Infrastructure-only v1.1:** Prefer workflow YAML-only; lint fixes are exception if needed for green CI.

## Sources

### Primary (HIGH confidence)

- `github.com/android-actions/setup-android` — packages input, JDK 17 example, `@v4` [CITED]
- `github.com/gradle/actions` + `docs/setup-gradle.md` — setup-gradle usage, caching, `@v6` [CITED]
- `docs.gradle.org/current/userguide/github-actions.html` — official Gradle-on-GHA guide [CITED]
- `docs.github.com` — concurrency `cancel-in-progress` syntax [CITED]
- Codebase: `app/build.gradle.kts`, `gitleaks.yml`, `gradle/libs.versions.toml`, `app/src/test/` [VERIFIED]

### Secondary (MEDIUM confidence)

- `github.com/android/camera-samples` workflow — explicit `sdkmanager` platform install pattern [CITED]
- Local Gradle execution 2026-07-27 — timing and lint failure state [VERIFIED: local run]

### Tertiary (LOW confidence)

- Gradle fail-fast task ordering without `--parallel` — standard behavior, not re-verified in blocked shell run [ASSUMED]

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — locked decisions + official action docs + existing gitleaks pattern
- Architecture: HIGH — single-module Android app, clear task boundaries
- Pitfalls: HIGH for lint failure and SDK platform; MEDIUM for fail-fast task ordering

**Research date:** 2026-07-27
**Valid until:** 2026-08-27 (stable Actions ecosystem; re-check if AGP or compileSdk bumps)
