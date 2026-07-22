#!/usr/bin/env bash
# Fetch SLS simmärken pages and shop product JSON for maintainer extraction.
# Snapshots are stored under docs/extraction/pdfs/sls/ (gitignored).
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/extract-sls.sh [OPTIONS]

Download SLS simmärken landing page and shop product JSON snapshots into
docs/extraction/pdfs/sls/ (gitignored).

Options:
  --help    Show this help and exit

SLS lacks Simidrott-equivalent protocol PDFs. Requirement text is extracted
from official shop product descriptions (MEDIUM confidence per D-06).

Sources:
  https://svenskalivraddningssallskapet.se/simmarken/
  https://shop.svenskalivraddningssallskapet.se/collections/sim-och-livraddningsmarken

If svenskalivraddningssallskapet.se times out, retry later or use shop JSON
endpoints only: https://shop.svenskalivraddningssallskapet.se/products/<slug>.json
EOF
}

sha256_file() {
  if command -v shasum >/dev/null 2>&1; then
    shasum -a 256 "$1" | awk '{print $1}'
  elif command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$1" | awk '{print $1}'
  else
    echo "sha256-unavailable"
  fi
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

SHOP_BASE="https://shop.svenskalivraddningssallskapet.se"
SLS_PAGE="https://svenskalivraddningssallskapet.se/simmarken/"
SHOP_COLLECTION="${SHOP_BASE}/collections/sim-och-livraddningsmarken"
OUTPUT_DIR="docs/extraction/pdfs/sls"

# product-slug|seed-badge-code pairs
PRODUCT_LIST=(
  "droppen|droppen"
  "skraddaren|skraddaren"
  "doppingen|doppingen-turkos"
  "doppingen-bla|doppingen-bla-utomhus"
  "livbojen-bla|livbojen-bla"
  "livbojen-rod|livbojen-rod"
  "livbojen-gron|livbojen-gron"
  "krabban-bla|krabban-bla"
  "krabban-rod|krabban-rod"
  "uttern-jarn|uttern-jarn"
  "uttern-brons|uttern-brons"
  "krokodilen-silver|krokodilen-silver"
  "krokodilen-guld|krokodilen-guld"
  "silvergrodan|silvergrodan"
  "guldgrodan|guldgrodan"
  "silversalen|silversalen"
  "guldsalen|guldsalen"
)

mkdir -p "${OUTPUT_DIR}"

echo "Fetching SLS sources into ${OUTPUT_DIR}"
echo

echo "→ simmarken landing page"
if curl -fsSL --max-time 30 -o "${OUTPUT_DIR}/simmarken.html" "${SLS_PAGE}"; then
  checksum="$(sha256_file "${OUTPUT_DIR}/simmarken.html")"
  echo "  Saved simmarken.html (SHA-256: ${checksum})"
else
  echo "  WARNING: ${SLS_PAGE} timed out or failed."
  echo "  Continue with shop product JSON only — see SLS-SOURCE-NOTES.md."
fi

echo "→ shop collection page"
if curl -fsSL --max-time 30 -o "${OUTPUT_DIR}/shop-collection.html" "${SHOP_COLLECTION}"; then
  checksum="$(sha256_file "${OUTPUT_DIR}/shop-collection.html")"
  echo "  Saved shop-collection.html (SHA-256: ${checksum})"
else
  echo "  WARNING: shop collection fetch failed."
fi

echo
echo "Downloading shop product JSON snapshots:"
for entry in "${PRODUCT_LIST[@]}"; do
  slug="${entry%%|*}"
  code="${entry#*|}"
  dest="${OUTPUT_DIR}/${slug}.json"
  url="${SHOP_BASE}/products/${slug}.json"
  echo "→ ${slug} (${code})"
  curl -fsSL --max-time 30 -o "${dest}" "${url}"
  checksum="$(sha256_file "${dest}")"
  echo "  SHA-256: ${checksum}"
done

echo
echo "Downloads complete. Parse product body_html Vad/Varför/Hur sections into requirements."
echo "See docs/extraction/SLS-SOURCE-NOTES.md for confidence ratings and excluded badges."
echo "Do NOT use GP article text as primary source (D-09)."
