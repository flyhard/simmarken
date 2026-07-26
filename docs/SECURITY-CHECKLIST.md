# Pre-Public Security Checklist

**Phase:** 7 — Security & Git Hygiene (`SECU-02`)  
**Gate:** Maintainer self-sign per D-06; completed in Phase 7 per D-08 (not deferred to Phase 12)  
**Date basis:** Evidence from Plans 07-01 and 07-02 (2026-07-25)

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | Gitleaks clean on full git history | [x] | **Local (Plan 07-01):** `gitleaks version: 8.30.1`; `gitleaks detect --source . --config .gitleaks.toml --verbose` — 172 commits scanned (~1.93 MB) in 352ms; `no leaks found` (exit 0). **CI (Plan 07-02):** https://github.com/flyhard/simmm-rken/actions/runs/30150423510 — conclusion `success`, `fetch-depth: 0`, `no leaks found`. |
| 2 | `.gitignore` covers signing/credential files | [x] | `git check-ignore -v upload.jks keystore.properties keystore-fingerprint.md play-credentials.json service-account.json local.properties`: `.gitignore:3:/local.properties` → `local.properties`; `.gitignore:14:*.jks` → `upload.jks`; `.gitignore:16:keystore.properties` → `keystore.properties`; `.gitignore:17:keystore-fingerprint.md` → `keystore-fingerprint.md`; `.gitignore:18:**/play-credentials.json` → `play-credentials.json`; `.gitignore:19:**/service-account*.json` → `service-account.json`. SECU-03 block also covers `*.keystore`, `*.p12`, `google-services.json`. Phase 8 captures SHA-256 locally in `keystore-fingerprint.md`; Play Console registration is Phase 10 (D-16). |
| 3 | No service account JSON in repo or history | [x] | `git log --all --oneline -- '**/service-account*.json'` — empty (0 lines). Broader credential audit (Plan 07-01): `git log --all --oneline -- '*.jks' '*.keystore' 'keystore.properties' '**/play-credentials.json' '**/service-account*.json' 'local.properties'` — empty (0 lines). |
| 4 | Badge asset licensing documented | [x] | Reviewed `docs/SOURCES.md`: **Svensk Simidrott → Licensing (promotional use)** — simmärkesmaterial may be downloaded and used for marketing/promotion of simmärken; requirement text from official protocols/affisch; pin artwork from official shop product photos for parent identification only. **SLS → Confidence notes** — requirement text from official shop product descriptions at MEDIUM confidence; GP article excluded as non-authoritative. **Images (Bilder)** — both catalogs sourced from official shop product photos with documented extraction scripts and paths. |
| 5 | Export JSON contains no unexpected PII | [x] | Reviewed `BackupDto.kt` field inventory: `BackupDto` (`exportVersion`, `exportedAtEpochMillis`, `kids`); `KidBackupDto` (`stableId`, `name`, `avatarColorArgb`, `createdAtEpochMillis`, `sortOrder`, progress lists); `RequirementProgressBackupDto` (catalog/badge/requirement codes, achievement flags, timestamps); `BadgeProgressBackupDto` (catalog/badge codes, gotten flags, timestamps). No email, phone, address, device IDs, or location fields. Catalog seed data and language preference explicitly excluded per D-05/D-21. |

**Notes**

- Item 1 requires both local `gitleaks detect` output (Plan 07-01) and a green GitHub Actions run URL (Plan 07-02).
- Item 5 PII scope is defined in `BackupDto.kt` and the section below — export excludes catalog seed data and language preference per D-05/D-21.

## Export JSON PII Scope (item 5)

Export schema: `app/src/main/java/se/simmarken/data/export/BackupDto.kt` (version 1 backup JSON).

**Expected fields**

| DTO | Fields |
|-----|--------|
| `BackupDto` | `exportVersion`, `exportedAtEpochMillis`, `kids` |
| `KidBackupDto` | `stableId` (UUID), `name`, `avatarColorArgb`, `createdAtEpochMillis`, `sortOrder`, `requirementProgress`, `badgeProgress` |
| `RequirementProgressBackupDto` | `catalogCode`, `badgeCode`, `requirementCode`, `isAchieved`, `achievedAtEpochMillis`, `updatedAtEpochMillis` |
| `BadgeProgressBackupDto` | `catalogCode`, `badgeCode`, `isGotten`, `achievedAtEpochMillis`, `gottenAtEpochMillis`, `updatedAtEpochMillis` |

**Explicitly excluded from export payload**

- Email, phone, address
- Device identifiers
- Location data
- Catalog seed data (requirements, badge metadata)
- Language preference

Child display `name` and UUID `stableId` are expected PII for parent-managed backup/migration — no additional personal fields are present in the DTO family.

## Sign-Off

- [x] All items verified
- Signed: ues201 Date: 2026-07-25
