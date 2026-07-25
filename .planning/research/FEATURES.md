# Feature Research

**Domain:** Android release engineering & open-source publishing
**Researched:** 2026-07-25
**Confidence:** MEDIUM (ecosystem patterns well-established; Play Console policy details verified against official Android/Google docs; solo-maintainer OSS norms from community standards)

## Feature Landscape

### Table Stakes (Users Expect These)

Features contributors, testers, and future maintainers assume exist. Missing these = project feels unprofessional or unsafe to open-source.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **CI build + unit tests on every push/PR** | Standard for any public GitHub repo; proves `main` is buildable | MEDIUM | `./gradlew testDebugUnitTest` on JDK 17; matches existing `compileSdk 36` / JVM 17 setup. No `.github/workflows/` exists yet. |
| **CI lint gate** | Android projects expect `lintDebug` in CI; catches resource/i18n issues before merge | LOW | Already have Swedish + English strings; lint catches missing translations and Compose issues. |
| **Secrets excluded from repo** | `.gitignore` for `local.properties`, `*.jks`, `*.keystore`, service account JSON | LOW | Current `.gitignore` covers `local.properties` but not explicit keystore patterns — add before first signing setup. |
| **Automated secrets scan (gitleaks)** | Non-negotiable before making repo public; scans full git history | LOW | `gitleaks/gitleaks-action` on push/PR; zero findings required per SECU-01. Gitleaks is the de-facto standard for git repos. |
| **Manual pre-public security review** | Automated scans miss context (test fixtures, issue bodies, fork history) | LOW | Checklist: no API keys in commits/issues/PRs, no personal emails in git config, no Play Console screenshots with account IDs. Maps to SECU-02. |
| **Release signing via CI secrets** | Signed AAB is mandatory for Play Store; secrets never in source | MEDIUM | Base64-encoded upload keystore + passwords in GitHub Secrets; `signingConfig` reads env vars in `app/build.gradle.kts` (not configured yet). |
| **Signed release AAB build** | Google Play requires AAB (not APK) since 2021 | MEDIUM | `./gradlew bundleRelease`; artifact uploaded to Play or stored as CI artifact. `isMinifyEnabled = false` today — acceptable for v1.1 internal. |
| **Play App Signing enrollment** | Mandatory for all new Play Store apps since Aug 2021 | MEDIUM | One-time Play Console setup: Google-generated app signing key + separate upload key. Cannot automate fully — manual first-time step. |
| **Play Store internal testing track** | Safe first distribution channel; up to 100 testers without public review wait | MEDIUM | `r0adkll/upload-google-play` with `track: internal`; service account with Release Manager role. Requires Play Console app record created first. |
| **versionCode increment per release** | Play Store rejects duplicate `versionCode`; currently `versionCode = 1` | LOW | Bump in `app/build.gradle.kts` before each upload; can automate from git tag in release workflow. |
| **README with build/run instructions** | First thing visitors read; must explain what app does and how to build | LOW | No README exists today. Should cover: prerequisites (JDK 17, Android SDK), `./gradlew assembleDebug`, test command, license. |
| **Open-source LICENSE (MIT)** | Legal clarity for redistribution; GitHub license detection | LOW | PROJECT.md specifies MIT. Add `LICENSE` file at repo root with copyright year + holder. |
| **CONTRIBUTING.md** | Tells contributors how to propose changes, run tests, PR expectations | LOW | Should reference `./gradlew testDebugUnitTest lintDebug`, branch naming, and that CI must pass. |

### Differentiators (Competitive Advantage)

