"""0.61.0: first-person cockpits (helicopter, fighter jet, B-2), sapo (frog) assets, new key names."""
import json,os,copy
from PIL import Image,ImageDraw
A=os.path.join(os.path.dirname(os.path.abspath(__file__)),'..','src','main','resources','assets','mareeletrica')
M=os.path.join(A,'models');T=os.path.join(A,'textures')
def save(p,d):json.dump(d,open(os.path.join(M,p+'.json'),'w'))
def load(p):return json.load(open(os.path.join(M,p+'.json')))
FACES=['north','south','east','west','up','down']
def box(f,t,tex,faces=None,rot=None):
    e={"from":list(f),"to":list(t),"faces":{k:{"texture":'#'+(faces.get(k,tex) if faces else tex),"uv":[0,0,16,16]} for k in FACES}}
    if rot:e["rotation"]=rot
    return e
# ------------------------------------------------------------------ textures
def png(name,draw,size=16):
    im=Image.new('RGBA',(size,size),(0,0,0,255));d=ImageDraw.Draw(im);draw(im,d);im.save(os.path.join(T,'block',name+'.png'))
def mfd(im,d):   # multi-function display: bezel + green attitude/radar screen
    d.rectangle([0,0,15,15],fill=(38,40,44,255));d.rectangle([1,1,14,14],fill=(18,20,22,255))
    d.rectangle([2,2,13,13],fill=(8,30,18,255))
    d.line([2,8,13,7],fill=(80,230,120,255));d.line([7,4,8,4],fill=(80,230,120,255));d.line([7,11,8,11],fill=(80,230,120,255))
    d.point([(4,5),(11,5),(4,10),(11,10)],fill=(60,170,90,255));d.line([6,8,9,8],fill=(200,255,200,255))
    for x in (0,15):
        for y in (4,7,10):d.point((x,y),fill=(120,120,125,255))
def gauges(im,d):   # four round dials with needles
    d.rectangle([0,0,15,15],fill=(30,32,35,255))
    for cx,cy,a in ((4,4,(6,2)),(11,4,(13,5)),(4,11,(2,10)),(11,11,(12,9))):
        d.ellipse([cx-3,cy-3,cx+3,cy+3],fill=(14,14,16,255),outline=(150,150,155,255))
        d.line([cx,cy,a[0],a[1]],fill=(255,200,60,255))
def buttons(im,d):   # switches and lit buttons
    d.rectangle([0,0,15,15],fill=(34,36,40,255))
    cols=[(220,60,50),(60,200,90),(240,190,50),(70,140,230)]
    for i,(x,y) in enumerate([(2,2),(6,2),(10,2),(13,2),(2,7),(6,7),(10,7),(13,7)]):d.rectangle([x,y,x+1,y+1],fill=cols[i%4]+(255,))
    for x in range(2,15,3):d.line([x,11,x,14],fill=(170,170,175,255))
def hud(im,d):   # head-up display frame: green symbology on transparent glass
    im.paste((0,0,0,0),[0,0,16,16]);g=(90,255,130,255)
    d.line([3,8,6,8],fill=g);d.line([9,8,12,8],fill=g);d.point((7,8),fill=g);d.point((8,8),fill=g)
    d.line([7,6,8,6],fill=g);d.line([2,3,2,13],fill=g);d.line([13,3,13,13],fill=g)
    for y in (4,6,10,12):d.point((1,y),fill=g);d.point((14,y),fill=g)
png('cockpit_mfd',mfd);png('cockpit_gauges',gauges);png('cockpit_buttons',buttons);png('cockpit_hud',hud)
VTEX={'olive':'mareeletrica:block/veh_olive','olived':'mareeletrica:block/veh_olive_dark','metal':'mareeletrica:block/veh_metal','dark':'mareeletrica:block/veh_dark',
      'black':'minecraft:block/black_concrete','seat':'minecraft:block/black_wool','mfd':'mareeletrica:block/cockpit_mfd','gauges':'mareeletrica:block/cockpit_gauges',
      'buttons':'mareeletrica:block/cockpit_buttons','hud':'mareeletrica:block/cockpit_hud','particle':'mareeletrica:block/veh_dark'}
