# Stack Research

**Domain:** Android release engineering & open-source publishing (v1.1 milestone)
**Researched:** 2026-07-25
**Confidence:** MEDIUM (official tool releases verified; ecosystem guidance cross-checked from multiple community sources)

## Recommended Stack

### Core Technologies

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| GitHub Actions | `ubuntu-latest` runner | CI/CD orchestration | Zero infra for a solo OSS project; Android SDK preinstalled on hosted runners (`ANDROID_SDK_ROOT`). Matches CI-01/CI-02/RELE-01 requirements. |
| `actions/checkout` | `@v6` | Clone repo in workflows | Node 24–compatible; pairs with gitleaks-action v3. Use `fetch-depth: 0` in secrets workflow for full-history scan. |
| `actions/setup-java` | `@v5` (pin `v5.6.0` for supply-chain hygiene) | Provision JDK on runner | Project compiles with Java 17 (`jvmTarget = "17"`). Temurin is the de-facto distribution for Android CI. |
| `gradle/actions/setup-gradle` | `@v6` (pin `v6.2.0`) | Gradle cache, wrapper validation, job summary | Official Gradle-maintained action; validates `gradle-wrapper.jar` checksum automatically. Use `cache-provider: basic` for 100% MIT-licensed caching on a public OSS repo. |
| Android Gradle Plugin (existing) | `9.3.0` | Build AAB/APK | Already in `libs.versions.toml`; no change needed. Release signing config is added in `app/build.gradle.kts`. |
| Gradle Wrapper (existing) | `9.6.1` | Reproducible builds | Already pinned; `setup-gradle` uses `gradle-version: wrapper` by default. |
| Play App Signing (Google Play Console) | Platform feature | App signing key custody | Google holds the app-signing key; CI only needs an **upload key**. Upload key is resettable if lost. Required for Play Store distribution. |
| Gradle Play Publisher (GPP) | `4.0.0` (`com.github.triplet.play`) | Upload signed AAB to Play internal track | Native Gradle plugin — no Ruby/fastlane toolchain. `./gradlew publishBundle` uploads AAB + can manage release notes later. Default track is `internal`, matching RELE-01. |
| `gitleaks/gitleaks-action` | `@v3` | Secrets scanning in CI | Fast, offline regex scan; official GitHub Action. v3 uses Node 24 (v2 breaks Sep 2026). Satisfies SECU-01. |
| Gitleaks CLI (pinned via action env) | `8.30.1` | Scanner engine | Latest stable CLI (Mar 2026). Pin with `GITLEAKS_VERSION: 8.30.1` in workflow env for reproducibility. |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `gradle/actions/dependency-submission` | `@v6` | Submit Gradle dependency graph to GitHub | Optional post-build step; enables Dependabot alerts on transitive deps. Add after CI is green. |
| `actions/upload-artifact` | `@v4` | Store signed AAB between jobs | When build and deploy are separate jobs, or for manual inspection before Play upload. |
| `r0adkll/upload-google-play` | `@v1` (`v1.1.5`) | Play upload via GitHub Action (no Gradle plugin) | **Fallback only** if you want deploy logic entirely in YAML instead of GPP. Not recommended as primary — duplicates what GPP does in Gradle. |
| TruffleHog (`trufflesecurity/trufflehog`) | Pin to release tag | Verified-secret deep scan | **Defer.** Gitleaks covers SECU-01. Add later as a scheduled workflow with `--only-verified` if you want live-credential confirmation. |
| `reactivecircus/android-emulator-runner` | `@v2` | Run `connectedAndroidTest` on emulator | **Defer.** v1.1 CI should run unit tests (`testDebugUnitTest`) only. Instrumented tests need emulator (~10 min extra). Add when CI-01 explicitly requires device tests. |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| `keytool` (JDK) | Generate upload keystore locally | One-time: `keytool -genkey -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload`. Never commit the file. |
| Google Play Console | Register app, enable Play App Signing, create internal testing track | First AAB **must** be uploaded manually once — Play Developer API cannot register a new app. After that, GPP handles subsequent uploads. |
| Google Cloud service account + Play Console API access | Authenticate GPP / Play uploads | Create JSON key in GCP; grant "Release manager" (or admin) on Play Console → Users and permissions. Store JSON as `PLAY_STORE_CREDENTIALS` GitHub secret. |
| GitHub Encrypted Secrets | Store keystore, passwords, service account JSON | Required secrets listed below. Base64-encode keystore: `base64 -i upload-keystore.jks \| tr -p '\n'`. |
| GitHub Push Protection + Secret Scanning | Platform-level secret blocking | Enable in repo/org settings before going public. Complements gitleaks; not a substitute. Free for public repos. |
| Dependabot (`dependabot.yml`) | Automated Gradle/GitHub Actions version PRs | Add `.github/dependabot.yml` for `gradle` and `github-actions` ecosystems. |

