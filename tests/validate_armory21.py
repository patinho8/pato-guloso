from pathlib import Path
import json,hashlib
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/mareeletrica';n=0
for p in (a/'models').rglob('armory21_*.json'):
 d=json.loads(p.read_text())
 for e in d.get('elements',[]):
  assert all(-16<=x<=32 for x in e['from']+e['to']),(p,e['name'],'bounds')
  assert all(x<y for x,y in zip(e['from'],e['to'])),(p,e['name'],'zero volume')
  for face in e['faces'].values():assert all(0<=v<=16 for v in face['uv']),(p,e['name'],'UV',face['uv'])
 n+=1
hashes=[hashlib.sha256(p.read_bytes()).hexdigest() for p in (a/'textures/block').glob('armory21_*.png')];assert len(hashes)==len(set(hashes))==56
seen={};adapt=0
for p in (r/'src/main/resources/data/mareeletrica/recipe').glob('*.json'):
 d=json.loads(p.read_text())
 if p.stem.startswith('adapt_'):adapt+=1;continue
 if 'pattern' not in d:continue
 if not any(p.stem.startswith(f+'_') for f in ['desert_eagle','usp','m4a1','ak47','m870','mp5','smg','sniper']):continue
 if not any(t in p.stem for t in ['optic','suppressor','_skin_','empty_magazine']):continue
 recipe=tuple(tuple(json.dumps(d['key'][c],sort_keys=True) if c!=' ' else '' for c in row) for row in d['pattern']);assert recipe not in seen,(p,seen.get(recipe));seen[recipe]=p
assert adapt==248,adapt
print(n,'model parts valid;',len(hashes),'unique mapped atlases;',len(seen),'distinct recipes;',adapt,'legacy adaptation recipes')
