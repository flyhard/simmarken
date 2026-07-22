---
phase: 02-catalog-seeding
verified: 2026-07-22T20:05:00Z
status: human_needed
score: 9/9 must-haves verified
overrides_applied: 0
human_verification:
  - test: "Walk D-12 checklists against official sources for all 37 seeded badges"
    expected: "Every requirement bullet in simidrott.json and sls.json matches official PDF/shop text; mark verified columns in SIMIDROTT-CHECKLIST.md and SLS-CHECKLIST.md"
    why_human: "D-12 requires manual affisch/protocol review for every badge; only Baddaren, Hajen, Droppen, and Skräddaren have automated golden tests"
  - test: "Legal review of docs/SOURCES.md licensing statements"
    expected: "Promotional-use basis for Simidrott affisch images and SLS shop images is acceptable for app distribution"
    why_human: "VALIDATION.md defers image licensing to human legal review"
  - test: "Run instrumented seed tests on emulator/device"
    expected: "./gradlew :app:connectedDebugAndroidTest --tests \"se.simmarken.data.seed.*\" exits 0"
    why_human: "No emulator connected during verification; androidTest APK compiles but runtime not executed"
  - test: "Install debug APK, launch on fresh install, confirm catalogs seeded"
    expected: "After first launch, Room contains simidrott and sls catalogs (inspect via Database Inspector or temporary debug query)"
    why_human: "Roadmap SC1 requires first-run population on device; AppContainer seeds asynchronously on IO dispatcher"
---

# Phase 2: Catalog Seeding Verification Report

**Phase Goal:** Both Svensk Simidrott and SLS badge catalogs are pre-loaded with categories, badges, requirements, and images  
**Verified:** 2026-07-22T20:05:00Z  
**Status:** human_needed  
**Re-verification:** No — initial verification

> **MVP mode note:** ROADMAP marks this phase `mode: mvp`, but the phase-level goal is not user-story formatted. Individual plans carry user stories; verification uses standard goal-backward checks against roadmap success criteria and plan must-haves.

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | App launches with both catalogs populated in Room on first run | ✓ VERIFIED (code) | `AppContainer` launches `CatalogSeedLoader.seedIfNeeded()` on `Dispatchers.IO`; `CatalogSeedLoaderTest.firstRunPopulatesBothCatalogs` and `CatalogSeedLoaderSmokeTest` assert both catalogs; runtime device confirmation pending human |
| 2 | Svensk Simidrott core progression (Vattenvana through Guld) bundled as JSON seed | ✓ VERIFIED | `app/src/main/assets/seed/simidrott.json` — `catalogVersion: 2026.03.02`, 7 categories (`vattenvana` … `guld`), 20 badges |
| 3 | SLS main progression badges bundled as JSON seed | ✓ VERIFIED | `app/src/main/assets/seed/sls.json` — `catalogVersion: 2026.07.22`, 8 categories, 17 badges |
| 4 | Categories and badges ordered by sortOrder within each catalog | ✓ VERIFIED | JSON `sortOrder` fields; `SlsRequirementAccuracyTest.categoriesOrderedBySortOrder`; `CatalogSeedLoaderTest.categoriesOrderedBySortOrder` |
| 5 | Sampled badge requirements match official sources (Baddaren, Hajen, SLS basics) | ✓ VERIFIED | `SimidrottRequirementAccuracyTest` (baddaren-gron count/text, hajen-silver count/order); `SlsRequirementAccuracyTest` (droppen, skraddaren); `./gradlew :app:testDebugUnitTest --tests "se.simmarken.data.seed.*"` BUILD SUCCESSFUL |
| 6 | Each badge has a visual identifier (image or tier-color placeholder) | ✓ VERIFIED | 32 WebP assets under `assets/badges/` (15 simidrott + 17 sls per 02-04-SUMMARY); 5 simidrott badges use `imageAssetPath: null`; `BadgePlaceholderColors` tier palette; `CatalogSeedLoaderTest.badgeImagesResolvable` and `nullImageBadgesHaveCategoryFallbackColor` |
| 7 | Catalog version metadata stored for future updates | ✓ VERIFIED | `CatalogEntity.catalogVersion`; `CatalogVersion.shouldMerge` compares `YYYY.MM.DD`; seed JSON carries version; merge triggered when bundled > stored |
| 8 | Catalog merge preserves kid progress when catalogVersion bumps | ✓ VERIFIED (code) | `CatalogSeedMergeTest.progressPreservedWhenTextChanges` — requirement ID stable, progress row survives text update after version bump; instrumented runtime pending human |
| 9 | Seed loader skips work when bundled version equals stored version | ✓ VERIFIED | `CatalogVersionTest.shouldMergeReturnsFalseWhenEqual`; `CatalogSeedLoaderTest.secondSeedRunDoesNotDuplicate` |

**Score:** 9/9 truths verified programmatically; 4 human items remain

### Required Artifacts

