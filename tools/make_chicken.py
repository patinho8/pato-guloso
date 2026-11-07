"""0.54: Galinha de Briga (fighting rooster) textures, part models, item models, lang, recipes."""
import json,os,random,math
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
TB=os.path.join(A,'textures/block');random.seed(42)
def tex(name,base,var=12,lines=None,sheen=None):
    im=Image.new('RGBA',(16,16))
    for y in range(16):
        for x in range(16):
            k=random.randint(-var,var);c=[max(0,min(255,v+k)) for v in base]
            if lines and (x+y*2)%lines==0:c=[int(v*.72) for v in c]
            if sheen and (x*3+y)%7==0:c=[max(0,min(255,int(a*.6+b*.4))) for a,b in zip(c,sheen)]
            im.putpixel((x,y),tuple(c)+(255,))
    im.save(os.path.join(TB,name+'.png'))
tex('rooster_red',(150,46,26),14,lines=5)
tex('rooster_gold',(232,150,40),16,lines=6)
tex('rooster_tail',(26,30,28),8,sheen=(30,140,90))
tex('rooster_wing',(110,52,30),12,lines=4)
tex('rooster_wingtip',(40,34,30),8,lines=3)
tex('rooster_comb',(214,30,30),14)
tex('rooster_beak',(240,190,50),10)
tex('rooster_leg',(226,170,60),10)
tex('rooster_eye',(250,250,245),2)
tex('rooster_pupil',(15,10,10),3)
T={"red":"mareeletrica:block/rooster_red","gold":"mareeletrica:block/rooster_gold","tail":"mareeletrica:block/rooster_tail","wing":"mareeletrica:block/rooster_wing",
   "tip":"mareeletrica:block/rooster_wingtip","comb":"mareeletrica:block/rooster_comb","beak":"mareeletrica:block/rooster_beak","leg":"mareeletrica:block/rooster_leg",
   "eye":"mareeletrica:block/rooster_eye","pupil":"mareeletrica:block/rooster_pupil","brow":"minecraft:block/black_concrete","spur":"mareeletrica:block/base_steel","particle":"mareeletrica:block/rooster_red"}
def b(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2]}
    return e
BODY=[b([4,5,3],[12,13,13],'red'),b([5,5.5,2],[11,11.5,4],'red'),b([5.5,11,2.2],[10.5,15,6],'gold'),b([5,12.5,5],[11,13.6,10],'gold'),
 b([5,9,12],[11,17,14],'tail',([8,9,13],'x',22.5)),b([6,13,13],[10,20,15],'tail',([8,13,14],'x',45)),b([7,14,13.5],[9,22,15],'tail',([8,14,14],'x',22.5)),
 b([4.5,4.5,4],[11.5,6,12],'red')]
HEAD=[b([5.5,14,1],[10.5,19.5,6],'gold'),
 b([7,16,-1.6],[9,17.2,1],'beak'),b([7.3,15.3,-1.4],[8.7,16,0.6],'beak'),
 b([7.2,13,-0.2],[8.8,15.4,1.1],'comb'),
 b([6.9,19.5,1.2],[9.1,21,5.4],'comb'),b([7.2,21,1.4],[8.8,22.8,2.6],'comb'),b([7.2,21,3],[8.8,23.4,4.2],'comb'),b([7.2,21,4.4],[8.8,22.4,5.6],'comb'),
 # angry eyes on the face, with frowning brows
 b([5.8,16.7,0.85],[7.4,18.2,1],'eye'),b([6.5,16.7,0.75],[7.3,17.6,0.85],'pupil'),
 b([8.6,16.7,0.85],[10.2,18.2,1],'eye'),b([8.7,16.7,0.75],[9.5,17.6,0.85],'pupil'),
 b([5.4,18.4,0.6],[7.9,19.1,0.85],'brow',([6.65,18.75,0.7],'z',-22.5)),b([8.1,18.4,0.6],[10.6,19.1,0.85],'brow',([9.35,18.75,0.7],'z',22.5)),
]
WL=[b([3,6.5,4],[4.2,12.5,12.5],'wing'),b([3.1,5.2,8.5],[4.1,8,13.8],'tip'),b([3.05,9,10.5],[4.15,12,13],'tip')]
WR=[b([11.8,6.5,4],[13,12.5,12.5],'wing'),b([11.9,5.2,8.5],[12.9,8,13.8],'tip'),b([11.85,9,10.5],[12.95,12,13],'tip')]
LEG=[b([7.4,0.5,7.4],[8.6,6,8.6],'leg'),b([6.8,4.5,6.8],[9.2,6.5,9.2],'red'),
 b([7.6,0,4.6],[8.4,.6,8.2],'leg'),b([6.2,0,6],[7.6,.6,8],'leg',([7.6,0,8],'y',22.5)),b([8.4,0,6],[9.8,.6,8],'leg',([8.4,0,8],'y',-22.5)),b([7.6,0,8.2],[8.4,.6,9.8],'leg'),
 b([7.7,2,8.6],[8.3,2.6,10.4],'spur')]
