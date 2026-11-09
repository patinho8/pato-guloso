"""Deterministic original, face-mapped firearm assets. No stretched whole-image UVs.
Run with Python 3 + CairoSVG. All lengths here are Minecraft model units.
"""
from pathlib import Path
import json,math,random,hashlib,copy
import cairosvg
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/mareeletrica'; D=ROOT/'src/main/resources/data/mareeletrica/recipe'
PIST=lambda i:i in (0,1,10,11,15)
FAMILIES=['desert_eagle','usp','m4a1','ak47','m870','mp5','smg','sniper','mp40','p90','python','glock','scar','ump45','spas12','beretta','uzi','famas','m249','barrett']
TITLES=['Desert Eagle','USP','M4A1','AK-47','M870','MP5','SMG Vetor','Sniper','MP40','P90','Python .357','Glock 18','SCAR-H','UMP-45','SPAS-12','Beretta M9','Uzi','FAMAS','M249','Barrett M82']
ZOOMS=[[1,2,3],[1,2,3],[1,3,6],[1,2,4],[1,2,3],[1,2,3],[1,2,4],[5,12,24],[1,2,3],[1,2,3],[1,2,3],[1,2,3],[1,3,6],[1,2,3],[1,2,3],[1,2,3],[1,2,3],[1,3,6],[1,2,4],[4,8,12]]
CAPS=[7,12,30,30,6,60,30,5,32,50,6,20,20,25,8,15,32,25,100,10]
MARKERS=['quartz','flint','redstone','coal','leather','string','amethyst_shard','lapis_lazuli','copper_ingot','quartz','gold_ingot','iron_nugget','emerald','slime_ball','bone','clay_ball','brick','feather','chain','obsidian']
MUZZLE=[-5,-3,-12,-12,-14,-8,-6,-15,-6,-5,-7,-3,-12,-6,-14,-3,-5,-12,-15,-15]
SKINS=[['Soberana','Safira Imperial','Porcelana','Eclipse Carmesim','Orquídea Elétrica','Dunas','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Catedral','Azul Abissal','Neve Polar','Névoa Rubra','Circuito Violeta','Musgo','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Regalia','Maré Noturna','Geleira','Fênix Mecânica','Néon Urbano','Floresta','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Czar','Dragão Azul','Geada','Fúria Escarlate','Órbita','Savana','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Relicário','Tempestade','Alabastro','Brasa','Ametista','Predador','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Midas','Oceano','Ártico','Víbora','Cyber Sakura','Selva','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Solstício','Raia Azul','Avalanche','Pulso Vermelho','Prisma','Mangue','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Aurea','Serpente Safira','Lótus Branco','Fênix','Nebulosa','Duna Fantasma','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Aço Escovado','Azul Naval','Neve','Ferrugem','Violeta Antigo','Oliva','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Grafite','Azul Ártico','Areia do Deserto','Vermelho Tático','Roxo Neon','Verde Floresta','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Aço Inox','Preto Fosco','Níquel Cetim','Azul Real','Ouro Velho','Verde Militar','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Polímero','Areia FDE','Branco Ártico','Vermelho Racing','Roxo Neon','Oliva','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Areia','Preto Tático','Inverno','Brasa','Céu Noturno','Selva','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Carvão','Coiote','Neve','Lava','Ultravioleta','Pântano','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Aço Fosco','Deserto','Gelo','Ferrugem','Ametista','Mata','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Bronze Militar','Azul Polido','Inox','Rubi','Lilás','Verde Musgo','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Aço Israelense','Cobalto','Branco Gelo','Escarlate','Violeta','Areia','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Tricolor','Marinho','Alpino','Vermelho Legião','Púrpura','Oliva','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Cinza Naval','Azul Aço','Neve','Ferro Vermelho','Ametista','Mato Grosso','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia'],['Preto Fosco','Azul Profundo','Tundra','Carmim','Noite Roxa','Deserto','Tigre','Digital','Fibra de Carbono','Damasco','Hex Neon','Galáxia']]
PALETTES=[('#282e36','#858e98','#b8c2ce'),('#8c5012','#f8d777','#ffeeb8'),('#092f73','#176dd0','#d2aa55'),('#b8d2d7','#f2f5ef','#37718d'),('#171b23','#5c252c','#f26342'),('#292146','#735aa8','#5ce0d0'),('#303d2d','#7b8250','#d2b988')]
def dump(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,ensure_ascii=False,separators=(',',':'))+'\n')
def atlas(f,skin):
 fi=FAMILIES.index(f);lo,hi,accent=PALETTES[skin]
 if skin==0 and f=='desert_eagle':lo,hi,accent='#b5bbc2','#fffdfa','#6c7783'
 if skin==0 and f=='usp':lo,hi,accent='#242c33','#68717b','#bcc0bc'
 rng=random.Random(21000+fi*100+skin)
 svg=['<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512"><defs>',f'<linearGradient id="paint" x1="0" y1="0" x2="0" y2="1"><stop stop-color="{hi}"/><stop offset=".22" stop-color="{lo}"/><stop offset=".52" stop-color="{hi}"/><stop offset="1" stop-color="{lo}"/></linearGradient>', '<linearGradient id="steel" x2="0" y2="1"><stop stop-color="#8796a1"/><stop offset=".3" stop-color="#252d36"/><stop offset=".65" stop-color="#596775"/><stop offset="1" stop-color="#172029"/></linearGradient></defs>', '<rect width="512" height="512" fill="#171e26"/>','<rect width="512" height="256" fill="url(#paint)"/>','<rect y="256" width="512" height="64" fill="url(#steel)"/>','<rect y="320" width="512" height="64" fill="#181d23"/>','<rect y="384" width="512" height="64" fill="#7e4120"/>','<rect y="448" width="512" height="64" fill="#ff2639"/>']
 # Fine machining follows each surface; fixed texel density keeps the receiver top crisp.
 for y in range(1,320,3):svg.append(f'<path d="M0 {y}H512" stroke="#ffffff" stroke-opacity=".065" stroke-width=".55"/>')
 for x in range(0,512,6):svg.append(f'<path d="M{x} 320l-28 64M{x} 320l28 64" stroke="#525a62" stroke-opacity=".36" stroke-width="1"/>')
 for i in range(80):
  x=rng.randrange(512);y=rng.randrange(384,448);svg.append(f'<path d="M{x} {y}q22 -4 54 2" fill="none" stroke="#e39c57" stroke-opacity=".24"/>')
 if skin:
  # Distinct etched motifs per family, continuous side/top panels, overlaid on metal.
  for band in [0,128,192]:
   for x in range(-32,540,42+fi*3):
    y=band+30+(fi%3)*5
    if skin==1:
     svg.append(f'<path d="M{x} {y+44}q-22 -50 20 -54q29 9 0 28q-16 8 -9 -10m9 10q44 -10 35 32q-7 25 -29 11q-13 -16 8 -19" fill="none" stroke="{accent}" stroke-width="1.4"/><path d="M{x-8} {y+50}L{x+24} {y+4}L{x+50} {y+48}" fill="none" stroke="#6e451f" stroke-width=".7"/>')
    elif skin==2:
     svg.append(f'<path d="M{x-20} {y+36}Q{x+12} {y-30} {x+40} {y+22}T{x+100} {y+16}" fill="none" stroke="{accent}" stroke-width="1.8"/><path d="M{x} {y+44}l24 -34l24 34l-24 -12z" fill="#133358" stroke="#87b4ef" stroke-width=".75"/>')
    elif skin==3:
     svg.append(f'<path d="M{x} {y+38}l20 -48l23 50l-23 -13zM{x+20} {y-10}v65" fill="#b5d6dd" fill-opacity=".5" stroke="{accent}" stroke-width="1"/>')
    elif skin==4:
     svg.append(f'<path d="M{x} {y+50}q0 -28 30 -56q-6 28 15 25q-10 37 -45 31zM{x+16} {y+35}q34 -32 44 -29" fill="#922f2f" stroke="{accent}" stroke-width="1.4"/>')
    elif skin==5:
     svg.append(f'<path d="M{x-15} {y+50}h18v-32h26v-26h23v63M{x+7} {y+44}h13v-16h20" fill="none" stroke="{accent}" stroke-width="1.4"/><circle cx="{x+29}" cy="{y-8}" r="3.5" fill="#ebd5f7"/><path d="M{x-7} {y}l12 -10l12 10l-12 10z" fill="#d58dca"/>')
    else:
     svg.append(f'<path d="M{x-14} {y}l21 -17l24 9l-12 21l-24 12l-20 -9z" fill="#28372a" opacity=".6"/><path d="M{x+8} {y+50}l14 -20l26 -7l7 15l-26 16z" fill="#b1aa7a" opacity=".6"/>')
  for y in [5,119,135,184,199,248]:svg.append(f'<path d="M0 {y}H512" stroke="{accent}" stroke-opacity=".72" stroke-width="1"/>')
 # Engraved identification plates, not a stretched photo; stay on side receiver.
 svg.append(f'<text x="177" y="57" font-family="sans-serif" font-size="6" font-weight="bold" letter-spacing="1.2" fill="{accent}">{TITLES[fi].upper()}</text><text x="178" y="65" font-family="monospace" font-size="3.5" fill="{accent}">ME / {210+fi} / {skin:02d}</text>')
 svg.append('</svg>');src=''.join(svg);dest=A/'textures/block'/f'armory21_{f}_{skin}.png';dest.parent.mkdir(parents=True,exist_ok=True);cairosvg.svg2png(bytestring=src.encode(),write_to=str(dest));return src
