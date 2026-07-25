---
phase: 02-catalog-seeding
reviewed: 2026-07-22T17:55:00Z
depth: standard
files_reviewed: 24
files_reviewed_list:
  - app/src/main/assets/seed/simidrott.json
  - app/src/main/assets/seed/sls.json
  - app/src/test/resources/seed/simidrott_sample.json
  - app/src/main/java/se/simmarken/data/seed/CatalogSeedDto.kt
  - app/src/main/java/se/simmarken/data/seed/CatalogSeedParser.kt
  - app/src/main/java/se/simmarken/data/seed/CatalogVersion.kt
  - app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt
  - app/src/main/java/se/simmarken/data/local/dao/CatalogDao.kt
  - app/src/main/java/se/simmarken/di/AppContainer.kt
  - app/src/main/java/se/simmarken/ui/badge/BadgePlaceholderColors.kt
  - scripts/extract-simidrott.sh
  - scripts/build-simidrott-json.py
  - scripts/extract-sls.sh
  - scripts/convert-badge-images.sh
  - scripts/extract-simidrott-badge-images.sh
  - scripts/extract-sls-badge-images.sh
  - app/src/test/java/se/simmarken/data/seed/SimidrottRequirementAccuracyTest.kt
  - app/src/test/java/se/simmarken/data/seed/SlsRequirementAccuracyTest.kt
  - app/src/test/java/se/simmarken/data/seed/CatalogVersionTest.kt
  - app/src/test/java/se/simmarken/data/seed/CatalogSeedParserTest.kt
  - app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderSmokeTest.kt
  - app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderTest.kt
  - app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt
  - .gitignore
findings:
  critical: 0
  warning: 6
  info: 2
  total: 8
status: issues_found
---

# Phase 2: Code Review Report

**Reviewed:** 2026-07-22T17:55:00Z
**Depth:** standard
**Files Reviewed:** 24
**Status:** issues_found

## Summary

Phase 02 delivers a well-structured catalog seeding pipeline: strict JSON parsing, version-gated merge, ID-preserving Room upserts, golden requirement tests, and maintainer extraction scripts. Core merge logic and progress-preservation behavior are sound and covered by instrumented tests.

The main risks are **startup timing** (seed runs fire-and-forget with no completion signal before Phase 3 catalog UI), a **broken `forBadgeCode` API** that will mis-color placeholders in Phase 4, and **maintainer script inconsistencies** (affisch PDF filename mismatch, invalid bash URL stripping) that block reproducible asset regeneration. No security vulnerabilities or data-loss bugs were found in the production Kotlin path.

## Narrative Findings (AI reviewer)

## Warnings

### WR-01: Seed runs asynchronously with no completion guarantee

**File:** `app/src/main/java/se/simmarken/di/AppContainer.kt:26-29`
**Issue:** `seedIfNeeded()` is launched in a fire-and-forget coroutine with no `CoroutineExceptionHandler`, no exposed `Deferred`/Flow, and no synchronization before repositories are used. Phase 02 context requires seeding "before UI needs catalog data"; Phase 3 catalog screens can observe an empty database on cold start or after seed failures that only appear in logcat.
**Fix:** Expose seed readiness from `AppContainer` (e.g. `val seedingComplete: Deferred<Unit>` or a `StateFlow<SeedState>`) and have catalog UI wait on it; add an exception handler that logs and surfaces a recoverable error state.

```kotlin
// AppContainer.kt — sketch
private val seedJob: Deferred<Unit>
init {
    seedJob = CoroutineScope(SupervisorJob() + Dispatchers.IO).async {
        CatalogSeedLoader(appContext, database).seedIfNeeded()
    }
}
suspend fun awaitSeed() = seedJob.await()
```

### WR-02: `forBadgeCode` looks up category colors with badge codes

