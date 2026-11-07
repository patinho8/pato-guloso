"""Wind energy machines (0.46.0): block models, moving parts, blockstates, item models, loot tables."""
import json,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
T={"steel":"mareeletrica:block/steel","dark":"mareeletrica:block/dark","copper":"mareeletrica:block/copper","teal":"mareeletrica:block/teal","light":"mareeletrica:block/light",
 "amber":"mareeletrica:block/amber","black":"mareeletrica:block/black","white":"mareeletrica:block/white","blue":"mareeletrica:block/blue",
 "snow":"minecraft:block/white_concrete","concrete":"minecraft:block/light_gray_concrete","wool":"minecraft:block/white_wool","wood":"minecraft:block/spruce_planks",
 "log":"minecraft:block/stripped_spruce_log","red":"minecraft:block/red_concrete","lime":"minecraft:block/lime_concrete","orange":"minecraft:block/orange_concrete","particle":"mareeletrica:block/steel"}
def box(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def oct_y(cx,cz,r,y0,y1,tex):a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
def oct_z(cx,cy,r,z0,z1,tex):a=r*.924;return [box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex),box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex,([cx,cy,(z0+z1)/2],'z',45))]
def write(name,el,ao=True):
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    json.dump({"ambientocclusion":ao,"textures":T,"elements":el},open(os.path.join(A,'models/block',name+'.json'),'w'),separators=(',',':'))
ITEM_DISPLAY={"gui":{"rotation":[25,225,0],"translation":[0,-2,0],"scale":[.5,.5,.5]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.3,.3,.3]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.35,.35,.35]}}
# ---- moving parts ----
write('wind_hub',oct_z(8,8,1.6,6.5,10,'snow')+oct_z(8,8,1.0,5.5,6.5,'snow'))
write('wind_blade',[box([7.1,9,7.5],[8.9,14,8.5],'snow',([8,16,8],'y',22.5)),box([7.4,14,7.6],[8.6,23,8.4],'snow',([8,16,8],'y',22.5)),box([7.6,23,7.7],[8.4,24,8.3],'red',([8,16,8],'y',22.5))])
write('windmill_sail',[box([7.5,8,7.4],[8.5,24,8.6],'log'),box([8.5,11,7.9],[13,23,8.1],'wool'),box([8.5,14.8,7.6],[13,15.4,8.4],'log'),box([8.5,19,7.6],[13,19.6,8.4],'log'),box([12.6,11,7.6],[13.2,23,8.4],'log')])
sav=oct_y(8,8,5,0,1,'steel')+oct_y(8,8,5,17,18,'steel')+[box([7.4,0,7.4],[8.6,18,8.6],'dark')]
for a in (45,-45):sav+= [box([3.5,1,7.6],[8,17,8.4],'teal',([8,9,8],'y',a)),box([8,1,7.6],[12.5,17,8.4],'teal',([8,9,8],'y',a))]
sav+=[box([3.4,1,5.5],[4.2,17,8],'teal'),box([11.8,1,8],[12.6,17,10.5],'teal')]
write('wind_savonius',sav)
write('anemometer_cups',[box([2,0,7.6],[14,.8,8.4],'steel'),box([7.6,0,2],[8.4,.8,14],'steel'),box([1,-1,6.5],[3,1.5,8.5],'red'),box([13,-1,7.5],[15,1.5,9.5],'red'),box([7.5,-1,1],[9.5,1.5,3],'red'),box([6.5,-1,13],[8.5,1.5,15],'red'),box([7.2,-.5,7.2],[8.8,1.2,8.8],'dark')])
write('wind_tower_segment',oct_y(8,8,2.6,0,32,'snow'))
write('wind_nacelle',[box([3,0,-8],[13,8,20],'snow'),box([3.5,8,4],[12.5,8.6,14],'steel'),box([7,8.6,14],[9,12,15],'dark'),box([6,12,14],[10,12.6,15],'red')])
# ---- machines (block models; the renderer adds rotors) ----
M={}
M['wind_turbine_small']=[box([3,0,3],[13,2,13],'concrete'),box([4,2,4],[12,3,12],'dark')]+oct_y(8,8,1.2,3,16.5,'snow')+[box([6.5,16,2],[9.5,20,12],'snow'),box([7.6,19,11],[8.4,21.5,15],'steel'),box([7.8,17,12],[8.2,22,17],'dark')]
M['wind_turbine_medium']=[box([1,0,1],[15,3,15],'concrete'),box([5,3,5],[11,4,11],'dark')]+oct_y(8,8,1.7,4,28,'snow')+[box([5.8,27.5,1],[10.2,32,13],'snow'),box([7,11,9.6],[9,15,10.4],'dark'),box([7.4,13.5,10.3],[8.6,14.5,10.5],'lime')]
M['wind_turbine_giant']=[box([-4,0,-4],[20,3,20],'concrete'),box([2,3,2],[14,5,14],'dark')]+oct_y(8,8,2.6,5,32,'snow')+[box([6.6,6,10.2],[9.4,10,10.6],'dark'),box([7.4,8,10.5],[8.6,9,10.7],'lime')]
legs=[]
for x,z,ax,ang in ((1.5,1.5,'x',-22.5),(14.5,1.5,'x',-22.5),(1.5,14.5,'x',22.5),(14.5,14.5,'x',22.5)):
    legs.append(box([x-.6,0,z-.6],[x+.6,24,z+.6],'log',([x,0,z],'z',22.5 if x<8 else -22.5)))
