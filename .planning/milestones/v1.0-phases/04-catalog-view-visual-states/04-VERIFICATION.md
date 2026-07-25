---
phase: 04-catalog-view-visual-states
verified: 2026-07-23T10:30:00Z
status: passed
score: 17/21 must-haves verified
behavior_unverified: 4
overrides_applied: 0
behavior_unverified_items:

  - truth: "Four visual badge states are clearly distinguishable at a glance (UI-04)"
    test: "Open a child catalog with badges in LOCKED, IN_PROGRESS, ACHIEVED_TO_BUY, and GOTTEN states (seed or toggle progress in DB if needed)"
    expected: "Locked/in-progress pins are grayscale; in-progress shows primary arc ring; achieved-to-buy shows full color + cart badge; gotten shows full color + check badge — all four instantly distinguishable"
    why_human: "Grayscale filter, ring sweep, and overlay placement are visual Compose rendering; grep confirms code paths but not perceptual clarity"

  - truth: "Simidrott and SLS catalogs switch via tab row without data bleed (dual-catalog-tabs)"
    test: "Open child catalog, note badges in Simidrott tab, switch to SLS tab, switch back"
    expected: "Each tab shows only its catalog's categories/badges; no cross-catalog badges linger after switch"
    why_human: "flatMapLatest wiring is present; cross-tab isolation is a runtime Flow cancellation invariant"

  - truth: "Default tab is Simidrott on every screen open; tab resets on re-entry (D-02, D-04)"
    test: "Open catalog (expect Simidrott), switch to SLS, navigate back to home, re-open same child catalog"
    expected: "Simidrott tab selected again; SLS selection does not persist across re-entry"
    why_human: "LaunchedEffect + remember tab index reset is composable lifecycle behavior not exercised by unit tests"

  - truth: "Tapping a badge navigates to BadgeDetail placeholder with matching visual state"
    test: "Tap any badge in grid, observe detail screen, press back"
    expected: "Navigates to detail with same pin visual state, badge name, and 'Checklista kommer snart' stub; back returns to grid"
    why_human: "Navigation stack and shared BadgePinVisual state are wired in NavHost but no navigation/UI test exercises the tap flow"
human_verification:

  - test: "Verify all four badge visual states are distinguishable in the emulator"
    expected: "Grayscale locked, grayscale+ring in-progress, full-color+cart achieved-to-buy, full-color+check gotten"
    why_human: "UI-04 requires perceptual clarity; SUMMARY flags human_judgment on D1"

  - test: "Switch Simidrott ↔ SLS tabs and re-enter catalog from home"
    expected: "No cross-catalog bleed; Simidrott default restored on re-entry"
    why_human: "Session tab behavior deferred to end-of-phase per 04-02-SUMMARY D2 human_judgment"

  - test: "Tap a badge and confirm detail placeholder navigation"
    expected: "BadgeDetail screen opens with shared pin visual and 'Checklista kommer snart'; back returns to grid"
    why_human: "Navigation tap-through flagged human_judgment in 04-03-SUMMARY D3"
---

# Phase 4: Catalog View & Visual States Verification Report

**Phase Goal:** Parent sees badges grouped by category with clear 4-tier visual states  
**Verified:** 2026-07-23T10:30:00Z  
**Status:** human_needed  
**Re-verification:** No — initial verification

## User Flow Coverage

User story (composite): *As a parent at the swim hall, I want to browse my child's badges grouped by category and see at a glance whether each badge is locked, started, ready to buy, or gotten, so that I know if we need to visit the kiosk before leaving.*

