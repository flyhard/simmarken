# Architecture Research

**Domain:** Release engineering & open-source publishing for existing Android app
**Researched:** 2026-07-25
**Confidence:** HIGH (app structure verified in repo); MEDIUM (CI/Play patterns are industry-standard, not yet validated in this project)

## Standard Architecture

### System Overview

Release engineering adds a **build-time control plane** around the frozen v1.0 application. The app runtime architecture (Compose → ViewModel → Repository → Room) is unchanged — no backend, no network, no new runtime modules. All new components live in Git hosting, CI runners, and Play Console.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     GitHub (source of truth)                             │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ gitleaks     │  │ CI workflow  │  │ Release wf   │  │ OSS docs   │ │
│  │ (secrets)    │  │ test+lint    │  │ sign+upload  │  │ README etc │ │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └────────────┘ │
│         │                 │                  │                           │
│         └─────────────────┴──────────────────┘                           │
│                           │                                              │
│              GitHub Secrets (encrypted at rest)                          │
│   KEYSTORE_BASE64 │ KEYSTORE_PASSWORD │ KEY_ALIAS │ KEY_PASSWORD         │
│   PLAY_SERVICE_ACCOUNT_JSON                                              │
├───────────────────────────┴─────────────────────────────────────────────┤
│                     GitHub Actions Runner (ephemeral)                    │
│  checkout → setup-java 17 → setup-gradle → ./gradlew tasks              │
│  [release only] decode keystore → bundleRelease → upload-google-play     │
├─────────────────────────────────────────────────────────────────────────┤
│                     Existing Gradle project (:app)                       │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │ app/build.gradle.kts  ← signingConfigs (NEW), buildTypes (MOD)  │    │
│  │ keystore.properties   ← local only, gitignored (NEW)            │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │ Unchanged app layers: ui/ → domain/ → data/ (Room, export)    │    │
│  └─────────────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────────────┤
│                     Google Play Console (external)                     │
│  Play App Signing (Google-held app key) ← upload key from CI             │
│  Internal testing track ← first automated deployments                    │
└─────────────────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| **gitleaks workflow** | Block secrets in code and git history before/during OSS | `gitleaks/gitleaks-action@v3`, `fetch-depth: 0` |
| **CI workflow** | Fast feedback on every push/PR | `./gradlew testDebugUnitTest lintDebug` |
| **Instrumented test job** | Room migration + DAO tests on emulator | `reactivecircus/android-emulator-runner@v2`, main-only |
| **Release workflow** | Signed AAB + Play upload | Decode keystore → `bundleRelease` → `r0adkll/upload-google-play@v1` |
| **signingConfigs (Gradle)** | Bridge CI secrets and local dev signing | Env vars in CI, `keystore.properties` locally |
| **GitHub Secrets** | Store signing creds and Play service account | Repository secrets, never in repo |
| **Play Console** | App listing, tracks, Play App Signing enrollment | Manual one-time setup |
| **OSS docs** | Contributor and user onboarding | `README.md`, `LICENSE`, `CONTRIBUTING.md`, `CODEOWNERS` |

### Baseline Application Architecture (unchanged)

The v1.0 app remains a single-module (`:app`) Kotlin/Compose/Room MVVM stack. Release engineering does **not** introduce new source packages, DI frameworks, or network layers. The only Gradle-module touchpoint is `app/build.gradle.kts` signing and optional version automation.

## Recommended Project Structure

```
simmmärken/
├── .github/
│   └── workflows/
│       ├── gitleaks.yml          # NEW — secrets scan (all branches, full history)
│       ├── ci.yml                # NEW — unit tests + lint on push/PR
│       └── release.yml           # NEW — signed AAB + Play internal track (main/tags)
├── .gitleaks.toml                # NEW (optional) — allowlist for false positives
├── app/
│   ├── build.gradle.kts          # MODIFIED — signingConfigs, release signingConfig
│   └── src/                      # UNCHANGED — application source
├── docs/
│   ├── SECURITY-CHECKLIST.md     # NEW — manual pre-public review (SECU-02)
│   └── extraction/pdfs/        # ALREADY gitignored — keep out of OSS
├── keystore.properties           # NEW, gitignored — local signing only
├── .gitignore                    # MODIFIED — add *.jks, keystore.properties
├── README.md                     # NEW — project overview, build instructions
├── LICENSE                       # NEW — MIT
├── CONTRIBUTING.md               # NEW — contribution guidelines
└── CODEOWNERS                    # NEW — default reviewers
```

### Structure Rationale

