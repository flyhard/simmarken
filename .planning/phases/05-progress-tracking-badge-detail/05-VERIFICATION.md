---
phase: 05-progress-tracking-badge-detail
verified: 2026-07-23T12:30:00Z
status: passed
score: 12/13 must-haves verified
behavior_unverified: 1
overrides_applied: 0
behavior_unverified_items:

  - truth: "Tapping a requirement instantly toggles it and persists to database"
    test: "On device/emulator: open a badge with requirements, toggle one checkbox, force-stop and relaunch the app, reopen the same badge"
    expected: "The toggled requirement remains checked; badge visual state reflects persisted progress"
    why_human: "ViewModel unit tests use FakeProgressRepository; DaoInstrumentedTest covers Room upserts in isolation but no test exercises ViewModel → ProgressRepositoryImpl → Room as one path"
human_verification:

  - test: "Swim-hall flow: Home → child card → catalog → badge detail"
    expected: "Badge detail shows 160dp pin image at top, badge name, 'X av Y klara' subtitle (when requirements exist), scrollable checklist, and 'Fysiskt märke köpt' switch at bottom"
    why_human: "Layout, image rendering, and one-handed usability cannot be verified by grep or unit tests"

  - test: "Toggle individual requirements on a multi-requirement badge"
    expected: "Checkbox and pin visual state update immediately on each tap; no confirmation dialog; purchase switch enables only when all requirements are checked"
    why_human: "Instant UI feedback and disabled-switch styling are runtime/visual behaviors"

  - test: "Mark badge köpt, then attempt to un-mark via switch"
    expected: "'Ta bort köpt-markering?' dialog appears; confirming clears gotten state while retaining achieved requirements"
    why_human: "Dialog presentation and copy require on-device verification"

  - test: "On a GOTTEN badge, uncheck one requirement"
    expected: "Gotten state clears silently (no dialog); badge reverts to appropriate visual state"
    why_human: "State transition ordering at runtime is not covered by a single behavioral test"

  - test: "Open a zero-requirement badge (e.g. Droppen)"
    expected: "'Inga kunskapskrav' note shown instead of checklist; purchase toggle enabled immediately"
    why_human: "Zero-requirement edge-case layout requires visual confirmation"

  - test: "After progressing badges for a child, return to home"
    expected: "Child card shows separate 'N pågår' and 'M att köpa' lines only when counts > 0; gotten badges do not inflate counts"
    why_human: "Subtitle visibility rules and copy are visual"
---

# Phase 5: Progress Tracking & Badge Detail Verification Report

**Phase Goal:** Parents can check off skills and mark badges as purchased at the swim hall.
**MVP user story (from plans):** As a parent at the swim hall, I want to check off skills and mark badges as purchased, so that I can answer whether my child passed and whether we bought the physical pin.
**Verified:** 2026-07-23T12:30:00Z
**Status:** human_needed
**Re-verification:** No — initial verification

> **Note:** ROADMAP.md goal is not in canonical user-story format (`As a …, I want …, so that ….`). Plan files use the valid user story above; MVP verification uses the plan wording.

## User Flow Coverage

