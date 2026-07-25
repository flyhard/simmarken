# Project Research Summary

**Project:** Simmärken v1.1 — Release Engineering & Open Source
**Domain:** Android release engineering & open-source publishing (additive infrastructure around frozen v1.0 app)
**Researched:** 2026-07-25
**Confidence:** MEDIUM

## Executive Summary

Simmärken v1.1 is not a product feature milestone — it is a **release engineering and publishing milestone** that wraps an existing offline Kotlin/Compose/Room app with CI/CD, Play Store distribution, and open-source hygiene. The v1.0 application architecture (single `:app` module, MVVM, no network) stays unchanged. All new work lives in GitHub Actions workflows, Gradle signing configuration, Play Console setup, and OSS documentation.

Experts build this kind of milestone as a **layered control plane**: security gates first (full-history secrets scan before any public visibility), then Gradle signing that works locally and in CI, then fast CI feedback (unit tests + lint on every PR), then privileged release automation (signed AAB → Play internal track), and finally OSS publication with public visibility as the **last** step. The recommended stack is GitHub Actions on `ubuntu-latest`, `gradle/actions/setup-gradle@v6`, Gitleaks `@v3` for SECU-01, conditional `signingConfigs` in `app/build.gradle.kts`, and **Gradle Play Publisher 4.0.0** (`publishBundle`) for RELE-01 — keeping deploy logic in Gradle rather than adding Fastlane or Ruby.

The dominant risks are security and sequencing mistakes, not technical complexity. Going public before scanning full git history, signing release builds with the debug keystore (current state — no `signingConfigs` block exists), upload key mismatch between manual Play setup and CI secrets, and `versionCode = 1` collision after any prior Play upload are all high-probability failures. Asset licensing is a separate OSS risk: MIT covers source code only; badge images under `app/src/main/assets/badges/` need explicit carve-out in LICENSE/NOTICE per `docs/SOURCES.md`. Mitigation is strict phase ordering: SECU-01/02 → signing → CI → Play Console bootstrap → automated release → OSS docs → **public visibility last**.

## Key Findings

### Recommended Stack

GitHub Actions is the clear CI/CD choice for a solo OSS Android project — zero infra, Android SDK preinstalled on `ubuntu-latest`, JDK 17 matches the existing project. Three separate workflows keep concerns isolated: `secrets.yml` (gitleaks on all pushes/PRs with `fetch-depth: 0`), `ci.yml` (unit tests + lint), and `release.yml` (sign + publish on tags or manual dispatch). Pin `actions/checkout@v6`, `actions/setup-java@v5`, `gradle/actions/setup-gradle@v6` (use `cache-provider: basic` for MIT-only caching on public OSS), and `gitleaks/gitleaks-action@v3` (v2 breaks Sep 2026).

**Core technologies:**
- **GitHub Actions** (`ubuntu-latest`) — CI/CD orchestration; matches CI-01/CI-02/RELE-01 with no self-hosted runner burden
- **Gradle Play Publisher 4.0.0** — `publishBundle` to Play internal track; native Gradle integration, no Fastlane/Ruby
- **Gitleaks `@v3` + CLI 8.30.1** — fast full-history secrets scan; satisfies SECU-01
- **Conditional `signingConfigs` in Gradle** — CI reads env vars from GitHub Secrets; local dev uses gitignored `keystore.properties`
- **Play App Signing** — Google holds app signing key; CI uses upload key only (resettable if lost)
- **Dependabot** (P2) — weekly Gradle + GitHub Actions version PRs once CI is green

**Avoid:** Fastlane, committing keystores/service account JSON, `gitleaks-action@v2`, APK uploads (Play requires AAB), emulator tests in v1.1 CI (defer instrumented tests to main-only or v2).

### Expected Features

v1.1 "done" means safe to open-source with automated internal Play testing — not production release or F-Droid distribution.

**Must have (table stakes):**
- Gitleaks scan — zero findings on full git history (SECU-01)
- Manual pre-public security review checklist (SECU-02)
- CI: `lintDebug` + `testDebugUnitTest` on every push/PR (CI-01)
- Release signing via GitHub Secrets + signed AAB build (CI-02)
- Play Store internal track upload via service account (RELE-01)
- OSS docs: README, MIT LICENSE, CONTRIBUTING, CODEOWNERS (OSS-01)
- `.gitignore` hardening for `*.jks`, `*.keystore`, service account JSON
- Play Console manual bootstrap (app record, Play App Signing, first upload)

**Should have (differentiators):**
- Separate CI vs release workflows — fast PR feedback, privileged release only on tags/main
- Gradle caching in CI — cuts build time from ~10 min to ~3–5 min
- Branch protection requiring CI green — enforce quality gate after CI stabilizes
- SECURITY.md, Dependabot, CI artifact retention — credibility and maintainability

**Defer (v2+):**
- Instrumented tests in CI (emulator) — 15–30 min, flaky; local androidTest sufficient for v1.1
- R8/ProGuard minification — `isMinifyEnabled = false` acceptable for internal track
- TruffleHog APK scan, Fastlane, Firebase Crashlytics, production track automation
- GitHub Releases with APK sideload distribution

