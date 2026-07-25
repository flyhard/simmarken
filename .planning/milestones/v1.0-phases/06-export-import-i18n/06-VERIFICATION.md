---
phase: 06-export-import-i18n
verified: 2026-07-25T08:45:00Z
status: passed
score: 18/18 must-haves verified
behavior_unverified: 0
overrides_applied: 0
re_verification:
  previous_status: gaps_found
  previous_score: 16/18
  gaps_closed:
    - "Truth #16: BadgeGridItem uses stringResource — no hardcoded Swedish chrome literals"
    - "Truth #17: badge_state_* and badge_pin_content_description wired in BadgeGridItem for both locales"
  gaps_remaining: []
  regressions: []
human_uat_approved: 2026-07-25
---

# Phase 6: Export/Import & i18n Verification Report

**Phase Goal:** Parents can back up progress and use the app in Swedish or English
**Verified:** 2026-07-25T08:45:00Z
**Status:** passed
**Re-verification:** Yes — after plan 06-04 gap closure and human UAT approval

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
| --- | ------- | ---------- | -------------- |
| 1 | Parent exports kids and progress to versioned JSON via share sheet (DATA-02) | ✓ VERIFIED | `SettingsViewModel.exportSelectedKids` → `ExportRepository.buildBackup` / `writeExportFile` / `fileToShareUri`; `SettingsScreen` launches `ACTION_SEND` chooser; `ExportRoundTripTest` passes |
| 2 | One kid skips picker; two+ kids show ExportKidPickerDialog with All default-selected (D-06, D-07) | ✓ VERIFIED | `SettingsViewModel.onExportClicked` branches on `kids.size`; picker initializes `selectedKidIds = kids.map { it.id }.toSet()`; `SettingsViewModelExportTest.singleKid_skipsPicker` passes |
| 3 | Export disabled when zero kids (UI-SPEC empty state) | ✓ VERIFIED | `SettingsDataSection(exportEnabled = uiState.kids.isNotEmpty())`; `onExportClicked` returns early when empty |
| 4 | Import validates, previews, merges only after confirmation (D-03, DATA-03) | ✓ VERIFIED | `onImportUriReceived` → `parseBackup` / `planImport` sets `importPreview`; `confirmImport` → `exportRepository.merge`; `ImportConfirmDialog` gates confirm button |
| 5 | Import merges — never replaces all local kids; stableId match updates progress only (D-01) | ✓ VERIFIED | `MergePlanner.plan` upserts progress for matching `stableId`; `merge()` creates new kids only from `acceptedNewKidStableIds`; no delete-all path |
| 6 | New kids unchecked by default; parent opts in per kid (D-02) | ✓ VERIFIED | `selectedNewKidStableIds` starts empty; checkboxes in `ImportConfirmDialog`; `MergePlannerTest.merge_newKidsUnchecked` passes |
| 7 | Conflicting progress resolves newer-wins via updatedAtEpochMillis (D-04) | ✓ VERIFIED | `MergePlanner.shouldApplyRemote(remote > local)`; `MergePlannerTest.merge_newerWins` passes |
| 8 | Invalid JSON or unsupported exportVersion shows ImportErrorDialog with no partial write | ✓ VERIFIED | `BackupValidator` returns `InvalidReason`; `onImportUriReceived` sets `importError` without `merge`; `BackupValidatorTest` passes; `ImportErrorDialog` maps reasons to `@StringRes` |
| 9 | Unknown catalog codes skipped with footnote count (RESEARCH Q2) | ✓ VERIFIED | `MergePlanner` increments `skippedRowCount`; `ImportConfirmDialog` shows `import_confirm_skipped`; `MergePlannerTest.merge_skipsUnknownCodes` passes |
| 10 | Existing kid profile fields kept local on merge — progress only | ✓ VERIFIED | `merge()` upserts progress rows for existing kids only; no `KidEntity` update for matches; `MergePlannerTest.merge_keepsLocalKidProfile` passes |
| 11 | Export kid list stable-sorted by sortOrder then name | ✓ VERIFIED | `ExportRepository.buildBackup` uses `sortedWith(compareBy { sortOrder }.thenBy { name })` |
| 12 | Gear icon on Home navigates to Settings (D-09) | ✓ VERIFIED | `HomeScreen` settings `IconButton` → `onSettingsClick`; `SimmarkenNavHost` navigates to `Settings` route |
| 13 | Share/open intents route to same confirm gate (D-12) | ✓ VERIFIED | `MainActivity.handleImportIntent` → `AppContainer.pendingImportUri`; `SimmarkenNavHost` navigates to Settings and calls `onImportUriReceived` |
| 14 | Swedish and English chrome strings in values/ and values-en/ (I18N-01, I18N-02) | ✓ VERIFIED | Expanded `strings.xml` + `values-en/strings.xml`; `StringsParityTest.allTranslatableDefaultKeysExistInEnglish` passes |
| 15 | Catalog badge/requirement text remains nameSv/textSv in all locales (D-13) | ✓ VERIFIED | `BadgeCatalogMapper` comment + `nameSv`/`textSv` bindings; `RequirementChecklistRow` renders `requirement.textSv`; `BadgeGridItem` Text and contentDescription first arg use `badge.nameSv` |
| 16 | All composables use stringResource — no hardcoded Swedish chrome literals | ✓ VERIFIED | `BadgeGridItem.kt` refactored: `badgeStateLabel(): Int` returns `R.string.badge_state_*`; no hardcoded `"låst"`/`"pågår"` literals; `BadgeGridItemChromeTest` passes |
| 17 | Accessibility contentDescription keys exist in both locales and are wired | ✓ VERIFIED | `badge_state_*` and `badge_pin_content_description` in `values/` and `values-en/`; `BadgeGridItem` uses `stringResource(R.string.badge_pin_content_description, badge.nameSv, stringResource(badgeStateLabel(...)))` — mirrors `BadgeDetailScreen.kt` |
| 18 | Language picker (System / Svenska / English) persists in DataStore; excluded from backup (I18N-03, D-21) | ✓ VERIFIED | `LocalePreferencesRepository` + `LanguageMode`; `SettingsLanguageSection` three radios; no `language_mode` in `data/export/`; `LocalePreferencesMappingTest` passes |
| 19 | Language preference applies via AppCompatDelegate at startup and on selection (D-18–D-20) | ✓ VERIFIED | `SimmarkenApplication` collects `mode` Flow; `SettingsViewModel.setLanguageMode` calls `setApplicationLocales`; human UAT approved 2026-07-25 (immediate apply + kill-and-relaunch) |
| 20 | Parent can import previously exported file and restore all data (roadmap SC 2) | ✓ VERIFIED | Encode/decode/merge logic verified by unit tests; human UAT approved 2026-07-25 (import preview/cancel/confirm on device) |
| 21 | Parent can switch language in settings; preference persists (roadmap SC 5) | ✓ VERIFIED | Persistence mechanism wired and unit-tested; human UAT approved 2026-07-25 |

