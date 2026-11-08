"""0.74.0: accessory models (grips, bipod, flashlight, lasers, drum) + lens textures + item models, recipes, lang."""
import json,os,math
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
MB=os.path.join(A,'models/block');TB=os.path.join(A,'textures/block')
def save(p,o):os.makedirs(os.path.dirname(p),exist_ok=True);json.dump(o,open(p,'w'),separators=(',',':'))
V=lambda n:'minecraft:block/'+n
T={'b':V('black_concrete'),'g':V('gray_concrete'),'m':V('iron_block'),'d':V('coal_block'),'t':'mareeletrica:block/acc_polymer'}
for n,c in (('white',(255,250,225)),('red',(255,40,30)),('green',(60,255,80))):
    im=Image.new('RGBA',(16,16))
    for x in range(16):
        for y in range(16):d=min(1,((x-7.5)**2+(y-7.5)**2)**.5/9);im.putpixel((x,y),tuple(int(v*(1-.35*d)+255*(1-d)*.35) for v in c)+(255,))
    im.save(os.path.join(TB,f'acc_lens_{n}.png'))
import random;random.seed(74)
im=Image.new('RGBA',(16,16))
for x in range(16):
    for y in range(16):v=38+random.randint(-4,4)+(5 if y%3==0 else 0);im.putpixel((x,y),(v,v,v+3,255))
im.save(os.path.join(TB,'acc_polymer.png'))
def fit(a,b):
    a,b=min(a,b),max(a,b)
    if b-a>=16:return 0,16
    a,b=a%16,a%16+(b-a)
    if b>16:return 16-(b-a),16
    return a,b
def uv(f,t,d):
    x=fit(f[0],t[0]);y=fit(16-t[1],16-f[1]);z=fit(f[2],t[2])
    return [round(v,3) for v in ({"up":(x[0],z[0],x[1],z[1]),"down":(x[0],z[0],x[1],z[1]),"north":(x[0],y[0],x[1],y[1]),"south":(x[0],y[0],x[1],y[1])}.get(d) or (z[0],y[0],z[1],y[1]))]
