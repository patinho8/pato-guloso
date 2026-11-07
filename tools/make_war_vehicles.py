"""Models for the 10-tube barrage launcher, its rocket, the fighter jet and its bomb (block models drawn by renderers)."""
import json,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica')
TEX={"dark":"mareeletrica:block/dark","steel":"mareeletrica:block/steel","black":"minecraft:block/black_concrete","gray":"minecraft:block/gray_concrete",
 "light":"minecraft:block/light_gray_concrete","white":"minecraft:block/white_concrete","red":"minecraft:block/red_concrete","orange":"minecraft:block/orange_concrete",
 "olive":"minecraft:block/green_terracotta","khaki":"minecraft:block/brown_terracotta","glass":"minecraft:block/light_blue_stained_glass","yellow":"minecraft:block/yellow_concrete",
 "particle":"mareeletrica:block/dark"}
def box(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def oct_y(cx,cz,r,y0,y1,tex):
    a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
def oct_z(cx,cy,r,z0,z1,tex):
    a=r*.924;return [box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex),box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex,([cx,cy,(z0+z1)/2],'z',45))]
def write(kind,name,el,display=None,tex=TEX):
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    m={"ambientocclusion":False,"textures":tex,"elements":el}
    if display:m["display"]=display
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
# ---------------- barrage launcher (pod faces -z, pivot at (8,11,8)) ----------------
def l_base():
    el=[box([0,0,0],[16,2.5,16],'olive'),box([1,2.5,1],[15,3,15],'dark')]
    for x,z in ((1.5,1.5),(14.5,1.5),(1.5,14.5),(14.5,14.5)):el+=oct_y(x,z,1.1,0,2.8,'black')   # outrigger feet
    el+=[box([.5,1,7],[15.5,2,9],'khaki')]
    return el
def l_turret():
    el=oct_y(8,8,5.5,3,5,'steel')+oct_y(8,8,4.2,5,6,'dark')
    for x0,x1 in ((1.2,2.8),(13.2,14.8)):el+=[box([x0,5,5.5],[x1,12.5,10.5],'olive'),box([x0-.2,10,7],[x1+.2,12,9],'dark')]   # yoke arms
    el+=[box([6,6,10],[10,9,13],'khaki'),box([6.5,9,10.5],[9.5,9.5,12.5],'black')]   # elevation motor box
    return el
def l_pod():
    el=[box([3,7.6,-6],[13,14.4,12],'olive'),box([2.8,7.4,-6.4],[13.2,14.6,-5.6],'dark'),box([2.8,7.4,11],[13.2,14.6,11.6],'dark'),
        box([2.8,10.8,-6],[3,11.2,12],'khaki'),box([13,10.8,-6],[13.2,11.2,12],'khaki'),box([3.2,14.4,0],[12.8,14.8,6],'khaki')]
    for i in range(10):   # dark holes in the back plate (back blast)
        x=8+((i%5)-2)*2.3;y=11+(1 if i<5 else -1)*1.4;el.append(box([x-.6,y-.6,11.6],[x+.6,y+.6,11.7],'black'))
    return el
def l_tube():   # one tube centred on the pod axis; the renderer places ten of them
    return oct_z(8,11,.95,-7.2,-5.5,'steel')+[box([7.45,10.45,-7.3],[8.55,11.55,-6.4],'black')]
def rocket():   # nose -z
    el=oct_z(8,8,.9,-3,6,'light')+oct_z(8,8,.6,-4.6,-3,'red')+[box([7.75,7.75,-5.2],[8.25,8.25,-4.6],'red')]
    el+=[box([6.2,7.85,4],[9.8,8.15,6.4],'dark'),box([7.85,6.2,4],[8.15,9.8,6.4],'dark'),box([7.4,7.4,6],[8.6,8.6,6.6],'black')]
    return el
