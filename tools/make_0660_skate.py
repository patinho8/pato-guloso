"""0.66.0: skatepark blocks — handrail (corrimão) and kicker ramp; recipes, loot, lang."""
import json,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
V=lambda n:"minecraft:block/"+n
def el(f,t,tex,rot=None):
    for v in f+t:assert -16<=v<=32,(f,t)
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex} for d in ["north","south","west","east","up","down"]}}
    if rot:e["rotation"]=rot
    return e
RT={"bar":V("iron_block"),"post":V("gray_concrete"),"base":V("black_concrete"),"yellow":V("yellow_concrete"),"particle":V("iron_block")}
RAIL=[el([7,10,0],[9,12,16],'bar'),el([7.3,0,7.3],[8.7,10,8.7],'post'),el([6,0,6],[10,.6,10],'base'),el([7.25,4,7.25],[8.75,4.6,8.75],'yellow')]
json.dump({"parent":"minecraft:block/block","textures":RT,"elements":RAIL},open(os.path.join(A,'models/block/skate_rail.json'),'w'),separators=(',',':'))
MT={"wood":V("spruce_planks"),"top":V("stripped_birch_log"),"pipe":V("iron_block"),"dark":V("dark_oak_planks"),"particle":V("spruce_planks")}
RAMP=[el([0,0,-6.627],[16,1,16],'top',{"origin":[8,0,16],"axis":"x","angle":45}),
      el([0,0,0],[16,16,1.4],'dark'),el([0,15.2,-.6],[16,16.6,.8],'pipe')]
for i in range(4):
    z0,z1,h=12-4*i,16-4*i,4*(i+1)-.6
    RAMP+=[el([0,0,z0],[.6,h,z1],'wood'),el([15.4,0,z0],[16,h,z1],'wood')]
json.dump({"parent":"minecraft:block/block","textures":MT,"elements":RAMP},open(os.path.join(A,'models/block/skate_ramp.json'),'w'),separators=(',',':'))
rot={"north":0,"east":90,"south":180,"west":270}
for n in ('skate_rail','skate_ramp'):
    json.dump({"variants":{f"facing={f}":{"model":"mareeletrica:block/"+n,**({"y":r} if r else {})} for f,r in rot.items()}},open(os.path.join(A,'blockstates',n+'.json'),'w'))
    json.dump({"parent":"mareeletrica:block/"+n},open(os.path.join(A,'models/item',n+'.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:"+n}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks',n+'.json'),'w'))
def shaped(name,pat,key,n=1):
    json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":pat,"key":{k:({"tag":v[1:]} if v.startswith('#') else {"item":v}) for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}},open(os.path.join(D,'recipe',name+'.json'),'w'))
shaped('skate_rail',["III"," B "],{"I":"minecraft:iron_ingot","B":"minecraft:iron_bars"},4)
shaped('skate_ramp',["  S"," SS","SSS"],{"S":"#minecraft:wooden_slabs"},3)
LANG={'pt_br':{"block.mareeletrica.skate_rail":"Corrimão de Skate","block.mareeletrica.skate_ramp":"Rampa de Skate"},'en_us':{"block.mareeletrica.skate_rail":"Skate Handrail","block.mareeletrica.skate_ramp":"Skate Ramp"}}
for lang,kv in LANG.items():
    pp=os.path.join(A,'lang',lang+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update(kv);json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('skate park ok')
