---
phase: 06-export-import-i18n
plan: 03
subsystem: ui
tags: [android, i18n, datastore, appcompat, locale, settings]

requires:
  - phase: 06-02
    provides: Bilingual chrome strings and stringResource migration
provides:
  - Three-way language preference (System / Svenska / English) in Settings
  - LocalePreferencesRepository with DataStore persistence (device-only, D-21)
  - AppCompatDelegate per-app locale apply at startup and on selection
  - LocalePreferencesMappingTest JVM gate for mode→locale mapping
affects: []

tech-stack:
  added:
    - androidx.datastore:datastore-preferences:1.2.1
    - androidx.appcompat:appcompat:1.7.1
  patterns:
    - "LanguageMode enum + mapModeToLocaleList for AppCompatDelegate"
    - "SimmarkenApplication collects locale Flow and applies on change"
    - "SettingsViewModel.setLanguageMode persists then setApplicationLocales on Main"

key-files:
  created:
    - app/src/main/java/se/simmarken/data/prefs/LanguageMode.kt
    - app/src/main/java/se/simmarken/data/prefs/LocalePreferencesRepository.kt
    - app/src/main/java/se/simmarken/ui/settings/components/SettingsLanguageSection.kt
    - app/src/main/java/se/simmarken/ui/settings/components/LanguageRadioRow.kt
    - app/src/main/res/xml/locales_config.xml
    - app/src/test/java/se/simmarken/data/prefs/LocalePreferencesMappingTest.kt
  modified:
    - app/src/main/java/se/simmarken/MainActivity.kt
    - app/src/main/java/se/simmarken/SimmarkenApplication.kt
    - app/src/main/java/se/simmarken/ui/settings/SettingsScreen.kt
    - app/src/main/java/se/simmarken/ui/settings/SettingsViewModel.kt
    - app/src/main/java/se/simmarken/domain/BadgeCatalogMapper.kt

key-decisions:
  - "Pure mapLanguageModeToTag helper for JVM tests; mapModeToLocaleList wraps LocaleListCompat"
  - "Application-scoped Flow collector re-applies locales on DataStore changes while running"
  - "Catalog mapper documents D-13 — always nameSv/textSv regardless of LanguageMode"

patterns-established:
  - "PreferenceDataStoreFactory with language_mode string key storing enum name"
  - "Settings language section above Data section with 24dp gap and HorizontalDivider"

requirements-completed: [I18N-03]

coverage:
  - id: D1
    description: "DataStore persists LanguageMode; SYSTEM default when absent (D-18)"
    requirement: I18N-03
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/data/prefs/LocalePreferencesMappingTest.kt#fromStoredValue_defaultsToSystemWhenAbsent"
        status: pass
    human_judgment: false
  - id: D2
    description: "Mode maps to empty/sv/en LocaleListCompat (D-22)"
    requirement: I18N-03
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/data/prefs/LocalePreferencesMappingTest.kt"
        status: pass
    human_judgment: false
  - id: D3
    description: "Settings Language section with three radio options (D-19, D-11)"
    requirement: I18N-03
    verification:
      - kind: unit
        ref: "./gradlew :app:compileDebugKotlin"
        status: pass
    human_judgment: true
    rationale: "Radio layout and 48dp tap targets need device spot-check"
  - id: D4
    description: "Language change applies immediately via AppCompatDelegate (D-20)"
    requirement: I18N-03
    verification:
      - kind: unit
        ref: "./gradlew :app:testDebugUnitTest"
        status: pass
    human_judgment: true
    rationale: "Activity recreate and chrome recomposition require locale switch on device"
  - id: D5
    description: "Language preference excluded from backup JSON (D-21)"
    requirement: I18N-03
    verification:
      - kind: unit
        ref: "! rg language_mode|LanguageMode app/src/main/java/se/simmarken/data/export/"
        status: pass
    human_judgment: false
  - id: D6
    description: "Catalog content stays Swedish in English UI mode (D-13)"
    requirement: I18N-03
    verification:
      - kind: unit
        ref: "app/src/main/java/se/simmarken/domain/BadgeCatalogMapper.kt"
        status: pass
    human_judgment: true
    rationale: "Badge detail requirement text must be verified visually after English switch"

duration: 35min
completed: 2026-07-25
status: complete
---

# Phase 6 Plan 3: Language Picker & Locale Persistence Summary

**Three-way language preference via DataStore and AppCompatDelegate per-app locales with Settings radio picker**

## Performance

