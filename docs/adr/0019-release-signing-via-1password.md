# ADR-0019: Release signing via 1Password

- **Status:** Proposed
- **Date:** 2026-09-28
- **Related:** PRD-0002 (RELE-01, SECU-03), ADR-0011, supersedes ADR-0012

## Context

ADR-0012 keeps the upload keystore and its passwords in a gitignored
`keystore.properties` next to `upload.jks` at the repo root. That works on one
machine, but releasing from more than one PC means copying the keystore and a
plaintext password file between them, and every copy is another place for the
key to leak from or be lost.

The maintainer already uses 1Password, which can hold the keystore file and its
passwords in one item, and whose CLI (`op run`) injects secrets into a single
process without writing them to disk.

## Decision

`app/build.gradle.kts` resolves the release `signingConfig` from, in order:

1. `keystore.properties` at the repo root (unchanged from ADR-0012);
2. **new:** environment variables `ANDROID_KEYSTORE_FILE`, `KEYSTORE_PASSWORD`,
   `KEY_ALIAS`, `KEY_PASSWORD`. `ANDROID_KEYSTORE_FILE` is a path to the
   keystore outside the checkout (a leading `~` is expanded). If it is set but
   the file is missing, the build fails with an actionable message;
3. environment variables `ANDROID_KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`,
   `KEY_ALIAS`, `KEY_PASSWORD` for CI (unchanged from ADR-0012).

The committed `release.env` holds 1Password secret references (`op://…`) for
the passwords and the keystore path. A signed build is:

```sh
op run --env-file=release.env -- ./gradlew bundleRelease
```

The keystore itself is stored as a 1Password document and downloaded once per
machine with `op document get` to `~/.android-keys/`. Everything else in
ADR-0012 stands: release tasks fail hard without signing, there is no debug
keystore fallback, and `scripts/generate-upload-keystore.sh` and
`scripts/verify-release-signature.sh` are unchanged.

## Alternatives considered

- **Keep `keystore.properties` on every machine** — works, but spreads plaintext
  passwords across machines.
- **Inject the keystore as base64 through `op run`** (reusing the CI path) —
  avoids the file on disk, but needs the base64 kept as a second copy in
  1Password alongside the original, and the decoded file lands in `build/`
  anyway.

## Consequences

- Passwords never touch disk on machines that use 1Password.
- `release.env` is committed; it contains only references, so Gitleaks should
  not flag it. The vault, item and field names in it are the maintainer's and
  must match their 1Password item.
- The keystore file still lives on each machine that builds releases, outside
  the checkout.
