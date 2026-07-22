#!/usr/bin/env bash
# Convert PNG/JPEG badge crops to WebP assets for APK bundling.
# Strips metadata and resizes to 256x256 per ARCHITECTURE.md guidance.
set -euo pipefail

QUALITY=85
SIZE=256
METADATA_FLAG="-metadata none"

usage() {
  cat <<'EOF'
Usage: scripts/convert-badge-images.sh INPUT_DIR OUTPUT_DIR [OPTIONS]

Convert PNG/JPEG badge crops to WebP files in OUTPUT_DIR.

Arguments:
  INPUT_DIR   Directory containing source PNG/JPEG files
  OUTPUT_DIR  Target directory (e.g. app/src/main/assets/badges/simidrott)

Options:
  --help      Show this help and exit
  -q NUM      WebP quality (default: 85)
  -s NUM      Resize to NUM x NUM pixels (default: 256)

Requires cwebp (libwebp). Install via:
  brew install webp

Fallback without cwebp:
  Use Android Studio: right-click PNG → Convert to WebP → lossy 85%, resize 256px.

Examples:
  scripts/convert-badge-images.sh /tmp/crops app/src/main/assets/badges/simidrott
  scripts/convert-badge-images.sh ./crops/sls app/src/main/assets/badges/sls -q 80
EOF
}

if [[ "${1:-}" == "--help" ]] || [[ $# -eq 0 ]]; then
  usage
  exit 0
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --help)
      usage
      exit 0
      ;;
    -q)
      QUALITY="$2"
      shift 2
      ;;
    -s)
      SIZE="$2"
      shift 2
      ;;
    -*)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
    *)
      break
      ;;
  esac
done

if [[ $# -lt 2 ]]; then
  echo "Error: INPUT_DIR and OUTPUT_DIR required." >&2
  usage >&2
  exit 1
fi

INPUT_DIR="$1"
OUTPUT_DIR="$2"

if [[ ! -d "$INPUT_DIR" ]]; then
  echo "Error: input directory not found: $INPUT_DIR" >&2
  exit 1
fi

if ! command -v cwebp >/dev/null 2>&1; then
  echo "Error: cwebp not found. Install webp (brew install webp) or use Android Studio WebP export." >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIR"
count=0

shopt -s nullglob
for src in "$INPUT_DIR"/*.{png,jpg,jpeg,PNG,JPG,JPEG}; do
  [[ -f "$src" ]] || continue
  base="$(basename "$src")"
  name="${base%.*}"
  dest="$OUTPUT_DIR/${name}.webp"
  cwebp -quiet -q "$QUALITY" -resize "$SIZE" "$SIZE" $METADATA_FLAG "$src" -o "$dest"
  echo "→ ${name}.webp"
  count=$((count + 1))
done
shopt -u nullglob

if [[ "$count" -eq 0 ]]; then
  echo "Warning: no PNG/JPEG files found in $INPUT_DIR" >&2
  exit 1
fi

echo "Converted $count file(s) to $OUTPUT_DIR"
