"""0.74.0 — M4A1 modelada peça por peça (estilo Desert Eagle 0.73): gera tools/m4_claude.bbmodel.
Cano para +x, y para cima, centrada em z=0. 1 unidade = 5 mm. x=0 é a frente do receptor superior; eixo do cano em y=35."""
import sys,os,math
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
from bbkit import Kit
OUT=sys.argv[1] if len(sys.argv)>1 else os.path.dirname(os.path.abspath(__file__))
K=Kit('m4_claude',{'upper':(42,44,48),'lower':(46,48,52),'hg':(38,40,44),'polymer':(28,28,30),'mag':(52,54,50),'steel':(62,64,68),'dark':(12,12,14),
 'polish':(112,116,122),'brass':(196,156,64),'red':(220,30,30),'white':(236,236,236),'engrave':(90,92,96),'rubber':(20,20,22)},seed=4,brushed=('steel','polish'))
c=K.cube;both=K.both;tube=K.tube;post=K.post
B=35   # eixo do cano
def rail(g,x0,x1,y,w=2.15,mat='upper',slot=1.0):
    """Picatinny: base + dentes com fendas"""
    c(g,'trilho_base',[x0,y,-w+.3],[x1,y+.7,w-.3],mat)
    x=x0+.25
    while x+.55<x1:c(g,'trilho_dente',[x,y+.7,-w],[x+.55,y+1.55,w],mat);c(g,'trilho_chanfro',[x,y+1.55,-w+.35],[x+.55,y+1.75,w-.35],mat);x+=slot
