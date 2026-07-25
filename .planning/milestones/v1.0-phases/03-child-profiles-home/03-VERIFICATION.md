---
phase: 03-child-profiles-home
verified: 2026-07-23T08:40:00Z
status: passed
score: 12/12
overrides_applied: 0
re_verification:
  previous_status: gaps_found
  previous_score: 10/12
  gaps_closed:
    - "Cross-mode ViewModel contamination (add vs edit, edit kid A vs kid B) — viewModel key separates modes"
    - "Edit save before data load silently no-ops — isReadyToSave + Swedish error feedback"
    - "Double-tap duplicate inserts — synchronous _isSaving guard + disabled save button"
    - "Repeat add/edit auto-dismiss — sheetSessionId in viewModel key creates fresh VM per sheet open (eabad1e)"
  gaps_remaining: []
  regressions: []
gaps: []
deferred:
  - truth: "Catalog view shows real badge grid"
    addressed_in: "Phase 4"
    evidence: "Phase 4 goal: Parent sees badges grouped by category with clear 4-tier visual states; Phase 3 ships ChildCatalogPlaceholderScreen with 'Simmärken kommer snart'"
  - truth: "Home child cards show badge progress summary counts"
    addressed_in: "Phase 5"
    evidence: "Phase 5 success criterion 5: Home screen child cards show 'X badges in progress' and 'Y badges to buy' summaries (PROG-05)"
---

# Phase 3: Child Profiles & Home Verification Report

**Phase Goal:** Parent can manage children and see them on a kid-first home screen
**Verified:** 2026-07-23T08:35:00Z
**Status:** gaps_found
**Re-verification:** Yes — after gap-fix commit `929001f`

> **MVP mode note:** ROADMAP marks this phase `mode: mvp` but the phase goal is not in user-story format (`As a …, I want …, so that ….`). User Flow Coverage below is derived from roadmap success criteria.

## User Flow Coverage

