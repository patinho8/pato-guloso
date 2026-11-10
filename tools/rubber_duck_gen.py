"""0.88.0: rubber duck textures (entity 64x32 declared, painted 4x) + 16x16 item icons + squeak sounds."""
import numpy as np, subprocess, json, os
from PIL import Image
S=4
rng=np.random.default_rng(3)
PAL={0:dict(body=(250,212,40),dark=(222,168,20),light=(255,240,120),beak=(245,120,20)),
     1:dict(body=(250,140,190),dark=(220,100,160),light=(255,200,225),beak=(245,120,20)),
     2:dict(body=(90,170,245),dark=(55,120,210),light=(170,215,255),beak=(245,150,30)),
     3:dict(body=(230,180,50),dark=(170,120,20),light=(255,238,150),beak=(200,90,20))}
# name, texOffs, size(w,h,d), material
BOX=[('body',(0,0),(6,5,7),'body'),('chest',(26,0),(5,4,1.2),'body'),('tail',(0,12),(3,2.6,2),'body'),
     ('head',(12,12),(4.4,5,4.4),'head'),('beak',(44,16),(3,1.4,2.3),'beak'),('wing_l',(40,0),(.6,2.8,4.6),'wing'),('wing_r',(40,8),(.6,2.8,4.6),'wing')]
def faces(uv,s):
    u,v=uv;w,h,d=s;u1,u2,u3,u4,u5=u+d,u+d+w,u+d+2*w,u+2*d+w,u+2*d+2*w;v1,v2=v+d,v+d+h
    return {'down':(u1,v,u2,v1),'up':(u2,v,u3,v1),'west':(u,v1,u1,v2),'north':(u1,v1,u2,v2),'east':(u2,v1,u4,v2),'south':(u4,v1,u5,v2)}
def tex(var):
    P=PAL[var];img=np.zeros((32*S,64*S,4),np.float32)
    for name,uv,s,m in BOX:
        for fn,f in faces(uv,s).items():
            x0,y0,x1,y1=[int(round(c*S)) for c in f];h,w=y1-y0,x1-x0
            if h<=0 or w<=0:continue
            a=np.zeros((h,w,4),np.float32);a[...,3]=255
            yy,xx=np.mgrid[0:h,0:w]
            col=np.array(P['beak'] if m=='beak' else P['dark'] if m=='wing' else P['body'],np.float32)
            a[...,:3]=col
            if fn=='up':a[...,:3]=np.array(P['light'])*.6+col*.4
            elif fn=='down':a[...,:3]=np.array(P['dark'])
            elif m!='beak':
                g=(yy/max(1,h-1))[...,None]          # lighter on top, darker at the bottom
                a[...,:3]=col*(1-g*.25)+np.array(P['light'])*(1-g)*.25
            # rounded look: darken the edges
            ed=np.minimum.reduce([xx,yy,w-1-xx,h-1-yy]).astype(np.float32);a[...,:3]*=(1-np.clip(1-ed/3,0,1)*.15)[...,None]
            if var==3:   # golden shine
                a[...,:3]+=(np.exp(-((xx-yy*.8-w*.4)/2.5)**2)*60)[...,None]
            if m=='wing':
                a[yy%4==0, :3]*=.9
            if name=='head' and fn=='north':   # two cartoon eyes looking forward
                for ex in (w*.27,w*.73):
                    r=np.hypot(xx-ex,yy-h*.42)
                    a[r<S*.8,:3]=255;a[r<S*.55,:3]=20
                    a[(np.hypot(xx-ex-S*.15,yy-h*.42+S*.2)<S*.18),:3]=255
            if name=='head' and fn in('west','east'):   # side eyes too
                ex=w*.3 if fn=='west' else w*.7
                r=np.hypot(xx-ex,yy-h*.42);a[r<S*.6,:3]=255;a[r<S*.4,:3]=20
            if m=='beak' and fn in('north','west','east'):
                a[yy==h//2,:3]*=.75                     # mouth line
            a[...,:3]+=rng.normal(0,2.5,(h,w))[...,None]
            img[y0:y1,x0:x1]=a
    return Image.fromarray(img.clip(0,255).astype(np.uint8),'RGBA')
def icon(var):
    P=PAL[var];im=Image.new('RGBA',(16,16),(0,0,0,0));px=im.load()
    O=tuple(int(c*.45) for c in P['dark'])+(255,)
    def put(x,y,c):
        if 0<=x<16 and 0<=y<16:px[x,y]=c
    body=set()
    for y in range(16):
        for x in range(16):
            if ((x-7.5)/6.2)**2+((y-10.5)/3.9)**2<=1:body.add((x,y))      # body
            if ((x-5)/3.3)**2+((y-5)/3.2)**2<=1:body.add((x,y))           # head
            if 12<=x<=14 and 6<=y<=9 and y>=15-x-1:body.add((x,y))        # tail
    for (x,y) in body:
        k=(y-2)/13;c=tuple(int(P['light'][i]*(1-k)+P['dark'][i]*k) for i in range(3))+(255,)
        put(x,y,c)
    for (x,y) in body:
        for dx,dy in((1,0),(-1,0),(0,1),(0,-1)):
            if (x+dx,y+dy) not in body:put(x+dx,y+dy,O)
    for (x,y) in((0,5),(1,5),(1,6),(0,6),(0,4)):put(x,y,P['beak']+(255,))
    put(-1+1,5,P['beak']+(255,))
    put(4,4,(20,20,20,255));put(4,3,(255,255,255,255))
    for (x,y) in((7,10),(8,10),(9,10),(8,11),(9,11),(10,11)):put(x,y,P['dark']+(255,))   # wing
    if var==3:
        for (x,y) in((3,7),(10,8)):put(x,y,(255,255,230,255))
    return im
os.makedirs('src/main/resources/assets/mareeletrica/textures/item',exist_ok=True)
for v in range(4):
    tex(v).save(f'src/main/resources/assets/mareeletrica/textures/entity/rubber_duck_{v}.png')
    icon(v).save(f'src/main/resources/assets/mareeletrica/textures/item/rubber_duck_{v}.png')
# item models
A='src/main/resources/assets/mareeletrica/models/item/'
json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/rubber_duck_0"},
 "overrides":[{"predicate":{"mareeletrica:variant":v/4},"model":f"mareeletrica:item/rubber_duck_{v}"} for v in (1,2,3)]},open(A+'rubber_duck.json','w'),indent=1)