def box(name,x0,y0,z0,x1,y1,z1,mat='paint',rot=None):
 # UV convention is world-projected by face orientation with 8 pixels per model unit.
 def uv(face):
  if mat=='paint':
   if face in ['east','west']:u0,v0,u1,v1=(z0+16)*8,(16-y1)*8,(z1+16)*8,(16-y0)*8
   elif face in ['up','down']:u0,v0,u1,v1=(z0+16)*8,128+x0*8,(z1+16)*8,128+x1*8
   else:u0,v0,u1,v1=x0*8,128+(16-y1)*8,x1*8,128+(16-y0)*8
  else:
   offset={'metal':256,'rubber':320,'wood':384,'red':448}[mat]
   if face in ['east','west']:a,b,c,d=z0,y0,z1,y1
   elif face in ['up','down']:a,b,c,d=z0,x0,z1,x1
   else:a,b,c,d=x0,y0,x1,y1
   u0,u1=(a+16)*8,(c+16)*8;v0=offset+2;v1=offset+2+min(59,(d-b)*5)
  return [round(v/32,5) for v in (u0,v0,u1,v1)]
 e={'name':name,'from':[x0,y0,z0],'to':[x1,y1,z1],'faces':{f:{'texture':'#atlas','uv':uv(f)} for f in ['north','south','east','west','up','down']}}
 if rot:e['rotation']={'origin':rot[0],'axis':rot[1],'angle':rot[2],'rescale':True}
 return e
def cylinder(name,x,y,z0,z1,r,mat='metal'):
 es=[]
 for angle in [0,45]:es.append(box(name,x-r,y-r*.414,z0,x+r,y+r*.414,z1,mat,([x,y,0],'z',angle) if angle else None));es.append(box(name,x-r*.414,y-r,z0,x+r*.414,y+r,z1,mat,([x,y,0],'z',angle) if angle else None))
 for e in es:
  if 'rotation' in e:e['rotation']['rescale']=False
 return es
def arc_mag(name,x0,x1,z_top,y_top,thick,seg,angles,mat='metal',base_extra=.5):
 import math
 out=[];zc,yc=z_top+thick/2,y_top
 for k,a in enumerate(angles):
  r=math.radians(a);dz,dy=math.sin(r),-math.cos(r)
  cz,cy=zc+dz*seg/2,yc+dy*seg/2
  out.append(box(name,x0,cy-seg/2,cz-thick/2,x1,cy+seg/2,cz+thick/2,mat,([(x0+x1)/2,cy,cz],'x',a) if a else None))
  zc,yc=zc+dz*seg,yc+dy*seg
 a=angles[-1];r=math.radians(a);dz,dy=math.sin(r),-math.cos(r)
 cz,cy=zc+dz*.3,yc+dy*.3
 out.append(box(name+'_base',x0-.25,cy-.3,cz-thick/2-base_extra/2,x1+.25,cy+.3,cz+thick/2+base_extra/2,'rubber',([(x0+x1)/2,cy,cz],'x',a) if a else None))
 return out
