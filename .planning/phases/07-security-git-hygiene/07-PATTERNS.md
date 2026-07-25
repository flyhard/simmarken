# Phase 7: Security & Git Hygiene - Pattern Map

**Mapped:** 2026-07-25
**Files analyzed:** 4 new/modified + 2 referenced
**Analogs found:** 2 / 4 (2 greenfield — no repo analog)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `.gitignore` | config | file-I/O | `.gitignore` | exact (extend) |
| `.gitleaks.toml` | config | batch (full-history scan) | — | no analog |
| `.github/workflows/gitleaks.yml` | config | event-driven (push/PR) | — | no analog |
| `docs/SECURITY-CHECKLIST.md` | doc | transform (human gate) | `docs/extraction/SLS-CHECKLIST.md` | role-match |
| `docs/SOURCES.md` | doc | — (referenced, not modified) | — | cross-ref only |
| `app/src/main/java/se/simmarken/data/export/BackupDto.kt` | model | transform | `BackupDto.kt` | exact (PII scope) |

## Pattern Assignments

### `.gitignore` (config, file-I/O)

**Analog:** `.gitignore` (extend in place — do not replace)

**Current baseline** (lines 1-11):

```gitignore
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
app/build
docs/extraction/pdfs/
```

**Extension pattern:** Append a commented section after existing rules. Repo convention is one pattern per line, no grouping comments today — add the first section comment for SECU-03 credential block only (planner discretion D-13).

**Patterns to add** (from RESEARCH + D-13; verify with `git check-ignore -v`):

```gitignore
# Android signing & Play credentials (SECU-03)
*.jks
*.keystore
keystore.properties
**/play-credentials.json
**/service-account*.json

# Optional adjacent patterns (planner discretion per D-13)
*.p12
google-services.json
```

**Note:** `/local.properties` already present (line 3). Do not duplicate.

**Verification command** (from RESEARCH):

```bash
git check-ignore -v local.properties upload.jks keystore.properties play-credentials.json service-account.json
```

---

### `.gitleaks.toml` (config, batch)

**Analog:** None — greenfield. First TOML config in repo.

**Use RESEARCH Pattern 2** — extend Gitleaks defaults + path allowlist for `local.properties` (D-04, D-10):

```toml
[extend]
useDefault = true

[[allowlists]]
description = "Android local SDK paths — not secrets (D-04)"
paths = [
  '''(?:^|/)local\.properties$''',
]
```

**Conventions:**
- Root-level file (gitleaks-action auto-detects)
- Use `[[allowlists]]` syntax (Gitleaks v8.25+), not legacy `[allowlist]`
- Allowlist only `local.properties` path pattern initially — no broad regex (D-10 pitfall)
- Pin verification: `gitleaks detect --source . --config .gitleaks.toml --verbose`

---

### `.github/workflows/gitleaks.yml` (config, event-driven)

**Analog:** None — greenfield. No `.github/workflows/` exists. Phase 9 adds lint/test workflow separately; Phase 7 owns Gitleaks only.

**Use RESEARCH Pattern 1** — official `gitleaks/gitleaks-action` README structure:

```yaml
name: gitleaks
on:
  pull_request:
  push:
jobs:
  scan:
    name: gitleaks
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v6
        with:
          fetch-depth: 0
      - uses: gitleaks/gitleaks-action@v3
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

**Locked constraints from CONTEXT:**
- `fetch-depth: 0` on every run (D-11) — shallow checkout misses history
- Fail on any non-allowlisted finding (D-09) — default action behavior
- Pin `gitleaks/gitleaks-action@v3` (not v2 — deprecated)
- Pin `actions/checkout@v6`
- `GITLEAKS_LICENSE` not required for personal GitHub accounts (RESEARCH A1)
- No pre-commit hook (D-12)

**File location:** `.github/workflows/gitleaks.yml` (workflow name `gitleaks` per RESEARCH)

---

### `docs/SECURITY-CHECKLIST.md` (doc, transform)

**Analog:** `docs/extraction/SLS-CHECKLIST.md`

**Title + metadata header pattern** (lines 1-5):

```markdown
# SLS Core Scope — D-12 Verification Checklist

