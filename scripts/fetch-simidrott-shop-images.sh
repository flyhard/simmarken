#!/usr/bin/env bash
# Download Simidrott badge product images from privat.ssfshopen.se (images.neh.com)
# and convert to WebP for app assets.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="$ROOT/app/src/main/assets/badges/simidrott"
BASE_URL="https://images.neh.com/upload/test//high"

mkdir -p "$OUT_DIR"

# output_filename<TAB>article_id
while IFS=$'\t' read -r filename article_id; do
  [[ "$filename" =~ ^#.*$ || -z "$filename" ]] && continue
  jpg="$OUT_DIR/${filename%.webp}.jpg"
  webp="$OUT_DIR/$filename"
  url="$BASE_URL/${article_id}.jpg"
  echo "Fetching $filename ($article_id)..."
  curl -fsSL "$url" -o "$jpg"
  cwebp -quiet "$jpg" -o "$webp"
  rm -f "$jpg"
done <<'MANIFEST'
sjohasten.webp	SVSF2518
fjarilsim.webp	SVSF11500
brostsim.webp	SVSF11600
ryggsim.webp	SVSF11700
crawl.webp	SVSF11800
kandidaten.webp	SVSF14400
jarnmagistern.webp	SVSF14000
bronsmagistern.webp	SVSF14100
silvermagistern.webp	SVSF14200
guldmagistern.webp	SVSF14300
vattenprovet_majblomman_2026.webp	SVSF26400
sallyprovet_oppet_vatten.webp	SVSF243
simborgarmarke_2026.webp	SVSF2600
simborgarmarke_5_ar.webp	SVSF17500
simborgarmarke_10_ar.webp	SVSF17600
simborgarmarke_25_ar.webp	SVSF17700
simborgarmarke_40_ar.webp	SVSF17800
simborgarmarke_50_ar.webp	SVSF17900
km_1.webp	SVSF16000
km_5.webp	SVSF16100
km_10.webp	SVSF16200
km_25.webp	SVSF16300
km_50.webp	SVSF16400
km_100.webp	SVSF16500
km_250.webp	SVSF16600
km_500.webp	SVSF16700
km_1000.webp	SVSF16800
vattenpolobollen_brons.webp	SVSF49400
vattenpolobollen_silver.webp	SVSF49500
vattenpolobollen_guld.webp	SVSF49600
simhopp_jarn.webp	SVSF49000
simhopp_brons.webp	SVSF49100
simhopp_silver.webp	SVSF49200
simhopp_guld.webp	SVSF49300
plumsaren.webp	SVSF48900
sarahmarket.webp	SVSF51000
MANIFEST

echo "Done. $(ls -1 "$OUT_DIR"/*.webp 2>/dev/null | wc -l | tr -d ' ') WebP files in $OUT_DIR"
