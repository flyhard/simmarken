# Phase 5: Progress Tracking & Badge Detail - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-23
**Phase:** 5-Progress Tracking & Badge Detail
**Areas discussed:** Requirement checklist, Purchase toggle, Unchecking rules, Home card summaries

---

## Requirement Checklist

| Option | Description | Selected |
|--------|-------------|----------|
| Pin fixed at top | Large pin stays visible, checklist scrolls below | ✓ |
| Everything scrolls | Pin scrolls away with checklist | |
| Checkbox + full text | Material Checkbox + full textSv, tap whole row | ✓ |
| Compact list | Smaller text, tighter spacing | |
| Zero-req: note + toggle only | No checklist for Droppen-type badges | ✓ |
| Empty checklist section | Empty list + toggle below pin | |
| "X av Y klara" subtitle | Progress count below badge name | ✓ |
| Ring only | No text progress summary | |

**User's choice:** Pin fixed; checkbox + full text; zero-req = note + purchase only; progress subtitle.
**Notes:** Aligns with existing `BadgePinVisual` detail size and seed `textSv` fields.

---

## Purchase Toggle

| Option | Description | Selected |
|--------|-------------|----------|
| Below checklist | "Fysiskt märke köpt" Switch after skills | ✓ |
| Below pin, above checklist | Purchase visible before scrolling | |
| Disabled until achieved | Can't mark köpt until all skills done | ✓ |
| Always enabled | Mark köpt anytime | |
| Reversible with confirm | Un-mark köpt needs confirmation dialog | ✓ |
| One-way köpt | Cannot undo | |
| Zero-req: toggle only | Sole interactive control on detail | ✓ |

**User's choice:** Below checklist; disabled until achieved; confirm on köpt undo; zero-req toggle only.
**Notes:** Zero-requirement badges skip disable rule (already ACHIEVED_TO_BUY).

---

## Unchecking Rules

| Option | Description | Selected |
|--------|-------------|----------|
| Revert visual state | ACHIEVED_TO_BUY → IN_PROGRESS; keep achievedAt | ✓ |
| Block uncheck when achieved | Lock requirements until köpt | |
| Auto-clear isGotten | Unchecking while GOTTEN clears köpt | ✓ |
| Block uncheck while köpt | Must un-mark köpt first | |
| Back to LOCKED | 0 skills → LOCKED state | ✓ |
| No confirm on uncheck | Instant requirement toggle | ✓ |

**User's choice:** Revert visual; auto-clear köpt on skill uncheck; LOCKED at zero; no uncheck confirm.
**Notes:** Distinct from D-07 köpt toggle undo dialog — skill uncheck clears köpt silently.

---

## Home Card Summaries

| Option | Description | Selected |
|--------|-------------|----------|
| Two subtitle lines | "2 pågår" and "1 att köpa" under name | ✓ |
| Single inline line | "2 pågår · 1 att köpa" | |
| Chips/icons | Visual count badges | |
| Hide zero lines | Only show counts > 0 | ✓ |
| Both catalogs | Simidrott + SLS combined | ✓ |
| No gotten count | Only pågår + att köpa | ✓ |

**User's choice:** Subtitle lines; hide zeros; both catalogs; no köpta count.
**Notes:** Uses `BadgeStateCalculator` aggregates per kid across all badges.

---

## Claude's Discretion

- Dialog copy, scroll structure, animations, repository helpers, summary computation location, disabled switch styling

## Deferred Ideas

None.
