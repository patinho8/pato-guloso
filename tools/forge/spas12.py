"""Franchi SPAS-12 — receptor de alumínio, protetor de calor ventilado sobre o cano, telha de bombear nervurada, punho de pistola e coronha dobrável com gancho."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('spas12',{'alu':(44,46,50),'steel':(52,54,58),'poly':(34,35,38),'polish':(140,144,150),'dark':(10,10,12),'brass':(196,156,64),
 'shell':(170,30,30),'white':(236,236,236),'rubber':(24,24,26),'engrave':(100,102,106)},seed=12)
c=K.cube;both=K.both;tube=K.tube;B=35
G='receptor'
c(G,'receptor',[-32,25.8,-3.0],[0,38,3.0],'alu')
both(G,'receptor_chanfro',-32,37,0,39,2.0,3.2,'alu',rot=[40,0,0],origin=[-16,38,2.6])
c(G,'receptor_topo',[-32,38,-2.0],[0,38.8,2.0],'alu')
c(G,'janela',[-20,31,2.98],[-8,36,3.1],'dark');c(G,'ferrolho_face',[-19,31.6,2.7],[-9,35.4,3.0],'polish')
c(G,'seletor_modo',[-4,26.4,-3.6],[0,28.6,3.6],'polish')
for x in (-28,-6):K.screw(G,x,27.2,3.0,.5)
K.text(G,-26,32,-3.02,[.5,.5,.5,.5,.3,.5,.5],h=1.4,s=-1)
K.trigger(G,-20,25.8,guard=True,gmat='alu',w=1.3,L=11,depth=6.2)
c(G,'trava',[-12,23,-1.4],[-10,24.6,1.4],'polish')
GR=dict(rot=[0,0,14],origin=[-28,25.8,0])
c(G,'punho',[-32,8,-2.6],[-24,26,2.6],'poly',**GR)
K.checker(G,-31,10,3,8,1.9,2.6,**GR)
c(G,'punho_base',[-32.4,7.2,-2.8],[-23.6,8.2,2.8],'poly',**GR)
G='miras'
c(G,'alca_base',[-30,38.8,-1.8],[-25,39.8,1.8],'alu');IRON=K.peep(G,-27.5,39.8,w=1.4,h=3.0,mat='alu')
ry=38.8
G='cano'
tube(G,'cano',B,0,0,72,1.5,'steel')
c(G,'protetor',[0,33.6,-2.4],[60,38.6,2.4],'alu')
for k in range(11):both(G,'protetor_furo',3+k*5,35,5+k*5,37.4,2.38,2.46,'dark');c(G,'protetor_furo_topo',[3+k*5,38.58,-1.0],[5+k*5,38.66,1.0],'dark')
c(G,'protetor_frente',[60,34,-2.0],[62,38,2.0],'alu')
c(G,'massa_base',[66,36.4,-1.2],[70,37.4,1.2],'steel');K.post(G,68,37.4,IRON,mat='steel')
tube(G,'tubo_carregador',30.6,0,0,60,1.5,'steel');tube(G,'tubo_tampa',30.6,0,60,63,1.7,'polish')
c(G,'olhal',[60,27.6,-.8],[62,29.2,.8],'polish')
tube(G,'boca',B,0,71.95,72.1,1.1,'dark')
G='coronha'
c(G,'coronha_base',[-36,30,-2.6],[-32,38,2.6],'alu')
both(G,'coronha_braco',-74,36.4,-36,37.6,1.8,2.6,'steel')
both(G,'coronha_braco_inf',-74,26.6,-36,27.8,1.8,2.6,'steel',rot=[0,0,0])
c(G,'coronha_soleira',[-77,24,-2.8],[-74,39,2.8],'steel')
c(G,'coronha_borracha',[-78,24.6,-2.6],[-77,38.4,2.6],'rubber')
c(G,'gancho',[-74,38,-.8],[-64,39.2,.8],'steel');c(G,'gancho_ponta',[-64,35,-.8],[-62.6,39.2,.8],'steel',rot=[0,0,-20],origin=[-63.3,39,0])
G='ferrolho'
c(G,'telha',[8,27.2,-2.7],[30,34.0,2.7],'poly')
for k in range(10):c(G,'telha_nervura',[9+k*2.1,26.6,-2.8],[10+k*2.1,34.4,2.8],'poly')
G='carregador'
c(G,'cartucho',[-18,24.2,-1.3],[-8,26.6,1.3],'shell');c(G,'cartucho_base',[-19,24.0,-1.4],[-18,26.8,1.4],'brass')
K.finish(scale=.235,support=(19,26.4),grip=(-28,25.8),muzzle_x=72.1,bore_y=B,iron_y=IRON,rail=(ry,-30,-4),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'alu':'main','steel':'main2','poly':'accent'},regions={'barrel':0,'stock':-32})