def save(kind,name,els,display=None):
    m={"textures":T,"elements":els}
    if display:m["display"]=display
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
for n,e in (('fchicken_body',BODY),('fchicken_head',HEAD),('fchicken_wing_l',WL),('fchicken_wing_r',WR),('fchicken_leg',LEG)):save('block',n,e)
def shift(els,dx):
    out=[]
    for e in els:
        e=json.loads(json.dumps(e));e['from'][0]+=dx;e['to'][0]+=dx
        if 'rotation' in e:e['rotation']['origin'][0]+=dx
        out.append(e)
    return out
DISP={"gui":{"rotation":[20,-150,0],"translation":[0,-2,0],"scale":[.72,.72,.72]},"ground":{"translation":[0,2,0],"scale":[.4,.4,.4]},"fixed":{"rotation":[0,180,0],"scale":[.7,.7,.7]},
 "thirdperson_righthand":{"rotation":[0,180,0],"translation":[0,1,1],"scale":[.4,.4,.4]},"thirdperson_lefthand":{"rotation":[0,180,0],"translation":[0,1,1],"scale":[.4,.4,.4]},
 "firstperson_righthand":{"rotation":[0,160,0],"translation":[1,2,0],"scale":[.45,.45,.45]},"firstperson_lefthand":{"rotation":[0,160,0],"translation":[1,2,0],"scale":[.45,.45,.45]}}
save('item','fighting_chicken',BODY+HEAD+WL+WR+shift(LEG,-1.6)+shift(LEG,1.6),DISP)
W={"gui":{"rotation":[25,-30,0],"scale":[1.2,1.2,1.2]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"fixed":{"scale":[1,1,1]},
 "thirdperson_righthand":{"translation":[0,3,1],"scale":[.6,.6,.6]},"thirdperson_lefthand":{"translation":[0,3,1],"scale":[.6,.6,.6]},
 "firstperson_righthand":{"rotation":[0,-25,0],"translation":[1,4,-1],"scale":[.7,.7,.7]},"firstperson_lefthand":{"rotation":[0,-25,0],"translation":[1,4,-1],"scale":[.7,.7,.7]}}
save('item','fight_whistle',[b([3,6,6.5],[11,9,9.5],'beak'),b([9,6.5,6.4],[10.5,8.5,9.6],'brow'),b([2,6.6,7],[3,8.4,9],'spur'),b([4.5,9,7],[7,10,9],'comb'),b([10.5,7,7.5],[13,8,8.5],'comb')],W)
for lang in('pt_br','en_us'):
    p=os.path.join(A,'lang',lang+'.json');j=json.load(open(p))
    j['entity.mareeletrica.fighting_chicken']='Galinha de Briga' if lang=='pt_br' else 'Fighting Rooster'
    j['item.mareeletrica.fighting_chicken']='Galinha de Briga' if lang=='pt_br' else 'Fighting Rooster'
    j['item.mareeletrica.fight_whistle']='Apito de Briga' if lang=='pt_br' else 'Fight Whistle'
    json.dump(j,open(p,'w'),ensure_ascii=False,indent=1)
json.dump({"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":"minecraft:egg"},{"item":"minecraft:feather"},{"item":"minecraft:iron_ingot"},{"item":"minecraft:blaze_powder"},{"tag":"c:seeds"}],"result":{"id":"mareeletrica:fighting_chicken","count":1}},open(os.path.join(D,'recipe/fighting_chicken.json'),'w'))
json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":[" N ","NIN"],"key":{"N":{"item":"minecraft:iron_nugget"},"I":{"item":"minecraft:gold_ingot"}},"result":{"id":"mareeletrica:fight_whistle","count":1}},open(os.path.join(D,'recipe/fight_whistle.json'),'w'))
print('chicken ok')

