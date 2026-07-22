#!/usr/bin/env bash
# Download official Svensk Simidrott simmärkesmaterial PDFs for maintainer extraction.
# PDFs are stored under docs/extraction/pdfs/simidrott/ (gitignored).
set -euo pipefail

usage() {
  cat <<'EOF'
Usage: scripts/extract-simidrott.sh [OPTIONS]

Download official Svensk Simidrott simmärkesaffisch and protocol PDFs from
svensksimidrott.se into docs/extraction/pdfs/simidrott/ (gitignored).

Options:
  --help    Show this help and exit

After download, run the printed pdftotext commands to extract requirement text.
catalogVersion basis: 2026.03.02 (simmärkesmaterial "Senast uppdaterad").
Source page: https://svensksimidrott.se/simkunnighet/simmarken/simmarkesmaterial
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

BASE_URL="https://svensksimidrott.se"
MATERIAL_PAGE="${BASE_URL}/simkunnighet/simmarken/simmarkesmaterial"
OUTPUT_DIR="docs/extraction/pdfs/simidrott"

# filename|url pairs (avoid bash 3.x associative arrays on macOS)
PDF_LIST=(
  "simmarkesaffisch.pdf|${BASE_URL}/download/18.2834ddaa18d0e8137e44ef74/1705584326390/simm%C3%A4rkesaffisch%202024.pdf"
  "1.baddaren protokoll.pdf|${BASE_URL}/download/18.403cacd3182adc241aa8e14/1660824188539/1.baddaren%20protokoll.pdf"
  "2.skoldpaddan protokoll.pdf|${BASE_URL}/download/18.403cacd3182adc241aa8e15/1660824188560/2.skoldpaddan%20protokoll.pdf"
  "3.pingvinen protokoll.pdf|${BASE_URL}/download/18.403cacd3182adc241aa8e16/1660824188577/3.pingvinen%20protokoll.pdf"
  "4.silverfisken protokoll.pdf|${BASE_URL}/download/18.403cacd3182adc241aa8e17/1660824188596/4.silverfisken%20protokoll.pdf"
  "5.guldfisken protokoll.pdf|${BASE_URL}/download/18.403cacd3182adc241aa8e18/1660824188612/5.guldfisken%20protokoll.pdf"
  "6.hajen protokoll.pdf|${BASE_URL}/download/18.403cacd3182adc241aa8e19/1660824188630/6.hajen%20protokoll.pdf"
  "11.markesprotokoll-grupp.pdf|${BASE_URL}/download/18.275257001825758a516dfea2/1660652534445/11.markesprotokoll-grupp.pdf"
  "12.markesprotokoll.pdf|${BASE_URL}/download/18.275257001825758a516dfea3/1660652534460/12.markesprotokoll.pdf"
)

mkdir -p "${OUTPUT_DIR}"

echo "Downloading Simidrott PDFs from ${MATERIAL_PAGE}"
echo "Output directory: ${OUTPUT_DIR}"
echo

for entry in "${PDF_LIST[@]}"; do
  filename="${entry%%|*}"
  url="${entry#*|}"
  dest="${OUTPUT_DIR}/${filename}"
  echo "→ ${filename}"
  curl -fsSL -o "${dest}" "${url}"
  checksum="$(sha256_file "${dest}")"
  echo "  SHA-256: ${checksum}"
done

echo
echo "Downloads complete. Extract text with:"
echo
for entry in "${PDF_LIST[@]}"; do
  filename="${entry%%|*}"
  dest="${OUTPUT_DIR}/${filename}"
  echo "  pdftotext -layout \"${dest}\" \"${dest%.pdf}.txt\""
done
echo
echo "Affisch page 2 contains simmärkesbestämmelser (requirement bullets)."
echo "Protocol PDFs are matrix forms — cross-check bullet counts against affisch."
