---
status: partial
phase: 02-catalog-seeding
source: [02-VERIFICATION.md]
started: 2026-07-22T20:10:00Z
updated: 2026-07-22T20:10:00Z
---

## Current Test

[awaiting human testing]

## Tests

### 1. D-12 full badge accuracy review
expected: Walk `docs/extraction/SIMIDROTT-CHECKLIST.md` and `docs/extraction/SLS-CHECKLIST.md` against official protocol PDFs and shop product pages; every badge requirement bullet matches `textSv` in seed JSON; mark `verified` columns
result: [pending]

### 2. Image licensing legal review
expected: Review `docs/SOURCES.md` promotional-use and shop-image statements; distribution basis acceptable for app store / internal release
result: [pending]

### 3. Instrumented seed test execution
expected: `./gradlew :app:connectedDebugAndroidTest --tests "se.simmarken.data.seed.*"` with emulator running — all seed instrumented tests pass
result: [pending]

### 4. First-run seed on device
expected: Fresh install `./gradlew :app:installDebug`, launch app, inspect database — `catalogs` table contains `simidrott` and `sls` rows with correct `catalogVersion`
result: [pending]

## Summary

total: 4
passed: 0
issues: 0
pending: 4
skipped: 0
blocked: 0

## Gaps
