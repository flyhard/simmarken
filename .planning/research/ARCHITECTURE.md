# Architecture Research

**Domain:** Local-first Android badge progress tracker
**Researched:** 2026-07-22
**Confidence:** HIGH

## Standard Architecture

### System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
├─────────────────────────────────────────────────────────────┤
│  HomeScreen    ChildCatalogScreen    BadgeDetailScreen     │
│       │                │                      │              │
│       └────────────────┴──────────────────────┘              │
│                        │                                     │
│                   ViewModels (StateFlow)                     │
├────────────────────────┴────────────────────────────────────┤
│                      Domain Layer                            │
├─────────────────────────────────────────────────────────────┤
│  Repositories: KidRepository, CatalogRepository,             │
│                ProgressRepository, ExportRepository           │
├─────────────────────────────────────────────────────────────┤
│                       Data Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  Room DAOs   │  │ Asset Seed   │  │ JSON I/O     │       │
│  │  (SQLite)    │  │ (catalog)    │  │ (backup)     │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|------------------------|
| Composable Screens | Render UI, handle user input | Jetpack Compose + Material 3 |
| ViewModel | UI state, event handling | `StateFlow` + `viewModelScope` |
| Repository | Business logic, data orchestration | Suspend functions + Flow |
| DAO | Database queries | Room `@Dao` with Flow returns |
| Seed Loader | Populate catalog on first launch | JSON in assets → Room insert |
| ExportRepository | Serialize/deserialize backup | kotlinx-serialization |

## Recommended Project Structure

```
app/src/main/java/se/simmarken/
├── data/
│   ├── local/
│   │   ├── entity/          # Room @Entity classes
│   │   ├── dao/             # Room @Dao interfaces
│   │   ├── AppDatabase.kt
│   │   └── Converters.kt
│   ├── seed/                # Catalog JSON + seed loader
│   └── export/              # JSON export/import
├── domain/
│   ├── model/               # UI-facing models (BadgeState, etc.)
│   └── repository/          # Repository interfaces + impls
├── ui/
│   ├── home/
│   ├── child/
│   ├── badge/
│   ├── settings/
│   ├── components/          # BadgeCard, StateOverlay, etc.
│   └── theme/
├── navigation/
│   └── NavGraph.kt
└── MainActivity.kt
```

### Structure Rationale

- **data/:** All persistence and I/O isolated from UI
- **domain/:** Badge state computation logic (locked → in progress → achieved → gotten)
- **ui/:** Feature-based screen packages matching navigation flows

## Architectural Patterns

### Pattern 1: Room as Single Source of Truth

**What:** UI never holds authoritative state — always observes Room Flows
**When to use:** All screens
**Trade-offs:** Simple for offline; no sync complexity

```kotlin
@Dao
interface ProgressDao {
    @Query("SELECT * FROM requirement_progress WHERE kidId = :kidId")
    fun observeForKid(kidId: Long): Flow<List<RequirementProgressEntity>>
}
```

### Pattern 2: Derived Badge State

**What:** Badge visual state computed from requirement progress + isGotten flag
**When to use:** Catalog grid and badge detail
**Trade-offs:** Must keep computation consistent across screens — centralize in domain layer

```kotlin
enum class BadgeVisualState { LOCKED, IN_PROGRESS, ACHIEVED_TO_BUY, GOTTEN }

fun computeBadgeState(
    requirements: List<RequirementProgress>,
    isGotten: Boolean
): BadgeVisualState
```

### Pattern 3: Catalog Seeding on First Launch

**What:** Pre-built JSON assets inserted into Room on app first run
**When to use:** Phase 2 catalog delivery
**Trade-offs:** App size grows with badge images; use WebP, reasonable resolutions

## Data Flow

### Requirement Check-off Flow

```
User taps checkbox
    ↓
BadgeDetailViewModel.toggleRequirement()
    ↓
ProgressRepository.updateRequirement(kidId, requirementId, achieved)
    ↓
Room DAO upsert → Flow emits
    ↓
ViewModel recomputes badge state
    ↓
UI recomposes (checklist + badge grid update)
```

### Badge State Computation

```
All requirements for badge
    ↓
Count achieved vs total
    ↓
0 achieved → LOCKED (or IN_PROGRESS if any started — product decision: first check = IN_PROGRESS)
partial → IN_PROGRESS
all achieved + !isGotten → ACHIEVED_TO_BUY
isGotten → GOTTEN
```

### Key Data Flows

1. **Home summary:** Aggregate per-kid counts of IN_PROGRESS and ACHIEVED_TO_BUY badges
2. **Catalog view:** Join Kid + Badge + Progress to render grid with states
3. **Export:** Serialize Kid + all Progress rows to versioned JSON file

## Entity Relationship

```
Catalog 1──* Category 1──* Badge 1──* Requirement
Kid 1──* RequirementProgress (kidId + requirementId → isAchieved)
Kid 1──* BadgeProgress (kidId + badgeId → isGotten)
```

**Note:** `isAchieved` on badge level can be derived from requirements but caching in BadgeProgress avoids repeated joins. Consider computed property vs stored field — derive at query time for v1 simplicity.

## Scaling Considerations

| Scale | Architecture Adjustments |
|-------|--------------------------|
| 1-5 kids, ~50 badges | Current architecture is sufficient |
| 10+ catalogs | Add catalog filtering; lazy-load images |
| 1000+ users | N/A — local-only, no server scale concerns |

## Anti-Patterns

### Anti-Pattern 1: Storing Badge State as Enum in DB

**What people do:** Write LOCKED/IN_PROGRESS/etc. to database
**Why it's wrong:** State becomes stale when requirements change
**Do this instead:** Derive state from requirement progress + isGotten at read time

### Anti-Pattern 2: Hardcoding Catalog in Code

**What people do:** Badge list as Kotlin objects
**Why it's wrong:** Can't update without app release; blocks multi-catalog
**Do this instead:** JSON seed files → Room, relational schema

### Anti-Pattern 3: Main Thread DB Access

**What people do:** Direct DAO calls from Composables
**Why it's wrong:** ANR risk, violates Android guidelines
**Do this instead:** ViewModel → Repository → DAO on Dispatchers.IO

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| None | — | 100% offline by design |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| UI ↔ ViewModel | Events up, StateFlow down | Unidirectional |
| ViewModel ↔ Repository | Suspend + Flow | No Android framework in domain |
| Repository ↔ DAO | Room types | Entity ↔ domain model mapping |

## Sources

- Android Architecture Guide (MVVM + Repository)
- PROJECT.md data model specification
- Room reactive Flow patterns

---
*Architecture research for: Simmärken Tracker*
*Researched: 2026-07-22*
