"""0.65: wounds system assets — breathing / heartbeat / bandage / syringe sounds (synthesised), screen blood, wound decals,
medical items, bulletproof vest (icon + armour layer), item models, recipes, damage type, lang."""
import json,os,subprocess,random,math,tempfile,wave
import numpy as np
from PIL import Image,ImageDraw,ImageFilter
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES=os.path.join(ROOT,'src/main/resources');A=os.path.join(RES,'assets/mareeletrica');D=os.path.join(RES,'data/mareeletrica')
SR=44100;rng=np.random.default_rng(65);R=random.Random(65)
# ---------------- sounds ----------------
def band(x,lo,hi):
    X=np.fft.rfft(x);f=np.fft.rfftfreq(len(x),1/SR);X[(f<lo)|(f>hi)]=0;return np.fft.irfft(X,len(x))
def env(n,a,peak,r):   # attack fraction, peak position, release shape
    t=np.linspace(0,1,n);e=np.where(t<peak,(t/peak)**a,((1-t)/(1-peak))**r);return e
def breath(dur,lo,hi,amp,a=1.6,peak=.45,r=1.4,rasp=0.0):
    n=int(dur*SR);x=band(rng.standard_normal(n),lo,hi);x/=np.abs(x).max()+1e-9
    if rasp>0:
        t=np.arange(n)/SR;x=x*(1+rasp*np.sin(2*np.pi*38*t)*rng.uniform(.6,1))
    return x*env(n,a,peak,r)*amp
def silence(d):return np.zeros(int(d*SR))
def write(name,x):
    x=np.clip(x/ (np.abs(x).max()+1e-9)*.9,-1,1);pcm=(x*32767).astype(np.int16)
    tmp=tempfile.mktemp(suffix='.wav')
    with wave.open(tmp,'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(SR);w.writeframes(pcm.tobytes())
    out=os.path.join(A,'sounds',name+'.ogg');subprocess.run(['ffmpeg','-y','-loglevel','error','-i',tmp,'-c:a','libvorbis','-q:a','4',out],check=True);os.remove(tmp)
def vol(x,v):return x*v
# calm: slow inhale through the nose, soft exhale (≈4 s)
write('breath_calm',np.concatenate([breath(1.3,500,2600,.5,peak=.6),silence(.35),breath(1.6,300,1800,.38,peak=.3,r=2),silence(.75)]))
# heavy: faster, louder, mouth breathing (≈2.4 s)
write('breath_heavy',np.concatenate([breath(.75,350,3200,.85,peak=.55,rasp=.25),silence(.12),breath(.95,250,2400,.75,peak=.25,r=2.2,rasp=.2),silence(.55)]))
# gasp: short ragged gasps, trembling (≈1.7 s)
g1=breath(.38,600,4200,1,a=.8,peak=.35,rasp=.6);g2=breath(.55,300,2600,.7,peak=.2,r=2.5,rasp=.5)
write('breath_gasp',np.concatenate([g1,silence(.05),breath(.3,600,4000,.8,a=.8,peak=.3,rasp=.6),silence(.08),g2,silence(.3)]))
# heartbeat: lub-dub (1 s)
def thump(d,f,amp):
    n=int(d*SR);t=np.arange(n)/SR;return np.sin(2*np.pi*f*t*(1-.3*t/d))*np.exp(-t*22)*amp
write('heartbeat',np.concatenate([thump(.16,55,1),silence(.08),thump(.14,48,.7),silence(.62)]))
# bandage: cloth rustle / wrap bursts (1.6 s)
parts=[]
for i in range(6):parts+= [breath(R.uniform(.12,.22),1200,7000,R.uniform(.5,.9),a=.6,peak=.2,r=1.2),silence(R.uniform(.04,.12))]
write('bandage_wrap',np.concatenate(parts))
# syringe: cap click, then a thin hiss (1 s)
click=band(rng.standard_normal(int(.02*SR)),2000,9000)*np.exp(-np.arange(int(.02*SR))/SR*300)
write('syringe_inject',np.concatenate([click,silence(.18),breath(.6,3500,9000,.35,a=.5,peak=.15,r=1.2),silence(.2)]))
snd=json.load(open(os.path.join(A,'sounds.json')))
for k in ['breath_calm','breath_heavy','breath_gasp','heartbeat','bandage_wrap','syringe_inject']:
    snd[k]={"subtitle":"subtitles.mareeletrica."+k,"sounds":[{"name":"mareeletrica:"+k,"stream":False}]}
json.dump(snd,open(os.path.join(A,'sounds.json'),'w'))
# ---------------- screen textures ----------------
T=os.path.join(A,'textures');os.makedirs(os.path.join(T,'gui/wounds'),exist_ok=True)
def vignette():
    W=256;im=Image.new('RGBA',(W,W),(0,0,0,0));px=im.load()
    noise=Image.effect_noise((W,W),60).filter(ImageFilter.GaussianBlur(6))
    for y in range(W):
        for x in range(W):
            dx=(x-W/2)/(W/2);dy=(y-W/2)/(W/2);d=math.sqrt(dx*dx*.9+dy*dy*1.1)
            n=noise.getpixel((x,y))/255.0
            a=max(0,min(1,(d-.45+ (n-.5)*.35)/.55))**1.3
            px[x,y]=(int(120+60*n),0,0,int(a*235))
    im.save(os.path.join(T,'gui/wounds/vignette.png'))
vignette()
def splat(i):
    W=96;im=Image.new('RGBA',(W,W),(0,0,0,0));d=ImageDraw.Draw(im)
    c=(W/2,W/2);col=(110,0,0,230)
    d.ellipse([c[0]-14,c[1]-14,c[0]+14,c[1]+14],fill=col)
    for k in range(R.randint(10,16)):
        ang=R.uniform(0,2*math.pi);dist=R.uniform(12,40);r=R.uniform(2,7)
        x=c[0]+math.cos(ang)*dist;y=c[1]+math.sin(ang)*dist;d.ellipse([x-r,y-r,x+r,y+r],fill=(R.randint(95,140),0,0,R.randint(170,230)))
        d.line([c,(x,y)],fill=col,width=int(r))
    for k in range(4):   # drips
        x=c[0]+R.uniform(-10,10);d.line([(x,c[1]),(x,c[1]+R.uniform(18,40))],fill=(100,0,0,210),width=R.randint(2,4))
    im=im.filter(ImageFilter.GaussianBlur(.8));im.save(os.path.join(T,'gui/wounds/splat_%d.png'%i))
for i in range(3):splat(i)
# ---------------- wound decals on the body (bullet hole + blood) ----------------
os.makedirs(os.path.join(T,'entity'),exist_ok=True)
def wound(i):
    W=16;im=Image.new('RGBA',(W,W),(0,0,0,0));d=ImageDraw.Draw(im)
    for k in range(9):
        x=R.uniform(3,13);y=R.uniform(3,13);r=R.uniform(.8,2.4);d.ellipse([x-r,y-r,x+r,y+r],fill=(R.randint(110,150),R.randint(0,12),R.randint(0,12),R.randint(180,235)))
    d.ellipse([5,5,11,11],fill=(140,8,8,240));d.ellipse([6.5,6.5,9.5,9.5],fill=(35,0,0,255))
    for k in range(2):x=R.uniform(6,10);d.line([(x,10),(x,10+R.uniform(3,5))],fill=(120,5,5,220),width=1)
    im.save(os.path.join(T,'entity/wound_%d.png'%i))
for i in range(3):wound(i)
# ---------------- item icons ----------------
def icon(name,draw):
    im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);draw(im,d);im.save(os.path.join(T,'item',name+'.png'))
