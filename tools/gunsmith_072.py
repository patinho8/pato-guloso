"""0.72.0 — Gunsmith: detailed java models for 22 arsenal guns (body/view/moving/magazine per skin), plus optics that fit each
gun's rail (red dot / holographic / prism / rifle scope, sized by zoom), suppressors and the data the renderer needs
(muzzle, bore height, sight heights). Same atlas UV convention as generate_armory21 (8 px per model unit on a 512 atlas),
so all 13 skins keep working. Coordinates: x across (centre 8), y up, z along the gun (muzzle negative, stock positive)."""
import json,math,os,copy,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/mareeletrica';MB=A/'models/block'
def dump(path,obj):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(obj,ensure_ascii=False,separators=(',',':'))+'\n')
# ---------------- primitives (UVs as generate_armory21.box) ----------------
def box(name,x0,y0,z0,x1,y1,z1,mat='paint',rot=None):
 x0,x1=min(x0,x1),max(x0,x1);y0,y1=min(y0,y1),max(y0,y1);z0,z1=min(z0,z1),max(z0,z1)
 for v in (x0,y0,z0,x1,y1,z1):assert -16<=v<=32,(name,v)
 def uv(face):
  if mat=='paint':
   if face in ['east','west']:u0,v0,u1,v1=(z0+16)*8,(16-y1)*8,(z1+16)*8,(16-y0)*8
   elif face in ['up','down']:u0,v0,u1,v1=(z0+16)*8,128+x0*8,(z1+16)*8,128+x1*8
   else:u0,v0,u1,v1=x0*8,128+(16-y1)*8,x1*8,128+(16-y0)*8
   u0,u1=max(0,min(512,u0)),max(0,min(512,u1));v0,v1=max(0,min(512,v0)),max(0,min(512,v1))
  else:
   offset={'metal':256,'rubber':320,'wood':384,'red':448}[mat]
   if face in ['east','west']:a,b,c,d=z0,y0,z1,y1
   elif face in ['up','down']:a,b,c,d=z0,x0,z1,x1
   else:a,b,c,d=x0,y0,x1,y1
   u0,u1=(a+16)*8,(c+16)*8;v0=offset+2;v1=offset+2+min(59,(d-b)*5)
   u0,u1=max(0,min(512,u0)),max(0,min(512,u1))
  return [round(v/32,5) for v in (u0,v0,u1,v1)]
 e={'name':name,'from':[round(x0,4),round(y0,4),round(z0,4)],'to':[round(x1,4),round(y1,4),round(z1,4)],'faces':{f:{'texture':'#atlas','uv':uv(f)} for f in ['north','south','east','west','up','down']}}
 if rot:e['rotation']={'origin':[round(v,4) for v in rot[0]],'axis':rot[1],'angle':rot[2],'rescale':False}
 return e
X=8
def cyl(name,y,z0,z1,r,mat='metal',x=X):
 """octagonal tube along z"""
 return [box(name,x-r,y-r*.414,z0,x+r,y+r*.414,z1,mat),box(name,x-r*.414,y-r,z0,x+r*.414,y+r,z1,mat),
         box(name,x-r,y-r*.414,z0,x+r,y+r*.414,z1,mat,([x,y,z0],'z',45)),box(name,x-r*.414,y-r,z0,x+r*.414,y+r,z1,mat,([x,y,z0],'z',45))]
def cyl_y(name,x,z,y0,y1,r,mat='metal'):
 """octagonal post along y"""
 return [box(name,x-r,y0,z-r*.414,x+r,y1,z+r*.414,mat),box(name,x-r*.414,y0,z-r,x+r*.414,y1,z+r,mat),box(name,x-r,y0,z-r*.414,x+r,y1,z+r*.414,mat,([x,y0,z],'y',45))]
def ring(name,y,z,r,t=.25,mat='metal'):return cyl(name,y,z,z+t,r,mat)
def rail(name,y,z0,z1,w=1.25,mat='metal',teeth=True):
 """picatinny: base + cross slots (teeth)"""
 o=[box(name,X-w*.8,y,z0,X+w*.8,y+.35,z1,mat),box(name+'_lip',X-w,y+.35,z0,X+w,y+.6,z1,mat)]
 if teeth:
  z=z0+.25
  while z+.45<z1:o.append(box(name+'_tooth',X-w,y+.6,z,X+w,y+.85,z+.45,mat));z+=.8
 return o
def side_rail(name,side,y,z0,z1,mat='metal'):
 x=X+side*1.7;o=[box(name,x-.25*side,y-.45,z0,x+.15*side,y+.45,z1,mat)]
 z=z0+.2
 while z+.4<z1:o.append(box(name+'_tooth',x+.15*side,y-.45,z,x+.35*side,y+.45,z+.4,mat));z+=.75
 return o
def vents(name,x0,x1,y0,y1,z0,z1,n,mat='rubber',side=0):
 """dark slots on both sides of a handguard (inset 0.02 outside the surface)"""
 o=[];step=(z1-z0)/n
 for k in range(n):
  za=z0+k*step+step*.2;zb=za+step*.6
  o.append(box(name,x0-.04,y0,za,x0+.02,y1,zb,mat));o.append(box(name,x1-.02,y0,za,x1+.04,y1,zb,mat))
 return o
def screws(name,pts,mat='metal'):return [box(name,x-.18 if x<X else x-.02,y-.18,z-.18,x+.02 if x<X else x+.18,y+.18,z+.18,mat) for x,y,z in pts]
def grip(name,z,ytop,ybot,w=1.3,mat='rubber',angle=-22.5,front=2.8,texture=True):
 """pistol grip raked back, with finger grooves on the front and stippling on the sides"""
 o=[box(name,X-w,ybot,z,X+w,ytop,z+front,mat,([X,ytop,z],'x',angle))]
 if texture:
  for k in range(3):o.append(box(name+'_groove',X-w+.1,ybot+.8+k*1.3,z-.15,X+w-.1,ybot+1.3+k*1.3,z+.05,mat,([X,ytop,z],'x',angle)))
  for s in (-1,1):
   for k in range(4):o.append(box(name+'_stipple',X+s*w-(.04 if s>0 else -.0),ybot+.6+k*1.1,z+.5,X+s*w+(.04 if s>0 else 0)-(0 if s>0 else .04),ybot+1.0+k*1.1,z+front-.5,'metal',([X,ytop,z],'x',angle)))
  o.append(box(name+'_cap',X-w-.05,ybot-.3,z-.2,X+w+.05,ybot,z+front+.3,'metal',([X,ytop,z],'x',angle)))
 return o
def trigger_group(z0,z1,y,ztrig,ytrig=None,mat='metal'):
 ytrig=ytrig or y+1.6
 return [box('trigger_guard',X-.7,y-.25,z0,X+.7,y+.25,z1,mat),box('guard_front',X-.7,y,z0,X+.7,y+1.6,z0+.45,mat),
         box('trigger',X-.25,y+.4,ztrig,X+.25,ytrig,ztrig+.45,'metal',([X,ytrig,ztrig],'x',22.5)),box('trigger_shoe',X-.3,y+.35,ztrig-.25,X+.3,y+.75,ztrig+.25,'metal',([X,ytrig,ztrig],'x',22.5))]
def selector(z,y,side=1):return [box('selector',X+side*1.5,y-.25,z-.3,X+side*1.85,y+.25,z+.9,'metal'),box('selector_hub',X+side*1.45,y-.4,z-.4,X+side*1.75,y+.4,z+.4,'metal')]
def ejection_port(z0,z1,y0,y1,xr=9.6):return [box('ejection_port',xr-.02,y0,z0,xr+.05,y1,z1,'rubber'),box('port_cover_hinge',xr,y0-.25,z0,xr+.15,y0,z1,'metal')]
def front_sight_a(z,ybase,top,w=.9):
 """A-frame front sight with a post"""
 return [box('fsb_base',X-w,ybase,z-.6,X+w,ybase+.6,z+.6,'metal'),box('fsb_wing_l',X-w,ybase+.5,z-.4,X-w+.35,top,z+.4,'metal'),box('fsb_wing_r',X+w-.35,ybase+.5,z-.4,X+w,top,z+.4,'metal'),
         box('front_post',X-.15,ybase+.5,z-.15,X+.15,top-.05,z+.15,'metal')]
def rear_aperture(z,ybase,top):
 return [box('rear_sight_base',X-.9,ybase,z,X+.9,ybase+.6,z+1.2,'metal'),box('rear_ear_l',X-.9,ybase+.6,z+.2,X-.5,top,z+.9,'metal'),box('rear_ear_r',X+.5,ybase+.6,z+.2,X+.9,top,z+.9,'metal'),
         box('aperture_l',X-.5,top-.7,z+.4,X-.15,top-.2,z+.7,'metal'),box('aperture_r',X+.15,top-.7,z+.4,X+.5,top-.2,z+.7,'metal')]
def muzzle_device(kind,zm,y,r):
 """kind: birdcage, brake, comp, flash, crown, shotgun"""
 o=[]
 if kind=='birdcage':
  o+=cyl('flash_hider',y,zm,zm+2.2,r*1.15)
  for k in range(4):a=k*90+45;dx=math.cos(math.radians(a))*r*.9;dy=math.sin(math.radians(a))*r*.9;o.append(box('hider_slot',X+dx-.12,y+dy-.12,zm+.1,X+dx+.12,y+dy+.12,zm+1.6,'rubber'))
  o+=cyl('hider_bore',y,zm-.03,zm,r*.55,'rubber')
 elif kind=='brake':
  o+=[box('brake',X-r*1.6,y-r*1.1,zm,X+r*1.6,y+r*1.1,zm+3,'metal')]
  for k in range(3):o+=[box('brake_port',X-r*1.6-.04,y-r*.7,zm+.4+k*.85,X-r*1.6+.02,y+r*.7,zm+.85+k*.85,'rubber'),box('brake_port',X+r*1.6-.02,y-r*.7,zm+.4+k*.85,X+r*1.6+.04,y+r*.7,zm+.85+k*.85,'rubber')]
  o+=cyl('brake_bore',y,zm-.03,zm,r*.5,'rubber')
 elif kind=='comp':
  o+=cyl('compensator',y,zm,zm+1.8,r*1.2)
  for k in range(2):o.append(box('comp_port',X-r*.5,y+r*1.15,zm+.3+k*.7,X+r*.5,y+r*1.25,zm+.6+k*.7,'rubber'))
  o+=cyl('comp_bore',y,zm-.03,zm,r*.55,'rubber')
 elif kind=='shotgun':
  o+=cyl('choke',y,zm,zm+.6,r*1.05)+cyl('shotgun_bore',y,zm-.03,zm,r*.75,'rubber')
  o.append(box('bead',X-.15,y+r,zm+.3,X+.15,y+r+.35,zm+.6,'red'))
 else:
  o+=cyl('crown',y,zm,zm+.4,r*1.08)+cyl('bore',y,zm-.03,zm,r*.55,'rubber')
 return o
def stock_m4(z0,y,mat='rubber'):
 """buffer tube + collapsible stock with cheek and pad"""
 o=cyl('buffer_tube',y+.9,z0,z0+7,.75,'metal')
 o+=[box('stock_body',X-1.1,y-1.2,z0+5,X+1.1,y+2.0,z0+10.5,mat),box('stock_cheek',X-.95,y+2.0,z0+5.5,X+.95,y+2.5,z0+10,mat),
     box('stock_lower',X-.9,y-3.2,z0+8,X+.9,y-1.2,z0+10.5,mat),box('stock_pad',X-1.2,y-3.4,z0+10.5,X+1.2,y+2.6,z0+11.2,'rubber'),
     box('stock_lever',X-.4,y-1.6,z0+6,X+.4,y-1.2,z0+8,'metal'),box('sling_loop',X+1.1,y-.6,z0+9,X+1.3,y+.4,z0+10,'metal')]
 return o
def stock_fixed(z0,z1,ytop,ybot,mat='rubber',w=1.3,thumbhole=False,pad=True):
 o=[]
 if thumbhole:
  o+=[box('stock_top',X-w,ytop-1.6,z0,X+w,ytop,z1,mat),box('stock_bottom',X-w,ybot,z0+2,X+w,ybot+1.6,z1,mat),box('stock_rear',X-w,ybot,z1-2.4,X+w,ytop,z1,mat),
      box('stock_wrist',X-w,ybot+.6,z0,X+w,ytop-1.2,z0+2.6,mat,([X,ytop,z0],'x',-22.5))]
 else:o.append(box('stock_body',X-w,ybot,z0,X+w,ytop,z1,mat))
 if pad:o.append(box('stock_pad',X-w-.15,ybot-.2,z1,X+w+.15,ytop+.2,z1+.7,'rubber'))
 o.append(box('stock_swivel',X-.3,ybot-.4,z1-2.5,X+.3,ybot,z1-1.7,'metal'))
 return o