# ------------------------------------------------------------------ helicopter (pilot eye ~ (5, 8.7, 3.7), nose toward -z)
heli=load('block/heli_body')
keep=[e for i,e in enumerate(heli['elements']) if i not in (0,1,2,3,4,5,8,9,10,11,16,17)]
I=[]
I.append(box((2,2,-5),(14,2.6,11),'dark'))                                   # floor
I.append(box((2,2.6,-5),(2.4,6,11),'olived'));I.append(box((13.6,2.6,-5),(14,6,11),'olived'))   # walls under the side windows
I.append(box((2,11.7,-5),(14,12.3,-4.4),'olive'))                            # windscreen top frame
I.append(box((2,11.9,-1.2),(14,12.5,11),'olived'))                           # roof (from above the seats back)
I.append(box((2,11.7,-4.4),(2.5,12.3,-1.2),'olive'));I.append(box((13.5,11.7,-4.4),(14,12.3,-1.2),'olive'))   # roof side rails
I.append(box((7.75,11.95,-4.4),(8.25,12.3,-1.2),'olive'))                    # roof centre rail (overhead window frame)
I.append(box((2,4,-5),(2.6,11.7,-4.4),'olive'));I.append(box((13.4,4,-5),(14,11.7,-4.4),'olive'))   # front pillars
I.append(box((7.7,7.6,-5.3),(8.3,11.7,-4.7),'olive'))                        # centre windscreen post
I.append(box((3,10.7,-9.2),(13,11,-8.8),'olive'))                            # nose bubble rim
I.append(box((3,3,-9.2),(3.3,11,-8.8),'olive'));I.append(box((12.7,3,-9.2),(13,11,-8.8),'olive'))
I.append(box((3,10.7,-9),(3.3,11,-5),'olive'));I.append(box((12.7,10.7,-9),(13,11,-5),'olive'))
I.append(box((2,5.8,-1.1),(2.5,6.1,11),'olive'));I.append(box((13.5,5.8,-1.1),(14,6.1,11),'olive'))  # window sills
# dashboard with glare shield, tilted toward the pilots
dash=box((2.5,6.4,-4.4),(13.5,7.7,-2.6),'dark',{'up':'black'},{"origin":[8,7,-3.5],"axis":"x","angle":-22.5,"rescale":False});I.append(dash)
I.append(box((2.5,7.5,-4.6),(13.5,7.9,-3.6),'black'))                        # glare shield lip
I.append(box((7,2.6,-3),(9,6.2,4),'dark',{'up':'black'}))                  # centre console
I.append(box((4.8,2.6,1.6),(5.2,5.8,2),'black'));I.append(box((4.65,5.8,1.45),(5.35,6.6,2.15),'black'))   # cyclic stick
I.append(box((10.8,2.6,1.6),(11.2,5.8,2),'black'));I.append(box((10.65,5.8,1.45),(11.35,6.6,2.15),'black'))
I.append(box((2.6,3.6,2),(3,4,6.5),'metal',rot={"origin":[2.8,3.8,6.5],"axis":"x","angle":22.5,"rescale":False}))   # collective lever
for x in (3.5,9.5):
    I.append(box((x,2.6,3.6),(x+3,4.2,6.6),'seat'));I.append(box((x,4.2,6.2),(x+3,10.2,7),'seat'))
I.append(box((2,2.6,11),(14,11.9,11.6),'olived'))                            # rear bulkhead
heli_fp={"textures":dict(heli['textures'],**VTEX),"elements":keep+I}
save('block/heli_fp',heli_fp)
# glowing instruments drawn full bright on the dashboard face
G=[]
for x in (3.2,9.4):
    G.append(box((x,6.55,-2.62),(x+1.6,7.55,-2.56),'mfd',rot={"origin":[8,7,-3.5],"axis":"x","angle":-22.5,"rescale":False}))
    G.append(box((x+1.7,6.55,-2.62),(x+3.2,7.55,-2.56),'gauges',rot={"origin":[8,7,-3.5],"axis":"x","angle":-22.5,"rescale":False}))
