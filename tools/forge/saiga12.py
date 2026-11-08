"""Saiga-12 — receptor tipo AK, cano grosso de escopeta com regulador de gás, trilhos, coronha dobrável e carregador de cartuchos 12."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun,ak_receiver,ak_trigger_group
K=Gun('saiga12',{'steel':(50,52,56),'poly':(36,37,40),'mag':(40,41,44),'polish':(130,134,140),'dark':(10,10,12),'brass':(196,156,64),
 'white':(236,236,236),'shell':(170,30,30),'rubber':(24,24,26),'engrave':(96,98,102)},seed=12)
c=K.cube;both=K.both;tube=K.tube;B=34
G='receptor'
top=ak_receiver(K,G,-42,0,25.2,36.8,3.2)
ry=K.rail(G,-38,-2,top,w=1.9)
K.text(G,-36,28,-3.22,[.5,.5,.5,.5,.5,.3,.5,.5],h=1.3,s=-1)
G='guarda_mao'
c(G,'guarda_mao',[0,29.4,-3.6],[24,39.4,3.6],'poly')
both(G,'gm_chanfro',0,38,24,40.4,2.6,3.8,'poly',rot=[40,0,0],origin=[12,39.2,3.2])
K.rail(G,0,24,39.4,w=1.9,mat='poly')
K.side_rail(G,3,22,33.4,3.6,mat='poly',s=1);K.side_rail(G,3,22,33.4,-3.6,mat='poly',s=-1)
G='miras'
c(G,'alca_base',[-40,top+1.75,-1.6],[-36,top+2.6,1.6],'steel');IRON=K.peep(G,-38,top+2.6,w=1.4,h=3.0)
c(G,'massa_base',[18,41.15,-1.6],[22,42,1.6],'steel');K.post(G,20,42,IRON)
G='cano'
tube(G,'cano',B,0,24,62,2.0,'steel');c(G,'bloco_gas',[24,30.6,-2.4],[30,40.4,2.4],'steel')
tube(G,'regulador',39.4,0,24,32,1.5,'polish');tube(G,'tubo_gas',39.4,0,30,40,1.1,'steel')
tube(G,'freio_boca',B,0,54,64,2.5,'steel')
for k in range(4):both(G,'freio_furo',55+k*2.2,B-.7,56.2+k*2.2,B+.7,2.35,2.6,'dark')
tube(G,'boca',B,0,63.95,64.1,1.5,'dark')
G='inferior'
ak_trigger_group(K,G,-24,25.2)
GR=dict(rot=[0,0,16],origin=[-31,25.2,0])
c(G,'punho',[-35,8,-2.5],[-27,25.4,2.5],'poly',**GR)
K.checker(G,-34,10,3,8,1.8,2.5,**GR)
c(G,'punho_base',[-35.4,7.2,-2.7],[-26.6,8.2,2.7],'poly',**GR)
G='coronha'
c(G,'coronha_dobra',[-46,26.6,-3.2],[-42,36,3.2],'steel')
both(G,'coronha_braco',-80,33,-46,34.6,2.0,2.8,'steel')
both(G,'coronha_braco2',-80,25.6,-46,27.2,2.0,2.8,'steel')
c(G,'coronha_corpo',[-82,22,-3.0],[-74,38,3.0],'poly')
c(G,'soleira',[-85,21.4,-3.2],[-82,39,3.2],'rubber')
G='ferrolho'
c(G,'alavanca',[-12,32.8,3.2],[-10,34.2,5.6],'steel');c(G,'alavanca_bola',[-12.4,32.4,5.0],[-9.6,34.6,6.2],'steel')
c(G,'transportador',[-24,32,2.9],[-12,35.6,3.25],'polish')
G='carregador'
K.mag_box(G,-22,-6,25.2,4,3.0,lean=2,curve=2,segs=2,base='poly')
c(G,'cartucho',[-21,25.2,-1.8],[-9,26.8,1.8],'shell');c(G,'cartucho_base',[-22,25.2,-1.9],[-21,26.8,1.9],'brass')
K.finish(scale=.36,grip=(-31,25.2),muzzle_x=64.1,bore_y=B,iron_y=IRON,rail=(ry,-36,-4),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'steel':'main','poly':'accent','mag':'mag'},regions={'barrel':24,'stock':-42})
