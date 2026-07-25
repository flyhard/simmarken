---
phase: 03-child-profiles-home
plan: 03
subsystem: ui
tags: [android, compose, edit-delete, overflow-menu, alert-dialog, kid-crud]

requires:
  - phase: 03-child-profiles-home
    plan: 01
    provides: KidFormBottomSheet, KidFormViewModel, observeById, KidSheetState
  - phase: 03-child-profiles-home
    plan: 02
    provides: ChildCard, HomeScreen layout, catalog navigation
provides:
  - Overflow menu on ChildCard with isolated Redigera/Ta bort actions per D-08
  - Edit mode in shared bottom sheet with field preservation per D-04/D-05
  - DeleteKidDialog with destructive confirmation and FK cascade delete
  - Full KIDS-04 child profile edit and remove capability
affects:
  - 04-catalog-badge-grid
  - 05-progress-tracking

tech-stack:
  added: []
  patterns:
    - Isolated overflow IconButton prevents accidental catalog navigation
    - KidSheetState.Edit(kidId) drives KidFormViewModelFactory with observeById pre-fill
    - deleteTarget in HomeUiState with mutually exclusive sheet/dialog per UI-SPEC

key-files:
  created:
    - app/src/main/java/se/simmarken/ui/home/components/DeleteKidDialog.kt
  modified:
    - app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt
    - app/src/main/java/se/simmarken/ui/home/KidFormViewModel.kt
    - app/src/main/java/se/simmarken/ui/home/KidFormBottomSheet.kt
    - app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt
    - app/src/main/java/se/simmarken/ui/home/HomeScreen.kt

key-decisions:
  - "requestDelete closes any open sheet before showing dialog per UI-SPEC state matrix"
  - "Edit save preserves existingKid entity fields via copy() — only name and avatarColorArgb change"
  - "confirmDelete uses kidRepository.delete on IO — no manual progress cleanup per FK CASCADE"

patterns-established:
  - "ChildCard body clickable isolated from 48dp overflow IconButton per RESEARCH Pattern 4"
  - "HomeScreen suppresses bottom sheet when deleteTarget is non-null"

requirements-completed: [KIDS-01, KIDS-02, KIDS-04]

duration: 2min
completed: 2026-07-23
---

# Phase 3 Plan 03: Edit & Delete Child Profiles Summary

**Overflow-menu edit/delete on child cards with shared bottom sheet edit mode, field preservation, and destructive delete confirmation with Room cascade**

## Performance

- **Duration:** 2 min
- **Started:** 2026-07-23T06:14:51Z
- **Completed:** 2026-07-23T06:16:40Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments

- `ChildCard` overflow menu with `MoreVert` icon, `Redigera` and `Ta bort` items, isolated from card navigation tap
- `KidSheetState.Edit(kidId)` and `KidFormViewModel` edit mode pre-fills via `observeById`, preserves `id`/`createdAtEpochMillis`/`sortOrder` on save
- `KidFormBottomSheet` shows `Redigera barn` title and `Spara` button in edit mode
- `DeleteKidDialog` with Swedish copy, error-styled confirm, and `kidRepository.delete` cascade wiring
- Sheet and delete dialog mutually exclusive — delete request closes any open sheet first

## Task Commits

Each task was committed atomically:

1. **Task 1: Overflow menu, edit bottom sheet mode, and field preservation** — `597dfc4` (feat)
2. **Task 2: Delete confirmation dialog and cascade delete wiring** — `42e898a` (feat)

## Files Created/Modified

- `app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt` — Overflow DropdownMenu with isolated tap targets
- `app/src/main/java/se/simmarken/ui/home/KidFormViewModel.kt` — Edit mode with observeById pre-fill and preserved upsert
- `app/src/main/java/se/simmarken/ui/home/KidFormBottomSheet.kt` — Dynamic title and save label for add vs edit
- `app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt` — Edit sheet state, deleteTarget, request/dismiss/confirm delete
- `app/src/main/java/se/simmarken/ui/home/HomeScreen.kt` — Overflow callbacks, sheet for Add/Edit, DeleteKidDialog overlay
- `app/src/main/java/se/simmarken/ui/home/components/DeleteKidDialog.kt` — AlertDialog per UI-SPEC section 8

## Decisions Made

- `requestDelete` sets `sheetState` to Hidden before showing dialog — ensures UI-SPEC state matrix compliance
- Edit mode sets `userPickedColor = true` on load so name changes don't reset manually chosen swatch
- No manual progress row deletion — Room FK CASCADE handles cleanup on `kidRepository.delete`

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 3 complete: full child profile CRUD (add, edit, delete) with swim-hall-safe interactions
- Phase 4 can replace catalog placeholder while keeping `ChildCatalog(kidId)` route stable
- Manual UAT: overflow → Redigera → change name/color → Spara; overflow → Ta bort → confirm/cancel

## Self-Check: PASSED

- FOUND: `app/src/main/java/se/simmarken/ui/home/components/DeleteKidDialog.kt`
- FOUND: `app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt`
- FOUND: `597dfc4` (Task 1)
- FOUND: `42e898a` (Task 2)

---
*Phase: 03-child-profiles-home*
*Completed: 2026-07-23*
