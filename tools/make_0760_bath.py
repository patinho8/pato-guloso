"""0.76.0: claw-foot bathtub (2 blocks): foot/head models, water surfaces, multipart blockstate, item, loot, recipe, lang."""
import json,os
R='src/main/resources/';A=R+'assets/mareeletrica/';D=R+'data/mareeletrica/'
def save(p,o):os.makedirs(os.path.dirname(p),exist_ok=True);json.dump(o,open(p,'w'),indent=1)
def el(f,t,tex,faces=None,tint=False):
    e={"from":f,"to":t,"faces":{d:dict({"texture":"#"+tex},**({"tintindex":0} if tint else {})) for d in (faces or ["north","south","west","east","up","down"])}}
    return e
TEX={"t":"minecraft:block/quartz_block_side","i":"minecraft:block/white_concrete","g":"minecraft:block/gold_block","m":"minecraft:block/iron_block",
     "r":"minecraft:block/red_concrete","b":"minecraft:block/light_blue_concrete","particle":"minecraft:block/quartz_block_side"}
def tub(head):
    E=[]
    # z runs foot (north, 0) -> head (south, 16) inside one block; head mirrors the walls
    if not head:
        E+=[el([1,2,1],[15,4,16],'t'),el([1,4,1],[15,10,3],'t'),el([1,4,1],[3,10,16],'t'),el([13,4,1],[15,10,16],'t')]
        E+=[el([.5,10,.5],[15.5,10.8,3.2],'t'),el([.5,10,.5],[3.2,10.8,16],'t'),el([12.8,10,.5],[15.5,10.8,16],'t')]
        E+=[el([3,3.9,3],[13,4.0,16],'i',['up'])]
        E+=[el([1.5,0,1.5],[3.5,2.2,3.5],'g'),el([12.5,0,1.5],[14.5,2.2,3.5],'g'),el([1.2,0,1.2],[3.8,.6,3.8],'g'),el([12.2,0,1.2],[14.8,.6,3.8],'g')]
        E+=[el([7,3.95,5],[9,4.05,7],'m',['up'])]   # drain
    else:
        E+=[el([1,2,0],[15,4,15],'t'),el([1,4,13],[15,10,15],'t'),el([1,4,0],[3,10,15],'t'),el([13,4,0],[15,10,15],'t')]
        E+=[el([.5,10,12.8],[15.5,10.8,15.5],'t'),el([.5,10,0],[3.2,10.8,15.5],'t'),el([12.8,10,0],[15.5,10.8,15.5],'t')]
        E+=[el([3,3.9,0],[13,4.0,13],'i',['up'])]
        E+=[el([1.5,0,12.5],[3.5,2.2,14.5],'g'),el([12.5,0,12.5],[14.5,2.2,14.5],'g'),el([1.2,0,12.2],[3.8,.6,14.8],'g'),el([12.2,0,12.2],[14.8,.6,14.8],'g')]
        # taps: spout, mixer and two handles on the end wall
        E+=[el([7.2,10.8,12.6],[8.8,12.6,14.4],'m'),el([7.2,11.2,10.4],[8.8,12.6,12.6],'m'),el([7.4,10.4,10.4],[8.6,11.2,11.4],'m')]
        E+=[el([4.2,10.8,13.2],[5.8,11.8,14.8],'m'),el([3.8,11.8,13.4],[6.2,12.4,14.6],'r'),el([10.2,10.8,13.2],[11.8,11.8,14.8],'m'),el([9.8,11.8,13.4],[12.2,12.4,14.6],'b')]
    return {"parent":"minecraft:block/block","ambientocclusion":False,"textures":TEX,"elements":E}
save(A+'models/block/bathtub_foot.json',tub(False));save(A+'models/block/bathtub_head.json',tub(True))
W={"t":"minecraft:block/water_still","particle":"minecraft:block/water_still"}
save(A+'models/block/bathtub_water_foot.json',{"render_type":"minecraft:translucent","ambientocclusion":False,"textures":W,"elements":[el([3,8.6,3],[13,8.6,16],'t',['up','down'],tint=True)]})
save(A+'models/block/bathtub_water_head.json',{"render_type":"minecraft:translucent","ambientocclusion":False,"textures":W,"elements":[el([3,8.6,0],[13,8.6,13],'t',['up','down'],tint=True)]})
Y={'north':0,'east':90,'south':180,'west':270}
mp=[]
for f,y in Y.items():
    for part in ('foot','head'):
        mp.append({"when":{"facing":f,"part":part},"apply":dict({"model":f"mareeletrica:block/bathtub_{part}"},**({"y":y} if y else {}))})
        mp.append({"when":{"facing":f,"part":part,"filled":"true"},"apply":dict({"model":f"mareeletrica:block/bathtub_water_{part}"},**({"y":y} if y else {}))})
save(A+'blockstates/bathtub.json',{"multipart":mp})
# item: both halves side by side
foot=tub(False)['elements'];head=tub(True)['elements']
def shift(es,dz):
    out=[]
    for e in es:
        e=json.loads(json.dumps(e));e['from'][2]+=dz;e['to'][2]+=dz;out.append(e)
    return out
items=shift(foot,-8)+shift(head,8)
for e in items:
    for k in ('from','to'):e[k]=[round(e[k][0]*.5+4,3),round(e[k][1]*.5+4,3),round(e[k][2]*.5+4,3)]
save(A+'models/item/bathtub.json',{"parent":"minecraft:block/block","textures":TEX,"elements":items,
 "display":{"gui":{"rotation":[30,45,0],"scale":[1.1]*3},"ground":{"scale":[.5]*3},"fixed":{"scale":[1]*3},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.5]*3},"thirdperson_righthand":{"rotation":[75,45,0],"scale":[.4]*3}}})
save(D+'loot_table/blocks/bathtub.json',{"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:bathtub",
 "conditions":[{"condition":"minecraft:block_state_property","block":"mareeletrica:bathtub","properties":{"part":"foot"}}]}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]})
save(D+'recipe/bathtub.json',{"type":"minecraft:crafting_shaped","category":"misc","pattern":["Q Q","QWQ","G G"],"key":{"Q":{"item":"minecraft:quartz_block"},"W":{"item":"minecraft:water_bucket"},"G":{"item":"minecraft:gold_nugget"}},"result":{"id":"mareeletrica:bathtub","count":1}})
for l,n in (('pt_br','Banheira'),('en_us','Bathtub')):
    p=A+f'lang/{l}.json';d=json.load(open(p,encoding='utf-8'));d['block.mareeletrica.bathtub']=n;json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('bath ok')
