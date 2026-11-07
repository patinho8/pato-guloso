"""3D knife / banana models: 2D design -> voxel boxes (0.25 unit cells) + position-mapped texture.
Outputs, per knife <id>:
  models/item/<id>.json   (45-degree 'handheld' pose, display transforms)  + textures/item/<id>.png
  models/block/knife_<id>.json (upright, used by the first-person viewmodel) + textures/item/<id>_up.png
The butterfly also gets knife_butterfly_handles / knife_butterfly_blade so the blade can open in hand.
Run:  python3 tools/make_knives.py   (Pillow, numpy, opencv)"""
import json,math,os
import numpy as np,cv2
from PIL import Image,ImageDraw,ImageFilter
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources/assets/mareeletrica')
S=24            # painting pixels per model unit (384 px canvas)
CELL=.25
N=int(16/CELL)
ANG=0.0
SC=1.0
def rot(p):
    c,s=math.cos(ANG),math.sin(ANG);x,y=(p[0]-8)*SC,(p[1]-8)*SC;return (x*c-y*s+8,x*s+y*c+8)
def px(p):q=rot(p);return (q[0]*S,(16-q[1])*S)
class Art:
    def __init__(s):s.im=Image.new('RGBA',(16*S,16*S),(34,34,38,255));s.d=ImageDraw.Draw(s.im)
    def poly(s,pts,fill):s.d.polygon([px(p) for p in pts],fill=fill)
    def line(s,a,b,fill,w=1):s.d.line([px(a),px(b)],fill=fill,width=w)
    def circle(s,c,r,fill,n=28):s.poly([(c[0]+r*math.cos(2*math.pi*i/n),c[1]+r*math.sin(2*math.pi*i/n)) for i in range(n)],fill)
    def grad(s,pts,c0,c1,axis=1,noise=.0):
        m=Image.new('L',s.im.size,0);ImageDraw.Draw(m).polygon([px(p) for p in pts],fill=255)
        yy,xx=np.mgrid[0:s.im.size[1],0:s.im.size[0]];X=xx/S;Y=16-yy/S
        c,sn=math.cos(-ANG),math.sin(-ANG);ux=(X-8)*c-(Y-8)*sn+8;uy=(X-8)*sn+(Y-8)*c+8
        v=ux if axis==0 else uy
        q=np.array([p[axis] for p in pts]);lo,hi=q.min(),q.max()
        t=np.clip((v-lo)/(hi-lo+1e-6),0,1)[...,None]
        arr=np.array(c0)[None,None,:]*(1-t)+np.array(c1)[None,None,:]*t
        if noise:arr=arr*(1+np.random.default_rng(3).standard_normal(arr.shape[:2])[...,None]*noise)
        a=np.concatenate([np.clip(arr,0,255),np.full(arr.shape[:2]+(1,),255)],axis=2).astype(np.uint8)
        s.im.paste(Image.fromarray(a,'RGBA'),(0,0),m)
    def brushed(s,pts,amount=.06):
        m=np.asarray(Image.new('L',s.im.size,0).copy());mm=Image.new('L',s.im.size,0);ImageDraw.Draw(mm).polygon([px(p) for p in pts],fill=255);mk=np.asarray(mm)>0
        rng=np.random.default_rng(11);n=rng.standard_normal(mk.shape).astype(np.float32);n=cv2.GaussianBlur(n,(1,41),sigmaX=0.1,sigmaY=9);n/=n.std()+1e-6
        a=np.asarray(s.im).astype(np.float32);a[...,:3]*=(1+n*amount)[...,None]*mk[...,None]+(~mk)[...,None]
        s.im=Image.fromarray(np.clip(a,0,255).astype(np.uint8),'RGBA');s.d=ImageDraw.Draw(s.im)
    def edges(s,colors):
        for i,c in enumerate(colors):s.d.rectangle([i*S,15*S,(i+1)*S-1,16*S-1],fill=c)
def spline(pts,n=8):
    P=[pts[0]]+pts+[pts[-1]];out=[]
    for i in range(1,len(P)-2):
        for k in range(n):
            t=k/n;p0,p1,p2,p3=P[i-1],P[i],P[i+1],P[i+2]
            out.append(tuple(.5*((2*p1[j])+(-p0[j]+p2[j])*t+(2*p0[j]-5*p1[j]+4*p2[j]-p3[j])*t*t+(-p0[j]+3*p1[j]-3*p2[j]+p3[j])*t**3) for j in (0,1)))
    out.append(pts[-1]);return out