Features that exceed baseline for a solo-maintainer parent utility app. Not required for v1.1 launch, but add credibility and maintainability.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **CODEOWNERS for review routing** | Auto-assigns PR reviews to maintainer; signals ownership | LOW | `.github/CODEOWNERS` with `* @maintainer`. Required per OSS-01. |
| **Separate CI vs release workflows** | PRs get fast feedback; releases only on tag/main with secrets | MEDIUM | `ci.yml` (push/PR) + `release.yml` (tag `v*` or push to `main`). Prevents accidental Play uploads from feature branches. |
| **CI artifact retention (AAB + reports)** | Debuggable failed builds without re-running locally | LOW | `actions/upload-artifact` for test reports, lint HTML, release AAB. 14–30 day retention. |
| **Gradle dependency caching in CI** | Cuts CI time from ~10 min to ~3–5 min on cache hit | LOW | `gradle/actions/setup-gradle@v4` with caching enabled. |
| **Mapping file upload to Play** | Deobfuscates crash reports when R8/ProGuard enabled later | LOW | `mappingFile:` param on upload action. Not needed while `isMinifyEnabled = false`, but wire the path now for future. |
| **Branch protection requiring CI green** | Enforces quality gate before merge | LOW | GitHub repo setting: require `test` + `lint` checks. Manual config, not code. |
| **Dependabot for Gradle/GitHub Actions** | Keeps dependencies and action versions current | LOW | `.github/dependabot.yml` for `gradle` + `github-actions` ecosystems. |
| **SECURITY.md** | Responsible disclosure path for security findings | LOW | Email or GitHub private vulnerability reporting. Good OSS hygiene beyond table stakes. |
| **Issue/PR templates** | Structured bug reports and change descriptions | LOW | `.github/ISSUE_TEMPLATE/`, `pull_request_template.md`. Reduces maintainer triage burden. |
| **TruffleHog APK scan (optional)** | Scans built AAB/APK for embedded secrets beyond git history | MEDIUM | Native APK parsing in TruffleHog v3+. Belt-and-suspenders after gitleaks; useful if build injects secrets into resources. |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good but create problems for this milestone's scope and risk profile.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| **Auto-deploy to production track** | "Ship faster" | Skips internal testing validation; Play review delays; irreversible public exposure | Internal track only (RELE-01); manual promotion to closed/open/production later |
| **Committing keystore or service account JSON** | "Simpler CI setup" | Immediate credential compromise on public repo; Play account takeover risk | GitHub Secrets only; base64 keystore in `KEYSTORE_BASE64` secret |
| **Fastlane full pipeline** | Industry standard for large teams | Heavy Ruby dependency, complex `Fastfile` maintenance for solo dev with one app | `r0adkll/upload-google-play` GitHub Action — sufficient for internal track |
| **Gradle Play Publisher plugin in app** | Native Gradle integration for Play uploads | Adds plugin dependency, couples build to Play credentials, harder to separate CI concerns | GitHub Action upload step post-`bundleRelease` |
| **Instrumented tests in CI (emulator)** | Higher test confidence | 15–30 min CI, flaky emulator, expensive GitHub Actions minutes for offline Room app | Keep JVM unit tests + existing `androidTest` local-only for v1.1; add emulator CI in v2 if needed |
| **Firebase App Distribution** | Alternative test distribution | Extra Google project setup, not Play Store — doesn't validate Play signing/shrinking path | Play internal track is the right first channel for Play-bound app |
| **Making repo public before secrets scan** | "Get feedback early" | Git history is permanent; leaked secrets get scraped within minutes | Run gitleaks locally + CI on private repo first; manual checklist; then flip public |
| **Rewriting entire git history** | "Clean slate" | Breaks forks, loses attribution, often unnecessary if gitleaks passes | Only rewrite if scan finds secrets; otherwise tag v1.0 and go public |
| **Crashlytics / analytics in v1.1** | Production monitoring | Adds SDK, privacy policy requirements, network dependency — contradicts offline-first v1.0 design | Defer to v2; Play Console crash reports sufficient for internal testing |
| **Publishing APK instead of AAB** | Simpler artifact | Google Play requires AAB for new apps; APK bypasses Play's optimized delivery | Always build and upload AAB |
| **Cloud sync / backend for CI** | "Modern DevOps" | Out of scope per PROJECT.md; adds infrastructure cost and privacy surface | Stay 100% offline app; CI is build/test only |

## Feature Dependencies

```
[Play Console app record + Play App Signing]
    └──requires──> [Upload keystore generated locally]
                       └──requires──> [Signing config in build.gradle.kts]

[Signed release AAB in CI]
    └──requires──> [Upload keystore in GitHub Secrets]
    └──requires──> [Signing config in build.gradle.kts]

[Play Store internal deploy (RELE-01)]
    └──requires──> [Signed release AAB in CI]
    └──requires──> [Google Play service account JSON in GitHub Secrets]
    └──requires──> [Play Console app record (manual one-time)]
    └──requires──> [versionCode bump]

[Public GitHub repo (OSS-01)]
    └──requires──> [Gitleaks scan passes (SECU-01)]
    └──requires──> [Manual pre-public review (SECU-02)]
    └──requires──> [README + LICENSE + CONTRIBUTING]

[CI on push/PR (CI-01)]
    └──requires──> [GitHub Actions workflow files]
    └──enhances──> [CONTRIBUTING.md build instructions]

[Release workflow (CI-02)]
    └──requires──> [CI on push/PR (CI-01)] — same Gradle commands, adds signing
    └──requires──> [All secrets scanning gates passed]

[CODEOWNERS]
    └──enhances──> [Public GitHub repo] — review routing once public

[Branch protection]
    └──requires──> [CI on push/PR (CI-01)] — checks must exist first
```