# ======================= RECEPTOR SUPERIOR =======================
G='superior'
c(G,'superior',[-36,30.2,-2.5],[0,38.6,2.5],'upper')
c(G,'superior_topo',[-36,38.6,-2.1],[0,40.2,2.1],'upper')
both(G,'chanfro',-36,37.6,0,39.4,2.0,2.6,'upper',rot=[45,0,0],origin=[-18,38.5,2.3])
rail(G,-35.6,0,40.2)
for k in range(1,21):c(G,'numero_trilho',[-35.6+k*1.75-.2,40.6,2.12],[-35.6+k*1.75+.05,40.75,2.16],'white') if k%2==0 else None
c(G,'porca_cano',[-0.2,31.6,-2.6],[1.4,38.4,2.6],'steel')
# janela de ejeção + tampa aberta + defletor + assistente de avanço
c(G,'janela_ejecao',[-22,33.2,2.45],[-10.5,37.0,2.56],'dark')
c(G,'tampa_ejecao',[-22,31.6,2.5],[-10.5,32.2,3.6],'upper',rot=[30,0,0],origin=[-16,32.2,2.5])
c(G,'tampa_ejecao_mola',[-22.4,32.0,2.5],[-21.6,32.6,2.9],'steel')
c(G,'defletor',[-25.2,33.2,2.4],[-22.2,37.6,3.8],'upper',rot=[0,-20,0],origin=[-23.7,35,2.5])
tube(G,'assistente',36.0,3.4,-30.6,-27.6,1.1,'upper')
c(G,'assistente_botao',[-27.8,35.0,4.4],[-27.2,37.0,4.7],'steel')
for k in range(4):c(G,'assistente_estria',[-30.4+k*.7,36.9,3.9],[-30.1+k*.7,37.4,4.5],'dark')
c(G,'pino_trava_sup',[-33.6,31.0,-2.62],[-32.6,32.0,2.62],'steel')
# alça de mira traseira rebatível (no trilho)
c(G,'alca_base',[-34.6,41.95,-2.0],[-29.4,43.0,2.0],'polymer')
c(G,'alca_corpo',[-33.6,43.0,-1.7],[-31.6,47.4,1.7],'polymer')
c(G,'alca_furo',[-33.62,45.2,-.45],[-31.58,46.1,.45],'dark')
c(G,'alca_roda',[-32.8,43.4,1.7],[-32.2,44.6,2.6],'steel')
both(G,'alca_orelha',-33.6,45.6,-31.6,47.8,1.2,1.7,'polymer')
# ======================= RECEPTOR INFERIOR =======================
G='inferior'
c(G,'inferior',[-37,23.2,-2.35],[-3,30.2,2.35],'lower')
c(G,'boca_carregador',[-21.4,16.4,-2.65],[-6.6,23.4,2.65],'lower')
c(G,'boca_chanfro',[-21.8,16.0,-2.9],[-6.2,17.2,2.9],'lower')
c(G,'boca_frente',[-7.2,22.6,-2.4],[-3.0,30.2,2.4],'lower')
c(G,'tampao_tras',[-38.6,26.0,-2.0],[-36.6,30.2,2.0],'lower')
# guarda-mato, gatilho e grupo de controles
c(G,'guarda_mato',[-32.2,17.2,-1.1],[-21.4,18.4,1.1],'lower')
c(G,'guarda_frente',[-22.4,17.2,-1.1],[-21.2,23.4,1.1],'lower')
c(G,'gatilho',[-26.8,19.6,-.45],[-25.6,23.4,.45],'steel',rot=[0,0,-10],origin=[-26.2,23.4,0])
c(G,'gatilho_ponta',[-26.4,18.6,-.45],[-25.2,19.8,.45],'steel',rot=[0,0,14],origin=[-25.8,19.8,0])
for k in range(3):c(G,'gatilho_estria',[-25.66,19.8+k*.9,-.4],[-25.5,20.2+k*.9,.4],'dark',rot=[0,0,-10],origin=[-26.2,23.4,0])
tube(G,'seletor_eixo',27.4,-2.5,-31.4,-30.0,.75,'steel') if False else None
c(G,'seletor',[-32.4,26.8,-3.0],[-28.0,27.8,-2.35],'steel',rot=[0,0,-12],origin=[-30.4,27.3,-2.6])
c(G,'seletor_eixo',[-31.0,26.6,-2.9],[-29.8,28.0,-2.35],'steel')
both(G,'seletor_marca_seg',-34.6,28.4,-33.8,29.0,2.35,2.42,'white')
c(G,'seletor_marca_semi',[-28.4,29.0,-2.42],[-27.6,29.6,-2.35],'red')
c(G,'retem_ferrolho',[-10.4,26.6,-3.0],[-6.4,29.6,-2.35],'steel')
c(G,'retem_aba',[-10.6,24.6,-3.2],[-8.0,27.0,-2.35],'steel')
c(G,'botao_carregador',[-11.6,25.4,2.35],[-9.4,27.6,3.1],'steel')
c(G,'guarda_botao',[-12.4,24.6,2.35],[-8.6,28.4,2.7],'lower')
for x,y in((-35.0,28.6),(-4.4,28.4),(-26.6,27.0),(-23.2,27.0)):
    both(G,'pino',x-.45,y-.45,x+.45,y+.45,2.35,2.48,'steel')
# gravações no inferior (lado esquerdo): "COLT M4 CARBINE"
x=-20.4
for k in range(12):
    w=[.4,.5,.4,.4,.2,.6,.5,.2,.5,.4,.5,.4][k];c(G,'gravacao',[x,24.0,-2.42],[x+w,25.4,-2.35],'engrave');x+=w+.25
c(G,'gravacao_linha',[-20.4,23.6,-2.42],[-12.0,23.75,-2.35],'engrave')
# ======================= PUNHO (A2, inclinado 24°) =======================
G='punho'
GR=dict(rot=[0,0,24],origin=[-31,23.2,0])
c(G,'punho',[-35,6.2,-2.2],[-28.4,23.4,2.2],'polymer',**GR)
c(G,'punho_dedo',[-28.8,13.6,-2.0],[-27.6,15.6,2.0],'polymer',**GR)
c(G,'punho_base',[-35.4,5.6,-2.4],[-28.0,6.6,2.4],'polymer',**GR)
c(G,'punho_dorso',[-35.6,8.0,-1.8],[-34.8,21.0,1.8],'polymer',**GR)
for s in (1,-1):
    for i in range(3):
        for j in range(8):
            z0,z1=(2.2,2.42) if s>0 else (-2.42,-2.2)
            c(G,'punho_quadriculado',[-34.2+i*1.9,8.0+j*1.65,z0],[-33.3+i*1.9,8.9+j*1.65,z1],'rubber',**GR)
