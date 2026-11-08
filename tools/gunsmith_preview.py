"""quick painter's-algorithm preview of gunsmith models (side + 3/4 view)"""
import sys,math,numpy as np
from PIL import Image,ImageDraw
sys.path.insert(0,'.');import gunsmith_072 as g
COL={'paint':(110,124,140),'metal':(58,62,68),'rubber':(30,30,33),'wood':(130,80,40),'red':(200,40,40)}
def mat_of(e):
 u=e['faces']['east']['uv'];v=u[1]*32
 return 'paint' if v<256 else 'metal' if v<320 else 'rubber' if v<384 else 'wood' if v<448 else 'red'
def polys(es):
 out=[]
 for e in es:
  f=np.array(e['from']);t=np.array(e['to']);c=[np.array([a,b,d]) for a in (f[0],t[0]) for b in (f[1],t[1]) for d in (f[2],t[2])]
  M=np.eye(3);o=np.zeros(3)
  if 'rotation' in e:
   r=e['rotation'];a=math.radians(r['angle']);o=np.array(r['origin']);ca,sa=math.cos(a),math.sin(a)
   M={'x':np.array([[1,0,0],[0,ca,-sa],[0,sa,ca]]),'y':np.array([[ca,0,sa],[0,1,0],[-sa,0,ca]]),'z':np.array([[ca,-sa,0],[sa,ca,0],[0,0,1]])}[r['axis']]
  P=[M@(p-o)+o for p in c];col=COL[mat_of(e)]
  for q in ([0,1,3,2],[4,6,7,5],[0,2,6,4],[1,5,7,3],[0,4,5,1],[2,3,7,6]):out.append(([P[i] for i in q],col))
 return out
def render(es,yaw,pitch,W,H,k):
 a,b=math.radians(yaw),math.radians(pitch)
 Ry=np.array([[math.cos(a),0,math.sin(a)],[0,1,0],[-math.sin(a),0,math.cos(a)]]);Rx=np.array([[1,0,0],[0,math.cos(b),-math.sin(b)],[0,math.sin(b),math.cos(b)]]);R=Rx@Ry
 L=np.array([.3,.8,.5]);L/=np.linalg.norm(L)
 img=Image.new('RGB',(W,H),(205,214,226));d=ImageDraw.Draw(img);pr=[]
 for P,c in polys(es):
  Q=[R@(p-np.array([8,8,4])) for p in P];n=np.cross(Q[1]-Q[0],Q[2]-Q[0]);nn=np.linalg.norm(n)
  if nn<1e-9:continue
  n/=nn;sh=.5+.5*abs(np.dot(R@L if False else L,n))
  pr.append((np.mean([q[2] for q in Q]),Q,tuple(int(v*sh) for v in c)))
 pr.sort(key=lambda t:t[0])
 for _,Q,c in pr:d.polygon([(W/2+q[0]*k,H/2-q[1]*k) for q in Q],fill=c,outline=tuple(max(0,v-20) for v in c))
 return img
if __name__=='__main__':
 names=sys.argv[2:];out=sys.argv[1];tiles=[]
 for n in names:
  r=getattr(g,n)();es=r['body']+r['move']+r['mag']
  tiles.append(render(es,-90,0,700,260,15));tiles.append(render(es,-60,22,700,260,13))
 W=1400;H=260*len(names);img=Image.new('RGB',(W,H))
 for i,t in enumerate(tiles):img.paste(t,((i%2)*700,(i//2)*260))
 img.save(out)
