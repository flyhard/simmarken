---
status: complete
phase: 02-catalog-seeding
source: [02-VERIFICATION.md]
started: 2026-07-22T20:10:00Z
updated: 2026-07-22T21:18:00Z
---

## Current Test

[testing complete]

## Tests

### 1. D-12 full badge accuracy review
expected: Walk `docs/extraction/SIMIDROTT-CHECKLIST.md` and `docs/extraction/SLS-CHECKLIST.md` against official protocol PDFs and shop product pages; every badge requirement bullet matches `textSv` in seed JSON; mark `verified` columns
result: issue
reported: "Bronsmarket, guldfisken, hajen brons, hajen guld, hajen silver, järnmärket, silverfisken och silvermärket är fel"
severity: major

### 2. Image licensing legal review
expected: Review `docs/SOURCES.md` promotional-use and shop-image statements; distribution basis acceptable for app store / internal release
result: pass

### 3. Instrumented seed test execution
expected: `./gradlew :app:connectedDebugAndroidTest --tests "se.simmarken.data.seed.*"` with emulator running — all seed instrumented tests pass
result: issue
reported: "FAILURE: Build failed with an exception. Problem configuring task :app:connectedDebugAndroidTest from command line. Unknown command-line option '--tests'."
severity: major

### 4. First-run seed on device
expected: Fresh install `./gradlew :app:installDebug`, launch app, inspect database — `catalogs` table contains `simidrott` and `sls` rows with correct `catalogVersion`
result: pass

## Summary

total: 4
passed: 2
issues: 2
pending: 0
skipped: 0
blocked: 0

## Gaps

- truth: "Every Simidrott badge requirement bullet matches official sources and bundled badge images depict the correct pin for each code"
  status: failed
  reason: "User reported: Bronsmarket, guldfisken, hajen brons, hajen guld, hajen silver, järnmärket, silverfisken och silvermärket är fel"
  severity: major
  test: 1
  deferred: true
  deferred_note: "User chose to delay artwork correction to a later phase"
  root_cause: "extract-simidrott-badge-images.sh pdfimages index mapping (024–104) assigns wrong affisch embedded images to badge filenames — e.g. silvermarket.webp is Guldbojen, hajen_silver.webp is crawl swimmer, jarnmarket.webp is Baddaren character"
  artifacts:
    - path: "scripts/extract-simidrott-badge-images.sh"
      issue: "Incorrect MAPPING array indices for 8+ badges"
    - path: "app/src/main/assets/badges/simidrott/"
      issue: "Wrong WebP assets bundled for bronsmarket, guldfisken, hajen-*, jarnmarket, silverfisken, silvermarket"
  missing:
    - "Re-map pdfimages indices against affisch layout and re-extract WebP assets"
    - "Visual spot-check all 15 Simidrott pins against official affisch"
  debug_session: ""

- truth: "All seed instrumented tests pass on emulator/device"
  status: failed
  reason: "User reported: FAILURE: Build failed with an exception. Problem configuring task :app:connectedDebugAndroidTest from command line. Unknown command-line option '--tests'."
  severity: major
  test: 3
  root_cause: "UAT/VERIFICATION docs use JVM --tests flag (invalid for connectedDebugAndroidTest); with correct package filter 7/8 pass — CatalogSeedMergeTest.progressPreservedWhenTextChanges NPE at line 100 because findRequirementByBadgeAndCode returns null after mergeCatalog (requirement not found under pre-merge badgeId)"
  artifacts:
    - path: "app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedMergeTest.kt"
      issue: "NPE at line 100 after mergeCatalog"
    - path: "app/src/main/java/se/simmarken/data/seed/CatalogSeedLoader.kt"
      issue: "ID-preserving merge may not retain requirement lookup by original badgeId"
    - path: ".planning/phases/02-catalog-seeding/02-VERIFICATION.md"
      issue: "Documents invalid --tests flag for connectedDebugAndroidTest"
  missing:
    - "Fix CatalogSeedMergeTest failure (merge ID preservation or test lookup)"
    - "Update UAT/VALIDATION docs with -Pandroid.testInstrumentationRunnerArguments.package=se.simmarken.data.seed"
  debug_session: ""
