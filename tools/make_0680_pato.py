"""0.68.0 — Pato Guloso: duck parts (tinted at render time), hats, cape, mini gun, nest, race block, items, lang, recipes."""
import json,os,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
M=lambda n:"mareeletrica:block/"+n;V=lambda n:"minecraft:block/"+n
random.seed(680)
def el(f,t,tex,rot=None,faces=None):
    for v in f+t:assert -16<=v<=32,(f,t)
    fs={d:{"texture":"#"+tex} for d in (faces or ["north","south","west","east","up","down"])}
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":fs}
    if rot:e["rotation"]=rot
    return e
def save(path,obj):os.makedirs(os.path.dirname(path),exist_ok=True);json.dump(obj,open(path,'w'),separators=(',',':'))
def png(name,img):p=os.path.join(A,'textures',name+'.png');os.makedirs(os.path.dirname(p),exist_ok=True);img.save(p)
def noise(base,amp=8,size=16):
    im=Image.new('RGBA',(size,size))
    for x in range(size):
        for y in range(size):d=random.randint(-amp,amp);im.putpixel((x,y),tuple(max(0,min(255,c+d)) for c in base)+(255,))
    return im
# ---------- textures ----------
feather=Image.new('RGBA',(16,16))
for x in range(16):
    for y in range(16):
        v=236+random.randint(-10,8)-(10 if (x+2*y)%7==0 else 0);feather.putpixel((x,y),(v,v,v,255))
