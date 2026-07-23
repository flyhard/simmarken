#!/usr/bin/env python3
"""Remove near-white backgrounds from badge product photos.

Flood-fills from image borders so only pixels connected to the edge
(white matte + soft shadow) become transparent. Badge highlights that
are not connected to the border are preserved.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError:
    print("Error: Pillow required. Install with: pip3 install Pillow", file=sys.stderr)
    sys.exit(1)


def border_seed_points(width: int, height: int, step: int = 8) -> list[tuple[int, int]]:
    points: list[tuple[int, int]] = [
        (0, 0),
        (width - 1, 0),
        (0, height - 1),
        (width - 1, height - 1),
    ]
    for x in range(0, width, step):
        points.append((x, 0))
        points.append((x, height - 1))
    for y in range(0, height, step):
        points.append((0, y))
        points.append((width - 1, y))
    return points


def remove_white_background(image: Image.Image, threshold: int) -> Image.Image:
    rgba = image.convert("RGBA")
    width, height = rgba.size
    marker = (255, 0, 255, 0)
    for point in border_seed_points(width, height):
        if rgba.getpixel(point)[3] == 0:
            continue
        ImageDraw.floodfill(rgba, point, marker, thresh=threshold)

    pixels = rgba.load()
    for y in range(height):
        for x in range(width):
            if pixels[x, y] == marker:
                pixels[x, y] = (0, 0, 0, 0)

    return rgba


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path, help="Input image (PNG/JPEG/WebP)")
    parser.add_argument("output", type=Path, help="Output PNG with alpha")
    parser.add_argument(
        "-t",
        "--threshold",
        type=int,
        default=40,
        help="Flood-fill color tolerance (default: 40)",
    )
    args = parser.parse_args()

    if not args.input.is_file():
        print(f"Error: input not found: {args.input}", file=sys.stderr)
        return 1

    args.output.parent.mkdir(parents=True, exist_ok=True)
    with Image.open(args.input) as image:
        result = remove_white_background(image, args.threshold)
        result.save(args.output, format="PNG")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
