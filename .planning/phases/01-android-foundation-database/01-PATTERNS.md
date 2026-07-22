# Phase 1: Android Foundation & Database - Pattern Map

**Mapped:** 2026-07-22
**Files analyzed:** 35
**Analogs found:** 0 codebase / 35 research-reference

> **Greenfield note:** No Android source exists in the repo yet. All analogs are drawn from `01-RESEARCH.md` code examples and `.planning/research/ARCHITECTURE.md` architecture patterns. Planner should treat these as the canonical copy-from sources until real code lands.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `gradle/libs.versions.toml` | config | batch | `01-RESEARCH.md` §Standard Stack | research-ref |
| `build.gradle.kts` | config | batch | `01-RESEARCH.md` §Standard Stack | research-ref |
| `settings.gradle.kts` | config | batch | `01-RESEARCH.md` §Recommended Project Structure | research-ref |
| `app/build.gradle.kts` | config | batch | `01-RESEARCH.md` Pattern 1 + Code Examples | research-ref |
| `app/src/main/AndroidManifest.xml` | config | — | `01-RESEARCH.md` §Validation Architecture | research-ref |
| `app/schemas/.../1.json` | migration | batch | `01-RESEARCH.md` Pattern 1 | research-ref (generated) |
| `SimmarkenApplication.kt` | provider | event-driven | `01-RESEARCH.md` Pattern 3 | research-ref |
| `di/AppContainer.kt` | provider | CRUD | `01-RESEARCH.md` Pattern 3 | research-ref |
| `data/local/AppDatabase.kt` | config | CRUD | `01-RESEARCH.md` Code Examples | research-ref |
| `data/local/Converters.kt` | utility | transform | `01-RESEARCH.md` Code Examples | research-ref |
| `data/local/entity/CatalogEntity.kt` | model | CRUD | `01-RESEARCH.md` §Entity Field Definitions | research-ref |
| `data/local/entity/CategoryEntity.kt` | model | CRUD | `01-RESEARCH.md` §Entity Field Definitions | research-ref |
| `data/local/entity/BadgeEntity.kt` | model | CRUD | `01-RESEARCH.md` §Entity Field Definitions | research-ref |
| `data/local/entity/RequirementEntity.kt` | model | CRUD | `01-RESEARCH.md` §Entity Field Definitions | research-ref |
| `data/local/entity/KidEntity.kt` | model | CRUD | `01-RESEARCH.md` §Entity Field Definitions | research-ref |
| `data/local/entity/RequirementProgressEntity.kt` | model | CRUD | `01-RESEARCH.md` §RequirementProgressEntity | research-ref |
| `data/local/entity/BadgeProgressEntity.kt` | model | CRUD | `01-RESEARCH.md` §BadgeProgressEntity | research-ref |
| `data/local/dao/CatalogDao.kt` | service | streaming | `ARCHITECTURE.md` Pattern 1 | research-ref |
| `data/local/dao/KidDao.kt` | service | streaming | `01-RESEARCH.md` Pattern 2 | research-ref |
| `data/local/dao/RequirementProgressDao.kt` | service | streaming | `ARCHITECTURE.md` Pattern 1 | research-ref |
| `data/local/dao/BadgeProgressDao.kt` | service | streaming | `ARCHITECTURE.md` Pattern 1 | research-ref |
| `domain/repository/CatalogRepository.kt` | service | CRUD | `ARCHITECTURE.md` Component Responsibilities | research-ref |
| `domain/repository/KidRepository.kt` | service | CRUD | `ARCHITECTURE.md` Component Responsibilities | research-ref |
| `domain/repository/ProgressRepository.kt` | service | CRUD | `ARCHITECTURE.md` Data Flow | research-ref |
| `MainActivity.kt` | component | request-response | `01-RESEARCH.md` §System Architecture Diagram | research-ref |
| `navigation/Routes.kt` | route | — | `01-RESEARCH.md` Pattern 4 | research-ref |
| `navigation/SimmarkenNavHost.kt` | component | request-response | `01-RESEARCH.md` Pattern 4 | research-ref |
| `ui/theme/Color.kt` | config | — | `01-RESEARCH.md` §Recommended Project Structure | research-ref |
| `ui/theme/Theme.kt` | component | — | `01-RESEARCH.md` §Recommended Project Structure | research-ref |
| `ui/theme/Type.kt` | config | — | `01-RESEARCH.md` §Recommended Project Structure | research-ref |
| `ui/home/HomePlaceholderScreen.kt` | component | — | `01-RESEARCH.md` Pattern 4 | research-ref |
| `androidTest/.../DaoInstrumentedTest.kt` | test | CRUD | `01-RESEARCH.md` §Validation Architecture | research-ref |
| `androidTest/.../DatabasePersistenceTest.kt` | test | file-I/O | `01-RESEARCH.md` Code Examples | research-ref |
| `test/.../PlaceholderUnitTest.kt` | test | — | `01-RESEARCH.md` §Recommended Project Structure | research-ref |