G.append(box((7.3,6.25,-1.6),(8.7,6.3,0),'buttons'))
save('block/heli_fp_glow',{"textures":VTEX,"elements":G})
# ------------------------------------------------------------------ fighter jet (eye ~ (8, 10.5, -1.2), nose toward -z, 1 px = .19 block)
J=[]
J.append(box((6.3,8.6,-3.6),(9.7,9.85,-2.7),'dark',{'up':'black'}))          # instrument panel
J.append(box((6.2,9.75,-3.75),(9.8,10.0,-3.3),'black'))                      # glare shield
J.append(box((6.15,9.75,-3.7),(6.4,11.3,-3.45),'dark'));J.append(box((9.6,9.75,-3.7),(9.85,11.3,-3.45),'dark'))   # windscreen bow posts
J.append(box((6.15,11.1,-3.7),(9.85,11.35,-3.45),'dark'))                    # windscreen top
J.append(box((7.88,11.15,-3.45),(8.12,11.4,2.6),'dark'))                     # canopy spine
J.append(box((6.15,9.3,-3.7),(6.45,9.75,2.8),'dark'));J.append(box((9.55,9.3,-3.7),(9.85,9.75,2.8),'dark'))   # canopy rails
J.append(box((7.85,8.0,-1.6),(8.15,9.3,-1.3),'black'));J.append(box((7.75,9.3,-1.7),(8.25,9.7,-1.2),'black'))  # stick
J.append(box((6.3,8.6,-2.7),(6.8,9.4,1.5),'dark',{'up':'buttons'}));J.append(box((9.2,8.6,-2.7),(9.7,9.4,1.5),'dark',{'up':'buttons'}))   # side consoles
save('block/jet_cockpit',{"textures":dict(VTEX,dark='mareeletrica:block/dark'),"elements":J})
JG=[box((6.55,8.75,-2.72),(7.65,9.7,-2.68),'mfd'),box((8.35,8.75,-2.72),(9.45,9.7,-2.68),'mfd'),box((7.7,8.75,-2.72),(8.3,9.7,-2.68),'gauges'),
    box((7.25,9.95,-3.42),(8.75,10.9,-3.38),'hud',{'north':'hud','south':'hud','east':'hud','west':'hud','up':'hud','down':'hud'})]
save('block/jet_cockpit_glow',{"textures":VTEX,"elements":JG})
# ------------------------------------------------------------------ B-2 (eye ~ (8, 8.8, 2.3), 1 px = .3 block): the wing without the closed hump, plus a cockpit
b2=load('block/b2_bomber')
def is_(e,f,t):return [round(x,2) for x in e['from']]==list(f) and [round(x,2) for x in e['to']]==list(t)
outer=[e for e in b2['elements'] if not (is_(e,(5,7.6,-10),(11,9.2,6)) or is_(e,(6,9.2,-6),(10,9.8,2)) or is_(e,(5.6,8,-10.1),(10.4,9,-9.9)))]
B=[box((5,7.6,-10),(11,8.3,-1.4),'skin'),box((6,8.3,-10),(10,8.6,-2.5),'skin')]  # the nose deck ahead of the windscreen
B+= [box((5,7.6,4.5),(11,9.6,6),'skin'),box((6,9.2,1.5),(10,9.8,6),'skin')]        # hump behind the crew
B+= [box((5,7.6,-1.4),(5.6,8.6,4.5),'skin'),box((10.4,7.6,-1.4),(11,8.6,4.5),'skin')]   # cockpit side sills
B+= [box((6.2,7.6,-1.3),(9.8,8.3,-0.4),'dark',{'up':'black'}),box((6.1,8.25,-1.45),(9.9,8.45,-1),'black')]   # panel + glare shield
B+= [box((5.6,8.3,-1.6),(5.9,9.8,-1.3),'dark'),box((10.1,8.3,-1.6),(10.4,9.8,-1.3),'dark'),box((5.6,9.55,-1.6),(10.4,9.8,-1.3),'dark'),box((7.9,8.3,-1.6),(8.1,9.8,-1.3),'dark')]   # windscreen frame (2 panes)
B+= [box((5.6,9.6,-1.3),(5.9,9.8,1.5),'dark'),box((10.1,9.6,-1.3),(10.4,9.8,1.5),'dark')]
B+= [box((7.85,7.6,0.9),(8.15,8.4,1.2),'black'),box((7.75,8.4,0.8),(8.25,8.7,1.3),'black')]   # stick
save('block/b2_fp',{"textures":dict(b2['textures'],**{k:v for k,v in VTEX.items() if k not in b2['textures']}),"elements":outer+B})
save('block/b2_fp_glow',{"textures":VTEX,"elements":[box((6.4,7.7,-0.42),(7.6,8.25,-0.38),'mfd'),box((8.4,7.7,-0.42),(9.6,8.25,-0.38),'mfd'),box((7.65,7.7,-0.42),(8.35,8.25,-0.38),'gauges')]})
print('cockpits ok')

