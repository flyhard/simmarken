# PRD-0001: MVP badge tracker (v1.0)

- **Status:** Shipped (2026-07-25)
- **Owner:** Maintainer
- **Created:** 2026-07-22
- **Last updated:** 2026-09-25 (migrated from GSD `.planning/milestones/v1.0-*`)

## Problem

Parents of children in Swedish swim schools lose track of which *simmärken*
(swimming badges) each child has earned versus which physical pins they have
actually bought. The other parent often doesn't know the current state either,
and paper lists get lost.

**Core value:** At the swim hall, a parent can open the app and immediately
answer: *"Did they pass this badge, and did we buy the physical pin?"*

## Goals

- A parent can see every child's badge state for both official catalogs within
  a couple of taps, one-handed, at the pool edge.
- "Passed the skills" and "bought the pin" are tracked separately and are never
  confused.
- All data stays on the device and survives app restarts; moving to a new phone
  is possible without a cloud account.

## Non-goals

- Cloud sync, accounts or any backend.
- Real-time multi-parent sync (manual export/import instead).
- Swim-instructor mode or social features.

## Users & scenarios

- **Persona:** parent of one or more children in nybörjarsim / simskola.
- **Key moment:** a quick glance while the child is at the pool edge —
  one-handed, calm, minimal UI.
- **Secondary:** after a lesson, ticking off newly passed skills; at the swim
  hall kiosk, checking which pins still need buying.

## Requirements

All requirements below shipped in v1.0.

### Child profiles (KIDS)

- [x] **KIDS-01** (must): Parent can add a child with a name.
  - Name is trimmed, must be non-empty, max ~30 characters.
- [x] **KIDS-02** (must): Parent can give a child a colour identity.
  - Initials on a coloured circle; default colour derived from the name, override
    via a 10-colour swatch grid. No photo avatars.
- [x] **KIDS-03** (must): Parent sees all children as cards on the home screen.
  - Single-column, full-width cards ordered by creation (oldest first); empty
    state message with the add button always visible.
- [x] **KIDS-04** (must): Parent can edit or remove a child.
  - Via the card's overflow menu; edit reuses the add form (bottom sheet);
    delete requires confirmation and removes the child's progress.

### Catalogs (CATA)

- [x] **CATA-01** (must): Svensk Simidrott catalog pre-loaded — core progression
  (Vattenvana through Guld), 20 badges.
- [x] **CATA-02** (must): SLS catalog pre-loaded — main badges, 17 badges.
- [x] **CATA-03** (must): Badges grouped by category, categories and badge order
  mirroring the official material (affisch tier names and sequence).
- [x] **CATA-04** (must): Each badge shows its official skill requirements as a
  checklist — Swedish text verbatim from official protocols, same bullet
  granularity as the source so it can be verified against the poster.
- [x] **CATA-05** (must): Each badge shows its official pin image, with a
  tier-colour + name fallback when no image is available.

### Progress tracking (PROG)

- [x] **PROG-01** (must): Parent can check and uncheck individual requirements
  per child. Toggles are instant — no confirmation.
- [x] **PROG-02** (must): A badge automatically becomes *achieved* when all its
  requirements are checked. Badges with zero requirements (e.g. Droppen) are
  achieved immediately.
- [x] **PROG-03** (must): Parent can mark a badge as physically purchased
  (*köpt*) with a toggle, separate from skill completion.
  - Toggle is enabled only once the badge is achieved.
  - Un-marking köpt asks for confirmation.
  - Unchecking a skill on a köpt badge clears köpt automatically.
- [x] **PROG-04** (must): Badge state is one of four tiers, evaluated in order:
  1. **Gotten** — marked köpt.
  2. **Achieved, to buy** — all requirements checked.
  3. **In progress** — at least one requirement checked.
  4. **Locked** — nothing checked.

  Categories are independent: no tier gating; all badges are always visible.
- [x] **PROG-05** (must): Home child card shows "N pågår" (in progress) and
  "N att köpa" (to buy) counts, aggregated across both catalogs; zero-count lines
  are hidden; bought badges are not counted.

### User interface (UI)

- [x] **UI-01** (must): Kid-first home screen with a FAB to add a child and a gear
  icon to Settings.
