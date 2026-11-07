"""0.53.0 resources: own lamp textures (replace nether/vanilla glow blocks in our models), round radar screen, LED lamps, lift posts, 3D items, filler block."""
import json,os,glob,math,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
TB=os.path.join(A,'textures/block');random.seed(5)
def lamp_tex(name,rgb,on=True):
    im=Image.new('RGBA',(16,16));r,g,b=rgb
    for y in range(16):
        for x in range(16):
            d=math.hypot(x-7.5,y-7.5)/10.6
            k=(1.15-d*.45) if on else (.32-d*.08)
            c=tuple(max(0,min(255,int(v*k+(70 if on and d<.35 else 0)))) for v in (r,g,b))
            if x in (0,15) or y in (0,15):c=tuple(max(0,int(v*(.55 if on else .2))) for v in (r,g,b))
            im.putpixel((x,y),c+(255,))
    im.save(os.path.join(TB,name+'.png'))
COL={"white":(255,244,232),"red":(255,48,48),"orange":(255,138,32),"yellow":(255,224,48),"lime":(128,255,64),"green":(48,192,64),"cyan":(48,232,240),"blue":(48,112,255),"purple":(160,64,255),"pink":(255,96,192)}
for n,c in COL.items():lamp_tex('lamp_'+n,c);lamp_tex('lamp_'+n+'_off',c,False)
# red glass (siren dome, runway light lens)
im=Image.new('RGBA',(16,16))
for y in range(16):
    for x in range(16):im.putpixel((x,y),(200,40,40,255) if not (2<=x+y-4<=4) else (255,150,150,255))
im.save(os.path.join(TB,'glass_red.png'))
# 1) replace vanilla glow blocks in all our models with our own textures
MAP={"minecraft:block/glowstone":"mareeletrica:block/lamp_yellow","minecraft:block/redstone_block":"mareeletrica:block/lamp_red","minecraft:block/emerald_block":"mareeletrica:block/lamp_green",
 "minecraft:block/shroomlight":"mareeletrica:block/lamp_orange","minecraft:block/redstone_lamp_on":"mareeletrica:block/lamp_red","minecraft:block/red_stained_glass":"mareeletrica:block/glass_red",
 "minecraft:block/green_stained_glass":"mareeletrica:block/radar_screen"}
changed=0
for f in glob.glob(os.path.join(A,'models/**/*.json'),recursive=True):
    j=json.load(open(f));t=j.get('textures')
    if not t:continue
    hit=False
    for k,v in list(t.items()):
        if v in MAP:t[k]=MAP[v];hit=True
    if hit:json.dump(j,open(f,'w'),separators=(',',':'));changed+=1
print('models retextured',changed)
# 2) round radar screen: 64x64, transparent outside the circle
S=64;im=Image.new('RGBA',(S,S),(0,0,0,0));d=ImageDraw.Draw(im)
d.ellipse([0,0,S-1,S-1],fill=(30,34,40,255));d.ellipse([3,3,S-4,S-4],fill=(12,46,22,255))
for r in (27,19,11):d.ellipse([32-r,32-r,32+r,32+r],outline=(46,150,70,255))
d.line([32,4,32,60],fill=(36,120,56,255));d.line([4,32,60,32],fill=(36,120,56,255))
for a in range(0,360,30):x=32+28*math.cos(math.radians(a));y=32+28*math.sin(math.radians(a));d.point((x,y),fill=(120,230,140,255))
im.save(os.path.join(TB,'radar_screen_round.png'))
for name in ('radar','radar_alert'):
    f=os.path.join(A,'models/block',name+'.json');j=json.load(open(f))
    j['textures']['round']='mareeletrica:block/radar_screen_round'
    els=[e for e in j['elements'] if not any(fc.get('texture') in ('#screen','#dark') and e['from'][1]>=15.3 and e['to'][1]<=15.8 for fc in e['faces'].values())]
    if not any('#round'==fc.get('texture') for e in els for fc in e['faces'].values()):els.append({"from":[-9.5,15.55,-9.5],"to":[25.5,15.65,25.5],"faces":{"up":{"texture":"#round","uv":[0,0,16,16]},"down":{"texture":"#round","uv":[0,0,16,16]}}})
    j['elements']=els;j['render_type']='minecraft:cutout'
    json.dump(j,open(f,'w'),separators=(',',':'))
# 3) LED lamps
def box(f,t,tex,emissive=False):
    fs={}
    for dn in ["down","up","north","south","west","east"]:
        fc={"texture":"#"+tex,"uv":[0,0,16,16]}
        if emissive:fc["neoforge_data"]={"block_light":15,"sky_light":15}
        fs[dn]=fc
    return {"from":f,"to":t,"faces":fs}
