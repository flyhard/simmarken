---
phase: 06
slug: export-import-i18n
status: verified
threats_open: 0
asvs_level: 1
created: 2026-07-25
---

# Phase 06 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| App ↔ External file | Untrusted JSON via document picker, ACTION_VIEW, or ACTION_SEND | Backup JSON (kid names, progress rows) |
| App ↔ Share sheet | FileProvider URI grant for export | Versioned backup JSON in cache/exports |
| App ↔ Device storage | DataStore language preference | LanguageMode enum (device-only, excluded from backup) |
| App ↔ Room DB | Import merge writes after user confirm | Kid entities, requirement/badge progress |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-06-01 | Tampering | BackupValidator / ExportRepository | high | mitigate | kotlinx-serialization decode + exportVersion gate; transactional merge | closed |
| T-06-02 | Tampering | MainActivity / SettingsViewModel | high | mitigate | Intent stashes URI only; sole write path via confirmImport → merge | closed |
| T-06-03 | Information Disclosure | FileProvider | medium | mitigate | cache-path exports/ only; provider not exported; FLAG_GRANT_READ_URI_PERMISSION | closed |
| T-06-04 | Denial of Service | MergePlanner / BackupValidator | medium | mitigate | Skip unknown catalog codes; reject unsupported version; no merge on invalid parse | closed |
| T-06-05 | Elevation | SettingsViewModel | medium | mitigate | contentResolver.openInputStream(uri) only; no untrusted path concatenation | closed |
| T-06-06 | Spoofing | ImportErrorDialog | low | mitigate | InvalidReason mapped to @StringRes; no raw exception text in UI | closed |
| T-06-07 | Information Disclosure | SettingsDataSection | low | accept | Intentional PII hint copy in settings_import_hint (see Accepted Risks) | closed |
| T-06-08 | Tampering | BackupDto | low | mitigate | Language preference excluded from backup schema (D-21) | closed |
| T-06-09 | Spoofing | LocalePreferencesRepository | low | accept | Limited setApplicationLocales callers (see Accepted Risks) | closed |
| T-06-10 | Tampering | MergePlanner / ExportRepository | high | mitigate | New kids default unchecked; merge filters by acceptedNewKidStableIds | closed |
| T-06-11 | Information Disclosure | ExportRepository | medium | mitigate | Export omits catalog seed; progress codes only (D-05) | closed |
| T-06-12 | Information Disclosure | BackupDto | medium | mitigate | Language preference absent from backup encode path (D-21) | closed |
| T-06-13 | Tampering | AndroidManifest intent filters | medium | mitigate | Broad MIME accepted but BackupValidator gates all import paths before preview/write | closed |
| T-06-14 | Tampering | ExportRepository.merge | high | mitigate | Upserts only; no kid DELETE; existing profile preserved on merge (D-01) | closed |
| T-06-SC | Tampering | Gradle dependencies | high | mitigate | Official androidx.datastore and androidx.appcompat only | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-06-01 | T-06-07 | `settings_import_hint` intentionally references child data in UX copy; no technical leak beyond parent's own device context | gsd-security-auditor | 2026-07-25 |
| AR-06-02 | T-06-09 | `setApplicationLocales` limited to SettingsViewModel and SimmarkenApplication; no external caller surface | gsd-security-auditor | 2026-07-25 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-07-25 | 15 | 15 | 0 | gsd-security-auditor (retroactive-STRIDE, ASVS L1) |

### Security Audit 2026-07-25

| Metric | Count |
|--------|-------|
| Threats found | 15 |
| Closed | 15 |
| Open (blocking) | 0 |
| Open (non-blocking, accepted) | 2 |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-07-25
