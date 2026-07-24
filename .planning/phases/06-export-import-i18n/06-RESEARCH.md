# Phase 6: Export/Import & i18n - Research

**Researched:** 2026-07-24
**Domain:** Android offline JSON backup/restore + per-app locale (Kotlin/Compose/Room)
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Import & Merge Behavior
- **D-01:** Import uses **merge**, not full replace — existing kids on device are kept; progress updates where kid IDs match in the file.
- **D-02:** Kids in the import file that **do not exist** on device are **not created automatically** — parent must confirm each new kid before creation.
- **D-03:** **Always confirm** before writing any import — show a summary of what will be updated and which new kids would be added; parent taps Import or Cancel.
- **D-04:** When the same kid + badge/requirement exists locally and in the file with conflicting values, **keep the newer** record per item using timestamp comparison. — **Reversibility:** costly — conflict-resolution rules are embedded in export schema and merge logic.

#### Export Behavior
- **D-05:** Export includes selected kids and their `RequirementProgress` + `BadgeProgress` rows (not catalog seed data).
- **D-06:** **One kid on device** → generate JSON and open share sheet immediately (no kid picker).
- **D-07:** **Two or more kids** → show kid picker first (select specific children or **All**), then generate JSON and open share sheet.
- **D-08:** Export uses Android share sheet (save to Files, Drive, email, etc.); no in-app "save to path" browser. — **Reversibility:** one-way — published JSON backup format with `exportVersion` must stay forward-compatible per PITFALLS.md.

#### Settings & Navigation
- **D-09:** Settings entry = **gear icon** in Home screen top app bar (one tap from kid cards).
- **D-10:** Settings is a new navigation route pushed from Home (same stack pattern as catalog/detail).
- **D-11:** Settings screen groups **Language** and **Data** (export/import) in separate sections — exact Material layout at implementer discretion.
- **D-12:** **Import entry points:** (a) "Import backup" row in Settings opens system document picker (`.json`); (b) app accepts files **opened or shared into** the app from another app (intent filter / share target).

#### Language Scope
- **D-13:** **Catalog content stays Swedish** in all language modes — use `nameSv`, `textSv`, and Swedish category names from seed; do **not** switch to `nameEn`/`textEn` when English UI is selected.
- **D-14:** **App chrome translates** — buttons, labels, Settings, navigation, empty states, dialogs, accessibility strings for UI chrome.
- **D-15:** **Progress/summary UI translates with chrome** — home card lines ("pågår", "att köpa"), badge detail progress subtitle ("X av Y klara"), purchase toggle label, confirmation dialogs, etc. move to `strings.xml` / `values-en`.
- **D-16:** Catalog tab labels **"Simidrott"** and **"SLS"** stay **as-is** in both languages (official catalog names).
- **D-17:** Import/export feedback (errors, success toasts, confirmation copy) **translates with chrome**.

#### Language Preference
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

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope.

(v2 SHAR-01/SHAR-02 share-as-text/image and per-child formatted export remain out of scope per REQUIREMENTS.md.)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| DATA-02 | Parent can export all progress data to a file | Versioned kotlinx-serialization JSON + FileProvider share sheet; kid picker when 2+; payload = kids + progress only |
| DATA-03 | Parent can import progress data from an exported file | Parse/validate → preview/confirm → merge; document picker + ACTION_VIEW/SEND; clear errors for invalid/incompatible |
| I18N-01 | App UI available in Swedish | Default `values/strings.xml` (Swedish chrome + progress copy) |
| I18N-02 | App UI available in English | `values-en/strings.xml`; catalog fields remain `*Sv` (D-13) |
| I18N-03 | Parent can switch language in settings | Settings route + DataStore preference + `AppCompatDelegate.setApplicationLocales` |
</phase_requirements>

## Summary

Phase 6 adds the phone-migration and bilingual polish layers on top of an already-complete progress stack. Export/import must serialize **user data only** (kids + requirement/badge progress) into a versioned JSON document, share it via the system sheet, and restore via **merge-with-confirm** — never wipe local kids, never auto-create kids from the file, and never silently apply conflicts. i18n moves ~40 hardcoded Swedish chrome/progress strings into `stringResource`, keeps catalog badge/requirement text Swedish in all locales (D-13), and persists a three-way language preference (System / Svenska / English) in DataStore while applying locales through the official AppCompat per-app language API.