def arc(c,r,a0,a1,n=18):return [(c[0]+r*math.cos(math.radians(a0+(a1-a0)*i/n)),c[1]+r*math.sin(math.radians(a0+(a1-a0)*i/n))) for i in range(n+1)]
def mask(polys,holes=()):
    K=4;im=Image.new('L',(N*K,N*K),0);d=ImageDraw.Draw(im)
    f=lambda q:((rot(q)[0])*N*K/16,(16-rot(q)[1])*N*K/16)
    for p in polys:d.polygon([f(q) for q in p],fill=255)
    for p in holes:d.polygon([f(q) for q in p],fill=0)
    return (np.asarray(im).astype(np.float32)/255).reshape(N,K,N,K).mean(axis=(1,3))>.45
def erode(m,k):return cv2.erode(m.astype(np.uint8),np.ones((2*k+1,2*k+1),np.uint8))>0
def element(x0,y0,x1,y1,z0,z1,edge):
    ep=[edge+.25,15.25,edge+.75,15.75]
    f={'south':{'texture':'#t','uv':[x0,16-y1,x1,16-y0]},'north':{'texture':'#t','uv':[x1,16-y1,x0,16-y0]}}
    for d in ('east','west','up','down'):f[d]={'texture':'#t','uv':ep}
    return {'from':[round(x0,3),round(y0,3),round(z0,3)],'to':[round(x1,3),round(y1,3),round(z1,3)],'faces':f}
def boxes(m,zr,edge):
    """m: boolean cell mask (row 0 = top). zr: list of (z0,z1) slabs."""
    out=[]
    for r in range(N):
        c=0
        while c<N:
            if not m[r,c]:c+=1;continue
            c2=c
            while c2+1<N and m[r,c2+1]:c2+=1
            for z0,z1 in zr:out.append(element(c*CELL,16-(r+1)*CELL,(c2+1)*CELL,16-r*CELL,z0,z1,edge))
            c=c2+1
    return out
def bevel(blade_polys,core_z=(7.6,8.4),edge_z=(7.85,8.15),k=3,edge=0,holes=()):
    m=mask(blade_polys,holes);core=erode(m,k)
    return boxes(core,[core_z],edge)+boxes(m&~core,[edge_z],edge)
HAND={"gui":{"rotation":[0,0,0],"translation":[0,0,0],"scale":[1,1,1]},"ground":{"translation":[0,2,0],"scale":[.55,.55,.55]},"fixed":{"rotation":[0,180,0],"scale":[1,1,1]},"head":{"rotation":[0,180,0],"translation":[0,13,7]},
 "thirdperson_righthand":{"rotation":[0,-90,55],"translation":[0,4,.5],"scale":[.85,.85,.85]},"thirdperson_lefthand":{"rotation":[0,90,-55],"translation":[0,4,.5],"scale":[.85,.85,.85]},
 "firstperson_righthand":{"rotation":[0,-90,25],"translation":[1.13,3.2,1.13],"scale":[.68,.68,.68]},"firstperson_lefthand":{"rotation":[0,90,-25],"translation":[1.13,3.2,1.13],"scale":[.68,.68,.68]}}
def write(kind,name,tex,el,display=None):
    os.makedirs(f'{RES}/models/{kind}',exist_ok=True)
    m={'ambientocclusion':False,'textures':{'t':f'mareeletrica:item/{tex}','particle':f'mareeletrica:item/{tex}'},'elements':el}
    if display:m['display']=display
    json.dump(m,open(f'{RES}/models/{kind}/{name}.json','w'),separators=(',',':'))
def save_tex(name,art):
    os.makedirs(RES+'/textures/item',exist_ok=True);art.im.save(f'{RES}/textures/item/{name}.png')
