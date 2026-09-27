#!/usr/bin/env python3
"""Generate the Play store graphics from the adaptive launcher icon, and check
hand-taken phone screenshots against Play's rules (PRD-0002 RELE-05, RELE-06).

See ADR-0017 and docs/PLAY-CONSOLE-SETUP.md step 3. Standard library only;
rasterising uses rsvg-convert (librsvg). Every PNG is re-encoded here with fixed
settings and no metadata, so the same input always gives byte-identical output.
"""

from __future__ import annotations

import argparse
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import zlib
from pathlib import Path
from xml.etree import ElementTree as ET

REPO_ROOT = Path(__file__).resolve().parent.parent
RES_DIR = REPO_ROOT / "app" / "src" / "main" / "res"
ADAPTIVE_ICON = RES_DIR / "mipmap-anydpi" / "ic_launcher.xml"
DEFAULT_OUT = REPO_ROOT / "build" / "store-graphics"

APP_NAME = "Simmärken"
ANDROID_NS = "{http://schemas.android.com/apk/res/android}"

ICON_SIZE = 512
FEATURE_WIDTH, FEATURE_HEIGHT = 1024, 500

# Play Console phone-screenshot rules.
SCREENSHOT_MIN_COUNT = 2
SCREENSHOT_MAX_COUNT = 8
SCREENSHOT_MIN_SIDE = 320
SCREENSHOT_MAX_SIDE = 3840
SCREENSHOT_MAX_RATIO = 2.0  # long side at most twice the short side
SCREENSHOT_MAX_BYTES = 8 * 1024 * 1024
SCREENSHOT_RECOMMENDED_SIDE = 1080  # below this Play won't use them for promotion

HELP_EPILOG = f"""\
commands:
  generate                 Write play-icon.png ({ICON_SIZE}x{ICON_SIZE}, RGBA) and
                           feature-graphic.png ({FEATURE_WIDTH}x{FEATURE_HEIGHT}, RGB, no alpha)
                           to build/store-graphics/ (or --out).
  check-screenshots DIR    Check the phone screenshots in DIR against Play's rules:
                           {SCREENSHOT_MIN_COUNT}-{SCREENSHOT_MAX_COUNT} PNG or JPEG files, no alpha, each side
                           {SCREENSHOT_MIN_SIDE}-{SCREENSHOT_MAX_SIDE} px, long side at most {SCREENSHOT_MAX_RATIO:g}x the short
                           side, at most 8 MB each.

Screenshots are taken by hand on a phone or emulator. The store listing is
public: use MADE-UP child names only, never real ones, and no real photos.

requires: python3 (3.9+), rsvg-convert
  macOS:          brew install librsvg
  Debian/Ubuntu:  sudo apt-get install librsvg2-bin
  Fedora:         sudo dnf install librsvg2-tools
"""


class ToolError(Exception):
    pass


# ---------------------------------------------------------------------------
# PNG reading and writing (8-bit, non-interlaced, which is what rsvg-convert
# writes and what Play accepts).
# ---------------------------------------------------------------------------

PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"


def _png_chunks(data: bytes):
    if not data.startswith(PNG_SIGNATURE):
        raise ValueError("not a PNG file")
    pos = len(PNG_SIGNATURE)
    while pos + 8 <= len(data):
        length, kind = struct.unpack(">I4s", data[pos : pos + 8])
        yield kind, data[pos + 8 : pos + 8 + length]
        pos += 12 + length


def png_header(data: bytes) -> tuple[int, int, int, int, bool]:
    """Return (width, height, bit depth, colour type, has transparency chunk)."""
    header = None
    has_trns = False
    for kind, body in _png_chunks(data):
        if kind == b"IHDR":
            header = struct.unpack(">IIBB", body[:10])
        elif kind == b"tRNS":
            has_trns = True
        elif kind == b"IDAT":
            break
    if header is None:
        raise ValueError("PNG has no IHDR chunk")
    return (*header, has_trns)


