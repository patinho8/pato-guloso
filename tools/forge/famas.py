"""FAMAS F1 — bullpup com alça de transporte longa sobre o corpo, bipé dobrado, guarda-mato grande, carregador reto."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('famas',{'body':(44,46,48),'body2':(36,38,40),'mag':(52,54,56),'steel':(62,64,68),'dark':(10,10,12),'polish':(140,144,150),
 'brass':(196,156,64),'red':(200,30,30),'white':(236,236,236),'rubber':(24,24,26),'engrave':(96,98,102)},seed=88)
c=K.cube;both=K.both;tube=K.tube;B=33
G='corpo'
c(G,'corpo',[-62,24,-3.2],[14,38,3.2],'body')
both(G,'corpo_ombro',-62,36.6,14,39,2.2,3.4,'body',rot=[40,0,0],origin=[-24,37.8,2.8])
c(G,'corpo_baixo',[-62,17,-3.0],[-34,24.4,3.0],'body')
c(G,'soleira',[-65,16.4,-3.4],[-62,38.6,3.4],'rubber')
c(G,'bochecha',[-60,38,-2.2],[-40,39.6,2.2],'body2')
c(G,'boca_carregador',[-46,17,-2.9],[-34,24.4,2.9],'body2')
c(G,'janela',[-50,30,3.18],[-42,34,3.3],'dark');c(G,'janela_e',[-50,30,-3.3],[-42,34,-3.18],'body2')
c(G,'guarda_mao',[-4,25,-3.4],[14,36,3.4],'body2')
for k in range(5):both(G,'gm_fenda',-2+k*3.2,27,-.6+k*3.2,34,3.38,3.46,'dark')
c(G,'punho',[-26,8,-2.6],[-19,24.2,2.6],'body2',rot=[0,0,8],origin=[-22.5,24,0])
K.checker(G,-25.4,10,3,8,1.7,2.6,rot=[0,0,8],origin=[-22.5,24,0])
c(G,'guarda_mato',[-26,5.4,-1.8],[-6,7.6,1.8],'body')
c(G,'guarda_frente',[-8,5.4,-1.8],[-5.4,24.4,1.8],'body',rot=[0,0,-8],origin=[-6.7,15,0])
K.trigger(G,-15,24,guard=None)
c(G,'seletor',[-40,20,-1.2],[-36,21,1.2],'steel')
K.text(G,-30,27,-3.22,[.5,.5,.5,.5,.5],h=1.4,s=-1)
G='alca'
for x in (-56,10):c(G,'alca_pe',[x,38,-1.6],[x+4,44,1.6],'body')
c(G,'alca_topo',[-56,44,-1.8],[14,46,1.8],'body')
c(G,'alca_vazado',[-50,39.6,-1.62],[6,43.6,1.62],'dark')
c(G,'alca_frente',[12,40,-2],[16,46,2],'body',rot=[0,0,-20],origin=[14,40,0])
ry=46
c(G,'alca_mira',[-54,46,-1.6],[-50,48.6,1.6],'body2');c(G,'alca_mira_furo',[-54.05,47.2,-.4],[-49.95,48.0,.4],'dark')
c(G,'massa',[8,46,-.4],[9,47.6,.4],'body2');both(G,'massa_orelha',7,46,10,48.4,.9,1.6,'body2')
IRON=47.6
c(G,'alavanca',[-30,44.6,-1.0],[-24,46.6,1.0],'steel')
G='cano'
tube(G,'cano',B,0,14,40,1.3,'steel');tube(G,'cano_luva',B,0,14,20,2.0,'body2')
tube(G,'quebra_chamas',B,0,34,42,1.8,'steel')
for k in range(4):c(G,'qc_fenda',[36,B+1.5,-.3],[41.4,B+1.9,.3],'dark',rot=[k*90+45,0,0],origin=[38.7,B,0])
tube(G,'boca',B,0,41.95,42.1,1.0,'dark')
both(G,'bipe_perna',-2,31,26,32,3.4,4.0,'steel')
both(G,'bipe_pe',24,30.4,27,32.6,3.3,4.2,'steel')
c(G,'bipe_base',[-3,30,-4.0],[0,33,4.0],'steel')
G='ferrolho'
c(G,'manejo',[-28,44.2,-.8],[-25,45.0,.8],'polish')
G='carregador'
K.mag_box(G,-45.4,-34.6,17,-3,2.4,lean=1,segs=2,base='body2')
K.round_top(G,-44.8,-35.2,17,2.4,'mag')
K.finish(scale=.235,support=(4,25),grip=(-22.5,24),muzzle_x=42.1,bore_y=B,iron_y=IRON,rail=(ry,-50,8),moving=['ferrolho'],magazine=['carregador'],stock=[],
 roles={'body':'main','body2':'accent','mag':'mag'},regions={'barrel':14})
