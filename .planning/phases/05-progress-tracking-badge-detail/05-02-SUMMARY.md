---
phase: 05-progress-tracking-badge-detail
plan: 02
subsystem: ui
tags: [android, compose, material3, badge-detail, progress-tracking]

requires:
  - phase: 05-progress-tracking-badge-detail
    plan: 01
    provides: BadgeDetailViewModel progress API, RequirementChecklistRow, BadgeDetailUiState extensions
provides:
  - BadgeDetailScreen replacing placeholder with fixed pin header and scrollable body
  - RequirementChecklist and ZeroRequirementNote components
  - PurchaseToggleRow and UncheckPurchaseDialog köpt undo flow
  - SimmarkenNavHost wired to BadgeDetailScreen
affects:
  - 05-03 home card summary counts

tech-stack:
  added: []
  patterns:
    - Fixed header + verticalScroll body split per D-01
    - DeleteKidDialog-mirror AlertDialog for destructive köpt undo
    - Purchase switch gated via ViewModel isPurchaseEnabled

key-files:
  created:
    - app/src/main/java/se/simmarken/ui/badge/BadgeDetailScreen.kt
    - app/src/main/java/se/simmarken/ui/badge/components/RequirementChecklist.kt
    - app/src/main/java/se/simmarken/ui/badge/components/ZeroRequirementNote.kt
    - app/src/main/java/se/simmarken/ui/badge/components/PurchaseToggleRow.kt
    - app/src/main/java/se/simmarken/ui/badge/components/UncheckPurchaseDialog.kt
  modified:
    - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
  deleted:
    - app/src/main/java/se/simmarken/ui/badge/BadgeDetailPlaceholderScreen.kt

key-decisions:
  - "Fixed pin header stays outside verticalScroll; checklist and purchase toggle scroll together"
  - "PurchaseToggleRow uses 38% opacity label when disabled per UI-SPEC default"

patterns-established:
  - "Badge detail composables split into screen shell + components/ checklist, note, purchase, dialog"
  - "Purchase undo dialog shown at screen level when showUncheckPurchaseDialog is true"

requirements-completed: [UI-03, PROG-01, PROG-02, PROG-03]

coverage:
  - id: D1
    description: BadgeDetailScreen with fixed pin, scrollable checklist, and purchase toggle
    requirement: UI-03
    verification:
      - kind: unit
        ref: "./gradlew :app:compileDebugKotlin"
        status: pass
    human_judgment: true
    rationale: Compose layout and live pin updates require on-device visual verification
  - id: D2
    description: Progress subtitle X av Y klara when requirements exist
    requirement: PROG-01
    verification: []
    human_judgment: true
    rationale: Subtitle visibility and copy placement not covered by JVM tests
  - id: D3
    description: Zero-requirement badges show Inga kunskapskrav with purchase enabled
    requirement: PROG-02
    verification: []
    human_judgment: true
    rationale: Zero-requirement badge flow requires manual badge selection in app
  - id: D4
    description: Purchase toggle with köpt undo dialog wired to ViewModel API
    requirement: PROG-03
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt"
        status: pass
    human_judgment: true
    rationale: Switch enabled state and dialog presentation are UI concerns
  - id: D5
    description: NavHost routes BadgeDetail to BadgeDetailScreen
    requirement: UI-03
    verification:
      - kind: unit
        ref: "./gradlew :app:compileDebugKotlin"
        status: pass
    human_judgment: false

duration: 28min
completed: 2026-07-23
status: complete
---

# Phase 05 Plan 02: Badge Detail Screen Summary

**Full BadgeDetailScreen with fixed pin header, scrollable checklist, zero-requirement note, and purchase toggle with köpt undo dialog**

## Performance

- **Duration:** 28 min
- **Started:** 2026-07-23T12:02:53Z
- **Completed:** 2026-07-23T12:30:53Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments

- Replaced BadgeDetailPlaceholderScreen with BadgeDetailScreen — fixed pin, progress subtitle, scrollable body
- RequirementChecklist and ZeroRequirementNote components per UI-SPEC spacing and copy
- PurchaseToggleRow with isPurchaseEnabled gate and UncheckPurchaseDialog köpt undo flow
- SimmarkenNavHost navigates to BadgeDetailScreen; placeholder removed
- BadgeDetailViewModelProgressTest regression suite still green

## Task Commits

1. **Task 1: End-to-end BadgeDetailScreen shell** - `95fc6b3` (feat)
2. **Task 2: RequirementChecklist and ZeroRequirementNote** - `a9debc0` (feat)
3. **Task 3: Purchase toggle row and köpt undo dialog** - `c0d1ecc` (feat)

## Files Created/Modified

- `BadgeDetailScreen.kt` - Full detail screen with fixed header, scroll body, dialog
- `RequirementChecklist.kt` - Column wrapper for checklist rows
- `ZeroRequirementNote.kt` - Inga kunskapskrav note for zero-requirement badges
- `PurchaseToggleRow.kt` - Fysiskt märke köpt switch row with divider
- `UncheckPurchaseDialog.kt` - Ta bort köpt-markering confirmation
- `SimmarkenNavHost.kt` - BadgeDetail route uses BadgeDetailScreen
- `BadgeDetailPlaceholderScreen.kt` - Deleted

## Decisions Made

- Fixed pin header stays outside `verticalScroll`; only checklist and purchase section scroll
- Disabled purchase label uses `onSurface.copy(alpha = 0.38f)` per UI-SPEC discretion default

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- 05-03 can add home card summary counts (PROG-05) — detail screen complete for UI-03 and PROG-01/02/03
- Manual UAT recommended: toggle requirements, verify pin live update, köpt dialog on switch off

## Self-Check: PASSED

- FOUND: app/src/main/java/se/simmarken/ui/badge/BadgeDetailScreen.kt
- FOUND: app/src/main/java/se/simmarken/ui/badge/components/PurchaseToggleRow.kt
- FOUND: app/src/main/java/se/simmarken/ui/badge/components/UncheckPurchaseDialog.kt
- FOUND: 95fc6b3, a9debc0, c0d1ecc

---
*Phase: 05-progress-tracking-badge-detail*
*Completed: 2026-07-23*
