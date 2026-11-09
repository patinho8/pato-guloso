"""0.80.0: fits every gun's inventory icon inside one slot (keeps the 3/4 angle, centres it)."""
import json,math,glob
B='src/main/resources/assets/mareeletrica/'
FAM="desert_eagle usp m4a1 ak47 m870 mp5 smg sniper mp40 p90 python glock scar ump45 spas12 beretta uzi famas m249 barrett mac10 galil aug saiga12 svd".split()
ROT=(10,105,-30);FILL=1.0
def verts(fam):
    out=[]
    for part in('body','moving','magazine'):
        try:j=json.load(open(B+f'models/block/armory21_{fam}_{part}_0.json'))
        except FileNotFoundError:continue
        if 'model' not in j:continue
        p=j['model'].split(':')[1]
        out+=[[float(t) for t in l.split()[1:4]] for l in open(B+p) if l.startswith('v ')]
    return out
def rotXYZ(v,a):
    x,y,z=v;rx,ry,rz=[math.radians(t) for t in a]
    # rotationXYZ: v' = Rx * Ry * Rz * v
    c,s=math.cos(rz),math.sin(rz);x,y=x*c-y*s,x*s+y*c
    c,s=math.cos(ry),math.sin(ry);x,z=x*c+z*s,-x*s+z*c
    c,s=math.cos(rx),math.sin(rx);y,z=y*c-z*s,y*s+z*c
    return x,y,z
items={'desert_eagle':'revolver'}
for f in FAM:
    v=verts(f)
    if not v:print(f,'no model');continue
    r=[rotXYZ((q[0]-.5,q[1]-.5,q[2]-.5),ROT) for q in v]
    xs=[q[0] for q in r];ys=[q[1] for q in r]
    w,h=max(xs)-min(xs),max(ys)-min(ys);s=FILL/max(w,h)
    cx,cy=(max(xs)+min(xs))/2,(max(ys)+min(ys))/2
    gui={'rotation':list(ROT),'translation':[round(-cx*s*16,2),round(-cy*s*16,2),0],'scale':[round(s,3)]*3}
    hit=0
    for p in glob.glob(B+'models/item/*.json'):
        j=json.load(open(p))
        if j.get('parent')!='builtin/entity':continue
        name=p.split('/')[-1][:-5]
        if name!=f and name!=items.get(f):continue
        j.setdefault('display',{})['gui']=gui;json.dump(j,open(p,'w'));hit+=1
    print(f.ljust(13),'w %.2f h %.2f -> scale %.3f'%(w,h,s),'files',hit)
