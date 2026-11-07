"""0.63.1: cuter Robo Catador. Bigger rounded head with an LED face screen (open eyes, blink, happy, sad when full),
rosy cheeks, softer cream/coral colours, rounder chassis; head is a separate part so it can tilt and look at you."""
import json,os,copy,random
from PIL import Image,ImageDraw
R=os.path.join(os.path.dirname(os.path.abspath(__file__)),'..','src','main','resources')
A=os.path.join(R,'assets','mareeletrica');M=os.path.join(A,'models');T=os.path.join(A,'textures')
def save(p,d):json.dump(d,open(os.path.join(M,p+'.json'),'w'))
FACES=['north','south','east','west','up','down']
def box(f,t,tex,faces=None,rot=None):
    e={"from":[round(x,3) for x in f],"to":[round(x,3) for x in t],"faces":{k:{"texture":'#'+(faces.get(k,tex) if faces else tex),"uv":[0,0,16,16]} for k in FACES}}
    if rot:e["rotation"]=rot
    return e
def sq(cx,cz,side,y0,y1,tex,rot45=False):
    h=side/2;e=box((cx-h,y0,cz-h),(cx+h,y1,cz+h),tex)
    if rot45:e["rotation"]={"origin":[cx,(y0+y1)/2,cz],"axis":"y","angle":45,"rescale":False}
    return e
rnd=random.Random(63)
def png(name,draw):
    im=Image.new('RGBA',(16,16),(0,0,0,255));d=ImageDraw.Draw(im);draw(im,d);im.save(os.path.join(T,'block',name+'.png'))
def soft(base,amt=2):
    def f(im,d):
        for x in range(16):
            for y in range(16):
                k=rnd.randint(-amt,amt);im.putpixel((x,y),tuple(max(0,min(255,c+k)) for c in base)+(255,))
    return f
def cream(im,d):
    soft((246,242,232))(im,d)
    for x in range(16):im.putpixel((x,15),(226,220,208,255))   # soft shading at the lower edge
def coral(im,d):
    soft((255,146,92))(im,d)
    for x in range(16):im.putpixel((x,0),(255,184,140,255))
def mint(im,d):soft((120,214,196))(im,d)
# ---- LED face screen (black glass, cyan pixels); faces: open, blink, happy, sad (red)
def screen(bg,fg,eyes,mouth,cheeks=True,hi=(255,255,255)):
    def f(im,d):
        d.rectangle([0,0,15,15],fill=bg+(255,))
        d.rectangle([0,0,15,0],fill=tuple(max(0,c-8) for c in bg)+(255,));d.rectangle([0,15,15,15],fill=tuple(max(0,c-8) for c in bg)+(255,))
        for x,y in eyes:im.putpixel((x,y),fg+(255,))
        for x,y in mouth:im.putpixel((x,y),fg+(255,))
        if cheeks:
            for x,y in [(1,10),(2,10),(13,10),(14,10),(1,11),(14,11)]:im.putpixel((x,y),(255,120,160,255))
        if eyes and len(eyes)>6:
            for x,y in [(4,4),(11,4)]:im.putpixel((x,y),hi+(255,))
    return f
def blob(x0):return [(x0+dx,y) for y in range(4,9) for dx in range(3) if not((y in(4,8))and dx!=1 and False)]
OPEN=[(x,y) for x0 in (3,10) for y in range(4,9) for x in range(x0,x0+3) if not((y==4 or y==8)and x in(x0,x0+2))]
BLINK=[(x,7) for x0 in (3,10) for x in range(x0,x0+3)]+[(2,6),(13,6)]
HAPPY=[(3,7),(4,6),(5,7),(4,5),(10,7),(11,6),(12,7),(11,5),(2,8),(6,8),(9,8),(13,8)]
SAD=[(4,6),(4,7),(11,6),(11,7),(3,5),(12,5)]
SMILE=[(6,11),(7,12),(8,12),(9,11)]
GRIN=[(5,10),(6,11),(7,12),(8,12),(9,11),(10,10),(6,10),(7,10),(8,10),(9,10),(7,11),(8,11)]
FROWN=[(6,12),(7,11),(8,11),(9,12)]
C=(110,236,246);BG=(14,32,40)
png('robot_face_open',screen(BG,C,OPEN,SMILE))
png('robot_face_blink',screen(BG,C,BLINK,SMILE))
png('robot_face_happy',screen(BG,C,HAPPY,GRIN))
png('robot_face_sad',screen((42,10,12),(255,96,84),SAD,FROWN,cheeks=False))
png('robot_cream',cream);png('robot_coral',coral);png('robot_mint',mint)
def heart(im,d):
    d.rectangle([0,0,15,15],fill=(255,120,150,255))
    for x,y in [(4,4),(5,4),(4,5)]:im.putpixel((x,y),(255,220,230,255))
png('robot_bulb',heart)

TX={'w':'mareeletrica:block/robot_cream','o':'mareeletrica:block/robot_coral','m':'mareeletrica:block/robot_mint','k':'mareeletrica:block/robot_dark',
    't':'mareeletrica:block/robot_tire','h':'mareeletrica:block/robot_hatch','particle':'mareeletrica:block/robot_cream'}
