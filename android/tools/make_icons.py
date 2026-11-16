#!/usr/bin/env python3
"""Builds every launcher icon from the iOS app icon photo.

    python3 tools/make_icons.py          (run from android/, needs Pillow)

Outputs
  app/src/main/res/mipmap-*/ic_launcher_foreground.png  adaptive foreground (108dp)
  app/src/main/res/mipmap-*/ic_launcher.png             legacy rounded square (48dp)
  app/src/main/res/mipmap-*/ic_launcher_round.png       legacy circle (48dp)
  play-assets/icon-512.png                              Play listing icon

Adaptive icons are a 108dp layer of which launchers show only the middle 72dp,
masked to a circle or squircle. The whole photo is fitted to that middle 72dp,
so it fills the icon exactly as it does on iOS and the glasses and the scroll
stay inside the mask. The outer 18dp ring, which is only ever seen while a
launcher animates the layers, is a blurred, darkened continuation of the photo
over the dark brown background colour (res/values/colors.xml).
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageOps

ANDROID = Path(__file__).resolve().parent.parent
SOURCE = ANDROID.parent / "Scroll/Assets.xcassets/AppIcon.appiconset/346199.jpg"
RES = ANDROID / "app/src/main/res"
BACKGROUND = (0x2B, 0x15, 0x09)  # keep in step with ic_launcher_background

DENSITIES = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}
SS = 4  # supersampling for smooth mask edges


def foreground(photo: Image.Image, size: int) -> Image.Image:
    """108dp layer: sharp photo in the middle 72dp, soft continuation outside."""
    inner = round(size * 72 / 108)
    offset = (size - inner) // 2
    sharp = photo.resize((inner, inner), Image.LANCZOS)

    # Mirror the photo outwards so the ring continues its colours, then blur and
    # darken it towards the background colour.
    tile = Image.new("RGB", (inner * 3, inner * 3))
    for row in range(3):
        for col in range(3):
            piece = sharp
            if col != 1:
                piece = ImageOps.mirror(piece)
            if row != 1:
                piece = ImageOps.flip(piece)
            tile.paste(piece, (col * inner, row * inner))
    ring = tile.crop((inner - offset, inner - offset, inner - offset + size, inner - offset + size))
    ring = ring.filter(ImageFilter.GaussianBlur(size / 36))
    ring = Image.blend(ring, Image.new("RGB", ring.size, BACKGROUND), 0.45)

    ring.paste(sharp, (offset, offset))
    return ring


def masked(photo: Image.Image, size: int, shape: str) -> Image.Image:
    """Legacy 48dp icon: the photo inside a rounded square or a circle."""
    big = size * SS
    canvas = Image.new("RGBA", (big, big), (0, 0, 0, 0))
    mask = Image.new("L", (big, big), 0)
    draw = ImageDraw.Draw(mask)
    if shape == "round":
        pad = round(big * 1 / 48)
        draw.ellipse((pad, pad, big - pad - 1, big - pad - 1), fill=255)
    else:
        pad = round(big * 3 / 48)
        draw.rounded_rectangle((pad, pad, big - pad - 1, big - pad - 1), radius=round(big * 8 / 48), fill=255)
    body = big - 2 * pad
    canvas.paste(photo.resize((body, body), Image.LANCZOS), (pad, pad))
    canvas.putalpha(mask)
    return canvas.resize((size, size), Image.LANCZOS)


def main() -> None:
    photo = Image.open(SOURCE).convert("RGB")
    for name, scale in DENSITIES.items():
        out = RES / f"mipmap-{name}"
        out.mkdir(parents=True, exist_ok=True)
        foreground(photo, round(108 * scale)).save(out / "ic_launcher_foreground.png", optimize=True)
        masked(photo, round(48 * scale), "square").save(out / "ic_launcher.png", optimize=True)
        masked(photo, round(48 * scale), "round").save(out / "ic_launcher_round.png", optimize=True)

    play = ANDROID / "play-assets"
    play.mkdir(exist_ok=True)
    # Play wants a full-bleed 512x512 32-bit PNG and applies its own mask.
    photo.resize((512, 512), Image.LANCZOS).convert("RGBA").save(play / "icon-512.png", optimize=True)


if __name__ == "__main__":
    main()
