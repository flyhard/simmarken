# Pitfalls Research

**Domain:** Adding release engineering & open-source publishing to an existing offline Android app (Simmärken v1.1)
**Researched:** 2026-07-25
**Confidence:** MEDIUM (HIGH for project-specific gaps from codebase review; MEDIUM for ecosystem patterns cross-checked against official Android/Google/GitHub docs)

## Critical Pitfalls

### Pitfall 1: Making the repository public before scanning full git history

**What goes wrong:**
Secrets committed months ago in `local.properties`, debug keystores, Play service-account JSON, or `.env` files remain reachable via `git log` even after you delete the files from HEAD. Going public exposes the entire history instantly.

**Why it happens:**
Teams scan only the working tree or run `gitleaks detect --no-git` on the current checkout. Private-repo comfort leads to skipping history audits. Shallow CI checkouts (`fetch-depth: 1`) miss older commits.

**How to avoid:**
Run `gitleaks detect --source . --verbose` and `trufflehog git file://.` with `fetch-depth: 0` in CI **before** the visibility flip. Complete SECU-02 manual checklist (commit messages, branch names, issue titles, forked PRs). Add `*.jks`, `*.keystore`, `play-service-account.json`, and `secrets/` to `.gitignore` now—even though Simmärken has no API keys today.

**Warning signs:**
- `.gitignore` lists `/local.properties` but not keystore or Play credential patterns (current state)
- No `.gitleaks.toml` or pre-commit hook configured
- Team says "we never committed secrets" without running a history scan
- Any prior manual Play Console upload from a developer machine (keystore may exist only locally—fine, but document it)

**Phase to address:**
SECU-01 (automated scan) and SECU-02 (manual review)—**must complete before OSS-01**

---

### Pitfall 2: Treating a clean secrets scan as sufficient without PR diff review

**What goes wrong:**
A secret that appeared in a merged or closed PR remains visible in GitHub's cached "Files changed" diff even after `git filter-repo` rewrites history. Scanners pass; the credential is still public.

**Why it happens:**
GitHub stores PR diffs independently of current branch tips. History rewrite does not purge them. Teams close the ticket after `gitleaks` returns zero findings.

**How to avoid:**
During SECU-02, enumerate every PR (open, closed, merged) and search diffs for `password`, `BEGIN PRIVATE KEY`, `AIza`, base64 blobs, and filenames like `*.jks`. If anything was exposed, rotate/revoke first, rewrite history, then file a GitHub Support ticket to scrub cached PR diffs. Install pre-commit `gitleaks protect` after cleanup.

**Warning signs:**
- Closed PRs from early development never reviewed
- Someone force-pushed or used BFG but never checked PR URLs
- Forks exist that may have cloned before cleanup

**Phase to address:**
SECU-02 (manual review), with recovery steps documented before OSS-01

---

### Pitfall 3: Release build silently signed with debug keystore

**What goes wrong:**
`./gradlew bundleRelease` succeeds locally and in CI, but the AAB is debug-signed. Play Console rejects it at upload—or worse, you only discover the mismatch after a long pipeline run.

**Why it happens:**
Simmärken's `app/build.gradle.kts` has no `signingConfigs` block yet. Gradle falls back to the automatic debug signing for release when no release config is wired. This is the #1 cause of "signed with the wrong key" errors when adding CI signing to a project that previously only built debug.

**How to avoid:**
Add an explicit `signingConfigs { create("release") { ... } }` reading from environment variables (empty locally = unsigned or skip release task). In CI, decode keystore from GitHub Secrets immediately before `bundleRelease`. Add a pipeline step: `keytool -printcert -jarfile app/build/outputs/bundle/release/*.aab` and compare SHA-256 to Play Console → App integrity → Upload key certificate.

**Warning signs:**
- `build.gradle.kts` release block has no `signingConfig` assignment (current state)
- CI green but no certificate verification step
- `Owner: CN=Android Debug` in keytool output

**Phase to address:**
CI-02 (release signing)—gate RELE-01 on fingerprint match

