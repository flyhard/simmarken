# SLS Source Notes — D-06 Coverage Strategy

**catalogVersion basis:** `2026.07.22` (shop product verification date)  
**Extraction script:** `scripts/extract-sls.sh`  
**Primary shop:** [Sim- och livräddningsmärken](https://shop.svenskalivraddningssallskapet.se/collections/sim-och-livraddningsmarken)

## Authoritative Source Policy (D-09)

SLS does **not** publish per-badge protocol PDFs equivalent to Svensk Simidrott's simmärkesmaterial page. Requirement text for v1 comes from **official SLS webshop product descriptions** (`body_html` Vad/Varför/Hur sections). Confidence is **MEDIUM** — shop copy is official SLS material but not affisch-grade protocol PDFs.

**The Göteborgs-Posten (GP) simmärken article is NOT authoritative for D-09 verbatim wording.** It may be used only as a tertiary cross-check during D-12 manual review. Do not seed requirements from GP paraphrase.

- GP article (excluded as primary): https://www.gp.se/nyheter/sverige/har-ar-alla-simmarken-och-krav-flera-nya-2025.fa1c2ddc-aed7-47f5-ab97-50afed51ad2c

## Planned Badge Inventory

| code | nameSv | Source URL | Confidence | textSv basis |
|------|--------|------------|------------|--------------|
| droppen | Droppen | https://shop.svenskalivraddningssallskapet.se/products/droppen | MEDIUM | Shop Hur: inga kunskapskrav |
| skraddaren | Skräddaren | https://shop.svenskalivraddningssallskapet.se/products/skraddaren | MEDIUM | Shop Vad bullets |
| doppingen-turkos | Doppingen Turkos | https://shop.svenskalivraddningssallskapet.se/products/doppingen | MEDIUM | Shop Vad bullets |
| doppingen-bla-utomhus | Doppingen Blå - utomhus | https://shop.svenskalivraddningssallskapet.se/products/doppingen-bla | MEDIUM | Shop Vad bullets |
| livbojen-bla | Livbojen Blå | https://shop.svenskalivraddningssallskapet.se/products/livbojen-bla | MEDIUM | Shop Vad bullets |
| livbojen-rod | Livbojen Röd | https://shop.svenskalivraddningssallskapet.se/products/livbojen-rod | MEDIUM | Shop Vad bullets |
| livbojen-gron | Livbojen Grön | https://shop.svenskalivraddningssallskapet.se/products/livbojen-gron | MEDIUM | Shop Vad bullets |
| krabban-bla | Krabban Blå | https://shop.svenskalivraddningssallskapet.se/products/krabban-bla | MEDIUM | Shop Vad bullets |
| krabban-rod | Krabban Röd | https://shop.svenskalivraddningssallskapet.se/products/krabban-rod | MEDIUM | Shop Vad bullets |
| uttern-jarn | Uttern Järn | https://shop.svenskalivraddningssallskapet.se/products/uttern-jarn | MEDIUM | Shop Vad bullets |
| uttern-brons | Uttern Brons | https://shop.svenskalivraddningssallskapet.se/products/uttern-brons | MEDIUM | Shop Vad bullets |
| krokodilen-silver | Krokodilen Silver | https://shop.svenskalivraddningssallskapet.se/products/krokodilen-silver | MEDIUM | Shop Vad bullets |
| krokodilen-guld | Krokodilen Guld | https://shop.svenskalivraddningssallskapet.se/products/krokodilen-guld | MEDIUM | Shop Vad bullets |
| silvergrodan | Silvergrodan | https://shop.svenskalivraddningssallskapet.se/products/silvergrodan | MEDIUM | Shop Vad bullets |
| guldgrodan | Guldgrodan | https://shop.svenskalivraddningssallskapet.se/products/guldgrodan | MEDIUM | Shop Vad bullets |
| silversalen | Silversälen | https://shop.svenskalivraddningssallskapet.se/products/silversalen | MEDIUM | Shop Vad bullets |
| guldsalen | Guldsälen | https://shop.svenskalivraddningssallskapet.se/products/guldsalen | MEDIUM | Shop Vad bullets |

## Excluded Badges (LOW confidence or out of scope)

| nameSv | Reason |
|--------|--------|
| Grodan Rygg Blå / Grön | Rygg-variant progression; no standalone Vad bullets verified separate from main Grodan line in v1 |
| Vädermärket | Simborgar/weather badge — outside main progression per D-05 |
| Simborgarmärken (annual pins) | https://shop.svenskalivraddningssallskapet.se/collections/simborgarmarken — out of core scope |
| Magister-tier badges | Not in RESEARCH recommended v1 subset |

No badge is included on LOW-only confidence without an exclusion note above.

## Category Grouping (validated 2026-07-22)

Stable ASCII `code` slugs with shop-aligned `nameSv`:

| code | nameSv | nameEn | sortOrder |
|------|--------|--------|-----------|
| grund | Grund | Foundation | 1 |
| doppingen | Doppingen | Doppingen | 2 |
| livbojen | Livbojen | Lifebuoy | 3 |
| krabban | Krabban | Crab | 4 |
| uttern | Uttern | Otter | 5 |
| krokodilen | Krokodilen | Crocodile | 6 |
| grodan | Grodan | Frog | 7 |
| sal | Säl | Seal | 8 |

## Licensing Handoff (02-04)

Shop product images may be used for `imageAssetPath` WebP bundling. Document final licensing in `docs/SOURCES.md` — not in DB schema (D-03, D-16).
