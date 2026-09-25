# Catalog and Image Sources

Provenance for bundled badge catalogs and pin artwork. Source URLs and extraction dates live here only — not in the Room schema (`D-16`).

**Requirements extraction date:** 2026-07-22  
**Image extraction date:** 2026-07-23  
**Affisch version basis:** 2026.03.02 (`simmärkesmaterial` last updated)

---

## Svensk Simidrott

### Official pages

| Resource | URL |
|----------|-----|
| Simmärken overview | https://svensksimidrott.se/simkunnighet/simmarken |
| Simmärkesmaterial (protocols + affisch) | https://svensksimidrott.se/simkunnighet/simmarken/simmarkesmaterial |
| Simmärkesaffisch 2024 PDF | https://svensksimidrott.se/download/18.2834ddaa18d0e8137e44ef74/1705584326390/simm%C3%A4rkesaffisch%202024.pdf |
| 2026 affisch announcement | https://svensksimidrott.se/nyheter-svensk-simidrott/svensk-simidrott/2026-02-11-nu-ar-2026-ars-simmarkesaffischer-och-folder-har |
| Official shop — simmärken | https://privat.ssfshopen.se/marken-simmarken |

### Licensing (promotional use)

Svensk Simidrott's simmärkesmaterial page states that the material may be downloaded and used for marketing and promotion of simmärken (swimming badges). Requirement text in this app comes from the official simmärkesmaterial protocols and affisch downloaded from that page.

Badge pin artwork is taken from official product photos on the [Svensk Simidrott shop](https://privat.ssfshopen.se/marken-simmarken) (NeH e-commerce, `images.neh.com`), used solely to help parents identify official badges their children have earned. White studio backgrounds are removed in post-processing; see **Images (Bilder)** below.

Maintainer scripts: `scripts/extract-simidrott.sh` (requirements) · `scripts/apply-transparent-badge-backgrounds.sh` (images)  
Seed JSON: `app/src/main/assets/seed/simidrott.json` (`catalogVersion`: `2026.03.02`)

---

## SLS (Svenska Livräddningssällskapet)

### Official pages

| Resource | URL |
|----------|-----|
| Simmärken landing page | https://svenskalivraddningssallskapet.se/simmarken/ |
| Shop — sim- och livräddningsmärken | https://shop.svenskalivraddningssallskapet.se/collections/sim-och-livraddningsmarken |

### Confidence notes

SLS does not publish per-badge protocol PDFs equivalent to Simidrott's simmärkesmaterial. Requirement text comes from official shop product descriptions (`body_html` Vad/Varför/Hur sections) at **MEDIUM** confidence. See `docs/extraction/SLS-SOURCE-NOTES.md` for per-badge source URLs and confidence ratings.

**GP article excluded:** The Göteborgs-Posten simmärken article is not used as a primary source for requirements or images. It may appear only as a tertiary cross-check during manual D-12 review — not as authoritative data.

Maintainer download script: `scripts/extract-sls.sh`  
Seed JSON: `app/src/main/assets/seed/sls.json` (`catalogVersion`: `2026.07.22`)

---

## Images (Bilder)

Badge pin images are official product photos per `D-01` and `D-02`:

| Catalog | Source material | Output path |
|---------|-----------------|-------------|
| Svensk Simidrott | [SSF shop](https://privat.ssfshopen.se/marken-simmarken) product images (`images.neh.com/.../high/`) | `app/src/main/assets/badges/simidrott/*.webp` |
| SLS | [SLS shop](https://shop.svenskalivraddningssallskapet.se/collections/sim-och-livraddningsmarken) product images | `app/src/main/assets/badges/sls/*.webp` |

### Format and dimensions

- **Format:** WebP with alpha (`cwebp -q 85 -alpha_q 100`)
- **Background:** White studio matte removed via border flood-fill (`scripts/remove-white-background.py`)
- **Grid size:** 256×256 px (detail views may use the same asset scaled by Coil in Phase 4)
- **Target grid reference:** ~128 px display; 256 px source for retina (`ARCHITECTURE.md`)
- **Metadata:** EXIF stripped during `cwebp` conversion (`-metadata none`)
- **APK budget:** Total badge assets under 5 MB; individual files targeted under 50 KB

Conversion scripts: `scripts/convert-badge-images.sh` · `scripts/apply-transparent-badge-backgrounds.sh`  
Legacy affisch crop mapping (superseded for images): `scripts/extract-simidrott-badge-images.sh`

Paths in seed JSON use the convention `badges/{catalog}/{badge_code_underscores}.webp` (e.g. `baddaren-gron` → `baddaren_gron.webp`).

---

## catalogVersion

Each bundled seed JSON carries a `catalogVersion` string on the root catalog object. The `CatalogSeedLoader` (Phase 02-03) compares this to the stored `CatalogEntity.catalogVersion` and triggers a merge when the bundled version is newer (`D-14`).

| Catalog | Current version | Format | Meaning |
|---------|-----------------|--------|---------|
| `simidrott` | `2026.03.02` | `YYYY.MM.DD` | Date of simmärkesmaterial "Senast uppdaterad" |
| `sls` | `2026.07.22` | `YYYY.MM.DD` | Date shop products were verified for v1 seed |

Image assets for both catalogs were last verified against shop product pages on **2026-07-23**.

When Svensk Simidrott publishes a new affisch, update the seed JSON `catalogVersion`, re-extract requirements from simmärkesmaterial, and document the change here. Re-verify shop product images when pins are redesigned or new products appear on [marken-simmarken](https://privat.ssfshopen.se/marken-simmarken).

---

## Gaps — tier-color fallback (D-04)

Badges with `imageAssetPath: null` in seed JSON rely on `BadgePlaceholderColors` (Phase 4 Coil fallback) — a category-tier background color with badge `nameSv` or short code overlay.

### Simidrott — no bundled image

| Badge code | nameSv | Category | Fallback tier |
|------------|--------|----------|---------------|
| `simsattmarke-1` | Simsättsmärke 1 | Nybörjare | `nyborjare` (blue) |
| `simsattmarke-2` | Simsättsmärke 2 | Nybörjare | `nyborjare` (blue) |
| `simsattmarke-3` | Simsättsmärke 3 | Nybörjare | `nyborjare` (blue) |
| `simsattmarke-4` | Simsättsmärke 4 | Nybörjare | `nyborjare` (blue) |
| `kandidaten` | Kandidaten | Guld | `guld` (gold) |

Simsättsmärken are stroke-style pins without distinct affisch artwork in the core progression grid. Kandidaten is the terminal badge — affisch artwork exists but was deferred from the v1 WebP priority set; tier fallback applies until a dedicated crop is added.

### SLS — all badges have `imageAssetPath` set

All 17 seeded SLS badges have non-null `imageAssetPath` values. Shop product images were extracted where available. Document any future gaps here if a shop image becomes unavailable.

---

## Project license

Source code is MIT-licensed (`LICENSE`). The seed requirement text and pin images listed above are **not** covered by the MIT License. See `NOTICE` for the carve-out. Image use is limited to official promotional materials as described above.
