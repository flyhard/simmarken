---
phase: 7
slug: security-git-hygiene
status: validated
nyquist_compliant: true
wave_0_complete: true
created: 2026-07-25
revised: 2026-07-25
---

# Phase 7 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Shell smoke tests (`scripts/verify-phase-07.sh`) — infrastructure/config phase |
| **Config file** | `.gitleaks.toml` (Plan 07-01) |
| **Quick run command** | `bash scripts/verify-phase-07.sh` |
| **Full suite command** | `bash scripts/verify-phase-07.sh` (all automated checks) |
| **Estimated runtime** | ~30s local (includes gitleaks scan + `gh` API for main-branch CI) |

---

## Sampling Rate

- **After every task commit:** Run `gitleaks detect --source . --config .gitleaks.toml`
- **After every plan wave:** Run `bash scripts/verify-phase-07.sh --check 07-01-02`
- **Before `/gsd-verify-work`:** `bash scripts/verify-phase-07.sh` (all automated checks green)
- **Max feedback latency:** 60 seconds (all automated checks are local/API — no `gh run watch`)

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 07-01-01 | 01 | 1 | SECU-01, SECU-03 | T-07-02, T-07-03 | Full-history Gitleaks zero findings with `.gitleaks.toml` | integration (CLI) | `bash scripts/verify-phase-07.sh --check 07-01-01` | ✅ W0 | ✅ green |
| 07-01-02 | 01 | 1 | SECU-03 | T-07-01 | Credential paths gitignored; no credential files in history | smoke | `bash scripts/verify-phase-07.sh --check 07-01-02` | ✅ W0 | ✅ green |
| 07-01-03 | 01 | 1 | SECU-01 | T-07-03 | History remediation gate if secrets found | manual (checkpoint) | Skipped — scan clean per 07-01-SUMMARY | — | ✅ green |
| 07-02-01 | 02 | 2 | SECU-01 | T-07-04, T-07-05 | Gitleaks workflow YAML valid; push/PR triggers; full-history checkout | config | `bash scripts/verify-phase-07.sh --check 07-02-01` | ✅ W0 | ✅ green |
| 07-02-02 | 02 | 2 | SECU-01 | T-07-04 | CI Gitleaks green on main branch (latest run) | CI | `bash scripts/verify-phase-07.sh --check 07-02-02` | ✅ W0 | ✅ green |
| 07-02-03 | 02 | 2 | SECU-01 | T-07-SC | Maintainer confirms green CI run in GitHub UI | manual (checkpoint) | Confirmed run 30150423510 per 07-02-SUMMARY | — | ✅ green |
| 07-03-01 | 03 | 3 | SECU-02 | T-07-06 | Checklist scaffold with five D-07 items and PII scope | smoke | `bash scripts/verify-phase-07.sh --check 07-03-01` | ✅ W0 | ✅ green |
| 07-03-02 | 03 | 3 | SECU-02 | T-07-06, T-07-07 | All five items evidenced (local scan + CI URL from 07-02) | smoke | `bash scripts/verify-phase-07.sh --check 07-03-02` | ✅ W0 | ✅ green |
| 07-03-03 | 03 | 3 | SECU-02 | T-07-08 | Maintainer sign-off with name + date (D-06/D-08) | manual (checkpoint) | Signed ues201 2026-07-25 per 07-03-SUMMARY | — | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

### Nyquist gap fixes (2026-07-25)

| Task ID | Issue | Fix |
|---------|-------|-----|
| 07-01-02 | `wc -l` padded whitespace broke `grep -q '^0$'` | Strip whitespace: `wc -l \| tr -d '[:space:]'` then compare to `0` |
| 07-02-02 | Global latest run failed on UAT negative-path branch | Scope to main: `gh run list --workflow=gitleaks.yml --branch main --limit 1` |

---

## Wave 0 Requirements

- [x] `.gitleaks.toml` — Gitleaks config with `local.properties` allowlist (Plan 07-01)
- [x] `.gitignore` — SECU-03 credential patterns (Plan 07-01)
- [x] `.github/workflows/gitleaks.yml` — SECU-01 CI workflow (Plan 07-02)
- [x] `docs/SECURITY-CHECKLIST.md` — SECU-02 checklist with sign-off (Plan 07-03)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| History remediation decision | SECU-01 | Irreversible ops per D-01/D-02 | Skipped — gitleaks exit 0 and history audit empty (07-01-03) |
| Gitleaks CI visual confirmation | SECU-01 | Human gate on first CI run | Confirmed run 30150423510 with `fetch-depth: 0` (07-02-03) |
| Security checklist maintainer sign-off | SECU-02 | Human gate per D-06 | Signed ues201 2026-07-25 in checklist (07-03-03) |
| Export JSON PII review | SECU-02 | Schema review requires human judgment | Verified `BackupDto` contains only child name + UUID (item 5) |
| Badge asset licensing | SECU-02 | Cross-ref documentation | Confirmed `docs/SOURCES.md` documents Simidrott/SLS provenance (item 4) |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 / checkpoint dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify (checkpoints exempt)
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags (`gh run watch` removed from 07-02-02 — main-branch API check only)
- [x] Feedback latency < 60s for all automated checks
- [x] `nyquist_compliant: true` set in frontmatter
- [x] Nyquist gaps 07-01-02 and 07-02-02 resolved via `scripts/verify-phase-07.sh`

**Approval:** 2026-07-25 — all automated checks green; manual checkpoints complete per plan SUMMARYs

## Validation Audit 2026-07-25

| Metric | Count |
|--------|-------|
| Gaps found | 2 |
| Resolved | 2 |
| Escalated | 0 |