def preview(tag,el,art):
    out=Image.new('RGBA',(640,640),(18,18,22,255));k=640/16;t=art.im
    for e in el:
        x0,y0,_=e['from'];x1,y1,_=e['to'];u=e['faces']['south']['uv']
        crop=t.crop((int(u[0]*S),int(u[1]*S),max(int(u[2]*S),int(u[0]*S)+1),max(int(u[3]*S),int(u[1]*S)+1))).resize((max(1,round((x1-x0)*k)),max(1,round((y1-y0)*k))))
        out.paste(crop,(round(x0*k),round((16-y1)*k)))
    out.save(f'/tmp/pv_{tag}.png')
# ================= designs (upright coordinates; ANG decides the final tilt) =================
def oval(c,rx,ry,n=20):return [(c[0]+rx*math.cos(2*math.pi*k/n),c[1]+ry*math.sin(2*math.pi*k/n)) for k in range(n)]
PA=(7.05,8.7);PB=(8.95,8.7)   # pivot pins: handle A (held) and handle B (swings)
def butterfly_build(closed,shift=0.0):
    """Real balisong mechanics. Open layout: both handles side by side under the blade.
    Closed layout: blade turned 180 degrees about pin A, handle B carried to the other side of handle A; the blade hides in the seam.
    Returns (art, item_elements, handle_a, handle_b, blade) -- the three parts are only meaningful for the open layout."""
    a=Art()
    def sh(pts):return [(x+shift,y) for x,y in pts]
    def TA(pts):return sh(pts)
    def TB(pts):return sh([(x-3.8,y) for x,y in pts] if closed else pts)
    def TL(pts):return sh([(2*PA[0]-x,2*PA[1]-y) for x,y in pts] if closed else pts)
    def hand(cx):return [(cx-.95,.6),(cx-.65,.3),(cx+.65,.3),(cx+.95,.6)]+arc((cx,8.7),.95,0,180,16)
    hA=hand(PA[0]);hB=hand(PB[0])
    holes=lambda cx:[oval((cx,3.0),.5,1.0,14),oval((cx,5.8),.5,1.0,14)]
    edgeA=[(6.1,.6),(6.45,.6),(6.45,8.7),(6.1,8.7)];edgeB=[(9.55,.6),(9.9,.6),(9.9,8.7),(9.55,8.7)]
    botA=[(6.2,.3),(7.9,.3),(7.9,.75),(6.2,.75)];botB=[(8.1,.3),(9.8,.3),(9.8,.75),(8.1,.75)]
    latch=[(8.35,-.05),(9.55,-.05),(9.55,.35),(8.35,.35)]
    blade=[(7.0,9.7),(9.0,9.7),(9.0,13.4),(8.95,14.3),(8.75,15.1),(8.45,15.9),(8.0,15.4),(7.55,14.8),(7.0,14.2)]
    tang=[(6.6,8.0),(9.4,8.0),(9.4,9.75),(9.0,9.75),(8.85,10.05),(7.15,10.05),(7.0,9.75),(6.6,9.75)]
    # ---------------- paint: blade first, handles over it ----------------
    bl=TL(blade+[]) ;tg=TL(tang)
    a.poly(tg,(120,128,138,255));a.grad(tg,(110,118,128),(150,158,168),0)
    a.poly(bl,(150,158,168,255));a.grad(bl,(150,158,170),(230,236,244),0,.01);a.brushed(bl,.04)
    bev=TL([(8.55,9.8),(9.0,9.8),(9.0,13.4),(8.95,14.3),(8.75,15.1),(8.45,15.9),(8.3,15.3),(8.6,14.2),(8.55,13.3)])
    a.poly(bev,(214,222,232,255));a.brushed(bev,.03)
    for p0,p1,col,w in (((9.0,9.8),(9.0,13.4),(252,254,255,255),1),((7.02,9.8),(7.02,14.15),(92,100,112,255),1),((7.0,14.2),(8.45,15.9),(245,248,252,255),1),((8.55,9.9),(8.55,13.3),(176,184,196,255),1)):
        q=TL([p0,p1]);a.line(q[0],q[1],col,w)
    for zp in ((7.4,10.4),):
        q=TL([zp]);a.circle(q[0],.22,(70,74,82,255),10)
    q=TL([(7.6,11.4),(8.2,11.4)]);a.line(q[0],q[1],(110,116,126,255),1)          # maker's stamp
    q=TL([(7.6,11.7),(8.0,11.7)]);a.line(q[0],q[1],(110,116,126,255),1)
    # handles: titanium, brushed, bevelled outer edge, skeleton holes showing the blade channel
    for T,h,ed,bt,cx in ((TB,hB,edgeB,botB,PB[0]),(TA,hA,edgeA,botA,PA[0])):
        H=T(h);a.poly(H,(110,116,126,255));a.grad(H,(150,156,166),(78,84,96),1,.02);a.brushed(H,.05)
        E=T(ed);a.poly(E,(176,182,192,255));B=T(bt);a.poly(B,(96,100,108,255))
        a.line(T([(cx,1.0)])[0],T([(cx,8.0)])[0],(60,64,72,255),1)
        for o in holes(cx):a.poly(T(o),(18,20,24,255))
        for yy in (1.4,4.4,7.3):a.circle(T([(cx,yy)])[0],.22,(200,204,210,255),10);a.line(T([(cx-.15,yy)])[0],T([(cx+.15,yy)])[0],(80,84,90,255),1)
    a.poly(TB(latch),(26,28,32,255))
    for P,T in ((PA,TA),(PB,TB)):a.circle(T([P])[0],.48,(196,156,74,255),18);a.circle(T([P])[0],.2,(96,70,28,255),10)
    a.edges([(196,204,214,255),(150,156,166,255),(196,156,74,255),(26,28,32,255)])
    # ---------------- geometry ----------------
    def handle(T,h,ed,bt,cx,P,extra=()):
        mh=mask([T(h)],[T(o) for o in holes(cx)])
        strip=mask([T(ed),T(bt)])
        els=boxes(mh&~strip,[(6.7,7.45),(8.55,9.3)],1)+boxes(strip,[(6.7,9.3)],1)
        els+=boxes(mask([T(oval(P,.45,.45,12))]),[(6.55,9.45)],2)
        for poly in extra:els+=boxes(mask([T(poly)]),[(7.2,8.8)],3)
        return els
    hdA=handle(TA,hA,edgeA,botA,PA[0],PA)
    hdB=handle(TB,hB,edgeB,botB,PB[0],PB,(latch,))
    bld=bevel([TL(blade),TL(tang)],(7.75,8.25),(7.88,8.12),3,0)
    return a,hdA+hdB+bld,hdA,hdB,bld
