#!/usr/bin/env bash
# Download SLS shop product images and convert to WebP badge assets.
set -euo pipefail

SHOP_BASE="https://shop.svenskalivraddningssallskapet.se"
CROP_DIR="/tmp/sls-badge-crops"
OUTPUT_DIR="app/src/main/assets/badges/sls"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# product-slug|output-filename (without .webp)
PRODUCT_LIST=(
  "droppen|droppen"
  "skraddaren|skraddaren"
  "doppingen|doppingen_turkos"
  "doppingen-bla|doppingen_bla_utomhus"
  "livbojen-bla|livbojen_bla"
  "livbojen-rod|livbojen_rod"
  "livbojen-gron|livbojen_gron"
  "krabban-bla|krabban_bla"
  "krabban-rod|krabban_rod"
  "uttern-jarn|uttern_jarn"
  "uttern-brons|uttern_brons"
  "krokodilen-silver|krokodilen_silver"
  "krokodilen-guld|krokodilen_guld"
  "silvergrodan|silvergrodan"
  "guldgrodan|guldgrodan"
  "silversalen|silversalen"
  "guldsalen|guldsalen"
)

usage() {
  cat <<'EOF'
Usage: scripts/extract-sls-badge-images.sh [OPTIONS]

Download official SLS shop product images and convert to WebP in
app/src/main/assets/badges/sls/.

Options:
  --help    Show this help and exit

Uses shop product JSON image URLs (same products as extract-sls.sh).
EOF
}

if [[ "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "Error: python3 required to parse shop JSON." >&2
  exit 1
fi

mkdir -p "$CROP_DIR"
rm -f "$CROP_DIR"/*.png "$CROP_DIR"/*.jpg 2>/dev/null || true

for entry in "${PRODUCT_LIST[@]}"; do
  slug="${entry%%|*}"
  name="${entry#*|}"
  json_url="${SHOP_BASE}/products/${slug}.json"
  echo "→ ${slug}"
  json="$(curl -fsSL --max-time 30 "$json_url")"
  img_url="$(printf '%s' "$json" | python3 -c "
import json, sys
data = json.load(sys.stdin)
product = data.get('product', data)
images = product.get('images', [])
if not images:
    sys.exit(1)
print(images[0]['src'])
")"
  # Request a reasonable size from Shopify CDN
  img_url="${img_url//&width=[0-9]*/}"
  if [[ "$img_url" == *"?"* ]]; then
    img_url="${img_url}&width=512"
  else
    img_url="${img_url}?width=512"
  fi
  ext="jpg"
  [[ "$img_url" == *.png* ]] && ext="png"
  dest="$CROP_DIR/${name}.${ext}"
  curl -fsSL --max-time 30 -o "$dest" "$img_url"
  echo "  Saved ${name}.${ext}"
done

"$SCRIPT_DIR/convert-badge-images.sh" "$CROP_DIR" "$OUTPUT_DIR"
echo "SLS badge images ready in $OUTPUT_DIR"
