"""0.65.4: golf, bowling, support radio + supply crate, thermal goggles, rare golden egg — models, textures, recipes, loot, lang."""
import json,os,math
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica');T=os.path.join(A,'textures')
V=lambda n:"minecraft:block/"+n
def box(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def ball(cx,cy,cz,r,tex):   # three crossed boxes + a 45° pair = a decent sphere
    a=r*.82;return [box([cx-r,cy-a,cz-a],[cx+r,cy+a,cz+a],tex),box([cx-a,cy-r,cz-a],[cx+a,cy+r,cz+a],tex),box([cx-a,cy-a,cz-r],[cx+a,cy+a,cz+r],tex),
                    box([cx-a,cy-a,cz-a],[cx+a,cy+a,cz+a],tex,([cx,cy,cz],'y',45))]
def oct_y(cx,cz,r,y0,y1,tex):a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
def save(kind,name,m):
    for e in m.get("elements",[]):
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
TX={"white":V("white_concrete"),"red":V("red_concrete"),"gold":V("gold_block"),"yellow":V("yellow_concrete"),"olive":V("green_terracotta"),"khaki":V("brown_terracotta"),
    "dark":"mareeletrica:block/dark","steel":"mareeletrica:block/steel","black":V("black_concrete"),"blue":V("blue_concrete"),"navy":V("blue_terracotta"),"wood":V("oak_planks"),
    "grass":V("moss_block"),"lime":V("lime_concrete"),"gray":V("gray_concrete"),"lgray":V("light_gray_concrete"),"glass":V("lime_stained_glass"),"spruce":V("spruce_planks"),"string":V("white_wool"),
    "orange":V("orange_concrete"),"particle":V("white_concrete")}
def m(el,disp=None,parent=None):
    d={"textures":TX,"elements":el}
    if disp:d["display"]=disp
    if parent:d["parent"]=parent
    return d
SMALL={"gui":{"rotation":[25,-35,0],"scale":[1.6,1.6,1.6]},"ground":{"translation":[0,3,0],"scale":[1,1,1]},"fixed":{"scale":[1.4,1.4,1.4]},
       "thirdperson_righthand":{"translation":[0,3,1],"scale":[1,1,1]},"firstperson_righthand":{"translation":[0,4,0],"scale":[1.2,1.2,1.2]}}
MED={"gui":{"rotation":[25,-35,0],"scale":[.9,.9,.9]},"ground":{"translation":[0,3,0],"scale":[.5,.5,.5]},"fixed":{"scale":[.8,.8,.8]},
     "thirdperson_righthand":{"translation":[0,2.5,1.5],"scale":[.55,.55,.55]},"firstperson_righthand":{"rotation":[0,-25,0],"translation":[1,3,1],"scale":[.7,.7,.7]}}
TOOL={"gui":{"rotation":[0,0,-45],"translation":[0,0,0],"scale":[.85,.85,.85]},"ground":{"translation":[0,3,0],"scale":[.5,.5,.5]},"fixed":{"rotation":[0,0,-45],"scale":[.8,.8,.8]},
      "thirdperson_righthand":{"rotation":[0,90,0],"translation":[0,1,1],"scale":[.85,.85,.85]},"firstperson_righthand":{"rotation":[0,90,20],"translation":[1,2,1],"scale":[.8,.8,.8]}}
# ---------- rare golden egg (entity part + the egg item) ----------
EGG=[box([5.2,0,5.2],[10.8,7.5,10.8],'gold'),box([4.6,1,4.6],[11.4,6,11.4],'gold'),box([5.8,7.5,5.8],[10.2,9.4,10.2],'gold'),box([6.6,9.4,6.6],[9.4,10.2,9.4],'gold'),
     box([4.55,3,4.55],[11.45,3.6,11.45],'orange'),box([6,6.6,4.5],[7.2,7.4,4.6],'white')]
save('block','rare_egg',m(EGG));save('item','rooster_egg_dourado',m(EGG,MED))
# ---------- supply crate + parachute ----------
CR=[box([1,0,1],[15,13,15],'olive'),box([.8,0,.8],[15.2,1.2,15.2],'dark'),box([.8,11.8,.8],[15.2,13,15.2],'dark'),
    box([7,0,.7],[9,13,1],'khaki'),box([7,0,15],[9,13,15.3],'khaki'),box([.7,0,7],[1,13,9],'khaki'),box([15,0,7],[15.3,13,9],'khaki'),box([5,5,.6],[11,8,.8],'white')]
CH=[box([-12,14,-12],[28,16,28],'white'),box([-8,16,-8],[24,18,24],'red'),box([-3,18,-3],[19,19.5,19],'white')]
for x,z in ((-10,-10),(25,-10),(-10,25),(25,25)):CH.append(box([x,0,z],[x+1,14,z+1],'string'))
save('block','supply_crate',m(CR));save('block','supply_chute',m(CH))
# ---------- golf ----------
save('block','golf_ball_part',m(ball(8,1.6,8,1.6,'white')))
save('item','golf_ball',m(ball(8,8,8,3,'white'),SMALL))
CLUB=[box([7.4,4,7.4],[8.6,26,8.6],'lgray'),box([7.2,20,7.2],[8.8,27,8.8],'black'),box([5,1.5,6.8],[11,4.5,9.2],'steel'),box([4.6,1.5,6.6],[5.2,4.5,9.4],'gray')]
save('item','golf_club',m(CLUB,TOOL))
HOLE=[box([0,0,0],[16,15,16],'grass'),box([0,15,0],[16,16,6],'lime'),box([0,15,10],[16,16,16],'lime'),box([0,15,6],[6,16,10],'lime'),box([10,15,6],[16,16,10],'lime'),
      box([6.4,14.6,6.4],[9.6,15,9.6],'black'),box([7.7,15,7.7],[8.3,32,8.3],'white'),box([8.3,24,7.85],[15.5,30,8.15],'red')]
save('block','golf_hole',m(HOLE,parent="minecraft:block/block"))
json.dump({"parent":"mareeletrica:block/golf_hole","display":{"gui":{"rotation":[30,225,0],"translation":[0,-2,0],"scale":[.5,.5,.5]}}},open(os.path.join(A,'models/item/golf_hole.json'),'w'))
json.dump({"variants":{"":{"model":"mareeletrica:block/golf_hole"}}},open(os.path.join(A,'blockstates/golf_hole.json'),'w'))
# ---------- bowling ----------
BB=ball(8,3.6,8,3.6,'navy')+[box([7.2,6.6,6.4],[8,7.3,7.2],'black'),box([8.4,6.6,6.4],[9.2,7.3,7.2],'black'),box([7.8,6.4,8.6],[8.6,7.2,9.4],'black')]
save('block','bowling_ball_part',m(BB))
save('item','bowling_ball',m([box([e['from'][0],e['from'][1]+4.4,e['from'][2]],[e['to'][0],e['to'][1]+4.4,e['to'][2]],e['faces']['up']['texture'][1:],([8,8,8],'y',45) if 'rotation' in e else None) for e in BB],SMALL))
PIN=oct_y(8,8,1.5,0,1,'white')+oct_y(8,8,2.2,1,5,'white')+oct_y(8,8,1.7,5,6.5,'white')+oct_y(8,8,1.25,6.5,8,'red')+oct_y(8,8,1,8,9.5,'white')+oct_y(8,8,1.35,9.5,11.2,'white')+oct_y(8,8,1.1,11.2,12,'white')+oct_y(8,8,1.75,7,7.4,'red')
save('block','bowling_pin',m(PIN))
PS=[box([0,0,0],[16,4,16],'spruce'),box([1,4,1],[15,12,15],'wood'),box([0,12,0],[16,14,16],'spruce'),box([2,6,.6],[14,10,1],'dark'),box([3,7,.5],[13,9,.6],'red')]
for i,(x,z) in enumerate([(4,4),(8,4),(12,4),(6,8),(10,8),(8,12)]):PS+= [box([x-.8,14,z-.8],[x+.8,18,z+.8],'white'),box([x-.8,16,z-.8],[x+.8,16.6,z+.8],'red')]
save('block','bowling_pinsetter',m(PS,parent="minecraft:block/block"))
json.dump({"parent":"mareeletrica:block/bowling_pinsetter"},open(os.path.join(A,'models/item/bowling_pinsetter.json'),'w'))
json.dump({"variants":{f"facing={f}":{"model":"mareeletrica:block/bowling_pinsetter",**({"y":y} if y else {})} for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates/bowling_pinsetter.json'),'w'))
# ---------- support radio ----------
RADIO=[box([4,0,6],[12,11,10],'olive'),box([4.5,6,5.8],[11.5,10,6],'dark'),box([5,1,5.8],[11,5,6],'black'),box([10,11,7.5],[11,24,8.5],'black'),box([10.2,24,7.7],[10.8,27,8.3],'gray'),
       box([5,11,7],[7,12,9],'dark'),box([7.5,11,7],[9,12,9],'red'),box([3.5,3,7],[4,8,9],'khaki'),box([12,3,7],[12.5,8,9],'khaki')]
save('item','support_radio',m(RADIO,MED))
# (thermal goggles already existed in Gadgets — only their effect was upgraded, the item and its assets are untouched)
# ---------- recipes ----------
def shaped(name,pat,key,n=1):
    json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":pat,"key":{k:{"item":v} for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}},open(os.path.join(D,'recipe',name+'.json'),'w'))
shaped('golf_club',["  I"," S ","S  "],{"I":"minecraft:iron_ingot","S":"minecraft:stick"})
shaped('golf_ball',[" Q ","QSQ"," Q "],{"Q":"minecraft:quartz","S":"minecraft:slime_ball"},4)
shaped('golf_hole',[" S ","RFR","GGG"],{"S":"minecraft:stick","R":"minecraft:red_wool","F":"minecraft:flower_pot","G":"minecraft:moss_block"})
shaped('bowling_ball',["BOB","OBO","BOB"],{"B":"minecraft:blue_concrete","O":"minecraft:obsidian"})
shaped('bowling_pinsetter',["WRW","PDP","PPP"],{"W":"minecraft:white_wool","R":"minecraft:red_dye","D":"minecraft:dispenser","P":"minecraft:spruce_planks"})
shaped('support_radio',[" R ","PCP","PBP"],{"R":"minecraft:lightning_rod","P":"mareeletrica:iron_plate","C":"mareeletrica:control_module","B":"minecraft:redstone_block"})
for b in ('golf_hole','bowling_pinsetter'):
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:"+b}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks',b+'.json'),'w'))
# ---------- lang ----------
LANG={'pt_br':{"item.mareeletrica.golf_club":"Taco de Golfe","item.mareeletrica.golf_ball":"Bola de Golfe","block.mareeletrica.golf_hole":"Buraco de Golfe","item.mareeletrica.bowling_ball":"Bola de Boliche",
  "block.mareeletrica.bowling_pinsetter":"Montador de Pinos","entity.mareeletrica.bowling_pin":"Pino de Boliche","entity.mareeletrica.golf_ball":"Bola de Golfe","entity.mareeletrica.bowling_ball":"Bola de Boliche",
  "item.mareeletrica.support_radio":"Rádio de Suporte","entity.mareeletrica.supply_crate":"Caixa de Suprimentos",
  "item.mareeletrica.rooster_egg_dourado":"Ovo Raro (Galo Dourado)","entity.mareeletrica.rare_egg":"Ovo Raro","key.mareeletrica.jet_eject":"Caça: ejetar"},
 'en_us':{"item.mareeletrica.golf_club":"Golf Club","item.mareeletrica.golf_ball":"Golf Ball","block.mareeletrica.golf_hole":"Golf Hole","item.mareeletrica.bowling_ball":"Bowling Ball",
  "block.mareeletrica.bowling_pinsetter":"Bowling Pinsetter","entity.mareeletrica.bowling_pin":"Bowling Pin","entity.mareeletrica.golf_ball":"Golf Ball","entity.mareeletrica.bowling_ball":"Bowling Ball",
  "item.mareeletrica.support_radio":"Support Radio","entity.mareeletrica.supply_crate":"Supply Crate",
  "item.mareeletrica.rooster_egg_dourado":"Rare Egg (Golden Rooster)","entity.mareeletrica.rare_egg":"Rare Egg","key.mareeletrica.jet_eject":"Jet: eject"}}
for lang,kv in LANG.items():
    p=os.path.join(A,'lang',lang+'.json');dd=json.load(open(p,encoding='utf-8'));dd.update(kv);json.dump(dd,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.65.4 assets ok')
