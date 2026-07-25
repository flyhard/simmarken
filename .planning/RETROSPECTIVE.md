# Project Retrospective

*A living document updated after each milestone. Lessons feed forward into future planning.*

## Milestone: v1.0 — MVP

**Shipped:** 2026-07-25
**Phases:** 6 | **Plans:** 21 | **Tasks:** 57

### What Was Built

- Local-first Android app (Kotlin, Compose, Room) with offline persistence and MVVM architecture
- Svensk Simidrott (20 badges) and SLS (17 badges) catalogs seeded from official sources with WebP pin images
- Kid-first home with child profile CRUD, avatar colors, and catalog navigation
- 4-tier badge visual states (locked → in progress → achieved-to-buy → gotten) with Coil rendering
- Skill checklist with auto-achieve, purchase toggle, and home summary counts
- Versioned JSON export/import with newer-wins merge for device migration
- Bilingual Swedish/English UI with runtime language switching via DataStore

### What Worked

- Vertical MVP slices delivered incrementally testable value — each phase produced a runnable app state
- Centralized domain logic (BadgeStateCalculator, BadgeCatalogMapper) prevented state duplication across grid, detail, and home
- Golden tests on seed JSON caught catalog accuracy regressions early (affisch requirements, SLS shop data)
- Gap-closure plans (02-05, 06-04) addressed verification failures without scope creep
- Official source extraction tooling in `docs/extraction/` made catalog updates reproducible

### What Was Inefficient

- Phase 2 required a gap-closure plan when REPLACE upserts broke FK cascades — caught late in instrumented tests
- SLS requirement sourcing had lower confidence than Simidrott; needed extra verification passes
- STATE.md drifted from actual progress (showed Phase 02 while Phase 06 was completing)

### Patterns Established

- BadgeVisualState derived at read time — never persisted (D-29)
- ID-preserving catalog merge via lookup-by-code before upsert (D-15)
- ViewModel ioDispatcher constructor param for JVM tests with Dispatchers.IO in production
- StringsParityTest regex gate for sv/en locale key parity
- Parse → preview → confirm → transactional merge for import flow (D-03)

### Key Lessons

1. Destructive Room upserts on FK-linked tables need explicit merge strategy from day one — test with instrumented FK chains
2. Single BadgeStateCalculator path shared across grid, detail, and home prevents subtle state inconsistencies
3. Catalog content (badge names, requirements) stays Swedish regardless of UI locale — avoids translation drift from official sources
4. Gap-closure plans after verification are cheaper than discovering issues in later phases

### Cost Observations

- Timeline: 2026-07-22 → 2026-07-25 (4 days, 6 phases)
- 21 plans across 6 phases with 2 gap-closure plans
- Notable: Phase 6 export/import (90min) was the longest single plan

---

## Cross-Milestone Trends

### Process Evolution

| Milestone | Sessions | Phases | Key Change |
|-----------|----------|--------|------------|
| v1.0 | — | 6 | Initial GSD vertical MVP delivery |

### Cumulative Quality

| Milestone | Plans | Gap Closures | Verification |
|-----------|-------|--------------|--------------|
| v1.0 | 21 | 2 (02-05, 06-04) | All 6 phases passed |

### Top Lessons (Verified Across Milestones)

1. Centralize computed state in domain layer — BadgeStateCalculator pattern proved essential
2. Golden tests on seed data catch catalog accuracy regressions before UI work begins