def stock_wire(z0,z1,ytop,ybot):
 """folding/skeleton stock of thin bars"""
 return [box('stock_bar_top',X-1,ytop-.4,z0,X+1,ytop,z1,'metal'),box('stock_bar_low',X-1,ybot,z0+1,X+1,ybot+.4,z1,'metal'),
         box('stock_bar_low_l',X-1.1,ybot,z0,X-.7,ytop,z0+.6,'metal'),box('stock_butt',X-1.2,ybot-.3,z1,X+1.2,ytop+.3,z1+.6,'metal'),
         box('stock_butt_pad',X-1.1,ybot,z1+.6,X+1.1,ytop,z1+.9,'rubber'),box('stock_hinge',X-1.2,ytop-.8,z0-.4,X+1.2,ytop+.2,z0+.4,'metal')]
def mag_straight(name,z0,z1,ytop,ybot,w=1.2,mat='metal',ribs=True,windows=False):
 o=[box(name,X-w,ybot,z0,X+w,ytop,z1,mat),box(name+'_base',X-w-.2,ybot-.4,z0-.15,X+w+.2,ybot,z1+.15,'rubber')]
 if ribs:
  for s in (-1,1):o.append(box(name+'_rib',X+s*w-(0 if s>0 else .06),ybot+.6,z0+.35,X+s*w+(.06 if s>0 else 0),ytop-.6,z0+.6,'metal'))
 if windows:
  for k in range(3):o.append(box(name+'_window',X+w-.02,ybot+1+k*1.3,z0+.4,X+w+.04,ybot+1.6+k*1.3,z1-.4,'rubber'))
 return o
def mag_curved(name,ztop,ytop,depth,seg,angles,w=1.25,mat='metal'):
 o=[];zc,yc=ztop+depth/2,ytop
 for k,a in enumerate(angles):
  r=math.radians(a);dz,dy=math.sin(r),-math.cos(r);cz,cy=zc+dz*seg/2,yc+dy*seg/2
  o.append(box(name,X-w,cy-seg/2,cz-depth/2,X+w,cy+seg/2,cz+depth/2,mat,([X,cy,cz],'x',a) if a else None))
  o.append(box(name+'_rib',X-w-.06,cy-seg/2+.2,cz-depth/2+.4,X+w+.06,cy+seg/2-.2,cz-depth/2+.7,'metal',([X,cy,cz],'x',a) if a else None))
  zc,yc=zc+dz*seg,yc+dy*seg
 a=angles[-1];r=math.radians(a);dz,dy=math.sin(r),-math.cos(r);cz,cy=zc+dz*.25,yc+dy*.25
 o.append(box(name+'_base',X-w-.2,cy-.3,cz-depth/2-.2,X+w+.2,cy+.3,cz+depth/2+.2,'rubber',([X,cy,cz],'x',a) if a else None))
 return o
def bolt_handle(z,y,side=1,length=1.6,knob=.45):
 x=X+side*1.6
 return [box('bolt_handle',x,y-.2,z-.2,x+side*length,y+.2,z+.2,'metal'),box('bolt_knob',x+side*(length-.1)-knob,y-knob,z-knob,x+side*(length-.1)+knob,y+knob,z+knob,'rubber')]
def charging_handle(z,y):return [box('charging_handle',X-.5,y,z,X+.5,y+.45,z+1.6,'metal'),box('charging_latch',X-1.1,y,z+1.2,X+1.1,y+.45,z+1.7,'metal')]
def sling(z,y,side=-1):return [box('sling_swivel',X+side*1.5,y-.4,z-.4,X+side*1.75,y+.4,z+.4,'metal')]
def bipod_folded(z,y):
 o=[box('bipod_clamp',X-.9,y-.6,z-.6,X+.9,y,z+.6,'metal')]
 for s in (-1,1):o+=[box('bipod_leg',X+s*.55-.25,y-.75,z-.4,X+s*.55+.25,y-.25,z+8,'metal'),box('bipod_foot',X+s*.55-.35,y-.85,z+7.6,X+s*.55+.35,y-.15,z+8.4,'rubber')]
 return o
def carry_handle(z0,z1,ybase,top):
 return [box('handle_front',X-.6,ybase,z0,X+.6,top,z0+1,'paint'),box('handle_rear',X-.6,ybase,z1-1,X+.6,top,z1,'paint'),box('handle_bar',X-.6,top-.7,z0,X+.6,top,z1,'paint')]
# =====================================================================================================
# guns. Each returns dict(body,move,mag,rail=(top y,z0,z1),bore=y,muzzle=z,iron=y, mount=extra optic height)
# Shared anchors: pistol grip z 10..12.8 (y 1.5..6.5), trigger z ~8.2, support hand ~ (y 6.7, z -1.9), iron sight line 13.5.
# =====================================================================================================
def G(**k):k.setdefault('move',[]);k.setdefault('mag',[]);k.setdefault('iron',13.5);return k
def m4a1():
 b=[];mv=[];mg=[]
 b+=[box('upper_receiver',X-1.4,8.2,0,X+1.4,11.0,11.5),box('upper_chamfer',X-1.2,10.6,0,X+1.2,11.1,11.5),
     box('forward_assist',X+1.3,9.2,8.2,X+2.0,10.2,9.8,'metal'),box('forward_assist_cap',X+1.9,9.35,8.4,X+2.15,10.05,9.6,'metal'),
     box('lower_receiver',X-1.3,6.0,2.2,X+1.3,8.2,11.5),box('magwell',X-1.45,4.8,2.9,X+1.45,8.2,7.4),box('magwell_flare',X-1.6,4.6,2.7,X+1.6,5.0,7.6,'metal'),
     box('buffer_housing',X-1.0,7.2,11.5,X+1.0,10.6,12.4)]
 b+=ejection_port(3.2,7.0,8.8,10.4,X+1.4)+trigger_group(6.8,10.4,4.6,8.2)+selector(9.6,7.3,-1)
 b+=[box('bolt_catch',X-1.6,7.0,7.4,X-1.3,8.0,8.4,'metal'),box('mag_release',X+1.3,6.6,7.0,X+1.6,7.2,7.8,'metal')]
 b+=screws('pin',[(X-1.32,6.6,3.0),(X+1.32,6.6,3.0),(X-1.32,7.6,10.8),(X+1.32,7.6,10.8)])
 b+=grip('grip',10.0,6.4,1.4)
 b+=rail('top_rail',11.0,-8.6,11.3)
 # quad-rail handguard with heat vents
 b+=[box('handguard',X-1.55,7.1,-8.6,X+1.55,11.0,0,'metal'),box('handguard_nut',X-1.2,7.6,-.2,X+1.2,10.6,.3,'metal')]
 b+=side_rail('hg_rail_l',-1,9.1,-8.4,-.4)+side_rail('hg_rail_r',1,9.1,-8.4,-.4)
 b+=[box('bottom_rail',X-.9,6.8,-8.4,X+.9,7.1,-.4,'metal')]+[box('bottom_tooth',X-.9,6.55,-8.2+k*.8,X+.9,6.8,-7.8+k*.8,'metal') for k in range(10)]
 b+=vents('hg_vent',X-1.55,X+1.55,7.6,8.4,-8.0,-1.0,5)
 b+=cyl('barrel',9.6,-10.0,-8.6,.42)+cyl('gas_block',9.6,-9.4,-8.6,.7,'metal')+muzzle_device('birdcage',-12.2,9.6,.45)
 b+=[box('front_sight_rail',X-.8,11.85,-8.2,X+.8,12.3,-6.8,'metal'),box('front_post',X-.15,12.3,-7.7,X+.15,13.5,-7.4,'metal'),box('front_wing_l',X-.8,12.3,-7.9,X-.5,13.6,-7.1,'metal'),box('front_wing_r',X+.5,12.3,-7.9,X+.8,13.6,-7.1,'metal')]
 b+=rear_aperture(9.8,11.85,13.5)
 b+=stock_m4(11.4,8.8)+sling(-7.5,7.4,-1)
 mv+=charging_handle(11.4,10.55)+[box('bolt_carrier',X+1.36,8.95,3.4,X+1.4,10.25,6.8,'metal')]
 mg+=mag_curved('stanag',3.3,7.0,3.6,2.1,[0,0,-22.5,-22.5],1.2,'paint')
 return G(body=b,move=mv,mag=mg,rail=(11.85,-8.6,11.3),bore=9.6,muzzle=-12.2)
def scar():
 b=[];mv=[];mg=[]
 b+=[box('upper_receiver',X-1.6,8.0,-8.4,X+1.6,11.6,11.5),box('upper_bevel_l',X-1.75,8.6,-8.4,X-1.6,11.0,11.5),box('upper_bevel_r',X+1.6,8.6,-8.4,X+1.75,11.0,11.5),
     box('lower_receiver',X-1.35,5.9,2.0,X+1.35,8.0,11.3,'rubber'),box('magwell',X-1.5,4.6,2.8,X+1.5,8.0,7.6,'rubber'),
     box('rear_block',X-1.2,7.4,11.3,X+1.2,11.4,12.6,'rubber')]
 b+=ejection_port(3.0,6.8,8.8,10.6,X+1.6)+trigger_group(6.8,10.4,4.5,8.2)+selector(9.6,7.2,-1)+selector(9.6,7.2,1)
 b+=[box('mag_release_paddle',X-.6,4.6,7.6,X+.6,5.6,8.1,'metal')]+screws('pin',[(X-1.36,6.4,3.0),(X+1.36,6.4,3.0)])
 b+=grip('grip',10.0,6.3,1.4)
 b+=rail('top_rail',11.6,-8.4,11.3)+side_rail('rail_l',-1.08,9.0,-8.0,-2.0)+side_rail('rail_r',1.08,9.0,-8.0,-2.0)
 b+=[box('bottom_rail',X-.9,7.7,-8.0,X+.9,8.0,-2.0,'metal')]+[box('bottom_tooth',X-.9,7.45,-7.8+k*.8,X+.9,7.7,-7.4+k*.8,'metal') for k in range(7)]
 b+=vents('upper_vent',X-1.6,X+1.6,10.3,10.9,-7.6,-2.4,4)
 b+=cyl('barrel',9.7,-10.0,-8.4,.48)+cyl('gas_regulator',9.7,-9.0,-8.4,.75,'metal')+muzzle_device('birdcage',-12.2,9.7,.48)
 b+=[box('front_sight_base',X-.8,12.45,-8.2,X+.8,12.9,-7.0,'metal'),box('front_post',X-.15,12.9,-7.8,X+.15,13.5,-7.5,'metal'),box('front_ear_l',X-.8,12.9,-8.0,X-.5,13.6,-7.2,'metal'),box('front_ear_r',X+.5,12.9,-8.0,X+.8,13.6,-7.2,'metal')]
 b+=rear_aperture(9.8,12.45,13.5)
 # side-folding stock: hinge, telescoping body, cheek riser
 b+=[box('stock_hinge',X-1.3,7.6,12.6,X+1.3,11.2,13.6,'metal'),box('stock_tube_top',X-.9,9.6,13.6,X+.9,11.0,17.0,'rubber'),box('stock_tube_low',X-.9,7.4,13.6,X+.9,8.6,17.0,'rubber'),
     box('stock_body',X-1.2,6.2,16.8,X+1.2,11.2,21.6,'paint'),box('stock_cheek',X-1.0,11.2,17.2,X+1.0,11.9,21.2,'rubber'),box('stock_pad',X-1.3,6.0,21.6,X+1.3,11.4,22.3,'rubber'),
     box('stock_lock',X+1.2,8.8,18.0,X+1.45,9.6,19.4,'metal')]+sling(21.0,7.0,1)
 mv+=[box('charging_handle',X-1.6,10.2,-3.2,X-2.8,10.8,-2.6,'metal'),box('charging_knob',X-3.0,10.0,-3.4,X-2.6,11.0,-2.4,'rubber'),box('bolt_carrier',X+1.56,9.0,3.2,X+1.6,10.4,6.6,'metal')]
 mg+=mag_straight('scar_mag',3.4,7.4,7.6,1.4,1.25,'paint',True)
 return G(body=b,move=mv,mag=mg,rail=(12.45,-8.4,11.3),bore=9.7,muzzle=-12.2)