| Step | Expected | Evidence | Status |
|------|----------|----------|--------|
| Open app | Kid-first home with FAB | `HomeScreen` Scaffold + FAB `openAddSheet` | ✓ |
| Add child (first) | FAB → bottom sheet → name + color → save → card appears | `KidFormBottomSheet`, `KidFormViewModel.save()` → `kidRepository.upsert` | ✓ |
| Add child (repeat) | Second FAB open → fresh sheet → save → second card | Static `key = "add"` reuses VM with `saveCompleted=true` → `LaunchedEffect` auto-dismiss | ✗ |
| View children | Full-width cards with avatar + name | `LazyColumn` + `ChildCard` fed by `kidRepository.observeAll()` | ✓ |
| Empty home | Swedish empty state, FAB visible | `EmptyState` copy + FAB outside empty branch | ✓ |
| Open child | Card tap → catalog screen with child name | `navController.navigate(ChildCatalog(kidId))` → `ChildCatalogPlaceholderScreen` | ✓ |
| Edit child (first / cross-mode) | Overflow → Redigera → prefilled sheet → save | Per-kidId `viewModel` key + `isReadyToSave` + `observeById` pre-fill | ✓ |
| Edit child (repeat same kid) | Second edit → prefilled sheet → save | Same kidId key retains `saveCompleted=true` → auto-dismiss | ✗ |
| Delete child | Overflow → Ta bort → confirm → card removed | `DeleteKidDialog` + `kidRepository.delete` | ✓ |
| Outcome | Parent can manage children on kid-first home | Repeat add/edit blocked — goal not fully achieved | ✗ |

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Parent can add a child with name and avatar/color via FAB | ✗ FAILED | First add wired and works. Commit `929001f` adds `viewModel(key = "add")` but VM is scoped to Home `NavBackStackEntry`, not sheet composable. After first save, `saveCompleted` stays `true`; second add sheet auto-dismisses via `LaunchedEffect` at `HomeScreen.kt:150-153`. |
| 2 | Home screen displays a card for each child | ✓ VERIFIED | `HomeViewModel` combines `kidRepository.observeAll()`; `HomeScreen` renders `ChildCard` per kid. |
| 3 | Kid-first home with FAB always visible (UI-01) | ✓ VERIFIED | FAB in Scaffold `floatingActionButton`; visible in empty and populated states. |
| 4 | Empty state shows Swedish heading/body with FAB (D-03) | ✓ VERIFIED | `EmptyState.kt` — "Lägg till ditt första barn" + body copy. |
| 5 | Child avatar shows initials on selected color circle (KIDS-02) | ✓ VERIFIED | `KidAvatar` + `KidAvatarInitials.fromName()` on `avatarColorArgb` circle. |
| 6 | Invalid names block save with Swedish error text | ✓ VERIFIED | `KidNameValidation` + unit tests pass. |
| 7 | Children persist in Room with creation-order sorting | ✓ VERIFIED | `KidDao.observeAll()` ORDER BY `sortOrder ASC, createdAtEpochMillis ASC`. |
| 8 | Tapping child card navigates to catalog placeholder with child name | ✓ VERIFIED | `ChildCatalog(kidId)` route; `ChildCatalogViewModel.childName` from `observeById`. |
| 9 | Back from catalog returns to home with kid list intact | ✓ VERIFIED | `navController.popBackStack()`; Home `observeAll` Flow unchanged. |
| 10 | No progress summary counts on cards (D-02) | ✓ VERIFIED | `ChildCard` renders avatar + name only. |
| 11 | Overflow menu offers Redigera and Ta bort (KIDS-04) | ✓ VERIFIED | `ChildCard` `DropdownMenu` with both items. |
| 12 | Parent can edit child profile via bottom sheet with field preservation | ✗ FAILED | Cross-mode edit (after add, kid A vs kid B) fixed by per-kidId key. Repeat edit for same kid reuses cached VM with `saveCompleted=true`. `isReadyToSave` + "Kunde inte ladda barnet" error address WR-02. |
| 13 | Delete shows confirmation warning and cascades progress | ✓ VERIFIED | `DeleteKidDialog` + `kidRepository.delete` + `KidDaoTest.deleteCascadesProgress`. |

**Score:** 10/12 truths verified (2 failed — shared root cause: static viewModel key retains `saveCompleted`)

### Re-verification Delta (commit `929001f`)

| Item | Before | After |
|------|--------|-------|
| `viewModel` key in `KidFormSheetContent` | Missing | `key = kidId?.toString() ?: "add"` at `HomeScreen.kt:134-136` |
| Edit save before load | Silent no-op | `isReadyToSave` disables button; error "Kunde inte ladda barnet" |
| Double-tap save | Race possible | `if (_isSaving.value) return` + `enabled = isReadyToSave && !isSaving` |
| Add after edit / edit kid B after A | Broken (shared default VM) | Fixed (separate keys) |
| Repeat add / repeat edit same kid | Broken | Still broken (same static key per mode/kid) |

### Deferred Items

| # | Item | Addressed In | Evidence |
|---|------|-------------|----------|
| 1 | Real catalog badge grid (placeholder only) | Phase 4 | Phase 4 goal: category-grouped badges with 4-tier visuals |
| 2 | Progress summary counts on home cards | Phase 5 | PROG-05 / Phase 5 SC5 |

### Required Artifacts

