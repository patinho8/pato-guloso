"""Colt Python .357 6" — cano com faixa ventilada e trava inferior longa, tambor com caneluras (abre para a esquerda na recarga), cão com espora, punho de madeira com quadriculado."""
import sys,os,math,json;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('python',{'steel':(150,154,162),'steel2':(120,124,132),'wood':(110,62,30),'wood2':(84,46,22),'polish':(200,204,210),'dark':(14,14,16),'brass':(196,156,64),
 'white':(236,236,236),'red':(200,30,30),'blue':(40,44,52),'engrave':(96,98,104)},seed=357,brushed=('steel','steel2','polish'))
c=K.cube;both=K.both;tube=K.tube
CY=28.0;B=CY+3.0   # cylinder axis and the top chamber (bore)
G='armacao'
c(G,'cinta_topo',[-14,B+1.4,-1.8],[1.6,B+3.2,1.8],'steel')
c(G,'armacao_tras',[-17,21.4,-2.1],[-8,B+2.6,2.1],'steel')
c(G,'armacao_baixo',[-8,21.4,-1.9],[1.6,23.6,1.9],'steel')
c(G,'armacao_frente',[0,21.4,-2.0],[2.6,B+3.2,2.0],'steel')
c(G,'janela_tambor',[-8,23.6,-1.95],[0,B+1.4,1.95],'dark') if False else None
c(G,'trava_tambor',[-12,26.4,-2.5],[-9.4,27.6,-2.1],'polish')
for x,y in ((-14,24),(-11,29.6),(-5,22.4)):both(G,'parafuso',x-.4,y-.4,x+.4,y+.4,2.0,2.14,'polish')
c(G,'alca',[-13.6,B+3.2,-1.2],[-10.6,B+4.6,1.2],'blue');c(G,'alca_entalhe',[-13.65,B+4.0,-.35],[-10.55,B+4.65,.35],'dark')
IRON=B+4.5
K.text(G,-16,24,2.12,[.4,.4,.4,.4,.4,.4],h=.7,s=1)
c(G,'cao',[-19.6,B-.6,-.8],[-16.4,B+3.4,.8],'steel2',rot=[0,0,24],origin=[-17,B-1,0])
c(G,'cao_espora',[-21.6,B+2.2,-.9],[-18.4,B+3.6,.9],'steel2',rot=[0,0,24],origin=[-17,B-1,0])
for k in range(3):c(G,'cao_estria',[-21.4+k*.9,B+3.6,-.85],[-21.0+k*.9,B+3.8,.85],'dark',rot=[0,0,24],origin=[-17,B-1,0])
K.trigger(G,-9.6,21.4,guard=True,gmat='steel',w=1.0,L=9.4,depth=5.0)
GR=dict(rot=[0,0,22],origin=[-15.4,21.4,0])
c(G,'punho_armacao',[-19,4,-1.6],[-12,21.6,1.6],'steel',**GR)
both(G,'punho_madeira',-19.6,2.4,-11.2,20.4,1.6,2.6,'wood',**GR)
K.checker(G,-18.8,4.4,5,9,1.5,2.6,mat='wood2',**GR)
both(G,'medalhao',-16.4,15.4,-14.4,17.4,2.6,2.7,'polish',**GR)
c(G,'punho_base',[-19.6,1.8,-2.3],[-11.2,2.6,2.3],'steel',**GR)
G='cano'
tube(G,'cano',B,0,2.6,32,1.35,'steel')
c(G,'cano_trava',[2.6,B-4.2,-1.5],[32,B-.4,1.5],'steel')
both(G,'trava_chanfro',2.6,B-4.6,32,B-3.4,.8,1.6,'steel',rot=[-40,0,0],origin=[17,B-4,1.2])
tube(G,'haste_ejetor',B-3.2,0,2.6,10,.55,'polish')
c(G,'faixa',[2.6,B+2.0,-.9],[32,B+2.8,.9],'steel')
for x in range(4,31,5):c(G,'faixa_vent',[x,B+1.3,-.95],[x+3,B+2.0,.95],'dark')
c(G,'massa_rampa',[27,B+2.8,-.45],[31.6,B+3.8,.45],'steel',rot=[0,0,8],origin=[31.6,B+2.8,0])
c(G,'massa',[29.8,B+2.8,-.45],[31.6,IRON,.45],'steel');c(G,'massa_insert',[29.75,IRON-1.2,-.3],[29.85,IRON-.2,.3],'red')
K.text(G,6,B-3.8,-1.52,[.6,.6,.6,.6,.6,.6],h=1.0,s=-1)
tube(G,'boca',B,0,31.95,32.1,.8,'dark')
G='tambor'
for a in range(0,180,45):
    h=3.9*math.tan(math.pi/8);c(G,'tambor',[-7.8,CY-h,-3.9],[-.2,CY+h,3.9],'steel',rot=[a,0,0] if a else None,origin=[-4,CY,0])
for k in range(6):
    a=math.radians(k*60+30);yy=CY+math.sin(a)*3.75;zz=math.cos(a)*3.75
    c(G,'tambor_canelura',[-6.4,yy-.45,zz-.45],[-1.6,yy+.45,zz+.45],'dark',rot=[k*60+30,0,0],origin=[-4,yy,zz])
tube(G,'tambor_eixo',CY,0,-.2,.6,1.0,'polish')
G='ferrolho'
c(G,'ferrolho_vazio',[-16,B+3.0,-.1],[-15.9,B+3.1,.1],'steel')
Z,Y,SX=K.finish(scale=.4,grip=(-15.4,21.4),muzzle_x=32.1,bore_y=B,iron_y=IRON,rail=(B+3.2,-13,0),moving=['ferrolho'],magazine=['tambor'],stock=[],
 roles={'steel':'main','steel2':'main2','wood':'accent','wood2':'accent'},pistol=True,regions={'barrel':2.6})
# the six rounds that drop out / get loaded with the speedloader (java model, same space as the gun)
els=[]
for k in range(6):
    a=math.radians(k*60+90);yy=CY+math.sin(a)*3.0;zz=math.cos(a)*3.0;r=.85
    x0,x1=-7.9,-.6
    f=[8+(zz-r)*SX,Y(yy-r),Z(x1)];t=[8+(zz+r)*SX,Y(yy+r),Z(x0)]
    els.append({"name":"round","from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#b"} for d in ("north","south","east","west","up","down")}})
    f2=[8+(zz-r*1.15)*SX,Y(yy-r*1.15),Z(x0)];t2=[8+(zz+r*1.15)*SX,Y(yy+r*1.15),Z(x0)+.25]
    els.append({"name":"round_rim","from":[round(v,3) for v in f2],"to":[round(v,3) for v in t2],"faces":{d:{"texture":"#b"} for d in ("north","south","east","west","up","down")}})
root=os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
json.dump({"textures":{"b":"minecraft:block/gold_block","particle":"minecraft:block/gold_block"},"elements":els},open(os.path.join(root,'src/main/resources/assets/mareeletrica/models/block/armory21_python_rounds_0.json'),'w'))
print('cylinder centre px',round(Y(CY),2),'(old 8.7)')
