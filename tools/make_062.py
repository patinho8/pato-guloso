"""0.62.x: Robo Catador (original cleaning robot: three wheels, white/orange shell, cyan visor strip, antenna lamp, front scoop)
and the compacted trash cube it presses."""
import json,os,copy,random
from PIL import Image,ImageDraw
R=os.path.join(os.path.dirname(os.path.abspath(__file__)),'..','src','main','resources')
A=os.path.join(R,'assets','mareeletrica');D=os.path.join(R,'data')
M=os.path.join(A,'models');T=os.path.join(A,'textures')
def save(p,d):json.dump(d,open(os.path.join(M,p+'.json'),'w'))
FACES=['north','south','east','west','up','down']
def box(f,t,tex,faces=None,rot=None):
    e={"from":[round(x,3) for x in f],"to":[round(x,3) for x in t],"faces":{k:{"texture":'#'+(faces.get(k,tex) if faces else tex),"uv":[0,0,16,16]} for k in FACES}}
    if rot:e["rotation"]=rot
    return e
def sq(cx,cz,side,y0,y1,tex,rot45=False,faces=None):
    h=side/2;e=box((cx-h,y0,cz-h),(cx+h,y1,cz+h),tex,faces)
    if rot45:e["rotation"]={"origin":[cx,(y0+y1)/2,cz],"axis":"y","angle":45,"rescale":False}
    return e
rnd=random.Random(62)
def png(name,draw):
    im=Image.new('RGBA',(16,16),(0,0,0,255));d=ImageDraw.Draw(im);draw(im,d);im.save(os.path.join(T,'block',name+'.png'))
def noise(im,base,amt):
    for x in range(16):
        for y in range(16):
            k=rnd.randint(-amt,amt);im.putpixel((x,y),tuple(max(0,min(255,c+k)) for c in base)+(255,))
def white(im,d):
    noise(im,(232,232,226),5);d.line([0,7,15,7],fill=(196,198,196,255));d.line([7,0,7,6],fill=(196,198,196,255))
    for p in [(2,2),(13,2),(2,13),(13,13)]:im.putpixel(p,(170,172,170,255))
def orange(im,d):
    noise(im,(238,138,36),6)
    for x in range(0,16,4):d.line([x,15,x+3,12],fill=(206,106,20,255))
def dark(im,d):noise(im,(50,52,56),5)
def tire(im,d):
    noise(im,(34,34,36),4)
    for y in range(0,16,3):d.line([0,y,15,y],fill=(22,22,24,255))
def visor(color):
    def f(im,d):
        r,g,b=color
        for y in range(16):
            k=1-abs(y-7.5)/9;d.line([0,y,15,y],fill=(int(r*.25+r*.55*k),int(g*.25+g*.55*k),int(b*.25+b*.55*k),255))
        d.line([1,7,14,7],fill=(min(255,r+60),min(255,g+60),min(255,b+60),255));d.line([1,8,14,8],fill=(r,g,b,255))
        for x in range(2,15,4):im.putpixel((x,4),(min(255,r+40),min(255,g+40),min(255,b+40),255))
    return f
def lamp(im,d):
    d.rectangle([0,0,15,15],fill=(255,170,40,255));d.ellipse([3,3,12,12],fill=(255,214,110,255));d.ellipse([6,5,9,8],fill=(255,248,210,255))
def hatch(im,d):
    d.rectangle([0,0,15,15],fill=(44,46,50,255))
    for x in range(-16,16,5):d.polygon([(x,15),(x+3,15),(x+9,9),(x+6,9)],fill=(236,196,40,255))
    d.rectangle([0,0,15,8],fill=(44,46,50,255));d.rectangle([1,1,14,7],fill=(64,66,72,255));d.line([2,4,13,4],fill=(30,30,34,255))
def trash(im,d):
    pal=[(110,84,58),(92,92,90),(70,96,52),(130,110,80),(80,70,62),(120,120,116),(98,64,44),(150,140,120)]
    for x in range(0,16,2):
        for y in range(0,16,2):
            c=rnd.choice(pal);d.rectangle([x,y,x+1,y+1],fill=c+(255,))
    for y in (3,12):d.rectangle([0,y,15,y+1],fill=(150,154,160,255));d.line([0,y+1,15,y+1],fill=(96,100,106,255))
    for x in (0,15):
        for y in (3,12):im.putpixel((x,y),(200,204,210,255))