- **`.github/workflows/`:** Keeps CI concerns out of app source; three workflows separate security gate, fast feedback, and privileged release (secrets access).
- **`keystore.properties` at repo root:** Android convention; parallel to `local.properties` (SDK path). Both gitignored.
- **No `fastlane/` directory:** For a solo-maintainer single-module app, `r0adkll/upload-google-play` in a workflow is simpler than Fastlane. Gradle owns signing; GitHub Actions owns orchestration.
- **Security checklist in `docs/`:** Manual review artifact separate from automated gitleaks; documents human checks (PDF paths, personal data, Play Console draft state).

## Architectural Patterns

### Pattern 1: Conditional Release Signing (CI-safe, local-friendly)

**What:** Create `signingConfigs.release` only when credentials exist; debug builds never require a keystore.
**When to use:** Any project where contributors clone without signing keys.
**Trade-offs:** `assembleRelease` without keys produces unsigned AAB (useful for CI dry-runs); signed release requires secrets or local `keystore.properties`.

**Example (integrates with existing `app/build.gradle.kts`):**

```kotlin
// app/build.gradle.kts — add after defaultConfig block
import java.util.Properties

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(keystorePropertiesFile.inputStream())
    }
}

fun signingProp(name: String): String? =
    System.getenv(name) ?: keystoreProperties.getProperty(name)?.takeIf { it.isNotBlank() }

android {
    // ... existing compileSdk, defaultConfig, etc.

    signingConfigs {
        val storeFilePath = signingProp("KEYSTORE_FILE")
        val storePassword = signingProp("KEYSTORE_PASSWORD")
        val keyAlias = signingProp("KEY_ALIAS")
        val keyPassword = signingProp("KEY_PASSWORD")

        if (storeFilePath != null && storePassword != null && keyAlias != null && keyPassword != null) {
            create("release") {
                storeFile = file(storeFilePath)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // existing; enable later with R8 testing
            signingConfigs.findByName("release")?.let { signingConfig = it }
            proguardFiles(/* existing */)
        }
    }
}
```

### Pattern 2: Tiered CI Jobs (fast gate vs slow emulator)

**What:** Run JVM unit tests on every push/PR; run `connectedCheck` (androidTest) only on `main` or release branches.
**When to use:** Projects with both Robolectric unit tests and Room instrumented tests (this repo has ~15 unit + ~6 androidTest files).
**Trade-offs:** PRs may merge without emulator coverage; main branch catches migration/DAO regressions.

**Example job split:**

```yaml
# ci.yml — always runs (~2-4 min)
- run: ./gradlew testDebugUnitTest lintDebug --no-daemon

# ci.yml — instrumented, main only (~8-15 min)
- if: github.ref == 'refs/heads/main'
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 30
    script: ./gradlew connectedDebugAndroidTest --no-daemon
```

### Pattern 3: Secrets-Only-on-Protected-Branch Release

**What:** Release workflow triggers on `push` to `main` or `workflow_dispatch`; never on `pull_request` from forks (no secret exposure).
**When to use:** All signing and Play upload workflows.
**Trade-offs:** PR authors cannot test signed builds in CI; they rely on unit tests + manual local `bundleRelease` if they have keys.

## Data Flow

### CI Pipeline Flow (push/PR)

```
git push / pull_request
    ↓
[gitleaks] checkout (fetch-depth: 0) → scan full history → fail on findings
    ↓ (parallel)
[ci] checkout → setup-java 17 → setup-gradle (cache)
    ↓
chmod +x gradlew
    ↓
./gradlew testDebugUnitTest lintDebug
    ↓
[main only] android-emulator-runner → connectedDebugAndroidTest
    ↓
pass/fail status on PR
```

### Release Pipeline Flow (main / tag)

```
push to main (or manual dispatch)
    ↓
ci job must pass (workflow dependency or re-run tests)
    ↓
decode KEYSTORE_BASE64 → /tmp/release.keystore (ephemeral runner disk)
    ↓
env: KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
    ↓
./gradlew bundleRelease --no-daemon
    ↓
artifact: app/build/outputs/bundle/release/app-release.aab
    ↓
r0adkll/upload-google-play@v1
  serviceAccountJsonPlainText: PLAY_SERVICE_ACCOUNT_JSON
  packageName: se.simmarken
  releaseFiles: app-release.aab
  tracks: internal
  status: completed
    ↓
delete keystore file (cleanup step)
    ↓
Internal testers receive build in Play Console
```

### State Management

Release engineering introduces **no runtime state** in the app. Version state lives in:

| State | Location | Updated by |
|-------|----------|------------|
| `versionCode` / `versionName` | `app/build.gradle.kts` `defaultConfig` | Manual or CI bump on release |
| Signing keys | GitHub Secrets + Play App Signing | One-time generation; rotate on leak |
| Play track assignment | Play Console + upload action `tracks` input | Release workflow |
| Git history cleanliness | gitleaks + optional `git filter-repo` | Pre-OSS one-time scrub |

