---
phase: 03-child-profiles-home
plan: 02
subsystem: ui
tags: [android, compose, navigation, child-cards, empty-state, catalog-placeholder]

requires:
  - phase: 03-child-profiles-home
    plan: 01
    provides: Kid avatars, FAB add-child flow, HomeViewModel kid list, observeById
provides:
  - Production ChildCard and EmptyState components per UI-SPEC
  - Kid-first home with LazyColumn cards and Swedish empty state
  - Type-safe ChildCatalog(kidId) navigation to placeholder screen
affects:
  - 03-03-edit-delete
  - 04-catalog-badge-grid

tech-stack:
  added: [material-icons-extended]
  patterns:
    - Card body clickable navigates to ChildCatalog; overflow deferred to 03-03
    - ChildCatalogViewModel observes kid name via Room Flow stateIn

key-files:
  created:
    - app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt
    - app/src/main/java/se/simmarken/ui/home/components/EmptyState.kt
    - app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt
    - app/src/main/java/se/simmarken/ui/child/ChildCatalogPlaceholderScreen.kt
    - app/src/main/java/se/simmarken/navigation/ChildCatalogViewModelFactory.kt
  modified:
    - app/src/main/java/se/simmarken/ui/home/HomeScreen.kt
    - app/src/main/java/se/simmarken/navigation/Routes.kt
    - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
    - app/build.gradle.kts

key-decisions:
  - "ChildCard uses single clickable Row without overflow menu until 03-03 per plan scope"
  - "ChildCatalog route added in Task 1 commit to satisfy HomeScreen navigation compile dependency"

patterns-established:
  - "ChildCatalogViewModelFactory mirrors HomeViewModelFactory manual DI pattern"
  - "Invalid or deleted kidId shows empty app bar title with graceful back navigation"

requirements-completed: [KIDS-03, UI-01]

duration: 18min
completed: 2026-07-23
---

# Phase 3 Plan 02: Home Polish & Catalog Navigation Summary

**Kid-first home with full-width ElevatedCards, Swedish empty state, and type-safe navigation to a catalog placeholder showing the child's name**

## Performance

- **Duration:** 18 min
- **Started:** 2026-07-23T06:12:00Z
- **Completed:** 2026-07-23T06:30:00Z
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments

- `ChildCard` with 72dp min-height ElevatedCard, avatar + name only (no progress counts per D-02)
- `EmptyState` with Swedish copy and ChildCare icon; FAB remains visible per D-03
- `HomeScreen` shows empty vs populated states with UI-SPEC spacing (16dp horizontal, 8dp gap, 88dp bottom inset)
- `ChildCatalog(kidId)` type-safe route with placeholder screen and back stack per D-09
- `ChildCatalogViewModel` loads child name from `observeById` for app bar title

## Task Commits

Each task was committed atomically:

1. **Task 1: ChildCard, EmptyState, and production HomeScreen layout** — `dbbb90a` (feat)
2. **Task 2: ChildCatalog route and placeholder screen** — `60cea46` (feat)

## Files Created/Modified

- `app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt` — Full-width tappable card with KidAvatar and name
- `app/src/main/java/se/simmarken/ui/home/components/EmptyState.kt` — Centered Swedish empty state
- `app/src/main/java/se/simmarken/ui/home/HomeScreen.kt` — Empty/populated branches, navController, ChildCard list
- `app/src/main/java/se/simmarken/navigation/Routes.kt` — `ChildCatalog(kidId: Long)` serializable route
- `app/src/main/java/se/simmarken/ui/child/ChildCatalogViewModel.kt` — childName StateFlow from repository
- `app/src/main/java/se/simmarken/ui/child/ChildCatalogPlaceholderScreen.kt` — Scaffold with back and placeholder copy
- `app/src/main/java/se/simmarken/navigation/ChildCatalogViewModelFactory.kt` — Manual DI factory
- `app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt` — ChildCatalog composable destination
- `app/build.gradle.kts` — material-icons-extended for Outlined.ChildCare

## Decisions Made

- ChildCard omits overflow menu in this plan — 03-03 adds edit/delete per D-08
- ChildCatalog route committed in Task 1 so HomeScreen navigation compiles before NavHost destination in Task 2

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added material-icons-extended dependency**
- **Found during:** Task 1 (EmptyState compile)
- **Issue:** `Icons.Outlined.ChildCare` unresolved with material-icons-core only
- **Fix:** Added `implementation("androidx.compose.material:material-icons-extended")` to `app/build.gradle.kts`
- **Files modified:** `app/build.gradle.kts`
- **Verification:** `./gradlew :app:assembleDebug` succeeds
- **Committed in:** `dbbb90a`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Required for UI-SPEC empty state icon. No scope creep.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Home layout and catalog navigation complete; ready for 03-03 (overflow edit/delete on ChildCard)
- Phase 4 can replace `ChildCatalogPlaceholderScreen` content while keeping `ChildCatalog` route stable
- Manual UAT: empty state with FAB, add children, tap card → see name in app bar and "Simmärken kommer snart" → back preserves list

## Self-Check: PASSED

- FOUND: `app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt`
- FOUND: `app/src/main/java/se/simmarken/ui/home/components/EmptyState.kt`
- FOUND: `app/src/main/java/se/simmarken/ui/child/ChildCatalogPlaceholderScreen.kt`
- FOUND: `dbbb90a` (Task 1)
- FOUND: `60cea46` (Task 2)

---
*Phase: 03-child-profiles-home*
*Completed: 2026-07-23*
