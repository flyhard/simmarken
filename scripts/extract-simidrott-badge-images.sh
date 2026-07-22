#!/usr/bin/env bash
# Extract Simidrott badge pin images from official simmärkesaffisch PDF.
# Maps embedded PDF images to seed JSON filenames via affisch progression order.
set -euo pipefail

AFFISCH_PDF="docs/extraction/pdfs/simidrott/simmärkesaffisch-2024.pdf"
EXTRACT_DIR="/tmp/simidrott-affisch-extract"
CROP_DIR="/tmp/simidrott-badge-crops"
OUTPUT_DIR="app/src/main/assets/badges/simidrott"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<'EOF'
Usage: scripts/extract-simidrott-badge-images.sh [OPTIONS]

Extract badge pin images from the official simmärkesaffisch PDF and convert
to WebP in app/src/main/assets/badges/simidrott/.

Prerequisites:
  - docs/extraction/pdfs/simidrott/simmärkesaffisch-2024.pdf (run extract-simidrott.sh)
  - pdfimages, cwebp

Options:
  --help    Show this help and exit

Image mapping follows affisch progression order (D-08). Järn/Brons/Silver
märken use the three medal-tier pins from the affisch footer grid.
EOF
}

if [[ "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ ! -f "$AFFISCH_PDF" ]]; then
  echo "Error: affisch PDF not found at $AFFISCH_PDF" >&2
  echo "Run: scripts/extract-simidrott.sh" >&2
  exit 1
fi

if ! command -v pdfimages >/dev/null 2>&1; then
  echo "Error: pdfimages not found (install poppler)." >&2
  exit 1
fi

mkdir -p "$EXTRACT_DIR" "$CROP_DIR"
EXTRACT_PREFIX="$EXTRACT_DIR/affisch"
pdfimages -png "$AFFISCH_PDF" "$EXTRACT_PREFIX" >/dev/null

# pdfimages num -> seed filename (affisch progression, page 1 ICC/index images)
# See docs/SOURCES.md for source attribution.
declare -a MAPPING=(
  "003|baddaren_gron"
  "006|baddaren_bla"
  "009|baddaren_gul"
  "012|skoldpaddan"
  "015|blackfisken"
  "018|pingvinen_silver"
  "021|pingvinen_guld"
  "024|silverfisken"
  "027|guldfisken"
  "030|hajen_brons"
  "033|hajen_silver"
  "036|hajen_guld"
  "098|jarnmarket"
  "101|bronsmarket"
  "104|silvermarket"
)

rm -f "$CROP_DIR"/*.png 2>/dev/null || true

pad3() {
  printf '%03d' "$((10#$1))"
}

for entry in "${MAPPING[@]}"; do
  num="${entry%%|*}"
  name="${entry#*|}"
  src="$EXTRACT_PREFIX-$(pad3 "$num").png"
  if [[ ! -f "$src" ]]; then
    echo "Error: expected extract $src not found" >&2
    exit 1
  fi
  cp "$src" "$CROP_DIR/${name}.png"
  echo "Mapped affisch-$(pad3 "$num").png → ${name}.png"
done

"$SCRIPT_DIR/convert-badge-images.sh" "$CROP_DIR" "$OUTPUT_DIR"
echo "Simidrott badge images ready in $OUTPUT_DIR"