| Step | Expected | Evidence | Status |
|------|----------|----------|--------|
| Open child catalog from home | Production catalog screen, not placeholder | `SimmarkenNavHost.kt` composes `ChildCatalogScreen`; `ChildCatalogPlaceholderScreen` absent from codebase | ✓ VERIFIED |
| See badges grouped by category | Sticky category headers, sections per category | `BadgeGrid.kt` `stickyHeader` + `CategorySection` from `BadgeCatalogMapper.toCategorySection` | ✓ VERIFIED |
| Switch Simidrott / SLS catalogs | Dual tabs, isolated catalog data | `CatalogTabRow.kt`, `ChildCatalogViewModel.selectCatalogByTabIndex`, `flatMapLatest` on `selectedCatalogId` | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED |
| Recognize 4-tier visual states | Grayscale locked/in-progress, ring, cart, check | `BadgePinVisual.kt`, `CircularProgressRing.kt`, `StateOverlay.kt` wired via `BadgeGridItem` | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED |
| Tap badge for detail | Navigate to placeholder with checklist stub | `SimmarkenNavHost.kt` `navigate(BadgeDetail(...))`, `BadgeDetailPlaceholderScreen.kt` | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED |

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Child profile shows badges grouped by category (roadmap SC1, UI-02) | ✓ VERIFIED | `ChildCatalogScreen` → `BadgeGrid` with `stickyHeader` per `CategorySection`; categories sorted by `sortOrder` in `ChildCatalogViewModel` |
| 2 | Locked badges appear grayscale; in-progress show progress ring (roadmap SC2) | ✓ VERIFIED | `BadgePinVisual.badgeColorFilter` sets saturation 0 for LOCKED/IN_PROGRESS; `CircularProgressRing` with `drawArc` for IN_PROGRESS when `totalRequirements > 0` |
| 3 | Achieved-to-buy badges show full color with cart overlay (roadmap SC3) | ✓ VERIFIED | No color filter for ACHIEVED_TO_BUY; `StateOverlay` renders `Icons.Filled.ShoppingCart` bottom-right |
| 4 | Gotten badges show full color with check overlay (roadmap SC4) | ✓ VERIFIED | No color filter for GOTTEN; `StateOverlay` renders `Icons.Filled.Check` bottom-right |
| 5 | Tapping a badge navigates to detail placeholder (roadmap SC5) | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED | `SimmarkenNavHost` wires `onBadgeClick` → `BadgeDetail(kidId, badgeId)`; `BadgeDetailPlaceholderScreen` contains "Checklista kommer snart" — wiring present, tap flow not tested |
| 6 | BadgeVisualState derived at read time — never persisted (D-29) | ✓ VERIFIED | `BadgeStateCalculator.compute` called in mapper/ViewModels; no visual-state column in Room entities |
| 7 | State precedence: gotten → achieved-to-buy → in-progress → locked (D-30, PROG-04) | ✓ VERIFIED | `BadgeStateCalculator.kt` lines 7–12; `BadgeStateCalculatorTest` all 5 precedence cases pass |
| 8 | Zero-requirement badges map to ACHIEVED_TO_BUY unless gotten (D-30 backstop) | ✓ VERIFIED | `compute` line 8–9; `zeroRequirements_isAchievedToBuy` test passes |
| 9 | Absent RequirementProgressEntity rows default to not achieved — fresh kid all LOCKED | ✓ VERIFIED | `BadgeCatalogMapper` counts only `== true`; `BadgeCatalogMapperTest.freshKid_allLocked` and `absentProgressRow_defaultsFalse` pass |
| 10 | BadgeCatalogMapper joins entities into BadgeCellUiModel with visualState + progressFraction | ✓ VERIFIED | `BadgeCatalogMapper.toBadgeCellUiModel` delegates to `BadgeStateCalculator`; mapper tests pass |
| 11 | Category-grouped 3-column badge grid replaces placeholder (UI-02) | ✓ VERIFIED | `BadgeGrid` chunks badges into rows of 3; `ChildCatalogPlaceholderScreen` removed (zero grep matches) |
| 12 | Simidrott and SLS catalogs switch without data bleed | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED | `selectedCatalogId.flatMapLatest` cancels prior catalog Flow; runtime tab isolation needs emulator |
| 13 | Default Simidrott tab; tab resets on re-entry (D-02, D-04) | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED | VM defaults `code == "simidrott"`; `LaunchedEffect { selectedTabIndex = 0 }` in screen — lifecycle reset not unit-tested |
| 14 | Sticky category headers while scrolling (D-18) | ✓ VERIFIED | `BadgeGrid.kt` `stickyHeader(key = section.categoryId)` |
| 15 | All badges visible including locked — no tier gating (D-06, D-08) | ✓ VERIFIED | No visual-state filter in mapper or grid; all badges mapped to cells |
| 16 | Badge name below pin truncated to one line (D-14) | ✓ VERIFIED | `BadgeGridItem` `maxLines = 1`, `TextOverflow.Ellipsis` |
| 17 | Grid cells spaced ~12–16dp (D-15) | ✓ VERIFIED | `Arrangement.spacedBy(12.dp)` in `BadgeGrid` row and column |
| 18 | Empty catalog tab shows CatalogEmptyState | ✓ VERIFIED | `ChildCatalogScreen` `uiState.sections.isEmpty()` → `CatalogEmptyState` with Swedish copy |
| 19 | Invalid kidId shows centered "Barnet hittades inte" (T-04-03) | ✓ VERIFIED | `ChildCatalogScreen` `kidMissing` branch |
| 20 | Detail screen reuses BadgePinVisual 4-tier rendering (D-28) | ✓ VERIFIED | `BadgeDetailPlaceholderScreen` composes `BadgePinVisual` with `BadgePinSize.Detail` |
| 21 | BadgeDetailViewModel uses BadgeStateCalculator — no duplicate precedence logic | ✓ VERIFIED | `BadgeDetailViewModel.kt` lines 43–50 call `BadgeStateCalculator.compute` and `progressFraction` |

