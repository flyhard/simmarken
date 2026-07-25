---
phase: 06-export-import-i18n
plan: 04
subsystem: ui
tags: [android, i18n, accessibility, compose, talkback]

requires:
  - phase: 06-02
    provides: badge_state_* and badge_pin_content_description string keys in values/ and values-en/
  - phase: 06-03
    provides: AppCompatDelegate locale wiring for runtime language selection
provides:
  - BadgeGridItem localized accessibility contentDescription via stringResource
  - BadgeGridItemChromeTest JVM regression gate against hardcoded Swedish state literals
affects: []

tech-stack:
  added: []
  patterns:
    - "Mirror BadgeDetailScreen badgeStateLabel(): Int + badge_pin_content_description pattern in grid item"

key-files:
  created:
    - app/src/test/java/se/simmarken/ui/child/BadgeGridItemChromeTest.kt
  modified:
    - app/src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt

key-decisions:
  - "Duplicate private badgeStateLabel helper in BadgeGridItem rather than extract shared util — matches BadgeDetailScreen and minimizes gap-closure diff"

patterns-established:
  - "Source-scan JVM test for composable i18n regression (BadgeGridItemChromeTest)"

requirements-completed: [I18N-01, I18N-02]

coverage:
  - id: D1
    description: "BadgeGridItem contentDescription uses stringResource with badge_state_* keys (verification truths #16, #17)"
    requirement: I18N-01
    verification:
      - kind: unit
        ref: "app/src/test/java/se/simmarken/ui/child/BadgeGridItemChromeTest.kt#badgeGridItem_usesStringResources_notHardcodedSwedishStateLiterals"
        status: pass
      - kind: unit
        ref: "./gradlew :app:compileDebugKotlin"
        status: pass
    human_judgment: false
  - id: D2
    description: "English TalkBack announces localized state labels on catalog grid"
    requirement: I18N-02
    verification: []
    human_judgment: true
    rationale: "TalkBack announcement language requires device with English locale selected"
  - id: D3
    description: "Remaining Phase 6 device behaviors (export share, import confirm, locale persistence)"
    verification: []
    human_judgment: true
    rationale: "SAF, share intents, and AppCompatDelegate recreate require device UAT per 06-VERIFICATION.md"

duration: 5min
completed: 2026-07-25
status: checkpoint
---

# Phase 6 Plan 04: BadgeGridItem a11y i18n Gap Closure

**BadgeGridItem now mirrors BadgeDetailScreen — accessibility state labels resolve through string resources in both locales.**

## Performance

- **Duration:** ~5 min
- **Tasks:** 2/3 complete (implementation + test); 1 human-verify checkpoint pending
- **Files modified:** 2

## Accomplishments

- Refactored `BadgeGridItem.badgeStateLabel()` from hardcoded Swedish `String` to `@StringRes Int` mapping
- Wired `contentDescription` through `stringResource(R.string.badge_pin_content_description, badge.nameSv, …)`
- Added `BadgeGridItemChromeTest` regression gate; `StringsParityTest` still passes

## Checkpoint pending

**Task 3 — Device UAT** requires human approval before plan can seal.

### A. Badge grid TalkBack (required for gap closure)

1. Settings → Language → English
2. Open a child catalog grid
3. Enable TalkBack; focus a badge pin
4. Expected: Swedish badge name + English state (e.g. "Baddaren, locked")

### B. Spot-check remaining Phase 6 behaviors

- Export share sheet (1 kid / 2+ kids)
- Import preview → Cancel → Confirm flow
- Language persistence after kill-and-relaunch
- English chrome with Swedish catalog content
- Import concurrency (no write until confirm)

**Resume signal:** Type `approved` or list failing checks.

## Deviations from Plan

None.

## Verification

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest --tests "se.simmarken.ui.child.BadgeGridItemChromeTest" --tests "se.simmarken.res.StringsParityTest"
```

Both pass.