- [x] **UI-02** (must): Child screen shows the catalog as a 3-column badge grid
  under sticky, always-expanded category headers, with a "Simidrott | SLS" tab
  row (Simidrott selected by default each time).
- [x] **UI-03** (must): Badge detail shows the pin (fixed at top), "X av Y klara",
  a scrollable requirement checklist, and the purchase toggle.
- [x] **UI-04** (must): The four states are clearly distinguishable:
  - Locked: grayscale pin, nothing else.
  - In progress: grayscale pin + progress ring (primary colour) proportional to
    checked requirements.
  - Achieved, to buy: full-colour pin + cart badge (bottom-right).
  - Gotten: full-colour pin + checkmark badge (bottom-right).

  Grid and detail use the same visuals and the same state computation.

### Data & offline (DATA)

- [x] **DATA-01** (must): All data persists locally with no network dependency.
- [x] **DATA-02** (must): Parent can export progress to a file.
  - One child → share sheet opens directly; several → pick children (or All)
    first. Export contains children and their progress, not catalog data or
    language preference.
- [x] **DATA-03** (must): Parent can import progress from an exported file.
  - Entry via Settings (document picker) or by opening/sharing a `.json` file
    into the app.
  - Always shows a summary and asks for confirmation before writing.
  - Merges rather than replaces: existing children are kept; for conflicting
    items the newer record wins; children not on the device are only created
    after the parent confirms them.

### Internationalization (I18N)

- [x] **I18N-01** (must): App UI available in Swedish.
- [x] **I18N-02** (must): App UI available in English.
- [x] **I18N-03** (must): Parent can choose *System default · Svenska · English*
  in Settings; the change applies immediately without restart. Official catalog
  content (badge names, requirements, catalog names) always stays Swedish.

## Out of scope

| Item | Reason |
|------|--------|
| Cloud sync / user accounts | Offline by design; export/import handles migration |
| Real-time multi-parent sync | Requires a backend |
| Swim-instructor mode | Different persona; parent-only |
| Social features | Not core to the swim-hall use case |
| Magister-tier and full SLS coverage | Core progression first; sources sparse for SLS |
| Manual reordering of children | Creation order is sufficient for v1 |
| Sharing progress as text/image, custom catalogs | Deferred to [PRD-0003](0003-sharing-and-custom-catalogs.md) |

## Release criteria (met)

- All requirements above checked, with JVM unit tests for state calculation,
  progress writes, seed accuracy (golden tests against official sources),
  backup validation/merge and sv/en string parity, plus Room instrumented tests.
- Swim-hall usability confirmed in manual acceptance testing.

## Lessons carried forward

- Centralise computed state in the domain layer — one calculator shared by grid,
  detail and home prevents inconsistencies ([ADR-0004](../adr/0004-progress-model-and-derived-badge-state.md)).
- Destructive upserts on foreign-key-linked tables wipe child rows; catalog
  updates need an explicit, ID-preserving merge ([ADR-0005](../adr/0005-versioned-catalog-seed-with-code-based-merge.md)).
- Golden tests on seed data catch catalog accuracy regressions early.
- Keep planning state small; the previous process's status files drifted from
  reality ([ADR-0001](../adr/0001-use-prds-and-adrs.md)).

## Related ADRs

- [ADR-0002](../adr/0002-native-android-kotlin-compose-room-mvvm.md) Native Android: Kotlin, Compose, Room, MVVM
- [ADR-0003](../adr/0003-offline-only-no-backend.md) Offline-only, no backend
- [ADR-0004](../adr/0004-progress-model-and-derived-badge-state.md) Progress model and derived badge state
- [ADR-0005](../adr/0005-versioned-catalog-seed-with-code-based-merge.md) Versioned catalog seed with code-based merge
- [ADR-0006](../adr/0006-official-catalog-content-verbatim-swedish.md) Official catalog content stays verbatim Swedish
- [ADR-0007](../adr/0007-bundled-official-pin-images.md) Bundle official pin images
- [ADR-0008](../adr/0008-json-backup-format-and-merge.md) JSON backup format and merge
- [ADR-0009](../adr/0009-per-app-language-preference.md) Per-app language preference
- [ADR-0010](../adr/0010-manual-dependency-injection.md) Manual dependency injection
