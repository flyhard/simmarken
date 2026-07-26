# Phase 8: Gradle Signing Configuration - Research

**Researched:** 2026-07-26
**Domain:** Android Gradle Plugin release signing (Kotlin DSL), keystore bootstrap, CI env-var credentials
**Confidence:** HIGH

## Summary

Phase 8 wires conditional release signing into the existing `app/build.gradle.kts` so `bundleRelease` produces a Play-ready signed AAB using the upload keystore — never the debug keystore, and never silently unsigned. The project already runs AGP 9.3.0 / Gradle 9.6.1 with a release `buildType` but no `signingConfigs` block; `.gitignore` already blocks `*.jks` and `keystore.properties` per SECU-03.

AGP's default behavior is helpful but insufficient alone: **debug** builds auto-sign with the debug keystore; **release** builds are **unsigned** unless a `signingConfig` is explicitly assigned [CITED: developer.android.com/build/build-variants]. That means the primary risk is not accidental debug signing — it is producing an **unsigned** release AAB that looks successful until Play upload. The locked D-01 fail-hard requirement therefore needs an explicit `gradle.taskGraph.whenReady` (or equivalent task guard) that throws a clear `GradleException` when `bundleRelease` / `assembleRelease` runs without complete credentials [CITED: github.com/aisleron/aisleron/pull/129].

Credential resolution follows a two-path model locked in CONTEXT: local maintainers copy `keystore.properties.example` → gitignored `keystore.properties` pointing at repo-root `upload.jks`; CI supplies `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` with Gradle decoding base64 to an ephemeral file under `app/build/` (discretion area — CI-side decode is also valid if the env-var contract is honored). A committed `scripts/generate-upload-keystore.sh` bootstraps the keystore with Google-default parameters (RSA 2048, alias `upload`, 10 000-day validity) and captures SHA-256 to gitignored `keystore-fingerprint.md` for Phase 10 Play Console registration.

**Primary recommendation:** Inline signing resolution in `app/build.gradle.kts` with guarded `keystore.properties` loading, Gradle-side base64 decode for CI, `signingConfigs.create("release")` only when credentials are complete, and a `whenReady` fail-hard guard — plus committed bootstrap script, example properties file, and fingerprint doc template.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Missing-Keystore Behavior
- **D-01:** Fail hard on release — `./gradlew bundleRelease` (and `assembleRelease`) must error with a clear message when signing credentials are absent; no silent debug-keystore fallback.
- **D-02:** Debug builds unaffected — `assembleDebug`, `testDebugUnitTest`, and other debug tasks work without any signing configuration.
- **D-03:** Actionable error message — Release signing failure must point maintainers to `keystore.properties.example` and keystore creation docs/script.
- **D-04:** Same rule in CI — Phase 11 release jobs must have signing secrets configured; no unsigned release fallback when secrets are absent.

#### Keystore Bootstrap
- **D-05:** Committed generation script — `scripts/generate-upload-keystore.sh` runs `keytool` with documented parameters; maintainer executes once locally.
- **D-06:** Google-default key parameters — RSA 2048, 10 000-day validity, alias `upload` (script defaults; override only if documented).
- **D-07:** Keystore at repo root — Generated `upload.jks` lives at project root (already covered by Phase 7 `.gitignore` `*.jks` pattern).
- **D-08:** Interactive password prompts — Generation script prompts for store/key passwords interactively; never echoes or writes passwords to disk.

#### Local Credential Layout
- **D-09:** `keystore.properties` at repo root — Standard Android convention; gitignored per Phase 7 SECU-03.
- **D-10:** Committed example template — `keystore.properties.example` with placeholder values committed to repo.
- **D-11:** Standard four properties — `storeFile`, `storePassword`, `keyAlias`, `keyPassword` in `keystore.properties`.
- **D-12:** Parallel CI env vars — When `keystore.properties` is absent, Gradle reads `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` from environment (CI decodes base64 keystore to ephemeral path).

#### Private Fingerprint Documentation
- **D-13:** Gitignored local fingerprint file — `keystore-fingerprint.md` (or equivalent) at repo root, added to `.gitignore`; never committed.
- **D-14:** Fingerprint + metadata — Document contains SHA-256 fingerprint, key alias, creation date, and `keytool` command to re-verify.
- **D-15:** Script captures fingerprint — Keystore generation script prints and writes SHA-256 to the gitignored fingerprint file.
- **D-16:** Capture in Phase 8, register in Phase 10 — Fingerprint documented during keystore creation; Play Console enrollment is Phase 10's responsibility.