| Artifact | Expected | Status | Details |
| -------- | ----------- | ------ | ------- |
| `KidDao.kt` | CRUD + D-04 ordering | ✓ VERIFIED | `observeAll`, `observeById`, `upsert`, `deleteById` |
| `KidFormBottomSheet.kt` | Add/edit ModalBottomSheet | ✓ VERIFIED | `isReadyToSave && !isSaving` on save button |
| `KidAvatar.kt` | Initials avatar | ✓ VERIFIED | Wired in `ChildCard` and sheet |
| `KidDaoTest.kt` | CRUD + ordering tests | ✓ VERIFIED | Ordering + cascade delete tests |
| `ChildCard.kt` | Full-width tappable card | ✓ VERIFIED | Overflow menu + isolated click targets |
| `EmptyState.kt` | Empty home state | ✓ VERIFIED | Swedish copy per D-03 |
| `Routes.kt` | ChildCatalog route | ✓ VERIFIED | `@Serializable data class ChildCatalog(val kidId: Long)` |
| `ChildCatalogPlaceholderScreen.kt` | Catalog placeholder | ✓ VERIFIED | Child name in app bar |
| `DeleteKidDialog.kt` | Destructive confirmation | ✓ VERIFIED | Progress-loss warning |
| `KidFormViewModel.kt` | Edit mode + preservation | ⚠️ PARTIAL | Guards added; `saveCompleted` never reset on reuse |

### Key Link Verification

| From | To | Via | Status | Details |
| ---- | --- | --- | ------ | ------- |
| `KidFormViewModel.save()` | `KidRepository.upsert` | `viewModelScope.launch(IO)` | ✓ WIRED | Upsert in save() |
| `HomeViewModel.uiState` | `KidDao.observeAll` | `kidRepository.observeAll().stateIn` | ✓ WIRED | combine in HomeViewModel |
| `KidFormBottomSheet` | `KidNameValidation.validateName` | inline before save | ✓ WIRED | validateName in save() |
| `ChildCard.onCardClick` | `ChildCatalog` | `navController.navigate` | ✓ WIRED | HomeScreen line 82 |
| `HomeScreen` | `KidRepository.observeAll` | `HomeViewModel.uiState.kids` | ✓ WIRED | LazyColumn items |
| `ChildCatalogPlaceholderScreen` | `KidRepository.observeById` | `ChildCatalogViewModel` | ✓ WIRED | childName StateFlow |
| `ChildCard` overflow | `KidFormBottomSheet` Edit | `HomeViewModel.openEditSheet` | ⚠️ PARTIAL | First edit per kid works; repeat edit same kid fails |
| `DeleteKidDialog` confirm | `KidRepository.delete` | `HomeViewModel.confirmDelete` | ✓ WIRED | delete on IO dispatcher |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| -------- | ------------- | ------ | ------------------ | ------ |
| `HomeScreen` | `uiState.kids` | `kidRepository.observeAll()` | Room query | ✓ FLOWING |
| `ChildCatalogPlaceholderScreen` | `childName` | `kidRepository.observeById(kidId)` | Room query | ✓ FLOWING |
| `KidFormBottomSheet` | `name`, `selectedColorArgb` | User input + `observeById` (edit) | Real on first open per key | ⚠️ STATIC on same-key reuse |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| -------- | ------- | ------ | ------ |
| Name validation unit tests | `./gradlew :app:testDebugUnitTest --tests KidNameValidationTest` | BUILD SUCCESSFUL | ✓ PASS |
| Avatar initials/colors unit tests | `./gradlew :app:testDebugUnitTest --tests KidAvatarInitialsTest --tests KidAvatarColorsTest` | BUILD SUCCESSFUL | ✓ PASS |
| Debug APK assembles | `./gradlew :app:assembleDebug` | BUILD SUCCESSFUL | ✓ PASS |
| KidDao instrumented tests | `./gradlew :app:connectedDebugAndroidTest --tests KidDaoTest` | Not run (no emulator) | ? SKIP |

### Probe Execution

Step 7c: SKIPPED — no probe scripts declared for this phase.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| ----------- | ---------- | ----------- | ------ | -------- |
| KIDS-01 | 03-01, 03-03 | Parent can add a child with a name | ⚠️ PARTIAL | First add works; repeat add blocked by `saveCompleted` reuse |
| KIDS-02 | 03-01, 03-03 | Parent can assign avatar or color theme | ✓ SATISFIED | `ColorSwatchGrid`, `KidAvatarColors`, `KidAvatar` |
| KIDS-03 | 03-02 | Parent can view all children as cards on home | ✓ SATISFIED | `LazyColumn` + `ChildCard` |
| KIDS-04 | 03-03 | Parent can edit or remove a child profile | ⚠️ PARTIAL | Delete complete; first edit works; repeat edit same kid blocked |
| UI-01 | 03-02 | Kid-first home with FAB to add child | ✓ SATISFIED | FAB + kid cards layout |