def geometry(f):
 i=FAMILIES.index(f);pistol=PIST(i);m=MUZZLE[i];b=[];move=[];mag=[];rounds=[]
 if f=='python':
  # Python .357 revolver: solid frame, fluted cylinder, ribbed barrel with underlug, hammer, wooden grip. Rounds sit inside the cylinder.
  import math
  b+=[box('frame_front',6.75,6.2,-.2,9.25,10.7,.3),box('frame_floor',6.75,5.9,.3,9.25,6.7,5.6),box('frame_roof',6.75,10.5,.3,9.25,11.0,5.6),box('top_strap',6.85,10.1,5.4,9.15,11.0,10.4),box('rear_frame',6.75,6.4,5.4,9.25,10.1,10.2),box('trigger_guard_front',6.7,4.5,4.8,9.3,6.4,5.3,'metal'),box('trigger_guard_base',6.7,4.2,4.8,9.3,4.8,9.4,'metal'),box('trigger',7.7,5.2,6.5,8.3,7.5,7,'metal',([8,6,7],'x',22.5)),box('grip_core',6.7,1.6,8.6,9.3,7.2,11.8,'wood',([8,7.2,9.5],'x',-22.5)),box('grip_butt',6.6,1.0,9.8,9.4,1.8,12.6,'metal',([8,7.2,9.5],'x',-22.5))]
  mag+=cylinder('cylinder',8,8.7,.3,5.0,1.95)
  for k in range(6):
   a=math.radians(k*60+30);mag.append(box('flute',8+1.95*math.cos(a)-.22,8.7+1.95*math.sin(a)-.22,.9,8+1.95*math.cos(a)+.22,8.7+1.95*math.sin(a)+.22,4.4,'rubber'))
  b+=cylinder('barrel',8,9.9,m,.4,.8)+cylinder('muzzle_bore',8,9.9,m-.02,m+.01,.5,'rubber')
  b+=[box('barrel_rib',7.65,10.7,m,8.35,11.2,.4,'metal'),box('underlug',7.35,8.5,m+1.8,8.65,9.3,.4,'metal'),box('front_sight_ramp',7.75,11.2,m,8.25,11.9,m+.9,'metal'),box('cylinder_release',9.25,8.2,5.2,9.7,9.0,6.2,'metal')]
  b+=cylinder('ejector_rod',8,8.2,m+2,.3,.26)
  move+=[box('hammer',7.55,9.9,10.2,8.45,11.9,11.6,'metal',([8,10.4,10.8],'x',22.5)),box('hammer_spur',7.4,11.2,10.6,8.6,11.7,11.9,'rubber',([8,10.4,10.8],'x',22.5))]
  rounds+=[box('round',8+1.2*math.cos(math.radians(k*60+30))-.28,8.7+1.2*math.sin(math.radians(k*60+30))-.28,.6,8+1.2*math.cos(math.radians(k*60+30))+.28,8.7+1.2*math.sin(math.radians(k*60+30))+.28,4.6,'wood') for k in range(6)]
  rearz=10.2;frontz=m+.4;iron=12.0
 elif pistol:
  wide=1.85 if i==0 else 1.5 if f=='glock' else 1.4 if f=='beretta' else 1.55;top=12.3 if i==0 else 11.3 if f=='glock' else 11.5 if f=='beretta' else 11.6
  b += [box('receiver',6.4,7.0,m+.8,9.6,9.8,12.4),box('trigger_guard_front',6.7,4.5,4.8,9.3,7.5,5.3,'metal'),box('trigger_guard_base',6.7,4.2,4.8,9.3,4.8,9.4,'metal'),box('trigger',7.7,5.2,6.5,8.3,7.5,7,'metal',([8,6,7],'x',22.5)),box('grip_core',6.5,1.2,9.2,9.5,7.5,13.1,'paint',([8,7.5,10],'x',-22.5))]
  b += [box('grip_panel_left',6.25,1.8,9.5,6.55,6.5,12.9,'paint',([8,7.5,10],'x',-22.5)),box('grip_panel_right',9.45,1.8,9.5,9.75,6.5,12.9,'paint',([8,7.5,10],'x',-22.5))]
  for x in [6.15,9.55]:
   for y in [2.5,5.5]:b.append(box('grip_screw',x,y,11,x+.3,y+.3,11.35,'metal'))
  move += [box('slide_faceted',8-wide,9.4,m,8+wide,top-1,12.6),box('slide_crown',6.85,top-1,m,9.15,top,12.6)]
  for z in [8,8.7,9.4,10.1,10.8,11.5]:
   for x in [8-wide-.05,8+wide-.10]:move.append(box('slide_serration',x,9.8,z,x+.16,top-.55,z+.18,'metal'))
  move.append(box('ejection_port',9.45,10.2,2.5,9.9,11.25,5.3,'metal'))
  b+=cylinder('barrel',8,10.5,m-.35,m+1,.8);b+=cylinder('muzzle_bore',8,10.5,m-.37,m-.34,.49,'rubber')
  b+=[box('safety',9.6,8.4,9.6,10.05,9,11,'metal'),box('slide_lock',9.55,7.8,6.8,10.05,8.25,9,'metal'),box('hammer',7.45,9.2,12.5,8.55,11,13.1,'metal')]
  mag+=[box('mag_body',6.65,1.1,9.6,9.35,6.1,12.5,'metal',([8,7.5,10],'x',-22.5)),box('mag_base',6.25,.3,11.4,9.75,1.1,14.5)]
  if i==0:
   # Desert Eagle: heavy dust cover, triangular barrel hood, top rail, muzzle slots
   b+=[box('dust_cover',6.2,6.1,m+.8,9.8,7.5,6.0,'paint'),box('barrel_hood',7.1,8.9,m-.9,8.9,10.3,m+2.8,'metal'),box('front_strap',6.55,3.0,5.4,9.45,4.7,6.1,'metal'),box('mag_release',9.5,6.7,9.0,10.0,7.4,9.9,'metal')]
   move+=[box('top_rail',7.4,top,m+1,8.6,top+.3,11.8,'metal')]
   for k in range(10):move.append(box('rail_tooth',7.2,top+.3,m+1.2+k*1.0,8.8,top+.5,m+1.5+k*1.0,'metal'))
   for k in range(3):b.append(box('brake_slot',7.0,10.2,m+.2+k*.7,9.0,11.0,m+.5+k*.7,'rubber'))
  else:
   # USP: accessory rail, rear sight block, mag release, front slide bevel
   b+=[box('under_rail',6.95,6.45,m+1.2,9.05,7.0,5.2,'metal'),box('mag_release',9.5,6.7,8.9,10.0,7.3,9.8,'metal'),box('takedown_lever',9.5,8.2,5.5,9.9,8.7,7.4,'metal')]
   for k in range(5):b.append(box('rail_slot',6.9,6.6,m+1.6+k*.85,9.1,6.9,m+1.9+k*.85,'rubber'))
   move+=[box('rear_sight_block',7.2,top,11.0,8.8,top+.45,12.4,'metal'),box('front_slide_bevel',6.9,top-1.4,m-.25,9.1,top-.1,m+.5,'paint')]
  rearz=11.7;frontz=m+.7;iron=top+1.0
 else:
  b += [box('upper_receiver',6.4,8,0,9.6,11.5,13),box('lower_receiver',6.65,6,4,9.35,8.3,12.5),box('pistol_grip',6.7,1.5,10,9.3,6.5,12.8,'rubber',([8,6,10],'x',-22.5)),box('trigger_guard',6.7,4.6,6.2,9.3,5.1,10.3,'metal'),box('guard_front',6.7,4.8,6.2,9.3,6.5,6.7,'metal'),box('trigger',7.75,5,8.2,8.25,6.3,8.6,'metal')]
  if f=='smg':
   b += [box('vector_lower',6.3,3,0,9.7,8.3,7),box('vector_nose',6.2,8,-4.5,9.8,11.3,1),box('stock_upper',7.1,9,13,8.9,10.7,22),box('stock_pad',6.4,5.5,21,9.6,11,22.5,'rubber')]
  elif f=='mp5':
   b+=cylinder('mp5_tube',8,9.4,-6,12,1.7,'paint');b+=[box('slim_handguard',6.5,6.8,-6,9.5,9.3,1.5,'rubber'),box('stock_rail',7.5,9,12,8.5,10,22,'metal'),box('stock_pad',6.2,5.2,21.5,9.8,11.3,23,'rubber')]
  elif f=='ak47':
   b+=[box('wood_stock',6.7,6,12,9.3,10.5,23,'wood'),box('wood_handguard',6.3,7,-7,9.7,9.8,0,'wood')];b+=cylinder('gas_tube',8,11,-10,1,.6);b+=[box('dust_cover',6.6,11.5,1.5,9.4,12.2,12,'metal'),box('gas_block',7.3,10.2,-9.5,8.7,12.4,-8,'metal'),box('front_sight_post',7.85,12.4,-9.2,8.15,13.7,-8.8,'metal'),box('rear_sight_ladder',7.3,11.9,6,8.7,12.9,8.4,'metal'),box('upper_handguard',6.8,10,-7,9.2,10.9,-.5,'wood'),box('muzzle_nut',7.2,10.2,-13.3,8.8,11.8,-12,'metal')];b+=cylinder('cleaning_rod',8,9.0,-12,-1,.28,'metal')
  elif f=='m870':
   b+=[box('stock',6.5,6,12.5,9.5,10.2,23,'paint'),box('pump',6.3,6.8,-8,9.7,9.2,-1,'rubber')];b+=cylinder('mag_tube',8,8,-13,1,.7)
   for z in range(-8,0):b.append(box('pump_rib',6.15,6.65,z,9.85,7,z+.2,'metal'))
   b+=[box('forend_cap',7.3,7.4,-13.6,8.7,8.7,-12.8,'metal'),box('loading_port',9.55,8.2,2.2,9.75,9.4,6.2,'metal'),box('side_saddle',9.65,7.3,.5,9.95,9.8,8.2,'rubber')]
   for k in range(6):b.append(box('side_shell',9.95,7.6,.7+k*1.1,10.3,9.5,1.5+k*1.1,'red'))
  elif f=='sniper':
   b+=[box('chassis',6.1,6.4,-5,9.9,9,14),box('cheek_piece',6.6,10,15,9.4,11.5,22),box('precision_stock',6.5,6.5,14,9.5,9.8,24),box('buttpad',6.1,5.5,23.5,9.9,11.5,24.5,'rubber'),box('bolt_housing',6.9,9,2,9.1,10.3,11,'metal'),box('brake_body',6.8,9.2,-15.9,9.2,11.4,-14.3,'metal'),box('stock_spacer',6.6,6.2,22.2,9.4,9.9,23.5,'rubber')]
   for k in range(3):b.append(box('brake_slot',6.6,9.6,-15.7+k*.5,9.4,11.0,-15.45+k*.5,'rubber'))
   move+=[box('bolt_handle',9.1,9.4,8,10.7,9.8,8.5,'metal'),box('bolt_knob',10.5,9.1,7.7,11.1,10.1,8.8,'rubber')]
  elif f=='mp40':
   b+=cylinder('barrel_jacket',8,9.8,-6,0,1.5,'metal')
   b+=[box('bakelite_foregrip',6.6,6.2,-1,9.4,8.2,3.2,'rubber'),box('stock_wire_top',7.5,9.3,12,8.5,9.9,22),box('stock_wire_bottom',7.5,7.1,12,8.5,7.7,22),box('stock_rear_plate',6.5,6.5,22,9.5,10.5,23,'metal'),box('stock_hinge',6.9,7.5,12,9.1,10,13,'metal')]
   for z in [-5,-3.4,-1.8]:b.append(box('jacket_vent',6.45,9.2,z,6.65,10.4,z+.8,'rubber'));b.append(box('jacket_vent',9.35,9.2,z,9.55,10.4,z+.8,'rubber'))
  elif f=='scar':
   # SCAR-H: long monolithic upper with full top rail, short railed handguard, side charging handle, folding stock with cheek riser
   b+=[box('scar_upper',6.3,9.4,-9,9.7,11.5,9),box('scar_lower_front',6.6,6.6,-1,9.4,8.3,4),box('scar_hinge',6.6,8.6,12.4,9.4,10.2,13.4,'metal'),box('scar_stock_body',6.4,6.5,13,9.6,11,21),box('scar_cheek',6.8,11,14,9.2,11.8,20,'rubber'),box('scar_buttpad',6.2,5.8,21,9.8,11.4,22.2,'rubber'),box('side_charging',9.7,10.2,-4,10.4,10.9,-2.8,'metal'),box('flash_hider',7.1,9.1,-13.4,8.9,10.9,-12,'metal')]
   for z in range(-9,-1):b.append(box('side_rail',9.65,8.4,z,9.9,9.0,z+.5,'metal'));b.append(box('side_rail',6.1,8.4,z,6.35,9.0,z+.5,'metal'))
  elif f=='ump45':
   # UMP-45: boxy polymer receiver, grip web, skeleton folding stock, under rail
   b+=[box('ump_body',6.3,7.0,-4,9.7,11.3,12),box('ump_grip_web',6.6,4.8,8.4,9.4,7.0,10.2,'rubber'),box('ump_stock_top',7.3,8.6,12,8.7,10.4,20,'rubber'),box('ump_stock_bottom',7.3,6.0,18.5,8.7,10.4,20,'rubber'),box('ump_buttpad',6.6,5.8,20,9.4,10.8,21,'rubber'),box('ump_front_rail',6.9,6.4,-3.6,9.1,7.0,1,'metal')]
   for z in (-3,-1.5,0):b.append(box('ump_vent',9.65,8.6,z,9.85,10.4,z+.8,'rubber'));b.append(box('ump_vent',6.15,8.6,z,6.35,10.4,z+.8,'rubber'))
  elif f=='spas12':
   # SPAS-12: perforated heat shield over the barrel, pump forend, tube magazine, top-folding stock
   b+=[box('spas_heat_shield',6.5,9.3,-13,9.5,11.2,0,'metal'),box('spas_forend',6.2,6.8,-9,9.8,9.3,-2,'rubber'),box('spas_receiver_plate',6.3,7.0,0,9.7,10.6,9,'paint'),box('spas_stock_top',7.4,10.2,12.5,8.6,10.9,22,'metal'),box('spas_stock_strut',7.4,6.3,12.5,8.6,7.0,21,'metal'),box('spas_butt',6.6,6.0,21,9.4,11.0,22.4,'rubber')]
   b+=cylinder('mag_tube',8,8,-13,1,.7)
   for z in range(-12,0,2):b.append(box('shield_hole',6.45,9.8,z,6.65,10.6,z+.9,'rubber'));b.append(box('shield_hole',9.35,9.8,z,9.55,10.6,z+.9,'rubber'))
  elif f=='uzi':
   # Uzi: boxy compact receiver, magazine through the pistol grip, top cocking knob, folding wire stock
   b+=[box('uzi_body',6.3,7.2,-3,9.7,11.2,12),box('uzi_cocking_knob',7.5,11.2,3,8.5,11.9,4.4,'metal'),box('uzi_grip_safety',6.8,4.4,12.5,9.2,7.2,13.4,'rubber'),box('uzi_stock_arm',7.5,8.6,12,8.5,9.2,19.5,'metal'),box('uzi_stock_lower',7.5,7.0,12,8.5,7.6,19.5,'metal'),box('uzi_butt',6.6,6.6,19.5,9.4,9.8,20.3,'metal')]
  elif f=='famas':
   # FAMAS: long bullpup body with a full-length carry handle on top and folded bipod legs
   b+=[box('famas_body',6.2,6.5,-6,9.8,11.0,18),box('famas_handle_bridge',7.3,13.0,-3,8.7,13.6,9),box('famas_handle_front',7.3,11.0,-3,8.7,13.0,-2.2,'rubber'),box('famas_handle_rear',7.3,11.0,8.2,8.7,13.0,9,'rubber'),box('famas_butt',6.0,5.8,18,10.0,11.4,19.4,'rubber'),box('famas_bipod_l',6.3,6.0,-8.5,6.8,6.5,-2,'metal'),box('famas_bipod_r',9.2,6.0,-8.5,9.7,6.5,-2,'metal')]
  elif f=='m249':
   # M249: long barrel with heat shield, carry handle, box magazine on the side, bipod, skeleton stock
   b+=[box('m249_feed_cover',6.3,11.5,1,9.7,12.4,10,'metal'),box('m249_handguard',6.1,7.2,-9,9.9,10.2,0,'rubber'),box('m249_heat_shield',7.0,10.5,-12,9.0,11.2,-6,'metal'),box('m249_carry',7.4,12.4,-4,8.6,13.2,-1,'metal'),box('m249_stock',6.6,6.6,13,9.4,10.6,22,'rubber'),box('m249_butt',6.2,5.8,22,9.8,11.2,23.2,'rubber'),box('m249_bipod_l',6.4,4.0,-10,6.8,7.2,-9.4,'metal'),box('m249_bipod_r',9.2,4.0,-10,9.6,7.2,-9.4,'metal')]
  elif f=='barrett':
   # Barrett M82: heavy upper receiver, long fluted barrel, double-chamber muzzle brake, bipod, long stock with monopod
   b+=[box('barrett_upper',6.0,9.0,-6,10.0,11.6,12),box('barrett_lower',6.4,6.4,-4,9.6,9.0,11),box('barrett_stock',6.4,6.0,12,9.6,11.2,23.5),box('barrett_pad',6.0,5.4,23.5,10.0,11.8,24.6,'rubber'),box('barrett_monopod',7.6,3.4,20,8.4,6.0,20.8,'metal'),box('brake_front',6.6,8.9,-15.9,9.4,11.1,-14.7,'metal'),box('brake_rear',6.6,8.9,-14.2,9.4,11.1,-13.0,'metal'),box('barrett_bipod_l',6.0,4.4,-9,6.5,9.0,-8.4,'metal'),box('barrett_bipod_r',9.5,4.4,-9,10.0,9.0,-8.4,'metal'),box('barrett_carry',7.4,11.6,4,8.6,12.4,6,'metal')]
   for z in range(-12,-6,2):b.append(box('barrel_flute',7.2,10.75,z,8.8,10.85,z+1.2,'rubber'))
  elif f=='p90':
   # P90: bullpup shell, thumbhole grip, top-mounted horizontal magazine, integrated forward sight rail.
   b+=[box('p90_shell',6.2,5.6,-3,9.8,11.5,14,'paint'),box('p90_butt',6.4,5.2,14,9.6,10.8,16,'rubber'),box('p90_thumbhole_bar',6.4,5.6,3.5,9.6,7.4,4.6,'metal'),box('p90_foregrip',6.5,5.0,-3,9.5,6.2,2,'rubber'),box('p90_ejection_chute',6.4,5.2,11.5,9.6,6.2,12.8,'metal')]
   for z in [-2,0,2,4,6,8,10]:b.append(box('p90_vent',9.75,8.0,z,9.95,10.6,z+.7,'rubber'))
  else:
   # M4A1: round free-float handguard with top rail, gas block, delta ring, collapsible stock.
   b+=cylinder('m4_handguard',8,9.2,-8.5,.4,1.75,'paint')
   b+=[box('hg_rail_base',7.1,10.9,-8.5,8.9,11.6,.4,'metal'),box('gas_block',7.2,10.9,-9.2,8.8,12.3,-7.5,'metal'),box('delta_ring',6.1,7.8,.2,9.9,10.8,.9,'metal')]
   for z in range(-8,0):b.append(box('hg_rail_tooth',6.9,11.6,z,9.1,12,z+.45,'metal'))
   for z in [-7.5,-5.7,-3.9,-2.1]:
    for x in [6.15,9.65]:b.append(box('hg_slot',x,8.2,z,x+.2,10.2,z+.9,'rubber'))
   b+=cylinder('buffer_tube',8,9.6,12,19.5,.95,'metal')
   b+=[box('adjustable_stock',6.3,6.6,15.5,9.7,10.8,22.8,'paint'),box('stock_cheek_rib',7,10.8,16,9,11.3,22.4,'rubber'),box('stock_latch',7.4,6.2,15,8.6,6.6,17,'metal'),box('buttpad',6,5.8,22.8,10,11.4,23.8,'rubber')]
   for z in [16.5,18,19.5,21]:
    for x in [6.25,9.65]:b.append(box('stock_groove',x,7.5,z,x+.1,9.8,z+.4,'rubber'))
   b+=[box('magwell_flare',6.4,5.8,3.5,9.6,8.4,7.9,'paint'),box('forward_assist',9.6,9.2,9.6,10.3,10.2,10.5,'metal'),box('brass_deflector',9.6,10.6,8.3,10.2,11.3,9.6,'metal')]
   for dx,dy in [(7.75,11.05),(7.75,8.85)]:b.append(box('flash_slot',dx,dy,m-.4,dx+.5,dy+.1,m+.8,'rubber'))
   for dx,dy in [(6.9,9.75),(9.0,9.75)]:b.append(box('flash_slot',dx,dy-.25,m-.4,dx+.1,dy+.25,m+.8,'rubber'))
  b+=cylinder('barrel',8,10,m,0,.75);b+=cylinder('muzzle_device',8,10,m-.5,m+1,1.05);b+=cylinder('bore',8,10,m-.53,m-.5,.56,'rubber')
  for z in range(0,12):b.append(box('rail_tooth',6.9,11.6,z,9.1,12,z+.45,'metal'))
  if f not in ['ak47','m870','spas12','uzi','famas','m249','barrett']:
   for z in range(-7,0,2):
    for x in [6.4,9.45]:b.append(box('vent',x,8.7,z,x+.16,9.9,z+.9,'rubber'))
  b += [box('selector',9.55,7.5,9,10,8,10,'metal'),box('ejection_port',9.55,8.7,4,9.85,10.5,8,'rubber')]
  move += [box('bolt',9.65,9.1,4,9.95,10.1,7,'metal'),box('charging_handle',9.7,9.4,7,10.9,9.9,7.7,'metal')]
  magz=2 if f=='smg' else 4;magbottom=0 if f not in ['m870','sniper','spas12'] else 3.1
  if f=='ak47':
   mag += arc_mag('ak_mag',6.7,9.3,3.8,7.0,3.3,2.5,[0,-22.5,-22.5,-45],'paint')
  elif f=='p90':mag += [box('p90_mag',6.8,11.5,-1.5,9.2,12.9,11.5,'metal'),box('p90_mag_window',6.95,12.9,0,9.05,13.05,10,'rubber'),box('p90_mag_front',6.8,11.5,-2.2,9.2,12.9,-1.5,'paint')]
  elif f=='mp5':mag += arc_mag('mp5_mag',6.75,9.25,4.2,7.0,2.7,2.4,[0,-22.5,-22.5],'metal')
  elif f=='uzi':mag += [box('uzi_mag',6.95,-3.2,10.6,9.05,6.4,12.0,'metal'),box('uzi_mag_base',6.7,-3.6,10.3,9.3,-3.2,12.3,'rubber')]
  elif f=='famas':mag += [box('famas_mag',6.8,1.5,13.2,9.2,6.5,16.0,'metal'),box('famas_mag_base',6.6,1.1,13.0,9.4,1.5,16.2,'rubber')]
  elif f=='m249':mag += [box('m249_box',4.6,1.6,2.0,8.4,6.6,7.2,'paint'),box('m249_box_lid',4.4,6.6,1.8,8.6,7.0,7.4,'metal'),box('m249_belt',8.4,6.6,3.6,9.4,7.6,5.6,'wood')]
  elif f=='barrett':mag += [box('barrett_mag',6.7,2.2,3.4,9.3,6.4,7.6,'metal'),box('barrett_mag_base',6.5,1.8,3.2,9.5,2.2,7.8,'rubber')]
  elif f=='mp40':mag += [box('mp40_mag',6.9,-3.2,5,9.1,7,6.8,'metal'),box('mp40_mag_base',6.6,-3.6,4.6,9.4,-3.2,7.2,'rubber')]
  elif f=='m4a1':mag += arc_mag('stanag',6.7,9.3,3.6,7.0,3.8,2.1,[0,0,-22.5,-22.5],'paint')
  else:mag.append(box('detachable_mag',6.7,magbottom,magz,9.3,7,magz+3.8))
  for y in [1,2,3,4,5]:
   if y<magbottom or f in ('p90','uzi','famas','m249','barrett'):continue
   for x in [6.6,9.3]:mag.append(box('mag_rib',x,y,magz+.4,x+.12,y+.12,magz+3.2,'metal'))
  rearz=11;frontz=max(m+2,-8);iron=13.5
 # Rear notch and narrow front blade only: no dots on the iron sights.
 for x in [6.85,8.7]:b.append(box('rear_notch',x,iron-.9,rearz,x+.45,iron+.15,rearz+.65,'metal'))
 b.append(box('front_blade',7.84,iron-1.2,frontz,8.16,iron,frontz+.35,'metal'))
 P={'body':b,'moving':move,'magazine':mag}
 if f=='python':P['rounds']=rounds
 return P,iron
