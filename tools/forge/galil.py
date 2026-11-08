"""IWI Galil ACE — receptor tipo AK com trilho no topo, guarda-mão com trilhos, alavanca à esquerda, coronha de polímero, carregador curvo."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun,ak_receiver,ak_trigger_group
K=Gun('galil',{'steel':(50,52,56),'poly':(40,41,44),'mag':(44,45,48),'polish':(130,134,140),'dark':(10,10,12),'brass':(196,156,64),
 'white':(236,236,236),'red':(200,30,30),'rubber':(24,24,26),'engrave':(96,98,102),'tan':(120,104,80)},seed=33)
c=K.cube;both=K.both;tube=K.tube;B=34
G='receptor'
top=ak_receiver(K,G,-42,0,25.4,36.4,2.9)
ry=K.rail(G,-38,-2,top,w=1.8,mat='steel')
K.text(G,-36,28,-2.92,[.5,.5,.5,.5,.5],h=1.3,s=-1)
G='guarda_mao'
c(G,'guarda_mao',[0,29.6,-3.4],[28,39.4,3.4],'poly')
both(G,'gm_chanfro',0,38,28,40.4,2.4,3.6,'poly',rot=[40,0,0],origin=[14,39.2,3])
K.rail(G,0,28,39.4,w=1.8,mat='poly')
K.side_rail(G,4,26,33.4,3.4,mat='poly',s=1);K.side_rail(G,4,26,33.4,-3.4,mat='poly',s=-1)
c(G,'gm_baixo',[2,28.6,-1.8],[26,29.6,1.8],'poly')
for x in range(3,25,2):c(G,'gm_baixo_dente',[x,27.8,-2.1],[x+1,28.6,2.1],'poly')
G='miras'
c(G,'alca_base',[-40,top+1.75,-1.6],[-36,top+2.6,1.6],'steel');IRON=K.peep(G,-38,top+2.6,w=1.4,h=3.0)
c(G,'massa_base',[22.4,41.15,-1.6],[26.4,42,1.6],'steel');K.post(G,24.4,42,IRON,hood=1.8)
G='cano'
tube(G,'cano',B,0,28,58,1.3,'steel');c(G,'bloco_gas',[28,31,-1.9],[33,39,1.9],'steel')
tube(G,'quebra_chamas',B,0,52,60,1.9,'steel')
for k in range(4):c(G,'qc_fenda',[54,B+1.6,-.3],[59.4,B+2.0,.3],'dark',rot=[k*90+45,0,0],origin=[56.7,B,0])
tube(G,'boca',B,0,59.95,60.1,1.0,'dark')
G='inferior'
ak_trigger_group(K,G,-24,25.4)
GR=dict(rot=[0,0,22],origin=[-31,25.4,0])
c(G,'punho',[-35,8,-2.4],[-27,25.6,2.4],'poly',**GR)
K.checker(G,-34,10,3,8,1.8,2.4,**GR)
c(G,'punho_dedo',[-27.6,14,-2.2],[-26.4,16.4,2.2],'poly',**GR)
c(G,'punho_base',[-35.4,7.2,-2.6],[-26.6,8.2,2.6],'poly',**GR)
G='coronha'
c(G,'coronha_dobra',[-46,26.6,-3.2],[-42,36,3.2],'poly')
tube(G,'coronha_tubo',33,0,-62,-46,2.0,'poly')
c(G,'coronha_corpo',[-82,24,-2.6],[-58,37,2.6],'poly')
c(G,'coronha_vazado',[-76,27,-2.65],[-62,32,2.65],'dark')
c(G,'coronha_bochecha',[-80,37,-2.2],[-62,39.4,2.2],'poly')
c(G,'soleira',[-85,21.4,-3.0],[-82,39.6,3.0],'rubber')
for k in range(7):c(G,'soleira_estria',[-85.1,22.6+k*2.4,-2.8],[-85,23.4+k*2.4,2.8],'dark')
G='ferrolho'
c(G,'alavanca',[-14,33.6,-5.4],[-12,35,-2.9],'steel');c(G,'alavanca_bola',[-14.4,33.2,-6.4],[-11.6,35.4,-5.2],'poly')
c(G,'transportador',[-24,33,2.6],[-12,36,2.95],'polish')
G='carregador'
K.mag_box(G,-20.8,-6.8,25.4,1,2.5,lean=3,curve=6,segs=4,base='poly')
K.round_top(G,-20.2,-7.4,25.4,2.5,'mag')
K.finish(scale=.235,grip=(-31,25.4),muzzle_x=60.1,bore_y=B,iron_y=IRON,rail=(ry,-36,-4),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'steel':'main','poly':'accent','mag':'mag'},regions={'barrel':28,'stock':-42})