# ======================= GUARDA-MÃO M-LOK =======================
G='guarda_mao'
c(G,'guarda_mao',[1.4,30.6,-3.9],[46,39.6,3.9],'hg')
both(G,'gm_chanfro_sup',1.4,38.0,46,40.2,2.9,4.0,'hg',rot=[45,0,0],origin=[23.7,39.1,3.45])
both(G,'gm_chanfro_inf',1.4,29.9,46,32.2,2.9,4.0,'hg',rot=[-45,0,0],origin=[23.7,31.1,3.45])
c(G,'gm_topo',[1.4,39.6,-2.1],[46,40.2,2.1],'hg')
rail(G,1.4,46,40.2,mat='hg')
for k in range(6):   # fendas M-LOK nos lados e embaixo
    x=4+k*6.8
    both(G,'mlok',x,33.4,x+5.0,35.4,3.88,3.96,'dark')
    c(G,'mlok_baixo',[x,30.52,-1.0],[x+5.0,30.62,1.0],'dark')
for k in range(5):both(G,'mlok_alto',6+k*8.0,37.0,9.5+k*8.0,37.8,3.88,3.96,'dark')
c(G,'gm_frente',[45.6,31.4,-3.6],[46.4,39.4,3.6],'hg')
c(G,'qd_copo',[3,30.0,3.0],[5.4,30.7,3.9],'steel')
# ======================= CANO, BLOCO DE GÁS, QUEBRA-CHAMAS =======================
G='cano'
tube(G,'cano',B,0,46,62,1.25,'steel')
tube(G,'cano_m203',B,0,52.6,53.4,1.4,'steel')
c(G,'bloco_gas',[46.4,32.4,-1.7],[50.4,37.2,1.7],'steel')
both(G,'bloco_parafuso',47.4,35.4,48.4,36.4,1.7,1.85,'polish')
tube(G,'rosca',B,0,62,63.6,1.35,'polish')
tube(G,'quebra_chamas',B,0,63.6,73.0,1.85,'steel')
for k in range(5):   # fendas da gaiola (A2)
    a=k*72+90
    yy=B+math.sin(math.radians(a))*1.55;zz=math.cos(math.radians(a))*1.55
    c(G,'qc_fenda',[65.2,yy-.3,zz-.3],[71.6,yy+.3,zz+.3],'dark',rot=[a,0,0],origin=[68.4,yy,zz])