Two schema gaps must be planned explicitly: (1) Room auto-increment `kid.id` cannot match kids across devices — export needs a **stable kid UUID**; (2) progress rows lack a general `updatedAt`, and uncheck clears `achievedAtEpochMillis`, so D-04 newer-wins needs an explicit **write-time timestamp** (recommended: add `updatedAtEpochMillis` on both progress tables). kotlinx-serialization is already in the project; DataStore Preferences and AppCompat are not. Locale apply requires migrating `MainActivity` to `AppCompatActivity` and the theme parent to AppCompat.

**Primary recommendation:** Ship `ExportRepository` + stable-ID Room migration + merge planner UI first (06-01), then bulk `stringResource` extraction (06-02), then Settings + locale DataStore wired through `AppCompatDelegate` (06-03).

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Export JSON serialization | API / Backend (app data layer) | Database / Storage | `ExportRepository` reads Room via repos/DAOs; pure Kotlin DTOs |
| Share sheet / FileProvider | Browser / Client (Android framework) | CDN / Static — | Intent + content URI; no network |
| Import parse + schema validate | API / Backend (data layer) | Browser / Client | Untrusted JSON → typed DTOs before UI preview |
| Import preview / confirm | Browser / Client | — | Parent gates all writes (D-02, D-03) |
| Merge write (newer-wins) | API / Backend | Database / Storage | Transactional Room upserts; conflict rules in domain |
| Language preference persist | Database / Storage (DataStore) | — | Device-only; excluded from backup (D-21) |
| Locale apply / string resolve | Browser / Client | Frontend Server — | `setApplicationLocales` + resource system |
| Catalog display language | Database / Storage (seed fields) | Browser / Client | Always bind `nameSv`/`textSv` (D-13) |
| Settings navigation | Browser / Client | — | Compose NavHost route from Home gear |

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| kotlinx-serialization-json | 1.7.3 (in `libs.versions.toml`) | Export/import DTO encode/decode | Already used by `CatalogSeedParser`; versioned backup schema [VERIFIED: gradle/libs.versions.toml] |
| Room | 2.8.4 | Persist kids/progress; migration for stableId/updatedAt | Existing SSOT [VERIFIED: gradle/libs.versions.toml] |
| Jetpack Compose + Navigation | BOM 2025.12.01 / nav 2.9.8 | Settings UI + routes | Existing pattern [VERIFIED: gradle/libs.versions.toml] |
| DataStore Preferences | 1.2.1 | Language preference (System/SV/EN) | Official Android settings storage; locked by D-21 [CITED: developer.android.com/jetpack/androidx/releases/datastore] |
| AndroidX AppCompat | 1.7.x (latest stable) | `AppCompatDelegate.setApplicationLocales` | Official per-app language API; Activity must be `AppCompatActivity` [CITED: developer.android.com/guide/topics/resources/app-languages] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| androidx.core FileProvider | via existing `core-ktx` 1.15.0 | content:// URI for share sheet | Export share (D-08) [CITED: developer.android.com/training/secure-file-sharing/setup-sharing] |
| Activity Result Contracts | via activity-compose 1.9.3 | `OpenDocument` / `GetContent` for import | Settings import picker (D-12a) [ASSUMED: Activity Result API already transitive] |
| Material 3 | Compose BOM | Settings sections, radio list, dialogs | Match existing UI |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `AppCompatDelegate.setApplicationLocales` | Compose-only `CompositionLocalProvider` + manual string maps | Breaks system resource resolution, a11y, and Android 13 per-app language settings — **do not use** for this phase |
| DataStore (D-21) | AppCompat `autoStoreLocales` only | Conflicts with three-way System/SV/EN model and D-21; use DataStore as source of truth, call `setApplicationLocales` from it |
| FileProvider + ACTION_SEND | `CreateDocument` SAF save-only | D-08 requires share sheet (Files/Drive/email), not only SAF create |
| Room auto `kid.id` in export | Name matching | Fragile across rename/devices; use UUID `stableId` |

