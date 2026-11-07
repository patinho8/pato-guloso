from pathlib import Path
import zipfile,shutil
r=Path(__file__).resolve().parent;p=r/'MareEletrica'
(p/'REBUILD_0210').mkdir(exist_ok=True)
for n in ['run_gradle.py','prepare_client.py','launch_client.py','checkpoint.py']:
 shutil.copy(r/n,p/'REBUILD_0210'/n)
out=r/'Mare_Eletrica_0.21.0_EM_TESTES.zip';temp=out.with_suffix('.tmp')
with zipfile.ZipFile(temp,'w',zipfile.ZIP_DEFLATED,6) as z:
 for f in p.rglob('*'):
  if not f.is_file():continue
  rel=f.relative_to(p)
  if any(x in {'build','.gradle','run-armory21','run-camera-test','run','__pycache__','.rsync-tmp'} for x in rel.parts):continue
  if f.suffix=='.png' and 'evidence_0.20.0' in str(rel):continue
  z.write(f,'MareEletrica/'+str(rel))
with zipfile.ZipFile(temp) as z:assert z.testzip() is None
temp.replace(out);print(out.resolve(),out.stat().st_size)