### Claude's Discretion
- Exact Gradle `signingConfigs` block structure and env-var decode logic in `app/build.gradle.kts`.
- Whether to add a small `signing.gradle.kts` include vs inline in `app/build.gradle.kts`.
- `keystore-fingerprint.md` exact filename and `.gitignore` entry placement.
- Base64 decode location (Gradle vs CI workflow step) — as long as D-12 env var contract is honored.
- Error message wording and link targets within repo docs.

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CI-02 (supported) | Release workflow builds signed release AAB using GitHub Secrets | D-12 env-var contract (`ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`); Gradle decode pattern; no secrets in source |
| RELE-01 (supported) | Upload keystore generated and Play App Signing enrolled | `scripts/generate-upload-keystore.sh` with RSA 2048 / 10 000-day / alias `upload`; SHA-256 capture in `keystore-fingerprint.md` for Phase 10 registration |
| SECU-03 (dependency) | `.gitignore` hardened for keystores and signing files | Existing `*.jks`, `keystore.properties` patterns verified; extend for `keystore-fingerprint.md` |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Release AAB signing | Build system (Gradle / AGP) | Local JDK `keytool` | Signing is a compile-time build concern; AGP `signingConfigs` owns APK/AAB signatures |
| Credential storage (local) | Developer filesystem (gitignored) | — | `keystore.properties` + `upload.jks` never enter git per SECU-03 |
| Credential storage (CI) | GitHub Secrets → runner env | Gradle decode | Secrets injected as env vars; binary keystore decoded ephemerally on runner |
| Keystore generation | Shell script (`scripts/`) | JDK `keytool` | One-time maintainer bootstrap; not app runtime code |
| SHA-256 fingerprint doc | Gitignored markdown at repo root | — | Private maintainer artifact for Phase 10 Play Console |
| Fail-hard enforcement | Gradle configuration (`whenReady` guard) | — | Must intercept `bundleRelease` / `assembleRelease` before unsigned artifacts ship |
| Debug build signing | AGP default debug keystore | — | Unchanged; no project signing config needed (D-02) |

## Standard Stack

### Core

| Library / Tool | Version | Purpose | Why Standard |
|----------------|---------|---------|--------------|
| Android Gradle Plugin | 9.3.0 (project-pinned) | `signingConfigs`, `bundleRelease` | Already in `gradle/libs.versions.toml`; official signing DSL [VERIFIED: codebase] |
| Gradle | 9.6.1 (wrapper) | Build orchestration, `whenReady` guard | Project wrapper; supports Kotlin DSL signing blocks [VERIFIED: codebase] |
| JDK `keytool` | Bundled with JDK 17+ | Keystore generation, SHA-256 extraction | Official Android docs recommend `keytool` for upload keys [CITED: developer.android.com/build/building-cmdline] |
| JDK `jarsigner` | Bundled with JDK | Verify AAB signature post-build | AAB is a signed JAR; `jarsigner -verify` validates release output [CITED: developer.android.com/build/building-cmdline] |

### Supporting

| Tool | Version | Purpose | When to Use |
|------|---------|---------|-------------|
| `apksigner` (Android SDK Build Tools) | SDK-managed | APK signature verification | If verifying APK instead of AAB; optional for this phase |
| `keytool -printcert -jarfile` | JDK | Extract cert fingerprint from signed AAB | Cross-check signed AAB fingerprint against `keystore-fingerprint.md` |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Inline `app/build.gradle.kts` signing | `app/signing.gradle.kts` applied via `apply(from=...)` | Separate file cleaner at scale; inline preferred for solo-maintainer single-module project |
| Gradle base64 decode | CI workflow decode to `$RUNNER_TEMP/upload.jks` | CI decode keeps Gradle simpler but splits credential contract across two files; Gradle decode honors D-12 env vars directly |
| `jarsigner -verify` | `bundletool` validate | jarsigner sufficient for signature check; bundletool adds Play-specific validation (Phase 11) |

**Installation:** No new packages. Uses existing AGP + JDK tooling.

**Version verification:**
```
agp = "9.3.0"          # gradle/libs.versions.toml [VERIFIED: codebase]
Gradle 9.6.1           # gradle/wrapper/gradle-wrapper.properties [VERIFIED: codebase]
keytool at /usr/bin/keytool [VERIFIED: local environment probe]
```

