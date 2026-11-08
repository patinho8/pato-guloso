"""0.73.0 — Desert Eagle .50 AE (Mark XIX) modelada peça por peça: gera tools/deagle_claude.bbmodel + prévias.
Cano para +x, y para cima, centrada em z=0. 1 unidade = 5 mm (comprimento real 270 mm = 54 unidades)."""
import json,uuid,base64,io,random,sys,os,math
from PIL import Image
random.seed(50)
OUT=sys.argv[1] if len(sys.argv)>1 else os.path.dirname(os.path.abspath(__file__))
# paleta 32x32: 16 amostras 8x8
SW={'slide':(0,0,(176,180,186)),'frame':(8,0,(160,164,170)),'barrel':(16,0,(188,192,198)),'grip':(24,0,(28,28,30)),
    'grip_d':(0,8,(16,16,18)),'accent':(8,8,(54,56,60)),'dark':(16,8,(14,14,16)),'polish':(24,8,(222,226,232)),
    'red':(0,16,(220,30,30)),'white':(8,16,(240,240,240)),'brass':(16,16,(196,156,64)),'engrave':(24,16,(96,98,104))}
tex=Image.new('RGBA',(32,32),(0,0,0,0))
for n,(x0,y0,c) in SW.items():
    for x in range(8):
        for y in range(8):
            d=random.randint(-5,5) if n not in('polish','white','red') else random.randint(-2,2)
            if n in('slide','barrel','frame','polish'):d+=int(6*math.sin(x*1.3))   # escovado
            tex.putpixel((x0+x,y0+y),tuple(max(0,min(255,v+d)) for v in c)+(255,))
buf=io.BytesIO();tex.save(buf,'PNG');TEX_SRC='data:image/png;base64,'+base64.b64encode(buf.getvalue()).decode()
def uid():return str(uuid.uuid4())
groups={};order=[]
def cube(group,name,f,t,mat,rot=None,origin=None,faces=None):
    fs={}
    for d in ['north','east','south','west','up','down']:
        sx,sy,_=SW[mat]
        fs[d]={"uv":[sx+1,sy+1,sx+7,sy+7],"texture":0} if (faces is None or d in faces) else {"uv":[0,0,0,0],"texture":None}
    e={"name":name,"box_uv":False,"rescale":False,"locked":False,"render_order":"default","allow_mirror_modeling":True,
       "from":[round(v,4) for v in f],"to":[round(v,4) for v in t],"autouv":0,"color":0,
       "origin":origin or [round((f[i]+t[i])/2,4) for i in range(3)],"rotation":rot or [0,0,0],"faces":fs,"type":"cube","uuid":uid()}
    groups.setdefault(group,[]).append(e)
    if group not in order:order.append(group)
    return e
def both(group,name,x0,y0,x1,y1,z0,z1,mat,**k):
    """the same piece on the left (-z) and right (+z) side"""
    cube(group,name+'_d',[x0,y0,z0],[x1,y1,z1],mat,**k);cube(group,name+'_e',[x0,y0,-z1],[x1,y1,-z0],mat,**{a:(None if b is None else ([b[0],b[1],-b[2]] if a=='origin' else b)) for a,b in k.items()})
