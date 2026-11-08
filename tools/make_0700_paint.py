"""0.70.0: gun painting — overlay models per paintable region (same geometry as the gun, re-UV'd onto shared paint textures,
tinted by the renderer), the paint/pattern/sticker textures, the paint bench, sprays, recipes and lang. Writes assets/mareeletrica/paint_index.txt."""
import json,os,glob,math,random,re,sys
from PIL import Image,ImageDraw,ImageFilter
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
from paint_regions import java_region,obj_region
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
MB=os.path.join(A,'models/block');TB=os.path.join(A,'textures/block');TI=os.path.join(A,'textures/item')
for f in glob.glob(os.path.join(MB,'armory21p_*'))+glob.glob(os.path.join(MB,'paint_*.obj')):os.remove(f)
def save(path,obj):os.makedirs(os.path.dirname(path),exist_ok=True);json.dump(obj,open(path,'w'),separators=(',',':'))
random.seed(700)
VAR=['s','1','2','3']   # solid base + 3 patterns (stripes, dots, camo) drawn on top in the second colour
TEX={'s':'mareeletrica:block/paint_base','1':'mareeletrica:block/paint_stripes','2':'mareeletrica:block/paint_dots','3':'mareeletrica:block/paint_camo'}
index=[]
# ---------------- textures (64x64, 4 px per model unit) ----------------
N=64
base=Image.new('RGBA',(N,N))
for x in range(N):
    for y in range(N):v=232+random.randint(-7,7);base.putpixel((x,y),(v,v,v,255))
base.save(os.path.join(TB,'paint_base.png'))
def mask(fn,name):
    im=Image.new('RGBA',(N,N),(0,0,0,0))
    for x in range(N):
        for y in range(N):
            if fn(x,y):v=240+random.randint(-6,6);im.putpixel((x,y),(v,v,v,255))
    im.save(os.path.join(TB,name+'.png'))
