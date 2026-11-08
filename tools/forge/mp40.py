"""MP40 — tubo do receptor, caixa do carregador nervurada, punho de baquelite, coronha rebatível, apoio do cano e massa com capuz."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('mp40',{'steel':(52,54,58),'steel2':(44,46,50),'bake':(70,40,26),'mag':(48,50,54),'dark':(10,10,12),'polish':(140,144,150),
 'brass':(196,156,64),'white':(230,230,230),'engrave':(100,102,106)},seed=40)
c=K.cube;both=K.both;tube=K.tube;B=35
G='receptor'
tube(G,'receptor',36.4,0,-46,10,2.6,'steel')
for k in range(5):tube(G,'receptor_anel',36.4,0,-42+k*10,-41.2+k*10,2.75,'steel2')
c(G,'fenda_ferrolho',[-34,36.0,-2.75],[-6,37.6,-2.5],'dark')
for x in (-34,-25,-16):c(G,'entalhe_seg',[x,37.6,-2.75],[x+1.2,38.6,-2.5],'dark')
c(G,'janela',[-14,33.4,-1.6],[-6,34.2,1.6],'dark')
tube(G,'tampa_tras',36.4,0,-48,-46,2.4,'steel2')
c(G,'trava_tampa',[-47.2,38.6,-.6],[-46.2,39.8,.6],'polish')
c(G,'carcaca_inferior',[-38,28.8,-2.4],[-8,34.0,2.4],'steel2')
c(G,'caixa_carregador',[-8,20,-2.6],[1,34.2,2.6],'steel')
for k in range(7):both(G,'caixa_nervura',-7.4,21+k*1.8,.4,21.6+k*1.8,2.6,2.75,'steel2')
c(G,'caixa_reten',[1,27,1.6],[2.8,29.4,2.9],'polish')
c(G,'caixa_base',[-8.4,19.4,-2.9],[1.4,20.6,2.9],'steel2')
K.text(G,-34,30.4,2.42,[.5,.5,.5,.3,.5,.5],h=1.2,s=1)
G='miras'
c(G,'alca_base',[-28,38.6,-1.6],[-22,39.6,1.6],'steel2')
c(G,'alca_folha',[-25.4,39.6,-1.3],[-24.6,42.8,1.3],'steel2');c(G,'alca_entalhe',[-25.45,42.0,-.4],[-24.55,42.85,.4],'dark')
c(G,'alca_folha2',[-26.2,39.4,-1.2],[-23.2,40.2,1.2],'steel2',rot=[0,0,-60],origin=[-25,39.6,0])
IRON=42.4
G='cano'
tube(G,'cano',B,0,10,38,1.3,'steel2')
tube(G,'cano_porca',B,0,10,13,2.0,'steel')
c(G,'apoio_cano',[11,31.4,-1.4],[22,33.9,1.4],'steel');c(G,'apoio_gancho',[11,29.8,-1.2],[13.4,31.6,1.2],'steel')
tube(G,'cano_ponta',B,0,36,39.4,1.6,'steel')
c(G,'massa_base',[34.4,36.2,-1.6],[38.4,37.4,1.6],'steel');K.post(G,36.4,37.4,IRON,hood=1.7,mat='steel')
tube(G,'boca',B,0,39.35,39.5,.85,'dark')
G='inferior'
c(G,'armacao',[-40,23.8,-2.6],[-12,29.2,2.6],'bake')
c(G,'armacao_frente',[-14,22.6,-2.2],[-8,29,2.2],'bake')
K.trigger(G,-22.4,23.8,guard=True,gmat='steel',w=1.2,L=11,depth=5.8)
GR=dict(rot=[0,0,14],origin=[-34,23.8,0])
c(G,'punho',[-38.8,6,-2.6],[-29.4,24,2.6],'bake',**GR)
for k in range(9):both(G,'punho_estria',-38,8+k*1.7,-30.2,8.7+k*1.7,2.6,2.8,'bake',**GR)
c(G,'punho_parafuso',[-35,14,2.75],[-33.6,15.4,2.95],'polish',**GR)
c(G,'punho_base',[-39.4,5.2,-2.8],[-29,6.2,2.8],'steel2',**GR)
c(G,'olhal',[-40,26,-3.2],[-38,28,-2.6],'polish')
G='coronha'
c(G,'coronha_dobradica',[-44,26,-2.4],[-40,31,2.4],'steel')
both(G,'coronha_braco',-74,27.8,-42,29.4,2.0,2.9,'steel')
c(G,'coronha_trava',[-46,29.0,-1.2],[-42,30.4,1.2],'polish')
c(G,'soleira',[-77,20.6,-3.0],[-74,32.4,3.0],'steel')
c(G,'soleira_meio',[-76,23,-2.6],[-74.6,30,2.6],'steel2')
both(G,'soleira_dobra',-76,31.2,-73,32.6,1.4,3.0,'steel')
G='ferrolho'
c(G,'alavanca',[-30.2,36.2,-5.2],[-28.6,37.4,-2.4],'polish')
c(G,'alavanca_bola',[-30.6,35.8,-6.0],[-28.2,37.8,-4.8],'steel')
G='carregador'
K.mag_box(G,-7.2,.2,20,-12,1.9,lean=0,segs=1,base='steel2')
K.round_top(G,-6.8,-.2,20,1.9,'mag')
K.finish(scale=.235,grip=(-34,23.8),muzzle_x=39.5,bore_y=B,iron_y=IRON,rail=(39.4,-20,6),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'steel':'main','steel2':'main2','bake':'accent','mag':'mag'},regions={'barrel':10,'stock':-44})
