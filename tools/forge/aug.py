"""Steyr AUG A3 — bullpup verde com punho dentro da guarda grande, receptor com trilho, cano com empunhadura dobrável, carregador translúcido."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('aug',{'body':(88,100,76),'body2':(74,86,64),'alu':(46,48,52),'mag':(70,76,70),'steel':(62,64,68),'dark':(10,10,12),'polish':(140,144,150),
 'brass':(196,156,64),'red':(200,30,30),'white':(236,236,236),'rubber':(26,26,28)},seed=77)
c=K.cube;both=K.both;tube=K.tube;B=34
G='corpo'
c(G,'corpo',[-62,22,-3.4],[-4,33,3.4],'body')
both(G,'corpo_chanfro',-62,20.8,-4,23.4,2.4,3.6,'body',rot=[-40,0,0],origin=[-33,22,3.0])
c(G,'coronha_tras',[-66,16.6,-3.6],[-62,34.4,3.6],'rubber')
c(G,'coronha_baixo',[-62,16,-3.2],[-40,22.4,3.2],'body')
c(G,'coronha_bochecha',[-60,33,-2.6],[-40,35.6,2.6],'body')
c(G,'boca_carregador',[-38,16,-3.0],[-26,22.4,3.0],'body2')
c(G,'janela_ejecao',[-54,27,3.38],[-46,31,3.5],'dark')
c(G,'tampa_ejecao',[-54,27,-3.5],[-46,31,-3.38],'body2')
c(G,'botao_seguranca',[-18,25,-3.8],[-14,26.2,3.8],'body2')
both(G,'logo',-40,26,-34,28,3.4,3.48,'body2')
# guarda que envolve a mão inteira + punho
c(G,'punho',[-15,8,-2.6],[-8,22.4,2.6],'body',rot=[0,0,10],origin=[-11.5,22,0])
for k in range(3):c(G,'punho_dedo',[-8.6,10.6+k*3.8,-2.4],[-7.4,13+k*3.8,2.4],'body',rot=[0,0,10],origin=[-11.5,22,0])
c(G,'guarda_baixo',[-14,5.2,-2.2],[8,7.6,2.2],'body2')
c(G,'guarda_frente',[5.6,5.2,-2.2],[8.6,22.4,2.2],'body2',rot=[0,0,-6],origin=[7,14,0])
c(G,'guarda_topo',[-4,20.6,-2.6],[8,23,2.6],'body')
K.trigger(G,-5.2,21,guard=None)
G='receptor'
c(G,'receptor',[-48,33,-2.6],[10,37.6,2.6],'alu')
ry=K.rail(G,-46,6,37.6,w=2.0,mat='alu')
c(G,'receptor_frente',[6,30,-2.8],[12,37.6,2.8],'alu')
c(G,'alca_base',[-45,39.35,-1.8],[-40,40.4,1.8],'alu');IRON=K.peep(G,-42.5,40.4,w=1.4,h=3.2,mat='alu')
c(G,'massa_base',[1,39.35,-1.6],[5,40.4,1.6],'alu');K.post(G,3,40.4,IRON,mat='alu')
G='cano'
tube(G,'cano',B,0,10,58,1.35,'steel');tube(G,'cano_porca',B,0,10,16,2.4,'alu')
c(G,'cano_bloco_gas',[16,31,-2],[22,37,2],'alu')
c(G,'cano_empunhadura_base',[14,29.6,-1.6],[20,31.6,1.6],'alu')
c(G,'empunhadura',[15,18,-1.6],[19,29.6,1.6],'body',rot=[0,0,-6],origin=[17,29.6,0])
for k in range(4):c(G,'empunhadura_estria',[18.6,19.5+k*2.4,-1.5],[19.3,20.3+k*2.4,1.5],'body2',rot=[0,0,-6],origin=[17,29.6,0])
tube(G,'quebra_chamas',B,0,56,64,1.85,'steel')
for k in range(4):c(G,'qc_fenda',[58,B+1.55,-.3],[63.4,B+1.95,.3],'dark',rot=[k*90+45,0,0],origin=[60.7,B,0])
tube(G,'boca',B,0,63.95,64.1,1.0,'dark')
G='ferrolho'
c(G,'alavanca',[-2,33.6,-4.8],[0,35.2,-2.6],'alu');c(G,'alavanca_bola',[-2.6,33.2,-5.8],[.6,35.6,-4.6],'body2')
G='carregador'
K.mag_box(G,-37.6,-26.4,16.4,-3.4,2.6,lean=3,curve=4,segs=3,base='body2')
K.round_top(G,-37,-27,16.4,2.6,'mag')
K.finish(scale=.235,support=(17,24),grip=(-11.5,22),muzzle_x=64.1,bore_y=B,iron_y=IRON,rail=(ry,-44,4),moving=['ferrolho'],magazine=['carregador'],stock=[],
 roles={'body':'main','body2':'main2','alu':'accent','mag':'mag'},regions={'barrel':14})
