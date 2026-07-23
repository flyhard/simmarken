---
phase: 05-progress-tracking-badge-detail
reviewed: 2026-07-23T12:32:00Z
depth: standard
files_reviewed: 22
files_reviewed_list:
  - app/src/main/java/se/simmarken/domain/model/RequirementRowUiModel.kt
  - app/src/main/java/se/simmarken/domain/model/BadgeDetailUiState.kt
  - app/src/main/java/se/simmarken/domain/model/KidProgressSummary.kt
  - app/src/main/java/se/simmarken/domain/ProgressWriteLogic.kt
  - app/src/main/java/se/simmarken/domain/KidProgressSummaryCalculator.kt
  - app/src/main/java/se/simmarken/domain/repository/CatalogRepository.kt
  - app/src/main/java/se/simmarken/domain/repository/CatalogRepositoryImpl.kt
  - app/src/main/java/se/simmarken/domain/repository/ProgressRepository.kt
  - app/src/main/java/se/simmarken/domain/repository/ProgressRepositoryImpl.kt
  - app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt
  - app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt
  - app/src/main/java/se/simmarken/ui/badge/BadgeDetailScreen.kt
  - app/src/main/java/se/simmarken/ui/badge/components/RequirementChecklistRow.kt
  - app/src/main/java/se/simmarken/ui/badge/components/RequirementChecklist.kt
  - app/src/main/java/se/simmarken/ui/badge/components/ZeroRequirementNote.kt
  - app/src/main/java/se/simmarken/ui/badge/components/PurchaseToggleRow.kt
  - app/src/main/java/se/simmarken/ui/badge/components/UncheckPurchaseDialog.kt
  - app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt
  - app/src/main/java/se/simmarken/ui/home/HomeScreen.kt
  - app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt
  - app/src/main/java/se/simmarken/navigation/HomeViewModelFactory.kt
  - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
findings:
  critical: 1
  warning: 4
  info: 2
  total: 7
status: issues_found
---

# Phase 5: Code Review Report

**Reviewed:** 2026-07-23T12:32:00Z  
**Depth:** standard  
**Files Reviewed:** 22  
**Status:** issues_found

## Summary

Phase 5 delivers the progress-write vertical slice (requirement toggles, köpt flag, home summaries) with strong domain test coverage for `ProgressWriteLogic`, `BadgeDetailViewModel`, and `KidProgressSummaryCalculator`. Visual state derivation consistently routes through `BadgeCatalogMapper` → `BadgeStateCalculator` as required by D-17/D-20.

The main risks are in `BadgeDetailViewModel.toggleRequirement`: paired Room upserts are neither ordered correctly for the GOTTEN auto-clear path (D-10) nor wrapped in a transaction, so a process kill or slow I/O can leave `isGotten = true` while a requirement is unchecked. Secondary concerns are concurrent toggle races and a Compose checkbox row pattern that can double-fire toggles.

## Critical Issues

### CR-01: Non-atomic dual upsert can persist GOTTEN with unchecked requirements (D-10 violation)

**File:** `app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt:128-158`  
**Issue:** When unchecking a requirement on a GOTTEN badge, the ViewModel upserts `RequirementProgressEntity` first (line 128), then upserts `BadgeProgressEntity` with `isGotten = false` only if `badgeProgressDirty` (lines 156–158). These are separate, non-transactional DAO calls. If the process is killed or the second upsert fails after the first succeeds, Room retains `isGotten = true` with `isAchieved = false` for that requirement. `BadgeStateCalculator` returns `GOTTEN` whenever `isGotten` is true, so the badge stays köpt despite the unchecked skill — a persistent violation of D-10.

Between the two writes, the UI can also briefly show GOTTEN with an unchecked checkbox because `isGotten` is evaluated before requirement counts in `BadgeStateCalculator.compute`.

**Fix:** Upsert badge progress (clearing gotten) **before** the requirement upsert when `shouldClearGottenOnUncheck` is true, and wrap both writes in a single Room `@Transaction` (e.g. add `ProgressRepository.toggleRequirementWithSideEffects(...)` backed by a transactional DAO method):

```kotlin
@Transaction
suspend fun applyRequirementToggle(
    requirementProgress: RequirementProgressEntity,
    badgeProgress: BadgeProgressEntity?,
) {
    badgeProgress?.let { badgeProgressDao.upsert(it) }
    requirementProgressDao.upsert(requirementProgress)
}
```