### Dependency Notes

- **Play Store deploy requires manual Play Console bootstrap:** Google requires first-time app creation, Play App Signing enrollment, and often a manual first upload before API-based uploads work reliably. Budget a one-time manual session before automating RELE-01.
- **Signing config is a hard prerequisite for CI-02 and RELE-01:** Current `app/build.gradle.kts` has no `signingConfigs` block. Must add before any release workflow.
- **Secrets scan gates OSS publication:** SECU-01 and SECU-02 must complete before flipping repo to public. Order: scan private repo → fix findings → manual review → add OSS docs → go public.
- **README depends on working CI commands:** Document the exact `./gradlew` commands that CI runs so contributors get identical results.
- **versionCode is a Play Store hard gate:** Each upload needs `versionCode` > previous. Tie to git tag or CI run number to avoid manual forget.
- **Existing v1.0 app code has no CI dependency:** All release engineering is additive infrastructure — no changes to Room schema, ViewModels, or UI required.

## MVP Definition

### Launch With (v1.1)

Minimum for "safe to open-source with automated internal Play testing."

- [ ] **Gitleaks scan — zero findings** (SECU-01) — blocks public release if secrets in history
- [ ] **Manual pre-public review checklist** (SECU-02) — catches what automation misses
- [ ] **CI workflow: lint + unit tests on push/PR** (CI-01) — `./gradlew lintDebug testDebugUnitTest`
- [ ] **Release workflow: signed AAB** (CI-02) — triggered on version tag or `main` merge
- [ ] **Play Store internal track upload** (RELE-01) — automated via service account
- [ ] **OSS docs: README, MIT LICENSE, CONTRIBUTING, CODEOWNERS** (OSS-01)
- [ ] **Keystore + signing config** — upload key in GitHub Secrets, never in repo
- [ ] **`.gitignore` hardening** — `*.jks`, `*.keystore`, `*.pem`, `service-account*.json`

### Add After Validation (v1.1.x)

Features to add once core pipeline is green and first internal release lands.

- [ ] **Branch protection rules** — require CI checks before merge (after CI-01 is stable)
- [ ] **Dependabot** — automated dependency update PRs for Gradle + Actions
- [ ] **SECURITY.md** — vulnerability reporting process
- [ ] **Issue/PR templates** — when first external contributor appears
- [ ] **CHANGELOG.md** — when release cadence exceeds one-off v1.1
- [ ] **TruffleHog APK scan** — if gitleaks passes but paranoia about build-time secret injection

### Future Consideration (v2+)

Defer until product features milestone or production Play release.

- [ ] **Instrumented tests in CI (emulator)** — high cost; local `androidTest` sufficient for now
- [ ] **R8/ProGuard minification** — enable `isMinifyEnabled = true` + mapping upload before production
- [ ] **Staged rollout automation** — `rollout: 0.1` param; relevant for production, not internal
- [ ] **Closed/open testing tracks** — promote from internal after UAT
- [ ] **Fastlane** — only if multi-app or multi-store distribution needed
- [ ] **Firebase Crashlytics** — contradicts offline-first; revisit if production monitoring needed
- [ ] **GitHub Release with APK artifact** — sideload distribution outside Play; not in v1.1 scope

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Gitleaks secrets scan (SECU-01) | HIGH | LOW | P1 |
| Manual security review (SECU-02) | HIGH | LOW | P1 |
| CI lint + unit tests (CI-01) | HIGH | MEDIUM | P1 |
| Signing config + GitHub Secrets | HIGH | MEDIUM | P1 |
| Signed AAB release workflow (CI-02) | HIGH | MEDIUM | P1 |
| Play internal track deploy (RELE-01) | HIGH | MEDIUM | P1 |
| README + LICENSE + CONTRIBUTING (OSS-01) | HIGH | LOW | P1 |
| CODEOWNERS | MEDIUM | LOW | P1 |
| `.gitignore` keystore patterns | HIGH | LOW | P1 |
| Play Console manual setup | HIGH | MEDIUM | P1 |
| Separate CI vs release workflows | MEDIUM | LOW | P2 |
| Gradle caching in CI | MEDIUM | LOW | P2 |
| Branch protection | MEDIUM | LOW | P2 |
| CI artifact upload | MEDIUM | LOW | P2 |
| Dependabot | MEDIUM | LOW | P2 |
| SECURITY.md | LOW | LOW | P2 |
| Issue/PR templates | LOW | LOW | P3 |
| TruffleHog APK scan | LOW | MEDIUM | P3 |
| Emulator instrumented tests in CI | LOW | HIGH | P3 |
| Fastlane pipeline | LOW | HIGH | P3 |

