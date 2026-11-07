"""Hand-built atlases for the Python .357: blued/polished steel, engraved frame, checkered walnut grip.
Writes tools/atlas_originals/armory21_python_<skin>.png (then run enhance_textures.py)."""
import os,math
import numpy as np
from PIL import Image,ImageDraw,ImageFont
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OR=os.path.join(ROOT,'tools','atlas_originals')
PAL=[(176,184,194),(58,62,68),(206,204,196),(34,66,124),(186,148,62),(66,90,58)]   # per skin 0..5 (+6 reuse 0)
ACC=['#b8c2ce','#ffeeb8','#d2aa55','#37718d','#f26342','#5ce0d0','#d2b988']
def lerp(a,b,t):return a+(b-a)*t
for skin in range(7):
    base=Image.open(os.path.join(OR,'armory21_desert_eagle_%d.png'%skin)).convert('RGB')
    arr=np.asarray(base).astype(np.float32)
    col=np.array(PAL[min(skin,5) if skin<6 else 0],dtype=np.float32)
    # --- frame / paint band: blued finish with a soft specular streak and corner darkening
    y=np.linspace(0,1,256)[:,None,None]
    spec=np.exp(-((y-.30)/.06)**2)*70+np.exp(-((y-.78)/.05)**2)*26
    body=lerp(col*1.22,col*.30,y**.85)+spec
    arr[0:256]=np.clip(np.repeat(body,512,axis=1),0,255)
    im=Image.fromarray(arr.astype(np.uint8));d=ImageDraw.Draw(im,'RGBA')
    gold=tuple(int(ACC[skin][i:i+2],16) for i in (1,3,5))
    # engraved scrollwork along two bands of the paint region
    for yc in (44,150,206):
        pts=[(x,yc+10*math.sin(x/9.0)+4*math.sin(x/3.1)) for x in range(0,512,2)]
        d.line(pts,fill=gold+(70,),width=1)
        pts2=[(x,yc+10*math.sin(x/9.0+math.pi)-4*math.sin(x/3.1)) for x in range(0,512,2)]
        d.line(pts2,fill=gold+(46,),width=1)
        for x in range(8,512,36):d.ellipse([x-2,yc-2,x+2,yc+2],outline=gold+(90,))
    # --- steel band: polished, with bright streaks
    st=np.asarray(im).astype(np.float32)
    t=np.linspace(0,1,64)[:,None,None]
    steel=lerp(np.array([196,204,214.]),np.array([48,54,62.]),t)+np.exp(-((t-.25)/.08)**2)*60
    st[256:320]=np.clip(np.repeat(steel,512,axis=1),0,255)
    # --- grip band (walnut): grain + diamond checkering
    yy,xx=np.mgrid[0:64,0:512]
    grain=(np.sin(xx/22.0+np.sin(yy/7.0)*2.0)*.5+.5)[...,None]
    wood=np.array([84,48,22.])*(.75+.5*grain)
    chk=((np.sin((xx+yy)*.9)>.8)|(np.sin((xx-yy)*.9)>.8))[...,None]
    wood=np.where(chk,wood*.55,wood)
    st[384:448]=np.clip(wood,0,255)
    out=Image.fromarray(st.astype(np.uint8));d=ImageDraw.Draw(out)
    # plate text
    f1=ImageFont.load_default(size=7);f2=ImageFont.load_default(size=4)
    d.text((177,50),'PYTHON',font=f1,fill=ACC[skin]);d.text((178,60),'ME / 357 / %02d'%skin,font=f2,fill=ACC[skin])
    out.save(os.path.join(OR,'armory21_python_%d.png'%skin))
print('python atlases rebuilt')