for v in (1,2,3):json.dump({"parent":"minecraft:item/generated","textures":{"layer0":f"mareeletrica:item/rubber_duck_{v}"}},open(A+f'rubber_duck_{v}.json','w'),indent=1)
# squeaks
SR=44100
def squeak(seed,dur):
    r=np.random.default_rng(seed);n=int(SR*dur);t=np.arange(n)/SR
    f0=1650+r.random()*300;rise=np.where(t<dur*.35,t/(dur*.35),1-(t-dur*.35)/(dur*.65)*.55)
    f=f0*(0.82+0.3*rise)+40*np.sin(2*np.pi*28*t)
    ph=2*np.pi*np.cumsum(f)/SR
    x=np.sin(ph)+.45*np.sin(2*ph+.3)+.25*np.sin(3*ph)+.12*np.sin(4*ph)
    env=np.minimum(1,t/.012)*np.clip(1-(t-dur*.7)/(dur*.3),0,1)**1.5
    air=r.standard_normal(n)*.12*env
    import scipy.signal as sg
    b,a=sg.butter(2,[1800/(SR/2),6000/(SR/2)],'band');air=sg.lfilter(b,a,air)
    y=(x*.55*env+air);y=np.tanh(y*1.6);y/=np.abs(y).max();y*=.85
    return y
for i,d in enumerate((.22,.3,.18)):
    y=squeak(70+i,d);pcm=(y*32767).astype(np.int16)
    open('/tmp/sq.raw','wb').write(pcm.tobytes())
    subprocess.run(['ffmpeg','-y','-loglevel','error','-f','s16le','-ar',str(SR),'-ac','1','-i','/tmp/sq.raw','-c:a','libvorbis','-q:a','6',f'src/main/resources/assets/mareeletrica/sounds/rubber_squeak{i+1}.ogg'],check=True)
print('ok')
