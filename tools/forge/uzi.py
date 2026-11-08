"""Uzi — receptor caixa com nervuras, tampa com botão de armar em cima, carregador no punho, coronha de metal rebatível."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('uzi',{'steel':(56,58,62),'steel2':(46,48,52),'grip':(34,34,36),'mag':(50,52,56),'dark':(10,10,12),'polish':(140,144,150),
 'brass':(196,156,64),'white':(230,230,230),'engrave':(100,102,106),'red':(200,30,30)},seed=9)
c=K.cube;both=K.both;tube=K.tube;B=34
G='receptor'
c(G,'receptor',[-34,28.4,-3.1],[10,38.6,3.1],'steel')
c(G,'tampa',[-34,38.6,-2.8],[10,40.2,2.8],'steel2')
for k in range(4):both(G,'nervura',-32,30.6+k*2.0,8,31.2+k*2.0,3.1,3.25,'steel2')
c(G,'janela',[-12,32,3.08],[-2,36.4,3.2],'dark');c(G,'ferrolho_face',[-11,32.6,2.8],[-3,35.8,3.1],'polish')
c(G,'fenda_armar',[-30,40.15,-.6],[-2,40.3,.6],'dark')
c(G,'tampa_trava',[-36,36,-2.4],[-34,39.4,2.4],'polish')
for x in (-30,6):both(G,'rebite',x-.4,29.4,x+.4,30.2,3.1,3.25,'polish')
c(G,'olhal',[8,27.4,-3.6],[10,29.4,-3.0],'polish')
K.text(G,-30,36.4,-3.12,[.5,.5,.5],h=1.2,s=-1)
G='miras'
both(G,'alca_orelha',-33,40.2,-30,43.6,1.6,2.3,'steel2');c(G,'alca',[-32.2,40.2,-1.0],[-30.8,42.6,1.0],'steel2');c(G,'alca_furo',[-32.25,41.4,-.35],[-30.75,42.1,.35],'dark')
both(G,'massa_orelha',5,40.2,8.6,43.6,1.6,2.3,'steel2');c(G,'massa_poste',[6.4,40.2,-.35],[7.2,41.75,.35],'steel2')
IRON=41.75
G='cano'
tube(G,'cano',B,0,10,22,1.3,'steel2');tube(G,'cano_porca',B,0,10,13.6,2.6,'steel')
for k in range(6):c(G,'porca_estria',[10.2,B+2.4,-.3],[13.4,B+2.75,.3],'steel2',rot=[k*30,0,0],origin=[11.8,B,0])
tube(G,'boca',B,0,21.95,22.1,.85,'dark')
G='inferior'
c(G,'armacao',[-24,23.6,-2.8],[2,28.6,2.8],'steel2')
K.trigger(G,-6.4,23.6,guard=True,gmat='steel2',w=1.3,L=15,depth=6.6)
c(G,'seletor',[-20,24.8,-3.4],[-16.6,26,-2.8],'steel',rot=[0,0,20],origin=[-18,25.4,-3])
for k,col in enumerate(('white','red','red')):c(G,'seletor_marca',[-21+k*1.3,26.6,-2.85],[-20.5+k*1.3,27.2,-2.8],col)
GR=dict(rot=[0,0,6],origin=[-15,23.6,0])
c(G,'punho',[-19.6,2,-2.8],[-10.4,23.8,2.8],'grip',**GR)
both(G,'placa_punho',-19,4,-11,20.4,2.8,3.2,'grip',**GR)
for k in range(10):both(G,'placa_estria',-18.6,4.8+k*1.5,-11.4,5.4+k*1.5,3.2,3.3,'dark',**GR)
c(G,'seguranca_punho',[-21,8,-1.6],[-19.4,22,1.6],'steel',**GR)
c(G,'reten',[-11,4,-1.4],[-9.6,6.4,1.4],'polish',**GR)
G='coronha'
c(G,'coronha_dobra',[-38,25.4,-2.6],[-34,36,2.6],'steel')
both(G,'coronha_braco',-70,32.0,-38,33.4,2.2,3.0,'steel2')
both(G,'coronha_braco2',-70,26.0,-38,27.4,2.2,3.0,'steel2')
c(G,'soleira',[-73,22,-3.0],[-70,35.4,3.0],'steel')
c(G,'soleira_borracha',[-73.6,22.4,-2.6],[-73,35,2.6],'dark')
G='ferrolho'
c(G,'botao_armar_base',[-26,40.2,-.6],[-23,40.8,.6],'steel')
c(G,'botao_armar',[-26.2,40.8,-1.4],[-22.8,42.2,1.4],'steel',rot=[0,0,0])
for k in range(4):c(G,'botao_estria',[-26+k*.85,42.2,-1.3],[-25.6+k*.85,42.4,1.3],'dark')
G='carregador'
c(G,'carregador',[-18.8,-6,-2.0],[-11.4,23.4,2.0],'mag',**GR)
c(G,'carregador_base',[-19.6,-7.6,-2.4],[-10.6,-5.8,2.4],'mag',**GR)
both(G,'carregador_nervura',-17.6,-5,-12.6,-1,2.0,2.2,'mag',**GR)
K.finish(scale=.36,support=(6,28),grip=(-15,23.6),muzzle_x=22.1,bore_y=B,iron_y=IRON,rail=(40.2,-28,0),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'steel':'main','steel2':'main2','grip':'accent','mag':'mag'},regions={'barrel':10,'stock':-34})
