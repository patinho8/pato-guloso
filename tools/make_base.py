"""0.51.0 resources: command panel, siren, runway lights, helipad, lift, garage + door, delivery, shield, transformers, kits, paint variants, siren sound."""
import json,os,math,random,subprocess
import numpy as np
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
TB=os.path.join(A,'textures/block');TI=os.path.join(A,'textures/item');random.seed(11)
def noise(name,base,var=6,border=False,lines=None,rivets=False,size=16):
    im=Image.new('RGBA',(size,size))
    for y in range(size):
        for x in range(size):
            c=[max(0,min(255,v+random.randint(-var,var))) for v in base]
            if border and (x in (0,size-1) or y in (0,size-1)):c=[max(0,v-30) for v in base]
            if lines and y%lines==0:c=[max(0,v-26) for v in base]
            if rivets and (x,y) in ((2,2),(size-3,2),(2,size-3),(size-3,size-3)):c=[min(255,v+45) for v in base]
            im.putpixel((x,y),tuple(c)+(255,))
    im.save(os.path.join(TB,name+'.png'))
noise('garage_slats',(150,156,164),4,lines=4)
noise('base_steel',(84,92,102),5,border=True,rivets=True)
noise('base_dark',(34,38,44),4,border=True)
noise('lift_grate',(70,74,80),4,lines=3)
# monitor screen: dark blue with faint scanlines
im=Image.new('RGBA',(16,16))
for y in range(16):
    for x in range(16):im.putpixel((x,y),(10,26,40,255) if y%2 else (14,34,52,255))
im.save(os.path.join(TB,'monitor_screen.png'))
# helipad: 48x48 dark concrete, yellow ring, white H
S=48;im=Image.new('RGBA',(S,S),(70,72,76,255));d=ImageDraw.Draw(im)
for i in range(400):x,y=random.randrange(S),random.randrange(S);d.point((x,y),fill=(60+random.randint(0,25),)*3+(255,))
d.ellipse([4,4,S-5,S-5],outline=(240,200,40,255),width=3)
d.rectangle([15,13,19,34],fill=(240,240,240,255));d.rectangle([28,13,32,34],fill=(240,240,240,255));d.rectangle([19,21,28,25],fill=(240,240,240,255))
for x in range(0,S,6):d.rectangle([x,0,x+2,1],fill=(240,200,40,255));d.rectangle([x,S-2,x+2,S-1],fill=(240,200,40,255))
im.save(os.path.join(TB,'helipad.png'))
T={"steel":"mareeletrica:block/base_steel","dark":"mareeletrica:block/base_dark","screen":"mareeletrica:block/monitor_screen","slats":"mareeletrica:block/garage_slats",
 "grate":"mareeletrica:block/lift_grate","hazard":"mareeletrica:block/veh_hazard","pad":"mareeletrica:block/helipad","olive":"mareeletrica:block/veh_olive","metal":"mareeletrica:block/veh_metal",
 "red":"minecraft:block/redstone_block","redg":"minecraft:block/red_stained_glass","glow":"minecraft:block/glowstone","lamp":"minecraft:block/redstone_lamp_on","lime":"minecraft:block/lime_concrete",
 "blue":"minecraft:block/light_blue_concrete","cyan":"minecraft:block/cyan_concrete","white":"minecraft:block/white_concrete","yellow":"minecraft:block/yellow_concrete","black":"minecraft:block/black_concrete",
 "glass":"mareeletrica:block/veh_glass","wood":"minecraft:block/spruce_planks","particle":"mareeletrica:block/base_steel"}
