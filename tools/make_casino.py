"""0.57: casino machines (12 games + prize counter), chips, field kits."""
import json,os,math,random
from PIL import Image,ImageDraw
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
TB=os.path.join(A,'textures/block');TI=os.path.join(A,'textures/item');random.seed(7)
GAMES={"crash":0x2A6CFF,"roleta":0xC0182A,"blackjack":0x1F7A3A,"plinko":0xE0A020,"mines":0x3AA0A0,"dice":0x8A40D0,"limbo":0xFF6A20,"keno":0x2A9AE0,
 "baccarat":0x8A1F2A,"wheel":0xE8C020,"hilo":0x40B060,"andar_bahar":0xD04A90}
LABEL={"crash":"Crash","roleta":"Roleta","blackjack":"Blackjack","plinko":"Plinko","mines":"Mines","dice":"Dice","limbo":"Limbo","keno":"Keno","baccarat":"Baccarat",
 "wheel":"Wheel","hilo":"Hi-Lo","andar_bahar":"Andar Bahar"}
KEY={"crash":"minecraft:firework_rocket","roleta":"minecraft:clock","blackjack":"minecraft:paper","plinko":"minecraft:slime_ball","mines":"minecraft:tnt","dice":"minecraft:bone",
 "limbo":"minecraft:feather","keno":"minecraft:book","baccarat":"minecraft:map","wheel":"minecraft:compass","hilo":"minecraft:arrow","andar_bahar":"minecraft:gold_nugget"}
def rgb(c):return ((c>>16)&255,(c>>8)&255,c&255)
def body(name,c):
    im=Image.new('RGBA',(16,16));r,g,b=rgb(c)
    for y in range(16):
        for x in range(16):
            k=random.randint(-8,8);edge=x in(0,15) or y in(0,15)
            v=[max(0,min(255,int(t*(.55 if edge else .8)+k))) for t in (r,g,b)];im.putpixel((x,y),tuple(v)+(255,))
    im.save(os.path.join(TB,name+'.png'))
def marquee(name,c):
    im=Image.new('RGBA',(16,16),rgb(c)+(255,));d=ImageDraw.Draw(im)
    for i in range(0,16,3):d.point((i,1),fill=(255,240,150));d.point((i,14),fill=(255,240,150))
    d.rectangle([2,4,13,11],fill=tuple(min(255,v+60) for v in rgb(c)))
    im.save(os.path.join(TB,name+'.png'))
def screen(gid,c):
    S=32;im=Image.new('RGBA',(S,S),(10,12,20,255));d=ImageDraw.Draw(im);col=rgb(c);W=(240,240,240);R=(220,40,40);G=(60,200,90);Y=(240,200,50);K=(20,20,20)
    if gid=="crash":
        pts=[(3,28),(10,25),(16,20),(21,13),(26,5)];d.line(pts,fill=G,width=2);d.ellipse([24,2,30,8],fill=(255,120,40))
    elif gid=="roleta":
        for i in range(12):d.pieslice([3,3,29,29],i*30,i*30+30,fill=R if i%2 else K)
        d.ellipse([12,12,20,20],fill=(200,170,60))
    elif gid in("blackjack","baccarat"):
        d.rectangle([5,7,16,24],fill=W,outline=K);d.rectangle([14,9,25,26],fill=W,outline=K);d.text((7,8),"A",fill=K);d.text((16,10),"K",fill=R)
        if gid=="baccarat":d.ellipse([20,20,29,29],fill=Y,outline=K)
    elif gid=="plinko":
        for row in range(5):
            for i in range(row+2):d.point((16+(i-(row+1)/2)*5,6+row*5),fill=W)
        for s,cc in enumerate([R,Y,G,Y,R]):d.rectangle([4+s*5,28,8+s*5,31],fill=cc)
        d.ellipse([14,2,18,6],fill=(255,90,170))
    elif gid=="mines":
        for i in range(4):
            for j in range(4):d.rectangle([3+i*7,3+j*7,8+i*7,8+j*7],fill=(40,60,80))
        d.polygon([(12,13),(15,10),(18,13),(15,16)],fill=(80,220,255));d.ellipse([18,18,24,24],fill=K,outline=R)
    elif gid=="dice":
        for (x0,y0) in((4,8),(16,12)):
            d.rectangle([x0,y0,x0+11,y0+11],fill=W,outline=K)
            for (px,py) in((2,2),(8,8),(5,5)):d.rectangle([x0+px,y0+py,x0+px+1,y0+py+1],fill=K)
    elif gid=="limbo":
        d.polygon([(16,3),(26,15),(20,15),(20,28),(12,28),(12,15),(6,15)],fill=(255,120,40))
    elif gid=="keno":
        for i in range(4):
            for j in range(4):d.ellipse([3+i*7,3+j*7,8+i*7,8+j*7],fill=Y if (i*3+j)%5==0 else (60,140,220))
    elif gid=="wheel":
        cols=[R,Y,G,(60,120,230),(160,80,220),(240,140,40)]
        for i in range(18):d.pieslice([2,2,30,30],i*20,i*20+20,fill=cols[i%6])
        d.ellipse([12,12,20,20],fill=W);d.polygon([(14,0),(18,0),(16,5)],fill=W)
    elif gid=="hilo":
        d.rectangle([9,6,22,26],fill=W,outline=K);d.text((12,10),"7",fill=R)
        d.polygon([(26,8),(30,13),(22,13)],fill=G);d.polygon([(22,19),(30,19),(26,24)],fill=R)
    elif gid=="andar_bahar":
        d.rectangle([12,2,20,12],fill=Y,outline=K)
        for i in range(4):d.rectangle([3+i*7,15,8+i*7,22],fill=W,outline=K);d.rectangle([3+i*7,24,8+i*7,31],fill=W,outline=K)
    elif gid=="premios":
        d.rectangle([7,13,25,28],fill=R);d.rectangle([5,9,27,14],fill=(240,60,60));d.rectangle([15,9,17,28],fill=Y);d.ellipse([10,4,16,10],outline=Y);d.ellipse([16,4,22,10],outline=Y)
    im.save(os.path.join(TB,'casino_'+gid+'_screen.png'))
