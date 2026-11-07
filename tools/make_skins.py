"""Six extra weapon skins (7..12) for every family, painted on the 'paint' band (rows 0-255) of the original atlas.
7 Tigre, 8 Digital, 9 Fibra de Carbono, 10 Damasco, 11 Hex Neon, 12 Galáxia. Writes tools/atlas_originals/armory21_<family>_<skin>.png."""
import os,math,glob
import numpy as np,cv2
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
O=os.path.join(ROOT,'tools','atlas_originals')
H,W=256,512
def noise(rng,sx,sy):
    n=rng.standard_normal((H,W)).astype(np.float32);n=cv2.GaussianBlur(n,(0,0),sigmaX=max(sx,.1),sigmaY=max(sy,.1));return n/(n.std()+1e-6)
def tiger(rng):
    base=np.zeros((H,W,3),np.float32);y=np.linspace(0,1,H)[:,None,None]
    base[:]=np.array([232,126,30])*(1-.35*y)+np.array([255,190,90])*.0
    xx=np.arange(W)[None,:];yy=np.arange(H)[:,None]
    warp=noise(rng,30,30)*18
    s=np.sin((xx+warp*2+yy*.35)/11.0)+noise(rng,6,40)*.45
    stripes=(s>.72)[...,None]
    return np.where(stripes,np.array([22,16,12.]),base)
def digital(rng):
    pal=np.array([[78,92,58],[104,118,72],[60,66,44],[132,120,84],[40,44,34]],np.float32)
    out=np.zeros((H,W,3),np.float32)
    for scale,p in ((16,0),(8,1),(4,2)):
        h,w=H//scale,W//scale;n=rng.random((h,w))
        idx=np.floor(n*len(pal)).astype(int);blk=pal[idx]
        up=np.kron(blk,np.ones((scale,scale,1)))
        mask=(rng.random((h,w))<(.85 if p==0 else .45))
        m=np.kron(mask,np.ones((scale,scale)))[...,None]>0
        out=np.where(m,up,out) if p else up
    return out
def carbon(rng):
    yy,xx=np.mgrid[0:H,0:W];c=8
    cell=((xx//c)+(yy//c))%2
    a=np.where(cell==0,(np.sin(xx/c*math.pi)**2),(np.sin(yy/c*math.pi)**2))
    v=24+a*46
    out=np.stack([v,v*1.02,v*1.08],-1)
    return out+noise(rng,1,1)[...,None]*3
def damascus(rng):
    yy,xx=np.mgrid[0:H,0:W].astype(np.float32)
    w=noise(rng,40,40)*30+noise(rng,12,12)*8
    layers=np.sin((yy+w+np.sin(xx/37)*14)/3.4)
    v=128+layers*44
    out=np.stack([v*1.0,v*1.02,v*1.06],-1)
    return out
def hexneon(rng):
    out=np.zeros((H,W,3),np.float32)+np.array([14,18,26])
    img=np.zeros((H,W),np.uint8);r=11
    dx=r*1.5;dy=r*math.sqrt(3)
    for i in range(-1,int(W/dx)+2):
        for j in range(-1,int(H/dy)+2):
            cx=i*dx;cy=j*dy+(dy/2 if i%2 else 0)
            pts=np.array([[cx+r*math.cos(math.radians(60*k)),cy+r*math.sin(math.radians(60*k))] for k in range(6)],np.int32)
            cv2.polylines(img,[pts],True,255,1)
    glow=cv2.GaussianBlur(img.astype(np.float32),(0,0),2.2)
    hue=(np.arange(W)/W)[None,:]
    col=np.stack([40+hue*200*np.ones((H,1)),230-hue*120*np.ones((H,1)),255*np.ones((H,W))],-1)
    out+=col*(glow/255*1.6)[...,None]+col*(img/255.*.9)[...,None]
    return out
def galaxy(rng):
    yy,xx=np.mgrid[0:H,0:W].astype(np.float32)
    n=noise(rng,28,22)*.6+noise(rng,9,9)*.3
    base=np.stack([30+40*np.clip(n+.3,0,2),10+18*np.clip(n,0,2),70+70*np.clip(n+.4,0,2)],-1)
    neb=np.clip(noise(rng,20,14),0,None)[...,None]*np.array([150,40,120])*.5
    out=base+neb
    stars=rng.random((H,W))>.9965
    out[stars]=[255,255,240]
    big=rng.random((H,W))>.9993
    for y,x in zip(*np.where(big)):cv2.circle(out,(int(x),int(y)),1,(255,240,220),-1)
    return out
PATTERNS=[tiger,digital,carbon,damascus,hexneon,galaxy]
def accent(skin):return [(20,14,10),(48,56,38),(150,160,176),(220,226,236),(90,240,255),(255,210,255)][skin-7]
if __name__=='__main__':
    fams=sorted({os.path.basename(p).rsplit('_',1)[0].replace('armory21_','') for p in glob.glob(os.path.join(O,'armory21_*_0.png'))})
    for fi,fam in enumerate(fams):
        base=np.asarray(Image.open(os.path.join(O,f'armory21_{fam}_0.png')).convert('RGB')).astype(np.float32)
        plate=base[46:70,170:250].copy()
        for k,fn in enumerate(PATTERNS):
            skin=7+k;rng=np.random.default_rng(7000+sum(map(ord,fam))*31+skin)   # stable per family name
            img=base.copy();pat=fn(rng)
            # keep the machined highlights of the original paint band so the shape still reads
            lum=base[0:256].mean(-1,keepdims=True)/255.0
            img[0:256]=np.clip(pat*(.72+.5*lum),0,255)
            # panel lines like the stock atlases
            for y in (5,119,135,184,199,248):img[y,:]=np.array(accent(skin))*.8+img[y,:]*.2
            img[46:70,170:250]=np.clip(plate*.25+img[46:70,170:250]*.75,0,255)
            Image.fromarray(img.astype(np.uint8)).save(os.path.join(O,f'armory21_{fam}_{skin}.png'))
        print(fam,'ok')
