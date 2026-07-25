---
phase: 06-export-import-i18n
verified: 2026-07-25T06:10:00Z
status: gaps_found
score: 16/18 must-haves verified
behavior_unverified: 5
overrides_applied: 0
gaps:
  - truth: "Catalog badge grid accessibility strings use stringResource in both Swedish and English (I18N-01/02, plan 06-02)"
    status: failed
    reason: "BadgeGridItem.kt hardcodes Swedish state labels in contentDescription while badge_state_* string keys already exist in values/ and values-en/"
    artifacts:
      - path: app/src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt
        issue: "badgeStateLabel() returns hardcoded Swedish literals; BadgeDetailScreen.kt already uses R.string.badge_state_* correctly"
    missing:
      - "Refactor BadgeGridItem to mirror BadgeDetailScreen badgeStateLabel() → @StringRes + stringResource(R.string.badge_pin_content_description, badge.nameSv, stateLabel)"
behavior_unverified_items:
  - truth: "Share sheet opens with readable JSON export on device (DATA-02)"
    test: "Export with one kid and with two+ kids from Settings; confirm system share chooser and JSON contents"
    expected: "Share sheet opens; file is valid exportVersion:1 JSON with selected kids and progress"
    why_human: "Intent/share UI cannot be exercised in JVM tests"
  - truth: "Import via document picker and share/open intent uses confirm gate; cancel leaves DB unchanged (DATA-03)"
    test: "Import valid backup via picker → preview → Cancel; share JSON into app via ACTION_SEND → preview → Cancel"
    expected: "Preview shown before any write; cancel dismisses without Room mutations"
    why_human: "SAF and intent entry require device; no instrumented test for cancel-before-merge invariant"
  - truth: "Language selection applies immediately and persists across app restart (I18N-03, D-20)"
    test: "Settings → English → verify chrome; kill app; relaunch; repeat for Svenska and System default"
    expected: "Chrome strings match selection without manual restart; preference restored after kill"
    why_human: "AppCompatDelegate activity recreate and DataStore reload are runtime behaviors"
  - truth: "English UI mode keeps catalog badge/requirement text in Swedish (D-13)"
    test: "Switch to English; open badge detail and catalog grid"
    expected: "Buttons/labels in English; badge names and requirement checklist still nameSv/textSv"
    why_human: "Visual locale composition cannot be proven by static grep alone"
  - truth: "Interrupted or parallel import attempts leave DB unchanged until confirm completes (concurrency probe)"
    test: "Start import preview; attempt second import or dismiss mid-flow before confirming"
    expected: "No partial merge writes until ImportConfirmDialog confirm"
    why_human: "State transition invariant not covered by a named behavioral test"
human_verification:
  - test: "Export backup via share sheet (1 kid and 2+ kids)"
    expected: "Single kid skips picker; multi-kid shows ExportKidPickerDialog with All default-selected; share chooser opens"
    why_human: "System share UI and SAF require device"
  - test: "Import valid backup — preview, cancel, then confirm"
    expected: "Preview summary shown; cancel leaves data unchanged; confirm merges per newer-wins rules"
    why_human: "End-to-end import UX and DB state require device"
  - test: "Import invalid/unsupported JSON"
    expected: "ImportErrorDialog with localized body; no Room writes"
    why_human: "Dialog copy and no-write guarantee need device spot-check (validator logic is unit-tested)"
  - test: "Language picker — System / Svenska / English"
    expected: "Chrome translates immediately; preference survives kill-and-relaunch; catalog content stays Swedish"
    why_human: "Runtime locale and visual composition"
  - test: "Badge grid TalkBack in English locale"
    expected: "Accessibility state labels read in English (locked/in progress/etc.)"
    why_human: "Blocked until BadgeGridItem gap is fixed; then verify on device with TalkBack"
---

# Phase 6: Export/Import & i18n Verification Report