body('casino_panel',0x26282E)
for gid,c in GAMES.items():
    body('casino_'+gid+'_body',c);marquee('casino_'+gid+'_marquee',c);screen(gid,c)
def b(f,t,tex,rot=None,em=False):
    fc={d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}
    if em:
        for v in fc.values():v["neoforge_data"]={"block_light":15,"sky_light":15}
    e={"from":f,"to":t,"faces":fc}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2]}
    return e
for gid in GAMES:
    T={"body":"mareeletrica:block/casino_"+gid+"_body","screen":"mareeletrica:block/casino_"+gid+"_screen","marquee":"mareeletrica:block/casino_"+gid+"_marquee",
       "panel":"mareeletrica:block/casino_panel","r":"mareeletrica:block/lamp_red","g":"mareeletrica:block/lamp_green","y":"mareeletrica:block/lamp_yellow","particle":"mareeletrica:block/casino_"+gid+"_body"}
    els=[b([1,0,3],[15,11,15],'body'),b([1,11,5],[15,28,15],'body'),
         b([1,10,1],[15,12.5,9],'panel',([8,11,8],'x',-22.5)),
         b([3,12.4,2.5],[5,13.4,4.5],'r',([8,11,8],'x',-22.5),True),b([7,12.4,2.5],[9,13.4,4.5],'y',([8,11,8],'x',-22.5),True),b([11,12.4,2.5],[13,13.4,4.5],'g',([8,11,8],'x',-22.5),True),
         b([2,14,4.6],[14,27,5],'screen',None,True),
         b([1,28,3],[15,32,15],'marquee',None,True),b([0.5,0,2.5],[1.5,32,15.5],'panel'),b([14.5,0,2.5],[15.5,32,15.5],'panel')]
    if gid=="premios":
        els=[b([0,0,4],[16,14,16],'body'),b([0,14,3],[16,15,16],'panel'),b([2,15,10],[14,27,11],'screen',None,True),b([0,27,4],[16,32,16],'marquee',None,True),b([0,15,14],[1,27,16],'panel'),b([15,15,14],[16,27,16],'panel')]
    json.dump({"textures":T,"elements":els},open(os.path.join(A,'models/block/casino_'+gid+'.json'),'w'),separators=(',',':'))
    json.dump({"variants":{f"facing={f}":{"model":"mareeletrica:block/casino_"+gid,"y":r} for f,r in (("north",0),("east",90),("south",180),("west",270))}},open(os.path.join(A,'blockstates/casino_'+gid+'.json'),'w'))
    json.dump({"parent":"mareeletrica:block/casino_"+gid,"display":{"gui":{"rotation":[25,210,0],"translation":[0,-3,0],"scale":[.45,.45,.45]},"ground":{"scale":[.3,.3,.3]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.3,.3,.3]},"firstperson_righthand":{"rotation":[0,45,0],"scale":[.3,.3,.3]},"fixed":{"scale":[.5,.5,.5]}}},open(os.path.join(A,'models/item/casino_'+gid+'.json'),'w'))
    json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:casino_"+gid}],"conditions":[{"condition":"minecraft:survives_explosion"}]}]},open(os.path.join(D,'loot_table/blocks/casino_'+gid+'.json'),'w'))
    json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":["IGI","RXR","III"],"key":{"I":{"item":"minecraft:iron_ingot"},"G":{"item":"minecraft:glass_pane"},"R":{"item":"minecraft:redstone"},"X":{"item":KEY[gid]}},"result":{"id":"mareeletrica:casino_"+gid,"count":1}},open(os.path.join(D,'recipe/casino_'+gid+'.json'),'w'))