def decode_rgba_png(data: bytes) -> tuple[int, int, bytes]:
    """Decode an 8-bit RGB or RGBA PNG (rsvg-convert writes either) to RGBA pixels."""
    width, height, depth, colour, _ = png_header(data)
    if depth != 8 or colour not in (2, 6):
        raise ValueError(f"expected 8-bit RGB(A) PNG, got depth {depth} colour type {colour}")
    raw = zlib.decompress(b"".join(body for kind, body in _png_chunks(data) if kind == b"IDAT"))
    bpp = 4 if colour == 6 else 3
    stride = width * bpp
    out = bytearray(height * stride)
    prev = bytearray(stride)
    pos = 0
    for y in range(height):
        ftype = raw[pos]
        line = bytearray(raw[pos + 1 : pos + 1 + stride])
        pos += 1 + stride
        for i in range(stride):
            a = line[i - bpp] if i >= bpp else 0
            b = prev[i]
            c = prev[i - bpp] if i >= bpp else 0
            if ftype == 1:
                line[i] = (line[i] + a) & 0xFF
            elif ftype == 2:
                line[i] = (line[i] + b) & 0xFF
            elif ftype == 3:
                line[i] = (line[i] + ((a + b) >> 1)) & 0xFF
            elif ftype == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                pred = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                line[i] = (line[i] + pred) & 0xFF
            elif ftype != 0:
                raise ValueError(f"bad PNG filter type {ftype}")
        out[y * stride : (y + 1) * stride] = line
        prev = line
    if colour == 2:
        rgba = bytearray(width * height * 4)
        rgba[0::4], rgba[1::4], rgba[2::4] = out[0::3], out[1::3], out[2::3]
        rgba[3::4] = b"\xff" * (width * height)
        out = rgba
    return width, height, bytes(out)


def encode_png(width: int, height: int, pixels: bytes, alpha: bool) -> bytes:
    """Canonical PNG: filter 0 on every row, zlib level 9, no metadata chunks."""
    channels = 4 if alpha else 3
    stride = width * channels
    raw = b"".join(b"\x00" + pixels[y * stride : (y + 1) * stride] for y in range(height))

    def chunk(kind: bytes, body: bytes) -> bytes:
        return struct.pack(">I", len(body)) + kind + body + struct.pack(">I", zlib.crc32(kind + body))

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6 if alpha else 2, 0, 0, 0)
    return PNG_SIGNATURE + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")


