"""Beretta M9 — ferrolho aberto com cano à mostra, cão externo, trava no ferrolho, placas de punho, guarda-mato redondo."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('beretta',{'slide':(52,53,56),'frame':(56,57,60),'mag':(54,56,60),'steel':(70,72,76),'dark':(10,10,12),'polish':(150,154,160),
 'brass':(196,156,64),'grip':(26,26,28),'white':(236,236,236),'engrave':(96,98,102),'barrel':(84,86,92)},seed=92)
c=K.cube;both=K.both;tube=K.tube;B=23.6
G='ferrolho'
c(G,'ferrolho_tras',[-22.6,20.8,-2.5],[-4,27.2,2.5],'slide')
both(G,'ferrolho_chanfro',-22.6,26.0,-4,27.6,1.4,2.7,'slide',rot=[40,0,0],origin=[-13,26.8,2.1])
both(G,'ferrolho_lado',-4,20.8,13.6,24.6,1.2,2.5,'slide')   # open top: two side rails
both(G,'ferrolho_lado_topo',-4,24.6,9.6,25.6,1.6,2.5,'slide')
c(G,'ferrolho_ponte',[9.6,20.8,-2.5],[16.4,26.4,2.5],'slide')
c(G,'ferrolho_frente',[15.2,21.4,-2.3],[16.8,25.8,2.3],'slide')
K.serr(G,-21.8,-14.6,21.6,26.2,2.5,7)
c(G,'trava_seg',[-20.6,24.6,-3.3],[-17.2,26.2,-2.5],'steel');c(G,'trava_seg_eixo',[-19.6,24.4,-3.4],[-18.2,26.4,-2.5],'polish')
c(G,'trava_seg_d',[-20.6,24.6,2.5],[-17.2,26.2,3.3],'steel')
c(G,'alca',[-21.6,27.6,-1.8],[-19.0,29.0,1.8],'dark');c(G,'alca_entalhe',[-21.65,28.2,-.45],[-18.95,29.05,.45],'slide')
for z in (-1.3,.8):c(G,'alca_ponto',[-21.7,28.2,z],[-21.6,28.7,z+.5],'white')
c(G,'massa',[13.8,26.4,-.45],[15.4,28.6,.45],'slide');c(G,'massa_ponto',[15.35,27.6,-.3],[15.45,28.2,.3],'white')
K.text(G,-3,21.4,-2.52,[.4,.4,.4,.4,.4,.4,.4],h=1.0,s=-1)
K.text(G,-3,21.4,2.52,[.3,.3,.3,.3,.3,.3,.3,.3,.3],h=.7,s=1)
# barrel showing through the open top
tube(G,'cano',B,0,-4,16.6,1.25,'barrel')
c(G,'cano_capuz',[-6,22.2,-1.6],[-1.4,26.0,1.6],'barrel')
c(G,'extrator',[-7,24.6,2.5],[-3,25.2,2.7],'steel')
tube(G,'boca',B,0,16.55,16.7,.75,'dark')
G='armacao'
c(G,'armacao',[-22.2,17.6,-2.4],[13.6,20.8,2.4],'frame')
c(G,'armacao_frente',[6,16.4,-2.2],[14.2,17.8,2.2],'frame')
c(G,'cao',[-24.6,22.0,-.9],[-22.4,26.0,.9],'steel',rot=[0,0,16],origin=[-23,22,0])
c(G,'cao_espora',[-25.6,25.0,-.9],[-24,26.2,.9],'steel',rot=[0,0,16],origin=[-23,22,0])
c(G,'trava_desmontagem',[1.0,18.8,-2.75],[3.6,20.2,-2.4],'steel');c(G,'botao_desm',[1.8,18.6,2.4],[2.8,19.6,2.7],'polish')
c(G,'retem_ferrolho',[-10.4,19.6,-2.7],[-5,20.8,-2.4],'steel')
for x in (-14,-3.4):K.screw(G,x,19.2,2.4,.35)
K.trigger(G,-7.8,17.6,guard=True,gmat='frame',w=1.2,L=12.6,depth=6.6)
c(G,'reten_carregador',[-12.6,13.2,2.4],[-10.8,15.0,2.9],'steel')
GR=dict(rot=[0,0,14],origin=[-16.6,17.6,0])
c(G,'punho',[-21.6,-5.4,-2.4],[-12.0,17.8,2.4],'frame',**GR)
c(G,'punho_cauda',[-24.0,16.0,-2.3],[-20.8,18.0,2.3],'frame',rot=[0,0,-8],origin=[-21,17,0])
both(G,'placa_punho',-20.8,-3.6,-12.8,14.6,2.4,3.0,'grip',**GR)
K.checker(G,-20.2,-2.6,5,11,1.5,2.95,mat='dark',**GR)
both(G,'placa_parafuso',-17.2,10.6,-16.2,11.6,2.95,3.15,'polish',**GR)
for k in range(9):c(G,'punho_frente_estria',[-12.3,-3+k*2.0,-2.3],[-11.8,-2.1+k*2.0,2.3],'dark',**GR)
c(G,'olhal',[-22.6,-6.2,-.8],[-21.0,-4.8,.8],'steel',**GR)
G='carregador'
c(G,'carregador',[-20.4,-4.2,-2.0],[-13.2,16.6,2.0],'mag',**GR)
c(G,'carregador_base',[-21.4,-6.6,-2.6],[-12.2,-5.2,2.6],'grip',**GR)
c(G,'carregador_bala',[-19.4,16.6,-1],[-14,17.6,1],'brass',**GR)
K.finish(scale=.56,grip=(-16.6,17.6),muzzle_x=16.7,bore_y=B,iron_y=28.6,rail=(27.6,-15.5,-6),moving=['ferrolho'],magazine=['carregador'],
 roles={'slide':'main','frame':'main2','grip':'accent','mag':'mag'},pistol=True,regions={'barrel':17})
