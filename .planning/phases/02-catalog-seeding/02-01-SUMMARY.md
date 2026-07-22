---
phase: 02-catalog-seeding
plan: 01
subsystem: database
tags: [kotlin, android, json, simidrott, seed-data, junit]

requires:
  - phase: 01-android-foundation-database
    provides: Room entity schema with code, bilingual fields, imageAssetPath, sortOrder
provides:
  - simidrott.json bundled seed with catalogVersion 2026.03.02
  - PDF extraction script and D-12 verification checklist
  - Golden requirement accuracy tests for Baddaren Grön and Hajen Silver
affects:
  - 02-03 (CatalogSeedLoader parses simidrott.json)
  - 02-04 (WebP images for 15 priority badges)

tech-stack:
  added: []
  patterns:
    - "JSON seed hierarchy mirrors CatalogSeedDto (catalog → categories → badges → requirements)"
    - "Stable ASCII badge codes with verbatim affisch textSv"
    - "Golden-file JVM tests resolve project root for assets path"

key-files:
  created:
    - app/src/main/assets/seed/simidrott.json
    - scripts/extract-simidrott.sh
    - scripts/build-simidrott-json.py
    - docs/extraction/SIMIDROTT-CHECKLIST.md
    - app/src/test/java/se/simmarken/data/seed/SimidrottRequirementAccuracyTest.kt
    - app/src/test/resources/seed/simidrott_sample.json
  modified:
    - .gitignore

key-decisions:
  - "Omitted guldmarket badge — 2024 affisch progression is Järn → Brons → Silver → Kandidaten with no separate Guldmärket pin"
  - "Used kotlinx-serialization in unit tests because Android org.json is stubbed on JVM"
  - "15 badges carry imageAssetPath; simsättsmärken and Kandidaten use null for tier fallback"

patterns-established:
  - "Maintainer PDFs gitignored under docs/extraction/pdfs/; only JSON seed committed"
  - "Requirement codes follow {badge-code}-{nn} zero-padded convention"

requirements-completed: [CATA-01, CATA-03, CATA-04]

duration: 22min
completed: 2026-07-22
---

# Phase 2 Plan 1: Simidrott Seed Extraction Summary

**Svensk Simidrott core progression seed (20 badges, verbatim affisch requirements) with PDF extraction tooling and golden accuracy tests**

## Performance

- **Duration:** 22 min
- **Started:** 2026-07-22T17:18:00Z
- **Completed:** 2026-07-22T17:40:00Z
- **Tasks:** 3
- **Files modified:** 8

## Accomplishments

- Maintainer extraction script downloads affisch + protocol PDFs from svensksimidrott.se with SHA-256 checksums
- `simidrott.json` ships 20 core-scope badges across 7 affisch tiers with `catalogVersion` `2026.03.02`
- Golden tests lock Baddaren Grön (2 bullets) and Hajen Silver (2 bullets) requirement accuracy
- D-12 checklist documents all badges for manual verification before ship

## Task Commits

1. **Task 1: Download official Simidrott sources and build extraction checklist** - `2ad2a18` (feat)
2. **Task 2: Produce simidrott.json seed from affisch and protocol PDFs** - `7850ec1` (feat)
3. **Task 3: Golden requirement accuracy tests and unit-test fixture** - `4cf35aa` (test)

**Plan metadata:** pending (docs commit after state update)

## Files Created/Modified

- `scripts/extract-simidrott.sh` - Downloads official PDFs; prints pdftotext commands
- `scripts/build-simidrott-json.py` - Regenerates seed JSON from curated affisch text
- `docs/extraction/SIMIDROTT-CHECKLIST.md` - D-12 per-badge verification table
- `app/src/main/assets/seed/simidrott.json` - Production catalog seed asset
- `app/src/test/java/se/simmarken/data/seed/SimidrottRequirementAccuracyTest.kt` - Golden accuracy tests
- `app/src/test/resources/seed/simidrott_sample.json` - Minimal parser fixture for 02-03
- `.gitignore` - Ignores `docs/extraction/pdfs/`

## Decisions Made

- Omitted `guldmarket` — current affisch and SSF shop list Järnmärket through Kandidaten without a separate Guldmärket swimming badge
- Used kotlinx-serialization for JVM unit test JSON parsing (Android `org.json` throws "not mocked" in local unit tests)
- Simsättsmärke 1–4 and Kandidaten use `imageAssetPath: null` per 02-04 WebP budget contract

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed extract script for macOS bash 3.x**
- **Found during:** Task 1
- **Issue:** Associative array syntax failed on macOS default bash during `--help`
- **Fix:** Reordered `--help` before downloads; replaced `declare -A` with pipe-delimited URL list
- **Files modified:** `scripts/extract-simidrott.sh`
- **Committed in:** `2ad2a18`

**2. [Rule 1 - Data accuracy] Omitted non-existent Guldmärket badge**
- **Found during:** Task 2
- **Issue:** Plan listed `guldmarket` but 2024 affisch / markesprotokoll have no Guldmärket pin between Silvermärket and Kandidaten
- **Fix:** Guld category contains only `kandidaten`; documented in checklist notes
- **Files modified:** `simidrott.json`, `SIMIDROTT-CHECKLIST.md`
- **Committed in:** `7850ec1`

**3. [Rule 3 - Blocking] Switched test JSON parser to kotlinx-serialization**
- **Found during:** Task 3
- **Issue:** `org.json.JSONObject` methods throw "not mocked" in Android JVM unit tests
- **Fix:** Parse seed with `kotlinx.serialization.json.Json` and resolve project root via `settings.gradle.kts`
- **Files modified:** `SimidrottRequirementAccuracyTest.kt`
- **Committed in:** `4cf35aa`

---

**Total deviations:** 3 auto-fixed (1 bug, 1 data accuracy, 1 blocking)
**Impact on plan:** Guldmärket omission aligns seed with official affisch; other fixes required for script portability and test execution.

## Issues Encountered

- Gradle 8.13 wrapper download required network on first test run
- Affisch PDF multi-column layout makes automated pdftotext extraction unreliable — manual curation via `build-simidrott-json.py`

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `simidrott.json` ready for `CatalogSeedLoader` in 02-03
- 15 `imageAssetPath` entries ready for WebP bundling in 02-04
- D-12 checklist rows need human verification before phase sign-off

---
*Phase: 02-catalog-seeding*
*Completed: 2026-07-22*

## Self-Check: PASSED

- FOUND: app/src/main/assets/seed/simidrott.json
- FOUND: app/src/test/java/se/simmarken/data/seed/SimidrottRequirementAccuracyTest.kt
- FOUND: docs/extraction/SIMIDROTT-CHECKLIST.md
- FOUND: scripts/extract-simidrott.sh
- FOUND: commit 2ad2a18
- FOUND: commit 7850ec1
- FOUND: commit 4cf35aa