# ------------------------------------------------------------------ sapo pequenino (tiny frog)
import random
COL={'green':((78,154,46),(52,110,30),(196,214,120)),'gold':((236,182,40),(40,30,24),(250,226,140)),'blue':((40,110,220),(16,30,70),(150,190,240))}
for v,(main,spot,belly) in COL.items():
    r=random.Random(v)
    im=Image.new('RGBA',(16,16),main+(255,));d=ImageDraw.Draw(im)
    for _ in range(9):
        x,y=r.randrange(16),r.randrange(16);d.ellipse([x,y,x+r.choice((1,2)),y+r.choice((1,2))],fill=spot+(255,))
    for _ in range(14):
        x,y=r.randrange(16),r.randrange(16);c=tuple(min(255,max(0,c+r.randint(-14,14))) for c in main);im.putpixel((x,y),c+(255,))
    im.save(os.path.join(T,'block','frog_'+v+'.png'))
    im=Image.new('RGBA',(16,16),belly+(255,))
    for _ in range(10):
        x,y=r.randrange(16),r.randrange(16);im.putpixel((x,y),tuple(max(0,c-18) for c in belly)+(255,))
    im.save(os.path.join(T,'block','frog_belly_'+v+'.png'))
def eye(im,d):
    d.rectangle([0,0,15,15],fill=(226,190,60,255));d.ellipse([3,3,12,12],fill=(20,16,12,255));d.rectangle([4,7,11,8],fill=(10,8,6,255));d.point([(5,5),(6,5)],fill=(255,255,240,255))
png('frog_eye',eye)
png('frog_mouth',lambda im,d:d.rectangle([0,0,15,15],fill=(40,30,26,255)))
png('frog_throat',lambda im,d:(d.rectangle([0,0,15,15],fill=(244,234,190,255)),d.ellipse([4,4,11,11],fill=(250,244,214,255))))
png('frog_tongue',lambda im,d:(d.rectangle([0,0,15,15],fill=(232,96,120,255)),d.rectangle([0,0,15,3],fill=(250,140,160,255))))
for v in COL:
    tx={'skin':'mareeletrica:block/frog_'+v,'belly':'mareeletrica:block/frog_belly_'+v,'eye':'mareeletrica:block/frog_eye','mouth':'mareeletrica:block/frog_mouth','particle':'mareeletrica:block/frog_'+v}
    E=[]
    E.append(box((5.5,0.7,5),(10.5,3.4,11),'skin',{'down':'belly'}))                   # body
    E.append(box((6,3.4,6),(10,3.9,10),'skin'))                                       # back hump
    E.append(box((5.2,1.3,2),(10.8,3.9,5.5),'skin',{'down':'belly'}))                 # head
    E.append(box((5.6,1.8,1.6),(10.4,3.5,2),'skin'))                                  # snout
    E.append(box((5.4,2.05,1.55),(10.6,2.25,2.05),'mouth'))                           # mouth line
    E.append(box((5.0,3.5,2.5),(6.8,5.1,4.3),'eye',{'up':'skin','down':'skin','east':'skin'}))   # eyes on top of the head
    E.append(box((9.2,3.5,2.5),(11.0,5.1,4.3),'eye',{'up':'skin','down':'skin','west':'skin'}))
    E.append(box((4.9,0,3.6),(5.9,1.6,4.4),'skin'));E.append(box((10.1,0,3.6),(11.1,1.6,4.4),'skin'))   # front legs
    E.append(box((4.4,0,2.8),(6.0,0.3,4.0),'skin'));E.append(box((10.0,0,2.8),(11.6,0.3,4.0),'skin'))   # front feet
    save('block/frog_body_'+v,{"textures":tx,"elements":E})
    for side,(x0,x1,fx0,fx1) in {'l':(10.2,11.8,10.4,12.4),'r':(4.2,5.8,3.6,5.6)}.items():
        L=[box((x0,0.4,8.5),(x1,2.2,11.8),'skin'),box((fx0,0,6.2),(fx1,0.35,9.2),'skin')]
        save('block/frog_leg_'+side+'_'+v,{"textures":tx,"elements":L})