def famas():
 b=[];mv=[];mg=[]
 b+=[box('body',X-1.6,6.2,-9.0,X+1.6,10.8,20.4),box('body_top',X-1.3,10.8,-8.6,X+1.3,11.2,19.6),box('body_cheek',X-1.7,9.4,14.0,X+1.7,10.6,19.0,'rubber'),
     box('butt',X-1.5,5.2,19.0,X+1.5,10.8,21.0),box('butt_pad',X-1.6,5.0,21.0,X+1.6,11.0,21.6,'rubber'),
     box('front_cap',X-1.5,7.0,-9.6,X+1.5,10.4,-9.0,'rubber'),box('lower_front',X-1.4,5.2,-6.0,X+1.4,6.2,4.0,'rubber')]
 b+=vents('body_vent',X-1.6,X+1.6,7.6,9.6,-8.2,-1.0,6)
 # the long carry handle with sights inside it
 b+=[box('handle_front',X-.7,11.2,-7.0,X+.7,13.9,-5.4,'paint'),box('handle_rear',X-.7,11.2,9.4,X+.7,13.9,11.4,'paint'),box('handle_bar',X-.7,13.3,-7.0,X+.7,13.9,11.4,'paint'),
     box('handle_rib',X-.75,13.9,-6.6,X+.75,14.1,11.0,'metal')]
 b+=[box('rear_sight',X-.5,12.5,9.6,X+.5,13.5,10.3,'metal'),box('front_post',X-.15,12.4,-6.4,X+.15,13.5,-6.1,'metal')]
 b+=trigger_group(5.6,10.8,4.0,8.0)+grip('grip',10.0,6.2,1.4,mat='rubber')
 b+=[box('ejection_port_r',X+1.6,8.0,12.0,X+1.65,9.6,15.0,'rubber'),box('ejection_port_l',X-1.65,8.0,12.0,X-1.6,9.6,15.0,'rubber'),box('magwell',X-1.45,5.6,12.8,X+1.45,6.4,16.4,'rubber')]
 b+=cyl('barrel',9.0,-10.0,-9.6,.45)+muzzle_device('comp',-12.0,9.0,.45)
 b+=[box('bipod_l',X-1.8,7.4,-8.6,X-1.6,8.0,-1.0,'metal'),box('bipod_r',X+1.6,7.4,-8.6,X+1.8,8.0,-1.0,'metal')]+sling(19.5,6.0,1)
 mv+=[box('cocking_lever',X-.5,11.2,0.0,X+.5,12.6,1.2,'metal'),box('cocking_tab',X-.9,12.3,0.0,X+.9,12.7,1.0,'metal')]
 mg+=mag_straight('famas_mag',13.2,16.0,6.4,1.4,1.2,'metal',True)
 return G(body=b,move=mv,mag=mg,rail=(14.1,-6.6,11.0),bore=9.0,muzzle=-12.0)
def galil():
 b=[];mv=[];mg=[]
 b+=[box('receiver',X-1.45,7.4,0.4,X+1.45,10.9,11.6),box('receiver_rib',X-1.55,8.2,0.4,X+1.55,8.5,11.6,'metal'),
     box('dust_cover',X-1.35,10.9,1.0,X+1.35,11.4,11.4),box('trunnion',X-1.5,7.2,-.2,X+1.5,10.6,1.2,'metal'),box('magwell',X-1.45,6.0,2.8,X+1.45,7.4,7.0,'metal')]
 b+=rail('top_rail',11.4,1.4,11.2)
 b+=[box('selector_lever',X+1.45,8.8,2.8,X+1.75,10.4,9.0,'metal'),box('selector_tab',X+1.5,9.6,8.4,X+1.9,10.6,9.4,'metal'),box('mag_catch',X-.5,5.8,7.0,X+.5,6.6,7.8,'metal')]
 b+=trigger_group(6.8,10.4,4.8,8.2)+grip('grip',10.0,6.6,1.6)+screws('rivet',[(X-1.46,8.0,1.6),(X+1.46,8.0,1.6),(X-1.46,9.8,10.6),(X+1.46,9.8,10.6),(X+1.46,7.8,5.0)])
 b+=[box('handguard',X-1.6,7.1,-7.6,X+1.6,10.6,0,'rubber')]+vents('hg_vent',X-1.6,X+1.6,7.8,9.8,-7.2,-.6,5)
 b+=side_rail('hg_rail_l',-1,9.0,-7.0,-1.0)+side_rail('hg_rail_r',1,9.0,-7.0,-1.0)+rail('hg_top',10.6,-7.4,-.2)
 b+=cyl('gas_tube',11.0,-9.0,-7.6,.5,'metal')+cyl('barrel',9.4,-10.6,-7.6,.45)
 b+=[box('gas_block',X-.8,8.6,-9.8,X+.8,11.6,-8.6,'metal'),box('front_post',X-.15,11.6,-9.4,X+.15,13.5,-9.1,'metal'),box('front_ring_l',X-.8,11.6,-9.6,X-.55,13.6,-8.9,'metal'),box('front_ring_r',X+.55,11.6,-9.6,X+.8,13.6,-8.9,'metal')]
 b+=muzzle_device('brake',-12.6,9.4,.45)+rear_aperture(10.0,12.25,13.5)
 b+=[box('stock_hinge',X-1.2,7.6,11.6,X+1.2,10.6,12.6,'metal')]+stock_m4(12.4,8.8,'rubber')[1:]+cyl('stock_tube',9.7,12.6,19.4,.7,'metal')
 mv+=[box('charging_handle',X+1.45,9.6,8.6,X+2.9,10.1,9.1,'metal'),box('charging_knob',X+2.6,9.35,8.4,X+3.2,10.35,9.3,'metal'),box('bolt',X+1.42,9.0,3.6,X+1.46,10.2,7.4,'metal')]
 mg+=mag_curved('ace_mag',3.0,7.4,3.4,2.4,[0,-22.5,-22.5,-45],1.2,'paint')
 return G(body=b,move=mv,mag=mg,rail=(12.25,1.4,11.2),bore=9.4,muzzle=-12.6)
def aug():
 b=[];mv=[];mg=[]
 b+=[box('stock_shell',X-1.7,5.4,4.0,X+1.7,10.0,21.2),box('stock_top',X-1.4,10.0,4.0,X+1.4,10.6,19.6),box('butt_pad',X-1.8,5.2,21.2,X+1.8,10.2,21.8,'rubber'),
     box('stock_underside',X-1.5,4.8,13.0,X+1.5,5.4,20.4),box('receiver',X-1.4,8.0,-2.0,X+1.4,10.8,4.0,'metal'),box('receiver_top',X-1.2,10.8,-1.6,X+1.2,11.2,9.6,'metal')]
 b+=rail('top_rail',11.2,-1.4,9.4)
 # the big trigger guard that encloses the whole hand, and the folding fore-grip
 b+=[box('guard_front',X-.7,3.0,3.6,X+.7,5.4,4.2,'paint'),box('guard_bottom',X-.7,2.4,3.6,X+.7,3.0,12.0,'paint'),box('guard_rear',X-.7,2.4,11.6,X+.7,5.0,12.4,'paint')]
 b+=[box('trigger',X-.25,4.6,8.0,X+.25,6.0,8.45,'metal',([X,6.0,8.0],'x',22.5))]+grip('grip',10.0,5.6,2.4,1.25,'paint',-22.5,2.6,False)
 b+=[box('foregrip_hinge',X-.8,6.8,-1.6,X+.8,8.0,.2,'metal'),box('foregrip',X-.7,2.6,-1.4,X+.7,6.8,.4,'paint'),box('foregrip_cap',X-.8,2.3,-1.5,X+.8,2.6,.5,'rubber')]
 b+=[box('ejection_port',X+1.7,7.0,14.0,X+1.75,8.6,17.4,'rubber'),box('magwell',X-1.45,4.6,12.8,X+1.45,5.4,16.4,'rubber'),box('cross_bolt',X-1.85,6.8,9.6,X+1.85,7.4,10.2,'metal')]
 b+=cyl('barrel',8.8,-10.0,-2.0,.5)+cyl('barrel_lug',8.8,-3.0,-2.0,.85,'metal')+muzzle_device('birdcage',-12.2,8.8,.5)
 b+=[box('front_sight',X-.5,12.05,-1.0,X+.5,12.4,0,'metal'),box('front_post',X-.15,12.4,-.7,X+.15,13.5,-.4,'metal')]+rear_aperture(8.0,12.05,13.5)+sling(20.4,5.6,1)
 mv+=[box('cocking_handle',X-1.4,9.4,-1.0,X-2.6,10.0,-.4,'metal'),box('cocking_knob',X-2.8,9.2,-1.2,X-2.4,10.2,-.2,'rubber')]
 mg+=mag_straight('aug_mag',13.2,16.0,6.4,1.4,1.2,'rubber',False,True)
 return G(body=b,move=mv,mag=mg,rail=(12.05,-1.4,9.4),bore=8.8,muzzle=-12.2)
def m249():
 b=[];mv=[];mg=[]
 b+=[box('receiver',X-1.6,7.6,-2.0,X+1.6,11.4,12.0,'metal'),box('receiver_side_l',X-1.7,8.2,0,X-1.6,10.8,11.0,'metal'),box('receiver_side_r',X+1.6,8.2,0,X+1.7,10.8,11.0,'metal'),
     box('feed_cover',X-1.5,11.4,1.6,X+1.5,12.3,10.2,'metal'),box('feed_cover_latch',X-.6,11.4,10.2,X+.6,12.0,11.0,'metal'),box('lower',X-1.3,5.8,2.0,X+1.3,7.6,11.8,'rubber'),
     box('feed_tray',X-1.75,9.6,2.4,X-1.6,11.2,7.0,'metal')]
 b+=rail('top_rail',12.3,2.0,10.0)+trigger_group(6.8,10.4,4.4,8.2)+grip('grip',10.0,6.2,1.6)+screws('rivet',[(X+1.7,9.0,1.0),(X+1.7,9.0,10.4),(X-1.7,9.0,10.4)])
 b+=[box('carry_handle_base',X-.5,11.4,-1.8,X+.5,12.0,-.6,'metal'),box('carry_handle',X-.4,12.0,-3.6,X+.4,13.6,-.6,'rubber'),box('carry_grip',X-.6,13.0,-3.6,X+.6,13.8,-1.0,'rubber')]
 b+=[box('heat_shield',X-1.0,10.4,-11.6,X+1.0,11.0,-2.0,'metal')]+[box('shield_hole',X-.6,10.98,-11.0+k*1.2,X+.6,11.04,-10.4+k*1.2,'rubber') for k in range(7)]
 b+=cyl('barrel',9.6,-13.0,-2.0,.55)+cyl('gas_tube',8.2,-11.2,-2.0,.5,'metal')+muzzle_device('birdcage',-15.2,9.6,.55)
 b+=[box('handguard',X-1.5,6.6,-4.0,X+1.5,8.8,-.2,'rubber')]+vents('hg_vent',X-1.5,X+1.5,7.0,8.2,-3.8,-.6,3)
 b+=[box('front_sight_base',X-.4,10.0,-12.2,X+.4,12.6,-11.6,'metal'),box('front_post',X-.15,12.6,-12.1,X+.15,13.5,-11.8,'metal')]+rear_aperture(8.6,12.3,13.5)
 b+=bipod_folded(-11.4,8.0)
 b+=stock_fixed(12.0,21.6,10.8,6.0,'rubber',1.25)+[box('stock_spine',X-1.3,8.2,12.4,X+1.3,8.8,21.0,'metal')]
 mv+=[box('charging_handle',X+1.7,8.4,2.6,X+2.9,9.0,3.2,'metal'),box('charging_knob',X+2.7,8.2,2.4,X+3.2,9.2,3.4,'rubber')]
 mg+=[box('ammo_box',X-3.4,1.6,2.0,X-.6,6.6,7.2,'paint'),box('ammo_box_lid',X-3.6,6.6,1.8,X-.4,7.0,7.4,'metal'),box('ammo_box_latch',X-3.5,4.0,7.2,X-2.5,5.2,7.4,'metal')]
 mg+=[box('belt_link',X-.6,6.6,3.0+k*.7,X+.9,7.6,3.5+k*.7,'metal') for k in range(4)]+[box('round_tip',X-.2,7.6,3.1+k*.7,X+.4,8.0,3.4+k*.7,'wood') for k in range(4)]
 return G(body=b,move=mv,mag=mg,rail=(13.15,2.0,10.0),bore=9.6,muzzle=-15.2)
