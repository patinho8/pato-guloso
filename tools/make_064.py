"""0.64: sentry gun, flak battery, auto grenade launcher (block parts drawn by BatteryRenderer), their projectiles,
the flak shell / 40mm grenade items and the ammo crate. Recipes, loot tables, blockstates and lang keys too."""
import json,os,copy
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
TEX={"dark":"mareeletrica:block/dark","steel":"mareeletrica:block/steel","black":"minecraft:block/black_concrete","gray":"minecraft:block/gray_concrete",
 "light":"minecraft:block/light_gray_concrete","olive":"minecraft:block/green_terracotta","khaki":"minecraft:block/brown_terracotta",
 "yellow":"minecraft:block/yellow_concrete","brass":"minecraft:block/gold_block","red":"minecraft:block/red_concrete","glass":"minecraft:block/light_blue_stained_glass",
 "particle":"mareeletrica:block/dark"}
def box(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def oct_y(cx,cz,r,y0,y1,tex):
    a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
def oct_z(cx,cy,r,z0,z1,tex):
    a=r*.924;return [box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex),box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex,([cx,cy,(z0+z1)/2],'z',45))]
def oct_x(cy,cz,r,x0,x1,tex):
    a=r*.924;return [box([x0,cy-a,cz-a],[x1,cy+a,cz+a],tex),box([x0,cy-a,cz-a],[x1,cy+a,cz+a],tex,([(x0+x1)/2,cy,cz],'x',45))]
def write(kind,name,el,display=None,tex=TEX):
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    m={"ambientocclusion":False,"textures":tex,"elements":el}
    if display:m["display"]=display
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
def moved(els,dx=0,dy=0,dz=0):
    out=[]
    for e in copy.deepcopy(els):
        e['from']=[e['from'][0]+dx,e['from'][1]+dy,e['from'][2]+dz];e['to']=[e['to'][0]+dx,e['to'][1]+dy,e['to'][2]+dz]
        if 'rotation' in e:o=e['rotation']['origin'];e['rotation']['origin']=[o[0]+dx,o[1]+dy,o[2]+dz]
        out.append(e)
    return out

# ---------------- sentry gun (pivot y=10): tripod post, yoke, receiver, 6-barrel gatling cluster ----------------
def s_base():
    el=oct_y(8,8,5.2,0,1.2,'gray')+oct_y(8,8,2,1.2,6,'steel')+oct_y(8,8,2.8,1.2,2.2,'dark')
    for x,z in ((2.5,2.5),(13.5,2.5),(2.5,13.5),(13.5,13.5)):el+=[box([x-1,0,z-1],[x+1,1.6,z+1],'black')]
    return el
def s_turret():
    el=oct_y(8,8,3.6,6,7,'dark')
    el+=[box([3.6,7,6.6],[5,11.3,9.4],'olive'),box([11,7,6.6],[12.4,11.3,9.4],'olive'),box([3.6,7,6],[12.4,7.8,10],'olive')]
    el+=[box([12.4,7.4,6.5],[15.2,10.4,10.5],'olive'),box([12.6,10.4,7],[15,10.8,10],'khaki')]   # ammo can on the side
    return el
def s_gun():
    el=[box([5.5,8.5,4.5],[10.5,11.5,13],'dark'),box([6.5,11.5,5.5],[9.5,12,12],'black'),box([7.4,12,6.5],[8.6,13.2,7.6],'black'),box([7.6,12,10.5],[8.4,12.8,11.3],'black')]
    el+=[box([6,9.2,13],[7,10.6,14.6],'black'),box([9,9.2,13],[10,10.6,14.6],'black'),box([10.5,8.8,7.5],[12.4,10,10.5],'khaki')]   # spade grips, belt feed
    el+=oct_z(8,10,1.6,2,4.5,'steel')+[box([7.4,9.6,4.4],[8.6,10.4,4.6],'red')]
    return el
def s_barrel():
    el=oct_z(8,10,1.35,-6.4,-5.6,'dark')+oct_z(8,10,1.35,-1.2,-.4,'dark')+oct_z(8,10,.45,-6.8,2,'black')
    import math
    for i in range(6):
        a=math.radians(i*60);x=8+math.cos(a)*.9;y=10+math.sin(a)*.9
        el.append(box([x-.27,y-.27,-7],[x+.27,y+.27,2],'steel'))
    return el
# ---------------- flak battery (pivot y=9): platform, armoured carriage, twin long barrels ----------------
def f_base():
    el=[box([0,0,0],[16,2,16],'olive'),box([1,2,1],[15,3,15],'dark')]
    for x,z in ((1.5,1.5),(14.5,1.5),(1.5,14.5),(14.5,14.5)):el+=oct_y(x,z,1.1,0,2.6,'black')
    el+=[box([.5,.8,7],[15.5,1.8,9],'khaki'),box([7,.8,.5],[9,1.8,15.5],'khaki')]
    return el