**Score:** 18/18 primary must-haves verified (0 present, behavior-unverified)

### Re-verification Delta (plan 06-04)

| Item | Previous | Current |
| ---- | -------- | ------- |
| Truth #16 | ✗ FAILED — hardcoded Swedish in `BadgeGridItem` | ✓ VERIFIED — `stringResource` + `@StringRes Int` pattern |
| Truth #17 | ✗ FAILED — a11y keys not wired in grid | ✓ VERIFIED — same pattern as `BadgeDetailScreen` |
| `BadgeGridItem` → `R.string.badge_state_*` key link | ✗ NOT_WIRED | ✓ WIRED |
| I18N-01 / I18N-02 requirements | ⚠️ PARTIAL | ✓ SATISFIED |
| Human UAT (TalkBack + Phase 6 spot-checks) | Pending | Approved 2026-07-25 |

### Required Artifacts

| Artifact | Expected | Status | Details |
| -------- | ----------- | ------ | ------- |
| `app/src/main/java/se/simmarken/data/export/BackupDto.kt` | Versioned backup DTO exportVersion:1 | ✓ VERIFIED | `@Serializable` DTO with stableId + catalog codes; KDoc locks schema |
| `app/src/main/java/se/simmarken/data/export/ExportRepository.kt` | encode, decode, merge transactional writes | ✓ VERIFIED | 230 lines; `withTransaction` merge; share file helpers |
| `app/src/main/java/se/simmarken/domain/export/MergePlanner.kt` | ImportPreview with newer-wins | ✓ VERIFIED | Pure planner; skipped row count; new kid previews |
| `app/src/main/java/se/simmarken/ui/settings/ImportConfirmDialog.kt` | Confirm gate before merge | ✓ VERIFIED | Wired from `SettingsScreen`; `canImport` guard |
| `app/src/main/res/xml/file_paths.xml` | FileProvider limited to cache/exports | ✓ VERIFIED | `<cache-path name="exports" path="exports/" />` only |
| `app/src/main/res/values/strings.xml` | Swedish default chrome | ✓ VERIFIED | Full chrome + export/import + language + badge_state_* keys |
| `app/src/main/res/values-en/strings.xml` | English chrome | ✓ VERIFIED | Parity test passes; English badge_state_* equivalents |
| `app/src/test/java/se/simmarken/res/StringsParityTest.kt` | Locale key parity gate | ✓ VERIFIED | Regex XML key extraction; passes |
| `app/src/main/java/se/simmarken/data/prefs/LocalePreferencesRepository.kt` | DataStore Flow of LanguageMode | ✓ VERIFIED | `language_mode` key; defaults SYSTEM |
| `app/src/main/java/se/simmarken/ui/settings/components/SettingsLanguageSection.kt` | Three-option radio group | ✓ VERIFIED | Above Data section in `SettingsScreen` |
| `app/src/main/res/xml/locales_config.xml` | Per-app languages sv and en | ✓ VERIFIED | Both locales declared |
| `app/src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt` | Localized grid pin contentDescription | ✓ VERIFIED | `badgeStateLabel(): Int` + `stringResource` wiring (plan 06-04) |
| `app/src/test/java/se/simmarken/ui/child/BadgeGridItemChromeTest.kt` | Regression gate against hardcoded literals | ✓ VERIFIED | Source-scan test passes |