## Package Legitimacy Audit

> No new external packages are installed in this phase. Signing uses AGP built-in DSL and JDK tooling only.

| Package | Registry | Verdict | Disposition |
|---------|----------|---------|-------------|
| *(none)* | — | N/A | No package installs |

**Packages removed due to SLOP verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        Maintainer (one-time)                            │
│  scripts/generate-upload-keystore.sh                                    │
│    → keytool -genkeypair → upload.jks (repo root, gitignored)           │
│    → keytool -list -v    → keystore-fingerprint.md (gitignored)         │
│    → maintainer copies keystore.properties.example → keystore.properties│
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     app/build.gradle.kts (signing resolver)             │
│                                                                         │
│  ┌──────────────────┐    ┌──────────────────────────────────────────┐   │
│  │ keystore.properties│   │ CI env vars (Phase 11 workflow)          │   │
│  │ (local, gitignored)│   │ ANDROID_KEYSTORE_BASE64                  │   │
│  │ storeFile=upload.jks│  │ KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD│  │
│  └────────┬─────────┘    └──────────────────┬───────────────────────┘   │
│           │                                  │                          │
│           │         ┌────────────────────────┘                          │
│           ▼         ▼                                                   │
│     ┌─────────────────────────┐                                         │
│     │ resolveReleaseSigning() │                                         │
│     │  local path OR decode   │                                         │
│     │  base64 → app/build/... │                                         │
│     └───────────┬─────────────┘                                         │
│                 ▼                                                       │
│     ┌─────────────────────────┐     ┌──────────────────────────────┐    │
│     │ signingConfigs.release  │────▶│ buildTypes.release           │    │
│     │ (only if complete creds)│     │ signingConfig = release      │    │
│     └─────────────────────────┘     └──────────────┬───────────────┘    │
│                                                     │                   │
│     ┌───────────────────────────────────────────────┘                   │
│     │ gradle.taskGraph.whenReady                                          │
│     │  IF bundleRelease|assembleRelease AND no signing → GradleException  │
│     └───────────────────────────────────────────────────────────────────│
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
         ./gradlew assembleDebug          ./gradlew bundleRelease
         (no signing config needed)       (signed AAB or fail-hard)
                    │                               │
                    ▼                               ▼
           debug APK (debug key)        app/build/outputs/bundle/release/
                                        app-release.aab (upload key)
```

### Recommended Project Structure

```
simmmärken/
├── app/
│   └── build.gradle.kts          # signingConfigs + credential resolver + whenReady guard
├── scripts/
│   └── generate-upload-keystore.sh   # NEW: keytool bootstrap + fingerprint capture
├── keystore.properties.example       # NEW: committed template
├── keystore.properties               # gitignored — local credentials
├── upload.jks                        # gitignored — upload keystore
├── keystore-fingerprint.md           # gitignored — SHA-256 + metadata
└── .gitignore                        # extend for keystore-fingerprint.md
```

### Pattern 1: Guarded Credential Resolution (Local + CI)

**What:** Load signing credentials only when complete; never parse partial `keystore.properties` at configuration time.
**When to use:** Always — prevents debug sync failures when `keystore.properties` is absent or placeholder [CITED: github.com/aisleron/aisleron/pull/129].

**Example:**
```kotlin
// Source: developer.android.com/build/build-variants + community fail-hard pattern
import java.util.Base64
import java.util.Properties

data class ReleaseSigningCredentials(
    val storeFile: java.io.File,
    val storePassword: String,
    val keyAlias: String,
    val keyPassword: String,
)

