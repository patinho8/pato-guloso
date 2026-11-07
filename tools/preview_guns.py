"""Side-view preview of the generated firearm models (z horizontal, y vertical)."""
import json,glob,math,os,sys
from PIL import Image,ImageDraw
RES=os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),'src/main/resources/assets/mareeletrica/models/block')
COL=[(120,130,142),(70,82,96),(36,40,46),(124,72,36),(255,40,60)]
def band(el):
    f=el['faces']
    for d in ('east','west','up','north'):
        if d in f and 'uv' in f[d]:return min(4,int(f[d]['uv'][1]//(16/8*1.0)) if False else int(min(f[d]['uv'][1],15.99)//3.2))
    return 0
def draw(f,skin=0,parts=('body','moving','magazine'),scale=18):
    W,H=int(60*scale/1.8),int(24*scale/1.8);img=Image.new('RGB',(W,H),(24,26,32));d=ImageDraw.Draw(img,'RGBA')
    els=[]
    for p in parts:
        fp=f'{RES}/armory21_{f}_{p}_{skin}.json'
        if os.path.exists(fp):els+=json.load(open(fp))['elements']
    els.sort(key=lambda e:(e['from'][0]+e['to'][0]))
    k=scale/1.8
    def X(z):return (z+22)*k
    def Y(y):return H-(y+3)*k
    for e in els:
        (x0,y0,z0),(x1,y1,z1)=e['from'],e['to']
        pts=[(z0,y0),(z1,y0),(z1,y1),(z0,y1)]
        r=e.get('rotation')
        if r and r['axis']=='x':
            oy,oz=r['origin'][1],r['origin'][2];a=math.radians(r['angle']);c,s=math.cos(a),math.sin(a)
            pts=[((z-oz)*c-(y-oy)*s+oz,(z-oz)*s+(y-oy)*c+oy) for z,y in pts]
        col=COL[band(e)]
        d.polygon([(X(z),Y(y)) for z,y in pts],fill=col+(235,),outline=(10,10,12,160))
    return img
if __name__=='__main__':
    fams=sys.argv[1:] or ['desert_eagle','usp','m4a1','ak47','m870','mp5','smg','sniper','mp40','p90']
    ims=[draw(f) for f in fams];W=ims[0].width;H=ims[0].height
    cols=2;rows=(len(ims)+1)//2;sheet=Image.new('RGB',(W*cols,H*rows))
    for i,im in enumerate(ims):sheet.paste(im,((i%cols)*W,(i//cols)*H))
    sheet.save('/tmp/guns.png');print(sheet.size)