mask(lambda x,y:((x+y)//6)%2==0,'paint_stripes')
def dot(x,y):
    gx,gy=x%16,y%16;ox=8 if (y//16)%2 else 0
    return any(((x-cx-ox)%N-0)**2+0 for cx in [0]) and ((x-ox)%16-8)**2+(gy-8)**2<=12
mask(dot,'paint_dots')
# tileable camo: random blobs on a torus
blobs=[(random.uniform(0,N),random.uniform(0,N),random.uniform(5,11)) for _ in range(16)]
def camo(x,y):
    s=0
    for bx,by,r in blobs:
        dx=min(abs(x-bx),N-abs(x-bx));dy=min(abs(y-by),N-abs(y-by));s+=max(0,1-(dx*dx+dy*dy)/(r*r))
    return s>.45
mask(camo,'paint_camo')
# stickers 16x16
S={
 'skull':["................","....XXXXXXXX....","...XXXXXXXXXX...","..XXXXXXXXXXXX..","..XXXXXXXXXXXX..","..XX...XX...XX..","..XX...XX...XX..","..XXXXXXXXXXXX..","...XXXXX.XXXX...","....XXXXXXXX....","....X.X.X.X.....","....XXXXXXXX....",".....X.X.X.X....","................","................","................"],
 'duck':["................","......YYYY......",".....YYYYYY.....",".....YYKYYYOO...",".....YYYYYYOOO..","......YYYY......",".......YYY......","..YYYYYYYYYYY...",".YYYYYYYYYYYYY..",".YYYYYYYWWYYYY..",".YYYYYYYYWWYYY..","..YYYYYYYYYYY...","...YYYYYYYYY....",".....OO..OO.....","................","................"],
 'bolt':["................","........YYYYY...",".......YYYYY....","......YYYYY.....",".....YYYYY......","....YYYYYYYYY...","...YYYYYYYYY....","........YYY.....",".......YYY......","......YYY.......",".....YYY........","....YY..........","...Y............","................","................","................"],
 'star':["................",".......YY.......",".......YY.......","......YYYY......","......YYYY......",".YYYYYYYYYYYYYY.","..YYYYYYYYYYYY..","...YYYYYYYYYY...","....YYYYYYYY....","....YYYYYYYY....","...YYYYYYYYYY...","...YYYY..YYYY...","..YYY......YYY..","..Y..........Y..","................","................"],
 'flame':["................",".......R........","......RR........","......RRR..R....",".....RRRR.RR....","....RRRORRRR....","....RROORRRR..R.","...RRROOORRR.RR.","...RROOYOORRRRR.","..RRROYYYOORRR..","..RROYYYYYORRR..","..RROYYYYYOORR..","...RROYYYOORR...","....RRROOORR....","......RRRR......","................"],
 'heart':["................","................","..RRRR....RRRR..",".RRRRRR..RRRRRR.",".RRWRRRRRRRRRRR.",".RRRRRRRRRRRRRR.",".RRRRRRRRRRRRRR.","..RRRRRRRRRRRR..","...RRRRRRRRRR...","....RRRRRRRR....",".....RRRRRR.....","......RRRR......",".......RR.......","................","................","................"]}
PAL={'X':(240,240,235,255),'Y':(255,214,40,255),'K':(20,20,20,255),'O':(255,140,20,255),'W':(255,255,255,255),'R':(225,35,40,255)}
STICKERS=['skull','duck','bolt','star','flame','heart']
for n,rows in S.items():
    im=Image.new('RGBA',(16,16),(0,0,0,0))
    for y,row in enumerate(rows):
        for x,c in enumerate(row):
            if c in PAL:im.putpixel((x,y),PAL[c])
    # dark outline so it reads on any paint
    o=Image.new('RGBA',(16,16),(0,0,0,0))
    for x in range(16):
        for y in range(16):
            if im.getpixel((x,y))[3]==0 and any(0<=x+dx<16 and 0<=y+dy<16 and im.getpixel((x+dx,y+dy))[3] for dx,dy in((1,0),(-1,0),(0,1),(0,-1))):o.putpixel((x,y),(25,25,25,255))
    o.alpha_composite(im);os.makedirs(os.path.join(A,'textures/sticker'),exist_ok=True);o.save(os.path.join(A,'textures/sticker',n+'.png'))
# ---------------- java (element) guns ----------------
def fit(a,b):
    a,b=min(a,b),max(a,b)
    if b-a>=16:return 0,16
    a,b=a%16,a%16+(b-a)
    if b>16:return 16-(b-a),16
    return a,b
def uv(f,t,d):
    x=fit(f[0],t[0]);y=fit(16-t[1],16-f[1]);z=fit(f[2],t[2])
    return [round(v,3) for v in ({"up":(x[0],z[0],x[1],z[1]),"down":(x[0],z[0],x[1],z[1]),"north":(x[0],y[0],x[1],y[1]),"south":(x[0],y[0],x[1],y[1])}.get(d) or (z[0],y[0],z[1],y[1]))]
def resolve(name):
    p=os.path.join(MB,name+'.json')
    if not os.path.exists(p):return None
    m=json.load(open(p))
    if 'elements' in m:return m
    if 'parent' in m:
        par=resolve(m['parent'].split('/')[-1]);
        if par is None:return None
        if 'loader' in par:return par
        return par
    return m
def emit(fam,key,regions):
    """regions: {r:[elements]} -> armory21p_<fam>_<key>_<r>_<variant>"""
    for r,els in regions.items():
        if not els:continue
        nm=f'armory21p_{fam}_{key}_{r}'
        save(os.path.join(MB,nm+'_s.json'),{"ambientocclusion":False,"textures":{"paint":TEX['s'],"particle":TEX['s']},"elements":els})
        for v in VAR[1:]:save(os.path.join(MB,f'{nm}_{v}.json'),{"parent":"mareeletrica:block/"+nm+"_s","textures":{"paint":TEX[v],"particle":TEX['s']}})
        index.append(f'{fam} {key} {r}')
def repaint(e):
    f,t=e['from'],e['to'];n={k:v for k,v in e.items() if k in('from','to','rotation','name')}
    n['faces']={d:{"texture":"#paint","uv":uv(f,t,d),"tintindex":0} for d in e.get('faces',{})}
    return n
decals={}
fams=sorted({os.path.basename(p)[9:-12] for p in glob.glob(os.path.join(MB,'armory21_*_body_0.json'))})
for fam in fams:
    m=resolve(f'armory21_{fam}_body_0')
    if m is None:continue
    for key in ('body','view'):
        if 'loader' in m:break      # imported guns: body/view done from the OBJ below
        mm=resolve(f'armory21_{fam}_{key}_0')
        if not mm or not mm.get('elements'):continue
        reg={0:[],1:[],2:[]}
        for e in mm['elements']:reg[java_region(e.get('name',''))].append(repaint(e))
        emit(fam,key,reg)
        if key=='body':
            rec=[e for e in mm['elements'] if java_region(e.get('name',''))==0 and not e.get('rotation')]
            rec=max(rec,key=lambda e:(e['to'][1]-e['from'][1])*(e['to'][2]-e['from'][2]))
            h=rec['to'][1]-rec['from'][1];L=rec['to'][2]-rec['from'][2]
            decals[fam]=(rec['from'][0],rec['to'][0],(rec['from'][1]+rec['to'][1])/2,(rec['from'][2]+rec['to'][2])/2,round(min(3.2,h*.8,L*.8),2))
    for key,r in (('moving',3),('magazine',4),('optic_1',5),('optic_2',5),('optic_3',5)):
        mm=resolve(f'armory21_{fam}_{key}_0' if not key.startswith('optic') else f'armory21_{fam}_{key}')
        if not mm or not mm.get('elements'):continue
        els=[repaint(e) for e in mm['elements'] if not re.search('lens|glass|reticle',e.get('name',''))]
        emit(fam,key,{r:els})
# ---------------- imported OBJ guns ----------------
def load(fn):
    V=[];F=[];mat=None
    for l in open(fn):
        p=l.split()
        if not p:continue
        if p[0]=='v':V.append(tuple(map(float,p[1:4])))
        elif p[0]=='usemtl':mat=p[1]
        elif p[0]=='f':F.append((mat,[int(x.split('/')[0])-1 for x in p[1:]]))
    return V,F
open(os.path.join(MB,'paint.mtl'),'w').write('newmtl paint\nmap_Kd #paint\nforge_TintIndex 0\n')
def obj_emit(tag,key,objname,force=None):
    V,F=load(os.path.join(MB,objname+'.obj'))
    groups={}
    for mat,idx in F:
        pts=[V[i] for i in idx];z=sum(p[2] for p in pts)/len(pts)*16;y=sum(p[1] for p in pts)/len(pts)*16
        if mat in('lens','red'):continue
        r=force if force is not None else obj_region(tag,mat,z,y)
        groups.setdefault(r,[]).append(pts)
    for r,faces in groups.items():
        L=['mtllib paint.mtl','usemtl paint'];vt=[];out=[];n=0
        for pts in faces:
            # planar UV along the face's dominant axis, 4 px per unit on a 64 px texture (wraps)
            ax=[max(p[k] for p in pts)-min(p[k] for p in pts) for k in range(3)];drop=ax.index(min(ax))
            u_ax,v_ax=[k for k in range(3) if k!=drop]
            fi=[]
            def span(k):
                lo=min(p[k] for p in pts);hi=max(p[k] for p in pts);sh=math.floor(lo);sc=1/(hi-lo) if hi-lo>1 else 1
                return lambda q:(q-lo)*sc if sc!=1 else q-sh
            fu,fv=span(u_ax),span(v_ax)
            for p in pts:
                n+=1;L.append('v %.6f %.6f %.6f'%p);vt.append('vt %.5f %.5f'%(min(1,max(0,fu(p[u_ax]))),min(1,max(0,fv(p[v_ax])))));fi.append(f'{n}/{n}')
            out.append('f '+' '.join(fi))
        nm=f'paint_{tag}_{key}_{r}'
        open(os.path.join(MB,nm+'.obj'),'w').write('\n'.join(L+vt+out)+'\n')
        for v in VAR:
            save(os.path.join(MB,f'armory21p_{tag}_{key}_{r}_{v}.json'),{"loader":"neoforge:obj","model":f"mareeletrica:models/block/{nm}.obj","flip_v":True,"textures":{"paint":TEX[v],"particle":TEX['s']}})
        index.append(f'{tag} {key} {r}')
    # sticker spot: centre of the biggest side face of the body
    if key=='body':
        best=None
        for r,faces in groups.items():
            if r!=0:continue
            for pts in faces:
                xs=[p[0] for p in pts]
                if max(xs)-min(xs)>1e-4:continue
                ys=[p[1]*16 for p in pts];zs=[p[2]*16 for p in pts];a=(max(ys)-min(ys))*(max(zs)-min(zs))
                if best is None or a>best[0]:best=(a,(min(ys)+max(ys))/2,(min(zs)+max(zs))/2,max(ys)-min(ys),max(zs)-min(zs))
        _,yc,zc,h,L=best
        allx=[p[0]*16 for pts in groups.get(0,[]) if min(q[1] for q in pts)*16<=yc<=max(q[1] for q in pts)*16 and min(q[2] for q in pts)*16<=zc<=max(q[2] for q in pts)*16 for p in pts]
        decals[tag]=(min(allx),max(allx),yc,zc,round(min(3.2,h*.8,L*.8),2))
obj_emit('ak47','body','armory21_ak47_new_body');obj_emit('ak47','view','armory21_ak47_new_view');obj_emit('ak47','magazine','armory21_ak47_new_magazine',force=4)
obj_emit('sniper','body','armory21_sniper_claude_body');obj_emit('sniper','view','armory21_sniper_claude_view')
obj_emit('m4a1','body','armory21_m4a1_claude_body');obj_emit('m4a1','view','armory21_m4a1_claude_view')
obj_emit('m4a1','moving','armory21_m4a1_claude_moving',force=3);obj_emit('m4a1','magazine','armory21_m4a1_claude_magazine',force=4)
obj_emit('desert_eagle','body','armory21_desert_eagle_claude_body');obj_emit('desert_eagle','view','armory21_desert_eagle_claude_view')
obj_emit('desert_eagle','moving','armory21_desert_eagle_claude_moving',force=3);obj_emit('desert_eagle','magazine','armory21_desert_eagle_claude_magazine',force=4)
obj_emit('mp5','body','armory21_mp5_forge_body');obj_emit('mp5','view','armory21_mp5_forge_view')
obj_emit('mp5','moving','armory21_mp5_forge_moving',force=3);obj_emit('mp5','magazine','armory21_mp5_forge_magazine',force=4)
obj_emit('glock','body','armory21_glock_forge_body');obj_emit('glock','view','armory21_glock_forge_view')
obj_emit('glock','moving','armory21_glock_forge_moving',force=3);obj_emit('glock','magazine','armory21_glock_forge_magazine',force=4)
obj_emit('usp','body','armory21_usp_forge_body');obj_emit('usp','view','armory21_usp_forge_view')
obj_emit('usp','moving','armory21_usp_forge_moving',force=3);obj_emit('usp','magazine','armory21_usp_forge_magazine',force=4)
obj_emit('beretta','body','armory21_beretta_forge_body');obj_emit('beretta','view','armory21_beretta_forge_view')
obj_emit('beretta','moving','armory21_beretta_forge_moving',force=3);obj_emit('beretta','magazine','armory21_beretta_forge_magazine',force=4)
obj_emit('ump45','body','armory21_ump45_forge_body');obj_emit('ump45','view','armory21_ump45_forge_view')
obj_emit('ump45','moving','armory21_ump45_forge_moving',force=3);obj_emit('ump45','magazine','armory21_ump45_forge_magazine',force=4)
obj_emit('scar','body','armory21_scar_forge_body');obj_emit('scar','view','armory21_scar_forge_view')
obj_emit('scar','moving','armory21_scar_forge_moving',force=3);obj_emit('scar','magazine','armory21_scar_forge_magazine',force=4)
obj_emit('mp40','body','armory21_mp40_forge_body');obj_emit('mp40','view','armory21_mp40_forge_view')
obj_emit('mp40','moving','armory21_mp40_forge_moving',force=3);obj_emit('mp40','magazine','armory21_mp40_forge_magazine',force=4)
obj_emit('uzi','body','armory21_uzi_forge_body');obj_emit('uzi','view','armory21_uzi_forge_view')
obj_emit('uzi','moving','armory21_uzi_forge_moving',force=3);obj_emit('uzi','magazine','armory21_uzi_forge_magazine',force=4)
obj_emit('mac10','body','armory21_mac10_forge_body');obj_emit('mac10','view','armory21_mac10_forge_view')
obj_emit('mac10','moving','armory21_mac10_forge_moving',force=3);obj_emit('mac10','magazine','armory21_mac10_forge_magazine',force=4)
obj_emit('p90','body','armory21_p90_forge_body');obj_emit('p90','view','armory21_p90_forge_view')
obj_emit('p90','moving','armory21_p90_forge_moving',force=3);obj_emit('p90','magazine','armory21_p90_forge_magazine',force=4)
obj_emit('aug','body','armory21_aug_forge_body');obj_emit('aug','view','armory21_aug_forge_view')
obj_emit('aug','moving','armory21_aug_forge_moving',force=3);obj_emit('aug','magazine','armory21_aug_forge_magazine',force=4)
obj_emit('famas','body','armory21_famas_forge_body');obj_emit('famas','view','armory21_famas_forge_view')
obj_emit('famas','moving','armory21_famas_forge_moving',force=3);obj_emit('famas','magazine','armory21_famas_forge_magazine',force=4)
obj_emit('galil','body','armory21_galil_forge_body');obj_emit('galil','view','armory21_galil_forge_view')
obj_emit('galil','moving','armory21_galil_forge_moving',force=3);obj_emit('galil','magazine','armory21_galil_forge_magazine',force=4)
obj_emit('svd','body','armory21_svd_forge_body');obj_emit('svd','view','armory21_svd_forge_view')
obj_emit('svd','moving','armory21_svd_forge_moving',force=3);obj_emit('svd','magazine','armory21_svd_forge_magazine',force=4)
obj_emit('saiga12','body','armory21_saiga12_forge_body');obj_emit('saiga12','view','armory21_saiga12_forge_view')
obj_emit('saiga12','moving','armory21_saiga12_forge_moving',force=3);obj_emit('saiga12','magazine','armory21_saiga12_forge_magazine',force=4)
obj_emit('m870','body','armory21_m870_forge_body');obj_emit('m870','view','armory21_m870_forge_view')
obj_emit('m870','moving','armory21_m870_forge_moving',force=3);obj_emit('m870','magazine','armory21_m870_forge_magazine',force=4)
obj_emit('spas12','body','armory21_spas12_forge_body');obj_emit('spas12','view','armory21_spas12_forge_view')
obj_emit('spas12','moving','armory21_spas12_forge_moving',force=3);obj_emit('spas12','magazine','armory21_spas12_forge_magazine',force=4)
obj_emit('m249','body','armory21_m249_forge_body');obj_emit('m249','view','armory21_m249_forge_view')
obj_emit('m249','moving','armory21_m249_forge_moving',force=3);obj_emit('m249','magazine','armory21_m249_forge_magazine',force=4)
obj_emit('barrett','body','armory21_barrett_forge_body');obj_emit('barrett','view','armory21_barrett_forge_view')
obj_emit('barrett','moving','armory21_barrett_forge_moving',force=3);obj_emit('barrett','magazine','armory21_barrett_forge_magazine',force=4)
obj_emit('smg','body','armory21_smg_forge_body');obj_emit('smg','view','armory21_smg_forge_view')
obj_emit('smg','moving','armory21_smg_forge_moving',force=3);obj_emit('smg','magazine','armory21_smg_forge_magazine',force=4)
obj_emit('python','body','armory21_python_forge_body');obj_emit('python','view','armory21_python_forge_view')
obj_emit('python','moving','armory21_python_forge_moving',force=3);obj_emit('python','magazine','armory21_python_forge_magazine',force=4)
obj_emit('sniperh','body','armory21_sniper_patinho_body');obj_emit('sniperh','view','armory21_sniper_patinho_view')
for fam in fams:
    if fam in('ak47','sniper'):continue
for k,(x0,x1,y,z,s) in decals.items():index.append(f'decal {k} {x0:.3f} {x1:.3f} {y:.3f} {z:.3f} {s}')
open(os.path.join(A,'paint_index.txt'),'w').write('\n'.join(index)+'\n')
print(len(index),'index lines',len([l for l in index if not l.startswith('decal')])*4,'models')
# ---------------- paint bench, sprays, recipes, lang ----------------
V=lambda n:"minecraft:block/"+n
def el(f,t,tex,faces=None):
    return {"from":f,"to":t,"faces":{d:{"texture":"#"+tex,"uv":uv(f,t,d)} for d in (faces or ["north","south","west","east","up","down"])}}
els=[el([0,12,0],[16,14,16],'top'),el([1,0,1],[3,12,3],'leg'),el([13,0,1],[15,12,3],'leg'),el([1,0,13],[3,12,15],'leg'),el([13,0,13],[15,12,15],'leg'),
     el([2,7,1.5],[14,11.5,14],'side'),el([3,8,1],[13,10.5,1.5],'drawer'),el([7,9,.5],[9,9.6,1],'knob'),
     el([1,14,14],[15,24,15.5],'board'),el([1,23.5,13.6],[15,24.5,15.9],'leg')]
# pegboard hooks with a hanging gun silhouette and spray cans on the top
els+=[el([3,19,13.4],[12,20.2,14],'knob'),el([4,17.5,13.4],[5.4,19,14],'knob'),el([10.5,18.2,13.4],[11.5,19.2,14],'knob')]
for i,(x,z,c) in enumerate([(2.5,4,'r'),(4.6,3,'b'),(6.7,4.5,'y'),(11,9,'g')]):
    els+=[el([x,14,z],[x+1.8,18,z+1.8],c),el([x+.4,18,z+.4],[x+1.4,19,z+1.4],'cap')]
els+=[el([9,14,3],[14,14.6,7],'cloth')]
save(os.path.join(MB,'paint_bench.json'),{"parent":"minecraft:block/block","textures":{"top":V("spruce_planks"),"leg":V("dark_oak_planks"),"side":V("spruce_planks"),"drawer":V("stripped_spruce_log"),
  "knob":V("iron_block"),"board":V("birch_planks"),"r":V("red_concrete"),"b":V("blue_concrete"),"y":V("yellow_concrete"),"g":V("lime_concrete"),"cap":V("white_concrete"),"cloth":V("white_wool"),"particle":V("spruce_planks")},"elements":els})
save(os.path.join(A,'models/item/paint_bench.json'),{"parent":"mareeletrica:block/paint_bench"})
save(os.path.join(A,'blockstates/paint_bench.json'),{"variants":{f"facing={f}":{"model":"mareeletrica:block/paint_bench",**({"y":r} if r else {})} for f,r in {"north":0,"east":90,"south":180,"west":270}.items()}})
save(os.path.join(D,'loot_table/blocks/paint_bench.json'),{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:paint_bench"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
def can(name,body,band=None,rainbow=False):
    im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
    d.rectangle([5,5,10,14],fill=body);d.line([5,5,5,14],fill=tuple(min(255,c+50) for c in body[:3])+(255,));d.line([10,5,10,14],fill=tuple(max(0,c-60) for c in body[:3])+(255,))
    if rainbow:
        for i,c in enumerate([(230,40,40),(240,150,30),(240,220,40),(60,190,70),(50,120,230),(150,60,200)]):d.line([6,6+i+2,9,6+i+2],fill=c+(255,))
    if band:d.rectangle([6,8,9,10],fill=band)
    d.rectangle([6,3,9,4],fill=(200,200,205,255));d.rectangle([7,1,8,2],fill=(60,60,60,255));d.point((9,1),fill=(60,60,60,255))
    d.line([5,14,10,14],fill=(70,70,75,255))
    im.save(os.path.join(TI,name+'.png'))
    save(os.path.join(A,'models/item',name+'.json'),{"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/"+name}})
can('spray_paint',(220,50,50,255),(250,250,250,255))
can('spray_metallic',(170,175,185,255),(240,240,245,255))
can('spray_glow',(120,240,90,255),(255,255,170,255))
can('spray_rainbow',(240,240,240,255),rainbow=True)
im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
d.rectangle([5,6,10,14],fill=(150,200,230,190));d.rectangle([6,9,9,13],fill=(200,240,255,220));d.rectangle([6,3,9,5],fill=(140,100,60,255));d.rectangle([5,6,10,7],fill=(230,240,250,230))
im.save(os.path.join(TI,'paint_solvent.png'));save(os.path.join(A,'models/item/paint_solvent.json'),{"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/paint_solvent"}})
def shapeless(name,items):save(os.path.join(D,'recipe',name+'.json'),{"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":i} for i in items],"result":{"id":"mareeletrica:"+name,"count":1}})
save(os.path.join(D,'recipe/paint_bench.json'),{"type":"minecraft:crafting_shaped","category":"misc","pattern":["RYB","PPP","P P"],"key":{"R":{"item":"minecraft:red_dye"},"Y":{"item":"minecraft:yellow_dye"},"B":{"item":"minecraft:blue_dye"},"P":{"tag":"minecraft:planks"}},"result":{"id":"mareeletrica:paint_bench","count":1}})
shapeless('spray_paint',['minecraft:iron_ingot','minecraft:red_dye','minecraft:yellow_dye','minecraft:blue_dye'])
shapeless('spray_metallic',['minecraft:iron_ingot','minecraft:gold_ingot','minecraft:light_gray_dye'])
shapeless('spray_glow',['minecraft:iron_ingot','minecraft:glowstone_dust','minecraft:glow_ink_sac'])
shapeless('spray_rainbow',['minecraft:iron_ingot','minecraft:red_dye','minecraft:lime_dye','minecraft:blue_dye','minecraft:amethyst_shard'])
shapeless('paint_solvent',['minecraft:glass_bottle','minecraft:sugar','minecraft:bone_meal'])
LANG={'pt_br':{"block.mareeletrica.paint_bench":"Bancada de Pintura","item.mareeletrica.spray_paint":"Spray de Tinta","item.mareeletrica.spray_metallic":"Spray Metálico",
  "item.mareeletrica.spray_glow":"Spray Brilhante","item.mareeletrica.spray_rainbow":"Spray Arco-íris","item.mareeletrica.paint_solvent":"Solvente"},
 'en_us':{"block.mareeletrica.paint_bench":"Paint Bench","item.mareeletrica.spray_paint":"Spray Paint","item.mareeletrica.spray_metallic":"Metallic Spray",
  "item.mareeletrica.spray_glow":"Glow Spray","item.mareeletrica.spray_rainbow":"Rainbow Spray","item.mareeletrica.paint_solvent":"Paint Solvent"}}
for lang,kv in LANG.items():
    pp=os.path.join(A,'lang',lang+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update(kv);json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.70.0 paint assets ok')