fun Project.resolveReleaseSigningCredentials(): ReleaseSigningCredentials? {
    val propsFile = rootProject.file("keystore.properties")
    if (propsFile.exists()) {
        val props = Properties().apply { propsFile.inputStream().use { load(it) } }
        val storeFilePath = props.getProperty("storeFile") ?: return null
        val storeFile = rootProject.file(storeFilePath)
        val storePassword = props.getProperty("storePassword")
        val keyAlias = props.getProperty("keyAlias")
        val keyPassword = props.getProperty("keyPassword")
        if (storeFile.exists() && !storePassword.isNullOrBlank() &&
            !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()
        ) {
            return ReleaseSigningCredentials(storeFile, storePassword, keyAlias, keyPassword)
        }
        return null // partial/placeholder file — do not throw at config time (D-02)
    }

    val base64 = System.getenv("ANDROID_KEYSTORE_BASE64")?.trim().orEmpty()
    val storePassword = System.getenv("KEYSTORE_PASSWORD")
    val keyAlias = System.getenv("KEY_ALIAS")
    val keyPassword = System.getenv("KEY_PASSWORD")
    if (base64.isNotEmpty() && !storePassword.isNullOrBlank() &&
        !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()
    ) {
        val decoded = layout.buildDirectory.file("signing/ci-upload.jks").get().asFile
        decoded.parentFile.mkdirs()
        decoded.writeBytes(Base64.getDecoder().decode(base64))
        return ReleaseSigningCredentials(decoded, storePassword, keyAlias, keyPassword)
    }
    return null
}
```

### Pattern 2: Conditional signingConfigs + Fail-Hard Guard

**What:** Create `signingConfigs.release` only when credentials resolve; guard release tasks via `whenReady`.
**When to use:** Enforces D-01/D-03/D-04 without breaking debug builds (D-02).

**Example:**
```kotlin
// Inside android {} block in app/build.gradle.kts
val releaseSigning = resolveReleaseSigningCredentials()

signingConfigs {
    if (releaseSigning != null) {
        create("release") {
            storeFile = releaseSigning.storeFile
            storePassword = releaseSigning.storePassword
            keyAlias = releaseSigning.keyAlias
            keyPassword = releaseSigning.keyPassword
        }
    }
}

buildTypes {
    release {
        isMinifyEnabled = false
        signingConfig = signingConfigs.findByName("release") // nullable — no debug fallback
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro",
        )
    }
}

gradle.taskGraph.whenReady {
    val isReleaseBuild = allTasks.any { task ->
        val n = task.name
        n == "bundleRelease" || n == "assembleRelease" ||
            n.endsWith("BundleRelease") || n.endsWith("AssembleRelease")
    }
    if (isReleaseBuild) {
        val cfg = android.signingConfigs.findByName("release")
        val store = cfg?.storeFile
        if (cfg == null || store == null || !store.exists()) {
            throw GradleException(
                """
                Release signing is not configured.
                Local: copy keystore.properties.example → keystore.properties and run scripts/generate-upload-keystore.sh
                CI: set ANDROID_KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
                See keystore.properties.example for details.
                """.trimIndent()
            )
        }
    }
}
```

### Pattern 3: Keystore Bootstrap Script

**What:** Committed shell script wrapping `keytool` with locked defaults and fingerprint capture.
**When to use:** First-time maintainer setup (D-05–D-08, D-15).

**Example:**
```bash
#!/usr/bin/env bash
# Source: developer.android.com/build/building-cmdline
set -euo pipefail
KEYSTORE="upload.jks"
ALIAS="upload"
FINGERPRINT_FILE="keystore-fingerprint.md"

if [[ -f "$KEYSTORE" ]]; then
  echo "ERROR: $KEYSTORE already exists. Refusing to overwrite." >&2
  exit 1
fi

keytool -genkeypair -v \
  -keystore "$KEYSTORE" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

SHA256=$(keytool -list -v -keystore "$KEYSTORE" -alias "$ALIAS" 2>/dev/null \
  | awk -F': ' '/SHA256:/{print $2; exit}')

cat > "$FINGERPRINT_FILE" <<EOF
# Upload Keystore Fingerprint (PRIVATE — gitignored)

- **Created:** $(date -u +"%Y-%m-%d")
- **Keystore:** $KEYSTORE
- **Alias:** $ALIAS
- **SHA-256:** $SHA256

## Re-verify

\`\`\`bash
keytool -list -v -keystore $KEYSTORE -alias $ALIAS
\`\`\`
EOF

echo "SHA-256: $SHA256"
echo "Fingerprint written to $FINGERPRINT_FILE (gitignored)"
```

### Anti-Patterns to Avoid

