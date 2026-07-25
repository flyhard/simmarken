---
phase: 07
slug: security-git-hygiene
status: verified
threats_open: 0
asvs_level: 1
created: 2026-07-25
---

# Phase 7 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Developer workstation → git index | Untrusted `git add` of signing/credential files before Phase 8 | Keystores, Play JSON, service accounts |
| Gitleaks allowlist → scan results | Over-broad allowlist could hide real secrets | Secret scan findings |
| GitHub Actions runner → git clone | Must receive full history, not shallow tip | Full git history |
| gitleaks-action → GitHub API | GITHUB_TOKEN scoped to repo; no extra secrets in workflow YAML | Repo metadata, PR comments |
| Maintainer review → public release | Human attestation that automation missed nothing material | Release-readiness evidence |
| Export JSON → parent device share | Child name is expected PII; unexpected fields would leak on share | Backup/migration JSON |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-07-01 | Information Disclosure | `.gitignore` | high | mitigate | D-13 credential patterns + `git check-ignore -v` verification (Plan 07-01 Task 2) | closed |
| T-07-02 | Information Disclosure | `.gitleaks.toml` allowlist | medium | mitigate | Single `local.properties` path regex only per D-10 | closed |
| T-07-03 | Information Disclosure | git history | critical | mitigate | Full-history `gitleaks detect`; zero credential files in history (Plan 07-01) | closed |
| T-07-04 | Information Disclosure | `gitleaks.yml` shallow checkout | critical | mitigate | `fetch-depth: 0` on `actions/checkout@v6` per D-11 | closed |
| T-07-05 | Tampering | CI workflow bypass | medium | mitigate | Workflow on all `push`/`pull_request`; fails on any finding per D-09 | closed |
| T-07-SC | Tampering | `gitleaks/gitleaks-action@v3` | high | mitigate | Pin `@v3`; green CI run confirmed (run 30150423510) | closed |
| T-07-06 | Information Disclosure | Export JSON | medium | mitigate | Item 5 BackupDto field review in `docs/SECURITY-CHECKLIST.md` per D-07 | closed |
| T-07-07 | Information Disclosure | Service account JSON | high | mitigate | Item 3 git history audit + SECU-03 `.gitignore` patterns | closed |
| T-07-08 | Repudiation | Checklist sign-off | low | mitigate | D-06 name+date in-file; evidence column per item (ues201, 2026-07-25) | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above `workflow.security_block_on` (high) count toward `threats_open`*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

No accepted risks.

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-07-25 | 9 | 9 | 0 | gsd-secure-phase (L1 verification) |

### Security Audit 2026-07-25

| Metric | Count |
|--------|-------|
| Threats found | 9 |
| Closed | 9 |
| Open | 0 |

**L1 evidence summary:**
- `.gitignore` SECU-03 block covers all five D-13 test paths (`git check-ignore -v` pass)
- `.gitleaks.toml` has `[extend] useDefault = true` and exactly one `[[allowlists]]` entry for `local.properties`
- Credential paths absent from full git history (0 commits)
- `.github/workflows/gitleaks.yml`: `fetch-depth: 0`, `gitleaks/gitleaks-action@v3`, triggers on `push` and `pull_request`
- `docs/SECURITY-CHECKLIST.md`: all five D-07 items evidenced and signed (ues201, 2026-07-25)
- `BackupDto.kt` field inventory matches documented PII scope (no unexpected PII fields)

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-07-25
