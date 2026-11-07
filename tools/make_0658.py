"""0.65.8: new skateboard — bigger (1.2 blocks), kicktails, grip tape, graphic underneath, plywood edges, real trucks, round wheels."""
import json,os,random,math
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');T=os.path.join(A,'textures/block')
random.seed(658)
def img(fn,name):
    im=Image.new('RGBA',(16,16))
    for y in range(16):
        for x in range(16):im.putpixel((x,y),fn(x,y))
    im.save(os.path.join(T,name+'.png'))
def n(a=8):return random.randint(-a,a)
img(lambda x,y:(lambda g:(g,g,g+4,255))(38+n(7)) if random.random()>.06 else (70,70,76,255),'skate_grip')
def art(x,y):   # underside graphic: sunset gradient along the board, a white lightning bolt
    t=y/15;r,g,b=int(255*(1-t)+190*t),int(140*(1-t)+40*t),int(30*(1-t)+170*t)
    bolt=[(9,1),(8,2),(8,3),(7,4),(7,5),(6,6),(8,6),(9,6),(8,7),(8,8),(7,9),(7,10),(6,11),(6,12),(5,13),(9,7),(10,6)]
    if (x,y) in bolt:return (255,255,240,255)
    if x in (0,15):return (30,30,40,255)
    return (r,g,b,255)
img(art,'skate_art')
img(lambda x,y:[(214,176,120,255),(200,60,50,255),(214,176,120,255),(40,110,190,255)][(y//2)%4],'skate_ply')
img(lambda x,y:(250,215,120,255) if 3<=x<=12 else (230,190,95,255),'skate_wheel')
TX={"grip":"mareeletrica:block/skate_grip","art":"mareeletrica:block/skate_art","ply":"mareeletrica:block/skate_ply","wheel":"mareeletrica:block/skate_wheel",
    "truck":"minecraft:block/iron_block","base":"minecraft:block/gray_concrete","bolt":"minecraft:block/light_gray_concrete","particle":"mareeletrica:block/skate_art"}
def el(f,t,side,up=None,down=None,rot=None,uv_up=None,uv_down=None):
    for v in f+t:assert -16<=v<=32,(f,t)
    fc={d:{"texture":"#"+side,"uv":[0,0,16,4] if side=='ply' else [0,0,16,16]} for d in ["north","south","west","east"]}
    fc["up"]={"texture":"#"+(up or side),"uv":uv_up or [0,0,16,16]};fc["down"]={"texture":"#"+(down or side),"uv":uv_down or [0,0,16,16]}
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":fc}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2]}
    return e
Y0,Y1=3.2,3.9
B=[el([4.5,Y0,1.5],[11.5,Y1,14.5],'ply','grip','art',uv_up=[0,2,16,14],uv_down=[0,2,16,14])]
# kicktails (tips up): back (+z) needs a negative angle, front (−z) a positive one
for (z0,z1,z2,z3,org,ang,va,vb) in ((14.5,16.8,16.8,17.6,14.5,-22.5,(14,16),(0,0)),(-.8,1.5,-1.6,-.8,1.5,22.5,(0,2),(0,0))):
    o=[8,(Y0+Y1)/2,org]
    B.append(el([4.5,Y0,min(z0,z1)],[11.5,Y1,max(z0,z1)],'ply','grip','art',([o,'x',ang]),uv_up=[0,va[0],16,va[1]],uv_down=[0,va[0],16,va[1]]))
    B.append(el([5.4,Y0,min(z2,z3)],[10.6,Y1,max(z2,z3)],'ply','grip','art',([o,'x',ang])))
for z in (4,12):
    B+= [el([6.3,2.2,z-1.2],[9.7,Y0,z+1.2],'base'),el([5.3,.9,z-.6],[10.7,2.2,z+.6],'truck'),el([3.6,1.1,z-.25],[12.4,1.5,z+.25],'truck'),
         el([7.6,1.8,z-.3],[8.4,2.8,z+.3],'bolt')]
    for bx in (6.8,9.2):
        for bz in (z-.8,z+.8):B.append(el([bx-.25,Y1,bz-.25],[bx+.25,Y1+.05,bz+.25],'bolt'))
    for x0 in (3.4,10.8):
        B.append(el([x0,0,z-1.3],[x0+1.8,2.6,z+1.3],'wheel'))
        B.append(el([x0+.05,.05,z-1.25],[x0+1.75,2.55,z+1.25],'wheel',rot=([x0+.9,1.3,z],'x',45)))
json.dump({"textures":TX,"elements":B},open(os.path.join(A,'models/block/skateboard.json'),'w'),separators=(',',':'))
json.dump({"textures":TX,"elements":B,"display":{"gui":{"rotation":[30,45,0],"translation":[0,3,0],"scale":[1.05,1.05,1.05]},"ground":{"translation":[0,2,0],"scale":[.7,.7,.7]},
  "fixed":{"rotation":[-90,0,0],"scale":[1,1,1]},"thirdperson_righthand":{"rotation":[0,90,-70],"translation":[0,3,2],"scale":[.6,.6,.6]},
  "firstperson_righthand":{"rotation":[0,60,-40],"translation":[2,3,0],"scale":[.6,.6,.6]}}},open(os.path.join(A,'models/item/skateboard.json'),'w'),separators=(',',':'))
print('0.65.8 skate model ok',len(B),'elements')