**Priority key:**
- P1: Must have for v1.1 launch
- P2: Should have, add when possible
- P3: Nice to have, future consideration

## Expected Behavior Summary

What "done" looks like for each capability area:

### CI/CD
- Every push and PR runs `lintDebug` + `testDebugUnitTest` on Ubuntu with JDK 17
- PR cannot merge (with branch protection) if CI fails
- Release workflow builds signed AAB only on protected branches/tags
- Build completes in ~5–10 minutes with Gradle cache

### Play Store Internal Testing
- Push to release trigger uploads AAB to internal track automatically
- Testers added in Play Console receive update within minutes (no Play review for internal)
- Each upload has incremented `versionCode`
- Upload key in CI; app signing key managed by Google Play App Signing

### Secrets Scanning
- Gitleaks runs on every push/PR; build fails on any finding
- One-time full-history scan before going public returns zero results
- Manual checklist confirms: no secrets in issues, wiki, PR descriptions, or binary assets

### Open Source
- Repo is public with MIT license
- README explains project purpose, build steps, and contribution path
- CONTRIBUTING.md documents test/lint commands matching CI
- CODEOWNERS routes all PRs to maintainer
- No credentials, personal data, or Play Console account details in repo or history

## Competitor Feature Analysis

Release engineering comparison across typical open-source Android apps (not swim-badge competitors — infrastructure peers).

| Feature | F-Droid app | Typical OSS Android | Simmärken v1.1 Approach |
|---------|-------------|---------------------|-------------------------|
| Distribution | F-Droid repo + APK | GitHub Releases APK/AAB | Play Store internal track (primary); no F-Droid |
| CI | GitLab CI / GitHub Actions | GitHub Actions | GitHub Actions (new) |
| Signing | Reproducible builds | Maintainer keystore in Secrets | Play App Signing + upload key in Secrets |
| Secrets scan | Manual review | Gitleaks in CI | Gitleaks + manual checklist |
| License | GPL common | MIT/Apache common | MIT (per PROJECT.md) |
| Crash reporting | None / self-hosted | Firebase optional | None (offline app; defer) |
| i18n in CI | Lint checks | Lint + missing translation | Already have sv/en; lint catches gaps |

## Sources

- [Android Developers — Sign your app](https://developer.android.com/studio/publish/app-signing) (Play App Signing, upload key) — HIGH confidence
- [Android Developers — Upload your app to Play Console](https://developer.android.com/studio/publish/upload-bundle) (AAB requirement, internal testing) — HIGH confidence
- [Google Play Console Help — Use Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756) — HIGH confidence
- [gitleaks/gitleaks](https://github.com/gitleaks/gitleaks) — secret scanning tool — HIGH confidence
- [trufflesecurity/trufflehog](https://github.com/trufflesecurity/trufflehog) — APK-aware secret scanning — MEDIUM confidence
- [r0adkll/upload-google-play](https://github.com/r0adkll/upload-google-play) — Play Store GitHub Action — MEDIUM confidence
- [GitHub Docs — Licensing a repository](https://docs.github.com/articles/licensing-a-repository) — HIGH confidence
- Community CI/CD guides (SudarshanTechLabs, Kemal Codes) — LOW confidence, cross-checked against official patterns
- PROJECT.md v1.1 requirements (SECU-01/02, CI-01/02, RELE-01, OSS-01) — HIGH confidence (project-specific)
- Existing codebase: `app/build.gradle.kts` (no signing config, `versionCode = 1`), `.gitignore` (no keystore patterns) — HIGH confidence

---
*Feature research for: Android release engineering & open-source publishing (Simmärken v1.1)*
*Researched: 2026-07-25*
