"""M249 — receptor de chapa com tampa de alimentação, alça de transporte no cano, cilindro de gás, bipé dobrado, caixa de munição com fita, coronha fixa."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('m249',{'steel':(48,50,54),'poly':(36,37,40),'box':(70,74,60),'polish':(140,144,150),'dark':(10,10,12),'brass':(196,156,64),
 'white':(236,236,236),'red':(200,30,30),'rubber':(24,24,26),'engrave':(100,102,106)},seed=249)
c=K.cube;both=K.both;tube=K.tube;B=34
G='receptor'
c(G,'receptor',[-44,25.6,-3.4],[8,38,3.4],'steel')
both(G,'receptor_nervura',-42,28,6,28.6,3.4,3.55,'dark')
c(G,'tampa',[-40,38,-3.2],[2,41.4,3.2],'steel')
both(G,'tampa_chanfro',-40,40.2,2,42.2,2.2,3.4,'steel',rot=[40,0,0],origin=[-19,41.2,2.8])
ry=K.rail(G,-36,0,41.4,w=2.0)
c(G,'tampa_trava',[-42,38,-1.6],[-40,40.6,1.6],'polish')
c(G,'bandeja',[-2,30.4,-6.6],[6,37.2,-3.4],'steel')
c(G,'janela',[-20,25.5,-1.6],[-8,25.65,1.6],'dark')
c(G,'alavanca_fenda',[-30,31,3.38],[-6,32.4,3.5],'dark')
c(G,'olhal',[-44,30,-4],[-42,32,-3.4],'polish')
K.text(G,-38,33,-3.42,[.5,.5,.5,.5,.5],h=1.4,s=-1)
c(G,'alca_base',[-34,43.15,-1.8],[-30,44,1.8],'steel');IRON=K.peep(G,-32,44,w=1.5,h=3.2)
G='guarda_mao'
c(G,'guarda_mao',[8,25,-3.2],[28,32,3.2],'poly')
for k in range(5):both(G,'gm_sulco',10+k*3.6,26,11.4+k*3.6,31,3.2,3.32,'dark')
c(G,'trilho_baixo',[10,24.2,-1.6],[26,25.2,1.6],'steel')
G='cano'
tube(G,'cano',B,0,8,84,1.45,'steel')
tube(G,'cilindro_gas',28.4,0,28,58,1.3,'steel');c(G,'bloco_gas',[56,27,-2],[62,36,2],'steel')
c(G,'alca_transporte_base',[22,35.4,-1.6],[28,38.6,1.6],'steel')
c(G,'alca_transporte',[14,38.6,-1.2],[34,40.4,1.2],'poly');c(G,'alca_transporte_pe',[32,36,-1],[34,40,1],'poly')
c(G,'massa_base',[76,35.6,-1.6],[80,37.2,1.6],'steel');K.post(G,78,37.2,IRON,ears=True)
tube(G,'quebra_chamas',B,0,82,90,1.9,'steel')
for k in range(4):c(G,'qc_fenda',[84,B+1.6,-.3],[89.4,B+2.0,.3],'dark',rot=[k*90+45,0,0],origin=[86.7,B,0])
tube(G,'boca',B,0,89.95,90.1,1.0,'dark')
c(G,'bipe_base',[60,25,-2.4],[64,28,2.4],'steel')
both(G,'bipe_perna',36,25.6,62,26.8,1.4,2.2,'steel')
both(G,'bipe_pe',34,24.8,37,27.4,1.2,2.6,'steel')
G='inferior'
K.trigger(G,-28,25.6,guard=True,gmat='steel',w=1.3,L=12,depth=6.4)
c(G,'seguranca',[-24,26,-3.8],[-22,27.2,3.8],'polish')
GR=dict(rot=[0,0,0],origin=[-34,25.6,0])
c(G,'punho',[-38,8,-2.6],[-30,25.8,2.6],'poly',**GR)
K.checker(G,-37,10,3,8,1.9,2.6,**GR)
c(G,'punho_base',[-38.4,7.2,-2.8],[-29.6,8.2,2.8],'poly',**GR)
G='coronha'
c(G,'coronha',[-84,22,-3.0],[-44,36,3.0],'poly')
c(G,'coronha_vazado',[-76,24.6,-3.05],[-56,28.6,3.05],'dark')
c(G,'coronha_baixo',[-84,18,-2.6],[-70,22.4,2.6],'poly')
c(G,'coronha_bochecha',[-80,36,-2.4],[-54,38.4,2.4],'poly')
c(G,'soleira',[-87,17.4,-3.4],[-84,38.6,3.4],'rubber')
c(G,'coronha_tubo',[-62,14,-1.4],[-58,22,1.4],'steel')
G='ferrolho'
c(G,'alavanca',[-8,31,3.4],[-6,32.4,6.0],'steel');c(G,'alavanca_bola',[-8.4,30.6,5.4],[-5.6,32.8,6.6],'steel')
G='carregador'
c(G,'caixa',[-22,6,-7.4],[-2,25.4,-2.0],'box')
c(G,'caixa_tampa',[-22.4,24.4,-7.8],[-1.6,25.4,-1.6],'box')
c(G,'caixa_alca',[-14,4.6,-6],[-10,6,-3.4],'box')
c(G,'caixa_janela',[-20,14,-7.45],[-14,22,-7.38],'dark')
for k in range(5):c(G,'fita_bala',[-4.0+k*.1,25.4+k*1.2,-6.8+k*.1],[2.0,26.4+k*1.2,-4.4],'brass')
K.finish(scale=.32,support=(18,25),grip=(-34,25.6),muzzle_x=90.1,bore_y=B,iron_y=IRON,rail=(ry,-34,-2),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'steel':'main','poly':'accent','box':'mag'},regions={'barrel':8,'stock':-44})
