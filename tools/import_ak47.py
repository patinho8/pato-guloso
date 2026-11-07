"""0.67.1: the player's own AK-47 (Blockbench free/mesh model) imported as OBJ parts for the AK-47 (body, aim view, magazine),
with procedural textures for the 13 skins. Usage: python3 tools/import_ak47.py <aaaa.bbmodel>"""
import json,math,os,sys,random
import numpy as np
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica')
src=json.load(open(sys.argv[1]))

def rot(r):   # Blockbench mesh rotation (degrees), R = Rx·Ry·Rz
    x,y,z=[math.radians(a) for a in r]
    Rx=np.array([[1,0,0],[0,math.cos(x),-math.sin(x)],[0,math.sin(x),math.cos(x)]])
    Ry=np.array([[math.cos(y),0,math.sin(y)],[0,1,0],[-math.sin(y),0,math.cos(y)]])
    Rz=np.array([[math.cos(z),-math.sin(z),0],[math.sin(z),math.cos(z),0],[0,0,1]])
    return Rx@Ry@Rz
# model frame of the mod's guns: barrel along -z (muzzle near z=-13), centred on x=8, iron-sight line at y≈13.6 (pixels)
# 0.67.2: full size (1 Blockbench px = 1 px), anchored so the pistol grip sits where the hand holds the old AK (grip top at z 10.5, y 6.5)
S=1.0;SX=1.4
def to_mod(p):return np.array([8+p[2]*SX,9.0+(p[1]-25)*S,10.5-(p[0]+9.5)*S])
WOOD={11,12,15,36,37};MAG={1,16,17,18,19};STOCK={11,12};SKIP={0}
parts={'body':[],'view':[],'magazine':[]}
for i,e in enumerate(src['elements']):
    if i in SKIP or e.get('visibility',True) is False and i==0:continue
    M=rot(e.get('rotation',[0,0,0]));o=np.array(e.get('origin',[0,0,0]),float)
    V={k:to_mod(M@np.array(v,float)+o) for k,v in e['vertices'].items()}
    cen=np.mean(list(V.values()),axis=0)
    mat='wood' if i in WOOD else 'mag' if i in MAG else 'metal'
    faces=[]
    for f in e['faces'].values():
        P=[V[k] for k in f['vertices']]
        if len(P)!=4:continue
        fc=np.mean(P,axis=0);n=fc-cen
        nn=np.cross(P[1]-P[0],P[2]-P[0])
        if np.dot(nn,n)<0:P=P[::-1]
        faces.append((P,mat))
    if i in MAG:parts['magazine']+=faces
    else:
        parts['body']+=faces
        if i not in STOCK:parts['view']+=faces
def write_obj(name,faces):
    L=['mtllib armory21_ak47_new.mtl']
    vt=[];vi=1;out=[];cur=None
    for P,mat in faces:
        a=np.linalg.norm(P[1]-P[0]);b=np.linalg.norm(P[3]-P[0])
        u=min(1,a/16*2.2);v=min(1,b/16*2.2)   # texel density a bit above 1 px
        if mat!=cur:out.append('usemtl '+mat);cur=mat
        for p,(tu,tv) in zip(P,[(0,0),(u,0),(u,v),(0,v)]):
            L.append('v %.5f %.5f %.5f'%(p[0]/16,p[1]/16,p[2]/16));vt.append('vt %.4f %.4f'%(tu,1-tv))
        out.append('f %d/%d %d/%d %d/%d %d/%d'%(vi,vi,vi+1,vi+1,vi+2,vi+2,vi+3,vi+3));vi+=4
    open(os.path.join(A,'models/block',name+'.obj'),'w').write('\n'.join(L+vt+out)+'\n')
for k,f in parts.items():write_obj('armory21_ak47_new_'+k,f)
open(os.path.join(A,'models/block/armory21_ak47_new.mtl'),'w').write(''.join(f'newmtl {m}\nmap_Kd #{m}\n\n' for m in ('metal','wood','mag')))
allp=np.array([p for P,_ in parts['body'] for p in P]);print('body bounds px',allp.min(0).round(2),allp.max(0).round(2))
mp=np.array([p for P,_ in parts['magazine'] for p in P]);print('mag bounds px',mp.min(0).round(2),mp.max(0).round(2))

# ---------------- skins: metal / furniture / magazine textures ----------------
random.seed(47)
def noise(base,amp=10):
    im=Image.new('RGBA',(16,16))
    for x in range(16):
        for y in range(16):
            d=random.randint(-amp,amp);im.putpixel((x,y),tuple(max(0,min(255,c+d)) for c in base)+(255,))
    return im