# ---------------- fighter jet (nose -z, centre of gravity (8,6,8), drawn at scale 2) ----------------
def jet():
    el=[]
    el+=oct_z(8,6,3.2,-6,26,'light')                                   # fuselage
    el+=oct_z(8,6.2,2.5,-10,-6,'light')+oct_z(8,6.4,1.6,-13,-10,'gray')+oct_z(8,6.5,.8,-15,-13,'dark')+[box([7.8,6.3,-16],[8.2,6.7,-15],'black')]   # nose & pitot
    el+=[box([6,8.7,3],[10,10.2,10],'gray')]                            # spine
    for x0,x1 in ((3.2,5),(11,12.8)):el+=[box([x0,3.6,-1],[x1,8,14],'gray'),box([x0+.3,4,-1.2],[x1-.3,7.6,-.9],'black')]   # intakes
    # swept wings (stepped trapezoids) and the wing guns
    for side in (-1,1):
        def X(a,b):return (8-b,8-a) if side<0 else (8+a,8+b)
        for (a,b,z0,z1) in ((3,8,4,22),(8,13,9,22),(13,18,14,22),(18,21.5,18,22)):
            x0,x1=X(a,b);el.append(box([x0,5,z0],[x1,5.8,z1],'light'))
        x0,x1=X(18,21.5);el.append(box([x0,4.8,18],[x1,6,22.4],'gray'))
        x0,x1=X(6.2,7.2);el+=[box([x0,4.4,-1],[x1,5.2,7],'dark'),box([x0+.2,4.6,-2],[x1-.2,5,-1],'black')]      # gun barrels
        x0,x1=X(9,10.2);el.append(box([x0,3.2,12],[x1,5,20],'gray'))     # missile pylon look
        for (a,b,z0) in ((3,8,24),(8,12,26)):
            x0,x1=X(a,b);el.append(box([x0,5.6,z0],[x1,6.2,31],'light'))  # tailplanes
        x0,x1=X(1.6,2.4);el+=[box([x0,9,21],[x1,13,30],'light'),box([x0,13,24],[x1,16.5,30],'light'),box([x0,16.5,27],[x1,17.5,30],'red')]  # twin fins
    el+=oct_z(8,6,2.6,26,29.5,'dark')+[box([6.2,4.2,29.5],[9.8,7.8,29.7],'black')]   # nozzle
    el+=[box([7.6,0,-5],[8.4,3,-4],'black'),box([7.4,0,-5.3],[8.6,1,-3.7],'black')]   # nose gear
    for x0 in (3.6,11.6):el+=[box([x0,0,12],[x0+.8,3.2,13],'black'),box([x0-.2,0,11.6],[x0+1,1.1,13.4],'black')]   # main gear
    el+=[box([7.9,9.2,13],[8.1,10.2,17],'yellow'),box([4.6,5.85,8],[5.4,5.95,10],'red'),box([10.6,5.85,8],[11.4,5.95,10],'red')]   # markings
    return el
def jet_canopy():
    return [box([6.2,8.6,-6],[9.8,10.6,3],'glass'),box([6.6,10.6,-4],[9.4,11.2,1.5],'glass'),box([7.6,10.4,-6.4],[8.4,11.3,-5.8],'dark')]
def jet_bomb():   # hangs under the fuselage
    el=oct_z(8,1.6,1.3,2,15,'olive')+oct_z(8,1.6,.8,0,2,'olive')+oct_z(8,1.6,.4,-.6,0,'dark')
    el+=[box([6,1.45,14],[10,1.75,17],'dark'),box([7.85,-.4,14],[8.15,3.6,17],'dark'),box([7.6,2.9,6],[8.4,3.3,10],'steel'),box([6.7,.3,10.8],[9.3,2.9,11.1],'yellow')]
    return el
