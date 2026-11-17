from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
NAVY = (18, 27, 48)
BLUE = (86, 125, 255)
CYAN = (108, 207, 255)
WHITE = (255, 255, 255)

def bell_art(size, color):
    # Supersampled, clean bell silhouette with a small clapper.
    scale = 4
    s = size * scale
    image = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(image)
    cx = s / 2
    # Bell body: rounded dome, flared lower rim, and clapper.
    d.arc((cx - s*.245, s*.19, cx + s*.245, s*.70), 180, 360,
          fill=color, width=max(3, int(s*.075)))
    d.line((cx - s*.245, s*.445, cx - s*.245, s*.665),
           fill=color, width=max(3, int(s*.075)))
    d.line((cx + s*.245, s*.445, cx + s*.245, s*.665),
           fill=color, width=max(3, int(s*.075)))
    d.arc((cx - s*.30, s*.49, cx + s*.30, s*.77), 5, 175,
          fill=color, width=max(3, int(s*.075)))
    d.line((cx - s*.30, s*.635, cx + s*.30, s*.635),
           fill=color, width=max(3, int(s*.075)))
    d.ellipse((cx - s*.055, s*.70, cx + s*.055, s*.81), fill=color)
    d.ellipse((cx - s*.075, s*.10, cx + s*.075, s*.25), fill=color)
    return image.resize((size, size), Image.Resampling.LANCZOS)

def launcher_icon(size, rounded=True):
    scale = 4
    s = size * scale
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    # Rich blue gradient tile.
    for y in range(s):
        t = y / max(1, s - 1)
        c = tuple(int(NAVY[i] * (1-t) + BLUE[i] * t) for i in range(3)) + (255,)
        d.line((0, y, s, y), fill=c)
    if rounded:
        mask = Image.new("L", (s, s), 0)
        md = ImageDraw.Draw(mask)
        md.rounded_rectangle((0, 0, s-1, s-1), radius=int(s*.23), fill=255)
        im.putalpha(mask)
    mark = bell_art(int(size*.58), WHITE)
    im.alpha_composite(mark.resize((int(size*.58)*scale, int(size*.58)*scale), Image.Resampling.LANCZOS),
                       (int((size*.42)*scale), int((size*.20)*scale)))
    return im.resize((size, size), Image.Resampling.LANCZOS)

def main():
    # Adaptive icon foreground stays transparent outside the centered safe zone.
    foreground = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    bell = bell_art(252, WHITE)
    foreground.alpha_composite(bell, (90, 80))
    out = ROOT / "mipmap-xxxhdpi"
    out.mkdir(parents=True, exist_ok=True)
    foreground.save(out / "ic_launcher_foreground.png")

    for folder, size in [("mipmap-mdpi",48),("mipmap-hdpi",72),("mipmap-xhdpi",96),
                         ("mipmap-xxhdpi",144),("mipmap-xxxhdpi",192)]:
        target = ROOT / folder
        target.mkdir(parents=True, exist_ok=True)
        icon = launcher_icon(size)
        icon.save(target / "ic_launcher.png")
        icon.save(target / "ic_launcher_round.png")

    drawable = ROOT / "drawable"
    drawable.mkdir(parents=True, exist_ok=True)
    small = bell_art(96, WHITE)
    small.save(drawable / "ic_notification.png")
    print("Generated clean bell launcher artwork and notification glyph.")

if __name__ == "__main__":
    main()