**Installation:**

```kotlin
// libs.versions.toml
datastore = "1.2.1"
appcompat = "1.7.1" // confirm latest stable at implement time

// libraries
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }

// app/build.gradle.kts
implementation(libs.androidx.datastore.preferences)
implementation(libs.androidx.appcompat)
```

**Version verification:** DataStore Preferences **1.2.1** confirmed on official AndroidX release notes (2026). AppCompat version pin should be re-checked against Google Maven at implement time — use current 1.7.x stable. [CITED: developer.android.com/jetpack/androidx/releases/datastore]

## Package Legitimacy Audit

> `gsd-tools query package-legitimacy` seam unavailable in this runtime (`gsd-tools.cjs` not found under `$HOME/.cursor/get-shit-done` or `$HOME/.cursor/gsd-core`). Legitimacy assessed via official Android documentation + Google Maven identity.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| androidx.datastore:datastore-preferences | Google Maven | years (1.0.0 Aug 2021 → 1.2.1) | very high | android/androidx-main | OK (official AndroidX) | Approved |
| androidx.appcompat:appcompat | Google Maven | years | very high | android/androidx-main | OK (official AndroidX) | Approved |
| org.jetbrains.kotlinx:kotlinx-serialization-json | Maven Central | years (already in project) | very high | Kotlin/kotlinx.serialization | OK (already shipped) | Approved — no new install |

**Packages removed due to [SLOP] verdict:** none  
**Packages flagged as suspicious [SUS]:** none  

*No WebSearch-discovered third-party packages recommended. Only first-party AndroidX + already-vendored kotlinx-serialization.*

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│  ENTRY POINTS                                                    │
│  Home gear → Settings                                            │
│  Settings: Export / Import (SAF picker)                          │
│  Manifest: ACTION_VIEW / ACTION_SEND (JSON) → MainActivity       │
└───────────────┬─────────────────────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────────┐
│  SettingsViewModel / ImportSession                               │
│  - language preference ↔ LocalePreferencesRepository (DataStore) │
│  - export: kid selection → ExportRepository.export(kids)         │
│  - import: Uri → bytes → parse/validate → PreviewUiState         │
│  - confirm → ExportRepository.merge(preview decisions)           │
└───────────────┬─────────────────────────────────────────────────┘
                │
        ┌───────┴────────┐
        ▼                ▼
┌───────────────┐  ┌──────────────────────────────────────────────┐
│ FileProvider  │  │ ExportRepository (data/export/)               │
│ cache/exports │  │  encode: Kid + Progress → BackupDto           │
│ ACTION_SEND   │  │  decode: Json.decode → validate exportVersion │
└───────────────┘  │  merge: match stableId; map codes→Room IDs    │
                   │       newer-wins on updatedAtEpochMillis      │
                   └───────────────┬──────────────────────────────┘
                                   │
                                   ▼
                   ┌───────────────────────────────┐
                   │ Room: kids, requirement_      │
                   │ progress, badge_progress      │
                   │ Catalog tables READ-ONLY      │
                   │ (code lookup for remap)       │
                   └───────────────────────────────┘

Locale path (separate):
  Settings language radio → DataStore → AppCompatDelegate
  .setApplicationLocales(sv|en|empty) → Activity recreate → stringResource
  Catalog mappers still bind nameSv/textSv (D-13)
```

### Recommended Project Structure

```
app/src/main/java/se/simmarken/
├── data/
│   ├── export/
│   │   ├── BackupDto.kt              # @Serializable exportVersion + kids + progress
│   │   ├── ExportJson.kt             # Json config (strict + ignoreUnknown for forward compat)
│   │   └── ExportRepository[.kt|Impl.kt]
│   ├── prefs/
│   │   └── LocalePreferencesRepository.kt
│   └── local/entity/                 # + stableId, updatedAtEpochMillis (migration)
├── domain/
│   └── export/
│       ├── BackupValidator.kt        # version + required fields + code presence
│       └── MergePlanner.kt           # preview model + newer-wins decisions
├── ui/settings/
│   ├── SettingsScreen.kt
│   ├── SettingsViewModel.kt
│   ├── ExportKidPickerDialog.kt
│   └── ImportConfirmDialog.kt
└── navigation/
    └── Routes.kt                     # + Settings object
