"""Shrinks the mod's resources without changing how anything looks in game.
1. Weapon atlases (textures/block/armory21_*.png): 512 -> 256 px + 256-colour palette (still 4x vanilla item detail).
   tools/atlas_originals/*.png: palette only (kept at 512 as input for enhance_textures.py).
2. Models with identical geometry (e.g. the 13 colour variants of a gun part) become a tiny "parent" reference
   that only swaps the texture.
3. Every JSON is rewritten minified.
Run it LAST, after generate_armory21.py / enhance_textures.py / make_*.py."""
import json,os,glob,sys
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources')
A=os.path.join(RES,'assets/mareeletrica')
def size(paths):return sum(os.path.getsize(p) for p in paths)
def shrink_png(path,max_side=None):
    im=Image.open(path)
    if im.mode in ('P','L') and (max_side is None or max(im.size)<=max_side):return
    im=im.convert('RGBA');alpha=im.getchannel('A').getextrema()[0]<255
    if max_side and max(im.size)>max_side:im=im.resize((max_side,max_side*im.size[1]//im.size[0]),Image.LANCZOS)
    if alpha:im.save(path,optimize=True);return
    im.convert('RGB').quantize(colors=256,method=Image.Quantize.MEDIANCUT,dither=Image.Dither.NONE).save(path,optimize=True)
def main():
    tex=glob.glob(os.path.join(A,'textures/block/armory21_*.png'));orig=glob.glob(os.path.join(ROOT,'tools/atlas_originals/*.png'))
    b=size(tex)+size(orig)
    for p in tex:shrink_png(p,256)
    for p in orig:shrink_png(p)
    big=[p for p in glob.glob(os.path.join(A,'textures/**/*.png'),recursive=True) if os.path.getsize(p)>120_000 and 'armory21_' not in p]
    b2=size(big)
    for p in big:shrink_png(p)
    print(f'other large textures: {b2/1e6:.1f} MB -> {size(big)/1e6:.1f} MB')
    print(f'textures: {b/1e6:.1f} MB -> {(size(tex)+size(orig))/1e6:.1f} MB')
    # ---- model dedupe ----
    models={}
    for kind in ('block','item'):
        for p in glob.glob(os.path.join(A,'models',kind,'*.json')):
            try:models[(kind,os.path.basename(p)[:-5])]=json.load(open(p,encoding='utf-8'))
            except Exception as e:print('skip',p,e)
    groups={}
    for key,m in models.items():
        el=m.get('elements')
        if not el or 'parent' in m:continue
        groups.setdefault(json.dumps(el,sort_keys=True,separators=(',',':')),[]).append(key)
    converted=0
    for keys in groups.values():
        if len(keys)<2:continue
        keys.sort(key=lambda k:(k[0]!='block',len(k[1]),k[1]))
        canon=keys[0];cm=models[canon]
        for k in keys[1:]:
            m=models[k]
            if 'display' not in m and 'display' in cm:continue      # would inherit a display it never had
            new={'parent':f'mareeletrica:{canon[0]}/{canon[1]}','textures':m.get('textures',{})}
            for opt in ('display','gui_light'):
                if opt in m:new[opt]=m[opt]
            if m.get('ambientocclusion',True)!=cm.get('ambientocclusion',True):new['ambientocclusion']=m.get('ambientocclusion',True)
            models[k]=new;converted+=1
    for (kind,name),m in models.items():
        json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w',encoding='utf-8'),ensure_ascii=False,separators=(',',':'))
    print('models turned into parent references:',converted)
    # ---- minify every other JSON ----
    allj=glob.glob(os.path.join(RES,'**/*.json'),recursive=True);b=size(allj)
    for p in allj:
        try:d=json.load(open(p,encoding='utf-8'))
        except Exception:continue
        json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,separators=(',',':'))
    print(f'json: {b/1e6:.1f} MB -> {size(allj)/1e6:.1f} MB')
if __name__=='__main__':main()