- **Unconditional `keystore.properties` load:** Breaks `./gradlew assembleDebug` and Android Studio sync when file is absent [CITED: github.com/flutter/website/issues/11995].
- **Reading `storeFile` from empty placeholder:** `file(null)` crashes configuration phase for all variants [CITED: github.com/fundacja-reborn/reapps/commit/b9b0b10].
- **Hardcoding passwords in `build.gradle.kts`:** Violates SECU-03 and official Android guidance [CITED: developer.android.com/build/build-variants].
- **Assuming unsigned release = failure:** AGP completes `bundleRelease` unsigned by default; explicit guard required (D-01).
- **Committing `upload.jks` or `keystore-fingerprint.md`:** Gitleaks/SECU-03 violation; extend `.gitignore` for fingerprint file.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Keystore / key pair generation | Custom crypto or openssl wrappers | JDK `keytool -genkeypair` | Google-documented defaults; PKCS12/JKS compatibility with AGP |
| AAB/APK signing | Manual `jarsigner` in build pipeline | AGP `signingConfigs` + `bundleRelease` | AGP integrates v1/v2/v3 signing, bundles, and caching |
| SHA-256 fingerprint extraction | Parsing cert binaries manually | `keytool -list -v` / `keytool -printcert -jarfile` | Standard tooling; Play Console expects this format |
| Secret scanning for keystores | Custom regex | Phase 7 `.gitignore` + Gitleaks | Already enforced; extend patterns only for fingerprint doc |
| Base64 encode/decode | Custom alphabet handling | JDK `Base64` / `base64 -d` | Standard; strip whitespace from GitHub Secrets before decode |

**Key insight:** Android signing looks simple but has three failure classes — config-time crashes (breaking debug), silent unsigned releases, and accidental secret commits. Use guarded loading + `whenReady` + gitignore, not clever fallbacks.

## Common Pitfalls

### Pitfall 1: Silent Unsigned Release AAB
**What goes wrong:** `bundleRelease` succeeds but AAB is unsigned; Play upload fails later with cryptic errors.
**Why it happens:** AGP does not sign release builds unless `signingConfig` is assigned [CITED: developer.android.com/build].
**How to avoid:** Assign `signingConfig` when credentials exist; `whenReady` guard throws before task execution when missing (D-01).
**Warning signs:** No `SigningConfig` line in build output; `jarsigner -verify` reports "jar is unsigned".

### Pitfall 2: Configuration-Time Crash on Missing keystore.properties
**What goes wrong:** `./gradlew testDebugUnitTest` fails because `keystore.properties` doesn't exist.
**Why it happens:** Unconditional `Properties.load()` or `as String` casts on missing keys.
**How to avoid:** Return `null` from resolver when incomplete; only create `signingConfigs.release` when credentials are complete (D-02).
**Warning signs:** `null cannot be cast to non-null type kotlin.String` during configuration.

### Pitfall 3: Partial Placeholder keystore.properties
**What goes wrong:** Committed example copied but passwords not filled — `file(null)` or empty signing config.
**Why it happens:** Guard checks `file.exists()` only, not property completeness.
**How to avoid:** Validate all four properties non-blank AND `storeFile.exists()` before creating signing config.
**Warning signs:** Gradle sync fails with "Cannot convert 'null' to File".

### Pitfall 4: Base64 Secret Corruption in CI
**What goes wrong:** Decoded keystore is invalid; signing fails mid-build with opaque errors.
**Why it happens:** Newlines/whitespace in `ANDROID_KEYSTORE_BASE64` GitHub Secret.
**How to avoid:** `.trim()` before decode; optional `keytool -list` smoke check in Phase 11 workflow before Gradle.
**Warning signs:** `Keystore was tampered with, or password was incorrect` immediately after decode.

### Pitfall 5: Debug Keystore Confusion
**What goes wrong:** Maintainer thinks release used debug key.
**Why it happens:** Misreading AGP defaults or comparing wrong artifacts.
**How to avoid:** Verify with `keytool -printcert -jarfile app-release.aab` — fingerprint must match `keystore-fingerprint.md`, not `~/.android/debug.keystore`.
**Warning signs:** SHA-256 matches debug keystore (`android` / `AndroidDebugKey`).

## Code Examples

### keystore.properties.example (committed template)

```properties
# Copy to keystore.properties (gitignored) after running scripts/generate-upload-keystore.sh
storeFile=upload.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```

### Verify signed AAB (post-build validation)

```bash
# Source: developer.android.com/build/building-cmdline
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab

# Extract SHA-256 from signed artifact
keytool -printcert -jarfile app/build/outputs/bundle/release/app-release.aab | grep SHA256
```

### Encode keystore for GitHub Secret (maintainer one-time)

