"""preview a free-format .bbmodel made of cubes: python3 bb_preview.py model.bbmodel out.png [scale]"""
import json,sys,math,base64,io,numpy as np
from PIL import Image,ImageDraw
bb=json.load(open(sys.argv[1]));src=bb['textures'][0]['source'].split(',')[1];tex=Image.open(io.BytesIO(base64.b64decode(src))).convert('RGB')
K=float(sys.argv[3]) if len(sys.argv)>3 else 20
def R(r):
 x,y,z=[math.radians(a) for a in r]
 Rx=np.array([[1,0,0],[0,math.cos(x),-math.sin(x)],[0,math.sin(x),math.cos(x)]]);Ry=np.array([[math.cos(y),0,math.sin(y)],[0,1,0],[-math.sin(y),0,math.cos(y)]]);Rz=np.array([[math.cos(z),-math.sin(z),0],[math.sin(z),math.cos(z),0],[0,0,1]])
 return Rx@Ry@Rz
polys=[]
for e in bb['elements']:
 f=np.array(e['from']);t=np.array(e['to']);M=R(e['rotation']);o=np.array(e['origin'])
 c=[np.array([a,b,d]) for a in (f[0],t[0]) for b in (f[1],t[1]) for d in (f[2],t[2])]
 P=[M@(p-o)+o for p in c]
 for d,q in {'west':[0,1,3,2],'east':[4,6,7,5],'down':[0,2,6,4],'up':[1,5,7,3],'north':[0,4,5,1],'south':[2,3,7,6]}.items():
  if e['faces'][d]['texture'] is None:continue
  u=e['faces'][d]['uv'];polys.append(([P[i] for i in q],np.array(tex.getpixel((int(u[0])+2,int(u[1])+2)))/255))
allp=np.array([p for Q,_ in polys for p in Q]);C=(allp.min(0)+allp.max(0))/2
L=np.array([.35,.85,.4]);L/=np.linalg.norm(L)
def view(yaw,pitch,W,H):
 a,b=math.radians(yaw),math.radians(pitch)
 Ry=np.array([[math.cos(a),0,math.sin(a)],[0,1,0],[-math.sin(a),0,math.cos(a)]]);Rx=np.array([[1,0,0],[0,math.cos(b),-math.sin(b)],[0,math.sin(b),math.cos(b)]]);RR=Rx@Ry
 img=Image.new('RGB',(W,H),(205,214,226));d=ImageDraw.Draw(img);pr=[]
 for Q,col in polys:
  Qr=[RR@(p-C) for p in Q];n=np.cross(Q[1]-Q[0],Q[2]-Q[0]);nn=np.linalg.norm(n)
  if nn<1e-9:continue
  sh=.45+.55*abs(np.dot(n/nn,L));pr.append((np.mean([q[2] for q in Qr]),Qr,tuple(int(v*255*sh) for v in col)))
 pr.sort(key=lambda t:t[0])
 for _,Qr,c in pr:d.polygon([(W/2+q[0]*K,H/2-q[1]*K) for q in Qr],fill=c,outline=tuple(max(0,v-18) for v in c))
 return img
W,H=int(60*K),int(36*K)
views=[view(0,0,W,H),view(-35,18,W,H),view(180,0,W,H),view(-90,5,int(W*.5),H),view(35,-20,W,H),view(0,85,W,int(H*.6))]
out=Image.new('RGB',(W*2,H*3))
for i,v in enumerate(views):out.paste(v,((i%2)*W,(i//2)*H))
out.save(sys.argv[2])