def flatten_to_rgb(rgba: bytes, background: tuple[int, int, int]) -> bytes:
    out = bytearray(len(rgba) // 4 * 3)
    for i in range(len(rgba) // 4):
        r, g, b, a = rgba[i * 4 : i * 4 + 4]
        for c, (fg, bg) in enumerate(((r, background[0]), (g, background[1]), (b, background[2]))):
            # rsvg-convert writes straight (non-premultiplied) alpha.
            out[i * 3 + c] = (fg * a + bg * (255 - a) + 127) // 255
    return bytes(out)


# ---------------------------------------------------------------------------
# Adaptive icon -> SVG
# ---------------------------------------------------------------------------


def _res_values(kind: str) -> dict[str, str]:
    values = {}
    for xml in sorted((RES_DIR / "values").glob("*.xml")):
        for el in ET.parse(xml).getroot().iter(kind):
            values[el.get("name")] = (el.text or "").strip()
    return values


def resolve_color(value: str) -> str:
    """Resolve '@color/x' or an opaque '#RGB' / '#RRGGBB' / '#FFRRGGBB' to '#RRGGBB'."""
    seen = set()
    while value.startswith("@color/"):
        name = value[len("@color/") :]
        if name in seen:
            raise ToolError(f"colour reference loop at @color/{name}")
        seen.add(name)
        colors = _res_values("color")
        if name not in colors:
            raise ToolError(f"@color/{name} not found in {RES_DIR / 'values'}")
        value = colors[name]
    return android_color(value)


def android_color(value: str) -> str:
    m = re.fullmatch(r"#([0-9A-Fa-f]{3,8})", value.strip())
    if not m or len(m.group(1)) not in (3, 4, 6, 8):
        raise ToolError(f"unsupported colour value {value!r}")
    hexpart = m.group(1)
    if len(hexpart) in (3, 4):
        hexpart = "".join(ch * 2 for ch in hexpart)
    if len(hexpart) == 8:
        if hexpart[:2].upper() != "FF":
            raise ToolError(f"translucent colour {value!r} is not supported yet")
        hexpart = hexpart[2:]
    return f"#{hexpart.upper()}"


def rgb_tuple(svg_color: str) -> tuple[int, int, int]:
    h = svg_color[1:7]
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16)


_PATH_ATTRS = {
    "fillColor": "fill",
    "strokeColor": "stroke",
    "strokeWidth": "stroke-width",
    "strokeLineCap": "stroke-linecap",
    "strokeLineJoin": "stroke-linejoin",
    "strokeMiterLimit": "stroke-miterlimit",
    "fillAlpha": "fill-opacity",
    "strokeAlpha": "stroke-opacity",
}
_IGNORED_ATTRS = {"name"}


def _svg_escape(text: str) -> str:
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")


def _vector_children(el: ET.Element, source: Path) -> list[str]:
    out = []
    for child in el:
        tag = child.tag
        attrs = {k.replace(ANDROID_NS, ""): v for k, v in child.attrib.items()}
        if tag == "path":
            parts = [f'd="{_svg_escape(attrs.pop("pathData"))}"']
            if "fillColor" not in attrs:
                parts.append('fill="none"')
            for key, value in attrs.items():
                if key in _IGNORED_ATTRS:
                    continue
                if key == "fillType":
                    parts.append(f'fill-rule="{"evenodd" if value == "evenOdd" else "nonzero"}"')
                elif key in ("fillColor", "strokeColor"):
                    parts.append(f'{_PATH_ATTRS[key]}="{resolve_color(value)}"')
                elif key in _PATH_ATTRS:
                    parts.append(f'{_PATH_ATTRS[key]}="{_svg_escape(value)}"')
                else:
                    raise ToolError(f"{source.name}: <path> attribute android:{key} is not supported yet")
            out.append("<path " + " ".join(parts) + "/>")
        elif tag == "group":
            px, py = float(attrs.pop("pivotX", 0)), float(attrs.pop("pivotY", 0))
            tx, ty = float(attrs.pop("translateX", 0)), float(attrs.pop("translateY", 0))
            sx, sy = float(attrs.pop("scaleX", 1)), float(attrs.pop("scaleY", 1))
            rot = float(attrs.pop("rotation", 0))
            unknown = set(attrs) - _IGNORED_ATTRS
            if unknown:
                raise ToolError(f"{source.name}: <group> attribute(s) {sorted(unknown)} not supported yet")
            # Android applies scale and rotation about the pivot, then translation.
            transform = f"translate({tx + px} {ty + py}) rotate({rot}) scale({sx} {sy}) translate({-px} {-py})"
            out.append(f'<g transform="{transform}">' + "".join(_vector_children(child, source)) + "</g>")
        else:
            raise ToolError(f"{source.name}: <{tag}> is not supported yet; extend scripts/store-graphics.py")
    return out


def load_drawable(ref: str) -> str:
    """Return an SVG fragment for a colour or vector drawable in the 108x108 dp adaptive-icon space."""
    if ref.startswith(("@color/", "#")):
        return f'<rect width="108" height="108" fill="{resolve_color(ref)}"/>'
    if not ref.startswith("@drawable/"):
        raise ToolError(f"unsupported drawable reference {ref!r}")
    source = RES_DIR / "drawable" / f"{ref[len('@drawable/'):]}.xml"
    if not source.is_file():
        raise ToolError(f"{ref}: {source} not found (only XML vector drawables are supported)")
    root = ET.parse(source).getroot()
    if root.tag != "vector":
        raise ToolError(f"{source.name}: expected a <vector> drawable, got <{root.tag}>")
    unsupported = {"tint", "tintMode", "alpha"} & {k.replace(ANDROID_NS, "") for k in root.attrib}
    if unsupported:
        raise ToolError(f"{source.name}: <vector> attribute(s) {sorted(unsupported)} not supported yet")
    vw = float(root.get(f"{ANDROID_NS}viewportWidth"))
    vh = float(root.get(f"{ANDROID_NS}viewportHeight"))
    body = "".join(_vector_children(root, source))
    # Stretch the viewport over the 108dp adaptive-icon canvas.
    return f'<g transform="scale({108 / vw} {108 / vh})">{body}</g>'


def load_adaptive_icon() -> tuple[str, str, str]:
    """Return (background svg, foreground svg, brand colour)."""
    root = ET.parse(ADAPTIVE_ICON).getroot()
    layers = {}
    for layer in ("background", "foreground"):
        el = root.find(layer)
        if el is None or f"{ANDROID_NS}drawable" not in el.attrib:
            raise ToolError(f"{ADAPTIVE_ICON}: missing <{layer} android:drawable=...>")
        layers[layer] = el.get(f"{ANDROID_NS}drawable")
    if not layers["background"].startswith(("@color/", "#")):
        # The feature graphic uses the background as its brand colour (ADR-0017).
        raise ToolError(f"{ADAPTIVE_ICON}: the background layer must be a colour, got {layers['background']}")
    return load_drawable(layers["background"]), load_drawable(layers["foreground"]), resolve_color(layers["background"])


def icon_svg(background: str, foreground: str) -> str:
    # Launchers show the centre 72dp of the 108dp canvas; Play applies its own mask.
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{ICON_SIZE}" height="{ICON_SIZE}" '
        f'viewBox="18 18 72 72">{background}{foreground}</svg>'
    )