def reticle_shapes(f,v,cy,z0):
 R='red';import math
 def bx(n,x0,y0,x1,y1,rot=None):
  e=box(n,x0,y0,z0,x1,y1,z0+.03,R,rot)
  if 'rotation' in e:e['rotation']['rescale']=False
  return e
 if f in ('m4a1','ak47','scar','famas','m249'):
  if v==1:   # holographic ring + dot
   o=[bx('dot',7.88,cy-.12,8.12,cy+.12)]
   for k in range(16):
    a=math.radians(k*22.5);x=8+.66*math.cos(a);y=cy+.66*math.sin(a);o.append(bx('ring',x-.06,y-.06,x+.06,y+.06))
   return o
  if v==2:   # ACOG chevron with ballistic drop marks
   o=[bx('chevron_l',7.62,cy-.06,7.98,cy+.04,([7.8,cy,z0],'z',45)),bx('chevron_r',8.02,cy-.06,8.38,cy+.04,([8.2,cy,z0],'z',-45)),bx('post',7.97,cy-1.05,8.03,cy-.25)]
   for k,wd in enumerate((.34,.28,.22,.16)):o.append(bx('bdc',8-wd,cy-.45-k*.2,8+wd,cy-.40-k*.2))
   return o
  # v==3: duplex crosshair with mil dots
  o=[bx('h_thin',7.0,cy-.025,9.0,cy+.025),bx('h_thick_l',7.0,cy-.07,7.5,cy+.07),bx('h_thick_r',8.5,cy-.07,9.0,cy+.07),bx('v_thin',7.975,cy-1.0,8.025,cy+.8),bx('v_thick_b',7.93,cy-1.0,8.07,cy-.55),bx('v_thick_t',7.93,cy+.5,8.07,cy+.8),bx('dot',7.94,cy-.06,8.06,cy+.06)]
  for x in (7.55,7.77,8.23,8.45):o.append(bx('mil',x-.04,cy-.04,x+.04,cy+.04))
  return o
 if f in ('sniper','barrett'):   # wide duplex: broad bar, long stem down, short stem up
  return [bx('h_thin',6.8,cy-.025,9.2,cy+.025),bx('h_thick_l',6.8,cy-.08,7.5,cy+.08),bx('h_thick_r',8.5,cy-.08,9.2,cy+.08),bx('v_down',7.975,cy-1.4,8.025,cy),bx('v_thick',7.92,cy-1.4,8.08,cy-.8),bx('v_up',7.975,cy,8.025,cy+.45),bx('dot',7.92,cy-.08,8.08,cy+.08)]
 return [box('holographic_dot',7.86,cy-.14,z0,8.14,cy+.14,z0+.03,R)]