# ---------- 0.55: breeds (colour + type), eggs, rooster ring ----------
BREEDS={ # id: (label, body, neck, tail, wing, catalyst item, type label)
 "normal":("Galo de Briga",(150,46,26),(232,150,40),(26,30,28),(110,52,30),"minecraft:wheat"),
 "fogo":("Galo Brasa",(205,62,22),(255,176,44),(96,20,10),(178,40,18),"minecraft:magma_cream"),
 "agua":("Galo Maré",(40,92,196),(116,196,244),(18,40,112),(30,70,162),"minecraft:prismarine_shard"),
 "planta":("Galo Folha",(62,140,50),(176,214,84),(30,80,30),(50,112,40),"minecraft:moss_block"),
 "eletrico":("Galo Faísca",(232,200,40),(255,242,124),(56,56,70),(204,170,30),"minecraft:redstone"),
 "gelo":("Galo Geada",(206,232,246),(150,210,242),(88,150,204),(172,212,236),"minecraft:snowball"),
 "pedra":("Galo Rocha",(122,112,100),(172,152,122),(58,54,50),(100,90,80),"minecraft:cobblestone"),
 "vento":("Galo Ventania",(236,236,242),(204,222,252),(118,158,222),(222,226,236),"minecraft:phantom_membrane"),
 "sombra":("Galo Sombrio",(42,30,56),(112,62,152),(14,10,20),(62,40,82),"minecraft:ink_sac"),
 "dourado":("Galo Dourado",(226,176,36),(255,226,108),(150,98,18),(208,158,28),None)}
def retex(els,mp):
    out=json.loads(json.dumps(els))
    for e in out:
        for f in e['faces'].values():
            k=f['texture'][1:]
            if k in mp:f['texture']='#'+mp[k]
    return out
for bid,(label,body,neck,tail,wing,cat) in BREEDS.items():
    tex('rooster_'+bid+'_body',body,14,lines=5);tex('rooster_'+bid+'_neck',neck,16,lines=6)
    tex('rooster_'+bid+'_tail',tail,8,sheen=tuple(min(255,v+70) for v in tail));tex('rooster_'+bid+'_wing',wing,12,lines=4)
    tex('rooster_'+bid+'_tip',tuple(int(v*.45) for v in wing),8,lines=3)
    T.update({"red_"+bid:"mareeletrica:block/rooster_"+bid+"_body","gold_"+bid:"mareeletrica:block/rooster_"+bid+"_neck","tail_"+bid:"mareeletrica:block/rooster_"+bid+"_tail",
              "wing_"+bid:"mareeletrica:block/rooster_"+bid+"_wing","tip_"+bid:"mareeletrica:block/rooster_"+bid+"_tip"})
    mp={"red":"red_"+bid,"gold":"gold_"+bid,"tail":"tail_"+bid,"wing":"wing_"+bid,"tip":"tip_"+bid}
    parts={'fchicken_body_':retex(BODY,mp),'fchicken_head_':retex(HEAD,mp),'fchicken_wing_l_':retex(WL,mp),'fchicken_wing_r_':retex(WR,mp)}
    for n,e in parts.items():save('block',n+bid,e)
    save('item','rooster_egg_'+bid,parts['fchicken_body_']+parts['fchicken_head_']+parts['fchicken_wing_l_']+parts['fchicken_wing_r_']+shift(LEG,-1.6)+shift(LEG,1.6),DISP)
    if cat:json.dump({"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":"mareeletrica:fighting_chicken"},{"item":cat}],"result":{"id":"mareeletrica:rooster_egg_"+bid,"count":1}},open(os.path.join(D,'recipe/rooster_egg_'+bid+'.json'),'w'))