## Integration with Existing Project

### Gradle changes (`app/build.gradle.kts`)

Add release signing that reads CI secrets via environment variables (works locally with `~/.gradle/gradle.properties` overrides):

```kotlin
// plugins block — add:
id("com.github.triplet.play") version "4.0.0"

android {
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_FILE")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false  // keep as-is until R8 rules are ready
        }
    }
}

play {
    val credsFile = System.getenv("PLAY_STORE_CREDENTIALS")
    if (credsFile != null) {
        serviceAccountCredentials.set(file(credsFile))
    }
    track.set("internal")
}
```

### Version catalog (`gradle/libs.versions.toml`)

```toml
[versions]
playPublisher = "4.0.0"

[plugins]
play-publisher = { id = "com.github.triplet.play", version.ref = "playPublisher" }
```

### `.gitignore` additions (before first push to public GitHub)

```
# Signing & Play credentials — never commit
*.jks
*.keystore
upload-keystore.jks
play-credentials.json
**/play-service-account.json

# Local Android SDK path (already partially covered)
local.properties
```

### GitHub Secrets (repository)

| Secret | Contents |
|--------|----------|
| `KEYSTORE_BASE64` | Base64-encoded upload keystore (no newlines) |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias (e.g. `upload`) |
| `KEY_PASSWORD` | Key password (often same as keystore password) |
| `PLAY_STORE_CREDENTIALS` | Full JSON service account key (written to temp file in CI) |

`GITLEAKS_LICENSE` is only required for **organization** repos on gitleaks-action; skip for personal accounts.

## Installation

### 1. Gradle Play Publisher plugin

In `app/build.gradle.kts` plugins block:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    // ... existing plugins ...
    alias(libs.plugins.play.publisher)  // after adding to libs.versions.toml
}
```

Or inline:

```kotlin
id("com.github.triplet.play") version "4.0.0"
```

### 2. GitHub Actions workflows (`.github/workflows/`)

Three workflows — keep concerns separated:

**`secrets.yml`** — runs on all pushes and PRs; blocks merge if secrets found:

```yaml
name: Secrets Scan
on: [push, pull_request]
jobs:
  gitleaks:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v6
        with:
          fetch-depth: 0
      - uses: gitleaks/gitleaks-action@v3
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          GITLEAKS_VERSION: 8.30.1
```

**`ci.yml`** — build + unit tests on every push/PR:

```yaml
name: CI
on: [push, pull_request]
concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true
jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    steps:
      - uses: actions/checkout@v6
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 17
      - uses: gradle/actions/setup-gradle@v6
        with:
          cache-provider: basic
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}
      - run: ./gradlew testDebugUnitTest lint --no-daemon --stacktrace
```

**`release.yml`** — sign AAB and publish to Play internal track (trigger on version tags or manual dispatch):

```yaml
name: Release
on:
  workflow_dispatch:
  push:
    tags: ['v*']
jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v6
      - uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: 17
      - uses: gradle/actions/setup-gradle@v6
        with:
          cache-provider: basic
      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_BASE64 }}" | tr -d '\n\r' | base64 --decode > upload-keystore.jks
      - name: Write Play credentials
        run: echo '${{ secrets.PLAY_STORE_CREDENTIALS }}' > play-credentials.json
      - name: Build & publish
        env:
          KEYSTORE_FILE: ${{ github.workspace }}/upload-keystore.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          PLAY_STORE_CREDENTIALS: ${{ github.workspace }}/play-credentials.json
        run: ./gradlew publishBundle --no-daemon
      - name: Cleanup secrets
        if: always()
        run: rm -f upload-keystore.jks play-credentials.json
```

### 3. Dependabot (optional, recommended for OSS)

`.github/dependabot.yml`:

```yaml
version: 2
updates:
  - package-ecosystem: gradle
    directory: /
    schedule:
      interval: weekly
  - package-ecosystem: github-actions
    directory: /
    schedule:
      interval: weekly
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| Gradle Play Publisher `4.0.0` | `r0adkll/upload-google-play@v1` | If you want zero Gradle plugin changes and all deploy logic in YAML. Trade-off: two tools (Gradle sign + Action upload) instead of one `publishBundle` task. |
| Gradle Play Publisher `4.0.0` | fastlane (`supply`) | Multi-platform teams already on fastlane, or need iOS parity later. Adds Ruby/Bundler maintenance — overkill for single-module Kotlin app. |
| Gitleaks `@v3` | TruffleHog | When you need **live credential verification** (`--only-verified`) for incident response or compliance audits. Slower; better as scheduled supplement, not primary gate. |
| Gitleaks `@v3` | GitHub Secret Scanning alone | Never sufficient alone for pre-public audit — only scans default branch going forward; won't catch secrets in git history. Use both. |
| Native Gradle `signingConfigs` | `r0adkll/sign-android-release@v1` | Third-party sign action adds indirection; Gradle signingConfigs is the Android-documented pattern and keeps signing in the build graph. |
| `gradle/actions/setup-gradle@v6` (`cache-provider: basic`) | `cache-provider: enhanced` (default) | Enhanced uses proprietary `gradle-actions-caching` (Gradle ToS). Basic is MIT-only — appropriate for OSS where transparency matters. |
| `gradle/actions/setup-gradle@v6` | Manual `actions/cache` on `~/.gradle` | Manual cache keys are fragile and miss wrapper validation. setup-gradle is strictly better. |
| Unit tests only in CI | `android-emulator-runner` for instrumented tests | When Room migration tests or UI tests become release blockers. Project has 15+ instrumented tests today — acceptable to defer for v1.1 CI speed. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| fastlane + Ruby/Bundler | Extra toolchain for a Gradle-native single-module app; no iOS target | GPP `publishBundle` |
| Committing keystore or service account JSON | Irreversible secret exposure once public | GitHub Encrypted Secrets + `.gitignore` patterns |
| `gitleaks/gitleaks-action@v2` | Node 20 deprecated; stops working Sep 2026 | `@v3` |
| `gradle/gradle-build-action` | Deprecated; replaced by `gradle/actions/setup-gradle` | `gradle/actions/setup-gradle@v6` |
| `actions/setup-java@v4` | Superseded by v5 (Node 24, signature verification) | `@v5` |
| TruffleHog as sole scanner for v1.1 | Slower, network-dependent verification; SECU-01 needs fast PR gate | Gitleaks primary; TruffleHog optional later |
| APK release to Play Store | Play requires AAB for new apps | `bundleRelease` / `publishBundle` |
| `android-actions/setup-android` on hosted runners | SDK already on `ubuntu-latest`; action can conflict with runner layout | Rely on preinstalled SDK; add only if a specific API level is missing |
| Self-hosted runners | Unnecessary ops burden for solo OSS project | `ubuntu-latest` |
| Firebase App Distribution | Out of scope (Play internal track is the target) | Play internal testing via GPP |
| Storing secrets in `gradle.properties` committed to repo | Will be caught by gitleaks, but prevention > detection | CI env vars + local `~/.gradle/gradle.properties` (gitignored) |
| `@main` / floating tags on third-party actions | Supply-chain risk; breaking changes without notice | Pin major version tag or commit SHA |

## Stack Patterns by Variant

