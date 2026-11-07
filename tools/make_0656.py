"""0.65.6: skateboard — model (entity part + item), recipe, lang."""
import json,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
V=lambda n:"minecraft:block/"+n
def el(f,t,side,up=None,down=None,rot=None):
    faces={d:{"texture":"#"+side} for d in ["north","south","west","east"]};faces["up"]={"texture":"#"+(up or side)};faces["down"]={"texture":"#"+(down or side)}
    e={"from":f,"to":t,"faces":faces}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2]}
    for v in f+t:assert -16<=v<=32
    return e
TX={"grip":V("black_concrete"),"deck":V("stripped_oak_log"),"art":V("red_concrete"),"art2":V("yellow_concrete"),"truck":V("iron_block"),"wheel":V("white_concrete"),"particle":V("stripped_oak_log")}
B=[el([5.2,2.6,2.5],[10.8,3.2,13.5],'deck','grip','art'),
   el([5.2,2.6,13.4],[10.8,3.2,16],'deck','grip','art2',([8,2.9,13.5],'x',22.5)),
   el([5.2,2.6,0],[10.8,3.2,2.6],'deck','grip','art2',([8,2.9,2.5],'x',-22.5)),
   el([7.4,2.55,6],[8.6,2.6,10],'art2'),
   el([5.9,1.8,3.5],[10.1,2.6,4.5],'truck'),el([5.9,1.8,11.5],[10.1,2.6,12.5],'truck')]
for z in (4,12):
    for x in (4.9,10.1):B.append(el([x,0,z-1.1],[x+1,2.2,z+1.1],'wheel'))
json.dump({"textures":TX,"elements":B},open(os.path.join(A,'models/block/skateboard.json'),'w'),separators=(',',':'))
json.dump({"textures":TX,"elements":B,"display":{"gui":{"rotation":[30,45,0],"translation":[0,3,0],"scale":[1.3,1.3,1.3]},"ground":{"translation":[0,2,0],"scale":[.8,.8,.8]},
  "fixed":{"rotation":[-90,0,0],"scale":[1.2,1.2,1.2]},"thirdperson_righthand":{"rotation":[0,90,-70],"translation":[0,3,2],"scale":[.7,.7,.7]},
  "firstperson_righthand":{"rotation":[0,60,-40],"translation":[2,3,0],"scale":[.7,.7,.7]}}},open(os.path.join(A,'models/item/skateboard.json'),'w'),separators=(',',':'))
json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":["SSS","I I"],"key":{"S":{"tag":"minecraft:wooden_slabs"},"I":{"item":"minecraft:iron_ingot"}},
  "result":{"id":"mareeletrica:skateboard","count":1}},open(os.path.join(D,'recipe/skateboard.json'),'w'))
LANG={'pt_br':{"item.mareeletrica.skateboard":"Skate","entity.mareeletrica.skateboard":"Skate"},'en_us':{"item.mareeletrica.skateboard":"Skateboard","entity.mareeletrica.skateboard":"Skateboard"}}
for lang,kv in LANG.items():
    p=os.path.join(A,'lang',lang+'.json');dd=json.load(open(p,encoding='utf-8'));dd.update(kv);json.dump(dd,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.65.6 assets ok')
