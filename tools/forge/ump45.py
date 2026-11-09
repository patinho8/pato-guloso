"""H&K UMP45 — receptor de polímero com trilhos, mira de tambor, alavanca à esquerda, carregador reto .45, coronha dobrável."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('ump45',{'poly':(44,45,48),'poly2':(36,37,40),'mag':(40,41,44),'steel':(62,64,68),'dark':(10,10,12),'polish':(140,144,150),
 'brass':(196,156,64),'red':(200,30,30),'white':(236,236,236),'engrave':(96,98,102),'rubber':(22,22,24)},seed=45)
c=K.cube;both=K.both;tube=K.tube;B=34
G='receptor'
c(G,'receptor',[-42,28.4,-3.3],[20,40.2,3.3],'poly')
both(G,'receptor_chanfro',-42,38.6,20,41.0,2.2,3.5,'poly',rot=[40,0,0],origin=[-11,39.6,2.9])
c(G,'receptor_topo',[-42,40.2,-2.2],[20,41.0,2.2],'poly')
ry=K.rail(G,-40,14,41.0,w=2.0)
for x in (-30,-6):K.screw(G,x,40.4,2.2,.4,'steel')
c(G,'janela',[-26,33.2,3.28],[-15.4,37.6,3.38],'dark');c(G,'ferrolho_face',[-24.6,33.8,3.0],[-17,37.0,3.3],'polish')
c(G,'defletor',[-15.4,32.6,3.2],[-13.8,38.2,4.0],'poly')
both(G,'receptor_nervura',-40,30.4,18,30.9,3.3,3.4,'dark')
for k in range(8):both(G,'gm_ventilacao',2+k*2.2,34.8,3.2+k*2.2,38.2,3.28,3.4,'dark')
K.side_rail(G,4,18,32.4,3.3,s=1);K.side_rail(G,4,18,32.4,-3.3,s=-1)
c(G,'trilho_baixo',[4,27.4,-1.6],[18,28.4,1.6],'steel')
for x in range(5,17,2):c(G,'trilho_baixo_dente',[x,26.8,-1.9],[x+1,27.4,1.9],'steel')
K.text(G,-36,31.6,-3.32,[.5,.5,.5,.5,.3,.5,.5],h=1.4,s=-1)
tube(G,'tubo_manejo',38.4,0,14,22,1.6,'poly2')
c(G,'trava_coronha',[-44,30,-2.6],[-42,39.4,2.6],'steel')
G='miras'
c(G,'tambor_base',[-38.8,42.75,-1.8],[-34.4,43.6,1.8],'steel')
for a in range(0,180,45):c(G,'tambor',[-37.9,45.6-2.0*math.tan(math.pi/8),-2.0],[-35.3,45.6+2.0*math.tan(math.pi/8),2.0],'steel',rot=[a,0,0],origin=[-36.6,45.6,0])
c(G,'tambor_furo',[-37.95,45.2,-.42],[-37.8,46.0,.42],'dark')
both(G,'tambor_orelha',-38.6,43.6,-34.6,46.8,2.1,2.6,'steel')
IRON=45.6
c(G,'massa_pe',[9,42.75,-1.6],[13,43.6,1.6],'steel')
K.post(G,11,43.6,IRON,hood=1.8)
G='cano'
tube(G,'cano',B,0,20,34,1.3,'steel');tube(G,'cano_rosca',B,0,32,35.6,1.6,'polish');tube(G,'boca',B,0,35.55,35.7,.85,'dark')
c(G,'gm_frente',[19.4,29,-3.0],[21,39.6,3.0],'poly2')
G='inferior'
c(G,'inferior',[-40,22.4,-3.0],[-16,28.6,3.0],'poly2')
c(G,'boca_carregador',[-14,22.0,-3.2],[-2,28.6,3.2],'poly2')
c(G,'reten_pa',[-16.4,20.2,-1.4],[-13.8,21.6,1.4],'steel',rot=[0,0,-20],origin=[-14,21.4,0])
K.trigger(G,-26.4,22.4,guard=True,gmat='poly2',w=1.6,L=14,depth=6.6)
c(G,'seletor',[-38,25.2,-3.8],[-33,26.4,-3.0],'steel',rot=[0,0,-25],origin=[-35.5,25.8,-3.4])
for k,col in enumerate(('white','red','red')):c(G,'seletor_marca',[-37+k*1.4,27.2,-3.05],[-36.4+k*1.4,27.8,-3.0],col)
GR=dict(rot=[0,0,0],origin=[-35.6,22.4,0])
c(G,'punho',[-39.6,5.6,-2.6],[-31.6,22.6,2.6],'poly2',**GR)
for k in range(3):c(G,'punho_dedo',[-32.2,8.6+k*4.4,-2.4],[-31,11.2+k*4.4,2.4],'poly2',**GR)
c(G,'punho_base',[-40.2,4.8,-2.9],[-31.2,6.0,2.9],'poly2',**GR)
K.checker(G,-38.8,8,4,9,1.5,2.6,**GR)
G='coronha'
for y in (37.4,31.0):both(G,'coronha_braco',-80,y-.9,-44,y+.9,2.4,3.4,'poly2')
c(G,'coronha_dobra',[-48,29.6,-3.6],[-44,39.4,3.6],'poly2')
c(G,'soleira',[-84,22.6,-3.8],[-80,40.4,3.8],'rubber')
for k in range(7):c(G,'soleira_estria',[-84.1,23.6+k*2.4,-3.6],[-84,24.4+k*2.4,3.6],'dark')
c(G,'coronha_diag',[-80,25.6,-1.4],[-56,27.4,1.4],'poly2',rot=[0,0,14],origin=[-68,26.5,0])
c(G,'coronha_bochecha',[-78,38.4,-2.4],[-56,40.4,2.4],'poly2')
G='ferrolho'
c(G,'alavanca_haste',[17.6,37.8,-5.0],[19.0,39.0,-1.6],'steel')
c(G,'alavanca_bola',[17.2,37.4,-6.2],[19.4,39.4,-4.8],'poly2')
G='carregador'
K.mag_box(G,-13.4,-2.6,22.0,-2.2,2.6,lean=2,segs=2,base='mag')
K.round_top(G,-12.8,-3.2,22,2.6,'mag')
K.finish(scale=.36,grip=(-35.6,22.4),muzzle_x=35.7,bore_y=B,iron_y=IRON,rail=(ry,-34,10),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'poly':'main','poly2':'accent','mag':'mag'},regions={'barrel':20,'stock':-44})
