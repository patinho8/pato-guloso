"""0.65.2: 3D medical items, coloured tennis balls, arcade / popcorn machine / fireworks show blocks (models, textures, recipes, loot, lang)."""
import json,os,copy,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica');T=os.path.join(A,'textures')
R=random.Random(652)
def box(f,t,tex,rot=None,faces=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+((faces or {}).get(d,tex)),"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def oct_y(cx,cz,r,y0,y1,tex):a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
def oct_x(cy,cz,r,x0,x1,tex):a=r*.924;return [box([x0,cy-a,cz-a],[x1,cy+a,cz+a],tex),box([x0,cy-a,cz-a],[x1,cy+a,cz+a],tex,([(x0+x1)/2,cy,cz],'x',45))]
def save(kind,name,m):
    for e in m.get("elements",[]):
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
def png(path,draw,size=16):
    im=Image.new('RGBA',(size,size),(0,0,0,0));d=ImageDraw.Draw(im);draw(im,d);os.makedirs(os.path.dirname(os.path.join(T,path)),exist_ok=True);im.save(os.path.join(T,path+'.png'))
V=lambda n:"minecraft:block/"+n
# ---------------- textures ----------------
def screen(im,d):
    d.rectangle([0,0,15,15],fill=(6,10,18,255))
    for x,y in [(3,8),(4,8),(5,8),(6,8),(6,7),(6,6),(7,6),(8,6)]:im.putpixel((x,y),(90,255,80,255))
    im.putpixel((8,6),(170,255,140,255));im.putpixel((11,4),(255,60,90,255))
    for x in range(16):im.putpixel((x,0),(0,200,230,255));im.putpixel((x,15),(0,200,230,255))
    for x in range(1,6):im.putpixel((x,2),(255,220,60,255))
def marquee(im,d):
    d.rectangle([0,0,15,15],fill=(140,30,170,255))
    for x in range(1,15,3):d.rectangle([x,5,x+1,10],fill=(255,230,80,255))
    d.line([(0,1),(15,1)],fill=(255,120,220,255));d.line([(0,14),(15,14)],fill=(255,120,220,255))
def kernels(im,d):
    for x in range(16):
        for y in range(16):c=R.choice([(255,250,235),(250,240,200),(255,230,150),(245,245,240)]);im.putpixel((x,y),c+(255,))
def popcorn_sign(im,d):
    d.rectangle([0,0,15,15],fill=(230,40,40,255))
    for x in range(1,15,2):d.rectangle([x,3,x,12],fill=(255,240,120,255))
    d.rectangle([0,0,15,1],fill=(255,255,255,255));d.rectangle([0,14,15,15],fill=(255,255,255,255))
def popcorn_item(im,d):
    d.polygon([(3,6),(12,6),(11,15),(4,15)],fill=(240,240,240,255))
    for x in (4,6,8,10):d.polygon([(x,6),(x+1,6),(x+1,15),(x,15)],fill=(220,30,40,255))
    for x,y,r in [(5,4,2),(8,3,2),(11,4,2),(7,5,2),(10,5,1),(4,5,1),(9,2,1)]:d.ellipse([x-r,y-r,x+r,y+r],fill=(255,248,225,255),outline=(230,200,130,255))
def paper(im,d):
    d.rectangle([0,0,15,15],fill=(235,225,200,255))
    for y in range(0,16,4):d.line([(0,y),(15,y)],fill=(210,195,160,255))
png('block/arcade_screen',screen);png('block/arcade_marquee',marquee);png('block/popcorn_kernels',kernels);png('block/popcorn_sign',popcorn_sign);png('item/popcorn',popcorn_item);png('block/firework_paper',paper)
# ---------------- 3D medical items ----------------
MT={"white":V("white_concrete"),"wool":V("white_wool"),"light":V("light_gray_concrete"),"gray":V("gray_concrete"),"red":V("red_concrete"),"glass":V("light_blue_stained_glass"),
    "steel":"mareeletrica:block/steel","dark":"mareeletrica:block/dark","black":V("black_concrete"),"orange":V("orange_concrete"),"particle":V("white_wool")}
DSP={"gui":{"rotation":[25,-35,0],"scale":[.95,.95,.95]},"ground":{"translation":[0,3,0],"scale":[.6,.6,.6]},"fixed":{"scale":[.9,.9,.9]},
     "thirdperson_righthand":{"rotation":[0,0,0],"translation":[0,2.5,1.5],"scale":[.55,.55,.55]},"firstperson_righthand":{"rotation":[0,-25,0],"translation":[1,3,1],"scale":[.7,.7,.7]},
     "thirdperson_lefthand":{"rotation":[0,0,0],"translation":[0,2.5,1.5],"scale":[.55,.55,.55]},"firstperson_lefthand":{"rotation":[0,-25,0],"translation":[1,3,1],"scale":[.7,.7,.7]}}
# bandage: a fat roll of gauze (axis x) with the loose end hanging out
BAND=oct_x(7,8,4.6,3.5,12.5,'wool')+oct_x(7,8,1.6,3.3,12.7,'light')+[box([4.2,2.2,3.2],[11.8,2.7,8],'wool'),box([4.2,.5,3],[11.8,2.6,3.6],'wool'),box([7.3,1.2,2.9],[8.7,2.2,3.05],'red')]
save('item','bandage',{"textures":MT,"elements":BAND,"display":DSP})
# syringe: barrel along z with red liquid, plunger and needle
SY=oct_x(8,8,1,0,0,'glass')[:0]
SY=[box([7,7,3],[9,9,11],'glass'),box([7.35,7.35,6],[8.65,8.65,10.6],'red'),box([6,6.6,10.8],[10,9.4,11.4],'white'),box([7.6,7.6,11.4],[8.4,8.4,14],'light'),box([6.8,6.8,14],[9.2,9.2,14.6],'white'),
    box([7.5,7.5,2.2],[8.5,8.5,3],'gray'),box([7.85,7.85,-1.5],[8.15,8.15,2.2],'steel')]
save('item','syringe',{"textures":MT,"elements":SY,"display":{**DSP,"gui":{"rotation":[30,-45,0],"scale":[1.05,1.05,1.05]}}})
# medical kit: red case, white cross on both faces, handle and latches
MK=[box([2,2,5],[14,11,11],'red'),box([1.8,6.2,4.8],[14.2,6.8,11.2],'gray'),
    box([7,4,4.85],[9,9,5],'white'),box([5.5,5.5,4.85],[10.5,7.5,5],'white'),box([7,4,11],[9,9,11.15],'white'),box([5.5,5.5,11],[10.5,7.5,11.15],'white'),
    box([6,11,7.4],[6.8,13,8.6],'dark'),box([9.2,11,7.4],[10,13,8.6],'dark'),box([6,12.4,7.4],[10,13.2,8.6],'dark'),
    box([3.5,6,4.6],[4.5,8,4.85],'steel'),box([11.5,6,4.6],[12.5,8,4.85],'steel')]
save('item','medkit',{"textures":MT,"elements":MK,"display":DSP})
# ---------------- coloured tennis balls ----------------
base=json.load(open(os.path.join(A,'models/item/tennis_ball.json')))
NAMES={"pink":"rosa","orange":"laranja","blue":"azul","red":"vermelha","white":"branca","magenta":"roxa","yellow":"amarela","green":"verde"}
EN={"pink":"Pink","orange":"Orange","blue":"Blue","red":"Red","white":"White","magenta":"Purple","yellow":"Yellow","green":"Green"}
DYE={"pink":"pink_dye","orange":"orange_dye","blue":"blue_dye","red":"red_dye","white":"white_dye","magenta":"purple_dye","yellow":"yellow_dye","green":"green_dye"}
for c in NAMES:
    m=copy.deepcopy(base)
    for e in m['elements']:
        for f in e['faces'].values():
            if f['texture']=='#lime':f['texture']='#'+c
            elif f['texture']=='#white'and c=='white':f['texture']='#gray'
    save('item','tennis_ball_'+c,m)
    json.dump({"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":"mareeletrica:tennis_ball"},{"item":"minecraft:"+DYE[c]}],"result":{"id":"mareeletrica:tennis_ball_"+c,"count":1}},open(os.path.join(D,'recipe','tennis_ball_'+c+'.json'),'w'))