# chip
im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);d.ellipse([1,1,14,14],fill=(200,30,40),outline=(90,10,15));
for a in range(0,360,45):x=7.5+math.cos(math.radians(a))*5.5;y=7.5+math.sin(math.radians(a))*5.5;d.rectangle([x-1,y-1,x,y],fill=(250,250,250))
d.ellipse([4,4,11,11],fill=(240,220,120),outline=(150,110,30));d.text((5,3),"M",fill=(120,20,20))
im.save(os.path.join(TI,'casino_chip.png'))
json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/casino_chip"}},open(os.path.join(A,'models/item/casino_chip.json'),'w'))
for item,n in (("minecraft:gold_ingot",4),("minecraft:emerald",6),("minecraft:diamond",16)):
    json.dump({"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":item}],"result":{"id":"mareeletrica:casino_chip","count":n}},open(os.path.join(D,'recipe/casino_chip_from_'+item.split(':')[1]+'.json'),'w'))
# field kits: item icon = a little field
KITS={"futebol":((70,150,60),"mareeletrica:soccer_ball","minecraft:grass_block"),"basquete":((150,100,60),"mareeletrica:basketball","minecraft:spruce_planks"),
      "volei":((220,200,140),"mareeletrica:volleyball","minecraft:sand"),"tenis":((60,140,80),"mareeletrica:tennis_ball","minecraft:green_concrete"),"galos":((140,140,140),"mareeletrica:rooster_ring","minecraft:spruce_stairs")}
for k,(c,ball,floor) in KITS.items():
    im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);d.rectangle([1,3,14,12],fill=c,outline=(250,250,250));d.line([7,3,7,12],fill=(250,250,250))
    if k in("futebol","basquete"):d.rectangle([0,6,1,9],fill=(250,250,250));d.rectangle([14,6,15,9],fill=(250,250,250))
    if k in("volei","tenis"):d.line([7,2,7,13],fill=(40,40,40))
    if k=="galos":d.rectangle([4,5,11,10],fill=(230,225,210),outline=(200,30,30))
    im.save(os.path.join(TI,'field_kit_'+k+'.png'))
    json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/field_kit_"+k}},open(os.path.join(A,'models/item/field_kit_'+k+'.json'),'w'))
    json.dump({"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":ball},{"item":floor},{"item":"minecraft:white_concrete"},{"item":"minecraft:iron_ingot"}],"result":{"id":"mareeletrica:field_kit_"+k,"count":1}},open(os.path.join(D,'recipe/field_kit_'+k+'.json'),'w'))
# the prize counter was removed (chips buy nothing): clean up its files
for p in ('models/block/casino_premios.json','models/item/casino_premios.json','blockstates/casino_premios.json','textures/block/casino_premios_body.png','textures/block/casino_premios_screen.png','textures/block/casino_premios_marquee.png'):
    q=os.path.join(A,p)
    if os.path.exists(q):os.remove(q)
for p in ('loot_table/blocks/casino_premios.json','recipe/casino_premios.json'):
    q=os.path.join(D,p)
    if os.path.exists(q):os.remove(q)
# lang
for lang in('pt_br','en_us'):
    p=os.path.join(A,'lang',lang+'.json');j=json.load(open(p));j.pop('block.mareeletrica.casino_premios',None)
    for gid,l in LABEL.items():j['block.mareeletrica.casino_'+gid]=(("Máquina " if gid!="premios" else "")+l) if lang=='pt_br' else l+(" Machine" if gid!="premios" else "")
    j['item.mareeletrica.casino_chip']='Ficha do Cassino' if lang=='pt_br' else 'Casino Chip'
    json.dump(j,open(p,'w'),ensure_ascii=False,indent=1)
# pickaxe tag
t=os.path.join(ROOT,'src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json');j=json.load(open(t))
for gid in GAMES:
    n='mareeletrica:casino_'+gid
    if n not in j['values']:j['values'].append(n)
if 'mareeletrica:casino_premios' in j['values']:j['values'].remove('mareeletrica:casino_premios')
json.dump(j,open(t,'w'),indent=1)
print('casino ok')