write('block','barrage_base',l_base());write('block','barrage_turret',l_turret());write('block','barrage_pod',l_pod());write('block','barrage_tube',l_tube())
write('block','barrage_rocket_proj',rocket());write('block','fighter_jet',jet());write('block','jet_canopy',jet_canopy());write('block','jet_bomb_part',jet_bomb())
write('block','barrage_launcher',[],tex={"particle":"minecraft:block/green_terracotta"})
# items
D_BIG={"gui":{"rotation":[30,225,0],"translation":[0,0,0],"scale":[.3,.3,.3]},"ground":{"translation":[0,2,0],"scale":[.3,.3,.3]},"fixed":{"scale":[.5,.5,.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.3,.3,.3]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,3,0],"scale":[.35,.35,.35]}}
D_LAUNCH={"gui":{"rotation":[30,225,0],"scale":[.6,.6,.6]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.375,.375,.375]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,0,0],"scale":[.4,.4,.4]}}
D_SMALL={"gui":{"rotation":[30,225,0],"scale":[1.1,1.1,1.1]},"ground":{"translation":[0,3,0],"scale":[.6,.6,.6]},"fixed":{"rotation":[0,90,0],"scale":[1,1,1]},
 "thirdperson_righthand":{"rotation":[0,0,0],"translation":[0,3,1],"scale":[.6,.6,.6]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,3,0],"scale":[.7,.7,.7]}}
icon=l_base()+l_turret()+l_pod()
for i in range(10):
    for e in l_tube():
        e=json.loads(json.dumps(e));dx=((i%5)-2)*2.3;dy=(1 if i<5 else -1)*1.4
        e['from']=[e['from'][0]+dx,e['from'][1]+dy,e['from'][2]];e['to']=[e['to'][0]+dx,e['to'][1]+dy,e['to'][2]]
        if 'rotation' in e:e['rotation']['origin']=[e['rotation']['origin'][0]+dx,e['rotation']['origin'][1]+dy,e['rotation']['origin'][2]]
        icon.append(e)
write('item','barrage_launcher',icon,D_LAUNCH)
write('item','barrage_rocket',rocket(),D_SMALL)
write('item','jet_bomb',jet_bomb(),D_SMALL)
write('item','fighter_jet',jet()+jet_canopy(),D_BIG)
json.dump({"variants":{f"facing={f}":{"model":"mareeletrica:block/barrage_launcher"} for f in ("north","south","east","west")}},open(os.path.join(A,'blockstates/barrage_launcher.json'),'w'))
print('ok',len(icon),len(jet()))

# ---------------- motorcycle (nose -z, front hub z=1, rear hub z=17, hubs at y=3.5; drawn at scale 2) ----------------
TEX_B=dict(TEX,paint="minecraft:block/red_concrete",chrome="minecraft:block/iron_block",lamp="minecraft:block/sea_lantern",tail="minecraft:block/redstone_lamp")
def oct_x(cy,cz,r,x0,x1,tex):
    a=r*.924;return [box([x0,cy-a,cz-a],[x1,cy+a,cz+a],tex),box([x0,cy-a,cz-a],[x1,cy+a,cz+a],tex,([(x0+x1)/2,cy,cz],'x',45))]
def bike_body():
    el=[box([6.5,3,7],[9.5,7,12],'dark'),box([6.2,4,7.5],[6.6,6.5,11],'steel'),box([9.4,4,7.5],[9.8,6.5,11],'steel')]   # engine + covers
    el+=[box([6.2,7.3,4.6],[9.8,10,9.6],'paint'),box([6.7,10,5.2],[9.3,10.4,8.6],'paint'),box([7.6,10.4,6],[8.4,10.6,6.8],'chrome')]   # tank + cap
    el+=[box([6.6,8.6,9.4],[9.4,9.7,15],'black'),box([6.9,9.7,12.6],[9.1,10.3,15],'black')]                                       # seat
    el+=[box([7,8.2,14],[9,9,19],'paint'),box([7.3,8.4,19],[8.7,9.2,19.4],'tail'),box([7.6,6,18.5],[8.4,8.2,19],'dark')]           # tail + light + plate
    el+=[box([6.8,3,10],[7.3,4,17.5],'steel'),box([8.7,3,10],[9.2,4,17.5],'steel'),box([7,4,8],[9,7.6,9],'steel')]                # swing arm + frame
    el+=[box([9.7,3.4,9],[10.7,4.4,18],'chrome'),box([9.8,3.5,18],[10.6,4.3,19.5],'dark'),box([9.5,4.4,8],[10.2,6,9.2],'chrome')]  # exhaust
    el+=[box([6.8,6.5,3.5],[9.2,9.5,5],'dark')]                                                                               # head tube
    return el