def feature_svg(brand: str, foreground: str) -> str:
    # The badge artwork spans x 33..75 dp of the 108 dp canvas. Draw it at 4.5 px/dp
    # (about 190 px wide) with its left edge 110 px in, and centre the app name in
    # the space to its right so the layout holds whatever width the system font has.
    scale = 4.5
    canvas = 108 * scale
    x = 110 - 33 * scale
    y = (FEATURE_HEIGHT - canvas) / 2
    text_left, text_right = 110 + 42 * scale + 40, FEATURE_WIDTH - 60
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{FEATURE_WIDTH}" height="{FEATURE_HEIGHT}" '
        f'viewBox="0 0 {FEATURE_WIDTH} {FEATURE_HEIGHT}">'
        f'<rect width="{FEATURE_WIDTH}" height="{FEATURE_HEIGHT}" fill="{brand}"/>'
        f'<g transform="translate({x} {y}) scale({scale})">{foreground}</g>'
        f'<text x="{(text_left + text_right) / 2}" y="{FEATURE_HEIGHT / 2}" text-anchor="middle" '
        f'dominant-baseline="central" '
        f'font-family="Roboto, Helvetica Neue, Helvetica, Arial, DejaVu Sans, sans-serif" '
        f'font-weight="bold" font-size="100" fill="#FFFFFF">{_svg_escape(APP_NAME)}</text>'
        f"</svg>"
    )


# ---------------------------------------------------------------------------
# Commands
# ---------------------------------------------------------------------------


def require_tools() -> str:
    rsvg = shutil.which("rsvg-convert")
    if rsvg is None:
        raise ToolError(
            "rsvg-convert not found.\n"
            "  macOS:          brew install librsvg\n"
            "  Debian/Ubuntu:  sudo apt-get install librsvg2-bin\n"
            "  Fedora:         sudo dnf install librsvg2-tools"
        )
    return rsvg