png('block/duck_feather',feather)
png('block/duck_orange',noise((240,150,40),10));png('block/duck_black',noise((20,20,22),4));png('block/duck_gold',noise((232,186,52),14))
png('block/duck_red',noise((190,30,36),10));png('block/duck_red_dark',noise((130,18,24),8));png('block/duck_green',noise((70,92,52),8))
png('block/duck_metal',noise((50,52,56),6));png('block/duck_lens',noise((40,120,200),10));png('block/duck_white',noise((246,246,246),4))
erm=noise((245,245,245),3)
for (x,y) in [(3,3),(10,5),(6,11),(13,13),(1,9)]:erm.putpixel((x,y),(20,20,20,255))
png('block/duck_ermine',erm)
check=Image.new('RGBA',(16,16))
for x in range(16):
    for y in range(16):check.putpixel((x,y),(240,240,240,255) if ((x//4)+(y//4))%2 else (25,25,25,255))
png('block/duck_race_top',check)
eggi=Image.new('RGBA',(16,16),(0,0,0,0));dr=ImageDraw.Draw(eggi);dr.ellipse([4,2,12,14],fill=(232,186,52,255),outline=(150,105,20,255));dr.ellipse([6,4,8,7],fill=(255,240,160,255))
png('item/golden_duck_egg',eggi)
wh=Image.new('RGBA',(16,16),(0,0,0,0));dr=ImageDraw.Draw(wh);dr.rectangle([2,7,11,10],fill=(240,150,40,255),outline=(150,80,20,255));dr.ellipse([9,5,14,11],fill=(240,150,40,255),outline=(150,80,20,255));dr.rectangle([4,7,5,8],fill=(60,30,10,255));dr.line([(1,8),(3,8)],fill=(90,90,90,255))
png('item/duck_whistle',wh)
# ---------- duck parts (front = -z, centred on x=8, feet at y=0) ----------
def part(name,tex,els,item=False):
    for e in els:
        for fc in e['faces'].values():
            if fc['texture']=='#f':fc['tintindex']=0
    save(os.path.join(A,'models/block',name+'.json'),{"parent":"minecraft:block/block","textures":dict(tex,particle=list(tex.values())[0]),"elements":els})
F={"f":M("duck_feather")}
part('duck_body',F,[el([5,3,4],[11,8,13],'f'),el([5.5,3.5,2.8],[10.5,7.5,4],'f'),el([6,6,12.5],[10,8.6,15],'f',{"origin":[8,7,13],"axis":"x","angle":-22.5})])
part('duck_neck',F,[el([6.5,6,3.4],[9.5,9,6],'f')])
part('duck_head',F,[el([6,8,1.5],[10,12,5.5],'f')])
part('duck_beak',{"o":M("duck_orange")},[el([6.8,8.6,-1.4],[9.2,9.6,1.6],'o'),el([7.1,8.1,-1.0],[8.9,8.6,1.6],'o')])
part('duck_eyes',{"k":M("duck_black"),"w":M("duck_white")},[el([5.9,10,2.4],[6,11,3.4],'k'),el([10,10,2.4],[10.1,11,3.4],'k'),el([5.92,10.6,2.4],[5.98,11,2.8],'w',faces=["west"]),el([10.02,10.6,2.4],[10.08,11,2.8],'w',faces=["east"])])
part('duck_eyes_closed',{"k":M("duck_black")},[el([5.9,10.3,2.3],[6,10.6,3.5],'k'),el([10,10.3,2.3],[10.1,10.6,3.5],'k')])
part('duck_wing_l',F,[el([10.6,4.4,5],[11.6,7.6,12.4],'f')])
part('duck_wing_r',F,[el([4.4,4.4,5],[5.4,7.6,12.4],'f')])
part('duck_leg_l',{"o":M("duck_orange")},[el([9,0.4,8],[9.8,3.4,8.8],'o'),el([8.3,0,6.4],[10.5,.5,9],'o')])
part('duck_leg_r',{"o":M("duck_orange")},[el([6.2,0.4,8],[7,3.4,8.8],'o'),el([5.5,0,6.4],[7.7,.5,9],'o')])
# ---------- hats (head top at y=12, centre x=8, z≈3.5) ----------
HATS={
 'cap':({"r":M("duck_red"),"d":M("duck_red_dark"),"w":M("duck_white")},[el([6,12,1.5],[10,13.6,5.5],'r'),el([6.3,12,-1],[9.7,12.4,1.5],'d'),el([7.6,13.6,3.1],[8.4,13.9,3.9],'w')]),
 'tophat':({"k":M("duck_black"),"r":M("duck_red")},[el([5.4,12,.9],[10.6,12.4,6.1],'k'),el([6.5,12.4,2],[9.5,16.8,5],'k'),el([6.45,12.4,1.95],[9.55,13.1,5.05],'r')]),
 'crown':({"g":M("duck_gold"),"r":M("duck_red"),"b":M("duck_lens")},[el([6,12,1.5],[10,13.2,1.9],'g'),el([6,12,5.1],[10,13.2,5.5],'g'),el([6,12,1.9],[6.4,13.2,5.1],'g'),el([9.6,12,1.9],[10,13.2,5.1],'g'),
   el([6,13.2,1.5],[6.8,14.6,1.9],'g'),el([7.6,13.2,1.5],[8.4,15,1.9],'g'),el([9.2,13.2,1.5],[10,14.6,1.9],'g'),el([6,13.2,5.1],[6.8,14.4,5.5],'g'),el([9.2,13.2,5.1],[10,14.4,5.5],'g'),
   el([7.7,12.3,1.35],[8.3,12.9,1.5],'r'),el([6.6,12.3,1.35],[7,12.7,1.5],'b'),el([9,12.3,1.35],[9.4,12.7,1.5],'b')]),
 'sunglasses':({"k":M("duck_black")},[el([5.7,9.8,1.2],[10.3,10.2,1.5],'k'),el([5.7,9.8,1.5],[5.95,11.2,4],'k'),el([10.05,9.8,1.5],[10.3,11.2,4],'k'),el([5.7,11,1.2],[10.3,11.3,1.5],'k')]),
 'helmet':({"g":M("duck_green"),"k":M("duck_black")},[el([5.6,11.6,1.1],[10.4,13.2,5.9],'g'),el([6.2,13.2,1.7],[9.8,13.8,5.3],'g'),el([5.4,11.4,.6],[10.6,11.8,6.4],'g'),el([5.55,11.8,3],[5.65,12.4,4],'k'),el([10.35,11.8,3],[10.45,12.4,4],'k')]),
 'scope':({"k":M("duck_black"),"b":M("duck_lens"),"m":M("duck_metal")},[el([7.7,12,2],[8.3,12.4,2.6],'m'),el([7.7,12,4.4],[8.3,12.4,5],'m'),el([7.2,12.4,0],[8.8,14,6.2],'k'),el([6.9,12.1,-.8],[9.1,14.3,0],'k'),el([7.0,12.2,-.85],[9.0,14.2,-.8],'b'),el([7.9,14,2.7],[8.1,14.6,3.3],'m')]),
}
for n,(tex,els) in HATS.items():part('duck_hat_'+n,tex,els)
part('duck_cape',{"r":M("duck_red"),"e":M("duck_ermine")},[el([4.6,8,5],[11.4,8.4,12.6],'r'),el([4.5,3.6,5],[4.9,8.4,12.6],'r'),el([11.1,3.6,5],[11.5,8.4,12.6],'r'),el([4.4,8.3,4.6],[11.6,8.9,5.6],'e')])
part('duck_minigun',{"m":M("duck_metal"),"k":M("duck_black")},[el([11.5,5.6,1],[12.3,6.6,9],'m'),el([11.6,4.6,7],[12.2,5.6,8],'k'),el([11.55,6.6,4],[12.25,7.1,6],'k')])
# ---------- item models: hats centred for the inventory ----------
def shifted(els,dx,dy,dz):
    out=[]
    for e in els:
        e=json.loads(json.dumps(e));e['from']=[e['from'][0]+dx,e['from'][1]+dy,e['from'][2]+dz];e['to']=[e['to'][0]+dx,e['to'][1]+dy,e['to'][2]+dz]
        if 'rotation' in e:o=e['rotation']['origin'];e['rotation']['origin']=[o[0]+dx,o[1]+dy,o[2]+dz]
        out.append(e)
    return out
DISP={"gui":{"rotation":[25,-40,0],"scale":[1.6,1.6,1.6]},"ground":{"scale":[.8,.8,.8]},"fixed":{"scale":[1.4,1.4,1.4]},
      "thirdperson_righthand":{"rotation":[0,0,0],"translation":[0,2,0],"scale":[.7,.7,.7]},"firstperson_righthand":{"translation":[0,2,0],"scale":[.8,.8,.8]}}
for n,(tex,els) in HATS.items():
    dy=-10 if n=='sunglasses' else -12;save(os.path.join(A,'models/item','duck_hat_'+n+'.json'),{"parent":"minecraft:block/block","textures":dict(tex,particle=list(tex.values())[0]),"elements":shifted(els,0,dy+5,4.5),"display":DISP})
save(os.path.join(A,'models/item/golden_duck_egg.json'),{"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/golden_duck_egg"}})
save(os.path.join(A,'models/item/duck_whistle.json'),{"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/duck_whistle"}})
save(os.path.join(A,'models/item/duck_spawn_egg.json'),{"parent":"minecraft:item/template_spawn_egg"})
# ---------- nest & race ----------
NT={"h":V("hay_block_side"),"t":V("hay_block_top"),"s":V("dirt"),"e":M("duck_white")}
NEST=[el([1,0,1],[15,1.5,15],'t'),el([1,1.5,1],[15,4,3],'h'),el([1,1.5,13],[15,4,15],'h'),el([1,1.5,3],[3,4,13],'h'),el([13,1.5,3],[15,4,13],'h'),el([2,1.5,2],[14,2,14],'s',faces=["up"]),el([6.5,1.5,7],[8.5,3.8,9.4],'e'),el([8.8,1.5,5.6],[10.6,3.4,7.6],'e')]
save(os.path.join(A,'models/block/duck_nest.json'),{"parent":"minecraft:block/block","textures":dict(NT,particle=NT["t"]),"elements":NEST})
save(os.path.join(A,'models/item/duck_nest.json'),{"parent":"mareeletrica:block/duck_nest"})
save(os.path.join(A,'blockstates/duck_nest.json'),{"variants":{"":{"model":"mareeletrica:block/duck_nest"}}})
RT={"p":V("spruce_planks"),"c":M("duck_race_top"),"r":M("duck_red"),"g":M("duck_gold"),"f":M("duck_feather"),"o":M("duck_orange")}
RACE=[el([0,0,0],[16,12,16],'p'),el([0,12,0],[16,12.5,16],'c'),el([.5,4,-.2],[15.5,9,0],'r',faces=["north"]),
      el([5,12.5,7],[11,16,12],'f'),el([6,15,4.5],[10,18.5,8],'f'),el([6.8,15.8,2.6],[9.2,16.6,4.6],'o'),el([2,12.5,2],[3,20,3],'g'),el([3,17,2.2],[7,20,2.8],'r')]
save(os.path.join(A,'models/block/duck_race.json'),{"parent":"minecraft:block/block","textures":dict(RT,particle=RT["p"]),"elements":RACE})
save(os.path.join(A,'models/item/duck_race.json'),{"parent":"mareeletrica:block/duck_race"})
save(os.path.join(A,'blockstates/duck_race.json'),{"variants":{f"facing={f}":{"model":"mareeletrica:block/duck_race",**({"y":r} if r else {})} for f,r in {"north":0,"east":90,"south":180,"west":270}.items()}})
for n in ('duck_nest','duck_race'):save(os.path.join(D,'loot_table/blocks',n+'.json'),{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:"+n}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
def shaped(name,pat,key,n=1):save(os.path.join(D,'recipe',name+'.json'),{"type":"minecraft:crafting_shaped","category":"misc","pattern":pat,"key":{k:({"tag":v[1:]} if v.startswith('#') else {"item":v}) for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}})
shaped('duck_whistle',[" N ","NSN"],{"N":"minecraft:iron_nugget","S":"minecraft:orange_dye"})
shaped('duck_nest',["W W","HHH"],{"W":"minecraft:wheat","H":"minecraft:hay_block"})
shaped('duck_race',["CFC","PPP"],{"C":"mareeletrica:casino_chip","F":"minecraft:feather","P":"#minecraft:planks"})
shaped('duck_hat_cap',["RRR","R  "],{"R":"minecraft:red_wool"})
shaped('duck_hat_tophat',[" B ","BRB"],{"B":"minecraft:black_wool","R":"minecraft:red_dye"})
shaped('duck_hat_crown',["G G","GDG"],{"G":"minecraft:gold_ingot","D":"minecraft:diamond"})
shaped('duck_hat_sunglasses',["GBG"],{"G":"minecraft:black_stained_glass_pane","B":"minecraft:iron_nugget"})
shaped('duck_hat_helmet',[" I ","IGI"],{"I":"minecraft:iron_ingot","G":"minecraft:green_dye"})
shaped('duck_hat_scope',["S","N"],{"S":"minecraft:spyglass","N":"minecraft:iron_nugget"})
save(os.path.join(D,'neoforge/biome_modifier/duck.json'),{"type":"neoforge:add_spawns","biomes":["minecraft:river","minecraft:swamp","minecraft:mangrove_swamp","minecraft:beach","minecraft:plains","minecraft:meadow","minecraft:forest"],
     "spawners":{"type":"mareeletrica:duck","weight":8,"minCount":2,"maxCount":4}})
sj=os.path.join(A,'sounds.json');d=json.load(open(sj));d["duck_quack"]={"subtitle":"subtitles.mareeletrica.duck_quack","sounds":[{"name":"mareeletrica:duck_quack","stream":False}]};json.dump(d,open(sj,'w'))
LANG={'pt_br':{"entity.mareeletrica.duck":"Pato","item.mareeletrica.duck_spawn_egg":"Ovo Gerador de Pato","item.mareeletrica.golden_duck_egg":"Ovo de Pato Dourado","item.mareeletrica.duck_whistle":"Apito de Pato",
  "block.mareeletrica.duck_nest":"Ninho de Pato","block.mareeletrica.duck_race":"Corrida de Patos","subtitles.mareeletrica.duck_quack":"Pato: quack!",
  "item.mareeletrica.duck_hat_cap":"Boné de Pato","item.mareeletrica.duck_hat_tophat":"Cartola de Pato","item.mareeletrica.duck_hat_crown":"Coroa de Pato","item.mareeletrica.duck_hat_sunglasses":"Óculos Escuros de Pato",
  "item.mareeletrica.duck_hat_helmet":"Capacete Militar de Pato","item.mareeletrica.duck_hat_scope":"Mini-luneta de Pato"},
 'en_us':{"entity.mareeletrica.duck":"Duck","item.mareeletrica.duck_spawn_egg":"Duck Spawn Egg","item.mareeletrica.golden_duck_egg":"Golden Duck Egg","item.mareeletrica.duck_whistle":"Duck Whistle",
  "block.mareeletrica.duck_nest":"Duck Nest","block.mareeletrica.duck_race":"Duck Race","subtitles.mareeletrica.duck_quack":"Duck quacks",
  "item.mareeletrica.duck_hat_cap":"Duck Cap","item.mareeletrica.duck_hat_tophat":"Duck Top Hat","item.mareeletrica.duck_hat_crown":"Duck Crown","item.mareeletrica.duck_hat_sunglasses":"Duck Sunglasses",
  "item.mareeletrica.duck_hat_helmet":"Duck Army Helmet","item.mareeletrica.duck_hat_scope":"Duck Mini Scope"}}
for lang,kv in LANG.items():
    pp=os.path.join(A,'lang',lang+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update(kv);json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('pato assets ok')
