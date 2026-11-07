"""Arruma a sniper A (modelo do jogador): endireita peças tortas (giros de 1–6° sem querer) e acrescenta detalhes."""
import json,uuid
d=json.load(open('tools/sniper_patinho_v2.bbmodel'))
def snap(a,tol=6):
    n=round(a/90)*90;return float(n) if abs(a-n)<=tol else a
for i,e in enumerate(d['elements']):
    if i in (0,1,27) or 61<=i<=65:continue
    e['rotation']=[snap(a) for a in e.get('rotation',[0,0,0])]
d['elements'][2]['rotation']=[0,0,-5]   # base da coronha acompanha a coronha
# cano um pouco mais fino (escala 0,72 na seção, em volta do eixo do cano)
b=d['elements'][66];import numpy as np
vs=np.array(list(b['vertices'].values()),float);c=(vs.min(0)+vs.max(0))/2
for k,v in b['vertices'].items():b['vertices'][k]=[v[0],c[1]+(v[1]-c[1])*.72,c[2]+(v[2]-c[2])*.72]
def cube(name,f,t,rot=None,origin=None):
    fs={k:{"uv":[0,0,4,4],"texture":0} for k in ['north','east','south','west','up','down']}
    e={"name":name,"type":"cube","from":f,"to":t,"rotation":rot or [0,0,0],"origin":origin or [(f[i]+t[i])/2 for i in range(3)],"faces":fs,"uuid":str(uuid.uuid4()),"box_uv":False,"color":0}
    d['elements'].append(e);d['outliner'].append(e['uuid'])
cube('boca_do_cano',[-43.4,25.5,-1.25],[-41.4,27.9,1.25])
cube('ponta_guarda_mao',[-26.1,22.8,-2.2],[-24.6,27.8,2.1])
cube('olhal_frente',[-22.5,22.3,-.4],[-21.5,23.4,.4])
cube('soleira',[16.4,17.6,-1.9],[17.6,26.2,1.9],[0,0,-5],[17,21.9,0])
cube('haste_ferrolho',[-9.5,25.6,-4.6],[-8.7,26.4,-2.1])
cube('bola_ferrolho',[-9.9,25.2,-5.4],[-8.3,26.8,-4.4])
cube('lente_frente',[-23.3,28.6,-1.3],[-23.2,30.8,1.0])
cube('lente_tras',[-7.4,28.8,-1.0],[-7.3,30.5,.6])
cube('tampa_torre',[-16.4,31.7,-1.0],[-14.9,32.1,.8])
d['name']='sniper_patinho_v3'
json.dump(d,open('tools/sniper_patinho_v3.bbmodel','w'))
print(len(d['elements']))