def rasterise(rsvg: str, svg: str, width: int, height: int) -> bytes:
    with tempfile.TemporaryDirectory() as tmp:
        src = Path(tmp) / "in.svg"
        src.write_text(svg, encoding="utf-8")
        result = subprocess.run(
            [rsvg, "--format=png", f"--width={width}", f"--height={height}", str(src)],
            capture_output=True,
        )
    if result.returncode != 0:
        raise ToolError(f"rsvg-convert failed: {result.stderr.decode(errors='replace').strip()}")
    w, h = png_header(result.stdout)[:2]
    if (w, h) != (width, height):
        raise ToolError(f"rsvg-convert produced {w}x{h}, expected {width}x{height}")
    return result.stdout


def cmd_generate(out_dir: Path) -> int:
    rsvg = require_tools()
    background, foreground, brand = load_adaptive_icon()
    out_dir.mkdir(parents=True, exist_ok=True)

    _, _, icon_rgba = decode_rgba_png(rasterise(rsvg, icon_svg(background, foreground), ICON_SIZE, ICON_SIZE))
    icon = encode_png(ICON_SIZE, ICON_SIZE, icon_rgba, alpha=True)

    _, _, feature_rgba = decode_rgba_png(
        rasterise(rsvg, feature_svg(brand, foreground), FEATURE_WIDTH, FEATURE_HEIGHT)
    )
    feature = encode_png(
        FEATURE_WIDTH, FEATURE_HEIGHT, flatten_to_rgb(feature_rgba, rgb_tuple(brand)), alpha=False
    )

    for name, data, expected in (
        ("play-icon.png", icon, (ICON_SIZE, ICON_SIZE, 8, 6)),
        ("feature-graphic.png", feature, (FEATURE_WIDTH, FEATURE_HEIGHT, 8, 2)),
    ):
        if png_header(data)[:4] != expected:
            raise ToolError(f"internal error: {name} header {png_header(data)[:4]} != {expected}")
        path = out_dir / name
        path.write_bytes(data)
        print(f"wrote {path.relative_to(REPO_ROOT) if path.is_relative_to(REPO_ROOT) else path} "
              f"({expected[0]}x{expected[1]}, {'RGBA' if expected[3] == 6 else 'RGB, no alpha'})")
    return 0


def image_info(path: Path) -> tuple[str, int, int, bool]:
    """Return (format, width, height, has alpha) for a PNG or JPEG file."""
    data = path.read_bytes()
    if data.startswith(PNG_SIGNATURE):
        width, height, _, colour, has_trns = png_header(data)
        return "PNG", width, height, colour in (4, 6) or has_trns
    if data.startswith(b"\xff\xd8"):
        pos = 2
        while pos + 4 <= len(data):
            if data[pos] != 0xFF:
                raise ValueError("corrupt JPEG marker stream")
            marker = data[pos + 1]
            if marker == 0xFF:
                pos += 1
                continue
            if marker in (0xD8, 0x01) or 0xD0 <= marker <= 0xD7:
                pos += 2
                continue
            (length,) = struct.unpack(">H", data[pos + 2 : pos + 4])
            if marker in (0xC0, 0xC1, 0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF):
                height, width = struct.unpack(">HH", data[pos + 5 : pos + 9])
                return "JPEG", width, height, False
            pos += 2 + length
        raise ValueError("JPEG has no frame header")
    raise ValueError("not a PNG or JPEG file")


