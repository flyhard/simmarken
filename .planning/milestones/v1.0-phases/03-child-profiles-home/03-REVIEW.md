---
phase: 03-child-profiles-home
reviewed: 2026-07-23T08:20:00Z
depth: standard
files_reviewed: 19
files_reviewed_list:
  - app/src/main/java/se/simmarken/data/local/dao/KidDao.kt
  - app/src/main/java/se/simmarken/domain/repository/KidRepository.kt
  - app/src/main/java/se/simmarken/domain/util/KidAvatarInitials.kt
  - app/src/main/java/se/simmarken/domain/validation/KidNameValidation.kt
  - app/src/main/java/se/simmarken/navigation/ChildCatalogViewModelFactory.kt
  - app/src/main/java/se/simmarken/navigation/Routes.kt
  - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
  - app/src/main/java/se/simmarken/ui/child/ChildCatalogPlaceholderScreen.kt
  - app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt
  - app/src/main/java/se/simmarken/ui/home/HomeScreen.kt
  - app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt
  - app/src/main/java/se/simmarken/ui/home/KidFormBottomSheet.kt
  - app/src/main/java/se/simmarken/ui/home/KidFormViewModel.kt
  - app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt
  - app/src/main/java/se/simmarken/ui/home/components/ColorSwatchGrid.kt
  - app/src/main/java/se/simmarken/ui/home/components/DeleteKidDialog.kt
  - app/src/main/java/se/simmarken/ui/home/components/EmptyState.kt
  - app/src/main/java/se/simmarken/ui/home/components/KidAvatar.kt
  - app/src/main/java/se/simmarken/ui/theme/KidAvatarColors.kt
findings:
  critical: 1
  warning: 3
  info: 1
  total: 5
status: issues_found
---

# Phase 3: Code Review Report

**Reviewed:** 2026-07-23T08:20:00Z
**Depth:** standard
**Files Reviewed:** 19
**Status:** issues_found

## Summary

Reviewed all source files listed across `03-01-SUMMARY.md`, `03-02-SUMMARY.md`, and `03-03-SUMMARY.md` for the child-profiles-home phase. The CRUD flows, Room ordering, cascade delete wiring, and UI-SPEC layout are generally sound. One critical ViewModel scoping defect in the add/edit bottom sheet can reuse stale form state across sessions and auto-dismiss new sheets. Three warnings cover save/delete race conditions and silent failure paths.

## Critical Issues

### CR-01: KidFormViewModel reused across sheet opens without a key

**File:** `app/src/main/java/se/simmarken/ui/home/HomeScreen.kt:134-136`
**Issue:** `KidFormSheetContent` calls `viewModel(factory = KidFormViewModelFactory(...))` without a `key`. The ViewModel is scoped to the `Home` navigation back-stack entry, so it survives when the sheet is dismissed. Subsequent opens (add after edit, edit kid B after kid A) return the cached instance; the factory is ignored. Stale `name`, `selectedColorArgb`, and `saveCompleted = true` carry over. `LaunchedEffect(saveCompleted)` at line 149 can immediately dismiss a freshly opened sheet, blocking add/edit entirely.
**Fix:**
```kotlin
val formViewModel: KidFormViewModel = viewModel(
    key = kidId?.toString() ?: "add",
    factory = KidFormViewModelFactory(kidRepository, kidId),
)
```

## Warnings

### WR-01: Double-tap on save can insert duplicate children

**File:** `app/src/main/java/se/simmarken/ui/home/KidFormViewModel.kt:70-77`
**Issue:** `save()` launches a coroutine before setting `_isSaving = true` inside `Dispatchers.IO`. Two rapid taps before the first coroutine runs both pass validation and each perform an `upsert` with `id = 0`, creating duplicate kid rows.
**Fix:** Guard synchronously before launching:
```kotlin
fun save() {
  if (_isSaving.value) return
  val error = KidNameValidation.validateName(_name.value)
  if (error != null) {
    _nameError.value = error
    return
  }
  _isSaving.value = true
  viewModelScope.launch(Dispatchers.IO) {
    try {
      // ... upsert logic ...
      _saveCompleted.value = true
    } finally {
      _isSaving.value = false
    }
  }
}
```

### WR-02: Edit save fails silently when kid has not loaded yet

**File:** `app/src/main/java/se/simmarken/ui/home/KidFormViewModel.kt:79-80`
**Issue:** In edit mode, if the user taps `Spara` before `observeById` delivers the kid (`existingKid` is still null), `save()` returns from the coroutine with no error message and no persistence. The UI shows no feedback.
**Fix:** Surface an error or disable the save button until `existingKid` is non-null:
```kotlin
if (isEditMode) {
  val existing = existingKid
  if (existing == null) {
    _nameError.value = "Kunde inte ladda barnprofilen. Försök igen."
    return@launch
  }
  kidRepository.upsert(existing.copy(...))
}
```

### WR-03: Delete dialog dismisses before delete outcome is known

**File:** `app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt:67-72`
**Issue:** `confirmDelete()` clears `deleteTarget` (closing the dialog) before `kidRepository.delete` runs on `Dispatchers.IO`. If the delete throws (disk full, DB locked), the user receives no error and may believe the child was removed while the list still shows them.
**Fix:** Clear `deleteTarget` only after a successful delete, and expose an error state on failure:
```kotlin
fun confirmDelete() {
  val kid = deleteTarget.value ?: return
  viewModelScope.launch(Dispatchers.IO) {
    try {
      kidRepository.delete(kid.id)
      deleteTarget.value = null
    } catch (e: Exception) {
      // set deleteError or re-show dialog with error copy
    }
  }
}
```

## Info

### IN-01: Color swatches lack accessibility labels

**File:** `app/src/main/java/se/simmarken/ui/home/components/ColorSwatchGrid.kt:34-47`
**Issue:** Swatch `Box` elements are clickable but have no `contentDescription` or semantics label. Screen readers cannot identify or confirm color selection.
**Fix:** Add `Modifier.semantics { contentDescription = "Färg …" }` (or `clickable` overload with `onClickLabel`) per swatch.

---

_Reviewed: 2026-07-23T08:20:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