def butterfly(closed):
    a,el,ha,hb,bl=butterfly_build(closed,1.9 if closed else 0.0)
    return a,el,bl,ha+hb
def karambit():
    a=Art()
    back=spline([(6.9,9.2),(7.1,10.8),(7.9,12.4),(9.3,13.6),(11.0,14.2),(12.7,14.0),(14.2,12.9)],6)
    edge=spline([(8.9,9.2),(9.1,10.2),(9.8,11.3),(11.0,12.1),(12.2,12.6),(14.2,12.9)][::-1],6)
    blade=back+edge[1:]
    handle=spline([(6.5,3.4),(6.2,5.0),(6.55,7.0),(6.7,9.2)],5)+spline([(9.2,9.2),(9.45,7.0),(9.1,5.0),(9.3,3.4)],5)
    ring_o=arc((8,2.0),2.15,0,360,28);ring_i=arc((8,2.0),1.1,0,360,22)
    a.poly(handle,(18,60,40,255));a.grad(handle,(30,92,60),(10,34,22),1,.05)
    for yy in (4.0,4.9,5.8,6.7,7.6,8.4):a.line((6.4,yy),(9.4,yy),(6,18,12,255),1)
    a.poly(ring_o,(40,44,50,255));a.poly(ring_i,(34,34,38,255));a.brushed(ring_o,.05)
    a.poly(blade,(120,130,142,255));a.grad(blade,(86,96,108),(238,244,250),1,.02);a.brushed(blade,.06)
    for p,q in zip(edge[:-1],edge[1:]):a.line(p,q,(252,254,255,255),2)
    for p,q in zip(back[:-1],back[1:]):a.line(p,q,(54,60,70,255),2)
    c=spline([(7.7,10.0),(8.5,11.5),(9.8,12.6),(11.4,13.2)],6)
    for p,q in zip(c[:-1],c[1:]):a.line(p,q,(60,70,82,255),1)
    a.circle((7.95,9.55),.5,(222,176,70,255),12)
    a.edges([(96,106,118,255),(12,40,26,255),(222,176,70,255)])
    el=bevel([blade],(7.6,8.4),(7.85,8.15),3,0)+boxes(mask([handle]),[(7.0,9.0)],1)+boxes(mask([ring_o],[ring_i]),[(7.1,8.9)],1)
    return a,el