def barrett():
 b=[];mv=[];mg=[]
 b+=[box('upper',X-1.8,8.4,-6.0,X+1.8,12.0,13.0),box('upper_bevel',X-1.6,12.0,-5.6,X+1.6,12.3,12.6),box('lower',X-1.6,5.6,0,X+1.6,8.4,13.6),
     box('magwell',X-1.75,5.2,3.0,X+1.75,8.4,8.0,'metal'),box('recoil_buffer',X-1.4,8.0,13.0,X+1.4,11.6,14.6,'metal')]
 b+=[box('barrel_shroud_hole',X-1.82,9.4,-5.4+k*1.6,X+1.82,10.8,-4.6+k*1.6,'rubber') for k in range(4)]
 b+=rail('top_rail',12.3,-5.4,12.4)+trigger_group(7.0,10.6,4.8,8.4)+grip('grip',10.0,6.6,1.5)+selector(10.2,7.2,-1)
 b+=screws('pin',[(X-1.62,6.2,1.0),(X+1.62,6.2,1.0),(X-1.62,7.4,12.8),(X+1.62,7.4,12.8),(X-1.82,11.4,-5.0),(X+1.82,11.4,-5.0)])
 b+=[box('carry_handle',X-.5,12.9,2.6,X+.5,13.4,7.4,'rubber'),box('carry_post_f',X-.4,12.3,2.6,X+.4,12.9,3.2,'metal'),box('carry_post_r',X-.4,12.3,6.8,X+.4,12.9,7.4,'metal')]
 b+=cyl('barrel',10.0,-12.0,-6.0,.7)+cyl('barrel_flutes',10.0,-11.0,-7.0,.75,'rubber')+muzzle_device('brake',-15.2,10.0,.75)
 b+=bipod_folded(-6.6,8.4)
 b+=stock_fixed(14.6,22.8,11.6,6.4,'paint',1.4)+[box('cheek_pad',X-1.3,11.6,15.6,X+1.3,12.3,21.0,'rubber'),box('monopod',X-.4,4.4,20.0,X+.4,6.4,20.8,'metal'),box('monopod_foot',X-.7,4.1,19.8,X+.7,4.4,21.0,'rubber')]
 b+=[box('front_sight',X-.5,12.65,-5.2,X+.5,13.5,-4.4,'metal')]+rear_aperture(11.0,12.65,13.5)
 mv+=[box('charging_handle',X+1.8,9.8,8.0,X+3.4,10.4,8.6,'metal'),box('charging_knob',X+3.1,9.5,7.8,X+3.7,10.7,8.8,'rubber')]
 mg+=[box('barrett_mag',X-1.4,2.0,3.4,X+1.4,6.8,7.6,'metal'),box('barrett_mag_base',X-1.6,1.6,3.2,X+1.6,2.0,7.8,'rubber'),box('mag_rib',X+1.4,2.6,3.8,X+1.48,6.2,4.2,'metal'),box('mag_rib',X-1.48,2.6,3.8,X-1.4,6.2,4.2,'metal')]
 return G(body=b,move=mv,mag=mg,rail=(13.15,-5.4,12.4),bore=10.0,muzzle=-15.2)
def svd():
 b=[];mv=[];mg=[]
 b+=[box('receiver',X-1.4,7.8,0.6,X+1.4,10.9,12.0),box('receiver_rib',X-1.5,8.3,.6,X+1.5,8.55,12.0,'metal'),box('dust_cover',X-1.3,10.9,2.4,X+1.3,11.4,11.8),
     box('trunnion',X-1.45,7.6,0,X+1.45,10.6,1.2,'metal'),box('magwell',X-1.4,6.6,3.4,X+1.4,7.8,6.8,'metal'),box('pso_rail',X-1.75,8.4,2.6,X-1.4,10.6,10.2,'metal')]
 b+=[box('pso_rail_slot',X-1.8,8.8,3.0+k*1.8,X-1.75,10.2,3.8+k*1.8,'rubber') for k in range(4)]
 b+=[box('selector_lever',X+1.4,9.2,2.4,X+1.7,10.6,8.4,'metal'),box('selector_tab',X+1.45,9.8,7.8,X+1.85,10.8,8.8,'metal')]
 b+=trigger_group(7.0,10.6,4.8,8.4)+screws('rivet',[(X-1.42,8.0,1.8),(X+1.42,8.0,1.8),(X+1.42,10.0,11.2)])
 # wooden thumbhole stock with cheek rest, wooden vented handguard
 b+=stock_fixed(12.0,22.6,11.2,4.6,'wood',1.2,True)+[box('cheek_rest',X-1.25,11.2,15.0,X+1.25,12.0,20.0,'wood'),box('stock_grip_front',X-1.2,4.6,10.4,X+1.2,7.8,12.4,'wood',([X,7.8,12.0],'x',-22.5))]
 b+=[box('handguard',X-1.5,7.6,-7.0,X+1.5,10.8,0.6,'wood')]+vents('hg_vent',X-1.5,X+1.5,8.8,10.0,-6.6,-.2,6,'rubber')+[box('hg_ring',X-1.55,7.5,-7.4,X+1.55,10.9,-7.0,'metal')]
 b+=cyl('gas_tube',11.0,-9.0,-7.0,.45,'metal')+cyl('barrel',9.4,-14.0,-7.0,.42)+[box('gas_block',X-.7,8.6,-10.0,X+.7,11.6,-9.0,'metal')]
 b+=[box('front_hood_l',X-.6,11.6,-9.9,X-.4,13.6,-9.2,'metal'),box('front_hood_r',X+.4,11.6,-9.9,X+.6,13.6,-9.2,'metal'),box('front_post',X-.12,11.6,-9.7,X+.12,13.5,-9.4,'metal')]
 b+=[box('flash_hider',X-.6,9.4-.6,-15.9,X+.6,9.4+.6,-14.0,'metal')]+[box('hider_slot',X-.62,9.0,-15.5+k*.6,X+.62,9.8,-15.2+k*.6,'rubber') for k in range(3)]+cyl('bore',9.4,-15.93,-15.9,.3,'rubber')
 b+=[box('rear_sight_base',X-.8,11.4,1.0,X+.8,12.0,3.0,'metal'),box('rear_leaf',X-.7,12.0,1.4,X+.7,13.5,2.0,'metal',([X,12.0,1.4],'x',-22.5)),box('rear_notch',X-.15,13.2,1.0,X+.15,13.6,1.5,'rubber')]
 b+=sling(-6.0,7.4,-1)+sling(21.0,4.8,-1)
 mv+=[box('charging_handle',X+1.4,9.6,9.2,X+2.8,10.1,9.7,'metal'),box('charging_knob',X+2.5,9.4,9.0,X+3.0,10.3,9.9,'metal')]
 mg+=mag_curved('svd_mag',3.5,7.6,3.0,2.0,[0,-22.5],1.15,'metal')
 return G(body=b,move=mv,mag=mg,rail=(11.2,2.6,10.2),bore=9.4,muzzle=-15.9,side_mount=True)
def mp5():
 b=[];mv=[];mg=[]
 b+=cyl('receiver_tube',9.5,-5.0,11.6,1.55,'paint')+[box('receiver_flat',X-1.2,7.9,-4.6,X+1.2,9.5,11.6,'paint'),box('receiver_spine',X-.9,11.0,-4.6,X+.9,11.3,11.4,'paint')]
 b+=[box('trigger_housing',X-1.3,5.6,4.4,X+1.3,8.0,12.0,'rubber'),box('magwell',X-1.4,5.8,3.4,X+1.4,8.0,6.6,'paint'),box('mag_release',X-.5,5.6,6.6,X+.5,6.2,7.2,'metal')]
 b+=trigger_group(6.6,10.0,4.6,8.0)+grip('grip',10.0,6.4,1.4)+[box('selector',X-1.6,7.0,9.0,X-1.3,7.6,10.4,'metal'),box('selector_hub',X-1.7,6.8,9.2,X-1.3,7.8,10.0,'metal')]
 b+=[box('ejection_port',X+1.5,9.2,2.6,X+1.62,10.6,5.2,'rubber')]+screws('pin',[(X-1.32,6.4,5.2),(X+1.32,6.4,5.2),(X-1.32,6.4,11.2),(X+1.32,6.4,11.2)])
 b+=[box('handguard',X-1.45,6.8,-6.2,X+1.45,9.0,1.6,'rubber'),box('handguard_lip',X-1.5,6.6,-6.4,X+1.5,7.0,-5.6,'rubber')]+[box('hg_groove',X-1.5,7.2,-5.6+k*1.1,X+1.5,8.6,-5.2+k*1.1,'rubber') for k in range(6)]
 b+=cyl('cocking_tube',11.4,-6.2,-1.0,.5,'metal')
 b+=cyl('barrel',9.5,-7.6,-5.0,.45)+[box('barrel_lug',X-.15,9.5+.4,-7.4,X+.15,9.5+.7,-6.8,'metal'),box('barrel_lug',X-.7,9.35,-7.4,X-.4,9.65,-6.8,'metal'),box('barrel_lug',X+.4,9.35,-7.4,X+.7,9.65,-6.8,'metal')]+muzzle_device('crown',-8.0,9.5,.45)
 b+=[box('front_hood_base',X-.8,11.0,-5.0,X+.8,11.4,-3.8,'paint')]+ring('front_hood',12.6,-4.9,.9,.9)+[box('front_post',X-.12,11.4,-4.6,X+.12,12.5,-4.3,'metal')]
 b+=[box('rear_drum_base',X-.7,11.3,9.4,X+.7,11.8,11.0,'paint')]+cyl('rear_drum',12.6,9.8,10.8,.8,'metal')+[box('rear_peep',X-.2,13.2,9.7,X+.2,13.5,10.9,'rubber')]
 b+=rail('claw_rail',11.3,1.2,8.6,1.0)
 b+=[box('stock_rail_l',X-1.1,9.3,11.6,X-.8,10.2,21.0,'metal'),box('stock_rail_r',X+.8,9.3,11.6,X+1.1,10.2,21.0,'metal'),box('stock_butt',X-1.6,5.2,20.8,X+1.6,11.4,22.0,'rubber'),
     box('stock_butt_top',X-1.3,10.4,19.8,X+1.3,11.4,20.8,'rubber'),box('stock_butt_low',X-1.3,5.2,19.8,X+1.3,6.4,20.8,'rubber')]+sling(-5.0,8.0,-1)
 mv+=[box('cocking_handle',X-.5,11.0,-5.8,X-1.9,11.6,-5.2,'metal'),box('cocking_knob',X-2.2,10.9,-6.0,X-1.8,11.8,-5.0,'rubber')]
 mg+=mag_curved('mp5_mag',3.5,7.0,2.7,2.4,[0,-22.5,-22.5],1.15,'metal')
 return G(body=b,move=mv,mag=mg,rail=(12.15,1.2,8.6),bore=9.5,muzzle=-8.0)
def smg():   # KRISS Vector
 b=[];mv=[];mg=[]
 b+=[box('upper',X-1.4,8.2,-4.6,X+1.4,11.3,12.8),box('upper_nose',X-1.3,8.6,-5.2,X+1.3,11.0,-4.6,'rubber'),
     box('lower',X-1.6,3.0,-1.6,X+1.6,8.2,7.2),box('lower_chin',X-1.5,2.4,-1.2,X+1.5,3.0,5.0,'rubber'),box('lower_bevel',X-1.4,3.2,-2.2,X+1.4,7.8,-1.6),
     box('rear_block',X-1.3,7.0,7.2,X+1.3,8.2,12.6)]
 b+=rail('top_rail',11.3,-4.4,12.6)+[box('front_rail',X-.9,2.1,-1.0,X+.9,2.4,4.6,'metal')]+[box('front_tooth',X-.9,1.85,-.8+k*.8,X+.9,2.1,-.4+k*.8,'metal') for k in range(7)]
 b+=side_rail('nose_rail_l',-.88,9.6,-4.4,-1.0)+side_rail('nose_rail_r',.88,9.6,-4.4,-1.0)
 b+=trigger_group(7.0,10.2,4.6,8.2)+grip('grip',10.0,6.8,1.4,angle=-22.5)+selector(9.4,7.6,-1)+selector(9.4,7.6,1)
 b+=[box('magwell',X-1.45,1.6,1.0,X+1.45,3.0,4.6,'metal'),box('ejection_port',X+1.4,9.0,3.0,X+1.45,10.4,6.0,'rubber')]+screws('pin',[(X-1.62,7.0,0),(X+1.62,7.0,0),(X-1.62,4.0,5.6),(X+1.62,4.0,5.6)])
 b+=cyl('barrel',10.0,-6.2,-5.2,.45)+muzzle_device('comp',-7.4,10.0,.45)
 b+=[box('front_flip',X-.7,12.15,-4.0,X+.7,13.6,-3.4,'metal'),box('front_post',X-.12,12.6,-3.9,X+.12,13.5,-3.6,'metal')]+rear_aperture(10.8,12.15,13.5)
 b+=stock_wire(12.8,21.0,11.2,7.2)+[box('stock_cheek',X-1.0,10.8,15.0,X+1.0,11.6,20.0,'rubber')]
 mv+=[box('charging_handle',X-1.4,10.2,-1.0,X-2.6,10.8,-.4,'metal'),box('charging_knob',X-2.8,10.0,-1.2,X-2.4,11.0,-.2,'rubber')]
 mg+=mag_straight('vector_mag',1.3,4.3,1.8,-4.2,1.15,'metal',True)
 return G(body=b,move=mv,mag=mg,rail=(12.15,-4.4,12.6),bore=10.0,muzzle=-7.4)