def box(f,t,tex,rot=None,faces=None):
    fs={d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in (faces or ["down","up","north","south","west","east"])}
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":fs}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def write(kind,name,el,extra=None,ao=True,tex=T):
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    m={"ambientocclusion":ao,"textures":tex,"elements":el}
    if extra:m.update(extra)
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
def hstate(name,model=None,props=""):
    model=model or name
    json.dump({"variants":{(props+"," if props else "")+f"facing={f}":({"model":f"mareeletrica:block/{model}","y":y} if y else {"model":f"mareeletrica:block/{model}"}) for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates',name+'.json'),'w'))
def item(name,parent=None,display=None):
    m={"parent":parent or f"mareeletrica:block/{name}"}
    if display:m["display"]=display
    json.dump(m,open(os.path.join(A,'models/item',name+'.json'),'w'))
def loot(name,drop=True):
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"mareeletrica:{name}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}] if drop else []},open(os.path.join(D,'loot_table/blocks',name+'.json'),'w'))
BIG={"gui":{"rotation":[25,205,0],"translation":[0,-1,0],"scale":[.3,.3,.3]},"ground":{"translation":[0,3,0],"scale":[.15,.15,.15]},"fixed":{"scale":[.3,.3,.3]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.15,.15,.15]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.2,.2,.2]}}
STD={"gui":{"rotation":[30,225,0],"scale":[.625,.625,.625]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.375,.375,.375]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.4,.4,.4]}}
# ---------------- command panel: 3-wide desk (open side -z = toward the player), three monitors ----------------
P=[box([-16,0,2],[32,1,15],'dark'),box([-15,1,3],[31,12,14],'steel'),box([-16,12,1],[32,13.5,15],'dark'),box([-16,13.5,1],[32,14,9],'steel')]
P+=[box([-12,13.6,2],[28,14.1,7],'black')]+[box([-11+i*3.5,14.1,2.5+(i%2)],[-9+i*3.5,14.4,3.5+(i%2)],['lime','red','yellow','blue'][i%4]) for i in range(11)]
for i,(x0,ang) in enumerate(((-15,0),(-6,0),(3,0))):
    mon=[box([x0,14,11],[x0+12,24,12.5],'dark'),box([x0+.5,14.5,10.9],[x0+11.5,23.5,11],'screen'),box([x0+5,13.5,12.5],[x0+7,15,14],'steel')]
    if ang:
        for e in mon:e["rotation"]={"origin":[x0+6,19,12],"axis":"y","angle":ang,"rescale":False}
    P+=mon
P+=[box([-15,1,2.9],[31,11,3],'hazard')]
write('block','command_panel',P);hstate('command_panel',props="active=false");
v=json.load(open(os.path.join(A,'blockstates/command_panel.json')))['variants'];v.update({k.replace("active=false","active=true"):x for k,x in v.items()});json.dump({"variants":v},open(os.path.join(A,'blockstates/command_panel.json'),'w'))
item('command_panel',display=BIG);loot('command_panel')
# ---------------- siren ----------------
SI=[box([4,0,4],[12,2,12],'steel'),box([5,2,5],[11,3,11],'dark'),box([5.5,3,5.5],[10.5,9,10.5],'redg'),box([6.5,9,6.5],[9.5,10,9.5],'dark'),box([7.2,3,7.2],[8.8,8,8.8],'lamp')]
write('block','alarm_siren',SI)
SION=[*SI[:2],box([5.5,3,5.5],[10.5,9,10.5],'red'),*SI[3:]]
write('block','alarm_siren_on',SION)
var={}
rot={"up":{},"down":{"x":180},"north":{"x":90},"south":{"x":90,"y":180},"east":{"x":90,"y":90},"west":{"x":90,"y":270}}
for f,r in rot.items():
    for a in ("false","true"):var[f"active={a},facing={f}"]=dict(model="mareeletrica:block/alarm_siren"+("_on" if a=="true" else ""),**r)
json.dump({"variants":var},open(os.path.join(A,'blockstates/alarm_siren.json'),'w'));item('alarm_siren',display=STD);loot('alarm_siren')
write('block','siren_beam',[box([7.5,5,8],[8.5,7,16],'red'),box([7.5,5,0],[8.5,7,8],'red'),box([6,4.5,15],[10,7.5,16],'red'),box([6,4.5,0],[10,7.5,1],'red')],ao=False)
# ---------------- runway light ----------------
write('block','runway_light',[box([4,0,4],[12,1.5,12],'steel'),box([5,1.5,5],[11,2.5,11],'redg'),box([6,2.5,10],[10,3,11],'dark')])
write('block','runway_glow',[box([4.8,1.4,4.8],[11.2,2.8,11.2],'glow'),box([6.5,2.8,6.5],[9.5,3.4,9.5],'glow')],ao=False)
hstate('runway_light');item('runway_light',display=STD);loot('runway_light')
# ---------------- helipad (3x3) ----------------
HP=[{"from":[-16,0,-16],"to":[32,1.5,32],"faces":{"up":{"texture":"#pad","uv":[0,0,16,16]},"down":{"texture":"#dark","uv":[0,0,16,16]},"north":{"texture":"#dark","uv":[0,0,16,1]},"south":{"texture":"#dark","uv":[0,0,16,1]},"west":{"texture":"#dark","uv":[0,0,16,1]},"east":{"texture":"#dark","uv":[0,0,16,1]}}}]
for x,z in ((-15,-15),(29,-15),(-15,29),(29,29),(7,-15),(7,29),(-15,7),(29,7)):HP.append(box([x,1.5,z],[x+2,2.5,z+2],'lime'))
write('block','helipad',HP);json.dump({"variants":{"":{"model":"mareeletrica:block/helipad"}}},open(os.path.join(A,'blockstates/helipad.json'),'w'));item('helipad',display=BIG);loot('helipad')
# ---------------- vehicle lift, stop, platform ----------------
L=[box([-16,0,-16],[32,4,32],'dark'),box([-16,4,-16],[32,12,-14],'hazard'),box([-16,4,30],[32,12,32],'hazard'),box([-16,4,-14],[-14,12,30],'hazard'),box([30,4,-14],[32,12,30],'hazard'),
   box([-12,4,-12],[28,6,28],'steel'),box([4,6,4],[12,12,12],'metal')]