def f_turret():
    el=oct_y(8,8,6.2,3,4.6,'steel')+oct_y(8,8,4.6,4.6,5.4,'dark')
    el+=[box([.8,5,2.5],[2.4,12.4,13.5],'olive'),box([13.6,5,2.5],[15.2,12.4,13.5],'olive'),   # side shields
         box([.8,11.4,2.5],[2.4,12.4,4],'khaki'),box([13.6,11.4,2.5],[15.2,12.4,4],'khaki'),
         box([2.4,8.2,7.2],[13.6,9.8,8.8],'dark'),                                              # trunnion axle
         box([4,5.4,11],[12,7,14.5],'dark'),box([5,7,13],[11,7.6,15.5],'black')]                 # gunner's seat / footplate
    el+=[box([14.6,10.5,5],[15.6,13.5,8],'dark'),box([14.8,12.5,5.2],[15.8,13.2,7.8],'glass')]  # sight box
    return el
def f_gun():
    el=[box([4,7,4.5],[12,11,13],'olive'),box([3.8,7.4,8.5],[6.6,10.6,14.5],'dark'),box([9.4,7.4,8.5],[12.2,10.6,14.5],'dark')]
    for x in (4.2,9.8):el+=[box([x,10.6,9.5],[x+2,13,13],'brass'),box([x+.2,13,9.8],[x+1.8,13.4,12.7],'gray')]   # 4-round clips on top
    el+=[box([6.8,11,5],[9.2,11.6,12],'black')]
    return el
def f_barrel():
    return oct_z(8,9,.8,-14,5,'dark')+oct_z(8,9,1.15,-16,-13.6,'black')+oct_z(8,9,1.15,1.5,5.5,'steel')+oct_z(8,9,1.25,-5,-4,'steel')
# ---------------- auto grenade launcher (pivot y=9): squat body, drum magazine, fat short barrel ----------------
def g_base():
    el=oct_y(8,8,6,0,2,'gray')+[box([3,2,3],[13,3,13],'dark')]
    for x,z in ((2.3,2.3),(13.7,2.3),(2.3,13.7),(13.7,13.7)):el+=[box([x-1.1,0,z-1.1],[x+1.1,2.4,z+1.1],'black')]
    return el
def g_turret():
    el=oct_y(8,8,4.6,3,4.6,'steel')+[box([3,4.6,6],[4.6,10.6,10.5],'khaki'),box([11.4,4.6,6],[13,10.6,10.5],'khaki'),box([3,4.6,6],[13,5.4,10.5],'khaki')]
    return el
def g_gun():
    el=[box([4.6,7,5],[11.4,11,14],'olive'),box([4.4,10.4,5],[11.6,11.2,13],'khaki')]
    el+=oct_x(5.6,10,2.6,5,11,'dark')+[box([5.2,5.2,9.4],[10.8,6,10.6],'yellow')]   # ammo drum under the gun
    el+=[box([7.5,11.2,7.5],[8.5,12.6,9],'black'),box([7.2,12.2,7.3],[8.8,13,7.7],'black'),box([6.6,8,14],[9.4,10.2,15.8],'black')]
    return el
def g_barrel():
    return oct_z(8,9,1.3,-4,5.2,'dark')+oct_z(8,9,1.65,-5.2,-3.6,'black')+oct_z(8,9,.75,-5.25,-5.1,'gray')
# ---------------- projectiles (nose -z, centred on 8,8) and ammo items ----------------
def flak_proj():return oct_z(8,8,.6,-1.6,2.6,'brass')+oct_z(8,8,.48,-2.8,-1.6,'gray')+oct_z(8,8,.25,-3.4,-2.8,'red')
def gren_proj():return oct_z(8,8,.95,-.8,1.6,'brass')+oct_z(8,8,.95,-2.2,-.8,'olive')+oct_z(8,8,.55,-2.9,-2.2,'olive')+oct_z(8,8,.98,-.9,-.6,'yellow')
def crate():
    el=[box([2,0,4],[14,7.5,12],'olive'),box([1.6,7.5,3.6],[14.4,9,12.4],'khaki'),box([7,4.8,3.55],[9,7.6,4],'black'),
        box([.8,4.6,7],[2,5.6,9],'black'),box([14,4.6,7],[15.2,5.6,9],'black'),box([2,2.4,3.95],[14,3.4,4],'yellow'),box([2,2.4,12],[14,3.4,12.05],'yellow')]
    return el
SENTRY=('sentry_gun',s_base,s_turret,s_gun,s_barrel);FLAK=('flak_battery',f_base,f_turret,f_gun,f_barrel);GREN=('auto_grenade_launcher',g_base,g_turret,g_gun,g_barrel)
D_LAUNCH={"gui":{"rotation":[30,225,0],"scale":[.6,.6,.6]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.375,.375,.375]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,0,0],"scale":[.4,.4,.4]}}
D_AMMO={"gui":{"rotation":[30,225,45],"scale":[2.2,2.2,2.2]},"ground":{"translation":[0,3,0],"scale":[1,1,1]},"fixed":{"rotation":[0,90,0],"scale":[2,2,2]},
 "thirdperson_righthand":{"translation":[0,3,1],"scale":[1,1,1]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,3,0],"scale":[1.2,1.2,1.2]}}
