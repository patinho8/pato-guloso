"""Modelagem de sniper (competição): gera um .bbmodel (Blockbench, formato livre, cubos) + imagens de prévia.
Barril para +x, y para cima, centrado em z=0 — mesma orientação da AK do jogador. 1 px = 1 px."""
import json,uuid,base64,io,random,sys,os
from PIL import Image
random.seed(77)
OUT=sys.argv[1] if len(sys.argv)>1 else '.'
# ---------- paleta 32x32: swatches 8x8 com ruído ----------
SW={'green':(0,0,(74,86,52)),'green_d':(8,0,(52,61,37)),'metal':(16,0,(44,46,50)),'metal_l':(24,0,(84,88,94)),
    'black':(0,8,(22,22,24)),'lens':(8,8,(40,90,140)),'rubber':(16,8,(30,30,30)),'brass':(24,8,(176,140,60)),
    'tan':(0,16,(150,130,96)),'red':(8,16,(170,30,30)),'steel':(16,16,(120,124,130)),'flute':(24,16,(30,31,34))}
tex=Image.new('RGBA',(32,32),(0,0,0,0))
for n,(x0,y0,c) in SW.items():
    for x in range(8):
        for y in range(8):
            d=random.randint(-7,7) if n not in('lens',) else (12 if (x+y)%7==0 else 0)
            tex.putpixel((x0+x,y0+y),tuple(max(0,min(255,v+d)) for v in c)+(255,))
buf=io.BytesIO();tex.save(buf,'PNG');TEX_SRC='data:image/png;base64,'+base64.b64encode(buf.getvalue()).decode()
def uid():return str(uuid.uuid4())
groups={};order=[]
def cube(group,name,f,t,mat,rot=None,origin=None,faces=None,mats=None):
    x0,y0,_=SW[mat]
    fs={}
    for d in ['north','east','south','west','up','down']:
        m=(mats or {}).get(d,mat);sx,sy,_=SW[m]
        fs[d]={"uv":[sx+1,sy+1,sx+7,sy+7],"texture":0} if (faces is None or d in faces) else {"uv":[0,0,0,0],"texture":None}
    e={"name":name,"box_uv":False,"rescale":False,"locked":False,"render_order":"default","allow_mirror_modeling":True,
       "from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"autouv":0,"color":0,
       "origin":origin or [round((f[i]+t[i])/2,3) for i in range(3)],"rotation":rot or [0,0,0],
       "faces":fs,"type":"cube","uuid":uid()}
    groups.setdefault(group,[]).append(e)
    if group not in order:order.append(group)
    return e
