---
phase: 04-catalog-view-visual-states
reviewed: 2026-07-23T10:30:00Z
depth: standard
files_reviewed: 26
files_reviewed_list:
  - app/build.gradle.kts
  - gradle/libs.versions.toml
  - app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt
  - app/src/main/java/se/simmarken/domain/BadgeCatalogMapper.kt
  - app/src/main/java/se/simmarken/domain/BadgeStateCalculator.kt
  - app/src/main/java/se/simmarken/domain/model/BadgeDetailUiState.kt
  - app/src/main/java/se/simmarken/domain/model/BadgeVisualState.kt
  - app/src/main/java/se/simmarken/domain/model/CatalogUiModels.kt
  - app/src/main/java/se/simmarken/domain/repository/CatalogRepository.kt
  - app/src/main/java/se/simmarken/domain/repository/CatalogRepositoryImpl.kt
  - app/src/main/java/se/simmarken/navigation/BadgeDetailViewModelFactory.kt
  - app/src/main/java/se/simmarken/navigation/ChildCatalogViewModelFactory.kt
  - app/src/main/java/se/simmarken/navigation/Routes.kt
  - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
  - app/src/main/java/se/simmarken/ui/badge/BadgeDetailPlaceholderScreen.kt
  - app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt
  - app/src/main/java/se/simmarken/ui/child/ChildCatalogScreen.kt
  - app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt
  - app/src/main/java/se/simmarken/ui/child/components/BadgeGrid.kt
  - app/src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt
  - app/src/main/java/se/simmarken/ui/child/components/CatalogEmptyState.kt
  - app/src/main/java/se/simmarken/ui/child/components/CatalogTabRow.kt
  - app/src/main/java/se/simmarken/ui/child/components/CategoryStickyHeader.kt
  - app/src/main/java/se/simmarken/ui/components/BadgePinVisual.kt
  - app/src/main/java/se/simmarken/ui/components/CircularProgressRing.kt
  - app/src/main/java/se/simmarken/ui/components/StateOverlay.kt
findings:
  critical: 1
  warning: 5
  info: 2
  total: 8
status: issues_found
---

# Phase 4: Code Review Report

**Reviewed:** 2026-07-23T10:30:00Z
**Depth:** standard
**Files Reviewed:** 26
**Status:** issues_found

## Summary

Phase 04 delivers the planned domain foundation (`BadgeStateCalculator`, `BadgeCatalogMapper`), reactive catalog grid, four-tier `BadgePinVisual` rendering with Coil, and `BadgeDetail` navigation. Core state precedence (D-30) and shared visual components are implemented correctly and covered by unit tests.

One **blocker** was found: catalog tab selection is split between ViewModel state (survives rotation) and local `remember` state (resets on configuration change), causing the tab indicator to lie about which catalog is displayed after rotation. Several warnings cover Flow side effects, duplicated state-derivation logic between grid and detail, and edge-case empty-catalog handling.

## Critical Issues

### CR-01: Tab indicator desyncs from selected catalog after configuration change

**File:** `app/src/main/java/se/simmarken/ui/child/ChildCatalogScreen.kt:38-42`
**Issue:** `selectedTabIndex` is held in `remember { mutableIntStateOf(0) }`, which resets to `0` on every configuration change (rotation, locale change, etc.). `ChildCatalogViewModel.selectedCatalogId` lives in the ViewModel and survives rotation. After the user switches to the SLS tab and rotates the device, the tab row highlights Simidrott (index 0) while the grid still shows SLS badges. This violates D-02/D-04 ("default Simidrott on screen open") in the common rotation case and misleads the parent about which catalog they are viewing.

**Fix:** Derive tab selection from ViewModel state instead of local `remember`. Expose `selectedCatalogId` (or catalog `code`) in `ChildCatalogUiState` and compute the tab index from it:

```kotlin
// ChildCatalogUiState
data class ChildCatalogUiState(
    val sections: List<CategorySection> = emptyList(),
    val catalogs: List<CatalogEntity> = emptyList(),
    val selectedCatalogId: Long? = null,
    val kidMissing: Boolean = false,
)

// ChildCatalogScreen — remove local selectedTabIndex
val selectedTabIndex = uiState.catalogs
    .sortedBy { it.sortOrder }
    .indexOfFirst { it.id == uiState.selectedCatalogId }
    .coerceAtLeast(0)
```

## Warnings

### WR-01: Mutable state updated inside `combine` transform

**File:** `app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt:88-92`
**Issue:** `selectedCatalogId.update { … }` runs inside the `combine { … }` lambda that feeds `stateIn`. Flow transforms should be pure; mutating upstream state during collection can cause extra emissions, complicate testing, and is fragile under Flow operator changes.

**Fix:** Initialize the default catalog outside the transform, e.g. with a dedicated `init` block or `onEach` on `catalogRepository.observeCatalogs()`:

```kotlin
init {
    viewModelScope.launch {
        catalogRepository.observeCatalogs()
            .collect { catalogs ->
                if (selectedCatalogId.value == null && catalogs.isNotEmpty()) {
                    selectedCatalogId.value =
                        catalogs.find { it.code == "simidrott" }?.id ?: catalogs.first().id
                }
            }
    }
}
```

Then keep the outer `combine` pure (no side effects in the lambda).

### WR-02: Badge detail duplicates grid state derivation instead of reusing `BadgeCatalogMapper`

**File:** `app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt:32-54`
**Issue:** Visual state, `achievedCount`, and `progressFraction` are computed inline with the same logic as `BadgeCatalogMapper.toBadgeCellUiModel`. Plan 04-03 requires detail to reuse the same 4-tier rendering as the grid (D-28); divergent copies risk grid and detail showing different states after a future change.

**Fix:** Reuse the mapper:

```kotlin
val cell = BadgeCatalogMapper.toBadgeCellUiModel(
    badge = badge,
    categoryCode = category?.code.orEmpty(),
    requirements = requirements,
    requirementProgressById = requirementProgressById,
    isGotten = isGotten,
)
BadgeDetailUiState(
    nameSv = cell.nameSv,
    imageAssetPath = cell.imageAssetPath,
    categoryCode = cell.categoryCode,
    visualState = cell.visualState,
    progressFraction = cell.progressFraction,
    achievedCount = cell.achievedCount,
    totalRequirements = cell.totalRequirements,
    badgeMissing = false,
    isLoading = category == null,
)
```

### WR-03: Tab tap can update UI without changing catalog when target catalog is missing

**File:** `app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt:108-116`
**Issue:** `selectCatalogByTabIndex` updates the tab index in the screen (`selectedTabIndex = index`) before calling the ViewModel. If the requested catalog code (`"sls"`) is not in `uiState.value.catalogs` (stale read or seed gap), `find` returns null, `selectCatalog` is never called, and the tab highlight changes while badge data stays on the previous catalog.

**Fix:** Return a `Boolean` from `selectCatalogByTabIndex` indicating success, and only update `selectedTabIndex` in the screen when the ViewModel confirms the switch. Alternatively, derive tab index from ViewModel state (see CR-01 fix), which eliminates this class of desync.

### WR-04: Detail screen renders default LOCKED state while `isLoading` is true

**File:** `app/src/main/java/se/simmarken/ui/badge/BadgeDetailPlaceholderScreen.kt:70-87`
**Issue:** `BadgeDetailUiState` initial value and the loading branch set `isLoading = true` with `visualState = LOCKED`, but the screen never gates on `isLoading`. On slow Room emissions, the user briefly sees a grayscale locked pin before the correct state appears — misleading at the swim hall.

**Fix:** Show a centered progress indicator (or skeleton) while `uiState.isLoading`, and render `BadgePinVisual` only when loading is complete:

```kotlin
when {
    uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
    else -> Column { BadgePinVisual(...) }
}
```

### WR-05: Catalog with empty categories does not show `CatalogEmptyState`

**File:** `app/src/main/java/se/simmarken/ui/child/ChildCatalogScreen.kt:85-86`
**Issue:** Empty state is gated on `uiState.sections.isEmpty()`. If the active catalog has categories but every category has zero badges, `sections` is non-empty (headers only) and the user sees blank sticky headers instead of the "Inga simmärken" empty state.

**Fix:** Treat a catalog as empty when all sections have no badges:

```kotlin
val isCatalogEmpty = uiState.sections.all { it.badges.isEmpty() }
if (isCatalogEmpty) CatalogEmptyState(...) else BadgeGrid(...)
```

## Info

### IN-01: Duplicated `badgeStateLabel` helper

**File:** `app/src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt:21-26`, `app/src/main/java/se/simmarken/ui/badge/BadgeDetailPlaceholderScreen.kt:27-32`
**Issue:** Identical private `badgeStateLabel` functions exist in two files. A single shared utility (e.g. `BadgeVisualState.labelSv`) would prevent copy drift when labels change for i18n.

**Fix:** Move to a shared function on `BadgeVisualState` or a small `ui` util.

### IN-02: Null-image placeholder omits badge initials (UI-SPEC deviation)

**File:** `app/src/main/java/se/simmarken/ui/components/BadgePinVisual.kt:114-129`
**Issue:** UI-SPEC calls for a tier-color box with badge initials or short code when `imageAssetPath` is null. `BadgePinPlaceholder` renders only a colored `Box` with no text. Five Simidrott badges rely on this fallback.

**Fix:** Add centered `Text` with badge initials inside `BadgePinPlaceholder` (requires passing `nameSv` or a short code into `BadgePinVisual`).

---

_Reviewed: 2026-07-23T10:30:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