def optic(f,v):
 i=FAMILIES.index(f);base=[12.4,11.7,12.3,12.3,12.3,12.3,12.3,12.3,12.3,13.0,12.0,11.4,12.3,12.3,12.3,11.6,12.3,12.3,12.3,12.6][i];cy=[14.4,13.7,14.8,14.5,14.2,14.6,14.4,15.7,14.4,14.9,13.9,13.4,14.8,14.6,14.2,13.6,14.4,14.8,14.6,16.0][i]+(v-1)*.55;cz=5.3 if i in (0,1,11,15) else 7 if i==10 else 6
 e=[box('mount',6.8,base-.3,cz-1.7,9.2,base+.35,cz+1.7,'metal'),box('riser',7.2,base,cz-.8,8.8,cy-.9,cz+.8,'metal')]
 w=([1.4,1.3,2.05,1.7,1.55,1.65,1.6,2.3,1.6,1.6,1.3,1.3,2.05,1.65,1.55,1.3,1.6,1.9,1.8,2.3][i])+(v-1)*.15;h=w if v>1 or f in ['mp5','sniper'] else w*.88
 depth=(1 if PIST(i) else 1.7) if v==1 else (2.9 if PIST(i) else 4) if v==2 else (4.5 if PIST(i) else 7)
 if v>1 or f in ['mp5','sniper']:
  for angle in [0,45]:
   rot=([8,cy,cz],'z',angle) if angle else None
   e += [box('octagon_roof',8-w*.414,cy+w,cz-depth/2,8+w*.414,cy+w+.3,cz+depth/2,'metal',rot),box('octagon_floor',8-w*.414,cy-w-.3,cz-depth/2,8+w*.414,cy-w,cz+depth/2,'metal',rot),box('octagon_left',8-w-.3,cy-w*.414,cz-depth/2,8-w,cy+w*.414,cz+depth/2,'metal',rot),box('octagon_right',8+w,cy-w*.414,cz-depth/2,8+w+.3,cy+w*.414,cz+depth/2,'metal',rot)]
 else:
  for z in [cz-depth/2,cz+depth/2-.25]:
   e += [box('frame_left',8-w-.25,cy-h,z,8-w,cy+h,z+.25,'metal'),box('frame_right',8+w,cy-h,z,8+w+.25,cy+h,z+.25,'metal'),box('frame_top',8-w,cy+h,z,8+w,cy+h+.25,z+.25,'metal'),box('frame_bottom',8-w,cy-h-.25,z,8+w,cy-h,z+.25,'metal')]
  if f in ['m4a1','m870','smg']:
   for x in [8-w-.55,8+w+.3]:e.append(box('protective_hood',x,cy-h-.2,cz-depth/2-.1,x+.25,cy+h+.4,cz+depth/2+.1,'rubber'))
 if v>1:e.append(box('adjuster',8+w+.1,cy-.45,cz-.6,8+w+.8,cy+.45,cz+.6,'rubber'))
 if v==1:e.append(box('battery',8+w+.1,base,cz-.6,8+w+.8,base+.8,cz+.6,'rubber'))
 if f=='ak47':e += [box('side_bracket',9.65,9.7,3,10.1,base+.2,8,'metal'),box('side_bridge',8.7,base,3,10,base+.3,8,'metal')]
 for element in e:
  if 'rotation' in element:element['rotation']['rescale']=False
 dot=reticle_shapes(f,v,cy,cz-depth/2+.4)
 return e,dot,cy