# re-save all part models so the shared texture map includes every breed
for n,e in (('fchicken_body',BODY),('fchicken_head',HEAD),('fchicken_wing_l',WL),('fchicken_wing_r',WR),('fchicken_leg',LEG)):save('block',n,e)
# the ring: canvas floor, apron, 4 posts (red/blue corners), 3 ropes; drawn stretched 5/3 wide by the renderer
tex('ring_canvas',(226,222,206),6,lines=None);tex('ring_apron',(30,40,90),8,lines=4);tex('rope_red',(200,30,30),10,lines=3);tex('rope_white',(240,240,240),6,lines=3);tex('rope_blue',(30,70,200),10,lines=3)
tex('ring_post_red',(180,30,30),8);tex('ring_post_blue',(30,60,180),8);tex('ring_pad',(30,30,34),6)
T.update({"canvas":"mareeletrica:block/ring_canvas","apron":"mareeletrica:block/ring_apron","ropeR":"mareeletrica:block/rope_red","ropeW":"mareeletrica:block/rope_white","ropeB":"mareeletrica:block/rope_blue",
          "postR":"mareeletrica:block/ring_post_red","postB":"mareeletrica:block/ring_post_blue","pad":"mareeletrica:block/ring_pad"})
R=[b([-16,0,-16],[32,3.6,32],'apron'),b([-15.5,3.6,-15.5],[31.5,4,31.5],'canvas')]
posts=[(-16,-16,'postR'),(30.2,-16,'postB'),(-16,30.2,'postB'),(30.2,30.2,'postR')]
for x,z,t in posts:R+= [b([x,4,z],[x+1.8,26,z+1.8],t),b([max(-16,x-.2),20,max(-16,z-.2)],[min(32,x+2),24,min(32,z+2)],'pad')]
for y,t in ((12,'ropeR'),(17,'ropeW'),(22,'ropeB')):
    R+= [b([-14.2,y,-15.6],[30.2,y+.7,-15],t),b([-14.2,y,31],[30.2,y+.7,31.6],t),b([-15.6,y,-14.2],[-15,y+.7,30.2],t),b([31,y,-14.2],[31.6,y+.7,30.2],t)]
save('block','rooster_ring',R)
json.dump({"variants":{"facing=north":{"model":"mareeletrica:block/empty"},"facing=south":{"model":"mareeletrica:block/empty"},"facing=east":{"model":"mareeletrica:block/empty"},"facing=west":{"model":"mareeletrica:block/empty"}}},open(os.path.join(A,'blockstates/rooster_ring.json'),'w'))
RD=dict(DISP,gui={"rotation":[30,225,0],"translation":[0,0,0],"scale":[.36,.36,.36]})
save('item','rooster_ring',R,RD)
json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:rooster_ring"}]}]},open(os.path.join(D,'loot_table/blocks/rooster_ring.json'),'w'))
json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":["RSB","WWW"],"key":{"R":{"item":"minecraft:red_wool"},"B":{"item":"minecraft:blue_wool"},"S":{"item":"minecraft:string"},"W":{"item":"minecraft:white_wool"}},"result":{"id":"mareeletrica:rooster_ring","count":1}},open(os.path.join(D,'recipe/rooster_ring.json'),'w'))
for lang in('pt_br','en_us'):
    p=os.path.join(A,'lang',lang+'.json');j=json.load(open(p))
    j['block.mareeletrica.rooster_ring']='Ringue de Galos' if lang=='pt_br' else 'Rooster Ring'
    j['item.mareeletrica.fighting_chicken']='Ovo Surpresa de Galo' if lang=='pt_br' else 'Mystery Rooster Egg'
    for bid,(label,*_) in BREEDS.items():j['item.mareeletrica.rooster_egg_'+bid]='Ovo de '+label
    json.dump(j,open(p,'w'),ensure_ascii=False,indent=1)
print('breeds + ring ok')