**If publishing to Play internal track only (v1.1 default):**
- GPP `track.set("internal")` — no production exposure
- Manual first upload in Play Console, then automate with `publishBundle`
- Closed testing can reuse same pipeline with `track.set("alpha")` later

**If CI should gate on instrumented tests (future):**
- Add `reactivecircus/android-emulator-runner@v2` job running `./gradlew connectedDebugAndroidTest`
- Use API 35/36 emulator matching `targetSdk`/`compileSdk`
- Expect ~15–20 min workflow; run on `main` only, not every PR

**If repo stays private during initial CI setup:**
- All tools work identically; gitleaks license not needed for personal accounts
- Enable GitHub Push Protection before switching to public

**If avoiding GPP Gradle plugin:**
- Use `r0adkll/upload-google-play@v1` in `release.yml` after `./gradlew bundleRelease`
- Still need Gradle `signingConfigs` for signing; only the upload step moves to YAML

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| AGP `9.3.0` | Gradle `9.6.1` | Already validated in project. GPP 4.0.0 supports current AGP. |
| GPP `4.0.0` | Gradle `8.x`–`9.x` | Released Jan 2026; targets current Android Gradle Plugin ecosystem. |
| Kotlin `2.2.10` | AGP `9.3.0` | No CI tooling changes needed. |
| Java `17` | AGP `9.3.0`, `setup-java@v5` | Do not bump to Java 21 in CI without updating `jvmTarget` in project. |
| `setup-gradle@v6` | Gradle Wrapper `9.6.1` | Default `gradle-version: wrapper` uses project's wrapper. |
| `gitleaks-action@v3` | `actions/checkout@v6`, runner `>= v2.327.1` | Node 24 runtime requirement. |
| Play Developer API v3 | GPP `4.0.0`, `upload-google-play@v1` | Both use same underlying API. |
| `compileSdk 36` / `targetSdk 35` | `ubuntu-latest` runner image | GitHub runner images include recent SDK platforms; if `compileSdk 36` missing, add `sdkmanager "platforms;android-36"` step. |

## Required Manual Steps (Not Automatable)

1. Create Google Play Developer account ($25 one-time)
2. Create app listing in Play Console (`se.simmarken`)
3. Enable Play App Signing; generate/download upload key
4. Create GCP project → enable Google Play Android Developer API → create service account → link in Play Console
5. Upload first AAB manually to internal track (GPP cannot register new apps)
6. Add GitHub secrets
7. Run gitleaks scan + manual SECU-02 checklist before `git push` to public remote

## Sources

- [Gradle Play Publisher releases](https://github.com/Triple-T/gradle-play-publisher/releases) — v4.0.0 verified (HIGH)
- [Gradle Plugin Portal: com.github.triplet.play](https://plugins.gradle.org/plugin/com.github.triplet.play) — v4.0.0 (HIGH)
- [gitleaks-action v3.0.0 release](https://github.com/gitleaks/gitleaks-action/releases/tag/v3.0.0) — Node 24 migration (HIGH)
- [gitleaks v8.30.1 release](https://github.com/gitleaks/gitleaks/releases/tag/v8.30.1) — CLI version (HIGH)
- [gradle/actions releases](https://github.com/gradle/actions/releases) — setup-gradle v6.2.0 (HIGH)
- [actions/setup-java releases](https://github.com/actions/setup-java/releases) — v5.6.0 (HIGH)
- [Gradle GitHub Actions user guide](https://docs.gradle.org/current/userguide/github-actions.html) — setup-gradle patterns (HIGH)
- [actions/runner-images install-android-sdk.sh](https://github.com/actions/runner-images/blob/main/images/ubuntu/scripts/build/install-android-sdk.sh) — SDK preinstalled on ubuntu-latest (HIGH)
- [r0adkll/upload-google-play releases](https://github.com/r0adkll/upload-google-play/releases) — v1.1.5 fallback reference (HIGH)
- Project files: `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `.gitignore` (HIGH)

---
*Stack research for: Simmärken v1.1 release engineering & open source*
*Researched: 2026-07-25*
