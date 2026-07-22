---
phase: 02-catalog-seeding
plan: 04
subsystem: ui
tags: [android, webp, cwebp, badge-images, licensing, compose, assets]

requires:
  - plan: 02-01
    provides: simidrott.json with 15 imageAssetPath entries and affisch PDF extraction
  - plan: 02-02
    provides: sls.json with 17 imageAssetPath entries and shop product URLs
provides:
  - docs/SOURCES.md licensing and provenance per D-03/D-16
  - 15 Simidrott + 17 SLS WebP badge pin assets (272 KB total)
  - convert-badge-images.sh and catalog-specific extraction scripts
  - BadgePlaceholderColors tier fallback palette for Phase 4 Coil
affects:
  - 02-03 (CatalogSeedLoader image path resolution tests)
  - Phase 4 (Coil badge grid and placeholder UI)

tech-stack:
  added: []
  patterns:
    - "WebP 256px via cwebp -q 85 -metadata none"
    - "Affisch pdfimages num→filename mapping for Simidrott pins"
    - "SLS images from shop product JSON first image URL"
    - "BadgePlaceholderColors.forCategoryCode / forBadgeCode tier hues"

key-files:
  created:
    - docs/SOURCES.md
    - scripts/convert-badge-images.sh
    - scripts/extract-simidrott-badge-images.sh
    - scripts/extract-sls-badge-images.sh
    - app/src/main/assets/badges/simidrott/*.webp
    - app/src/main/assets/badges/sls/*.webp
    - app/src/main/java/se/simmarken/ui/badge/BadgePlaceholderColors.kt
  modified: []

key-decisions:
  - "Simidrott pins mapped from affisch embedded images by progression order (pdfimages indices 003–104)"
  - "SLS images downloaded from official shop product JSON at 512px width before WebP conversion"
  - "Five Simidrott badges retain null imageAssetPath with tier fallback (simsättsmärken + Kandidaten)"

patterns-established:
  - "Maintainer image pipeline: extract scripts → convert-badge-images.sh → assets/badges/{catalog}/"
  - "Image provenance in docs/SOURCES.md only — no Room schema fields (D-16)"

requirements-completed: [CATA-05]

duration: 25min
completed: 2026-07-22
---

# Phase 2 Plan 4: Badge Image Bundling Summary

**Official Simidrott and SLS pin WebP assets (32 files, 272 KB) with SOURCES.md licensing and tier-color fallback palette**

## Performance

- **Duration:** 25 min
- **Started:** 2026-07-22T17:33:00Z
- **Completed:** 2026-07-22T17:58:00Z
- **Tasks:** 3
- **Files modified:** 37

## Accomplishments

- `docs/SOURCES.md` documents Simidrott promotional license, SLS shop sources, WebP format, and `catalogVersion` semantics
- 15 Simidrott WebP pins extracted from official simmärkesaffisch PDF via `pdfimages` mapping
- 17 SLS WebP pins downloaded from official shop product images
- `BadgePlaceholderColors` supplies distinct tier hues for five Simidrott badges without bundled artwork
- Total badge asset footprint: 272 KB (well under 5 MB APK budget)

## Task Commits

1. **Task 1: Create SOURCES.md with licensing and provenance** - `2fb1c4d` (docs)
2. **Task 2: Extract and convert badge images to WebP** - `e2dbb26` (feat)
3. **Task 3: Define tier-color fallback palette for missing images** - `943cc1e` (feat)

**Plan metadata:** `3a21434` (docs: complete plan)

## Files Created/Modified

- `docs/SOURCES.md` - Licensing, image sources, catalogVersion format, gap table
- `scripts/convert-badge-images.sh` - Generic cwebp 256px converter with `--help`
- `scripts/extract-simidrott-badge-images.sh` - Affisch PDF → 15 WebP pins
- `scripts/extract-sls-badge-images.sh` - Shop JSON → 17 WebP pins
- `app/src/main/assets/badges/simidrott/*.webp` - 15 official Simidrott pin images
- `app/src/main/assets/badges/sls/*.webp` - 17 official SLS shop product images
- `app/src/main/java/se/simmarken/ui/badge/BadgePlaceholderColors.kt` - D-04 tier fallback colors

## Decisions Made

- Simidrott image mapping uses affisch embedded image numbers (003–036 progression, 098/101/104 for Järn/Brons/Silver märken)
- SLS images sourced from shop product JSON `images[0].src` at 512px CDN width
- Kandidaten and simsättsmärken 1–4 documented as tier-fallback gaps (no WebP in v1 priority set)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Fixed affisch extraction script temp-dir collision**
- **Found during:** Task 2
- **Issue:** `extract-simidrott-badge-images.sh` deleted extracted PNGs by cleaning the same directory used for `pdfimages` output
- **Fix:** Split `EXTRACT_DIR` and `CROP_DIR` into separate `/tmp` paths
- **Files modified:** `scripts/extract-simidrott-badge-images.sh`
- **Committed in:** `e2dbb26`

**2. [Rule 3 - Blocking] Fixed octal printf for affisch image numbers**
- **Found during:** Task 2
- **Issue:** `printf '%03d' 009` failed with "invalid number" (octal interpretation)
- **Fix:** Added `pad3()` helper using `$((10#$1))` for decimal coercion
- **Files modified:** `scripts/extract-simidrott-badge-images.sh`
- **Committed in:** `e2dbb26`

---

**Total deviations:** 2 auto-fixed (both blocking script issues)
**Impact on plan:** Script fixes required to produce WebP assets; no scope change.

## Issues Encountered

- Gradle `assembleDebug` required unsandboxed execution (FileLockContentionHandler / wildcard IP)

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- All seed `imageAssetPath` values resolve to bundled WebP files (32/32 non-null paths)
- `BadgePlaceholderColors` ready for Phase 4 Coil `placeholder`/`error` callbacks
- D-12 manual verification of image accuracy recommended before phase sign-off

---
*Phase: 02-catalog-seeding*
*Completed: 2026-07-22*

## Self-Check: PASSED

- FOUND: docs/SOURCES.md
- FOUND: app/src/main/assets/badges/simidrott/baddaren_gron.webp
- FOUND: app/src/main/java/se/simmarken/ui/badge/BadgePlaceholderColors.kt
- FOUND: commit 2fb1c4d
- FOUND: commit e2dbb26
- FOUND: commit 943cc1e