# ================= modelo =================
# --- coronha (polímero verde, com buraco de polegar e apoio de bochecha) ---
G='coronha'
cube(G,'soleira',[-34,13,-1.6],[-33,24.5,1.6],'rubber')
cube(G,'coronha_baixo',[-33,13,-1.4],[-18,16,1.4],'green')
cube(G,'coronha_cima',[-33,21,-1.4],[-15,24,1.4],'green')
cube(G,'coronha_tras',[-33,16,-1.4],[-29,21,1.4],'green')
cube(G,'apoio_bochecha',[-31,24,-1.2],[-21,25.6,1.2],'green_d')
cube(G,'parafuso_bochecha',[-27,24,1.2],[-26,25,1.5],'metal_l')
cube(G,'monopé',[-30,11,-.6],[-28.8,13,.6],'metal')
cube(G,'pé_monopé',[-30.6,10.4,-1],[-28.2,11,1],'rubber')
cube(G,'pescoço',[-20,15,-1.4],[-15,21,1.4],'green')
# --- punho e gatilho ---
G='punho'
cube(G,'punho',[-17,10,-1.3],[-13,18,1.3],'green',rot=[0,0,-22.5],origin=[-15,17,0])
cube(G,'guarda_mato',[-13,15,-.5],[-6,15.8,.5],'metal')
cube(G,'guarda_frente',[-6.8,15.8,-.5],[-6,18,.5],'metal')
cube(G,'gatilho',[-10.2,16,-.25],[-9.6,18,.25],'metal_l',rot=[0,0,-22.5],origin=[-9.9,18,0])
# --- corpo (receptor) ---
G='corpo'
cube(G,'receptor',[-15,18,-1.5],[8,23,1.5],'metal')
cube(G,'trilho',[-12,23,-1],[8,23.6,1],'metal_l')
for i in range(10):cube(G,f'dente_{i}',[-11.5+i*2,23.6,-1],[-10.7+i*2,24,1],'black')
cube(G,'janela_ejeção',[-6,20.5,1.5],[1,22.5,1.55],'black',faces=['east','south','west','north','up','down'])
cube(G,'ferrolho',[-8,21,1.5],[-6,22,2.4],'steel')
cube(G,'alavanca',[-7.4,21.2,2.4],[-6.6,21.8,5],'steel',rot=[0,-22.5,0],origin=[-7,21.5,2.4])
cube(G,'bola_alavanca',[-8.3,20.6,4.3],[-6.3,22.4,6.1],'black')
cube(G,'trava',[-14,23,-.4],[-13,23.6,.4],'red')
# --- carregador ---
G='carregador'
cube(G,'carregador',[-4.5,13,-1.2],[1.5,18,1.2],'metal')
cube(G,'base_carregador',[-4.8,12.4,-1.4],[1.8,13,1.4],'black')
cube(G,'trava_carregador',[1.5,15,-.6],[2.3,17,.6],'metal_l')
# --- guarda-mão ---
G='guarda_mao'
cube(G,'guarda_mao',[8,17.5,-1.7],[24,22,1.7],'green')
cube(G,'guarda_mao_frente',[24,18,-1.5],[26,21.6,1.5],'green_d')
for i in range(4):cube(G,f'respiro_{i}',[11+i*3.4,19,1.7],[13+i*3.4,20.6,1.75],'black',faces=['east','north','south','up','down','west'])
cube(G,'olhal',[20,16.8,-.4],[21,17.5,.4],'metal_l')
# --- bipé (dobrado) ---
G='bipe'
cube(G,'base_bipe',[21,16.2,-1.4],[24,17.5,1.4],'metal')
for z in (-1.2,.4):cube(G,'perna_bipe',[23,16.6,z],[36,17.4,z+.8],'metal_l')
for z in (-1.2,.4):cube(G,'pé_bipe',[35.5,16.3,z-.1],[37,17.6,z+.9],'rubber')
# --- cano + freio de boca ---
G='cano'
cube(G,'cano',[26,20,-.8],[56,21.6,.8],'metal')
for i in range(3):cube(G,f'canelura_{i}',[28,20.3+i*.45,.8],[50,20.5+i*.45,.82],'flute',faces=['south','up','down','east','west'])
cube(G,'freio',[56,19.3,-1.3],[61,22.3,1.3],'metal')
for i in range(3):cube(G,f'porta_freio_{i}',[56.8+i*1.4,19.8,1.3],[57.6+i*1.4,21.8,1.35],'black',faces=['south','east','west','up','down'])
for i in range(3):cube(G,f'porta_freio_b{i}',[56.8+i*1.4,19.8,-1.35],[57.6+i*1.4,21.8,-1.3],'black',faces=['north','east','west','up','down'])
cube(G,'boca',[61,20.3,-.6],[61.1,21.3,.6],'black')
# --- luneta ---
G='luneta'
for x in (-6,4):
    cube(G,'anel',[x,23.6,-1.6],[x+1.8,26,1.6],'metal')
    cube(G,'parafuso_anel',[x+.4,24.2,1.6],[x+1.4,25,2],'metal_l')
cube(G,'tubo',[-9,26,-1.3],[12,28.6,1.3],'black')
cube(G,'sino_frente',[12,25.4,-1.9],[19,29.2,1.9],'black')
cube(G,'para_sol',[19,25.6,-1.8],[22,29,1.8],'black')
cube(G,'lente_frente',[22,25.9,-1.5],[22.05,28.7,1.5],'lens')
cube(G,'ocular',[-15,25.6,-1.7],[-9,29,1.7],'black')
cube(G,'anel_ocular',[-11,25.5,-1.8],[-10,29.1,1.8],'rubber')
cube(G,'lente_tras',[-15.05,25.9,-1.4],[-15,28.7,1.4],'lens')
cube(G,'torre_elevação',[1,28.6,-.9],[3,30.4,.9],'metal_l')
cube(G,'tampa_elevação',[1.2,30.4,-.7],[2.8,30.8,.7],'brass')
cube(G,'torre_vento',[1,26.4,1.3],[3,28.2,2.8],'metal_l')
cube(G,'foco',[-2.5,26.4,1.3],[-1,28.2,2.3],'metal')
cube(G,'marca',[12.4,29.2,-.3],[13.2,29.25,.3],'red',faces=['up'])
# ===== 0.72.0: mais detalhes =====
G='luneta'
for x in (-6,4):   # braçadeiras dos anéis com 2 parafusos de cada lado
    for zz in (-1.8,1.6):cube(G,'braçadeira',[x-.2,24.4,zz],[x+2,25.6,zz+.2],'metal')
    for dx in (.3,1.2):cube(G,'parafuso_anel_b',[x+dx,24.6,-2.0],[x+dx+.5,25.1,-1.8],'metal_l')