def xeno():
    """Combat knife (as in the reference photo): straight clip-point blade with a saw-back, ricasso slot,
    small guard with a thumb hook, skeleton handle with three triangle cut-outs and a lanyard cord."""
    a=Art()
    blade=[(6.9,7.0),(6.85,12.6),(6.95,14.6),(7.25,16.3),(8.3,15.3),(9.3,14.0),(9.9,13.1),(9.9,7.0)]
    teeth=[]
    for k in range(7):
        y=9.4+k*.5;teeth.append([(9.95,y),(9.45,y+.18),(9.95,y+.36)])
    slot=oval((8.35,7.95),.32,.85,14)
    guard=[(6.2,6.35),(10.3,6.35),(10.3,7.05),(6.2,7.05)]
    hook=[(9.7,7.0),(10.25,7.0),(11.0,8.3),(10.7,8.5)]
    handle=[(6.85,.6),(9.65,.6),(9.95,6.35),(6.65,6.35)]
    tris=[[(7.6,1.75),(8.9,1.75),(8.25,2.85)],[(7.6,4.15),(8.25,3.05),(8.9,4.15)],[(7.6,4.35),(8.9,4.35),(8.25,5.45)]]
    lan=oval((8.25,1.15),.3,.3,10)
    cord=[(9.4,1.3),(9.8,1.1),(10.6,.25),(10.3,.05),(9.5,.85)]
    # paint
    a.poly(blade,(150,156,162,255));a.grad(blade,(118,124,130),(206,212,218),0,.02);a.brushed(blade,.05)
    bev=[(6.9,7.6),(7.75,7.6),(7.75,13.0),(7.95,15.0),(7.3,16.1),(6.95,14.6),(6.85,12.6)]
    a.poly(bev,(214,220,226,255));a.brushed(bev,.03)
    a.line((6.9,7.2),(6.9,14.4),(250,252,255,255),2)
    for t in teeth:a.poly(t,(40,42,46,255))
    a.poly(slot,(34,34,38,255))
    a.poly(guard,(60,64,66,255));a.poly(hook,(60,64,66,255));a.brushed(guard,.05)
    a.poly(handle,(64,70,72,255));a.grad(handle,(78,84,86),(40,44,46),0,.04)
    for t in tris:a.poly(t,(34,34,38,255))
    a.circle((8.3,5.85),.34,(150,156,160,255),12)
    a.poly(lan,(34,34,38,255));a.poly(cord,(22,22,24,255))
    a.edges([(110,116,122,255),(44,48,50,255),(22,22,24,255)])
    el=bevel([blade],(7.6,8.4),(7.85,8.15),3,0,holes=teeth+[slot])
    el+=boxes(mask([handle],tris+[lan]),[(7.15,8.85)],1)+boxes(mask([guard,hook]),[(6.9,9.1)],1)+boxes(mask([cord]),[(7.8,8.2)],2)
    return a,el
