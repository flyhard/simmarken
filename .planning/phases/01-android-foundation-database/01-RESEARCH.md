# Phase 1: Android Foundation & Database - Research

**Researched:** 2026-07-22
**Domain:** Android greenfield setup — Kotlin, Jetpack Compose, Room 2.8 + KSP, MVVM walking skeleton
**Confidence:** HIGH

## Summary

Phase 1 establishes a greenfield Android project at `se.simmarken` with the full relational Room schema for catalog hierarchy (Catalog → Category → Badge → Requirement), Kid profiles, and two progress tables (RequirementProgress, BadgeProgress). No catalog seeding, CRUD UI, or progress toggling happens in this phase — only buildable app shell, schema, DAOs, repository interfaces, and persistence verification.

The locked decisions from CONTEXT.md (D-01 through D-05) map cleanly to Room: composite primary keys on progress tables, `achievedAt`/`gottenAt` as nullable epoch-millis timestamps (never a visual-state enum column), and foreign keys with `CASCADE` on catalog hierarchy and kid deletion. Badge visual state remains out of the database entirely.

**Version posture:** STACK.md specifies Room 2.6.1+ and Kotlin 2.0+. Official Android docs list Room **2.8.4** (stable, Nov 2025) as current for the `androidx.room` artifact line. Room **3.0.0** (Jul 2026) exists under a new `androidx.room3` package with breaking API changes — defer to a future milestone; use **2.8.4** for this walking skeleton. [CITED: developer.android.com/jetpack/androidx/releases/room]

**Primary recommendation:** Scaffold with Android Studio / AGP 8.7.2, Kotlin 2.1.21, KSP 2.1.21-2.0.2, Room 2.8.4 + Room Gradle Plugin (schema export from day 1), Compose BOM 2025.12.01, Navigation Compose 2.9.8 with type-safe routes, manual DI via `AppContainer`, and instrumented tests using a **disk-backed** Room database to prove restart persistence.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Progress Data Model
- **D-01:** Use two progress tables — `RequirementProgress` (per kid + requirement) and `BadgeProgress` (per kid + badge). Skill completion and physical pin purchase are separate concerns.
- **D-02:** `BadgeProgress` stores `isGotten` (boolean), `achievedAt` (timestamp), and `gottenAt` (timestamp). Do not store derived visual state enums (LOCKED / IN_PROGRESS / ACHIEVED_TO_BUY / GOTTEN).
- **D-03:** Write `achievedAt` on `BadgeProgress` when the last requirement is checked — set once at achievement moment, never auto-cleared if requirements are later unchecked.
- **D-04:** `RequirementProgress` stores `isAchieved` (boolean) and `achievedAt` (timestamp). Support bidirectional toggle (check and uncheck) from day one.
- **D-05:** Derive badge visual state at read time from requirement progress + `isGotten`. Centralize computation in domain layer (BadgeStateCalculator in later phases).

