"""0.49.0 models: helicopter, tank, radar, hangar, armor charging station, bunker door, sports balls/goal/hoop."""
import json,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
T={"dark":"mareeletrica:block/dark","steel":"mareeletrica:block/steel","black":"minecraft:block/black_concrete","gray":"minecraft:block/gray_concrete",
 "light":"minecraft:block/light_gray_concrete","white":"minecraft:block/white_concrete","olive":"minecraft:block/green_terracotta","khaki":"minecraft:block/brown_terracotta",
 "glass":"minecraft:block/light_blue_stained_glass","red":"minecraft:block/red_concrete","yellow":"minecraft:block/yellow_concrete","lime":"minecraft:block/lime_concrete",
 "orange":"minecraft:block/orange_concrete","blue":"minecraft:block/blue_concrete","cyan":"minecraft:block/cyan_concrete","green":"minecraft:block/green_concrete",
 "brown":"minecraft:block/brown_concrete","teal":"mareeletrica:block/teal","amber":"mareeletrica:block/amber","glow_red":"minecraft:block/redstone_block",
 "glow_green":"minecraft:block/emerald_block","glow_yellow":"minecraft:block/glowstone","screen":"minecraft:block/green_stained_glass","net":"mareeletrica:block/tennis_net",
 "wood":"minecraft:block/oak_planks","particle":"mareeletrica:block/steel"}
def box(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def oct_y(cx,cz,r,y0,y1,tex):a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
def oct_z(cx,cy,r,z0,z1,tex):a=r*.924;return [box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex),box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex,([cx,cy,(z0+z1)/2],'z',45))]
def write(kind,name,el,display=None,extra=None,ao=False):
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    m={"ambientocclusion":ao,"textures":T,"elements":el}
    if display:m["display"]=display
    if extra:m.update(extra)
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
BIG={"gui":{"rotation":[30,225,0],"scale":[.3,.3,.3]},"ground":{"translation":[0,3,0],"scale":[.15,.15,.15]},"fixed":{"scale":[.3,.3,.3]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.15,.15,.15]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.2,.2,.2]}}
# ---------------- helicopter (nose -z, rotor pivot (8,11.2,8)) ----------------
heli=[box([3,1.5,-4],[13,9,10],'olive'),box([4,2.5,-7],[12,8.2,-4],'glass'),box([4.5,8.2,-5.5],[11.5,9,-4],'olive'),box([2.9,4.5,-3],[3,8,6],'glass'),box([13,4.5,-3],[13.1,8,6],'glass'),
 box([5,9,0],[11,10.6,9],'olive'),box([7.3,10.6,7.3],[8.7,11.4,8.7],'dark'),
 box([6.8,5.5,10],[9.2,8,30],'olive'),box([7.6,8,26],[8.4,13,30],'olive'),box([4,6,26],[12,6.6,28],'olive'),box([8.4,9.8,28.3],[9.4,11.2,29.7],'dark'),
 box([2,0,-5],[3,.8,9],'dark'),box([13,0,-5],[14,.8,9],'dark'),
 box([2.2,.8,-2],[2.8,1.6,-1],'dark'),box([2.2,.8,6],[2.8,1.6,7],'dark'),box([13.2,.8,-2],[13.8,1.6,-1],'dark'),box([13.2,.8,6],[13.8,1.6,7],'dark'),
 box([-1,4,0],[3,4.6,4],'olive'),box([13,4,0],[17,4.6,4],'olive'),box([-.5,2.5,-1],[1.5,4,5],'gray'),box([14.5,2.5,-1],[16.5,4,5],'gray'),
 box([7.2,1,-8],[8.8,2.5,-6],'black'),box([7.6,1.4,-10],[8.4,2.1,-8],'black'),   # chin gun
 box([3.1,2,9.9],[12.9,4,10.1],'yellow')]
write('block','heli_body',heli)
write('block','heli_blade',[box([7.3,-.25,8.6],[8.7,.25,32],'dark'),box([7.1,-.4,7],[8.9,.4,9],'black')])
write('block','heli_tail_rotor',[box([7.85,3,7.5],[8.15,13,8.5],'dark'),box([7.85,7.5,3],[8.15,8.5,13],'dark')])
write('item','helicopter',heli,BIG)
# ---------------- tank (nose -z) ----------------
hull=[box([-1,1.5,-6],[17,6.5,22],'olive'),box([0,2,-8.5],[16,5,-6],'olive'),box([1,6.5,16],[15,7.2,21],'khaki'),
 box([-3,0,-7],[1,4.5,23],'dark'),box([15,0,-7],[19,4.5,23],'dark')]
