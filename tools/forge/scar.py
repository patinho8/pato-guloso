"""FN SCAR-H — receptor superior longo com trilho inteiro, inferior de polímero, coronha dobrável com apoio de rosto, carregador 7.62."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('scar',{'upper':(150,128,92),'lower':(140,118,84),'black':(32,32,34),'mag':(44,45,48),'steel':(58,60,64),'dark':(10,10,12),'polish':(130,134,140),
 'brass':(196,156,64),'red':(200,30,30),'white':(236,236,236),'engrave':(110,96,72),'rubber':(26,26,28)},seed=17)
c=K.cube;both=K.both;tube=K.tube;B=35
G='superior'
c(G,'superior',[-38,31.6,-3.1],[46,40.4,3.1],'upper')
both(G,'sup_chanfro',-38,30.6,46,32.6,2.2,3.3,'upper',rot=[-40,0,0],origin=[4,31.6,2.8])
ry=K.rail(G,-37.6,45.6,40.4,w=2.15,mat='upper')
for k in range(1,36,2):c(G,'numero_trilho',[-37.6+k*2.3,41.0,2.14],[-37.4+k*2.3,41.2,2.2],'white')
K.side_rail(G,14,42,35,3.1,mat='upper',s=1);K.side_rail(G,14,42,35,-3.1,mat='upper',s=-1)
c(G,'trilho_baixo',[16,30.0,-1.8],[42,31.6,1.8],'upper')
for x in range(17,41,2):c(G,'trilho_baixo_dente',[x,29.2,-2.1],[x+1,30.0,2.1],'upper')
c(G,'janela',[-24,34.2,3.08],[-12,38.4,3.18],'dark');c(G,'ferrolho_face',[-22.6,34.8,2.8],[-13.4,37.8,3.1],'polish')
c(G,'fenda_manejo',[-14,36.4,-3.2],[14,37.6,-3.0],'dark')
for x in (-30,0,30):K.screw(G,x,33.4,3.1,.45,'black')
K.text(G,-34,33.0,-3.12,[.5,.5,.5,.5,.3,.5],h=1.3,s=-1)
G='miras'
c(G,'alca_base',[-36.6,42.15,-2.0],[-31.4,43.2,2.0],'black');IRON=K.peep(G,-34,43.2,w=1.5,h=3.4,mat='black')
c(G,'massa_base',[38.6,42.15,-2.0],[43.6,43.2,2.0],'black');K.post(G,41,43.2,IRON,mat='black')
G='cano'
tube(G,'cano',B,0,46,66,1.35,'steel');c(G,'bloco_gas',[46,32,-1.8],[50,38,1.8],'steel')
c(G,'regulador',[48,31.2,-2.4],[49.6,33.2,2.4],'polish')
tube(G,'quebra_chamas',B,0,66,74,2.0,'black')
for k in range(4):a=k*90+45;c(G,'qc_fenda',[68,B+1.7,-.3],[73.4,B+2.1,.3],'dark',rot=[a,0,0],origin=[70.7,B,0])
tube(G,'boca',B,0,73.95,74.1,1.0,'dark')
G='inferior'
c(G,'inferior',[-38,24.2,-2.8],[-4,31.8,2.8],'lower')
c(G,'boca_carregador',[-21.4,17.2,-3.1],[-5.2,24.4,3.1],'lower')
c(G,'boca_chanfro',[-21.8,16.8,-3.3],[-4.8,17.8,3.3],'lower')
K.trigger(G,-26.4,24.2,guard=True,gmat='lower',w=1.3,L=11.6,depth=6.4)
c(G,'seletor',[-33.4,27.2,-3.5],[-29,28.2,-2.8],'black',rot=[0,0,-12],origin=[-31.2,27.7,-3.1])
c(G,'reten_ferrolho',[-10,26.4,-3.4],[-6.4,29.4,-2.8],'black');c(G,'botao_carregador',[-10.6,25.4,2.8],[-8.6,27.4,3.4],'black')
c(G,'reten_ambi',[-6.4,18.6,-1.4],[-4.4,20.8,1.4],'black')
GR=dict(rot=[0,0,24],origin=[-31,24.2,0])
c(G,'punho',[-35,7.2,-2.3],[-28.2,24.4,2.3],'black',**GR)
c(G,'punho_base',[-35.4,6.6,-2.5],[-27.8,7.6,2.5],'black',**GR)
K.checker(G,-34,9,3,8,1.8,2.3,**GR)
c(G,'punho_dedo',[-28.6,14.6,-2.1],[-27.4,16.6,2.1],'black',**GR)
G='coronha'
c(G,'coronha_dobra',[-42,27.6,-3.2],[-38,39.4,3.2],'black')
c(G,'coronha_corpo',[-74,30,-2.6],[-42,38.6,2.6],'lower')
c(G,'coronha_vazado',[-66,31.6,-2.65],[-48,35.6,2.65],'dark')
c(G,'coronha_bochecha',[-72,38.6,-2.4],[-50,42.2,2.4],'lower')
c(G,'bochecha_trava',[-60,38.6,2.4],[-57,40.6,3.0],'black')
c(G,'coronha_baixo',[-76,21.6,-2.6],[-66,30,2.6],'lower')
c(G,'coronha_diag',[-68,24,-2.2],[-48,27,2.2],'lower',rot=[0,0,18],origin=[-58,26,0])
c(G,'soleira',[-78.4,20.8,-3.2],[-76,42.6,3.2],'rubber')
for k in range(8):c(G,'soleira_estria',[-78.5,22+k*2.5,-3.0],[-78.4,22.8+k*2.5,3.0],'dark')
G='ferrolho'
c(G,'alavanca_base',[4,36.2,-3.4],[6,37.8,-3.0],'black')
c(G,'alavanca',[4.2,36.4,-6.4],[5.8,37.6,-3.2],'black',rot=[0,-20,0],origin=[5,37,-3.2])
c(G,'alavanca_bola',[4.0,36.1,-7.4],[6.4,37.9,-6.0],'black',rot=[0,-20,0],origin=[5,37,-3.2])
G='carregador'
K.mag_box(G,-20.8,-5.8,17.2,-1.6,2.7,lean=3,segs=2,base='black')
K.round_top(G,-20,-6.6,17.2,2.7,'mag')
K.finish(scale=.235,grip=(-31,24.2),muzzle_x=74.1,bore_y=B,iron_y=IRON,rail=(ry,-30,36),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'upper':'main','lower':'main2','black':'accent','mag':'mag'},regions={'barrel':46,'stock':-38})
