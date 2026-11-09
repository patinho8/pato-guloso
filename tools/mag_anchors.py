"""0.79.0: measures each gun's magazine (bottom and top centre, model px) for the per-gun reload."""
import json,re
B='src/main/resources/assets/mareeletrica/models/block/'
FAM="desert_eagle usp m4a1 ak47 m870 mp5 smg sniper mp40 p90 python glock scar ump45 spas12 beretta uzi famas m249 barrett mac10 galil aug saiga12 svd".split()
def verts(name):
    j=json.load(open(B+f'armory21_{name}_0.json'))
    if 'model' not in j:return []
    p=j['model'].split(':')[1];return [[float(t)*16 for t in l.split()[1:4]] for l in open('src/main/resources/assets/mareeletrica/'+p) if l.startswith('v ')]
def mv(f):
    m=verts(f+'_moving')
    if not m:return [0,0,0]
    return [round((min(q[0] for q in m)+max(q[0] for q in m))/2,2),round(max(q[1] for q in m),2),round(max(q[2] for q in m),2)]
out={}
for f in FAM:
    v=verts(f+'_magazine') if f not in('m870','spas12','python') else []
    if not v:out[f]=[8,0,0,0,0]+mv(f);print(f,'no mag',out[f]);continue
    ys=[q[1] for q in v];y0,y1=min(ys),max(ys)
    bot=[q for q in v if q[1]<y0+.35];top=[q for q in v if q[1]>y1-.35]
    zb=sum(q[2] for q in bot)/len(bot);zt=sum(q[2] for q in top)/len(top)
    x=(min(q[0] for q in v)+max(q[0] for q in v))/2
    out[f]=[round(x,2),round(y0,2),round(zb,2),round(y1,2),round(zt,2)]+mv(f)
    print(f.ljust(13),out[f],'len %.2f'%((y1-y0)**2+(zt-zb)**2)**.5)
json.dump(out,open('tools/mag_anchors.json','w'))
J='src/main/java/br/mareeletrica/client/ReloadPath.java';j=open(J).read()
row='static final float[][] MAG={'+','.join('{'+','.join(f'{v}F' for v in out[f])+'}' for f in FAM)+'};'
if 'static final float[][] MAG=' in j:j=re.sub(r'static final float\[\]\[\] MAG=\{.*?\}\};',row,j)
else:j=j.replace(' private ReloadPath(){}',' /** 0.79.0 (tools/mag_anchors.py): magazine x | bottom y,z | top y,z | moving part x, top y, rear z (model px), one row per WeaponFamily */\n '+row+'\n private ReloadPath(){}',1)
open(J,'w').write(j)
