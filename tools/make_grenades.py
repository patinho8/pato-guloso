"""Grenade models (block model for the thrown entity + item model). Octagonal cylinders = square + square turned 45 degrees."""
import json,math,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica')
TEX={"olive":"minecraft:block/green_terracotta","olive2":"minecraft:block/moss_block","steel":"mareeletrica:block/steel","dark":"mareeletrica:block/dark",
 "yellow":"minecraft:block/yellow_concrete","gray":"minecraft:block/light_gray_concrete","gray2":"minecraft:block/gray_concrete","white":"minecraft:block/white_concrete",
 "red":"minecraft:block/red_concrete","black":"minecraft:block/black_concrete","green":"minecraft:block/green_concrete","copper":"mareeletrica:block/copper","particle":"mareeletrica:block/steel"}
def box(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def oct_y(cx,cz,r,y0,y1,tex):
    a=r*.924
    return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
def lever(x,y0,y1,z0,z1):return [box([x,y0,z0],[x+.35,y1,z1],'steel'),box([x-1.6,y1-.35,z0],[x+.35,y1,z1],'steel')]
def ring(x,y,z):   # pull ring on the north face
    return [box([x-1,y-1,z-.2],[x+1,y-.7,z+.2],'steel'),box([x-1,y+.7,z-.2],[x+1,y+1,z+.2],'steel'),box([x-1,y-1,z-.2],[x-.7,y+1,z+.2],'steel'),box([x+.7,y-1,z-.2],[x+1,y+1,z+.2],'steel'),box([x-.15,y+1,z-.15],[x+.15,y+2.2,z+.15],'steel')]
def fuse(top):return oct_y(8,8,1.25,top,top+1.6,'steel')+oct_y(8,8,.9,top+1.6,top+2.1,'dark')
def frag():
    el=[];R=3.9;c=6.8
    for y0 in [x*.5 for x in range(int((c-R)*2),int((c+R)*2))]:
        mid=y0+.25;r=math.sqrt(max(R*R-(mid-c)**2,0));
        if r<.4:continue
        tex='olive' if int(y0*2)%3 else 'olive2'
        el+=oct_y(8,8,r,y0,y0+.5,tex)
    el+=oct_y(8,8,R*.97+.08,8.3,8.7,'yellow')
    top=c+R-.3
    el+=fuse(top)+lever(10.3,c-1.5,top+2.0,7.4,8.6)+ring(6.2,top+1.2,6.4)
    return el
def can(r,y0,y1,body,bands,holes=None):
    el=oct_y(8,8,r,y0,y1,body)
    for (b0,b1,tex) in bands:el+=oct_y(8,8,r+.06,b0,b1,tex)
    if holes:
        for yy in holes:
            for k in range(8):
                a=math.radians(k*45+22.5);x=8+(r+.02)*math.cos(a);z=8+(r+.02)*math.sin(a)
                el.append(box([x-.3,yy-.3,z-.3],[x+.3,yy+.3,z+.3],'black'))
    el+=fuse(y1)+lever(8+r*.95,y0+2,y1+2.0,7.4,8.6)+ring(8-r+.2,y1+1.2,6.5)
    return el
SHAPES={'frag_grenade':frag(),
 'flash_grenade':can(2.5,2.4,11.2,'gray',[(10.2,10.7,'white'),(2.4,2.9,'gray2')],holes=[4.2,6.4,8.6]),
 'smoke_grenade':can(2.8,1.4,11.8,'gray2',[(9.6,10.6,'white'),(3.0,3.5,'green')]),
 'incendiary_grenade':can(2.5,2.0,11.4,'gray2',[(5.5,7.5,'red'),(10.4,10.9,'black')])}
DISPLAY={"gui":{"rotation":[25,-30,0],"translation":[0,0,0],"scale":[1.15,1.15,1.15]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"fixed":{"scale":[1,1,1]},
 "thirdperson_righthand":{"rotation":[0,0,0],"translation":[0,2.5,1.5],"scale":[.5,.5,.5]},"thirdperson_lefthand":{"rotation":[0,0,0],"translation":[0,2.5,1.5],"scale":[.5,.5,.5]},
 "firstperson_righthand":{"rotation":[0,-25,0],"translation":[1,3.5,-1],"scale":[.6,.6,.6]},"firstperson_lefthand":{"rotation":[0,-25,0],"translation":[1,3.5,-1],"scale":[.6,.6,.6]}}
for name,el in SHAPES.items():
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    json.dump({"ambientocclusion":False,"textures":TEX,"elements":el},open(os.path.join(A,'models/block',f'grenade_{name}.json'),'w'),separators=(',',':'))
    json.dump({"ambientocclusion":False,"textures":TEX,"elements":el,"display":DISPLAY},open(os.path.join(A,'models/item',f'{name}.json'),'w'),separators=(',',':'))
    print(name,len(el),'elements')