### Key Link Verification

| From | To | Via | Status | Details |
| ---- | --- | --- | ------ | ------- |
| `SettingsViewModel.onExportClicked` | `ExportRepository.buildBackup` | `exportSelectedKids` on IO dispatcher | ✓ WIRED | Emits share URI on success |
| `ImportConfirmDialog` confirm | `ExportRepository.merge` | `confirmImport` → `database.withTransaction` | ✓ WIRED | Only after user confirmation |
| `BackupDto.kids` | `KidEntity.stableId` | UUID match in `MergePlanner` | ✓ WIRED | Not Room auto-increment id |
| `MainActivity` import intent | `SettingsViewModel.onImportUriReceived` | `pendingImportUri` → NavHost | ✓ WIRED | No auto-merge on intent |
| `SettingsViewModel.setLanguageMode` | `LocalePreferencesRepository.setMode` | IO dispatcher | ✓ WIRED | Also calls `setApplicationLocales` on Main |
| `LocalePreferencesRepository` | `AppCompatDelegate.setApplicationLocales` | `SimmarkenApplication` Flow collector | ✓ WIRED | Startup + runtime changes |
| `BadgeGridItem` | `R.string.badge_state_*` | `stringResource(badgeStateLabel(...))` | ✓ WIRED | Matches `BadgeDetailScreen` pattern (06-04) |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| -------- | ------------- | ------ | ------------------ | ------ |
| `SettingsScreen` | `uiState.kids` | `kidRepository.observeAll()` via ViewModel combine | Yes — Room-backed kid list | ✓ FLOWING |
| `SettingsScreen` | `uiState.importPreview` | `ExportRepository.planImport` after URI parse | Yes — computed from backup + local DB | ✓ FLOWING |
| `SettingsLanguageSection` | `selectedMode` | `localePreferencesRepository.mode` Flow | Yes — DataStore preference | ✓ FLOWING |
| `ExportKidPickerDialog` | `kids` / `selectedKidIds` | ViewModel state from repository | Yes — real kid entities | ✓ FLOWING |
| `BadgeGridItem` | `contentDescription` | `stringResource` + `badge.nameSv` + localized state label | Yes — locale-aware from DataStore/AppCompatDelegate | ✓ FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| -------- | ------- | ------ | ------ |
| BadgeGridItem i18n regression | `./gradlew :app:testDebugUnitTest --tests se.simmarken.ui.child.BadgeGridItemChromeTest` | BUILD SUCCESSFUL | ✓ PASS |
| String key parity sv/en | `./gradlew :app:testDebugUnitTest --tests se.simmarken.res.StringsParityTest` | BUILD SUCCESSFUL | ✓ PASS |
| Export round-trip preserves progress | `./gradlew :app:testDebugUnitTest --tests se.simmarken.data.export.ExportRoundTripTest` | BUILD SUCCESSFUL | ✓ PASS |
| Backup validator rejects bad JSON/version | `./gradlew :app:testDebugUnitTest --tests se.simmarken.domain.export.BackupValidatorTest` | BUILD SUCCESSFUL | ✓ PASS |
| Merge newer-wins + new kids unchecked | `./gradlew :app:testDebugUnitTest --tests se.simmarken.domain.export.MergePlannerTest` | BUILD SUCCESSFUL | ✓ PASS |
| Locale mode mapping | `./gradlew :app:testDebugUnitTest --tests se.simmarken.data.prefs.LocalePreferencesMappingTest` | BUILD SUCCESSFUL | ✓ PASS |

