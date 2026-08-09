# Phase 10: Play Console Setup - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-08-09
**Phase:** 10-Play Console Setup
**Areas discussed:** First AAB upload path, Service account & GitHub Secrets, Internal testing scope, Store listing minimum

---

## First AAB Upload Path

| Option | Description | Selected |
|--------|-------------|----------|
| Manual Console upload | Upload Phase 8 signed AAB via Play Console UI once; GPP/API after app record exists | ✓ |
| Try GPP first | Attempt publishBundle before manual upload; fall back to Console | |
| You decide | Minimize Phase 10 risk; document runbook either way | |

| Option | Description | Selected |
|--------|-------------|----------|
| versionCode 1 / versionName 1.0 | Matches current app/build.gradle.kts defaults | ✓ |
| Bump before upload | Increment version before manual upload | |
| You decide | Whatever Play accepts with least friction | |

| Option | Description | Selected |
|--------|-------------|----------|
| Local signed AAB | bundleRelease + verify-release-signature.sh locally | ✓ |
| CI-built AAB | Build in GitHub Actions before Phase 11 workflow | |
| You decide | Fastest path to Play acceptance | |

| Option | Description | Selected |
|--------|-------------|----------|
| Committed checklist with evidence | docs/PLAY-CONSOLE-SETUP.md with evidence slots | ✓ |
| Planning artifacts only | Evidence in SUMMARY/VERIFICATION only | |
| You decide | Minimal doc overhead for Phase 11 handoff | |

**User's choice:** Manual Console upload, versionCode 1, local AAB, committed checklist with evidence.
**Notes:** Aligns with research that Play Developer API cannot register new apps.

---

## Service Account & GitHub Secrets

| Option | Description | Selected |
|--------|-------------|----------|
| Release Manager only | Least privilege for internal-track uploads | ✓ |
| Admin | Broader Console access | |
| You decide | Minimum permissions for GPP internal track | |

| Option | Description | Selected |
|--------|-------------|----------|
| PLAY_SERVICE_ACCOUNT_JSON | Matches ARCHITECTURE.md; aligns with ANDROID_KEYSTORE_BASE64 style | ✓ |
| PLAY_STORE_CREDENTIALS | Matches STACK.md draft | |
| You decide | Pick one for Phase 11 | |

| Option | Description | Selected |
|--------|-------------|----------|
| Play Console linked GCP project | SA in project from Setup → API access | ✓ |
| Existing personal GCP project | Separate project + API access invite | |
| You decide | Follow GPP setup order | |

| Option | Description | Selected |
|--------|-------------|----------|
| Play SA only in Phase 10 | PLAY_SERVICE_ACCOUNT_JSON now; keystore secrets in Phase 11 | ✓ |
| All release secrets now | Also set keystore base64 + passwords in GitHub | |
| You decide | Split by bootstrap vs automation needs | |

**User's choice:** Release Manager, PLAY_SERVICE_ACCOUNT_JSON, Play-linked GCP project, Play secret only in Phase 10.
**Notes:** Keystore secrets explicitly deferred to Phase 11.

---

## Internal Testing Scope

| Option | Description | Selected |
|--------|-------------|----------|
| Internal testing track | Matches ROADMAP; up to 100 testers | ✓ |
| Closed testing track | Email-list testers with opt-in link | |
| You decide | GPP default internal | |

| Option | Description | Selected |
|--------|-------------|----------|
| Maintainer only | Solo validation for v1.1 bootstrap | ✓ |
| Family testers | Add spouse/co-parent emails now | |
| You decide | Start minimal; document adding testers | |

| Option | Description | Selected |
|--------|-------------|----------|
| Swedish, minimal | e.g. "Första interna testversion" | ✓ |
| English, minimal | e.g. "First internal test build" | |
| Both SV + EN | Bilingual release notes from start | |

| Option | Description | Selected |
|--------|-------------|----------|
| Default internal track | Standard Play internal testing; no custom name | ✓ |
| Named release | Descriptive release name in Console | |
| You decide | Whatever GPP track=internal expects | |

**User's choice:** Internal track, maintainer-only testers, Swedish release notes, default internal track.
**Notes:** Family testers deferred to post-bootstrap.

---

## Store Listing Minimum

| Option | Description | Selected |
|--------|-------------|----------|
| Simmärken | Short branding name | ✓ |
| Simmärken Tracker | Matches PROJECT.md title | |
| You decide | Pass Play listing review | |

| Option | Description | Selected |
|--------|-------------|----------|
| Swedish (Sweden) sv-SE | Primary audience; Swedish catalog content | ✓ |
| English (US) en-US | Dev/docs language | |
| You decide | Match primary users | |

| Option | Description | Selected |
|--------|-------------|----------|
| Simple hosted policy | GitHub Pages/gist: offline, no collection, local storage, JSON export | ✓ |
| docs/PRIVACY.md in repo | Commit policy; raw GitHub URL for Play | |
| Minimal Play declaration only | Data safety form only if Play allows | |

| Option | Description | Selected |
|--------|-------------|----------|
| Complete IARC questionnaire | Honest answers; expect Everyone/PEGI 3 | ✓ |
| Minimum for internal only | Defer full rating until public listing | |
| You decide | Follow Play required steps | |

**User's choice:** Simmärken, sv-SE, GitHub Pages privacy policy, full IARC questionnaire.
**Notes:** Privacy policy must state offline-only and no data collection per app architecture.

---

## Claude's Discretion

None — user made explicit choices for all presented options.

## Deferred Ideas

- Family/co-parent internal testers (post-bootstrap)
- Keystore GitHub Secrets (Phase 11)
- GPP automation (Phase 11)
- Optional docs/PRIVACY.md source file in repo
