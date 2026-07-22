# Phase 2: Catalog Seeding - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-22
**Phase:** 2-Catalog Seeding
**Areas discussed:** Badge visuals, Catalog coverage, Requirement wording, Seed update behavior

---

## Badge Visuals

### Q1: Default visual strategy for badges in v1?

| Option | Description | Selected |
|--------|-------------|----------|
| Color-coded placeholders | Tier/category colors, no official artwork | |
| Official pin images | Bundle high-res artwork from official sources | ✓ |
| Hybrid | Placeholders now, swap in official where licensing clear | |
| You decide | Pick what fits best | |

**User's choice:** Official pin images
**Notes:** User accepts pursuing official artwork despite PITFALLS.md copyright warning.

### Q2: Format/resolution for bundling images?

| Option | Description | Selected |
|--------|-------------|----------|
| WebP in assets | Compressed, ~100-200px display size | |
| Vector drawables | Simplified SVG-style icons | |
| Higher-res WebP | Larger files for detail screen zoom | |
| You decide | Pick format and sizing | ✓ |

**User's choice:** You decide
**Notes:** Planner should use WebP in assets per ARCHITECTURE.md.

### Q3: Where to source official artwork?

| Option | Description | Selected |
|--------|-------------|----------|
| Official affisch PDFs | Extract from simmärkesmaterial | |
| Website/shop images | Download from public pages | |
| Best available per catalog | Affisch for Simidrott, SLS public sources | ✓ |
| You decide | Pick sources | |

**User's choice:** Best available per catalog

### Q4: Fallback for badges without licensed image?

| Option | Description | Selected |
|--------|-------------|----------|
| Tier color + badge name | Category color background with initials/code | |
| Generic swim icon | Same placeholder for all missing | |
| No fallback | Every badge must have image before ship | |
| You decide | Pick fallback approach | ✓ |

**User's choice:** You decide
**Notes:** Tier color + badge name recommended for SLS gaps.

---

## Catalog Coverage

### Q1: How complete should seeded catalogs be at launch?

| Option | Description | Selected |
|--------|-------------|----------|
| Full affisch | All tiers including Magister, all SLS badges | |
| Core progression | Vattenvana through Guld + main SLS badges | ✓ |
| Sample set only | Baddaren, Hajen, SLS basics | |
| You decide | Pick scope | |

**User's choice:** Core progression

### Q2: Simidrott vs SLS depth balance?

| Option | Description | Selected |
|--------|-------------|----------|
| Equal depth | Same completeness bar for both | |
| Simidrott deeper | Full core Simidrott, thinner SLS if sparse | ✓ |
| SLS minimal | Only badges with verified data | |
| You decide | Pick balance | |

**User's choice:** Simidrott deeper

### Q3: Category structure?

| Option | Description | Selected |
|--------|-------------|----------|
| Official category names | Mirror affisch tiers exactly | ✓ |
| Simplified grouping | Fewer, broader categories | |
| You decide | Pick structure | |

**User's choice:** Official category names

### Q4: Badge ordering within categories?

| Option | Description | Selected |
|--------|-------------|----------|
| Flat within category | No progression order | |
| Affisch order | sortOrder matches official sequence | ✓ |
| Prerequisite chain | Store unlock dependencies | |
| You decide | Pick ordering | |

**User's choice:** Affisch order

---

## Requirement Wording

### Q1: How should requirement text be written?

| Option | Description | Selected |
|--------|-------------|----------|
| Verbatim from PDFs | Exact official Swedish wording | ✓ |
| Parent-friendly summaries | Shorter swim-hall text | |
| Verbatim SV + summary EN | Full SV, simplified EN | |
| You decide | Pick approach | |

**User's choice:** Verbatim from PDFs (Swedish)

### Q2: What should textEn contain?

| Option | Description | Selected |
|--------|-------------|----------|
| Professional translation | Accurate English equivalent | |
| Machine-assisted + review | Auto-translate, verify samples | ✓ |
| Swedish only in seed | Duplicate textSv into textEn | |
| You decide | Pick translation approach | |

**User's choice:** Machine-assisted + review

### Q3: Requirement granularity?

| Option | Description | Selected |
|--------|-------------|----------|
| One skill per requirement | Each checkbox = one skill | |
| Grouped skills | Combine micro-skills | |
| Match affisch layout | Same bullet count as official | ✓ |
| You decide | Pick granularity | |

**User's choice:** Match affisch layout

### Q4: Verification scope before ship?

| Option | Description | Selected |
|--------|-------------|----------|
| Roadmap samples only | Baddaren, Hajen, SLS basics | |
| All badges verified | Every requirement checked | ✓ |
| One badge per tier | Spot-check per category | |
| You decide | Pick verification scope | |

**User's choice:** All badges verified

---

## Seed Update Behavior

### Q1: What happens when app updates with newer badge data?

| Option | Description | Selected |
|--------|-------------|----------|
| First launch only | Seed once, never update | |
| Re-seed on version bump | Replace catalog data | |
| Merge on update | Add/update badges, preserve progress | ✓ |
| You decide | Pick strategy | |

**User's choice:** Merge on update

### Q2: When to check for catalog updates?

| Option | Description | Selected |
|--------|-------------|----------|
| App update | Check on every launch after update | |
| Explicit version check | Only when bundled > stored version | |
| You decide | Pick trigger | ✓ |

**User's choice:** You decide
**Notes:** Explicit version check on launch recommended.

### Q3: Progress preservation when requirements change?

| Option | Description | Selected |
|--------|-------------|----------|
| Preserve orphan progress | Keep rows for removed requirements | |
| Prune orphans | Delete progress for removed skills | |
| Remap by code | Match by stable code field | ✓ |
| You decide | Pick preservation strategy | |

**User's choice:** Remap by code

### Q4: Where should source provenance metadata live?

| Option | Description | Selected |
|--------|-------------|----------|
| In seed JSON | sourceUrl per catalog in assets | |
| In database | Add metadata fields to CatalogEntity | |
| Docs only | LICENSE/SOURCES.md in repo | ✓ |
| You decide | Pick storage | |

**User's choice:** Docs only

---

## Claude's Discretion

- Image format/resolution (WebP recommended)
- Merge trigger timing (explicit version check on launch)
- Missing-image fallback visual (tier color + badge name)
- SLS subset selection within "main badges"
- Merge algorithm implementation details

## Deferred Ideas

None captured during this session.
