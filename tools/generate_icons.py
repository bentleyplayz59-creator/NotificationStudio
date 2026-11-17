from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
NAVY = (18, 27, 48)
BLUE = (86, 125, 255)
WHITE = (255, 255, 255)

def mark_art(size, color):
    scale = 4
    s = size * scale
    image = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(image)
    w = max(3, int(s * 0.115))
    points = [
        (s * 0.27, s * 0.73),
        (s * 0.27, s * 0.27),
        (s * 0.73, s * 0.73),
        (s * 0.73, s * 0.27),
    ]
    d.line(points, fill=color, width=w, joint="curve")
    r = w // 2
    for x, y in (points[0], points[-1]):
        d.ellipse((x-r, y-r, x+r, y+r), fill=color)
    return image.resize((size, size), Image.Resampling.LANCZOS)

def launcher_icon(size):
    scale = 4
    s = size * scale
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    for y in range(s):
        t = y / max(1, s - 1)
        c = tuple(int(NAVY[i] * (1-t) + BLUE[i] * t) for i in range(3)) + (255,)
        d.line((0, y, s, y), fill=c)
    mask = Image.new("L", (s, s), 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, s-1, s-1), radius=int(s*.23), fill=255)
    im.putalpha(mask)
    mark = mark_art(int(size * .62), WHITE)
    im.alpha_composite(mark.resize((int(size*.62)*scale, int(size*.62)*scale), Image.Resampling.LANCZOS),
                       (int((size*.38)*scale), int((size*.19)*scale)))
    return im.resize((size, size), Image.Resampling.LANCZOS)

def main():
    foreground = Image.new("RGBA", (432, 432), (0, 0, 0, 0))
    mark = mark_art(260, WHITE)
    foreground.alpha_composite(mark, (86, 86))
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
    mark_art(96, WHITE).save(drawable / "ic_notification.png")
    print("Generated Notification Studio monogram artwork.")

if __name__ == "__main__":
    main()
