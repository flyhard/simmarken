---
phase: 05-progress-tracking-badge-detail
plan: 03
subsystem: ui
tags: [android, compose, room, flow, badge-state, home-screen]

requires:
  - phase: 05-progress-tracking-badge-detail
    plan: 01
    provides: BadgeStateCalculator path, ProgressRepository writes
  - phase: 05-progress-tracking-badge-detail
    plan: 02
    provides: Badge detail screen complete — home summaries are final PROG-05 slice
provides:
  - KidProgressSummaryCalculator aggregating IN_PROGRESS and ACHIEVED_TO_BUY across all catalogs
  - CatalogDao observeAllBadges and observeAllCategories queries
  - HomeViewModel summariesByKidId reactive combine per kid
  - ChildCard pågår / att köpa subtitle lines (D-13, D-14)
affects:
  - phase 6 i18n for summary strings

tech-stack:
  added: []
  patterns:
    - Domain aggregator reuses BadgeCatalogMapper.toBadgeCellUiModel — no duplicate state logic (D-17, D-20)
    - HomeViewModel flatMapLatest on kids with per-kid progress combine (T-05-07)
    - ChildCard subtitles hidden when count is zero (D-14)

key-files:
  created:
    - app/src/main/java/se/simmarken/domain/KidProgressSummaryCalculator.kt
    - app/src/main/java/se/simmarken/domain/model/KidProgressSummary.kt
    - app/src/test/java/se/simmarken/domain/KidProgressSummaryCalculatorTest.kt
  modified:
    - app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt
    - app/src/main/java/se/simmarken/domain/repository/CatalogRepository.kt
    - app/src/main/java/se/simmarken/domain/repository/CatalogRepositoryImpl.kt
    - app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt
    - app/src/main/java/se/simmarken/ui/home/HomeScreen.kt
    - app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt
    - app/src/main/java/se/simmarken/navigation/HomeViewModelFactory.kt
    - app/src/main/java/se/simmarken/navigation/SimmarkenNavHost.kt
    - app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt

key-decisions:
  - "Home summary counts use BadgeCatalogMapper + BadgeStateCalculator — same path as catalog grid (Pitfall 3)"
  - "GOTTEN and LOCKED badges excluded from home counts per D-16"
  - "Both Simidrott and SLS badges summed via observeAllBadges per D-15"

patterns-established:
  - "KidProgressSummaryCalculator pure object for per-kid badge attention counts"
  - "HomeViewModel scopes progressRepository.observe* per kid.id in combine"

requirements-completed: [PROG-05]

coverage:
  - id: D1
    description: KidProgressSummaryCalculator counts IN_PROGRESS and ACHIEVED_TO_BUY via BadgeCatalogMapper
    requirement: PROG-05
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/domain/KidProgressSummaryCalculatorTest.kt"
        status: pass
    human_judgment: false
  - id: D2
    description: HomeViewModel emits per-kid summariesByKidId on progress changes
    requirement: PROG-05
    verification:
      - kind: unit
        ref: "./gradlew :app:compileDebugKotlin"
        status: pass
    human_judgment: true
    rationale: Multi-flow combine reactivity requires on-device verification with multiple kids
  - id: D3
    description: ChildCard shows pågår and att köpa subtitles when counts > 0
    requirement: PROG-05
    verification:
      - kind: unit
        ref: "./gradlew :app:compileDebugKotlin"
        status: pass
    human_judgment: true
    rationale: Subtitle layout, spacing, and zero-hide behavior need visual check on device

duration: 18min
completed: 2026-07-23
status: complete
---

# Phase 5 Plan 3: Home Card Summary Counts Summary

**Per-kid pågår and att köpa counts on home ChildCards, aggregated across both catalogs via KidProgressSummaryCalculator and BadgeCatalogMapper**

## Performance

- **Duration:** 18 min
- **Started:** 2026-07-23T12:23:00Z
- **Completed:** 2026-07-23T12:41:00Z
- **Tasks:** 3
- **Files modified:** 11

## Accomplishments

- KidProgressSummaryCalculator aggregates IN_PROGRESS and ACHIEVED_TO_BUY across all badges using BadgeCatalogMapper (D-15, D-16, D-17, D-20)
- HomeViewModel combines catalog and per-kid progress flows into summariesByKidId
- ChildCard shows `{count} pågår` and `{count} att köpa` subtitle lines when counts > 0 (D-13, D-14)

## Task Commits

Each task was committed atomically:

1. **Task 1 (RED):** End-to-end kid summary count — `08f48a4` (test)
2. **Task 1 (GREEN):** KidProgressSummaryCalculator + catalog queries — `db0a84a` (feat)
3. **Task 2:** HomeViewModel per-kid summary combine — `330e9d3` (feat)
4. **Task 3:** ChildCard subtitle lines and HomeScreen wiring — `4c2a928` (feat)

## Files Created/Modified

- `app/src/main/java/se/simmarken/domain/KidProgressSummaryCalculator.kt` — Pure aggregator over all badges
- `app/src/main/java/se/simmarken/domain/model/KidProgressSummary.kt` — Per-kid count data class
- `app/src/test/java/se/simmarken/domain/KidProgressSummaryCalculatorTest.kt` — Five behavior cases
- `app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt` — summariesByKidId combine
- `app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt` — Subtitle lines with UI-SPEC spacing
- `app/src/main/java/se/simmarken/navigation/HomeViewModelFactory.kt` — Catalog + progress injection

## Decisions Made

- Reused BadgeCatalogMapper.toBadgeCellUiModel in calculator — single BadgeStateCalculator path (Pitfall 3)
- Per-kid progress flows scoped by kid.id in combine — kid A progress never feeds kid B counts (T-05-07)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Updated FakeCatalogRepository for new observe methods**
- **Found during:** Task 1 (GREEN compile)
- **Issue:** CatalogRepository interface gained observeAllBadges/observeAllCategories; test fake did not implement them
- **Fix:** Added passthrough overrides to FakeCatalogRepository
- **Files modified:** app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt
- **Committed in:** db0a84a

**2. [Rule 3 - Blocking] Explicit emptyList types in HomeViewModel**
- **Found during:** Task 2 (compile)
- **Issue:** Kotlin could not infer types for empty progress fallback pair
- **Fix:** Typed emptyList<RequirementProgressEntity>() and emptyList<BadgeProgressEntity>()
- **Files modified:** app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt
- **Committed in:** 330e9d3

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** Compile fixes only; no scope change.

## Issues Encountered

None beyond compile-time fixes documented above.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 5 complete — PROG-01 through PROG-05 delivered
- Phase 6 can add i18n for summary strings (`pågår`, `att köpa`) and export/import

## Self-Check: PASSED

- FOUND: .planning/phases/05-progress-tracking-badge-detail/05-03-SUMMARY.md
- FOUND: app/src/main/java/se/simmarken/domain/KidProgressSummaryCalculator.kt
- FOUND: commit 08f48a4, db0a84a, 330e9d3, 4c2a928

---
*Phase: 05-progress-tracking-badge-detail*
*Completed: 2026-07-23*