### Claude's Discretion
- App shell at launch (placeholder vs minimal nav scaffold) — follow ARCHITECTURE.md recommended structure
- Migration posture, min SDK, DI approach, catalog i18n column design — follow STACK.md and ARCHITECTURE.md defaults unless research flags a conflict
- Package name `se.simmarken` per architecture research unless build tooling requires adjustment

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope. Unselected gray areas (catalog i18n in DB, migration posture, app shell) deferred to research defaults and planner discretion.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| DATA-01 | All data persists locally via Room with no network dependency | Room 2.8.4 on-device SQLite; no network libraries; disk-backed `AppDatabase`; instrumented test closes/reopens DB to verify persistence across simulated restart |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| SQLite schema & migrations | Database / Storage (Room) | — | Room owns entity definitions, FK constraints, schema JSON export |
| DAO queries & Flow emissions | Database / Storage (Room) | — | `@Dao` is the persistence API; repositories wrap DAOs |
| Repository orchestration | API / Backend (domain layer) | — | Business rules (e.g., D-03 write-once `achievedAt`) live in repository impl, not Composables |
| App shell & navigation scaffold | Browser / Client (Compose) | — | `MainActivity`, `NavHost`, placeholder screens only in Phase 1 |
| Badge visual state | Domain (later phase) | Client (Compose renders) | D-05: never stored in DB; `BadgeStateCalculator` deferred to Phase 4 |
| Catalog seed data | Database / Storage | — | Phase 2; tables exist empty in Phase 1 |
| Network / sync | — | — | Out of scope; no tier owns this |

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin | 2.1.21 | Language | Official Android language; KGP 2.1.21 ↔ AGP 8.7.2 fully supported [CITED: kotlinlang.org/docs/releases.html] |
| Android Gradle Plugin | 8.7.2 | Build | Meets compileSdk 35 minimum (8.6.0+); max fully supported AGP for Kotlin 2.1.21 [CITED: kotlinlang.org/docs/gradle-configure-project.html] |
| Gradle | 8.9 | Build wrapper | AGP 8.7.x requires Gradle 8.9+ [CITED: developer.android.com/build/releases/about-agp] |
| KSP | 2.1.21-2.0.2 | Annotation processing | Required for Room; version must match Kotlin compiler [CITED: github.com/google/ksp/releases] |
| Room (`room-runtime`, `room-ktx`, `room-compiler`) | 2.8.4 | Local SQLite ORM | Latest stable `androidx.room` line; Flow + coroutines; Kotlin codegen default since 2.7 [CITED: developer.android.com/jetpack/androidx/releases/room] |
| Room Gradle Plugin (`androidx.room`) | 2.8.4 | Schema export | `room { schemaDirectory(...) }` from 2.6.0+; reproducible schema JSON for migrations [CITED: developer.android.com/jetpack/androidx/releases/room] |
| Jetpack Compose BOM | 2025.12.01 | UI toolkit | STACK.md 2024.12+; monthly BOM pins compatible Compose artifacts [CITED: developer.android.com/develop/ui/compose/bom] |
| Material 3 | (via Compose BOM) | Design system | Project design direction |
| Navigation Compose | 2.9.8 | Type-safe nav scaffold | Stable type-safe routes since 2.8.0 [CITED: developer.android.com/jetpack/androidx/releases/navigation] |
| Lifecycle ViewModel Compose | 2.8.7 | ViewModel + Compose | `viewModel()` composable for later phases [ASSUMED] |
| kotlinx-coroutines | 1.9.0 | Async / Flow | Room `suspend` + `Flow` returns [ASSUMED] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| kotlinx-serialization-json | 1.7.3 | Serialization | Required plugin dep for Navigation type-safe routes; export/import in Phase 6 |
| androidx.test / Espresso / JUnit4 | (BOM-managed) | Instrumented tests | DAO + persistence verification |
| `room-testing` | 2.8.4 | Migration test helpers | Phase 1 scaffold; full migration tests when schema bumps |
| Hilt | — | DI | **Defer** — manual `AppContainer` sufficient for walking skeleton per STACK.md |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Room 2.8.4 | Room 3.0.0 (`androidx.room3`) | Room 3.0 is greenfield-friendly but new package, coroutine-only APIs, Jul 2026 release — higher integration risk for MVP; revisit post-v1 |
| Room 2.8.4 | Room 2.6.1 (STACK.md floor) | 2.8.4 is backward-compatible superset; better Kotlin 2.0/KSP2 support |
| Manual DI | Hilt 2.52+ | Hilt adds boilerplate for single-module app; add when ViewModel count grows |
| AGP 8.7.2 | AGP 8.8.2 | 8.8.2 works with compileSdk 35 but sits outside KGP "fully supported" range for Kotlin 2.1.21 |

**Installation (representative `gradle/libs.versions.toml`):**

```toml
[versions]
agp = "8.7.2"
kotlin = "2.1.21"
ksp = "2.1.21-2.0.2"
room = "2.8.4"
composeBom = "2025.12.01"
navigation = "2.9.8"
serialization = "1.7.3"
lifecycle = "2.8.7"
coroutines = "1.9.0"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```

**Build verification commands:**

```bash
# From project root after scaffold
./gradlew :app:assembleDebug          # CI-friendly compile + package
./gradlew :app:testDebugUnitTest      # JVM unit tests (domain helpers)
./gradlew :app:connectedDebugAndroidTest  # Instrumented DAO + persistence (requires emulator/device)
./gradlew :app:lintDebug              # Static analysis gate
```

## Package Legitimacy Audit

> Android dependencies resolve from Google Maven (`maven.google.com`), not npm. `slopcheck` was unavailable at research time and targets npm/PyPI — all coordinates below verified against official Android/Kotlin release pages only.

