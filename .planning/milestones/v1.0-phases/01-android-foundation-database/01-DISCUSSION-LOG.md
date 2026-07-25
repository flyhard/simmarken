# Phase 1: Android Foundation & Database - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-22
**Phase:** 1-Android Foundation & Database
**Areas discussed:** Progress table shape

---

## Progress Table Shape

### Q1: How should kid progress be stored in Room?

| Option | Description | Selected |
|--------|-------------|----------|
| Both tables | RequirementProgress per skill + BadgeProgress for isGotten only | ✓ |
| RequirementProgress only | Derive everything; add purchase flag later | |
| You decide | Pick what fits the roadmap best | |

**User's choice:** Both tables
**Notes:** Aligns with core value prop — skill achievement and physical pin purchase are separate.

### Q2: What fields should BadgeProgress store?

| Option | Description | Selected |
|--------|-------------|----------|
| isGotten only | Badge achievement derived from requirements at read time | |
| isGotten + gottenAt | When parent marked pin purchased | |
| Full badge progress | isGotten + achievedAt + gottenAt | ✓ |

**User's choice:** Full badge progress (isGotten + achievedAt + gottenAt)
**Notes:** User wants achievement date cached, not only derived at query time.

### Q3: How should achievedAt on BadgeProgress behave?

| Option | Description | Selected |
|--------|-------------|----------|
| Write on last check | Set once when last requirement checked, never auto-clear | ✓ |
| Derive only | Don't store achievedAt — derive from latest requirement check | |
| Nullable placeholder | Store column but leave null until Phase 5 | |

**User's choice:** Write achievedAt when last requirement checked
**Notes:** Phase 1 creates column; Phase 5 implements write logic. Achievement moment is preserved even if requirements later unchecked.

### Q4: What should each RequirementProgress row track?

| Option | Description | Selected |
|--------|-------------|----------|
| Boolean only | isAchieved boolean — minimal schema | |
| With timestamp | isAchieved + achievedAt — when parent checked the skill | ✓ |
| With notes | isAchieved + achievedAt + optional notes field | |

**User's choice:** isAchieved + achievedAt
**Notes:** Bidirectional toggle required — pitfalls research flags missing uncheck as common gap.

---

## Claude's Discretion

- App shell, migration posture, catalog i18n columns, min SDK, DI — user did not discuss; defaults from research apply.

## Deferred Ideas

None captured during discussion.
