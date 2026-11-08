"""Remington 870 — receptor de aço, cano com faixa ventilada e massa de ponto, tubo de cartuchos, telha de bombear (anda no tiro), coronha de madeira."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('m870',{'steel':(46,48,52),'wood':(118,66,32),'wood2':(92,50,24),'polish':(140,144,150),'dark':(10,10,12),'brass':(196,156,64),
 'shell':(170,30,30),'white':(236,236,236),'rubber':(26,24,22),'engrave':(100,102,106),'gold':(210,170,70)},seed=870)
c=K.cube;both=K.both;tube=K.tube;B=35
G='receptor'
c(G,'receptor',[-32,26.4,-2.8],[0,37.4,2.8],'steel')
both(G,'receptor_chanfro',-32,36.2,0,38.2,1.8,3.0,'steel',rot=[40,0,0],origin=[-16,37.2,2.4])
c(G,'receptor_topo',[-32,37.4,-1.8],[0,38.2,1.8],'steel')
ry=K.rail(G,-28,-4,38.2,w=1.7)
c(G,'alca_base',[-9,ry,-1.7],[-4,ry+.6,1.7],'steel');IRON=K.peep(G,-6.5,ry+.6,w=1.3,h=2.6)
c(G,'janela',[-20,31.2,2.78],[-8,35.6,2.9],'dark');c(G,'ferrolho_face',[-19,31.8,2.5],[-9,35,2.8],'polish')
c(G,'porta_carga',[-22,25.6,-1.6],[-6,26.5,1.6],'dark')
c(G,'trava_bomba',[-6,25.4,-.6],[-4,26.4,.6],'polish')
for x in (-26,-14):both(G,'pino',x-.45,27.6,x+.45,28.5,2.8,2.95,'polish')
K.text(G,-28,29.6,-2.82,[.5,.5,.5,.5,.5,.5,.5,.5,.5],h=1.0,s=-1)
c(G,'gravacao_moldura',[-29,28.6,-2.86],[-4,28.8,-2.8],'gold')
K.trigger(G,-22,26.4,guard=True,gmat='steel',w=1.1,L=10,depth=5.6)
c(G,'seguranca',[-28,25.4,-3.0],[-26.4,26.4,3.0],'polish')
G='cano'
tube(G,'cano',B,0,0,84,1.55,'steel')
c(G,'faixa',[2,36.8,-.9],[84,37.4,.9],'steel')
for x in range(4,84,5):c(G,'faixa_poste',[x,36.2,-.5],[x+1,36.9,.5],'steel')
K.post(G,81,37.2,IRON,ears=True)
tube(G,'tubo_carregador',30.6,0,0,62,1.45,'steel');tube(G,'tubo_tampa',30.6,0,62,65,1.6,'polish')
c(G,'braco_tubo',[60,30.4,-1],[64,34.6,1],'steel')
tube(G,'boca',B,0,83.95,84.1,1.15,'dark')
G='coronha'
c(G,'pescoco',[-44,24,-2.4],[-32,33,2.4],'wood',rot=[0,0,-10],origin=[-32,30,0])
c(G,'coronha',[-82,22,-2.6],[-42,32,2.6],'wood',rot=[0,0,-5],origin=[-42,30,0])
c(G,'coronha_topo',[-82,30,-2.4],[-40,35.6,2.4],'wood',rot=[0,0,-4],origin=[-40,34,0])
c(G,'soleira',[-85,18.6,-2.9],[-82,36.4,2.9],'rubber')
c(G,'soleira_linha',[-82.2,18.8,-2.95],[-81.8,36.2,2.95],'white')
K.checker(G,-40,26,4,3,1.4,2.4,mat='wood2',rot=[0,0,-10],origin=[-32,30,0])
G='ferrolho'
c(G,'telha',[10,27.6,-2.5],[30,33.6,2.5],'wood')
for k in range(9):both(G,'telha_sulco',11+k*2.1,28,12+k*2.1,33.2,2.5,2.62,'wood2')
c(G,'telha_frente',[29.4,28.0,-2.3],[30.6,33.2,2.3],'wood2');c(G,'telha_tras',[9.4,28.0,-2.3],[10.6,33.2,2.3],'wood2')
both(G,'barra_acao',-4,30.0,10,31,1.2,1.8,'polish')
G='carregador'
c(G,'cartucho',[-18,24.2,-1.3],[-8,26.6,1.3],'shell');c(G,'cartucho_base',[-19,24.0,-1.4],[-18,26.8,1.4],'brass')
K.finish(scale=.36,support=(20,27),grip=(-38,28),muzzle_x=84.1,bore_y=B,iron_y=IRON,rail=(ry,-26,-10),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'steel':'main','wood':'accent','wood2':'accent'},regions={'barrel':0,'stock':-32})