def bike_fork():   # turns with the steering; pivot at z=2
    el=[]
    for x0 in (6.6,8.9):el+=[box([x0,3.5,.7],[x0+.5,10.5,1.3],'chrome',([x0+.25,7,1],'x',-22.5))]
    el+=[box([6.4,8.6,1.4],[9.6,11.6,4],'paint'),box([7,9.4,.6],[9,11,1.4],'lamp'),box([6.8,9.2,.5],[9.2,11.2,.7],'chrome')]   # fairing + headlight
    el+=[box([4,11.8,2.6],[12,12.3,3.1],'black'),box([3.6,11.6,2.4],[5,12.5,3.3],'dark'),box([11,11.6,2.4],[12.4,12.5,3.3],'dark'),box([6.6,12.3,2.5],[9.4,13.2,3.6],'black')]   # bars + dash
    el+=[box([7,7.3,-1.5],[9,7.8,3.5],'paint')]                                                                               # front fender
    return el
def bike_wheel():   # centred on (8,8,8), rolls about x
    el=oct_x(8,8,3.5,7.25,8.75,'black')+oct_x(8,8,2.6,7.15,8.85,'gray')+oct_x(8,8,1,6.9,9.1,'chrome')
    for a in (0,45):el.append(box([7.05,7.75,5.6],[8.95,8.25,10.4],'dark',([8,8,8],'x',a) if a else None))
    el.append(box([7.05,5.6,7.75],[8.95,10.4,8.25],'dark'))
    return el
write('block','bike_body',bike_body(),tex=TEX_B);write('block','bike_fork',bike_fork(),tex=TEX_B);write('block','bike_wheel',bike_wheel(),tex=TEX_B)
icon=bike_body()+bike_fork()
for zc in (1,17):
    for e in bike_wheel():
        e=json.loads(json.dumps(e));dy,dz=3.5-8,zc-8
        e['from']=[e['from'][0],e['from'][1]+dy,e['from'][2]+dz];e['to']=[e['to'][0],e['to'][1]+dy,e['to'][2]+dz]
        if 'rotation' in e:o=e['rotation']['origin'];e['rotation']['origin']=[o[0],o[1]+dy,o[2]+dz]
        icon.append(e)
write('item','motorcycle',icon,{"gui":{"rotation":[20,135,0],"translation":[0,-1,0],"scale":[.55,.55,.55]},"ground":{"translation":[0,2,0],"scale":[.4,.4,.4]},"fixed":{"rotation":[0,90,0],"scale":[.6,.6,.6]},
 "thirdperson_righthand":{"rotation":[0,90,0],"translation":[0,2,0],"scale":[.4,.4,.4]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,2,0],"scale":[.45,.45,.45]}},tex=TEX_B)
print('bike ok')

# ---------------- parachute canopy (harness at y=0, canopy around y 20..27, centre x/z 8) ----------------
def parachute():
    el=[]
    cols=['orange','white']
    for i,(x0,x1,y0) in enumerate(((-14,-8,21),(-8,-2,23.5),(-2,4,25),(4,10,25),(10,16,25),(16,22,23.5),(22,30,21))):
        el.append(box([x0,y0,-6],[x1,y0+1.2,22],cols[i%2]))
    el+=[box([-14,20,-6.5],[30,21,-6],'dark'),box([-14,20,22],[30,21,22.5],'dark')]
    for x in (-13,29):
        for z in (-5,21):
            dx=(8-x);dz=(8-z)
            el.append(box([x,1.5,z],[x+.25,21,z+.25],'black',([x,11,z],'z',22.5 if dx>0 else -22.5)))
    el+=[box([6.5,0,6.5],[9.5,2,9.5],'dark')]
    return el