def hrt3():
    a=Art()
    blade=spline([(6.9,6.4),(6.9,12.4),(7.4,14.4),(8.6,15.8),(10.0,16.3)],5)+[(10.0,16.3)]+spline([(10.0,16.3),(10.0,12.8),(9.9,6.4)][::1],3)
    blade=[(6.9,6.4),(6.9,13.2),(7.3,15.0),(8.4,16.1),(9.6,16.5),(10.1,15.6),(10.1,6.4)]
    handle=[(6.6,.8),(10.1,.8),(9.8,2.4),(10.2,3.8),(9.8,5.2),(10.0,6.4),(6.8,6.4),(6.4,4.2)]
    guard=[(6.2,6.4),(10.6,6.4),(10.6,7.2),(6.2,7.2)]
    a.poly(blade,(30,32,36,255));a.grad(blade,(26,28,32),(78,84,92),0,.03);a.brushed(blade,.04)
    a.poly([(7.1,7.4),(9.7,7.4),(9.7,12.4),(9.3,14.8),(8.6,15.6),(7.4,13.6)],(46,50,58,255))
    a.line((10.0,7.6),(10.0,15.4),(176,182,192,255),2)
    for yy in [7.6,8.0,8.4,8.8,9.2,9.6]:a.poly([(10.1,yy),(9.5,yy+.12),(10.1,yy+.3)],(10,10,12,255))
    for yy in [8.4,8.7,9.0,9.3,9.6,9.9,10.2]:a.line((6.9,yy),(7.2,yy+.1),(176,182,192,255),1)
    a.grad(handle,(72,92,56),(40,56,34),1,.06)
    for yy in (1.6,2.2,2.8,3.4,4.0,4.6,5.2,5.8):a.line((6.6,yy),(10.1,yy),(20,30,16,255),1)
    a.circle((8.3,1.5),.42,(12,14,10,255),12)
    a.poly(guard,(28,30,34,255));a.brushed(guard,.05)
    for x in (7.0,10.0):a.circle((x,3.6),.2,(200,200,205,255),8)
    a.edges([(54,58,66,255),(40,56,34,255),(28,30,34,255)])
    el=bevel([blade],(7.55,8.45),(7.8,8.2),3,0)+boxes(mask([handle],[arc((8.3,1.5),.42,0,360,10)]),[(7.0,9.0)],1)+boxes(mask([guard]),[(6.7,9.3)],2)
    return a,el
def banana():
    a=Art()
    P0,P1,P2=(3.4,1.4),(1.0,10.6),(12.6,15.2)
    def bez(t):return tuple((1-t)**2*P0[i]+2*(1-t)*t*P1[i]+t*t*P2[i] for i in (0,1))
    smp=[bez(i/240) for i in range(241)]
    def width(t):return 1.95*math.sin(math.pi*min(max(t,0),1))**.5+.22
    L=[];R=[]
    for i in range(0,241,6):
        t=i/240;p0=bez(max(t-.01,0));p1=bez(min(t+.01,1));dx,dy=p1[0]-p0[0],p1[1]-p0[1];ln=math.hypot(dx,dy);nx,ny=-dy/ln,dx/ln;w=width(t);p=bez(t)
        L.append((p[0]+nx*w,p[1]+ny*w));R.append((p[0]-nx*w,p[1]-ny*w))
    body=L+R[::-1]
    stem=[(11.9,14.6),(13.0,15.4),(13.6,16.3),(12.7,16.4),(11.5,15.4)];tip=[(2.6,.8),(4.0,.8),(4.1,1.9),(2.8,2.0)]
    yy,xx=np.mgrid[0:16*S,0:16*S];X=xx/S;Y=16-yy/S;c,sn=math.cos(-ANG),math.sin(-ANG)
    ux=(X-8)*c-(Y-8)*sn+8;uy=(X-8)*sn+(Y-8)*c+8
    best=np.full(X.shape,1e9);bt=np.zeros(X.shape);sg=np.zeros(X.shape)
    for i,(sx,sy) in enumerate(smp):
        dd=(ux-sx)**2+(uy-sy)**2;m=dd<best;best=np.where(m,dd,best);bt=np.where(m,i/240,bt)
    dist=np.sqrt(best);w=np.array([width(t) for t in np.linspace(0,1,241)])[(bt*240).astype(int)]
    r=np.clip(dist/np.maximum(w,.05),0,1);shade=1-.42*r**2
    ridges=1-.10*(np.abs(np.sin(r*5.2))>.93)
    col=np.array([250,214,48])[None,None,:]*(shade*ridges)[...,None]
    col=np.where((bt<.06)[...,None],col*np.array([.55,.5,.4]),col);col=np.where((bt>.92)[...,None],col*np.array([.72,.8,.4]),col)
    arr=np.zeros((16*S,16*S,4),np.uint8);arr[...]=(34,34,38,255);arr[...,:3]=np.clip(col,0,255).astype(np.uint8)
    a.im=Image.fromarray(arr,'RGBA');a.d=ImageDraw.Draw(a.im)
    rng=np.random.default_rng(5)
    for _ in range(60):
        t=rng.random()*.9+.05;p=bez(t);a.circle((p[0]+rng.normal(0,.5),p[1]+rng.normal(0,.5)),.05,(110,72,22,255),6)
    a.poly(stem,(88,104,38,255));a.poly(tip,(36,24,14,255))
    a.edges([(214,174,34,255),(88,104,38,255),(36,24,14,255)])
    el=[]
    mb=mask([body]);ms=mask([stem]);mt=mask([tip])
    for rr in range(N):
        for cc in range(N):
            if not(mb[rr,cc] or ms[rr,cc] or mt[rr,cc]):continue
            cx=(cc+.5)*CELL;cy=16-(rr+.5)*CELL
            qx=(cx-8)*math.cos(-ANG)-(cy-8)*math.sin(-ANG)+8;qy=(cx-8)*math.sin(-ANG)+(cy-8)*math.cos(-ANG)+8
            j=min(range(0,241,2),key=lambda i:(qx-smp[i][0])**2+(qy-smp[i][1])**2);t=j/240
            d=math.hypot(qx-smp[j][0],qy-smp[j][1]);ww=max(width(t),.25);rr_=min(d/ww,1)
            half=max(.2,1.5*math.sqrt(max(1-rr_*rr_,0))*(min(1,math.sin(math.pi*t)**.3+.2)))
            edge=2 if mt[rr,cc] else (1 if ms[rr,cc] else 0)
            el.append(element(cc*CELL,16-(rr+1)*CELL,(cc+1)*CELL,16-rr*CELL,8-half,8+half,edge))
    return a,el
