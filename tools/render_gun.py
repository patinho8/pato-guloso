"""Prévia de uma arma importada (OBJ do arsenal): python3 tools/render_gun.py <family> <tag> <yaw> <pitch> <saida.png> [W H]"""
import sys,os,math
import numpy as np
from PIL import Image,ImageDraw
A=os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),'src/main/resources/assets/mareeletrica')
def load(fam,tag):
    V=[];F=[];cur=None
    for l in open(f'{A}/models/block/armory21_{fam}_{tag}_body.obj'):
        p=l.split()
        if not p:continue
        if p[0]=='v':V.append(np.array(list(map(float,p[1:4])))*16)
        elif p[0]=='usemtl':cur=p[1]
        elif p[0]=='f':F.append(([V[int(x.split('/')[0])-1] for x in p[1:]],cur))
    cols={m:np.array(Image.open(f'{A}/textures/block/{fam}_{tag}_{m}.png').convert('RGB')).reshape(-1,3).mean(0) for _,m in F}
    return F,cols
def render(fam,tag,yaw,pitch,W=900,H=330):
    F,cols=load(fam,tag);a,b=math.radians(yaw),math.radians(pitch)
    Ry=np.array([[math.cos(a),0,math.sin(a)],[0,1,0],[-math.sin(a),0,math.cos(a)]]);Rx=np.array([[1,0,0],[0,math.cos(b),-math.sin(b)],[0,math.sin(b),math.cos(b)]]);R=Rx@Ry
    allp=np.array([p for P,_ in F for p in P]);c=(allp.min(0)+allp.max(0))/2
    Lv=np.array([.3,.8,.5]);Lv/=np.linalg.norm(Lv);pr=[];pts=[]
    for P,m in F:
        Q=[R@np.array([-(p[2]-c[2]),p[1]-c[1],p[0]-c[0]]) for p in P]
        n=np.cross(Q[1]-Q[0],Q[2]-Q[0]);nn=np.linalg.norm(n)
        if nn==0 or n[2]<0:continue
        sh=.55+.45*max(0,np.dot(n/nn,Lv));pr.append((np.mean([q[2] for q in Q]),Q,np.clip(cols[m]*sh*1.15,0,255)));pts+=Q
    pts=np.array(pts);k=min(W*.92/np.ptp(pts[:,0]),H*.85/np.ptp(pts[:,1]))
    cx=(pts[:,0].max()+pts[:,0].min())/2;cy=(pts[:,1].max()+pts[:,1].min())/2
    pr.sort(key=lambda t:t[0]);img=Image.new('RGB',(W,H),(214,222,232));d=ImageDraw.Draw(img)
    for _,Q,c2 in pr:
        col=tuple(int(v) for v in c2);d.polygon([(W/2+(q[0]-cx)*k,H/2-(q[1]-cy)*k) for q in Q],fill=col,outline=tuple(max(0,v-18) for v in col))
    return img
if __name__=='__main__':
    a=sys.argv;W,H=(int(a[6]),int(a[7])) if len(a)>7 else (900,330)
    render(a[1],a[2],float(a[3]),float(a[4]),W,H).save(a[5])