write('block','parachute_canopy',parachute())
D_P={"gui":{"rotation":[30,225,0],"scale":[.35,.35,.35]},"ground":{"scale":[.25,.25,.25]},"fixed":{"scale":[.3,.3,.3]},"thirdperson_righthand":{"translation":[0,0,0],"scale":[.15,.15,.15]},"firstperson_righthand":{"translation":[0,0,0],"scale":[.2,.2,.2]}}
write('item','parachute',[box([4,0,5],[12,9,11],'olive'),box([4.5,9,5.5],[11.5,10,10.5],'khaki'),box([5,3,11],[6,8,11.6],'black'),box([10,3,11],[11,8,11.6],'black'),box([6.5,5,4.5],[9.5,7,5],'orange')],
 {"gui":{"rotation":[30,225,0],"scale":[1,1,1]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"fixed":{"scale":[1,1,1]},"thirdperson_righthand":{"translation":[0,2,1],"scale":[.6,.6,.6]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,2,0],"scale":[.6,.6,.6]}})
write('item','flare',[box([7,0,7],[9,10,9],'red'),box([6.8,10,6.8],[9.2,11.5,9.2],'black'),box([7.3,11.5,7.3],[8.7,12,8.7],'yellow'),box([6.8,2,6.8],[9.2,4,9.2],'white')],
 {"gui":{"rotation":[30,225,0],"scale":[1.1,1.1,1.1]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"fixed":{"scale":[1,1,1]},"thirdperson_righthand":{"translation":[0,2,1],"scale":[.55,.55,.55]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,3,0],"scale":[.6,.6,.6]}})
print('parachute/flare ok')

# ---------------- flare glow cores (drawn full-bright) ----------------
FLARE_TEX={"red":"red_concrete","green":"lime_concrete","yellow":"yellow_concrete","magenta":"magenta_concrete","orange":"orange_concrete","cyan":"light_blue_concrete"}
for name,tex in FLARE_TEX.items():
    T={"c":"minecraft:block/"+tex,"w":"minecraft:block/white_concrete","particle":"minecraft:block/"+tex}
    el=[box([4.5,4.5,4.5],[11.5,11.5,11.5],'c'),box([2,7,7],[14,9,9],'c'),box([7,2,7],[9,14,9],'c'),box([7,7,2],[9,9,14],'c'),
        box([5,5,5],[11,11,11],'c',([8,8,8],'y',45)),box([6.5,11.5,6.5],[9.5,12.5,9.5],'w')]
    write('block','flare_glow_'+name,el,tex=T)
print('flare glows ok')

# ---------------- security camera (lens looks north / -z, wall at z=16) ----------------
def security_camera():
    T=TEX
    R=lambda: ([8,10.9,10],'x',-22.5)   # housing tilted nose-down
    el=oct_z(8,8,3.2,15,16,'light')
    for x,y in ((6,6),(10,6),(6,10),(10,10)):el.append(box([x-.3,y-.3,14.8],[x+.3,y+.3,15],'dark'))
    el+=[box([7.2,7.4,10.5],[8.8,9.0,15],'steel'),box([6.9,8.3,9.6],[9.1,10.5,11.2],'dark')]          # arm + swivel joint
    el+=[box([5.2,8.8,2.5],[10.8,13.0,11.5],'white',R()),                                              # housing
         box([4.8,13.0,1.4],[11.2,13.6,11.5],'light',R()),box([4.8,10.6,1.4],[5.2,13.0,4.2],'light',R()),box([10.8,10.6,1.4],[11.2,13.0,4.2],'light',R()),   # sun hood
         box([5.6,9.2,2.2],[10.4,12.6,2.5],'black',R()),                                               # front bezel
         box([6.5,9.9,1.6],[9.5,11.9,2.2],'glass',R()),box([7.0,9.4,1.6],[9.0,12.4,2.2],'glass',R()),  # lens
         box([7.3,10.2,1.45],[8.7,11.6,1.6],'blue',R()),
         box([5.9,11.9,2.05],[6.4,12.4,2.2],'red',R()),box([9.6,11.9,2.05],[10.1,12.4,2.2],'red',R()),box([5.9,9.4,2.05],[6.4,9.9,2.2],'red',R()),
         box([9.7,9.3,2.0],[10.1,9.6,2.2],'red',R()),
         box([5.6,9.2,11.5],[10.4,12.6,12.3],'light',R()),box([7.6,9.6,12.3],[8.4,10.2,14.6],'black'),   # rear cap + cable
         box([5.15,9.6,5],[5.25,10.2,9],'blue',R())]                                                    # brand stripe
    return el
