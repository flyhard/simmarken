# Simidrott Core Scope — D-12 Verification Checklist

**catalogVersion basis:** `2026.03.02` (simmärkesmaterial "Senast uppdaterad")  
**Sources:** [Simmärkesmaterial](https://svensksimidrott.se/simkunnighet/simmarken/simmarkesmaterial), simmärkesaffisch 2024 PDF, protocol PDFs per group  
**Excluded per D-05:** Advanced affisch tiers (D-05), Vattenpolo, Simborgarmärken, Vattenprovet, Utmanarpokal, Plumsaren, Sarahmärket

| code | nameSv | protocol PDF | req count | imageAssetPath | verified |
|------|--------|--------------|-----------|----------------|----------|
| baddaren-gron | Baddaren Grön | 1.baddaren protokoll.pdf | 2 | badges/simidrott/baddaren_gron.webp | [ ] |
| baddaren-bla | Baddaren Blå | 1.baddaren protokoll.pdf | 3 | badges/simidrott/baddaren_bla.webp | [ ] |
| baddaren-gul | Baddaren Gul | 1.baddaren protokoll.pdf | 3 | badges/simidrott/baddaren_gul.webp | [ ] |
| skoldpaddan | Sköldpaddan | 2.skoldpaddan protokoll.pdf | 1 | badges/simidrott/skoldpaddan.webp | [ ] |
| blackfisken | Bläckfisken | 2.skoldpaddan protokoll.pdf | 1 | badges/simidrott/blackfisken.webp | [ ] |
| pingvinen-silver | Pingvinen Silver | 3.pingvinen protokoll.pdf | 4 | badges/simidrott/pingvinen_silver.webp | [ ] |
| pingvinen-guld | Pingvinen Guld | 3.pingvinen protokoll.pdf | 4 | badges/simidrott/pingvinen_guld.webp | [ ] |
| simsattmarke-1 | Simsättsmärke 1 | 3.pingvinen protokoll.pdf | 1 | null (tier fallback) | [ ] |
| simsattmarke-2 | Simsättsmärke 2 | 3.pingvinen protokoll.pdf | 1 | null (tier fallback) | [ ] |
| silverfisken | Silverfisken | 4.silverfisken protokoll.pdf | 2 | badges/simidrott/silverfisken.webp | [ ] |
| guldfisken | Guldfisken | 5.guldfisken protokoll.pdf | 2 | badges/simidrott/guldfisken.webp | [ ] |
| hajen-brons | Hajen Brons | 6.hajen protokoll.pdf | 1 | badges/simidrott/hajen_brons.webp | [ ] |
| hajen-silver | Hajen Silver | 6.hajen protokoll.pdf | 2 | badges/simidrott/hajen_silver.webp | [ ] |
| hajen-guld | Hajen Guld | 6.hajen protokoll.pdf | 6 | badges/simidrott/hajen_guld.webp | [ ] |
| simsattmarke-3 | Simsättsmärke 3 | 6.hajen protokoll.pdf | 3 | null (tier fallback) | [ ] |
| simsattmarke-4 | Simsättsmärke 4 | 6.hajen protokoll.pdf | 2 | null (tier fallback) | [ ] |
| jarnmarket | Järnmärket | simmärkesaffisch (bestämmelser) | 4 | badges/simidrott/jarnmarket.webp | [ ] |
| bronsmarket | Bronsmärket | simmärkesaffisch (bestämmelser) | 5 | badges/simidrott/bronsmarket.webp | [ ] |
| silvermarket | Silvermärket | simmärkesaffisch (bestämmelser) | 7 | badges/simidrott/silvermarket.webp | [ ] |
| kandidaten | Kandidaten | simmärkesaffisch (bestämmelser) | 10 | null (tier fallback) | [ ] |

**Notes**

- `guldmarket` omitted: 2024 affisch and markesprotokoll list Järnmärket → Bronsmärket → Silvermärket → Kandidaten; no separate Guldmärket pin in core progression.
- Badges with `imageAssetPath: null` use tier-color fallback in Phase 4 (D-04).
- Requirement counts follow affisch simmärkesbestämmelser 2024; cross-check protocol matrix rows during D-12 sign-off.
- Run `bash scripts/extract-simidrott.sh` before re-verifying after affisch updates.
