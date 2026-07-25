# Phase 6: Export/Import & i18n - Context

**Gathered:** 2026-07-24
**Status:** Ready for planning

<domain>
## Phase Boundary

Parents can back up and restore child progress via versioned JSON, and use the app in Swedish or English. Phase 6 delivers a Settings screen (gear on Home), export via share sheet with optional per-kid selection, import via file picker and share/open intents with merge semantics and confirmation, full UI string resources for chrome + progress copy, and a persisted language picker (DATA-02, DATA-03, I18N-01, I18N-02, I18N-03). Catalog badge/requirement text from seed stays Swedish in all languages.

</domain>

<decisions>
## Implementation Decisions

### Import & Merge Behavior
- **D-01:** Import uses **merge**, not full replace — existing kids on device are kept; progress updates where kid IDs match in the file.
- **D-02:** Kids in the import file that **do not exist** on device are **not created automatically** — parent must confirm each new kid before creation.
- **D-03:** **Always confirm** before writing any import — show a summary of what will be updated and which new kids would be added; parent taps Import or Cancel.
- **D-04:** When the same kid + badge/requirement exists locally and in the file with conflicting values, **keep the newer** record per item using timestamp comparison. — **Reversibility:** costly — conflict-resolution rules are embedded in export schema and merge logic.

### Export Behavior
- **D-05:** Export includes selected kids and their `RequirementProgress` + `BadgeProgress` rows (not catalog seed data).
- **D-06:** **One kid on device** → generate JSON and open share sheet immediately (no kid picker).
- **D-07:** **Two or more kids** → show kid picker first (select specific children or **All**), then generate JSON and open share sheet.
- **D-08:** Export uses Android share sheet (save to Files, Drive, email, etc.); no in-app "save to path" browser. — **Reversibility:** one-way — published JSON backup format with `exportVersion` must stay forward-compatible per PITFALLS.md.

### Settings & Navigation
- **D-09:** Settings entry = **gear icon** in Home screen top app bar (one tap from kid cards).
- **D-10:** Settings is a new navigation route pushed from Home (same stack pattern as catalog/detail).
- **D-11:** Settings screen groups **Language** and **Data** (export/import) in separate sections — exact Material layout at implementer discretion.
- **D-12:** **Import entry points:** (a) "Import backup" row in Settings opens system document picker (`.json`); (b) app accepts files **opened or shared into** the app from another app (intent filter / share target).

### Language Scope
- **D-13:** **Catalog content stays Swedish** in all language modes — use `nameSv`, `textSv`, and Swedish category names from seed; do **not** switch to `nameEn`/`textEn` when English UI is selected.
- **D-14:** **App chrome translates** — buttons, labels, Settings, navigation, empty states, dialogs, accessibility strings for UI chrome.
- **D-15:** **Progress/summary UI translates with chrome** — home card lines ("pågår", "att köpa"), badge detail progress subtitle ("X av Y klara"), purchase toggle label, confirmation dialogs, etc. move to `strings.xml` / `values-en`.
- **D-16:** Catalog tab labels **"Simidrott"** and **"SLS"** stay **as-is** in both languages (official catalog names).
- **D-17:** Import/export feedback (errors, success toasts, confirmation copy) **translates with chrome**.

### Language Preference
- **D-18:** **First install** (no saved preference): follow **system locale** (Swedish UI if phone is Swedish, English if English).
- **D-19:** Language picker offers **three options:** System default · Svenska · English (radio list or equivalent).
- **D-20:** Language change applies **immediately** — no restart required; recompose with selected locale.
- **D-21:** Language preference persisted in **DataStore** (device-only); **not included** in backup JSON — new phone uses system default until parent sets preference again.
- **D-22:** Explicit Svenska or English choice **overrides** system locale until parent changes it back to System default.