- **Duration:** 35 min
- **Started:** 2026-07-25T06:03:00Z
- **Completed:** 2026-07-25T06:38:00Z
- **Tasks:** 3
- **Files modified:** 18

## Accomplishments

- Added `LanguageMode` enum and `LocalePreferencesRepository` persisting preference in DataStore (defaults to SYSTEM per D-18)
- Migrated `MainActivity` to `AppCompatActivity` with AppCompat theme and `locales_config.xml` (sv, en)
- `SimmarkenApplication` applies saved locale at startup and on DataStore changes while app is running
- Built `SettingsLanguageSection` with three radio options above Data section (D-11, D-19)
- `SettingsViewModel.setLanguageMode` persists and calls `setApplicationLocales` immediately (D-20)
- Confirmed backup/export path has no language preference field (D-21)
- Documented D-13 Swedish-only catalog mapping in `BadgeCatalogMapper`

## Task Commits

Each task was committed atomically:

1. **Task 1: End-to-end locale persist — DataStore → AppCompatDelegate → stringResource** - `52be0c7` (feat)
2. **Task 2: Settings Language section and immediate apply on selection** - `ec6611e` (feat)
3. **Task 3: Phase 6 integration verify — locale override and catalog language guard** - `00f5b40` (feat)

## Files Created/Modified

- `app/src/main/java/se/simmarken/data/prefs/LanguageMode.kt` - Enum + pure tag/locale mapping helpers
- `app/src/main/java/se/simmarken/data/prefs/LocalePreferencesRepository.kt` - DataStore Flow and setMode
- `app/src/main/java/se/simmarken/ui/settings/components/SettingsLanguageSection.kt` - Language radio group
- `app/src/main/java/se/simmarken/ui/settings/components/LanguageRadioRow.kt` - 48dp full-width radio row
- `app/src/main/res/xml/locales_config.xml` - Per-app languages sv and en
- `app/src/test/java/se/simmarken/data/prefs/LocalePreferencesMappingTest.kt` - Mapping unit tests
- `app/src/main/java/se/simmarken/SimmarkenApplication.kt` - Startup + runtime locale observer
- `app/src/main/java/se/simmarken/ui/settings/SettingsViewModel.kt` - languageMode in uiState + setLanguageMode

## Decisions Made

- Pure `mapLanguageModeToTag()` for JVM tests; `mapModeToLocaleList()` wraps `LocaleListCompat` for production
- Application-level Flow collector handles runtime locale re-apply; ViewModel also calls `setApplicationLocales` on selection for immediate UX
- Updated both `navigation/` and `ui/settings/` `SettingsViewModelFactory` copies for locale injection

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed SettingsViewModelExportTest DataStore scope**
- **Found during:** Task 3 (full unit suite)
- **Issue:** Real `LocalePreferencesRepository` DataStore read on default dispatcher prevented `combine` from emitting in coroutine test
- **Fix:** Use `PreferenceDataStoreFactory.create(scope = CoroutineScope(testDispatcher), ...)` in test
- **Files modified:** `app/src/test/java/se/simmarken/ui/settings/SettingsViewModelExportTest.kt`
- **Verification:** `./gradlew :app:testDebugUnitTest` — 73 tests pass
- **Committed in:** `00f5b40` (Task 3 commit)

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Test-only fix; no production behavior change.

## Issues Encountered

None beyond the test DataStore scope issue above.

## Manual UAT Checklist

1. Open Settings → verify Language section above Data with System / Svenska / English radios
2. Select **English** → Home chrome shows English strings (e.g. "Add child"); open badge detail → requirement text still Swedish
3. Select **Svenska** → chrome returns to Swedish
4. Select **System default** → chrome follows device locale
5. Kill app and relaunch → last selected language preference restored
6. Export backup JSON → confirm no `language_mode` field

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 6 complete: export/import + bilingual chrome + language picker shipped
- Ready for `/gsd-verify-work` manual UAT on language switch and catalog Swedish guard
- Phase 6 ROADMAP success criteria 4 and 5 satisfied

## Self-Check: PASSED

- `app/src/main/java/se/simmarken/data/prefs/LocalePreferencesRepository.kt` — FOUND
- `app/src/main/java/se/simmarken/ui/settings/components/SettingsLanguageSection.kt` — FOUND
- `app/src/test/java/se/simmarken/data/prefs/LocalePreferencesMappingTest.kt` — FOUND
- Commit `52be0c7` — FOUND
- Commit `ec6611e` — FOUND
- Commit `00f5b40` — FOUND

---
*Phase: 06-export-import-i18n*
*Completed: 2026-07-25*
