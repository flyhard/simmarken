---
phase: 05
slug: progress-tracking-badge-detail
status: verified
threats_open: 0
asvs_level: 1
created: 2026-07-24
---

# Phase 05 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| ViewModel → ProgressRepository | Untrusted UI events must not corrupt achievedAt write-once or skip gotten auto-clear | Badge/requirement progress writes |
| Progress maps → BadgeStateCalculator | Sparse progress rows must default to not achieved | Progress entity maps |
| Purchase switch → ViewModel | User cannot mark köpt when skills incomplete | isPurchaseEnabled + setGotten |
| Dialog dismiss → switch state | Dismiss must not leave DB and UI inconsistent | confirmClearGotten / dismissClearGotten |
| Home aggregate → grid/detail | Summary counts must match per-badge BadgeStateCalculator output | KidProgressSummary |
| Multi-kid progress flows | Kid A progress must not leak into kid B counts | kidId-scoped observe calls |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-05-01 | Tampering | BadgeDetailViewModel.toggleRequirement | high | mitigate | `ProgressWriteLogic.preserveAchievedAt` + `ProgressWriteLogicTest` / `BadgeDetailViewModelProgressTest` | closed |
| T-05-02 | Tampering | setGotten / confirmClearGotten | medium | mitigate | Separate `isGotten` from requirement `isAchieved`; ViewModelProgressTest covers both upsert shapes | closed |
| T-05-03 | Tampering | GOTTEN auto-clear on uncheck | medium | mitigate | D-10 path in `toggleRequirement` covered by ViewModelProgressTest | closed |
| T-05-04 | Spoofing | PurchaseToggleRow enabled state | medium | mitigate | `isPurchaseEnabled` from ViewModel via `ProgressWriteLogic.isPurchaseEnabled` | closed |
| T-05-05 | Tampering | UncheckPurchaseDialog | low | mitigate | `confirmClearGotten` only on confirm; dismiss leaves DB unchanged | closed |
| T-05-06 | Tampering | KidProgressSummaryCalculator | medium | mitigate | Reuses BadgeStateCalculator; `KidProgressSummaryCalculatorTest` | closed |
| T-05-07 | Information Disclosure | HomeViewModel combine | low | mitigate | Progress observe calls scoped per `kidId` | closed |
| T-05-SC | Tampering | npm/pip/cargo installs | high | accept | No new packages beyond kotlinx-coroutines-test for JVM tests | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-05-SC | T-05-SC | No new third-party packages in phase 5; M3 Checkbox/Switch and existing test libs only | plan accept | 2026-07-24 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-07-24 | 8 | 8 | 0 | gsd-verify-work / ASVS L1 |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-07-24
