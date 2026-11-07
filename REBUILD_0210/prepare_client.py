from pathlib import Path
import urllib.request,hashlib,json,gzip,subprocess,concurrent.futures,os,time
root=Path(__file__).resolve().parent
def get(url):
 for i in range(4):
  try:return urllib.request.urlopen(url,timeout=90).read()
  except Exception:
   if i==3:raise
def graphics():
 wanted={'xvfb','libxfont2','libxkbfile1','xauth','libxmuu1','x11-xkb-utils'}
 out=root/'graphics';out.mkdir(exist_ok=True)
 for component in ['main','universe']:
  raw=gzip.decompress(get('https://archive.ubuntu.com/ubuntu/dists/noble/'+component+'/binary-amd64/Packages.gz')).decode()
  for block in raw.split('\n\n'):
   d=dict(line.split(': ',1) for line in block.splitlines() if ': ' in line and not line.startswith(' '))
   if d.get('Package') not in wanted:continue
   b=get('https://archive.ubuntu.com/ubuntu/'+d['Filename']);assert hashlib.sha256(b).hexdigest()==d['SHA256'];p=out/(d['Package']+'.deb');p.write_bytes(b);subprocess.run(['dpkg-deb','-x',str(p),str(out)],check=True);wanted.remove(d['Package'])
 assert not wanted,wanted
 p=Path('/usr/bin/xkbcomp')
 if not p.exists():p.symlink_to(out/'usr/bin/xkbcomp')
 print('Graphics ready',flush=True)
def punchy():
 d=json.loads(get('https://api.modrinth.com/v2/version/pk9eILcr'));f=next(x for x in d['files'] if x['primary']);b=get(f['url']);assert hashlib.sha512(b).hexdigest()==f['hashes']['sha512'];p=root/'compat';p.mkdir(exist_ok=True);(p/'punchy-2.8g-neoforge-1.21.1.jar').write_bytes(b);print('Punchy verified',flush=True)
def assets():
 p=root/'gradle-cache/caches/neoformruntime/artifacts/minecraft_1.21.1_version_manifest.json'
 for _ in range(300):
  if p.exists():break
  time.sleep(1)
 d=json.loads(p.read_text());index=d['assetIndex'];b=get(index['url']);assert hashlib.sha1(b).hexdigest()==index['sha1'];a=root/'gradle-cache/caches/neoformruntime/assets';(a/'indexes').mkdir(parents=True,exist_ok=True);(a/'indexes'/ (index['id']+'.json')).write_bytes(b)
 def one(obj):
  h=obj['hash'];p=a/'objects'/h[:2]/h
  if p.exists() and p.stat().st_size==obj['size']:return
  b=get('https://resources.download.minecraft.net/'+h[:2]+'/'+h);assert hashlib.sha1(b).hexdigest()==h;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b)
 with concurrent.futures.ThreadPoolExecutor(max_workers=40) as ex:list(ex.map(one,json.loads(b)['objects'].values()))
 print('Minecraft assets verified',flush=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=3) as ex:
 for result in ex.map(lambda f:f(),[graphics,punchy,assets]):pass
