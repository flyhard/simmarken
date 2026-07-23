---
status: testing
phase: 04-catalog-view-visual-states
source: [04-VERIFICATION.md]
started: 2026-07-23T10:35:00Z
updated: 2026-07-23T10:35:00Z
---

## Current Test

number: 1
name: Verify all four badge visual states are distinguishable in the emulator
expected: |
  Grayscale locked, grayscale+ring in-progress, full-color+cart achieved-to-buy, full-color+check gotten — each instantly distinguishable.
awaiting: user response

## Tests

### 1. Four-tier visual distinction (UI-04)
expected: Grayscale locked, grayscale + primary ring in-progress, full-color + cart achieved-to-buy, full-color + check gotten — all four instantly distinguishable.
result: [pending]

### 2. Dual-catalog tab switching and session reset
expected: Correct catalog per tab with no cross-catalog bleed; Simidrott tab restored on re-entry after navigating back to home.
result: [pending]

### 3. Badge tap → detail navigation
expected: BadgeDetail screen opens with matching pin visual, badge name, and "Checklista kommer snart"; back returns to grid.
result: [pending]

## Summary

total: 3
passed: 0
issues: 0
pending: 3
skipped: 0
blocked: 0

## Gaps
