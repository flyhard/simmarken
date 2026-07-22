---
status: partial
phase: 01-android-foundation-database
source: [01-VERIFICATION.md]
started: 2026-07-22T18:55:00Z
updated: 2026-07-22T18:55:00Z
---

## Current Test

awaiting human testing

## Tests

### 1. Install debug APK and launch app
expected: App opens to Home screen showing title "Simmärken" and "Kids: 0"
result: [pending]

### 2. Tap "Add test kid" button
expected: "Kids: 1" (or higher) appears without manual refresh
result: [pending]

### 3. Force-stop and relaunch app
expected: Kid count remains after app restart (not reset to 0)
result: [pending]

### 4. Run connected instrumented tests
expected: `./gradlew :app:connectedDebugAndroidTest` exits 0
result: [pending]

## Summary

total: 4
passed: 0
issues: 0
pending: 4
skipped: 0
blocked: 0

## Gaps