# ---------------- entertainment blocks (front = north) ----------------
BT={"blue":V("blue_concrete"),"purple":V("purple_concrete"),"magenta":V("magenta_concrete"),"black":V("black_concrete"),"red":V("red_concrete"),"yellow":V("yellow_concrete"),"lime":V("lime_concrete"),
    "white":V("white_concrete"),"glass":V("white_stained_glass"),"steel":"mareeletrica:block/steel","dark":"mareeletrica:block/dark","gray":V("gray_concrete"),"spruce":V("spruce_planks"),
    "screen":"mareeletrica:block/arcade_screen","marquee":"mareeletrica:block/arcade_marquee","kernels":"mareeletrica:block/popcorn_kernels","sign":"mareeletrica:block/popcorn_sign","paper":"mareeletrica:block/firework_paper","cyan":V("cyan_concrete")}
ARC=[box([2,0,5],[14,15,14],'blue'),box([1.6,0,5],[2,15,14],'magenta'),box([14,0,5],[14.4,15,14],'magenta'),
     box([2.5,8.5,4.2],[13.5,13.5,5],'black'),box([3.3,9.1,4.05],[12.7,13,4.2],'screen',faces={'north':'screen'}),
     box([2,5.5,1.8],[14,7.5,5],'purple'),box([2,4,4.4],[14,5.5,5],'dark'),
     box([4.6,7.5,3],[5.4,9,3.8],'dark'),box([4.2,9,2.6],[5.8,10.4,4.2],'red'),
     box([8,7.5,2.6],[9.2,7.9,3.8],'yellow'),box([10,7.5,2.6],[11.2,7.9,3.8],'lime'),box([12,7.5,2.6],[13.2,7.9,3.8],'cyan'),
     box([2,13.5,4],[14,16,5.2],'marquee',faces={'north':'marquee'}),box([2,15,5],[14,16,14],'blue'),box([3,1,4.8],[13,3.5,5],'black')]
