# Requirements: Simmärken Tracker

**Defined:** 2026-07-22
**Core Value:** At the swim hall, a parent can open the app and immediately answer: "Did they pass this badge, and did we buy the physical pin?"

## v1 Requirements

### Child Profiles

- [x] **KIDS-01**: Parent can add a child with a name
- [x] **KIDS-02**: Parent can assign an avatar or color theme to a child
- [x] **KIDS-03**: Parent can view all children as cards on the home screen
- [x] **KIDS-04**: Parent can edit or remove a child profile

### Catalog

- [x] **CATA-01**: App ships with Svensk Simidrott badge catalog pre-loaded (categories, badges, requirements)
- [x] **CATA-02**: App ships with SLS badge catalog pre-loaded (categories, badges, requirements)
- [x] **CATA-03**: Badges are displayed grouped by category within each catalog
- [x] **CATA-04**: Each badge shows its official skill requirements as a checklist
- [x] **CATA-05**: Each badge displays an image or visual identifier

### Progress Tracking

- [ ] **PROG-01**: Parent can check and uncheck individual requirements per child
- [ ] **PROG-02**: Badge automatically becomes Achieved when all requirements are checked
- [ ] **PROG-03**: Parent can mark a badge as physically purchased (Gotten) via a toggle
- [ ] **PROG-04**: Badge visual state reflects locked, in progress, achieved-to-buy, or gotten
- [ ] **PROG-05**: Home screen child card shows summary counts (badges in progress, badges to buy)

### User Interface

- [x] **UI-01**: Home screen uses kid-first navigation with FAB to add a child
- [ ] **UI-02**: Child profile screen shows catalog badges grouped by category in a grid
- [ ] **UI-03**: Badge detail screen shows badge image, requirement checklist, and purchase toggle
- [ ] **UI-04**: Four visual badge states are clearly distinguishable (grayscale, partial progress, cart overlay, checkmark/star)

### Data & Offline

- [ ] **DATA-01**: All data persists locally via Room with no network dependency
- [ ] **DATA-02**: Parent can export all progress data to a file
- [ ] **DATA-03**: Parent can import progress data from an exported file

### Internationalization

- [ ] **I18N-01**: App UI is available in Swedish
- [ ] **I18N-02**: App UI is available in English
- [ ] **I18N-03**: Parent can switch language in app settings

## v2 Requirements

### Sharing

- **SHAR-01**: Parent can export a child's progress as formatted text for sharing
- **SHAR-02**: Parent can export a child's progress as an image for sharing

### Catalogs

- **CATA-06**: Parent can add or switch to custom swim club badge catalogs

## Out of Scope

| Feature | Reason |
|---------|--------|
| Cloud sync / user accounts | 100% offline by design; JSON export/import handles migration |
| Real-time multi-parent sync | Requires backend; manual export is v1 approach |
| Swim instructor mode | Different user persona; parent-only v1 |
| Social features | Not core to swim-hall use case |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| DATA-01 | Phase 1 | Pending |
| CATA-01 | Phase 2 | Complete |
| CATA-02 | Phase 2 | Complete |
| CATA-03 | Phase 2 | Complete |
| CATA-04 | Phase 2 | Complete |
| CATA-05 | Phase 2 | Complete |
| KIDS-01 | Phase 3 | Complete |
| KIDS-02 | Phase 3 | Complete |
| KIDS-03 | Phase 3 | Complete |
| KIDS-04 | Phase 3 | Complete |
| UI-01 | Phase 3 | Complete |
| UI-02 | Phase 4 | Pending |
| UI-04 | Phase 4 | Pending |
| PROG-04 | Phase 4 | Pending |
| UI-03 | Phase 5 | Pending |
| PROG-01 | Phase 5 | Pending |
| PROG-02 | Phase 5 | Pending |
| PROG-03 | Phase 5 | Pending |
| PROG-05 | Phase 5 | Pending |
| DATA-02 | Phase 6 | Pending |
| DATA-03 | Phase 6 | Pending |
| I18N-01 | Phase 6 | Pending |
| I18N-02 | Phase 6 | Pending |
| I18N-03 | Phase 6 | Pending |

**Coverage:**

- v1 requirements: 24 total
- Mapped to phases: 24
- Unmapped: 0 ✓

---
*Requirements defined: 2026-07-22*
*Last updated: 2026-07-22 after roadmap creation*