### Architecture Approach

Release engineering adds a **build-time control plane** around the frozen app — no new runtime modules, no backend, no network layer. Three GitHub Actions workflows (gitleaks, CI, release) interact with GitHub Secrets and an ephemeral runner that decodes credentials to temp paths, runs Gradle, and cleans up. The only Gradle touchpoint is `app/build.gradle.kts` (conditional `signingConfigs.release`). OSS docs live at repo root; security checklist in `docs/SECURITY-CHECKLIST.md`.

**Major components:**
1. **Gitleaks workflow** — blocks secrets in code and full git history; gate before public visibility
2. **CI workflow** — fast feedback via `./gradlew testDebugUnitTest lintDebug` on every push/PR
3. **Release workflow** — decode keystore → `bundleRelease` → GPP `publishBundle` to internal track
4. **Conditional signing (Gradle)** — same contract for CI (env vars) and local dev (`keystore.properties`)
5. **OSS documentation** — README, LICENSE (with asset carve-out), CONTRIBUTING, CODEOWNERS

### Critical Pitfalls

1. **Public before full-history scan** — secrets in old commits are instantly exposed; use `fetch-depth: 0` in gitleaks, complete SECU-02 before OSS-01 visibility flip
2. **Debug-signed release builds** — no `signingConfigs` block today means Gradle falls back to debug signing; add explicit release config and verify certificate fingerprint with `keytool`
3. **Upload key mismatch** — generate one keystore before any Play upload; use same key for manual first upload and all CI builds; document SHA-256 fingerprint privately
4. **versionCode collision** — `versionCode = 1` is consumed after any Play upload; bump before RELE-01 automation
5. **Asset licensing gap** — MIT for source only; badge images are not MIT-licensed; add NOTICE/ASSETS.md carve-out referencing `docs/SOURCES.md`

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Security & Git Hygiene (SECU-01, SECU-02)
**Rationale:** Public visibility is irreversible for git history; must gate everything else. Current `.gitignore` lacks keystore patterns.
**Delivers:** Hardened `.gitignore`, gitleaks workflow (`fetch-depth: 0`), local + CI scan passing with zero findings, `docs/SECURITY-CHECKLIST.md` completed (including PR diff review)
**Addresses:** Automated secrets scan, manual pre-public review, keystore gitignore patterns
**Avoids:** Pitfall 1 (public before history scan), Pitfall 2 (PR cached diffs), Pitfall 10 (visibility before SECU gate)

### Phase 2: Gradle Signing Configuration
**Rationale:** Hard prerequisite for CI-02 and RELE-01; easier to debug signing locally than in CI. Current `app/build.gradle.kts` has no `signingConfigs`.
**Delivers:** Conditional `signingConfigs.release` reading env vars / `keystore.properties`, upload keystore generated locally (never committed), local `bundleRelease` produces correctly signed AAB
**Addresses:** Release signing via CI secrets, signed release AAB build
**Avoids:** Pitfall 3 (debug-signed release), Pitfall 4 (upload key mismatch — generate key here before Play upload)

### Phase 3: CI Build & Test (CI-01)
**Rationale:** Fast feedback loop; does not require Play Console. Can run in parallel with Phase 4 once basic history scan is clean.
**Delivers:** `ci.yml` with JDK 17, `setup-gradle@v6` caching, `./gradlew testDebugUnitTest lintDebug`, optional main-only instrumented test job (document tier explicitly)
**Addresses:** CI build + unit tests, CI lint gate, Gradle caching
**Avoids:** Pitfall 7 (CI that skips Room instrumentation tests — at minimum document test tiers)

### Phase 4: Play Console Setup (manual, parallel with Phase 3)
**Rationale:** Play API returns precondition errors until app exists with at least one build on a testing track. Cannot fully automate first-time setup.
**Delivers:** Play Developer account, app listing (`se.simmarken`), Play App Signing enrolled, GCP service account with Release Manager role, first manual internal upload (or CI first upload with pre-registered key), GitHub Secrets populated
**Addresses:** Play App Signing enrollment, Play Store internal testing track, service account auth
**Avoids:** Pitfall 4 (upload key mismatch), Pitfall 5 (versionCode collision — check consumed codes), Pitfall 8 (service account 403)

### Phase 5: Release Pipeline (CI-02, RELE-01)
**Rationale:** Depends on signing config (Phase 2), Play bootstrap (Phase 4), and CI green (Phase 3).
**Delivers:** `release.yml` triggered on `v*` tags or `workflow_dispatch`, keystore decoded to `$RUNNER_TEMP/`, GPP `publishBundle` to internal track, certificate verification step, `versionCode` bump strategy, cleanup of secrets on runner
**Uses:** GPP 4.0.0, GitHub Secrets (5 minimum), conditional signing from Phase 2
**Avoids:** Pitfall 3 (certificate verification), Pitfall 5 (versionCode), Pitfall 9 (secrets decoded to tracked paths)

