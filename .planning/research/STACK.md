# Stack Research

**Domain:** Local-first Android progress-tracking app (parent/child, offline)
**Researched:** 2026-07-22
**Confidence:** HIGH

## Recommended Stack

### Core Technologies

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Kotlin | 2.0+ | Language | Official Android language; coroutines/Flow native support |
| Jetpack Compose | BOM 2024.12+ | UI | State-driven UI ideal for reactive progress tracking |
| Room | 2.6.1+ | Local database | Google-recommended SQLite wrapper; Flow integration for reactive UI |
| KSP | 2.0.x | Annotation processing | Required for Room 2.6+; faster than deprecated kapt |
| MVVM + Repository | — | Architecture | Standard Android pattern; separates UI from data layer |
| Navigation Compose | 2.8.x | Navigation | Type-safe routing between Home → Child → Badge Detail |
| Material 3 | Compose BOM | Design system | Clean minimal UI with system theming support |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| kotlinx-serialization-json | 1.7+ | JSON export/import | Backup/migration file format |
| Coil | 2.7+ | Image loading | Badge images from assets/drawables |
| DataStore Preferences | 1.1+ | Settings | Language preference, theme |
| Hilt (optional) | 2.52+ | DI | If project grows beyond manual DI; defer until needed |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Android Studio Ladybug+ | IDE | Compose preview, Room schema export |
| Gradle Kotlin DSL | Build | Standard for new Android projects |
| Room schema export | Migrations | Export JSON schemas from day 1 for safe migrations |

## Installation

```kotlin
// libs.versions.toml (representative)
[versions]
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
room = "2.6.1"
composeBom = "2024.12.01"
navigation = "2.8.5"
coil = "2.7.0"
serialization = "1.7.3"

// build.gradle.kts (app)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)
}
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| Room | SQLDelight | Kotlin Multiplatform needed (not in scope) |
| Compose | XML Views | Legacy codebase integration (greenfield — use Compose) |
| Manual DI | Hilt | Team grows or DI complexity increases |
| JSON export | Android Backup API | If user wants zero-config cloud backup (out of scope) |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| kapt | Deprecated for Room; slower builds | KSP |
| LiveData for new code | Compose prefers Flow/StateFlow | Kotlin Flow + collectAsStateWithLifecycle |
| Network stack (Retrofit, WorkManager sync) | 100% offline app — no backend | Room as sole source of truth |
| SharedPreferences for structured data | No relational queries | Room |
| Hardcoded strings | Need SV + EN | strings.xml + values-sv/values-en |

## Stack Patterns by Variant

**Pure offline (this project):**
- Room is the single source of truth — no sync layer needed
- UI observes `Flow` from DAOs directly via ViewModel `StateFlow`
- All writes on `Dispatchers.IO`

**If export/import added:**
- kotlinx-serialization for portable JSON schema
- Version field in export format for forward compatibility

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| Room 2.6.1 | KSP 2.0.x | Requires KSP, not kapt |
| Compose BOM 2024.12 | AGP 8.7+ | Match compileSdk 35 |
| Navigation 2.8.x | Compose 1.7+ | Use type-safe routes |

## Sources

- [Android Room docs](https://developer.android.com/training/data-storage/room) — Flow, KSP setup (HIGH)
- [Jetpack Compose docs](https://developer.android.com/jetpack/compose) — state management (HIGH)
- Svensk Simidrott simmärkesaffisch 2024 — badge data source (HIGH)

---
*Stack research for: Simmärken Tracker*
*Researched: 2026-07-22*