def check_screenshot(path: Path) -> tuple[list[str], list[str]]:
    """Return (errors, warnings) for one screenshot."""
    errors, warnings = [], []
    try:
        fmt, width, height, has_alpha = image_info(path)
    except (OSError, ValueError, struct.error) as exc:
        return [f"unreadable: {exc}"], []
    short, long_ = sorted((width, height))
    if has_alpha:
        errors.append("PNG has an alpha channel; Play needs 24-bit PNG or JPEG without alpha")
    if short < SCREENSHOT_MIN_SIDE:
        errors.append(f"{width}x{height}: shorter side {short} px is below the {SCREENSHOT_MIN_SIDE} px minimum")
    if long_ > SCREENSHOT_MAX_SIDE:
        errors.append(f"{width}x{height}: longer side {long_} px is above the {SCREENSHOT_MAX_SIDE} px maximum")
    if long_ > SCREENSHOT_MAX_RATIO * short:
        errors.append(
            f"{width}x{height}: aspect ratio {long_ / short:.2f}:1 exceeds {SCREENSHOT_MAX_RATIO:g}:1 "
            "(long side may be at most twice the short side)"
        )
    size = path.stat().st_size
    if size > SCREENSHOT_MAX_BYTES:
        errors.append(f"file is {size / 1024 / 1024:.1f} MB; Play's limit is 8 MB")
    if not errors and short < SCREENSHOT_RECOMMENDED_SIDE:
        warnings.append(
            f"{width}x{height}: accepted, but Play only features screenshots at least "
            f"{SCREENSHOT_RECOMMENDED_SIDE} px on each side"
        )
    return errors, warnings


def cmd_check_screenshots(folder: Path) -> int:
    if not folder.is_dir():
        print(f"error: {folder} is not a directory", file=sys.stderr)
        return 2
    candidates = sorted(p for p in folder.iterdir() if p.is_file() and not p.name.startswith("."))
    images = [p for p in candidates if p.suffix.lower() in (".png", ".jpg", ".jpeg")]
    for p in candidates:
        if p not in images:
            print(f"skip  {p.name}: not a .png/.jpg/.jpeg file")

    failed = 0
    for p in images:
        errors, warnings = check_screenshot(p)
        if errors:
            failed += 1
            print(f"FAIL  {p.name}")
            for e in errors:
                print(f"        {e}")
        else:
            print(f"ok    {p.name}")
        for w in warnings:
            print(f"        warning: {w}")

    count_ok = SCREENSHOT_MIN_COUNT <= len(images) <= SCREENSHOT_MAX_COUNT
    if not count_ok:
        print(
            f"FAIL  found {len(images)} screenshot(s); Play needs "
            f"{SCREENSHOT_MIN_COUNT} to {SCREENSHOT_MAX_COUNT} phone screenshots"
        )
    print("Reminder: the listing is public — screenshots must show made-up child names only.")
    if failed or not count_ok:
        print(f"{failed} of {len(images)} screenshot(s) failed" + ("" if count_ok else "; wrong count"))
        return 1
    print(f"all {len(images)} screenshots meet Play's phone-screenshot rules")
    return 0


def main(argv: list[str] | None = None) -> int:
    if sys.version_info < (3, 9):
        print("error: Python 3.9 or newer is required (macOS: xcode-select --install or brew install python)",
              file=sys.stderr)
        return 2
    parser = argparse.ArgumentParser(
        prog="scripts/store-graphics.py",
        description="Play store graphics from the adaptive launcher icon, plus a screenshot check.",
        epilog=HELP_EPILOG,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    sub = parser.add_subparsers(dest="command", required=True, metavar="command")
    gen = sub.add_parser("generate", help="write play-icon.png and feature-graphic.png")
    gen.add_argument("--out", type=Path, default=DEFAULT_OUT, help=f"output folder (default: {DEFAULT_OUT.relative_to(REPO_ROOT)})")
    chk = sub.add_parser(
        "check-screenshots",
        help="check phone screenshots in a folder (made-up child names only!)",
        description="Check phone screenshots against Play's rules. The listing is public: "
        "screenshots must use made-up child names, never real ones.",
    )
    chk.add_argument("folder", type=Path)
    args = parser.parse_args(argv)
    try:
        if args.command == "generate":
            return cmd_generate(args.out.resolve())
        return cmd_check_screenshots(args.folder)
    except ToolError as exc:
        print(f"error: {exc}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