### Key Data Flows

1. **Secrets scan → OSS gate:** Full-history gitleaks must pass with zero findings before repository goes public. Findings in history require rotation + history rewrite, not just deletion in latest commit.
2. **Upload key → Play App Signing:** CI signs AAB with upload key; Google re-signs with app signing key for distribution. Upload key loss is recoverable via Play Console reset; app signing key loss is not.
3. **Service account → Play API:** JSON key in `PLAY_SERVICE_ACCOUNT_JSON` secret grants `androidpublisher` scope. Principle of least privilege: create dedicated service account with Release Manager role only.

## New vs Modified Integration Points

### New Components

| Component | Path / Secret | Depends on |
|-----------|---------------|------------|
| Gitleaks workflow | `.github/workflows/gitleaks.yml` | — |
| CI workflow | `.github/workflows/ci.yml` | Gradle wrapper, JDK 17 |
| Release workflow | `.github/workflows/release.yml` | CI passing, all signing secrets |
| Gitleaks allowlist | `.gitleaks.toml` (optional) | Known false positives only |
| Security checklist | `docs/SECURITY-CHECKLIST.md` | Manual review |
| Upload keystore | `KEYSTORE_BASE64` secret | Generated locally via `keytool` |
| Play service account | `PLAY_SERVICE_ACCOUNT_JSON` secret | Play Console API access |
| OSS documentation | `README.md`, `LICENSE`, etc. | — |

### Modified Components

| Component | Change | Risk if skipped |
|-----------|--------|-----------------|
| `app/build.gradle.kts` | Add conditional `signingConfigs.release` | CI produces unsigned AAB; Play rejects |
| `.gitignore` | Add `*.jks`, `keystore.properties`, `release.keystore` | Accidental keystore commit |
| `defaultConfig.versionCode` | Bump strategy for Play (monotonic integer) | Play rejects duplicate versionCode |
| `gradle.properties` (optional) | `org.gradle.daemon=false` for CI | Runner memory pressure |

### Unchanged Components

| Component | Why unchanged |
|-----------|---------------|
| `app/src/**` (UI, domain, data) | No runtime behavior change for v1.1 |
| `settings.gradle.kts` | Single-module project sufficient |
| `gradle/libs.versions.toml` | No new runtime dependencies |
| Room schema / migrations | Tested in CI, not modified |
| `AndroidManifest.xml` | No new permissions for CI/CD |

## Suggested Phase Build Order

Dependencies dictate this ordering for roadmap phases:

```
Phase A: Git hygiene & secrets scan
    ├── Harden .gitignore (*.jks, keystore.properties)
    ├── Add gitleaks workflow (fetch-depth: 0)
    ├── Run gitleaks locally; fix/rotate any findings
    └── Complete docs/SECURITY-CHECKLIST.md manual review
         ↓ (gate: zero gitleaks findings)
Phase B: Gradle signing config
    ├── Generate upload keystore (local, never committed)
    ├── Add conditional signingConfigs to app/build.gradle.kts
    └── Verify local bundleRelease produces signed AAB
         ↓
Phase C: CI build & test
    ├── Add ci.yml (testDebugUnitTest, lintDebug)
    ├── Add instrumented job on main (optional but recommended — Room migrations)
    └── Store signing secrets in GitHub (prep for Phase D)
         ↓
Phase D: Play Console setup (manual, parallel with C)
    ├── Create Play app listing (se.simmarken)
    ├── Enroll Play App Signing
    ├── Create service account + JSON key
    └── First manual internal upload (unblocks API uploads)
         ↓
Phase E: Release pipeline
    ├── Add release.yml (sign + upload-google-play)
    └── Verify automated internal track deployment
         ↓
Phase F: Open-source publish (OSS-01)
    ├── README, LICENSE (MIT), CONTRIBUTING, CODEOWNERS
    └── Make repository public (only after Phase A gate passes)
```

**Rationale:**
- **A before F:** Public repo without history scan risks exposing past secrets.
- **B before E:** Release workflow needs Gradle signing hook; debugging signing locally is faster than in CI.
- **D before E:** Play API returns "Precondition check failed" until at least one build exists on a testing track.
- **C parallel with B/D:** CI does not require Play; unit tests validate app while signing/Play setup proceeds.

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| Solo maintainer (now) | 3 workflows, no Fastlane, manual version bumps |
| 2-5 contributors | Add PR template, branch protection requiring CI + gitleaks |
| Frequent releases | Automate `versionCode` bump in release workflow from git tag |
| Flaky emulator CI | Cache AVD snapshot or move androidTest to nightly schedule |

