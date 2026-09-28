# ADR-0017: Store graphics generated from the launcher icon

- **Status:** Proposed
- **Date:** 2026-09-27
- **Related:** PRD-0002 (RELE-05, RELE-06), ADR-0015

## Context

The Play store listing needs a 512×512 app icon (32-bit PNG with alpha), a
1024×500 feature graphic (PNG or JPEG, no alpha) and at least two phone
screenshots (flyhard/simmmarken#6). Hand-made artwork drifts from the app: the
launcher icon (RELE-06) is an adaptive icon defined as XML vector drawables, and
the store icon should look exactly like it. Screenshots are taken by hand, and
Play rejects them only at upload time if they break its size or ratio rules.

## Decision

- We add `scripts/store-graphics.py`, the single source of truth for the store
  graphics. The PNGs are build outputs in the git-ignored `build/store-graphics/`.
- `generate` reads `res/mipmap-anydpi/ic_launcher.xml`, turns its background
  colour and foreground `<vector>` into SVG, and rasterises with
  **rsvg-convert** (librsvg):
  - **Play icon:** the launcher's visible 72 dp area of the 108 dp canvas,
    full bleed, 512×512 RGBA. Play applies its own corner mask.
  - **Feature graphic:** brand background, the badge artwork and the app name
    **Simmärken** in bold white system sans-serif. No promotional text.
- The script depends only on **Python 3.9+ standard library** and
  rsvg-convert. It decodes rsvg's PNG and re-encodes it with fixed settings
  (filter 0, zlib level 9, no metadata chunks), so the output is byte-identical
  between runs on the same machine. It also drops the feature graphic's alpha
  channel.
- `check-screenshots DIR` checks hand-taken screenshots against Play's phone
  rules (2–8 files, PNG/JPEG, no alpha, sides 320–3840 px, long side ≤ 2× short
  side, ≤ 8 MB) and warns below 1080 px. It reads image headers itself, so it
  needs no image library.
- Unsupported vector features (clip paths, gradients, translucent colours)
  make the script fail loudly instead of rendering something different from
  the launcher.
- The script lives outside Gradle and CI. Its unit tests run with
  `python3 -m unittest discover -s scripts/tests`.

## Alternatives considered

- **Pillow** (as used by `scripts/remove-white-background.py`) — has no vector
  path rendering. We would still need an SVG rasteriser, and a pip install on
  top.
- **ImageMagick** — SVG rendering depends on which delegate a build has
  (internal MSVG, librsvg or Inkscape), so output differs between Homebrew and
  distro packages.
- **Inkscape / CairoSVG** — heavier installs (Inkscape) or a pip dependency
  plus native Cairo (CairoSVG) for no gain over librsvg.
- **A Gradle task using AGP's vector-drawable renderer** — ties a maintainer
  script to AGP internals and puts store artwork into the app build.
- **Hand-made artwork in a design tool** — drifts from the launcher icon and
  cannot be regenerated after an icon change.

## Consequences

- An icon change (RELE-06) is followed by one command to refresh the store
  graphics.
- Text in the feature graphic uses whichever bold sans-serif fontconfig picks
  (Roboto, Helvetica, Arial or DejaVu Sans, in that order). Output is
  reproducible per machine, but glyphs may differ slightly between macOS and
  Linux. Commit the generated PNGs if an exact reference is needed.
- If the icon gains vector features the converter doesn't handle, the script
  must be extended (it says so rather than mis-render).
- Play's screenshot rules are hard-coded; if Play changes them, update the
  constants at the top of the script.
