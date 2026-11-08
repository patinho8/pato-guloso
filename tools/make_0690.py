"""0.69.0: furniture models/blockstates/loot/recipes, Duck Potion item + effect icon, lang."""
import json,os,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
V=lambda n:"minecraft:block/"+n;M=lambda n:"mareeletrica:block/"+n
random.seed(690)
def el(f,t,tex,faces=None,rot=None):
    for v in f+t:assert -16<=v<=32,(f,t)
    def fit(a,b):
        a,b=min(a,b),max(a,b)
        if b-a>=16:return 0,16
        if a<0:return 0,b-a
        if b>16:return 16-(b-a),16
        return a,b
    def uv(d):
        x=fit(f[0],t[0]);y=fit(16-t[1],16-f[1]);z=fit(f[2],t[2])
        return [round(v,3) for v in ({"up":(x[0],z[0],x[1],z[1]),"down":(x[0],z[0],x[1],z[1]),"north":(x[0],y[0],x[1],y[1]),"south":(x[0],y[0],x[1],y[1])}.get(d) or (z[0],y[0],z[1],y[1]))]
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":uv(d)} for d in (faces or ["north","south","west","east","up","down"])}}
    if rot:e["rotation"]=rot
    return e
def save(path,obj):os.makedirs(os.path.dirname(path),exist_ok=True);json.dump(obj,open(path,'w'),separators=(',',':'))
def model(name,tex,els,item=True,display=None):
    m={"parent":"minecraft:block/block","textures":dict(tex,particle=list(tex.values())[0]),"elements":els}
    if display:m["display"]=display
    save(os.path.join(A,'models/block',name+'.json'),m)
    if item:save(os.path.join(A,'models/item',name+'.json'),{"parent":"mareeletrica:block/"+name})
ROT={"north":0,"east":90,"south":180,"west":270}
def facing_states(name,extra=None):
    v={}
    for f,r in ROT.items():
        if extra:
            for key,mdl in extra.items():v[f"facing={f},{key}"]={"model":"mareeletrica:block/"+mdl,**({"y":r} if r else {})}
        else:v[f"facing={f}"]={"model":"mareeletrica:block/"+name,**({"y":r} if r else {})}
    save(os.path.join(A,'blockstates',name+'.json'),{"variants":v})