def build(name,fn,tilt):
    global ANG,SC
    ANG=math.radians(-45) if tilt else 0.0;SC=.86 if tilt else 1.0
    return fn()
if __name__=='__main__':
    # item models (tilted) + viewmodel models (upright)
    KARAMBIT_DISPLAY={"gui":{"rotation":[0,0,-35],"scale":[.9,.9,.9]},"ground":{"translation":[0,2,0],"scale":[.55,.55,.55]},"fixed":{"rotation":[0,180,0]},"head":{"rotation":[0,180,0],"translation":[0,13,7]},
     "thirdperson_righthand":{"rotation":[0,-90,0],"translation":[0,2,.5],"scale":[.85,.85,.85]},"thirdperson_lefthand":{"rotation":[0,90,0],"translation":[0,2,.5],"scale":[.85,.85,.85]},
     "firstperson_righthand":{"rotation":[0,-90,25],"translation":[1.13,3.2,1.13],"scale":[.68,.68,.68]},"firstperson_lefthand":{"rotation":[0,90,-25],"translation":[1.13,3.2,1.13],"scale":[.68,.68,.68]}}
    for name,fn in (('karambit',karambit),('xeno_hunter',xeno),('hrt3_knife',hrt3),('banana',banana)):
        if name=='karambit':
            a,el=build(name,fn,False);save_tex('karambit',a);write('item','karambit','karambit',el,KARAMBIT_DISPLAY);preview('karambit',el,a)
        else:
            a,el=build(name,fn,True);save_tex(name,a);write('item',name,name,el,HAND);preview(name,el,a)
        a,el=build(name,fn,False)
        if name=='xeno_hunter':   # longer blade in hand: stretch everything above the guard 30%
            f=lambda y:y if y<=7.0 else 7.0+(y-7.0)*1.3
            for e in el:e['from'][1]=round(f(e['from'][1]),3);e['to'][1]=round(f(e['to'][1]),3)
        save_tex(name+'_up',a);write('block','knife_'+name,name+'_up',el);preview(name+'_up',el,a)
        print(name,len(el))
    # butterfly: closed item model (inventory / third person), open-layout parts for the first-person viewmodel
    ANG=math.radians(-45);SC=.80;a,el,_,_,_=butterfly_build(False);save_tex('butterfly_knife',a);write('item','butterfly_knife','butterfly_knife',el,HAND);preview('butterfly_knife',el,a)
    ANG=0.0;SC=1.0;a,el,ha,hb,bl=butterfly_build(False);save_tex('butterfly_knife_up',a)
    write('block','knife_butterfly_handle_a','butterfly_knife_up',ha);write('block','knife_butterfly_handle_b','butterfly_knife_up',hb);write('block','knife_butterfly_blade','butterfly_knife_up',bl)
    preview('butterfly_open',el,a)
    ANG=0.0;SC=1.0;a,elc,_,_,_=butterfly_build(True,1.9);preview('butterfly_closed_up',elc,a)
    print('butterfly',len(el),len(ha),len(hb),len(bl))
