# Product Requirements Documents (PRDs)

PRDs describe **what** we are building and **why**: the problem, the users, the
requirements, and how we know we are done. They deliberately avoid prescribing
implementation — architectural choices go into [ADRs](../adr/README.md).

## Index

| PRD | Title | Status |
|-----|-------|--------|
| [0001](0001-mvp-badge-tracker.md) | MVP badge tracker (v1.0) | Shipped 2026-07-25 |
| [0002](0002-release-and-open-source.md) | Release & open source (v1.1) | In progress |
| [0003](0003-sharing-and-custom-catalogs.md) | Sharing & custom catalogs (v2 candidates) | Draft |

## Workflow

1. Copy [`TEMPLATE.md`](TEMPLATE.md) to `NNNN-short-slug.md` using the next free number.
2. Start with status **Draft**. Fill in problem, goals, non-goals and requirements.
3. Give every requirement a stable ID (`AREA-NN`, e.g. `SHAR-01`). IDs are never
   reused or renumbered — they are referenced from ADRs, commits and PRs.
4. Move to **Accepted** when scope is agreed, **In progress** when work starts,
   and **Shipped** when all *must* requirements are done. Tick requirements off
   (`[x]`) as they land and link the PR or evidence.
5. Anything cut from scope moves to *Out of scope* with a reason, or into a new
   Draft PRD — never silently deleted.
6. When a requirement forces a significant technical choice, record it in an ADR
   and link it from the PRD's *Related ADRs* section.

Statuses: `Draft` → `Accepted` → `In progress` → `Shipped` (or `Abandoned`).

## Requirement ID areas

| Prefix | Area |
|--------|------|
| `KIDS` | Child profiles |
| `CATA` | Badge catalogs |
| `PROG` | Progress tracking |
| `UI` | Screens & navigation |
| `DATA` | Persistence, export/import |
| `I18N` | Languages |
| `SECU` | Security & secrets |
| `CI` | Continuous integration |
| `RELE` | Release & Play Store |
| `OSS` | Open-source publishing |
| `SHAR` | Sharing |

## History

Before 2026-09 the project was planned with the GSD workflow in `.planning/`
(milestones, phases, plans). The durable content — requirements, scope and
decisions — was migrated into these PRDs and the ADRs. Per-plan execution logs,
research notes and verification reports were not migrated; they remain available
in git history, e.g. `git show dcf12df:.planning/ROADMAP.md` or
`git ls-tree -r --name-only dcf12df -- .planning`.
