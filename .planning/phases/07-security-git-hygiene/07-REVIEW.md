---
phase: 07-security-git-hygiene
reviewed: 2026-07-25T08:06:00Z
depth: standard
files_reviewed: 4
files_reviewed_list:
  - .gitignore
  - .gitleaks.toml
  - .github/workflows/gitleaks.yml
  - docs/SECURITY-CHECKLIST.md
findings:
  critical: 0
  warning: 2
  info: 1
  total: 3
status: issues_found
---

# Phase 7: Code Review Report

**Reviewed:** 2026-07-25T08:06:00Z
**Depth:** standard
**Files Reviewed:** 4
**Status:** issues_found

## Summary

Phase 7 security infrastructure is largely correct and aligned with SECU-01/02/03 requirements: `.gitleaks.toml` extends defaults with a narrowly scoped `local.properties` allowlist, the Gitleaks workflow uses `fetch-depth: 0` and pins Gitleaks 8.30.1, and the security checklist accurately documents export PII scope against `BackupDto.kt`. Local `gitleaks detect` against the current tree reports zero leaks.

Two robustness gaps remain: `.gitignore` uses a root-anchored `/local.properties` pattern that does not ignore nested paths (e.g. `app/local.properties`), and the GitHub Actions workflow omits an explicit `permissions` block, which can break PR comment integration or fail on repositories with restrictive default `GITHUB_TOKEN` scopes. Neither gap undermines the current clean-history baseline, but both should be fixed before Phase 8 introduces signing files locally.

## Warnings

### WR-01: Root-only `local.properties` ignore leaves nested paths trackable

**File:** `.gitignore:3`
**Issue:** The pattern `/local.properties` only ignores `local.properties` at the repository root. A nested file such as `app/local.properties` is not matched (`git check-ignore -v app/local.properties` returns no rule). D-13 and SECU-03 intend to block `local.properties` generally; the gitleaks allowlist would suppress scan noise if such a file were committed, but git would still track it.
**Fix:** Replace the root-anchored pattern with a path-agnostic rule:

```gitignore
local.properties
```

Or, if root-only is intentional, document that constraint in the SECU-03 comment and add `**/local.properties` for defense in depth.

### WR-02: Gitleaks workflow lacks explicit `permissions`

**File:** `.github/workflows/gitleaks.yml:6-16`
**Issue:** The workflow does not declare `permissions` for `GITHUB_TOKEN`. On repositories or organizations where the default token scope is `contents: read` only, gitleaks-action may fail to post PR review comments (`Resource not accessible by integration`). The core scan may still run, but SECU-01 CI feedback on pull requests becomes unreliable without explicit scopes.
**Fix:** Add least-privilege permissions at the job level:

```yaml
jobs:
  scan:
    name: gitleaks
    runs-on: ubuntu-latest
    permissions:
      contents: read
      pull-requests: write
    steps:
      # ...
```

Set `GITLEAKS_ENABLE_COMMENTS: "false"` only if PR comments are intentionally disabled.

## Info

### IN-01: Checklist item 2 evidence omits patterns it claims to cover

**File:** `docs/SECURITY-CHECKLIST.md:10`
**Issue:** Item 2 states that the SECU-03 block also covers `*.keystore`, `*.p12`, and `google-services.json`, but the pasted `git check-ignore -v` evidence only exercises `upload.jks` and the JSON/property paths. The patterns do work when tested (`upload.keystore`, `test.p12`, `app/google-services.json` all match), but the audit trail is incomplete for future re-verification.
**Fix:** Extend the evidence line to include representative paths, e.g. `git check-ignore -v upload.keystore test.p12 google-services.json`.

---

_Reviewed: 2026-07-25T08:06:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