| Package (Maven coordinate) | Registry | Source Repo | slopcheck | Disposition |
|----------------------------|----------|-------------|-----------|-------------|
| `androidx.room:room-runtime:2.8.4` | Google Maven | android/androidx (GitHub) | N/A | Approved [CITED: developer.android.com] |
| `androidx.room:room-compiler:2.8.4` | Google Maven | android/androidx | N/A | Approved [CITED] |
| `androidx.compose:compose-bom:2025.12.01` | Google Maven | android/androidx | N/A | Approved [CITED] |
| `androidx.navigation:navigation-compose:2.9.8` | Google Maven | android/androidx | N/A | Approved [CITED] |
| `org.jetbrains.kotlin:kotlin-stdlib:2.1.21` | Maven Central | JetBrains/kotlin | N/A | Approved [CITED] |

**Packages removed due to slopcheck [SLOP] verdict:** none (slopcheck not run)
**Packages flagged as suspicious [SUS]:** none

*All version pins should be confirmed at implementation time against current release notes — monthly Compose BOM and patch Room releases move frequently.*

## Project Constraints (from .cursor/rules/)

- **Platform:** Native Android only — Kotlin + Jetpack Compose + Room + MVVM
- **Network:** None — 100% offline; do not add Retrofit, WorkManager sync, or network permissions
- **Storage:** Local device only; Room is single source of truth
- **i18n:** Swedish + English — catalog text columns use `_sv` / `_en` suffixes in DB (UI strings via `strings.xml` in later phases)
- **GSD workflow:** Phase work should flow through `/gsd-execute-phase`; avoid ad-hoc repo edits outside GSD commands
- **Do not use:** kapt for Room, LiveData for new code, SharedPreferences for structured data, derived badge state enums in DB

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Compose App Shell (Phase 1)               │
│  MainActivity → SimmarkenTheme → NavHost(Home placeholder)  │
└────────────────────────────┬────────────────────────────────┘
                             │ observes (later phases)
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              ViewModels (skeleton only in Phase 1)           │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│         Repository interfaces + stub/default impls           │
│   CatalogRepository │ KidRepository │ ProgressRepository    │
└────────────────────────────┬────────────────────────────────┘
                             │ suspend / Flow
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                    Room DAOs (Flow + suspend)                │
│  CatalogDao │ KidDao │ RequirementProgressDao │ BadgeProgressDao │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              AppDatabase (SQLite file on disk)               │
│  schemas/1.json exported │ version=1 │ FK CASCADE          │
└─────────────────────────────────────────────────────────────┘

