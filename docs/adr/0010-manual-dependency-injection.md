# ADR-0010: Manual dependency injection

- **Status:** Accepted
- **Date:** 2026-07-22 (recorded retroactively 2026-09-25)
- **Related:** ADR-0002

## Context

The app has a small object graph: one Room database, a handful of repositories,
a DataStore and a seed loader.

## Decision

Use **manual DI**: `SimmarkenApplication` owns an `AppContainer`
(`se.simmarken.di`) that constructs the database, repositories and loaders.
ViewModels are created through small factories in `navigation/`.

## Alternatives considered

- **Hilt** — extra annotation processing and build complexity not justified at
  this size; can be adopted later if the graph grows or contributors prefer it.

## Consequences

- Each new ViewModel needs a factory.
- Tests construct dependencies directly without a DI framework.

## Origin

GSD `research/STACK.md` ("Hilt optional — defer until needed"); Phase 1 discretion.