```

### Pattern 1: Versioned Backup DTO (codes, not Room IDs)

**What:** Export progress keyed by stable catalog codes and kid `stableId`, never by auto-increment Room IDs.  
**When to use:** All export/import.  
**Example:**

```kotlin
// Source: mirror CatalogSeedParser pattern + PITFALLS.md exportVersion
@Serializable
data class BackupDto(
    val exportVersion: Int = 1,
    val exportedAtEpochMillis: Long,
    val kids: List<KidBackupDto>,
)

@Serializable
data class KidBackupDto(
    val stableId: String, // UUID — match key for merge (D-01)
    val name: String,
    val avatarColorArgb: Int,
    val createdAtEpochMillis: Long,
    val sortOrder: Int,
    val requirementProgress: List<RequirementProgressBackupDto>,
    val badgeProgress: List<BadgeProgressBackupDto>,
)

@Serializable
data class RequirementProgressBackupDto(
    val catalogCode: String,
    val badgeCode: String,
    val requirementCode: String,
    val isAchieved: Boolean,
    val achievedAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long,
)

@Serializable
data class BadgeProgressBackupDto(
    val catalogCode: String,
    val badgeCode: String,
    val isGotten: Boolean,
    val achievedAtEpochMillis: Long?,
    val gottenAtEpochMillis: Long?,
    val updatedAtEpochMillis: Long,
)
```

### Pattern 2: Parse → Preview → Confirm → Merge

**What:** Never write Room on parse. Build an immutable `ImportPreview` (updates vs new kids vs skipped unknown codes), show UI, write only after confirm.  
**When to use:** Both Settings picker and intent-filter entry (D-12). Intent entry must land on the same confirm UI — do not auto-merge.

### Pattern 3: Official per-app locales + DataStore preference mode

**What:** Store enum `SYSTEM | SV | EN` in DataStore. Map to `LocaleListCompat.getEmptyLocaleList()` / `"sv"` / `"en"` and call `AppCompatDelegate.setApplicationLocales`.  
**When to use:** Startup (`Application` or early Activity) and Settings change (D-18–D-22).  
**Example:**

```kotlin
// Source: https://developer.android.com/guide/topics/resources/app-languages
val locales = when (mode) {
    LanguageMode.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
    LanguageMode.SWEDISH -> LocaleListCompat.forLanguageTags("sv")
    LanguageMode.ENGLISH -> LocaleListCompat.forLanguageTags("en")
}
AppCompatDelegate.setApplicationLocales(locales)
```

### Anti-Patterns to Avoid

- **Exporting Room auto IDs as merge keys:** Breaks phone migration (D-01 fails on new device). Use `stableId` + catalog codes.
- **Replace-all import:** Violates D-01/D-02; never `DELETE FROM kids`.
- **Auto-import from intent without confirm:** Violates D-03.
- **Switching catalog to `nameEn`/`textEn`:** Violates D-13.
- **Compose-only locale without AppCompat:** `stringResource` and system per-app language will diverge; Activity must be `AppCompatActivity` [CITED: developer.android.com/guide/topics/resources/app-languages].
- **Including language preference in backup JSON:** Violates D-21.
- **Hand-rolling JSON with org.json / string templates:** Use kotlinx-serialization like seed parser.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JSON encode/decode | Custom string builders | kotlinx-serialization | Schema evolution, type safety; already in project |
| Secure file share URI | `file://` Uri | FileProvider + content:// | FileUriExposedException / grant flags [CITED: developer.android.com/training/secure-file-sharing/setup-sharing] |
| Locale persistence + apply | Manual `Configuration` hacks | DataStore + `AppCompatDelegate.setApplicationLocales` | Official path; recreates Activity [CITED: developer.android.com/guide/topics/resources/app-languages] |
| Document picker | Custom file browser | `ActivityResultContracts.OpenDocument` | SAF is platform-standard |
| Conflict merge timestamps | Export-only `exportedAt` for all rows | Per-row `updatedAtEpochMillis` set on every write | Uncheck clears `achievedAt`; export-time stamp cannot compare mutations |