| Step | Expected | Evidence | Status |
|------|----------|----------|--------|
| Open child from home | Tap child card navigates to catalog | `HomeScreen.kt` → `ChildCatalog(kidId)`; `SimmarkenNavHost.kt` composable wiring | ✓ VERIFIED |
| Open badge detail | Tap badge opens interactive detail | `ChildCatalogScreen` `onBadgeClick` → `BadgeDetail` route → `BadgeDetailScreen` | ✓ VERIFIED |
| Check off skills | Requirement rows toggle and update pin state | `RequirementChecklist` → `toggleRequirement` → `upsertRequirementProgress`; `BadgeDetailViewModelProgressTest` | ✓ VERIFIED (ViewModel layer) |
| All skills checked → ready to buy | Visual state becomes ACHIEVED_TO_BUY | `lastRequirement_setsAchievedToBuy` test; `BadgeStateCalculator.compute` | ✓ VERIFIED |
| Mark physically purchased | Purchase switch sets gotten | `PurchaseToggleRow` → `setGotten`; `setGotten_true_upsertsGottenFields` test | ✓ VERIFIED |
| Answer "passed + bought" | Detail shows checklist progress and köpt state; home shows attention counts | `BadgeDetailScreen` progress subtitle + `ChildCard` pågår/att köpa lines | ✓ VERIFIED (wiring) |
| Persistence survives relaunch | Toggled progress stored in Room | `ProgressRepositoryImpl` → DAO upsert; no e2e instrumented test | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED |

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Badge detail shows high-res image and checklist of official requirements (SC-1, UI-03) | ✓ VERIFIED | `BadgeDetailScreen` renders `BadgePinVisual(size=Detail)` (160dp / 320px coil) + `RequirementChecklist` from catalog `RequirementEntity.textSv` |
| 2 | Tapping a requirement instantly toggles it and persists to database (SC-2, PROG-01) | ⚠️ PRESENT_BEHAVIOR_UNVERIFIED | `toggleRequirement` → `progressRepository.upsertRequirementProgress` on `Dispatchers.IO`; `toggleRequirement_persistsUpsert` test uses fake repo — Room round-trip not exercised end-to-end |
| 3 | When all requirements are checked, badge state updates to Achieved-to-buy automatically (SC-3, PROG-02) | ✓ VERIFIED | `ProgressWriteLogic.shouldSetAchievedAt` + `lastRequirement_setsAchievedToBuy` test; `BadgeStateCalculator` returns `ACHIEVED_TO_BUY` |
| 4 | Purchase toggle marks badge as Gotten independently of skill completion (SC-4, PROG-03) | ✓ VERIFIED | `setGotten` upserts `isGotten`/`gottenAtEpochMillis`; zero-requirement badge enables purchase immediately (`isPurchaseEnabled_trueForZeroRequirementBadge`) |
| 5 | Home screen child cards show "X badges in progress" and "Y badges to buy" summaries (SC-5, PROG-05) | ✓ VERIFIED | `KidProgressSummaryCalculator` + `ChildCard` `"$inProgressCount pågår"` / `"$toBuyCount att köpa"`; wired via `HomeViewModel.summariesByKidId` |
| 6 | achievedAt write-once; unchecking does not clear it (D-09, D-18) | ✓ VERIFIED | `ProgressWriteLogic.preserveAchievedAt`; `uncheckWhileGotten_clearsGotten` retains `achievedAtEpochMillis` |
| 7 | Unchecking a skill while GOTTEN auto-clears gotten without dialog (D-10) | ✓ VERIFIED | `shouldClearGottenOnUncheck` + `uncheckWhileGotten_clearsGotten` test |
| 8 | Unchecking all skills returns badge to LOCKED (D-11) | ✓ VERIFIED | `uncheckWhileGotten_clearsGotten` asserts `BadgeVisualState.LOCKED` after uncheck |
| 9 | All visual state derived only through BadgeStateCalculator — never persisted enum (D-17) | ✓ VERIFIED | `BadgeCatalogMapper.toBadgeCellUiModel` calls `BadgeStateCalculator.compute`; entities store booleans/timestamps only |
| 10 | Zero-requirement badges show note with purchase toggle enabled (D-04, D-06) | ✓ VERIFIED | `ZeroRequirementNote` ("Inga kunskapskrav"); `ProgressWriteLogic.isPurchaseEnabled(totalRequirements=0, …)` |
| 11 | Purchase switch disabled until ACHIEVED_TO_BUY or GOTTEN (D-06) | ✓ VERIFIED | `PurchaseToggleRow(enabled=isPurchaseEnabled)`; `ProgressWriteLogic.isPurchaseEnabled` gates on `visualState` |
| 12 | Köpt undo requires confirmation dialog (D-07) | ✓ VERIFIED | `requestClearGotten` → `UncheckPurchaseDialog`; `confirmClearGotten_clearsGottenRetainsAchievedAt` test |
| 13 | Summary counts aggregate both catalogs; gotten excluded (D-15, D-16) | ✓ VERIFIED | `observeAllBadges()` in `HomeViewModel`; `bothCatalogs_summed` and `gotten_excluded` tests |