POP=[box([2,0,2],[14,1,14],'dark')]+[box([x,0,z],[x+1.6,1.6,z+1.6],'black') for x,z in ((2.2,2.2),(12.2,2.2),(2.2,12.2),(12.2,12.2))]+[
     box([2,1,2],[14,6,14],'red'),box([2.2,4,1.8],[13.8,5,2],'white'),
     box([3,6,3],[13,15,13],'glass'),box([3.3,6,3.3],[12.7,9.5,12.7],'kernels'),
     box([7.2,12,7.2],[8.8,14.8,8.8],'steel'),box([6,11,6],[10,12.6,10],'steel'),box([7,9.5,7],[9,10.6,9],'kernels'),
     box([2.5,15,2.5],[13.5,16,13.5],'red'),box([4,16,7.4],[12,19.5,8.6],'sign')]
FW=[box([1,0,1],[15,5,15],'spruce'),box([.8,4,.8],[15.2,5,15.2],'dark')]
cols=['red','blue','yellow','lime','magenta','cyan','red','yellow','blue']
for i,(x,z) in enumerate([(x,z) for x in (4,8,12) for z in (4,8,12)]):
    h=11+(i*3)%5;FW+=oct_y(x,z,1.5,5,h,cols[i])+oct_y(x,z,1.2,h,h+.6,'paper')
for name,el,tx in (('arcade',ARC,BT),('popcorn_machine',POP,BT),('fireworks_show',FW,BT)):
    m={"parent":"minecraft:block/block","textures":dict(tx,particle=tx['red'] if name!='arcade' else tx['blue']),"elements":el}
    if name=='popcorn_machine':m["render_type"]="minecraft:translucent"
    save('block',name,m)
    json.dump({"parent":"mareeletrica:block/"+name},open(os.path.join(A,'models/item',name+'.json'),'w'))
    json.dump({"variants":{f"facing={f}":{"model":"mareeletrica:block/"+name,**({"y":y} if y else {})} for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates',name+'.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:"+name}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks',name+'.json'),'w'))
json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/popcorn"}},open(os.path.join(A,'models/item/popcorn.json'),'w'))
def shaped(name,pat,key,n=1):
    json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":pat,"key":{k:{"item":v} for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}},open(os.path.join(D,'recipe',name+'.json'),'w'))
shaped('arcade',["GGG","RNR","PPP"],{"G":"minecraft:black_stained_glass_pane","R":"minecraft:redstone","N":"minecraft:note_block","P":"mareeletrica:iron_plate"})
shaped('popcorn_machine',["GGG","GFG","RIR"],{"G":"minecraft:glass","F":"minecraft:furnace","R":"minecraft:red_dye","I":"minecraft:iron_ingot"})
shaped('fireworks_show',["RRR","RDR","PPP"],{"R":"minecraft:firework_rocket","D":"minecraft:dispenser","P":"minecraft:spruce_planks"})
L={'pt_br':{"block.mareeletrica.arcade":"Fliperama","block.mareeletrica.popcorn_machine":"Máquina de Pipoca","block.mareeletrica.fireworks_show":"Lançador de Show de Fogos","item.mareeletrica.popcorn":"Pipoca"},
   'en_us':{"block.mareeletrica.arcade":"Arcade Cabinet","block.mareeletrica.popcorn_machine":"Popcorn Machine","block.mareeletrica.fireworks_show":"Fireworks Show Launcher","item.mareeletrica.popcorn":"Popcorn"}}
for c in NAMES:L['pt_br']["item.mareeletrica.tennis_ball_"+c]="Bola de tênis "+NAMES[c];L['en_us']["item.mareeletrica.tennis_ball_"+c]=EN[c]+" Tennis Ball"
for lang,kv in L.items():
    p=os.path.join(A,'lang',lang+'.json');dd=json.load(open(p,encoding='utf-8'));dd.update(kv);json.dump(dd,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.65.2 assets ok')
