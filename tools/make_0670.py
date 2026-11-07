"""0.67.0: minigolf blocks + windmill, ping-pong table/paddle, surfboard + wave machine: models, textures, blockstates, loot, recipes, lang."""
import json,os,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
V=lambda n:"minecraft:block/"+n
M=lambda n:"mareeletrica:block/"+n
def el(f,t,tex,rot=None,faces=None):
    for v in f+t:assert -16<=v<=32,(f,t)
    fs={}
    for d in faces or ["north","south","west","east","up","down"]:fs[d]={"texture":"#"+tex}
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":fs}
    if rot:e["rotation"]=rot
    return e
def save(path,obj):
    os.makedirs(os.path.dirname(path),exist_ok=True);json.dump(obj,open(path,'w'),separators=(',',':'))
def model(name,tex,elements,item=True,display=None):
    m={"parent":"minecraft:block/block","textures":tex,"elements":elements}
    if display:m["display"]=display
    save(os.path.join(A,'models/block',name+'.json'),m)
    if item:save(os.path.join(A,'models/item',name+'.json'),{"parent":"mareeletrica:block/"+name})
ROT={"north":0,"east":90,"south":180,"west":270}
def facing_states(name,model_name=None):
    save(os.path.join(A,'blockstates',name+'.json'),{"variants":{f"facing={f}":{"model":"mareeletrica:block/"+(model_name or name),**({"y":r} if r else {})} for f,r in ROT.items()}})
