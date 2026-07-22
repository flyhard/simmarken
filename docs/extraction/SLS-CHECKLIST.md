# SLS Core Scope — D-12 Verification Checklist

**catalogVersion basis:** `2026.07.22` (shop product verification)  
**Sources:** [SLS shop — sim- och livräddningsmärken](https://shop.svenskalivraddningssallskapet.se/collections/sim-och-livraddningsmarken), product JSON via `scripts/extract-sls.sh`  
**Excluded per D-05/D-06:** Simborgarmärken, Vädermärket, Grodan Rygg variants, Magister-tier badges

| code | nameSv | shop product | req count | imageAssetPath | verified |
|------|--------|--------------|-----------|----------------|----------|
| droppen | Droppen | droppen | 1 | badges/sls/droppen.webp | [ ] |
| skraddaren | Skräddaren | skraddaren | 2 | badges/sls/skraddaren.webp | [ ] |
| doppingen-turkos | Doppingen Turkos | doppingen | 3 | badges/sls/doppingen_turkos.webp | [ ] |
| doppingen-bla-utomhus | Doppingen Blå - utomhus | doppingen-bla | 4 | badges/sls/doppingen_bla_utomhus.webp | [ ] |
| livbojen-bla | Livbojen Blå | livbojen-bla | 2 | badges/sls/livbojen_bla.webp | [ ] |
| livbojen-rod | Livbojen Röd | livbojen-rod | 4 | badges/sls/livbojen_rod.webp | [ ] |
| livbojen-gron | Livbojen Grön | livbojen-gron | 3 | badges/sls/livbojen_gron.webp | [ ] |
| krabban-bla | Krabban Blå | krabban-bla | 1 | badges/sls/krabban_bla.webp | [ ] |
| krabban-rod | Krabban Röd | krabban-rod | 1 | badges/sls/krabban_rod.webp | [ ] |
| uttern-jarn | Uttern Järn | uttern-jarn | 1 | badges/sls/uttern_jarn.webp | [ ] |
| uttern-brons | Uttern Brons | uttern-brons | 1 | badges/sls/uttern_brons.webp | [ ] |
| krokodilen-silver | Krokodilen Silver | krokodilen-silver | 3 | badges/sls/krokodilen_silver.webp | [ ] |
| krokodilen-guld | Krokodilen Guld | krokodilen-guld | 3 | badges/sls/krokodilen_guld.webp | [ ] |
| silvergrodan | Silvergrodan | silvergrodan | 3 | badges/sls/silvergrodan.webp | [ ] |
| guldgrodan | Guldgrodan | guldgrodan | 3 | badges/sls/guldgrodan.webp | [ ] |
| silversalen | Silversälen | silversalen | 2 | badges/sls/silversalen.webp | [ ] |
| guldsalen | Guldsälen | guldsalen | 2 | badges/sls/guldsalen.webp | [ ] |

**Notes**

- Requirement bullets parsed from shop `body_html` **Vad:** sections (MEDIUM confidence per D-06).
- Droppen has no skill bullets — seeded with official Hur text: "För att ta Droppen finns inga kunskapskrav."
- GP article text must not be used for verification — shop product pages only.
- Run `bash scripts/extract-sls.sh` before re-verifying after shop updates.
