---
phase: 02-catalog-seeding
plan: 05
subsystem: database
tags: [room, upsert, android-test, catalog-seed, merge]

requires:
  - phase: 02-03
    provides: CatalogSeedLoader merge-by-code with DAO upserts
provides:
  - Non-destructive @Upsert catalog hierarchy writes preserving stable IDs
  - Green instrumented seed test suite (8/8) on emulator
  - Correct connectedDebugAndroidTest package-filter documentation
affects: [phase-03, phase-05]

tech-stack:
  added: []
  patterns:
    - "@Upsert with post-upsert code lookup for FK IDs (@Upsert returns -1 on update)"
    - "Instrumented tests filtered via -Pandroid.testInstrumentationRunnerArguments.package"

key-files:
  created: []
  modified:
    - app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt
    - app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt
    - app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt
    - .planning/phases/02-catalog-seeding/02-VERIFICATION.md
    - .planning/phases/02-catalog-seeding/02-HUMAN-UAT.md

key-decisions:
  - "Re-fetch entity IDs after @Upsert instead of using return value (-1 on update)"
  - "Keep find-by-code → toEntity(existingId) merge pattern; only ID resolution changed"

patterns-established:
  - "Catalog merge: @Upsert + lookup-by-code for stable IDs across version bumps"

requirements-completed: [CATA-01, CATA-02, CATA-03, CATA-04, CATA-05]

duration: 25min
completed: 2026-07-22
---

# Phase 02 Plan 05 Summary

**Room @Upsert replaces destructive REPLACE upserts so catalog merges preserve requirement IDs and all 8 seed instrumented tests pass**

## Performance

- **Duration:** ~25 min
- **Tasks:** 3
- **Files modified:** 5

## Accomplishments

- Replaced four `@Insert(REPLACE)` upserts in `CatalogDao` with `@Upsert` to stop CASCADE-deleting child rows on version bumps
- Fixed `CatalogSeedLoader` to re-fetch IDs after upsert (`@Upsert` returns `-1` on update, breaking FK chains when return value was used)
- Hardened `CatalogSeedMergeTest.progressPreservedWhenTextChanges` with descriptive `assertNotNull` assertions
- All 8 `se.simmarken.data.seed` instrumented tests pass on emulator; unit tests unaffected
- Updated VERIFICATION and HUMAN-UAT docs with correct `-Pandroid.testInstrumentationRunnerArguments.package` filter; UAT gap 2 resolved

## Task Commits

1. **Task 1: Replace destructive REPLACE upserts with Room @Upsert** - `74e3c08` (fix)
2. **Task 2: Verify merge test passes and harden post-merge assertions** - `2bb65aa` (fix)
3. **Task 3: Update VERIFICATION and UAT docs** - (docs commit pending below)

## Files Created/Modified

- `app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt` - `@Upsert` on all four hierarchy upsert methods
- `app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt` - Re-fetch IDs after upsert for FK integrity
- `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt` - Stable-ID assertions without NPE
- `.planning/phases/02-catalog-seeding/02-VERIFICATION.md` - Correct instrumented test command
- `.planning/phases/02-catalog-seeding/02-HUMAN-UAT.md` - Gap 2 resolved, test 3 pass

## Decisions Made

- Re-fetch entity by code after each upsert rather than using upsert return value — required because Room `@Upsert` returns `-1` on update paths

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Blocking] CatalogSeedLoader ID resolution after @Upsert**
- **Found during:** Task 2 (instrumented test run)
- **Issue:** `@Upsert` returns `-1` on update; loader used return value as FK id → `FOREIGN KEY constraint failed` on first seed
- **Fix:** Re-fetch catalog/category/badge by code after upsert to obtain stable id
- **Files modified:** `app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt`
- **Verification:** `connectedDebugAndroidTest` 8/8 pass
- **Committed in:** `2bb65aa`

**Total deviations:** 1 auto-fixed (blocking)
**Impact on plan:** Essential companion to @Upsert migration; no scope creep

## Issues Encountered

None after loader ID resolution fix

## User Setup Required

None

## Next Phase Readiness

- Catalog seeding gap closure complete; deferred UAT gap 1 (badge image artwork) remains for a later phase
- Phase 3 (child profiles) can proceed once phase verification re-runs

---
*Phase: 02-catalog-seeding*
*Completed: 2026-07-22*