D_CRATE={"gui":{"rotation":[30,225,0],"translation":[0,1,0],"scale":[.8,.8,.8]},"ground":{"translation":[0,3,0],"scale":[.4,.4,.4]},"fixed":{"scale":[.6,.6,.6]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.4,.4,.4]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,2,0],"scale":[.5,.5,.5]}}
for name,b,t,g,br in (SENTRY,FLAK,GREN):
    write('block',name+'_base',b());write('block',name+'_turret',t());write('block',name+'_gun',g());write('block',name+'_barrel',br())
    write('block',name,[],tex={"particle":"mareeletrica:block/dark"})
    bars=moved(br(),-3.2)+moved(br(),3.2) if name=='flak_battery' else br()
    dl=copy.deepcopy(D_LAUNCH)
    if name=='flak_battery':dl['gui']={"rotation":[30,225,0],"translation":[-2.1,1,0],"scale":[.47,.47,.47]}
    write('item',name,b()+t()+g()+bars,dl)
    json.dump({"variants":{"":{"model":"mareeletrica:block/"+name}}},open(os.path.join(A,'blockstates',name+'.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:"+name}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks',name+'.json'),'w'))
write('block','flak_shell_proj',flak_proj());write('block','grenade_round_proj',gren_proj())
write('item','flak_shell',moved(flak_proj(),0,0,8.4),D_AMMO);write('item','grenade_round',moved(gren_proj(),0,0,8.6),D_AMMO);write('item','ammo_crate',crate(),D_CRATE)
# ---------------- recipes ----------------
def shaped(name,pat,key,n=1):
    json.dump({"type":"minecraft:crafting_shaped","category":"equipment","pattern":pat,"key":{k:{"item":v} for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}},open(os.path.join(D,'recipe',name+'.json'),'w'))
shaped('sentry_gun',["BBP","PMP"," I "],{"B":"minecraft:iron_bars","P":"mareeletrica:iron_plate","M":"mareeletrica:control_module","I":"minecraft:iron_block"})
shaped('flak_battery',["B B","PMP","III"],{"B":"minecraft:iron_bars","P":"mareeletrica:iron_plate","M":"mareeletrica:control_module","I":"minecraft:iron_block"})
shaped('auto_grenade_launcher',[" B ","PMP","IDI"],{"B":"minecraft:iron_bars","P":"mareeletrica:iron_plate","M":"mareeletrica:control_module","I":"minecraft:iron_block","D":"minecraft:dispenser"})
shaped('flak_shell',["NGN","GCG"],{"N":"minecraft:iron_nugget","G":"minecraft:gunpowder","C":"minecraft:copper_ingot"},16)
shaped('grenade_round',[" G ","NCN"],{"N":"minecraft:iron_nugget","G":"minecraft:gunpowder","C":"minecraft:copper_ingot"},8)
shaped('ammo_crate',["PPP","PCP"],{"P":"mareeletrica:iron_plate","C":"minecraft:chest"})
# ---------------- lang ----------------
L={'pt_br':{"block.mareeletrica.sentry_gun":"Torre Sentinela","block.mareeletrica.flak_battery":"Bateria Antiaérea","block.mareeletrica.auto_grenade_launcher":"Lança-granadas Automático",
  "item.mareeletrica.flak_shell":"Granada antiaérea","item.mareeletrica.grenade_round":"Granada 40mm","item.mareeletrica.ammo_crate":"Caixa de Munição",
  "entity.mareeletrica.flak_shell":"Granada antiaérea","entity.mareeletrica.grenade_round":"Granada 40mm",
  "hint.mareeletrica.flak_shell":"Munição da Bateria Antiaérea: explode perto do alvo no ar.","hint.mareeletrica.grenade_round":"Munição do Lança-granadas Automático."},
 'en_us':{"block.mareeletrica.sentry_gun":"Sentry Gun","block.mareeletrica.flak_battery":"Flak Battery","block.mareeletrica.auto_grenade_launcher":"Automatic Grenade Launcher",
  "item.mareeletrica.flak_shell":"Flak Shell","item.mareeletrica.grenade_round":"40mm Grenade","item.mareeletrica.ammo_crate":"Ammo Crate",
  "entity.mareeletrica.flak_shell":"Flak Shell","entity.mareeletrica.grenade_round":"40mm Grenade",
  "hint.mareeletrica.flak_shell":"Flak Battery ammo: bursts near airborne targets.","hint.mareeletrica.grenade_round":"Automatic Grenade Launcher ammo."}}
for lang,kv in L.items():
    p=os.path.join(A,'lang',lang+'.json');d=json.load(open(p,encoding='utf-8'));d.update(kv)
    json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.64 ok')
