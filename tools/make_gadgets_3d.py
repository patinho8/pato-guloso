"""3D item models for the portable electronics, tennis ball, racket and net (0.46.0)."""
import json,os,math
from PIL import Image
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica')
T={"dark":"mareeletrica:block/dark","steel":"mareeletrica:block/steel","black":"minecraft:block/black_concrete","gray":"minecraft:block/gray_concrete",
 "light":"minecraft:block/light_gray_concrete","white":"minecraft:block/white_concrete","glass":"minecraft:block/light_blue_stained_glass","lens":"minecraft:block/cyan_concrete",
 "orange":"minecraft:block/orange_concrete","red":"minecraft:block/red_concrete","yellow":"minecraft:block/yellow_concrete","lime":"minecraft:block/lime_concrete",
 "pink":"minecraft:block/pink_concrete","magenta":"minecraft:block/magenta_concrete","green":"minecraft:block/green_concrete","blue":"minecraft:block/blue_concrete",
 "lamp":"minecraft:block/shroomlight","strings":"mareeletrica:block/tennis_strings","net":"mareeletrica:block/tennis_net","particle":"mareeletrica:block/dark"}
def box(f,t,tex,rot=None):
    e={"from":[round(v,3) for v in f],"to":[round(v,3) for v in t],"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2],"rescale":False}
    return e
def oct_z(cx,cy,r,z0,z1,tex):a=r*.924;return [box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex),box([cx-a,cy-a,z0],[cx+a,cy+a,z1],tex,([cx,cy,(z0+z1)/2],'z',45))]
def oct_y(cx,cz,r,y0,y1,tex):a=r*.924;return [box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex),box([cx-a,y0,cz-a],[cx+a,y1,cz+a],tex,([cx,(y0+y1)/2,cz],'y',45))]
HAND={"gui":{"rotation":[25,-30,0],"scale":[1.1,1.1,1.1]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"fixed":{"scale":[1,1,1]},
 "thirdperson_righthand":{"translation":[0,2.5,1.5],"scale":[.55,.55,.55]},"thirdperson_lefthand":{"translation":[0,2.5,1.5],"scale":[.55,.55,.55]},
 "firstperson_righthand":{"rotation":[0,-25,0],"translation":[1,3.5,-1],"scale":[.6,.6,.6]},"firstperson_lefthand":{"rotation":[0,-25,0],"translation":[1,3.5,-1],"scale":[.6,.6,.6]}}
def write(name,el,display=HAND,cutout=False):
    for e in el:
        for v in e['from']+e['to']:assert -16<=v<=32,(name,v)
    m={"textures":T,"elements":el,"display":display,"gui_light":"side"}
    if cutout:m["render_type"]="minecraft:cutout"
    json.dump(m,open(os.path.join(A,'models/item',name+'.json'),'w'),separators=(',',':'))
# binoculars: two tubes with a hinge bridge, rubber eyecups, blue-tinted objectives (point -z)
el=[]
for x in (4.8,11.2):el+=oct_z(x,8,2.4,3,11.5,'dark')+oct_z(x,8,1.7,11.5,14,'black')+oct_z(x,8,2.6,2,3.2,'steel')+[box([x-1.6,6.4,1.9],[x+1.6,9.6,2.0],'lens')]
el+=[box([6.8,7,5],[9.2,9,10],'gray'),box([7.4,9,6],[8.6,10.2,8],'steel')]   # bridge + focus wheel
write('binoculars',el)
# flashlight: tube, wide head, glowing lens, rubber switch
el=oct_z(8,8,1.5,3,15,'dark')+oct_z(8,8,2.4,0,4,'steel')+[box([6.2,6.2,-.1],[9.8,9.8,.1],'lamp'),box([7.4,9.3,8],[8.6,9.9,10],'red'),box([7.2,7.2,15],[8.8,8.8,15.6],'black')]
write('flashlight',el)
# GPS: rugged handheld with screen, buttons and stub antenna
el=[box([4.5,1,6.5],[11.5,13,9.5],'dark'),box([5.2,6,6.4],[10.8,12.2,6.5],'lens'),box([5.5,3,6.3],[7,4.2,6.5],'orange'),box([9,3,6.3],[10.5,4.2,6.5],'gray'),box([7.25,3,6.3],[8.75,4.2,6.5],'gray'),
    box([9.5,13,7.4],[10.7,17,8.6],'black'),box([4.3,1,6.3],[11.7,2,9.7],'black')]
write('gps',el)
# walkie-talkie: tall body, long antenna, speaker grille, small screen, orange PTT on the side
el=[box([5,0,6],[11,12,10],'black'),box([5.6,1,5.9],[10.4,6,6],'gray'),box([6,7.5,5.9],[10,10,6],'green'),box([9.2,12,7.2],[10.4,22,8.4],'dark'),box([9,22,7],[10.6,23,8.6],'red'),
    box([4.7,6,7],[5,9,9],'orange'),box([6,12,7],[7.5,13.2,8.5],'gray')]
write('walkie_talkie',el)
# thermal goggles: two glowing orange lenses in a dark frame on a black strap
el=[box([2,6,7],[14,10,9],'dark')]
for x in (5,11):el+=oct_z(x,8,2.3,5.6,7.2,'gray')+[box([x-1.5,6.5,5.4],[x+1.5,9.5,5.6],'orange')]
el+=[box([0,7,7.2],[2,9,16],'black'),box([14,7,7.2],[16,9,16],'black'),box([0,7,15],[16,9,16],'black'),box([7,10,7],[9,11.5,8.5],'red')]
GOG=dict(HAND,head={"translation":[0,-4.2,-0.5],"scale":[1.06,1.06,1.06]})
write('thermal_goggles',el,GOG)
# Danoninho: small pink pot with a foil lid
el=oct_y(8,8,3.4,0,5.5,'pink')+oct_y(8,8,3.7,5.5,6,'white')+[box([6,6,6],[10,6.2,10],'magenta')]
write('danoninho',el,dict(HAND,gui={"rotation":[25,-30,0],"translation":[0,-2,0],"scale":[1.6,1.6,1.6]}))
# tennis ball: rounded fuzzy-yellow ball with the white seam
el=[box([6,6,6],[10,10,10],'lime'),box([5.6,6.4,6.4],[10.4,9.6,9.6],'lime'),box([6.4,5.6,6.4],[9.6,10.4,9.6],'lime'),box([6.4,6.4,5.6],[9.6,9.6,10.4],'lime'),
    box([5.55,7.7,6],[10.45,8.3,10],'white',([8,8,8],'y',45))]
BALL=dict(HAND,gui={"rotation":[25,-30,0],"scale":[2.4,2.4,2.4]},ground={"translation":[0,0,0],"scale":[1,1,1]},fixed={"scale":[2,2,2]},
 thirdperson_righthand={"translation":[0,1,1.5],"scale":[1,1,1]},thirdperson_lefthand={"translation":[0,1,1.5],"scale":[1,1,1]},
 firstperson_righthand={"translation":[1,3,-1],"scale":[1.2,1.2,1.2]},firstperson_lefthand={"translation":[1,3,-1],"scale":[1.2,1.2,1.2]})
write('tennis_ball',el,BALL)
# racket: grip, throat, oval frame made of short straight segments, see-through strings
el=[box([7.1,-6,7.1],[8.9,4,8.9],'black'),box([7.3,4,7.4],[8.7,6,8.6],'white')]
el+=[box([6.4,5.5,7.5],[7.4,8.5,8.5],'red',([7,7,8],'z',-22.5)),box([8.6,5.5,7.5],[9.6,8.5,8.5],'red',([9,7,8],'z',22.5))]
cx,cy,rx,ry,n=8,14,5,6.5,20
for i in range(n):
    a0=2*math.pi*i/n;a1=2*math.pi*(i+1)/n
    x0,y0=cx+rx*math.cos(a0),cy+ry*math.sin(a0);x1,y1=cx+rx*math.cos(a1),cy+ry*math.sin(a1)
    mx,my=(x0+x1)/2,(y0+y1)/2;L=math.hypot(x1-x0,y1-y0)+.5;ang=math.degrees(math.atan2(y1-y0,x1-x0))
    ang=(ang+90)%180-90   # -90..90
    if abs(ang)<=45:best=min((-45,-22.5,0,22.5,45),key=lambda q:abs(q-ang));el.append(box([mx-L/2,my-.45,7.4],[mx+L/2,my+.45,8.6],'red',([mx,my,8],'z',best) if best else None))
    else:a2=ang-90 if ang>0 else ang+90;best=min((-45,-22.5,0,22.5,45),key=lambda q:abs(q-a2));el.append(box([mx-.45,my-L/2,7.4],[mx+.45,my+L/2,8.6],'red',([mx,my,8],'z',best) if best else None))
el.append({"from":[cx-rx,cy-ry,7.95],"to":[cx+rx,cy+ry,8.05],"faces":{"north":{"texture":"#strings","uv":[0,0,16,16]},"south":{"texture":"#strings","uv":[0,0,16,16]}}})
RACKET=dict(HAND,gui={"rotation":[0,0,-45],"translation":[0,0,0],"scale":[.75,.75,.75]},ground={"translation":[0,2,0],"scale":[.5,.5,.5]},
 thirdperson_righthand={"rotation":[0,90,0],"translation":[0,5,1],"scale":[.85,.85,.85]},thirdperson_lefthand={"rotation":[0,90,0],"translation":[0,5,1],"scale":[.85,.85,.85]},
 firstperson_righthand={"rotation":[0,90,25],"translation":[1.1,5,.8],"scale":[.7,.7,.7]},firstperson_lefthand={"rotation":[0,90,25],"translation":[1.1,5,.8],"scale":[.7,.7,.7]})
write('tennis_racket',el,RACKET,True)
# textures: string grid and net mesh (transparent holes)
def grid(path,step,col,band=None):
    im=Image.new('RGBA',(16,16),(0,0,0,0))
    for y in range(16):
        for x in range(16):
            if x%step==0 or y%step==0:im.putpixel((x,y),col)
    if band:
        for x in range(16):
            for y in range(2):im.putpixel((x,y),band)
    im.save(path)
grid(os.path.join(A,'textures/block/tennis_strings.png'),2,(240,240,235,255))
grid(os.path.join(A,'textures/block/tennis_net.png'),3,(30,30,34,255),(245,245,245,255))
# tennis net block: posts at both ends, mesh panel, white top tape (facing north: spans x)
NT=dict(T)
net=[box([0,0,7.5],[16,14,8.5],'net'),box([0,13,7.3],[16,14,8.7],'white'),box([0,0,7.3],[16,.6,8.7],'dark')]
json.dump({"textures":NT,"render_type":"minecraft:cutout","ambientocclusion":False,"elements":net},open(os.path.join(A,'models/block/tennis_net.json'),'w'),separators=(',',':'))
json.dump({"parent":"mareeletrica:block/tennis_net","display":{"gui":{"rotation":[30,225,0],"scale":[.625,.625,.625]},"ground":{"translation":[0,3,0],"scale":[.25,.25,.25]},"fixed":{"scale":[.5,.5,.5]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.375,.375,.375]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.4,.4,.4]}}},open(os.path.join(A,'models/item/tennis_net.json'),'w'))
json.dump({"variants":{f"facing={f}":({"model":"mareeletrica:block/tennis_net","y":y} if y else {"model":"mareeletrica:block/tennis_net"}) for f,y in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates/tennis_net.json'),'w'))
print('gadgets 3d ok')
