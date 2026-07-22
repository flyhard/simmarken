# Feature Research

**Domain:** Swedish swimming badge progress tracker for parents
**Researched:** 2026-07-22
**Confidence:** HIGH

## Feature Landscape

### Table Stakes (Users Expect These)

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Multiple child profiles | Families have 2+ kids in simskola | LOW | Name + avatar/color |
| Official badge catalog | App is useless without real simmärken data | HIGH | Seed from Svensk Simidrott + SLS public sources |
| Per-skill checklist | Badges have multiple requirements (e.g., "doppa huvudet 5 gånger") | MEDIUM | Core interaction at swim hall |
| Progress persistence | Data must survive app restart | LOW | Room handles this |
| Visual progress indication | Parent needs at-a-glance status | MEDIUM | 4-tier state system is differentiator but also expected clarity |
| Offline operation | Swim halls have poor connectivity | LOW | No network = simpler architecture |

### Differentiators (Competitive Advantage)

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| "Achieved vs Bought" separation | Solves core pain — passed badge ≠ purchased pin | LOW | `isAchieved` auto + `isGotten` manual toggle |
| Home screen "badges to buy" summary | Actionable at the kiosk/shop in swim hall | LOW | Derived from progress data |
| Dual catalog (Simidrott + SLS) | Covers both major Swedish badge systems | HIGH | Relational catalog schema supports this |
| JSON export/import | Phone migration without cloud | MEDIUM | User-requested; privacy-friendly backup |
| Bilingual UI (SV/EN) | International parents, English-speaking households | MEDIUM | string resources |

### Anti-Features (Commonly Requested, Often Problematic)

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| Cloud sync / accounts | "Share with other parent" | Backend cost, auth complexity, privacy | JSON export/import in v1 |
| Social sharing | Grandparents want updates | Scope creep, image generation complexity | Defer to v2 |
| Swim instructor mode | Coaches might use it | Different user, different permissions | Parent-only v1 |
| Real-time multi-device sync | Both parents want live updates | Requires backend | Manual export/import |
| Custom club badges | Local swim clubs have variants | Data maintenance burden | v2 with catalog abstraction already in schema |

## Feature Dependencies

```
Catalog Seeding
    └──requires──> Database Schema
                       └──requires──> Android Project Setup

Child Profiles
    └──requires──> Database Schema

Progress Tracking
    └──requires──> Catalog Seeding
    └──requires──> Child Profiles

Badge Visual States
    └──requires──> Progress Tracking

Export/Import
    └──requires──> Progress Tracking + Catalog

i18n
    └──enhances──> All UI screens
```

### Dependency Notes

- **Progress requires Catalog:** Can't track requirements without seeded badge data
- **Visual states require Progress:** States are derived from requirement completion + purchase flag
- **Export requires stable schema:** Version export format from day 1

## MVP Definition

### Launch With (v1)

- [x] Child profile management — core navigation unit
- [x] Svensk Simidrott + SLS catalogs seeded — app has real data
- [x] Requirement checklist per badge per child — core swim-hall interaction
- [x] Achieved vs Gotten distinction — core value proposition
- [x] 4-tier visual badge states — UX clarity
- [x] Offline Room persistence — no network dependency
- [x] JSON export/import — phone migration
- [x] Swedish + English UI — user requirement

### Add After Validation (v1.x)

- [ ] Share progress as text/image — after core tracking validated
- [ ] Widget for quick glance — if parents request it

### Future Consideration (v2+)

- [ ] Custom swim club catalogs — schema supports, data doesn't exist yet
- [ ] Cloud sync with family sharing — if export/import proves insufficient

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Requirement checklist | HIGH | MEDIUM | P1 |
| Achieved vs Bought | HIGH | LOW | P1 |
| Child profiles | HIGH | LOW | P1 |
| Catalog seeding (both) | HIGH | HIGH | P1 |
| 4-tier visual states | HIGH | MEDIUM | P1 |
| JSON export/import | MEDIUM | MEDIUM | P1 |
| Bilingual UI | MEDIUM | MEDIUM | P1 |
| Share via SMS/image | MEDIUM | HIGH | P3 |
| Custom club catalogs | LOW | HIGH | P3 |

## Competitor Feature Analysis

| Feature | Paper lists / Notes app | Generic habit trackers | Our Approach |
|---------|------------------------|----------------------|--------------|
| Official badge requirements | Manual copy | N/A | Pre-seeded from official sources |
| Per-badge purchase tracking | Not supported | N/A | Dedicated `isGotten` flag |
| Swim-specific categories | N/A | N/A | Catalog → Category → Badge hierarchy |
| Offline at pool | Paper works | Varies | Native offline-first |

## Sources

- [Svensk Simidrott simmärken](https://svensksimidrott.se/simkunnighet/simmarken) — official catalog structure
- [Simmärkesmaterial PDFs](https://svensksimidrott.se/simkunnighet/simmarken/simmarkesmaterial) — requirement protocols
- GP article on simmärken 2025 — dual catalog context (Simidrott vs SLS)
- PROJECT.md user specification

---
*Feature research for: Simmärken Tracker*
*Researched: 2026-07-22*
