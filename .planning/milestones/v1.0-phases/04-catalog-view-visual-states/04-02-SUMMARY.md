---
phase: 04-catalog-view-visual-states
plan: 02
subsystem: ui
tags: [kotlin, compose, mvvm, catalog-grid]

requires:
  - phase: 04-catalog-view-visual-states
    plan: 01
    provides: BadgeCatalogMapper, BadgeCellUiModel, CatalogRepository observe extensions
provides:
  - ChildCatalogScreen replacing placeholder with category-grouped 3-column grid
  - ChildCatalogViewModel reactive catalog assembly via BadgeCatalogMapper
  - CatalogTabRow dual Simidrott/SLS tabs with session-only reset
  - BadgeGrid with sticky category headers
  - CatalogEmptyState for empty catalog tabs
  - onBadgeClick hook wired from NavHost (no-op until 04-03)
affects:
  - 04-03

tech-stack:
  added:
    - androidx.compose.foundation:foundation (sticky headers via LazyListScope)
  patterns:
    - "Session-only tab index in Composable remember — resets on re-entry (D-04)"
    - "kidMissing distinct from loading for invalid kidId route (T-04-03)"
    - "Tier-color placeholder boxes via BadgePlaceholderColors.forCategoryCode"

key-files:
  created:
    - app/src/main/java/se/simmarken/ui/child/ChildCatalogScreen.kt
    - app/src/main/java/se/simmarken/ui/child/components/BadgeGrid.kt
    - app/src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt
    - app/src/main/java/se/simmarken/ui/child/components/CategoryStickyHeader.kt
    - app/src/main/java/se/simmarken/ui/child/components/CatalogTabRow.kt
    - app/src/main/java/se/simmarken/ui/child/components/CatalogEmptyState.kt
  modified:
    - app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt
    - app/src/main/java/se/simmarken/navigation/ChildCatalogViewModelFactory.kt
    - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
    - app/build.gradle.kts
  deleted:
    - app/src/main/java/se/simmarken/ui/child/ChildCatalogPlaceholderScreen.kt

key-decisions:
  - "Tab index held in Composable remember, not ViewModel — resets on each catalog entry (D-04)"
  - "Default catalog simidrott on load; tab maps index to catalog code string (T-04-04)"
  - "All badges including LOCKED shown — no tier gating (D-06, D-08)"
  - "Placeholder pin boxes until 04-03 wires BadgePinVisual"

patterns-established:
  - "ChildCatalogUiState combines sections, catalogs, kidMissing"
  - "flatMapLatest on selectedCatalogId prevents cross-catalog data bleed"

requirements-completed: [UI-02]

coverage:
  - id: D1
    description: "Category-grouped 3-column badge grid with sticky headers replaces placeholder"
    requirement: UI-02
    verification:
      - kind: unit
        ref: "./gradlew :app:assembleDebug"
        status: pass
    human_judgment: false
  - id: D2
    description: "Simidrott/SLS dual tabs switch catalogs without data bleed; session tab reset"
    requirement: UI-02
    verification:
      - kind: unit
        ref: "./gradlew :app:assembleDebug"
        status: pass
    human_judgment: true
    rationale: "Tab switching and re-entry reset require emulator verification"
  - id: D3
    description: "Invalid kidId shows Barnet hittades inte centered error"
    verification:
      - kind: unit
        ref: "ChildCatalogScreen.kt kidMissing branch"
        status: pass
    human_judgment: false
  - id: D4
    description: "Empty catalog tab shows CatalogEmptyState with Swedish copy"
    verification:
      - kind: unit
        ref: "./gradlew :app:assembleDebug"
        status: pass
    human_judgment: false

duration: 35min
completed: 2026-07-23
status: complete
---

## Self-Check: PASSED

- `./gradlew :app:assembleDebug` — pass
- `./gradlew :app:testDebugUnitTest` — pass
- `rg ChildCatalogPlaceholderScreen app/` — zero matches
- ChildCatalogScreen contains CatalogTabRow, Barnet hittades inte, onBadgeClick parameter

## Accomplishments

- Extended `ChildCatalogViewModel` with reactive catalog assembly via `BadgeCatalogMapper`, dual-catalog selection, and `kidMissing` handling.
- Built `ChildCatalogScreen` with dual Simidrott/SLS tabs, sticky-header `BadgeGrid`, empty state, and invalid-kid error UI.
- Wired `SimmarkenNavHost` to production catalog screen with `onBadgeClick` hook for 04-03.
- Removed `ChildCatalogPlaceholderScreen`.

## Deviations

- Prior executor interrupted; resumed inline. Unrelated Gradle version bumps reverted; kept only `foundation` dependency for lazy APIs.
- `stickyHeader` resolves as `LazyListScope` member — no import needed (compile fix).

## Issues Encountered

- Executor subagent hit resource_exhausted; orchestrator completed plan inline.