def tube(group,name,axis_y,axis_z,x0,x1,r,mat,sides=8):
    """octagonal tube along x"""
    for k in range(sides//2):
        a=180/sides*k
        cube(group,name,[x0,axis_y-r*math.tan(math.pi/sides),axis_z-r],[x1,axis_y+r*math.tan(math.pi/sides),axis_z+r],mat,rot=[a,0,0],origin=[x0,axis_y,axis_z])
GRIP=dict(rot=[0,0,-18],origin=[-17.5,15.6,0])
BORE=22.4
# ================================ FERROLHO (anda para trás no tiro) ================================
G='ferrolho'
cube(G,'ferrolho',[-27,19.6,-3.3],[6,25.6,3.3],'slide')
cube(G,'ferrolho_topo',[-27,25.6,-2.3],[6,27.4,2.3],'slide')
for s in (1,-1):   # chanfros de 45° nas quinas de cima
    cube(G,'chanfro',[-27,25.0,s*2.75-.62],[6,26.24,s*2.75+.62],'polish',rot=[45,0,0],origin=[-10.5,25.62,s*2.75])
cube(G,'chanfro_tras',[-27.35,20.2,-3.0],[-27,26.8,3.0],'slide')
for k in range(12):   # serrilhas traseiras inclinadas
    x=-26.2+k*.66
    both(G,'serrilha',x,20.3,x+.3,25.3,3.28,3.38,'dark',rot=[0,0,8],origin=[x,22.8,3.3])
for k in range(5):   # serrilhas dianteiras
    x=0.6+k*.9
    both(G,'serrilha_f',x,20.8,x+.35,24.6,3.28,3.38,'dark')
cube(G,'janela_ejecao',[-5.2,22.0,3.25],[3.2,25.4,3.36],'dark')
cube(G,'cartucho',[-3.6,22.5,2.6],[1.6,24.9,3.3],'brass')
cube(G,'cartucho_ponta',[1.6,22.8,2.7],[2.6,24.6,3.25],'engrave')
cube(G,'extrator',[3.2,23.9,3.3],[5.4,24.7,3.55],'accent')
cube(G,'extrator_pino',[4.6,24.0,3.5],[5.0,24.6,3.62],'polish')
# alça de mira (traseira) com dois pontos brancos
cube(G,'alca_base',[-26.2,27.4,-2.1],[-21.8,28.1,2.1],'accent')
cube(G,'alca',[-25.6,28.1,-1.9],[-23.4,29.7,1.9],'accent')
cube(G,'alca_entalhe',[-25.62,28.9,-.38],[-23.38,29.75,.38],'dark')
for s in (1,-1):cube(G,'ponto_branco',[-25.66,28.95,s*1.05-.28],[-25.6,29.5,s*1.05+.28],'white')
cube(G,'alca_parafuso',[-23.0,27.3,-.4],[-22.2,27.5,.4],'polish')
# travas ambidestras com serrilha
for s in (1,-1):
    cube(G,'trava',[-24.6,24.2,s*3.3 if s>0 else -3.95],[-20.4,25.6,3.95 if s>0 else -3.3],'accent')
    cube(G,'trava_aba',[-21.4,25.6,s*3.3 if s>0 else -3.95],[-19.8,26.4,3.95 if s>0 else -3.3],'accent',rot=[0,0,-20],origin=[-21.4,25.6,s*3.6])
    for k in range(3):cube(G,'trava_serrilha',[-21.2+k*.45,25.7,s*3.9 if s>0 else -4.0],[-21.0+k*.45,26.3,4.0 if s>0 else -3.9],'dark',rot=[0,0,-20],origin=[-21.4,25.6,s*3.6])
# gravação "DESERT EAGLE" no lado esquerdo e marca no direito
random.seed(7)
x=-17.2
for k in range(14):
    h=random.choice([.9,1.1,.7]);w=random.choice([.32,.42])
    cube(G,'gravacao',[x,22.4,-3.36],[x+w,22.4+h,-3.3],'engrave');x+=w+.22
cube(G,'gravacao_linha',[-17.2,21.9,-3.36],[-6.4,22.05,-3.3],'engrave')
cube(G,'logo_aguia',[-14,21.0,3.3],[-11.4,23.2,3.36],'engrave')
cube(G,'logo_aguia2',[-13.4,23.2,3.3],[-12.0,24.0,3.36],'engrave')
# ================================ CANO (fixo, perfil triangular) ================================
G='cano'
cube(G,'cano_baixo',[6,19.6,-3.0],[27,23.4,3.0],'barrel')
cube(G,'cano_meio',[6,23.4,-2.4],[27,25.8,2.4],'barrel')
cube(G,'cano_topo',[6,25.8,-1.7],[27,27.4,1.7],'barrel')
for s in (1,-1):   # faces inclinadas do triângulo
    cube(G,'cano_face',[6,22.0,s*2.35-.35],[27,26.9,s*2.35+.35],'barrel',rot=[s*22,0,0],origin=[16.5,24.4,s*2.35])
    cube(G,'cano_quina',[6,23.15,s*2.95-.25],[27,23.55,s*2.95+.25],'polish',rot=[45,0,0],origin=[16.5,23.35,s*2.95])
cube(G,'boca',[26.9,19.8,-2.8],[27.15,27.2,2.8],'barrel')
tube(G,'boca_coroa',BORE,0,27.1,27.3,1.75,'polish')
tube(G,'boca_furo',BORE,0,26.6,27.32,1.25,'dark')
tube(G,'raias',BORE,0,26.5,27.33,.7,'grip_d')
# trilho Picatinny integrado
cube(G,'trilho_base',[8.2,27.4,-1.55],[25.2,27.9,1.55],'accent')
for k in range(14):
    x=8.6+k*1.18
    cube(G,'trilho_dente',[x,27.9,-1.75],[x+.62,28.45,1.75],'accent')
# massa de mira com ponto vermelho
cube(G,'massa',[24.2,28.45,-.55],[26.0,30.0,.55],'accent')
cube(G,'massa_base',[23.6,27.9,-1.2],[26.4,28.45,1.2],'accent')
cube(G,'massa_ponto',[24.17,29.2,-.3],[24.22,29.7,.3],'red')
# gravação ".50 AE" e ranhuras de alívio
x=10
for k in range(6):
    w=[.3,.5,.5,.25,.5,.5][k];cube(G,'calibre',[x,21.0,3.0],[x+w,22.0,3.06],'engrave');x+=w+.25
for s in (1,-1):cube(G,'ranhura',[9,20.2,s*3.0-.04],[25,20.5,s*3.0+.04],'dark')
cube(G,'parafuso_cano',[20.2,21.4,-3.06],[21.0,22.2,-3.0],'polish')
# ================================ ARMAÇÃO ================================
G='armacao'
cube(G,'armacao',[-24,15.6,-2.9],[22,19.6,2.9],'frame')
cube(G,'trilho_armacao',[-24,19.3,-3.05],[6,19.6,3.05],'accent')
cube(G,'frente_chanfro',[20.8,14.8,-2.9],[22.6,16.6,2.9],'frame',rot=[0,0,45],origin=[21.7,15.6,0])
cube(G,'guia_mola',[22.0,16.6,-.9],[22.35,18.4,.9],'dark')
cube(G,'trilho_baixo',[10.8,15.0,-1.5],[20.2,15.6,1.5],'accent')
for k in range(5):cube(G,'trilho_baixo_dente',[11.2+k*1.8,14.5,-1.6],[12.0+k*1.8,15.0,1.6],'accent')
# guarda-mato quadrado com gancho e estrias
cube(G,'guarda_frente',[3.6,9.8,-1.0],[5.0,15.6,1.0],'frame')
cube(G,'guarda_baixo',[-13.0,9.4,-1.0],[5.0,10.6,1.0],'frame')
cube(G,'guarda_gancho',[4.2,8.8,-1.0],[5.6,9.9,1.0],'frame',rot=[0,0,30],origin=[5,9.4,0])
for k in range(4):cube(G,'guarda_estria',[4.98,10.8+k*.95,-.8],[5.08,11.2+k*.95,.8],'dark')
# gatilho curvo em três partes
cube(G,'gatilho_cima',[-1.6,13.2,-.45],[-.5,15.6,.45],'accent',rot=[0,0,-8],origin=[-1,15.6,0])
cube(G,'gatilho_meio',[-1.5,11.6,-.45],[-.4,13.4,.45],'accent',rot=[0,0,6],origin=[-1,13.3,0])
cube(G,'gatilho_ponta',[-1.1,10.9,-.45],[.1,11.8,.45],'accent',rot=[0,0,24],origin=[-.5,11.8,0])
for k in range(3):cube(G,'gatilho_estria',[-.45,11.9+k*.6,-.4],[-.33,12.2+k*.6,.4],'dark',rot=[0,0,6],origin=[-1,13.3,0])
# alavancas e botões
cube(G,'retem_ferrolho',[-7.2,18.5,-3.45],[1.0,19.3,-2.9],'accent')
cube(G,'retem_aba',[-7.2,17.6,-3.7],[-4.4,19.4,-2.9],'accent')
for k in range(3):cube(G,'retem_estria',[-6.8+k*.8,17.7,-3.78],[-6.5+k*.8,19.3,-3.7],'dark')
tube(G,'botao_desmontagem',17.3,-3.1,3.7,5.1,.65,'polish')
cube(G,'botao_carregador',[-9.8,13.4,-3.25],[-7.8,15.3,-2.7],'accent')
for x,y in ((-18,17.6),(-4,18.6),(14,17.6),(9,16.4)):
    for s in (1,-1):tube(G,'pino',y,s*2.95,x-.4,x+.4,.38,'polish')
# cão (martelo) com espora serrilhada e cauda de castor
cube(G,'cao',[-28.7,21.4,-1.0],[-27.0,26.4,1.0],'accent',rot=[0,0,-14],origin=[-27.8,21.4,0])
cube(G,'cao_espora',[-29.8,25.6,-1.05],[-27.6,26.9,1.05],'accent',rot=[0,0,-14],origin=[-27.8,21.4,0])
for k in range(4):cube(G,'cao_estria',[-29.6+k*.5,26.85,-1.0],[-29.4+k*.5,27.05,1.0],'dark',rot=[0,0,-14],origin=[-27.8,21.4,0])
cube(G,'cauda_castor',[-27.2,16.2,-2.5],[-23.4,18.9,2.5],'frame',rot=[0,0,-12],origin=[-23.4,17.6,0])
cube(G,'cauda_ponta',[-28.2,16.6,-2.2],[-27.0,18.4,2.2],'frame',rot=[0,0,-22],origin=[-27.2,17.6,0])
# ================================ PUNHO (inclinado 18°) ================================
G='punho'
cube(G,'punho',[-23,0.8,-2.9],[-12,17.6,2.9],'frame',**GRIP)
cube(G,'dorso_curvo',[-23.9,3.0,-2.7],[-22.9,12.5,2.7],'frame',**GRIP)
cube(G,'boca_carregador',[-23.4,0.2,-3.2],[-11.6,1.4,3.2],'frame',**GRIP)
for k in range(9):cube(G,'dorso_estria',[-23.08,2.4+k*1.3,-2.4],[-22.95,2.8+k*1.3,2.4],'dark',**GRIP)
for k in range(8):cube(G,'frente_estria',[-12.05,2.6+k*1.4,-2.6],[-11.9,3.1+k*1.4,2.6],'dark',**GRIP)
for s in (1,-1):
    z0,z1=(2.9,3.35) if s>0 else (-3.35,-2.9)
    cube(G,'placa',[-22.2,1.8,z0],[-12.8,14.4,z1],'grip',**GRIP)
    cube(G,'placa_borda',[-22.4,14.4,z0],[-12.6,14.9,z1+.05*s],'grip_d',**GRIP)
    for i in range(6):
        for j in range(9):   # quadriculado
            x=-21.6+i*1.45;y=2.4+j*1.3
            if (i in(2,3)) and j==5:continue
            q0,q1=(3.35,3.62) if s>0 else (-3.62,-3.35)
            cube(G,'quadriculado',[x,y,q0],[x+.75,y+.75,q1],'grip_d',**GRIP)
    q0,q1=(3.35,3.75) if s>0 else (-3.75,-3.35)
    cube(G,'parafuso_placa',[-18.4,8.6,q0],[-16.6,10.0,q1],'polish',**GRIP)
    cube(G,'parafuso_fenda',[-18.3,9.2,q1-.02*s],[-16.7,9.4,q1+.03*s] if s>0 else [-16.7,9.4,q1-.03],'dark',**GRIP) if False else None
# ================================ CARREGADOR (sai no recarregamento) ================================
G='carregador'
cube(G,'carregador',[-21.4,-0.4,-2.2],[-13.6,14.8,2.2],'accent',**GRIP)
cube(G,'carregador_base',[-22.9,-1.5,-3.05],[-12.1,0.3,3.05],'polish',**GRIP)
cube(G,'carregador_base_aba',[-12.5,-1.2,-1.4],[-11.4,0.1,1.4],'polish',**GRIP)
cube(G,'carregador_janela',[-17.8,4,-2.25],[-17.2,11,2.25],'dark',**GRIP)
cube(G,'carregador_bala',[-21.0,14.8,-1.0],[-14.6,16.0,1.0],'brass',**GRIP)
# ================= exportar .bbmodel =================
outliner=[];allel=[];idx={}
for g in order:
    els=groups[g];idx[g]=list(range(len(allel),len(allel)+len(els)));allel+=els
    outliner.append({"name":g,"origin":[0,0,0],"color":0,"uuid":uid(),"export":True,"mirror_uv":False,"isOpen":True,"locked":False,"visibility":True,"autouv":0,"children":[e['uuid'] for e in els]})
bb={"meta":{"format_version":"4.10","model_format":"free","box_uv":False},"name":"deagle_claude","model_identifier":"","visible_box":[1,1,0],
    "variable_placeholders":"","variable_placeholder_buttons":[],"timeline_setups":[],"unhandled_root_fields":{},
    "resolution":{"width":32,"height":32},"elements":allel,"outliner":outliner,
    "textures":[{"path":"","name":"deagle_claude.png","folder":"block","namespace":"","id":"0","width":32,"height":32,"uv_width":32,"uv_height":32,
                 "particle":False,"render_mode":"default","render_sides":"auto","frame_time":1,"frame_order_type":"loop","frame_order":"","frame_interpolate":False,
                 "visible":True,"internal":True,"saved":False,"uuid":uid(),"source":TEX_SRC}]}
json.dump(bb,open(os.path.join(OUT,'deagle_claude.bbmodel'),'w'),ensure_ascii=False)
json.dump(idx,open(os.path.join(OUT,'deagle_claude_groups.json'),'w'))
print(len(allel),'cubos',{g:len(v) for g,v in idx.items()})