def el(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":uv(f,t,d)} for d in ["north","south","west","east","up","down"]}}
    if rot:e["rotation"]=rot
    return e
def oct_y(x,z,y0,y1,r,tex):   # post along y
    h=r*math.tan(math.pi/8);return [el([x-r,y0,z-h],[x+r,y1,z+h],tex),el([x-h,y0,z-r],[x+h,y1,z+r],tex),el([x-r,y0,z-h],[x+r,y1,z+h],tex,{"angle":45,"axis":"y","origin":[x,y0,z]})]
def oct_z(x,y,z0,z1,r,tex):   # tube along z
    h=r*math.tan(math.pi/8);return [el([x-r,y-h,z0],[x+r,y+h,z1],tex),el([x-h,y-r,z0],[x+h,y+r,z1],tex),el([x-r,y-h,z0],[x+r,y+h,z1],tex,{"angle":45,"axis":"z","origin":[x,y,z0]})]
def oct_x(y,z,x0,x1,r,tex):   # disc along x
    h=r*math.tan(math.pi/8);return [el([x0,y-h,z-r],[x1,y+h,z+r],tex),el([x0,y-r,z-h],[x1,y+r,z+h],tex),el([x0,y-h,z-r],[x1,y+h,z+r],tex,{"angle":45,"axis":"x","origin":[x0,y,z]})]
def part(name,els,tex=None):save(os.path.join(MB,name+'.json'),{"ambientocclusion":False,"textures":dict(tex or T,particle=V('black_concrete')),"elements":els})
# --- under the barrel (top of the model at y=0) ---
clamp=lambda w=.75,L=1.6:[el([8-w,-.45,-L/2],[8+w,0,L/2],'m'),el([8-w-.12,-.35,-L/2+.2],[8-w,0,L/2-.2],'b'),el([8+w,-.35,-L/2+.2],[8+w+.12,0,L/2-.2],'b'),el([8+w+.12,-.3,-.25],[8+w+.32,-.05,.25],'m')]
vg=clamp()+oct_y(8,0,-3.6,-.45,.55,'t')+oct_y(8,0,-3.9,-3.55,.62,'b')
for k in range(5):vg+=oct_y(8,0,-1.0-k*.55,-.8-k*.55,.6,'b')
part('acc_vertical_grip',vg)
ag=clamp(.75,2.6)+[el([7.4,-1.5,-1.2],[8.6,-.45,1.2],'t'),el([7.45,-2.4,-.4],[8.55,-1.4,1.25],'t',{"angle":-22.5,"axis":"x","origin":[8,-1.4,1.2]}),el([7.5,-1.6,-1.25],[8.5,-1.45,1.05],'b')]
part('acc_angled_grip',ag)
bp=[el([7.0,-.9,-.7],[9.0,0,.7],'m'),el([7.3,-1.3,-.5],[8.7,-.9,.5],'b')]
for s in (-1,1):bp+=[el([8+s*.6-.22,-1.25,-5.2],[8+s*.6+.22,-.85,.2],'m'),el([8+s*.6-.3,-1.35,-5.8],[8+s*.6+.3,-.75,-5.2],'b'),el([8+s*.6-.25,-1.3,-2.8],[8+s*.6+.25,-.8,-2.4],'g')]
part('acc_bipod',bp)
# --- side rail (mount at x=8, body towards +x, centred on y=0, z=0) ---
def light_body(x0=8.0):
    return [el([x0,-.5,-.7],[x0+.45,.5,.7],'m'),el([x0+.45,-.3,-.45],[x0+.75,.3,.45],'b')]+oct_z(x0+1.25,0,-1.7,1.6,.48,'b')+oct_z(x0+1.25,0,-2.15,-1.7,.62,'m')+oct_z(x0+1.25,0,1.6,1.95,.4,'g')+[el([x0+1.05,.45,.2],[x0+1.45,.65,.8],'g')]
part('acc_flashlight',light_body());part('acc_flashlight_lens',[el([9.25-.4,-.4,-2.18],[9.25+.4,.4,-2.14],'w')],{'w':'mareeletrica:block/acc_lens_white'})
las=lambda:[el([8.0,-.5,-.6],[8.45,.5,.6],'m'),el([8.45,-.45,-1.4],[9.45,.45,1.3],'b'),el([8.6,.45,-.8],[9.3,.6,.2],'g'),el([9.45,-.15,-.4],[9.55,.15,.4],'m')]+oct_z(8.95,.1,-1.6,-1.4,.22,'m')
part('acc_laser_red',las());part('acc_laser_green',las())
part('acc_laser_red_lens',[el([8.8,-.05,-1.63],[9.1,.25,-1.6],'r')],{'r':'mareeletrica:block/acc_lens_red'})
part('acc_laser_green_lens',[el([8.8,-.05,-1.63],[9.1,.25,-1.6],'r')],{'r':'mareeletrica:block/acc_lens_green'})
cb=[el([8.0,-.6,-.8],[8.45,.6,.8],'m'),el([8.45,-.75,-1.6],[9.95,.75,1.5],'b'),el([9.95,-.3,-.6],[10.1,.3,.6],'g')]+oct_z(9.25,.1,-2.05,-1.6,.55,'m')
part('acc_combo',cb);part('acc_combo_lens',[el([8.88,-.27,-2.08],[9.62,.47,-2.05],'w'),el([9.6,-.62,-1.63],[9.85,-.37,-1.6],'r')],{'w':'mareeletrica:block/acc_lens_white','r':'mareeletrica:block/acc_lens_red'})
# pistol: light / laser under the frame (mount on top at y=0, body below, along z)
def under_light(lens=False):
    if lens:return [el([7.6,-1.05,-2.1],[8.4,-.25,-2.06],'w')]
    return [el([7.3,-.4,-1.2],[8.7,0,1.2],'m')]+oct_z(8,-.65,-1.9,1.4,.45,'b')+oct_z(8,-.65,-2.1,-1.9,.55,'m')
part('acc_flashlight_under',under_light());part('acc_flashlight_under_lens',under_light(True),{'w':'mareeletrica:block/acc_lens_white'})
ul=lambda:[el([7.3,-.4,-1.0],[8.7,0,1.0],'m'),el([7.4,-1.3,-1.4],[8.6,-.4,1.2],'b')]
part('acc_laser_red_under',ul());part('acc_laser_green_under',ul());part('acc_combo_under',ul()+oct_z(8,-.85,-1.7,-1.4,.35,'m'))
part('acc_laser_red_under_lens',[el([7.85,-1.05,-1.43],[8.15,-.75,-1.4],'r')],{'r':'mareeletrica:block/acc_lens_red'})
part('acc_laser_green_under_lens',[el([7.85,-1.05,-1.43],[8.15,-.75,-1.4],'r')],{'r':'mareeletrica:block/acc_lens_green'})
part('acc_combo_under_lens',[el([7.65,-1.2,-1.73],[8.35,-.5,-1.7],'w'),el([8.3,-1.25,-1.43],[8.5,-1.05,-1.4],'r')],{'w':'mareeletrica:block/acc_lens_white','r':'mareeletrica:block/acc_lens_red'})
# --- drum magazine (top of the feed tower at y=0) ---
dr=[el([7.3,-1.4,-.9],[8.7,0,.9],'t'),el([7.2,-1.6,-1.0],[8.8,-1.3,1.0],'b')]
for x0,x1 in ((6.3,7.7),(8.3,9.7)):dr+=oct_x(-3.6,.2,x0,x1,2.3,'b')+oct_x(-3.6,.2,(x0+x1)/2-.1,(x0+x1)/2+.1,2.42,'t')
dr+=oct_x(-3.6,.2,7.7,8.3,1.6,'g')+oct_x(-3.6,.2,6.15,6.3,.7,'m')+oct_x(-3.6,.2,9.7,9.85,.7,'m')+[el([9.75,-4.2,1.8],[9.9,-3.0,2.4],'m')]
part('acc_drum',dr)
# --- items ---
for n in ['vertical_grip','angled_grip','bipod','flashlight','laser_red','laser_green','combo','extended_mag','drum_mag']:
    src={'extended_mag':'acc_drum','drum_mag':'acc_drum'}.get(n,'acc_'+n)
    m=json.load(open(os.path.join(MB,src+'.json')))
    es=m['elements']
    lo=[min(e['from'][a] for e in es) for a in range(3)];hi=[max(e['to'][a] for e in es) for a in range(3)];dd=[8-(lo[a]+hi[a])/2 for a in range(3)]
    for e in es:
        for k in ('from','to'):e[k]=[round(e[k][a]+dd[a],3) for a in range(3)]
        if 'rotation' in e:e['rotation']['origin']=[round(e['rotation']['origin'][a]+dd[a],3) for a in range(3)]
    if n=='extended_mag':
        es=[el([7,2,6],[9,14,10],'b'),el([6.8,1.2,5.8],[9.2,2,10.2],'t'),el([7.1,5,6.2],[8.9,5.6,9.8],'t'),el([7.1,9,6.2],[8.9,9.6,9.8],'t')]
    m['elements']=es;m['display']={'gui':{'rotation':[20,-45,0],'translation':[0,0,0],'scale':[1.6]*3},'ground':{'translation':[0,3,0],'scale':[.8]*3},'fixed':{'scale':[1.4]*3},'firstperson_righthand':{'rotation':[0,-45,0],'scale':[.8]*3},'thirdperson_righthand':{'scale':[.6]*3}}
    save(os.path.join(A,'models/item',f'acc_{n}.json'),m)
# --- recipes ---
I=lambda i:{"item":"minecraft:"+i}
R={'vertical_grip':(["P","P","I"],{"P":I('black_concrete'),"I":I('iron_nugget')}),'angled_grip':(["PP","I "],{"P":I('black_concrete'),"I":I('iron_nugget')}),
   'bipod':(["III","S S"],{"I":I('iron_ingot'),"S":I('stick')}),'flashlight':(["IGT"],{"I":I('iron_ingot'),"G":I('glass_pane'),"T":I('torch')}),
   'laser_red':(["IRG"],{"I":I('iron_ingot'),"R":I('redstone'),"G":I('glass_pane')}),'laser_green':(["ILG"],{"I":I('iron_ingot'),"L":I('lime_dye'),"G":I('glass_pane')}),
   'combo':(["FL"],{"F":{"item":"mareeletrica:acc_flashlight"},"L":{"item":"mareeletrica:acc_laser_red"}}),
   'extended_mag':(["I","I","I"],{"I":I('iron_ingot')}),'drum_mag':(["III","I I","III"],{"I":I('iron_ingot')})}
for n,(pat,key) in R.items():save(os.path.join(D,'recipe',f'acc_{n}.json'),{"type":"minecraft:crafting_shaped","category":"equipment","pattern":pat,"key":key,"result":{"id":f"mareeletrica:acc_{n}","count":1}})
names={'vertical_grip':('Empunhadura Vertical','Vertical Grip'),'angled_grip':('Empunhadura Angulada','Angled Grip'),'bipod':('Bipé','Bipod'),'flashlight':('Lanterna Tática','Tactical Flashlight'),
 'laser_red':('Laser Vermelho','Red Laser'),'laser_green':('Laser Verde','Green Laser'),'combo':('Lanterna + Laser','Light + Laser'),'extended_mag':('Pente Estendido','Extended Magazine'),'drum_mag':('Carregador de Tambor','Drum Magazine')}
for i,l in enumerate(('pt_br','en_us')):
    pp=os.path.join(A,'lang',l+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update({f"item.mareeletrica.acc_{n}":v[i] for n,v in names.items()});json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('acc ok')