def suppressor(f,v):
 i=FAMILIES.index(f);m=MUZZLE[i];y=(9.9 if i==10 else 10.5) if PIST(i) else 10;length=(8 if f=='usp' else 6 if PIST(i) else 8)+(v-1)*1.3
 # Keep JSON within [-16,32]. The renderer positions this local model at the muzzle.
 e=cylinder('suppressor_shell',8,y,-length,0,1.1+v*.17,'metal')
 for z in [-length+.3,-length+1.1,-1.1,-.4]:e+=cylinder('machined_ring',8,y,z,z+.22,1.23+v*.17,'paint')
 e+=cylinder('dark_bore',8,y,-length-.03,-length,.5,'rubber')
 for k in range(2+v):e.append(box('heat_flute',9.1+v*.17,y-.25,-length+2+k,9.2+v*.17,y+.25,-length+2.4+k,'rubber'))
 if f=='usp':e.append(box('usp_lower_flat',7,y-1.5,-length+.5,9,y-1.2,-.5,'paint'))
 return e
def model(f,skin,es):return {'ambientocclusion':False,'textures':{'atlas':f'mareeletrica:block/armory21_{f}_{skin}','particle':f'mareeletrica:block/armory21_{f}_{skin}'},'elements':es}
def centered_item(es):
 es=copy.deepcopy(es);lo=[min(e['from'][a] for e in es) for a in range(3)];hi=[max(e['to'][a] for e in es) for a in range(3)];delta=[8-(lo[a]+hi[a])/2 for a in range(3)]
 for e in es:
  for k in ['from','to']:e[k]=[round(e[k][a]+delta[a],5) for a in range(3)]
  if 'rotation' in e:e['rotation']['origin']=[round(e['rotation']['origin'][a]+delta[a],5) for a in range(3)]
 return es
