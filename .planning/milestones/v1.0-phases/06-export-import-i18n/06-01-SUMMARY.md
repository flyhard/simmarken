---
phase: 06-export-import-i18n
plan: 01
subsystem: database
tags: [kotlin, room, kotlinx-serialization, export, import, fileprovider, compose]

requires:
  - phase: 05-badge-detail
    provides: Progress entities, BadgeDetailViewModel write paths, Home navigation
provides:
  - Versioned JSON backup export with share sheet
  - Import validate-preview-confirm-merge flow
  - Room v2 stableId and updatedAtEpochMillis migration
  - Settings route with Data section
affects:
  - 06-02-i18n
  - 06-03-language

tech-stack:
  added:
    - robolectric (unit tests)
  patterns:
    - Parse → preview → confirm → transactional merge (D-03)
    - Catalog code-keyed progress in JSON, stableId kid identity (D-08)
    - FileProvider cache/exports only (T-06-03)

key-files:
  created:
    - app/src/main/java/se/simmarken/data/export/BackupDto.kt
    - app/src/main/java/se/simmarken/data/export/ExportJson.kt
    - app/src/main/java/se/simmarken/data/export/ExportRepository.kt
    - app/src/main/java/se/simmarken/domain/export/BackupValidator.kt
    - app/src/main/java/se/simmarken/domain/export/MergePlanner.kt
    - app/src/main/java/se/simmarken/ui/settings/SettingsScreen.kt
    - app/src/main/java/se/simmarken/ui/settings/ExportKidPickerDialog.kt
    - app/src/main/java/se/simmarken/ui/settings/ImportConfirmDialog.kt
    - app/src/main/res/xml/file_paths.xml
  modified:
    - app/src/main/java/se/simmarken/data/local/entity/KidEntity.kt
    - app/src/main/java/se/simmarken/MainActivity.kt
    - app/src/main/AndroidManifest.xml

key-decisions:
  - "Locked exportVersion:1 schema per schema-v1-research (D-08)"
  - "Newer-wins merge via per-row updatedAtEpochMillis (D-04)"
  - "New kids in import file default unchecked; parent opts in (D-02)"
  - "Pending import URI routed via AppContainer → NavHost → Settings confirm gate"

patterns-established:
  - "ExportJson mirrors CatalogSeedParser strict/lenient JSON helpers"
  - "MergePlanner pure Kotlin; ExportRepository owns transactional Room writes"

requirements-completed: [DATA-02, DATA-03]

coverage:
  - id: export-json-share
    description: Export selected kids to versioned JSON via system share sheet
    requirement: DATA-02
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/data/export/ExportRoundTripTest.kt#exportRoundTrip_preservesProgress"
        status: pass
      - kind: unit
        ref: "app/src/test/java/se/simmarken/ui/settings/SettingsViewModelExportTest.kt#singleKid_skipsPicker"
        status: pass
      - kind: integration
        ref: "app/src/androidTest/java/se/simmarken/data/local/Migration1To2Test.kt#migration_backfillsStableId"
        status: pass
    human_judgment: false
  - id: import-confirm-merge
    description: Import validates, previews, and merges only after user confirmation
    requirement: DATA-03
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/domain/export/BackupValidatorTest.kt"
        status: pass
      - kind: unit
        ref: "app/src/test/java/se/simmarken/domain/export/MergePlannerTest.kt"
        status: pass
    human_judgment: true
    rationale: Share-intent and multi-kid picker UX require device manual pass

duration: 90min
completed: 2026-07-25
status: complete
---

# Phase 06 Plan 01: Export/Import Vertical Slice Summary

**Versioned JSON backup with stableId identity, newer-wins merge, Settings export/import UI, and share-intent entry points.**

## Performance

- **Duration:** ~90 min (resumed session)
- **Tasks:** 3/3 (1 decision checkpoint + 2 execution tasks)
- **Files modified:** 39+ across two commits

## Accomplishments

- Room v2 migration backfills `stableId` UUIDs and `updatedAtEpochMillis` on progress rows
- `BackupDto` exportVersion:1 encode/decode with catalog-code progress keys (no seed payload)
- Settings Data section: export (single-kid immediate, multi-kid picker), import document picker
- `BackupValidator` + `MergePlanner` with confirm gate before transactional merge
- `ACTION_VIEW` / `ACTION_SEND` JSON intents stash URI and navigate to Settings preview
- FileProvider limited to `cache/exports/`

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] MigrationTestHelper schema load failure**
- **Found during:** Task 2
- **Issue:** `MigrationTestHelper.createDatabase` failed with kotlinx.serialization `AbstractMethodError` when loading Room schema bundles
- **Fix:** Migration test calls `MIGRATION_1_2.migrate()` directly on a v1 SupportSQLiteDatabase
- **Files modified:** `Migration1To2Test.kt`
- **Commit:** c3246f7

**2. [Rule 2 - Missing critical] Robolectric for AndroidViewModel unit tests**
- **Found during:** Task 2
- **Issue:** `SettingsViewModel` extends `AndroidViewModel`; JVM tests need Android context
- **Fix:** Added Robolectric test dependency; `ExportRepository` methods marked `open` for test doubles
- **Commit:** c3246f7, c540ed8

## Self-Check: PASSED

- FOUND: `.planning/phases/06-export-import-i18n/06-01-SUMMARY.md`
- FOUND: c3246f7 feat(06-01): end-to-end one-kid export with Room v2 migration
- FOUND: c540ed8 feat(06-01): import preview, merge, and multi-kid export
- FOUND: 92819d8 docs(06-01): lock backup JSON schema v1 in BackupDto KDoc