### Probe Execution

Step 7c: SKIPPED — no probe scripts declared for this phase.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| ----------- | ---------- | ----------- | ------ | -------- |
| DATA-02 | 06-01 | Parent can export all progress data to a file | ✓ SATISFIED | ExportRepository + share sheet + ExportRoundTripTest + human UAT |
| DATA-03 | 06-01 | Parent can import progress data from exported file | ✓ SATISFIED | Validator + preview + merge flow + MergePlannerTest + human UAT |
| I18N-01 | 06-02, 06-04 | App UI available in Swedish | ✓ SATISFIED | Chrome strings complete; BadgeGridItem a11y uses `values/strings.xml` |
| I18N-02 | 06-02, 06-04 | App UI available in English | ✓ SATISFIED | values-en parity + BadgeGridItem `stringResource` wiring + TalkBack UAT |
| I18N-03 | 06-03 | Parent can switch language in settings | ✓ SATISFIED | SettingsLanguageSection + DataStore + AppCompatDelegate + human UAT |

### Anti-Patterns Found

No blockers. No TBD/FIXME/XXX debt markers in phase-modified source files. Hardcoded Swedish state literals removed from `BadgeGridItem.kt` (previously flagged blocker — resolved in 06-04).

### Human Verification Completed

User approved plan 06-04 device UAT checkpoint on **2026-07-25**, covering:

- **Badge grid TalkBack (truths #16/#17):** English locale announces English state labels; badge names remain Swedish (D-13)
- **Export share sheet (DATA-02):** Single-kid and multi-kid export flows
- **Import confirm gate (DATA-03):** Preview, cancel-without-write, confirm merge
- **Language persistence (I18N-03):** Immediate apply and survive kill-and-relaunch
- **Catalog Swedish guard (D-13):** English chrome with Swedish badge/requirement content
- **Import concurrency:** No partial writes before confirm

### Gaps Summary

All verification gaps closed. Plan 06-04 refactored `BadgeGridItem.kt` to mirror `BadgeDetailScreen.kt` for localized accessibility labels. `BadgeGridItemChromeTest` provides a JVM regression gate. Phase goal achieved: parents can back up progress and use the app in Swedish or English.

---

_Verified: 2026-07-25T08:45:00Z_
_Verifier: Claude (gsd-verifier)_
