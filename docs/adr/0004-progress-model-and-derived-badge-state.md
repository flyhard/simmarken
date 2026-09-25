# ADR-0004: Progress model and derived badge state

- **Status:** Accepted
- **Date:** 2026-07-22 (recorded retroactively 2026-09-25)
- **Related:** PRD-0001 (PROG-01 … PROG-05, UI-04)

## Context

The core value of the app is distinguishing *"passed the skills"* from *"bought
the pin"*. Badge state must be identical on the grid, detail screen and home
summary, and must stay correct when skills are unchecked.

## Decision

**Storage — two progress tables:**

- `RequirementProgressEntity` (kid × requirement): `isAchieved`, `achievedAt`,
  `updatedAt`. Toggling is bidirectional.
- `BadgeProgressEntity` (kid × badge): `isGotten`, `gottenAt`, `achievedAt`, `updatedAt`.

**Never persist the visual state.** It is derived at read time by a single
`BadgeStateCalculator` with this precedence:

1. `isGotten` → **GOTTEN**
2. all requirements achieved (or badge has none) → **ACHIEVED_TO_BUY**
3. any requirement achieved → **IN_PROGRESS**
4. otherwise → **LOCKED**

Grid, detail and home (`KidProgressSummaryCalculator`) all go through this
calculator and `BadgeCatalogMapper` — no duplicated state logic.

**Write rules** (`ProgressWriteLogic`):

- `achievedAt` on the badge is set when the last requirement becomes achieved and
  is **write-once** — never cleared by a later uncheck.
- Unchecking any requirement on a GOTTEN badge clears `isGotten` and `gottenAt`
  automatically (keeps "köpt" honest).
- Explicitly un-marking köpt is a separate, confirmed user action.
- Categories are independent: no tier gating in the state calculation.

## Alternatives considered

- **Persist a state enum** — risks drift between stored and actual progress; every
  write path would need to keep it in sync.
- **Single progress table** — mixes two independent concerns (skills vs. purchase).

## Consequences

- State is always consistent with the underlying checkboxes.
- Home summaries compute state for every badge per kid; fine at current catalog
  size (~37 badges), revisit if catalogs grow substantially.
- `achievedAt` records "first achieved", not "currently achieved".

## Origin

GSD Phase 1 D-01…D-05; Phase 4 D-05…D-08, D-29, D-30; Phase 5 D-06…D-11, D-17…D-20.
