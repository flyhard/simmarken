# ADR-0003: Offline-only, no backend

- **Status:** Accepted
- **Date:** 2026-07-22 (recorded retroactively 2026-09-25)
- **Related:** PRD-0001 (DATA-01, DATA-02, DATA-03), PRD-0002 out-of-scope list

## Context

The app is used in swim halls with unreliable connectivity and stores
information about children. The maintainer wants privacy and simplicity with no
server to run.

## Decision

The app has **no network access and no backend**. All data lives in the local
Room database. Device migration and sharing between parents is done by manual
JSON export/import (ADR-0008). No analytics, crash reporting or other SDKs that
phone home.

## Alternatives considered

- **Cloud sync / accounts** — requires a backend, privacy policy and operations.
- **Android Auto Backup / Backup API** — opaque to the user and not usable for
  sharing with another parent's phone; may be reconsidered as an addition.

## Consequences

- No `INTERNET` permission; no network libraries (Retrofit, WorkManager sync, Firebase).
- Two parents on two phones do not stay in sync automatically; the backup merge
  (newer wins) is the mitigation.
- Features such as sharing (PRD-0003) must work through Android share intents.

## Origin

GSD `PROJECT.md` constraints and Key Decisions; v1.1 out-of-scope (Crashlytics).
