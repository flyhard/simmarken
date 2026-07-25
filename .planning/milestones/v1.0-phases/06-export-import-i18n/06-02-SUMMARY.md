---
phase: 06-export-import-i18n
plan: 02
subsystem: ui
tags: [android, i18n, strings.xml, compose, stringResource]

requires:
  - phase: 06-01
    provides: Settings screen and export/import dialogs with hardcoded Swedish copy
provides:
  - Bilingual chrome strings in values/strings.xml and values-en/strings.xml
  - stringResource usage across Home, catalog, badge detail, and Settings UI
  - StringsParityTest JVM gate for locale key parity
  - KidNameValidation enum errors resolved in composables
affects: [06-03]

tech-stack:
  added: []
  patterns:
    - "stringResource(R.string.*) for all chrome copy"
    - "KidNameError enum mapped to strings in KidFormBottomSheet"
    - "InvalidReason mapped to @StringRes in ImportErrorDialog"

key-files:
  created:
    - app/src/main/res/values-en/strings.xml
    - app/src/test/java/se/simmarken/res/StringsParityTest.kt
    - app/src/main/java/se/simmarken/ui/home/KidFormFieldError.kt
  modified:
    - app/src/main/res/values/strings.xml
    - app/src/main/java/se/simmarken/ui/home/HomeScreen.kt
    - app/src/main/java/se/simmarken/ui/settings/SettingsViewModel.kt
    - app/src/main/java/se/simmarken/domain/validation/KidNameValidation.kt

key-decisions:
  - "Catalog tab labels Simidrott/SLS use translatable=false string resources per D-16"
  - "KidNameValidation returns KidNameError enum; composables resolve string resources"
  - "SettingsViewModel snackbar uses @StringRes Int instead of hardcoded Swedish"
  - "Import error bodies aligned to UI-SPEC Copywriting Contract (D-17)"

patterns-established:
  - "StringsParityTest regex-parses XML to assert values-en parity without Robolectric"
  - "Requirement checklist rows unchanged — still bind textSv per D-13"

requirements-completed: [I18N-01, I18N-02]

coverage:
  - id: D1
    description: "Swedish chrome strings in values/strings.xml as default locale"
    requirement: I18N-01
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/res/StringsParityTest.kt#allTranslatableDefaultKeysExistInEnglish"
        status: pass
    human_judgment: false
  - id: D2
    description: "English chrome strings in values-en/strings.xml"
    requirement: I18N-02
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/res/StringsParityTest.kt#allTranslatableDefaultKeysExistInEnglish"
        status: pass
    human_judgment: false
  - id: D3
    description: "Catalog badge/requirement text remains nameSv/textSv in all locales (D-13)"
    requirement: I18N-01
    verification:
      - kind: unit
        ref: "./gradlew :app:compileDebugKotlin"
        status: pass
    human_judgment: true
    rationale: "Compile proves textSv bindings unchanged; visual catalog-language check needs device spot-check"
  - id: D4
    description: "Import/export feedback strings translate with chrome (D-17)"
    requirement: I18N-02
    verification:
      - kind: unit
        ref: "./gradlew :app:testDebugUnitTest"
        status: pass
    human_judgment: true
    rationale: "Error variant copy requires locale switch verification in 06-03"

duration: 25min
completed: 2026-07-25
status: complete
---

# Phase 6 Plan 2: Chrome i18n Migration Summary

**Bilingual string resources for all app chrome with StringsParityTest gate — catalog content stays Swedish per D-13**

## Performance

- **Duration:** 25 min
- **Started:** 2026-07-25T05:56:00Z
- **Completed:** 2026-07-25T06:21:00Z
- **Tasks:** 3
- **Files modified:** 24

## Accomplishments

- Migrated Home, catalog, badge detail, kid form, and Settings chrome from hardcoded Swedish to `stringResource`
- Created `values-en/strings.xml` with English equivalents for all translatable keys
- Added `StringsParityTest` asserting full key parity between default and English locales
- Refactored `KidNameValidation` to return `KidNameError` enum; composables resolve localized messages
- Mapped `InvalidReason` and snackbar feedback to string resources (D-17, T-06-06)

## Task Commits

Each task was committed atomically:

1. **Task 1: End-to-end Home chrome i18n** - `4f6bb53` (feat)
2. **Task 2: Migrate catalog, badge detail, and kid form chrome strings** - `d3cdea2` (feat)
3. **Task 3: Settings and import/export dialog strings + full parity gate** - `45551ea` (feat)

## Files Created/Modified

- `app/src/main/res/values/strings.xml` - Swedish default chrome + progress strings (expanded)
- `app/src/main/res/values-en/strings.xml` - English chrome strings
- `app/src/test/java/se/simmarken/res/StringsParityTest.kt` - Locale key parity JVM test
- `app/src/main/java/se/simmarken/domain/validation/KidNameValidation.kt` - Enum-based validation
- `app/src/main/java/se/simmarken/ui/**` - Composables migrated to `stringResource`

## Decisions Made

- Used `translatable="false"` for Simidrott/SLS tab labels while still referencing via `stringResource` (D-16)
- Regex XML parsing in parity test avoids Robolectric `XmlPullParserFactory` mocking issues
- Settings snackbar holds `@StringRes Int`; screen resolves via `context.getString`

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed StringsParityTest XmlPullParser not mocked on JVM**
- **Found during:** Task 1
- **Issue:** `XmlPullParserFactory.newInstance()` throws on unit test classpath
- **Fix:** Switched to regex-based XML key extraction
- **Files modified:** `app/src/test/java/se/simmarken/res/StringsParityTest.kt`
- **Committed in:** `4f6bb53`

**2. [Rule 3 - Blocking] Updated KidNameValidationTest for enum return type**
- **Found during:** Task 3 verification
- **Issue:** Tests expected Swedish string literals from validation
- **Fix:** Assert `KidNameError.EMPTY` / `TOO_LONG` instead
- **Files modified:** `app/src/test/java/se/simmarken/domain/validation/KidNameValidationTest.kt`
- **Committed in:** `45551ea`

---

**Total deviations:** 2 auto-fixed (1 bug, 1 blocking)
**Impact on plan:** Required for test gate; no scope creep.

## Issues Encountered

None beyond auto-fixed test infrastructure issues.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- I18N-01 and I18N-02 complete; ready for 06-03 language picker and `AppCompatDelegate` locale application
- Language section string keys pre-added (`settings_language_section`, `language_*`) for 06-03 wiring

## Self-Check: PASSED

- FOUND: `.planning/phases/06-export-import-i18n/06-02-SUMMARY.md`
- FOUND: `app/src/main/res/values-en/strings.xml`
- FOUND: `app/src/test/java/se/simmarken/res/StringsParityTest.kt`
- FOUND: commit `4f6bb53`
- FOUND: commit `d3cdea2`
- FOUND: commit `45551ea`

---
*Phase: 06-export-import-i18n*
*Completed: 2026-07-25*