Manual DI: SimmarkenApplication → AppContainer → repositories → DAOs
```

### Recommended Project Structure

```
simmarken/
├── gradle/
│   └── libs.versions.toml
├── app/
│   ├── build.gradle.kts
│   ├── schemas/                          # Room exported JSON (commit to VCS)
│   │   └── se.simmarken.data.local.AppDatabase/
│   │       └── 1.json
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/se/simmarken/
│       │       ├── SimmarkenApplication.kt
│       │       ├── MainActivity.kt
│       │       ├── di/
│       │       │   └── AppContainer.kt
│       │       ├── data/
│       │       │   └── local/
│       │       │       ├── AppDatabase.kt
│       │       │       ├── Converters.kt
│       │       │       ├── entity/
│       │       │       │   ├── CatalogEntity.kt
│       │       │       │   ├── CategoryEntity.kt
│       │       │       │   ├── BadgeEntity.kt
│       │       │       │   ├── RequirementEntity.kt
│       │       │       │   ├── KidEntity.kt
│       │       │       │   ├── RequirementProgressEntity.kt
│       │       │       │   └── BadgeProgressEntity.kt
│       │       │       └── dao/
│       │       │           ├── CatalogDao.kt
│       │       │           ├── KidDao.kt
│       │       │           ├── RequirementProgressDao.kt
│       │       │           └── BadgeProgressDao.kt
│       │       ├── domain/
│       │       │   └── repository/
│       │       │       ├── CatalogRepository.kt
│       │       │       ├── KidRepository.kt
│       │       │       └── ProgressRepository.kt
│       │       ├── navigation/
│       │       │   ├── Routes.kt
│       │       │   └── SimmarkenNavHost.kt
│       │       └── ui/
│       │           ├── theme/
│       │           │   ├── Color.kt
│       │           │   ├── Theme.kt
│       │           │   └── Type.kt
│       │           └── home/
│       │               └── HomePlaceholderScreen.kt
│       ├── androidTest/java/se/simmarken/
│       │   └── data/local/
│       │       ├── DaoInstrumentedTest.kt
│       │       └── DatabasePersistenceTest.kt
│       └── test/java/se/simmarken/
│           └── PlaceholderUnitTest.kt
├── build.gradle.kts
└── settings.gradle.kts
```

### Entity Field Definitions

All catalog text fields use bilingual columns (`nameSv`/`nameEn`, `textSv`/`textEn`) to support I18N-01/02 without a separate translation table in v1. [ASSUMED: user accepts column-per-locale over normalized i18n table — confirm if seed JSON structure differs.]

#### `CatalogEntity` — table `catalogs`

| Column | Kotlin type | Constraints |
|--------|-------------|-------------|
| `id` | `Long` | `@PrimaryKey(autoGenerate = true)` |
| `code` | `String` | Unique index; e.g. `"simidrott"`, `"sls"` |
| `nameSv` | `String` | NOT NULL |
| `nameEn` | `String` | NOT NULL |
| `catalogVersion` | `String` | NOT NULL; seed metadata for Phase 2 |
| `sortOrder` | `Int` | NOT NULL, default 0 |

#### `CategoryEntity` — table `categories`

| Column | Kotlin type | Constraints |
|--------|-------------|-------------|
| `id` | `Long` | PK autoGenerate |
| `catalogId` | `Long` | FK → `CatalogEntity.id`, `onDelete = CASCADE`, index |
| `code` | `String` | Unique per catalog: index `(catalogId, code)` |
| `nameSv` / `nameEn` | `String` | NOT NULL |
| `sortOrder` | `Int` | NOT NULL |

#### `BadgeEntity` — table `badges`

| Column | Kotlin type | Constraints |
|--------|-------------|-------------|
| `id` | `Long` | PK autoGenerate |
| `categoryId` | `Long` | FK → `CategoryEntity.id`, CASCADE, index |
| `code` | `String` | Unique per category: index `(categoryId, code)` |
| `nameSv` / `nameEn` | `String` | NOT NULL |
| `imageAssetPath` | `String?` | Nullable; populated Phase 2 |
| `sortOrder` | `Int` | NOT NULL |

#### `RequirementEntity` — table `requirements`

| Column | Kotlin type | Constraints |
|--------|-------------|-------------|
| `id` | `Long` | PK autoGenerate |
| `badgeId` | `Long` | FK → `BadgeEntity.id`, CASCADE, index |
| `code` | `String` | Unique per badge: index `(badgeId, code)` |
| `textSv` / `textEn` | `String` | NOT NULL |
| `sortOrder` | `Int` | NOT NULL |

#### `KidEntity` — table `kids`

| Column | Kotlin type | Constraints |
|--------|-------------|-------------|
| `id` | `Long` | PK autoGenerate |
| `name` | `String` | NOT NULL |
| `avatarColorArgb` | `Int` | NOT NULL; hex color for card theme (Phase 3) |
| `createdAtEpochMillis` | `Long` | NOT NULL; `System.currentTimeMillis()` at insert |
| `sortOrder` | `Int` | NOT NULL, default 0 |

#### `RequirementProgressEntity` — table `requirement_progress` (D-01, D-04)

| Column | Kotlin type | Constraints |
|--------|-------------|-------------|
| `kidId` | `Long` | Composite PK; FK → `KidEntity.id`, CASCADE, index |
| `requirementId` | `Long` | Composite PK; FK → `RequirementEntity.id`, CASCADE, index |
| `isAchieved` | `Boolean` | NOT NULL, default false |
| `achievedAtEpochMillis` | `Long?` | Nullable; set on check, cleared on uncheck |

```kotlin
@Entity(
    tableName = "requirement_progress",
    primaryKeys = ["kidId", "requirementId"],
    foreignKeys = [
        ForeignKey(
            entity = KidEntity::class,
            parentColumns = ["id"],
            childColumns = ["kidId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RequirementEntity::class,
            parentColumns = ["id"],
            childColumns = ["requirementId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("kidId"),
        Index("requirementId"),
    ],
)
data class RequirementProgressEntity(
    val kidId: Long,
    val requirementId: Long,
    val isAchieved: Boolean,
    val achievedAtEpochMillis: Long?,
)
```

#### `BadgeProgressEntity` — table `badge_progress` (D-01, D-02, D-03)

| Column | Kotlin type | Constraints |
|--------|-------------|-------------|
| `kidId` | `Long` | Composite PK; FK → `KidEntity.id`, CASCADE |
| `badgeId` | `Long` | Composite PK; FK → `BadgeEntity.id`, CASCADE |
| `isGotten` | `Boolean` | NOT NULL, default false (D-02) |
| `achievedAtEpochMillis` | `Long?` | Write-once when all requirements first achieved (D-03); never auto-cleared |
| `gottenAtEpochMillis` | `Long?` | Set when `isGotten` toggled true (D-02) |

**No** `visualState`, `status`, or enum column — D-02/D-05.

### Pattern 1: Room Gradle Plugin + Schema Export

**What:** Export `schemas/<flavor><variant>/.../N.json` on every compile for migration testing.
**When to use:** Always from day 1 — ROADMAP success criterion #2 depends on stable schema.

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.room)
}

android {
    namespace = "se.simmarken"
    compileSdk = 35
    defaultConfig {
        applicationId = "se.simmarken"
        minSdk = 26
        targetSdk = 35
    }
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

@Database(
    entities = [/* all 7 entities */],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase()
```

[CITED: developer.android.com/jetpack/androidx/releases/room — Room Gradle Plugin section]

### Pattern 2: DAO with Flow for Reactive Reads

**What:** Read paths return `Flow<List<T>>` for Compose collection in later phases.
**When to use:** All observe queries (kids, catalog tree, progress by kid).

```kotlin
@Dao
interface KidDao {
    @Query("SELECT * FROM kids ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<KidEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(kid: KidEntity): Long
}
```

[CITED: developer.android.com/training/data-storage/room]

### Pattern 3: Disk-Backed Database Singleton (Manual DI)

**What:** `AppContainer` builds one `Room.databaseBuilder` instance per process.
**When to use:** Production and instrumented persistence tests (not in-memory).

```kotlin
// Source: developer.android.com/training/data-storage/room
class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "simmarken.db",
    ).build()

    val kidRepository: KidRepository = KidRepositoryImpl(database.kidDao())
    // ...
}
```

### Pattern 4: Type-Safe Navigation Scaffold

**What:** `@Serializable object Home` route with empty placeholder composable.
**When to use:** Phase 1 app shell; extended in Phase 3+.

```kotlin
@Serializable object Home

@Composable
fun SimmarkenNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Home) {
        composable<Home> { HomePlaceholderScreen() }
    }
}
```

[CITED: developer.android.com/guide/navigation/design/type-safety]

### Pattern 5: Migration Scaffold (v1 — no migration yet)

**What:** Version 1 database with exported schema; placeholder migration test class for future bumps.
**When to use:** Day 1 setup; first real migration at first schema change post-v1.

```kotlin
// Future: when version → 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ALTER TABLE ...
    }
}

// Phase 1 builder — no migrations array needed at v1
Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME).build()
```

[CITED: developer.android.com/training/data-storage/room/migrating-db-versions]

### Anti-Patterns to Avoid

- **Storing `BadgeVisualState` enum in DB:** Violates D-02; causes stale state (see PITFALLS.md #3)
- **`Room.inMemoryDatabaseBuilder` for persistence verification:** Data dies with process — use disk builder for restart test [CITED: developer.android.com/training/data-storage/room/testing-db]
- **DAO calls from Composables:** ANR risk; always go through ViewModel → Repository (Phase 3+)
- **`fallbackToDestructiveMigration()` in production:** Acceptable only in debug builds; never in release — schema export exists precisely to avoid this
- **kapt for Room:** Deprecated path; use KSP [CITED: developer.android.com/build/migrate-to-ksp]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| SQLite schema management | Raw SQLiteOpenHelper | Room 2.8.4 + exported schemas | FK validation, compile-time query checks, migration helpers |
| Reactive DB reads | Polling / manual cursor | Room `Flow` return types | Lifecycle-aware, integrates with Compose `collectAsStateWithLifecycle` |
| Annotation processing | kapt | KSP 2.1.21-2.0.2 | 2× faster builds; Room Kotlin codegen requires KSP |
| Navigation route strings | String routes `"home"` | Type-safe `@Serializable` routes | Compile-time safety since Navigation 2.8 [CITED] |
| Timestamp storage | String dates in DB | `Long` epoch millis + optional `TypeConverter` for `Instant` | SQLite INTEGER indexing; unambiguous timezone handling [CITED: developer.android.com/training/data-storage/room/referencing-data] |
| DI framework (Phase 1) | Custom service locator graph | Simple `AppContainer` data class | STACK.md defers Hilt until complexity warrants it |

**Key insight:** Room + exported schema is the entire persistence story for this offline app. Custom SQL layers or preference-store hybrids create sync bugs with zero benefit.

## Common Pitfalls

### Pitfall 1: Missing FK Child Indices

**What goes wrong:** Room emits `MISSING_INDEX_ON_FOREIGN_KEY_CHILD` warnings; slow deletes on catalog cascade.
**Why it happens:** FK declared without `@Index` on child column.
**How to avoid:** Add `indices = [Index("catalogId")]` on every child entity per official ForeignKey docs [CITED: developer.android.com/reference/androidx/room/ForeignKey].
**Warning signs:** Build warnings during KSP; slow instrumented tests on cascade delete.

### Pitfall 2: Testing Persistence with In-Memory DB

**What goes wrong:** Phase 1 "persists across restart" criterion appears met but production uses disk.
**Why it happens:** `inMemoryDatabaseBuilder` is the default Room testing example.
**How to avoid:** Dedicated `DatabasePersistenceTest` uses named disk DB, `db.close()`, new builder, re-query [CITED: developer.android.com/training/data-storage/room/testing-db].
**Warning signs:** Test passes but manual emulator restart shows empty data.

### Pitfall 3: KSP/Kotlin Version Mismatch

**What goes wrong:** Cryptic KSP compile errors, Room processor fails.
**Why it happens:** KSP version doesn't match Kotlin patch version.
**How to avoid:** Pin `ksp = "2.1.21-2.0.2"` whenever `kotlin = "2.1.21"` [CITED: github.com/google/ksp].
**Warning signs:** `:app:kspDebugKotlin` task failure on clean build.

### Pitfall 4: Accidentally Caching Derived Badge State

**What goes wrong:** `BadgeProgress` gets a `state` column; UI shows wrong tier after uncheck.
**Why it happens:** Temptation to denormalize for query speed.
**How to avoid:** Only `isGotten`, `achievedAtEpochMillis`, `gottenAtEpochMillis` on `badge_progress`; visual state in `BadgeStateCalculator` (Phase 4) [per D-02, D-05, PITFALLS.md].
**Warning signs:** Any enum column in entity definitions.

### Pitfall 5: Clearing `BadgeProgress.achievedAt` on Uncheck

**What goes wrong:** Achievement history lost; contradicts parent requirement.
**Why it happens:** Symmetric toggle logic copied from `RequirementProgress`.
**How to avoid:** Repository write-once semantics for `achievedAtEpochMillis` — set when transitioning to all-requirements-met, never clear (D-03). Phase 5 implements logic; schema must allow nullable non-clearing column from day 1.
**Warning signs:** `UPDATE badge_progress SET achievedAt = NULL` in any DAO.

### Pitfall 6: Schema JSON Not Committed

**What goes wrong:** Future migrations untestable; CI can't validate schema drift.
**Why it happens:** `schemas/` gitignored by habit.
**How to avoid:** Commit `app/schemas/**`; add to PR review checklist.
**Warning signs:** Empty `schemas/` after first build.

## Code Examples

### AppDatabase Registration

```kotlin
// Source: developer.android.com/jetpack/androidx/releases/room
@Database(
    entities = [
        CatalogEntity::class,
        CategoryEntity::class,
        BadgeEntity::class,
        RequirementEntity::class,
        KidEntity::class,
        RequirementProgressEntity::class,
        BadgeProgressEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun kidDao(): KidDao
    abstract fun requirementProgressDao(): RequirementProgressDao
    abstract fun badgeProgressDao(): BadgeProgressDao

    companion object {
        const val DB_NAME = "simmarken.db"
    }
}
```

### Epoch Millis Converters (optional `Instant` in domain layer)

```kotlin
// Source: developer.android.com/training/data-storage/room/referencing-data
class Converters {
    @TypeConverter
    fun fromEpochMillis(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toEpochMillis(instant: Instant?): Long? =
        instant?.toEpochMilli()
}
```

### Disk Persistence Instrumented Test

```kotlin
// Source: developer.android.com/training/data-storage/room/testing-db (adapted for disk)
@RunWith(AndroidJUnit4::class)
class DatabasePersistenceTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DB_NAME)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(AppDatabase.DB_NAME)
    }

    @Test
    fun kidSurvivesCloseAndReopen() = runBlocking {
        val db1 = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME).build()
        val id = db1.kidDao().upsert(
            KidEntity(name = "Ella", avatarColorArgb = 0xFF2196F3.toInt(), createdAtEpochMillis = 1L, sortOrder = 0)
        )
        db1.close()

        val db2 = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME).build()
        val kids = db2.kidDao().observeAll().first()
        db2.close()

        assertEquals(1, kids.size)
        assertEquals(id, kids[0].id)
        assertEquals("Ella", kids[0].name)
    }
}
```

### app/build.gradle.kts KSP + Room

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.androidx.room.testing)
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| kapt for Room | KSP | Room 2.6+ | Required for Kotlin codegen; faster builds |
| String navigation routes | Type-safe `@Serializable` routes | Navigation 2.8.0 (2024) | Compile-time route safety |
| Java Room codegen | Kotlin codegen default | Room 2.7.0 | `room.generateKotlin = true` default |
| Room 2.x artifact | Room 3.0 (`androidx.room3`) | Jul 2026 | New package, KSP-only, coroutine APIs — not adopted in Phase 1 |
| LiveData in Room | Kotlin Flow | Compose era | Use `Flow` + `collectAsStateWithLifecycle` |

**Deprecated/outdated:**
- **kapt:** Maintenance mode; migrate to KSP [CITED: developer.android.com/build/migrate-to-ksp]
- **Room 2.6.1 floor from STACK.md:** Still valid minimum; 2.8.4 recommended for greenfield

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Bilingual columns (`nameSv`/`nameEn`) for catalog entities | Entity definitions | Phase 2 seed JSON shape mismatch — may need migration |
| A2 | `minSdk = 26` (Android 8.0) | Standard Stack | Excludes <0.5% devices; user may want 24 |
| A3 | Manual `AppContainer` DI (no Hilt) | Architecture | Refactor cost if team prefers Hilt early |
| A4 | Epoch millis (`Long`) for timestamps instead of `Instant` in entities | Entity definitions | Domain layer needs converter; simpler SQLite storage |
| A5 | Room 2.8.4 over Room 3.0 | Standard Stack | Miss newest APIs; avoids Jul 2026 breaking migration |
| A6 | `lifecycle-viewmodel-compose:2.8.7` and `coroutines:1.9.0` | Standard Stack | Patch version drift — verify at scaffold time |

## Open Questions

1. **Room 2.8.4 vs 3.0 for greenfield**
   - What we know: Room 3.0 stable Jul 2026 with new `androidx.room3` package [CITED: developer.android.com/jetpack/androidx/releases/room3]
   - What's unclear: Team appetite for bleeding-edge vs proven 2.8 line
   - Recommendation: Stay on 2.8.4 for Phase 1; revisit before Phase 2 if 3.x ecosystem matures

2. **Catalog i18n column naming**
   - What we know: I18N requirements need SV + EN text; seed source is Swedish-primary
   - What's unclear: Whether Phase 2 JSON uses nested `i18n` objects vs flat columns
   - Recommendation: Flat `_sv`/`_en` columns match simplest Room + seed loader; adjust seed format in Phase 2 planning

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK | AGP/Kotlin compile | ✓ | OpenJDK 21.0.10 | JDK 17 also supported by AGP 8.7 |
| Android SDK | Build + emulator tests | ✓ | SDK at `~/Library/Android/sdk` | Install via Android Studio |
| adb | Instrumented tests | ✓ | 1.0.41 | — |
| Android emulator/device | `connectedDebugAndroidTest` | ? | — | Start emulator before test task; CI needs emulator image |
| Android Studio Ladybug+ | Project creation | ? | — | CLI-only scaffold possible via template; Studio recommended |
| ctx7 CLI | Doc lookup | ✗ | — | Used WebFetch + official docs instead |

**Missing dependencies with no fallback:**
- Running emulator or USB device for `connectedDebugAndroidTest` (required for DATA-01 persistence proof)

**Missing dependencies with fallback:**
- ctx7 — official Android docs used directly

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4 + AndroidX Test 1.6+ + Room Testing 2.8.4 |
| Config file | `app/build.gradle.kts` (`testInstrumentationRunner`, `androidTestImplementation`) |
| Quick run command | `./gradlew :app:assembleDebug` |
| Full suite command | `./gradlew :app:connectedDebugAndroidTest` |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| DATA-01 | Room schema creates all 7 tables with FK relationships | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.DaoInstrumentedTest"` | ❌ Wave 0 |
| DATA-01 | Kid row survives `db.close()` + new builder (simulated restart) | instrumented | `./gradlew :app:connectedDebugAndroidTest --tests "*.DatabasePersistenceTest"` | ❌ Wave 0 |
| DATA-01 | Debug APK builds and launches | smoke | `./gradlew :app:assembleDebug` | ❌ Wave 0 |
| — | Project scaffold compiles | build | `./gradlew :app:assembleDebug` | ❌ Wave 0 |

### Sampling Rate

- **Per task commit:** `./gradlew :app:assembleDebug`
- **Per wave merge:** `./gradlew :app:connectedDebugAndroidTest` (with emulator running)
- **Phase gate:** `assembleDebug` green + persistence instrumented test green before `/gsd-verify-work`

### Wave 0 Gaps

- [ ] `app/build.gradle.kts` — Android application module with KSP, Room plugin, Compose, Navigation
- [ ] `gradle/libs.versions.toml` — version catalog pins
- [ ] `app/src/androidTest/.../DaoInstrumentedTest.kt` — inserts catalog chain + kid + progress rows; verifies FK integrity
- [ ] `app/src/androidTest/.../DatabasePersistenceTest.kt` — disk DB close/reopen persistence
- [ ] `app/schemas/` — committed schema JSON after first compile
- [ ] `app/src/main/AndroidManifest.xml` — `application android:name=".SimmarkenApplication"`
- [ ] Framework install: Android project scaffold (Android Studio Empty Activity Compose template or equivalent)

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | Offline local app; no accounts |
| V3 Session Management | no | N/A |
| V4 Access Control | no | Single-user device storage |
| V5 Input Validation | yes (minimal) | Room parameterized queries; entity NOT NULL constraints; repository validation in later phases |
| V6 Cryptography | partial | App-private SQLite file (`MODE_PRIVATE`); no SQLCipher in v1 [ASSUMED: acceptable for swim-badge data per PROJECT.md privacy stance] |

### Known Threat Patterns for Android Room

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| SQL injection | Tampering | Room `@Query` with bound parameters only — never string-concat SQL |
| Backup leakage | Information disclosure | `android:allowBackup` — set `false` or exclude DB from Auto Backup if sensitive [ASSUMED: evaluate in manifest during scaffold] |
| Rooted device file read | Information disclosure | Standard Android app sandbox; encryption out of scope v1 |

## Sources

### Primary (HIGH confidence)
- [developer.android.com/jetpack/androidx/releases/room](https://developer.android.com/jetpack/androidx/releases/room) — Room 2.8.4, KSP setup, Gradle plugin, schema export
- [developer.android.com/jetpack/androidx/releases/navigation](https://developer.android.com/jetpack/androidx/releases/navigation) — Navigation 2.9.8, type-safe routes
- [developer.android.com/build/migrate-to-ksp](https://developer.android.com/build/migrate-to-ksp) — KSP migration, kapt deprecation
- [developer.android.com/training/data-storage/room/testing-db](https://developer.android.com/training/data-storage/room/testing-db) — in-memory vs instrumented testing
- [developer.android.com/reference/androidx/room/ForeignKey](https://developer.android.com/reference/androidx/room/ForeignKey) — CASCADE, index requirements
- [kotlinlang.org/docs/releases.html](https://kotlinlang.org/docs/releases.html) — Kotlin 2.1.21
- [kotlinlang.org/docs/gradle-configure-project.html](https://kotlinlang.org/docs/gradle-configure-project.html) — KGP ↔ AGP compatibility matrix

### Secondary (MEDIUM confidence)
- [github.com/google/ksp/releases/tag/2.1.21-2.0.2](https://github.com/google/ksp/releases/tag/2.1.21-2.0.2) — KSP version pairing
- [developer.android.com/jetpack/androidx/releases/room3](https://developer.android.com/jetpack/androidx/releases/room3) — Room 3.0 existence, not adopted
- [mvnrepository.com/artifact/androidx.compose/compose-bom](https://mvnrepository.com/artifact/androidx.compose/compose-bom) — Compose BOM release dates

### Tertiary (LOW confidence)
- None marked as authoritative — all critical claims cross-checked with official docs

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — official Android + Kotlin release pages confirm versions and APIs
- Architecture: HIGH — matches ARCHITECTURE.md, CONTEXT.md locked decisions, and established Android MVVM patterns
- Pitfalls: HIGH — drawn from PITFALLS.md + verified Room testing/FK documentation

**Research date:** 2026-07-22
**Valid until:** 2026-08-22 (30 days — stable Android stack; re-check Compose BOM monthly)
