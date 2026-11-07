"""0.50.0: bigger, better looking helicopter, tank and radar table (+ textures), sport outfits (armor layers + icons)."""
import json,os,math,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
TB=os.path.join(A,'textures/block');random.seed(7)
def noise_tex(name,base,var=6,border=None,rivets=False,lines=None):
    im=Image.new('RGBA',(16,16))
    for y in range(16):
        for x in range(16):
            n=random.randint(-var,var);c=tuple(max(0,min(255,v+n)) for v in base)
            if border and (x in (0,15) or y in (0,15)):c=tuple(max(0,v-28) for v in base)
            if lines and y in lines:c=tuple(max(0,v-22) for v in base)
            if rivets and (x,y) in ((2,2),(13,2),(2,13),(13,13)):c=tuple(min(255,v+40) for v in base)
            im.putpixel((x,y),c+(255,))
    im.save(os.path.join(TB,name+'.png'))
noise_tex('veh_olive',(92,104,56),7,border=True,rivets=True)
noise_tex('veh_olive_plain',(92,104,56),6)
noise_tex('veh_olive_dark',(62,72,40),6,border=True)
noise_tex('veh_khaki',(150,130,88),7,border=True)
noise_tex('veh_metal',(70,74,80),5,border=True,rivets=True)
noise_tex('veh_dark',(40,42,46),4)
noise_tex('veh_rotor',(34,36,40),3,lines=[7,8])
im=Image.new('RGBA',(16,16))
for y in range(16):
    for x in range(16):
        b=150+int(60*(1-y/15));c=(70,120,b,255)
        if 2<=x+y-6<=4 or 2<=x+y-14<=3:c=(210,235,250,255)
        if x in (0,15) or y in (0,15):c=(40,44,50,255)
        im.putpixel((x,y),c)
