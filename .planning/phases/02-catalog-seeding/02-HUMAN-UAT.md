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
expected: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=se.simmarken.data.seed` with emulator running — all seed instrumented tests pass
result: pass

### 4. First-run seed on device
expected: Fresh install `./gradlew :app:installDebug`, launch app, inspect database — `catalogs` table contains `simidrott` and `sls` rows with correct `catalogVersion`
result: pass

## Summary

total: 4
passed: 3
issues: 1
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
  status: resolved
  reason: "Fixed @Upsert replaces destructive REPLACE upserts preserving stable IDs; docs updated with package filter"
  severity: major
  test: 3
  root_cause: "@Insert(REPLACE) CASCADE-deleted child rows on catalog version bump; UAT docs used invalid JVM --tests flag for connectedDebugAndroidTest"
  resolution: "@Upsert in CatalogDao + -Pandroid.testInstrumentationRunnerArguments.package=se.simmarken.data.seed"
