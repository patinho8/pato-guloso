"""Dragunov SVD — receptor tipo AK longo, coronha de madeira vazada com apoio de rosto, guarda-mão ventilado, trilho lateral de luneta, cano longo com quebra-chamas de fendas."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun,ak_receiver,ak_trigger_group
K=Gun('svd',{'steel':(48,50,54),'wood':(120,64,30),'wood2':(96,50,24),'mag':(44,45,48),'polish':(130,134,140),'dark':(10,10,12),'brass':(196,156,64),
 'white':(236,236,236),'red':(200,30,30),'rubber':(30,26,24)},seed=96)
c=K.cube;both=K.both;tube=K.tube;B=34
G='receptor'
top=ak_receiver(K,G,-46,0,25.0,36.4,2.9)
c(G,'trilho_luneta',[-40,28.4,-3.9],[-14,34.4,-2.9],'steel')
for k in range(6):c(G,'trilho_luneta_dente',[-39+k*4.2,28.0,-4.1],[-37.6+k*4.2,34.8,-3.85],'steel')
ry=top+.4
G='miras'
c(G,'alca_bloco',[0,36.4,-2.0],[8,38.6,2.0],'steel')
c(G,'alca_folha',[1,38.6,-1.6],[8,39.4,1.6],'steel');c(G,'alca_entalhe',[1,39.0,-.4],[2,39.45,.4],'dark')
IRON=39.3
G='guarda_mao'
c(G,'guarda_mao',[8,29,-3.0],[40,38.6,3.0],'wood')
both(G,'gm_chanfro',8,37.6,40,39.4,2.0,3.2,'wood',rot=[40,0,0],origin=[24,38.4,2.6])
for k in range(6):both(G,'gm_ventilacao',11+k*4.6,32,13+k*4.6,36.2,2.98,3.08,'dark')
c(G,'gm_anel',[39.4,29,-2.8],[41.4,38.6,2.8],'steel');c(G,'gm_anel2',[7,29,-3.0],[9,38.6,3.0],'steel')
tube(G,'tubo_gas',39.6,0,8,44,1.2,'steel')
G='cano'
tube(G,'cano',B,0,40,102,1.25,'steel');c(G,'bloco_gas',[42,31.6,-1.8],[47,41,1.8],'steel')
c(G,'regulador',[44,30.4,-2.2],[45.6,32,2.2],'polish')
c(G,'baioneta',[50,30.6,-.8],[54,32.6,.8],'steel')
c(G,'massa_base',[90,35.2,-1.6],[94,37.6,1.6],'steel');K.post(G,92,37.6,IRON,hood=1.6)
tube(G,'quebra_chamas',B,0,94,104,1.6,'steel')
for k in range(5):c(G,'qc_fenda',[96,B+1.3,-.3],[103.4,B+1.7,.3],'dark',rot=[k*72+90,0,0],origin=[99.7,B,0])
tube(G,'boca',B,0,103.95,104.1,.8,'dark')
G='inferior'
ak_trigger_group(K,G,-28,25.0)
G='coronha'
c(G,'coronha_frente',[-50,24,-2.4],[-46,36.6,2.4],'wood2')
c(G,'coronha_topo',[-86,33.4,-2.4],[-50,37.4,2.4],'wood')
c(G,'coronha_bochecha',[-80,37.4,-2.2],[-58,40.2,2.2],'wood2')
c(G,'coronha_baixo',[-87,12.6,-2.4],[-38,16.6,2.4],'wood',rot=[0,0,-8.6],origin=[-62.5,14.6,0])
c(G,'punho',[-41,9,-2.4],[-33,26,2.4],'wood',rot=[0,0,10],origin=[-37,25,0])
for k in range(7):both(G,'punho_estria',-40,11+k*1.9,-34,11.8+k*1.9,2.4,2.55,'wood2',rot=[0,0,10],origin=[-37,25,0])
c(G,'coronha_tras',[-90,16,-2.4],[-86,37.4,2.4],'wood')
c(G,'soleira',[-92,17.4,-2.8],[-90,37.8,2.8],'rubber')
G='ferrolho'
c(G,'alavanca',[-12,32.6,2.9],[-10,34.0,5.2],'steel');c(G,'alavanca_bola',[-12.4,32.2,4.6],[-9.6,34.4,5.8],'steel')
c(G,'transportador',[-24,32,2.6],[-12,35.6,2.95],'polish')
G='carregador'
K.mag_box(G,-24,-11,25.0,9,2.4,lean=2,curve=3,segs=2,base='steel')
K.round_top(G,-23.4,-11.6,25.0,2.4,'mag')
K.finish(scale=.235,grip=(-37,25),muzzle_x=104.1,bore_y=B,iron_y=IRON,rail=(ry,-40,-6),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'steel':'main','wood':'accent','wood2':'accent','mag':'mag'},regions={'barrel':40,'stock':-46})
