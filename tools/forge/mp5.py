"""MP5A3 — receptor estampado, guarda-mão fino, tubo de manejo, mira de tambor, coronha retrátil, carregador curvo."""
import sys,os,math;sys.path.insert(0,os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from gunforge import Gun
K=Gun('mp5',{'upper':(50,52,56),'polymer':(38,38,40),'mag':(58,60,62),'steel':(58,60,64),'dark':(10,10,12),'polish':(118,122,128),
 'brass':(196,156,64),'red':(210,30,30),'white':(236,236,236),'engrave':(84,86,90),'rubber':(18,18,20)},seed=55)
c=K.cube;both=K.both;tube=K.tube;B=35
# ---------- receptor ----------
G='receptor'
c(G,'receptor',[-44,29,-2.9],[0,39.4,2.9],'upper')
both(G,'receptor_ombro',-44,38.2,0,40.6,1.7,3.1,'upper',rot=[40,0,0],origin=[-22,39.4,2.4])
c(G,'receptor_topo',[-44,39.4,-2.0],[0,41,2.0],'upper')
both(G,'receptor_nervura',-42,31.2,-2,31.8,2.9,3.0,'dark')
both(G,'receptor_nervura2',-42,37.0,-24,37.4,2.9,3.0,'dark')
c(G,'tampa_tras',[-46,29.4,-2.7],[-44,40.6,2.7],'steel')
for x in (-34,-12):c(G,'garra_luneta',[x-1.6,41,-2.2],[x+1.6,41.8,2.2],'upper');c(G,'garra_fenda',[x-.6,41.6,-2.25],[x+.6,41.85,2.25],'dark')
# trilho de mira sobre as garras (montagem de garra)
c(G,'montagem_base',[-36,41.8,-2.0],[-8,42.3,2.0],'steel')
for x in (-34,-12):both(G,'montagem_trava',x-1.2,40.4,x+1.2,42.3,2.2,2.7,'steel');K.screw(G,x,41.4,2.7,.35)
ry=K.rail(G,-35.6,-8.4,42.3,w=1.8,mat='steel')
# janela de ejeção (direita) e alavanca do ferrolho
c(G,'janela',[-24,33.6,2.88],[-13.4,38.0,2.98],'dark')
c(G,'janela_borda',[-24.4,38.0,2.88],[-13.0,38.4,3.1],'upper')
c(G,'ferrolho_face',[-22.6,34.2,2.6],[-15.2,37.4,2.9],'polish')
c(G,'extrator',[-16.4,35.2,2.85],[-15.0,36.6,2.95],'steel')
# frente do receptor: tubo de manejo + soquete do cano
tube(G,'tubo_manejo',39.0,0,0,34,2.3,'upper')
c(G,'tubo_fenda',[6,38.8,-2.38],[30,39.6,-2.2],'dark')
tube(G,'tubo_tampa',39.0,0,34,35.2,2.0,'steel')
c(G,'trava_tubo',[0,30.6,-2.6],[2.4,38.0,2.6],'upper')
# soldas de gancho de bandoleira
both(G,'gancho',-41,40.2,-39.2,41.4,2.1,3.2,'steel')
# gravação do lado esquerdo: "MP5 A3" + seletor
x=K.text(G,-36,32.6,-2.92,[.5,.5,.5,.3,.5,.5],h=1.3,s=-1)
K.text(G,-36,30.4,-2.92,[.3,.3,.3,.3,.3,.3,.3,.3],h=.6,s=-1)
# ---------- mira traseira de tambor ----------
G='miras'
c(G,'tambor_base',[-42.6,41,-2.0],[-38.4,42.6,2.0],'upper')
tube(G,'tambor',44.6,0,-42.2,-38.8,2.2,'steel') if False else None
for a in range(0,180,45):c(G,'tambor',[-41.8,44.6-2.2*math.tan(math.pi/8),-2.2],[-39.2,44.6+2.2*math.tan(math.pi/8),2.2],'steel',rot=[0,0,0] if a==0 else [a,0,0],origin=[-40.5,44.6,0])
c(G,'tambor_furo',[-41.85,44.18,-.42],[-41.7,45.02,.42],'dark')
c(G,'tambor_furo_frente',[-39.3,44.0,-.6],[-39.15,45.2,.6],'dark')
both(G,'tambor_orelha',-42.4,42.6,-38.6,46.2,2.3,2.8,'upper')
c(G,'tambor_parafuso',[-41,42.6,2.75],[-40,43.6,3.1],'polish')
IRON=44.6
# massa de mira com capuz redondo, sobre o tubo de manejo
c(G,'massa_pe',[29.4,41.2,-1.8],[33.6,42.6,1.8],'upper')
K.post(G,31.5,42.6,IRON,hood=2.0,mat='upper')
# ---------- cano, guarda-mão ----------
G='cano'
tube(G,'cano',B,0,0,46,1.25,'steel')
tube(G,'cano_ponta',B,0,44,48,1.65,'steel')
for a in (90,210,330):c(G,'cano_trava',[45,B+1.5,-.5],[47.6,B+2.1,.5],'steel',rot=[a,0,0],origin=[46.3,B,0])
tube(G,'cano_boca',B,0,47.9,48.05,.8,'dark')
G='guarda_mao'
c(G,'guarda_mao',[1.4,27.6,-3.5],[28.4,37.2,3.5],'polymer')
both(G,'gm_chanfro',1.4,35.8,28.4,38.0,2.6,3.7,'polymer',rot=[40,0,0],origin=[14.9,36.6,3.2])
both(G,'gm_chanfro_inf',1.4,27.0,28.4,29.2,2.6,3.7,'polymer',rot=[-40,0,0],origin=[14.9,28.4,3.2])
for k in range(6):c(G,'gm_dedo',[4+k*4,27.2,-2.6],[6+k*4,27.7,2.6],'polymer')
for k in range(5):both(G,'gm_furo',5+k*4.4,31.2,7.4+k*4.4,33.6,3.48,3.56,'dark')
c(G,'gm_frente',[28,28.4,-3.2],[29,36.6,3.2],'polymer')
c(G,'gm_pino',[2,33.4,-3.6],[3,34.4,3.6],'polish')
# ---------- inferior (gatilho Navy) ----------
G='inferior'
c(G,'inferior',[-45,22.6,-2.7],[-17,29.2,2.7],'polymer')
c(G,'inferior_frente',[-18,24.4,-2.6],[-15.4,29.2,2.6],'polymer',rot=[0,0,-30],origin=[-17,29.2,0])
c(G,'inferior_tras',[-47,24,-2.5],[-44.6,29.2,2.5],'polymer')
for x in (-43,-19):K.screw(G,x,27.8,2.7,.6,'steel')
K.trigger(G,-27.6,24.4,guard=True,gmat='polymer',w=1.3,L=12,depth=6.0)
c(G,'seletor',[-42.2,25.4,-3.4],[-36.6,26.6,-2.7],'steel',rot=[0,0,-30],origin=[-39.4,26,-3])
c(G,'seletor_eixo',[-40.0,25.2,-3.5],[-38.8,26.8,-2.7],'polish')
for k,col in enumerate(('white','red','red','red')):c(G,'seletor_marca',[-41+k*1.3,27.6,-2.75],[-40.5+k*1.3,28.2,-2.7],col)
# punho inclinado com textura
GR=dict(rot=[0,0,16],origin=[-37,22.8,0])
c(G,'punho',[-40.6,6,-2.5],[-33.4,22.8,2.5],'polymer',**GR)
for k in range(3):c(G,'punho_dedo',[-34.0,9+k*4.4,-2.3],[-32.8,11.6+k*4.4,2.3],'polymer',**GR)
c(G,'punho_base',[-41.2,5.2,-2.8],[-33,6.4,2.8],'polymer',**GR)
c(G,'punho_dorso',[-41.4,8,-2.0],[-40.4,21.6,2.0],'polymer',**GR)
K.checker(G,-39.8,8.4,4,9,1.45,2.5,**GR)
c(G,'olhal',[-42.6,4.2,-.8],[-40.6,5.6,.8],'steel',**GR)
# boca do carregador e retém tipo pá
c(G,'boca_carregador',[-15.4,21.8,-2.9],[-5.0,29.2,2.9],'upper')
c(G,'boca_chanfro',[-15.8,21.2,-3.1],[-4.6,22.4,3.1],'upper')
c(G,'reten_pa',[-17.8,20.2,-1.4],[-15.2,21.4,1.4],'steel',rot=[0,0,-20],origin=[-15.4,21.4,0])
c(G,'reten_botao',[-14.2,24.4,2.85],[-12.6,26.0,3.3],'steel')
# ---------- coronha retrátil A3 ----------
G='coronha'
c(G,'coronha_bloco',[-49,30,-3.0],[-46,40.0,3.0],'steel')
for y in (38.0,31.4):
    for s in (1,-1):tube(G,'coronha_trilho',y,s*2.4,-80,-46,.7,'steel')
c(G,'coronha_trava',[-52,35.2,-1.2],[-49,37.4,1.2],'steel')
c(G,'coronha_trava_bot',[-51.4,37.4,-.6],[-50.2,38.4,.6],'polish')
c(G,'soleira_suporte',[-81,28.4,-3.2],[-78.4,41.4,3.2],'steel')
c(G,'soleira',[-83.4,21.6,-3.8],[-81,42.6,3.8],'rubber')
for k in range(8):c(G,'soleira_estria',[-83.5,22.6+k*2.5,-3.7],[-83.4,23.4+k*2.5,3.7],'dark')
both(G,'soleira_aba',-81,21.6,-79.4,28.6,2.8,3.8,'rubber')
c(G,'coronha_gancho',[-79.6,29.6,-1],[-78,31.2,1],'steel')
# ---------- móvel: alavanca de manejo (esquerda, dobrada para a frente) ----------
G='ferrolho'
c(G,'alavanca_haste',[24.6,38.4,-5.6],[26.0,39.6,-2.2],'steel',rot=[0,-25,0],origin=[25.3,39,-2.3])
c(G,'alavanca_bola',[25.6,38.1,-6.6],[28.0,39.9,-5.0],'polymer',rot=[0,-25,0],origin=[25.3,39,-2.3])
c(G,'alavanca_base',[24.2,38.2,-2.6],[26.4,39.8,-2.0],'steel')
# ---------- carregador curvo 30 tiros ----------
G='carregador'
xs=K.mag_box(G,-14.6,-5.8,21.8,-1.0,2.15,lean=4,curve=5,segs=4,window=False,base='mag')
K.round_top(G,-14.0,-6.2,21.8,2.15,'mag')
K.finish(scale=.36,grip=(-37,22.8),muzzle_x=48.05,bore_y=B,iron_y=IRON,rail=(ry,-35.6,-8.4),moving=['ferrolho'],magazine=['carregador'],stock=['coronha'],
 roles={'upper':'main','polymer':'accent','mag':'mag'},regions={'barrel':29,'stock':-47})