CAM_TEX=dict(TEX,glass="minecraft:block/black_stained_glass",blue="minecraft:block/light_blue_concrete",red="minecraft:block/red_concrete",white="minecraft:block/white_concrete",light="minecraft:block/light_gray_concrete")
write('block','security_camera',security_camera(),{"gui":{"rotation":[30,135,0],"scale":[.9,.9,.9]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"fixed":{"rotation":[0,180,0]},
 "thirdperson_righthand":{"rotation":[75,180,0],"translation":[0,2.5,0],"scale":[.5,.5,.5]},"firstperson_righthand":{"rotation":[0,135,0],"translation":[0,2,0],"scale":[.5,.5,.5]}},tex=CAM_TEX)
print('camera ok')

# ---------------- anti-air missile launcher (rack points -z; pivot (8,10,8)) and missile ----------------
def sam_base():
    el=[box([1,0,1],[15,2,15],'gray'),box([2,2,2],[14,3,14],'dark')]+oct_y(8,8,3.4,3,7,'steel')
    el+=[box([2,1,2],[4,3,4],'black'),box([12,1,2],[14,3,4],'black'),box([2,1,12],[4,3,14],'black'),box([12,1,12],[14,3,14],'black')]
    el+=[box([10.5,3,10],[13.5,6,13.5],'dark'),box([11,6,11],[13,6.4,13],'red')]   # radar box
    return el
def sam_rack():
    el=[box([5.5,7,6.5],[10.5,9.5,9.5],'dark'),box([4.5,9.5,2],[11.5,14.5,15],'olive'),box([4.3,9.3,1.6],[11.7,14.7,2.2],'dark')]
    for x,y in ((6.3,10.6),(9.7,10.6),(6.3,13.4),(9.7,13.4)):el+=[box([x-1.1,y-1.1,1.4],[x+1.1,y+1.1,1.7],'black'),box([x-.5,y-.5,1.3],[x+.5,y+.5,1.45],'red')]
    el+=[box([11.5,12,6],[13,14,10],'dark'),box([12.6,12.4,6.2],[13.1,13.6,9.8],'glass')]   # tracking camera
    return el
def sam_missile():   # slim white body, black band, ogive red nose, canards, big tail fins, dark nozzle
    el=oct_z(8,8,.75,-1.5,7,'white')+oct_z(8,8,.62,-2.8,-1.5,'white')+oct_z(8,8,.45,-3.8,-2.8,'red')+oct_z(8,8,.25,-4.5,-3.8,'red')
    el+=oct_z(8,8,.78,1.0,1.6,'black')+oct_z(8,8,.55,7,7.6,'dark')
    el+=[box([6.9,7.92,-1.2],[9.1,8.08,-.2],'dark'),box([7.92,6.9,-1.2],[8.08,9.1,-.2],'dark')]                       # canards
    el+=[box([6.0,7.9,4.8],[10.0,8.1,7.0],'gray'),box([7.9,6.0,4.8],[8.1,10.0,7.0],'gray'),box([6.0,7.9,6.4],[6.4,8.1,7.0],'red'),box([9.6,7.9,6.4],[10.0,8.1,7.0],'red')]   # tail fins
    return el
write('block','sam_base',sam_base());write('block','sam_rack',sam_rack());write('block','sam_missile_proj',sam_missile())
write('block','missile_launcher',[],tex={"particle":"minecraft:block/gray_concrete"})
write('item','missile_launcher',sam_base()+sam_rack(),{"gui":{"rotation":[30,225,0],"scale":[.62,.62,.62]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.375,.375,.375]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.4,.4,.4]}})
write('item','sam_missile',sam_missile(),{"gui":{"rotation":[30,225,0],"scale":[1.1,1.1,1.1]},"ground":{"translation":[0,3,0],"scale":[.6,.6,.6]},"fixed":{"rotation":[0,90,0]},
 "thirdperson_righthand":{"translation":[0,3,1],"scale":[.6,.6,.6]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,3,0],"scale":[.7,.7,.7]}})
