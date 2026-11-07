"""0.59: B-2 bomber, RPG model + missile, prettier landmine."""
import json,os,random
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica');TB=os.path.join(A,'textures/block');random.seed(9)
def tex(name,base,var=6,lines=None):
    im=Image.new('RGBA',(16,16))
    for y in range(16):
        for x in range(16):
            k=random.randint(-var,var);c=[max(0,min(255,v+k)) for v in base]
            if lines and (x%lines==0 or y%lines==0):c=[int(v*.82) for v in c]
            im.putpixel((x,y),tuple(c)+(255,))
    im.save(os.path.join(TB,name+'.png'))
tex('b2_skin',(70,73,78),5,8);tex('b2_dark',(38,40,44),4);tex('rpg_olive',(84,92,52),8,8);tex('rpg_warhead',(70,80,46),6);tex('mine_body',(78,84,58),7,5);tex('mine_plate',(52,56,44),5,4)
T={"skin":"mareeletrica:block/b2_skin","dark":"mareeletrica:block/b2_dark","glass":"mareeletrica:block/veh_glass","olive":"mareeletrica:block/rpg_olive","war":"mareeletrica:block/rpg_warhead",
   "black":"minecraft:block/black_concrete","metal":"mareeletrica:block/veh_metal","steel":"mareeletrica:block/base_steel","red":"mareeletrica:block/lamp_red","green":"mareeletrica:block/lamp_green",
   "yellow":"mareeletrica:block/lamp_yellow","hazard":"mareeletrica:block/veh_hazard","mine":"mareeletrica:block/mine_body","plate":"mareeletrica:block/mine_plate","white":"minecraft:block/white_concrete","particle":"mareeletrica:block/b2_skin"}
def b(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2]}
    return e
def save(kind,name,els,disp=None):
    m={"textures":T,"elements":els}
    if disp:m["display"]=disp
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
# ---------------- B-2 flying wing (nose toward -z, centre 8,6,8)
B2=[]
# flying wing built from 2-pixel strips: the leading edge runs straight from the nose back to the tips,
# the trailing edge is the B-2's double "W" sawtooth
def te(d):   # trailing edge z at distance d from the centre line
    if d<4:return 19-d*.5
    if d<10:return 17-(d-4)*.9+0
    if d<16:return 11.6+(d-10)*.9
    return 17-(d-16)*1.1
for side in(-1,1):
    for i in range(12):
        d0=i*2;d1=d0+2;dm=(d0+d1)/2
        if side<0:x0,x1=8-d1,8-d0
        else:x0,x1=8+d0,8+d1
        if x0<-16 or x1>32:continue
        z0=-15+dm*1.25;z1=te(dm)
        if z1-z0<1.5:z1=z0+1.5
        th=max(.5,1.6-dm*.06)
        B2.append(b([x0,6-th/2,z0],[x1,6+th/2,z1],'skin'))
B2+=[b([4,4.6,-14],[12,7.6,17],'skin'),b([5,7.6,-10],[11,9.2,6],'skin'),b([6,9.2,-6],[10,9.8,2],'skin'),b([5.6,8,-10.1],[10.4,9,-9.9],'glass'),
     b([1.5,6.6,-4],[4.5,7.8,4],'dark'),b([11.5,6.6,-4],[14.5,7.8,4],'dark'),b([1.7,6.6,-4.2],[4.3,7.6,-4],'black'),b([11.7,6.6,-4.2],[14.3,7.6,-4],'black'),
     b([1.5,6.5,9],[4.5,7.2,15],'dark'),b([11.5,6.5,9],[14.5,7.2,15],'dark'),
     b([5,4.4,-2],[7.8,4.6,8],'dark'),b([8.2,4.4,-2],[11,4.6,8],'dark'),b([-15.5,5.6,4],[-14,6.2,6],'red'),b([30,5.6,4],[31.5,6.2,6],'green')]
save('block','b2_bomber',B2)
save('item','b2_bomber',B2,{"gui":{"rotation":[60,180,0],"translation":[0,0,0],"scale":[.36,.36,.36]},"ground":{"scale":[.2,.2,.2]},"fixed":{"rotation":[90,0,0],"scale":[.36,.36,.36]},
 "thirdperson_righthand":{"rotation":[60,180,0],"translation":[0,3,0],"scale":[.2,.2,.2]},"firstperson_righthand":{"rotation":[30,160,0],"translation":[0,4,0],"scale":[.25,.25,.25]}})
