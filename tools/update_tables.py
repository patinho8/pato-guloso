"""copies tools/gunsmith_072_data.json into the tables at the top of ArmoryRenderer.java"""
import json,re,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
d=json.load(open(os.path.join(ROOT,'tools/gunsmith_072_data.json')))
order=['desert_eagle','usp','m4a1','ak47','m870','mp5','smg','sniper','mp40','p90','python','glock','scar','ump45','spas12','beretta','uzi','famas','m249','barrett','mac10','galil','aug','saiga12','svd']
fl=lambda xs:','.join(f'{x:.2f}F' for x in xs)
p=os.path.join(ROOT,'src/main/java/br/mareeletrica/client/ArmoryRenderer.java');s=open(p).read()
s=re.sub(r' static final double\[\] MUZZLE=\{[^}]*\};',' static final double[] MUZZLE={'+','.join(f'{d[f]["muzzle"]:.2f}' for f in order)+'};',s)
s=re.sub(r' static final float\[\] BORE=\{[^}]*\};',' static final float[] BORE={'+fl([d[f]['bore'] for f in order])+'};',s)
s=re.sub(r' static final float\[\] IRON=\{[^}]*\};',' static final float[] IRON={'+fl([d[f]['iron'] for f in order])+'};',s)
s=re.sub(r' static final float\[\]\[\] SIGHT=\{.*?\}\};',' static final float[][] SIGHT={'+','.join('{'+fl(d[f]['sights'])+'}' for f in order)+'};',s,flags=re.S)
open(p,'w').write(s);print('tables ok',d['m4a1'])
