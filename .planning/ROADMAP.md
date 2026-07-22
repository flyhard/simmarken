# Roadmap: Simmärken Tracker

## Overview

Build a local-first Android app that lets parents track Swedish swimming badge progress for their children. Six vertical MVP phases deliver an increasingly complete swim-hall companion: from database foundation through catalog seeding, child profiles, visual badge browsing, progress tracking, and finally export/import with bilingual polish.

## Phases

- [x] **Phase 1: Android Foundation & Database** — Project skeleton, Room schema, offline persistence (completed 2026-07-22)
- [ ] **Phase 2: Catalog Seeding** — Svensk Simidrott + SLS badge data loaded from official sources
- [ ] **Phase 3: Child Profiles & Home** — Kid-first navigation with add/edit children
- [ ] **Phase 4: Catalog View & Visual States** — Category-grouped badge grid with 4-tier visuals
- [ ] **Phase 5: Progress Tracking & Badge Detail** — Skill checklist, auto-achieve, purchase toggle
- [ ] **Phase 6: Export/Import & i18n** — JSON backup and Swedish/English UI

## Phase Details

### Phase 1: Android Foundation & Database

**Goal**: Runnable Android app with Room database schema matching the relational catalog model
**Mode:** mvp
**Depends on**: Nothing (first phase)
**Requirements**: DATA-01
**UI hint**: no
**Success Criteria** (what must be TRUE):

  1. Android project builds and launches on emulator/device
  2. Room database contains Catalog, Category, Badge, Requirement, Kid, and Progress entities with correct relationships
  3. Database persists data across app restarts

**Plans**: 3 plans
Plans:
**Wave 1**

- [x] 01-01-PLAN.md — Gradle scaffold, Compose NavHost shell, Wave 0 test stubs

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 01-02-PLAN.md — Room entities (D-01–D-05), DAOs, schema export, DaoInstrumentedTest

**Wave 3** *(blocked on Wave 2 completion)*

- [x] 01-03-PLAN.md — AppContainer + repositories, Home kid-insert slice, persistence test

### Phase 2: Catalog Seeding

**Goal**: Both Svensk Simidrott and SLS badge catalogs are pre-loaded with categories, badges, requirements, and images
**Mode:** mvp
**Depends on**: Phase 1
**Requirements**: CATA-01, CATA-02, CATA-03, CATA-04, CATA-05
**UI hint**: no
**Success Criteria** (what must be TRUE):

  1. App launches with both catalogs populated in Room on first run
  2. Badge requirements match official sources for sampled badges (Baddaren, Hajen, SLS basics)
  3. Each badge has a visual identifier (image or color-coded placeholder)
  4. Catalog version metadata is stored for future updates

**Plans**: 4 plans

Plans:

**Wave 1** *(parallel — 02-01 and 02-02)*

- [x] 02-01-PLAN.md — Extract Svensk Simidrott catalog from official PDFs into simidrott.json
- [x] 02-02-PLAN.md — Extract curated SLS catalog from official sources into sls.json

**Wave 2** *(02-04 after 02-02 — needs SLS-SOURCE-NOTES.md; does not edit seed JSON)*

- [x] 02-04-PLAN.md — Bundle WebP badge images, tier fallbacks, and SOURCES.md licensing

**Wave 3** *(blocked on Waves 1–2 completion)*

- [ ] 02-03-PLAN.md — Seed DTOs, version-gated merge loader, DAO upserts, startup wiring

### Phase 3: Child Profiles & Home

**Goal**: Parent can manage children and see them on a kid-first home screen
**Mode:** mvp
**Depends on**: Phase 2
**Requirements**: KIDS-01, KIDS-02, KIDS-03, KIDS-04, UI-01
**UI hint**: yes
**Success Criteria** (what must be TRUE):

  1. Parent can add a child with name and avatar/color via FAB
  2. Home screen displays a card for each child
  3. Parent can tap a child card to navigate to their catalog view
  4. Parent can edit or delete a child profile

**Plans**: 3 plans

Plans:

- [ ] 03-01: Kid entity CRUD repository and ViewModel
- [ ] 03-02: Home screen with child cards and FAB
- [ ] 03-03: Add/edit child dialog with avatar/color picker

### Phase 4: Catalog View & Visual States

**Goal**: Parent sees badges grouped by category with clear 4-tier visual states
**Mode:** mvp
**Depends on**: Phase 3
**Requirements**: UI-02, UI-04, PROG-04
**UI hint**: yes
**Success Criteria** (what must be TRUE):

  1. Child profile shows badges grouped by category (Vattenvana, Nybörjare, etc.)
  2. Locked badges appear grayscale; in-progress badges show partial coloring or progress ring
  3. Achieved-to-buy badges show full color with cart/pin overlay
  4. Gotten badges show full color with star/checkmark overlay
  5. Tapping a badge navigates to detail screen (placeholder OK until Phase 5)

**Plans**: 3 plans

Plans:

- [ ] 04-01: BadgeStateCalculator domain logic with unit tests
- [ ] 04-02: Category-grouped badge grid Composable
- [ ] 04-03: Badge card component with 4-tier visual overlays

### Phase 5: Progress Tracking & Badge Detail

**Goal**: Parent can check off skills and mark badges as purchased — the core swim-hall flow
**Mode:** mvp
**Depends on**: Phase 4
**Requirements**: UI-03, PROG-01, PROG-02, PROG-03, PROG-05
**UI hint**: yes
**Success Criteria** (what must be TRUE):

  1. Badge detail shows high-res image and checklist of official requirements
  2. Tapping a requirement instantly toggles it and persists to database
  3. When all requirements are checked, badge state updates to Achieved-to-buy automatically
  4. Purchase toggle marks badge as Gotten independently of skill completion
  5. Home screen child cards show "X badges in progress" and "Y badges to buy" summaries

**Plans**: 3 plans

Plans:

- [ ] 05-01: Progress repository (requirement toggle, badge purchase flag)
- [ ] 05-02: Badge detail screen with checklist and purchase toggle
- [ ] 05-03: Home screen summary counts derived from progress state

### Phase 6: Export/Import & i18n

**Goal**: Parents can back up progress and use the app in Swedish or English
**Mode:** mvp
**Depends on**: Phase 5
**Requirements**: DATA-02, DATA-03, I18N-01, I18N-02, I18N-03
**UI hint**: yes
**Success Criteria** (what must be TRUE):

  1. Parent can export all kids and progress to a JSON file via share sheet
  2. Parent can import a previously exported file and restore all data
  3. Import shows clear error for invalid or incompatible files
  4. All UI strings available in Swedish and English
  5. Parent can switch language in settings; preference persists

**Plans**: 3 plans

Plans:

- [ ] 06-01: Versioned JSON export/import with validation
- [ ] 06-02: Swedish and English string resources for all screens
- [ ] 06-03: Settings screen with language picker

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Android Foundation & Database | 3/3 | Complete   | 2026-07-22 |
| 2. Catalog Seeding | 3/4 | In Progress|  |
| 3. Child Profiles & Home | 0/3 | Not started | - |
| 4. Catalog View & Visual States | 0/3 | Not started | - |
| 5. Progress Tracking & Badge Detail | 0/3 | Not started | - |
| 6. Export/Import & i18n | 0/3 | Not started | - |
