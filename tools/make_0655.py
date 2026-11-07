"""0.65.5: pool table (sinuca) and cue — models, textures, blockstates, recipes, lang."""
import json,os,random
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica');T=os.path.join(A,'textures')
V=lambda n:"minecraft:block/"+n
def box(f,t,tex,uv=None):
    return {"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex} for d in ["down","up","north","south","west","east"]}}
def save(kind,name,m):
    for e in m.get("elements",[]):
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
# textures
random.seed(655)
im=Image.new('RGBA',(16,16))
for x in range(16):
    for y in range(16):
        n=random.randint(-6,6);im.putpixel((x,y),(22+n//2,118+n,58+n//2,255))
im.save(os.path.join(T,'block/pool_felt.png'))
os.makedirs(os.path.join(T,'entity'),exist_ok=True);Image.new('RGBA',(16,16),(255,255,255,255)).save(os.path.join(T,'entity/pool_white.png'))
TX={"wood":V("dark_oak_planks"),"felt":"mareeletrica:block/pool_felt","black":V("black_concrete"),"gold":V("gold_block"),"particle":V("dark_oak_planks"),
    "white":V("white_concrete"),"blue":V("blue_concrete"),"light":V("stripped_birch_log_top")}
def m(el,disp=None):
    d={"textures":TX,"elements":el}
    if disp:d["display"]=disp
    return d
FELT=box([0,12,0],[16,13,16],'wood');FELT["faces"]["up"]["texture"]="#felt"
BODY=[box([0,8,0],[16,12,16],'wood'),FELT]
# corner: pocket at (0,0), rails on the west (x=0) and north (z=0) sides, leg under the corner
CORNER=BODY+[box([1,0,1],[4.5,8,4.5],'wood'),
 box([0,13,3],[1.2,15,16],'wood'),box([1.2,13,3],[2,14.4,16],'felt'),
 box([3,13,0],[16,15,1.2],'wood'),box([3,13,1.2],[16,14.4,2],'felt'),
 box([0,13,0],[3,13.05,3],'black'),box([0,13,0],[.6,15,3],'gold'),box([0,13,0],[3,15,.6],'gold')]
MIDA=BODY+[box([0,13,0],[1.2,15,13.5],'wood'),box([1.2,13,0],[2,14.4,13.5],'felt'),box([0,13,13.5],[2.4,13.05,16],'black'),box([0,13,13.5],[.6,15,16],'gold')]
MIDB=BODY+[box([0,13,2.5],[1.2,15,16],'wood'),box([1.2,13,2.5],[2,14.4,16],'felt'),box([0,13,0],[2.4,13.05,2.5],'black'),box([0,13,0],[.6,15,2.5],'gold')]
for n,e in (('pool_table_corner',CORNER),('pool_table_mid_a',MIDA),('pool_table_mid_b',MIDB)):save('block',n,m(e))
MODELS=['pool_table_corner','pool_table_mid_a','pool_table_mid_b']
json.dump({"variants":{f"kind={k},main={mm},rot={r}":{"model":"mareeletrica:block/"+MODELS[k],**({"y":90*r} if r else {})} for k in range(3) for r in range(4) for mm in ('false','true')}},
          open(os.path.join(A,'blockstates/pool_table.json'),'w'))
# item: a little table
IT=[box([1,8,4],[15,10,12],'wood'),box([1.6,10,4.6],[14.4,10.6,11.4],'felt'),box([1,10,4],[15,11,4.6],'wood'),box([1,10,11.4],[15,11,12],'wood'),box([1,10,4],[1.6,11,12],'wood'),box([14.4,10,4],[15,11,12],'wood'),
    box([2,3,5],[3.5,8,6.5],'wood'),box([12.5,3,5],[14,8,6.5],'wood'),box([2,3,9.5],[3.5,8,11],'wood'),box([12.5,3,9.5],[14,8,11],'wood'),
    box([5,10.6,7.5],[6,11.6,8.5],'white'),box([9,10.6,6.5],[10,11.6,7.5],'blue'),box([10,10.6,8],[11,11.6,9],'gold'),box([9.6,10.6,9.4],[10.6,11.6,10.4],'black')]
save('item','pool_table',m(IT,{"gui":{"rotation":[30,225,0],"translation":[0,-1,0],"scale":[1,1,1]},"ground":{"translation":[0,3,0],"scale":[.5,.5,.5]},
   "fixed":{"scale":[.9,.9,.9]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.375,.375,.375]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.4,.4,.4]}}))
CUE=[box([7.3,-8,7.3],[8.7,4,8.7],'wood'),box([7.4,-9,7.4],[8.6,-8,8.6],'black'),box([7.5,4,7.5],[8.5,22,8.5],'light'),box([7.6,22,7.6],[8.4,23,8.4],'blue'),box([7.25,2,7.25],[8.75,3,8.75],'gold')]
save('item','pool_cue',m(CUE,{"gui":{"rotation":[0,0,-45],"translation":[0,0,0],"scale":[.6,.6,.6]},"ground":{"translation":[0,3,0],"scale":[.5,.5,.5]},"fixed":{"rotation":[0,0,-45],"scale":[.6,.6,.6]},
   "thirdperson_righthand":{"rotation":[70,0,0],"translation":[0,1,3],"scale":[.85,.85,.85]},"firstperson_righthand":{"rotation":[80,10,0],"translation":[1,2,2],"scale":[.8,.8,.8]}}))
# recipes
def shaped(name,pat,key,n=1):
    json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":pat,"key":{k:{"item":v} for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}},open(os.path.join(D,'recipe',name+'.json'),'w'))
shaped('pool_table',["GGG","PBP","P P"],{"G":"minecraft:green_wool","P":"minecraft:dark_oak_planks","B":"minecraft:black_wool"})
shaped('pool_cue',["  D"," S ","P  "],{"D":"minecraft:blue_dye","S":"minecraft:stick","P":"minecraft:dark_oak_planks"})
LANG={'pt_br':{"block.mareeletrica.pool_table":"Mesa de Sinuca","item.mareeletrica.pool_table":"Mesa de Sinuca","item.mareeletrica.pool_cue":"Taco de Sinuca"},
      'en_us':{"block.mareeletrica.pool_table":"Pool Table","item.mareeletrica.pool_table":"Pool Table","item.mareeletrica.pool_cue":"Pool Cue"}}
for lang,kv in LANG.items():
    p=os.path.join(A,'lang',lang+'.json');dd=json.load(open(p,encoding='utf-8'));dd.update(kv);json.dump(dd,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.65.5 assets ok')