def mp40():
 b=[];mv=[];mg=[]
 b+=cyl('receiver',9.6,-2.6,12.0,1.25,'metal')+[box('receiver_ribs',X-1.3,9.2+k*.0,-1.6+k*1.2,X+1.3,10.0,-1.2+k*1.2,'metal') for k in range(4)]
 b+=[box('bolt_slot',X-1.27,9.6,1.0,X-1.22,10.2,8.0,'rubber'),box('safety_notch',X-1.27,10.0,6.6,X-1.22,10.6,7.4,'rubber')]
 b+=[box('lower_frame',X-1.25,6.4,2.0,X+1.25,8.4,12.4,'rubber'),box('magwell',X-1.3,5.0,4.4,X+1.3,8.4,7.4,'metal'),box('magwell_lip',X-1.45,4.8,4.2,X+1.45,5.2,7.6,'metal'),
     box('mag_release',X-1.6,6.0,7.4,X-1.25,6.8,8.2,'metal')]
 b+=trigger_group(7.4,10.2,4.8,8.6)+grip('grip',10.0,6.6,1.5,mat='rubber')
 b+=cyl('barrel',9.6,-6.2,-2.6,.45)+cyl('barrel_nut',9.6,-3.2,-2.6,.75,'metal')+[box('barrel_rest',X-.9,7.6,-4.6,X+.9,8.6,-3.0,'rubber'),box('rest_arm',X-.4,8.6,-4.2,X+.4,9.2,-3.4,'metal')]+muzzle_device('crown',-6.6,9.6,.45)
 b+=[box('front_hood',X-.7,10.4,-6.0,X+.7,12.0,-5.0,'metal'),box('front_hood_top',X-.7,12.0,-6.0,X+.7,13.7,-5.6,'metal'),box('front_post',X-.12,10.6,-5.7,X+.12,13.5,-5.4,'metal')]
 b+=[box('rear_sight',X-.7,10.8,3.0,X+.7,13.5,3.6,'metal'),box('rear_notch',X-.15,13.1,2.95,X+.15,13.55,3.65,'rubber')]
 b+=stock_wire(12.4,20.8,9.8,6.6)+[box('stock_release',X+1.0,9.4,12.6,X+1.4,10.2,13.4,'metal')]+sling(-2.0,8.4,-1)
 mv+=[box('bolt_handle',X-1.25,9.8,6.8,X-2.6,10.3,7.3,'metal'),box('bolt_knob',X-2.9,9.6,6.6,X-2.4,10.5,7.5,'rubber')]
 mg+=mag_straight('mp40_mag',5.0,6.8,7.0,-3.2,1.05,'metal',True)
 return G(body=b,move=mv,mag=mg,rail=None,bore=9.6,muzzle=-6.6,mount=(10.85,0.6,9.0))
def p90():
 b=[];mv=[];mg=[]
 b+=[box('body',X-1.6,6.0,-2.6,X+1.6,11.2,15.4),box('body_front',X-1.5,6.6,-3.6,X+1.5,10.6,-2.6),box('body_rear',X-1.5,5.4,13.0,X+1.5,11.0,16.0,'rubber'),
     box('butt_pad',X-1.6,5.2,16.0,X+1.6,11.2,16.6,'rubber'),box('thumbhole_top',X-1.4,5.6,4.0,X+1.4,6.2,13.0),
     box('handstop',X-1.2,2.4,-2.2,X+1.2,6.0,-.8),box('hand_bottom',X-1.2,2.0,-2.2,X+1.2,2.6,12.6),box('grip_back',X-1.25,2.0,11.4,X+1.25,6.0,13.0,'rubber')]
 b+=[box('grip_texture',X-1.27,3.0+k*.8,11.6,X+1.27,3.4+k*.8,12.8,'metal') for k in range(4)]
 b+=[box('trigger',X-.25,3.2,4.2,X+.25,5.4,4.7,'metal',([X,5.4,4.2],'x',22.5)),box('ejection_chute',X-.8,5.8,13.2,X+.8,6.0,15.0,'rubber'),box('selector_disc',X-.6,1.85,9.0,X+.6,2.0,10.4,'metal')]
 b+=[box('sight_riser_f',X-1.5,11.2,-1.6,X+1.5,13.0,-.6,'paint'),box('sight_riser_r',X-1.5,11.2,9.6,X+1.5,13.0,10.6,'paint'),box('sight_bridge',X-1.0,13.0,-1.6,X+1.0,13.25,10.6,'metal')]
 b+=rail('top_rail',13.25,-1.4,10.4,.9,teeth=True)
 b+=cyl('barrel',9.2,-5.0,-3.6,.42)+muzzle_device('comp',-6.0,9.2,.42)
 b+=[box('front_post',X-.12,14.1,-1.1,X+.12,14.5,-.8,'metal')]
 mv+=[box('charging_handle',X-1.6,8.4,-1.8,X-2.5,9.0,-1.2,'metal'),box('charging_handle',X+1.6,8.4,-1.8,X+2.5,9.0,-1.2,'metal')]
 mg+=[box('p90_mag',X-1.05,11.3,-1.2,X+1.05,12.9,9.4,'rubber'),box('p90_mag_window',X-1.1,11.5,-.8,X+1.1,12.7,8.8,'wood'),box('p90_mag_front',X-1.05,11.3,-1.8,X+1.05,12.9,-1.2,'paint'),box('p90_mag_rear',X-1.05,11.3,9.4,X+1.05,12.9,9.8,'paint')]
 return G(body=b,move=mv,mag=mg,rail=(14.1,-1.4,10.4),bore=9.2,muzzle=-6.0,iron=14.5)
def ump45():
 b=[];mv=[];mg=[]
 b+=[box('receiver',X-1.5,6.8,-4.4,X+1.5,11.2,11.8),box('receiver_step',X-1.55,9.4,-4.4,X+1.55,9.7,11.8,'rubber'),box('nose',X-1.3,7.2,-5.2,X+1.3,10.8,-4.4,'rubber'),
     box('magwell',X-1.45,5.6,2.8,X+1.45,6.8,6.8),box('rear_cap',X-1.3,7.4,11.8,X+1.3,10.8,12.6,'rubber')]
 b+=rail('top_rail',11.2,-4.2,11.4)+side_rail('rail_l',-.9,8.4,-4.0,0)+side_rail('rail_r',.9,8.4,-4.0,0)+[box('bottom_rail',X-.9,6.5,-4.0,X+.9,6.8,0,'metal')]
 b+=vents('nose_vent',X-1.5,X+1.5,9.8,10.8,-3.8,-.4,3)+trigger_group(6.8,10.2,4.8,8.2)+grip('grip',10.0,6.8,1.5,mat='paint')+selector(9.4,7.6,-1)
 b+=[box('ejection_port',X+1.5,8.0,2.8,X+1.55,9.3,5.8,'rubber'),box('mag_release',X-.5,5.4,6.8,X+.5,6.0,7.4,'metal')]+screws('pin',[(X-1.52,7.4,1.2),(X+1.52,7.4,1.2),(X-1.52,7.4,10.8),(X+1.52,7.4,10.8)])
 b+=cyl('barrel',9.6,-7.0,-5.2,.45)+muzzle_device('crown',-7.4,9.6,.45)
 b+=[box('front_sight',X-.7,12.05,-4.0,X+.7,13.6,-3.2,'metal'),box('front_post',X-.12,12.4,-3.8,X+.12,13.5,-3.5,'metal')]+rear_aperture(10.0,12.05,13.5)
 b+=stock_fixed(12.6,21.4,11.0,5.6,'paint',1.15,True)+[box('stock_hinge',X-1.25,7.6,12.4,X+1.25,10.8,13.2,'metal')]+sling(-4.0,7.0,-1)
 mv+=[box('charging_handle',X-1.5,10.2,-3.4,X-2.8,10.8,-2.8,'metal'),box('charging_knob',X-3.0,10.0,-3.6,X-2.6,11.0,-2.6,'rubber')]
 mg+=mag_straight('ump_mag',3.2,6.4,6.8,-.6,1.2,'paint',True)
 return G(body=b,move=mv,mag=mg,rail=(12.05,-4.2,11.4),bore=9.6,muzzle=-7.4)
def uzi():
 b=[];mv=[];mg=[]
 b+=[box('receiver',X-1.4,7.6,-3.0,X+1.4,10.6,12.0,'metal'),box('top_cover',X-1.3,10.6,-2.6,X+1.3,11.0,8.6,'metal')]+[box('cover_rib',X-1.35,10.6,-2.2+k*1.4,X+1.35,11.15,-1.8+k*1.4,'metal') for k in range(8)]
 b+=[box('ejection_port',X+1.4,8.8,1.6,X+1.45,10.2,4.6,'rubber'),box('grip_frame',X-1.35,6.4,6.0,X+1.35,7.6,12.8,'rubber'),box('mag_release',X-1.7,2.6,11.0,X-1.35,3.4,11.8,'metal')]
 b+=[box('grip',X-1.35,1.0,9.8,X+1.35,6.4,13.0,'rubber'),box('grip_safety',X-.6,2.6,13.0,X+.6,6.0,13.4,'metal'),box('selector',X-1.7,6.8,9.6,X-1.35,7.4,10.8,'metal')]
 b+=[box('grip_texture',X-1.38,1.6+k*.9,10.2,X+1.38,2.0+k*.9,12.6,'metal') for k in range(5)]
 b+=[box('trigger_guard',X-.7,4.6,5.6,X+.7,5.2,9.8,'metal'),box('guard_front',X-.7,4.8,5.6,X+.7,6.4,6.2,'metal'),box('trigger',X-.25,5.0,8.0,X+.25,6.4,8.45,'metal',([X,6.4,8.0],'x',22.5))]
 b+=cyl('barrel',9.2,-4.6,-3.0,.45)+cyl('barrel_nut',9.2,-3.6,-3.0,1.05,'metal')+muzzle_device('crown',-5.0,9.2,.45)
 b+=[box('front_wing_l',X-.9,10.6,-2.4,X-.6,13.6,-1.4,'metal'),box('front_wing_r',X+.6,10.6,-2.4,X+.9,13.6,-1.4,'metal'),box('front_post',X-.12,11.0,-2.0,X+.12,13.5,-1.7,'metal')]
 b+=[box('rear_sight',X-.8,11.0,9.0,X+.8,13.5,10.0,'metal'),box('rear_peep',X-.2,13.1,8.95,X+.2,13.5,10.05,'rubber')]
 b+=stock_wire(12.0,20.0,10.2,6.8)+[box('stock_mount',X-1.3,7.2,11.8,X+1.3,10.4,12.6,'metal')]+sling(-2.0,7.6,-1)+sling(11.0,9.0,1)
 mv+=[box('cocking_knob',X-.5,11.15,2.4,X+.5,11.9,3.4,'rubber'),box('cocking_stem',X-.3,10.9,2.6,X+.3,11.2,3.2,'metal')]
 mg+=[box('uzi_mag',X-.95,-3.2,10.6,X+.95,6.4,12.0,'metal'),box('uzi_mag_base',X-1.2,-3.6,10.3,X+1.2,-3.2,12.3,'rubber'),box('uzi_mag_rib',X-1.0,-2.6,10.8,X+1.0,-2.2,11.8,'metal')]
 return G(body=b,move=mv,mag=mg,rail=None,bore=9.2,muzzle=-5.0,mount=(11.15,-1.0,8.6))
def mac10():
 b=[];mv=[];mg=[]
 b+=[box('upper',X-1.3,8.6,-1.0,X+1.3,10.8,12.6,'metal'),box('lower',X-1.25,6.4,-.6,X+1.25,8.6,12.6,'metal'),box('upper_seam',X-1.32,8.5,-1.0,X+1.32,8.7,12.6,'rubber')]
 b+=[box('cocking_slot',X-.15,10.78,1.0,X+.15,10.83,10.4,'rubber'),box('ejection_port',X+1.3,8.9,1.4,X+1.35,10.1,4.0,'rubber')]+screws('rivet',[(X-1.28,7.2,.4),(X+1.28,7.2,.4),(X-1.28,7.2,11.8),(X+1.28,7.2,11.8),(X+1.32,9.8,11.8)])
 b+=[box('grip',X-1.25,1.0,9.8,X+1.25,6.4,13.0,'rubber'),box('grip_cap',X-1.3,.6,9.6,X+1.3,1.0,13.2,'metal')]+[box('grip_texture',X-1.28,1.6+k*.9,10.2,X+1.28,2.0+k*.9,12.6,'metal') for k in range(5)]
 b+=[box('trigger_guard',X-.7,4.6,5.6,X+.7,5.2,9.8,'metal'),box('guard_front',X-.7,4.8,5.6,X+.7,6.4,6.2,'metal'),box('trigger',X-.25,5.0,8.0,X+.25,6.4,8.45,'metal',([X,6.4,8.0],'x',22.5))]
 b+=[box('strap_loop',X-.8,5.6,-.6,X+.8,6.4,.6,'metal'),box('strap_loop_low',X-.8,4.8,-.6,X+.8,5.2,1.4,'metal'),box('selector',X+1.25,7.0,4.0,X+1.6,7.6,5.2,'metal')]
 b+=cyl('barrel',9.6,-4.6,-1.0,.5)+[box('thread',X-.58,9.6-.58,-4.4+k*.6,X+.58,9.6+.58,-4.2+k*.6,'metal') for k in range(6)]+muzzle_device('crown',-5.0,9.6,.5)
 b+=[box('front_sight',X-.6,10.8,-.6,X+.6,13.5,.2,'metal'),box('front_slot',X-.15,12.8,-.62,X+.15,13.55,.22,'rubber'),box('rear_sight',X-.7,10.8,11.0,X+.7,13.5,12.0,'metal'),box('rear_peep',X-.2,13.1,10.95,X+.2,13.5,12.05,'rubber')]
 b+=stock_wire(12.6,19.6,10.2,7.0)
 mv+=[box('cocking_knob',X-.6,10.85,7.8,X+.6,11.8,9.0,'rubber'),box('cocking_notch',X-.62,11.2,8.2,X+.62,11.5,8.6,'metal')]
 mg+=[box('mac_mag',X-.95,-3.2,10.6,X+.95,6.4,12.0,'metal'),box('mac_mag_base',X-1.2,-3.6,10.3,X+1.2,-3.2,12.3,'rubber')]
 return G(body=b,move=mv,mag=mg,rail=None,bore=9.6,muzzle=-5.0,mount=(10.85,1.0,10.4))