All five phase requirement IDs accounted for. None orphaned.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| ---- | ---- | ------- | -------- | ------ |
| `HomeScreen.kt` | 134-136 | Static viewModel key (`"add"` / kidId) — VM survives sheet dismiss in NavBackStackEntry store | 🛑 Blocker | Repeat add/edit auto-dismiss |
| `KidFormViewModel.kt` | 108 | `_saveCompleted` set true, never reset | 🛑 Blocker | Same-key reuse triggers immediate dismiss |
| `HomeViewModel.kt` | 67-72 | `deleteTarget` cleared before delete completes | ⚠️ Warning | Dialog closes before IO delete (WR-03) |
| `ColorSwatchGrid.kt` | 34-47 | Swatches lack accessibility labels | ℹ️ Info | Screen reader cannot identify colors (IN-01) |

No `TBD`/`FIXME`/`XXX` debt markers in phase-modified source files.

### Human Verification Required

### 1. Repeat add flow (critical)

**Test:** Add child "Ella" → save → FAB again → add child "Noah" → save
**Expected:** Both cards appear; second sheet stays open until user saves
**Why human:** Code analysis shows static `"add"` key retains `saveCompleted=true`; confirm on device whether behavior differs

### 2. Repeat edit same kid

**Test:** Edit child name → save → overflow → Redigera same child again
**Expected:** Sheet opens prefilled; does not auto-dismiss
**Why human:** Same static kidId key may retain `saveCompleted`

### 3. Add-child first use

**Test:** Launch app → FAB → enter name → pick color → Lägg till
**Expected:** Card appears with correct initials and color
**Why human:** Visual avatar rendering

### 4. Home empty and populated states

**Test:** 0 kids: empty state + FAB. 2+ kids: full-width cards, no summary counts
**Expected:** Swedish empty copy; avatar + name only on cards
**Why human:** Layout per UI-SPEC

### 5. Catalog navigation

**Test:** Tap card → child name in top bar + "Simmärken kommer snart" → back
**Expected:** Round-trip preserves list
**Why human:** Back-stack behavior

### 6. Delete flow

**Test:** Overflow → Ta bort → Avbryt keeps child; confirm removes
**Expected:** Confirmation dialog works
**Why human:** Destructive action UX

### Gaps Summary

Commit `929001f` correctly implements the CR-01 prescription (`viewModel` key, `isReadyToSave`, double-tap guard, edit-load error). Cross-mode contamination is resolved: add vs edit and edit kid A vs kid B now get separate ViewModel instances.

However, the `viewModel` key is scoped to the Home `NavBackStackEntry`, not the sheet composable's lifetime. Keys `"add"` and `"<kidId>"` are static across sheet sessions, so `saveCompleted` persists after the first successful save. Reopening the same-mode sheet retrieves the cached ViewModel and `LaunchedEffect(saveCompleted)` at `HomeScreen.kt:150-153` immediately dismisses it. This blocks adding a second child and re-editing the same profile — both required for "manage children."

**Recommended fix:** Add a monotonic `sheetSessionId` in `HomeViewModel`, increment on `openAddSheet`/`openEditSheet`, and include it in the viewModel key (e.g. `"add-$sessionId"` / `"$kidId-$sessionId"`). Alternatively, wrap `KidFormSheetContent` in `rememberViewModelStoreOwner()` (Lifecycle 2.11+; project is on 2.8.7).

---

_Verified: 2026-07-23T08:35:00Z_
_Verifier: Claude (gsd-verifier)_
