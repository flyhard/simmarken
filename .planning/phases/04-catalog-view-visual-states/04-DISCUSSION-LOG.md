# Phase 4: Catalog View & Visual States - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-23
**Phase:** 4-Catalog View & Visual States
**Areas discussed:** Dual-catalog navigation, Locked badge rules, In-progress visual, Badge grid layout, Category headers, State overlays, Badge detail placeholder

---

## Dual-Catalog Navigation

### Catalog switcher

| Option | Description | Selected |
|--------|-------------|----------|
| Tab row below top bar | "Simidrott" \| "SLS"; one catalog at a time | ✓ |
| Segmented button in top bar | Compact switcher next to child name | |
| Single long scroll | Both catalogs in one scroll, no switching | |
| You decide | | |

**User's choice:** Tab row below top bar

### Default catalog

| Option | Description | Selected |
|--------|-------------|----------|
| Default to Simidrott | Most kids start here | ✓ |
| Default to SLS | | |
| Remember last-used per child | | |
| You decide | | |

**User's choice:** Default to Simidrott

### Tab labels (Phase 4, pre-i18n)

| Option | Description | Selected |
|--------|-------------|----------|
| Short Swedish: "Simidrott" \| "SLS" | | ✓ |
| Full names | | |
| Icons only | | |
| You decide | | |

**User's choice:** Short Swedish labels

### Tab persistence

| Option | Description | Selected |
|--------|-------------|----------|
| Session only — reset to Simidrott on each open | | ✓ |
| Remember per child | | |
| Remember globally | | |
| You decide | | |

**User's choice:** Session only

### Empty catalog behavior

**User's choice:** You decide (Claude discretion)

---

## Locked Badge Rules

### Locked definition

| Option | Description | Selected |
|--------|-------------|----------|
| Zero requirements checked | | ✓ |
| Sequential unlock within category | | |
| First badge per category unlocked only | | |
| You decide | | |

**User's choice:** Zero requirements checked

### Locked visibility

| Option | Description | Selected |
|--------|-------------|----------|
| All badges visible, grayscale when locked | | ✓ |
| Hide locked badges until unlocked | | |
| You decide | | |

**User's choice:** All visible

### First check transition

| Option | Description | Selected |
|--------|-------------|----------|
| First requirement checked → IN_PROGRESS immediately | | ✓ |
| Stay locked until threshold | | |
| You decide | | |

**User's choice:** Immediate IN_PROGRESS

### Cross-category gating

| Option | Description | Selected |
|--------|-------------|----------|
| Categories independent — no tier gating | | ✓ |
| Must complete prior tier category first | | |
| You decide | | |

**User's choice:** Independent categories

---

## In-Progress Visual

### Style

| Option | Description | Selected |
|--------|-------------|----------|
| Circular progress ring by requirement count | | ✓ |
| Partial color saturation | | |
| Fraction overlay (e.g. "2/5") | | |
| You decide | | |

**User's choice:** Progress ring

### Ring color

| Option | Description | Selected |
|--------|-------------|----------|
| Theme primary color | | ✓ |
| Category/tier color | | |
| Child avatar color | | |
| You decide | | |

**User's choice:** Theme primary

### Pin image while in progress

| Option | Description | Selected |
|--------|-------------|----------|
| Grayscale pin + colored ring | | ✓ |
| Full-color pin + ring | | |
| You decide | | |

**User's choice:** Grayscale pin

### Transition to achieved-to-buy

| Option | Description | Selected |
|--------|-------------|----------|
| Full color + cart overlay, ring removed | | ✓ |
| Ring completes to 100% then cart | | |
| You decide | | |

**User's choice:** Full color, no ring

---

## Badge Grid Layout

### Columns

| Option | Description | Selected |
|--------|-------------|----------|
| 3 columns | Larger pins for swim-hall | ✓ |
| 4 columns | Denser grid | |
| Adaptive by screen width | | |
| You decide | | |

