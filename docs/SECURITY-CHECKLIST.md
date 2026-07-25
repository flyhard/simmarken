# Pre-Public Security Checklist

**Phase:** 7 — Security & Git Hygiene (`SECU-02`)  
**Gate:** Maintainer self-sign per D-06; completed in Phase 7 per D-08 (not deferred to Phase 12)  
**Date basis:** Evidence from Plans 07-01 and 07-02 (2026-07-25)

| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | Gitleaks clean on full git history | [ ] | TBD — fill in Task 2 |
| 2 | `.gitignore` covers signing/credential files | [ ] | TBD — fill in Task 2 |
| 3 | No service account JSON in repo or history | [ ] | TBD — fill in Task 2 |
| 4 | Badge asset licensing documented | [ ] | TBD — fill in Task 2 |
| 5 | Export JSON contains no unexpected PII | [ ] | TBD — fill in Task 2 |

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

- [ ] All items verified
- Signed: _________________ Date: _________
