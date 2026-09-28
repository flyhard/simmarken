# Simmärken

An offline Android app that helps parents track their children's progress through
the official Swedish swimming badge systems: **Svensk Simidrott** and
**Svenska Livräddningssällskapet (SLS)**.

At the swim hall, you can open the app and see right away whether your child has
passed a badge and whether you have bought the pin.

## Features

- A profile for each child, with a colour identity.
- Both official badge catalogs are bundled, with the official skill requirements
  (in Swedish) and pin images.
- Tick off skills one by one. A badge counts as *achieved* when every skill is
  done. Buying the pin (*köpt*) is tracked separately.
- Every badge is in one of four states: gotten, achieved but not bought, in
  progress, or locked.
- Export and import progress as a JSON file, for moving to a new phone or sharing
  with the other parent.
- The app UI is in Swedish and English. You can switch language inside the app.
- **No network, no account, no analytics.** All data stays on the device.

## Prerequisites

- JDK 17
- Android SDK with platform `android-36` (Android Studio installs this for you)
- An emulator or device running Android 8.0 (API 26) or later

## Build and test

```sh
./gradlew assembleDebug                     # debug APK in app/build/outputs/apk/debug/
./gradlew lintDebug testDebugUnitTest       # the same checks CI runs on every push and PR
./gradlew connectedDebugAndroidTest         # Room and seed instrumented tests (needs an emulator; not run in CI)
```

Release builds need signing credentials that are not in the repository. See
[ADR-0012](docs/adr/0012-release-signing-configuration.md) and
`keystore.properties.example`.

## Project layout

| Path | Contents |
|------|----------|
| `app/src/main/java/se/simmarken/` | Kotlin sources: `data` (Room, seed, export, prefs), `domain`, `ui`, `di`, `navigation` |
| `app/src/main/assets/seed/` | Bundled catalog JSON (official requirement text) |
| `app/src/main/assets/badges/` | Bundled pin images |
| `docs/prd/` | Product requirements |
| `docs/adr/` | Architecture decisions |
| `docs/SOURCES.md` | Where the catalog content and images come from |
| `scripts/` | Maintainer scripts for catalog extraction, release signing and Play store graphics |

The stack is Kotlin, Jetpack Compose (Material 3), Room, MVVM, and manual
dependency injection. The reasons are in the [ADRs](docs/adr/README.md).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

The source code is released under the [MIT License](LICENSE).

The badge catalog content (requirement text) and pin images belong to Svensk
Simidrott and SLS. They are **not** covered by the MIT License. See [NOTICE](NOTICE).
