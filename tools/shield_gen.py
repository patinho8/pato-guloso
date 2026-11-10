"""0.87.0: generates the tactical shield model (Java) and its high-resolution texture.
Model units like the vanilla shield (front at z=-2, -y = up). Texture declared 64x128, painted at S px per unit."""
import math, random, numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter

S = 8                     # pixels per texture unit
TW, TH = 64, 80          # declared texture size (units)
OUT_TEX = 'src/main/resources/assets/mareeletrica/textures/entity/tactical_shield.png'
OUT_JAVA = 'src/main/java/br/mareeletrica/client/TacticalShieldModel.java'
rng = np.random.default_rng(7); random.seed(7)

# name, parent, layer, material, origin(x,y,z), size(w,h,d), pivot, rotY
P = []
def part(name, mat, o, s, parent=None, layer='solid', pivot=(0, 0, 0), roty=0.0):
    P.append(dict(name=name, mat=mat, o=o, s=s, parent=parent, layer=layer, pivot=pivot, roty=roty))
A = 0.30   # wing angle (rad)
# ---- front panel: centre (cut around the visor) + two angled wings
part('c_top', 'panel', (-5, -16, -2), (10, 3, 1))
part('c_bot', 'panel', (-5, -8, -2), (10, 24, 1))
part('c_l', 'panel', (-5, -13, -2), (1.5, 5, 1))
part('c_r', 'panel', (3.5, -13, -2), (1.5, 5, 1))
part('glass', 'glass', (-3.5, -13, -1.7), (7, 5, .3), layer='glass')
part('f_t', 'gasket', (-4, -13.6, -2.45), (8, .6, .6))
part('f_b', 'gasket', (-4, -8, -2.45), (8, .6, .6))
part('f_l', 'gasket', (-4, -13, -2.45), (.5, 5, .6))
part('f_r', 'gasket', (3.5, -13, -2.45), (.5, 5, .6))
part('wl', 'panelwl', (-4, -16, 0), (4, 32, 1), pivot=(-5, 0, -2), roty=A)
part('wr', 'panelwr', (0, -16, 0), (4, 32, 1), pivot=(5, 0, -2), roty=-A)
# steel frame
part('wl_rim', 'steel', (-4.8, -16.6, -.4), (.9, 33.2, 1.8), parent='wl')
part('wr_rim', 'steel', (3.9, -16.6, -.4), (.9, 33.2, 1.8), parent='wr')
part('wl_top', 'steel', (-4, -16.7, -.35), (4, .8, 1.7), parent='wl')
part('wl_bot', 'steel', (-4, 15.9, -.35), (4, .8, 1.7), parent='wl')
part('wr_top', 'steel', (0, -16.7, -.35), (4, .8, 1.7), parent='wr')
part('wr_bot', 'steel', (0, 15.9, -.35), (4, .8, 1.7), parent='wr')
part('c_trim', 'steel', (-5, -16.7, -2.35), (10, .8, 1.7))
part('c_brim', 'steel', (-5, 15.9, -2.35), (10, .8, 1.7))
part('skid_l', 'rubber', (-4.5, 16.5, -2.5), (2.5, .9, 2))
part('skid_r', 'rubber', (2, 16.5, -2.5), (2.5, .9, 2))
# back side: foam pad, grip, arm strap
part('pad', 'foam', (-4, -6, -1), (8, 17, .7))
part('pad_top', 'foam', (-3.5, -16, -1), (7, 2.2, .5))
part('m_top', 'metal', (-1.3, -5, -.3), (2.6, 1.2, 4.3))
part('m_bot', 'metal', (-1.3, 3.8, -.3), (2.6, 1.2, 4.3))
part('grip', 'grip', (-1, -3.8, 2.4), (2, 7.6, 1.6))
part('strap', 'strap', (-3.6, 7, -.3), (7.2, 1.6, 3.4))
part('buckle', 'metal', (2.6, 6.8, 2.9), (1.2, 2, .4))
# flashlight on the right wing
part('light', 'lightbody', (.8, -15.2, -1.9), (2.2, 2.2, 1.9), parent='wr')
part('lens', 'lens', (1.1, -14.9, -2.0), (1.6, 1.6, .1), parent='wr', layer='glow')
# bolts on the front
for i, (x, y) in enumerate([(-4.3, -15.2), (3.7, -15.2), (-4.3, 14.6), (3.7, 14.6), (-4.3, 0), (3.7, 0)]):
    part(f'bolt{i}', 'bolt', (x, y, -2.25), (.6, .6, .25))

