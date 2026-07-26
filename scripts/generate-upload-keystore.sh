#!/usr/bin/env bash
# Generate the Play upload keystore (upload.jks) and capture SHA-256 fingerprint.
# upload.jks and keystore-fingerprint.md are gitignored (SECU-03).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

KEYSTORE="upload.jks"
ALIAS="upload"
FINGERPRINT_FILE="keystore-fingerprint.md"

usage() {
  cat <<'EOF'
Usage: scripts/generate-upload-keystore.sh [OPTIONS]

One-time maintainer workflow to create the Play upload keystore at repo root.

  1. Run this script (keytool prompts for passwords and DN fields interactively)
  2. Copy keystore.properties.example → keystore.properties
  3. Fill storePassword and keyPassword in keystore.properties

Creates:
  upload.jks              — upload keystore (gitignored)
  keystore-fingerprint.md — SHA-256 fingerprint doc (gitignored)

Options:
  --help    Show this help and exit

Requires: keytool (JDK 17+)
EOF
}

if [[ "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ "${1:-}" != "" ]]; then
  echo "Unknown option: $1" >&2
  usage >&2
  exit 1
fi

if ! command -v keytool >/dev/null 2>&1; then
  echo "Error: keytool not found. Install JDK 17+ and ensure keytool is on PATH." >&2
  exit 1
fi

cd "$REPO_ROOT"

if [[ -f "$KEYSTORE" ]]; then
  echo "ERROR: $KEYSTORE already exists. Refusing to overwrite." >&2
  exit 1
fi

echo "Generating upload keystore at $REPO_ROOT/$KEYSTORE"
echo "keytool will prompt for keystore and key passwords (not stored by this script)."
echo

keytool -genkeypair -v \
  -keystore "$KEYSTORE" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

echo
echo "Extracting SHA-256 fingerprint (keytool will re-prompt for keystore password)..."
SHA256=$(keytool -list -v -keystore "$KEYSTORE" -alias "$ALIAS" \
  | awk -F': ' '/SHA256:/{print $2; exit}')

if [[ -z "$SHA256" ]]; then
  echo "Error: could not extract SHA-256 fingerprint from $KEYSTORE" >&2
  exit 1
fi

CREATED_DATE="$(date -u +"%Y-%m-%d")"

cat > "$FINGERPRINT_FILE" <<EOF
# Upload Keystore Fingerprint (PRIVATE — gitignored)

- **Created:** ${CREATED_DATE}
- **Keystore:** ${KEYSTORE}
- **Alias:** ${ALIAS}
- **SHA-256:** ${SHA256}

## Re-verify

\`\`\`bash
keytool -list -v -keystore ${KEYSTORE} -alias ${ALIAS}
\`\`\`
EOF

echo "SHA-256: $SHA256"
echo "Fingerprint written to $REPO_ROOT/$FINGERPRINT_FILE (gitignored)"