| Artifact | Expected | Status | Details |
| -------- | ----------- | ------ | ------- |
| `app/src/main/assets/seed/simidrott.json` | Full Simidrott catalog seed | ✓ VERIFIED | 624 lines; `code: simidrott`, `catalogVersion: 2026.03.02` |
| `app/src/main/assets/seed/sls.json` | Curated SLS catalog seed | ✓ VERIFIED | `code: sls`, 17 badges across 8 categories |
| `app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt` | Version-gated merge loader | ✓ VERIFIED | 82 lines; `seedIfNeeded`, ID-preserving merge via `find*ByCode` |
| `app/src/main/java/se/simmarken/data/seed/CatalogSeedDto.kt` | kotlinx-serialization DTOs | ✓ VERIFIED | Maps to all 4 catalog entity types |
| `app/src/main/java/se/simmarken/data/seed/CatalogVersion.kt` | Version comparator | ✓ VERIFIED | `shouldMerge` with null = first run |
| `app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt` | Lookup-by-code queries | ✓ VERIFIED | `findCatalogByCode`, `findCategoryByCatalogAndCode`, `findBadgeByCategoryAndCode`, `findRequirementByBadgeAndCode` |
| `app/src/main/java/se/simmarken/di/AppContainer.kt` | Startup seed wiring | ✓ WIRED | `CoroutineScope(IO).launch { CatalogSeedLoader(...).seedIfNeeded() }` |
| `app/src/test/java/se/simmarken/data/seed/SimidrottRequirementAccuracyTest.kt` | Golden tests Baddaren/Hajen | ✓ VERIFIED | 5 tests; bullet counts and verbatim `textSv` |
| `app/src/test/java/se/simmarken/data/seed/SlsRequirementAccuracyTest.kt` | Golden tests Droppen/Skräddaren | ✓ VERIFIED | 5 tests including bilingual text check |
| `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderTest.kt` | First-run + image/fallback | ✓ VERIFIED | 6 tests; compiles; runtime not executed |
| `app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt` | Progress preservation | ✓ VERIFIED | `progressPreservedWhenTextChanges`; compiles |
| `docs/SOURCES.md` | Licensing and provenance | ✓ VERIFIED | Simidrott promotional-use statement, SLS shop sources, image paths |
| `docs/extraction/SIMIDROTT-CHECKLIST.md` | D-12 verification checklist | ✓ VERIFIED (exists) | 20 badges listed; all `verified` columns unchecked `[ ]` — human sign-off pending |
| `docs/extraction/SLS-CHECKLIST.md` | D-12 SLS checklist | ✓ VERIFIED (exists) | 17 badges listed; all `verified` columns unchecked `[ ]` |
| `app/src/main/java/se/simmarken/ui/badge/BadgePlaceholderColors.kt` | Tier color fallback | ✓ VERIFIED | Maps simidrott + SLS category codes to `Color` values |
| `app/src/main/assets/badges/simidrott/*.webp` | Simidrott pin images | ✓ VERIFIED | Sample `baddaren_gron.webp` confirmed; 15 per plan |
| `app/src/main/assets/badges/sls/*.webp` | SLS pin images | ✓ VERIFIED | Sample `droppen.webp` confirmed; 17 per plan |

### Key Link Verification

| From | To | Via | Status | Details |
| ---- | --- | --- | ------ | ------- |
| `AppContainer.kt` | `CatalogSeedLoader.seedIfNeeded` | IO coroutine in `init` | ✓ WIRED | Line 27–29 |
| `CatalogSeedLoader.kt` | `assets/seed/simidrott.json` | `CatalogSeedParser.loadFromAssets` | ✓ WIRED | `SEED_ASSET_PATHS` list |
| `CatalogSeedLoader.kt` | `CatalogDao.findCatalogByCode` | version gate in `seedIfNeeded` | ✓ WIRED | `CatalogVersion.shouldMerge` before `mergeCatalog` |
| `CatalogSeedLoader.kt` | Room entities | `toEntity(existingId)` + upsert | ✓ WIRED | ID-preserving merge in `mergeCategory/Badge/Requirement` |
| `simidrott.json imageAssetPath` | `assets/badges/simidrott/*.webp` | matching filename | ✓ WIRED | e.g. `badges/simidrott/baddaren_gron.webp` opens in test |
| `BadgePlaceholderColors` | null `imageAssetPath` badges | `forCategoryCode` | ✓ WIRED | Test asserts non-default colors for null-image badges |
| `docs/SOURCES.md` | affisch PDF promotional license | licensing section | ✓ WIRED | "marketing and promotion of simmärken" statement |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| -------- | ------------- | ------ | ------------------ | ------ |
| `CatalogSeedLoader` | `CatalogSeedDto` | `assets/seed/*.json` via strict JSON parse | Yes — 37 badges, hundreds of requirements | ✓ FLOWING |
| `CatalogEntity` | `catalogVersion` | seed JSON `catalogVersion` field | Yes — `2026.03.02` / `2026.07.22` | ✓ FLOWING |
| `BadgeEntity` | `imageAssetPath` | seed JSON + bundled WebP | Yes — 32 resolved paths, 5 null with fallback | ✓ FLOWING |
| `RequirementProgressEntity` | progress rows | kid upsert + merge by stable `code` | Yes — merge test preserves `requirementId` | ✓ FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| -------- | ------- | ------ | ------ |
| Seed unit tests pass | `./gradlew :app:testDebugUnitTest --tests "se.simmarken.data.seed.*"` | BUILD SUCCESSFUL | ✓ PASS |
| Debug APK builds | `./gradlew :app:assembleDebug` | BUILD SUCCESSFUL | ✓ PASS |
| androidTest compiles | `./gradlew :app:assembleDebugAndroidTest` | BUILD SUCCESSFUL | ✓ PASS |
| Simidrott seed parseable | `simidrott.json` contains `baddaren-gron-01` verbatim text | Matches golden test expectation | ✓ PASS |
| SLS seed has droppen | `sls.json` droppen-01 textSv | "För att ta Droppen finns inga kunskapskrav." | ✓ PASS |