def bandage(im,d):
    d.ellipse([2,4,13,13],fill=(236,232,220,255),outline=(170,165,150,255))
    d.ellipse([5,7,10,10],fill=(205,200,185,255),outline=(150,145,130,255))
    d.rectangle([11,9,15,12],fill=(236,232,220,255),outline=(170,165,150,255))
    d.line([(4,6),(11,6)],fill=(255,255,255,255));im.putpixel((13,10),(190,30,30,255))
def syringe(im,d):
    for i in range(9):   # diagonal barrel
        x=3+i;y=12-i;d.rectangle([x-1,y-1,x+1,y+1],fill=(225,240,250,255))
    for i in range(1,7):x=4+i;y=11-i;im.putpixel((x,y),(230,60,60,255))
    d.line([(11,4),(14,1)],fill=(200,200,210,255));d.line([(1,12),(4,15)],fill=(80,80,90,255));d.line([(2,11),(4,13)],fill=(80,80,90,255))
    d.line([(1,14),(3,12)],fill=(120,120,130,255))
def medkit(im,d):
    d.rounded_rectangle([1,4,14,14],radius=2,fill=(210,40,40,255),outline=(140,20,20,255));d.rectangle([5,2,10,4],outline=(90,90,90,255))
    d.rectangle([6,6,9,12],fill=(250,250,250,255));d.rectangle([4,8,11,10],fill=(250,250,250,255))
def vest(im,d):
    d.polygon([(3,1),(6,1),(8,3),(10,1),(13,1),(14,4),(14,14),(2,14),(2,4)],fill=(64,72,52,255),outline=(38,42,30,255))
    d.rectangle([4,7,7,10],fill=(48,54,40,255),outline=(30,34,24,255));d.rectangle([9,7,12,10],fill=(48,54,40,255),outline=(30,34,24,255))
    d.line([(2,12),(14,12)],fill=(30,34,24,255));d.rectangle([7,4,9,5],fill=(200,200,200,255))
icon('bandage',bandage);icon('syringe',syringe);icon('medkit',medkit);icon('bulletproof_vest',vest)
for n in ['bandage','syringe','medkit','bulletproof_vest']:
    json.dump({"parent":"minecraft:item/generated","textures":{"layer0":"mareeletrica:item/"+n}},open(os.path.join(A,'models/item',n+'.json'),'w'))