### Scaling Priorities

1. **First bottleneck:** Emulator instrumented tests (~10 min). Mitigate: run only on `main`, cache AVD, or defer to Phase C as optional job.
2. **Second bottleneck:** Play upload API quota / draft-state errors. Mitigate: ensure listing complete, use `internal` track, `status: completed` only after validation.

## Anti-Patterns

### Anti-Pattern 1: Committing Keystore or `keystore.properties`

**What people do:** Add signing files to repo for "convenience" or store in `local.properties`.
**Why it's wrong:** Public OSS instantly exposes upload key; attackers can publish malicious updates.
**Do this instead:** Gitignore `*.jks` and `keystore.properties`; use GitHub Secrets with base64 decode in release workflow only.

### Anti-Pattern 2: Signing on Pull Request Workflows

**What people do:** Run release/signing job on `pull_request` events.
**Why it's wrong:** Fork PRs can exfiltrate secrets via malicious workflow edits (unless using environments with approval gates).
**Do this instead:** Sign and upload only on `push` to protected `main` or `workflow_dispatch` from maintainers.

### Anti-Pattern 3: Gitleaks with `fetch-depth: 1`

**What people do:** Default shallow checkout to save CI time.
**Why it's wrong:** Secrets in older commits remain in public history undetected.
**Do this instead:** `fetch-depth: 0` for gitleaks job; accept the one-time full clone cost.

### Anti-Pattern 4: Fastlane for a Single-Module Solo App

**What people do:** Add Fastlane + Ruby toolchain for one `upload_to_play_store` call.
**Why it's wrong:** Extra dependency surface, Gemfile maintenance, duplicates Gradle signing.
**Do this instead:** `r0adkll/upload-google-play` action directly in `release.yml`.

### Anti-Pattern 5: Skipping First Manual Play Upload

**What people do:** Automate API upload on day one without Play Console setup.
**Why it's wrong:** API returns precondition errors; debugging in CI is painful.
**Do this instead:** Complete store listing, enroll Play App Signing, upload one build manually to internal track, then enable automation.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| **GitHub Actions** | Workflow YAML in `.github/workflows/` | JDK 17, `gradle/actions/setup-gradle@v4`, ubuntu-latest |
| **GitHub Secrets** | Encrypted env injection into workflows | 5 secrets minimum for full pipeline |
| **Google Play Console** | Play Developer API via service account | Package `se.simmarken`; start on `internal` track |
| **Google Play App Signing** | Enroll at first upload; Google holds app signing key | CI uses upload key only |
| **Gitleaks** | `gitleaks/gitleaks-action@v3` | Personal repo: no license needed |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| Workflow ↔ Gradle | Shell env vars → `signingConfigs` | Same contract for local (`keystore.properties`) and CI (env) |
| CI ↔ Release job | `needs: [ci]` or branch protection | Release never runs on failing tests |
| App code ↔ CI | Zero coupling | No `BuildConfig` CI flags needed for v1.1 |
| OSS docs ↔ Build | README documents `./gradlew` commands | Debug build requires no secrets |

### GitHub Secrets Inventory

| Secret | Used by | Purpose |
|--------|---------|---------|
| `KEYSTORE_BASE64` | release.yml | Upload keystore binary |
| `KEYSTORE_PASSWORD` | release.yml | Keystore password |
| `KEY_ALIAS` | release.yml | Key alias |
| `KEY_PASSWORD` | release.yml | Key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | release.yml | Play Developer API auth |
| `GITHUB_TOKEN` | gitleaks.yml | PR review comments (auto-provided) |

## Sources

- [Android CI/CD with GitHub Actions](https://pcsalt.com/android/ci-cd-android-github-actions/) — signing via env vars
- [Empathy.co Android CI/CD guide](https://engineering.empathy.co/android-ci-cd-with-github-actions/) — Gradle-centric signing (preferred over action-only signing)
- [r0adkll/upload-google-play](https://github.com/r0adkll/upload-google-play) — Play upload action, `tracks` API
- [gitleaks/gitleaks-action](https://github.com/gitleaks/gitleaks-action) — secrets scanning workflow
- [reactivecircus/android-emulator-runner](https://github.com/ReactiveCircus/android-emulator-runner) — instrumented tests on CI
- Project files: `app/build.gradle.kts`, `.gitignore`, `PROJECT.md` (v1.1 requirements)
- Existing v1.0 architecture: `.planning/research/ARCHITECTURE.md` (2026-07-22, superseded for app layers; runtime architecture unchanged)

---
*Architecture research for: Simmärken v1.1 Release & Open Source*
*Researched: 2026-07-25*