for z in range(-5,22,4):hull+=[box([-3.3,.8,z],[-3,3.2,z+2.4],'gray'),box([19,.8,z],[19.3,3.2,z+2.4],'gray')]
hull+=[box([2,5,22],[5,6.5,23],'black'),box([11,5,22],[14,6.5,23],'black'),box([1,4.5,-6.2],[3,5.5,-5.8],'yellow'),box([13,4.5,-6.2],[15,5.5,-5.8],'yellow')]
write('block','tank_hull',hull)
tur=[box([2,6.5,0],[14,11,15],'olive'),box([3,11,1],[13,11.6,13],'olive'),box([5,7,-1.5],[11,11,0],'khaki'),
 box([3,11.6,9],[7,12.6,13],'dark'),box([9.5,11.6,10],[10.5,14,11],'dark'),box([1.5,8,6],[2,10,12],'khaki'),box([14,8,6],[14.5,10,12],'khaki')]
write('block','tank_turret',tur)
bar=oct_z(8,9.5,.9,-14,0,'olive')+oct_z(8,9.5,1.25,-16,-13.5,'dark')+[box([7,8.5,-1],[9,10.5,1],'khaki')]
write('block','tank_barrel',bar)
write('item','battle_tank',hull+tur+bar,BIG)
# ---------------- radar ----------------
radar=[box([1,0,1],[15,10,15],'dark'),box([1.5,10,1.5],[14.5,10.6,14.5],'steel'),box([2,10.6,2],[14,10.8,14],'screen'),
 box([2.5,4,.9],[13.5,8,1],'black'),box([3,6,.8],[5,7.5,.9],'glow_green'),box([6,6,.8],[8,7.5,.9],'glow_yellow'),
 box([13,10.6,13],[15,24,15],'steel')]
write('block','radar',radar,extra={"ambientocclusion":True})
write('block','radar_alert',[dict(e) for e in radar]+[box([13.2,24,13.2],[14.8,25.2,14.8],'glow_red')])
write('block','radar_dish',[box([7.4,0,7.4],[8.6,2,8.6],'dark'),box([2,2,7],[14,6,8],'light',([8,4,7.5],'x',-22.5)),box([7.5,3.5,8],[8.5,4.5,11],'dark')])
write('block','radar_blip_red',[box([7.5,7.5,7.5],[8.5,8.5,8.5],'glow_red')]);write('block','radar_blip_green',[box([7.5,7.5,7.5],[8.5,8.5,8.5],'glow_green')]);write('block','radar_blip_yellow',[box([7.5,7.5,7.5],[8.5,8.5,8.5],'glow_yellow')])
write('block','radar_sweep',[box([7.9,7.9,2],[8.1,8.1,8],'glow_green')])
json.dump({"variants":{f"alert={a},facing={f}":({"model":"mareeletrica:block/radar"+("_alert" if a=="true" else ""),"y":y} if y else {"model":"mareeletrica:block/radar"+("_alert" if a=="true" else "")}) for a in ("false","true") for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates/radar.json'),'w'))
json.dump({"parent":"mareeletrica:block/radar","display":{"gui":{"rotation":[30,225,0],"translation":[0,-2,0],"scale":[.5,.5,.5]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.3,.3,.3]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.35,.35,.35]}}},open(os.path.join(A,'models/item/radar.json'),'w'))
# ---------------- machine kinds: armor charging station and vehicle hangar ----------------
ITEM={"gui":{"rotation":[25,225,0],"translation":[0,-1,0],"scale":[.5,.5,.5]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.3,.3,.3]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.35,.35,.35]}}
M={}
M['armor_charging_station']=lambda glow:[box([0,0,0],[16,1.5,16],'dark'),box([2,1.5,2],[14,2,14],glow),box([0,1.5,13],[3,28,16],'steel'),box([13,1.5,13],[16,28,16],'steel'),box([0,28,13],[16,31,16],'steel'),
    box([.5,6,12.6],[2.5,22,13],glow),box([13.5,6,12.6],[15.5,22,13],glow),box([5,24,12.6],[11,27,13],'black'),box([5.5,24.5,12.5],[10.5,26.5,12.6],glow)]
M['vehicle_hangar']=lambda glow:[box([-16,0,-16],[32,1,32],'gray')]+[box([x,1,-16],[x+4,1.1,32],'yellow') for x in (-16,28)]+[box([-16,1,y],[32,1.1,y+2],'black') for y in (-16,30)]+[
    box([26,1,26],[31,6,31],'dark'),box([27,6,27],[30,24,30],'steel'),box([10,22,27.5],[30,24,29.5],'steel'),box([11,14,28],[12,22,29],'black'),box([10,12,27.5],[13,14,29.5],'yellow'),
    box([26.5,3,25.9],[30.5,5,26],glow)]