**Key insight:** Backup format and merge identity are irreversible once parents exchange files — get `exportVersion`, stable kid IDs, and code-keyed progress right in v1.

## Common Pitfalls

### Pitfall 1: Kid ID mismatch across devices (CRITICAL)

**What goes wrong:** Export uses Room `kid.id`; import on new phone finds no matches → every kid treated as "new" or progress orphans.  
**Why it happens:** Auto-increment IDs are device-local.  
**How to avoid:** Add `KidEntity.stableId: String` (UUID at create); export/match on `stableId`; Room migration 1→2 backfilling UUIDs for existing kids.  
**Warning signs:** Merge preview always shows "new kids" after phone migration.

### Pitfall 2: Missing `updatedAt` breaks D-04

**What goes wrong:** Uncheck sets `achievedAtEpochMillis = null` (`BadgeDetailViewModel` line ~155); cannot tell which side is newer when one side is unchecked.  
**Why it happens:** Schema only stores achievement timestamps, not mutation times.  
**How to avoid:** Add `updatedAtEpochMillis` to both progress entities; set `System.currentTimeMillis()` on every toggle/purchase write; compare that field for newer-wins.  
**Warning signs:** Re-import after uncheck on one device incorrectly restores old checks.

### Pitfall 3: Exporting catalog / using numeric FK IDs in JSON

**What goes wrong:** Import fails after catalog reseeds with different Room IDs, or file balloons with seed data.  
**Why it happens:** Progress entities store `requirementId`/`badgeId` Longs.  
**How to avoid:** Remap via `catalogCode` + `badgeCode` + `requirementCode` using existing `CatalogDao` code lookups; omit catalog seed from payload (D-05).  
**Warning signs:** Import errors "unknown requirement id" after app update.

### Pitfall 4: Invalid/incompatible file UX (PITFALLS.md #4)

**What goes wrong:** Corrupt JSON or future `exportVersion` crashes or silently drops rows.  
**Why it happens:** No validation gate.  
**How to avoid:** Strict parse for known version; reject unsupported `exportVersion` with translated error; `ignoreUnknownKeys = true` only for same-major forward fields if needed; never partial-write after failed validate.  
**Warning signs:** Import succeeds with empty progress.

### Pitfall 5: AppCompat locale API with `ComponentActivity` + Material theme

**What goes wrong:** Language picker appears to do nothing on API ≤32.  
**Why it happens:** Docs require `AppCompatActivity` and AppCompat theme for backport.  
**How to avoid:** Change `MainActivity` to `AppCompatActivity`; theme parent `Theme.AppCompat.Light.NoActionBar` (keep Compose Material 3 inside `setContent`); add `locale_config` / `generateLocaleConfig`.  
**Warning signs:** Works on Android 14 emulator only.

### Pitfall 6: Intent filter auto-writes

**What goes wrong:** Sharing a JSON into the app merges without confirmation.  
**Why it happens:** Handling `ACTION_SEND` in `onCreate` writes immediately.  
**How to avoid:** Intent only opens Settings/import preview with pending Uri; same confirm gate as picker (D-03).

## Code Examples

### Share export via FileProvider

```kotlin
// Source: https://developer.android.com/training/sharing/send
// + https://developer.android.com/training/secure-file-sharing/setup-sharing
val file = File(context.cacheDir, "exports/$fileName")
file.parentFile?.mkdirs()
file.writeText(json)
val uri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.fileprovider",
    file,
)
val send = Intent(Intent.ACTION_SEND).apply {
    type = "application/json"
    putExtra(Intent.EXTRA_STREAM, uri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
context.startActivity(Intent.createChooser(send, null))
```

### Manifest receive filters (discretion on exact MIME)

```xml
<!-- Source: https://developer.android.com/training/sharing/receive -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:mimeType="application/json" />
    <data android:mimeType="text/plain" />
    <data android:scheme="content" />
    <data android:scheme="file" />
</intent-filter>
<intent-filter>
    <action android:name="android.intent.action.SEND" />
    <category android:name="android.intent.category.DEFAULT" />
    <data android:mimeType="application/json" />
    <data android:mimeType="text/*" />
</intent-filter>
```

