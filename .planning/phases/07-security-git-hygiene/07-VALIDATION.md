---
phase: 7
slug: security-git-hygiene
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2026-07-25
revised: 2026-07-25
---

# Phase 7 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | No phase-specific unit tests — infrastructure/config phase |
| **Config file** | `.gitleaks.toml` (Plan 07-01) |
| **Quick run command** | `gitleaks detect --source . --config .gitleaks.toml` |
| **Full suite command** | Local scan + GitHub Actions `gitleaks.yml` on push/PR |
| **Estimated runtime** | ~30s local; CI task (07-02-02) up to 10 min with `gh run watch` |

---

## Sampling Rate

- **After every task commit:** Run `gitleaks detect --source . --config .gitleaks.toml`
- **After every plan wave:** Run `git check-ignore` smoke + local full-history scan
- **Before `/gsd-verify-work`:** CI workflow green on pushed branch + checklist signed
- **Max feedback latency:** 60 seconds (exception: 07-02-02 CI wait via `gh run watch` — bounded to 10 min per plan precondition)

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 07-01-01 | 01 | 1 | SECU-01, SECU-03 | T-07-02, T-07-03 | Full-history Gitleaks zero findings with `.gitleaks.toml` | integration (CLI) | `gitleaks detect --source . --config .gitleaks.toml --verbose` | ❌ W0 | ⬜ pending |
| 07-01-02 | 01 | 1 | SECU-03 | T-07-01 | Credential paths gitignored; no credential files in history | smoke | `git check-ignore -v upload.jks keystore.properties play-credentials.json service-account.json local.properties && git log --all --oneline -- '*.jks' '*.keystore' 'keystore.properties' '**/play-credentials.json' '**/service-account*.json' 'local.properties' \| wc -l \| grep -q '^0$'` | ❌ W0 | ⬜ pending |
| 07-01-03 | 01 | 1 | SECU-01 | T-07-03 | History remediation gate if secrets found | manual (checkpoint) | Remediation decision checkpoint — re-run 07-01-01 after resolution | — | ⬜ pending |
| 07-02-01 | 02 | 2 | SECU-01 | T-07-04, T-07-05 | Gitleaks workflow YAML valid; push/PR triggers; full-history checkout | config | `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/gitleaks.yml'))" && grep -q 'fetch-depth: 0' .github/workflows/gitleaks.yml && grep -q 'gitleaks/gitleaks-action@v3' .github/workflows/gitleaks.yml && grep -q 'pull_request:' .github/workflows/gitleaks.yml && grep -q 'push:' .github/workflows/gitleaks.yml` | ❌ W0 | ⬜ pending |
| 07-02-02 | 02 | 2 | SECU-01 | T-07-04 | CI Gitleaks green on full history after push | CI | `gh run watch --workflow=gitleaks.yml --exit-status && gh run list --workflow=gitleaks.yml --limit 1 --json conclusion -q '.[0].conclusion' \| grep -q success` | ❌ W0 | ⬜ pending |
| 07-02-03 | 02 | 2 | SECU-01 | T-07-SC | Maintainer confirms green CI run in GitHub UI | manual (checkpoint) | Human-verify: Actions → gitleaks → green run, `fetch-depth: 0` in logs | — | ⬜ pending |
| 07-03-01 | 03 | 3 | SECU-02 | T-07-06 | Checklist scaffold with five D-07 items and PII scope | smoke | `test -f docs/SECURITY-CHECKLIST.md && grep -q 'Pre-Public Security Checklist' docs/SECURITY-CHECKLIST.md && grep -q 'Sign-Off' docs/SECURITY-CHECKLIST.md && grep -q 'BackupDto' docs/SECURITY-CHECKLIST.md` | ❌ W0 | ⬜ pending |
| 07-03-02 | 03 | 3 | SECU-02 | T-07-06, T-07-07 | All five items evidenced (local scan + CI URL from 07-02) | smoke | `grep -c '\[x\]' docs/SECURITY-CHECKLIST.md \| awk '{exit ($1 >= 5) ? 0 : 1}'` | ❌ W0 | ⬜ pending |
| 07-03-03 | 03 | 3 | SECU-02 | T-07-08 | Maintainer sign-off with name + date (D-06/D-08) | manual (checkpoint) | Human-verify: sign `docs/SECURITY-CHECKLIST.md` Sign-Off block | — | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `.gitleaks.toml` — Gitleaks config with `local.properties` allowlist (Plan 07-01)
- [ ] `.gitignore` — SECU-03 credential patterns (Plan 07-01)
- [ ] `.github/workflows/gitleaks.yml` — SECU-01 CI workflow (Plan 07-02)
- [ ] `docs/SECURITY-CHECKLIST.md` — SECU-02 checklist with sign-off (Plan 07-03)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| History remediation decision | SECU-01 | Irreversible ops per D-01/D-02 | Plan 07-01 checkpoint if gitleaks or history audit finds secrets |
| Gitleaks CI visual confirmation | SECU-01 | Human gate on first CI run | Plan 07-02 checkpoint: confirm green run and `fetch-depth: 0` in logs |
| Security checklist maintainer sign-off | SECU-02 | Human gate per D-06 | Plan 07-03 checkpoint: review evidence, sign name + date |
| Export JSON PII review | SECU-02 | Schema review requires human judgment | Verify `BackupDto` contains only child name + UUID (item 5) |
| Badge asset licensing | SECU-02 | Cross-ref documentation | Confirm `docs/SOURCES.md` documents Simidrott/SLS provenance (item 4) |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 / checkpoint dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify (checkpoints exempt)
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags (CI wait is explicit `gh run watch` in 07-02-02 only)
- [x] Feedback latency < 60s except documented 07-02-02 CI exception
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending execution