for name,fn in M.items():
    write('block',name,fn('amber'),ao=True);write('block',name+'_active',fn('cyan' if name.startswith('armor') else 'lime'),ao=True)
    json.dump({"variants":{f"active={a},facing={f}":(dict(model=f"mareeletrica:block/{name}{'_active' if a=='true' else ''}",**({"y":y} if y else {}))) for a in ("false","true") for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates',name+'.json'),'w'))
    json.dump({"parent":f"mareeletrica:block/{name}","display":ITEM},open(os.path.join(A,'models/item',name+'.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"mareeletrica:{name}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks',name+'.json'),'w'))
# ---------------- bunker door (vanilla door templates) ----------------
for part in ("bottom_left","bottom_left_open","bottom_right","bottom_right_open","top_left","top_left_open","top_right","top_right_open"):
    json.dump({"parent":f"minecraft:block/door_{part}","textures":{"bottom":"mareeletrica:block/bunker_door_bottom","top":"mareeletrica:block/bunker_door_top"}},open(os.path.join(A,f'models/block/bunker_door_{part}.json'),'w'))
var={}
for f,base in (("east",0),("south",90),("west",180),("north",270)):
    for half,hp in (("lower","bottom"),("upper","top")):
        for hinge in ("left","right"):
            for op in ("false","true"):
                y=base
                if op=="true":y=(base+90)%360 if hinge=="left" else (base+270)%360
                m={"model":f"mareeletrica:block/bunker_door_{hp}_{hinge}"+("_open" if op=="true" else "")}
                if y:m["y"]=y
                var[f"facing={f},half={half},hinge={hinge},open={op}"]=m
json.dump({"variants":var},open(os.path.join(A,'blockstates/bunker_door.json'),'w'))
json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/bunker_door"}},open(os.path.join(A,'models/item/bunker_door.json'),'w'))
json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:bunker_door"}],"conditions":[{"condition":"minecraft:block_state_property","block":"mareeletrica:bunker_door","properties":{"half":"lower"}},{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks/bunker_door.json'),'w'))
json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/access_card"}},open(os.path.join(A,'models/item/access_card.json'),'w'))
# ---------------- sports ----------------
SPORT_HAND={"gui":{"rotation":[25,-30,0],"scale":[1.3,1.3,1.3]},"ground":{"translation":[0,0,0],"scale":[.8,.8,.8]},"fixed":{"scale":[1.2,1.2,1.2]},
 "thirdperson_righthand":{"translation":[0,1,1.5],"scale":[.7,.7,.7]},"firstperson_righthand":{"translation":[1,3,-1],"scale":[.8,.8,.8]},"thirdperson_lefthand":{"translation":[0,1,1.5],"scale":[.7,.7,.7]},"firstperson_lefthand":{"translation":[1,3,-1],"scale":[.8,.8,.8]}}
def ball(main,band,patch=None):
    el=[box([4,4,4],[12,12,12],main),box([3.3,4.7,4.7],[12.7,11.3,11.3],main),box([4.7,3.3,4.7],[11.3,12.7,11.3],main),box([4.7,4.7,3.3],[11.3,11.3,12.7],main)]
    el+=[box([3.2,7.6,4.5],[12.8,8.4,11.5],band),box([7.6,3.2,4.5],[8.4,12.8,11.5],band)]
    if patch:el+=[box([6.5,6.5,3.2],[9.5,9.5,3.4],patch),box([6.5,6.5,12.6],[9.5,9.5,12.8],patch),box([3.2,10,10],[3.4,12,12],patch),box([12.6,4,4],[12.8,6,6],patch)]
    return el
write('item','soccer_ball',ball('white','white','black'),SPORT_HAND)
write('item','basketball',ball('orange','black'),SPORT_HAND)
write('item','volleyball',[*ball('white','yellow')[:4],box([3.2,7.6,4.5],[12.8,8.4,11.5],'yellow'),box([4.5,3.2,7.6],[11.5,12.8,8.4],'blue')],SPORT_HAND)
goal=[box([-16,0,1],[-14.5,24,2.5],'white'),box([30.5,0,1],[32,24,2.5],'white'),box([-16,22.5,1],[32,24,2.5],'white'),
 box([-16,0,15],[-15,20,16],'white'),box([31,0,15],[32,20,16],'white'),box([-16,19,2],[32,20,16],'white'),
 {"from":[-15,0,15.4],"to":[31,19,15.6],"faces":{"north":{"texture":"#net","uv":[0,0,16,16]},"south":{"texture":"#net","uv":[0,0,16,16]}}},
 {"from":[-14.5,0,2.5],"to":[-14.4,22.5,15.5],"faces":{"east":{"texture":"#net","uv":[0,0,16,16]},"west":{"texture":"#net","uv":[0,0,16,16]}}},
 {"from":[30.4,0,2.5],"to":[30.5,22.5,15.5],"faces":{"east":{"texture":"#net","uv":[0,0,16,16]},"west":{"texture":"#net","uv":[0,0,16,16]}}},
 {"from":[-14.5,22.4,2.5],"to":[30.5,22.5,15.5],"faces":{"up":{"texture":"#net","uv":[0,0,16,16]},"down":{"texture":"#net","uv":[0,0,16,16]}}}]
write('block','soccer_goal',goal,extra={"render_type":"minecraft:cutout"})
hoop=[box([6.5,0,14],[9.5,30,16],'steel'),box([4,0,12],[12,1.5,16],'dark'),box([-4,22,12.5],[20,32,13.5],'white'),box([3,24,12.4],[13,30,12.5],'red'),box([4,25,12.3],[12,29,12.4],'white'),
 box([6.5,25.5,11],[9.5,26.5,12.5],'orange'),box([1.5,26,1.5],[14.5,26.6,2.5],'orange'),box([1.5,26,11],[14.5,26.6,12],'orange'),box([1.5,26,1.5],[2.5,26.6,12],'orange'),box([13.5,26,1.5],[14.5,26.6,12],'orange'),
 {"from":[2,21,2],"to":[14,26,2.5],"faces":{"north":{"texture":"#net","uv":[0,0,16,16]},"south":{"texture":"#net","uv":[0,0,16,16]}}},
 {"from":[2,21,11],"to":[14,26,11.5],"faces":{"north":{"texture":"#net","uv":[0,0,16,16]},"south":{"texture":"#net","uv":[0,0,16,16]}}},
 {"from":[2,21,2],"to":[2.5,26,11.5],"faces":{"east":{"texture":"#net","uv":[0,0,16,16]},"west":{"texture":"#net","uv":[0,0,16,16]}}},
 {"from":[13.5,21,2],"to":[14,26,11.5],"faces":{"east":{"texture":"#net","uv":[0,0,16,16]},"west":{"texture":"#net","uv":[0,0,16,16]}}}]
write('block','basketball_hoop',hoop,extra={"render_type":"minecraft:cutout"})
for n in ('soccer_goal','basketball_hoop'):
    json.dump({"variants":{f"facing={f}":({"model":f"mareeletrica:block/{n}","y":y} if y else {"model":f"mareeletrica:block/{n}"}) for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,f'blockstates/{n}.json'),'w'))
    json.dump({"parent":f"mareeletrica:block/{n}","display":{"gui":{"rotation":[30,225,0],"translation":[0,-2,0],"scale":[.3,.3,.3]},"ground":{"translation":[0,3,0],"scale":[.15,.15,.15]},"fixed":{"scale":[.3,.3,.3]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.15,.15,.15]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.2,.2,.2]}}},open(os.path.join(A,f'models/item/{n}.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"mareeletrica:{n}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,f'loot_table/blocks/{n}.json'),'w'))
json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:radar"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks/radar.json'),'w'))
# ---------------- textures ----------------
from PIL import Image
def tex(path,fn,S=16):
    im=Image.new('RGBA',(S,S));
    for y in range(S):
        for x in range(S):im.putpixel((x,y),fn(x,y))
    im.save(path)
STEEL=lambda x,y:(88,94,102,255) if (x%8 and y%8) else (60,64,70,255)
def door_bottom(x,y):
    if y>=12:return ((230,190,30,255) if ((x+y)//2)%2 else (30,30,30,255))
    if x in (0,15) or y==0:return (55,58,64,255)
    if (x,y) in ((3,3),(12,3),(3,9),(12,9)):return (150,155,160,255)
    return STEEL(x,y)
def door_top(x,y):
    if x in (0,15) or y==15:return (55,58,64,255)
    if 5<=x<=10 and 4<=y<=8:return (40,40,46,255) if not(6<=x<=9 and 5<=y<=7) else (120,200,230,255)
    if (x,y) in ((3,3),(12,3),(3,12),(12,12)):return (150,155,160,255)
    return STEEL(x,y)
tex(os.path.join(A,'textures/block/bunker_door_bottom.png'),door_bottom);tex(os.path.join(A,'textures/block/bunker_door_top.png'),door_top)
def door_item(x,y):
    if x<3 or x>12:return (0,0,0,0)
    if y<8:return door_top(int((x-3)*16/10),y*2) if True else 0
    return door_bottom(int((x-3)*16/10),(y-8)*2)
tex(os.path.join(A,'textures/item/bunker_door.png'),door_item)
def card(x,y):
    if not(1<=x<=14 and 4<=y<=11):return (0,0,0,0)
    if y in (4,11) or x in (1,14):return (30,60,110,255)
    if 3<=x<=6 and 6<=y<=8:return (230,200,60,255)
    if 8<=x<=12 and y in (6,8):return (220,230,240,255)
    return (60,110,190,255)
tex(os.path.join(A,'textures/item/access_card.png'),card)
print('vehicles2 ok')
