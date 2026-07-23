---
status: testing
phase: 05-progress-tracking-badge-detail
source: [05-01-SUMMARY.md, 05-02-SUMMARY.md, 05-03-SUMMARY.md]
started: 2026-07-23T12:35:00Z
updated: 2026-07-23T12:35:00Z
---

## Current Test

number: 1
name: Swim-hall detail layout
expected: |
  Badge detail shows 160dp pin image at top, badge name, "X av Y klara" subtitle (when requirements exist), scrollable checklist, and "Fysiskt märke köpt" switch at bottom.
awaiting: user response

## Tests

### 1. Swim-hall detail layout
expected: Home → child card → catalog → badge detail shows pin, name, progress subtitle, checklist, and purchase switch
result: [pending]

### 2. Requirement toggle responsiveness
expected: Checkbox and pin visual state update immediately on each tap; no confirmation dialog; purchase switch enables only when all requirements checked
result: [pending]

### 3. Köpt confirmation dialog
expected: Mark köpt, flip switch off → "Ta bort köpt-markering?" dialog; confirm clears gotten, dismiss leaves köpt
result: [pending]

### 4. GOTTEN skill uncheck
expected: Uncheck a requirement on a köpt badge → gotten clears silently without dialog
result: [pending]

### 5. Zero-requirement badge
expected: "Inga kunskapskrav" note instead of checklist; purchase toggle enabled immediately
result: [pending]

### 6. Home summary subtitles
expected: Child card shows "N pågår" and "M att köpa" only when counts > 0; gotten badges excluded
result: [pending]

### 7. Room persistence round-trip
expected: Toggle requirements, force-stop app, relaunch → toggled requirement remains checked and visual state reflects progress
result: [pending]

## Summary

total: 7
passed: 0
issues: 0
pending: 7
skipped: 0
blocked: 0

## Gaps

