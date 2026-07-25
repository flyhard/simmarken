---
phase: 6
slug: export-import-i18n
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-24
---

# Phase 6 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4.13.2 + kotlinx-coroutines-test 1.9.0 (JVM); AndroidJUnit4 + Room testing (instrumented) |
| **Config file** | none dedicated — Gradle `test` / `androidTest` source sets |
| **Quick run command** | `./gradlew :app:testDebugUnitTest --tests 'se.simmarken.data.export.*' --tests 'se.simmarken.domain.export.*'` |
| **Full suite command** | `./gradlew :app:testDebugUnitTest` |
| **Estimated runtime** | ~30–90 seconds (unit); instrumented longer |

---

## Sampling Rate

- **After every task commit:** Run quick run command (export/domain export packages)
- **After every plan wave:** Run `./gradlew :app:testDebugUnitTest`
- **Before `/gsd-verify-work`:** Full unit suite green + manual UAT of share sheet, import confirm, language switch
- **Max feedback latency:** ~90 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 06-W0-01 | 00 | 0 | DATA-02 | — | N/A | unit | `./gradlew :app:testDebugUnitTest --tests '*ExportRepository*'` | ❌ W0 | ⬜ pending |
| 06-W0-02 | 00 | 0 | DATA-03 | — | Reject invalid/incompatible JSON; no write | unit | `./gradlew :app:testDebugUnitTest --tests '*BackupValidator*'` | ❌ W0 | ⬜ pending |
| 06-W0-03 | 00 | 0 | DATA-03 | — | Merge newer-wins; no auto-create kids | unit | `./gradlew :app:testDebugUnitTest --tests '*MergePlanner*'` | ❌ W0 | ⬜ pending |
| 06-W0-04 | 00 | 0 | DATA-02/03 | — | Round-trip encode→decode preserves progress | unit | `./gradlew :app:testDebugUnitTest --tests '*ExportRoundTrip*'` | ❌ W0 | ⬜ pending |
| 06-W0-05 | 00 | 0 | I18N-03 | — | DataStore mode ↔ locale tags | unit | `./gradlew :app:testDebugUnitTest --tests '*LocalePreferences*'` | ✅ | ✅ green |
| 06-W0-06 | 00 | 0 | I18N-01/02 | — | Required keys in values + values-en | unit/smoke | Resource/key presence asserts | ✅ | ✅ green |
| 06-03-T1 | 03 | 3 | I18N-03 | T-06-08, T-06-SC | DataStore + AppCompat locale apply at startup | unit | `./gradlew :app:testDebugUnitTest --tests '*LocalePreferences*' && ./gradlew :app:compileDebugKotlin` | ✅ | ✅ green |
| 06-03-T2 | 03 | 3 | I18N-03 | T-06-09 | Settings language picker; no language in backup JSON | compile/grep | `./gradlew :app:compileDebugKotlin && ! rg -q 'language_mode\|LanguageMode' app/src/main/java/se/simmarken/data/export/` | ✅ | ✅ green |
| 06-03-T3 | 03 | 3 | I18N-03, D-13 | — | Catalog Swedish-only; full unit suite | unit | `./gradlew :app:testDebugUnitTest` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

*Planner fills concrete task IDs when PLAN.md files are written; Wave 0 rows above are the Nyquist seed from RESEARCH.md.*

---

## Wave 0 Requirements

- [ ] `app/src/test/java/se/simmarken/data/export/ExportRoundTripTest.kt` — covers DATA-02/03
- [ ] `app/src/test/java/se/simmarken/domain/export/BackupValidatorTest.kt` — covers DATA-03 invalid/incompatible
- [ ] `app/src/test/java/se/simmarken/domain/export/MergePlannerTest.kt` — covers D-01–D-04
- [x] `app/src/test/java/se/simmarken/data/prefs/LocalePreferencesMappingTest.kt` — covers I18N-03
- [ ] Fake `KidRepository` / `ProgressRepository` / `CatalogRepository` extensions for bulk get/upsert (follow `BadgeDetailViewModelProgressTest` fake style — no Mockito)
- [ ] Room migration test for v1→v2 `stableId` / `updatedAt` backfill (JVM Room migration test or instrumented)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Share sheet export (Files/Drive/email) | DATA-02 | System UI / Intent | Device: export with 1 kid and 2+ kids; confirm share sheet opens and file is readable JSON |
| Import via document picker + share/open intent; confirm before write | DATA-03 | SAF / Intent + UX gate | Share JSON into app → preview summary → Cancel leaves DB unchanged; Import applies merge |
| Language switch applies immediately and persists | I18N-03 | Runtime locale + DataStore | Settings → Svenska / English / System; kill app; preference restored; chrome translated, catalog stays Swedish |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 90s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
