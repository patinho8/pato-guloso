"""FN P90 — corpo bullpup arredondado com furo para o polegar, carregador translúcido em cima com as balas à vista, trilhos e quebra-chamas."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('p90',{'shell':(46,47,50),'shell2':(38,39,42),'mag':(80,84,78),'steel':(62,64,68),'dark':(10,10,12),'polish':(140,144,150),
 'brass':(196,156,64),'red':(200,30,30),'white':(236,236,236),'engrave':(96,98,102),'rubber':(24,24,26)},seed=90)
c=K.cube;both=K.both;tube=K.tube;B=34
G='corpo'
c(G,'corpo',[-46,29.6,-3.4],[26,40,3.4],'shell')
both(G,'corpo_ombro',-46,38.6,26,41.0,2.4,3.6,'shell',rot=[40,0,0],origin=[-10,39.8,3.0])
c(G,'corpo_frente',[24,29.6,-3.2],[29,38.6,3.2],'shell',rot=[0,0,-14],origin=[24,29.6,0])
c(G,'coronha',[-50,14,-3.4],[-26,30,3.4],'shell')
c(G,'coronha_tras',[-52,15,-3.2],[-49,39.6,3.2],'rubber')
c(G,'coronha_baixo',[-50,12.6,-3.0],[-28,14.2,3.0],'shell2',rot=[0,0,4],origin=[-39,14,0])
both(G,'coronha_painel',-46,17,-30,28,3.4,3.5,'shell2')
c(G,'barra_inferior',[-28,13.4,-2.8],[14,17,2.8],'shell2')
c(G,'punho',[-12,16.6,-2.9],[-5,30,2.9],'shell2',rot=[0,0,-10],origin=[-8.5,30,0])
K.checker(G,-11.2,18,3,7,1.6,2.9,rot=[0,0,-10],origin=[-8.5,30,0])
c(G,'guarda_polegar',[6,16.6,-3.0],[13,30,3.0],'shell2',rot=[0,0,-18],origin=[9.5,30,0])
c(G,'guarda_polegar_lip',[12,24,-3.0],[16,30,3.0],'shell2',rot=[0,0,-30],origin=[14,30,0])
K.trigger(G,-1.4,29.6,guard=None)
c(G,'seletor_disco',[-4,15.0,-2],[0,15.6,2],'steel')
for x in (-40,-20,0,18):K.screw(G,x,31,3.4,.45,'steel')
K.text(G,-40,34.4,-3.42,[.5,.5,.5,.5],h=1.4,s=-1)
c(G,'janela_ejecao',[-36,13.9,-1.4],[-30,14.1,1.4],'dark')
G='topo'
both(G,'trilho_lado',-26,40,22,44.4,2.9,3.4,'shell2')
ry=K.rail(G,-26,22,44.4,w=2.0,mat='steel')
c(G,'alca',[-24,46.15,-1.6],[-21,48.2,1.6],'steel');c(G,'alca_entalhe',[-24.05,47.3,-.45],[-20.95,48.25,.45],'dark')
c(G,'massa',[17,46.15,-1.0],[19.6,47.8,1.0],'steel');c(G,'massa_poste',[18,46.15,-.3],[18.6,47.75,.3],'white')
IRON=47.75
G='cano'
tube(G,'cano',B,0,26,36,1.25,'steel');tube(G,'quebra_chamas',B,0,32,40,1.9,'shell2')
for k in range(4):c(G,'qc_fenda',[34,B+1.6,-.3],[39.4,B+2.0,.3],'dark',rot=[k*90+45,0,0],origin=[36.7,B,0])
tube(G,'boca',B,0,39.95,40.1,1.0,'dark')
G='ferrolho'
c(G,'alavanca',[12,31.6,-4.8],[15,33.4,-3.4],'shell2');c(G,'alavanca2',[12,31.6,3.4],[15,33.4,4.8],'shell2')
G='carregador'
c(G,'carregador',[-28,40.2,-2.8],[20,43.8,2.8],'mag')
for k in range(15):both(G,'carregador_bala',-26+k*3.0,41.0,-24.2+k*3.0,43.0,2.75,2.85,'brass')
c(G,'carregador_trava',[-30,40.4,-2.0],[-28,43.2,2.0],'shell2')
K.finish(scale=.36,support=(11,27),grip=(-8.5,30),muzzle_x=40.1,bore_y=B,iron_y=IRON,rail=(ry,-24,18),moving=['ferrolho'],magazine=['carregador'],stock=[],
 roles={'shell':'main','shell2':'accent','mag':'mag'},regions={'barrel':26})