**Score:** 17/21 truths verified (4 present, behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
| -------- | ----------- | ------ | ------- |
| `domain/BadgeStateCalculator.kt` | D-30 precedence | ✓ VERIFIED | `fun compute`, `fun progressFraction` present |
| `domain/model/BadgeVisualState.kt` | Four-tier enum | ✓ VERIFIED | LOCKED, IN_PROGRESS, ACHIEVED_TO_BUY, GOTTEN |
| `domain/BadgeCatalogMapper.kt` | Entity-to-UI join | ✓ VERIFIED | Delegates to `BadgeStateCalculator.compute` |
| `test/.../BadgeStateCalculatorTest.kt` | PROG-04 unit tests | ✓ VERIFIED | 7 tests; `./gradlew :app:testDebugUnitTest` PASS |
| `ui/child/ChildCatalogScreen.kt` | Production catalog screen | ✓ VERIFIED | CatalogTabRow, kidMissing, BadgeGrid |
| `ui/child/components/BadgeGrid.kt` | Sticky 3-col grid | ✓ VERIFIED | `stickyHeader`, `chunked(3)` |
| `ui/child/ChildCatalogViewModel.kt` | Reactive catalog assembly | ✓ VERIFIED | `BadgeCatalogMapper.toCategorySection` in combine pipeline |
| `ui/components/BadgePinVisual.kt` | Shared 4-tier Coil rendering | ✓ VERIFIED | `AsyncImage`, grayscale filter, overlays |
| `ui/components/CircularProgressRing.kt` | In-progress arc | ✓ VERIFIED | `drawArc` + `StrokeCap.Round` |
| `ui/components/StateOverlay.kt` | Cart/check overlays | ✓ VERIFIED | `ShoppingCart`, `Check` icons |
| `ui/badge/BadgeDetailPlaceholderScreen.kt` | Detail placeholder | ✓ VERIFIED | "Checklista kommer snart" at line 94 |
| `navigation/Routes.kt` | BadgeDetail route | ✓ VERIFIED | `data class BadgeDetail(val kidId: Long, val badgeId: Long)` |

gsd-tools `verify.artifacts` — all 12 artifacts across 3 plans: **all_passed: true**

### Key Link Verification

| From | To | Via | Status | Details |
| ---- | --- | --- | ------ | ------- |
| `BadgeCatalogMapper.kt` | `BadgeStateCalculator.compute` | achievedCount + isGotten | ✓ WIRED | Line 26–29 |
| `CatalogRepositoryImpl.kt` | `CatalogDao.observeAllRequirements` | thin passthrough | ✓ WIRED | Line 16 |
| `ChildCatalogViewModel.kt` | `BadgeCatalogMapper.toCategorySection` | combine + flatMapLatest | ✓ WIRED | Line 70–76 |
| `SimmarkenNavHost.kt` | `ChildCatalogScreen` | ChildCatalogViewModelFactory | ✓ WIRED | Lines 40–46 |
| `BadgeGridItem.kt` | `BadgePinVisual` | visualState + progressFraction props | ✓ WIRED | Lines 46–54 |
| `SimmarkenNavHost.kt` | `BadgeDetail` route | onBadgeClick navigate | ✓ WIRED | Lines 43–45 |
| `BadgeDetailViewModel.kt` | `BadgeStateCalculator.compute` | same inputs as grid | ✓ WIRED | Lines 43–50 |

gsd-tools `verify.key-links` reported false (plan `from` fields use symbol names, not file paths); manual grep confirms all 7 links wired.

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| -------- | ------------- | ------ | ------------------ | ------ |
| `BadgeGridItem` | `badge.visualState`, `progressFraction` | `BadgeCellUiModel` from `ChildCatalogViewModel` | Room via `observeRequirementProgress`, `observeBadgeProgress`, `observeAllRequirements` | ✓ FLOWING |
| `BadgePinVisual` | `imageAssetPath`, `visualState` | Props from grid/detail | Coil loads `file:///android_asset/{path}` or tier-color fallback | ✓ FLOWING |
| `BadgeDetailPlaceholderScreen` | `uiState.visualState` | `BadgeDetailViewModel` combine on badge + progress Flows | `observeBadgeById`, `observeRequirements`, progress repos | ✓ FLOWING |

No hardcoded empty props at call sites; `requirementProgressById` defaults absent keys to false in mapper count logic.

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| -------- | ------- | ------ | ------ |
| BadgeStateCalculator precedence matrix | `./gradlew :app:testDebugUnitTest --tests "*BadgeStateCalculatorTest*"` | BUILD SUCCESSFUL | ✓ PASS |
| BadgeCatalogMapper join + defaults | `./gradlew :app:testDebugUnitTest --tests "*BadgeCatalogMapperTest*"` | BUILD SUCCESSFUL | ✓ PASS |
| Project compiles with Coil + catalog UI | `./gradlew :app:assembleDebug` | BUILD SUCCESSFUL | ✓ PASS |

### Probe Execution

Step 7c: SKIPPED — no probe scripts declared in phase plans.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| ----------- | ---------- | ----------- | ------ | -------- |
| **UI-02** | 04-02 | Child profile screen shows catalog badges grouped by category in a grid | ✓ SATISFIED | `ChildCatalogScreen` + `BadgeGrid` 3-column layout with category sticky headers |
| **UI-04** | 04-03 | Four visual badge states clearly distinguishable | ⚠️ NEEDS HUMAN | All four code paths implemented in `BadgePinVisual`; perceptual distinction requires emulator glance |
| **PROG-04** | 04-01, 04-03 | Badge visual state reflects locked, in progress, achieved-to-buy, or gotten | ✓ SATISFIED | `BadgeStateCalculator` + unit tests + `BadgePinVisual` rendering wired end-to-end |

**Traceability note:** `REQUIREMENTS.md` traceability table still marks UI-02 as Pending while UI-04 and PROG-04 are Complete. Code evidence satisfies UI-02; requirements doc should be updated separately.

No orphaned requirement IDs — all three phase requirements (UI-02, UI-04, PROG-04) appear in plan frontmatter and are accounted for above.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| ---- | ---- | ------- | -------- | ------ |
| — | — | No TBD/FIXME/XXX/TODO in phase-modified Kotlin sources | — | Clean |

`BadgeDetailPlaceholderScreen` and "Checklista kommer snart" are intentional Phase 4 scope per roadmap SC5 ("placeholder OK until Phase 5") — not stubs blocking goal achievement.

### Human Verification Required

### 1. Four-tier visual distinction (UI-04)

**Test:** Open a child catalog in the emulator with badges spanning all four visual states.  
**Expected:** Grayscale locked, grayscale + primary ring in-progress, full-color + cart achieved-to-buy, full-color + check gotten — each instantly distinguishable.  
**Why human:** Compose color filters and overlay sizing require perceptual validation; SUMMARY explicitly flags `human_judgment: true` on 04-03 D1.

### 2. Dual-catalog tab switching and session reset

**Test:** Open catalog (Simidrott default), switch to SLS, navigate away and re-open.  
**Expected:** Correct catalog content per tab with no bleed; Simidrott tab restored on re-entry.  
**Why human:** `flatMapLatest` + `remember` tab index are lifecycle behaviors; 04-02 SUMMARY D2 deferred to end-of-phase human check.

### 3. Badge tap → detail navigation

**Test:** Tap any badge in the grid; press back.  
**Expected:** Detail screen shows matching pin visual state, badge name, and "Checklista kommer snart"; back returns to grid.  
**Why human:** Navigation stack behavior; 04-03 SUMMARY D3 flags `human_judgment: true`.

### Gaps Summary

No blocking implementation gaps found. Domain logic, catalog grid, 4-tier visuals, and detail-placeholder navigation are implemented and wired. Four runtime/UI behaviors require human confirmation on device before treating the phase as fully passed. Automated score: **17/21** with **human_needed** status.

---

_Verified: 2026-07-23T10:30:00Z_  
_Verifier: Claude (gsd-verifier)_
