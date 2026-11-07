"""Importa um modelo do Blockbench (formato livre: meshes ou cubos) como as partes OBJ de uma arma do arsenal.
Uso: python3 tools/import_gun.py <config.json>
config: {"bbmodel":..., "family":"sniper", "tag":"patinho", "scale":.83, "trigger":[x,y], "muzzle_dir":-1,
         "materials":{"wood":[1,2,...],"black":[...]}, "default":"metal", "stock":[ids sem aparecer na mira], "skins":[0]}
O gatilho (trigger, coordenadas do Blockbench) vai para o ponto onde a mão segura no arsenal (z 8.4, y 6.3 px)."""
import json,math,os,sys,random
import numpy as np
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica')
cfg=json.load(open(sys.argv[1]));src=json.load(open(cfg['bbmodel']))
S=cfg['scale'];SX=S*cfg.get('width',1.0);tx,ty=cfg['trigger'];sgn=cfg['muzzle_dir']
def rot(r):
    x,y,z=[math.radians(a) for a in r]
    Rx=np.array([[1,0,0],[0,math.cos(x),-math.sin(x)],[0,math.sin(x),math.cos(x)]]);Ry=np.array([[math.cos(y),0,math.sin(y)],[0,1,0],[-math.sin(y),0,math.cos(y)]]);Rz=np.array([[math.cos(z),-math.sin(z),0],[math.sin(z),math.cos(z),0],[0,0,1]])
    return Rx@Ry@Rz
# muzzle_dir -1: the muzzle is at -x in Blockbench (→ -z in game). Proper rotation: z' = -sgn·x·…, x' = sgn·z
def to_mod(p):return np.array([8+sgn*p[2]*SX,6.3+(p[1]-ty)*S,8.4+sgn*(p[0]-tx)*S*-1])
mat_of={}
for m,ids in cfg.get('materials',{}).items():
    for i in ids:mat_of[i]=m
SWATCH=cfg.get('swatches')   # for cube models painted with a swatch texture: {"x,y":"material"}
parts={'body':[],'view':[]}
for i,e in enumerate(src['elements']):
    if e.get('visibility') is False:continue
    M=rot(e.get('rotation',[0,0,0]));o=np.array(e.get('origin',[0,0,0]),float)
    if e.get('type','cube')=='mesh':
        V={k:to_mod(M@np.array(v,float)+o) for k,v in e['vertices'].items()}
        quads=[([V[k] for k in f['vertices']],None) for f in e['faces'].values()]
    else:
        f0=np.array(e['from'],float);t0=np.array(e['to'],float)
        c=[np.array([a,b,d]) for a in (f0[0],t0[0]) for b in (f0[1],t0[1]) for d in (f0[2],t0[2])]
        P=lambda k:to_mod(M@(c[k]-o)+o)
        Q={'west':[0,1,3,2],'east':[4,6,7,5],'down':[0,2,6,4],'up':[1,5,7,3],'north':[0,4,5,1],'south':[2,3,7,6]}
        quads=[([P(k) for k in q],e['faces'][d]) for d,q in Q.items() if e['faces'].get(d,{}).get('texture') is not None]
    cen=np.mean([p for q,_ in quads for p in q],axis=0)
    for P,face in quads:
        if len(P)!=4:continue
        m=mat_of.get(i,cfg.get('default','metal'))
        if SWATCH and face:u=face['uv'];m=SWATCH.get(f"{int(u[0]-1)},{int(u[1]-1)}",m)
        n=np.mean(P,axis=0)-cen;nn=np.cross(P[1]-P[0],P[2]-P[0])
        if np.dot(nn,n)<0:P=P[::-1]
        parts['body'].append((P,m))
        if i not in cfg.get('stock',[]):parts['view'].append((P,m))