# ---- chassis (front = -z): a rounder, lower body with a coral belt and a mint belly light
BODY=[sq(8,8,10.5,3,8,'w'),sq(8,8,8.8,3,8,'w',True),sq(8,8,9.4,2.4,8.6,'w'),
      sq(8,8,10.9,4.6,5.8,'o'),sq(8,8,9.2,4.6,5.8,'o',True),
      sq(8,8,8,2,2.6,'k'),
      box((6.6,6.2,2.55),(9.4,7.4,2.9),'m'),    # little belly light
      box((5.4,3.6,13.3),(10.6,7.2,13.7),'h',{'north':'k','up':'k','down':'k','east':'k','west':'k','south':'h'}),
      box((0.4,4.0,8.8),(2.6,4.6,13.2),'o'),box((13.4,4.0,8.8),(15.6,4.6,13.2),'o'),
      box((7.1,2.0,3.2),(7.4,3.4,4.6),'k'),box((8.6,2.0,3.2),(8.9,3.4,4.6),'k'),
      box((7.2,8,7.2),(8.8,9.2,8.8),'k')]   # neck
save('block/robot_body',{"textures":TX,"elements":BODY})
# ---- head: big and rounded (two overlapping boxes bevel the corners), round coral ear caps, a short antenna
HEAD=[box((3.4,9.4,4.4),(12.6,14.6,11.6),'w'),box((4,8.9,5),(12,15.1,11),'w'),
      box((2.6,10.6,6.6),(3.4,13.4,9.4),'o'),box((12.6,10.6,6.6),(13.4,13.4,9.4),'o'),
      box((2.2,11.3,7.3),(2.6,12.7,8.7),'m'),box((13.4,11.3,7.3),(13.8,12.7,8.7),'m'),
      box((7.7,15.1,7.7),(8.3,17.2,8.3),'k')]
save('block/robot_head',{"textures":TX,"elements":HEAD})
for f in ['open','blink','happy','sad']:
    save('block/robot_face_'+f,{"textures":{'f':'mareeletrica:block/robot_face_'+f,'particle':'mareeletrica:block/robot_face_'+f},
          "elements":[box((4.4,9.9,4.15),(11.6,14.1,4.45),'f',{'north':'f','south':'f','east':'f','west':'f','up':'f','down':'f'})]})
save('block/robot_lamp',{"textures":{'l':'mareeletrica:block/robot_bulb','particle':'mareeletrica:block/robot_bulb'},
      "elements":[box((7.1,17.2,7.1),(8.9,19,8.9),'l'),box((7.4,16.9,7.4),(8.6,19.3,8.6),'l')]})
# wheels/caster/scoop in the new colours
WHEEL=[box((7,0,6),(9,4,10),'t',{'east':'k','west':'k'}),dict(box((7,0,6),(9,4,10),'t',{'east':'k','west':'k'}),rotation={"origin":[8,2,8],"axis":"x","angle":45,"rescale":False}),
       box((6.8,1.2,7.2),(9.2,2.8,8.8),'o'),box((6.7,1.7,7.7),(9.3,2.3,8.3),'m')]
save('block/robot_wheel',{"textures":TX,"elements":WHEEL})
CASTER=[box((7.4,0,3.0),(8.6,2.4,4.6),'t'),dict(box((7.4,0,3.0),(8.6,2.4,4.6),'t'),rotation={"origin":[8,1.2,3.8],"axis":"x","angle":45,"rescale":False})]
save('block/robot_caster',{"textures":TX,"elements":CASTER})
SCOOP=[box((4.6,3.0,1.4),(5.3,6.0,3.0),'k'),box((10.7,3.0,1.4),(11.4,6.0,3.0),'k'),
       box((4.2,1.0,-0.4),(11.8,1.5,2.3),'o'),box((4.2,1.0,1.9),(11.8,3.2,2.4),'o'),box((4.2,1.0,-0.4),(4.7,2.5,2.3),'o'),box((11.3,1.0,-0.4),(11.8,2.5,2.3),'o')]
save('block/robot_scoop',{"textures":TX,"elements":SCOOP})
# ---- item icon
def moved(els,dx=0,dy=0,dz=0):
    out=[]
    for e in copy.deepcopy(els):
        e['from']=[e['from'][0]+dx,e['from'][1]+dy,e['from'][2]+dz];e['to']=[e['to'][0]+dx,e['to'][1]+dy,e['to'][2]+dz]
        if 'rotation' in e:o=e['rotation']['origin'];e['rotation']['origin']=[o[0]+dx,o[1]+dy,o[2]+dz]
        out.append(e)
    return out
tx=dict(TX);tx.update({'f':'mareeletrica:block/robot_face_open','l':'mareeletrica:block/robot_bulb'})
EL=BODY+HEAD+moved(WHEEL,-6.5,0,3)+moved(WHEEL,6.5,0,3)+CASTER+SCOOP+[box((4.4,9.9,4.15),(11.6,14.1,4.45),'f'),box((7.1,17.2,7.1),(8.9,19,8.9),'l')]
it=json.load(open(os.path.join(M,'item','catador_robot.json')))
it['textures']=tx;it['elements']=EL;it['display']['gui']={"rotation":[20,160,0],"translation":[0,-1.6,0],"scale":[0.8,0.8,0.8]}
save('item/catador_robot',it)
print('cute robot ok')