---

## Pattern Assignments

### Build & Config Layer

#### `gradle/libs.versions.toml` (config, batch)

**Analog:** `01-RESEARCH.md` §Standard Stack (lines 96-115)

**Version catalog pattern:**
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

**Key constraints:** KSP version must match Kotlin patch (`2.1.21` ↔ `2.1.21-2.0.2`). Room 2.8.4, not 3.0 (`androidx.room3`).

---

#### `app/build.gradle.kts` (config, batch)

**Analog:** `01-RESEARCH.md` Pattern 1 (lines 363-388) + Code Examples (lines 616-636)

**Plugins block:**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}
```

**Android + Room schema export:**
```kotlin
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
```

**Dependencies pattern:**
```kotlin
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)          // NOT kapt
    implementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.androidx.room.testing)
}
```

**Anti-pattern:** Do not use `fallbackToDestructiveMigration()` in release builds.

---

#### `app/src/main/AndroidManifest.xml` (config)

**Analog:** `01-RESEARCH.md` §Validation Architecture (line 725)

**Application registration:**
```xml
<application
    android:name=".SimmarkenApplication"
    android:allowBackup="false"
    ...>
```

Evaluate `allowBackup` per security research — default to `false` or exclude DB from Auto Backup.

---

#### `app/schemas/se.simmarken.data.local.AppDatabase/1.json` (migration, batch — generated)

**Analog:** `01-RESEARCH.md` Pattern 1 (lines 358-390)

**What:** Auto-generated on first compile when `exportSchema = true` and Room Gradle Plugin configured. **Commit to VCS** — do not gitignore.

---

### Data Layer — Database

#### `data/local/AppDatabase.kt` (config, CRUD)

**Analog:** `01-RESEARCH.md` Code Examples (lines 536-560)

**Core pattern:**
```kotlin
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

**Phase 1 builder:** No migrations array — version 1 only. See Pattern 5 for future bumps.

---

#### `data/local/Converters.kt` (utility, transform)

**Analog:** `01-RESEARCH.md` Code Examples (lines 565-576)

**Optional pattern** — entities use `Long` epoch millis directly; converters enable `Instant` in domain layer later:
```kotlin
class Converters {
    @TypeConverter
    fun fromEpochMillis(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toEpochMillis(instant: Instant?): Long? =
        instant?.toEpochMilli()
}
```

---

### Data Layer — Catalog Entities

#### `data/local/entity/CatalogEntity.kt` (model, CRUD)

**Analog:** `01-RESEARCH.md` §CatalogEntity (lines 254-263)

**Field pattern:**
```kotlin
@Entity(
    tableName = "catalogs",
    indices = [Index(value = ["code"], unique = true)],
)
data class CatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,           // e.g. "simidrott", "sls"
    val nameSv: String,
    val nameEn: String,
    val catalogVersion: String,
    val sortOrder: Int = 0,
)
```

**i18n convention:** Bilingual columns (`nameSv`/`nameEn`) — no separate translation table in v1.

---

#### `data/local/entity/CategoryEntity.kt` (model, CRUD)

**Analog:** `01-RESEARCH.md` §CategoryEntity (lines 265-273)

**FK + index pattern** (apply to all child entities):
```kotlin
@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = CatalogEntity::class,
            parentColumns = ["id"],
            childColumns = ["catalogId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("catalogId"),
        Index(value = ["catalogId", "code"], unique = true),
    ],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val catalogId: Long,
    val code: String,
    val nameSv: String,
    val nameEn: String,
    val sortOrder: Int,
)
```