**Score:** 12/13 truths verified (1 present, behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `app/src/main/java/se/simmarken/ui/badge/BadgeDetailViewModel.kt` | Write orchestration | ✓ VERIFIED | 201 lines; `toggleRequirement`, `setGotten`, `confirmClearGotten` |
| `app/src/main/java/se/simmarken/domain/model/RequirementRowUiModel.kt` | Checklist row model | ✓ VERIFIED | Data class with `id`, `textSv`, `isAchieved` |
| `app/src/main/java/se/simmarken/ui/badge/components/RequirementChecklistRow.kt` | M3 Checkbox row | ✓ VERIFIED | Full-row clickable + Checkbox |
| `app/src/test/java/se/simmarken/ui/badge/BadgeDetailViewModelProgressTest.kt` | ViewModel progress tests | ✓ VERIFIED | 8 tests covering toggle, achieved, gotten, purchase |
| `app/src/main/java/se/simmarken/ui/badge/BadgeDetailScreen.kt` | Full detail screen | ✓ VERIFIED | Pin header, checklist, purchase toggle, loading/error states |
| `app/src/main/java/se/simmarken/ui/badge/components/RequirementChecklist.kt` | Scrollable checklist | ✓ VERIFIED | Maps rows to `RequirementChecklistRow` |
| `app/src/main/java/se/simmarken/ui/badge/components/PurchaseToggleRow.kt` | Purchase switch | ✓ VERIFIED | "Fysiskt märke köpt" + Switch |
| `app/src/main/java/se/simmarken/ui/badge/components/UncheckPurchaseDialog.kt` | Köpt undo dialog | ✓ VERIFIED | AlertDialog with confirm/dismiss |
| `app/src/main/java/se/simmarken/domain/KidProgressSummaryCalculator.kt` | Home count aggregation | ✓ VERIFIED | Counts IN_PROGRESS and ACHIEVED_TO_BUY via `BadgeCatalogMapper` |
| `app/src/main/java/se/simmarken/ui/home/components/ChildCard.kt` | Summary subtitles | ✓ VERIFIED | Conditional pågår/att köpa lines |
| `app/src/main/java/se/simmarken/ui/home/HomeViewModel.kt` | Per-kid summaries | ✓ VERIFIED | `summariesByKidId` combine with `observeAllBadges` |
| `app/src/test/java/se/simmarken/domain/KidProgressSummaryCalculatorTest.kt` | Count rules tests | ✓ VERIFIED | 5 tests |
| `BadgeDetailPlaceholderScreen` | Removed per 05-02 | ✓ VERIFIED | File absent; `SimmarkenNavHost` uses `BadgeDetailScreen` |

### Key Link Verification

| From | To | Via | Status | Details |
|------|-----|-----|--------|---------|
| `BadgeDetailViewModel.toggleRequirement` | `ProgressRepository.upsertRequirementProgress` | `viewModelScope.launch(ioDispatcher)` | ✓ WIRED | Line 128 in ViewModel |
| `BadgeDetailViewModel uiState` | `BadgeStateCalculator.compute` | `BadgeCatalogMapper.toBadgeCellUiModel` | ✓ WIRED | combine block lines 57–63 |
| `RequirementChecklist` | `BadgeDetailViewModel.toggleRequirement` | per-row `onToggle` callback | ✓ WIRED | `BadgeDetailScreen` line 140 |
| `SimmarkenNavHost` BadgeDetail route | `BadgeDetailScreen` | composable replacement | ✓ WIRED | Lines 52–66 |
| `PurchaseToggleRow` | `setGotten` / `requestClearGotten` | `onCheckedChange` | ✓ WIRED | `BadgeDetailScreen` lines 148–154 |
| `HomeViewModel` combine | `KidProgressSummaryCalculator` | per-kid progress + all badges | ✓ WIRED | Lines 72–79 |
| `HomeScreen` `ChildCard` | `summariesByKidId[kid.id]` | `inProgressCount` / `toBuyCount` props | ✓ WIRED | Lines 78–83 |
| `CatalogDao.observeAllBadges` | `HomeViewModel` | `CatalogRepositoryImpl` | ✓ WIRED | DAO query → repository → ViewModel |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|---------------|--------|-------------------|--------|
| `BadgeDetailScreen` | `uiState` | `catalogRepository.observeBadgeById` + `progressRepository.observe*` Flows | Yes — catalog seed + Room progress | ✓ FLOWING |
| `ChildCard` | `inProgressCount`, `toBuyCount` | `KidProgressSummaryCalculator.compute` over all badges per kid | Yes — derived from live progress Flows | ✓ FLOWING |
| `PurchaseToggleRow` | `isGotten`, `isPurchaseEnabled` | `BadgeProgressEntity` + `BadgeStateCalculator` via mapper | Yes — not hardcoded | ✓ FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Full unit test suite | `./gradlew :app:testDebugUnitTest` | BUILD SUCCESSFUL | ✓ PASS |
| BadgeDetailViewModel progress | `--tests BadgeDetailViewModelProgressTest` | BUILD SUCCESSFUL (8 tests) | ✓ PASS |
| Progress write rules | `--tests ProgressWriteLogicTest` | BUILD SUCCESSFUL (5 tests) | ✓ PASS |
| Home summary counts | `--tests KidProgressSummaryCalculatorTest` | BUILD SUCCESSFUL (5 tests) | ✓ PASS |

### Probe Execution

Step 7c: SKIPPED — no probe scripts declared for this UI phase.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| PROG-01 | 05-01, 05-02 | Check/uncheck individual requirements per child | ✓ SATISFIED | `toggleRequirement` + checklist UI + tests |
| PROG-02 | 05-01, 05-02 | Badge auto-achieved when all requirements checked | ✓ SATISFIED | `shouldSetAchievedAt` + `lastRequirement_setsAchievedToBuy` |
| PROG-03 | 05-01, 05-02 | Mark badge physically purchased via toggle | ✓ SATISFIED | `setGotten` / `confirmClearGotten` + `PurchaseToggleRow` |
| PROG-05 | 05-03 | Home card summary counts | ✓ SATISFIED | `KidProgressSummaryCalculator` + `ChildCard` subtitles |
| UI-03 | 05-02 | Badge detail: image, checklist, purchase toggle | ✓ SATISFIED | `BadgeDetailScreen` full layout |

No orphaned requirements mapped to Phase 5 in REQUIREMENTS.md beyond the five above.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| — | — | None in phase-modified sources | — | No blockers |

Scanned phase key-files for `TODO`, `FIXME`, `TBD`, `PLACEHOLDER`, stub returns — none found in `se.simmarken` package.

### Human Verification Required

### 1. Swim-hall detail layout

**Test:** Home → child → badge with requirements → observe detail screen
**Expected:** Large pin image, name, progress subtitle, scrollable checklist, purchase switch
**Why human:** Visual layout and image rendering

### 2. Requirement toggle responsiveness

**Test:** Tap requirement rows rapidly
**Expected:** Immediate checkbox and pin state updates; no per-skill confirmation
**Why human:** Runtime UX feel

### 3. Köpt confirmation dialog

**Test:** Mark köpt, then flip switch off
**Expected:** "Ta bort köpt-markering?" dialog; confirm clears gotten, cancel preserves
**Why human:** Dialog presentation

### 4. GOTTEN skill uncheck

**Test:** On a köpt badge, uncheck one requirement
**Expected:** Gotten clears silently; visual state updates
**Why human:** State transition at runtime

### 5. Zero-requirement badge

**Test:** Open badge with no requirements
**Expected:** "Inga kunskapskrav" note; purchase toggle enabled immediately
**Why human:** Edge-case layout

### 6. Home summary subtitles

**Test:** Progress badges for a child, return home
**Expected:** Separate pågår/att köpa lines when counts > 0; hidden when zero
**Why human:** Visual subtitle rules

### 7. Room persistence round-trip

**Test:** Toggle requirements, force-stop app, relaunch, reopen badge
**Expected:** Progress retained
**Why human:** No instrumented e2e test for ViewModel → Room path

### Gaps Summary

No implementation gaps found — all phase artifacts exist, are substantive, wired, and covered by unit tests at the ViewModel/domain layer. Status is `human_needed` because:

1. One roadmap success criterion (database persistence from toggle) lacks end-to-end behavioral proof.
2. MVP mode requires on-device confirmation of the swim-hall user flow and visual polish.

Automated checks (`./gradlew :app:testDebugUnitTest`) pass. Phase is ready for human UAT before proceeding.

---

_Verified: 2026-07-23T12:30:00Z_
_Verifier: Claude (gsd-verifier)_