for n,f in [('robot_white',white),('robot_orange',orange),('robot_dark',dark),('robot_tire',tire),('robot_visor',visor((70,220,240))),
            ('robot_visor_red',visor((250,70,60))),('robot_lamp',lamp),('robot_hatch',hatch),('trash_cube',trash)]:png(n,f)

TX={'w':'mareeletrica:block/robot_white','o':'mareeletrica:block/robot_orange','k':'mareeletrica:block/robot_dark','t':'mareeletrica:block/robot_tire',
    'h':'mareeletrica:block/robot_hatch','particle':'mareeletrica:block/robot_white'}
# ---- body (front = -z): star-octagon chassis, orange band, square head, antenna pole, back hatch
BODY=[sq(8,8,11,3,9,'w'),sq(8,8,9.1,3,9,'w',True),
      sq(8,8,11.4,5.2,6.4,'o'),sq(8,8,9.5,5.2,6.4,'o',True),
      sq(8,8,9,2,3,'k'),
      box((4.5,9,5),(11.5,13,11),'w'),box((4.2,9,5.6),(11.8,9.6,10.4),'o'),box((5.2,13,5.6),(10.8,13.8,10.4),'w'),
      box((3.8,10.2,6.6),(4.5,12,9.4),'o'),box((11.5,10.2,6.6),(12.2,12,9.4),'o'),
      box((7.7,13.8,7.7),(8.3,17,8.3),'k'),
      box((5,4,13.4),(11,8.4,13.9),'h',{'north':'k','up':'k','down':'k','east':'k','west':'k','south':'h'}),
      # side guards over the rear wheels
      box((0.3,4.2,8.6),(2.7,4.8,13.4),'o'),box((13.3,4.2,8.6),(15.7,4.8,13.4),'o'),
      # caster fork
      box((7.1,2.2,3.4),(7.4,3.6,4.6),'k'),box((8.6,2.2,3.4),(8.9,3.6,4.6),'k')]
save('block/robot_body',{"textures":TX,"elements":BODY})
save('block/robot_visor',{"textures":{'v':'mareeletrica:block/robot_visor','particle':'mareeletrica:block/robot_visor'},"elements":[box((5,10.3,4.7),(11,12,5.05),'v')]})
save('block/robot_visor_red',{"textures":{'v':'mareeletrica:block/robot_visor_red','particle':'mareeletrica:block/robot_visor_red'},"elements":[box((5,10.3,4.7),(11,12,5.05),'v')]})
save('block/robot_lamp',{"textures":{'l':'mareeletrica:block/robot_lamp','particle':'mareeletrica:block/robot_lamp'},"elements":[box((7.2,17,7.2),(8.8,18.6,8.8),'l')]})
# wheel centred on (8,2,8), radius 2; the renderer moves it to each side and spins it
WHEEL=[box((7,0,6),(9,4,10),'t',{'east':'k','west':'k','north':'t','south':'t','up':'t','down':'t'}),
       dict(box((7,0,6),(9,4,10),'t',{'east':'k','west':'k','north':'t','south':'t','up':'t','down':'t'}),rotation={"origin":[8,2,8],"axis":"x","angle":45,"rescale":False}),
       box((6.8,1.3,7.3),(9.2,2.7,8.7),'o')]
save('block/robot_wheel',{"textures":TX,"elements":WHEEL})
CASTER=[box((7.4,0,3.2),(8.6,2.4,4.8),'t'),dict(box((7.4,0,3.2),(8.6,2.4,4.8),'t'),rotation={"origin":[8,1.2,4],"axis":"x","angle":45,"rescale":False})]
save('block/robot_caster',{"textures":TX,"elements":CASTER})
# front scoop on two arms, pivot at (8,6,3)
SCOOP=[box((4.4,3.2,1.6),(5.2,6.4,3.2),'k'),box((10.8,3.2,1.6),(11.6,6.4,3.2),'k'),
       box((4,1.2,-0.6),(12,1.7,2.4),'o'),box((4,1.2,2),(12,3.6,2.5),'o'),box((4,1.2,-0.6),(4.5,2.8,2.4),'o'),box((11.5,1.2,-0.6),(12,2.8,2.4),'o')]
