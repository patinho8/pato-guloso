"""0.71.0 — Giant Evil Duck boss: glowing eyes + angry brows parts, cursed egg and quack staff items, recipe, lang."""
import json,os,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
M=lambda n:"mareeletrica:block/"+n
random.seed(710)
def el(f,t,tex,rot=None,faces=None):
    e={"from":f,"to":t,"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in (faces or ["north","south","west","east","up","down"])}}
    if rot:e["rotation"]=rot
    return e
def save(path,obj):os.makedirs(os.path.dirname(path),exist_ok=True);json.dump(obj,open(path,'w'),separators=(',',':'))
def part(name,tex,els):save(os.path.join(A,'models/block',name+'.json'),{"parent":"minecraft:block/block","ambientocclusion":False,"textures":dict(tex,particle=list(tex.values())[0]),"elements":els})
# glowing red eye texture: hot centre, darker rim
im=Image.new('RGBA',(16,16))
for x in range(16):
    for y in range(16):
        d=((x-7.5)**2+(y-7.5)**2)**.5/10.6;im.putpixel((x,y),(255,int(max(0,200-d*260)),int(max(0,60-d*90)),255))
im.save(os.path.join(A,'textures/block/duck_evil_red.png'))
# eyes: red glowing square over each black eye with a black slit pupil
part('duck_evil_eyes',{"r":M("duck_evil_red"),"k":M("duck_black")},[
 el([5.82,9.9,2.2],[5.92,11.2,3.6],'r',faces=["west"]),el([10.08,9.9,2.2],[10.18,11.2,3.6],'r',faces=["east"]),
 el([5.78,10.0,2.75],[5.8,11.1,3.05],'k',faces=["west"]),el([10.2,10.0,2.75],[10.22,11.1,3.05],'k',faces=["east"]),
 el([6.3,9.9,1.42],[7.7,11.0,1.5],'r',faces=["north"]),el([8.3,9.9,1.42],[9.7,11.0,1.5],'r',faces=["north"]),
 el([6.85,10.0,1.39],[7.15,10.9,1.41],'k',faces=["north"]),el([8.85,10.0,1.39],[9.15,10.9,1.41],'k',faces=["north"])])
# angry brows: thick black bars slanting down towards the beak
part('duck_evil_brows',{"k":M("duck_black")},[
 el([5.6,11.3,1.6],[6.2,11.9,4.2],'k',rot={"angle":-22.5,"axis":"x","origin":[6,11.6,2.9]}),
 el([9.8,11.3,1.6],[10.4,11.9,4.2],'k',rot={"angle":-22.5,"axis":"x","origin":[10,11.6,2.9]}),
 el([6.0,11.15,1.2],[7.9,11.75,1.5],'k',rot={"angle":-22.5,"axis":"z","origin":[7,11.45,1.35]}),
 el([8.1,11.15,1.2],[10.0,11.75,1.5],'k',rot={"angle":22.5,"axis":"z","origin":[9,11.45,1.35]})])
# items
def item(name,img):
    img.save(os.path.join(A,'textures/item',name+'.png'))
    save(os.path.join(A,'models/item',name+'.json'),{"parent":"minecraft:item/"+("handheld" if name=='quack_staff' else "generated"),"textures":{"layer0":"mareeletrica:item/"+name}})
egg=Image.new('RGBA',(16,16),(0,0,0,0))
for x in range(16):
    for y in range(16):
        dx=(x-7.5)/5.2;dy=(y-8.6)/(6.4 if y>8 else 7.2)
        if dx*dx+dy*dy<=1:
            shade=1-.45*max(0,dx)-.2*max(0,dy)
            c=(int(95*shade),int(35*shade),int(130*shade),255)
            if (x*3+y*5)%11==0:c=(40,10,55,255)
            egg.putpixel((x,y),c)
d=ImageDraw.Draw(egg);d.line([(7,3),(6,6),(8,8),(7,11)],fill=(255,70,60,255));d.point((5,5),fill=(220,190,255,255));d.point((5,6),fill=(220,190,255,255))
item('cursed_duck_egg',egg)
st=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(st)
for i in range(10):d.point((2+i,13-i),fill=(110,70,40,255));d.point((3+i,13-i),fill=(80,50,28,255))
d.rectangle([10,1,14,5],fill=(255,214,40,255));d.rectangle([14,3,15,4],fill=(255,140,20,255));d.point((12,2),fill=(20,20,20,255))
d.point((11,6),fill=(160,60,200,255));d.point((13,6),fill=(160,60,200,255));d.point((9,4),fill=(160,60,200,255))
item('quack_staff',st)
save(os.path.join(D,'recipe/cursed_duck_egg.json'),{"type":"minecraft:crafting_shaped","category":"misc","pattern":["OFO","FGF","OFO"],
 "key":{"O":{"item":"minecraft:obsidian"},"F":{"item":"minecraft:fermented_spider_eye"},"G":{"item":"mareeletrica:golden_duck_egg"}},"result":{"id":"mareeletrica:cursed_duck_egg","count":1}})
LANG={'pt_br':{"entity.mareeletrica.evil_duck":"Pato Gigante Malvado","entity.mareeletrica.evil_egg":"Ovo Explosivo","item.mareeletrica.cursed_duck_egg":"Ovo Amaldiçoado","item.mareeletrica.quack_staff":"Cajado do Quack"},
 'en_us':{"entity.mareeletrica.evil_duck":"Giant Evil Duck","entity.mareeletrica.evil_egg":"Exploding Egg","item.mareeletrica.cursed_duck_egg":"Cursed Duck Egg","item.mareeletrica.quack_staff":"Quack Staff"}}
for lang,kv in LANG.items():
    pp=os.path.join(A,'lang',lang+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update(kv);json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.71.0 boss assets ok')
