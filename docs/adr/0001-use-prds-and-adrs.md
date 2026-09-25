# ADR-0001: Use PRDs and ADRs instead of GSD planning

- **Status:** Accepted
- **Date:** 2026-09-25
- **Related:** all PRDs and ADRs

## Context

Until now the project was planned and executed with the GSD workflow: a
`.planning/` directory holding `PROJECT.md`, `REQUIREMENTS.md`, `ROADMAP.md`,
`STATE.md`, per-milestone archives, and per-phase folders with CONTEXT,
RESEARCH, PATTERNS, PLAN, SUMMARY, VALIDATION, VERIFICATION, REVIEW, UAT and
SECURITY files (over 170 files for nine phases). A generated `.cursor/rules`
file required every repo edit to start through a GSD command.

Problems observed:

- Most of those files are execution logs with no lasting value once a phase ships.
- The durable information — requirements and design decisions — was scattered
  across phase `CONTEXT.md` files and identified only by phase-local IDs
  (`D-01` exists in every phase).
- Status files drifted from reality (e.g. `STATE.md` showed an earlier phase
  while later phases were completing; requirements stayed "Pending" after their
  evidence was signed off).
- The workflow is tool-specific and heavy for a solo-maintainer project about to
  accept outside contributors.

## Decision

We will use two lightweight, tool-agnostic document types under `docs/`:

- **PRDs** (`docs/prd/`) for product scope: problem, goals, non-goals,
  requirements with stable IDs and acceptance criteria, release criteria.
- **ADRs** (`docs/adr/`) for significant technical decisions, one decision per
  file, immutable once accepted and superseded rather than edited.

Work is tracked through PRD requirement checkboxes, GitHub issues and pull
requests. There is no separate state file.

We migrate the durable GSD content: milestone requirements become PRDs 0001–0003,
and phase decisions become ADRs 0002–0015, each citing its original decision
IDs. The `.planning/` directory and the GSD Cursor rule are removed; they remain
in git history (commit `dcf12df`).

## Alternatives considered

- **Keep GSD** — rejected for the reasons above.
- **Keep `.planning/` as a read-only archive in the tree** — rejected; it keeps
  stale instructions and ~170 files discoverable by humans and AI tools, and git
  history already preserves it.
- **Issues only (no docs in repo)** — rejected; decisions need to be reviewable
  alongside the code and survive a move away from GitHub.

## Consequences

- New features start with a PRD (or a new requirement in an existing PRD);
  significant technical choices get an ADR in the same PR as the code.
- Code comments that cite GSD decision IDs (e.g. `D-13`) stay valid via the
  *Origin* section of each ADR; new comments should cite ADR numbers instead.
- Per-plan research notes, verification reports and metrics are no longer kept
  in the tree.
