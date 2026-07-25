---
phase: 04
slug: catalog-view-visual-states
status: verified
threats_open: 0
asvs_level: 1
created: 2026-07-25
---

# Phase 04 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Nav route → ViewModel | Untrusted `kidId` / `badgeId` route params must not crash or leak cross-kid data | Long route arguments |
| ViewModel → Room | Progress reads scoped per kid; catalog reads scoped per catalog | Progress + catalog entities |
| Seed data → Coil | Bundled `imageAssetPath` values loaded from `android_asset` only | WebP asset URIs |
| Tab selection → catalog query | Tab index maps to seeded catalog codes, not free text | Catalog ID selection |
| Progress maps → BadgeStateCalculator | Sparse progress rows default to not achieved | Requirement/badge progress maps |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-04-01 | Tampering | BadgeCatalogMapper | medium | mitigate | `requirementProgressById[id] == true`; absent keys default false (`BadgeCatalogMapperTest`) | closed |
| T-04-02 | Tampering | CatalogDao | low | mitigate | Parameterized `@Query` with `Long` params only; no dynamic SQL | closed |
| T-04-03 | Spoofing | ChildCatalogViewModel / Screen | medium | mitigate | `kidMissing` when `observeById` emits null; centered error UI | closed |
| T-04-04 | Tampering | CatalogTabRow | low | mitigate | Tab index maps to seeded codes `simidrott`/`sls`, not free text | closed |
| T-04-05 | Tampering | BadgePinVisual / Coil | low | accept | `file:///android_asset/$imageAssetPath` only; no network Coil module; seed paths trusted | closed |
| T-04-06 | Spoofing | BadgeDetailViewModel / Screen | medium | mitigate | `badgeMissing` when badge not found; error UI | closed |
| T-04-07 | Denial of Service | BadgePinVisual / Coil | low | accept | `coilSizePx` 96 (grid) / 320 (detail) decode size limits | closed |
| T-04-08 | Information Disclosure | ChildCatalogViewModel | medium | mitigate | `flatMapLatest` on `selectedCatalogId`; `observeForKid(kidId)` scopes progress | closed |
| T-04-09 | Spoofing | BadgeDetail route | low | accept | No `kidId` validation on detail route; normal nav passes consistent kidId; no deep-link surface | closed |
| T-04-SC | Tampering | Gradle dependencies | high | accept | 04-01/04-02 added no packages; 04-03 Coil 3.5.0 vetted in Package Legitimacy Audit | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-04-SC | T-04-SC | Plans 04-01/04-02 added no third-party packages; 04-03 Coil 3.5.0 approved in 04-RESEARCH Package Legitimacy Audit with version pin in `libs.versions.toml` | gsd-security-auditor | 2026-07-25 |
| AR-04-05 | T-04-05 | `imageAssetPath` values originate from bundled seed JSON only; Coil loads `android_asset` URIs with no remote fetch module | gsd-security-auditor | 2026-07-25 |
| AR-04-07 | T-04-07 | Coil decode size capped at 96px (grid) / 320px (detail); acceptable residual DoS risk for local-only assets | gsd-security-auditor | 2026-07-25 |
| AR-04-09 | T-04-09 | `BadgeDetail(kidId, badgeId)` does not re-validate `kidId`; app has no exported deep links and catalog navigation always passes matching kidId | gsd-security-auditor | 2026-07-25 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-07-25 | 10 | 10 | 0 | gsd-security-auditor (retroactive-STRIDE, ASVS L1) |

### Security Audit 2026-07-25

| Metric | Count |
|--------|-------|
| Threats found | 10 |
| Closed | 10 |
| Open (blocking) | 0 |
| Open (non-blocking, accepted) | 4 |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-07-25
