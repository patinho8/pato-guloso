from pathlib import Path
import subprocess,sys,time,shutil,json,hashlib
r=Path(__file__).resolve().parent;p=r/'MareEletrica';e=p/'tests/evidence_0.21.0'
for i in range(900):
 log=(r/'compile21-final.log').read_text() if (r/'compile21-final.log').exists() else ''
 if 'BUILD FAILED' in log:raise RuntimeError('Compilation failed; inspect compile21-final.log')
 if 'BUILD SUCCESSFUL' in log:break
 time.sleep(1)
else:raise RuntimeError('Compilation timeout')
print('Compiled; checking binary equivalence',flush=True)
expected=json.loads((e/'tested-production-classes.json').read_text());classes=p/'build/classes/java/main'
changed=[n for n,h in expected.items() if hashlib.sha256((classes/n).read_bytes()).hexdigest()!=h]
assert not changed,changed
# Retain verified production bytecode as a small durable recovery aid.
with __import__('zipfile').ZipFile(e/'tested-production-bytecode.zip','w',__import__('zipfile').ZIP_DEFLATED) as z:
 for n in expected:z.write(classes/n,n)
with (r/'client21-final.log').open('w') as out:subprocess.run([sys.executable,str(r/'launch_client.py'),'-PtestNeoForgeVersion=21.1.229','-PtestRendererJar='+str(r/'compat/punchy-2.8g-neoforge-1.21.1.jar'),'-PquickArmory','-x','compileJava'],cwd=r,stdout=out,stderr=subprocess.STDOUT,check=True)
test=p/'run-armory21/armory21-test.txt';log=test.read_text();assert 'PASS visual cycle 18' in log and 'Punchy=true' in log and 'FAIL' not in log,log[-300:]
shutil.copy(test,e/'punchy-test.txt');shutil.copy(p/'run-armory21/config/punchy/punchy_config.json',e/'punchy-config.json')
for f in (p/'run-armory21/screenshots').glob('*.png'):shutil.copy(f,e/(f.stem+'-final.png'))
subprocess.run([sys.executable,str(p/'tests/validate_armory21.py')],cwd=r,check=True,stdout=(e/'asset-validation.txt').open('w'))
with (r/'release-build.log').open('w') as out:subprocess.run([sys.executable,str(r/'run_gradle.py'),'jar','-PtestNeoForgeVersion=21.1.229','-x','compileJava'],cwd=r,stdout=out,stderr=subprocess.STDOUT,check=True)
for n in ['run_gradle.py','prepare_client.py','launch_client.py','checkpoint.py','package_release.py','finish_pipeline.py']:shutil.copy(r/n,p/'REBUILD_0210'/n)
subprocess.run([sys.executable,str(r/'package_release.py')],cwd=r,check=True)
