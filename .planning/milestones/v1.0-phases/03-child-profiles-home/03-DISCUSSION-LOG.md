# Phase 3: Child Profiles & Home - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-23
**Phase:** 3-Child Profiles & Home
**Areas discussed:** Home card layout, Add/edit child flow

---

## Home Card Layout

### Card arrangement

| Option | Description | Selected |
|--------|-------------|----------|
| Single-column list | One card per row, easy one-handed tap at the pool | ✓ |
| Two-column grid | More kids visible at once, smaller tap targets | |
| Horizontal carousel | Swipe between kids | |

**User's choice:** Single-column list

### Card content

| Option | Description | Selected |
|--------|-------------|----------|
| Name + color accent | Colored circle/avatar with name; no progress counts in Phase 3 | ✓ |
| Name only | Minimal text card | |
| Name + color + placeholder subtitle | e.g. "Tap to view badges" | |

**User's choice:** Name + color accent

### Empty state

| Option | Description | Selected |
|--------|-------------|----------|
| Centered message + FAB | "Add your first child" with + always visible | ✓ |
| FAB only | No empty-state text | |
| Illustration + message | Friendly empty state with icon | |

**User's choice:** Centered message + FAB

### Card ordering

| Option | Description | Selected |
|--------|-------------|----------|
| Creation order | First added stays on top | ✓ |
| Alphabetical by name | Re-sorts when names change | |
| Manual drag-to-reorder | Parent controls order | |

**User's choice:** Creation order

---

## Add/Edit Child Flow

### Form presentation

| Option | Description | Selected |
|--------|-------------|----------|
| Bottom sheet | Slides up from FAB, contextual, easy to dismiss | ✓ |
| Full-screen form | Dedicated add/edit screen | |
| Centered modal dialog | Compact overlay | |

**User's choice:** Bottom sheet

### Name input rules

| Option | Description | Selected |
|--------|-------------|----------|
| Required, trimmed | Empty/whitespace blocked; max ~30 chars | ✓ |
| Required, no max | Any non-empty name | |
| Optional with default | Falls back to "Child" | |

**User's choice:** Required, trimmed

### Color/avatar picker

| Option | Description | Selected |
|--------|-------------|----------|
| Color swatch grid | 8–12 preset colors as tappable circles | |
| Preset swatches + custom | Adds color wheel or system picker | |
| Initials on colored circle | Auto-pick color from name hash; override with swatches | ✓ |

**User's choice:** Initials on colored circle with swatch override

### Edit/delete access (captured during add/edit flow)

| Option | Description | Selected |
|--------|-------------|----------|
| Overflow menu on card | ⋮ opens Edit / Delete; edit reuses bottom sheet | ✓ |
| Long-press context menu | Hold card for options | |
| Separate edit icon | Tap card = catalog; pencil for edit | |

**User's choice:** Overflow menu on card

---

## Claude's Discretion

- Delete confirmation dialog copy and styling
- Preset swatch palette and hash-to-color algorithm
- Catalog placeholder screen content until Phase 4
- Bottom sheet UX details (peek height, validation messages)
- Swedish hardcoded strings until Phase 6 i18n

## Deferred Ideas

None recorded.