### Probe Execution

Step 7c: SKIPPED — no probe scripts declared for this phase.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| ----------- | ---------- | ----------- | ------ | -------- |
| CATA-01 | 02-01, 02-03 | Svensk Simidrott catalog pre-loaded | ✓ SATISFIED | `simidrott.json` + `CatalogSeedLoader` populates Room |
| CATA-02 | 02-02, 02-03 | SLS catalog pre-loaded | ✓ SATISFIED | `sls.json` + loader integration |
| CATA-03 | 02-01, 02-02, 02-03 | Badges grouped by category | ✓ SATISFIED | Category hierarchy in JSON; `sortOrder` ordering tests |
| CATA-04 | 02-01, 02-02, 02-03 | Official skill requirements as checklist | ✓ SATISFIED | Requirements per badge in JSON; golden tests for samples; D-12 full manual review pending |
| CATA-05 | 02-04, 02-03 | Image or visual identifier per badge | ✓ SATISFIED | 32 WebP images + `BadgePlaceholderColors` for null paths |

**Orphaned requirements:** None — all five CATA IDs declared in plan frontmatter and mapped to implementation evidence.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| ---- | ---- | ------- | -------- | ------ |
| `docs/extraction/SIMIDROTT-CHECKLIST.md` | 9–28 | All `verified` checkboxes `[ ]` | ℹ️ Info | D-12 human sign-off not recorded; expected per VALIDATION.md manual-only table |
| `docs/extraction/SLS-CHECKLIST.md` | 9–25 | All `verified` checkboxes `[ ]` | ℹ️ Info | Same as above |
| `BadgePlaceholderColors.kt` | 30 | `forBadgeCode` delegates to `forCategoryCode(badgeCode)` | ℹ️ Info | Badge codes won't match category map; tests use `forCategoryCode` directly — Phase 4 should use parent category code |

No `TBD`/`FIXME`/`XXX` debt markers in phase-delivered source files.

### Human Verification Required

### 1. D-12 full badge accuracy review

**Test:** Walk `docs/extraction/SIMIDROTT-CHECKLIST.md` and `docs/extraction/SLS-CHECKLIST.md` against official protocol PDFs and shop product pages  
**Expected:** Every badge requirement bullet matches `textSv` in seed JSON; mark `verified` columns  
**Why human:** CONTEXT D-12 requires all 37 badges manually verified; automated golden tests cover only 4 sample badges

### 2. Image licensing legal review

**Test:** Review `docs/SOURCES.md` promotional-use and shop-image statements  
**Expected:** Distribution basis is acceptable for app store / internal release  
**Why human:** VALIDATION.md explicitly defers licensing to human legal review

### 3. Instrumented seed test execution

**Test:** `./gradlew :app:connectedDebugAndroidTest --tests "se.simmarken.data.seed.*"` with emulator running  
**Expected:** `CatalogSeedLoaderTest`, `CatalogSeedMergeTest`, `CatalogSeedLoaderSmokeTest` all pass  
**Why human:** No emulator connected; tests compile but runtime not executed in verifier environment

### 4. First-run seed on device

**Test:** Fresh install `./gradlew :app:installDebug`, launch app, inspect database  
**Expected:** `catalogs` table contains `simidrott` and `sls` rows with correct `catalogVersion`  
**Why human:** Roadmap SC1; async IO seed timing requires device confirmation

### Gaps Summary

No blocking code gaps found. All four plans are implemented in source with substantive (non-stub) seed data, loader, merge logic, images, and documentation. Unit tests pass. Status is `human_needed` because D-12 manual verification checklists are unsigned, image licensing awaits legal review, and instrumented/device runtime checks could not be completed in the verifier environment.

**Planning doc drift (informational):** ROADMAP.md still lists 02-03 as incomplete (3/4 plans) while `02-03-SUMMARY.md` and all loader artifacts exist in the codebase.

---

_Verified: 2026-07-22T20:05:00Z_  
_Verifier: Claude (gsd-verifier)_
