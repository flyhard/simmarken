# Phase 7: Security & Git Hygiene - Context

**Gathered:** 2026-07-25
**Status:** Ready for planning

<domain>
## Phase Boundary

Harden the repository against accidental secret commits before any public visibility. Deliverables: full-history Gitleaks scan with zero real-secret findings, hardened `.gitignore` for signing/credential files (SECU-03), Gitleaks GitHub Actions workflow on every push/PR with `fetch-depth: 0` (SECU-01), and a completed `docs/SECURITY-CHECKLIST.md` with maintainer sign-off (SECU-02). Does not include release signing setup (Phase 8), general CI build/test (Phase 9), or making the repo public (Phase 12).

</domain>

<decisions>
## Implementation Decisions

### History Remediation
- **D-01:** Zero tolerance for real secrets in git history — any confirmed secret must be purged (BFG/filter-repo) or rotated before public release; no "it's old" exceptions.
- **D-02:** Prefer history rewrite first — use BFG or git-filter-repo to remove confirmed secrets from all commits; rotate credentials if they were ever live/active.
- **D-03:** Public-release gate is hard-blocked — Phase 12 (OSS-05) cannot proceed until full-history Gitleaks scan is clean; no risk-acceptance workaround.
- **D-04:** `local.properties` is low-risk — SDK paths and machine-specific entries are not secrets; allowlist in `.gitleaks.toml` if found in history rather than rewriting for those alone.

### Security Checklist (SECU-02)
- **D-05:** Checklist scope is "release readiness" — beyond secrets: signing files not in repo, no service account JSON tracked, badge asset licensing documented, export JSON privacy review.
- **D-06:** Solo maintainer self-sign — maintainer checks all items and signs off with name + date directly in `docs/SECURITY-CHECKLIST.md`.
- **D-07:** Core checklist items for v1.1: (1) Gitleaks clean on full history, (2) `.gitignore` covers signing/credential files, (3) no service account JSON in repo, (4) badge asset licensing documented per `docs/SOURCES.md`, (5) export JSON contains no unexpected PII.
- **D-08:** Checklist completion is a Phase 7 gate — filled and signed in Phase 7; Phase 12 only re-verifies SECU-01 still passes on `main` before visibility change.

### Gitleaks CI & Strictness
- **D-09:** Workflow fails on any finding — Gitleaks GitHub Actions job fails push/PR on any non-allowlisted finding; aligns with SECU-01 zero-findings requirement.
- **D-10:** `.gitleaks.toml` allowlist for false positives — explicit allowlist entries (including `local.properties` path patterns); everything else must be zero findings.
- **D-11:** Full history on every CI run — `fetch-depth: 0` on push and PR; no diff-only optimization.
- **D-12:** CI-only enforcement — no mandatory local pre-commit hook; Gitleaks runs in GitHub Actions only (solo-maintainer simplicity).

### Gitignore (SECU-03 — not discussed; from requirements)
- **D-13:** Standard Android signing/credential patterns per ROADMAP success criteria — block at minimum: `*.jks`, `*.keystore`, `keystore.properties`, `**/play-credentials.json`, `**/service-account*.json`, `local.properties` (already present). Planner may add adjacent patterns (e.g. `*.p12`, `google-services.json` if ever introduced) without re-discussion.

### Claude's Discretion
- Exact `.gitleaks.toml` allowlist rule syntax for `local.properties` fingerprints.
- Choice between BFG Repo-Cleaner vs `git-filter-repo` if history rewrite is needed.
- Specific `.gitignore` pattern ordering and comments.
- Gitleaks GitHub Action version pin and workflow file structure (delegated to Phase 9 overlap boundary — Phase 7 owns the gitleaks workflow specifically).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requirements & Roadmap
- `.planning/REQUIREMENTS.md` — SECU-01, SECU-02, SECU-03 requirement definitions
- `.planning/ROADMAP.md` — Phase 7 goal, success criteria, dependency on Phase 6
- `.planning/PROJECT.md` — v1.1 milestone context; Gitleaks + GitHub Actions decision
- `.planning/STATE.md` — Security gates must pass before public visibility

### Security & Licensing
- `docs/SOURCES.md` — Badge asset provenance and licensing (checklist item D-07)
- `.gitignore` — Current baseline (minimal Android ignores; needs hardening per SECU-03)

### Out of Scope (later phases)
- Phase 8: `app/build.gradle.kts` signing configuration
- Phase 9: General lint/test CI workflow
- Phase 12: Public visibility change (OSS-05)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `.gitignore`: Already ignores `local.properties`, `app/build`, `.gradle` — extend, don't replace.
- No existing `.github/workflows/` — greenfield for Gitleaks workflow.
- No `.gitleaks.toml` — greenfield config with allowlist support.

### Established Patterns
- v1.1 is infrastructure-only — no app code changes expected in Phase 7.
- Tooling decision locked: Gitleaks (not TruffleHog), GitHub Actions (not Fastlane).
- Solo-maintainer workflow — prefer simple CI enforcement over local hook setup.

### Integration Points
- Gitleaks workflow will be first GitHub Actions workflow in repo; Phase 9 adds lint/test workflow separately.
- `docs/SECURITY-CHECKLIST.md` is new artifact; sign-off unblocks Phase 12's SECU-02 re-check only.
- `.gitignore` hardening protects files that Phase 8 (signing) and Phase 10/11 (Play credentials) will introduce locally.

</code_context>

<specifics>
## Specific Ideas

- User explicitly distinguished real secrets (zero tolerance + rewrite) from `local.properties` SDK paths (allowlist, not secret).
- Checklist should be practical for solo maintainer — not a comprehensive threat-model exercise.
- Phase 7 completion means checklist is done; don't defer sign-off to Phase 12.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope. Gitignore pattern details deferred to planner per D-13 (requirements-driven, not user-discussed).

</deferred>

---

*Phase: 7-Security & Git Hygiene*
*Context gathered: 2026-07-25*