**File:** `app/src/main/java/se/simmarken/ui/badge/BadgePlaceholderColors.kt:30`
**Issue:** `forBadgeCode(badgeCode)` delegates to `forCategoryCode(badgeCode)`. Badge codes (`simsattmarke-1`, `baddaren-gron`) do not match category map keys (`nyborjare`, `vattenvana`). Any Phase 4 caller using `forBadgeCode` will get `default` gray instead of the tier hue. Instrumented tests correctly use `forCategoryCode`, masking this defect.
**Fix:** Accept `categoryCode` (and optional `badgeCode`) or maintain a badge→category map derived from seed codes:

```kotlin
fun forBadge(categoryCode: String, badgeCode: String): Color =
    forCategoryCode(categoryCode)

// Or map badge codes explicitly when category context is unavailable
```

### WR-03: Affisch PDF paths disagree between extraction scripts

**File:** `scripts/extract-simidrott.sh:49`, `scripts/extract-simidrott-badge-images.sh:6`
**Issue:** `extract-simidrott.sh` downloads `docs/extraction/pdfs/simidrott/simmarkesaffisch.pdf` (ASCII, no year suffix). `extract-simidrott-badge-images.sh` expects `docs/extraction/pdfs/simidrott/simmärkesaffisch-2024.pdf` (umlaut, `-2024` suffix). Running the documented maintainer pipeline end-to-end fails without manual renaming.
**Fix:** Align on one canonical filename in both scripts and document it in `docs/SOURCES.md`, e.g.:

```bash
# Both scripts
AFFISCH_PDF="docs/extraction/pdfs/simidrott/simmarkesaffisch.pdf"
```

### WR-04: SLS image URL width stripping uses invalid bash glob syntax

**File:** `scripts/extract-sls-badge-images.sh:74`
**Issue:** `${img_url//&width=[0-9]*/}` is a glob replacement, not a regex. For URLs like `...&width=512`, bash matches `&width=5` only (single digit in `[0-9]`), leaving a corrupted query string and potentially wrong CDN dimensions.
**Fix:** Strip width with `sed` or Python (already used for JSON parsing):

```bash
img_url="$(printf '%s' "$img_url" | sed -E 's/[&?]width=[0-9]+//')"
```

### WR-05: Seed failures are uncaught and leave no user-visible recovery path

**File:** `app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt:15-23`, `app/src/main/java/se/simmarken/data/seed/CatalogVersion.kt:13-20`
**Issue:** `CatalogSeedParser.loadFromAssets` (strict JSON) and `CatalogVersion.parse` throw on malformed bundled assets or corrupt stored `catalogVersion`. These propagate into the unhandled `launch` block in `AppContainer`, aborting seeding with no retry surface. A partially migrated or manually corrupted DB version string can permanently block catalog updates.
**Fix:** Wrap per-catalog merge in `runCatching`, validate `catalogVersion` format before parse, and fall back to full merge when stored version is unparsable (with logging).

### WR-06: Merge does not prune removed categories or requirements

**File:** `app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt:26-73`
**Issue:** v1 merge upserts by code but never deletes categories, badges, or requirements absent from newer seed JSON (requirements called out in comment at line 72; same applies upstream). Users who earned progress on removed requirements retain stale rows; renamed/moved badges can orphan old rows because lookup is scoped to parent ID + code.
**Fix:** Document as accepted v1 limitation or add explicit prune pass (delete requirements not in seed set per badge) behind a version bump gate.

## Info

### IN-01: `mergeCatalog` is public and bypasses version gate

**File:** `app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt:26`
**Issue:** `mergeCatalog` can be invoked without `CatalogVersion.shouldMerge`, allowing forced merges in tests (intentional) but also accidental production use if exposed beyond test harness.
**Fix:** Restrict to `internal` for app module or add an explicit `force: Boolean` parameter guarded in production call sites.

### IN-02: `CatalogVersionTest` omits regression cases for skip and invalid input

**File:** `app/src/test/java/se/simmarken/data/seed/CatalogVersionTest.kt`
**Issue:** No test asserts `shouldMerge` returns `false` when bundled version is older than stored, and no test covers invalid version strings. These gaps match WR-05 risk.
**Fix:** Add `shouldMergeReturnsFalseWhenBundledOlder` and `parseRejectsInvalidFormat` tests.

---

_Reviewed: 2026-07-22T17:55:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
