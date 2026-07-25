# Phase 6: Export/Import & i18n - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-24
**Phase:** 6-Export/Import & i18n
**Areas discussed:** Import overwrite behavior, Settings & backup entry, Language scope, Default language & switch feel

---

## Import Overwrite Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Full replace | Wipe kids + progress, restore from file | |
| Merge | Keep existing kids; add/update where IDs match | ✓ |
| You decide | | |

**User's choice:** Merge (option 2)

**Follow-up — unknown kids in file:**

| Option | Description | Selected |
|--------|-------------|----------|
| Add them automatically | | |
| Skip unknown kids | | |
| Create only after asking | Parent must confirm before new kid is created | ✓ |

**User's choice:** "import should only create after asking"

**Follow-up — confirm before write:**

| Option | Description | Selected |
|--------|-------------|----------|
| Always confirm | Summary of updates/adds, then Import/Cancel | ✓ |
| Confirm only for new kids | | |
| You decide | | |

**User's choice:** Always confirm (option 1)

**Follow-up — conflict resolution:**

| Option | Description | Selected |
|--------|-------------|----------|
| File wins | Import overwrites local progress | |
| Keep newer | Timestamp comparison per item | ✓ |
| You decide | | |

**User's choice:** Keep newer (option 2)

---

## Settings & Backup Entry

| Option | Description | Selected |
|--------|-------------|----------|
| Gear icon on Home | Top app bar | ✓ |
| Overflow menu on Home | | |
| You decide | | |

**User's choice:** Gear icon on Home (option 1)

**Follow-up — Settings layout:**

| Option | Description | Selected |
|--------|-------------|----------|
| Single screen | Language + export/import rows | |
| Grouped sections | Language + Data sections | |
| You decide | Implementer chooses grouped sections | ✓ |

**User's choice:** You decide (option 3)

**Follow-up — export flow:**

| Option | Description | Selected |
|--------|-------------|----------|
| Share sheet immediately | | |
| Confirm first | | |
| Kid picker when 2+ kids | Select which kids or All, then share sheet; 1 kid exports directly | ✓ |

**User's choice:** Custom — if more than 1 kid, select which kids or all before share sheet

**Follow-up — import entry:**

| Option | Description | Selected |
|--------|-------------|----------|
| System file picker only | | |
| Share-target only | | |
| Both | Settings button + open/share into app | ✓ |

**User's choice:** Both (option 3)

---

## Language Scope

| Option | Description | Selected |
|--------|-------------|----------|
| Everything | Chrome + catalog text (nameEn/textEn) | |
| App chrome only | Catalog stays Swedish | ✓ |
| You decide | | |

**User's choice:** App chrome only (option 2)

**Follow-up — progress/summary UI:**

| Option | Description | Selected |
|--------|-------------|----------|
| Translate with chrome | "pågår", köpt toggle, etc. | ✓ |
| Keep Swedish always | | |
| You decide | | |

**User's choice:** Translate with chrome (option 1)

**Follow-up — tab labels Simidrott/SLS:**

| Option | Description | Selected |
|--------|-------------|----------|
| Keep as-is | Official catalog names | ✓ |
| Translate with chrome | | |
| You decide | | |

**User's choice:** Keep as-is (option 1)

**Follow-up — import/export messages:**

| Option | Description | Selected |
|--------|-------------|----------|
| Translate with chrome | Errors, toasts, dialogs | ✓ |
| Swedish only | | |
| You decide | | |

**User's choice:** Translate with chrome (option 1)

---

## Default Language & Switch Feel

| Option | Description | Selected |
|--------|-------------|----------|
| Follow system | Swedish/English from phone locale on first install | ✓ |
| Swedish always | | |
| You decide | | |

**User's choice:** Follow system (option 1)

**Follow-up — when change takes effect:**

| Option | Description | Selected |
|--------|-------------|----------|
| Immediately | | ✓ (Claude discretion) |
| After restart | | |
| You decide | User deferred to Claude | ✓ |

**User's choice:** You decide (option 3) — recorded as immediate in CONTEXT.md

**Follow-up — language picker options:**

| Option | Description | Selected |
|--------|-------------|----------|
| Two options | Svenska / English | |
| Three options | System default / Svenska / English | ✓ |
| You decide | | |

**User's choice:** Three options (option 2)

**Follow-up — language in backup JSON:**

| Option | Description | Selected |
|--------|-------------|----------|
| Device-only | Not in export | ✓ |
| Include in export | Restore on new phone | |
| You decide | | |

**User's choice:** Device-only (option 1)

---

## Claude's Discretion

- Settings section layout (grouped Language + Data)
- Language applies immediately on change
- Export filename, conflict timestamp fields, per-new-kid confirmation UX细节
- Locale implementation mechanism (AppCompat vs Compose)

## Deferred Ideas

None captured during discussion.