### Merge newer-wins (domain)

```kotlin
fun pickNewer(localUpdated: Long, remoteUpdated: Long): Boolean =
    remoteUpdated > localUpdated // remote wins when strictly newer; ties keep local
```

### String resources layout

```
res/values/strings.xml          # Swedish default (chrome + progress)
res/values-en/strings.xml       # English
# Do NOT localize Simidrott / SLS tab labels (D-16) — hardcode or non-translatable strings
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Hardcoded Swedish in Composables | `stringResource` + `values` / `values-en` | Phase 6 | I18N-01/02 |
| No backup | Versioned JSON + share sheet | Phase 6 | DATA-02/03 |
| Manual Configuration locale hacks | `AppCompatDelegate.setApplicationLocales` | AndroidX AppCompat 1.6+ / API 33 LocaleManager | Immediate apply (D-20) |
| SharedPreferences for settings | DataStore Preferences | AndroidX DataStore | D-21 persistence |

**Deprecated/outdated:**
- `PreferenceManager` / raw SharedPreferences for new settings code — use DataStore [CITED: developer.android.com/topic/libraries/architecture/datastore]
- Exporting without `exportVersion` — forbidden by PITFALLS.md

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | AppCompat 1.7.1 is acceptable pin; exact patch may differ at implement time | Standard Stack | Need version bump in Gradle |
| A2 | `OpenDocument` / `GetContent` available via existing activity-compose without extra dep | Standard Stack | Add explicit activity dependency |
| A3 | Adding `stableId` + `updatedAtEpochMillis` via Room migration is acceptable within Phase 6 (not deferred) | Architecture / Pitfalls | Planner must budget migration plan; else D-01/D-04 incomplete |
| A4 | Filename `simmarken-backup-YYYY-MM-DD.json` is fine default | Discretion | Cosmetic only |
| A5 | Android SDK/emulator available for instrumented intent tests (prior phases built) | Environment | Manual device testing only |

## Open Questions (RESOLVED)

1. **Room migration scope for stableId / updatedAt** — **RESOLVED:** Lock A3 — migrate Room to v2 with `kids.stableId` + progress `updatedAtEpochMillis`; backfill UUIDs and set `updatedAt` to `coalesce(achievedAt, gottenAt, createdAt, now)`.
   - What we knew: D-01 needs cross-device kid match; D-04 needs mutation timestamps; DB is currently version 1.
   - What was unclear: Whether discuss-phase expected schema change vs export-only UUID (UUID-only-in-JSON cannot update existing local kids' identity on first export round-trip cleanly).

2. **Unknown catalog codes in import** — **RESOLVED:** Fail validation only for schema/version errors; **skip unknown codes** with count in preview ("3 rows skipped — unknown badges") so phone migration still restores known progress.
   - What we knew: Codes may not exist if seed diverged.
   - What was unclear: Skip-with-warning vs fail entire file.

3. **Kid profile field merge (name/color)** — **RESOLVED:** On confirm for existing kid, **keep local profile** (name/color/sortOrder); only merge progress. New kids copy profile from file after per-kid confirm (D-02).
   - What we knew: D-04 text focuses on progress conflicts.
   - What was unclear: If same `stableId` has different name locally vs file.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | Gradle build | ✓ (project configured) | 17 | — |
| Android SDK / compileSdk 36 | Build + emulator | ✓ (prior phases) | 36 | — |
| kotlinx-serialization | Export JSON | ✓ | 1.7.3 | — |
| DataStore Preferences | Locale pref | ✗ (not in Gradle yet) | — | Add dep 1.2.1 |
| AppCompat | Locales API | ✗ (not in Gradle yet) | — | Add dep 1.7.x |
| FileProvider | Share sheet | ✓ (via core-ktx) | core-ktx 1.15.0 | Manifest + file_paths.xml still needed |
| gsd-tools package-legitimacy | Audit seam | ✗ | — | Official AndroidX docs used |

**Missing dependencies with no fallback:** none blocking research — DataStore/AppCompat are planned installs.

**Missing dependencies with fallback:** gsd-tools legitimacy seam → manual AndroidX verification.

**Step 2.6 note:** Full CLI probes were restricted in this agent environment; availability inferred from successful Phase 1–5 builds and current Gradle files [VERIFIED: app/build.gradle.kts, libs.versions.toml].

## Validation Architecture

> `workflow.nyquist_validation` is **true** in `.planning/config.json` — include tests for all DATA/I18N requirements.

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4.13.2 + kotlinx-coroutines-test 1.9.0 (JVM); AndroidJUnit4 + Room testing (instrumented) |
| Config file | none dedicated — Gradle `test` / `androidTest` source sets |
| Quick run command | `./gradlew :app:testDebugUnitTest --tests 'se.simmarken.data.export.*'` |
| Full suite command | `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest` |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| DATA-02 | Export encodes kids+progress with `exportVersion: 1` and codes | unit | `./gradlew :app:testDebugUnitTest --tests '*ExportRepository*'` | ❌ Wave 0 |
| DATA-02 | One-kid path skips picker (logic) | unit | ViewModel test with 1 vs 2 kids | ❌ Wave 0 |
| DATA-03 | Invalid JSON / bad version → error, no write | unit | `BackupValidatorTest` | ❌ Wave 0 |
| DATA-03 | Merge newer-wins + skip unknown codes | unit | `MergePlannerTest` | ❌ Wave 0 |
| DATA-03 | New kids require explicit accept list | unit | `MergePlannerTest` | ❌ Wave 0 |
| DATA-03 | Round-trip encode→decode→merge preserves progress | unit | `ExportRoundTripTest` | ❌ Wave 0 |
| I18N-01/02 | Required string keys exist in `values` and `values-en` | unit/smoke | Resource lint or JVM assert both files contain keys | ❌ Wave 0 |
| I18N-03 | DataStore mode maps to locale tags / empty list | unit | `LocalePreferencesRepositoryTest` | ❌ Wave 0 |
| DATA-03 | Intent/SAF confirm gate (no auto-write) | manual / optional instrumented | Device: share JSON into app → confirm dialog | manual-only OK |

### Sampling Rate

- **Per task commit:** `./gradlew :app:testDebugUnitTest --tests 'se.simmarken.data.export.*' --tests 'se.simmarken.domain.export.*'`
- **Per wave merge:** `./gradlew :app:testDebugUnitTest`
- **Phase gate:** Full unit suite green + manual UAT of share sheet, import confirm, language switch before `/gsd-verify-work`

### Wave 0 Gaps

- [ ] `app/src/test/java/se/simmarken/data/export/ExportRoundTripTest.kt` — covers DATA-02/03
- [ ] `app/src/test/java/se/simmarken/domain/export/BackupValidatorTest.kt` — covers DATA-03 invalid/incompatible
- [ ] `app/src/test/java/se/simmarken/domain/export/MergePlannerTest.kt` — covers D-01–D-04
- [ ] `app/src/test/java/se/simmarken/data/prefs/LocalePreferencesMappingTest.kt` — covers I18N-03
- [ ] Fake `KidRepository` / `ProgressRepository` / `CatalogRepository` extensions for bulk get/upsert (follow `BadgeDetailViewModelProgressTest` fake style — no Mockito)
- [ ] Room migration test for v1→v2 `stableId` / `updatedAt` backfill (JVM Room migration test or instrumented)

## Security Domain

> `security_enforcement` enabled (ASVS level 1) in `.planning/config.json`.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | Offline single-user app |
| V3 Session Management | no | — |
| V4 Access Control | no (single local user) | Confirm dialogs are UX gates, not auth |
| V5 Input Validation | **yes** | kotlinx-serialization typed decode + `BackupValidator` (version, required fields, code shape) |
| V6 Cryptography | no | No secrets in backup; no encryption required for v1 offline JSON |

### Known Threat Patterns for Android offline backup

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Malicious/corrupt JSON crash or partial write | Tampering / Denial of Service | Validate before write; transactional merge; clear user-facing error (D-03, success criterion 3) |
| Path traversal via shared file name | Elevation | Write exports only under app cache controlled path; read import via ContentResolver Uri, never concatenate untrusted paths |
| Over-broad FileProvider paths | Information Disclosure | `file_paths.xml` limited to `cache/exports/`; `exported=false`; temporary URI grants only |
| Intent spam auto-import | Tampering | Never merge without explicit confirm (D-03) |
| Backup includes PII (child names) | Information Disclosure | Expected for migration; no network upload; document in UX copy that file contains child names |

## Project Constraints (from .cursor/rules/)

No project-local `.cursor/rules/` or `.agents/skills/` found in this repo. Follow global user rules and planning research docs (ARCHITECTURE, STACK, PITFALLS). Org knowledge registry (`~/.agents/knowledge/registry.json`) absent — no `[ref:]` knowledge loaded; Android guidance from official docs + repo research [via model].

## Existing Codebase Baseline (verified)

| Asset | Status |
|-------|--------|
| `KidEntity` / progress entities | Present; **no** `stableId` / `updatedAt` |
| `ProgressRepository` | Per-kid Flow + upsert; **no** bulk getAll / merge APIs |
| `ExportRepository` / `data/export/` | **Absent** |
| `ui/settings/` | **Absent** |
| Routes | `Home`, `ChildCatalog`, `BadgeDetail` only |
| Home top bar | Title only — **no gear** |
| `strings.xml` | Only `app_name` |
| DataStore / AppCompat | **Not** in Gradle |
| Manifest | LAUNCHER only; `allowBackup=false` (intentional — export is backup path) |
| Serialization pattern | `CatalogSeedParser.strictJson` — copy for backup |
| Hardcoded Swedish | Home, ChildCard, dialogs, BadgeDetail, form validation, a11y state labels |

## Discretion Recommendations (for planner)

| Discretion item | Recommendation | Confidence |
|-----------------|----------------|------------|
| Timestamp for D-04 | Add `updatedAtEpochMillis` on both progress entities; set on every write | HIGH |
| Locale apply | `AppCompatDelegate` + `AppCompatActivity` (not CompositionLocal-only) | HIGH |
| Export filename | `simmarken-backup-yyyy-MM-dd.json` | MEDIUM |
| New-kid confirm UX | Single summary dialog with checklist (select which new kids to create) — fewer taps at pool | HIGH |
| Kid picker | Checkboxes; default **All** selected | MEDIUM |
| Intent MIME | `application/json` + `text/*` + pathPattern `.*\\.json` if needed | MEDIUM |

## Sources

### Primary (HIGH confidence)

- Repo: `app/src/main/java/se/simmarken/**` — entities, repos, ViewModels, NavHost, Gradle
- `.planning/phases/06-export-import-i18n/06-CONTEXT.md` — locked decisions
- `.planning/research/{ARCHITECTURE,STACK,PITFALLS,FEATURES}.md`
- [Android per-app languages](https://developer.android.com/guide/topics/resources/app-languages) — `setApplicationLocales`, AppCompatActivity
- [DataStore releases](https://developer.android.com/jetpack/androidx/releases/datastore) — Preferences 1.2.1
- [Secure file sharing / FileProvider](https://developer.android.com/training/secure-file-sharing/setup-sharing)
- [Send / receive simple data](https://developer.android.com/training/sharing/send) / [receive](https://developer.android.com/training/sharing/receive)

### Secondary (MEDIUM confidence)

- AppCompatDelegate API reference — empty locale list = follow system
- Community Compose locale notes (Activity must be AppCompatActivity) — cross-checked with official docs

### Tertiary (LOW confidence)

- Exact AppCompat patch version at implement time — re-verify on Google Maven

## Metadata

**Confidence breakdown:**
- Standard stack: **HIGH** — existing Gradle + official AndroidX docs for new deps
- Architecture: **HIGH** — codebase inventory + locked CONTEXT; stableId/updatedAt called out as required schema work
- Pitfalls: **HIGH** — verified against live entity/ViewModel write paths + PITFALLS.md

**Research date:** 2026-07-24  
**Valid until:** 2026-08-24 (AndroidX patch versions move; backup schema decisions remain stable)

**Org knowledge:** No installed knowledge registry entries for Android/i18n/backup domains.

**Graph:** `.planning/graphs/graph.json` not present — no graphify context.