**Phase Goal:** Parents can back up progress and use the app in Swedish or English
**Verified:** 2026-07-25T06:10:00Z
**Status:** gaps_found
**Re-verification:** No — initial verification

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
| 15 | Catalog badge/requirement text remains nameSv/textSv in all locales (D-13) | ✓ VERIFIED | `BadgeCatalogMapper` comment + `nameSv`/`textSv` bindings; `RequirementChecklistRow` renders `requirement.textSv` |
| 16 | All composables use stringResource — no hardcoded Swedish chrome literals | ✗ FAILED | `BadgeGridItem.kt` lines 21–26 hardcode Swedish state labels; `BadgeDetailScreen.kt` correctly uses `R.string.badge_state_*` |
| 17 | Accessibility contentDescription keys exist in both locales and are wired | ✗ FAILED | Keys `badge_state_*` and `badge_pin_content_description` exist in both `values/` and `values-en/` but `BadgeGridItem` does not use them |
| 18 | Language picker (System / Svenska / English) persists in DataStore; excluded from backup (I18N-03, D-21) | ✓ VERIFIED | `LocalePreferencesRepository` + `LanguageMode`; `SettingsLanguageSection` three radios; no `language_mode` in `data/export/`; `LocalePreferencesMappingTest` passes |
| 19 | Language preference applies via AppCompatDelegate at startup and on selection (D-18–D-20) | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED | `SimmarkenApplication` collects `mode` Flow; `SettingsViewModel.setLanguageMode` calls `setApplicationLocales`; mapping tests pass; device recreate not exercised |
| 20 | Parent can import previously exported file and restore all data (roadmap SC 2) | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED | Encode/decode/merge logic verified by unit tests; full device round-trip not run in verification |
| 21 | Parent can switch language in settings; preference persists (roadmap SC 5) | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED | Persistence mechanism wired and unit-tested; kill-and-relaunch behavior needs device |

