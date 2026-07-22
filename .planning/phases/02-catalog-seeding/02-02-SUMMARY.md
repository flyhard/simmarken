---
phase: 02-catalog-seeding
plan: 02
subsystem: database
tags: [kotlin, android, json, sls, seed-data, junit, shop-extraction]

requires:
  - phase: 01-android-foundation-database
    provides: Room entity schema with code, bilingual fields, imageAssetPath, sortOrder
  - plan: 02-01
    provides: CatalogSeedDto JSON hierarchy pattern and golden test conventions
provides:
  - sls.json bundled seed with catalogVersion 2026.07.22
  - SLS extraction script and D-12 verification checklist
  - Golden requirement accuracy tests for Droppen and Skräddaren
affects:
  - 02-03 (CatalogSeedLoader parses sls.json)
  - 02-04 (WebP images for 17 SLS badges)

tech-stack:
  added: []
  patterns:
    - "SLS requirements from official shop product Vad sections (MEDIUM confidence)"
    - "Eight stable category slugs: grund, doppingen, livbojen, krabban, uttern, krokodilen, grodan, sal"
    - "Droppen seeded with official no-requirements Hur text when Vad has no skill bullets"

key-files:
  created:
    - app/src/main/assets/seed/sls.json
    - scripts/extract-sls.sh
    - docs/extraction/SLS-CHECKLIST.md
    - docs/extraction/SLS-SOURCE-NOTES.md
    - app/src/test/java/se/simmarken/data/seed/SlsRequirementAccuracyTest.kt
  modified: []

key-decisions:
  - "SLS requirements sourced from shop.svenskalivraddningssallskapet.se product JSON (MEDIUM confidence per D-06)"
  - "GP article explicitly excluded as primary source for D-09 verbatim text"
  - "Droppen uses official Hur text as single requirement when shop states inga kunskapskrav"
  - "17 badges shipped across 8 categories — thinner than Simidrott by design"

patterns-established:
  - "Shop product JSON endpoints as SLS maintainer extraction source"
  - "Requirement codes follow {badge-code}-{nn} zero-padded convention (same as Simidrott)"

requirements-completed: [CATA-02, CATA-03, CATA-04]

duration: 18min
completed: 2026-07-22
---

# Phase 2 Plan 2: SLS Seed Extraction Summary

**SLS catalog seed (17 badges, shop-verified requirements) with extraction tooling and Droppen/Skräddaren golden tests**

## Performance

- **Duration:** 18 min
- **Started:** 2026-07-22T17:30:00Z
- **Completed:** 2026-07-22T17:48:00Z
- **Tasks:** 3
- **Files modified:** 5

## Accomplishments

- Maintainer extraction script fetches SLS simmärken page and shop product JSON into gitignored snapshots
- `sls.json` ships 17 curated badges across 8 progression categories with `catalogVersion` `2026.07.22`
- Golden tests lock Droppen (1 bullet) and Skräddaren (2 bullets) requirement accuracy from official shop
- D-12 checklist documents all included badges; GP article excluded as non-authoritative source

## Task Commits

1. **Task 1: Research SLS sources and document coverage strategy** - `0407e22` (feat)
2. **Task 2: Produce sls.json seed with verified requirements** - `742f9aa` (feat)
3. **Task 3: SLS golden requirement accuracy tests** - `c18820a` (test)

**Plan metadata:** `e98607f` (docs: complete plan)

## Files Created/Modified

- `scripts/extract-sls.sh` - Downloads SLS landing page and shop product JSON snapshots
- `docs/extraction/SLS-SOURCE-NOTES.md` - Per-badge confidence ratings and exclusion notes
- `docs/extraction/SLS-CHECKLIST.md` - D-12 per-badge verification table
- `app/src/main/assets/seed/sls.json` - Production SLS catalog seed asset
- `app/src/test/java/se/simmarken/data/seed/SlsRequirementAccuracyTest.kt` - Golden accuracy tests

## Decisions Made

- SLS requirement text from official shop product descriptions (MEDIUM confidence) — no protocol PDFs exist
- GP article excluded as primary source; shop URLs cited in golden test comments
- Droppen requirement is official "inga kunskapskrav" Hur text (shop has no Vad skill bullets)
- 17 badges within RESEARCH recommended subset; Grodan Rygg and Vädermärket excluded

## Deviations from Plan

None - plan executed exactly as written.

## Known Stubs

| File | Field | Reason |
|------|-------|--------|
| `sls.json` | `imageAssetPath` on all 17 badges | WebP assets deferred to 02-04; paths pre-declared per D-04 contract |

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `sls.json` ready for `CatalogSeedLoader` in 02-03
- 17 `imageAssetPath` entries ready for WebP bundling in 02-04
- D-12 checklist rows need human verification before phase sign-off

---
*Phase: 02-catalog-seeding*
*Completed: 2026-07-22*

## Self-Check: PASSED

- FOUND: app/src/main/assets/seed/sls.json
- FOUND: app/src/test/java/se/simmarken/data/seed/SlsRequirementAccuracyTest.kt
- FOUND: docs/extraction/SLS-CHECKLIST.md
- FOUND: docs/extraction/SLS-SOURCE-NOTES.md
- FOUND: scripts/extract-sls.sh
- FOUND: commit 0407e22
- FOUND: commit 742f9aa
- FOUND: commit c18820a
