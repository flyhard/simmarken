---
phase: 06-export-import-i18n
reviewed: 2026-07-25T06:10:00Z
depth: standard
files_reviewed: 31
files_reviewed_list:
  - app/src/main/java/se/simmarken/data/export/BackupDto.kt
  - app/src/main/java/se/simmarken/data/export/ExportJson.kt
  - app/src/main/java/se/simmarken/data/export/ExportRepository.kt
  - app/src/main/java/se/simmarken/domain/export/BackupValidator.kt
  - app/src/main/java/se/simmarken/domain/export/MergePlanner.kt
  - app/src/main/java/se/simmarken/ui/settings/SettingsScreen.kt
  - app/src/main/java/se/simmarken/ui/settings/ExportKidPickerDialog.kt
  - app/src/main/java/se/simmarken/ui/settings/ImportConfirmDialog.kt
  - app/src/main/java/se/simmarken/ui/settings/ImportErrorDialog.kt
  - app/src/main/java/se/simmarken/ui/settings/SettingsViewModel.kt
  - app/src/main/java/se/simmarken/ui/settings/components/SettingsDataSection.kt
  - app/src/main/java/se/simmarken/ui/settings/components/SettingsLanguageSection.kt
  - app/src/main/java/se/simmarken/ui/settings/components/LanguageRadioRow.kt
  - app/src/main/java/se/simmarken/data/local/entity/KidEntity.kt
  - app/src/main/java/se/simmarken/data/local/Migrations.kt
  - app/src/main/java/se/simmarken/data/local/dao/KidDao.kt
  - app/src/main/java/se/simmarken/data/prefs/LanguageMode.kt
  - app/src/main/java/se/simmarken/data/prefs/LocalePreferencesRepository.kt
  - app/src/main/java/se/simmarken/domain/BadgeCatalogMapper.kt
  - app/src/main/java/se/simmarken/domain/validation/KidNameValidation.kt
  - app/src/main/java/se/simmarken/ui/home/HomeScreen.kt
  - app/src/main/java/se/simmarken/ui/home/KidFormFieldError.kt
  - app/src/main/java/se/simmarken/di/AppContainer.kt
  - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
  - app/src/main/java/se/simmarken/MainActivity.kt
  - app/src/main/java/se/simmarken/SimmarkenApplication.kt
  - app/src/main/AndroidManifest.xml
  - app/src/main/res/xml/file_paths.xml
  - app/src/main/res/xml/locales_config.xml
  - app/src/main/res/values/strings.xml
  - app/src/main/res/values-en/strings.xml
findings:
  critical: 2
  warning: 6
  info: 2
  total: 10
status: issues_found
---

# Phase 6: Code Review Report

**Reviewed:** 2026-07-25T06:10:00Z
**Depth:** standard
**Files Reviewed:** 31
**Status:** issues_found

## Summary

Phase 06 delivers export/import backup, bilingual chrome strings, and a language picker. The core schema, newer-wins merge logic, FileProvider scoping, and locale persistence are sound. The main risks are **unhandled failures on the import/export coroutine paths** (user gets no feedback and the app may crash on DB constraint violations) and **gaps in backup validation** that allow malformed kid identity data to reach the merge transaction. Several UX and robustness gaps around silent progress loss for new kids, MIME-type filtering on share intents, and unbounded import file reads should be addressed before shipping to parents.

## Critical Issues

### CR-01: Import merge failures are unhandled — success UI may show incorrectly or app may crash

**File:** `app/src/main/java/se/simmarken/ui/settings/SettingsViewModel.kt:193-204`
**Issue:** `confirmImport()` launches a coroutine that calls `exportRepository.merge()` with no `try/catch`. A Room/SQLite failure (e.g. unique `stableId` violation, FK error) propagates as an uncaught coroutine exception. On failure the success snackbar is never set, but the import preview is also not cleared and no error dialog is shown — leaving the user in an ambiguous state. In worst cases the exception can crash the process.
**Fix:**
```kotlin
fun confirmImport() {
    val preview = importPreview.value ?: return
    val accepted = selectedNewKidStableIds.value
    if (preview.updateCount == 0 && accepted.isEmpty()) return

    viewModelScope.launch(ioDispatcher) {
        try {
            exportRepository.merge(preview, accepted)
            importPreview.value = null
            selectedNewKidStableIds.value = emptySet()
            snackbarMessageRes.value = R.string.import_success_snackbar
        } catch (_: Exception) {
            importError.value = InvalidReason.ParseFailed // or a dedicated MergeFailed reason
        }
    }
}
```

### CR-02: Duplicate `stableId` values in backup can abort the entire merge transaction

