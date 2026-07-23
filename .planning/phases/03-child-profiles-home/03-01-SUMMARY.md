---
phase: 03-child-profiles-home
plan: 01
subsystem: ui
tags: [android, compose, room, mvvm, kid-crud, bottom-sheet]

requires:
  - phase: 01-android-foundation-database
    provides: KidEntity, KidDao, KidRepository, MVVM scaffolding
  - phase: 02-catalog-seeding
    provides: Catalog data in Room for cascade-delete instrumented tests
provides:
  - Kid avatar initials and color palette utilities with unit tests
  - KidDao observeById/deleteById and D-04 ordering
  - FAB-driven add-child ModalBottomSheet flow
  - HomeScreen LazyColumn of child rows with initials avatars
affects:
  - 03-02-home-polish
  - 03-03-catalog-navigation

tech-stack:
  added: [material-icons-core]
  patterns:
    - Bottom sheet as conditional composition overlay on HomeScreen
    - Pure domain validation before Room upsert
    - Name-hash default avatar color with manual swatch override

key-files:
  created:
    - app/src/main/java/se/simmarken/domain/util/KidAvatarInitials.kt
    - app/src/main/java/se/simmarken/ui/theme/KidAvatarColors.kt
    - app/src/main/java/se/simmarken/domain/validation/KidNameValidation.kt
    - app/src/main/java/se/simmarken/ui/home/KidFormViewModel.kt
    - app/src/main/java/se/simmarken/ui/home/KidFormBottomSheet.kt
    - app/src/main/java/se/simmarken/ui/home/components/KidAvatar.kt
    - app/src/main/java/se/simmarken/ui/home/components/ColorSwatchGrid.kt
    - app/src/androidTest/java/se/simmarken/data/local/KidDaoTest.kt
  modified:
    - app/src/main/java/se/simmarken/data/local/dao/KidDao.kt
    - app/src/main/java/se/simmarken/domain/repository/KidRepository.kt
    - app/src/main/java/se/simmarken/ui/home/HomeScreen.kt
    - app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt

key-decisions:
  - "KidFormViewModel tracks userPickedColor so name-hash default updates until parent taps a swatch"
  - "Yellow swatch (index 2) uses black initials for contrast per UI-SPEC"
  - "Sheet dismiss-after-save uses sheetState.hide() invokeOnCompletion pattern from RESEARCH"

patterns-established:
  - "KidSheetState sealed interface on HomeViewModel orchestrates bottom sheet visibility"
  - "KidFormViewModelFactory mirrors HomeViewModelFactory manual DI pattern"

requirements-completed: [KIDS-01, KIDS-02]

duration: 12min
completed: 2026-07-23
---

# Phase 3 Plan 01: Add-Child Vertical Slice Summary

**FAB-driven add-child flow with initials avatars, 10-color swatch grid, Swedish validation, and Room persistence ordered by creation**

## Performance

- **Duration:** 12 min
- **Started:** 2026-07-23T06:06:00Z
- **Completed:** 2026-07-23T06:11:26Z
- **Tasks:** 2
- **Files modified:** 19

## Accomplishments

- Pure `KidAvatarInitials`, `KidAvatarColors`, and `KidNameValidation` utilities with unit tests
- Extended `KidDao`/`KidRepository` with D-04 ordering, `observeById`, and cascade-safe `delete`
- Instrumented `KidDaoTest` covers ordering, upsert field preservation, and progress cascade delete
- `KidFormBottomSheet` add flow with validation, color picker, and dismiss-after-save animation
- `HomeScreen` shows FAB + creation-ordered kid list with initials avatars

## Task Commits

Each task was committed atomically:

1. **Task 1: Avatar utilities, name validation, and KidDao/Repository extensions with tests** — `78d8cf2` (test), `69e8143` (feat)
2. **Task 2: Add-child bottom sheet and minimal home list wired end-to-end** — `4c2bd95` (feat)

## Files Created/Modified

- `app/src/main/java/se/simmarken/domain/util/KidAvatarInitials.kt` — Two-word/single-word/empty initials logic
- `app/src/main/java/se/simmarken/ui/theme/KidAvatarColors.kt` — 10-color palette and name-hash default
- `app/src/main/java/se/simmarken/domain/validation/KidNameValidation.kt` — Swedish empty/length validation
- `app/src/main/java/se/simmarken/data/local/dao/KidDao.kt` — D-04 ORDER BY, observeById, deleteById
- `app/src/androidTest/java/se/simmarken/data/local/KidDaoTest.kt` — CRUD ordering and cascade tests
- `app/src/main/java/se/simmarken/ui/home/KidFormViewModel.kt` — Form state, validation, upsert on IO
- `app/src/main/java/se/simmarken/ui/home/KidFormBottomSheet.kt` — ModalBottomSheet add form
- `app/src/main/java/se/simmarken/ui/home/HomeScreen.kt` — FAB, kid list, sheet orchestration

## Decisions Made

- `userPickedColor` flag prevents overwriting manual swatch selection when name changes
- Yellow swatch uses `Color.Black` initials; all other swatches use white
- `material-icons-core` added (Compose BOM) for `Icons.Default.Add` on FAB

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added material-icons-core dependency**
- **Found during:** Task 2 (HomeScreen FAB compile)
- **Issue:** `Icons.Default.Add` unresolved — icons not on classpath
- **Fix:** Added `implementation("androidx.compose.material:material-icons-core")` to `app/build.gradle.kts`
- **Files modified:** `app/build.gradle.kts`
- **Verification:** `./gradlew :app:assembleDebug` succeeds
- **Committed in:** `4c2bd95`

**2. [Rule 1 - Bug] Fixed catalog version in KidDaoTest**
- **Found during:** Task 1 (instrumented test run)
- **Issue:** `catalogVersion = "1"` triggered `CatalogVersion.parse` crash in cascade delete test setup
- **Fix:** Use `catalogVersion = "2026.01.01"` matching project version format
- **Files modified:** `app/src/androidTest/java/se/simmarken/data/local/KidDaoTest.kt`
- **Verification:** `./gradlew :app:connectedDebugAndroidTest` with KidDaoTest class — 3/3 pass
- **Committed in:** `78d8cf2`

---

**Total deviations:** 2 auto-fixed (1 blocking, 1 bug)
**Impact on plan:** Both fixes required for green build/tests. No scope creep.

## Issues Encountered

- `connectedDebugAndroidTest` does not accept `--tests` flag; used `-Pandroid.testInstrumentationRunnerArguments.class` instead

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Add-child flow complete; ready for 03-02 (ChildCard polish, empty state, edit/delete)
- Catalog navigation placeholder still needed in 03-03
- Manual UAT: FAB → enter name → pick swatch → Lägg till → verify card on home

## Self-Check: PASSED

- FOUND: `.planning/phases/03-child-profiles-home/03-01-SUMMARY.md`
- FOUND: `78d8cf2` (test commit)
- FOUND: `69e8143` (feat Task 1)
- FOUND: `4c2bd95` (feat Task 2)

---
*Phase: 03-child-profiles-home*
*Completed: 2026-07-23*
