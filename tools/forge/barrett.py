"""Barrett M82A1 — receptor superior com furos de refrigeração, trilho longo, cano pesado com freio de boca de duas câmaras, bipé, carregador .50 e coronha com monopé."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('barrett',{'upper':(44,46,50),'lower':(52,54,58),'steel':(60,62,66),'polish':(140,144,150),'dark':(10,10,12),'brass':(196,156,64),
 'white':(236,236,236),'red':(200,30,30),'rubber':(24,24,26),'mag':(46,48,52),'engrave':(100,102,106)},seed=82)
c=K.cube;both=K.both;tube=K.tube;B=35
G='superior'
c(G,'superior',[-50,30,-3.8],[46,40.6,3.8],'upper')
both(G,'sup_chanfro',-50,39.4,46,41.6,2.6,4.0,'upper',rot=[40,0,0],origin=[-2,40.6,3.4])
ry=K.rail(G,-48,30,40.6,w=2.2,mat='upper')
for k in range(7):both(G,'furo_refrig',4+k*5.4,33,8+k*5.4,37.4,3.78,3.88,'dark')
c(G,'janela',[-30,32,3.78],[-16,37.6,3.9],'dark');c(G,'ferrolho_face',[-28.6,32.6,3.5],[-17.4,37,3.8],'polish')
c(G,'fenda_manejo',[-14,34,3.78],[2,35.4,3.9],'dark')
K.text(G,-44,33,-3.82,[.5,.5,.5,.5,.5,.5,.5],h=1.4,s=-1)
c(G,'alca_base',[-46,42.35,-2.0],[-41,43.4,2.0],'upper');IRON=K.peep(G,-43.5,43.4,w=1.6,h=3.4)
c(G,'massa_base',[24,42.35,-2.0],[28,43.4,2.0],'upper');K.post(G,26,43.4,IRON)
G='inferior'
c(G,'inferior',[-80,24,-3.4],[-6,30.2,3.4],'lower')
c(G,'boca_carregador',[-32,18,-3.6],[-10,24.4,3.6],'lower')
K.trigger(G,-44,24,guard=True,gmat='lower',w=1.4,L=13,depth=6.8)
c(G,'seguranca',[-50,25.6,-4.0],[-46,27,-3.4],'polish')
GR=dict(rot=[0,0,16],origin=[-52,24,0])
c(G,'punho',[-56,6,-2.7],[-48,24.2,2.7],'rubber',**GR)
K.checker(G,-55,8,3,8,1.9,2.7,mat='dark',**GR)
c(G,'punho_base',[-56.4,5.2,-2.9],[-47.6,6.2,2.9],'lower',**GR)
G='cano'
tube(G,'cano',B,0,46,118,2.0,'steel')
for k in range(6):tube(G,'cano_estria',B,0,48+k*4,50+k*4,2.15,'steel')
tube(G,'freio',B,0,112,124,3.6,'steel')
for x in (113.6,118.8):both(G,'freio_janela',x,B-2.2,x+3.4,B+2.2,3.2,3.7,'dark')
tube(G,'boca',B,0,123.95,124.1,1.4,'dark')
c(G,'bipe_base',[40,27,-3],[46,30,3],'steel')
both(G,'bipe_perna',46,27.6,78,29,2.6,3.6,'steel')
both(G,'bipe_pe',76,26.6,80,29.6,2.4,4.0,'steel')
G='coronha'
c(G,'coronha',[-112,22,-3.2],[-80,36,3.2],'lower')
c(G,'coronha_bochecha',[-108,36,-2.6],[-84,40,2.6],'rubber')
c(G,'coronha_vazado',[-104,25,-3.25],[-88,31,3.25],'dark')
c(G,'soleira',[-116,18,-3.6],[-112,40,3.6],'rubber')
c(G,'monope',[-100,12,-1.2],[-97,22,1.2],'steel');c(G,'monope_pe',[-101,10.6,-2],[-96,12,2],'steel')
G='ferrolho'
c(G,'alavanca',[-8,34,3.8],[-5,35.6,7.0],'steel');c(G,'alavanca_bola',[-8.6,33.4,6.4],[-4.4,36.2,8.0],'rubber')
G='carregador'
K.mag_box(G,-31,-11,18,0,3.2,lean=2,segs=1,base='mag')
c(G,'carregador_bala',[-30,18,-1.6],[-16,19.6,1.6],'brass');c(G,'carregador_ponta',[-16,18.2,-1.2],[-12,19.4,1.2],'brass')
K.finish(scale=.235,support=(30,30),grip=(-52,24),muzzle_x=124.1,bore_y=B,iron_y=IRON,rail=(ry,-40,20),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'upper':'main','lower':'main2','rubber':'accent','mag':'mag'},regions={'barrel':46,'stock':-80})
