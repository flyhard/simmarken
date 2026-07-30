---
phase: 09-ci-build-test
reviewed: 2026-07-30T12:44:00Z
depth: standard
files_reviewed: 2
files_reviewed_list:
  - app/src/main/java/se/simmarken/ui/settings/SettingsScreen.kt
  - .github/workflows/ci.yml
findings:
  critical: 0
  warning: 1
  info: 1
  total: 2
status: issues_found
---

# Phase 9: Code Review Report

**Reviewed:** 2026-07-30T12:44:00Z
**Depth:** standard
**Files Reviewed:** 2
**Status:** issues_found

## Summary

Phase 9 delivers a lint-driven Compose fix in `SettingsScreen.kt` and a minimal GitHub Actions workflow that mirrors the verified local Gradle command. The CI workflow aligns with locked CONTEXT decisions (D-01–D-16): separate from gitleaks, JDK 17, Android SDK 36, Gradle caching, concurrency cancel-in-progress, and `contents: read` permissions only. No signing secrets or release tasks are referenced.

The lint refactor correctly hoists `stringResource` calls and removes `LocalContextGetResourceValueCall` violations. One behavioral regression remains in the share-export `LaunchedEffect`: the chooser title is captured at effect start instead of resolved at share time, which can show a stale locale compared to the pre-change `context.getString` call. The CI workflow is otherwise sound and matches the green run documented in Plan 09-03.

## Warnings

### WR-01: Share chooser title can be stale after locale change

**File:** `app/src/main/java/se/simmarken/ui/settings/SettingsScreen.kt:45-66`
**Issue:** `shareChooserTitle` is hoisted via `stringResource` in the composable body, but `LaunchedEffect(viewModel)` does not restart when `shareChooserTitle` changes. The running `shareExportUri` collector closes over the title from the effect’s first launch. The previous implementation called `context.getString(R.string.export_share_chooser_title)` inside the collector, resolving the current locale at share time. After an in-session language change, export can open the system chooser with the old language until the effect restarts (activity recreate from `AppCompatDelegate.setApplicationLocales` usually masks this, but the code no longer guarantees correct locale at emission time).
**Fix:** Use `rememberUpdatedState` so the collector always reads the latest resolved title without restarting the flow subscription:

```kotlin
val shareChooserTitle = stringResource(R.string.export_share_chooser_title)
val currentShareChooserTitle by rememberUpdatedState(shareChooserTitle)

LaunchedEffect(viewModel) {
    viewModel.shareExportUri.collect { uri ->
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(send, currentShareChooserTitle),
        )
    }
}
```

Alternatively, add `shareChooserTitle` to the `LaunchedEffect` keys if restarting the collector on locale change is acceptable.

## Info

### IN-01: CI workflow has no job-level timeout

**File:** `.github/workflows/ci.yml:12-33`
**Issue:** The `lint-and-test` job does not set `timeout-minutes`. A hung Gradle or SDK step could consume runner minutes until the default 6-hour limit. CI-03 targets sub-10-minute feedback; a timeout (e.g. 15 minutes) would fail fast on hangs without affecting normal runs (documented run was ~7 minutes).
**Fix:** Add under the job definition:

```yaml
  lint-and-test:
    name: lint-and-test
    runs-on: ubuntu-latest
    timeout-minutes: 15
```

---

_Reviewed: 2026-07-30T12:44:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