DISPLAY={'gui':{'rotation':[10,105,-14],'translation':[0,0,0],'scale':[.68]*3},'ground':{'rotation':[0,0,90],'translation':[0,2,0],'scale':[.4]*3},'fixed':{'rotation':[0,90,0],'scale':[.6]*3},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,0.34,-0.4],'scale':[0.6]*3},'thirdperson_lefthand':{'rotation':[0,0,0],'translation':[0,0.34,-0.4],'scale':[0.6]*3}}
def recipe(id,pattern,key):dump(D/(id+'.json'),{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'id':'mareeletrica:'+id,'count':1}})
def main():
 langs={l:json.loads((A/'lang'/(l+'.json')).read_text()) for l in ['pt_br','en_us']}
 records=[]
 for fi,f in enumerate(FAMILIES):
  parts,iron=geometry(f)
  for skin in range(13):
   if skin<len(PALETTES):atlas(f,skin)   # skins 7-12 come from tools/make_skins.py
   for part,es in parts.items():dump(A/'models/block'/f'armory21_{f}_{part}_{skin}.json',model(f,skin,es))
   view=[e for e in parts['body'] if not any(t in e['name'] for t in ['stock','buttpad','buffer_tube','cheek_piece'])]
   dump(A/'models/block'/f'armory21_{f}_view_{skin}.json',model(f,skin,view))
  for v in range(1,4):
   es,dot,cy=optic(f,v)
   dump(A/'models/block'/f'armory21_{f}_optic_{v}.json',model(f,0,es));dump(A/'models/block'/f'armory21_{f}_reticle_{v}.json',model(f,0,dot));dump(A/'models/block'/f'armory21_{f}_suppressor_{v}.json',model(f,0,suppressor(f,v)))
  for t,num in [('optic',3),('suppressor',3),('skin',12)]:
   for v in range(1,num+1):
    id=f+'_'+t+'_'+str(v)
    if t=='skin':es=[box('paint_chip',2,4,4,14,12,5.5),box('chip_edge',1.7,3.7,4,14.3,4,5.5,'metal')];obj=model(f,v,es)
    else:obj=model(f,0,optic(f,v)[0] if t=='optic' else suppressor(f,v))
    obj['elements']=centered_item(obj['elements']);obj['display']={'gui':{'rotation':[18,115,-10],'translation':[0,0,0],'scale':[.8]*3},'ground':{'translation':[0,3,0],'scale':[.5]*3}};dump(A/'models/item'/(id+'.json'),obj)
    label=(f'Mira {TITLES[fi]} {ZOOMS[fi][v-1]}×' if t=='optic' else f'Silenciador {TITLES[fi]} '+['Compacto','Tático','Integral'][v-1] if t=='suppressor' else TITLES[fi]+' · '+SKINS[fi][v-1])
    for lang in langs:langs[lang]['item.mareeletrica.'+id]=label
    marker='minecraft:'+MARKERS[fi]
    if t=='optic':recipe(id,[[' I ','FGI',' R '],[' II','FGI',' R '],['III','FGI',' R ']][v-1],{'I':'minecraft:iron_ingot','G':'minecraft:glass','F':marker,'R':'minecraft:repeater'})
    elif t=='suppressor':recipe(id,[[' II','FCI','II '],['III','FCI','II '],['III','FCI','III']][v-1],{'I':'minecraft:iron_ingot','C':'minecraft:copper_ingot','F':marker})
    else:recipe(id,['PD ','DIF'],{'P':'minecraft:paper','D':['minecraft:yellow_dye','minecraft:blue_dye','minecraft:white_dye','minecraft:red_dye','minecraft:purple_dye','minecraft:green_dye','minecraft:orange_dye','minecraft:lime_dye','minecraft:black_dye','minecraft:light_gray_dye','minecraft:cyan_dye','minecraft:magenta_dye'][v-1],'I':'minecraft:iron_nugget','F':marker})
  for full in [False,True]:
   id=f+('_magazine' if full else '_empty_magazine');obj=model(f,0,centered_item(parts['magazine']));obj['display']={'gui':{'rotation':[10,110,-12],'translation':[0,0,0],'scale':[1.2]*3},'ground':{'translation':[0,4,0],'scale':[.8]*3}};dump(A/'models/item'/(id+'.json'),obj)
   for lang in langs:langs[lang]['item.mareeletrica.'+id]=f'Pente {TITLES[fi]}'+(' abastecido' if full else ' vazio')
   if not full:recipe(id,['I I','IFI',' N '],{'I':'minecraft:iron_nugget','N':'minecraft:copper_ingot','F':'minecraft:'+MARKERS[fi]})
  legacy=[('rifle_suppressor_'+str(n),'suppressor',min(n,3)) for n in range(1,6)]
  legacy += [('rifle_sticker_'+str(n),'skin',1+(n-1)%6) for n in range(1,17)]
  legacy += [('smg_sticker_'+str(n),'skin',n) for n in range(1,7)]
  legacy += [('smg_scope_'+str(n)+'x','optic',min(range(3),key=lambda j:abs(ZOOMS[fi][j]-n))+1) for n in range(1,5)]
  for old,t,v in legacy:dump(D/f'adapt_{f}_{old}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'mareeletrica:'+old},{'item':'mareeletrica:'+f+'_empty_magazine'}],'result':{'id':'mareeletrica:'+f+'_'+t+'_'+str(v),'count':1}})
  itemid='revolver'  if f=='desert_eagle' else f
  dump(A/'models/item'/(itemid+'.json'),{'parent':'builtin/entity','textures':{'particle':f'mareeletrica:block/armory21_{f}_0'},'display':DISPLAY})
  records.append({'family':f,'skins':6,'capacity':CAPS[fi],'muzzle':MUZZLE[fi],'iron_y':iron,'optic_y':[optic(f,v)[2] for v in range(1,4)]})
 for lang,d in langs.items():dump(A/'lang'/(lang+'.json'),d)
 dump(ROOT/'ARTWORK_0.21.0.json',{'style':'Original parametric hard-surface models; original engraved metal SVG atlases','pixels_per_model_unit':8,'families':records})
 print('Created 56 mapped atlases, 8 firearms, 48 skins, 24 optics, 24 suppressors and 16 magazine items.')
if __name__=='__main__':main()
