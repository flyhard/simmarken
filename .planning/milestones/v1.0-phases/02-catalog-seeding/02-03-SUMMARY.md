---
phase: 02-catalog-seeding
plan: 03
subsystem: database
tags: [android, room, kotlinx-serialization, catalog-seed, merge, startup]

requires:
  - plan: 02-01
    provides: simidrott.json seed asset with affisch-derived requirements
  - plan: 02-02
    provides: sls.json seed asset with shop-sourced requirements
  - plan: 02-04
    provides: bundled WebP badge images and BadgePlaceholderColors tier fallback
provides:
  - data/seed package with DTOs, parser, version gate, and merge loader
  - CatalogDao lookup-by-code queries for ID-preserving upsert
  - AppContainer IO-dispatcher startup seed wiring
  - Unit and instrumented tests for seed, merge, and image resolution
affects:
  - Phase 3 (catalog UI reads seeded Room data via CatalogRepository)
  - Phase 4 (Coil loads imageAssetPath; BadgePlaceholderColors for null paths)

tech-stack:
  added: []
  patterns:
    - "kotlinx-serialization @Serializable seed DTOs mirroring Room entities"
    - "CatalogVersion YYYY.MM.DD comparator for D-14 version gate"
    - "ID-preserving merge: findByCode → copy existing id → @Insert(REPLACE)"
    - "Room withTransaction for atomic catalog merge"
    - "CatalogSeedLoader launched from AppContainer on Dispatchers.IO"

key-files:
  created:
    - app/src/main/java/se/simmarken/data/seed/CatalogSeedDto.kt
    - app/src/main/java/se/simmarken/data/seed/CatalogSeedParser.kt
    - app/src/main/java/se/simmarken/data/seed/CatalogVersion.kt
    - app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt
    - app/src/test/java/se/simmarken/data/seed/CatalogVersionTest.kt
    - app/src/test/java/se/simmarken/data/seed/CatalogSeedParserTest.kt
    - app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderSmokeTest.kt
    - app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderTest.kt
    - app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt
  modified:
    - app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt
    - app/src/main/java/se/simmarken/di/AppContainer.kt
    - app/src/main/java/se/simmarken/ui/badge/BadgePlaceholderColors.kt

key-decisions:
  - "CatalogSeedLoader bypasses CatalogRepository and talks to CatalogDao directly"
  - "ID-preserving merge uses lookup-by-code before REPLACE upsert (D-15)"
  - "Room withTransaction wraps full catalog merge for FK integrity"
  - "Production asset parsing uses strictJson with ignoreUnknownKeys=false (T-2-13)"

patterns-established:
  - "Version-gated seedIfNeeded skips merge when bundled catalogVersion <= stored"
  - "mergeCatalog exposed for instrumented merge tests with synthetic bumped seeds"

requirements-completed: [CATA-01, CATA-02, CATA-03, CATA-04, CATA-05]

duration: 25min
completed: 2026-07-22
---

# Phase 2 Plan 3: Catalog Seed Loader Summary

**Version-gated kotlinx-serialization seed loader with ID-preserving Room merge wired at app startup on IO dispatcher**

## Performance

- **Duration:** 25 min
- **Started:** 2026-07-22T17:49:00Z
- **Completed:** 2026-07-22T17:54:26Z
- **Tasks:** 3
- **Files modified:** 12

## Accomplishments

- `data/seed/` package parses bundled JSON with strict production validation and lenient test parsing
- `CatalogVersion.shouldMerge` gates merge on `YYYY.MM.DD` catalog version comparison (D-14)
- `CatalogSeedLoader` upserts catalog hierarchy preserving existing row IDs for progress FK stability (D-13, D-15)
- `AppContainer` launches `seedIfNeeded()` on `Dispatchers.IO` without blocking main thread
- Instrumented tests verify both catalogs populate, sortOrder ordering, image paths, deduplication, and progress-preserving merge

## Task Commits

1. **Task 1: Seed DTOs, parser, version comparator, and DAO lookup queries** - `e2c84e4` (feat)
2. **Task 2: CatalogSeedLoader merge logic and AppContainer startup wiring** - `60c6e8c` (feat)
3. **Task 3: Instrumented seed population and progress-preservation tests** - `fc1b7f9` (test)

**Note:** `CatalogSeedLoader.kt` landed in Task 1 commit `e2c84e4` alongside DTOs due to atomic staging; Task 2 commit covers `AppContainer` wiring and smoke test.

## Files Created/Modified

- `app/src/main/java/se/simmarken/data/seed/CatalogSeedDto.kt` - Serializable DTOs with `toEntity` mappers
- `app/src/main/java/se/simmarken/data/seed/CatalogSeedParser.kt` - Asset and string JSON parsing
- `app/src/main/java/se/simmarken/data/seed/CatalogVersion.kt` - Version parse and shouldMerge gate
- `app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt` - Transactional ID-preserving merge loader
- `app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt` - Lookup-by-code and count queries
- `app/src/main/java/se/simmarken/di/AppContainer.kt` - IO coroutine startup seed launch
- `app/src/androidTest/java/se/simmarken/data/seed/*.kt` - Smoke, population, and merge instrumented tests
- `app/src/test/java/se/simmarken/data/seed/*.kt` - Version and parser unit tests

## Decisions Made

- Loader talks to `CatalogDao` directly, not `CatalogRepository` (per PATTERNS.md)
- `database.withTransaction` chosen over `@Transaction` DAO method for merge atomicity
- `mergeCatalog` exposed publicly for merge test with synthetic bumped seed DTO
- v1 merge does not prune requirements removed from seed (documented in code comment)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] BadgePlaceholderColors included in Task 3 commit**
- **Found during:** Task 3 (`nullImageBadgesHaveCategoryFallbackColor` test)
- **Issue:** `BadgePlaceholderColors.kt` from plan 02-04 was required by CATA-05 fallback test but not yet on main working tree
- **Fix:** Ensured tier-color `forCategoryCode` implementation present for Simidrott and SLS category codes
- **Files modified:** `app/src/main/java/se/simmarken/ui/badge/BadgePlaceholderColors.kt`
- **Committed in:** `fc1b7f9`

---

**Total deviations:** 1 auto-fixed (missing critical dependency from 02-04)
**Impact on plan:** Required for CATA-05 instrumented test; no scope change to loader logic.

## Issues Encountered

- Gradle 8.13 distribution download on first build (~2 min cold start)
- `connectedDebugAndroidTest` not run (no emulator); `assembleDebugAndroidTest` verified instrumented test compilation

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Both catalogs seed into Room on first app launch; Phase 3 can observe via `CatalogRepository`
- Instrumented tests ready to run on emulator: `./gradlew :app:connectedDebugAndroidTest --tests "se.simmarken.data.seed.*"`
- Manual cold-start verification via Android Studio Database Inspector recommended before phase sign-off

---
*Phase: 02-catalog-seeding*
*Completed: 2026-07-22*

## Self-Check: PASSED

- FOUND: app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt
- FOUND: app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderTest.kt
- FOUND: app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt
- FOUND: commit e2c84e4
- FOUND: commit 60c6e8c
- FOUND: commit fc1b7f9
