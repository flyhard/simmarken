# ADR-0008: JSON backup format and merge

- **Status:** Accepted
- **Date:** 2026-07-25 (recorded retroactively 2026-09-25)
- **Related:** PRD-0001 (DATA-02, DATA-03), ADR-0003

## Context

With no backend (ADR-0003), a user-visible file is the only way to move data to
a new phone or share it with the other parent. Once a file is in the wild the
format is effectively public and must stay readable.

## Decision

- **Format:** JSON via kotlinx-serialization (`BackupDto`), with an
  `exportVersion` (currently `1`) that is validated on import.
- **Identity:** kids are keyed by a UUID `stableId`, never Room auto-increment IDs.
  Progress rows reference catalog content by catalog/badge/requirement **codes**
  (ADR-0005).
- **Contents:** selected kids with their requirement and badge progress. Catalog
  seed data and the language preference are **not** included. No other personal
  data (reviewed in `docs/SECURITY-CHECKLIST.md`).
- **Export:** via the Android share sheet (no in-app file browser). With one kid
  the share sheet opens directly; with several a picker comes first.
- **Import:** parse → validate (`BackupValidator`) → plan (`MergePlanner`) →
  show summary → confirm → apply in one transaction.
  - Merge, never replace: local kids not in the file are untouched.
  - Conflicts are resolved per item by `updatedAtEpochMillis` — newer wins.
  - Kids not present on the device are created only after the user confirms them.
- **Entry points:** Settings (system document picker) and `ACTION_VIEW` /
  `ACTION_SEND` intents for JSON files opened or shared into the app.

## Consequences

- **One-way door:** the format must remain backwards compatible. Any breaking
  change requires a new `exportVersion` and continued support for reading older ones.
- Every progress write must maintain `updatedAtEpochMillis`, or merges pick the
  wrong side.
- Last-writer-wins can lose a concurrent edit made on the other phone; accepted
  for a manual, infrequent workflow.

## Origin

GSD Phase 6 D-01…D-08, D-12, D-21.
