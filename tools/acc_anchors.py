"""0.74.0: measures where accessories sit on each gun from its body/magazine OBJ (block units*16)."""
import json,os,sys
B='src/main/resources/assets/mareeletrica/models/block/'
D=json.load(open('tools/gunsmith_072_data.json'))
FAM="desert_eagle usp m4a1 ak47 m870 mp5 smg sniper mp40 p90 python glock scar ump45 spas12 beretta uzi famas m249 barrett mac10 galil aug saiga12 svd".split()
PISTOL={'desert_eagle','usp','python','glock','beretta'}
def faces(name):
    j=json.load(open(B+f'armory21_{name}_0.json'))
    if 'model' not in j:
        while 'elements' not in j:j=json.load(open(B+j['parent'].split('/')[-1]+'.json'))
        return [[(e['from'][a],e['to'][a]) for a in range(3)] for e in j['elements']]
    p=j['model'].split(':')[1]
    v=[];F=[]
    for l in open('src/main/resources/assets/mareeletrica/'+p):
        if l.startswith('v '):v.append([float(t)*16 for t in l.split()[1:4]])
        elif l.startswith('f '):
            ps=[v[int(t.split('/')[0])-1] for t in l.split()[1:]]
            F.append([(min(q[a] for q in ps),max(q[a] for q in ps)) for a in range(3)])
    return F
out={}
for f in FAM:
    if f=='sniper':out[f]=[0]*8;continue
    body=faces(f+'_body');mag=faces(f+'_magazine') if f not in('m870','spas12','python') else []
    mz=float(D[f]['muzzle']);bore=float(D[f]['bore'])
    span=lambda F,z,x0,x1:[b for b in F if b[2][0]-.05<=z<=b[2][1]+.05 and b[0][0]<x1 and b[0][1]>x0]
    if mag:
        mzs=[b[2][0] for b in mag]+[b[2][1] for b in mag];mfront,mback=min(mzs),max(mzs)
    else:mfront=mback=mz*.3
    if f in PISTOL:z0=mz+1.4
    elif mfront>9:z0=mz+(mfront-mz)*.4
    else:z0=mfront-(mfront-mz)*.4
    sl=span(body,z0,7.6,8.4)
    uy=min([b[1][0] for b in sl if b[1][0]<bore+.5] or [bore-1])
    ss=[b for b in span(body,z0-1.5,6,10) if b[1][0]<bore+.6 and b[1][1]>bore-.6]
    sx=max([b[0][1] for b in ss] or [8.6])
    # magazine top where it leaves the body, at the mag middle
    mc=(mfront+mback)/2
    bl=span(body,mc,7.7,8.3);my=min([b[1][0] for b in bl if b[1][0]<bore] or [bore-2]) if mag else 0
    mt=max([b[1][1] for b in mag] or [0])
    out[f]=[round(uy,2),round(z0,2),round(sx,2),round(bore+.2,2),round(z0-1.5,2),round(my,2),round(mc,2),round(mt,2)]
    print(f,out[f],'mag',round(mfront,2),round(mback,2))
json.dump(out,open('tools/acc_anchors.json','w'))

J='src/main/java/br/mareeletrica/client/ArmoryRenderer.java';j=open(J).read()
row='static final float[][] ACC={'+','.join('{'+','.join(f'{v}F' for v in out[f])+'}' for f in FAM)+'};'
import re
if 'static final float[][] ACC=' in j:j=re.sub(r'static final float\[\]\[\] ACC=\{.*?\}\};',row,j)
else:j=j.replace(' public static double muzzle(WeaponFamily f)',' /** 0.74.0 (tools/acc_anchors.py): under y,z | side x,y,z | drum y,z | magazine top */\n '+row+'\n public static double muzzle(WeaponFamily f)',1)
open(J,'w').write(j)
