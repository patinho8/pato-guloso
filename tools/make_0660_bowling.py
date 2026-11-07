"""0.66.0: bowling lane / arrow lane / gutter blocks, kit icons (bowling alley, golf course), tinted bowling ball."""
import json,os,random
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica');T=os.path.join(A,'textures')
random.seed(660)
def img(fn,path):
    im=Image.new('RGBA',(16,16))
    for y in range(16):
        for x in range(16):im.putpixel((x,y),fn(x,y))
    im.save(os.path.join(T,path+'.png'))
# maple boards running along the lane (texture v axis = facing direction)
def maple(x,y):
    board=x//2;n=random.randint(-6,6)+(board%3)*4
    c=(228+n,196+n,146+n//2,255)
    return (c[0]-28,c[1]-28,c[2]-24,255) if x%2==1 and random.random()<.25 else c
img(maple,'block/bowling_lane')
def arrow(x,y):
    c=maple(x,y)
    if 5<=y<=12 and abs(x-7.5)<=(y-5)*.6:return (60,40,30,255)
    return c
img(arrow,'block/bowling_lane_arrow')
img(lambda x,y:(lambda g:(g,g,g+6,255))(70+random.randint(-6,6)+(abs(x-7.5)<4)*12),'block/bowling_gutter')
def kit(top,mid,edge):
    def f(x,y):
        if y in (3,12) and 1<=x<=14:return edge
        if 4<=y<=11 and 1<=x<=14:return top if 6<=x<=9 else mid
        if 6<=y<=9 and x in (0,15):return mid
        return (0,0,0,0)
    return f
img(kit((228,196,146,255),(80,80,90,255),(60,40,30,255)),'item/field_kit_boliche')
img(kit((40,140,60,255),(90,175,70,255),(235,220,150,255)),'item/field_kit_golfe')
for n in ('boliche','golfe'):json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/field_kit_"+n}},open(os.path.join(A,'models/item/field_kit_'+n+'.json'),'w'))
def cube(top,side,h=16):
    return {"parent":"minecraft:block/block","textures":{"top":top,"side":side,"particle":side},"elements":[{"from":[0,0,0],"to":[16,h,16],"faces":{
        "up":{"texture":"#top","uv":[0,0,16,16]},"down":{"texture":"#side","cullface":"down"},"north":{"texture":"#side","uv":[0,16-h,16,16]},"south":{"texture":"#side","uv":[0,16-h,16,16]},
        "west":{"texture":"#side","uv":[0,16-h,16,16]},"east":{"texture":"#side","uv":[0,16-h,16,16]}}}]}
json.dump(cube("mareeletrica:block/bowling_lane","mareeletrica:block/bowling_lane"),open(os.path.join(A,'models/block/bowling_lane.json'),'w'))
json.dump(cube("mareeletrica:block/bowling_lane_arrow","mareeletrica:block/bowling_lane"),open(os.path.join(A,'models/block/bowling_lane_arrow.json'),'w'))
# gutter: a rounded trough 10 px high
G={"parent":"minecraft:block/block","textures":{"g":"mareeletrica:block/bowling_gutter","particle":"mareeletrica:block/bowling_gutter"},"elements":[
   {"from":[0,0,0],"to":[16,8,16],"faces":{d:{"texture":"#g"} for d in ["up","down","north","south","west","east"]}},
   {"from":[0,8,0],"to":[3,10,16],"faces":{d:{"texture":"#g"} for d in ["up","down","north","south","west","east"]}},
   {"from":[13,8,0],"to":[16,10,16],"faces":{d:{"texture":"#g"} for d in ["up","down","north","south","west","east"]}}]}
json.dump(G,open(os.path.join(A,'models/block/bowling_gutter.json'),'w'))
rot={"north":0,"east":90,"south":180,"west":270}
for n in ('bowling_lane','bowling_lane_arrow','bowling_gutter'):
    json.dump({"variants":{f"facing={f}":{"model":"mareeletrica:block/"+n,**({"y":r} if r else {})} for f,r in rot.items()}},open(os.path.join(A,'blockstates',n+'.json'),'w'))
    json.dump({"parent":"mareeletrica:block/"+n},open(os.path.join(A,'models/item',n+'.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:"+n}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks',n+'.json'),'w'))
def shaped(name,pat,key,n=1):
    json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":pat,"key":{k:({"tag":v[1:]} if v.startswith('#') else {"item":v}) for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}},open(os.path.join(D,'recipe',name+'.json'),'w'))
shaped('bowling_lane',["SSS","PPP"],{"S":"minecraft:honeycomb","P":"minecraft:birch_planks"},6)
shaped('bowling_lane_arrow',["L","D"],{"L":"mareeletrica:bowling_lane","D":"minecraft:black_dye"})
shaped('bowling_gutter',["I I","III"],{"I":"minecraft:iron_ingot"},6)
shaped('field_kit_boliche',["LPL","LBL","LLL"],{"L":"mareeletrica:bowling_lane","P":"mareeletrica:bowling_pinsetter","B":"mareeletrica:bowling_ball"})
shaped('field_kit_golfe',["MHM","MCM","MMM"],{"M":"minecraft:moss_block","H":"mareeletrica:golf_hole","C":"mareeletrica:golf_club"})
# bowling ball: tint the shell (tintindex 0), finger holes stay dark
p=os.path.join(A,'models/block/bowling_ball_part.json');m=json.load(open(p))
for e in m['elements']:
    for f in e['faces'].values():
        if f['texture'] not in ('#black','#dark'):f['tintindex']=0;f['texture']='#white'
json.dump(m,open(p,'w'),separators=(',',':'))
LANG={'pt_br':{"block.mareeletrica.bowling_lane":"Pista de Boliche","block.mareeletrica.bowling_lane_arrow":"Pista de Boliche (setas)","block.mareeletrica.bowling_gutter":"Canaleta de Boliche"},
      'en_us':{"block.mareeletrica.bowling_lane":"Bowling Lane","block.mareeletrica.bowling_lane_arrow":"Bowling Lane (arrows)","block.mareeletrica.bowling_gutter":"Bowling Gutter"}}
for lang,kv in LANG.items():
    pp=os.path.join(A,'lang',lang+'.json');dd=json.load(open(pp,encoding='utf-8'));dd.update(kv);json.dump(dd,open(pp,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('bowling assets ok')
