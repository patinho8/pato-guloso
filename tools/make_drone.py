"""Drone models: body (front = -z), 2-blade rotor, finned bomb, and the inventory item."""
import json,math,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica')
TEX={"dark":"mareeletrica:block/dark","steel":"mareeletrica:block/steel","black":"minecraft:block/black_concrete","gray":"minecraft:block/gray_concrete",
 "light":"minecraft:block/light_gray_concrete","white":"minecraft:block/white_concrete","red":"minecraft:block/red_concrete","green":"minecraft:block/lime_concrete",
 "teal":"mareeletrica:block/teal","glass":"minecraft:block/black_stained_glass","orange":"minecraft:block/orange_concrete","olive":"minecraft:block/green_terracotta","particle":"mareeletrica:block/dark"}
def box(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def oct_y(cx,cz,r,y0,y1,tex):
    a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
MOTORS=[(2,2),(14,2),(2,14),(14,14)]
def body():
    el=[]
    # fuselage with chamfered corners, top cover, battery
    el+=[box([5,3.5,4.4],[11,6.6,11.6],'dark'),box([5.6,3.5,4.9],[10.4,6.6,11.1],'dark',([8,5,8],'y',45)),
         box([5.4,6.6,4.8],[10.6,7.3,11.2],'steel'),box([6.2,7.3,6.1],[9.8,8.2,10.4],'black'),box([6.2,8.2,6.6],[9.8,8.3,7.0],'teal')]
    # arms (an X) and motor pods
    el+=[box([7.35,5.2,-.4],[8.65,6.1,16.4],'gray',([8,5.65,8],'y',45)),box([7.35,5.2,-.4],[8.65,6.1,16.4],'gray',([8,5.65,8],'y',-45))]
    for x,z in MOTORS:
        el+=oct_y(x,z,1.25,4.9,7.3,'steel')+oct_y(x,z,.75,7.3,7.85,'dark')
        el.append(box([x-.35,4.3,z-.35],[x+.35,4.9,z+.35],'green' if z<8 else 'red'))   # navigation lights under the motors
    # front camera gimbal
    el+=[box([7.1,2.9,3.9],[8.9,3.5,5.2],'steel'),box([6.7,1.3,3.2],[9.3,3.0,5.1],'dark'),box([7.25,1.7,2.85],[8.75,2.75,3.25],'glass'),box([7.6,1.95,2.75],[8.4,2.5,2.9],'teal')]
    # status LEDs and antennas at the back
    el+=[box([6.2,6.0,11.55],[7.0,6.5,11.8],'red'),box([9.0,6.0,11.55],[9.8,6.5,11.8],'green'),box([5.6,7.3,10.6],[5.95,10.2,10.95],'black'),box([10.05,7.3,10.6],[10.4,10.2,10.95],'black')]
    # landing gear (skids) and bomb rail
    for x0,x1 in ((3.9,4.7),(11.3,12.1)):
        el.append(box([x0,0,3.4],[x1,.5,12.6],'black'))
    for x0,x1 in ((4.3,5.1),(10.9,11.7)):
        for z0 in (4.3,11.0):el.append(box([x0,.4,z0],[x1,3.6,z0+.7],'black'))
    el+=[box([5.7,3.4,5.9],[10.3,3.5,10.1],'black')]   # bomb bay (seen when the doors open)
    return el
def rotor():
    el=oct_y(8,8,.55,0,.6,'dark')
    el+=[box([3.6,.15,7.55],[12.4,.4,8.45],'light',([8,.3,8],'x',22.5)) if False else box([3.6,.15,7.55],[12.4,.4,8.45],'light')]
    el+=[box([3.6,.14,7.5],[4.4,.42,8.5],'orange'),box([11.6,.14,7.5],[12.4,.42,8.5],'orange')]
    return el
def bomb():
    el=oct_y(8,8,.95,.6,2.9,'olive')+oct_y(8,8,.55,.1,.6,'dark')
    el+=[box([7.9,2.9,6.9],[8.1,3.0,9.1],'steel')]
    for rot in (0,45):pass
    el+=[box([6.6,2.4,7.9],[9.4,2.9,8.1],'dark'),box([7.9,2.4,6.6],[8.1,2.9,9.4],'dark'),box([6.95,1.6,6.95],[9.05,1.8,9.05],'orange')]
    return el
DISPLAY={"gui":{"rotation":[30,225,0],"scale":[.85,.85,.85]},"ground":{"translation":[0,2,0],"scale":[.45,.45,.45]},"fixed":{"scale":[.8,.8,.8]},
 "thirdperson_righthand":{"rotation":[0,0,0],"translation":[0,3,1],"scale":[.5,.5,.5]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,3,0],"scale":[.55,.55,.55]}}
def write(kind,name,el,display=False):
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    m={"ambientocclusion":False,"textures":TEX,"elements":el}
    if display:m["display"]=DISPLAY
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
write('block','drone_body',body());write('block','drone_hatch_l',[box([5.7,3.1,5.9],[8.0,3.4,10.1],'steel'),box([5.9,3.05,6.3],[7.8,3.1,9.7],'dark')]);write('block','drone_hatch_r',[box([8.0,3.1,5.9],[10.3,3.4,10.1],'steel'),box([8.2,3.05,6.3],[10.1,3.1,9.7],'dark')]);write('block','drone_rotor',rotor());write('block','drone_bomb_part',bomb())
icon=body()
for x,z in MOTORS:
    for e in rotor():
        e=json.loads(json.dumps(e));e['from']=[e['from'][0]-8+x,e['from'][1]+7.85,e['from'][2]-8+z];e['to']=[e['to'][0]-8+x,e['to'][1]+7.85,e['to'][2]-8+z];e.pop('rotation',None);icon.append(e)
write('item','drone',icon,True)
print('drone models:',len(body()),len(rotor()),len(bomb()),len(icon))