### Phase 6: Open Source Publish (OSS-01)
**Rationale:** Public visibility is the **final** step — only after SECU-01/02 pass on `main` with full history.
**Delivers:** README (build/run instructions matching CI commands), MIT LICENSE with asset licensing carve-out (NOTICE or `docs/ASSETS.md`), CONTRIBUTING.md, CODEOWNERS, repository made public
**Addresses:** README, LICENSE, CONTRIBUTING, CODEOWNERS
**Avoids:** Pitfall 6 (asset license gap), Pitfall 10 (visibility before SECU gate)

### Phase Ordering Rationale

- **Security before everything public:** SECU phases are hard gates; OSS visibility change is the last sub-step of Phase 6, not the first
- **Signing before release automation:** Debugging `signingConfigs` locally is faster than iterating in CI
- **Play Console before API automation:** First manual upload unblocks GPP; API cannot register new apps
- **CI parallel with Play setup:** Unit tests validate app while manual Play Console work proceeds; no dependency between them
- **P2 features after core pipeline green:** Branch protection, Dependabot, SECURITY.md, artifact retention — add once CI-01 is stable

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 4 (Play Console Setup):** Manual steps with policy nuances; service account permissions and first-upload sequencing need step-by-step runbook during `/gsd-plan-phase --research-phase 4`
- **Phase 5 (Release Pipeline):** GPP 4.0.0 integration with existing AGP 9.3.0 / Gradle 9.6.1 — verify `compileSdk 36` availability on `ubuntu-latest` runner; may need `sdkmanager` step
- **Phase 6 (OSS-01):** Asset licensing requires legal/policy review of `docs/SOURCES.md` promotional-use claims — may need human decision on NOTICE wording

Phases with standard patterns (skip research-phase):
- **Phase 1 (Security):** Gitleaks workflow is well-documented; `.gitignore` patterns are boilerplate
- **Phase 2 (Signing):** Conditional `signingConfigs` is Android-documented standard pattern
- **Phase 3 (CI):** GitHub Actions + Gradle unit test CI is established; `gradle/actions/setup-gradle` docs are comprehensive

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | MEDIUM | Official tool versions verified (GPP 4.0.0, gitleaks v3, setup-gradle v6); ecosystem guidance cross-checked from multiple sources |
| Features | MEDIUM | Play Console policy from official Android/Google docs; solo-maintainer OSS norms from community standards; PROJECT.md requirements are authoritative |
| Architecture | HIGH (app) / MEDIUM (CI) | App structure verified in repo; CI/Play patterns are industry-standard but not yet validated in this project |
| Pitfalls | MEDIUM | HIGH for project-specific gaps (no signing config, versionCode=1, asset licensing); MEDIUM for ecosystem patterns |

**Overall confidence:** MEDIUM

### Gaps to Address

- **GPP vs `upload-google-play` action:** STACK recommends GPP 4.0.0 as primary; FEATURES/ARCHITECTURE mention `r0adkll/upload-google-play` as alternative. Resolve during Phase 5 planning — prefer GPP per STACK research unless Gradle plugin coupling is undesirable
- **`compileSdk 36` on GitHub runners:** May need explicit `sdkmanager "platforms;android-36"` step; validate in first CI run
- **Instrumented test CI tier:** Decide during Phase 3 planning — main-only emulator job vs documented manual-only waiver for Room migration tests
- **Asset licensing wording:** Human review needed for NOTICE/ASSETS.md before public launch; promotional-use rationale in SOURCES.md is not a legal license
- **versionCode automation:** Decide manual bump in release PR vs CI injection from git tag during Phase 5 planning

## Sources

### Primary (HIGH confidence)
- [Gradle Play Publisher releases](https://github.com/Triple-T/gradle-play-publisher/releases) — v4.0.0
- [gitleaks-action v3.0.0](https://github.com/gitleaks/gitleaks-action/releases/tag/v3.0.0) — Node 24 migration
- [gradle/actions releases](https://github.com/gradle/actions/releases) — setup-gradle v6.2.0
- [Android Developers — Sign your app](https://developer.android.com/studio/publish/app-signing) — Play App Signing, upload key
- [Android Developers — Upload your app to Play Console](https://developer.android.com/studio/publish/upload-bundle) — AAB requirement
- [GitHub Docs — Removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository) — history cleanup
- Project files: `app/build.gradle.kts`, `.gitignore`, `docs/SOURCES.md`, `PROJECT.md`

### Secondary (MEDIUM confidence)
- [r0adkll/upload-google-play](https://github.com/r0adkll/upload-google-play) — fallback Play upload action
- [reactivecircus/android-emulator-runner](https://github.com/ReactiveCircus/android-emulator-runner) — deferred instrumented CI
- Community CI/CD guides (SudarshanTechLabs, Kemal Codes, Empathy.co) — cross-checked against official patterns

### Tertiary (LOW confidence)
- Solo-maintainer OSS norms from community standards — needs validation against PROJECT.md requirements only

---
*Research completed: 2026-07-25*
*Ready for roadmap: yes*
