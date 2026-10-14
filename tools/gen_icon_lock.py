import math
import os
from PIL import Image, ImageDraw, ImageFont

S = 432
WHITE = (255, 255, 255, 255)
DARK = (51, 51, 51, 255)
DARK_HEX = "#333333"

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")
SRC = Image.open(os.path.join(ROOT, "tools", "lock_source.png")).convert("RGBA")
# dark square occupies ~[20,20,1004,1004] of the 1024 source
ICON = SRC.crop((20, 20, 1004, 1004))
ICON_GRAY = ICON.convert("L")

DENSITIES = {
    "mdpi": (48, 108),
    "hdpi": (72, 162),
    "xhdpi": (96, 216),
    "xxhdpi": (144, 324),
    "xxxhdpi": (192, 432),
}


def glyph_mask():
    """Alpha mask of the white lock glyph inside the dark square."""
    m = ICON_GRAY.point(lambda v: 255 if v > 140 else 0)
    w, h = m.size
    # outer white margin is contiguous around the rounded square — flood it
    for corner in [(2, 2), (w - 3, 2), (2, h - 3), (w - 3, h - 3)]:
        ImageDraw.floodfill(m, corner, 128)
    return m.point(lambda v: 255 if v == 255 else 0)


def glyph_image(size, glyph_scale):
    """White silhouette of the lock on transparent bg."""
    mask = glyph_mask().resize((size, size), Image.LANCZOS)
    g = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    g.paste(Image.new("RGBA", (size, size), WHITE), (0, 0), mask)
    # scale glyph to glyph_scale of canvas, centered
    inner = int(size * glyph_scale)
    g = g.resize((inner, inner), Image.LANCZOS)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.alpha_composite(g, ((size - inner) // 2, (size - inner) // 2))
    return out


def render(size, mode):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    if mode == "launcher":
        img = ICON.resize((size, size), Image.LANCZOS).convert("RGBA")
    elif mode == "round":
        d = ImageDraw.Draw(img)
        d.ellipse([0, 0, size - 1, size - 1], fill=DARK)
        img.alpha_composite(glyph_image(size, 0.56))
    elif mode == "fg":
        img = glyph_image(size, 0.66)
    return img


# ---------- simplified padlock polygons for vector drawables ----------

def rrect(x0, y0, x1, y1, r, n=8):
    pts = []
    for cx, cy, a0 in [(x1 - r, y0 + r, -90), (x1 - r, y1 - r, 0),
                       (x0 + r, y1 - r, 90), (x0 + r, y0 + r, 180)]:
        for i in range(n):
            t = math.radians(a0 + i * 10)
            pts.append((cx + r * math.cos(t), cy + r * math.sin(t)))
    return pts


def arcband(cx, cy, ro, ri, a0, a1, n=40):
    outer = [(cx + ro * math.cos(math.radians(a0 + (a1 - a0) * i / n)),
              cy + ro * math.sin(math.radians(a0 + (a1 - a0) * i / n)))
             for i in range(n + 1)]
    inner = [(cx + ri * math.cos(math.radians(a0 + (a1 - a0) * i / n)),
              cy + ri * math.sin(math.radians(a0 + (a1 - a0) * i / n)))
             for i in range(n + 1)]
    return outer + inner[::-1]


def ell(cx, cy, rx, ry, n=40):
    return [(cx + rx * math.cos(2 * math.pi * i / n),
             cy + ry * math.sin(2 * math.pi * i / n)) for i in range(n)]


def padlock_shapes():
    body = rrect(96, 208, 336, 392, 36)
    shackle = arcband(216, 212, 118, 66, 180, 360)
    key_top = ell(216, 296, 34, 34)[::-1]
    key_stem = [(198, 296), (234, 296), (222, 362), (210, 362)][::-1]
    return [shackle, body, key_top, key_stem]


def path_data(viewport, lock_scale):
    scale = viewport / S
    cx = cy = S / 2
    parts = []
    for sh in padlock_shapes():
        pts = [((cx + (x - cx) * lock_scale) * scale,
                (cy + (y - cy) * lock_scale) * scale) for (x, y) in sh]
        parts.append("M" + " ".join(f"{x:.1f},{y:.1f}" for x, y in pts) + "Z")
    return " ".join(parts)


def write_vector(path, viewport, lock_scale):
    xml = (
        f'<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{viewport}dp"\n'
        f'    android:height="{viewport}dp"\n'
        f'    android:viewportWidth="{viewport}"\n'
        f'    android:viewportHeight="{viewport}">\n'
        f'    <path\n'
        f'        android:fillColor="#FFFFFF"\n'
        f'        android:fillType="nonZero"\n'
        f'        android:pathData="{path_data(viewport, lock_scale)}" />\n'
        f'</vector>\n'
    )
    with open(path, "w", newline="\n") as f:
        f.write(xml)
    print("wrote", path)


def mono_preview(size=432):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    sc = size / S
    bs = 0.62
    cx = cy = S / 2
    for i, sh in enumerate(padlock_shapes()):
        pts = [((cx + (x - cx) * bs) * sc, (cy + (y - cy) * bs) * sc)
               for (x, y) in sh]
        d.polygon(pts, fill=WHITE if i < 2 else DARK)
    return img


def gen_banner(path):
    w, h = 640, 360
    img = Image.new("RGBA", (w, h), DARK)
    bird = glyph_image(h, 0.86)
    img.alpha_composite(bird, (40, 0))
    d = ImageDraw.Draw(img)
    text = "FuckDPI"
    size = 110
    font = ImageFont.truetype("arialbd.ttf", size)
    while d.textlength(text, font=font) > w - 420 and size > 10:
        size -= 4
        font = ImageFont.truetype("arialbd.ttf", size)
    d.text((400, h // 2), text, font=font, fill=WHITE, anchor="lm")
    img.save(path)
    print("wrote", path)


def main():
    for density, (launcher_size, fg_size) in DENSITIES.items():
        d = os.path.join(RES, f"mipmap-{density}")
        render(launcher_size, "launcher").convert("RGB").save(
            os.path.join(d, "ic_launcher.webp"), "WEBP", quality=95)
        render(launcher_size, "round").save(
            os.path.join(d, "ic_launcher_round.webp"), "WEBP", quality=95)
        render(fg_size, "fg").save(
            os.path.join(d, "ic_launcher_foreground.webp"), "WEBP", quality=95)
        print("wrote", density)

    write_vector(os.path.join(RES, "drawable", "ic_notification.xml"), 24, 0.72)
    write_vector(os.path.join(RES, "drawable", "ic_launcher_monochrome.xml"), 108, 0.62)

    gen_banner(os.path.join(RES, "mipmap-xhdpi", "ic_banner.png"))

    render(512, "launcher").convert("RGB").save(
        os.path.join(ROOT, "fastlane", "metadata", "android",
                     "en-US", "images", "icon.png"), "PNG")

    render(512, "launcher").convert("RGB").save(
        os.path.join(ROOT, ".github", "images", "app.png"), "PNG")
    render(512, "round").save(
        os.path.join(ROOT, ".github", "images", "logo.png"), "PNG")

    render(432, "launcher").save("lock_preview.png")
    render(432, "fg").save("lock_fg_preview.png")
    render(432, "round").save("lock_round_preview.png")
    mono_preview().save("lock_mono_preview.png")
    print("done")


if __name__ == "__main__":
    main()
