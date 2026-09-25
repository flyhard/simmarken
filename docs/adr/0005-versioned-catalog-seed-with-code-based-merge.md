# ADR-0005: Versioned catalog seed with code-based merge

- **Status:** Accepted
- **Date:** 2026-07-22 (recorded retroactively 2026-09-25)
- **Related:** PRD-0001 (CATA-01 … CATA-04), PRD-0003 (CATA-07)

## Context

Official catalogs ship with the app and will change over time (new badges,
reworded requirements). Kids' progress references catalog rows by foreign key
and must survive catalog updates. An early implementation used
`OnConflictStrategy.REPLACE`, which deletes and re-inserts rows and so cascaded
deletes into progress tables.

## Decision

- Catalogs are bundled as JSON in `app/src/main/assets/seed/` (`simidrott.json`,
  `sls.json`), parsed with kotlinx-serialization.
- Every catalog, category, badge and requirement has a **stable `code`**.
  Progress is preserved by matching on `code`, never on row ID or text.
- Each catalog has a `catalogVersion`. On app start (on the IO dispatcher),
  `CatalogSeedLoader` merges a catalog only when the bundled version is greater
  than the stored one.
- The merge is **ID-preserving**: look up existing rows by code, then use Room
  `@Upsert` (never `REPLACE`). New items are added, changed text is updated,
  kid progress is kept.
- Source provenance (URLs, extraction dates) is documented in `docs/SOURCES.md`
  and `docs/extraction/`, not in the schema. Extraction scripts in `scripts/`
  make updates reproducible. Golden tests pin the seed content against sources.

## Alternatives considered

- **Wipe and reseed** — destroys progress.
- **Match by row ID or text** — IDs are unstable across reseeds; text changes
  whenever official wording changes.
- **Remote catalog download** — conflicts with ADR-0003.

## Consequences

- Changing a `code` is a breaking change (progress for that item is orphaned);
  codes must be treated as permanent identifiers.
- Any catalog content change must bump `catalogVersion`, or it won't reach
  existing installs.
- Seed merges are covered by instrumented tests (`CatalogSeedMergeTest`).

## Origin

GSD Phase 2 D-13…D-16, gap-closure plan 02-05 (REPLACE → `@Upsert`).