**Pitfall:** Every FK child column needs `@Index` — avoids `MISSING_INDEX_ON_FOREIGN_KEY_CHILD` warnings.

---

#### `data/local/entity/BadgeEntity.kt` (model, CRUD)

**Analog:** `01-RESEARCH.md` §BadgeEntity (lines 275-284)

Same FK+CASCADE+index pattern as CategoryEntity, parent = `CategoryEntity`, child column = `categoryId`, unique index on `(categoryId, code)`.

---

#### `data/local/entity/RequirementEntity.kt` (model, CRUD)

**Analog:** `01-RESEARCH.md` §RequirementEntity (lines 286-294)

Same FK+CASCADE+index pattern, parent = `BadgeEntity`, child column = `badgeId`, unique index on `(badgeId, code)`. Text fields: `textSv`/`textEn`.

---

#### `data/local/entity/KidEntity.kt` (model, CRUD)

**Analog:** `01-RESEARCH.md` §KidEntity (lines 296-304)

```kotlin
@Entity(tableName = "kids")
data class KidEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarColorArgb: Int,
    val createdAtEpochMillis: Long,  // System.currentTimeMillis() at insert
    val sortOrder: Int = 0,
)
```

---

#### `data/local/entity/RequirementProgressEntity.kt` (model, CRUD)

**Analog:** `01-RESEARCH.md` §RequirementProgressEntity (lines 306-344) — **exact excerpt, D-01/D-04**

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
    val achievedAtEpochMillis: Long?,  // set on check, cleared on uncheck (D-04)
)
```

**D-04:** Bidirectional toggle — `achievedAtEpochMillis` nullable, cleared on uncheck.

---

#### `data/local/entity/BadgeProgressEntity.kt` (model, CRUD)

**Analog:** `01-RESEARCH.md` §BadgeProgressEntity (lines 346-356) — **D-01/D-02/D-03**

```kotlin
@Entity(
    tableName = "badge_progress",
    primaryKeys = ["kidId", "badgeId"],
    foreignKeys = [
        ForeignKey(
            entity = KidEntity::class,
            parentColumns = ["id"],
            childColumns = ["kidId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BadgeEntity::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("kidId"),
        Index("badgeId"),
    ],
)
data class BadgeProgressEntity(
    val kidId: Long,
    val badgeId: Long,
    val isGotten: Boolean = false,
    val achievedAtEpochMillis: Long? = null,  // write-once (D-03); never auto-cleared
    val gottenAtEpochMillis: Long? = null,      // set when isGotten toggled true (D-02)
)
```

**Anti-pattern:** No `visualState`, `status`, or enum column (D-02/D-05). No `UPDATE ... SET achievedAt = NULL` (D-03).

---

### Data Layer — DAOs

#### `data/local/dao/KidDao.kt` (service, streaming)

**Analog:** `01-RESEARCH.md` Pattern 2 (lines 397-406) — **canonical DAO template**

```kotlin
@Dao
interface KidDao {
    @Query("SELECT * FROM kids ORDER BY sortOrder, name")
    fun observeAll(): Flow<List<KidEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(kid: KidEntity): Long
}
```

**Convention for all DAOs:**
- Read/observe → `Flow<T>` (not LiveData)
- Write → `suspend fun`
- Use `@Insert(onConflict = OnConflictStrategy.REPLACE)` for upserts

---

#### `data/local/dao/RequirementProgressDao.kt` (service, streaming)

**Analog:** `ARCHITECTURE.md` Pattern 1 (lines 86-92)

```kotlin
@Dao
interface RequirementProgressDao {
    @Query("SELECT * FROM requirement_progress WHERE kidId = :kidId")
    fun observeForKid(kidId: Long): Flow<List<RequirementProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: RequirementProgressEntity)
}
```

---

#### `data/local/dao/CatalogDao.kt` / `BadgeProgressDao.kt` (service, streaming)

**Analog:** `KidDao.kt` template above + entity-specific queries.

`CatalogDao` should expose observe queries for catalog hierarchy (catalogs, categories-by-catalog, badges-by-category, requirements-by-badge) as needed for Phase 2 seeding verification. `BadgeProgressDao` mirrors `RequirementProgressDao` with `observeForKid(kidId)`.

---

### Domain Layer — Repositories

#### `domain/repository/KidRepository.kt` + impl (service, CRUD)

**Analog:** `ARCHITECTURE.md` Component Responsibilities (lines 36-43) + Internal Boundaries (lines 200-204)

**Interface pattern:**
```kotlin
interface KidRepository {
    fun observeAll(): Flow<List<KidEntity>>
    suspend fun upsert(kid: KidEntity): Long
}
```

**Impl pattern:**
```kotlin
class KidRepositoryImpl(private val kidDao: KidDao) : KidRepository {
    override fun observeAll(): Flow<List<KidEntity>> = kidDao.observeAll()
    override suspend fun upsert(kid: KidEntity): Long = kidDao.upsert(kid)
}
```

**Boundary rule:** Repository wraps DAO; no Android framework types in interface. Business rules (e.g., D-03 write-once `achievedAt`) live in `ProgressRepository` impl — deferred logic to Phase 5, but interface exists in Phase 1.

---

#### `domain/repository/CatalogRepository.kt` (service, CRUD)

**Analog:** `ARCHITECTURE.md` Component Responsibilities — stub/default impl wrapping `CatalogDao`. Phase 1: pass-through; Phase 2 adds seed orchestration.

---

#### `domain/repository/ProgressRepository.kt` (service, CRUD)

**Analog:** `ARCHITECTURE.md` Data Flow (lines 117-131)

Phase 1: interface + stub impl delegating to `RequirementProgressDao` and `BadgeProgressDao`. Write-once `achievedAtEpochMillis` logic implemented in Phase 5 per D-03.

---

### DI & Application Entry

#### `di/AppContainer.kt` (provider, CRUD)

**Analog:** `01-RESEARCH.md` Pattern 3 (lines 415-427)

```kotlin
class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DB_NAME,
    ).build()

    val kidRepository: KidRepository = KidRepositoryImpl(database.kidDao())
    val catalogRepository: CatalogRepository = CatalogRepositoryImpl(database.catalogDao())
    val progressRepository: ProgressRepository = ProgressRepositoryImpl(
        database.requirementProgressDao(),
        database.badgeProgressDao(),
    )
}
```

**Key:** One disk-backed `Room.databaseBuilder` per process. Use `context.applicationContext`.

---

#### `SimmarkenApplication.kt` (provider, event-driven)

**Analog:** `01-RESEARCH.md` §System Architecture Diagram (line 186)

```kotlin
class SimmarkenApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
```

Referenced from `AndroidManifest.xml` via `android:name=".SimmarkenApplication"`.

---

#### `MainActivity.kt` (component, request-response)

**Analog:** `01-RESEARCH.md` §System Architecture Diagram (lines 157-161)

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimmarkenTheme {
                SimmarkenNavHost()
            }
        }
    }
}
```