write('block','vehicle_lift',L);json.dump({"variants":{"":{"model":"mareeletrica:block/vehicle_lift"}}},open(os.path.join(A,'blockstates/vehicle_lift.json'),'w'));item('vehicle_lift',display=BIG);loot('vehicle_lift')
LS=[box([0,0,0],[16,4,16],'steel'),box([1,4,1],[15,4.3,15],'hazard'),box([6,4.3,6],[10,5,10],'lime')]
write('block','lift_stop',LS);json.dump({"variants":{"":{"model":"mareeletrica:block/lift_stop"}}},open(os.path.join(A,'blockstates/lift_stop.json'),'w'));item('lift_stop',display=STD);loot('lift_stop')
write('block','lift_platform',[box([-16,0,-16],[32,7,32],'grate'),box([-16,7,-16],[32,8,-14],'hazard'),box([-16,7,30],[32,8,32],'hazard'),box([-16,7,-14],[-14,8,30],'hazard'),box([30,7,-14],[32,8,30],'hazard')])
# ---------------- garage console ----------------
G=[box([0,0,2],[16,16,16],'steel'),box([1,2,1.9],[15,6,2],'hazard'),box([2,8,1.8],[14,14,2],'screen'),box([3,9,1.7],[8,10,1.8],'lime'),box([3,11,1.7],[11,12,1.8],'cyan'),
   box([0,16,0],[16,17,16],'dark'),box([1,1,0],[15,2,2],'dark')]
write('block','vehicle_garage',G);hstate('vehicle_garage');item('vehicle_garage',display=STD);loot('vehicle_garage')
# ---------------- garage door ----------------
write('block','garage_door_closed',[box([0,0,6.5],[16,16,9.5],'slats'),box([0,0,6.3],[16,1,9.7],'hazard')])
write('block','garage_door_closed_top',[box([0,0,6.5],[16,16,9.5],'slats'),box([0,12,4],[16,16,12],'steel')])
write('block','garage_door_open',[])
write('block','garage_door_open_top',[box([0,10,4],[16,16,12],'steel'),box([0,10,6],[16,12,10],'slats')])
var={}
for f,y in (("north",0),("east",90),("south",180),("west",270)):
    for o in ("false","true"):
        for r in range(4):
            m="garage_door_"+("open" if o=="true" else "closed")+("_top" if r==3 else "")
            vv={"model":"mareeletrica:block/"+m}
            if y:vv["y"]=y
            var[f"facing={f},open={o},row={r}"]=vv