# ---------------- RPG missile (nose toward -z, axis at y=8)
RM=[b([7,7,-2],[9,9,6],'olive'),b([7.3,7.3,-4],[8.7,8.7,-2],'war'),b([7.6,7.6,-5],[8.4,8.4,-4],'war'),b([7.2,7.2,6],[8.8,8.8,7],'black'),
    b([6,7.8,4],[10,8.2,6.5],'olive'),b([7.8,6,4],[8.2,10,6.5],'olive'),b([7.4,7.4,7],[8.6,8.6,7.5],'yellow')]
save('block','rpg_missile',RM)
# ---------------- RPG launcher (held along z, both hands)
L=[b([6.2,7.2,-6],[9.8,10.8,22],'olive'),                                   # tube
   b([5.6,6.6,22],[10.4,11.4,25],'dark'),b([6,7,25],[10,11,26],'black'),     # flared back with the exhaust
   b([5.8,6.8,-7],[10.2,11.2,-5],'dark'),                                    # muzzle ring
   b([6.7,7.7,-12],[9.3,10.3,-7],'war'),b([7.2,8.2,-15],[8.8,9.8,-12],'war'),b([7.6,8.6,-16],[8.4,9.4,-15],'war'),  # loaded warhead
   b([9.8,9.6,2],[12.6,12.2,9],'dark'),b([10,10,1.8],[12.4,11.8,2],'glass'),b([10.2,10.2,9],[12.2,11.6,9.2],'black'),  # optic sight
   b([7.2,3.2,4],[8.8,7.2,6.5],'dark',([8,7.2,5],'x',-22.5)),b([7.2,3.2,12],[8.8,7.2,14.5],'dark',([8,7.2,13],'x',-22.5)),  # two grips
   b([7.4,5.6,6.5],[8.6,6.2,8],'black'),                                     # trigger
   b([5.8,6.4,14],[10.2,7.2,20],'black'),                                    # shoulder pad
   b([6.1,10.8,-4],[9.9,11,0],'hazard'),b([6.1,10.8,17],[9.9,11,21],'hazard')]
H={"gui":{"rotation":[20,-135,0],"translation":[0,0,0],"scale":[.55,.55,.55]},"ground":{"translation":[0,2,0],"scale":[.4,.4,.4]},"fixed":{"rotation":[0,90,0],"scale":[.6,.6,.6]},
 "thirdperson_righthand":{"rotation":[0,0,0],"translation":[0,1.5,1],"scale":[.8,.8,.8]},"thirdperson_lefthand":{"rotation":[0,0,0],"translation":[0,1.5,1],"scale":[.8,.8,.8]},
 "firstperson_righthand":{"rotation":[0,0,0],"translation":[0,0,0],"scale":[.75,.75,.75]},"firstperson_lefthand":{"rotation":[0,0,0],"translation":[0,0,0],"scale":[.75,.75,.75]}}
save('item','missile_launcher_portable',L,H)
# ---------------- prettier landmine: round body, pressure plate with ridges, bolts, arming light
def mine(light):
    els=[b([3,0,4],[13,1.6,12],'mine'),b([4,0,3],[12,1.6,13],'mine'),b([3.5,0,3.5],[12.5,1.6,12.5],'mine'),
         b([5,1.6,5],[11,2.2,11],'plate'),b([5.5,2.2,5.5],[10.5,2.4,10.5],'plate')]
    for i in range(3):els.append(b([5.6,2.4,6+i*1.6],[10.4,2.6,6.6+i*1.6],'black'))
    for (x,z) in((4,7.5),(11.4,7.5),(7.5,4),(7.5,11.4)):els.append(b([x,1.6,z],[x+.6,1.9,z+.6],'steel'))
    els.append(b([11.4,1.6,11.4],[12.2,2.4,12.2],light))
    els.append(b([2.6,0,7.4],[3.2,.8,8.6],'yellow'))
    return els
save('block','landmine_off',mine('green'));save('block','landmine_on',mine('red'))
print('war3 ok')