fam=cfg['family'];tag=cfg['tag'];mats=sorted({m for P,m in parts['body']})
def write_obj(name,faces):
    L=[f'mtllib {fam}_{tag}.mtl'];vt=[];out=[];vi=1;cur=None
    for P,m in sorted(faces,key=lambda t:t[1]):
        a=np.linalg.norm(P[1]-P[0]);b=np.linalg.norm(P[3]-P[0]);u=min(1,a/16*2);v=min(1,b/16*2)
        if m!=cur:out.append('usemtl '+m);cur=m
        for p,(tu,tv) in zip(P,[(0,0),(u,0),(u,v),(0,v)]):L.append('v %.5f %.5f %.5f'%(p[0]/16,p[1]/16,p[2]/16));vt.append('vt %.4f %.4f'%(tu,1-tv))
        out.append('f %d/%d %d/%d %d/%d %d/%d'%(vi,vi,vi+1,vi+1,vi+2,vi+2,vi+3,vi+3));vi+=4
    open(os.path.join(A,'models/block',name+'.obj'),'w').write('\n'.join(L+vt+out)+'\n')
for k,f in parts.items():write_obj(f'armory21_{fam}_{tag}_{k}',f)
open(os.path.join(A,f'models/block/{fam}_{tag}.mtl'),'w').write(''.join(f'newmtl {m}\nmap_Kd #{m}\n\n' for m in mats))
allp=np.array([p for P,_ in parts['body'] for p in P]);print(tag,'bounds px',allp.min(0).round(1),allp.max(0).round(1),'materials',mats)
# ---------- textures ----------
random.seed(hash(tag)&0xffff)
COL={'wood':(128,70,34),'wood_d':(92,48,22),'metal':(48,50,54),'metal_l':(86,90,96),'black':(24,24,26),'green':(74,86,52),'green_d':(52,61,37),
     'lens':(40,90,140),'rubber':(30,30,30),'brass':(176,140,60),'red':(170,30,30),'steel':(120,124,130),'flute':(30,31,34),'tan':(150,130,96)}
for m in mats:
    im=Image.new('RGBA',(16,16));base=COL.get(m,(90,90,90))
    for x in range(16):
        for y in range(16):
            if m.startswith('wood'):g=(math.sin(y*1.2+math.sin(x*.35)*1.6)+1)/2;c=[int(v*(1-g*.28))+random.randint(-5,5) for v in base]
            else:d=random.randint(-6,6);c=[v+d for v in base]
            im.putpixel((x,y),tuple(max(0,min(255,v)) for v in c)+(255,))
    im.save(os.path.join(A,f'textures/block/{fam}_{tag}_{m}.png'))
def paint(name,base,pattern=None):
    im=Image.new('RGBA',(16,16))
    for x in range(16):
        for y in range(16):
            dv=random.randint(-6,6);c=[v+dv for v in base]
            if pattern=='stripes' and (x+y*2)%5<2:c=[v*.25 for v in c]
            if pattern=='dots' and random.random()<.05:c=[235,235,255]
            if pattern=='check' and ((x//2)+(y//2))%2:c=[v*.6 for v in c]
            if pattern=='camo':g=((x//3)*7+(y//3)*13)%3;c=[v*(.75+.2*g) for v in c]
            im.putpixel((x,y),tuple(max(0,min(255,int(v))) for v in c)+(255,))
    im.save(os.path.join(A,f'textures/block/{name}.png'))
for sk in cfg.get('skins',[0]):
    tex={m:f'mareeletrica:block/{fam}_{tag}_{m}' for m in mats};tex['particle']=tex[mats[0]]
    for m,spec in cfg.get('skin_colors',{}).get(str(sk),{}).items():
        if m not in mats:continue
        n=f'{fam}_{tag}_{m}_{sk}';paint(n,spec[:3],spec[3] if len(spec)>3 else None);tex[m]='mareeletrica:block/'+n
    for part in ('body','view'):
        json.dump({"loader":"neoforge:obj","model":f"mareeletrica:models/block/armory21_{fam}_{tag}_{part}.obj","flip_v":True,"textures":tex},open(os.path.join(A,f'models/block/armory21_{fam}_{part}_{sk}.json'),'w'))
    json.dump({"textures":{"particle":tex['particle']},"elements":[]},open(os.path.join(A,f'models/block/armory21_{fam}_moving_{sk}.json'),'w'))
print('ok',tag)