**Score:** 16/18 truths verified (5 present, behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
| -------- | ----------- | ------ | ------- |
| `app/src/main/java/se/simmarken/data/export/BackupDto.kt` | Versioned backup DTO exportVersion:1 | ✓ VERIFIED | `@Serializable` DTO with stableId + catalog codes; KDoc locks schema |
| `app/src/main/java/se/simmarken/data/export/ExportRepository.kt` | encode, decode, merge transactional writes | ✓ VERIFIED | 230 lines; `withTransaction` merge; share file helpers |
| `app/src/main/java/se/simmarken/domain/export/MergePlanner.kt` | ImportPreview with newer-wins | ✓ VERIFIED | Pure planner; skipped row count; new kid previews |
| `app/src/main/java/se/simmarken/ui/settings/ImportConfirmDialog.kt` | Confirm gate before merge | ✓ VERIFIED | Wired from `SettingsScreen`; `canImport` guard |
| `app/src/main/res/xml/file_paths.xml` | FileProvider limited to cache/exports | ✓ VERIFIED | `<cache-path name="exports" path="exports/" />` only |
| `app/src/main/res/values/strings.xml` | Swedish default chrome | ✓ VERIFIED | Full chrome + export/import + language keys |
| `app/src/main/res/values-en/strings.xml` | English chrome | ✓ VERIFIED | Parity test passes |
| `app/src/test/java/se/simmarken/res/StringsParityTest.kt` | Locale key parity gate | ✓ VERIFIED | Regex XML key extraction; passes |
| `app/src/main/java/se/simmarken/data/prefs/LocalePreferencesRepository.kt` | DataStore Flow of LanguageMode | ✓ VERIFIED | `language_mode` key; defaults SYSTEM |
| `app/src/main/java/se/simmarken/ui/settings/components/SettingsLanguageSection.kt` | Three-option radio group | ✓ VERIFIED | Above Data section in `SettingsScreen` |
| `app/src/main/res/xml/locales_config.xml` | Per-app languages sv and en | ✓ VERIFIED | Both locales declared |

### Key Link Verification

| From | To | Via | Status | Details |
| ---- | --- | --- | ------ | ------- |
| `SettingsViewModel.onExportClicked` | `ExportRepository.buildBackup` | `exportSelectedKids` on IO dispatcher | ✓ WIRED | Emits share URI on success |
| `ImportConfirmDialog` confirm | `ExportRepository.merge` | `confirmImport` → `database.withTransaction` | ✓ WIRED | Only after user confirmation |
| `BackupDto.kids` | `KidEntity.stableId` | UUID match in `MergePlanner` | ✓ WIRED | Not Room auto-increment id |
| `MainActivity` import intent | `SettingsViewModel.onImportUriReceived` | `pendingImportUri` → NavHost | ✓ WIRED | No auto-merge on intent |
| `SettingsViewModel.setLanguageMode` | `LocalePreferencesRepository.setMode` | IO dispatcher | ✓ WIRED | Also calls `setApplicationLocales` on Main |
| `LocalePreferencesRepository` | `AppCompatDelegate.setApplicationLocales` | `SimmarkenApplication` Flow collector | ✓ WIRED | Startup + runtime changes |
| `BadgeGridItem` | `R.string.badge_state_*` | stringResource | ✗ NOT_WIRED | Hardcoded Swedish strings instead |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| -------- | ------------- | ------ | ------------------ | ------ |
| `SettingsScreen` | `uiState.kids` | `kidRepository.observeAll()` via ViewModel combine | Yes — Room-backed kid list | ✓ FLOWING |
| `SettingsScreen` | `uiState.importPreview` | `ExportRepository.planImport` after URI parse | Yes — computed from backup + local DB | ✓ FLOWING |
| `SettingsLanguageSection` | `selectedMode` | `localePreferencesRepository.mode` Flow | Yes — DataStore preference | ✓ FLOWING |
| `ExportKidPickerDialog` | `kids` / `selectedKidIds` | ViewModel state from repository | Yes — real kid entities | ✓ FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| -------- | ------- | ------ | ------ |
| Export round-trip preserves progress | `./gradlew :app:testDebugUnitTest --tests se.simmarken.data.export.ExportRoundTripTest` | BUILD SUCCESSFUL | ✓ PASS |
| Backup validator rejects bad JSON/version | `./gradlew :app:testDebugUnitTest --tests se.simmarken.domain.export.BackupValidatorTest` | BUILD SUCCESSFUL | ✓ PASS |
| Merge newer-wins + new kids unchecked | `./gradlew :app:testDebugUnitTest --tests se.simmarken.domain.export.MergePlannerTest` | BUILD SUCCESSFUL | ✓ PASS |
| Single kid export skips picker | `./gradlew :app:testDebugUnitTest --tests se.simmarken.ui.settings.SettingsViewModelExportTest` | BUILD SUCCESSFUL | ✓ PASS |
| Locale mode mapping | `./gradlew :app:testDebugUnitTest --tests se.simmarken.data.prefs.LocalePreferencesMappingTest` | BUILD SUCCESSFUL | ✓ PASS |
| String key parity sv/en | `./gradlew :app:testDebugUnitTest --tests se.simmarken.res.StringsParityTest` | BUILD SUCCESSFUL | ✓ PASS |

### Probe Execution

Step 7c: SKIPPED — no probe scripts declared for this phase.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| ----------- | ---------- | ----------- | ------ | -------- |
| DATA-02 | 06-01 | Parent can export all progress data to a file | ✓ SATISFIED | ExportRepository + share sheet + ExportRoundTripTest |
| DATA-03 | 06-01 | Parent can import progress data from exported file | ✓ SATISFIED | Validator + preview + merge flow + MergePlannerTest |
| I18N-01 | 06-02 | App UI available in Swedish | ⚠️ PARTIAL | Chrome strings complete; BadgeGridItem a11y labels hardcoded Swedish |
| I18N-02 | 06-02 | App UI available in English | ⚠️ PARTIAL | values-en parity passes; BadgeGridItem a11y not localized |
| I18N-03 | 06-03 | Parent can switch language in settings | ✓ SATISFIED | SettingsLanguageSection + DataStore + AppCompatDelegate wiring |

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| ---- | ---- | ------- | -------- | ------ |
| `BadgeGridItem.kt` | 21–26 | Hardcoded Swedish literals in composable | 🛑 Blocker | English TalkBack reads Swedish state labels; violates plan 06-02 must-haves |

No TBD/FIXME/XXX debt markers found in phase-modified source files.

### Human Verification Required

### 1. Share sheet export

**Test:** Export with one kid and with two+ kids from Settings → Data.
**Expected:** Single kid opens share chooser immediately; multi-kid shows picker with All selected; JSON file is valid backup.
**Why human:** System Intent UI cannot be automated in JVM tests.

### 2. Import confirm gate

**Test:** Import valid backup via document picker; cancel, then re-import and confirm. Also share JSON into app via Files/Drive.
**Expected:** Preview before write; cancel leaves DB unchanged; confirm applies merge.
**Why human:** SAF and intent flows require device.

### 3. Invalid import error

**Test:** Import malformed JSON or unsupported exportVersion.
**Expected:** `ImportErrorDialog` with localized message; no partial DB writes.
**Why human:** Visual error copy; validator logic already unit-tested.

### 4. Language switch and persistence

**Test:** Settings → English / Svenska / System; kill app and relaunch.
**Expected:** Chrome matches selection immediately; preference restored; catalog text stays Swedish.
**Why human:** AppCompatDelegate recreate and visual locale composition.

### 5. Badge grid accessibility (after gap fix)

**Test:** Enable TalkBack in English locale; focus catalog badge grid items.
**Expected:** State announced in English (locked, in progress, etc.).
**Why human:** Currently blocked by BadgeGridItem hardcoded strings.

### Gaps Summary

Phase 6 delivers the core export/import vertical slice and bilingual settings infrastructure with strong unit-test coverage. One i18n wiring gap blocks full goal achievement: **`BadgeGridItem.kt` hardcodes Swedish accessibility state labels** even though `badge_state_*` and `badge_pin_content_description` string resources exist and are used correctly in `BadgeDetailScreen.kt`. Fix is a small refactor mirroring the detail screen pattern.

Five behavior-dependent truths (share sheet UX, device import round-trip, locale recreate/persistence, catalog Swedish guard, import concurrency) are present and wired but require manual UAT on device.

---

_Verified: 2026-07-25T06:10:00Z_
_Verifier: Claude (gsd-verifier)_
