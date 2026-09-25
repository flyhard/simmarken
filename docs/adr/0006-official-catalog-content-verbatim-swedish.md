# ADR-0006: Official catalog content stays verbatim Swedish

- **Status:** Accepted
- **Date:** 2026-07-22 (recorded retroactively 2026-09-25)
- **Related:** PRD-0001 (CATA-03, CATA-04, I18N-01 … I18N-03), ADR-0009

## Context

Badge names and requirement texts come from official Swedish swim-school
material. Parents compare the app with the paper affisch and with what
instructors say at the pool. Translations risk drifting from the official wording.

## Decision

- Requirement text (`textSv`) is **verbatim** from official protocols, with the
  same bullet granularity as the source. No simplification or summarising.
- Categories mirror the official tier names and order (Vattenvana, Nybörjare,
  Järn, Brons, Silver, Guld, …); badge `sortOrder` follows official progression.
- The UI shows **Swedish catalog content in every UI language**: `nameSv`,
  `textSv` and Swedish category names are used even when English UI is selected.
  Catalog tab labels "Simidrott" and "SLS" are never translated.
- App chrome (buttons, labels, dialogs, progress summaries, accessibility
  strings) is translated through Android string resources.
- The seed keeps `nameEn`/`textEn` columns (machine-assisted, human-sampled) for
  possible future use, but they are not displayed.

## Consequences

- English-speaking parents see Swedish badge names — acceptable, since those are
  the names used at the pool.
- `StringsParityTest` enforces sv/en key parity for chrome strings only.

## Origin

GSD Phase 2 D-07…D-12; Phase 4 D-19, D-20; Phase 6 D-13…D-17 ("D-13" is cited in
code comments in `BadgeCatalogMapper`).
