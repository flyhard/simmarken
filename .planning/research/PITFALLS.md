# Pitfalls Research

**Domain:** Local-first Android swimming badge tracker
**Researched:** 2026-07-22
**Confidence:** HIGH

## Critical Pitfalls

### Pitfall 1: Incorrect or Outdated Badge Requirements

**What goes wrong:**
Parents trust the app at the swim hall but requirements don't match what the instructor tests. App loses credibility immediately.

**Why it happens:**
Badge requirements change (Svensk Simidrott updated affisch 2024→2026). Copying from secondary sources instead of official PDFs.

**How to avoid:**
Seed data from official Svensk Simidrott simmärkesmaterial PDFs and SLS public sources. Document source URLs and version date in seed metadata. Include `catalogVersion` in database.

**Warning signs:**
Requirements don't match the paper protocol at simskolan. Parents report mismatches.

**Phase to address:**
Phase 2 (Catalog Seeding)

---

### Pitfall 2: Conflating "Achieved" and "Gotten"

**What goes wrong:**
Badge shows as complete when skills are done but pin wasn't bought — or vice versa. Core value proposition breaks.

**Why it happens:**
Single boolean for badge completion. UI doesn't distinguish states.

**How to avoid:**
Separate `RequirementProgress.isAchieved` (per skill) from `BadgeProgress.isGotten` (manual purchase toggle). Derive ACHIEVED_TO_BUY only when all requirements met AND !isGotten.

**Warning signs:**
No shopping cart overlay state. Single checkmark for everything.

**Phase to address:**
Phase 5 (Progress Tracking)

---

### Pitfall 3: Badge State Drift Across Screens

**What goes wrong:**
Home screen says "1 badge to buy" but catalog shows different count. Badge detail shows Achieved but grid shows In Progress.

**Why it happens:**
Duplicate state computation logic in multiple ViewModels.

**How to avoid:**
Single `BadgeStateCalculator` (or domain function) used by all screens. Unit test state transitions.

**Warning signs:**
Different screens show different states for same kid+badge.

**Phase to address:**
Phase 4 (Visual States) and Phase 5

---

### Pitfall 4: Export/Import Data Loss

**What goes wrong:**
Parent exports on old phone, imports on new phone — loses progress or corrupts data.

**Why it happens:**
No schema version in export format. Missing fields on import. No validation.

**How to avoid:**
Versioned JSON schema (`exportVersion: 1`). Validate on import. Show clear error if incompatible. Test round-trip.

**Warning signs:**
Import silently drops requirements or kids.

**Phase to address:**
Phase 6 (Export/Import)

---

### Pitfall 5: Copyright on Badge Images

**What goes wrong:**
Bundling official simmärke artwork without permission causes legal issues or Play Store rejection.

**Why it happens:**
Downloading images from Svensk Simidrott shop/material without checking license.

**How to avoid:**
Research usage rights for badge images. Consider: placeholder icons with color coding, user-provided photos, or contacting Svensk Simidrott/SLS for permission. Official affisch images may be for personal/educational use — verify.

**Warning signs:**
High-res official pin images bundled without attribution or license.

**Phase to address:**
Phase 2 (Catalog Seeding)

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Hardcode one catalog | Faster MVP | Can't add SLS without refactor | Never — schema supports both |
| Skip export versioning | Faster ship | Broken imports on update | Never |
| Store derived badge state | Simpler queries | Stale state bugs | Never — derive at read time |
| Emoji avatars only | Skip image picker | Limited personalization | v1 acceptable |

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| Too many taps to check a skill | Frustrating at wet swim hall | Single tap toggle, large targets |
| No home screen summary | Must drill into each child | "3 in progress, 1 to buy" on card |
| Grayscale locked badges look broken | Confusing for kids/parents | Clear "not started" visual with progress ring |
| Language not persisted | Resets every launch | DataStore for locale preference |

## "Looks Done But Isn't" Checklist

- [ ] **Catalog seeding:** Often missing SLS or Magister-tier badges — verify full affisch coverage
- [ ] **Progress tracking:** Often missing uncheck (toggle off) — verify bidirectional toggle
- [ ] **Export:** Often missing import error handling — verify corrupt file UX
- [ ] **i18n:** Often missing requirement text translation — verify badge requirements in both languages
- [ ] **Visual states:** Often missing ACHIEVED_TO_BUY overlay — verify shopping cart icon

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| Wrong requirements | Phase 2 | Cross-check 3 badges against official PDF |
| Achieved vs Gotten | Phase 5 | Manual test: complete skills, verify cart overlay |
| State drift | Phase 4 | Same badge, three screens, same state |
| Export data loss | Phase 6 | Round-trip export/import test |
| Image copyright | Phase 2 | Document image source and license |

## Sources

- Svensk Simidrott simmärkesmaterial (official PDFs)
- Android offline-first architecture guides
- PROJECT.md constraints

---
*Pitfalls research for: Simmärken Tracker*
*Researched: 2026-07-22*