**catalogVersion basis:** `2026.07.22` (shop product verification)  
**Sources:** [SLS shop — sim- och livräddningsmärken](https://shop.svenskalivraddningssallskapet.se/collections/sim-och-livraddningsmarken), product JSON via `scripts/extract-sls.sh`  
**Excluded per D-05/D-06:** Simborgarmärken, Vädermärket, Grodan Rygg variants, Magister-tier badges
```

**Adapt for SECURITY-CHECKLIST:** Use `# Pre-Public Security Checklist` title + metadata block (phase, SECU-02, maintainer self-sign D-06).

**Table with verification column pattern** (lines 7-8, 25):

```markdown
| code | nameSv | shop product | req count | imageAssetPath | verified |
|------|--------|--------------|-----------|----------------|----------|
| droppen | Droppen | droppen | 1 | badges/sls/droppen.webp | [ ] |
...
| guldsalen | Guldsälen | guldsalen | 2 | badges/sls/guldsalen.webp | [ ] |
```

**Adapt:** Five D-07 items with `Status` and `Evidence` columns (RESEARCH Pattern 4):

```markdown
| # | Item | Status | Evidence |
|---|------|--------|----------|
| 1 | Gitleaks clean on full git history | [ ] | CI run URL / `gitleaks detect` output |
| 2 | `.gitignore` covers signing/credential files | [ ] | `git check-ignore -v` output |
| 3 | No service account JSON in repo or history | [ ] | `git log --all -- '**/service-account*.json'` |
| 4 | Badge asset licensing documented | [ ] | `docs/SOURCES.md` reviewed |
| 5 | Export JSON contains no unexpected PII | [ ] | `BackupDto` field review (see below) |
```

**Notes section pattern** (lines 27-32):

```markdown
**Notes**

- Requirement bullets parsed from shop `body_html` **Vad:** sections (MEDIUM confidence per D-06).
- Droppen has no skill bullets — seeded with official Hur text: "För att ta Droppen finns inga kunskapskrav."
- GP article text must not be used for verification — shop product pages only.
- Run `bash scripts/extract-sls.sh` before re-verifying after shop updates.
```

**Adapt:** Add `## Export JSON PII Scope (item 5)` and `## Sign-Off` sections per RESEARCH Pattern 4:

```markdown
## Export JSON PII Scope (item 5)

Expected fields only: child `name`, `stableId` (UUID), `avatarColorArgb`, progress timestamps/codes.
No email, phone, address, device identifiers, or location data.

## Sign-Off

- [ ] All items verified
- Signed: _________________ Date: _________
```

**Checkbox convention from `.planning/REQUIREMENTS.md`** (lines 13-15):

```markdown
- [ ] **SECU-01**: Automated secrets scan (gitleaks) passes with zero findings on full git history
- [ ] **SECU-02**: Manual pre-public security review checklist completed and signed off
- [ ] **SECU-03**: `.gitignore` hardened for keystores, Play credentials, and local signing files
```

---

### `docs/SOURCES.md` (referenced — checklist item 4, not modified)

**Analog:** Self — licensing sections already document badge asset use.

**Simidrott licensing** (lines 23-28):

```markdown
### Licensing (promotional use)

Svensk Simidrott's simmärkesmaterial page states that the material may be downloaded and used for marketing and promotion of simmärken (swimming badges). Requirement text in this app comes from the official simmärkesmaterial protocols and affisch downloaded from that page.

Badge pin artwork is taken from official product photos on the [Svensk Simidrott shop](https://privat.ssfshopen.se/marken-simmarken) (NeH e-commerce, `images.neh.com`), used solely to help parents identify official badges their children have earned. White studio backgrounds are removed in post-processing; see **Images (Bilder)** below.
```

**SLS confidence / source policy** (lines 43-47):

```markdown
### Confidence notes

SLS does not publish per-badge protocol PDFs equivalent to Simidrott's simmärkesmaterial. Requirement text comes from official shop product descriptions (`body_html` Vad/Varför/Hur sections) at **MEDIUM** confidence. See `docs/extraction/SLS-SOURCE-NOTES.md` for per-badge source URLs and confidence ratings.

**GP article excluded:** The Göteborgs-Posten simmärken article is not used as a primary source for requirements or images. It may appear only as a tertiary cross-check during manual D-12 review — not as authoritative data.
```

**Checklist item 4 evidence:** Maintainer confirms these sections were read; no SOURCES.md edit required in Phase 7 unless licensing gaps found.

---

### `BackupDto.kt` (model, transform — PII scope for checklist item 5)

**Analog:** `app/src/main/java/se/simmarken/data/export/BackupDto.kt`

**KDoc field semantics** (lines 5-17):

```kotlin
/**
 * Version 1 backup JSON schema for phone migration (D-08).
 *
 * Locked field semantics — forward-compatible readers required once parents exchange files:
 * - [exportVersion] must be `1` for this schema generation.
 * - Kids are keyed by [KidBackupDto.stableId] (UUID string), never Room auto-increment ids.
 * - Progress rows use catalog [RequirementProgressBackupDto.catalogCode] +
 *   [RequirementProgressBackupDto.badgeCode] + [RequirementProgressBackupDto.requirementCode]
 *   (or badge-level codes for [BadgeProgressBackupDto]) — not Room Long foreign keys.
 * - [RequirementProgressBackupDto.updatedAtEpochMillis] and [BadgeProgressBackupDto.updatedAtEpochMillis]
 *   drive D-04 newer-wins merge per row.
 * - Payload excludes catalog seed data (D-05) and language preference (D-21).
 */
```

**Expected PII fields** (lines 18-54):

```kotlin
@Serializable
data class BackupDto(
    val exportVersion: Int = 1,
    val exportedAtEpochMillis: Long,
    val kids: List<KidBackupDto>,
)

@Serializable
data class KidBackupDto(
    val stableId: String,
    val name: String,
    val avatarColorArgb: Int,
    val createdAtEpochMillis: Long,
    val sortOrder: Int,
    val requirementProgress: List<RequirementProgressBackupDto>,
    val badgeProgress: List<BadgeProgressBackupDto>,
)
```

**Checklist item 5:** Confirm no fields beyond `KidBackupDto`, progress DTOs, and export metadata. No email, phone, address, device IDs, or location.

---

## Shared Patterns

### Infrastructure-only phase — no app code changes

**Source:** `07-CONTEXT.md` code_context + `07-RESEARCH.md` Summary

v1.1 Phase 7 is config/CI/docs only. Do not modify Kotlin sources, Gradle signing config (Phase 8), or general CI workflow (Phase 9).

### Verification via CLI commands (not unit tests)

**Source:** `07-RESEARCH.md` Validation Architecture

| Requirement | Command |
|-------------|---------|
| SECU-01 local | `gitleaks detect --source . --config .gitleaks.toml --verbose` |
| SECU-03 | `git check-ignore -v upload.jks keystore.properties play-credentials.json service-account.json local.properties` |
| History audit | `git log --all --oneline -- '*.jks' '*.keystore' 'keystore.properties' '**/play-credentials.json' '**/service-account*.json' 'local.properties'` |

**Per-task:** run Gitleaks after each config commit. **Phase gate:** CI green + checklist signed.

### Conditional history remediation (not default path)

**Source:** `07-CONTEXT.md` D-01–D-03, `07-RESEARCH.md` Conditional History Rewrite

Baseline scan clean (169 commits, 0 findings). Only if Gitleaks finds confirmed secrets:

```bash
git filter-repo --path secrets/service-account.json --invert-paths
# or
echo 'literal:AKIA_EXAMPLE_KEY==>***REMOVED***' > /tmp/replacements.txt
git filter-repo --replace-text /tmp/replacements.txt
```

Prefer `git-filter-repo` over BFG (D-02 discretion). Always rotate live credentials after purge.

### Solo-maintainer doc style

**Source:** `docs/SOURCES.md`, `docs/extraction/SLS-CHECKLIST.md`

- Markdown tables for structured verification
- `**Bold metadata**` lines for basis dates and sources
- `[ ]` checkboxes for manual gates
- Evidence column or Notes section for audit trail (D-06)

### Tooling locked decisions

**Source:** `.planning/PROJECT.md` (lines 27-28), `07-CONTEXT.md` D-09–D-12

- Gitleaks (not TruffleHog)
- GitHub Actions (not Fastlane)
- CI-only enforcement (no mandatory pre-commit hook)
- Zero tolerance for real secrets in history (D-01)

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| `.gitleaks.toml` | config | batch | First secrets-scanner config in repo; use RESEARCH Pattern 2 + official Gitleaks docs |
| `.github/workflows/gitleaks.yml` | config | event-driven | No `.github/workflows/` directory exists; use RESEARCH Pattern 1 + gitleaks-action README |

## Metadata

**Analog search scope:** `.gitignore`, `docs/**`, `.github/**`, `**/*.toml`, `**/*.yml`, `.planning/**`, `app/src/main/java/se/simmarken/data/export/`
**Files scanned:** 12
**Pattern extraction date:** 2026-07-25