```bash
# macOS — for ANDROID_KEYSTORE_BASE64 secret
base64 -i upload.jks | tr -d '\n'
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Manual `jarsigner` after unsigned build | AGP `signingConfigs` during `bundleRelease` | AGP 2.x+ | Signing integrated into build graph |
| Self-sign app signing key | Play App Signing with upload key | Aug 2021 mandatory for new apps | Phase 8 creates upload key; Google holds app signing key |
| Passwords in `build.gradle` | `keystore.properties` / env vars | Android Studio guidance | SECU-03 alignment |
| `keytool -genkey` (deprecated flag) | `keytool -genkeypair` | JDK 9+ | Use `-genkeypair` in script |

**Deprecated/outdated:**
- `keytool -genkey`: alias for `-genkeypair` but `-genkeypair` is explicit and preferred [CITED: docs.oracle.com/en/java/javase/21/docs/specs/man/keytool.html].
- Unsigned release as acceptable CI artifact: rejected by D-01/D-04.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | AGP 9.3.0 `signingConfigs` DSL unchanged from 8.x patterns | Standard Stack | Build script API mismatch; verify against AGP 9 release notes during implementation |
| A2 | `jarsigner -verify` is sufficient to validate AAB signing in Phase 8 | Code Examples | May need `bundletool` for Play-specific checks in Phase 11 |
| A3 | Gradle-side base64 decode to `app/build/signing/` is acceptable for D-12 | Architecture Patterns | If maintainer prefers CI decode, env contract still works with `storeFile` path env var extension |

**Note:** All Google/Android guidance claims above are `[CITED: developer.android.com/...]` from official docs fetched this session. JDK `keytool` behavior is `[CITED: docs.oracle.com/...]`.

## Open Questions — RESOLVED

1. **Store/key password sameness** — **RESOLVED:** Allow different passwords (keytool supports both); `keystore.properties` already has separate `storePassword` and `keyPassword` fields. Script does not enforce equality per D-08 interactive prompts.

2. **Distinguished Name (DN) for upload certificate** — **RESOLVED:** Let `keytool` prompt interactively for DN fields (D-08); do not hardcode `-dname` in script. DN is not displayed to users and does not affect Play upload.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK (`java`, `keytool`) | Keystore script, signing, verification | ✓ | `/usr/bin/keytool` | Install JDK 17+ (matches project `jvmTarget = 17`) |
| Gradle wrapper (`./gradlew`) | Build verification | ✓ | Gradle 9.6.1 | — |
| Android SDK | `bundleRelease` | ✓ (assumed) | compileSdk 36 | Required for release build; local maintainer prerequisite |
| GitHub Secrets | CI signing (Phase 11) | N/A in Phase 8 | — | Phase 8 only defines contract; workflow lands in Phase 11 |

**Missing dependencies with no fallback:**
- Android SDK + build-tools for local `bundleRelease` (standard Android dev prerequisite)

**Missing dependencies with fallback:**
- None for Phase 8 deliverables (all tooling is JDK + existing Gradle wrapper)

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 (+ Robolectric 4.14.1 in `app/build.gradle.kts`) |
| Config file | none — Gradle Android plugin defaults |
| Quick run command | `./gradlew :app:assembleDebug` |
| Full suite command | `./gradlew :app:testDebugUnitTest` |

Phase 8 is build-configuration-only; behavioral verification is **command-based**, not unit-test-based.

### Phase Success Criteria → Test Map

| Criterion | Behavior | Test Type | Automated Command | File Exists? |
|-----------|----------|-----------|-------------------|-------------|
| SC-1 | `bundleRelease` signs with upload key, not debug | manual + command | `./gradlew :app:bundleRelease` then `keytool -printcert -jarfile app/build/outputs/bundle/release/app-release.aab` | ❌ Wave 0 — verification script optional |
| SC-2 | Local signing via gitignored `keystore.properties` | manual | Copy example, run bootstrap script, `bundleRelease` | ❌ `keystore.properties.example` not yet created |
| SC-3 | CI env-var contract documented | inspection | Review `app/build.gradle.kts` reads D-12 vars | ❌ signing block not yet created |
| SC-4 | SHA-256 fingerprint in gitignored doc | manual | Run `scripts/generate-upload-keystore.sh`; confirm `keystore-fingerprint.md` | ❌ script not yet created |
| D-01 | Fail-hard without credentials | command | `./gradlew :app:bundleRelease` (no props) → must fail with actionable message | ❌ guard not yet created |
| D-02 | Debug unaffected | command | `./gradlew :app:assembleDebug` without `keystore.properties` → success | ✅ baseline build works today |

### Sampling Rate
- **Per task commit:** `./gradlew :app:assembleDebug` (confirms no config-time regression)
- **Per wave merge:** `./gradlew :app:bundleRelease` with test keystore OR expect fail-hard without credentials
- **Phase gate:** Signed AAB fingerprint matches `keystore-fingerprint.md`; `jarsigner -verify` passes; Gitleaks still clean

### Wave 0 Gaps
- [ ] `keystore.properties.example` — template for local signing
- [ ] `scripts/generate-upload-keystore.sh` — bootstrap + fingerprint capture
- [ ] `app/build.gradle.kts` signing resolver + `whenReady` guard
- [ ] `.gitignore` entry for `keystore-fingerprint.md`
- [ ] Optional: `scripts/verify-release-signature.sh` wrapping `jarsigner` + `keytool` checks (planner discretion)

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | — |
| V3 Session Management | no | — |
| V4 Access Control | no | — |
| V5 Input Validation | yes | Validate env vars non-blank before decode; refuse overwrite of existing `upload.jks` in script |
| V6 Cryptography | yes | JDK `keytool` RSA 2048; no custom crypto; secrets in env/gitignored files only |
| V10 Malicious Code | partial | No secrets in committed source; Gitleaks CI from Phase 7 |

### Known Threat Patterns for Android Signing

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Keystore committed to public git | Information Disclosure | `.gitignore` `*.jks` (SECU-03); Gitleaks full-history scan |
| Passwords in `build.gradle.kts` | Information Disclosure | `keystore.properties` gitignored; CI via GitHub Secrets env vars |
| Unsigned release shipped to Play | Tampering | `whenReady` fail-hard (D-01); `jarsigner -verify` in release checklist |
| Base64 secret leakage in logs | Information Disclosure | Never `echo` secrets; Gradle does not log signing passwords by default |
| Ephemeral CI keystore persistence | Information Disclosure | Write under `app/build/` (ephemeral runner); Phase 11 cleans up |

## Project Constraints (from .cursor/rules/)

- **GSD workflow:** Phase work must go through GSD commands (`/gsd-execute-phase`); no ad-hoc repo edits outside workflow.
- **Infrastructure-only v1.1:** No app Kotlin/feature changes in Phase 8 — Gradle, scripts, and docs only.
- **Stack:** Kotlin + Gradle Kotlin DSL; match existing `app/build.gradle.kts` style.
- **Offline-first app:** Signing is build/release concern only; no network dependencies added to app runtime.

## Sources

### Primary (HIGH confidence)
- `gradle/libs.versions.toml` — AGP 9.3.0, Kotlin 2.2.10 [VERIFIED: codebase]
- `app/build.gradle.kts` — current release buildType without signing [VERIFIED: codebase]
- `.gitignore` — SECU-03 signing patterns [VERIFIED: codebase]
- Oracle JDK 21 `keytool` man page — `-genkeypair`, `-list -v`, SHA-256 default [CITED: docs.oracle.com/en/java/javase/21/docs/specs/man/keytool.html]

### Secondary (MEDIUM confidence)
- developer.android.com/build/build-variants — signingConfigs Kotlin DSL, env var passwords, keystore.properties guidance
- developer.android.com/build/building-cmdline — `keytool -genkeypair` RSA 2048 / 10000 validity; jarsigner for bundles
- developer.android.com/studio/publish/preparing — certificate validity past Oct 22, 2033
- github.com/aisleron/aisleron/pull/129 — `gradle.taskGraph.whenReady` fail-hard pattern for release signing

### Tertiary (LOW confidence — community patterns)
- GitHub Actions ANDROID_KEYSTORE_BASE64 decode workflows — validate during Phase 11 implementation
- Medium/Stackademic CI signing articles — secondary confirmation of env-var pattern

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — pinned AGP/Gradle in repo; official Android + Oracle docs for keytool
- Architecture: HIGH — locked decisions in CONTEXT.md constrain design space tightly
- Pitfalls: HIGH — well-documented AGP defaults + community fail-hard patterns

**Research date:** 2026-07-26
**Valid until:** 2026-08-26 (stable domain; AGP 9.x current)
