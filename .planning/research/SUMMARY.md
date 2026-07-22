# Project Research Summary

**Project:** Simmärken Tracker
**Domain:** Local-first Android parent app for Swedish swimming badge progress
**Researched:** 2026-07-22
**Confidence:** HIGH

## Executive Summary

Simmärken Tracker is a focused offline Android app — no backend, no accounts. The standard 2026 Android stack (Kotlin, Jetpack Compose, Room, MVVM) is ideal for reactive progress UI. The core differentiator is separating skill achievement from physical pin purchase, which maps cleanly to derived UI states over a relational catalog schema.

The main risks are data accuracy (official badge requirements must match current Svensk Simidrott/SLS specifications) and badge image licensing. Catalog seeding from official public PDFs is the highest-complexity phase. JSON export/import provides privacy-friendly backup without cloud infrastructure.

## Key Findings

### Recommended Stack

Kotlin + Jetpack Compose + Room 2.6+ with KSP, MVVM + Repository pattern, Navigation Compose, Material 3. No network libraries needed. kotlinx-serialization for export/import.

**Core technologies:**
- **Room:** Single source of truth, Flow-driven reactive UI
- **Compose:** State-driven badge grid and checklist
- **KSP:** Required annotation processor for Room

### Expected Features

**Must have (table stakes):**
- Child profiles with home-screen navigation
- Official Svensk Simidrott + SLS catalogs with per-skill checklists
- Offline persistence
- Clear visual progress states

**Should have (competitive):**
- Achieved vs Gotten (purchase) distinction — core value
- Home screen "badges to buy" summary
- JSON export/import for phone migration
- Swedish + English UI

**Defer (v2+):**
- Share via SMS/image
- Custom swim club catalogs

### Architecture Approach

Three-layer MVVM: Compose screens → ViewModels (StateFlow) → Repositories → Room DAOs. Badge visual state derived at read time from requirement progress + isGotten flag. Catalog seeded from JSON assets on first launch.

**Major components:**
1. **Catalog subsystem** — Catalog → Category → Badge → Requirement hierarchy
2. **Progress subsystem** — Per-kid requirement and badge progress
3. **Export subsystem** — Versioned JSON backup/restore

### Critical Pitfalls

1. **Wrong badge requirements** — Seed from official PDFs, version catalog data
2. **Achieved vs Gotten conflation** — Separate flags, distinct UI states
3. **State drift across screens** — Centralize badge state computation
4. **Export data loss** — Version export schema, validate imports
5. **Badge image copyright** — Verify licensing before bundling official artwork

## Implications for Roadmap

### Phase 1: Android Foundation & Database
**Rationale:** Everything depends on project skeleton and Room schema
**Delivers:** Runnable app with empty database, entity relationships
**Avoids:** Schema rework later

### Phase 2: Catalog Seeding
**Rationale:** Can't track progress without real badge data
**Delivers:** Svensk Simidrott + SLS catalogs in Room with requirements
**Avoids:** Wrong requirements, missing catalogs

### Phase 3: Child Profiles & Home (MVP slice)
**Rationale:** First end-to-end user capability
**Delivers:** Add/view children, kid-first home screen

### Phase 4: Catalog View & Visual States
**Rationale:** Parent can see badges before tracking progress
**Delivers:** Category-grouped badge grid with 4-tier visuals

### Phase 5: Progress Tracking & Badge Detail
**Rationale:** Core swim-hall interaction
**Delivers:** Checklist, auto-achieve, purchase toggle

### Phase 6: Export/Import & i18n
**Rationale:** Polish and migration path
**Delivers:** JSON backup, Swedish/English UI

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 2:** Badge image licensing, complete requirement text extraction from PDFs
- **Phase 2:** SLS catalog structure (less documented than Simidrott)

Phases with standard patterns:
- **Phase 1:** Well-documented Android/Room setup
- **Phase 3:** Standard CRUD + Compose list

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Standard Android patterns, well-documented |
| Features | HIGH | Clear user spec + official catalog sources |
| Architecture | HIGH | Simple offline MVVM, no sync complexity |
| Pitfalls | MEDIUM | Badge image licensing needs verification |

**Overall confidence:** HIGH

### Gaps to Address

- **SLS catalog completeness:** Research SLS badge list during Phase 2 planning
- **Badge image strategy:** Decide placeholder vs licensed images during Phase 2
- **Requirement i18n:** Swedish source text exists; English may need translation effort

---
*Research completed: 2026-07-22*
*Ready for roadmap: yes*