for n in COL:
    T={"glow":f"mareeletrica:block/lamp_{n}","off":f"mareeletrica:block/lamp_{n}_off","frame":"mareeletrica:block/base_steel","particle":f"mareeletrica:block/lamp_{n}"}
    frame=[box([0,0,0],[16,1,16],'frame'),box([0,15,0],[16,16,16],'frame'),box([0,1,0],[1,15,1],'frame'),box([15,1,0],[16,15,1],'frame'),box([0,1,15],[1,15,16],'frame'),box([15,1,15],[16,15,16],'frame')]
    for st,core in (("on",box([.5,1,.5],[15.5,15,15.5],'glow',True)),("off",box([.5,1,.5],[15.5,15,15.5],'off'))):
        json.dump({"parent":"minecraft:block/block","textures":T,"elements":frame+[core]},open(os.path.join(A,f'models/block/led_lamp_{n}_{st}.json'),'w'))
    json.dump({"variants":{"lit=true":{"model":f"mareeletrica:block/led_lamp_{n}_on"},"lit=false":{"model":f"mareeletrica:block/led_lamp_{n}_off"}}},open(os.path.join(A,f'blockstates/led_lamp_{n}.json'),'w'))
    json.dump({"parent":f"mareeletrica:block/led_lamp_{n}_on"},open(os.path.join(A,f'models/item/led_lamp_{n}.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"mareeletrica:led_lamp_{n}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,f'loot_table/blocks/led_lamp_{n}.json'),'w'))
    dye="minecraft:"+{"white":"white","red":"red","orange":"orange","yellow":"yellow","lime":"lime","green":"green","cyan":"cyan","blue":"blue","purple":"purple","pink":"pink"}[n]+"_dye"
    json.dump({"type":"minecraft:crafting_shapeless","category":"building","ingredients":[{"item":"minecraft:glass"},{"item":"minecraft:glowstone_dust"},{"item":dye},{"item":"minecraft:iron_nugget"}],"result":{"id":f"mareeletrica:led_lamp_{n}","count":4}},open(os.path.join(D,f'recipe/led_lamp_{n}.json'),'w'))
# 4) filler + empty model
json.dump({"elements":[],"textures":{"particle":"mareeletrica:block/base_steel"}},open(os.path.join(A,'models/block/empty.json'),'w'))
json.dump({"variants":{"":{"model":"mareeletrica:block/empty"}}},open(os.path.join(A,'blockstates/multiblock_filler.json'),'w'))
# 5) lift post (corner pole with a lamp) and models with own texture names
T2={"steel":"mareeletrica:block/base_steel","dark":"mareeletrica:block/base_dark","hazard":"mareeletrica:block/veh_hazard","lamp":"mareeletrica:block/lamp_yellow","glass":"mareeletrica:block/veh_glass",
 "red":"mareeletrica:block/lamp_red","green":"mareeletrica:block/lamp_green","white":"minecraft:block/white_concrete","metal":"mareeletrica:block/veh_metal","wood":"minecraft:block/spruce_planks",
 "black":"minecraft:block/black_concrete","olive":"mareeletrica:block/veh_olive","yellow":"minecraft:block/yellow_concrete","blue":"mareeletrica:block/lamp_blue","paper":"minecraft:block/white_wool","particle":"mareeletrica:block/base_steel"}
def b2(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def wm(kind,name,el,display=None):
    m={"textures":T2,"elements":el}
    if display:m["display"]=display;m["gui_light"]="side"
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
wm('block','lift_post',[b2([7,0,7],[9,30,9],'hazard'),b2([6.5,30,6.5],[9.5,32,9.5],'lamp')])
# 6) 3D item models
H={"gui":{"rotation":[25,-30,0],"scale":[1,1,1]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"fixed":{"scale":[1,1,1]},
 "thirdperson_righthand":{"translation":[0,2.5,1.5],"scale":[.55,.55,.55]},"thirdperson_lefthand":{"translation":[0,2.5,1.5],"scale":[.55,.55,.55]},
 "firstperson_righthand":{"rotation":[0,-25,0],"translation":[1,3.5,-1],"scale":[.6,.6,.6]},"firstperson_lefthand":{"rotation":[0,-25,0],"translation":[1,3.5,-1],"scale":[.6,.6,.6]}}
wm('item','fuel_can',[b2([3,0,5],[13,13,11],'red'),b2([4,13,6],[10,14,10],'red'),b2([9,13,6.5],[11.5,16,8.5],'dark'),b2([3.5,10,4.9],[12.5,11,5],'dark'),b2([4,14,7.5],[8,15.5,8.5],'dark')],H)
wm('item','armor_kit',[b2([2,0,3],[14,4,13],'metal'),b2([3,4,4],[13,9,12],'steel'),b2([5,9,6],[11,11,10],'metal'),b2([6,6,3.8],[10,8,4],'yellow')],H)
wm('item','engine_kit',[b2([3,2,4],[13,10,12],'dark'),b2([4,10,5],[6,13,7],'metal'),b2([7,10,5],[9,13,7],'metal'),b2([10,10,5],[12,13,7],'metal'),b2([13,4,6],[15,8,10],'red'),b2([1,4,6],[3,8,10],'steel')]+[b2([3+i*2,2,3.8],[4+i*2,10,4],'black') for i in range(5)],H)
wm('item','ammo_kit',[b2([2,0,4],[14,7,12],'olive'),b2([2,7,4],[14,8,12],'dark'),b2([6,3,3.8],[10,5,4],'yellow')]+[b2([3.5+i*2.4,8,6.5],[4.9+i*2.4,13,8],'yellow') for i in range(4)],H)
wm('item','garage_remote',[b2([5,0,7],[11,12,9],'dark'),b2([6,8,6.9],[10,11,7],'blue'),b2([6.5,3,6.8],[9.5,6,7],'red'),b2([10,12,7.7],[10.6,16,8.3],'metal')],H)
wm('item','route_tag',[b2([2,4,7.6],[14,12,8.4],'yellow',([8,8,8],'z',-22.5)),b2([3,7,7.4],[5,9,8.6],'black',([8,8,8],'z',-22.5)),b2([6,7.5,7.5],[13,8.5,8.5],'paper',([8,8,8],'z',-22.5))],H)
wm('item','access_card',[b2([2,5,7.7],[14,13,8.3],'blue'),b2([3,9,7.6],[6.5,11.5,8.4],'yellow'),b2([7.5,10,7.6],[13,10.6,8.4],'paper'),b2([7.5,8,7.6],[12,8.6,8.4],'paper')],H)
cd=json.load(open(os.path.join(A,'models/block/courier_drone.json')))
wm('item','delivery_drone',[b2(e['from'],e['to'],'steel') for e in []]+[{**e} for e in cd['elements']],dict(H,gui={"rotation":[25,-30,0],"translation":[0,2,0],"scale":[1.3,1.3,1.3]}))
j=json.load(open(os.path.join(A,'models/item/delivery_drone.json')));j['textures']=cd['textures'];json.dump(j,open(os.path.join(A,'models/item/delivery_drone.json'),'w'))
wm('item','portable_base',[b2([1,0,1],[15,1,15],'dark'),b2([1,1,1],[15,6,2],'white'),b2([1,1,14],[15,6,15],'white'),b2([1,1,2],[2,6,14],'white'),b2([14,1,2],[15,6,14],'white'),b2([1,6,1],[15,7,15],'steel'),
 b2([6,1,.9],[10,5,1],'metal'),b2([1,1,.95],[15,1.6,1.05],'yellow'),b2([3,7,3],[7,7.6,9],'blue'),b2([9,7,3],[13,7.6,9],'blue'),b2([11,7,11],[13,9,13],'red')],dict(H,gui={"rotation":[30,225,0],"scale":[1,1,1]}))
bd=[b2([4,0,7],[12,16,9],'metal'),b2([4.5,12.5,6.9],[11.5,13,7],'yellow'),b2([5,7,6.9],[11,7.5,7],'black'),b2([10,7,6.6],[11,9,7],'steel'),b2([6,10,6.9],[10,12,7],'glass')]
wm('item','bunker_door',bd,dict(H,gui={"rotation":[25,-30,0],"scale":[.9,.9,.9]}))
print('053 ok')
# 7) command panel: the three monitors were off-centre and overlapping; centre them on the 3-wide desk
f=os.path.join(A,'models/block/command_panel.json');j=json.load(open(f))
mon=[e for e in j['elements'] if e['from'][2]>=10.8 and e['from'][1]>=13.5]
cs=sorted(set(round((e['from'][0]+e['to'][0])/2) for e in mon))
if cs and max(cs)<20:
    shift={-9:3,0:8,9:13}
    for e in mon:
        c=round((e['from'][0]+e['to'][0])/2);d=shift.get(c)
        if d is None:continue
        e['from'][0]+=d;e['to'][0]+=d
    json.dump(j,open(f,'w'),separators=(',',':'))
    print('panel monitors centred')
# 8) 0.53.2: bigger tennis racket in the hand
f=os.path.join(A,'models/item/tennis_racket.json');j=json.load(open(f))
j['display']={"firstperson_righthand":{"rotation":[0,-90,25],"translation":[1.13,4.2,1.13],"scale":[.9,.9,.9]},"firstperson_lefthand":{"rotation":[0,90,-25],"translation":[1.13,4.2,1.13],"scale":[.9,.9,.9]},
 "thirdperson_righthand":{"rotation":[0,-90,55],"translation":[0,6,.5],"scale":[1.3,1.3,1.3]},"thirdperson_lefthand":{"rotation":[0,90,-55],"translation":[0,6,.5],"scale":[1.3,1.3,1.3]},"ground":{"scale":[.8,.8,.8]}}
json.dump(j,open(f,'w'),indent=1)
# 9) 0.53.2: soccer ball with black patches on every side (looks like a real ball when it rolls)
f=os.path.join(A,'models/item/soccer_ball.json');j=json.load(open(f))
els=[e for e in j['elements'] if list(e['faces'].values())[0]['texture']!='#black']
def blk(f,t):return {"from":f,"to":t,"faces":{d:{"texture":"#black","uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
pat=[blk([6.5,6.5,3.1],[9.5,9.5,3.4]),blk([6.5,6.5,12.6],[9.5,9.5,12.9]),blk([3.1,6.5,6.5],[3.4,9.5,9.5]),blk([12.6,6.5,6.5],[12.9,9.5,9.5]),
 blk([6.5,12.6,6.5],[9.5,12.9,9.5]),blk([6.5,3.1,6.5],[9.5,3.4,9.5]),
 blk([4.4,10.2,4.4],[5.6,11.4,5.6]),blk([10.4,10.2,10.4],[11.6,11.4,11.6]),blk([4.4,4.6,10.4],[5.6,5.8,11.6]),blk([10.4,4.6,4.4],[11.6,5.8,5.6])]
j['elements']=els+pat;json.dump(j,open(f,'w'),indent=1)
# 10) 0.53.2: round balls (stacked octagon slices instead of a cube)
def cube(f,t,tex):return {"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
def sphere(r,tex_of_slice):
    out=[];n=7;h=2*r/n
    for i in range(n):
        y0=8-r+i*h;y1=y0+h;ym=(y0+y1)/2;w=math.sqrt(max(r*r-(ym-8)**2,0))*1.04;k=w*.42;t=tex_of_slice(i,n)
        out+= [cube([8-w,y0,8-k],[8+w,y1,8+k],t),cube([8-k,y0,8-w],[8+k,y1,8+w],t),cube([8-w*.78,y0,8-w*.78],[8+w*.78,y1,8+w*.78],t)]
    return out
R=4.9
def ballmodel(name,slices,extra):
    f=os.path.join(A,'models/item',name+'.json');j=json.load(open(f))
    j['elements']=sphere(R,slices)+extra;json.dump(j,open(f,'w'),indent=1)
s=R*1.04+.02
soc=[cube([6.6,6.6,8-s-.05],[9.4,9.4,8-s+.25],'black'),cube([6.6,6.6,8+s-.25],[9.4,9.4,8+s+.05],'black'),cube([8-s-.05,6.6,6.6],[8-s+.25,9.4,9.4],'black'),cube([8+s-.25,6.6,6.6],[8+s+.05,9.4,9.4],'black'),
     cube([6.8,8+R-.05,6.8],[9.2,8+R+.25,9.2],'black'),cube([6.8,8-R-.25,6.8],[9.2,8-R+.05,9.2],'black')]
d=s*.78-.1
for sx in(-1,1):
    for sz in(-1,1):soc.append(cube([8+sx*d-.7,9.6,8+sz*d-.7],[8+sx*d+.7,10.8,8+sz*d+.7],'black'));soc.append(cube([8+sx*d-.7,5.2,8-sz*d-.7],[8+sx*d+.7,6.4,8-sz*d+.7],'black'))
ballmodel('soccer_ball',lambda i,n:'white',soc)
bas=[cube([7.85,8-R-.1,8-s-.1],[8.15,8+R+.1,8+s+.1],'black'),cube([8-s-.1,8-R-.1,7.85],[8+s+.1,8+R+.1,8.15],'black'),cube([8-s-.1,7.85,8-s*.8],[8+s+.1,8.15,8+s*.8],'black'),cube([8-s*.8,7.85,8-s-.1],[8+s*.8,8.15,8+s+.1],'black')]
ballmodel('basketball',lambda i,n:'orange',bas)
ballmodel('volleyball',lambda i,n:['blue','yellow','white','white','white','yellow','blue'][i],[])
print('balls round')