### Claude's Discretion
- Exact Settings section layout and row styling (grouped Language + Data per D-11)
- Export filename pattern (e.g. `simmarken-backup-YYYY-MM-DD.json`)
- Timestamp field used for D-04 conflict resolution (`updatedAt` on progress entities vs export metadata)
- Per-new-kid confirmation UX during import (inline checklist in summary vs sequential dialogs)
- `AppCompatDelegate` / `setApplicationLocales` vs Compose `CompositionLocalProvider` for locale application
- Whether export kid picker uses checkboxes or chips; default selection when opening picker
- Import intent MIME types and `ACTION_VIEW` / `ACTION_SEND` handling details

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project scope & requirements
- `.planning/PROJECT.md` — JSON export/import for phone migration; Swedish + English UI; 100% offline
- `.planning/REQUIREMENTS.md` — DATA-02, DATA-03, I18N-01, I18N-02, I18N-03
- `.planning/ROADMAP.md` — Phase 6 goal, success criteria, plan breakdown (06-01 through 06-03)

### Architecture & pitfalls
- `.planning/research/ARCHITECTURE.md` — `ExportRepository`, `settings/` package, kotlinx-serialization
- `.planning/research/STACK.md` — kotlinx-serialization-json for backup format; DataStore for locale
- `.planning/research/PITFALLS.md` — Pitfall: versioned JSON schema (`exportVersion: 1`); validate on import; DataStore for locale persistence
- `.planning/research/FEATURES.md` — Export/import as P1 phone migration; share deferred to v2

### Prior phase decisions
- `.planning/phases/01-android-foundation-database/01-CONTEXT.md` — `KidEntity`, `RequirementProgress`, `BadgeProgress` schema; `achievedAt` write-once
- `.planning/phases/03-child-profiles-home/03-CONTEXT.md` — Home screen, FAB, child cards; hardcoded Swedish until Phase 6
- `.planning/phases/04-catalog-view-visual-states/04-CONTEXT.md` — Tab labels Simidrott/SLS; `nameSv` in Phase 4
- `.planning/phases/05-progress-tracking-badge-detail/05-CONTEXT.md` — Progress UI strings to i18n in Phase 6; progress rows written here are export payload

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `KidEntity`, `RequirementProgressEntity`, `BadgeProgressEntity` — export/import payload tables
- `KidRepository`, `ProgressRepository` — read/write paths for serialize and merge restore
- `SimmarkenNavHost` + `Routes.kt` — add `Settings` route; gear `IconButton` on `HomeScreen` top bar
- `AppContainer` — wire `ExportRepository`, locale DataStore, Settings ViewModel factory
- Seed entities already store `nameSv`/`nameEn`, `textSv`/`textEn` — Phase 6 continues displaying Swedish catalog fields only (D-13)
- `app/src/main/res/values/strings.xml` — minimal today; expand with `values-en/strings.xml` for chrome + progress UI

### Established Patterns
- MVVM + manual ViewModel factories (no Hilt)
- Navigation Compose type-safe routes (`Home`, `ChildCatalog`, `BadgeDetail`)
- Repository writes on `Dispatchers.IO`
- UI currently hardcodes Swedish in Composables and uses `nameSv`/`textSv` in mappers — Phase 6 migrates chrome to `stringResource`

### Integration Points
- Home top bar: add settings gear alongside existing home chrome
- Settings screen: language section + export/import actions
- `MainActivity` / `SimmarkenApplication`: apply persisted locale at startup and on preference change
- Android share sheet for export; `ActivityResultContracts` for import file picker; manifest intent filters for open/share JSON

</code_context>

<specifics>
## Specific Ideas

- Export is selective when multiple kids — parent may backup one child for sharing with co-parent without exporting everyone
- Import is cautious — always preview + confirm; never silently wipe or add kids
- English mode is for expat/international parents using the app chrome; official badge terminology stays Swedish as at the pool
- Phone migration story: export on old phone → share to Files/Drive → import on new phone with merge + confirm

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

(v2 SHAR-01/SHAR-02 share-as-text/image and per-child formatted export remain out of scope per REQUIREMENTS.md.)

</deferred>

---

*Phase: 6-Export/Import & i18n*
*Context gathered: 2026-07-24*
