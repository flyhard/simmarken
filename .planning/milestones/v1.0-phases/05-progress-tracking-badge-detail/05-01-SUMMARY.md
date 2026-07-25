---
phase: 05-progress-tracking-badge-detail
plan: 01
subsystem: ui
tags: [android, compose, room, mvvm, progress-tracking, tdd]

requires:
  - phase: 04-catalog-view-visual-states
    provides: BadgeDetailViewModel read path, BadgeStateCalculator, placeholder screen
provides:
  - RequirementRowUiModel and RequirementChecklistRow tracer UI
  - toggleRequirement with achievedAt write-once and gotten auto-clear
  - setGotten / confirmClearGotten purchase persistence API
  - BadgeDetailViewModelProgressTest and ProgressWriteLogicTest
affects:
  - 05-02 BadgeDetailScreen and PurchaseToggleRow wiring

tech-stack:
  added: [kotlinx-coroutines-test (unit test)]
  patterns:
    - ProgressWriteLogic pure predicates for write-once rules
    - HomeViewModel-style MutableStateFlow dialog flag in BadgeDetailViewModel
    - Injectable ioDispatcher for JVM ViewModel tests

key-files:
  created:
    - app/src/main/java/se/simmarken/domain/model/RequirementRowUiModel.kt
    - app/src/main/java/se/simmarken/domain/ProgressWriteLogic.kt
    - app/src/main/java/se/simmarken/ui/badge/components/RequirementChecklistRow.kt
    - app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt
    - app/src/test/java/se/simmarken/domain/ProgressWriteLogicTest.kt
  modified:
    - app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt
    - app/src/main/java/se/simmarken/domain/model/BadgeDetailUiState.kt
    - app/src/main/java/se/simmarken/ui/badge/BadgeDetailPlaceholderScreen.kt
    - app/build.gradle.kts

key-decisions:
  - "ioDispatcher constructor param (default Dispatchers.IO) enables deterministic JVM tests without changing production write semantics"
  - "visualState remains derived only via BadgeCatalogMapper → BadgeStateCalculator in combine block"

patterns-established:
  - "Progress writes orchestrated in ViewModel on IO; Room Flow re-emission drives UI recompute"
  - "achievedAtEpochMillis write-once via ProgressWriteLogic.preserveAchievedAt"

requirements-completed: [PROG-01, PROG-02, PROG-03]

coverage:
  - id: D1
    description: Requirement toggle persists progress and recomputes visualState via BadgeStateCalculator
    requirement: PROG-01
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt#toggleFirstRequirement_lockedToInProgress"
        status: pass
    human_judgment: false
  - id: D2
    description: Last requirement sets achievedAt write-once; uncheck retains timestamp; GOTTEN auto-clears on uncheck
    requirement: PROG-02
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/domain/ProgressWriteLogicTest.kt"
        status: pass
      - kind: unit
        ref: "app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt#lastRequirement_setsAchievedToBuy"
        status: pass
      - kind: unit
        ref: "app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt#uncheckWhileGotten_clearsGotten"
        status: pass
    human_judgment: false
  - id: D3
    description: Purchase flag write API (setGotten, confirmClearGotten) and isPurchaseEnabled derivation
    requirement: PROG-03
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt#setGotten_true_upsertsGottenFields"
        status: pass
      - kind: unit
        ref: "app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt#confirmClearGotten_clearsGottenRetainsAchievedAt"
        status: pass
    human_judgment: false
  - id: D4
    description: RequirementChecklistRow renders on placeholder screen for tracer path
    requirement: PROG-01
    verification: []
    human_judgment: true
    rationale: Compose UI layout not covered by JVM unit tests; verify in 05-02 screen integration"

duration: 35min
completed: 2026-07-23
status: complete
---

# Phase 05 Plan 01: Progress Write Vertical Slice Summary

**ViewModel progress writes with achievedAt write-once, gotten auto-clear, purchase flag API, and tracer checklist row**

## Performance

- **Duration:** 35 min
- **Started:** 2026-07-23T11:56:00Z
- **Completed:** 2026-07-23T12:31:00Z
- **Tasks:** 3
- **Files modified:** 9

## Accomplishments

- Requirement toggle flows UI → ViewModel → ProgressRepository → Flow recompute (LOCKED → IN_PROGRESS)
- achievedAt write-once on last-requirement achieve; GOTTEN auto-clear on uncheck without dialog
- setGotten / confirmClearGotten / requestClearGotten purchase persistence ready for 05-02 PurchaseToggleRow
- First JVM unit tests for ViewModel progress orchestration

## Task Commits

1. **Task 1: End-to-end one requirement toggle** - `ffc3b91` (feat)
2. **Task 2: achievedAt write-once and gotten auto-clear** - `f7cfac8` (feat)
3. **Task 3: Purchase flag writes** - `7ee8432` (feat)

## Files Created/Modified

- `RequirementRowUiModel.kt` - Checklist row domain model
- `ProgressWriteLogic.kt` - Pure write-rule predicates
- `RequirementChecklistRow.kt` - M3 Checkbox row composable
- `BadgeDetailViewModel.kt` - toggleRequirement, setGotten, dialog methods
- `BadgeDetailUiState.kt` - requirements, purchase, dialog fields
- `BadgeDetailPlaceholderScreen.kt` - Renders checklist rows (replaces stub text)
- `BadgeDetailViewModelProgressTest.kt` - ViewModel progress tests
- `ProgressWriteLogicTest.kt` - Write-rule unit tests

## Decisions Made

- Added optional `ioDispatcher` constructor parameter (default `Dispatchers.IO`) for testability without altering production dispatch semantics
- Dialog state uses `MutableStateFlow` merged into uiState, mirroring HomeViewModel deleteTarget pattern

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Single-flow combine compile error for badge-missing branch**
- **Found during:** Task 3
- **Issue:** `combine(showUncheckPurchaseDialog) { }` inferred `Array<Boolean>` lambda parameter
- **Fix:** Replaced with `showUncheckPurchaseDialog.map { }` for missing-badge branch
- **Files modified:** `BadgeDetailViewModel.kt`
- **Committed in:** `7ee8432`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Compile fix only; no behavior change.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- 05-02 can wire PurchaseToggleRow, UncheckPurchaseDialog, and full BadgeDetailScreen to existing ViewModel API
- Placeholder screen still used in navigation until 05-02 replaces it

## Self-Check: PASSED

- FOUND: app/src/main/java/se/simmarken/domain/ProgressWriteLogic.kt
- FOUND: app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt
- FOUND: ffc3b91, f7cfac8, 7ee8432

---
*Phase: 05-progress-tracking-badge-detail*
*Completed: 2026-07-23*