def loot(name):save(os.path.join(D,'loot_table/blocks',name+'.json'),{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:"+name}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
def shaped(name,pat,key,n=1):save(os.path.join(D,'recipe',name+'.json'),{"type":"minecraft:crafting_shaped","category":"misc","pattern":pat,"key":{k:({"tag":v[1:]} if v.startswith('#') else {"item":v}) for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}})
def legs(tex,h,inset=1.5,s=1.5):return [el([inset,0,inset],[inset+s,h,inset+s],tex),el([16-inset-s,0,inset],[16-inset,h,inset+s],tex),el([inset,0,16-inset-s],[inset+s,h,16-inset],tex),el([16-inset-s,0,16-inset-s],[16-inset,h,16-inset],tex)]
# ---------- seats (front = north) ----------
model('sofa',{"c":V("gray_wool"),"k":V("light_gray_wool"),"l":V("dark_oak_planks")},legs('l',2,.8,1.6)+[
    el([0,2,0],[16,6,16],'c'),el([.5,6,1],[15.5,8.4,12],'k'),el([0,6,12],[16,16,16],'c'),el([1,8,11],[15,14.5,12.2],'k'),
    el([0,6,0],[2,12,12],'c'),el([14,6,0],[16,12,12],'c')])
model('armchair',{"c":V("red_wool"),"k":V("pink_wool"),"l":V("dark_oak_planks")},legs('l',2,1.6,1.6)+[
    el([1,2,1],[15,6,15],'c'),el([3,6,2],[13,8.4,12],'k'),el([1,6,12],[15,17,15],'c'),el([3,8,11],[13,15,12.2],'k'),
    el([1,6,1],[3,12,12],'c'),el([13,6,1],[15,12,12],'c')])
model('chair',{"w":V("oak_planks"),"d":V("spruce_planks")},legs('d',7,2.5,1.5)+[
    el([2,7,2],[14,8.5,14],'w'),el([2.5,8.5,12],[4,19,13.5],'d'),el([12,8.5,12],[13.5,19,13.5],'d'),el([2.5,13,12.2],[13.5,18.5,13.3],'w'),el([4,10,12.4],[12,11.5,13.1],'w')])
model('table',{"w":V("oak_planks"),"d":V("stripped_oak_log")},[el([0,13,0],[16,16,16],'w'),el([1,0,1],[3,13,3],'d'),el([13,0,1],[15,13,3],'d'),el([1,0,13],[3,13,15],'d'),el([13,0,13],[15,13,15],'d'),el([1.5,11,2],[14.5,12.5,3],'d'),el([1.5,11,13],[14.5,12.5,14],'d')])
for n in ('sofa','armchair','chair','table'):facing_states(n);loot(n)
# ---------- lamp ----------
LT=lambda shade:{"b":V("black_concrete"),"p":V("light_gray_concrete"),"s":shade,"i":V("glowstone")}
LAMP=lambda:[el([4,0,4],[12,1.5,12],'b'),el([7.4,1.5,7.4],[8.6,13,8.6],'p'),el([3,12,3],[13,13,13],'s'),el([3,13,3],[13,19,3.6],'s'),el([3,13,12.4],[13,19,13],'s'),el([3,13,3.6],[3.6,19,12.4],'s'),el([12.4,13,3.6],[13,19,12.4],'s'),el([6.5,13,6.5],[9.5,16,9.5],'i')]
model('floor_lamp',LT(V("light_gray_wool")),LAMP(),item=True)
model('floor_lamp_on',LT(V("white_wool")),LAMP(),item=False)
save(os.path.join(A,'blockstates/floor_lamp.json'),{"variants":{f"facing={f},lit={l}":{"model":"mareeletrica:block/floor_lamp"+("_on" if l=="true" else "")} for f in ROT for l in ("true","false")}})
loot('floor_lamp')
# ---------- TV (screen on the north face at z=6.5; the BER draws the picture) ----------
model('tv',{"k":V("black_concrete"),"g":V("gray_concrete"),"s":V("black_concrete"),"r":V("red_concrete")},[
    el([0,3,6.5],[16,14,9.5],'k'),el([.6,3.6,6.4],[15.4,13.4,6.5],'s',faces=["north"]),el([2,4,9.5],[14,13,11],'g'),
    el([6,0,6],[10,1,10],'g'),el([7,1,7.5],[9,3,8.5],'g'),el([14.2,3.3,6.35],[14.8,3.6,6.45],'r',faces=["north"])])
save(os.path.join(A,'blockstates/tv.json'),{"variants":{f"facing={f}":{"model":"mareeletrica:block/tv",**({"y":r} if r else {})} for f,r in ROT.items()}})
loot('tv')
# ---------- fridge (2 tall) ----------
FT={"w":V("white_concrete"),"g":V("light_gray_concrete"),"d":V("gray_concrete")}
model('fridge_lower',FT,[el([1,0,1.6],[15,16,15],'w'),el([1,0,1],[15,1,1.6],'d',faces=["north","up","west","east"]),el([1,1,1],[14.6,16,1.6],'w'),el([12.4,4,.2],[13.4,14,1],'g'),el([12.4,13,.6],[13.4,14,1],'g'),el([12.4,4,.6],[13.4,5,1],'g')],item=False)
model('fridge_upper',FT,[el([1,0,1.6],[15,15,15],'w'),el([1,4,1],[14.6,15,1.6],'w'),el([1,3.4,.9],[15,4,1.6],'d'),el([12.4,7,.2],[13.4,13,1],'g'),el([12.4,12,.6],[13.4,13,1],'g'),el([12.4,7,.6],[13.4,8,1],'g'),el([1,0,1],[14.6,3.4,1.6],'w')],item=False)
save(os.path.join(A,'blockstates/fridge.json'),{"variants":{f"facing={f},half={h}":{"model":"mareeletrica:block/fridge_"+h,**({"y":r} if r else {})} for f,r in ROT.items() for h in ("lower","upper")}})
save(os.path.join(A,'models/item/fridge.json'),{"parent":"mareeletrica:block/fridge_lower","display":{"gui":{"rotation":[30,225,0],"translation":[0,-2,0],"scale":[.5,.5,.5]}}})
save(os.path.join(D,'loot_table/blocks/fridge.json'),{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:fridge"}],
     "conditions":[{"condition":"minecraft:block_state_property","block":"mareeletrica:fridge","properties":{"half":"lower"}}]}]})
# ---------- double bed: vanilla bed parts, but 1.5 blocks wide (head = north end for facing=north) ----------
BT={"f":V("dark_oak_planks"),"m":V("white_wool"),"b":V("blue_wool"),"p":V("white_concrete"),"s":V("light_blue_wool")}
model('double_bed_head',BT,[el([-4,0,0],[20,3,16],'f'),el([-4,3,1.5],[20,7,16],'m'),el([-4,0,-.5],[20,20,1.5],'f'),el([-3,15,-.6],[19,18,1.6],'s'),
    el([-2.5,7,2.5],[6.5,9.5,8],'p'),el([9.5,7,2.5],[18.5,9.5,8],'p'),el([-4.2,4,9],[20.2,7.6,16],'b')],item=False)
model('double_bed_foot',BT,[el([-4,0,0],[20,3,16],'f'),el([-4,3,0],[20,7,14.5],'m'),el([-4.2,4,0],[20.2,7.6,14.8],'b'),el([-4.3,6.8,4],[20.3,7.8,6],'s'),el([-4,0,14.5],[20,10,16.5],'f'),
    el([-4,0,0],[-2,3,2],'f'),el([18,0,0],[20,3,2],'f')],item=False)