# ---------------- vest armour layer (body only; arms stay transparent = sleeveless) ----------------
os.makedirs(os.path.join(T,'models/armor'),exist_ok=True)
L=Image.new('RGBA',(64,32),(0,0,0,0));d=ImageDraw.Draw(L)
base=(64,72,52,255);dark=(42,47,34,255);strap=(30,32,26,255)
for box in [(20,20,27,31),(32,20,39,31),(16,20,19,31),(28,20,31,31),(20,16,27,19),(28,16,35,19)]:d.rectangle(box,fill=base)
for x in range(16,40):
    for y in range(16,32):
        p=L.getpixel((x,y))
        if p[3]:k=R.randint(-6,6);L.putpixel((x,y),(p[0]+k,p[1]+k,p[2]+k,255))
d.rectangle([21,24,23,27],fill=dark,outline=strap);d.rectangle([24,24,26,27],fill=dark,outline=strap)   # front mag pouches
d.line([(20,29),(27,29)],fill=strap);d.line([(32,29),(39,29)],fill=strap);d.line([(16,25),(19,25)],fill=strap);d.line([(28,25),(31,25)],fill=strap)
d.rectangle([22,21,25,22],fill=(205,205,205,255))   # name tape
L.save(os.path.join(T,'models/armor/bulletproof_layer_1.png'))
# ---------------- recipes ----------------
def shaped(name,pat,key,n=1):
    json.dump({"type":"minecraft:crafting_shaped","category":"equipment","pattern":pat,"key":{k:{"item":v} for k,v in key.items()},"result":{"id":"mareeletrica:"+name,"count":n}},open(os.path.join(D,'recipe',name+'.json'),'w'))
shaped('bandage',["SPS"],{"S":"minecraft:string","P":"minecraft:paper"},2)
shaped('syringe',["  N"," G ","S  "],{"N":"minecraft:iron_nugget","G":"minecraft:glass_bottle","S":"minecraft:sugar"},2)
shaped('medkit',["WRW","BSB","WWW"],{"W":"minecraft:white_wool","R":"minecraft:red_dye","B":"mareeletrica:bandage","S":"mareeletrica:syringe"})
shaped('bulletproof_vest',["P P","PLP","PLP"],{"P":"mareeletrica:iron_plate","L":"minecraft:leather"})
# ---------------- bleeding damage type (+ ignores armour) ----------------
os.makedirs(os.path.join(D,'damage_type'),exist_ok=True)
json.dump({"message_id":"mareeletrica.bleeding","scaling":"never","exhaustion":0.0},open(os.path.join(D,'damage_type','bleeding.json'),'w'))
for tag in ['bypasses_armor','bypasses_effects','bypasses_resistance','bypasses_enchantments']:
    p=os.path.join(RES,'data/minecraft/tags/damage_type');os.makedirs(p,exist_ok=True);f=os.path.join(p,tag+'.json')
    cur=json.load(open(f)) if os.path.exists(f) else {"replace":False,"values":[]}
    if "mareeletrica:bleeding" not in cur["values"]:cur["values"].append("mareeletrica:bleeding")
    json.dump(cur,open(f,'w'))
# ---------------- lang ----------------
LANG={'pt_br':{"item.mareeletrica.bandage":"Bandagem","item.mareeletrica.syringe":"Seringa de adrenalina","item.mareeletrica.medkit":"Kit médico","item.mareeletrica.bulletproof_vest":"Colete à prova de balas",
  "death.attack.mareeletrica.bleeding":"%1$s sangrou até a morte","death.attack.mareeletrica.bleeding.player":"%1$s sangrou até a morte por causa de %2$s",
  "subtitles.mareeletrica.breath_calm":"Respiração","subtitles.mareeletrica.breath_heavy":"Respiração ofegante","subtitles.mareeletrica.breath_gasp":"Respiração desesperada","subtitles.mareeletrica.heartbeat":"Coração batendo",
  "subtitles.mareeletrica.bandage_wrap":"Enrolando bandagem","subtitles.mareeletrica.syringe_inject":"Seringa aplicada"},
 'en_us':{"item.mareeletrica.bandage":"Bandage","item.mareeletrica.syringe":"Adrenaline Syringe","item.mareeletrica.medkit":"Medical Kit","item.mareeletrica.bulletproof_vest":"Bulletproof Vest",
  "death.attack.mareeletrica.bleeding":"%1$s bled to death","death.attack.mareeletrica.bleeding.player":"%1$s bled to death because of %2$s",
  "subtitles.mareeletrica.breath_calm":"Breathing","subtitles.mareeletrica.breath_heavy":"Heavy breathing","subtitles.mareeletrica.breath_gasp":"Gasping","subtitles.mareeletrica.heartbeat":"Heartbeat",
  "subtitles.mareeletrica.bandage_wrap":"Bandage wrapped","subtitles.mareeletrica.syringe_inject":"Syringe injected"}}
for lang,kv in LANG.items():
    p=os.path.join(A,'lang',lang+'.json');dd=json.load(open(p,encoding='utf-8'));dd.update(kv);json.dump(dd,open(p,'w',encoding='utf-8'),ensure_ascii=False,indent=1)
print('0.65 assets ok')