# ---- UV packing (box UV: width 2d+2w, height d+h)
def foot(p):
    w, h, d = p['s']; return math.ceil(2 * d + 2 * w + .001), math.ceil(d + h + .001)
order = sorted(P, key=lambda p: -foot(p)[1])
x = y = rowh = 0
for p in order:
    fw, fh = foot(p)
    if x + fw > TW: x = 0; y += rowh; rowh = 0
    p['uv'] = (x, y); x += fw; rowh = max(rowh, fh)
assert y + rowh <= TH, 'texture full'

# ---- faces in texture units (MC ModelPart.Cube box UV)
def faces(p):
    u, v = p['uv']; w, h, d = p['s']
    u1, u2, u3, u4, u5 = u + d, u + d + w, u + d + w + w, u + d + w + d, u + d + w + d + w
    v1, v2 = v + d, v + d + h
    return {'down': (u1, v, u2, v1), 'up': (u2, v, u3, v1), 'west': (u, v1, u1, v2), 'north': (u1, v1, u2, v2),
            'east': (u2, v1, u4, v2), 'south': (u4, v1, u5, v2)}

img = np.zeros((TH * S, TW * S, 4), np.float32)
def rect(f): return [int(round(c * S)) for c in f]
def noise(h, w, k=1.0, blur=0):
    n = rng.standard_normal((h, w)).astype(np.float32)
    if blur:
        im = Image.fromarray(((n * 40 + 128).clip(0, 255)).astype(np.uint8)).filter(ImageFilter.GaussianBlur(blur))
        n = (np.asarray(im, np.float32) - 128) / 40
    return n * k
def put(f, arr):
    x0, y0, x1, y1 = rect(f)
    if x1 <= x0 or y1 <= y0: return
    img[y0:y1, x0:x1] = arr[:y1 - y0, :x1 - x0]
def solid(h, w, col, nz=6, blur=0, alpha=255):
    a = np.zeros((h, w, 4), np.float32); a[..., :3] = col; a[..., :3] += noise(h, w, nz, blur)[..., None]; a[..., 3] = alpha; return a
