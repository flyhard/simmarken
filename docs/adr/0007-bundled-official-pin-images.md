# ADR-0007: Bundle official pin images

- **Status:** Accepted
- **Date:** 2026-07-22 (recorded retroactively 2026-09-25)
- **Related:** PRD-0001 (CATA-05, UI-04), PRD-0002 (OSS-02)

## Context

Parents recognise badges by the physical pin. Coloured placeholders would make
the grid harder to scan.

## Decision

- Bundle official pin artwork as WebP in `app/src/main/assets/badges/<catalog>/`,
  sourced from official shop product photos / affisch material, converted with
  scripts in `scripts/`. Rendered with Coil.
- If an image is missing, fall back to a tier/category colour with the badge name
  (`BadgePlaceholderColors`).
- Grayscale rendering indicates locked / in-progress states (ADR-0004); no
  separate grayscale assets.
- Licensing and provenance are documented in `docs/SOURCES.md`, not in the schema.

## Consequences

- Asset licensing differs from the source-code licence; the open-source release
  must carve the assets out explicitly (PRD-0002 OSS-02).
- APK size grows with each catalog (a few hundred KB today).

## Origin

GSD Phase 2 D-01…D-04; Phase 4 D-11, D-12, D-21…D-24.
