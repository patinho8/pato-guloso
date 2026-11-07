"""Adds surface detail to the firearm atlases (paint grain, brushed steel, grip stipple, wood grain).
Reads tools/atlas_originals/*.png and writes into the mod's textures. Safe to rerun (never compounds)."""
import glob,os,random
import numpy as np
import cv2
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC=os.path.join(ROOT,'tools','atlas_originals');DST=os.path.join(ROOT,'src/main/resources/assets/mareeletrica/textures/block')
def noise(rng,h,w,sx,sy):
    n=rng.standard_normal((h,w)).astype(np.float32)
    k=(max(1,int(sx)*2+1),max(1,int(sy)*2+1))
    n=cv2.GaussianBlur(n,k,sigmaX=max(sx,.1),sigmaY=max(sy,.1))
    return n/(n.std()+1e-6)
def enhance(path):
    name=os.path.basename(path);rng=np.random.default_rng(abs(hash(name))%(2**32) if False else sum(map(ord,name))*7919)
    img=np.asarray(Image.open(path).convert('RGB')).astype(np.float32)/255
    h,w,_=img.shape
    def band(y0,y1,fn):
        sub=img[y0:y1]; img[y0:y1]=np.clip(fn(sub),0,1)
    # paint: mottling + fine grain + speckle
    def paint(s):
        hh=s.shape[0];m=noise(rng,hh,w,22,22)*.035+noise(rng,hh,w,2,2)*.022+noise(rng,hh,w,.6,.6)*.018
        out=s*(1+m[...,None])
        sp=(rng.random((hh,w))>.9965)[...,None];out=np.where(sp,out*0.6,out)
        sp2=(rng.random((hh,w))>.997)[...,None];out=np.where(sp2,np.minimum(out*1.5+.04,1),out)
        return out
    band(0,256,paint)
    # steel: brushed streaks (long horizontally) + micro noise
    def steel(s):
        hh=s.shape[0];m=noise(rng,hh,w,28,.7)*.07+noise(rng,hh,w,.8,.8)*.025
        return s*(1+m[...,None])+0.0
    band(256,320,steel)
    # rubber: grip stipple, dull blotches
    def rubber(s):
        hh=s.shape[0];m=noise(rng,hh,w,.5,.5)*.07+noise(rng,hh,w,6,6)*.03
        dots=(rng.random((hh,w))>.93)[...,None]
        return np.where(dots,s*1.35+.02,s*(1+m[...,None]))
    band(320,384,rubber)
    # wood: long grain + faint rings
    def wood(s):
        hh=s.shape[0];yy=np.arange(hh)[:,None]
        g=noise(rng,hh,w,40,1.2)*.09+np.sin((yy*.55)+noise(rng,hh,w,60,3)*2.2)*.05+noise(rng,hh,w,1,1)*.02
        return s*(1+g[...,None])
    band(384,448,wood)
    im=Image.fromarray((img*255).astype(np.uint8));d=ImageDraw.Draw(im,'RGBA')
    r=random.Random(sum(map(ord,name)))
    # fine scratches / wear on paint and steel
    for _ in range(140):
        y=r.randrange(0,320);x=r.randrange(0,w);l=r.randrange(6,46);a=r.uniform(-.25,.25)
        c=(255,255,255,r.randrange(14,36)) if r.random()<.6 else (0,0,0,r.randrange(18,40))
        d.line([(x,y),(x+l,y+l*a)],fill=c,width=1)
    im.save(os.path.join(DST,name))
if __name__=='__main__':
    files=sorted(glob.glob(os.path.join(SRC,'armory21_*.png')))
    for f in files:enhance(f)
    print('enhanced',len(files))