def edge_shade(a, k=26, width=None):
    h, w = a.shape[:2]; width = width or max(2, S // 2)
    yy, xx = np.mgrid[0:h, 0:w]; d = np.minimum.reduce([xx, yy, w - 1 - xx, h - 1 - yy]).astype(np.float32)
    m = np.clip(1 - d / width, 0, 1)
    a[..., :3] += (m * k)[..., None]; return a

FONT = None
for fn in ('/usr/share/fonts/truetype/dejavu/DejaVuSansCondensed-Bold.ttf', '/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'):
    try: FONT = fn; ImageFont.truetype(fn, 10); break
    except Exception: FONT = None

def panel_front(h, w, kind, uh):
    """black polycarbonate panel. uh = face height in units (32 for the full wings); text + reflective band."""
    a = solid(h, w, (24, 26, 30), 3, 1)
    # fine hex/carbon weave
    yy, xx = np.mgrid[0:h, 0:w]
    weave = ((xx // 2 + yy // 2) % 2) * 5.0
    a[..., :3] += weave[..., None]
    # soft vertical sheen
    sheen = np.exp(-((xx - w * .35) / (w * .25 + 1)) ** 2) * 10
    a[..., :3] += sheen[..., None]
    return a

def draw_text(a, text, cx, cy, size, col=(235, 235, 235), outline=(10, 10, 12)):
    im = Image.fromarray(a.clip(0, 255).astype(np.uint8), 'RGBA'); d = ImageDraw.Draw(im)
    f = ImageFont.truetype(FONT, size)
    tw, th = d.textbbox((0, 0), text, font=f)[2:]
    d.text((cx - tw / 2, cy - th / 2), text, font=f, fill=col + (255,), stroke_width=1, stroke_fill=outline + (255,))
    return np.asarray(im, np.float32).copy()

def stripe(a, y0, y1, col=(200, 205, 210)):
    # retro-reflective silver band with a blue core
    h, w = a.shape[:2]
    a[y0:y1, :, :3] = col; a[y0:y1, :, :3] += noise(y1 - y0, w, 8)[..., None]
    mid0, mid1 = y0 + (y1 - y0) // 3, y1 - (y1 - y0) // 3
    a[mid0:mid1, :, :3] = (30, 70, 170); a[mid0:mid1, :, :3] += noise(mid1 - mid0, w, 6)[..., None]
    a[y0:y0 + 1, :, :3] = 240; a[y1 - 1:y1, :, :3] = 120
    return a

def paint(p):
    m = p['mat']; fs = faces(p)
    for name, f in fs.items():
        x0, y0, x1, y1 = rect(f); h, w = y1 - y0, x1 - x0
        if h <= 0 or w <= 0: continue
        if m in ('panel', 'panelwl', 'panelwr'):
            if name == 'north':
                a = panel_front(h, w, m, p['s'][1])
                # world y of this face's top in model units -> place the reflective band at y in [-2,1] and the text at y in [4,9]
                oy = p['o'][1]
                def py(yu): return int((yu - oy) * S)
                b0, b1 = py(-2.2), py(.6)
                if 0 < b1 and b0 < h: a = stripe(a, max(0, b0), min(h, b1))
                if p['name'] == 'c_bot':
                    a = draw_text(a, 'POLÍCIA', w / 2, py(6.0), int(S * 2.3))
                    a = draw_text(a, 'CHOQUE', w / 2, py(9.0), int(S * 1.2), col=(205, 170, 40))
                    # small warning label low on the plate
                    lx0, ly0 = int(w * .22), py(12.3); lx1, ly1 = int(w * .78), py(14.0)
                    a[ly0:ly1, lx0:lx1, :3] = (210, 175, 30); a[ly0:ly1, lx0:lx1, :3] += noise(ly1 - ly0, lx1 - lx0, 5)[..., None]
                    for k in range(lx0 - (ly1 - ly0), lx1, S // 2 * 2):   # hazard stripes
                        for t in range(ly1 - ly0):
                            xx = k + t
                            if lx0 <= xx < lx1 and ((xx - k) // (S // 2)) == 0: a[ly0 + t, xx, :3] = (20, 20, 22)
                    a = draw_text(a, 'NÍVEL IIIA', w / 2, py(15.0), int(S * .9), col=(150, 155, 160))
                if p['name'] == 'c_top':
                    pass
                a = edge_shade(a, -10)
            elif name == 'south':
                a = solid(h, w, (46, 48, 52), 5, 1); a = edge_shade(a, -8)
                # moulded ribs
                for yy in range(0, h, S * 2): a[yy:yy + S // 3, :, :3] -= 10
            else:
                a = solid(h, w, (38, 40, 44), 4)
        elif m == 'steel':
            a = solid(h, w, (130, 136, 144), 10, 0)
            yy, xx = np.mgrid[0:h, 0:w]
            a[..., :3] += (np.sin(xx * .9 + yy * .15) * 4)[..., None]            # brushed
            a = edge_shade(a, 40, S // 3)
            for _ in range(max(1, (h * w) // 900)):                                # scratches
                sx, sy = random.randrange(w), random.randrange(h); L = random.randint(S // 2, S * 2)
                for t in range(L):
                    X, Y = sx + t, sy + t // 3
                    if 0 <= X < w and 0 <= Y < h: a[Y, X, :3] += 35
        elif m == 'gasket':
            a = solid(h, w, (18, 18, 20), 3); a = edge_shade(a, 14, 2)
        elif m == 'glass':
            a = np.zeros((h, w, 4), np.float32); a[..., :3] = (150, 200, 225); a[..., 3] = 85
            yy, xx = np.mgrid[0:h, 0:w]
            dg = xx - yy * 1.2
            hl = np.exp(-((dg - w * .30) / (S * .9)) ** 2) + .55 * np.exp(-((dg - w * .30 - S * 2.2) / (S * .35)) ** 2)
            a[..., :3] = a[..., :3] * (1 - hl[..., None] * .8) + np.array([235, 248, 255]) * (hl[..., None] * .8)
            a[..., 3] += hl * 90
            a[..., 3] += (yy / max(1, h)) * -20                                         # clearer at the bottom
            a[..., 3] += np.clip(noise(h, w, 18, 2), -10, 30)                         # a bit of grime
            ed = np.minimum.reduce([xx, yy, w - 1 - xx, h - 1 - yy]) < S // 3
            a[ed, 3] = 170; a[ed, :3] = (90, 120, 135)
            if name != 'north' and name != 'south': a[..., 3] = 200; a[..., :3] = (60, 80, 90)
        elif m == 'rubber':
            a = solid(h, w, (28, 28, 30), 4)
        elif m == 'foam':
            a = solid(h, w, (70, 72, 74), 12, 1)
            yy, xx = np.mgrid[0:h, 0:w]
            a[..., :3] += (((xx // 3 + yy // 3) % 2) * 6)[..., None]
            a = edge_shade(a, -14, S // 2)
        elif m == 'metal':
            a = solid(h, w, (62, 64, 68), 6); a = edge_shade(a, 24, 2)
            if name in ('west', 'east', 'north', 'south'):
                c = (w // 2, h // 2); r = max(1, S // 4)
                a[max(0, c[1] - r):c[1] + r, max(0, c[0] - r):c[0] + r, :3] = 150
        elif m == 'grip':
            a = solid(h, w, (20, 20, 22), 4)
            for yy in range(0, h, max(2, S // 2)): a[yy:yy + 1, :, :3] = 40          # grip ridges
        elif m == 'strap':
            a = solid(h, w, (74, 78, 50), 8, 0)
            yy, xx = np.mgrid[0:h, 0:w]
            a[..., :3] += (((xx + yy) % 4 < 2) * 6)[..., None]                       # webbing weave
            a[1:2, :, :3] = 140; a[h - 2:h - 1, :, :3] = 140                          # stitches
            a[1:2, ::3, :3] = 74; a[h - 2:h - 1, ::3, :3] = 74
        elif m == 'lightbody':
            a = solid(h, w, (30, 31, 34), 4); a = edge_shade(a, 30, 2)
            if name in ('west', 'east'):
                for yy in range(1, h, 3): a[yy, :, :3] = 55                           # cooling fins
        elif m == 'lens':
            a = np.zeros((h, w, 4), np.float32); a[..., :3] = (255, 250, 225); a[..., 3] = 255
            yy, xx = np.mgrid[0:h, 0:w]; r = np.hypot(xx - w / 2, yy - h / 2) / (max(w, h) / 2)
            a[..., :3] = np.stack([255 - r * 40, 252 - r * 50, 230 - r * 90], -1)
        elif m == 'bolt':
            a = solid(h, w, (150, 154, 160), 6); a = edge_shade(a, -40, 2)
        else:
            a = solid(h, w, (255, 0, 255), 0)
        put(f, a)

for p in P: paint(p)
Image.fromarray(img.clip(0, 255).astype(np.uint8), 'RGBA').save(OUT_TEX, optimize=True)

# ---- Java
def fl(v): return f'{v}F'
lines = []
for p in P:
    (ox, oy, oz), (w, h, d) = p['o'], p['s']; u, v = p['uv']
    pose = f'PartPose.offsetAndRotation({fl(p["pivot"][0])},{fl(p["pivot"][1])},{fl(p["pivot"][2])},0F,{fl(p["roty"])},0F)' if p['roty'] else 'PartPose.ZERO'
    parent = 'r' if not p['parent'] else p['parent']
    lines.append(f'  PartDefinition {p["name"]}={parent}.addOrReplaceChild("{p["name"]}",CubeListBuilder.create().texOffs({u},{v}).addBox({fl(ox)},{fl(oy)},{fl(oz)},{fl(w)},{fl(h)},{fl(d)}),{pose});')
def layer(l): return [p['name'] for p in P if p['layer'] == l]
java = f'''package br.mareeletrica.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** GENERATED by tools/shield_gen.py — do not edit by hand. */
final class TacticalShieldModel {{
 private TacticalShieldModel(){{}}
 static final String[] GLASS={{{",".join('"'+n+'"' for n in layer("glass"))}}},GLOW={{{",".join('"'+n+'"' for n in layer("glow"))}}};
 static final String[] WR_GLOW={{"lens"}};
 static ModelPart build(){{
  var mesh=new MeshDefinition();PartDefinition r=mesh.getRoot();
{chr(10).join(lines)}
  return LayerDefinition.create(mesh,{TW},{TH}).bakeRoot();
 }}
}}
'''
open(OUT_JAVA, 'w').write(java)
print('parts', len(P), 'texture rows used', y + rowh)
