# Simmärken Tracker

## What This Is

A local-first Android app for parents to track their children's progress through official Swedish swimming badge systems (Svensk Simidrott and SLS). At the swim hall, parents can instantly see which skills are done, which badges are earned but not yet purchased, and which physical pins still need buying — without relying on memory, paper lists, or the other parent.

## Core Value

At the swim hall, a parent can open the app and immediately answer: "Did they pass this badge, and did we buy the physical pin?"

## Current State (v1.0 MVP — shipped 2026-07-25)

**Shipped:** Native Android app (Kotlin, Jetpack Compose, Room) with 6 phases complete.

- **Foundation:** Room schema (7 entities), MVVM architecture, offline persistence
- **Catalogs:** Svensk Simidrott (20 badges) + SLS (17 badges) seeded from official sources with WebP pin images
- **Profiles:** Kid-first home, add/edit/delete children with avatar colors
- **Visual states:** 4-tier badge display (locked → in progress → achieved-to-buy → gotten)
- **Progress:** Skill checklist, auto-achieve, purchase toggle, home summary counts
- **Data:** JSON export/import with newer-wins merge for device migration
- **i18n:** Swedish and English UI with runtime language switching

## Next Milestone Goals

- Share progress as formatted text or image (SHAR-01, SHAR-02)
- Custom swim club badge catalogs (CATA-06)
- Catalog version updates when official requirements change

## Requirements

### Validated (v1.0)

- ✓ Parent can add and manage child profiles (name, avatar/color theme) — v1.0 Phase 3
- ✓ Kid-first home screen with child cards and catalog navigation — v1.0 Phase 3
- ✓ Svensk Simidrott and SLS catalogs pre-loaded with categories, badges, requirements — v1.0 Phase 2
- ✓ Badges grouped by category with 4-tier visual states — v1.0 Phase 4
- ✓ Parent can check off individual skill requirements; badge auto-achieves when complete — v1.0 Phase 5
- ✓ Parent can mark a badge as physically purchased (separate from skill completion) — v1.0 Phase 5
- ✓ Home screen shows summary counts (badges in progress, badges to buy) — v1.0 Phase 5
- ✓ All data persists locally via Room with no network dependency — v1.0 Phase 1
- ✓ Parent can export/import progress as JSON for device migration — v1.0 Phase 6
- ✓ UI available in Swedish and English with settings language picker — v1.0 Phase 6

### Active (v2 candidates)

- [ ] Share progress as formatted text for grandparents (SHAR-01)
- [ ] Share progress as image for grandparents (SHAR-02)
- [ ] Custom swim club badge catalogs (CATA-06)

### Out of Scope

- Cloud sync or backend — 100% offline, local device storage only; JSON export/import handles migration
- Multi-parent real-time sync — single-device local data with manual export/import instead
- Swim instructor mode — different user persona; parent-only v1
- Social features — not core to swim-hall use case

## Context

**Problem:** Parents at Swedish swim schools lose track of which simmärken (swimming badges) their children have earned versus which physical pins they've actually purchased. The other parent often doesn't know the current state either.

**Users:** Parents of children in Swedish swim schools (nybörjarsim, simskola, etc.)

**Catalogs shipped in v1:**
- Svensk Simidrott — 20 badges with affisch-derived requirements
- SLS — 17 badges with shop-verified requirements

**Tech stack:** Kotlin, Jetpack Compose, Room (SQLite), MVVM, Coil, kotlinx-serialization, DataStore

**Design direction:** Clean and minimal — calm, parent-focused UI suitable for quick glances at the swim hall.

**Typical usage:** One-handed, quick check while child is at the pool edge. Kid-first navigation on home screen.

## Constraints

- **Platform**: Native Android only — Kotlin + Jetpack Compose
- **Architecture**: MVVM with Room Database (SQLite)
- **Network**: None — 100% offline, no backend
- **Storage**: Local device only; manual JSON export/import for phone migration
- **i18n**: Swedish and English UI strings
- **Data model**: Relational schema supporting multiple catalogs (Catalog → Category → Badge → Requirement; Kid + Progress join tables)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Kotlin + Jetpack Compose + Room + MVVM | User-specified; ideal for state-driven progress UI | ✓ Good — clean separation, testable ViewModels |
| Both Svensk Simidrott and SLS in v1 | User wants both official catalogs at launch | ✓ Good — 37 badges seeded with golden tests |
| Research badge data from public sources | User doesn't have assets ready; official data ensures accuracy | ✓ Good — affisch + shop sources with SOURCES.md |
| JSON export/import for backup | User wants phone migration without cloud sync | ✓ Good — versioned schema, newer-wins merge |
| Clean minimal design | Parent-focused, quick glance at swim hall | ✓ Good — UAT confirmed swim-hall usability |
| 4-tier badge visual state | Core UX differentiator; solves "passed vs bought" problem | ✓ Good — BadgeStateCalculator centralizes logic |
| achievedAt write-once + gotten auto-clear | Prevents progress corruption and keeps köpt honest | ✓ Good — validated in Phase 5 UAT |
| BadgeVisualState derived at read time | Never persist computed state; single source of truth | ✓ Good — BadgeStateCalculator + mapper pattern |
| Catalog content stays Swedish regardless of UI locale | Official badge names are Swedish; avoids translation drift | ✓ Good — D-13 enforced in mapper |
| No cloud/backend | Privacy, simplicity, offline reliability at pool | ✓ Good — zero network dependencies |

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
*Last updated: 2026-07-25 after v1.0 milestone*