for i in range(5):cube(G,'serrilha_torre',[1,28.8+i*.32,-.95],[3,28.95+i*.32,.95],'flute')
for i in range(4):cube(G,'serrilha_vento',[1.1+i*.5,26.5,2.8],[1.35+i*.5,28.1,2.95],'flute')
cube(G,'paralaxe',[-3,26.0,-2.9],[-1,28.6,-1.3],'metal_l')
for i in range(4):cube(G,'serrilha_paralaxe',[-2.9+i*.5,26.1,-3.05],[-2.65+i*.5,28.5,-2.9],'flute')
cube(G,'anel_zoom',[-9,25.8,-1.6],[-7.2,28.8,1.6],'rubber')
for i in range(5):cube(G,'aleta_zoom',[-8.9+i*.36,28.8,-.3],[-8.7+i*.36,29.0,.3],'black')
cube(G,'tampa_frente_aberta',[22.1,29.1,-1.6],[22.5,31.8,1.6],'black',rot=[0,0,-22.5],origin=[22.3,29.1,0])
cube(G,'tampa_tras_aberta',[-15.6,29.1,-1.5],[-15.2,31.6,1.5],'black',rot=[0,0,22.5],origin=[-15.4,29.1,0])
cube(G,'faixa_lente',[19.2,29.0,-1.85],[19.6,29.25,1.85],'red')
G='corpo'
cube(G,'capa_ferrolho',[-17,19,-1.2],[-15,22.6,1.2],'metal')
cube(G,'tampa_ferrolho',[-17.6,19.6,-.9],[-17,22,.9],'steel')
for i in range(3):cube(G,'serrilha_bola',[-8.2,21.0+i*.5,6.1],[-6.4,21.2+i*.5,6.2],'flute')
cube(G,'parafuso_receptor',[-12,19,1.5],[-11.2,19.8,1.6],'metal_l')
cube(G,'parafuso_receptor',[4,19,1.5],[4.8,19.8,1.6],'metal_l')
cube(G,'liberador_ferrolho',[-14.6,21.6,1.5],[-13.4,22.4,1.9],'steel')
cube(G,'boca_carregador',[-5,17.6,-1.6],[2,18.4,1.6],'metal')
cube(G,'liberador_carregador',[-6.4,15.2,-.5],[-5,16.6,.5],'steel')
cube(G,'porca_cano',[24.5,19.6,-1.2],[26,22,1.2],'steel')
G='cano'
cube(G,'rosca',[55,20,-.95],[56,21.6,.95],'steel')
cube(G,'chanfro_freio',[60.4,19.6,-1.1],[61,22,1.1],'steel')
G='guarda_mao'
for i in range(5):
    cube(G,f'mlok_{i}',[10.5+i*2.8,18.2,1.7],[12.2+i*2.8,18.8,1.75],'black',faces=['east','north','south','up','down','west'])
    cube(G,f'mlok_b{i}',[10.5+i*2.8,19,-1.75],[12.2+i*2.8,20.6,-1.7],'black',faces=['east','north','south','up','down','west'])
cube(G,'trilho_baixo',[9,17.1,-.9],[23,17.5,.9],'metal_l')
for i in range(7):cube(G,'dente_baixo',[9.4+i*2,16.8,-.9],[10.2+i*2,17.1,.9],'black')
cube(G,'copo_bandoleira',[9.2,18.4,1.7],[10.2,19.4,2.1],'steel')
G='coronha'
for i in range(3):cube(G,'espaçador',[-33.6+i*.0,13+i*0,-1.5],[-33.2,24.5,1.5],'black') if i==0 else None
cube(G,'botão_bochecha',[-25,24.2,1.25],[-24.2,25.0,1.7],'steel')
cube(G,'botão_bochecha',[-25,24.2,-1.7],[-24.2,25.0,-1.25],'steel')
cube(G,'copo_bandoleira_t',[-31,13.2,1.4],[-30,14.2,1.8],'steel')
cube(G,'faixa_coronha',[-33,16.2,1.42],[-29,16.6,1.45],'tan',faces=['south'])
cube(G,'faixa_coronha',[-33,16.2,-1.45],[-29,16.6,-1.42],'tan',faces=['north'])
G='punho'
for i in range(5):cube(G,'textura_punho',[-16.6,11+i*1.3,1.3],[-13.4,11.5+i*1.3,1.35],'green_d',rot=[0,0,-22.5],origin=[-15,17,0])
for i in range(5):cube(G,'textura_punho',[-16.6,11+i*1.3,-1.35],[-13.4,11.5+i*1.3,-1.3],'green_d',rot=[0,0,-22.5],origin=[-15,17,0])
cube(G,'apoio_polegar',[-13.4,17.6,-1.5],[-11.6,18.2,1.5],'green_d')
# ================= .bbmodel =================
outliner=[];allel=[]
for g in order:
    els=groups[g];allel+=els
    outliner.append({"name":g,"origin":[0,0,0],"color":0,"uuid":uid(),"export":True,"mirror_uv":False,"isOpen":True,"locked":False,"visibility":True,"autouv":0,"children":[e['uuid'] for e in els]})