save('block/frog_throat',{"textures":{'t':'mareeletrica:block/frog_throat','particle':'mareeletrica:block/frog_throat'},"elements":[box((6.8,-1.0,6.8),(9.2,1.0,9.2),'t')]})
save('block/frog_tongue',{"textures":{'t':'mareeletrica:block/frog_tongue','particle':'mareeletrica:block/frog_tongue'},"elements":[box((7.6,-0.2,7),(8.4,0.2,8),'t')]})
# items: spawn egg + the pocket frog (3D)
save('item/tiny_frog_spawn_egg',{"parent":"minecraft:item/template_spawn_egg"})
fr=load('block/frog_body_green');legs=load('block/frog_leg_l_green')['elements']+load('block/frog_leg_r_green')['elements']
save('item/tiny_frog',{"textures":fr['textures'],"elements":fr['elements']+legs,"display":{"gui":{"rotation":[25,145,0],"translation":[0,1.5,0],"scale":[1.5,1.5,1.5]},
 "ground":{"translation":[0,2,0],"scale":[.6,.6,.6]},"fixed":{"scale":[1.2,1.2,1.2]},"thirdperson_righthand":{"rotation":[0,180,0],"translation":[0,2,1],"scale":[.6,.6,.6]},
 "firstperson_righthand":{"rotation":[0,160,0],"translation":[0,3,0],"scale":[.8,.8,.8]},"firstperson_lefthand":{"rotation":[0,160,0],"translation":[0,3,0],"scale":[.8,.8,.8]}}})
# natural spawns: swamps, mangroves, rivers and jungles
D=os.path.join(A,'..','..','data','mareeletrica','neoforge','biome_modifier');os.makedirs(D,exist_ok=True)
json.dump({"type":"neoforge:add_spawns","biomes":["minecraft:swamp","minecraft:mangrove_swamp","minecraft:river","minecraft:jungle","minecraft:sparse_jungle","minecraft:bamboo_jungle","minecraft:lush_caves"],
 "spawners":{"type":"mareeletrica:tiny_frog","weight":12,"minCount":2,"maxCount":4}},open(os.path.join(D,'tiny_frog.json'),'w'),indent=1)
# names
for lang,n in {'pt_br':{'entity.mareeletrica.tiny_frog':'Sapo pequenino','item.mareeletrica.tiny_frog_spawn_egg':'Ovo gerador de sapo pequenino','item.mareeletrica.tiny_frog':'Sapo pequenino',
                        'key.mareeletrica.sport_5':'Esporte: dominar/conduzir a bola (futebol)'},
               'en_us':{'entity.mareeletrica.tiny_frog':'Tiny Frog','item.mareeletrica.tiny_frog_spawn_egg':'Tiny Frog Spawn Egg','item.mareeletrica.tiny_frog':'Tiny Frog',
                        'key.mareeletrica.sport_5':'Sport: control/carry the ball (football)'}}.items():
    p=os.path.join(A,'lang',lang+'.json');d=json.load(open(p,encoding='utf-8'));d.update(n);json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('frog ok')
