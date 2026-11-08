"""0.72.0: more LED colours (magenta, light blue, amber, teal, rainbow) with the same lamp model; brighter textures."""
import json,os,colorsys
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica');TB=os.path.join(A,'textures/block')
NEW={'magenta':((255,40,230),'magenta','Magenta','magenta'),'light_blue':((80,190,255),'light_blue','Azul-Claro','light blue'),
     'amber':((255,170,40),'orange','Âmbar','amber'),'teal':((20,230,170),'cyan','Turquesa','teal'),'rainbow':(None,None,'Arco-íris','rainbow')}
white=Image.open(os.path.join(TB,'lamp_white.png')).convert('RGBA')
def tint(rgb,dark=False):
    im=Image.new('RGBA',(16,16))
    for x in range(16):
        for y in range(16):
            p=white.getpixel((x,y));l=sum(p[:3])/765
            c=rgb(x,y) if callable(rgb) else rgb
            if dark:im.putpixel((x,y),tuple(int(v*.3*l) for v in c)+(255,))
            else:
                # bright centre washes towards white like a real LED, rim keeps the colour
                w=max(0,l-.8)*2.5;im.putpixel((x,y),tuple(min(255,int(v*l*1.05+255*w*.6)) for v in c)+(255,))
    return im
def rb(x,y):r,g,b=colorsys.hsv_to_rgb(((x+y)/30)%1,.85,1);return (int(r*255),int(g*255),int(b*255))
def box(f,t,tex,emissive=False):
    fs={}
    for dn in ["down","up","north","south","west","east"]:
        fc={"texture":"#"+tex,"uv":[0,0,16,16]}
        if emissive:fc["neoforge_data"]={"block_light":15,"sky_light":15}
        fs[dn]=fc
    return {"from":f,"to":t,"faces":fs}
lang={'pt_br':{},'en_us':{}}
for n,(rgb,dye,pt,en) in NEW.items():
    c=rb if rgb is None else rgb
    tint(c).save(os.path.join(TB,f'lamp_{n}.png'));tint(c,True).save(os.path.join(TB,f'lamp_{n}_off.png'))
    T={"glow":f"mareeletrica:block/lamp_{n}","off":f"mareeletrica:block/lamp_{n}_off","frame":"mareeletrica:block/base_steel","particle":f"mareeletrica:block/lamp_{n}"}
    frame=[box([0,0,0],[16,1,16],'frame'),box([0,15,0],[16,16,16],'frame'),box([0,1,0],[1,15,1],'frame'),box([15,1,0],[16,15,1],'frame'),box([0,1,15],[1,15,16],'frame'),box([15,1,15],[16,15,16],'frame')]
    for st,core in (("on",box([.5,1,.5],[15.5,15,15.5],'glow',True)),("off",box([.5,1,.5],[15.5,15,15.5],'off'))):
        json.dump({"parent":"minecraft:block/block","textures":T,"elements":frame+[core]},open(os.path.join(A,f'models/block/led_lamp_{n}_{st}.json'),'w'))
    json.dump({"variants":{"lit=true":{"model":f"mareeletrica:block/led_lamp_{n}_on"},"lit=false":{"model":f"mareeletrica:block/led_lamp_{n}_off"}}},open(os.path.join(A,f'blockstates/led_lamp_{n}.json'),'w'))
    json.dump({"parent":f"mareeletrica:block/led_lamp_{n}_on"},open(os.path.join(A,f'models/item/led_lamp_{n}.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"mareeletrica:led_lamp_{n}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,f'loot_table/blocks/led_lamp_{n}.json'),'w'))
    ing=[{"item":"minecraft:glass"},{"item":"minecraft:glowstone_dust"},{"item":"minecraft:iron_nugget"}]
    ing+=[{"item":"minecraft:red_dye"},{"item":"minecraft:lime_dye"},{"item":"minecraft:blue_dye"}] if dye is None else [{"item":f"minecraft:{dye}_dye"}]+([{"item":"minecraft:white_dye"}] if n in('amber','teal') else [])
    json.dump({"type":"minecraft:crafting_shapeless","category":"building","ingredients":ing,"result":{"id":f"mareeletrica:led_lamp_{n}","count":4}},open(os.path.join(D,f'recipe/led_lamp_{n}.json'),'w'))
    lang['pt_br'][f"block.mareeletrica.led_lamp_{n}"]=f"Lâmpada LED {pt}";lang['en_us'][f"block.mareeletrica.led_lamp_{n}"]=f"LED Lamp ({en})"
for l,kv in lang.items():
    pp=os.path.join(A,'lang',l+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update(kv);json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('led ok')
