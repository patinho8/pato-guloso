"""Glock 18 — ferrolho com serrilhas e seletor de rajada, armação de polímero com trilho, punho texturizado, carregador."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('glock',{'slide':(48,49,52),'frame':(40,40,42),'mag':(36,36,38),'steel':(70,72,76),'dark':(10,10,12),'polish':(150,154,160),
 'brass':(196,156,64),'red':(200,30,30),'white':(236,236,236),'engrave':(90,92,96),'rubber':(22,22,24),'orange':(230,110,30)},seed=17)
c=K.cube;both=K.both;tube=K.tube;B=23.4
# ---------- ferrolho (móvel) ----------
G='ferrolho'
c(G,'ferrolho',[-22,20.4,-2.55],[14.6,26.6,2.55],'slide')
both(G,'ferrolho_chanfro',-22,25.6,14.6,27.0,1.6,2.75,'slide',rot=[45,0,0],origin=[-3.7,26.3,2.2])
c(G,'ferrolho_topo',[-22,26.6,-1.7],[14.6,27.2,1.7],'slide')
c(G,'ferrolho_nariz',[14.6,21.0,-2.2],[15.35,26.0,2.2],'slide')
K.serr(G,-21,-14.6,21.2,26.2,2.55,7)
c(G,'janela',[-6,24.2,1.2],[3.2,27.25,2.6],'dark')
c(G,'capuz_cano',[-5.8,24.4,-1.5],[3.0,27.3,1.5],'polish')
c(G,'extrator',[-6.6,24.0,2.5],[-2.2,24.8,2.7],'steel')
c(G,'alca',[-21.4,27.2,-1.8],[-18.6,28.9,1.8],'dark');c(G,'alca_entalhe',[-21.45,28.0,-.5],[-18.55,28.95,.5],'slide')
both(G,'alca_ponto',-21.5,28.2,-21.4,28.5,-1.3,-.9,'white') if False else None
c(G,'alca_marca',[-21.5,28.0,-1.4],[-21.4,28.6,-.9],'white');c(G,'alca_marca2',[-21.5,28.0,.9],[-21.4,28.6,1.4],'white')
c(G,'massa',[11.6,27.2,-.5],[13.0,28.6,.5],'dark');c(G,'massa_ponto',[12.95,27.7,-.3],[13.05,28.3,.3],'white')
c(G,'seletor_rajada',[-21.0,24.6,-3.0],[-18.6,25.8,-2.5],'steel')
c(G,'seletor_rajada_base',[-21.6,23.8,-2.7],[-18.0,26.6,-2.5],'slide')
K.text(G,-12,21.6,-2.57,[.5,.4,.5,.5,.4,.5,.3,.3,.5],h=1.0,s=-1)
c(G,'ferrolho_trava_fundo',[-22.1,20.9,-1.2],[-21.9,26.0,1.2],'steel')
tube(G,'boca',B,0,15.35,15.5,.75,'dark')
# ---------- armação ----------
G='armacao'
c(G,'armacao',[-21.6,17.2,-2.45],[13.4,20.4,2.45],'frame')
c(G,'trilho_frente',[2,15.4,-2.2],[13.0,17.4,2.2],'frame')
for k in range(3):c(G,'trilho_fenda',[4+k*2.8,15.35,-2.25],[5.2+k*2.8,16.6,2.25],'dark')
c(G,'guia_mola',[13.4,18.2,-.7],[13.9,19.4,.7],'polish')
c(G,'trava_desmontagem',[-2.2,18.6,-2.7],[0.2,19.4,2.7],'steel')
c(G,'retem_ferrolho',[-8.4,19.8,-2.75],[-3.6,20.6,-2.45],'steel')
for x in (-9.6,-1.0,-17.4):K.screw(G,x,18.6,2.45,.35,'steel')
K.trigger(G,-6.6,17.2,guard=True,gmat='frame',w=1.2,L=10,depth=5.2)
c(G,'gatilho_seguranca',[-6.4,13.8,-.12],[-5.8,16.8,.12],'dark')
c(G,'reten_carregador',[-12.2,13.6,-2.85],[-10.8,15.6,2.85],'frame')
# punho 22°
GR=dict(rot=[0,0,16],origin=[-16,17.2,0])
c(G,'punho',[-21.0,-5.6,-2.85],[-11.4,17.4,2.85],'frame',**GR)
c(G,'punho_cauda',[-23.6,15.6,-2.3],[-20.4,17.6,2.3],'frame',rot=[0,0,-12],origin=[-21,17,0])
for k in range(3):c(G,'punho_dedo',[-11.8,-1.6+k*5.0,-2.6],[-10.8,1.6+k*5.0,2.6],'frame',**GR)
K.checker(G,-20.2,-3.8,6,11,1.45,2.85,**GR)
c(G,'punho_dorso',[-21.6,-4,-2.2],[-20.8,14,2.2],'frame',**GR)
c(G,'logo',[-18.4,13.6,2.85],[-14.6,15.2,2.95],'engrave',**GR)
c(G,'logo2',[-18.4,13.6,-2.95],[-14.6,15.2,-2.85],'engrave',**GR)
# ---------- carregador (preso no punho) ----------
G='carregador'
c(G,'carregador',[-20.0,-4.6,-2.2],[-12.6,16.4,2.2],'mag',**GR)
c(G,'carregador_base',[-21.2,-6.8,-2.95],[-11.0,-5.4,2.95],'mag',**GR)
c(G,'carregador_furo',[-12.7,-2,-2.25],[-12.5,12,2.25],'orange',**GR) if False else None
c(G,'carregador_bala',[-19,16.4,-1],[-13.4,17.4,1],'brass',**GR)
K.finish(scale=.56,grip=(-16,17.2),muzzle_x=15.5,bore_y=B,iron_y=28.3,rail=(27.2,-15.5,-7),moving=['ferrolho'],magazine=['carregador'],
 roles={'slide':'main','frame':'accent','mag':'mag'},pistol=True,regions={'barrel':16})
