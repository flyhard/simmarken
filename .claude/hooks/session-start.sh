#!/bin/bash
# SessionStart hook for Claude Code cloud sessions (ADR-0016, PRD-0002 CI-07).
#
# Installs the Android SDK pieces CI uses so `./gradlew lintDebug testDebugUnitTest`
# works in a fresh cloud container. Does nothing outside remote sessions.
# Idempotent: a second run with the SDK present only re-exports the paths.
# Fails soft: any error prints one warning line and exits 0 so the session starts.
set -uo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

# Keep in sync with app/build.gradle.kts (compileSdk) and what AGP 9.3.1 requires;
# anything missing here Gradle downloads on every fresh container.
CMDLINE_TOOLS_ZIP="commandlinetools-linux-16111833_latest.zip"
CMDLINE_TOOLS_SHA1="e025545c62a8e64c7559119566a569fb1dec5f60"
SDK_PACKAGES=("platforms;android-36" "build-tools;36.0.0" "platform-tools")

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"
SDK_DIR="${ANDROID_HOME:-$HOME/android-sdk}"
SDKMANAGER="$SDK_DIR/cmdline-tools/latest/bin/sdkmanager"

warn() {
  echo "session-start: WARNING: Android SDK setup failed ($1); ./gradlew lintDebug testDebugUnitTest will not work in this session." >&2
  exit 0
}

sdk_complete() {
  [ -x "$SDKMANAGER" ] || return 1
  for pkg in "${SDK_PACKAGES[@]}"; do
    [ -f "$SDK_DIR/${pkg//;//}/package.xml" ] || return 1
  done
}

fresh_install=false
if ! sdk_complete; then
  fresh_install=true
  if [ ! -x "$SDKMANAGER" ]; then
    tmp="$(mktemp -d)" || warn "mktemp"
    trap 'rm -rf "$tmp"' EXIT
    curl -fsL --retry 3 -o "$tmp/tools.zip" \
      "https://dl.google.com/android/repository/$CMDLINE_TOOLS_ZIP" \
      || warn "download of $CMDLINE_TOOLS_ZIP from dl.google.com"
    echo "$CMDLINE_TOOLS_SHA1  $tmp/tools.zip" | sha1sum -c --quiet - >/dev/null 2>&1 \
      || warn "checksum mismatch for $CMDLINE_TOOLS_ZIP"
    unzip -q "$tmp/tools.zip" -d "$tmp" || warn "unzip of $CMDLINE_TOOLS_ZIP"
    mkdir -p "$SDK_DIR/cmdline-tools" && rm -rf "$SDK_DIR/cmdline-tools/latest" \
      && mv "$tmp/cmdline-tools" "$SDK_DIR/cmdline-tools/latest" \
      || warn "install of cmdline-tools into $SDK_DIR"
  fi
  # `yes` dies of SIGPIPE when sdkmanager exits, so check sdkmanager's status only.
  yes 2>/dev/null | "$SDKMANAGER" --sdk_root="$SDK_DIR" --licenses >/dev/null 2>&1
  [ "${PIPESTATUS[1]}" -eq 0 ] || warn "accepting SDK licenses"
  "$SDKMANAGER" --sdk_root="$SDK_DIR" "${SDK_PACKAGES[@]}" >/dev/null 2>&1 \
    || warn "sdkmanager install of ${SDK_PACKAGES[*]}"
  sdk_complete || warn "SDK packages missing after install"
fi

if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
  {
    echo "export ANDROID_HOME=\"$SDK_DIR\""
    echo "export ANDROID_SDK_ROOT=\"$SDK_DIR\""
  } >> "$CLAUDE_ENV_FILE"
fi

# Git-ignored; lets Gradle find the SDK even where the env file isn't sourced.
if [ ! -f "$PROJECT_DIR/local.properties" ]; then
  echo "sdk.dir=$SDK_DIR" > "$PROJECT_DIR/local.properties"
fi

# First install only: fetch the Gradle distribution and plugins so the first
# real build is faster. Best effort; a failure here is not worth a warning.
if [ "$fresh_install" = true ]; then
  (cd "$PROJECT_DIR" && ANDROID_HOME="$SDK_DIR" ./gradlew --no-daemon -q help >/dev/null 2>&1) || true
fi

exit 0