**User's choice:** 3 columns

### Names under pins

| Option | Description | Selected |
|--------|-------------|----------|
| Name below, 1-line truncate | | ✓ |
| Pin only — name in detail | | |
| You decide | | |

**User's choice:** Name below

### Spacing

| Option | Description | Selected |
|--------|-------------|----------|
| Comfortable (12–16dp) | Thumb-friendly | ✓ |
| Compact (8dp) | | |
| You decide | | |

**User's choice:** Comfortable

### Tile shape

| Option | Description | Selected |
|--------|-------------|----------|
| Square cells | | ✓ |
| Circular clip | | |
| You decide | | |

**User's choice:** Square

---

## Category Headers

### Collapsible

| Option | Description | Selected |
|--------|-------------|----------|
| Always expanded | Fewer taps at swim hall | ✓ |
| Collapsible sections | | |
| Collapse empty categories | | |
| You decide | | |

**User's choice:** Always expanded

### Sticky headers

| Option | Description | Selected |
|--------|-------------|----------|
| Sticky while scrolling | | ✓ |
| Static headers | | |
| You decide | | |

**User's choice:** Sticky

### Language

| Option | Description | Selected |
|--------|-------------|----------|
| Swedish only (nameSv) | English in Phase 6 | ✓ |
| Swedish + English sublabels | | |
| You decide | | |

**User's choice:** Swedish only

### Order

| Option | Description | Selected |
|--------|-------------|----------|
| Official sortOrder from seed | | ✓ |
| Alphabetical | | |
| You decide | | |

**User's choice:** sortOrder

---

## State Overlays

### Achieved-to-buy

| Option | Description | Selected |
|--------|-------------|----------|
| Shopping cart icon | Signals buy the pin | ✓ |
| Pin/medal icon | | |
| Text badge "Köp" | | |
| You decide | | |

**User's choice:** Cart icon

### Gotten

| Option | Description | Selected |
|--------|-------------|----------|
| Checkmark | Done and bought | ✓ |
| Star | | |
| Checkmark in circle | | |
| You decide | | |

**User's choice:** Checkmark

### Locked treatment

| Option | Description | Selected |
|--------|-------------|----------|
| Grayscale only — no extra icon | | ✓ |
| Lock icon | | |
| Dim scrim | | |
| You decide | | |

**User's choice:** Grayscale only

### Overlay style

| Option | Description | Selected |
|--------|-------------|----------|
| Filled circle badge with white icon | Bottom-right | ✓ |
| Flat icon on image | | |
| You decide | | |

**User's choice:** Circle badge

---

## Badge Detail Placeholder

### Content

| Option | Description | Selected |
|--------|-------------|----------|
| Large image + name + "Checklista kommer snart" | | ✓ |
| Minimal — name in bar only | | |
| Full-screen image only | | |
| You decide | | |

**User's choice:** Image + name + stub text

### Navigation

| Option | Description | Selected |
|--------|-------------|----------|
| New route BadgeDetail(kidId, badgeId) on stack | | ✓ |
| Bottom sheet / dialog | | |
| You decide | | |

**User's choice:** New route

### Checklist in Phase 4

| Option | Description | Selected |
|--------|-------------|----------|
| View only — no toggles until Phase 5 | | ✓ |
| Grayed-out checklist preview | | |
| You decide | | |

**User's choice:** View only

### Visual state on detail

| Option | Description | Selected |
|--------|-------------|----------|
| Same 4-tier visuals as grid | | ✓ |
| Always full-color on detail | | |
| You decide | | |

**User's choice:** Match grid state

---

## Claude's Discretion

- Empty-catalog tab behavior
- Progress ring dimensions and animation
- Grayscale filter implementation
- Coil sizes and missing-image fallback rendering
- Lazy list structure for sticky headers + category grids
- Swedish hardcoded strings until Phase 6

## Deferred Ideas

None recorded.
