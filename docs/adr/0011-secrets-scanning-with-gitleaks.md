# ADR-0011: Secrets scanning with Gitleaks

- **Status:** Accepted
- **Date:** 2026-07-25 (recorded retroactively 2026-09-25)
- **Related:** PRD-0002 (SECU-01 … SECU-03, OSS-05)

## Context

The repository will become public. Anything ever committed — including signing
keys and Play credentials added later — would then be exposed permanently.

## Decision

- **Zero tolerance** for real secrets in history: any confirmed secret is purged
  from history (git-filter-repo/BFG) and rotated if it was ever live.
- **Gitleaks** (pinned version) runs in GitHub Actions (`.github/workflows/gitleaks.yml`)
  on every push and pull request with `fetch-depth: 0` (full history) and fails
  on any non-allowlisted finding.
- False positives are handled via explicit allowlist entries in `.gitleaks.toml`
  (currently only `local.properties`).
- CI-only enforcement; no mandatory local pre-commit hook.
- `.gitignore` blocks keystores, `keystore.properties`, the fingerprint file,
  Play/service-account JSON, `*.p12` and `google-services.json`.
- A pre-public checklist (`docs/SECURITY-CHECKLIST.md`) is signed by the
  maintainer. Making the repo public is hard-gated on a clean scan on `main`.

## Consequences

- Full-history scans get slower as history grows; acceptable at current size.
- A scan cannot catch everything (e.g. PII in fixtures); the checklist and PR
  review cover what tooling misses.

## Origin

GSD Phase 7 D-01…D-13.