save('block/robot_scoop',{"textures":TX,"elements":SCOOP})

# ---- item: the whole robot
def moved(els,dx=0,dy=0,dz=0):
    out=[]
    for e in copy.deepcopy(els):
        e['from']=[e['from'][0]+dx,e['from'][1]+dy,e['from'][2]+dz];e['to']=[e['to'][0]+dx,e['to'][1]+dy,e['to'][2]+dz]
        if 'rotation' in e:o=e['rotation']['origin'];e['rotation']['origin']=[o[0]+dx,o[1]+dy,o[2]+dz]
        out.append(e)
    return out
tx=dict(TX);tx.update({'v':'mareeletrica:block/robot_visor','l':'mareeletrica:block/robot_lamp'})
EL=BODY+moved(WHEEL,-6.5,0,3)+moved(WHEEL,6.5,0,3)+CASTER+SCOOP+[box((5,10.3,4.7),(11,12,5.05),'v'),box((7.2,17,7.2),(8.8,18.6,8.8),'l')]
save('item/catador_robot',{"textures":tx,"elements":EL,"display":{
 "gui":{"rotation":[25,155,0],"translation":[0,-1.2,0],"scale":[0.78,0.78,0.78]},"ground":{"translation":[0,2,0],"scale":[0.4,0.4,0.4]},
 "fixed":{"rotation":[0,180,0],"scale":[0.7,0.7,0.7]},"thirdperson_righthand":{"rotation":[75,180,0],"translation":[0,2.5,1],"scale":[0.4,0.4,0.4]},
 "thirdperson_lefthand":{"rotation":[75,180,0],"translation":[0,2.5,1],"scale":[0.4,0.4,0.4]},
 "firstperson_righthand":{"rotation":[0,160,0],"translation":[1,1,0],"scale":[0.45,0.45,0.45]},"firstperson_lefthand":{"rotation":[0,160,0],"translation":[1,1,0],"scale":[0.45,0.45,0.45]}}})

# ---- trash cube block
save('block/trash_cube',{"parent":"minecraft:block/cube_all","textures":{"all":"mareeletrica:block/trash_cube"}})
save('item/trash_cube',{"parent":"mareeletrica:block/trash_cube"})
json.dump({"variants":{"":{"model":"mareeletrica:block/trash_cube"}}},open(os.path.join(A,'blockstates','trash_cube.json'),'w'))
json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:trash_cube"}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},
          open(os.path.join(D,'mareeletrica','loot_table','blocks','trash_cube.json'),'w'))
os.makedirs(os.path.join(D,'neoforge','data_maps','item'),exist_ok=True)
json.dump({"values":{"mareeletrica:trash_cube":{"burn_time":1200}}},open(os.path.join(D,'neoforge','data_maps','item','furnace_fuels.json'),'w'),indent=1)
# ---- recipe: iron shell, glass visor, redstone, a chest inside, a piston for the press
json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":["IGI","RCR","IPI"],
 "key":{"I":{"item":"minecraft:iron_ingot"},"G":{"item":"minecraft:light_blue_stained_glass_pane"},"R":{"item":"minecraft:redstone"},"C":{"item":"minecraft:chest"},"P":{"item":"minecraft:piston"}},
 "result":{"id":"mareeletrica:catador_robot","count":1}},open(os.path.join(D,'mareeletrica','recipe','catador_robot.json'),'w'))
# ---- names
for lang,n in {'pt_br':{'entity.mareeletrica.catador_robot':'Robô Catador','item.mareeletrica.catador_robot':'Robô Catador','block.mareeletrica.trash_cube':'Cubo de Lixo Compactado'},
               'en_us':{'entity.mareeletrica.catador_robot':'Scavenger Robot','item.mareeletrica.catador_robot':'Scavenger Robot','block.mareeletrica.trash_cube':'Compacted Trash Cube'}}.items():
    p=os.path.join(A,'lang',lang+'.json');d=json.load(open(p,encoding='utf-8'));d.update(n);json.dump(d,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('robot ok')
