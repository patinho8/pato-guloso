"""Textures for the five power suits: armour layers (64x32) and item icons (16x16)."""
import os,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica')
SUITS={"salto":((46,140,70),(150,255,120)),"velocidade":((200,160,30),(255,250,140)),"voo":((70,110,170),(180,240,255)),"tita":((150,40,40),(255,170,60)),"sombra":((40,30,60),(190,90,255))}
def shade(c,f):return tuple(max(0,min(255,int(v*f))) for v in c)
def panel(d,x0,y0,x1,y1,base,rng):
    for y in range(y0,y1):
        for x in range(x0,x1):
            f=1.05-.25*(y-y0)/max(1,y1-y0)+rng.uniform(-.05,.05)
            d.point((x,y),fill=shade(base,f)+(255,))
    d.rectangle([x0,y0,x1-1,y1-1],outline=shade(base,.55)+(255,))
def layer1(base,acc,rng):
    im=Image.new('RGBA',(64,32),(0,0,0,0));d=ImageDraw.Draw(im)
    for r in [(8,0,24,8),(0,8,32,16)]:panel(d,*r,base,rng)                 # helmet
    for r in [(20,16,36,20),(16,20,40,32)]:panel(d,*r,base,rng)            # chest
    for r in [(44,16,52,20),(40,20,56,32)]:panel(d,*r,base,rng)            # arms
    for r in [(4,16,12,20),(0,20,16,32)]:panel(d,*r,shade(base,.85),rng)  # boots (leg uv)
    d.rectangle([9,11,14,12],fill=acc+(255,));d.point((10,10),fill=shade(acc,.8)+(255,));d.point((13,10),fill=shade(acc,.8)+(255,))   # visor
    d.rectangle([1,11,6,11],fill=shade(acc,.7)+(255,));d.rectangle([17,11,22,11],fill=shade(acc,.7)+(255,))
    d.rectangle([22,23,25,26],fill=acc+(255,));d.rectangle([23,24,24,25],fill=(255,255,255,255))   # chest core
    d.line([20,29,27,29],fill=shade(base,.5)+(255,));d.line([20,21,27,21],fill=shade(acc,.7)+(255,))
    for x0 in (40,44,48,52):d.line([x0,24,x0+3,24],fill=acc+(255,))   # arm stripes
    d.rectangle([0,29,15,31],fill=shade(base,.4)+(255,));d.line([4,26,7,26],fill=acc+(255,))       # soles + toe light
    return im
def layer2(base,acc,rng):
    im=Image.new('RGBA',(64,32),(0,0,0,0));d=ImageDraw.Draw(im)
    for r in [(20,16,36,20),(16,20,40,32)]:panel(d,*r,shade(base,.8),rng)
    for r in [(4,16,12,20),(0,20,16,32)]:panel(d,*r,base,rng)
    d.rectangle([20,20,27,21],fill=shade(base,.45)+(255,));d.point((23,20),fill=acc+(255,))       # belt
    d.rectangle([5,24,6,26],fill=acc+(255,));d.rectangle([13,24,14,26],fill=acc+(255,))             # knee lights
    return im
ICONS={"helmet":["................","....########....","...##########...","..############..","..##AAAAAAAA##..","..##AAAAAAAA##..","..############..","...##########...","...###....###...","................"],
 "chestplate":["................","..###......###..","..####....####..","..############..","..#####AA#####..","..####AAAA####..","..#####AA#####..","...##########...","...##########...","...##########...","....########....","................"],
 "leggings":["................","...##########...","...####A#####...","...####..####...","...###....###...","...###....###...","...###....###...","...#A#....#A#...","...###....###...","...###....###...","...###....###...","................"],
 "boots":["................","................","................","................","...###....###...","...###....###...","...#A#....#A#...","..####...####...","..####...####...",".#####..#####...",".#####..#####...","................"]}
def icon(kind,base,acc):
    im=Image.new('RGBA',(16,16),(0,0,0,0));rows=ICONS[kind];top=(16-len(rows))//2
    for y,row in enumerate(rows):
        for x,ch in enumerate(row):
            if ch=='#':im.putpixel((x,y+top),shade(base,1.15-.5*y/len(rows))+(255,))
            elif ch=='A':im.putpixel((x,y+top),acc+(255,))
    # dark outline
    out=im.copy()
    for y in range(16):
        for x in range(16):
            if im.getpixel((x,y))[3]==0 and any(0<=x+dx<16 and 0<=y+dy<16 and im.getpixel((x+dx,y+dy))[3] for dx,dy in((1,0),(-1,0),(0,1),(0,-1))):out.putpixel((x,y),shade(base,.35)+(255,))
    return out
os.makedirs(A+'/textures/models/armor',exist_ok=True)
for name,(base,acc) in SUITS.items():
    rng=random.Random(name)
    layer1(base,acc,rng).save(f'{A}/textures/models/armor/power_{name}_layer_1.png')
    layer2(base,acc,rng).save(f'{A}/textures/models/armor/power_{name}_layer_2.png')
    for kind in ICONS:
        icon(kind,base,acc).save(f'{A}/textures/item/power_{name}_{kind}.png')
        import json
        json.dump({"parent":"minecraft:item/generated","textures":{"layer0":f"mareeletrica:item/power_{name}_{kind}"}},open(f'{A}/models/item/power_{name}_{kind}.json','w'))
print('power armor textures ok')
