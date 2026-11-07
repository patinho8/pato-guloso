"""0.60.0 asset fixes: complete item icons (rooftop turbine shows its rotor), sane GUI sizes, missing entity names."""
import json,copy,os
A=os.path.join(os.path.dirname(__file__),'..','src','main','resources','assets','mareeletrica')
M=os.path.join(A,'models')
def load(p):return json.load(open(os.path.join(M,p+'.json')))
def save(p,d):json.dump(d,open(os.path.join(M,p+'.json'),'w'))
# 1) rooftop turbine icon: base + savonius rotor on top
base=load('block/rooftop_turbine');rot=load('block/wind_savonius')
el=copy.deepcopy(base['elements'])
for e in copy.deepcopy(rot['elements']):
    e['from'][1]+=4;e['to'][1]+=4
    if 'rotation' in e:e['rotation']['origin'][1]+=4
    el.append(e)
tx=dict(base['textures']);tx.update(rot['textures'])
HAND={"ground":{"translation":[0,3,0],"scale":[0.25,0.25,0.25]},"fixed":{"scale":[0.5,0.5,0.5]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[0.3,0.3,0.3]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[0.35,0.35,0.35]}}
save('item/rooftop_turbine',{"textures":tx,"elements":el,"display":dict(HAND,gui={"rotation":[25,225,0],"translation":[0,-1.5,0],"scale":[0.62,0.62,0.62]})})
# 2) vehicle hangar icon was bigger than the slot (model is 3x3 blocks)
h=load('item/vehicle_hangar');h['display']['gui']={"rotation":[25,225,0],"translation":[0,-1,0],"scale":[0.3,0.3,0.3]};save('item/vehicle_hangar',h)
# 3) vortex lift is a 3px-thin pad: centre it in the slot
v=load('item/vortex_lift');v['display']['gui']={"rotation":[30,225,0],"translation":[0,-3,0],"scale":[0.8,0.8,0.8]};save('item/vortex_lift',v)
# 4) entity names that were missing
for lang,names in {'pt_br':{'dmt_projectile':'Projétil DMT','grenade':'Granada'},'en_us':{'dmt_projectile':'DMT Projectile','grenade':'Grenade'}}.items():
    p=os.path.join(A,'lang',lang+'.json')
    if not os.path.exists(p):continue
    d=json.load(open(p,encoding='utf-8'))
    for k,n in names.items():d.setdefault('entity.mareeletrica.'+k,n)
    json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('ok')

# 5) every 3D item icon: centred in the slot and never bigger than it (computed from the model's own geometry)
import math,itertools
def _load(ref):
    if ':' not in ref:return None
    ns,p=ref.split(':')
    if ns!='mareeletrica':return None
    f=os.path.join(M,p+'.json');return json.load(open(f)) if os.path.exists(f) else None
def _resolve(ref):
    els=None;disp={};m=_load(ref)
    while m is not None:
        if els is None and 'elements' in m:els=m['elements']
        for k,v in m.get('display',{}).items():disp.setdefault(k,v)
        par=m.get('parent');m=_load(par) if par else None
    return els,disp.get('gui')
def _rot(v,ax,deg):
    a=math.radians(deg);c,s=math.cos(a),math.sin(a);x,y,z=v
    if ax=='x':return (x,y*c-z*s,y*s+z*c)
    if ax=='y':return (x*c+z*s,y,-x*s+z*c)
    return (x*c-y*s,x*s+y*c,z)
def _corners(e):
    pts=list(itertools.product(*zip(e['from'],e['to'])));r=e.get('rotation')
    if r:o=r['origin'];pts=[tuple(a+b for a,b in zip(_rot(tuple(p-q for p,q in zip(pt,o)),r['axis'],r['angle']),o)) for pt in pts]
    return pts
fixed=0
for fn in sorted(os.listdir(os.path.join(M,'item'))):
    name=fn[:-5];els,g=_resolve('mareeletrica:item/'+name)
    if not els or g is None:continue
    R=g.get('rotation',[0,0,0]);S=list(g.get('scale',[1,1,1]));T=g.get('translation',[0,0,0])
    xs=[];ys=[]
    for e in els:
        for p in _corners(e):
            v=tuple((c-8)/16*s for c,s in zip(p,S));v=_rot(v,'z',R[2]);v=_rot(v,'y',R[1]);v=_rot(v,'x',R[0]);xs.append(v[0]);ys.append(v[1])
    cx=(max(xs)+min(xs))/2;cy=(max(ys)+min(ys))/2;half=max(max(xs)-min(xs),max(ys)-min(ys))/2
    f=min(1,.56/half) if half>.6 else 1
    off=max(abs(cx+T[0]/16),abs(cy+T[1]/16))
    if f==1 and off<=.12:continue
    nt=[round(-cx*16*f,2),round(-cy*16*f,2),T[2] if len(T)>2 else 0]
    ns=[round(s*f,3) for s in S]
    m=json.load(open(os.path.join(M,'item',fn)))
    m.setdefault('display',{})['gui']={"rotation":R,"translation":nt,"scale":ns}
    json.dump(m,open(os.path.join(M,'item',fn),'w'));fixed+=1
print('icones ajustados:',fixed)
# 0.61.1: free look key name
for lang,v in {'pt_br':'Caça: olhar em volta (segurar)','en_us':'Jet: free look (hold)'}.items():
    p=os.path.join(A,'lang',lang+'.json');d=json.load(open(p,encoding='utf-8'));d['key.mareeletrica.jet_look']=v;json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