**File:** `app/src/main/java/se/simmarken/domain/export/BackupValidator.kt:27-40`, `app/src/main/java/se/simmarken/data/export/ExportRepository.kt:83-125`
**Issue:** `BackupValidator` accepts any kid list without checking for duplicate or blank `stableId` values. If a backup contains two new kids sharing the same `stableId`, the first `kidDao.upsert()` succeeds but the second violates the unique index on `kids.stableId`, aborting the Room transaction. Combined with CR-01, this surfaces as a silent failure or crash rather than a parse error.
**Fix:** Validate in `BackupValidator.validate()` after decode:
```kotlin
val stableIds = dto.kids.map { it.stableId }
if (stableIds.any { it.isBlank() } || stableIds.size != stableIds.toSet().size) {
    return ValidationResult.Invalid(InvalidReason.InvalidFile)
}
```

## Warnings

### WR-01: New-kid progress rows silently dropped during merge are not surfaced in preview

**File:** `app/src/main/java/se/simmarken/data/export/ExportRepository.kt:95-124`, `app/src/main/java/se/simmarken/domain/export/MergePlanner.kt:57-62`
**Issue:** `skippedRowCount` is incremented only when merging progress for **existing** local kids. For **new** kids, `MergePlanner` defers all progress to `merge()`, where unresolvable catalog codes are skipped with `?: continue` and never counted. A parent can confirm import of a new child believing full progress transferred when unknown catalog codes were silently dropped.
**Fix:** Pre-validate new-kid progress in `MergePlanner.plan()` (or a shared helper) and include those skips in `skippedRowCount`, or run the same resolution pass during preview and surface a warning in `ImportConfirmDialog`.

### WR-02: Export failures produce no user-visible error

**File:** `app/src/main/java/se/simmarken/ui/settings/SettingsViewModel.kt:135-149`
**Issue:** `exportSelectedKids()` wraps export I/O in `try/finally` but has no `catch`. Disk-full, encoding, or FileProvider failures leave `isExporting` cleared with no snackbar or dialog — the parent sees nothing happen.
**Fix:** Add a `catch` block that sets a `@StringRes` error snackbar (e.g. `R.string.export_error_snackbar`).

### WR-03: Import reads entire file into memory with no size guard

**File:** `app/src/main/java/se/simmarken/ui/settings/SettingsViewModel.kt:152-159`
**Issue:** `contentResolver.openInputStream(uri)?.readBytes()` loads the full payload unconditionally. A malicious or accidentally huge JSON file can cause `OutOfMemoryError` before validation runs.
**Fix:** Stream-read with a byte cap (e.g. 4–8 MB) and reject with `InvalidReason.InvalidFile` when exceeded.

### WR-04: Share-intent MIME filter is too narrow — some `.json` deliveries are ignored

**File:** `app/src/main/java/se/simmarken/MainActivity.kt:36-48`
**Issue:** `extractImportUri()` only accepts types containing `"json"` or starting with `"text/"`. Many share targets send `application/octet-stream`, `*/*`, or omit `type` entirely for `.json` attachments. Those intents return `null` and the import is silently dropped.
**Fix:** Also accept when `intent.data?.lastPathSegment?.endsWith(".json") == true`, or when `type` is null/generic and the stream parses as valid backup JSON.

### WR-05: Rapid consecutive share intents can drop the first URI

**File:** `app/src/main/java/se/simmarken/di/AppContainer.kt:47`, `app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt:34-43`
**Issue:** `pendingImportUri` is a `MutableStateFlow` that holds only the latest value. If two import intents arrive before the collector runs, the first URI is overwritten and never processed.
**Fix:** Use a `Channel` or queue (`SharedFlow` with replay/buffer) so each intent is processed sequentially.

### WR-06: Same-day export filenames overwrite prior exports

**File:** `app/src/main/java/se/simmarken/data/export/ExportRepository.kt:175-180`
**Issue:** Export files are named `simmarken-backup-$date.json` with no timestamp suffix. Multiple exports on the same day overwrite the previous cache file; if share is delayed, the parent may share stale data.
**Fix:** Append time (e.g. `HHmmss`) or a short random suffix: `simmarken-backup-$date-$time.json`.

## Info

### IN-01: Unused import in `SettingsViewModelFactory`

**File:** `app/src/main/java/se/simmarken/ui/settings/SettingsViewModelFactory.kt:5`
**Issue:** `kotlinx.coroutines.flow.MutableStateFlow` is imported but unused.
**Fix:** Remove the unused import.

### IN-02: Deprecated `getParcelableExtra` on API 33+

**File:** `app/src/main/java/se/simmarken/MainActivity.kt:42-44`
**Issue:** `intent.getParcelableExtra(Intent.EXTRA_STREAM)` is deprecated; typed overload should be used for forward compatibility.
**Fix:** Use `IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)` from `androidx.core.content.IntentCompat`.

---

_Reviewed: 2026-07-25T06:10:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