def single_state(name):save(os.path.join(A,'blockstates',name+'.json'),{"variants":{"":{"model":"mareeletrica:block/"+name}}})
def loot(name):save(os.path.join(D,'loot_table/blocks',name+'.json'),{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:"+name}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
def shaped(name,pat,key,n=1):
    save(os.path.join(D,'recipe',name+'.json'),{"type":"minecraft:crafting_shaped","category":"misc","pattern":pat,"key":{k:({"tag":v[1:]} if v.startswith('#') else {"item":v}) for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}})
def png(path,img):os.makedirs(os.path.dirname(path),exist_ok=True);img.save(path)
random.seed(670)

# ---------------- textures ----------------
turf=Image.new('RGBA',(16,16))
for x in range(16):
    for y in range(16):
        g=random.randint(0,3);base=[(62,160,58),(70,172,62),(56,148,52),(76,180,70)][g]
        turf.putpixel((x,y),base+(255,))
png(os.path.join(A,'textures/block/minigolf_turf.png'),turf)
mat=Image.new('RGBA',(16,16))
for x in range(16):
    for y in range(16):mat.putpixel((x,y),(38,110,40,255) if (x+y)%2 else (34,100,36,255))
png(os.path.join(A,'textures/block/minigolf_mat.png'),mat)
front=Image.new('RGBA',(16,16),(40,62,78,255));dr=ImageDraw.Draw(front)
dr.rectangle([0,0,15,15],outline=(150,160,170,255))
for i,y in enumerate((4,8,12)):
    for x in range(1,15):
        import math
        yy=y+round(math.sin((x+i*2)/2.2)*1.3);front.putpixel((x,yy),(90,190,230,255));front.putpixel((x,min(14,yy+1)),(40,120,180,255))
png(os.path.join(A,'textures/block/wave_machine_front.png'),front)
# ping-pong paddle (item)
pad=Image.new('RGBA',(16,16),(0,0,0,0));dr=ImageDraw.Draw(pad)
dr.rectangle([2,11,4,13],fill=(170,120,70,255));dr.rectangle([3,10,5,12],fill=(150,100,58,255));dr.line([(1,14),(5,10)],fill=(120,80,45,255),width=2)
dr.ellipse([5,1,15,11],fill=(200,30,36,255),outline=(60,30,20,255));dr.ellipse([7,3,11,7],fill=(225,60,60,255))
png(os.path.join(A,'textures/item/pingpong_paddle.png'),pad)
save(os.path.join(A,'models/item/pingpong_paddle.json'),{"parent":"minecraft:item/handheld","textures":{"layer0":"mareeletrica:item/pingpong_paddle"},
    "display":{"thirdperson_righthand":{"rotation":[0,-90,40],"translation":[0,3,1.5],"scale":[.75,.75,.75]},"thirdperson_lefthand":{"rotation":[0,90,-40],"translation":[0,3,1.5],"scale":[.75,.75,.75]},
               "firstperson_righthand":{"rotation":[0,-90,25],"translation":[1.13,3.2,1.13],"scale":[.6,.6,.6]},"firstperson_lefthand":{"rotation":[0,90,-25],"translation":[1.13,3.2,1.13],"scale":[.6,.6,.6]}}})
# field kit icon: the golf kit with a small red windmill on it
kit=Image.open(os.path.join(A,'textures/item/field_kit_golfe.png')).convert('RGBA');dr=ImageDraw.Draw(kit)
dr.rectangle([9,9,13,14],fill=(235,230,220,255));dr.polygon([(8,9),(11,6),(14,9)],fill=(200,40,40,255))
dr.line([(9,4),(13,8)],fill=(90,60,40,255));dr.line([(13,4),(9,8)],fill=(90,60,40,255))
png(os.path.join(A,'textures/item/field_kit_minigolfe.png'),kit)
save(os.path.join(A,'models/item/field_kit_minigolfe.json'),{"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/field_kit_minigolfe"}})

# ---------------- minigolf ----------------
TT={"turf":M("minigolf_turf"),"particle":M("minigolf_turf")}
model('minigolf_turf',{"all":M("minigolf_turf"),"particle":M("minigolf_turf")},[{"from":[0,0,0],"to":[16,16,16],"faces":{d:{"texture":"#all","cullface":d} for d in ["north","south","west","east","up","down"]}}])
single_state('minigolf_turf')
model('minigolf_border',{"wood":V("stripped_spruce_log"),"cap":V("white_concrete"),"particle":V("stripped_spruce_log")},
      [el([0,0,0],[16,4.2,16],'wood'),el([0,4.2,0],[16,5,16],'cap')])
single_state('minigolf_border')
model('minigolf_tee',{"turf":M("minigolf_turf"),"mat":M("minigolf_mat"),"white":V("white_concrete"),"particle":M("minigolf_turf")},
      [el([0,0,0],[16,16,16],'turf'),el([3,16,3],[13,16.3,13],'mat'),el([7.3,16.3,7.3],[8.7,16.6,8.7],'white')])
single_state('minigolf_tee')
RAMP=[]
for i in range(8):RAMP.append(el([0,0,14-2*i],[16,2*(i+1),16-2*i],'turf'))
RAMP+= [el([-.4,0,0],[0,16,16],'wood',faces=["west","east","up","north","south"]),el([16,0,0],[16.4,16,16],'wood',faces=["west","east","up","north","south"])]
model('minigolf_ramp',{"turf":M("minigolf_turf"),"wood":V("stripped_spruce_log"),"particle":M("minigolf_turf")},RAMP)
facing_states('minigolf_ramp')
WT={"wall":V("white_terracotta"),"roof":V("red_terracotta"),"dark":V("black_concrete"),"wood":V("spruce_planks"),"particle":V("white_terracotta")}
WM=[el([0,0,0],[4,16,16],'wall'),el([12,0,0],[16,16,16],'wall'),el([4,7,0],[12,16,16],'wall'),
    el([4,0,.5],[12,7,15.5],'dark',faces=["up","west","east"]),   # tunnel lining
    el([-1,16,-1],[17,18,17],'roof'),el([1,18,1],[15,21,15],'roof'),el([3,21,3],[13,24,13],'roof'),el([5,24,5],[11,27,11],'roof'),el([7,27,7],[9,29,9],'wood'),
    el([6,9,-.3],[10,13,0],'wood'),el([5.5,15,-.6],[10.5,17,0],'wood'),   # window frame + hub mount
    el([5,8,16],[11,13,16.3],'wood')]
model('minigolf_windmill',WT,WM)
facing_states('minigolf_windmill')
# blades: a cross around the hub (8,16) in front of the house; the BER spins it
BT={"wood":V("spruce_planks"),"sail":V("white_wool"),"hub":V("red_terracotta"),"particle":V("white_wool")}
BL=[el([7.4,1,-1.8],[8.6,31,-1.2],'wood'),el([-7,15.4,-1.8],[23,16.6,-1.2],'wood'),
    el([8.6,19,-1.6],[12,30,-1.3],'sail'),el([4,2,-1.6],[7.4,13,-1.3],'sail'),
    el([11,16.6,-1.6],[22,20,-1.3],'sail'),el([-6,12,-1.6],[5,15.4,-1.3],'sail'),
    el([6.8,14.8,-2.4],[9.2,17.2,-1.2],'hub')]
model('minigolf_windmill_blades',BT,BL,item=False)
# putter: the golf club with a flat blade and a blue grip
gc=json.load(open(os.path.join(A,'models/item/golf_club.json')))
gc['textures']['grip']=V("blue_concrete")
for e in gc['elements']:
    fc=set(f['texture'] for f in e['faces'].values())
    if fc=={'#black'}:
        for f in e['faces'].values():f['texture']='#grip'
gc['elements']=[e for e in gc['elements'] if set(f['texture'] for f in e['faces'].values())!={'#steel'} and set(f['texture'] for f in e['faces'].values())!={'#gray'}]
gc['elements']+= [el([3.5,-14,7.5],[12.5,-11.6,8.5],'steel'),el([7.2,-11.6,7.2],[8.8,-10.6,8.8],'gray')]
save(os.path.join(A,'models/item/minigolf_putter.json'),gc)
for n in ('minigolf_turf','minigolf_border','minigolf_tee','minigolf_ramp','minigolf_windmill'):loot(n)
shaped('minigolf_turf',["GG","GG"],{"G":"minecraft:green_wool"},4)
shaped('minigolf_border',["SSS"],{"S":"minecraft:spruce_slab"},6)
shaped('minigolf_tee',["W","T"],{"W":"minecraft:white_carpet","T":"mareeletrica:minigolf_turf"},1)
shaped('minigolf_ramp',["  T"," TT","TTT"],{"T":"mareeletrica:minigolf_turf"},4)
shaped('minigolf_windmill',["RRR","WSW","W W"],{"R":"minecraft:red_terracotta","W":"minecraft:white_terracotta","S":"minecraft:white_wool"},1)
shaped('minigolf_putter',["  I"," S ","B  "],{"I":"minecraft:iron_ingot","S":"minecraft:stick","B":"minecraft:blue_wool"},1)
shaped('field_kit_minigolfe',["TWT","TPT","TTT"],{"T":"mareeletrica:minigolf_turf","W":"mareeletrica:minigolf_windmill","P":"mareeletrica:minigolf_putter"},1)

# ---------------- ping-pong table: 6 parts (col 0..1 × row 0..2), north frame: col 1 to the west, row 2 to the north ----------------
PT={"blue":V("blue_concrete"),"white":V("white_concrete"),"leg":V("gray_concrete"),"frame":V("black_concrete"),"net":V("light_gray_wool"),"particle":V("blue_concrete")}
variants={}
for c in range(2):
    for r in range(3):
        E=[el([0,12,0],[16,13,16],'blue'),el([0,11.4,0],[16,12,16],'frame',faces=["north","south","west","east","down"])]
        up=["up"]
        if c==0:E.append(el([15.4,13,0],[16,13.08,16],'white',faces=up));E.append(el([0,13,0],[.15,13.08,16],'white',faces=up))
        else:E.append(el([0,13,0],[.6,13.08,16],'white',faces=up));E.append(el([15.85,13,0],[16,13.08,16],'white',faces=up))
        if r==0:E.append(el([0,13,15.4],[16,13.08,16],'white',faces=up))
        if r==2:E.append(el([0,13,0],[16,13.08,.6],'white',faces=up))
        if r!=1:
            lx=(13,14.6) if c==0 else (1.4,3)
            lz=(13,14.6) if r==0 else (1.4,3)
            E.append(el([lx[0],0,lz[0]],[lx[1],11.4,lz[1]],'leg'))
            E.append(el([lx[0]-.4,0,lz[0]-.4],[lx[1]+.4,.6,lz[1]+.4],'frame'))
        else:
            E.append(el([0,13,7.85],[16,16.2,8.15],'net'))
            E.append(el([0,16.2,7.75],[16,16.7,8.25],'white'))
            px=(16,16.7) if c==0 else (-.7,0)
            E.append(el([px[0],11.4,7.6],[px[1],16.8,8.4],'frame'))
            E.append(el([5,4,7.5],[11,5,8.5] if True else None,'frame'))   # cross brace under the net
        name=f'pingpong_table_{c}{r}'
        model(name,PT,E,item=False)
        for rot,y in {0:180,1:270,2:0,3:90}.items():
            variants[f"part={c*3+r},rot={rot}"]={"model":"mareeletrica:block/"+name,**({"y":y} if y else {})}
save(os.path.join(A,'blockstates/pingpong_table.json'),{"variants":variants})
# item: a mini table
IE=[el([0,8,2],[16,9,14],'blue'),el([7.8,9,2],[8.2,9.05,14],'white'),el([7.9,9,1.6],[8.1,11,14.4],'net'),el([1,0,3],[2,8,4],'leg'),el([14,0,3],[15,8,4],'leg'),el([1,0,12],[2,8,13],'leg'),el([14,0,12],[15,8,13],'leg')]
save(os.path.join(A,'models/item/pingpong_table.json'),{"parent":"minecraft:block/block","textures":PT,"elements":IE})
shaped('pingpong_table',["BWB","PPP","S S"],{"B":"minecraft:blue_concrete","W":"minecraft:white_wool","P":"#minecraft:planks","S":"minecraft:stick"},1)
shaped('pingpong_paddle',[" R"," P","S "],{"R":"minecraft:red_wool","P":"#minecraft:planks","S":"minecraft:stick"},1)

# ---------------- surf ----------------
ST={"deck":V("white_concrete"),"stripe":V("cyan_concrete"),"rail":V("light_blue_concrete"),"fin":V("black_concrete"),"wax":V("yellow_concrete"),"particle":V("white_concrete")}
# outline (z from tail -8 to nose +24, width in px)
W={-8:5,-7:7,-6:8,-4:9,-1:9.6,4:10,10:10,14:9.4,17:8.4,19:7,21:5.4,22.5:3.8,23.5:2}
zs=sorted(W);SB=[]
for i in range(len(zs)-1):
    z0,z1=zs[i],zs[i+1];w=W[z0] if z0<4 else W[z1];w=(W[z0]+W[z1])/2
    SB.append(el([8-w/2,0,z0],[8+w/2,1.4,z1],'deck'))
    SB.append(el([8-w/2-.01,-.01,z0],[8-w/2+.35,1.2,z1],'rail',faces=["west","down"]));SB.append(el([8+w/2-.35,-.01,z0],[8+w/2+.01,1.2,z1],'rail',faces=["east","down"]))
SB.append(el([7.5,1.4,-7],[8.5,1.45,23],'stripe',faces=["up"]))
SB.append(el([5.2,1.4,-5],[10.8,1.43,4],'wax',faces=["up"]))
SB.append(el([7.7,-2.6,-6.6],[8.3,0,-3.6],'fin'))
model('surfboard',ST,SB,item=False)
save(os.path.join(A,'models/item/surfboard.json'),{"parent":"mareeletrica:block/surfboard",
    "display":{"gui":{"rotation":[90,0,45],"translation":[0,0,0],"scale":[.42,.42,.42]},"ground":{"translation":[0,3,0],"scale":[.3,.3,.3]},
               "thirdperson_righthand":{"rotation":[0,0,90],"translation":[0,-2,4],"scale":[.45,.45,.45]},"thirdperson_lefthand":{"rotation":[0,0,-90],"translation":[0,-2,4],"scale":[.45,.45,.45]},
               "firstperson_righthand":{"rotation":[0,10,80],"translation":[0,-2,0],"scale":[.35,.35,.35]},"firstperson_lefthand":{"rotation":[0,-10,-80],"translation":[0,-2,0],"scale":[.35,.35,.35]},
               "fixed":{"rotation":[90,0,45],"scale":[.42,.42,.42]}}})
save(os.path.join(A,'models/block/wave_machine.json'),{"parent":"minecraft:block/orientable","textures":{"front":M("wave_machine_front"),"side":V("prismarine_bricks"),"top":V("dark_prismarine")}})
save(os.path.join(A,'models/item/wave_machine.json'),{"parent":"mareeletrica:block/wave_machine"})
facing_states('wave_machine');loot('wave_machine')
shaped('surfboard',["  W"," P ","C  "],{"W":"minecraft:white_wool","P":"#minecraft:planks","C":"minecraft:cyan_dye"},1)
shaped('wave_machine',["PRP","PSP","PRP"],{"P":"minecraft:prismarine_bricks","R":"minecraft:redstone","S":"minecraft:piston"},1)

LANG={'pt_br':{"block.mareeletrica.minigolf_turf":"Grama de Minigolfe","block.mareeletrica.minigolf_border":"Borda de Minigolfe","block.mareeletrica.minigolf_tee":"Saída de Minigolfe",
               "block.mareeletrica.minigolf_ramp":"Rampa de Minigolfe","block.mareeletrica.minigolf_windmill":"Moinho de Minigolfe","item.mareeletrica.minigolf_putter":"Putter de Minigolfe",
               "block.mareeletrica.pingpong_table":"Mesa de Pingue-Pongue","item.mareeletrica.pingpong_table":"Mesa de Pingue-Pongue","item.mareeletrica.pingpong_paddle":"Raquete de Pingue-Pongue",
               "entity.mareeletrica.pingpong_ball":"Bolinha de Pingue-Pongue","item.mareeletrica.surfboard":"Prancha de Surfe","entity.mareeletrica.surfboard":"Prancha de Surfe",
               "entity.mareeletrica.surf_wave":"Onda","block.mareeletrica.wave_machine":"Máquina de Ondas","item.mareeletrica.field_kit_minigolfe":"Minigolfe (pronto)"},
      'en_us':{"block.mareeletrica.minigolf_turf":"Minigolf Turf","block.mareeletrica.minigolf_border":"Minigolf Border","block.mareeletrica.minigolf_tee":"Minigolf Tee",
               "block.mareeletrica.minigolf_ramp":"Minigolf Ramp","block.mareeletrica.minigolf_windmill":"Minigolf Windmill","item.mareeletrica.minigolf_putter":"Minigolf Putter",
               "block.mareeletrica.pingpong_table":"Ping-Pong Table","item.mareeletrica.pingpong_table":"Ping-Pong Table","item.mareeletrica.pingpong_paddle":"Ping-Pong Paddle",
               "entity.mareeletrica.pingpong_ball":"Ping-Pong Ball","item.mareeletrica.surfboard":"Surfboard","entity.mareeletrica.surfboard":"Surfboard",
               "entity.mareeletrica.surf_wave":"Wave","block.mareeletrica.wave_machine":"Wave Machine","item.mareeletrica.field_kit_minigolfe":"Minigolf (ready)"}}
for lang,kv in LANG.items():
    pp=os.path.join(A,'lang',lang+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update(kv);json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.67.0 assets ok')