def wood(base,dark):
    im=Image.new('RGBA',(16,16))
    for x in range(16):
        for y in range(16):
            g=(math.sin(y*1.3+math.sin(x*.4)*1.5)+1)/2
            c=[int(base[i]*(1-g*.35)+dark[i]*g*.35)+random.randint(-5,5) for i in range(3)]
            im.putpixel((x,y),tuple(max(0,min(255,v)) for v in c)+(255,))
    return im
def stripes(base,stripe,period=5,width=2,tilt=1):
    im=noise(base,8)
    for x in range(16):
        for y in range(16):
            if (x+y*tilt)%period<width:im.putpixel((x,y),tuple(stripe)+(255,))
    return im
def checker(a,b):
    im=Image.new('RGBA',(16,16))
    for x in range(16):
        for y in range(16):im.putpixel((x,y),tuple(a if ((x//2)+(y//2))%2 else b)+(255,))
    return im
def camo(cols,cell=3):
    im=Image.new('RGBA',(16,16))
    grid={}
    for x in range(16):
        for y in range(16):
            k=(x//cell,y//cell)
            if k not in grid:grid[k]=random.choice(cols)
            im.putpixel((x,y),tuple(grid[k])+(255,))
    return im
def dots(base,dot,n=10):
    im=noise(base,6)
    for _ in range(n):im.putpixel((random.randrange(16),random.randrange(16)),tuple(dot)+(255,))
    return im
def hexes(base,line):
    im=noise(base,4)
    for x in range(16):
        for y in range(16):
            if y%5==0 and (x//3)%2==0 or (x%6==0 and y%5!=0):im.putpixel((x,y),tuple(line)+(255,))
    return im
def damask(base,line):
    im=noise(base,6)
    for x in range(16):
        for y in range(16):
            if abs(math.sin(x*.7+y*.25)*3-(y%6-3))<.7:im.putpixel((x,y),tuple(line)+(255,))
    return im
GUN=(46,48,52)
SK={
 0:(noise(GUN),wood((150,76,34),(70,30,10)),noise((58,60,64))),
 1:(noise((214,172,70),14),wood((110,52,24),(40,15,5)),noise((214,172,70),14)),
 2:(noise((20,38,84)),stripes((25,105,205),(12,53,120),6,2),stripes((25,105,205),(12,53,120),6,2)),
 3:(noise((215,228,232),6),noise((160,205,228),8),noise((215,228,232),6)),
 4:(noise((50,30,36)),noise((200,44,40),12),noise((96,40,47))),
 5:(noise((55,65,77)),dots((85,72,140),(230,230,255)),dots((55,65,77),(160,190,255))),
 6:(noise((80,82,64)),noise((192,162,104),10),noise((150,128,84))),
 7:(noise((30,28,28)),stripes((228,128,32),(25,20,18),5,2,2),stripes((228,128,32),(25,20,18),5,2,2)),
 8:(camo([(84,97,110),(57,67,79),(102,116,70)]),camo([(102,116,70),(84,97,110),(130,140,96)]),camo([(84,97,110),(57,67,79),(102,116,70)])),
 9:(checker((44,45,49),(26,27,30)),checker((44,45,49),(26,27,30)),checker((44,45,49),(26,27,30))),
 10:(damask((88,90,96),(150,152,160)),wood((90,46,22),(30,12,4)),damask((70,72,78),(130,132,140))),
 11:(hexes((26,31,37),(40,230,240)),hexes((26,31,37),(240,60,220)),hexes((26,31,37),(40,230,240))),
 12:(dots((33,8,79),(255,255,255),14),dots((60,10,110),(209,61,240),12),dots((33,8,79),(255,255,255),14)),
}
for i,(m,w,g) in SK.items():
    for n,im in (('metal',m),('wood',w),('mag',g)):im.save(os.path.join(A,f'textures/block/ak47_new_{n}_{i}.png'))
    tex={n:f'mareeletrica:block/ak47_new_{n}_{i}' for n in ('metal','wood','mag')};tex['particle']=tex['metal']
    for part in ('body','view','magazine'):
        json.dump({"loader":"neoforge:obj","model":f"mareeletrica:models/block/armory21_ak47_new_{part}.obj","flip_v":True,"textures":tex},
                  open(os.path.join(A,f'models/block/armory21_ak47_{part}_{i}.json'),'w'))
    # the new model has no separate moving bolt
    json.dump({"textures":{"particle":tex['metal']},"elements":[]},open(os.path.join(A,f'models/block/armory21_ak47_moving_{i}.json'),'w'))
print('AK-47 imported')
