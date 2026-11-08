"""MAC-10 — caixa compacta, cano rosqueado, alça de mão na frente, carregador no punho, coronha de arame retrátil."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('mac10',{'steel':(54,56,60),'steel2':(44,46,50),'mag':(48,50,54),'dark':(10,10,12),'polish':(140,144,150),
 'brass':(196,156,64),'white':(230,230,230),'engrave':(100,102,106),'strap':(40,36,30)},seed=10)
c=K.cube;both=K.both;tube=K.tube;B=33
G='receptor'
c(G,'superior',[-26,29.6,-3.0],[6,38.4,3.0],'steel')
c(G,'inferior',[-26,24.2,-2.9],[6,29.6,2.9],'steel2')
c(G,'janela',[-6,31,3.0],[2,35.6,3.1],'dark');c(G,'ferrolho_face',[-5,31.6,2.8],[1,35,3.0],'polish')
c(G,'fenda_armar',[-22,38.35,-.6],[2,38.5,.6],'dark')
for x in (-24,-14,4):both(G,'pino',x-.45,26.4,x+.45,27.3,2.9,3.05,'polish')
K.text(G,-22,32.6,-3.02,[.5,.5,.5,.3,.5,.5],h=1.2,s=-1)
c(G,'olhal_frente',[5,22.4,-.8],[7,24.4,.8],'polish')
c(G,'alca_mao',[6,22,-.4],[14,23,.4],'strap',rot=[0,0,-20],origin=[6,23,0])
G='miras'
c(G,'alca',[-25,38.4,-1.2],[-23,40.8,1.2],'steel2');c(G,'alca_furo',[-25.05,39.6,-.35],[-22.95,40.3,.35],'dark')
c(G,'massa',[2.4,38.4,-.9],[4.4,40.2,.9],'steel2');c(G,'massa_poste',[3.1,40.2,-.3],[3.7,40.0,.3],'steel2')
both(G,'massa_orelha',2.4,38.4,4.4,40.8,.9,1.4,'steel2')
IRON=39.95
G='cano'
tube(G,'cano',B,0,6,16,1.25,'steel2');tube(G,'rosca',B,0,9,16,1.45,'polish')
for k in range(10):tube(G,'rosca_filete',B,0,9.3+k*.66,9.6+k*.66,1.6,'steel2')
tube(G,'boca',B,0,15.95,16.1,.8,'dark')
G='inferior2'
K.trigger(G,-12.4,24.2,guard=True,gmat='steel2',w=1.2,L=9,depth=5.2)
c(G,'seletor',[-6,25,-3.5],[-3.4,26.2,-2.9],'steel')
GR=dict(rot=[0,0,4],origin=[-1.5,24.2,0])
c(G,'punho',[-6,2,-2.8],[3,24.4,2.8],'steel2',**GR)
both(G,'punho_placa',-5.6,4,2.6,21,2.8,3.1,'dark',**GR)
for k in range(9):both(G,'punho_estria',-5.2,4.8+k*1.8,2.2,5.4+k*1.8,3.1,3.2,'steel2',**GR)
c(G,'reten',[-6.6,1.6,-1.6],[-4.8,3.0,1.6],'polish',**GR)
G='coronha'
both(G,'coronha_arame',-62,31.4,-26,32.4,2.2,2.9,'polish')
both(G,'coronha_arame2',-62,26.4,-26,27.4,2.2,2.9,'polish')
c(G,'soleira',[-64,24,-3.0],[-61.6,34,3.0],'steel')
c(G,'coronha_trava',[-28,30.6,-1.0],[-26,33.0,1.0],'polish')
G='ferrolho'
c(G,'botao_armar',[-12,38.4,-.9],[-9,41.4,.9],'steel')
c(G,'botao_armar_topo',[-12.4,41.0,-1.2],[-8.6,41.8,1.2],'steel')
G='carregador'
c(G,'carregador',[-5.2,-8,-2.1],[2.2,23.8,2.1],'mag',**GR)
c(G,'carregador_base',[-5.8,-9.4,-2.4],[2.8,-7.8,2.4],'mag',**GR)
K.finish(scale=.235,support=(8,22.6),grip=(-1.5,24.2),muzzle_x=16.1,bore_y=B,iron_y=IRON,rail=(38.4,-22,0),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'steel':'main','steel2':'main2','mag':'mag'},regions={'barrel':6,'stock':-26})
