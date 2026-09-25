# Architecture Decision Records (ADRs)

ADRs capture **significant technical decisions**: the context, the choice, and
its consequences. They are short, immutable once accepted, and numbered in the
order they were made. Product scope lives in [PRDs](../prd/README.md).

## Index

| ADR | Title | Status |
|-----|-------|--------|
| [0001](0001-use-prds-and-adrs.md) | Use PRDs and ADRs instead of GSD planning | Accepted |
| [0002](0002-native-android-kotlin-compose-room-mvvm.md) | Native Android: Kotlin, Compose, Room, MVVM | Accepted |
| [0003](0003-offline-only-no-backend.md) | Offline-only, no backend | Accepted |
| [0004](0004-progress-model-and-derived-badge-state.md) | Progress model and derived badge state | Accepted |
| [0005](0005-versioned-catalog-seed-with-code-based-merge.md) | Versioned catalog seed with code-based merge | Accepted |
| [0006](0006-official-catalog-content-verbatim-swedish.md) | Official catalog content stays verbatim Swedish | Accepted |
| [0007](0007-bundled-official-pin-images.md) | Bundle official pin images | Accepted |
| [0008](0008-json-backup-format-and-merge.md) | JSON backup format and merge | Accepted |
| [0009](0009-per-app-language-preference.md) | Per-app language preference | Accepted |
| [0010](0010-manual-dependency-injection.md) | Manual dependency injection | Accepted |
| [0011](0011-secrets-scanning-with-gitleaks.md) | Secrets scanning with Gitleaks | Accepted |
| [0012](0012-release-signing-configuration.md) | Release signing configuration | Accepted |
| [0013](0013-ci-on-github-actions.md) | CI on GitHub Actions | Accepted |
| [0014](0014-play-store-publishing-via-gradle-play-publisher.md) | Play Store publishing via Gradle Play Publisher | Accepted (not yet implemented) |
| [0015](0015-play-console-bootstrap.md) | Play Console bootstrap | Accepted (not yet implemented) |
| [0016](0016-android-sdk-in-cloud-agent-sessions.md) | Android SDK in Claude Code cloud sessions | Accepted |

## When to write an ADR

Write one when a decision is hard to reverse, affects more than one module,
changes a persisted format (database schema, backup JSON, seed format), adds or
replaces a major dependency, or when you would otherwise have to explain the
"why" in a code review.

## Workflow

1. Copy [`TEMPLATE.md`](TEMPLATE.md) to `NNNN-short-slug.md` using the next free number.
2. Status **Proposed** while under discussion (e.g. in the PR that introduces it).
3. Merge as **Accepted** together with — or before — the code that implements it.
4. Never rewrite an accepted ADR's decision. To change course, write a new ADR
   and mark the old one **Superseded by ADR-NNNN** (editing only its status line).
5. Link related PRD requirement IDs so product and architecture stay traceable.

Statuses: `Proposed` → `Accepted` → (`Deprecated` | `Superseded by ADR-NNNN`), or `Rejected`.

## History

ADRs 0002–0015 were written retroactively on 2026-09-25 from the decisions
recorded during GSD phase discussions (`.planning/**/NN-CONTEXT.md`, decision IDs
like "Phase 5 D-10"). Each ADR lists the original decision IDs under *Origin* so
older commit messages and code comments that cite them (e.g. `D-13`) can be
traced. The original files are in git history at commit `dcf12df`.