def shotgun_core(b,fm=(X,)):pass
def m870():
 b=[];mv=[];mg=[]
 b+=[box('receiver',X-1.4,7.4,0,X+1.4,10.8,10.8),box('receiver_top',X-1.2,10.8,0,X+1.2,11.1,10.6),box('loading_port',X-.9,7.3,1.4,X+.9,7.45,6.4,'rubber'),
     box('ejection_port',X+1.4,8.6,1.6,X+1.45,10.0,5.6,'rubber'),box('slide_release',X-.6,6.8,6.6,X+.6,7.4,7.4,'metal'),box('safety_button',X-1.5,7.6,9.2,X+1.5,8.1,9.7,'metal')]
 b+=trigger_group(6.4,10.2,4.6,8.0)+grip('grip',10.0,6.8,1.4)+screws('pin',[(X-1.42,8.0,7.4),(X+1.42,8.0,7.4),(X-1.42,8.0,9.6),(X+1.42,8.0,9.6)])
 b+=cyl('barrel',10.0,-13.2,0,.55)+[box('vent_rib',X-.35,10.62,-13.0,X+.35,10.85,-.2,'metal')]+[box('rib_post',X-.2,10.5,-12.6+k*1.6,X+.2,10.66,-12.2+k*1.6,'metal') for k in range(8)]
 b+=muzzle_device('shotgun',-14.0,10.0,.55)+cyl('mag_tube',8.4,-11.6,0,.65)+[box('mag_cap',X-.65,7.75,-12.2,X+.65,9.05,-11.6,'metal'),box('barrel_clamp',X-.7,7.8,-11.4,X+.7,10.6,-10.8,'metal')]
 b+=[box('pump',X-1.4,7.0,-8.4,X+1.4,9.6,-1.6,'rubber')]+[box('pump_groove',X-1.45,7.4,-8.0+k*.8,X+1.45,9.2,-7.7+k*.8,'rubber') for k in range(8)]+[box('action_bar',X-1.1,7.6,-1.6,X-1.0,8.2,2.0,'metal')]
 b+=[box('side_saddle',X+1.4,7.6,.6,X+1.7,10.4,8.0,'rubber')]+[box('saddle_shell',X+1.7,7.8,.8+k*1.2,X+2.1,10.2,1.6+k*1.2,'red') for k in range(6)]+[box('shell_brass',X+1.75,7.8,.8+k*1.2,X+2.15,8.3,1.6+k*1.2,'wood') for k in range(6)]
 b+=stock_fixed(10.8,22.4,10.6,6.0,'paint',1.25)+[box('stock_wrist',X-1.2,6.6,10.8,X+1.2,10.6,13.2,'paint')]+sling(-10.5,7.6,-1)+sling(21.6,6.2,-1)
 b+=[box('rear_notch',X-.5,11.1,8.6,X+.5,11.6,9.4,'metal')]
 mg+=[box('shell',X-.45,6.2,2.0+k*1.3,X+.45,7.1,3.0+k*1.3,'red') for k in range(3)]+[box('shell_brass',X-.47,6.2,2.9+k*1.3,X+.47,7.1,3.15+k*1.3,'wood') for k in range(3)]
 return G(body=b,move=mv,mag=mg,rail=None,bore=10.0,muzzle=-14.0,iron=11.6,mount=(11.1,1.0,9.6))
def spas12():
 b=[];mv=[];mg=[]
 b+=[box('receiver',X-1.5,7.2,0,X+1.5,11.0,11.0,'metal'),box('receiver_top',X-1.3,11.0,0,X+1.3,11.3,10.8,'metal'),box('loading_port',X-1.0,7.1,1.2,X+1.0,7.25,6.6,'rubber'),
     box('ejection_port',X+1.5,8.6,1.6,X+1.55,10.2,5.8,'rubber'),box('cocking_button',X+1.5,7.6,8.6,X+1.8,8.2,9.4,'metal')]
 b+=trigger_group(6.4,10.2,4.6,8.0)+grip('grip',10.0,6.8,1.4)
 b+=cyl('barrel',10.0,-12.8,0,.55)+muzzle_device('shotgun',-13.6,10.0,.55)+cyl('mag_tube',8.4,-11.4,0,.65)
 b+=[box('heat_shield',X-1.1,9.6,-11.4,X+1.1,11.4,-1.0,'metal')]+[box('shield_hole',X-1.12,10.0,-10.8+k*1.3,X+1.12,10.9,-10.2+k*1.3,'rubber') for k in range(7)]+[box('shield_hole_top',X-.5,11.38,-10.8+k*1.3,X+.5,11.42,-10.2+k*1.3,'rubber') for k in range(7)]
 b+=[box('pump',X-1.6,6.6,-8.0,X+1.6,9.6,-2.0,'rubber'),box('pump_lock',X-.6,6.3,-3.0,X+.6,6.6,-2.2,'metal')]+[box('pump_groove',X-1.65,7.0,-7.6+k*.9,X+1.65,9.2,-7.2+k*.9,'rubber') for k in range(6)]
 b+=[box('front_sight',X-.15,11.4,-12.2,X+.15,13.5,-11.9,'metal'),box('front_base',X-.6,10.4,-12.4,X+.6,11.4,-11.6,'metal'),box('rear_sight',X-.6,11.3,9.6,X+.6,13.5,10.4,'metal'),box('rear_notch',X-.15,13.1,9.55,X+.15,13.55,10.45,'rubber')]
 b+=stock_wire(11.0,20.6,10.8,7.0)+[box('stock_hook',X-.3,7.6,10.6,X+.3,8.4,13.6,'metal',([X,8.0,13.6],'x',-22.5)),box('stock_hook_end',X-.3,6.4,13.0,X+.3,7.6,13.6,'metal')]+sling(-10.0,7.6,-1)
 mg+=[box('shell',X-.45,6.2,2.0+k*1.3,X+.45,7.1,3.0+k*1.3,'red') for k in range(3)]+[box('shell_brass',X-.47,6.2,2.9+k*1.3,X+.47,7.1,3.15+k*1.3,'wood') for k in range(3)]
 return G(body=b,move=mv,mag=mg,rail=None,bore=10.0,muzzle=-13.6,mount=(11.3,1.0,10.0))
def saiga12():
 b=[];mv=[];mg=[]
 b+=[box('receiver',X-1.45,7.4,0.4,X+1.45,10.9,11.6),box('receiver_rib',X-1.55,8.2,.4,X+1.55,8.45,11.6,'metal'),box('dust_cover',X-1.35,10.9,1.4,X+1.35,11.3,11.4),
     box('trunnion',X-1.5,7.2,-.2,X+1.5,10.6,1.2,'metal'),box('magwell',X-1.45,6.4,2.4,X+1.45,7.4,7.2,'metal')]
 b+=rail('top_rail',11.3,1.6,11.2)+[box('selector_lever',X+1.45,8.8,2.8,X+1.75,10.4,9.0,'metal'),box('selector_tab',X+1.5,9.6,8.4,X+1.9,10.6,9.4,'metal')]
 b+=trigger_group(6.8,10.4,4.8,8.2)+grip('grip',10.0,6.6,1.5)+screws('rivet',[(X-1.46,8.0,1.6),(X+1.46,8.0,1.6),(X+1.46,9.8,10.6)])
 b+=[box('handguard',X-1.5,7.2,-8.0,X+1.5,10.4,0,'rubber')]+vents('hg_vent',X-1.5,X+1.5,8.0,9.6,-7.6,-.6,5)+side_rail('rail_l',-.94,9.0,-7.4,-1.0)+side_rail('rail_r',.94,9.0,-7.4,-1.0)
 b+=cyl('gas_tube',11.0,-9.4,-8.0,.55,'metal')+cyl('barrel',9.4,-12.6,-8.0,.6)+[box('gas_block',X-.8,8.6,-10.2,X+.8,11.8,-9.0,'metal'),box('front_post',X-.15,11.8,-9.9,X+.15,13.5,-9.6,'metal')]
 b+=muzzle_device('shotgun',-13.4,9.4,.6)+rear_aperture(10.0,12.15,13.5)
 b+=[box('stock_hinge',X-1.2,7.6,11.6,X+1.2,10.6,12.6,'metal')]+stock_fixed(12.6,21.6,10.6,5.6,'rubber',1.2,True)
 mv+=[box('charging_handle',X+1.45,9.6,8.6,X+2.9,10.1,9.1,'metal'),box('charging_knob',X+2.6,9.35,8.4,X+3.2,10.35,9.3,'metal')]
 mg+=mag_straight('saiga_mag',2.8,7.0,7.4,1.0,1.35,'paint',True)+[box('saiga_mag_curve',X-1.35,1.0,2.4,X+1.35,2.6,6.6,'paint',([X,2.6,6.6],'x',-22.5))]
 return G(body=b,move=mv,mag=mg,rail=(12.15,1.6,11.2),bore=9.4,muzzle=-13.4)