json.dump({"variants":{"":{"model":"mareeletrica:block/missile_launcher"}}},open(os.path.join(A,'blockstates/missile_launcher.json'),'w'))
print('sam ok')

# ---------------- automatic gate (facing north: panel spans x at z 6.5..9.5) ----------------
GT=dict(TEX,iron="minecraft:block/iron_block",yellow="minecraft:block/yellow_concrete",black="minecraft:block/black_concrete",lamp="minecraft:block/redstone_lamp_on")
def gate_closed():
    el=[box([0,0,6],[16,1,10],'dark'),box([0,1,6.8],[16,3,9.2],'yellow'),box([0,13.5,6.8],[16,15.5,9.2],'iron'),box([0,7.4,7.2],[16,8.6,8.8],'iron')]
    for x in range(0,16,4):el.append(box([x,1,7.25],[x+2,3,8.75],'black'))   # hazard stripes on the bottom rail
    for x in (1,4,7,10,13):el.append(box([x,3,7.4],[x+1.4,13.5,8.6],'steel'))
    el+=[box([7,15.5,7],[9,16,9],'dark'),box([7.5,15.6,6.6],[8.5,15.95,7],'lamp')]   # sensor
    return el
def gate_open():
    return [box([0,0,6],[16,1,10],'dark'),box([0,1,6.8],[16,1.5,9.2],'yellow'),box([7,1,7],[9,1.6,9],'lamp')]
write('block','auto_gate_closed',gate_closed(),tex=GT);write('block','auto_gate_open',gate_open(),tex=GT)
write('item','auto_gate',gate_closed(),{"gui":{"rotation":[30,225,0],"scale":[.625,.625,.625]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.375,.375,.375]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.4,.4,.4]}},tex=GT)
var={}
for f,y in (("north",0),("east",90),("south",180),("west",270)):
    for o in ("true","false"):
        for m in ("0","1","2"):
            v={"model":"mareeletrica:block/auto_gate_"+("open" if o=="true" else "closed")}
            if y:v["y"]=y
            var[f"facing={f},mode={m},open={o}"]=v
json.dump({"variants":var},open(os.path.join(A,'blockstates/auto_gate.json'),'w'))
print('gate ok')

# ---------------- crewed weapons: machine-gun turret and 3x3 field cannon (barrels point -z) ----------------
def turret_base():
    el=[box([7,0,7],[9,11,9],'dark'),box([6.5,10,6.5],[9.5,11.5,9.5],'steel')]
    for rot in (0,120,240):pass
    el+=[box([7.6,0,-2],[8.4,1,8],'black',([8,.5,8],'x',22.5)),box([7.6,0,8],[8.4,1,18],'black',([8,.5,8],'x',-22.5)),box([-2,0,7.6],[8,1,8.4],'black',([8,.5,8],'z',-22.5)),box([8,0,7.6],[18,1,8.4],'black',([8,.5,8],'z',22.5))]
    el+=[box([6,4,12],[10,5,15],'olive'),box([7,0,13],[9,4,14],'dark')]   # ammo can
    return el
