# ADR-0009: Per-app language preference

- **Status:** Accepted
- **Date:** 2026-07-25 (recorded retroactively 2026-09-25)
- **Related:** PRD-0001 (I18N-01 … I18N-03), ADR-0006

## Context

Parents may run their phone in one language but prefer the app in another. The
language should switch without restarting the app.

## Decision

- Three options: **System default** (initial), **Svenska**, **English**. An
  explicit choice overrides the system locale until reset to System default.
- Stored in **DataStore Preferences** (`LocalePreferencesRepository`); device-only,
  not part of the backup file.
- Applied with `AppCompatDelegate.setApplicationLocales` (per-app locales), so
  `MainActivity` is an `AppCompatActivity`. Changes apply immediately.
- UI strings live in `values/` (Swedish, the default) and `values-en/`; `StringsParityTest` fails the
  build if keys diverge.

## Alternatives considered

- **Compose `CompositionLocalProvider` override** — doesn't affect system UI,
  resources outside Compose or the Android 13+ per-app language settings.

## Consequences

- Adds the AppCompat dependency.
- A new phone starts in the system language until the preference is set again.

## Origin

GSD Phase 6 D-18…D-22.