json.dump({"variants":var},open(os.path.join(A,'blockstates/garage_door.json'),'w'))
item('garage_door',"mareeletrica:block/garage_door_closed",STD);loot('garage_door',False)
# ---------------- delivery station, courier ----------------
DS=[box([0,0,0],[16,12,16],'wood'),box([0,12,0],[16,14,16],'steel'),box([1,14,1],[15,14.4,15],'hazard'),box([5,14.4,5],[11,14.6,11],'lime'),box([-.1,3,6],[0,9,10],'dark')]
write('block','delivery_station',DS);json.dump({"variants":{"":{"model":"mareeletrica:block/delivery_station"}}},open(os.path.join(A,'blockstates/delivery_station.json'),'w'));item('delivery_station',display=STD);loot('delivery_station')
CD=[box([5,4,5],[11,7,11],'steel'),box([2,6,7.4],[14,6.8,8.6],'dark',([8,6.4,8],'y',45)),box([2,6,7.4],[14,6.8,8.6],'dark',([8,6.4,8],'y',-45)),box([5.5,1,5.5],[10.5,4,10.5],'wood'),box([7,7,7],[9,8,9],'lime')]
for x,z in ((1.4,1.4),(13.4,1.4),(1.4,13.4),(13.4,13.4)):CD.append(box([x,6.4,z],[x+1.2,9.4,z+1.2],'dark'))
write('block','courier_drone',CD,ao=False);write('block','courier_rotor',[box([5,8,7.6],[11,8.3,8.4],'black')],ao=False)
# ---------------- shield generator (machine) ----------------
SH=lambda glow:[box([0,0,0],[16,4,16],'dark'),box([2,4,2],[14,10,14],'steel'),box([3,10,3],[13,11,13],'dark'),box([6,11,6],[10,15,10],glow),box([1,4,1],[3,10,3],glow),box([13,4,1],[15,10,3],glow),box([1,4,13],[3,10,15],glow),box([13,4,13],[15,10,15],glow)]
write('block','shield_generator',SH('blue'));write('block','shield_generator_active',SH('cyan'))
json.dump({"variants":{f"active={a},facing={f}":(dict(model="mareeletrica:block/shield_generator"+("_active" if a=="true" else ""),**({"y":y} if y else {}))) for a in ("false","true") for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates/shield_generator.json'),'w'))
item('shield_generator',display=STD);loot('shield_generator')
write('block','shield_emitter',[box([7,15,7],[9,22,9],'steel'),box([3,21,7.5],[13,22,8.5],'cyan'),box([7.5,21,3],[8.5,22,13],'cyan'),box([6.5,22,6.5],[9.5,25,9.5],'blue')],ao=False)
# ---------------- transformers ----------------
for name,col in (("low","lime"),("medium","yellow"),("high","red")):
    write('block',f'transformer_{name}_center',[box([3,3,3],[13,13,13],'dark'),box([2.8,5,2.8],[13.2,7,13.2],col),box([2.8,9,2.8],[13.2,11,13.2],col),box([5,13,5],[11,14,11],'steel')])
    bs=json.load(open(os.path.join(A,'blockstates/cable.json')))
    for part in bs["multipart"]:
        if part["apply"]["model"].endswith("cable_center"):part["apply"]["model"]=f"mareeletrica:block/transformer_{name}_center"
    json.dump(bs,open(os.path.join(A,f'blockstates/transformer_{name}.json'),'w'))
    item(f'transformer_{name}',f"mareeletrica:block/transformer_{name}_center",STD);loot(f'transformer_{name}')
# ---------------- paint variants for helicopter and tank ----------------
PAINT={1:{"veh_olive":(196,168,112),"veh_olive_plain":(196,168,112),"veh_olive_dark":(150,124,80),"veh_khaki":(120,96,62)},   # desert
       2:{"veh_olive":(54,84,46),"veh_olive_plain":(70,98,52),"veh_olive_dark":(40,56,32),"veh_khaki":(96,80,50)},          # forest
       3:{"veh_olive":(226,230,234),"veh_olive_plain":(214,220,226),"veh_olive_dark":(160,168,176),"veh_khaki":(120,128,138)}, # snow
       4:{"veh_olive":(38,40,44),"veh_olive_plain":(46,48,52),"veh_olive_dark":(26,28,30),"veh_khaki":(70,70,74)},          # black
       5:{"veh_olive":(126,132,140),"veh_olive_plain":(140,146,152),"veh_olive_dark":(92,98,106),"veh_khaki":(170,170,160)},  # urban grey
       6:{"veh_olive":(44,62,98),"veh_olive_plain":(52,72,110),"veh_olive_dark":(30,44,72),"veh_khaki":(150,150,140)}}       # navy
for p,cols in PAINT.items():
    for tname,base in cols.items():
        src=Image.open(os.path.join(TB,tname+'.png')).convert('RGB');arr=np.array(src).astype(float)
        orig=np.array({"veh_olive":(92,104,56),"veh_olive_plain":(92,104,56),"veh_olive_dark":(62,72,40),"veh_khaki":(150,130,88)}[tname],float)
        out=np.clip(arr-orig+np.array(base,float),0,255).astype(np.uint8)
        Image.fromarray(out).convert('RGBA').save(os.path.join(TB,f'{tname}_p{p}.png'))
    star=Image.open(os.path.join(TB,'veh_star.png')).convert('RGBA');px=star.load()
    for y in range(16):
        for x in range(16):
            if px[x,y][:3]==(92,104,56):px[x,y]=tuple(cols["veh_olive"])+(255,)
    star.save(os.path.join(TB,f'veh_star_p{p}.png'))
    for m in ("heli_body","tank_hull","tank_turret","tank_barrel"):
        j=json.load(open(os.path.join(A,'models/block',m+'.json')))
        for k in ("olive","olivep","olived","khaki","star"):
            if k in j["textures"]:j["textures"][k]=j["textures"][k]+f"_p{p}"
        json.dump(j,open(os.path.join(A,'models/block',f'{m}_p{p}.json'),'w'),separators=(',',':'))
# ---------------- item icons ----------------
def icon(name,draw):
    im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);draw(d);im.save(os.path.join(TI,name+'.png'))
    json.dump({"parent":"minecraft:item/generated","textures":{"layer0":f"mareeletrica:item/{name}"}},open(os.path.join(A,'models/item',name+'.json'),'w'))
icon('fuel_can',lambda d:(d.rectangle([3,4,12,14],fill=(190,40,30)),d.rectangle([3,4,12,14],outline=(110,20,16)),d.rectangle([9,1,11,4],fill=(60,60,64)),d.line([4,6,11,13],fill=(230,90,70)),d.line([4,13,11,6],fill=(230,90,70)),d.rectangle([4,2,8,3],fill=(110,20,16))))
icon('armor_kit',lambda d:(d.polygon([(8,1),(14,4),(13,11),(8,15),(3,11),(2,4)],fill=(120,128,138),outline=(60,64,70)),d.polygon([(8,4),(11,6),(10,10),(8,12),(6,10),(5,6)],fill=(170,176,184))))
icon('engine_kit',lambda d:(d.rectangle([2,5,13,12],fill=(70,74,80),outline=(30,32,36)),d.rectangle([4,2,6,5],fill=(90,94,100)),d.rectangle([9,2,11,5],fill=(90,94,100)),d.rectangle([13,7,15,10],fill=(200,60,40)),[d.line([3+i*2,7,3+i*2,10],fill=(40,42,46)) for i in range(5)]))
icon('ammo_kit',lambda d:(d.rectangle([1,6,14,14],fill=(80,96,52),outline=(40,50,26)),d.rectangle([6,8,9,10],fill=(230,200,60)),[d.rectangle([3+i*3,1,4+i*3,6],fill=(210,170,60)) for i in range(4)]))
icon('garage_remote',lambda d:(d.rectangle([4,3,11,15],fill=(40,42,48),outline=(20,20,24)),d.rectangle([6,5,9,7],fill=(80,200,255)),d.ellipse([6,9,9,12],fill=(230,60,50)),d.line([10,0,10,3],fill=(160,160,170))))
icon('route_tag',lambda d:(d.polygon([(2,4),(10,4),(14,8),(10,12),(2,12)],fill=(240,200,60),outline=(150,110,20)),d.ellipse([3,7,5,9],fill=(60,60,60)),d.line([6,8,11,8],fill=(150,110,20))))
icon('delivery_drone',lambda d:(d.rectangle([5,6,10,9],fill=(84,92,102)),d.line([1,4,14,11],fill=(30,30,34)),d.line([1,11,14,4],fill=(30,30,34)),[d.ellipse([x-2,y-1,x+2,y+1],outline=(20,20,20)) for x,y in ((2,4),(13,4),(2,11),(13,11))],d.rectangle([6,10,9,13],fill=(140,100,60))))
icon('portable_base',lambda d:(d.rectangle([1,6,14,14],fill=(160,164,170),outline=(80,84,90)),d.rectangle([1,6,14,7],fill=(230,190,30)),d.rectangle([6,9,9,14],fill=(70,74,80)),d.rectangle([2,3,6,5],fill=(40,60,120)),d.rectangle([9,3,13,5],fill=(40,60,120))))
# ---------------- siren sound ----------------
sr=22050;t=np.arange(int(sr*2.0))/sr
f=650+450*(0.5-0.5*np.cos(2*np.pi*t/2.0))
ph=2*np.pi*np.cumsum(f)/sr;sig=0.55*np.sign(np.sin(ph))*0.35+0.45*np.sin(ph)
sig*=np.minimum(1,np.minimum(t/0.05,(2.0-t)/0.05))
wav=os.path.join('/tmp','siren.wav');import wave
with wave.open(wav,'w') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(sr);w.writeframes((sig*32000*0.8).astype(np.int16).tobytes())
subprocess.run(['ffmpeg','-y','-loglevel','error','-i',wav,'-c:a','libvorbis','-q:a','4',os.path.join(A,'sounds/alarm_siren.ogg')],check=True)
sj=json.load(open(os.path.join(A,'sounds.json')));sj["alarm_siren"]={"subtitle":"subtitles.mareeletrica.alarm_siren","sounds":[{"name":"mareeletrica:alarm_siren","stream":False,"attenuation_distance":64}]}
json.dump(sj,open(os.path.join(A,'sounds.json'),'w'))
print('base ok')