def turret_gun():   # pivot (8,12,8)
    el=[box([6.6,11,4],[9.4,13.6,12],'dark'),box([6.9,13.6,6],[9.1,14.2,10],'olive'),box([9.4,10.8,5],[11.5,13.2,9],'olive')]   # receiver, cover, ammo box
    el+=oct_z(8,12.3,.75,-14,4,'black')+oct_z(8,12.3,1.15,-8,0,'gray')+[box([7.7,12,-14.6],[8.3,12.6,-14],'dark')]   # barrel + heat shroud
    el+=[box([7.6,13,-12.5],[8.4,14.2,-12],'dark'),box([7,8.5,12],[9,12.5,14],'dark'),box([5,10,11],[11,10.8,12],'black')]   # sight, grips
    el+=[box([4.5,8,8],[6.6,12,9],'steel'),box([9.4,8,8],[11.5,12,9],'steel')]   # yoke
    return el
def cannon_mount():   # fixed: concrete pad with a steel ring the gun turns on
    el=oct_y(8,8,11,0,1.5,'gray')+oct_y(8,8,8.5,1.5,2.5,'dark')
    for x,z in ((-1,-1),(17,-1),(-1,17),(17,17)):el.append(box([x-1,0,z-1],[x+1,2,z+1],'black'))   # anchor bolts
    return el
def cannon_carriage():   # turns with the aim: turntable, cradle and gun shield (no wheels)
    el=oct_y(8,8,7,2.5,4,'steel')
    el+=[box([1,4,2],[15,9,14],'olive'),box([2,9,4],[14,11,12],'olive')]           # cradle
    el+=[box([0,4,7],[2,12,9],'dark'),box([14,4,7],[16,12,9],'dark')]               # trunnion cheeks
    el+=[box([-3,7,-2],[19,22,-1],'olive'),box([-3,7,-2],[-2,18,6],'olive'),box([18,7,-2],[19,18,6],'olive')]   # gun shield
    el+=[box([5,4,14],[11,6,20],'dark'),box([6,6,18],[10,7,21],'black')]            # gunner's step and seat plate
    return el
def cannon_barrel():   # pivot (8,16,8)
    el=oct_z(8,16,2.3,-15,12,'dark')+oct_z(8,16,3.2,4,14,'olive')+oct_z(8,16,2.8,-16,-13,'gray')+[box([6.5,14.5,-16],[9.5,17.5,-15.9],'black')]
    el+=[box([5,12.5,9],[11,14,15],'gray'),box([6.5,19,6],[9.5,20,10],'dark')]   # recoil cylinders, sight
    return el
write('block','cannon_mount',cannon_mount());write('block','tracer_round',[box([7.25,7.25,0],[8.75,8.75,16],'tracer'),box([7.6,7.6,-2],[8.4,8.4,0],'tip')],tex=dict(TEX,tracer='minecraft:block/shroomlight',tip='minecraft:block/gold_block'));write('block','turret_base',turret_base());write('block','turret_gun',turret_gun());write('block','cannon_carriage',cannon_carriage());write('block','cannon_barrel',cannon_barrel())
write('item','gun_turret',turret_base()+turret_gun(),{"gui":{"rotation":[30,225,0],"scale":[.55,.55,.55]},"ground":{"translation":[0,3,0],"scale":[.3,.3,.3]},"fixed":{"scale":[.5,.5,.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.3,.3,.3]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.35,.35,.35]}})
write('item','field_cannon',cannon_mount()+cannon_carriage()+cannon_barrel(),{"gui":{"rotation":[30,225,0],"translation":[0,-1,0],"scale":[.36,.36,.36]},"ground":{"translation":[0,3,0],"scale":[.2,.2,.2]},"fixed":{"scale":[.3,.3,.3]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.2,.2,.2]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.25,.25,.25]}})
print('emplacements ok')