# ---------------- pistols ----------------
def pistol(m,top,wide,kind,frame_mat):
 b=[];mv=[];mg=[];by=(9.4+top-.8)/2+.15;piv=([X,7.5,10],'x',-22.5)
 b+=[box('frame',X-wide+.15,7.0,m+.9,X+wide-.15,9.4,12.4,frame_mat),box('frame_tang',X-wide+.25,7.4,12.4,X+wide-.25,8.4,13.6,frame_mat)]
 b+=[box('grip',X-wide+.1,1.2,9.2,X+wide-.1,7.5,13.1,frame_mat,piv)]
 for s in (-1,1):
  b+=[box('grip_panel',X+s*(wide-.1)-(0 if s>0 else .12),1.8,9.5,X+s*(wide-.1)+(.12 if s>0 else 0),6.6,12.8,'wood' if kind=='beretta' else 'rubber',piv)]
  b+=[box('grip_texture',X+s*(wide+.02)-(0 if s>0 else .06),2.2+k*.9,9.9,X+s*(wide+.02)+(.06 if s>0 else 0),2.6+k*.9,12.4,'metal',piv) for k in range(5)]
 if kind=='glock':b+=[box('finger_groove',X-wide+.15,1.8+k*1.2,8.9,X+wide-.15,2.4+k*1.2,9.3,frame_mat,piv) for k in range(4)]
 b+=[box('trigger_guard_front',X-.7,4.3,4.8,X+.7,7.2,5.3 if kind!='glock' else 5.4,'metal' if frame_mat=='paint' else frame_mat),box('trigger_guard_base',X-.7,4.0,4.8,X+.7,4.5,9.4,'metal' if frame_mat=='paint' else frame_mat),
     box('trigger',X-.25,5.0,6.6,X+.25,7.4,7.05,'metal',([X,7.4,6.6],'x',22.5)),box('mag_release',X+wide-.15,6.6,9.0,X+wide+.2,7.3,9.9,'metal'),box('slide_stop',X-wide-.05,8.6,4.8,X-wide+.15,9.2,9.4,'metal'),
     box('takedown',X-wide-.05,8.0,5.2,X-wide+.15,8.6,6.2,'metal')]
 if kind in ('glock','usp'):
  b+=[box('accessory_rail',X-wide+.3,6.4,m+1.4,X+wide-.3,7.0,4.6,'metal')]+[box('rail_slot',X-wide+.25,6.5,m+1.8+k*.85,X+wide-.25,6.9,m+2.15+k*.85,'rubber') for k in range(4)]
 if kind in ('usp','beretta','de'):b+=[box('hammer',X-.55,9.2,12.5,X+.55,11.0,13.2,'metal',([X,9.6,12.8],'x',22.5)),box('hammer_spur',X-.6,10.6,12.8,X+.6,11.2,13.5,'metal',([X,9.6,12.8],'x',22.5))]
 b+=[box('beavertail',X-wide+.3,8.2,12.6,X+wide-.3,8.6,13.9,frame_mat)]
 # slide (moves back when firing)
 slide_front=m+(1.8 if kind=='beretta' else 0)
 mv+=[box('slide',X-wide,9.4,slide_front,X+wide,top-.8,12.6),box('slide_top',X-wide+.35,top-.8,slide_front,X+wide-.35,top,12.6)]
 if kind=='beretta':mv+=[box('slide_arms_l',X-wide,9.4,m,X-wide+.5,top-1.4,slide_front),box('slide_arms_r',X+wide-.5,9.4,m,X+wide,top-1.4,slide_front),box('slide_nose',X-wide,9.4,m,X+wide,10.2,m+.6),box('decocker',X-wide-.2,top-1.6,11.0,X-wide,top-1.0,12.2,'metal')]
 for z in [8.8,9.5,10.2,10.9,11.6]:
  for s in (-1,1):mv.append(box('serration',X+s*wide-(.0 if s>0 else .1),9.8,z,X+s*wide+(.1 if s>0 else 0),top-1.0,z+.25,'metal'))
 if kind in ('usp','glock','de'):
  for z in [m+.6,m+1.3]:
   for s in (-1,1):mv.append(box('front_serration',X+s*wide-(.0 if s>0 else .1),9.8,z,X+s*wide+(.1 if s>0 else 0),top-1.0,z+.25,'metal'))
 mv+=[box('ejection_port',X+wide-.05,top-2.0 if kind!='de' else 10.6,2.2,X+wide+.05,top-.6,5.4,'rubber'),box('extractor',X+wide,10.0,5.6,X+wide+.12,10.4,7.4,'metal')]
 mv+=[box('rear_sight',X-.8,top,11.2,X+.8,top+1.0,12.2,'metal'),box('rear_notch',X-.18,top+.55,11.15,X+.18,top+1.05,12.25,'rubber'),box('front_sight',X-.2,top,m+.5,X+.2,top+1.0,m+1.0,'metal')]
 if kind=='de':
  mv+=[box('top_rail',X-.6,top,m+1.4,X+.6,top+.3,10.8,'metal')]+[box('rail_tooth',X-.75,top+.3,m+1.6+k*1.0,X+.75,top+.5,m+1.95+k*1.0,'metal') for k in range(int((10.6-m-1.6)/1.0))]
  b+=[box('dust_cover',X-wide+.1,6.4,m+.8,X+wide-.1,7.6,5.8,frame_mat),box('barrel_hood',X-.9,top-1.0,m-.2,X+.9,top-.2,m+2.6,'metal')]+[box('brake_slot',X-1.0,top-.95,m+.1+k*.7,X+1.0,top-.25,m+.4+k*.7,'rubber') for k in range(3)]
 b+=cyl('barrel',by,m-.3,m+1.4,.62 if kind=='de' else .55)+cyl('muzzle_bore',by,m-.33,m-.3,.38,'rubber')
 mg+=[box('mag_body',X-wide+.35,.9,9.7,X+wide-.35,6.1,12.5,'metal',piv),box('mag_base',X-wide+.1,.3,9.4,X+wide-.1,.9,12.9,'rubber',piv)]
 return G(body=b,move=mv,mag=mg,rail=None,bore=by,muzzle=m-.3,iron=top+1.0,mount=(top,4.4,10.8),pistol=True)
def desert_eagle():return pistol(-5,12.3,1.85,'de','paint')
def usp():return pistol(-3,11.6,1.55,'usp','rubber')
def glock():return pistol(-3,11.3,1.5,'glock','rubber')
def beretta():return pistol(-3,11.5,1.4,'beretta','paint')
GUNS=[]
# ---------------- optics ----------------
ZOOMS={'desert_eagle':[1,2,3],'usp':[1,2,3],'m4a1':[1,3,6],'ak47':[1,2,4],'m870':[1,2,3],'mp5':[1,2,3],'smg':[1,2,4],'sniper':[5,12,24],'mp40':[1,2,3],'p90':[1,2,3],'python':[1,2,3],
 'glock':[1,2,3],'scar':[1,3,6],'ump45':[1,2,3],'spas12':[1,2,3],'beretta':[1,2,3],'uzi':[1,2,3],'famas':[1,3,6],'m249':[1,2,4],'barrett':[4,8,12],'mac10':[1,2,3],'galil':[1,3,6],'aug':[1,3,6],'saiga12':[1,2,3],'svd':[4,8,12]}
def optic_kind(zoom,pistol):
 if zoom>=4:return 'scope'
 return {1:'dot',2:'holo',3:'prism'}[zoom] if not pistol else {1:'rmr',2:'holo',3:'pscope'}[zoom]
def optic(info,zoom):
 """returns (elements, reticle elements, centre height cy, centre z) — seated on the gun's rail"""
 pistol=info.get('pistol',False)
 if info.get('rail'):ry,z0,z1=info['rail'];base=[]
 else:
  my,z0,z1=info['mount'];ry=my+.85
  # clamp-on mount for guns without a rail
  base=rail('mount_rail',my,z0,z1,1.0)+[box('mount_clamp_l',X-1.25,my-.6,z0+.4,X-1.0,my+.6,z0+1.4,'metal'),box('mount_clamp_r',X+1.0,my-.6,z0+.4,X+1.25,my+.6,z0+1.4,'metal')]
  if info.get('side_ak'):base+=[box('side_bracket',X+1.45,my-2.6,z0+.6,X+1.75,my+.3,z1-.6,'metal'),box('side_bridge',X+.9,my-.1,z0+.6,X+1.75,my+.3,z1-.6,'metal')]
 kind=optic_kind(zoom,pistol);e=list(base);ret=[];R='red'
 span=z1-z0;mid=(z0+z1)/2
 def dot(cy,z,r=.13):return [box('reticle_dot',X-r,cy-r,z,X+r,cy+r,z+.03,R)]
 if kind=='rmr':
  cz=min(z1-1.6,mid+1.0);cy=ry+1.0
  e+=[box('rmr_base',X-.9,ry,cz-1.2,X+.9,ry+.4,cz+1.2,'metal'),box('rmr_hood_l',X-.9,ry+.4,cz-.9,X-.6,ry+1.8,cz+.2,'metal'),box('rmr_hood_r',X+.6,ry+.4,cz-.9,X+.9,ry+1.8,cz+.2,'metal'),
      box('rmr_hood_top',X-.9,ry+1.8,cz-.9,X+.9,ry+2.05,cz-.4,'metal'),box('rmr_lens',X-.6,ry+.4,cz-.75,X+.6,ry+1.8,cz-.7,'rubber'),box('rmr_window',X-.6,ry+.4,cz+.6,X+.6,ry+.7,cz+1.2,'metal')]
  ret=dot(cy,cz-.6,.11)
 elif kind=='dot':
  cz=min(z1-2.0,mid+1.5);cy=ry+1.45
  e+=[box('dot_mount',X-.9,ry,cz-1.4,X+.9,ry+.45,cz+1.4,'metal'),box('dot_riser',X-.55,ry+.45,cz-.8,X+.55,cy-.7,cz+.8,'metal')]+cyl('dot_tube',cy,cz-1.5,cz+1.5,.95,'metal')
  e+=ring('dot_lens_front',cy,cz-1.6,1.0,.12,'rubber')+cyl('dot_turret_top',cy+1.05,cz-.4,cz+.4,.35,'metal')+[box('dot_turret_side',X+.95,cy-.35,cz-.4,X+1.35,cy+.35,cz+.4,'metal'),box('dot_kill_flash',X-.8,cy-.8,cz-2.2,X+.8,cy+.8,cz-1.6,'rubber')]
  ret=dot(cy,cz-1.3)
 elif kind=='holo':
  cz=min(z1-2.2,mid+1.0);cy=ry+1.25;w=.95 if not pistol else .8
  e+=[box('holo_base',X-w-.2,ry,cz-1.6,X+w+.2,ry+.5,cz+1.8,'metal'),box('holo_body',X-w-.2,ry+.5,cz+.2,X+w+.2,cy-.3,cz+1.8,'metal'),box('holo_buttons',X+w+.2,ry+.5,cz+.6,X+w+.45,ry+1.0,cz+1.6,'rubber'),
      box('holo_wing_l',X-w-.25,ry+.5,cz-1.4,X-w,cy+1.15,cz+.2,'metal'),box('holo_wing_r',X+w,ry+.5,cz-1.4,X+w+.25,cy+1.15,cz+.2,'metal'),box('holo_roof',X-w-.25,cy+1.15,cz-1.4,X+w+.25,cy+1.4,cz+.2,'metal'),
      box('holo_glass_f',X-w,cy-.75,cz-1.2,X+w,cy+1.1,cz-1.15,'rubber'),box('holo_glass_r',X-w,cy-.75,cz+.1,X+w,cy+1.1,cz+.15,'rubber')]
  ret=[box('holo_dot',X-.09,cy-.09,cz-1.0,X+.09,cy+.09,cz-.97,R)]
  for k in range(16):a=math.radians(k*22.5);x=X+.6*math.cos(a);y=cy+.6*math.sin(a);ret.append(box('holo_ring',x-.06,y-.06,cz-1.0,x+.06,y+.06,cz-.97,R))
 elif kind in ('prism','pscope'):
  L=4.2 if kind=='prism' else 5.0;cz=min(z1-L/2-.4,mid+1.0);cy=ry+1.55;r=.95 if kind=='prism' else .62
  e+=[box('prism_mount',X-1.0,ry,cz-1.6,X+1.0,ry+.5,cz+1.6,'metal'),box('prism_lever',X+1.0,ry,cz-.6,X+1.4,ry+.6,cz+.6,'metal')]
  if kind=='prism':
   e+=[box('prism_body',X-r,cy-r,cz-L/2+.8,X+r,cy+r,cz+L/2-.6,'metal'),box('prism_roof',X-r*.7,cy+r,cz-L/2+.8,X+r*.7,cy+r+.3,cz+L/2-.6,'metal'),box('prism_neck',X-.6,ry+.5,cz-1.0,X+.6,cy-r,cz+1.0,'metal')]
   e+=cyl('prism_objective',cy,cz-L/2,cz-L/2+.9,r*1.05,'metal')+cyl('prism_ocular',cy,cz+L/2-.7,cz+L/2,r*.85,'rubber')+[box('prism_fiber',X-.15,cy+r+.3,cz-.8,X+.15,cy+r+.5,cz+.8,'red')]
  else:
   e+=[box('ring_f',X-.75,ry+.5,cz-1.6,X+.75,cy,cz-1.1,'metal'),box('ring_r',X-.75,ry+.5,cz+1.1,X+.75,cy,cz+1.6,'metal')]+cyl('pscope_tube',cy,cz-L/2+.6,cz+L/2-.6,r,'metal')
   e+=cyl('pscope_bell_f',cy,cz-L/2,cz-L/2+.8,r*1.35,'metal')+cyl('pscope_bell_r',cy,cz+L/2-.8,cz+L/2,r*1.25,'rubber')+cyl('pscope_turret',cy+r+.2,cz-.3,cz+.3,.3,'metal')
  # ACOG-style chevron
  zr=cz-L/2+1.0
  ret=[box('chevron_l',X-.32,cy-.06,zr,X+.02,cy+.04,zr+.03,R,([X-.15,cy,zr],'z',45)),box('chevron_r',X-.02,cy-.06,zr,X+.32,cy+.04,zr+.03,R,([X+.15,cy,zr],'z',-45)),box('post',X-.03,cy-.9,zr,X+.03,cy-.2,zr+.03,R)]
 else:   # rifle scope: long tube, bells, turrets, rings — length grows with zoom
  L=min(span+5,7.0+zoom*.35);rb=.62+min(zoom,12)*.045;rt=.5
  cz=z1-L/2-.2 if zoom<8 else (z0+z1)/2+.6
  cz=max(cz,z0+L/2-3.0);cy=ry+.95+rb
  for zz in (cz-L/4,cz+L/5):
   e+=[box('scope_ring_base',X-.95,ry,zz-.5,X+.95,ry+.5,zz+.5,'metal'),box('scope_ring',X-rt-.25,ry+.5,zz-.35,X+rt+.25,cy+rt+.25,zz+.35,'metal'),box('ring_screw',X+rt+.25,cy-.2,zz-.2,X+rt+.45,cy+.2,zz+.2,'metal'),box('ring_screw',X-rt-.45,cy-.2,zz-.2,X-rt-.25,cy+.2,zz+.2,'metal')]
  e+=cyl('scope_tube',cy,cz-L/2+1.6,cz+L/2-1.4,rt,'metal')+cyl('scope_objective',cy,cz-L/2,cz-L/2+1.6,rb,'metal')+cyl('scope_ocular',cy,cz+L/2-1.4,cz+L/2,rb*.85,'metal')
  e+=cyl('scope_taper_f',cy,cz-L/2+1.6,cz-L/2+2.1,(rb+rt)/2,'metal')+cyl('scope_power_ring',cy,cz+L/2-1.9,cz+L/2-1.4,rt+.12,'rubber')+cyl('scope_eyecup',cy,cz+L/2-.3,cz+L/2,rb*.9,'rubber')
  e+=ring('scope_lens_f',cy,cz-L/2-.05,rb*.82,.05,'rubber')+ring('scope_lens_r',cy,cz+L/2,rb*.7,.04,'rubber')
  e+=cyl_y('scope_turret_elev',X,cz,cy+rt-.05,cy+rt+.65,.42,'metal')+[box('turret_cap',X-.36,cy+rt+.65,cz-.36,X+.36,cy+rt+.8,cz+.36,'red')]
  e+=[box('scope_turret_wind',X+rt-.05,cy-.4,cz-.4,X+rt+.65,cy+.4,cz+.4,'metal'),box('scope_parallax',X-rt-.6,cy-.35,cz-.35,X-rt+.05,cy+.35,cz+.35,'metal')]
  if zoom>=8:e+=cyl('scope_sunshade',cy,cz-L/2-1.0 if cz-L/2-1.0>-16 else -16,cz-L/2,rb*.98,'metal')
  zr=cz-L/2+1.0
  ret=[box('duplex_h',X-.9,cy-.02,zr,X+.9,cy+.02,zr+.03,R),box('duplex_v',X-.02,cy-.9,zr,X+.02,cy+.9,zr+.03,R)]
 # keep the optic's line of sight clear above the iron sights: lift it on a riser if needed
 need=info.get('iron',13.5)+.35-cy
 if need>.05:
  nb=len(base);top=e[nb:]
  for el in top+ret:
   el['from'][1]=round(el['from'][1]+need,4);el['to'][1]=round(el['to'][1]+need,4)
   if 'rotation' in el:el['rotation']['origin'][1]=round(el['rotation']['origin'][1]+need,4)
  lo=min(el['from'][2] for el in top);hi=max(el['to'][2] for el in top)
  zc=(lo+hi)/2;half=min(1.6,(hi-lo)/2)
  e=base+[box('optic_riser',X-.85,ry,zc-half,X+.85,ry+need,zc+half,'metal'),box('riser_clamp',X-1.05,ry-.2,zc-half+.3,X+1.05,ry+.3,zc+half-.3,'metal')]+top;cy+=need
 for el in e+ret:
  for v in el['from']+el['to']:assert -16<=v<=32,(el['name'],v)
 return e,ret,round(cy,3),round(cz,3)
