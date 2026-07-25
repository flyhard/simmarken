---
status: diagnosed
phase: 01-android-foundation-database
source: [01-VERIFICATION.md]
started: 2026-07-22T18:55:00Z
updated: 2026-07-25T07:15:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Install debug APK and launch app
expected: App opens to Home screen showing title "Simmärken" and "Kids: 0"
result: pass

### 2. Tap "Add test kid" button
expected: "Kids: 1" (or higher) appears without manual refresh
result: pass

### 3. Force-stop and relaunch app
expected: Kid count remains after app restart (not reset to 0)
result: pass

### 4. Run connected instrumented tests
expected: `./gradlew :app:connectedDebugAndroidTest` exits 0
result: issue
reported: "CatalogSeedLoaderTest.nullImageBadgesHaveCategoryFallbackColor FAILED at line 114 — connectedDebugAndroidTest BUILD FAILED (1 failure in 14 tests)"
severity: major

## Summary

total: 4
passed: 3
issues: 1
pending: 0
skipped: 0
blocked: 0

## Gaps

- gap_id: G-01-4
  truth: "./gradlew :app:connectedDebugAndroidTest exits 0"
  status: failed
  reason: "User reported: CatalogSeedLoaderTest.nullImageBadgesHaveCategoryFallbackColor FAILED at line 114 — connectedDebugAndroidTest BUILD FAILED (1 failure in 14 tests)"
  severity: major
  test: 4
  root_cause: "Test asserts colors.size > 1 (multiple distinct category fallback colors among null-image badges), but seed data now has null-image badges only in konstsims (2 badges, one category) after badge images were added to simidrott.json"
  artifacts:
    - path: "app/src/androidTest/java/se/simmarken/data/seed/CatalogSeedLoaderTest.kt"
      issue: "Line 114 assertTrue(colors.size > 1) fails when only one category has null-image badges"
    - path: "app/src/main/assets/seed/simidrott.json"
      issue: "Only konstsims category badges konstsimsmarke-1-guld and konstsimsmarke-2-guld lack imageAssetPath"
  missing:
    - "Relax or scope test assertion: require distinct colors only when null-image badges span multiple categories"
  debug_session: ""