M['farm_windmill']=[box([0,0,0],[16,1,16],'wood')]+legs+[box([2,8,2],[14,8.8,14],'log'),box([3.5,16,3.5],[12.5,16.8,12.5],'log'),box([5,23,3],[11,29,13],'wood'),box([5.4,29,3.4],[10.6,30,12.6],'red'),
    box([7.8,24,13],[8.2,25,26],'log'),box([7.7,22,24],[8.3,31,31],'wood')]
M['rooftop_turbine']=[box([3,0,3],[13,2,13],'dark'),box([6.8,2,6.8],[9.2,4.5,9.2],'steel'),box([4,.5,12.9],[6,1.5,13.1],'lime')]
M['anemometer_mast']=[box([4,0,4],[12,1.5,12],'concrete')]+oct_y(8,8,.8,1.5,28,'steel')+[box([7,27,7],[9,28.6,9],'dark'),box([7.7,28.6,7.7],[8.3,29.4,8.3],'steel'),
    box([5,10,7.6],[11,10.6,8.4],'steel'),box([10.5,9,7],[12,12,9],'amber')]
M['wind_battery']=[box([1,0,1],[15,2,15],'dark'),box([2,2,2],[14,14,14],'teal'),box([2,5,1.9],[14,6,14.1],'snow'),box([2,10,1.9],[14,11,14.1],'snow'),
    box([4,14,4],[6,15.5,6],'copper'),box([10,14,10],[12,15.5,12],'copper'),box([5,7,1.6],[11,9.5,2],'black'),box([5.4,7.4,1.5],[8.5,9.1,1.6],'amber')]
fins=[box([3,0,3],[13,12,13],'dark'),box([4,12,4],[12,13,12],'steel'),box([4.5,4,2.8],[11.5,10,3],'black'),box([5,8,2.7],[8,9.4,2.8],'blue')]
for x in range(4,13,2):fins.append(box([x,1,13],[x+.8,11,15.5],'steel'))
M['wind_inverter']=fins
M['wind_control_panel']=[box([1,0,3],[15,8,15],'dark'),box([1,8,3],[15,10,15],'steel'),box([2,9,4],[14,10.6,11],'black',([8,9.5,7.5],'x',22.5)),box([2.6,9.7,4.6],[13.4,10.8,10.4],'blue',([8,9.5,7.5],'x',22.5)),
    box([3,10,11.5],[5,10.8,13.5],'red'),box([6,10,11.5],[8,10.8,13.5],'lime'),box([9,10,11.5],[13,10.6,13.5],'amber'),box([2,10,13.8],[14,14,15],'steel')]
for name,el in M.items():
    write(name,el)
    act=json.loads(json.dumps(el))
    for e in act:
        for f in e['faces'].values():
            if f['texture']=='#amber':f['texture']='#lime'
    write(name+'_active',act)
    json.dump({"variants":{f"active={a},facing={f}":(dict(model=f"mareeletrica:block/{name}{'_active' if a=='true' else ''}",**({"y":y} if y else {}))) for a in ("false","true") for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates',name+'.json'),'w'))
    json.dump({"parent":f"mareeletrica:block/{name}","display":ITEM_DISPLAY},open(os.path.join(A,'models/item',name+'.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":f"mareeletrica:{name}"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks',name+'.json'),'w'))
# wind cable: thicker white cable with teal stripes (same shapes as the normal cable)
cm=json.load(open(os.path.join(A,'models/block/cable_center.json'))) if os.path.exists(os.path.join(A,'models/block/cable_center.json')) else None
print('wind ok', cm is not None)
