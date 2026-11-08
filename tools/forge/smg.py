"""KRISS Vector — corpo alto e anguloso, trilho inteiro, punho na frente do carregador de Glock, guarda de cano, coronha dobrável."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('smg',{'body':(44,45,48),'body2':(36,37,40),'mag':(32,32,34),'steel':(60,62,66),'polish':(140,144,150),'dark':(10,10,12),'brass':(196,156,64),
 'white':(236,236,236),'red':(200,30,30),'rubber':(24,24,26),'engrave':(100,102,106)},seed=4)
c=K.cube;both=K.both;tube=K.tube;B=36
G='corpo'
c(G,'corpo',[-34,30,-3.0],[12,40.4,3.0],'body')
ry=K.rail(G,-34,12,40.4,w=2.0)
c(G,'corpo_baixo',[-30,22,-2.8],[-6,30.4,2.8],'body')
c(G,'kraken',[-8,14,-2.6],[10,30.4,2.6],'body2',rot=[0,0,-28],origin=[1,30,0])
c(G,'kraken_frente',[4,24,-2.8],[12,30.4,2.8],'body')
K.side_rail(G,-2,10,35,3.0,s=1);K.side_rail(G,-2,10,35,-3.0,s=-1)
c(G,'trilho_baixo',[4,23.2,-1.6],[12,24.2,1.6],'steel')
c(G,'janela',[-24,32,2.98],[-14,37,3.1],'dark')
c(G,'seletor',[-28,30.6,-3.6],[-24,31.8,-3.0],'polish')
K.text(G,-22,34,-3.02,[.5,.5,.5,.5,.5,.5],h=1.4,s=-1)
for x in (-30,-10,8):K.screw(G,x,38.6,3.0,.45)
K.trigger(G,-15,22,guard=True,gmat='body2',w=1.3,L=11,depth=6.0)
GR=dict(rot=[0,0,14],origin=[-21,22,0])
c(G,'punho',[-25,6,-2.5],[-17,22.2,2.5],'body2',**GR)
K.checker(G,-24,8,3,8,1.8,2.5,**GR)
c(G,'boca_carregador',[-6,12,-2.4],[2,22.4,2.4],'body2')
G='miras'
c(G,'alca_base',[-32,42.15,-1.8],[-28,43,1.8],'steel');IRON=K.peep(G,-30,43,w=1.4,h=3.0)
c(G,'massa_base',[8,42.15,-1.6],[12,43,1.6],'steel');K.post(G,10,43,IRON)
G='cano'
c(G,'guarda_cano',[12,32,-2.6],[22,39.4,2.6],'body2')
for k in range(3):both(G,'guarda_furo',13+k*3,34,15+k*3,37,2.58,2.66,'dark')
tube(G,'cano',B,0,22,28,1.3,'steel');tube(G,'rosca',B,0,25,29,1.5,'polish');tube(G,'boca',B,0,28.95,29.1,.9,'dark')
G='coronha'
c(G,'coronha_dobra',[-38,30,-3],[-34,39,3],'body2')
tube(G,'coronha_tubo',35,0,-64,-38,1.8,'body2')
c(G,'coronha_corpo',[-80,26,-2.6],[-58,38,2.6],'body2')
c(G,'coronha_bochecha',[-78,38,-2.2],[-60,40,2.2],'body2')
c(G,'soleira',[-83,24,-3.0],[-80,40,3.0],'rubber')
G='ferrolho'
c(G,'alavanca',[-4,36,-4.4],[0,37.6,-3.0],'polish')
G='carregador'
K.mag_box(G,-5.4,1.4,12,-14,2.0,lean=0,segs=1,ribs=False,base='mag')
c(G,'carregador_bala',[-5,12,-1],[0,13,1],'brass')
K.finish(scale=.235,support=(6,25),grip=(-21,22),muzzle_x=29.1,bore_y=B,iron_y=IRON,rail=(ry,-30,8),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'body':'main','body2':'accent','mag':'mag'},regions={'barrel':12,'stock':-34})
