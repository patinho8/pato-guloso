"""0.66.0: 2-block-tall arcade cabinet (lower: cabinet, coin door, 2-player control desk; upper: screen + lit marquee)."""
import json,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
V=lambda n:"minecraft:block/"+n
TX={"blue":V("blue_concrete"),"dark":V("gray_concrete"),"black":V("black_concrete"),"purple":V("purple_concrete"),"red":V("red_concrete"),
    "yellow":V("yellow_concrete"),"lime":V("lime_concrete"),"cyan":V("cyan_concrete"),"white":V("white_concrete"),"magenta":V("magenta_concrete"),
    "screen":"mareeletrica:block/arcade_screen","marquee":"mareeletrica:block/arcade_marquee","particle":V("blue_concrete")}
def box(f,t,tex,front=None):
    for v in f+t:assert -16<=v<=32
    fc={d:{"texture":"#"+tex} for d in ["north","south","west","east","up","down"]}
    if front:fc["north"]={"texture":"#"+front}
    return {"from":f,"to":t,"faces":fc}
LOW=[box([1,0,2],[15,16,15],'blue'),box([1.6,0,1.6],[14.4,8,2],'dark'),
     box([6,2.5,1.4],[10,6.5,1.6],'black'),box([6.7,4.6,1.3],[7.6,5.4,1.4],'red'),box([8.4,4.6,1.3],[9.3,5.4,1.4],'red'),
     box([1.5,10.6,-1.4],[14.5,12.4,5],'purple'),box([1.8,12.4,-1.2],[14.2,12.7,4.8],'black'),box([1.5,8,-1.4],[14.5,10.6,-1],'purple'),
     box([2,8,-1],[14,10.6,2],'dark'),box([2,12.7,4.4],[14,16,5],'dark')]
for jx,cols in ((3.6,('yellow','red')),(9.6,('lime','cyan'))):   # two players: joystick + 2 buttons each
    LOW+=[box([jx+.5,12.7,1],[jx+1.1,14.4,1.6],'dark'),box([jx+.1,14.4,.6],[jx+1.5,15.8,2],'red' if jx<5 else 'blue'),
          box([jx+2.1,12.7,.4],[jx+3,13,1.3],cols[0]),box([jx+2.1,12.7,2],[jx+3,13,2.9],cols[1])]
LOW+=[box([.9,2,6],[1,16,8],'red'),box([15,2,6],[15.1,16,8],'red'),box([.9,2,9],[1,16,10],'yellow'),box([15,2,9],[15.1,16,10],'yellow')]
UP=[box([1,0,4],[15,16,15],'blue'),box([2.4,0,4.2],[13.6,9.6,5],'black'),box([3.2,.8,4.05],[12.8,8.8,4.2],'black','screen'),
    box([1.6,10.2,3.2],[14.4,15,5],'black','marquee'),box([1,15,3],[15,16,15],'blue'),box([1.6,9.6,4.4],[14.4,10.2,5],'dark'),
    box([.9,0,6],[1,12,8],'red'),box([15,0,6],[15.1,12,8],'red'),box([.9,0,9],[1,12,10],'yellow'),box([15,0,9],[15.1,12,10],'yellow')]
for n,e in (('arcade_lower',LOW),('arcade_upper',UP)):
    json.dump({"parent":"minecraft:block/block","textures":TX,"elements":e},open(os.path.join(A,'models/block',n+'.json'),'w'),separators=(',',':'))
ITEM=LOW+[{**b,"from":[b["from"][0],b["from"][1]+16,b["from"][2]],"to":[b["to"][0],b["to"][1]+16,b["to"][2]]} for b in UP]
json.dump({"parent":"minecraft:block/block","textures":TX,"elements":ITEM,"display":{"gui":{"rotation":[30,225,0],"translation":[0,-3,0],"scale":[.42,.42,.42]},
  "ground":{"translation":[0,2,0],"scale":[.22,.22,.22]},"fixed":{"translation":[0,-3,0],"scale":[.4,.4,.4]},
  "thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2,0],"scale":[.25,.25,.25]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.25,.25,.25]}}},
  open(os.path.join(A,'models/item/arcade.json'),'w'),separators=(',',':'))
rot={"north":0,"east":90,"south":180,"west":270}
json.dump({"variants":{f"facing={f},half={h}":{"model":"mareeletrica:block/arcade_"+h,**({"y":r} if r else {})} for f,r in rot.items() for h in ("lower","upper")}},open(os.path.join(A,'blockstates/arcade.json'),'w'))
json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:arcade"}],
  "conditions":[{"condition":"minecraft:survives_explosion"},{"condition":"minecraft:block_state_property","block":"mareeletrica:arcade","properties":{"half":"lower"}}]}]},
  open(os.path.join(D,'loot_table/blocks/arcade.json'),'w'))
print('arcade ok',len(LOW),len(UP))
