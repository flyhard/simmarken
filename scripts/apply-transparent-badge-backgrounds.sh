#!/usr/bin/env bash
# Re-export badge WebPs with transparent backgrounds (white matte removal).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

QUALITY=85
SIZE=256
THRESHOLD=40
SOURCE_DIR=""
TARGET_DIR=""

usage() {
  cat <<'EOF'
Usage: scripts/apply-transparent-badge-backgrounds.sh SOURCE_DIR TARGET_DIR [OPTIONS]

Remove white product-photo backgrounds and write transparent WebP badges.

SOURCE_DIR   Directory of source JPEG/PNG files (or WebP via dwebp)
TARGET_DIR   Output directory for transparent .webp files

Options:
  --help           Show this help and exit
  -q NUM           WebP quality (default: 85)
  -s NUM           Output size NUM x NUM (default: 256)
  -t NUM           Flood-fill threshold (default: 40)

Requires: python3, Pillow, cwebp, dwebp (for WebP sources)
EOF
}

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
    -t)
      THRESHOLD="$2"
      shift 2
      ;;
    -*)
      echo "Unknown option: $1" >&2
      usage >&2
      exit 1
      ;;
    *)
      if [[ -z "$SOURCE_DIR" ]]; then
        SOURCE_DIR="$1"
      elif [[ -z "$TARGET_DIR" ]]; then
        TARGET_DIR="$1"
      else
        echo "Unexpected argument: $1" >&2
        exit 1
      fi
      shift
      ;;
  esac
done

if [[ -z "$SOURCE_DIR" || -z "$TARGET_DIR" ]]; then
  echo "Error: SOURCE_DIR and TARGET_DIR required." >&2
  usage >&2
  exit 1
fi

if ! python3 -c "import PIL" 2>/dev/null; then
  echo "Error: Pillow required. Create a venv and install: python3 -m venv .venv && .venv/bin/pip install Pillow" >&2
  exit 1
fi

for cmd in cwebp python3; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Error: $cmd not found." >&2
    exit 1
  fi
done

mkdir -p "$TARGET_DIR"
tmpdir="$(mktemp -d)"
trap 'rm -rf "$tmpdir"' EXIT

count=0
shopt -s nullglob
# Prefer lossless shop JPEGs/PNGs; only fall back to WebP when no raster sources exist.
sources=("$SOURCE_DIR"/*.{jpg,jpeg,png,JPG,JPEG,PNG})
if [[ ${#sources[@]} -eq 0 || ! -f "${sources[0]}" ]]; then
  sources=("$SOURCE_DIR"/*.{webp,WEBP})
fi

for src in "${sources[@]}"; do
  [[ -f "$src" ]] || continue
  base="$(basename "$src")"
  name="${base%.*}"
  rgba_png="$tmpdir/${name}.png"
  resized_png="$tmpdir/${name}-resized.png"

  if [[ "${src##*.}" == "webp" || "${src##*.}" == "WEBP" ]]; then
    if ! command -v dwebp >/dev/null 2>&1; then
      echo "Error: dwebp required for WebP sources." >&2
      exit 1
    fi
    decode_src="$tmpdir/${name}-decoded.png"
    dwebp -quiet "$src" -o "$decode_src"
    src="$decode_src"
  fi

  python3 "$SCRIPT_DIR/remove-white-background.py" "$src" "$rgba_png" -t "$THRESHOLD"

  sips -z "$SIZE" "$SIZE" "$rgba_png" --out "$resized_png" >/dev/null

  cwebp -quiet -q "$QUALITY" -alpha_q 100 -metadata none "$resized_png" -o "$TARGET_DIR/${name}.webp"
  echo "→ ${name}.webp (transparent)"
  count=$((count + 1))
done
shopt -u nullglob

if [[ "$count" -eq 0 ]]; then
  echo "Warning: no source images found in $SOURCE_DIR" >&2
  exit 1
fi

echo "Wrote $count transparent WebP(s) to $TARGET_DIR"
