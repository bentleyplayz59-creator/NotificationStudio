from pathlib import Path
from PIL import Image, ImageDraw
import math

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
PURPLE = (103, 80, 164, 255)
DARK = (33, 27, 56, 255)
WHITE = (255, 255, 255, 255)

def draw_mark(size, color):
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(image)
    s = size / 128
    # Notification card outline and bell glyph, drawn as original vector-like artwork.
    d.rounded_rectangle((18*s, 15*s, 110*s, 113*s), radius=int(22*s), fill=PURPLE)
    d.ellipse((43*s, 32*s, 85*s, 74*s), outline=color, width=max(2, int(6*s)))
    d.arc((39*s, 43*s, 89*s, 99*s), start=205, end=335, fill=color, width=max(2, int(6*s)))
    d.line((42*s, 77*s, 86*s, 77*s), fill=color, width=max(2, int(6*s)))
    d.ellipse((60*s, 83*s, 68*s, 91*s), fill=color)
    d.ellipse((88*s, 18*s, 104*s, 34*s), fill=(255, 190, 90, 255))
    return image

# Adaptive foreground artwork: transparent outside the safe central area.
foreground = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
mark = draw_mark(300, WHITE)
foreground.alpha_composite(mark, (66, 66))
out = ROOT / "mipmap-xxxhdpi"
out.mkdir(parents=True, exist_ok=True)
foreground.save(out / "ic_launcher_foreground.png")

# Legacy launcher icons for older Android versions.
for folder, size in [("mipmap-mdpi",48),("mipmap-hdpi",72),("mipmap-xhdpi",96),
                     ("mipmap-xxhdpi",144),("mipmap-xxxhdpi",192)]:
    target = ROOT / folder
    target.mkdir(parents=True, exist_ok=True)
    icon = Image.new("RGBA", (size, size), DARK)
    art = draw_mark(size, WHITE)
    icon.alpha_composite(art)
    icon.save(target / "ic_launcher.png")
    icon.save(target / "ic_launcher_round.png")

# A simple white-only transparent status-bar notification glyph.
drawable = ROOT / "drawable"
drawable.mkdir(parents=True, exist_ok=True)
small = Image.new("RGBA", (96, 96), (0,0,0,0))
d = ImageDraw.Draw(small)
d.ellipse((32, 15, 64, 47), outline=WHITE, width=7)
d.arc((23, 25, 73, 78), start=205, end=335, fill=WHITE, width=7)
d.line((25, 62, 71, 62), fill=WHITE, width=7)
d.ellipse((43, 70, 53, 80), fill=WHITE)
small.save(drawable / "ic_notification.png")
print("Generated Notification Studio launcher and notification icons with Pillow.")
