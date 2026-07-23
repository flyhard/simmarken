---
phase: 04-catalog-view-visual-states
plan: 01
subsystem: domain
tags: [kotlin, room, mvvm, junit, badge-state]

requires:
  - phase: 02-catalog-seeding
    provides: BadgeEntity, RequirementEntity, CatalogDao seed data
  - phase: 03-kid-profiles
    provides: Kid profiles and progress entity shapes
provides:
  - BadgeVisualState enum (LOCKED, IN_PROGRESS, ACHIEVED_TO_BUY, GOTTEN)
  - BadgeStateCalculator with D-30 precedence and progressFraction
  - BadgeCatalogMapper joining entities to BadgeCellUiModel
  - CatalogDao.observeAllRequirements and observeBadgeById
  - CatalogRepository read extensions for progress indexing
affects:
  - 04-02
  - 04-03

tech-stack:
  added: []
  patterns:
    - "Pure domain object BadgeStateCalculator (KidNameValidation style)"
    - "Read-time visual state derivation — never persisted in Room (D-29)"
    - "Sparse progress map defaults absent keys to not achieved (Pitfall 2)"

key-files:
  created:
    - app/src/main/java/se/simmarken/domain/model/BadgeVisualState.kt
    - app/src/main/java/se/simmarken/domain/BadgeStateCalculator.kt
    - app/src/main/java/se/simmarken/domain/model/CatalogUiModels.kt
    - app/src/main/java/se/simmarken/domain/BadgeCatalogMapper.kt
    - app/src/test/java/se/simmarken/domain/BadgeStateCalculatorTest.kt
    - app/src/test/java/se/simmarken/domain/BadgeCatalogMapperTest.kt
  modified:
    - app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt
    - app/src/main/java/se/simmarken/domain/repository/CatalogRepository.kt
    - app/src/main/java/se/simmarken/domain/repository/CatalogRepositoryImpl.kt

key-decisions:
  - "BadgeVisualState derived at read time via BadgeStateCalculator — never persisted (D-29)"
  - "State precedence: isGotten → GOTTEN; zero-req or all achieved → ACHIEVED_TO_BUY; partial → IN_PROGRESS; none → LOCKED (D-30)"
  - "Absent RequirementProgressEntity rows default to isAchieved=false in mapper (D-05 / Pitfall 2)"

patterns-established:
  - "BadgeStateCalculator.compute centralizes PROG-04 precedence — mapper delegates, no duplicate if/else"
  - "CategorySection preserves sortOrder for downstream UI ordering (D-20)"

requirements-completed: [PROG-04]

coverage:
  - id: D1
    description: "BadgeStateCalculator derives four visual states with D-30 precedence and progressFraction"
    requirement: PROG-04
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/domain/BadgeStateCalculatorTest.kt"
        status: pass
    human_judgment: false
  - id: D2
    description: "BadgeCatalogMapper joins badge + requirements + progress into BadgeCellUiModel"
    requirement: PROG-04
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/domain/BadgeCatalogMapperTest.kt"
        status: pass
    human_judgment: false
  - id: D3
    description: "CatalogDao and CatalogRepository expose observeAllRequirements and observeBadgeById"
    verification:
      - kind: unit
        ref: "./gradlew :app:compileDebugKotlin"
        status: pass
    human_judgment: false

duration: 25min
completed: 2026-07-23
status: complete
---

# Phase 04 Plan 01: Badge State Domain Foundation Summary

**PROG-04 state derivation centralized in BadgeStateCalculator with full JVM test matrix, entity-to-UI mapper, and CatalogDao progress-indexing queries**

## Performance

- **Duration:** 25 min
- **Started:** 2026-07-23T10:00:28Z
- **Completed:** 2026-07-23T10:05:26Z
- **Tasks:** 3
- **Files modified:** 9

## Accomplishments
- `BadgeStateCalculator` implements D-30 precedence (gotten overrides all; zero-requirement badges → ACHIEVED_TO_BUY)
- `BadgeCatalogMapper` joins Room entities to `BadgeCellUiModel` with absent-progress defaulting to not achieved
- `CatalogDao`/`CatalogRepository` extended with `observeAllRequirements` and `observeBadgeById` for ViewModels in 04-02/04-03

## Task Commits

Each task was committed atomically:

1. **Task 1: End-to-end badge state derivation** - `886e293` (test RED), `07452a4` (feat GREEN)
2. **Task 2: BadgeCatalogMapper joins entities to BadgeCellUiModel** - `78962bd` (test; implementation included in same commit)
3. **Task 3: CatalogDao and CatalogRepository read extensions** - `f6684a3` (feat)

## Files Created/Modified
- `app/src/main/java/se/simmarken/domain/model/BadgeVisualState.kt` - Four-tier visual state enum
- `app/src/main/java/se/simmarken/domain/BadgeStateCalculator.kt` - Pure compute + progressFraction
- `app/src/test/java/se/simmarken/domain/BadgeStateCalculatorTest.kt` - Full precedence test matrix
- `app/src/main/java/se/simmarken/domain/model/CatalogUiModels.kt` - BadgeCellUiModel, CategorySection
- `app/src/main/java/se/simmarken/domain/BadgeCatalogMapper.kt` - Entity-to-UI state join
- `app/src/test/java/se/simmarken/domain/BadgeCatalogMapperTest.kt` - Mapper behavior tests
- `app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt` - observeAllRequirements, observeBadgeById
- `app/src/main/java/se/simmarken/domain/repository/CatalogRepository.kt` - Interface extensions
- `app/src/main/java/se/simmarken/domain/repository/CatalogRepositoryImpl.kt` - Thin delegation

## Decisions Made
- Visual state computed at read time only — never stored in Room (D-29)
- Zero-requirement badges map to ACHIEVED_TO_BUY unless isGotten (D-30 backstop)
- Mapper counts achieved only when `requirementProgressById[id] == true` — absent keys are false

## Deviations from Plan

### TDD Gate Note

Task 2 RED/GREEN commits merged: the test commit (`78962bd`) includes the full mapper implementation because the Write step restored the GREEN implementation before staging. Tests pass; separate `feat` commit was not needed.

Otherwise: None - plan executed as written.

## Issues Encountered
- Initial Gradle wrapper download required network access in sandbox; resolved with full_network permission

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Domain foundation ready for 04-02 Compose catalog grid and 04-03 visual state styling
- ViewModels can combine `observeAllRequirements`, badge flows, and `BadgeCatalogMapper.toCategorySection`

---
*Phase: 04-catalog-view-visual-states*
*Completed: 2026-07-23*

## Self-Check: PASSED

All key files and task commits verified on disk.