# ---------------- suppressors (bore centre at y 10, muzzle at z 0; the renderer moves it to the gun) ----------------
def suppressor(style,v,pistol):
 L=(4.5 if pistol else 6.5)+(v-1)*1.4;r=(.75 if pistol else .95)+v*.12;y=10;e=[]
 if style==0:   # round can with machined rings
  e+=cyl('can',y,-L,0,r,'metal')+[c for z in (-L+.3,-L+1.0,-1.0,-.35) for c in ring('can_ring',y,z,r+.08,.22,'paint')]
 elif style==1:   # flat-sided octagon with side flats and heat vents
  e+=cyl('can',y,-L,0,r,'paint')+[box('can_flat',X-r-.08,y-.35,-L+.8,X+r+.08,y+.35,-.8,'metal')]+[box('can_vent',X+r+.02,y-.2,-L+1.2+k*.9,X+r+.1,y+.2,-L+1.6+k*.9,'rubber') for k in range(int((L-2)/.9))]
 else:   # segmented baffle stack
  n=int(L/1.1)
  for k in range(n):e+=cyl('baffle',y,-L+k*1.1,-L+k*1.1+.9,r,'metal')+cyl('baffle_gap',y,-L+k*1.1+.9,-L+k*1.1+1.1,r*.85,'rubber')
 e+=cyl('can_mount',y,-.6,0,r*.8,'rubber')+cyl('can_bore',y,-L-.03,-L,r*.4,'rubber')+ring('can_endcap',y,-L,r+.05,.25,'metal')
 return e
STYLE={'desert_eagle':0,'usp':0,'glock':1,'beretta':0,'python':2,'m4a1':2,'ak47':1,'m870':0,'mp5':0,'smg':1,'sniper':2,'mp40':0,'p90':1,'scar':2,'ump45':1,'spas12':0,'uzi':0,'famas':1,'m249':2,'barrett':2,'mac10':1,'galil':1,'aug':0,'saiga12':1,'svd':2}
EXTRA={   # guns not rebuilt here: just their sight mounts and muzzle data
 'beretta':dict(rail=(9.6, 7.36, 11.16), bore=8.0, muzzle=-1.72, iron=10.0, pistol=True),   # 0.75.0: OBJ from tools/forge/beretta.py
 'usp':dict(rail=(9.64, 7.84, 11.24), bore=8.08, muzzle=-1.56, iron=10.12, pistol=True),   # 0.75.0: OBJ from tools/forge/usp.py
 'glock':dict(rail=(9.6, 8.0, 11.4), bore=8.08, muzzle=-1.0, iron=10.04, pistol=True),   # 0.75.0: OBJ from tools/forge/glock.py
 'mp5':dict(rail=(10.59, 4.88, 11.27), bore=8.47, muzzle=-8.39, iron=10.72),   # 0.75.0: OBJ from tools/forge/mp5.py
 'python':dict(rail=(10.72, 5.44, 10.64), bore=9.44, muzzle=-7.4, iron=11.24, pistol=True),   # 0.75.0: OBJ from tools/forge/python.py
 'smg':dict(rail=(10.34, 4.79, 13.71), bore=8.89, muzzle=-0.17, iron=11.02),   # 0.75.0: OBJ from tools/forge/smg.py
 'barrett':dict(rail=(9.91, -5.32, 8.78), bore=8.18, muzzle=-29.78, iron=10.7),   # 0.75.0: OBJ from tools/forge/barrett.py
 'm249':dict(rail=(9.72, 4.08, 11.6), bore=7.57, muzzle=-17.56, iron=10.44),   # 0.75.0: OBJ from tools/forge/m249.py
 'spas12':dict(rail=(8.65, 5.96, 12.07), bore=7.76, muzzle=-11.92, iron=9.38),   # 0.75.0: OBJ from tools/forge/spas12.py
 'm870':dict(rail=(8.41, 5.02, 8.78), bore=7.24, muzzle=-17.09, iron=8.98),   # 0.75.0: OBJ from tools/forge/m870.py
 'svd':dict(rail=(8.89, 4.32, 12.3), bore=7.71, muzzle=-21.56, iron=8.96),   # 0.75.0: OBJ from tools/forge/svd.py
 'saiga12':dict(rail=(9.25, 5.26, 12.78), bore=7.67, muzzle=-10.75, iron=9.94),   # 0.75.0: OBJ from tools/forge/saiga12.py
 'galil':dict(rail=(9.11, 5.26, 12.78), bore=7.62, muzzle=-9.81, iron=9.8),   # 0.75.0: OBJ from tools/forge/galil.py
 'aug':dict(rail=(9.68, 7.96, 19.24), bore=8.42, muzzle=-6.17, iron=10.44),   # 0.75.0: OBJ from tools/forge/aug.py
 'famas':dict(rail=(10.77, 4.43, 18.06), bore=7.71, muzzle=-3.58, iron=11.15),   # 0.75.0: OBJ from tools/forge/famas.py
 'p90':dict(rail=(9.4, 5.37, 15.24), bore=6.54, muzzle=0.18, iron=9.77),   # 0.75.0: OBJ from tools/forge/p90.py
 'mac10':dict(rail=(8.94, 11.25, 16.42), bore=7.67, muzzle=7.46, iron=9.3),   # 0.75.0: OBJ from tools/forge/mac10.py
 'uzi':dict(rail=(9.5, 8.08, 14.66), bore=8.04, muzzle=2.88, iron=9.87),   # 0.75.0: OBJ from tools/forge/uzi.py
 'mp40':dict(rail=(9.27, 2.2, 8.31), bore=8.23, muzzle=-5.67, iron=9.97),   # 0.75.0: OBJ from tools/forge/mp40.py
 'ump45':dict(rail=(10.38, 0.88, 11.22), bore=8.33, muzzle=-5.16, iron=11.05),   # 0.75.0: OBJ from tools/forge/ump45.py
 'scar':dict(rail=(9.82, -4.14, 11.37), bore=8.14, muzzle=-13.1, iron=10.6),   # 0.75.0: OBJ from tools/forge/scar.py
 'ak47':dict(mount=(11.3,1.6,10.8),side_ak=True,bore=9.6,muzzle=-23.2,iron=11.1),
 'm4a1':dict(rail=(10.03,-6.3,12.6),bore=8.40,muzzle=-12.72,iron=10.90),   # 0.74.0: OBJ from tools/make_m4_claude.py
 'desert_eagle':dict(rail=(12.02,-5.9,0.7),bore=9.6,muzzle=-6.87,iron=12.4,pistol=True),   # 0.73.0: OBJ from tools/make_deagle_claude.py
 'sniper':dict(rail=(12.5,-6,6),bore=8.6,muzzle=-49.8,iron=13.5)}
FAMILY_ORDER=['desert_eagle','usp','m4a1','ak47','m870','mp5','smg','sniper','mp40','p90','python','glock','scar','ump45','spas12','beretta','uzi','famas','m249','barrett','mac10','galil','aug','saiga12','svd']
STOCK=('stock','buttpad','buffer_tube','cheek','butt')
def model(f,skin,es):return {'ambientocclusion':False,'textures':{'atlas':f'mareeletrica:block/armory21_{f}_{skin}','particle':f'mareeletrica:block/armory21_{f}_{skin}'},'elements':es}
def centered_item(es):
 es=copy.deepcopy(es);lo=[min(e['from'][a] for e in es) for a in range(3)];hi=[max(e['to'][a] for e in es) for a in range(3)];d=[8-(lo[a]+hi[a])/2 for a in range(3)]
 for e in es:
  for k in ('from','to'):e[k]=[round(e[k][a]+d[a],4) for a in range(3)]
  if 'rotation' in e:e['rotation']['origin']=[round(e['rotation']['origin'][a]+d[a],4) for a in range(3)]
 return es
def main():
 data={}
 for f in FAMILY_ORDER:
  info=globals()[f]() if f in GUNS else dict(EXTRA[f])
  if f in GUNS:
   view=[e for e in info['body'] if not any(t in e['name'] for t in STOCK)]
   for skin in range(13):
    for part,es in (('body',info['body']),('view',view),('moving',info['move']),('magazine',info['mag'])):dump(MB/f'armory21_{f}_{part}_{skin}.json',model(f,skin,es))
   for full in (False,True):
    obj=model(f,0,centered_item(info['mag']));obj['display']={'gui':{'rotation':[10,110,-12],'translation':[0,0,0],'scale':[1.2]*3},'ground':{'translation':[0,4,0],'scale':[.8]*3}}
    dump(A/'models/item'/(f+('_magazine' if full else '_empty_magazine')+'.json'),obj)
  sights=[]
  for v in (1,2,3):
   if f=='sniper':sights.append([15.7,16.25,16.8][v-1]);continue   # the snipers keep their own scope
   es,ret,cy,cz=optic(info,ZOOMS[f][v-1]);sights.append(cy)
   dump(MB/f'armory21_{f}_optic_{v}.json',model(f,0,es));dump(MB/f'armory21_{f}_reticle_{v}.json',model(f,0,ret))
   it=model(f,0,centered_item(es));it['display']={'gui':{'rotation':[18,115,-10],'translation':[0,0,0],'scale':[.8]*3},'ground':{'translation':[0,3,0],'scale':[.5]*3}};dump(A/'models/item'/f'{f}_optic_{v}.json',it)
  for v in (1,2,3):
   es=suppressor(STYLE[f],v,info.get('pistol',False));dump(MB/f'armory21_{f}_suppressor_{v}.json',model(f,0,es))
   it=model(f,0,centered_item(es));it['display']={'gui':{'rotation':[18,115,-10],'translation':[0,0,0],'scale':[.8]*3},'ground':{'translation':[0,3,0],'scale':[.5]*3}};dump(A/'models/item'/f'{f}_suppressor_{v}.json',it)
  data[f]=dict(muzzle=info['muzzle'],bore=info['bore'],iron=info['iron'],sights=sights,scope=[ZOOMS[f][v]>=4 for v in range(3)])
 # java tables for ArmoryRenderer
 fl=lambda xs:','.join(f'{x:.2f}F' for x in xs)
 print('MUZZLE  {'+','.join(f'{data[f]["muzzle"]:.2f}' for f in FAMILY_ORDER)+'}')
 print('BORE    {'+fl([data[f]['bore'] for f in FAMILY_ORDER])+'}')
 print('IRON    {'+fl([data[f]['iron'] for f in FAMILY_ORDER])+'}')
 print('SIGHT   {'+','.join('{'+fl(data[f]['sights'])+'}' for f in FAMILY_ORDER)+'}')
 json.dump(data,open(ROOT/'tools/gunsmith_072_data.json','w'),indent=1)
if __name__=='__main__':main()
