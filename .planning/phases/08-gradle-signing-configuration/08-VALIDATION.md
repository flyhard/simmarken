---
phase: 8
slug: gradle-signing-configuration
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-26
---

# Phase 8 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 (+ Robolectric 4.14.1 in `app/build.gradle.kts`) |
| **Config file** | none — Gradle Android plugin defaults |
| **Quick run command** | `./gradlew :app:assembleDebug` |
| **Full suite command** | `./gradlew :app:testDebugUnitTest` |
| **Estimated runtime** | ~30 seconds (debug assemble + unit tests); release fail-hard check ~60s (wave-level only) |

Phase 8 is build-configuration-only; behavioral verification is **command-based**, not unit-test-based.

---

## Sampling Rate

- **After every task commit:** Run `./gradlew :app:assembleDebug --quiet` (fast regression guard)
- **After Plan 08-01 tracer task:** Also run `./gradlew :app:testDebugUnitTest --quiet` (D-02)
- **After every plan wave:** Run wave-level bundleRelease fail-hard check (no credentials → must fail with actionable message referencing `keystore.properties.example` and `scripts/generate-upload-keystore.sh`)
- **Before `/gsd-verify-work`:** Signed AAB fingerprint matches `keystore-fingerprint.md`; `jarsigner -verify` passes; Gitleaks still clean
- **Max feedback latency:** 60 seconds per-task; bundleRelease reserved for wave boundary

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 08-01-01 | 01 | 1 | D-01,D-02,D-03,D-04,D-12 | T-08-01,T-08-03,T-08-04,T-08-05 | Signing resolver + whenReady guard; debug unaffected; CI env-var contract | command + inspection | `./gradlew :app:assembleDebug --quiet && ./gradlew :app:testDebugUnitTest --quiet` + static greps for resolver, whenReady, four D-12 env vars in `app/build.gradle.kts` | ❌ W0 | ⬜ pending |
| 08-01-02 | 01 | 1 | D-09,D-10,D-11 | T-08-02 | `keystore.properties.example` committed with four properties | inspection | `test -f keystore.properties.example` + property line counts | ❌ W0 | ⬜ pending |
| 08-01-wave | 01 | 1 | D-01,D-03 | T-08-01 | bundleRelease fails hard with actionable message | command | `./gradlew :app:bundleRelease > /tmp/phase8-bundle-release.txt 2>&1; BUNDLE_EXIT=$?; test $BUNDLE_EXIT -ne 0 && grep -qi keystore.properties.example /tmp/phase8-bundle-release.txt && grep -qi generate-upload-keystore.sh /tmp/phase8-bundle-release.txt` | ❌ W0 | ⬜ pending |
| 08-02-01 | 02 | 1 | D-05,D-07 | T-08-08 | Maintainer confirms one-time keystore generation | checkpoint | Human selects proceed or skip-generation | — | ⬜ pending |
| 08-02-02 | 02 | 1 | D-05..D-08,D-14,D-15 | T-08-06,T-08-07 | Keystore script: RSA 2048, alias upload, interactive passwords, fingerprint capture | inspection | `bash -n scripts/generate-upload-keystore.sh` + static greps (no -storepass) | ❌ W0 | ⬜ pending |
| 08-02-03 | 02 | 1 | D-13,D-16,SECU-03 | T-08-09 | Fingerprint doc gitignored; SECURITY-CHECKLIST updated | command | `git check-ignore -v keystore-fingerprint.md` | ❌ W0 | ⬜ pending |
| 08-03-01 | 03 | 2 | CI-02,RELE-01 | T-08-10 | verify-release-signature.sh wraps jarsigner + keytool | inspection | `bash -n scripts/verify-release-signature.sh` + static greps | ❌ W0 | ⬜ pending |
| 08-03-02 | 03 | 2 | SC-1..SC-4,D-01,D-02 | T-08-10,T-08-11,T-08-12 | Signed AAB end-to-end; fail-hard reconfirmed; gitleaks clean | manual | Human checkpoint: bundleRelease, verify script, rename keystore.properties fail-hard, assembleDebug, gitleaks | — | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `keystore.properties.example` — template for local signing (Plan 08-01)
- [ ] `app/build.gradle.kts` signing resolver + `whenReady` guard (Plan 08-01)
- [ ] `scripts/generate-upload-keystore.sh` — bootstrap + fingerprint capture (Plan 08-02)
- [ ] `.gitignore` entry for `keystore-fingerprint.md` (Plan 08-02)
- [ ] `scripts/verify-release-signature.sh` wrapping `jarsigner` + `keytool` checks (Plan 08-03)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Signed AAB uses upload key | SC-1 / RELE-01 | Requires maintainer keystore | Run `bundleRelease` with credentials; `./scripts/verify-release-signature.sh` |
| SHA-256 fingerprint doc | SC-4 / D-14 | Private maintainer artifact | Run bootstrap script; verify `keystore-fingerprint.md` contents |
| Keystore generation decision | D-05 | One-way signing identity | Plan 08-02 checkpoint: proceed or skip-generation |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s per-task (bundleRelease fail-hard at wave boundary only)
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
