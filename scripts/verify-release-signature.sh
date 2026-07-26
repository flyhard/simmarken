#!/usr/bin/env bash
# Verify release AAB signature and optionally compare SHA-256 to keystore-fingerprint.md.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

DEFAULT_AAB="app/build/outputs/bundle/release/app-release.aab"
FINGERPRINT_FILE="keystore-fingerprint.md"

usage() {
  cat <<'EOF'
Usage: scripts/verify-release-signature.sh [AAB_PATH] [OPTIONS]

Post-build validation for a signed release Android App Bundle.

Runs jarsigner -verify and keytool -printcert -jarfile on the AAB. When
keystore-fingerprint.md exists at repo root, compares SHA-256 fingerprints
and prints MATCH or MISMATCH (mismatch does not fail the script).

Arguments:
  AAB_PATH    Path to app-release.aab (default: app/build/outputs/bundle/release/app-release.aab)

Options:
  --help      Show this help and exit

Requires: jarsigner, keytool (JDK 17+)

Prerequisite: run ./gradlew :app:bundleRelease first.
EOF
}

if [[ "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

AAB_PATH="${DEFAULT_AAB}"
if [[ -n "${1:-}" ]]; then
  if [[ "$1" == --* ]]; then
    echo "Unknown option: $1" >&2
    usage >&2
    exit 1
  fi
  AAB_PATH="$1"
fi

if [[ -n "${2:-}" ]]; then
  echo "Unknown option: $2" >&2
  usage >&2
  exit 1
fi

for cmd in jarsigner keytool; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Error: $cmd not found. Install JDK 17+ and ensure it is on PATH." >&2
    exit 1
  fi
done

cd "$REPO_ROOT"

if [[ ! -f "$AAB_PATH" ]]; then
  echo "Error: AAB not found at $REPO_ROOT/$AAB_PATH" >&2
  echo "Run ./gradlew :app:bundleRelease first." >&2
  exit 1
fi

echo "Verifying signature: $AAB_PATH"
echo

echo "→ jarsigner -verify -verbose -certs"
jarsigner -verify -verbose -certs "$AAB_PATH"
echo

echo "→ keytool -printcert -jarfile"
CERT_OUTPUT="$(keytool -printcert -jarfile "$AAB_PATH")"
printf '%s\n' "$CERT_OUTPUT"

AAB_SHA256="$(printf '%s\n' "$CERT_OUTPUT" | awk -F': ' '/SHA256:/{print $2; exit}')"
if [[ -z "$AAB_SHA256" ]]; then
  echo "Error: could not extract SHA-256 from AAB certificate" >&2
  exit 1
fi

echo
echo "AAB SHA-256: $AAB_SHA256"

FINGERPRINT_PATH="$REPO_ROOT/$FINGERPRINT_FILE"
if [[ -f "$FINGERPRINT_PATH" ]]; then
  DOC_SHA256="$(grep -E 'SHA-256:' "$FINGERPRINT_PATH" | head -n 1 | sed -E 's/^.*SHA-256:\*\*? *//; s/\*\*.*$//; s/^[[:space:]]*//; s/[[:space:]]*$//')"
  if [[ -z "$DOC_SHA256" ]]; then
    DOC_SHA256="$(grep -E 'SHA-256:' "$FINGERPRINT_PATH" | head -n 1 | awk -F'SHA-256:' '{print $2}' | tr -d ' *`')"
  fi

  if [[ -z "$DOC_SHA256" ]]; then
    echo "Warning: could not parse SHA-256 from $FINGERPRINT_FILE" >&2
  elif [[ "$AAB_SHA256" == "$DOC_SHA256" ]]; then
    echo "Fingerprint comparison: MATCH ($FINGERPRINT_FILE)"
  else
    echo "Fingerprint comparison: MISMATCH ($FINGERPRINT_FILE)"
    echo "  AAB:  $AAB_SHA256" >&2
    echo "  Doc:  $DOC_SHA256" >&2
  fi
else
  echo "Note: $FINGERPRINT_FILE not found — skipping fingerprint comparison."
fi

echo
echo "Signature verification passed."
