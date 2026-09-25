# PRD-0003: Sharing & custom catalogs (v2 candidates)

- **Status:** Draft
- **Owner:** Maintainer
- **Created:** 2026-07-22
- **Last updated:** 2026-09-25 (migrated from GSD deferred requirements)

> Backlog PRD. These requirements were deferred from v1.0 and v1.1 and have not
> been scoped or prioritised yet. Refine problem, goals and acceptance criteria
> before moving to *Accepted*.

## Problem

- Grandparents and the other parent want to follow a child's progress but don't
  have the app or the device.
- Some swim clubs run their own badge programmes that aren't in the official
  Svensk Simidrott or SLS catalogs.
- Official requirements change occasionally, and the bundled catalogs need a
  clear update story.

## Goals

- Share a child's progress in a form anyone can read, without accounts or a backend.
- Support catalogs beyond the two official ones without breaking existing progress.

## Non-goals

- Cloud sync or live shared views ([ADR-0003](../adr/0003-offline-only-no-backend.md) still applies).

## Requirements

### Sharing (SHAR)

- [ ] **SHAR-01** (should): Parent can share a child's progress as formatted text
  (e.g. via SMS or messaging apps).
- [ ] **SHAR-02** (could): Parent can share a child's progress as an image.

### Catalogs (CATA)

- [ ] **CATA-06** (could): Parent can add or switch to a custom swim-club badge
  catalog.
- [ ] **CATA-07** (should): Bundled catalogs are updated when official
  requirements change, without losing kid progress.
  - The mechanism already exists ([ADR-0005](../adr/0005-versioned-catalog-seed-with-code-based-merge.md));
    this requirement covers the process of sourcing, verifying and shipping updates.

## Open questions

- Should custom catalogs be included in the backup file? That would require a new
  `exportVersion` ([ADR-0008](../adr/0008-json-backup-format-and-merge.md)).
- Text share: which language — UI language, with catalog content in Swedish?
- Should Magister-tier Simidrott badges and fuller SLS coverage be added under CATA-07?

## Related ADRs

- [ADR-0005](../adr/0005-versioned-catalog-seed-with-code-based-merge.md)
- [ADR-0008](../adr/0008-json-backup-format-and-merge.md)