save(os.path.join(A,'blockstates/double_bed.json'),{"variants":{f"facing={f},part={pt}":{"model":"mareeletrica:block/double_bed_"+pt,**({"y":r} if r else {})} for f,r in ROT.items() for pt in ("head","foot")}})
save(os.path.join(A,'models/item/double_bed.json'),{"parent":"mareeletrica:block/double_bed_foot","display":{"gui":{"rotation":[30,160,0],"translation":[0,0,0],"scale":[.42,.42,.42]}}})
save(os.path.join(D,'loot_table/blocks/double_bed.json'),{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:double_bed"}],
     "conditions":[{"condition":"minecraft:block_state_property","block":"mareeletrica:double_bed","properties":{"part":"head"}}]}]})
tagp=os.path.join(RES,'data/minecraft/tags/block/beds.json');os.makedirs(os.path.dirname(tagp),exist_ok=True);json.dump({"replace":False,"values":["mareeletrica:double_bed"]},open(tagp,'w'))
# ---------- recipes ----------
shaped('sofa',["  W","WWW","PPP"],{"W":"minecraft:gray_wool","P":"#minecraft:planks"},2)
shaped('armchair',["  W","WWW","P P"],{"W":"minecraft:red_wool","P":"#minecraft:planks"})
shaped('chair',["  S","PPP","S S"],{"S":"minecraft:stick","P":"#minecraft:planks"},2)
shaped('table',["PPP","S S","S S"],{"S":"minecraft:stick","P":"#minecraft:wooden_slabs"})
shaped('floor_lamp',["WGW"," S ","BBB"],{"W":"minecraft:white_wool","G":"minecraft:glowstone_dust","S":"minecraft:stick","B":"minecraft:black_concrete"})
shaped('tv',["BBB","BGB"," R "],{"B":"minecraft:black_concrete","G":"minecraft:glass_pane","R":"minecraft:redstone"})
shaped('fridge',["IWI","WCW","IWI"],{"I":"minecraft:iron_ingot","W":"minecraft:white_concrete","C":"minecraft:snowball"})
shaped('double_bed',["WBW","PPP"],{"W":"minecraft:white_wool","B":"minecraft:blue_wool","P":"#minecraft:planks"})
save(os.path.join(D,'recipe/duck_potion.json'),{"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":"minecraft:glass_bottle"},{"item":"minecraft:feather"},{"item":"minecraft:bread"},{"item":"minecraft:yellow_dye"}],"result":{"id":"mareeletrica:duck_potion","count":1}})
# ---------- duck potion item + effect icon ----------
im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
d.rectangle([6,1,9,4],fill=(200,220,235,255));d.rectangle([6,0,9,1],fill=(150,100,60,255))
d.ellipse([2,4,13,15],fill=(200,220,235,255));d.ellipse([3,6,12,14],fill=(250,205,40,255));d.ellipse([5,7,7,9],fill=(255,240,150,255))
d.rectangle([9,9,11,10],fill=(240,140,30,255));d.point((8,8),fill=(20,20,20,255))
im.save(os.path.join(A,'textures/item/duck_potion.png'))
save(os.path.join(A,'models/item/duck_potion.json'),{"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/duck_potion"}})
ic=Image.new('RGBA',(18,18),(0,0,0,0));d=ImageDraw.Draw(ic)
d.ellipse([3,7,14,15],fill=(250,250,245,255),outline=(60,60,60,255));d.ellipse([9,2,15,9],fill=(250,250,245,255),outline=(60,60,60,255));d.polygon([(15,5),(17,6),(15,7)],fill=(240,140,30,255));d.point((12,4),fill=(0,0,0,255))
os.makedirs(os.path.join(A,'textures/mob_effect'),exist_ok=True);ic.save(os.path.join(A,'textures/mob_effect/duck.png'))
LANG={'pt_br':{"block.mareeletrica.sofa":"Sofá","block.mareeletrica.armchair":"Poltrona","block.mareeletrica.chair":"Cadeira","block.mareeletrica.table":"Mesa","block.mareeletrica.floor_lamp":"Luminária",
  "block.mareeletrica.tv":"Televisão","block.mareeletrica.fridge":"Geladeira","block.mareeletrica.double_bed":"Cama de Casal","entity.mareeletrica.seat":"Assento",
  "item.mareeletrica.duck_potion":"Poção do Pato","effect.mareeletrica.duck":"Pato"},
 'en_us':{"block.mareeletrica.sofa":"Sofa","block.mareeletrica.armchair":"Armchair","block.mareeletrica.chair":"Chair","block.mareeletrica.table":"Table","block.mareeletrica.floor_lamp":"Floor Lamp",
  "block.mareeletrica.tv":"Television","block.mareeletrica.fridge":"Fridge","block.mareeletrica.double_bed":"Double Bed","entity.mareeletrica.seat":"Seat",
  "item.mareeletrica.duck_potion":"Duck Potion","effect.mareeletrica.duck":"Duck"}}
for lang,kv in LANG.items():
    pp=os.path.join(A,'lang',lang+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update(kv);json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.69.0 assets ok')
