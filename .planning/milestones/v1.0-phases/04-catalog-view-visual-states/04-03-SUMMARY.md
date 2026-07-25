---
phase: 04-catalog-view-visual-states
plan: 03
subsystem: ui
tags: [kotlin, compose, coil, badge-visuals, navigation]

requires:
  - phase: 04-catalog-view-visual-states
    plan: 02
    provides: ChildCatalogScreen, BadgeGridItem, onBadgeClick hook
provides:
  - BadgePinVisual shared 4-tier pin rendering with Coil
  - CircularProgressRing for IN_PROGRESS state
  - StateOverlay cart/check overlays
  - BadgeDetail route and placeholder screen
  - Grid-to-detail navigation with shared BadgeStateCalculator
affects:
  - 05-checklist-progress

tech-stack:
  added:
    - io.coil-kt.coil3:coil-compose:3.5.0
  patterns:
    - "BadgePinVisual shared between grid (BadgePinSize.Grid) and detail (BadgePinSize.Detail)"
    - "BadgeStateCalculator.compute reused in BadgeDetailViewModel — no duplicate precedence logic"
    - "Coil file:///android_asset/ URIs with size(96) grid / size(320) detail"

key-files:
  created:
    - app/src/main/java/se/simmarken/ui/components/BadgePinVisual.kt
    - app/src/main/java/se/simmarken/ui/components/CircularProgressRing.kt
    - app/src/main/java/se/simmarken/ui/components/StateOverlay.kt
    - app/src/main/java/se/simmarken/ui/badge/BadgeDetailPlaceholderScreen.kt
    - app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt
    - app/src/main/java/se/simmarken/domain/model/BadgeDetailUiState.kt
    - app/src/main/java/se/simmarken/navigation/BadgeDetailViewModelFactory.kt
  modified:
    - gradle/libs.versions.toml
    - app/build.gradle.kts
    - app/src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt
    - app/src/main/java/se/simmarken/navigation/Routes.kt
    - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
    - app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt
    - app/src/main/java/se/simmarken/domain/repository/CatalogRepository.kt
    - app/src/main/java/se/simmarken/domain/repository/CatalogRepositoryImpl.kt

key-decisions:
  - "compileSdk 36 required by coil-compose 3.5.0 AAR metadata — bumped from 35"
  - "Kotlin resolution strategy pins transitive kotlin-* artifacts to project compiler version"
  - "observeCategoryById added for reactive category code in BadgeDetailViewModel"
  - "Invalid badgeId shows Märket hittades inte — mitigates T-04-06 route spoofing"

patterns-established:
  - "Four-tier visual state matrix: grayscale locked/in-progress, full-color buy/gotten with overlays"
  - "BadgeDetail placeholder is view-only; Phase 5 replaces checklist stub in place"

requirements-completed: [UI-04, PROG-04]

coverage:
  - id: D1
    description: "Four-tier badge visuals in grid via BadgePinVisual (grayscale, ring, cart, check)"
    requirement: UI-04
    verification:
      - kind: unit
        ref: "./gradlew :app:assembleDebug"
        status: pass
    human_judgment: true
    rationale: "Visual distinction of all four states requires emulator glance check"
  - id: D2
    description: "Visual state derived via BadgeStateCalculator in grid and detail"
    requirement: PROG-04
    verification:
      - kind: unit
        ref: "./gradlew :app:testDebugUnitTest"
        status: pass
    human_judgment: false
  - id: D3
    description: "Badge tap navigates to BadgeDetail placeholder with Checklista kommer snart"
    verification:
      - kind: unit
        ref: "BadgeDetailPlaceholderScreen.kt contains Checklista kommer snart"
        status: pass
    human_judgment: true
    rationale: "Navigation flow and shared pin state require emulator tap-through"

duration: 45min
completed: 2026-07-23
status: complete
---

# Phase 04 Plan 03: Badge Visuals & Detail Placeholder Summary

**Coil-powered 4-tier BadgePinVisual with progress ring and cart/check overlays, plus BadgeDetail placeholder navigation reusing shared state calculation**

## Performance

- **Duration:** 45 min
- **Tasks:** 3
- **Files modified:** 15

## Accomplishments

- Added `BadgePinVisual` with Coil 3 `AsyncImage`, grayscale locked/in-progress, `StateOverlay` cart/check, and tier-color fallback.
- Implemented `CircularProgressRing` with `drawArc` / `StrokeCap.Round` for IN_PROGRESS badges.
- Wired `BadgeGridItem` to shared visual component with Swedish accessibility state labels.
- Added `BadgeDetail` route, `BadgeDetailViewModel` (via `BadgeStateCalculator`), and placeholder screen with `Checklista kommer snart`.
- Connected catalog grid badge tap to detail navigation with back stack return.

## Task Commits

1. **Task 1: End-to-end 4-tier BadgePinVisual in grid** - `98394b9` (feat)
2. **Task 2: Complete CircularProgressRing and all four visual states** - `6a994d5` (feat)
3. **Task 3: BadgeDetail placeholder route, screen, and grid navigation** - `b0eee96` (feat)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] compileSdk 36 for Coil 3.5.0**
- **Found during:** Task 1
- **Issue:** coil-compose 3.5.0 AAR metadata requires compileSdk 36; project was on 35
- **Fix:** Bumped `compileSdk` to 36 in `app/build.gradle.kts`
- **Files modified:** `app/build.gradle.kts`
- **Committed in:** `98394b9`

**2. [Rule 3 - Blocking] Kotlin stdlib version mismatch from Coil transitive deps**
- **Found during:** Task 1
- **Issue:** Coil pulled kotlin-stdlib 2.4.0 incompatible with project Kotlin 2.1.21 compiler
- **Fix:** Added `resolutionStrategy` to align all `org.jetbrains.kotlin` artifacts to project version
- **Files modified:** `app/build.gradle.kts`
- **Committed in:** `98394b9`

**3. [Rule 2 - Missing Critical] observeCategoryById for detail ViewModel**
- **Found during:** Task 3
- **Issue:** BadgeDetailViewModel needs reactive category code; only suspend `findCategoryById` existed
- **Fix:** Added `observeCategoryById` to CatalogDao and CatalogRepository
- **Files modified:** `CatalogDao.kt`, `CatalogRepository.kt`, `CatalogRepositoryImpl.kt`
- **Committed in:** `b0eee96`

## Issues Encountered

None beyond deviations above.

## Self-Check: PASSED

- `./gradlew :app:assembleDebug` — pass
- `./gradlew :app:testDebugUnitTest` — pass
- `BadgePinVisual.kt` imports `coil3.compose.AsyncImage` — verified
- `CircularProgressRing.kt` contains `drawArc` with `StrokeCap.Round` — verified
- `Routes.kt` contains `BadgeDetail` — verified
- `BadgeDetailPlaceholderScreen.kt` contains `Checklista kommer snart` — verified
- Commits `98394b9`, `6a994d5`, `b0eee96` — found in git log

---
*Phase: 04-catalog-view-visual-states*
*Completed: 2026-07-23*