tube(G,'qc_furo',B,0,72.6,73.05,1.1,'dark')
# massa de mira rebatível no trilho dianteiro
c(G,'massa_base',[41.2,41.95,-2.0],[45.6,43.0,2.0],'polymer')
both(G,'massa_orelha',41.8,43.0,44.2,46.6,1.3,1.8,'polymer')
c(G,'massa_poste',[42.6,43.0,-.35],[43.4,45.4,.35],'steel')
c(G,'massa_poste_ponta',[42.7,45.3,-.25],[43.3,45.9,.25],'white')
# ======================= TUBO DO AMORTECEDOR + CORONHA =======================
G='coronha'
tube(G,'tubo',B-1.0,0,-74,-38.6,2.6,'lower')
for k in range(6):c(G,'tubo_furo',[-70+k*1.9,30.5,-.5],[-69.4+k*1.9,31.0,.5],'dark')
tube(G,'porca_castelo',B-1.0,0,-41.4,-38.6,3.0,'steel')
for k in range(4):c(G,'castelo_dente',[-41.4,B-1.0+2.6,-.4+k*0],[-40.0,B-1.0+3.3,.4],'steel',rot=[k*45,0,0],origin=[-40.7,B-1.0,0])
c(G,'placa_final',[-38.8,27.6,-2.7],[-38.0,37.8,2.7],'steel')
c(G,'qd_placa',[-38.6,27.6,2.7],[-37.0,29.4,3.5],'steel')
# coronha retrátil (estilo SOPMOD): corpo, compartimentos, apoio de rosto, soleira
c(G,'coronha_corpo',[-82,26.4,-2.9],[-60,38.4,2.9],'polymer')
both(G,'coronha_tubo_bateria',-81,29.6,-62,35.8,2.9,4.4,'polymer')
both(G,'tampa_bateria',-62.4,30.0,-61.4,35.4,3.4,4.5,'polymer')
c(G,'coronha_bochecha',[-80,38.4,-2.4],[-63,40.2,2.4],'polymer')
c(G,'coronha_baixo',[-82,18.4,-2.4],[-70,26.6,2.4],'polymer')
c(G,'coronha_dente',[-70.4,22.0,-2.2],[-64,26.6,2.2],'polymer',rot=[0,0,24],origin=[-70,26.6,0])
c(G,'soleira',[-84,17.8,-3.1],[-82,40.6,3.1],'rubber')
for k in range(7):c(G,'soleira_estria',[-84.1,19.8+k*2.8,-3.0],[-84.0,20.6+k*2.8,3.0],'dark')
c(G,'alavanca_coronha',[-72,25.6,-.9],[-64,26.6,.9],'steel')
c(G,'qd_coronha',[-80,25.4,3.0],[-78,26.4,3.6],'steel')
c(G,'logo_coronha',[-74,32.4,4.38],[-69,34.2,4.45],'engrave')
# ======================= MÓVEIS: ALAVANCA DE MANEJO + FERROLHO =======================
G='ferrolho'
c(G,'alavanca_haste',[-40.4,38.0,-.7],[-35.6,39.4,.7],'upper')
c(G,'alavanca_T',[-42.0,37.8,-2.6],[-40.2,39.6,2.6],'upper')
c(G,'alavanca_trava',[-41.6,37.9,-3.4],[-39.6,39.5,-2.6],'upper')
c(G,'ferrolho_porta',[-21,33.6,2.2],[-11.6,36.6,2.47],'polish')
c(G,'ferrolho_cabeca',[-12.4,34.2,2.2],[-11.2,36.0,2.5],'steel')
for k in range(3):c(G,'ferrolho_corte',[-19.6+k*2.4,33.6,2.46],[-18.8+k*2.4,36.6,2.5],'steel')
# ======================= CARREGADOR STANAG (curvo) =======================
G='carregador'
segs=[(0,23.4,16.0),(4,16.2,10.4),(9,10.6,5.2),(14,5.4,0.6)]
x0=-20.9;x1=-7.1;prev_shift=0
for i,(ang,ytop,ybot) in enumerate(segs):
    sh=sum(math.tan(math.radians(a))*(t-b) for a,t,b in segs[:i])
    o=[(x0+x1)/2+sh,ytop,0]
    c(G,'carregador',[x0+sh,ybot,-2.4],[x1+sh,ytop,2.4],'mag',rot=[0,0,ang],origin=o)
    both(G,'carregador_nervura',x0+sh+1.6,ybot+.4,x1+sh-1.6,ytop-.4,2.4,2.62,'mag',rot=[0,0,ang],origin=[o[0],o[1],2.5])
    c(G,'carregador_janela',[x0+sh+4,ybot+.8,2.6],[x0+sh+5,ytop-.8,2.66],'dark',rot=[0,0,ang],origin=[o[0],o[1],2.6]) if i<3 else None
sh=sum(math.tan(math.radians(a))*(t-b) for a,t,b in segs)
c(G,'carregador_base',[x0+sh-.8,-0.6,-2.9],[x1+sh+.8,0.8,2.9],'polymer',rot=[0,0,14],origin=[(x0+x1)/2+sh,0.6,0])
c(G,'carregador_puxador',[x0+sh+2,-1.6,-1.4],[x1+sh-2,-0.4,1.4],'polymer',rot=[0,0,14],origin=[(x0+x1)/2+sh,0.6,0])
c(G,'carregador_bala',[-18.6,23.4,-1.0],[-9.6,24.6,1.0],'brass')
c(G,'carregador_ponta',[-9.6,23.6,-.8],[-8.2,24.4,.8],'steel')
idx,sw=K.save(OUT)
import json;json.dump({'sw':sw,'idx':idx},open(os.path.join(OUT,'m4_claude_meta.json'),'w'))