im.save(os.path.join(TB,'veh_glass.png'))
im=Image.new('RGBA',(16,16))
for y in range(16):
    for x in range(16):im.putpixel((x,y),(230,190,30,255) if ((x+y)//4)%2==0 else (28,28,30,255))
im.save(os.path.join(TB,'veh_hazard.png'))
im=Image.new('RGBA',(16,16))
for y in range(16):
    for x in range(16):im.putpixel((x,y),(30,30,32,255) if y%4 else (58,58,62,255))
im.save(os.path.join(TB,'veh_tread.png'))
im=Image.new('RGBA',(16,16),(92,104,56,255));d=ImageDraw.Draw(im)
pts=[(8+6*math.cos(math.radians(-90+i*36))*(1 if i%2==0 else .42),8+6*math.sin(math.radians(-90+i*36))*(1 if i%2==0 else .42)) for i in range(10)]
d.ellipse([1,1,14,14],fill=(240,240,235,255));d.ellipse([2,2,13,13],fill=(92,104,56,255));d.polygon(pts,fill=(240,240,235,255));im.save(os.path.join(TB,'veh_star.png'))
# radar screen: dark green, range rings, cross; and table steel
S=32;im=Image.new('RGBA',(S,S),(14,52,24,255));d=ImageDraw.Draw(im)
for r in (14,10,6):d.ellipse([16-r,16-r,16+r,16+r],outline=(40,140,60,255))
d.line([16,1,16,31],fill=(30,110,48,255));d.line([1,16,31,16],fill=(30,110,48,255));im.save(os.path.join(TB,'radar_screen.png'))
noise_tex('radar_table',(96,104,114),5,border=True,rivets=True)
noise_tex('radar_wood',(96,70,44),8,lines=[4,11])
T={"olive":"mareeletrica:block/veh_olive","olivep":"mareeletrica:block/veh_olive_plain","olived":"mareeletrica:block/veh_olive_dark","khaki":"mareeletrica:block/veh_khaki",
 "metal":"mareeletrica:block/veh_metal","dark":"mareeletrica:block/veh_dark","rotor":"mareeletrica:block/veh_rotor","glass":"mareeletrica:block/veh_glass","hazard":"mareeletrica:block/veh_hazard",
 "tread":"mareeletrica:block/veh_tread","star":"mareeletrica:block/veh_star","red":"minecraft:block/red_concrete","white":"minecraft:block/white_concrete","black":"minecraft:block/black_concrete",
 "lamp_red":"minecraft:block/redstone_block","lamp_green":"minecraft:block/emerald_block","glow_yellow":"minecraft:block/glowstone","screen":"mareeletrica:block/radar_screen",
 "table":"mareeletrica:block/radar_table","light":"minecraft:block/light_gray_concrete","particle":"mareeletrica:block/veh_olive"}
def box(f,t,tex,rot=None,uv=None):
    faces={}
    for dname in ["down","up","north","south","west","east"]:
        tx=tex[dname] if isinstance(tex,dict) else tex
        if tx is None:continue
        faces[dname]={"texture":"#"+tx,"uv":uv or [0,0,16,16]}
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":faces}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def side(tex,sidetex):return {"down":tex,"up":tex,"north":tex,"south":tex,"west":sidetex,"east":sidetex}
def oct_y(cx,cz,r,y0,y1,tex):a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
def oct_z(cx,cy,r,z0,z1,tex):a=r*.924;return [box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex),box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex,([cx,cy,(z0+z1)/2],'z',45))]
def write(kind,name,el,display=None,extra=None,ao=False):
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    m={"ambientocclusion":ao,"textures":T,"elements":el}
    if display:m["display"]=display
    if extra:m.update(extra)
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
BIG={"gui":{"rotation":[25,215,0],"translation":[0,-1,0],"scale":[.24,.24,.24]},"ground":{"translation":[0,2,0],"scale":[.12,.12,.12]},"fixed":{"scale":[.24,.24,.24]},
 "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.12,.12,.12]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.16,.16,.16]}}
# ======================= helicopter (scale 3: 1 px = 3/16 block; nose -z; rotor pivot (8,16.6,8)) =======================
H=[]
H+=[box([2,2,-5],[14,12,12],side('olive','olive')),box([2.5,1.5,-4.5],[13.5,12.5,11.5],'olivep'),box([3,12,-3],[13,13,11],'olivep')]   # cabin with bevels
H+=[box([3,3,-9],[13,11,-5],side('glass','glass')),box([4,4,-11],[12,10,-9],'glass'),box([5,5,-12],[11,9,-11],'glass'),box([3,2,-9],[13,4,-5],'olive'),box([4,2.5,-11],[12,4,-9],'olive')]   # canopy
H+=[box([1.9,6,-4],[2,11,1],'glass'),box([14,6,-4],[14.1,11,1],'glass'),box([1.9,6,3],[2,11,8],'glass'),box([14,6,3],[14.1,11,8],'glass')]   # side windows
H+=[box([1.85,3,2],[1.95,11.5,2.4],'dark'),box([14.05,3,2],[14.15,11.5,2.4],'dark'),box([1.85,3,8.6],[1.95,11.5,9],'dark'),box([14.05,3,8.6],[14.15,11.5,9],'dark')]   # door seams
H+=[box([1.8,4,9.4],[1.9,8,11],'star'),box([14.1,4,9.4],[14.2,8,11],'star')]
H+=[box([4,13,0],[12,15.5,11],'olived'),box([5,15.5,2],[11,16,10],'metal'),box([7,16,7],[9,16.8,9],'metal')]   # engine + mast
H+=[box([3,13,8],[4,14.5,11],'dark'),box([12,13,8],[13,14.5,11],'dark'),box([3.2,13.2,11],[3.8,14.3,12],'black'),box([12.2,13.2,11],[12.8,14.3,12],'black')]   # exhausts
H+=[box([6,7,12],[10,11,20],'olive'),box([6.5,7.5,20],[9.5,10.5,27],'olive'),box([7,8,27],[9,10,32],'olive'),box([6.9,8.9,22],[9.1,9.6,25],'hazard')]   # tail boom
H+=[box([7.4,10,27],[8.6,17,31],'olive',([8,13.5,29],'x',-22.5)),box([7.35,14,28.4],[8.65,16,30.6],'red',([8,13.5,29],'x',-22.5)),box([3,8.3,26],[13,9.1,29],'olive'),box([2.6,8.1,26.5],[3.4,10.5,28.5],'olive'),box([12.6,8.1,26.5],[13.4,10.5,28.5],'olive')]
H+=[box([9.2,13,29.8],[10,15,31.2],'metal')]   # tail rotor hub
H+=[box([1,0,-8],[2.2,1,12],'metal'),box([13.8,0,-8],[15,1,12],'metal'),box([1,.2,-10.5],[2.2,1.2,-8],'metal',([1.6,.7,-8],'x',22.5)),box([13.8,.2,-10.5],[15,1.2,-8],'metal',([14.4,.7,-8],'x',22.5))]
for z in (-3,8):H+=[box([1.3,1,z],[2,3,z+1],'metal'),box([14,1,z],[14.7,3,z+1],'metal')]
H+=[box([-5,6,1],[2,7,5],'olive'),box([14,6,1],[21,7,5],'olive'),box([-5,3.5,-1],[-1,6,7],side('metal','metal')),box([17,3.5,-1],[21,6,7],side('metal','metal'))]   # stub wings + rocket pods
for x in (-4.4,-2.6):H+=[box([x,4.1,-1.2],[x+1.2,5.3,-1],'black')]
for x in (17.6,19.4):H+=[box([x,4.1,-1.2],[x+1.2,5.3,-1],'black')]
H+=[box([7,0.8,-9],[9,2.8,-7],'metal')]+oct_z(8,1.4,.6,-14,-9,'dark')   # chin gun
H+=[box([12.9,11,-2],[13.4,11.6,-1],'lamp_red'),box([2.6,11,-2],[3.1,11.6,-1],'lamp_green'),box([7.6,17.6,31],[8.4,18.2,31.6],'lamp_red')]
write('block','heli_body',H)
write('block','heli_blade',[box([7.2,-.3,9],[8.8,.3,31],'rotor'),box([7.2,-.32,30],[8.8,.32,32],'hazard'),box([6.8,-.6,6.8],[9.2,.6,9.2],'metal'),box([7.4,.6,7.4],[8.6,1.2,8.6],'dark')])
write('block','heli_tail_rotor',[box([7.8,2,7.4],[8.2,14,8.6],'rotor'),box([7.8,7.4,2],[8.2,8.6,14],'rotor'),box([7.6,7.2,7.2],[8.4,8.8,8.8],'metal')])
write('item','helicopter',H,BIG)
# ======================= tank (scale 3; nose -z; barrel pivot (8,10.5,1)) =======================
K=[]
K+=[box([-3,0,-10],[2,6,22],side('tread','tread')),box([14,0,-10],[19,6,22],side('tread','tread'))]   # tracks
for z in range(-8,21,4):K+=[box([-3.3,1,z],[-2.9,4,z+3],'metal'),box([18.9,1,z],[19.3,4,z+3],'metal')]   # road wheels
K+=[box([-3.4,4.5,-9],[2.2,6.6,21],'olived'),box([13.8,4.5,-9],[19.4,6.6,21],'olived')]   # side skirts
K+=[box([2,1.5,-9],[14,7,21],'olive'),box([-1,6,-8],[17,8.5,20],'olive'),box([0,5,-12],[16,8,-8],'olive',([8,8,-8],'x',-22.5))]   # hull + glacis
K+=[box([-1,7.9,-7],[3,8.6,-5],'hazard'),box([13,7.9,-7],[17,8.6,-5],'hazard'),box([1,6.5,-12.2],[3,7.5,-11.8],'glow_yellow'),box([13,6.5,-12.2],[15,7.5,-11.8],'glow_yellow')]
K+=[box([2,8.5,15],[14,9.5,20],'metal'),box([3,7,20],[6,8.5,22],'black'),box([10,7,20],[13,8.5,22],'black'),box([-1,8.5,16],[1.5,10.5,20],'khaki'),box([14.5,8.5,16],[17,10.5,20],'khaki')]
K+=[box([10,8.5,-6],[13,9.2,-3],'dark')]   # driver hatch
write('block','tank_hull',K)
U=[box([0,8.5,-1],[16,13.5,16],side('olive','olive')),box([1,13.5,0],[15,14.5,15],'olivep'),box([-.5,9,1],[16.5,13,14],'olivep')]
U+=[box([4.5,8.8,-1.5],[11.5,12.6,0],'olived'),box([-.6,9.5,7],[-.5,12.5,11],'star'),box([16.5,9.5,7],[16.6,12.5,11],'star')]
U+=oct_y(4.5,11,2.4,14.5,16,'olived')+[box([3.1,16,9.6],[5.9,16.3,12.4],'dark'),box([10,14.5,9],[11,16,13],'metal')]+oct_z(10.5,16.5,.5,4,10,'dark')   # cupola + MG
U+=[box([13.5,14.5,13.5],[14,27,14],'dark'),box([-.5,9,16],[16.5,12.5,17.5],'khaki')]   # antenna, bustle rack
write('block','tank_turret',U)
B=oct_z(8,10.5,1.1,-14,0,'olive')+oct_z(8,10.5,1.5,-16,-13.5,'dark')+oct_z(8,10.5,1.4,-8,-6,'olived')+[box([6.5,9,-1],[9.5,12,1],'olived')]
write('block','tank_barrel',B)
write('item','battle_tank',K+U+B,BIG)
# ======================= radar table (3x3) =======================
R=[]
for x,z in ((-15,-15),(29,-15),(-15,29),(29,29)):R+=[box([x,0,z],[x+2,13,z+2],'table')]
R+=[box([-15.5,13,-15.5],[31.5,15,31.5],side('table','table')),box([-15,15,-15],[31,15.5,31],'dark')]
R+=[box([8-17,15.4,8-17],[8+17,15.5,8+17],'dark',([8,15.45,8],'y',a) if a else None) for a in (0,22.5,45,-22.5)]
R+=[box([8-15.6,15.5,8-15.6],[8+15.6,15.7,8+15.6],'screen',([8,15.6,8],'y',a) if a else None) for a in (0,22.5,45,-22.5)]
R+=[box([-12,1,-12],[28,2,28],'table'),box([-15.5,13.5,-16],[31.5,14.5,-15.5],'glow_yellow')]
R+=[box([-14,15.5,24],[-4,17,30],'dark'),box([-13,17,25],[-5,17.2,29],'lamp_green',([-9,17,27],'x',-22.5))]   # side console
R+=[box([26,15,26],[30,32,30],'metal')]   # antenna mast (dish drawn on top by the renderer)
write('block','radar',R,extra={"ambientocclusion":True})
write('block','radar_alert',R+[box([26.5,32-1.5,26.5],[29.5,32,29.5],'lamp_red'),box([-15.5,13.5,-16],[31.5,14.5,-15.5],'lamp_red')])
write('block','radar_dish',[box([7,0,7],[9,3,9],'metal'),box([-2,3,7],[18,11,8.2],'light',([8,7,7.6],'x',-22.5)),box([0,4,8.2],[16,10,8.6],'metal',([8,7,7.6],'x',-22.5)),box([7.5,6.5,8],[8.5,7.5,13],'dark')])
for c,t in (("red","lamp_red"),("green","lamp_green"),("yellow","glow_yellow")):write('block','radar_blip_'+c,[box([7,7,7],[9,9,9],t)])
write('block','radar_sweep',[box([7.8,7.9,-7.6],[8.2,8.1,8],'lamp_green'),box([7.2,7.9,-7.6],[7.8,8.05,-2],'lamp_green'),box([8.2,7.9,-7.6],[8.8,8.05,-2],'lamp_green')])
json.dump({"parent":"mareeletrica:block/radar","display":BIG},open(os.path.join(A,'models/item/radar.json'),'w'))
# ======================= sport outfits =======================
OUT=[("futebol",(30,158,58),(255,255,255),"10"),("goleiro",(242,196,26),(32,32,32),"1"),("basquete",(90,42,146),(242,196,26),"23"),("volei",(30,90,200),(242,230,74),"7"),("tenis",(244,244,240),(46,158,90),"")]
AR=os.path.join(A,'textures/models/armor');IT=os.path.join(A,'textures/item')
def rect(px,x0,y0,x1,y1,c):
    for y in range(y0,y1):
        for x in range(x0,x1):px[x,y]=c
for oid,main,trim,num in OUT:
    m=main+(255,);t=trim+(255,);dark=tuple(max(0,v-40) for v in main)+(255,)
    im=Image.new('RGBA',(64,32),(0,0,0,0));px=im.load()
    # layer 1: body (16..40,16..32) arms (40..56,16..32) boots on legs (0..16,16..32 lower part)
    rect(px,16,16,40,32,m);rect(px,40,16,56,32,m)
    rect(px,20,16,28,20,m);rect(px,28,16,36,20,m)
    rect(px,16,20,40,21,t);rect(px,40,20,56,21,t);rect(px,40,23,56,24,t)   # collar/sleeve trims
    rect(px,22,20,23,32,dark);rect(px,25,20,26,32,dark)
    if oid=="goleiro":rect(px,40,29,56,32,(250,250,250,255))   # gloves
    if num:
        # number-plate patch on the back and a chest badge
        rect(px,33,22,39,29,t);rect(px,34,23,38,28,m);rect(px,35,23,37,28,t)
        rect(px,25,21,27,23,t)
    # boots
    shoe=(20,20,22,255) if oid in ("futebol","goleiro") else (245,245,245,255)
    rect(px,0,26,16,32,shoe);rect(px,0,26,16,27,t)
    im.save(os.path.join(AR,f'outfit_{oid}_layer_1.png'))
    im=Image.new('RGBA',(64,32),(0,0,0,0));px=im.load()
    rect(px,0,16,16,25,trim if oid=="tenis" else main);rect(px,16,26,40,32,m);rect(px,20,16,36,20,m)
    rect(px,0,16,16,25,(main if oid!="tenis" else (250,250,250))+(255,));rect(px,0,23,16,24,t)
    im.save(os.path.join(AR,f'outfit_{oid}_layer_2.png'))
    for piece in ("chestplate","leggings","boots"):
        ic=Image.new('RGBA',(16,16),(0,0,0,0));p2=ic.load()
        if piece=="chestplate":
            for y in range(3,15):
                for x in range(2,14):
                    if y<7 and (x<4 or x>11):p2[x,y]=m if y>3 else (0,0,0,0)
                    elif 4<=x<=11:p2[x,y]=m
            for x in range(6,10):p2[x,3]=(0,0,0,0)
            for x in range(4,12):p2[x,13]=t
            rect(p2,1,4,4,7,t);rect(p2,12,4,15,7,t)
            if num:rect(p2,7,6,9,10,t)
            if oid=="goleiro":rect(p2,0,7,2,9,(250,250,250,255));rect(p2,14,7,16,9,(250,250,250,255))
        elif piece=="leggings":
            c=(main if oid!="tenis" else (250,250,250))+(255,)
            rect(p2,3,4,13,9,c);rect(p2,3,9,7,13,c);rect(p2,9,9,13,13,c);rect(p2,3,4,13,5,t);rect(p2,3,12,7,13,t);rect(p2,9,12,13,13,t)
        else:
            rect(p2,2,9,7,13,shoe);rect(p2,9,9,14,13,shoe);rect(p2,2,12,7,13,t);rect(p2,9,12,14,13,t)
            if oid in ("futebol","goleiro"):
                for x in (3,5,10,12):p2[x,13]=(160,160,160,255)
        ic.save(os.path.join(IT,f'outfit_{oid}_{piece}.png'))
        json.dump({"parent":"minecraft:item/generated","textures":{"layer0":f"mareeletrica:item/outfit_{oid}_{piece}"}},open(os.path.join(A,f'models/item/outfit_{oid}_{piece}.json'),'w'))
print('vehicles3 ok')