Call this from `toggleRequirement` instead of two independent `upsert*` calls.

## Warnings

### WR-01: Concurrent `toggleRequirement` calls can lose updates

**File:** `app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt:100-159`  
**Issue:** Each `toggleRequirement` call launches an independent coroutine that snapshots state via `.first()` on flows, then writes. Rapid opposite-direction taps (check then immediate uncheck) can both read the same initial `currentlyAchieved` value and write the same flipped state, leaving the checkbox stuck in the wrong position until the user taps again.

**Fix:** Serialize writes with a `Mutex` in the ViewModel, or debounce/disable row interaction while a toggle job is in flight:

```kotlin
private val toggleMutex = Mutex()

fun toggleRequirement(requirementId: Long) {
    viewModelScope.launch(ioDispatcher) {
        toggleMutex.withLock {
            // existing read-compute-write logic
        }
    }
}
```

### WR-02: Checkbox row registers two independent toggle handlers

**File:** `app/src/main/java/se/simmarken/ui/badge/components/RequirementChecklistRow.kt:29-36`  
**Issue:** The `Row` uses `Modifier.clickable(onClick = onToggle)` and the `Checkbox` also sets `onCheckedChange = { onToggle() }`. Material 3 documents that when the row is clickable, the checkbox should use `onCheckedChange = null` to avoid duplicate toggle events. If both handlers fire on a checkbox tap, `toggleRequirement` runs twice and the checkbox appears unresponsive (flipped twice).

**Fix:** Follow the M3 recommended pattern — handle toggle only on the row:

```kotlin
Row(
    modifier = modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .toggleable(
            value = requirement.isAchieved,
            onValueChange = { onToggle() },
        )
        .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Checkbox(
        checked = requirement.isAchieved,
        onCheckedChange = null,
    )
    // ...
}
```

### WR-03: `setGotten` bypasses `isPurchaseEnabled` guard

**File:** `app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt:162-175`  
**Issue:** `setGotten(true)` writes `isGotten = true` without checking `ProgressWriteLogic.isPurchaseEnabled`. The purchase `Switch` is gated in UI, but the ViewModel API allows marking köpt while requirements are incomplete if called directly (future feature hook, test mistake, or refactored UI). `BadgeStateCalculator` would show GOTTEN, contradicting D-06.

**Fix:** Snapshot current `uiState` or recompute visual state before writing and return early when purchase is not enabled:

```kotlin
fun setGotten(value: Boolean) {
    if (!value) return
    viewModelScope.launch(ioDispatcher) {
        val state = /* recompute or read latest cell */
        if (!ProgressWriteLogic.isPurchaseEnabled(state.totalRequirements, state.visualState)) return@launch
        // existing upsert
    }
}
```

### WR-04: Missing category leaves badge detail in perpetual loading

**File:** `app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt:89`  
**Issue:** `isLoading = category == null` treats a missing category as a loading state. If a badge references a deleted or orphaned `categoryId`, the screen shows `CircularProgressIndicator` indefinitely instead of an error or populated fallback.

**Fix:** Distinguish initial load from missing data, e.g. `isLoading = category == null && catalogRepository has not yet emitted` vs `badgeMissing`-style branch when category is permanently absent.

## Info

### IN-01: Orphan badges silently excluded from home summary counts

**File:** `app/src/main/java/se/simmarken/domain/KidProgressSummaryCalculator.kt:22`  
**Issue:** `categoriesById[badge.categoryId] ?: continue` skips badges whose category is absent from the categories map. During a transient catalog sync gap this undercounts pågår/att köpa without any signal.

**Fix:** Log or count skipped badges in debug builds; consider treating missing category as a catalog integrity error once flows have settled.

### IN-02: `KidProgressSummaryCalculator.compute` hardcodes `kidId = 0`

**File:** `app/src/main/java/se/simmarken/domain/KidProgressSummaryCalculator.kt:37-40`  
**Issue:** The calculator returns `kidId = 0` and callers must `.copy(kidId = kid.id)`. Harmless today but easy to misuse if called without the copy.

**Fix:** Add `kidId` as a parameter to `compute` and remove the placeholder `0`.

---

_Reviewed: 2026-07-23T12:32:00Z_  
_Reviewer: Claude (gsd-code-reviewer)_  
_Depth: standard_
