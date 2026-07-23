# Simmärken Tracker

## What This Is

A local-first Android app for parents to track their children's progress through official Swedish swimming badge systems (Svensk Simidrott and SLS). At the swim hall, parents can instantly see which skills are done, which badges are earned but not yet purchased, and which physical pins still need buying — without relying on memory, paper lists, or the other parent.

## Core Value

At the swim hall, a parent can open the app and immediately answer: "Did they pass this badge, and did we buy the physical pin?"

## Requirements

### Validated

- Parent can add and manage child profiles (name, avatar/color theme) — **Validated in Phase 3**
- Kid-first home screen with child cards, empty state, and catalog navigation placeholder — **Validated in Phase 3**

### Active

- [ ] Parent can view badges from Svensk Simidrott and SLS catalogs, grouped by category
- [ ] Parent can check off individual skill requirements per child; badge auto-achieves when all requirements are met
- [ ] Parent can mark a badge as physically purchased/sewn on (separate from skill completion)
- [ ] Badge visual states clearly distinguish: locked, in progress, achieved-to-buy, and gotten
- [ ] App works fully offline with local Room database storage
- [ ] Parent can export/import progress as a file for device migration
- [ ] UI available in Swedish and English

### Out of Scope

- Cloud sync or backend — 100% offline, local device storage only
- Sharing progress via SMS/image to grandparents — deferred to v2
- Custom swim club badge catalogs — deferred to v2
- Multi-parent real-time sync — single-device local data with manual export/import instead

## Context

**Problem:** Parents at Swedish swim schools lose track of which simmärken (swimming badges) their children have earned versus which physical pins they've actually purchased. The other parent often doesn't know the current state either.

**Users:** Parents of children in Swedish swim schools (nybörjarsim, simskola, etc.)

**Catalogs for v1:**
- Svensk Simidrott — standard progression badges (Vattenvana, Nybörjare, Järn/Brons/Silver/Guld tiers, etc.)
- SLS (Svenska Livräddningssällskapet) — lifesaving/swimming badges

**Badge data:** Research and seed from official public sources (names, requirements, images).

**Design direction:** Clean and minimal — calm, parent-focused UI suitable for quick glances at the swim hall.

**Typical usage:** One-handed, quick check while child is at the pool edge. Kid-first navigation on home screen.

## Constraints

- **Platform**: Native Android only — Kotlin + Jetpack Compose
- **Architecture**: MVVM with Room Database (SQLite)
- **Network**: None — 100% offline, no backend
- **Storage**: Local device only; manual JSON export/import for phone migration
- **i18n**: Swedish and English UI strings from v1
- **Data model**: Relational schema supporting multiple catalogs (Catalog → Category → Badge → Requirement; Kid + Progress join tables)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Kotlin + Jetpack Compose + Room + MVVM | User-specified; ideal for state-driven progress UI | — Pending |
| Both Svensk Simidrott and SLS in v1 | User wants both official catalogs available at launch | — Pending |
| Research badge data from public sources | User doesn't have assets ready; official data ensures accuracy | — Pending |
| JSON export/import for backup | User wants phone migration without cloud sync | — Pending |
| Clean minimal design | Parent-focused, quick glance at swim hall | — Pending |
| 4-tier badge visual state (locked → in progress → achieved/to buy → gotten) | Core UX differentiator; solves the "passed vs bought" problem | — Pending |
| No cloud/backend | Privacy, simplicity, offline reliability at pool | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-07-23 after Phase 3 completion*
