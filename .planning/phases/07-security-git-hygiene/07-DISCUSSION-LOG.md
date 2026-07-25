# Phase 7: Security & Git Hygiene - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-25
**Phase:** 7-Security & Git Hygiene
**Areas discussed:** History remediation, Security checklist scope, Gitleaks strictness

---

## History Remediation

| Option | Description | Selected |
|--------|-------------|----------|
| Zero tolerance | Any real secret in history must be removed or rotated before going public | ✓ |
| Rotate-only | If secret is already invalid, document and allowlist without rewrite | |
| You decide | Pick approach per finding type during implementation | |

| Option | Description | Selected |
|--------|-------------|----------|
| Rewrite history first | BFG/filter-repo to purge; rotate if ever live | ✓ |
| Rotate only if active | Dead credentials allowlisted without rewrite | |
| Case-by-case | Rewrite high-risk; allowlist low-risk | |

| Option | Description | Selected |
|--------|-------------|----------|
| Block public release | Phase 12 cannot proceed until scan is clean | ✓ |
| Document and proceed | Risk acceptance if rotation done | |
| You decide | Gate severity by secret type | |

| Option | Description | Selected |
|--------|-------------|----------|
| Treat as secret | Purge local.properties if ever committed | |
| Low-risk allowlist | SDK paths aren't secrets; allowlist in history | ✓ |
| Scan-only concern | Focus on keys/tokens/keystores only | |

**User's choice:** Zero tolerance for real secrets with rewrite-first remediation; hard block on public release until clean; local.properties is low-risk and allowlistable.

**Notes:** User drew a clear line between actionable secrets (purge/rotate) and machine-specific SDK paths in local.properties.

---

## Security Checklist Scope

| Option | Description | Selected |
|--------|-------------|----------|
| Secrets-focused | Gitleaks + gitignore + no tracked credentials | |
| Release readiness | Secrets + signing + deps/license + export privacy | ✓ |
| Comprehensive | Above plus threat model, permissions, ProGuard, supply chain | |

| Option | Description | Selected |
|--------|-------------|----------|
| Self-sign | Solo maintainer checks all items, signs with date | ✓ |
| Second pair of eyes | Require another reviewer | |
| Checkbox only | No formal sign-off | |

| Option | Description | Selected |
|--------|-------------|----------|
| Core v1.1 items | Gitleaks, gitignore, no SA JSON, SOURCES.md licensing, export PII | ✓ |
| Core + dependency audit | Above plus CVE/OSV review | |
| Minimal | Gitleaks + gitignore only | |

| Option | Description | Selected |
|--------|-------------|----------|
| Phase 7 completion gate | Checklist filled and signed in Phase 7 | ✓ |
| Phase 12 sign-off | Draft in Phase 7, final sign before public | |
| Both | Initial in Phase 7, re-sign in Phase 12 | |

**User's choice:** Release-readiness scope with core v1.1 items; self-sign in Phase 7; completion is a Phase 7 deliverable.

**Notes:** User wants practical checklist for solo maintainer, not exhaustive security audit.

---

## Gitleaks Strictness

| Option | Description | Selected |
|--------|-------------|----------|
| Fail PR/push | Any finding fails workflow | ✓ |
| Warn-only rollout | Advisory first, then blocking | |
| Block main only | Scan PR but fail merges to main | |

| Option | Description | Selected |
|--------|-------------|----------|
| .gitleaks.toml allowlist | Explicit false-positive entries including local.properties | ✓ |
| Strict zero | No allowlist; fix every finding | |
| Baseline file | Grandfather historical fingerprints only | |

| Option | Description | Selected |
|--------|-------------|----------|
| Full history every run | fetch-depth: 0 on push and PR | ✓ |
| Diff-only on PR | Full history on main only | |
| PR diff + nightly full | Balance speed and thoroughness | |

| Option | Description | Selected |
|--------|-------------|----------|
| CI only | Gitleaks in GitHub Actions; no local hook | ✓ |
| Optional local hook | Document in CONTRIBUTING | |
| Mandatory pre-commit | Install hook in Phase 7 | |

**User's choice:** Blocking CI on all findings; `.gitleaks.toml` allowlist; full history every run; no local hooks.

**Notes:** Aligns with SECU-01 zero-findings and ROADMAP fetch-depth: 0 criterion.

---

## Not Discussed

**Gitignore patterns** — User did not select this gray area. Planner should implement SECU-03 per ROADMAP (keystores, Play credentials, local signing files) — captured as D-13 in CONTEXT.md.

## Claude's Discretion

- BFG vs git-filter-repo tool choice
- Exact `.gitleaks.toml` allowlist syntax
- Workflow file structure and action version pin

## Deferred Ideas

None.
