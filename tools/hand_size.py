"""0.80.2: per-gun size in the hand, from each gun's real length (only shrinks guns that look too big next to the M4)."""
import json,re,glob
B='src/main/resources/assets/mareeletrica/'
FAM="desert_eagle usp m4a1 ak47 m870 mp5 smg sniper mp40 p90 python glock scar ump45 spas12 beretta uzi famas m249 barrett mac10 galil aug saiga12 svd".split()
REAL={'desert_eagle':.27,'usp':.19,'m4a1':.84,'ak47':.88,'m870':1.06,'mp5':.68,'smg':.62,'sniper':1.20,'mp40':.83,'p90':.50,'python':.29,'glock':.19,'scar':.97,'ump45':.69,'spas12':1.04,'beretta':.22,'uzi':.47,'famas':.76,'m249':1.04,'barrett':1.45,'mac10':.30,'galil':.98,'aug':.79,'saiga12':1.05,'svd':1.22}
PIST={'desert_eagle','usp','glock','beretta','python'}
def L(f,sk=0):
    j=json.load(open(B+f'models/block/armory21_{f}_body_{sk}.json'))
    while 'model' not in j and 'parent' in j:j=json.load(open(B+'models/'+j['parent'].split(':')[1]+'.json'))
    zs=[float(l.split()[3]) for l in open(B+j['model'].split(':')[1]) if l.startswith('v ')]
    return max(zs)-min(zs)
ratio=L('m4a1')*.66/REAL['m4a1'];K={}
for f in FAM:
    r=REAL[f]*ratio/(L(f)*(.62 if f in PIST else .66))
    K[f]=round(min(1.0,r)**.7,3)        # 70% of the way to real proportions, never bigger than now
    print(f.ljust(13),K[f])
J='src/main/java/br/mareeletrica/client/RifleHands.java';j=open(J).read()
row=' static final float[] SIZE={'+','.join(f'{K[f]}F' for f in FAM)+'};'
if ' static final float[] SIZE=' in j:j=re.sub(r' static final float\[\] SIZE=\{.*?\};',row,j)
else:j=j.replace(' static final float BIG=',' /** 0.80.2 (tools/hand_size.py): per-gun size, one per WeaponFamily (pistols were ~1.5x too big next to the rifles) */\n'+row+'\n static final float BIG=',1)
open(J,'w').write(j)
# third person: same factor on the item display (translation keeps the hand on the grip)
for f in FAM:
    if K[f]>=.999:continue
    s=round(.7*K[f],3);t=[0,round(1+4.4*(s-.75),2),round(-1-4*(s-.75),2)]
    for p in glob.glob(B+'models/item/*.json'):
        d=json.load(open(p))
        if d.get('parent')!='builtin/entity':continue
        n=p.split('/')[-1][:-5]
        if n!=f and not(f=='desert_eagle' and n=='revolver'):continue
        for k in('thirdperson_righthand','thirdperson_lefthand'):
            if k in d.get('display',{}):d['display'][k]['scale']=[s,s,s];d['display'][k]['translation']=t
        json.dump(d,open(p,'w'))
