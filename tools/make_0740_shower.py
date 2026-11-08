"""0.74.0: shower block model, blockstate, item, loot, recipe, lang."""
import json,os,math
R='src/main/resources/';A=R+'assets/mareeletrica/';D=R+'data/mareeletrica/'
def save(p,o):os.makedirs(os.path.dirname(p),exist_ok=True);json.dump(o,open(p,'w'),separators=(',',':'))
def el(f,t,tex,rot=None,faces=None):
    e={"from":f,"to":t,"faces":{d:{"texture":"#"+tex} for d in (faces or ["north","south","west","east","up","down"])}}
    if rot:e["rotation"]=rot
    return e
E_=[]
E_+= [el([6,3,15],[10,7,16],'c'),el([6.5,3.5,14.6],[9.5,6.5,15],'m')]                 # wall plate + valve escutcheon
E_+= [el([7.6,4.4,13.2],[8.4,5.6,14.6],'c'),el([6.4,4.6,12.8],[9.6,5.4,13.3],'r')]    # mixer lever
E_+= [el([7.3,6,14.2],[8.7,12.4,15.6],'c')]                                          # riser pipe
E_+= [el([7.3,11.4,7.2],[8.7,12.8,15.6],'c'),el([7.0,11.2,14.8],[9.0,13.0,16],'m')]   # arm + wall flange
E_+= [el([7.4,10.4,6.6],[8.6,11.6,7.8],'c')]                                         # elbow down
h=4.2*math.tan(math.pi/8)
for rot in (None,{"angle":45,"axis":"y","origin":[8,10,6.5]}):                         # round head (octagon)
    E_+=[el([3.8,9.2,6.5-h],[12.2,10.6,6.5+h],'c',rot),el([8-h,9.2,2.3],[8+h,10.6,10.7],'c',rot)]
E_+= [el([4.6,9.1,6.5-3.2*math.tan(math.pi/8)],[11.4,9.2,6.5+3.2*math.tan(math.pi/8)],'n',None,['down']),el([8-3.2*math.tan(math.pi/8),9.1,3.3],[8+3.2*math.tan(math.pi/8),9.2,9.7],'n',None,['down'])]
E_+= [el([6.6,10.6,5.1],[9.4,11.0,7.9],'m')]
for e in E_:
    for k in ('from','to'):e[k]=[round(v,3) for v in e[k]]
tex={"c":"minecraft:block/iron_block","m":"minecraft:block/light_gray_concrete","r":"minecraft:block/red_concrete","n":"mareeletrica:block/shower_nozzles","particle":"minecraft:block/iron_block"}
save(A+'models/block/shower.json',{"parent":"minecraft:block/block","ambientocclusion":False,"textures":tex,"elements":E_,
 "display":{"gui":{"rotation":[30,135,0],"translation":[0,-2,0],"scale":[1.1]*3},"ground":{"scale":[.5]*3},"fixed":{"scale":[1]*3},"firstperson_righthand":{"rotation":[0,135,0],"scale":[.5]*3},"thirdperson_righthand":{"rotation":[75,45,0],"scale":[.4]*3}}})
from PIL import Image
im=Image.new('RGBA',(16,16),(170,174,180,255))
for x in range(1,16,3):
    for y in range(1,16,3):im.putpixel((x,y),(40,44,52,255))
im.save(A+'textures/block/shower_nozzles.png')
Y={'north':0,'east':90,'south':180,'west':270}
save(A+'blockstates/shower.json',{"variants":{f"facing={f},powered={p}":({"model":"mareeletrica:block/shower","y":y} if y else {"model":"mareeletrica:block/shower"}) for f,y in Y.items() for p in ('true','false')}})
save(A+'models/item/shower.json',{"parent":"mareeletrica:block/shower"})
save(D+'loot_table/blocks/shower.json',{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:shower"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
save(D+'recipe/shower.json',{"type":"minecraft:crafting_shaped","category":"misc","pattern":["III"," I ","IWI"],"key":{"I":{"item":"minecraft:iron_ingot"},"W":{"item":"minecraft:water_bucket"}},"result":{"id":"mareeletrica:shower","count":1}})
for l,n in (('pt_br','Chuveiro'),('en_us','Shower')):
    p=A+f'lang/{l}.json';d=json.load(open(p,encoding='utf-8'));d['block.mareeletrica.shower']=n;json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('shower ok')
