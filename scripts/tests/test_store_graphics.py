"""Tests for scripts/store-graphics.py.

Run: python3 -m unittest discover -s scripts/tests
"""

from __future__ import annotations

import contextlib
import importlib.util
import io
import shutil
import struct
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parent.parent / "store-graphics.py"
_spec = importlib.util.spec_from_file_location("store_graphics", SCRIPT)
sg = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(sg)


def write_png(path: Path, width: int, height: int, alpha: bool = False) -> None:
    channels = 4 if alpha else 3
    path.write_bytes(sg.encode_png(width, height, b"\x80" * (width * height * channels), alpha))


def write_jpeg(path: Path, width: int, height: int) -> None:
    # SOI, an APP0 segment, then a baseline SOF0 frame header: enough for the size check.
    app0 = b"\xff\xe0" + struct.pack(">H", 16) + b"JFIF\x00\x01\x01\x00\x00\x01\x00\x01\x00\x00"
    sof0 = b"\xff\xc0" + struct.pack(">HBHHB", 11, 8, height, width, 1) + b"\x01\x11\x00"
    path.write_bytes(b"\xff\xd8" + app0 + sof0 + b"\xff\xd9")


class ScreenshotCheckTest(unittest.TestCase):
    def setUp(self):
        self.dir = Path(tempfile.mkdtemp())
        self.addCleanup(shutil.rmtree, self.dir)

    def run_check(self) -> tuple[int, str]:
        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            code = sg.cmd_check_screenshots(self.dir)
        return code, out.getvalue()

    def test_valid_png_and_jpeg_pass(self):
        write_png(self.dir / "1.png", 1080, 1920)
        write_jpeg(self.dir / "2.jpg", 1080, 2160)  # exactly 2:1 is allowed
        code, out = self.run_check()
        self.assertEqual(code, 0, out)
        self.assertIn("made-up child names", out)

    def test_too_small_fails(self):
        write_png(self.dir / "1.png", 1080, 1920)
        write_png(self.dir / "tiny.png", 300, 500)
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn("FAIL  tiny.png", out)
        self.assertIn("below the 320 px minimum", out)

    def test_too_large_fails(self):
        write_png(self.dir / "1.png", 1080, 1920)
        write_jpeg(self.dir / "huge.jpg", 2400, 4000)
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn("above the 3840 px maximum", out)

    def test_wrong_ratio_fails(self):
        write_png(self.dir / "1.png", 1080, 1920)
        write_jpeg(self.dir / "tall.jpg", 1080, 2400)
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn("FAIL  tall.jpg", out)
        self.assertIn("aspect ratio 2.22:1", out)

    def test_alpha_fails(self):
        write_png(self.dir / "1.png", 1080, 1920)
        write_png(self.dir / "alpha.png", 1080, 1920, alpha=True)
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn("alpha channel", out)

    def test_fewer_than_two_fails(self):
        write_png(self.dir / "1.png", 1080, 1920)
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn("found 1 screenshot(s)", out)

    def test_small_but_valid_warns(self):
        write_png(self.dir / "1.png", 720, 1280)
        write_png(self.dir / "2.png", 720, 1280)
        code, out = self.run_check()
        self.assertEqual(code, 0, out)
        self.assertIn("warning:", out)

    def test_non_image_is_skipped_not_counted(self):
        write_png(self.dir / "1.png", 1080, 1920)
        (self.dir / "notes.txt").write_text("x")
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn("skip  notes.txt", out)


class PngCodecTest(unittest.TestCase):
    def test_round_trip(self):
        pixels = bytes(range(256)) * 4  # 16x16 RGBA
        data = sg.encode_png(16, 16, pixels, alpha=True)
        self.assertEqual(sg.decode_rgba_png(data), (16, 16, pixels))

    def test_flatten_blends_onto_background(self):
        self.assertEqual(sg.flatten_to_rgb(b"\xff\xff\xff\x00\xff\xff\xff\xff", (0, 0, 0)), b"\x00\x00\x00\xff\xff\xff")


@unittest.skipUnless(shutil.which("rsvg-convert"), "rsvg-convert not installed")
class GenerateTest(unittest.TestCase):
    def test_outputs_match_play_specs_and_are_reproducible(self):
        with tempfile.TemporaryDirectory() as a, tempfile.TemporaryDirectory() as b:
            for out in (a, b):
                with contextlib.redirect_stdout(io.StringIO()):
                    self.assertEqual(sg.cmd_generate(Path(out)), 0)
            for name, header in (
                ("play-icon.png", (512, 512, 8, 6)),
                ("feature-graphic.png", (1024, 500, 8, 2)),
            ):
                first = (Path(a) / name).read_bytes()
                self.assertEqual(sg.png_header(first)[:4], header)
                self.assertFalse(sg.png_header(first)[4])
                self.assertEqual(first, (Path(b) / name).read_bytes(), f"{name} differs between runs")


if __name__ == "__main__":
    unittest.main()