bb={"meta":{"format_version":"4.10","model_format":"free","box_uv":False},"name":"sniper_claude","model_identifier":"","visible_box":[1,1,0],
    "variable_placeholders":"","variable_placeholder_buttons":[],"timeline_setups":[],"unhandled_root_fields":{},
    "resolution":{"width":32,"height":32},"elements":allel,"outliner":outliner,
    "textures":[{"path":"","name":"sniper_claude.png","folder":"block","namespace":"","id":"0","width":32,"height":32,"uv_width":32,"uv_height":32,
                 "particle":False,"render_mode":"default","render_sides":"auto","frame_time":1,"frame_order_type":"loop","frame_order":"","frame_interpolate":False,
                 "visible":True,"internal":True,"saved":False,"uuid":uid(),"source":TEX_SRC}]}
json.dump(bb,open(os.path.join(OUT,'sniper_claude.bbmodel'),'w'),ensure_ascii=False)
tex.save(os.path.join(OUT,'sniper_claude_textura.png'))
print(len(allel),'cubos')
# ================= prévia (render 3D simples) =================
import numpy as np,math
import matplotlib;matplotlib.use('Agg');import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d.art3d import Poly3DCollection
def rotm(r,o):
    x,y,z=[math.radians(a) for a in r]
    Rx=np.array([[1,0,0],[0,math.cos(x),-math.sin(x)],[0,math.sin(x),math.cos(x)]]);Ry=np.array([[math.cos(y),0,math.sin(y)],[0,1,0],[-math.sin(y),0,math.cos(y)]]);Rz=np.array([[math.cos(z),-math.sin(z),0],[math.sin(z),math.cos(z),0],[0,0,1]])
    return Rx@Ry@Rz
polys=[];cols=[]
L=np.array([.4,.8,.45]);L/=np.linalg.norm(L)
for e in allel:
    f=np.array(e['from']);t=np.array(e['to']);M=rotm(e['rotation'],e['origin']);o=np.array(e['origin'])
    c=[np.array([a,b,d]) for a in (f[0],t[0]) for b in (f[1],t[1]) for d in (f[2],t[2])]
    P=lambda i:M@(c[i]-o)+o
    quads={'west':[0,1,3,2],'east':[4,6,7,5],'down':[0,2,6,4],'up':[1,5,7,3],'north':[0,4,5,1],'south':[2,3,7,6]}
    for d,q in quads.items():
        if e['faces'][d]['texture'] is None:continue
        pts=[P(i) for i in q];n=np.cross(pts[1]-pts[0],pts[2]-pts[0]);nn=np.linalg.norm(n)
        if nn==0:continue
        n/=nn;sh=.55+.45*abs(np.dot(n,L))
        u=e['faces'][d]['uv'];col=np.array(tex.getpixel((u[0]+2,u[1]+2))[:3])/255*sh
        polys.append([(p[0],p[2],p[1]) for p in pts]);cols.append(np.clip(col,0,1))
from PIL import ImageDraw
def render(name,yaw,pitch,W=1600,H=560):
    a,b=math.radians(yaw),math.radians(pitch)
    Ry=np.array([[math.cos(a),0,math.sin(a)],[0,1,0],[-math.sin(a),0,math.cos(a)]]);Rx=np.array([[1,0,0],[0,math.cos(b),-math.sin(b)],[0,math.sin(b),math.cos(b)]])
    R=Rx@Ry;proj=[]
    for P,c in zip(polys,cols):
        Q=[R@np.array([p[0]-14,p[1]-21,p[2]]) for p in P]
        n=np.cross(Q[1]-Q[0],Q[2]-Q[0])
        proj.append((np.mean([q[2] for q in Q]),Q,c))
    proj.sort(key=lambda t:t[0])
    k=W/105;img=Image.new('RGB',(W,H),(207,216,227));d=ImageDraw.Draw(img)
    for _,Q,c in proj:
        pts=[(W/2+q[0]*k,H/2-q[1]*k) for q in Q];col=tuple(int(v*255) for v in c)
        d.polygon(pts,fill=col,outline=tuple(max(0,v-25) for v in col))
    img.save(os.path.join(OUT,f'sniper_claude_{name}.png'))
# polys are (x,z,y) — undo to (x,y,z) for the custom renderer
polys=[[ (p[0],p[2],p[1]) for p in P] for P in polys]
render('prévia_lado',0,0);render('prévia_3d',-28,18);render('prévia_cima',0,80)