---

### Pitfall 4: Upload key mismatch between CI secrets and Play Console

**What goes wrong:**
First Play upload registers an upload certificate. Later, CI uses a different keystore (regenerated, teammate's key, or wrong base64 secret). Every subsequent upload fails with "signed with the wrong key."

**Why it happens:**
Upload keys are created ad hoc on a developer laptop for the first manual upload, then CI generates a **new** keystore. Or base64 encoding corrupts the secret (line breaks, wrong file). Play App Signing makes upload key reset possible, but it is a manual Console process—not automatic.

**How to avoid:**
Generate **one** upload keystore before any Play upload. Store it in a password manager + GitHub Secrets (`KEYSTORE_BASE64`, passwords, alias). Use that same keystore for the first manual internal upload **or** let CI do the first upload—never both with different keys. Document the SHA-256 fingerprint in a private runbook (not the repo).

**Warning signs:**
- "We'll generate the CI keystore later" after someone already uploaded manually
- Multiple `.jks` files on different machines
- Keystore secret re-created because "the old one didn't work"

**Phase to address:**
CI-02 (before first RELE-01 upload)

---

### Pitfall 5: versionCode collision on first automated Play upload

**What goes wrong:**
Play rejects the AAB: "Version code 1 has already been used." Simmärken ships `versionCode = 1` in `app/build.gradle.kts`. If anyone uploaded a draft or internal build with code 1—even if later deleted—the integer is consumed.

**Why it happens:**
versionCode is monotonic and global per app, not per track. Drafts in App Bundle Explorer still reserve the code. CI that increments a local counter without querying Play can also collide after manual uploads.

**How to avoid:**
Before RELE-01 automation, check Play Console → App Bundle Explorer for consumed codes. Bump `versionCode` in `build.gradle.kts` (or inject via CI) to `max(playConsoleMax, local) + 1`. For automation, either commit the bumped code in a release PR or query Play API for the current max before building.

**Warning signs:**
- `versionCode = 1` unchanged since MVP (current state)
- Manual test upload happened during Play Console setup
- CI uses `github.run_number` without checking Play's max

**Phase to address:**
RELE-01 (version bump is part of release workflow, not optional)

---

### Pitfall 6: Open-sourcing code under MIT while bundled assets lack a clear license

**What goes wrong:**
GitHub shows MIT for source code, but contributors/users assume badge WebP images and requirement text extracted from Svensk Simidrott/SLS shops are freely redistributable. A takedown or trademark complaint follows.

**Why it happens:**
`docs/SOURCES.md` documents promotional-use rationale for images but there is no root `LICENSE` file and no separate `NOTICE` or asset license. Code and assets get conflated when the repo goes public.

**How to avoid:**
Ship MIT `LICENSE` for **source code only**. Add `NOTICE` or `docs/ASSETS.md` stating: badge images are official product photos used under promotional/educational terms described in `SOURCES.md`; they are **not** MIT-licensed. Do not claim contributors may freely reuse shop imagery. Consider replacing shop photos with affisch-derived artwork long-term if rights are unclear.

**Warning signs:**
- README says "MIT" without asset carve-out (OSS-01 not started)
- `SOURCES.md` line 118: "No separate LICENSE file at repository root"
- Images committed under `app/src/main/assets/badges/`

**Phase to address:**
OSS-01 (license polish)—must not be an afterthought to SECU

---

### Pitfall 7: Adding CI that cannot run existing Room instrumentation tests

**What goes wrong:**
CI runs `testDebugUnitTest` only; migration or DAO instrumentation tests pass locally in Android Studio but are never executed in CI. A schema change ships without `MigrationTestHelper` coverage.

**Why it happens:**
`app/build.gradle.kts` already configures Room schema export and androidTest assets, but instrumented tests need an emulator or Gradle Managed Device—slower and harder to set up than unit tests. Teams ship a "build-only" workflow and call CI done.

**How to avoid:**
Phase CI-01 in two waves: (1) unit tests + lint on every PR—fast; (2) add `managedVirtualDevices` or `reactivecircus/android-emulator-runner` for `connectedDebugAndroidTest` on main/tags. At minimum, document which tests are CI-gated vs manual. Never delete schema JSON files from `app/schemas/`.

**Warning signs:**
- Workflow only calls `assembleDebug`
- No emulator/GMD step in GitHub Actions
- Schema directory exists but no CI job runs androidTest

**Phase to address:**
CI-01 (define test tiers explicitly in workflow)

---

### Pitfall 8: Play Console service account misconfiguration

**What goes wrong:**
Gradle Play Publisher or Fastlane fails with 403 Forbidden, "application not found," or uploads succeed to the wrong app. Hours lost toggling API settings.

**Why it happens:**
Google Play Developer API not enabled in Cloud Console. Service account invited in Play Console but without release permissions for the specific app. JSON key committed to git (then SECU-01 fails). Project linking between Cloud and Play Console incomplete.

**How to avoid:**
Follow GPP setup order: create service account → enable Play Developer API → invite account in Play Console Users & Permissions with **Release to testing tracks** (not admin) → store JSON only in GitHub Secrets → decode to ephemeral path in CI → run `publishReleaseBundle` with `track = "internal"`. Verify with a dry-run internal upload before wiring tag-triggered production.

**Warning signs:**
- 403 on first `./gradlew publishReleaseBundle`
- Service account has "Admin" when it only needs testing-track release
- `play-service-account.json` anywhere in working tree

**Phase to address:**
RELE-01 (after CI-02 produces a signed AAB)

---

### Pitfall 9: Decoding signing secrets into tracked paths

**What goes wrong:**
CI writes `app/release.keystore` or `play-service-account.json` into the repo directory. A misconfigured workflow uploads artifacts including the keystore, or a developer copies the pattern locally and commits the file.

**Why it happens:**
Tutorials decode secrets to `app/` for simplicity. `.gitignore` does not exclude those paths. `actions/upload-artifact` glob is too broad.

**How to avoid:**
Decode to `$RUNNER_TEMP/` or `./.ci-secrets/` and add that path to `.gitignore`. Pass absolute paths to Gradle via `-P` flags or env vars. Restrict artifact uploads to `app/build/outputs/**/*.aab` only—never `*.jks` or `*.json` credentials.

**Warning signs:**
- Workflow decodes keystore to `app/release.keystore`
- No `.gitignore` entry for CI secret directories
- Artifact step uses `path: app/`

**Phase to address:**
CI-02 and RELE-01

---

### Pitfall 10: Flipping repository visibility before completing the security gate

**What goes wrong:**
Repo goes public for "open source polish" (README, CONTRIBUTING) while SECU-01/02 are still in progress. GitHub Secret Scanning partner alerts arrive too late; search engines index the repo.

**Why it happens:**
OSS-01 tasks feel user-facing and urgent; SECU tasks feel invisible. Visibility is a single GitHub setting; polish is many files.

**How to avoid:**
Treat **public visibility as the last OSS-01 step**, not the first. Use a private repo with all OSS files merged until SECU-01 passes on `main` with full history and SECU-02 checklist is signed off. Consider a short private "dry run" fork for external reviewer.

**Warning signs:**
- README drafted before gitleaks CI job exists
- Issue "make repo public" filed before secrets scan ticket closes
- No explicit "go public" gate in milestone plan

**Phase to address:**
OSS-01 (visibility change is the final sub-step, after SECU-01/02)

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| CI builds debug only, no release job | Fast green PR checks | Release regressions discovered only at tag time | Never past CI-01 merge—add release job in CI-02 |
| Manual Play upload instead of GPP | Ships internal test faster | Drift between CI-signed AAB and what Play receives | Once, for first Console setup only |
| `gitleaks` without `.gitleaks.toml` allowlists | Zero config | Noise from test fixtures; team disables scanner | Until first false positive—then tune, don't disable |
| Skip androidTest in CI | 5–10 min faster builds | Migration bugs reach Play internal testers | Never for schema-changing releases |
| `isMinifyEnabled = false` in release (current) | Simpler first release | Larger AAB, no R8 obfuscation | v1.1 internal track only—revisit before production |
| Store upload keystore only in GitHub Secrets | No local backup | Total loss if secrets deleted without offline backup | Never—keep encrypted offline backup of keystore |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| GitHub Actions ↔ Gradle | No `chmod +x gradlew`; JDK version mismatch (project uses 17) | `actions/setup-java@v4` with `distribution: temurin`, `java-version: 17`; `gradle/actions/setup-gradle@v4` |
| GitHub Actions ↔ Gradle | OOM on `ubuntu-latest` with default 2 GB heap | Set `org.gradle.jvmargs=-Xmx4g` in CI env or `gradle.properties` |
| GitHub Secrets ↔ Signing | Single secret for entire keystore file committed as text | Base64-encode binary keystore; separate secrets for store password, key password, alias |
| Play Console ↔ GPP | Service account created in wrong GCP project | Use the GCP project linked in Play Console → API access |
| Play Console ↔ Signing | Confusing upload key with app signing key for Firebase/OAuth | Simmärken has no Firebase today—document for future; only upload key needed for AAB |
| Gitleaks ↔ TruffleHog | Running only one tool | Gitleaks for fast PR gate; TruffleHog `--only-verified` for pre-public deep audit |
| JSON export ↔ Privacy | Sample export files in repo with real kid names from dogfooding | SECU-02: ensure no real child data in test fixtures, screenshots, or sample JSON |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| No Gradle caching | 8–15 min CI builds | `gradle/actions/setup-gradle@v4`; `org.gradle.caching=true` | Every PR after CI-01 |
| `./gradlew build` in CI | Runs lint + all variants + tests redundantly | `./gradlew testDebugUnitTest lintDebug assembleDebug` for PRs; `bundleRelease` only on release workflow | Immediately when CI added |
| Emulator in every PR | 20+ min queues, flaky boots | Unit tests on PR; instrumented tests on `main` push or nightly | When androidTest count grows |
| Re-downloading Android SDK | Slow cold starts | `android-actions/setup-android@v3` with cached SDK packages | First CI-01 setup |

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Keystore passwords in `gradle.properties` committed to git | Anyone can sign malware as your app | Env vars only; empty signing config when env unset |
| Play service account with Admin + all apps | Compromised CI secret owns entire developer account | Least privilege: one app, testing-track release only |
| Publishing internal-track APK/AAB artifacts publicly in GitHub Releases | Signed app distributed outside Play review channel | Upload AAB to Play internal track only; artifacts in private Actions or Play |
| Real kid names in export JSON test fixtures | GDPR/privacy exposure when OSS | Synthetic fixtures only; SECU-02 reviews `app/src/test` and `androidTest` assets |
| Assuming offline app = no secrets risk | `local.properties` sdk.dir paths leak username; git history may have more | Full-history scan still required |

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Internal track testers not added in Play Console | CI uploads succeed; nobody can install | RELE-01 includes tester list + install link verification |
| versionName stuck at `1.0` while versionCode bumps | Confusing feedback from testers | Bump both in release PR |
| Open-source README with no install path | Contributors clone but can't run on device | README: clone → Android Studio → Run; Play internal link for testers |
| Missing CONTRIBUTING build prerequisites | PRs fail CI for missing JDK/SDK | Document JDK 17, Android SDK 36, `./gradlew testDebugUnitTest` |

## "Looks Done But Isn't" Checklist

- [ ] **SECU-01:** Gitleaks runs on full git history (`fetch-depth: 0`), not just latest commit
- [ ] **SECU-02:** All PR diffs manually searched; not just `main` tree
- [ ] **CI-01:** Workflow fails on test/lint failure (`continue-on-error` not set)
- [ ] **CI-02:** Release AAB certificate fingerprint matches Play Console upload key
- [ ] **CI-02:** Keystore decoded outside tracked paths; not in artifacts
- [ ] **RELE-01:** versionCode strictly greater than any draft/live bundle in Console
- [ ] **RELE-01:** Internal testers can install from Play—not just "upload succeeded"
- [ ] **OSS-01:** MIT LICENSE present with asset licensing called out separately
- [ ] **OSS-01:** `.gitignore` covers `*.jks`, `*.keystore`, `play-service-account.json`, `.ci-secrets/`
- [ ] **OSS-01:** Repo visibility public only after SECU gates pass

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Secret in public git history | HIGH | Rotate secret → `git filter-repo` → force-push → GitHub Support PR diff scrub → re-clone for all devs |
| Wrong upload key uploaded | MEDIUM | Play Console → App integrity → Request upload key reset → register new PEM → update CI secrets |
| versionCode collision | LOW | Increment versionCode, rebuild AAB, re-upload; delete orphaned draft if needed |
| Asset license challenge | HIGH | Remove contested images, replace with affisch-derived or placeholder art, document in SOURCES.md |
| CI keystore lost (secrets deleted) | HIGH | If Play App Signing enabled: upload key reset. If not: cannot update app—new package name |
| Premature public repo | MEDIUM | Make private immediately → run SECU → rotate anything found → re-public when clean |

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| Public before history scan | SECU-01, SECU-02 | `gitleaks detect` + `trufflehog git file://.` exit 0 on `main`; manual PR diff review signed off |
| PR diff cached secrets | SECU-02 | Checklist item per PR URL; Support ticket filed if needed |
| Debug-signed release | CI-02 | `keytool -printcert -jarfile` SHA-256 matches Play upload certificate |
| Upload key mismatch | CI-02 | Same keystore for first upload and all CI builds; fingerprint doc matches |
| versionCode collision | RELE-01 | Play App Bundle Explorer shows new code accepted |
| Asset license gap | OSS-01 | LICENSE + NOTICE/ASSETS.md reviewed; SOURCES.md linked from README |
| CI skips instrumented tests | CI-01 | Workflow runs androidTest on `main` or documents explicit waiver |
| Play service account 403 | RELE-01 | `publishReleaseBundle` to internal track succeeds |
| Secrets decoded to tracked paths | CI-02, RELE-01 | `git status` clean after CI; artifacts contain only AAB |
| Visibility before SECU gate | OSS-01 | Repo stays private until SECU-01/02 tickets closed |

### Recommended phase ordering (pitfall-driven)

```
SECU-01 ──┐
SECU-02 ──┼──► CI-01 ──► CI-02 ──► RELE-01 ──► OSS-01 (public last)
          │
          └── Must complete before OSS-01 visibility change
```

CI-01 can start in parallel with SECU once basic history scan is clean, but **never merge OSS-01 public visibility before SECU-02 sign-off**.

## Sources

- [Android app signing (official)](https://developer.android.com/studio/publish/app-signing) — HIGH confidence
- [Play App Signing (Play Console Help)](https://support.google.com/googleplay/android-developer/answer/9842756) — HIGH confidence
- [Android app versioning](https://developer.android.com/studio/publish/versioning) — HIGH confidence
- [GitHub: Removing sensitive data from a repository](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository) — HIGH confidence
- [Gradle Play Publisher README](https://github.com/Triple-T/gradle-play-publisher) — HIGH confidence
- [Gitleaks documentation](https://github.com/gitleaks/gitleaks) — HIGH confidence
- [TruffleHog documentation](https://github.com/trufflesecurity/trufflehog) — HIGH confidence
- [Room migration testing](https://developer.android.com/training/data-storage/room/migrating-db-versions#test-migrations) — HIGH confidence
- Simmärken codebase review: `app/build.gradle.kts`, `.gitignore`, `docs/SOURCES.md`, `.planning/PROJECT.md` — HIGH confidence (project-specific)
- Community CI guides (GitHub Actions + Android) — LOW confidence, cross-checked against official patterns

---
*Pitfalls research for: Simmärken v1.1 release engineering & open source*
*Researched: 2026-07-25*