**Anti-pattern:** No DAO calls from Composables — always ViewModel → Repository → DAO (Phase 3+).

---

### Navigation Layer

#### `navigation/Routes.kt` + `navigation/SimmarkenNavHost.kt` (route + component)

**Analog:** `01-RESEARCH.md` Pattern 4 (lines 429-444)

**Routes:**
```kotlin
@Serializable object Home
```

**NavHost:**
```kotlin
@Composable
fun SimmarkenNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Home) {
        composable<Home> { HomePlaceholderScreen() }
    }
}
```

Requires `kotlinx-serialization` plugin. Type-safe routes — no string routes like `"home"`.

---

### UI Layer

#### `ui/theme/Theme.kt`, `Color.kt`, `Type.kt` (config/component)

**Analog:** `01-RESEARCH.md` §Recommended Project Structure (lines 234-237)

Standard Compose Material 3 theme scaffold from Android Studio Empty Activity Compose template. `SimmarkenTheme` wraps `MaterialTheme` with project colors/typography.

---

#### `ui/home/HomePlaceholderScreen.kt` (component)

**Analog:** `01-RESEARCH.md` Pattern 4 — minimal placeholder composable. No ViewModel or DB access in Phase 1.

```kotlin
@Composable
fun HomePlaceholderScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Simmärken")
    }
}
```

---

### Test Layer

#### `androidTest/.../DatabasePersistenceTest.kt` (test, file-I/O)

**Analog:** `01-RESEARCH.md` Code Examples (lines 579-614) — **disk-backed, not in-memory**

```kotlin
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

**Pitfall:** `inMemoryDatabaseBuilder` does NOT prove restart persistence (DATA-01).

---

#### `androidTest/.../DaoInstrumentedTest.kt` (test, CRUD)

**Analog:** `01-RESEARCH.md` §Validation Architecture (lines 705-708)

Insert full catalog chain (Catalog → Category → Badge → Requirement) + Kid + both progress rows. Verify FK integrity and cascade behavior. Use disk-backed builder same as persistence test.

---

#### `test/.../PlaceholderUnitTest.kt` (test)

**Analog:** `01-RESEARCH.md` §Recommended Project Structure — minimal JVM unit test proving test source set compiles. Domain logic tests added in later phases.

---

## Shared Patterns

### Room as Single Source of Truth
**Source:** `ARCHITECTURE.md` Pattern 1 (lines 80-92)
**Apply to:** All DAOs, repositories, future ViewModels

```kotlin
@Dao
interface ProgressDao {
    @Query("SELECT * FROM requirement_progress WHERE kidId = :kidId")
    fun observeForKid(kidId: Long): Flow<List<RequirementProgressEntity>>
}
```

UI never holds authoritative state — observes Room `Flow` via ViewModel `StateFlow` (Phase 3+).

---

### Derived Badge State (NOT in DB)
**Source:** `ARCHITECTURE.md` Pattern 2 (lines 94-107) + `01-CONTEXT.md` D-05
**Apply to:** Entity design validation — ensure no enum columns; `BadgeStateCalculator` deferred to Phase 4

```kotlin
enum class BadgeVisualState { LOCKED, IN_PROGRESS, ACHIEVED_TO_BUY, GOTTEN }

fun computeBadgeState(
    requirements: List<RequirementProgress>,
    isGotten: Boolean
): BadgeVisualState
```

---

### Manual DI via AppContainer
**Source:** `01-RESEARCH.md` Pattern 3 + `STACK.md` (Hilt deferred)
**Apply to:** `SimmarkenApplication`, `AppContainer`, all repository wiring

One `Room.databaseBuilder` per process. Repositories constructed in `AppContainer`, accessed via `(application as SimmarkenApplication).container`.

---

### FK CASCADE + Child Indices
**Source:** `01-RESEARCH.md` Pitfall 1 (lines 490-495)
**Apply to:** All entity files with foreign keys

Every `ForeignKey` child column gets `indices = [Index("childColumn")]`. Catalog hierarchy and kid deletion cascade correctly.

---

### Epoch Millis Timestamps
**Source:** `01-RESEARCH.md` §Don't Hand-Roll (lines 483-484)
**Apply to:** All timestamp fields in entities

Store `Long` epoch millis in SQLite (`INTEGER`). No string dates. Optional `Converters` for `Instant` in domain layer.

---

### Write-Once Badge Achievement
**Source:** `01-CONTEXT.md` D-03 + `01-RESEARCH.md` Pitfall 5 (lines 518-523)
**Apply to:** `BadgeProgressEntity` schema + future `ProgressRepository`

`achievedAtEpochMillis` set when all requirements first achieved; never cleared on uncheck. Symmetric toggle logic from `RequirementProgress` must NOT be copied to badge achievement.

---

### Layer Boundaries
**Source:** `ARCHITECTURE.md` Internal Boundaries (lines 200-204)

| Boundary | Communication |
|----------|---------------|
| UI ↔ ViewModel | Events up, StateFlow down |
| ViewModel ↔ Repository | Suspend + Flow |
| Repository ↔ DAO | Room entity types |

No Android framework in domain interfaces. All writes on `Dispatchers.IO` (Phase 3+).

---

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| All 35 files | — | — | Greenfield project — zero Android/Kotlin source in repo. Research docs serve as reference analogs. |

After Phase 1 execution, subsequent phases should re-map against real `app/src/main/java/se/simmarken/` code.

---

## Metadata

**Analog search scope:** Entire workspace (`/Users/ues201/Projects/simmmärken`)
**Files scanned:** 41 (all repo files; only `.planning/` and `.cursor/` contain content)
**Codebase Kotlin/XML files:** 0
**Reference analogs used:** `01-RESEARCH.md`, `.planning/research/ARCHITECTURE.md`, `.planning/research/STACK.md`
**Pattern extraction date:** 2026-07-22
