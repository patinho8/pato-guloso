"""0.75.0: shared tools to remodel the arsenal guns piece by piece (the M4A1/Desert Eagle way) and wire them into the mod.
Each gun script (tools/forge/<gun>.py) builds a Gun (a bbkit.Kit: cano para +x, y para cima, 1 unidade = 5 mm) and calls finish()."""
import json,math,os,re,subprocess,sys
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))
from bbkit import Kit
T=os.path.dirname(os.path.abspath(__file__));ROOT=os.path.dirname(T)
THEMES={  # skin: (main, accent, mag) — the same 13 finishes as the M4A1 / Desert Eagle
 1:((214,170,64),(40,24,14),(200,156,52)),2:((22,58,140),(200,160,80),(18,46,112)),3:((236,236,232),(40,90,150),(224,224,220)),
 4:((34,30,34),(150,20,30),(26,22,26)),5:((80,50,140),(40,200,210),(60,36,110)),6:((196,170,120),(110,88,58),(170,146,100)),
 7:((228,128,32,'stripes'),(26,22,20),(228,128,32)),8:((92,104,112,'camo'),(60,70,60,'camo'),(100,112,120)),9:((40,41,45,'check'),(24,24,26),(44,45,49)),
 10:((120,122,128,'stripes'),(50,36,26),(130,132,138)),11:((26,31,37),(40,200,220),(20,24,28)),12:((60,10,110,'dots'),(209,61,240,'dots'),(40,8,80))}
class Gun(Kit):
    def __init__(self,family,swatches,seed=7,brushed=('steel','polish')):
        super().__init__(family+'_forge',swatches,seed=seed,brushed=brushed);self.family=family
    def cube(self,group,name,f,t,mat,rot=None,origin=None,faces=None):
        if name in('punho','punho_armacao') and rot and rot[2]:
            # raked grip: tamer rake, a little taller (hidden inside the frame) and a solid web joining it to the frame
            a=rot[2];top=t[1];t=[t[0],t[1]+2.2,t[2]]
            sh=math.tan(math.radians(a))*3.0
            super().cube(group,'punho_juncao',[f[0]+min(0,sh)+.2,top-3.0,f[2]+.1],[t[0]+max(0,sh)-.2,top+.4,t[2]-.1],mat)
        return super().cube(group,name,f,t,mat,rot,origin,faces)
    # ---------- common parts ----------
    def rail(self,g,x0,x1,y,w=2.15,mat='steel',pitch=1.0):
        """Picatinny on top: base + teeth"""
        self.cube(g,'trilho_base',[x0,y,-w+.3],[x1,y+.7,w-.3],mat);x=x0+.25
        while x+.55<x1:self.cube(g,'trilho_dente',[x,y+.7,-w],[x+.55,y+1.55,w],mat);self.cube(g,'trilho_chanfro',[x,y+1.55,-w+.35],[x+.55,y+1.75,w-.35],mat);x+=pitch
        return y+1.75
    def side_rail(self,g,x0,x1,y,z,mat='steel',s=1):
        """short rail on a side (s=+1 right/+z, -1 left)"""
        a,b=(z,z+.6) if s>0 else (z-.6,z);self.cube(g,'trilho_lat',[x0,y-1.4,a],[x1,y+1.4,b],mat)
        x=x0+.2
        while x+.5<x1:
            a2,b2=(z+.6,z+1.2) if s>0 else (z-1.2,z-.6);self.cube(g,'trilho_lat_dente',[x,y-1.7,a2],[x+.5,y+1.7,b2],mat);x+=1.0
    def screw(self,g,x,y,z,r=.45,mat='polish',both=True):
        if both:self.both(g,'parafuso',x-r,y-r,x+r,y+r,z,z+.12,mat)
        else:self.cube(g,'parafuso',[x-r,y-r,z],[x+r,y+r,z+.12],mat)
    def serr(self,g,x0,x1,y0,y1,z,n,mat='dark',both=True,depth=.08):
        """n vertical serrations between x0..x1 on the side at |z|"""
        w=(x1-x0)/(2*n)
        for k in range(n):
            xa=x0+k*2*w+w*.5
            if both:self.both(g,'serrilha',xa,y0,xa+w,y1,z-depth,z+.02,mat)
            else:self.cube(g,'serrilha',[xa,y0,z-depth],[xa+w,y1,z+.02],mat)
    def text(self,g,x,y,z,widths,h=1.2,mat='engrave',s=1):
        """engraving: little bars (letters) on the side z (s=+1 faces +z)"""
        for w in widths:
            a,b=(z,z+.06) if s>0 else (z-.06,z);self.cube(g,'gravacao',[x,y,a],[x+w,y+h,b],mat);x+=w+.25
        return x
    def checker(self,g,x0,y0,nx,ny,step,z,mat='rubber',rot=None,origin=None,both=True,size=.55):
        for i in range(nx):
            for j in range(ny):
                f=[x0+i*step,y0+j*step];t=[f[0]+step*size,f[1]+step*size]
                if both:self.both(g,'quadriculado',f[0],f[1],t[0],t[1],z,z+.2,mat,rot=rot,origin=origin)
                else:self.cube(g,'quadriculado',[f[0],f[1],z],[t[0],t[1],z+.2],mat,rot=rot,origin=origin)
    def trigger(self,g,x,y,guard=(0,0),mat='steel',gmat='frame',w=1.1,L=10.0,depth=5.2,curve=True):
        """trigger blade hanging from y at x, plus a guard (front at x+guard[0]..)"""
        c=self.cube
        c(g,'gatilho',[x-.6,y-3.4,-.45],[x+.6,y,.45],mat,rot=[0,0,-8],origin=[x,y,0])
        c(g,'gatilho_ponta',[x-.3,y-4.4,-.45],[x+.9,y-3.2,.45],mat,rot=[0,0,16],origin=[x+.2,y-3.3,0])
        for k in range(3):c(g,'gatilho_estria',[x+.56,y-3.2+k*.9,-.4],[x+.7,y-2.8+k*.9,.4],'dark',rot=[0,0,-8],origin=[x,y,0])
        if guard:
            gx0=x-L*.55;gx1=x+L*.45;yb=y-depth
            c(g,'guarda_mato',[gx0,yb-w,-w/2-.1],[gx1,yb,w/2+.1],gmat)
            c(g,'guarda_frente',[gx1-w,yb-w,-w/2-.1],[gx1,y+.2,w/2+.1],gmat)
            if curve:c(g,'guarda_curva',[gx1-w*1.6,yb-w*.9,-w/2-.1],[gx1-w*.2,yb+.4,w/2+.1],gmat,rot=[0,0,45],origin=[gx1-w*.9,yb-w*.25,0])
    def peep(self,g,x,y,w=1.6,h=3.0,mat='steel'):
        """rear aperture sight standing on y; returns the aperture centre height"""
        c=self.cube;c(g,'alca_base',[x-1.6,y,-w-.3],[x+1.6,y+1.0,w+.3],mat);c(g,'alca_corpo',[x-.7,y+1.0,-w],[x+.7,y+h+1.0,w],mat)
        c(g,'alca_furo',[x-.72,y+h*.55,-.42],[x+.72,y+h*.55+.84,.42],'dark');return y+h*.55+.42
    def post(self,g,x,y,top,ears=True,mat='steel',hood=None):
        """front sight post from y to top (its tip is the sight line); ears or a round hood around it"""
        c=self.cube;c(g,'massa_base',[x-1.2,y,-1.6],[x+1.2,y+1.0,1.6],mat);c(g,'massa_poste',[x-.35,y+1.0,-.35],[x+.35,top,.35],mat)
        c(g,'massa_ponta',[x-.3,top-.5,-.25],[x+.3,top,.25],'white')
        if ears and not hood:self.both(g,'massa_orelha',x-.8,y+1.0,x+.8,top+.6,1.1,1.6,mat)
        if hood:   # ring hood (octagon) around the post
            r=hood;cy=top-.2;h=r*math.tan(math.pi/8)
            for a in range(0,180,45):
                c(g,'massa_capuz',[x-1.0,cy+r-.35,-h],[x+1.0,cy+r+.35,h],mat,rot=[a,0,0],origin=[x,cy,0])
                c(g,'massa_capuz',[x-1.0,cy-r-.35,-h],[x+1.0,cy-r+.35,h],mat,rot=[a,0,0],origin=[x,cy,0])
    def mag_box(self,g,x0,x1,ytop,ybot,w,mat='mag',lean=0,curve=0,segs=1,ribs=True,base='polymer',window=False):
        """box magazine (optionally curved in segments) hanging from ytop; lean tilts forward (deg)"""
        c=self.cube;h=(ytop-ybot)/segs;xs=0
        for i in range(segs):
            ang=lean+curve*i;yt=ytop-i*h;yb=yt-h-.05;o=[(x0+x1)/2+xs,yt,0]
            c(g,'carregador',[x0+xs,yb,-w],[x1+xs,yt,w],mat,rot=[0,0,ang],origin=o)
            if ribs:self.both(g,'carregador_nervura',x0+xs+1.2,yb+.4,x1+xs-1.2,yt-.4,w,w+.18,mat,rot=[0,0,ang],origin=[o[0],o[1],w])
            if window and i<segs-1:self.cube(g,'carregador_janela',[x0+xs+1.0,yb+.6,w+.17],[x0+xs+1.6,yt-.6,w+.22],'dark',rot=[0,0,ang],origin=[o[0],o[1],w])
            xs+=math.tan(math.radians(ang))*h
        ang=lean+curve*(segs-1)
        c(g,'carregador_base',[x0+xs-.7,ybot-1.2,-w-.4],[x1+xs+.7,ybot+.2,w+.4],base,rot=[0,0,ang],origin=[(x0+x1)/2+xs,ybot,0])
        return xs
    def round_top(self,g,x0,x1,ytop,w,mat):
        """bullet on top of a magazine"""
        self.cube(g,'carregador_bala',[x0,ytop,-w*.5],[x1-1.6,ytop+1.1,w*.5],'brass');self.cube(g,'carregador_ponta',[x1-1.6,ytop+.15,-w*.4],[x1-.6,ytop+.95,w*.4],'brass')
    # ---------- output ----------
    def finish(self,*,scale,width=1.3,grip,muzzle_x,bore_y,iron_y,rail,moving=(),magazine=(),stock=(),roles,pistol=False,regions=None,mag_item=None,support=None):
        """grip=(x,y): centre of the top of the pistol grip in model units — it lands where the arsenal hand holds (z 11.6, y 5.6).
        rail=(y_top,x0,x1): where optics sit. roles: material -> main/accent/mag (repainted by the 13 finishes)."""
        out=os.path.join(T,'forge');idx,sw=self.save(out);S=scale
        tx=grip[0]+3.2/S;ty=grip[1]+.7/S
        Z=lambda x:round(8.4-(x-tx)*S,2);Y=lambda y:round(6.3+(y-ty)*S,2)
        ids=lambda groups:[i for g in groups for i in idx.get(g,[])]
        base={n:list(c) for n,(_,_,c) in self.SW.items()}
        skins={'0':base}
        for k,(m,a,mg) in THEMES.items():
            d=dict(base)
            for mat,role in roles.items():
                v={'main':m,'main2':tuple(max(0,x-12) if isinstance(x,int) else x for x in m),'accent':a,'mag':mg}[role];d[mat]=list(v)
            skins[str(k)]=d
        cfg={"bbmodel":os.path.join(out,self.name+'.bbmodel'),"family":self.family,"tag":"forge","scale":S,"width":width,"trigger":[tx,ty],"muzzle_dir":1,
             "default":next(iter(self.SW)),"swatches":sw,"moving":ids(moving),"magazine":ids(magazine),"stock":ids(stock),"skins":list(range(13)),"skin_colors":skins}
        cp=os.path.join(out,self.family+'.json');json.dump(cfg,open(cp,'w'),indent=1)
        subprocess.run([sys.executable,os.path.join(T,'import_gun.py'),cp],check=True,cwd=ROOT)
        ex=dict(rail=(Y(rail[0]),Z(rail[2]),Z(rail[1])),bore=Y(bore_y),muzzle=Z(muzzle_x),iron=Y(iron_y))
        if pistol:ex['pistol']=True
        reg=regions or {}
        meta={'extra':ex,'regions':{k:Z(v) for k,v in reg.items()},'support':[Y(support[1]),Z(support[0])] if support else None}
        json.dump(meta,open(os.path.join(out,self.family+'_meta.json'),'w'),indent=1)
        wire(self.family,ex,meta['regions']);print(self.family,'EXTRA',ex,'regions',meta['regions'])
        return Z,Y,S*width
def wire(f,ex,regions):
    """gunsmith EXTRA instead of GUNS, paint regions, paint emit lines"""
    p=os.path.join(T,'gunsmith_072.py');s=open(p).read()
    s=re.sub(r"GUNS=\[(.*?)\]",lambda m:"GUNS=["+",".join(x for x in m.group(1).split(',') if x.strip()!=f"'{f}'")+"]",s,count=1)
    line=f" '{f}':dict({', '.join(f'{k}={v!r}' for k,v in ex.items())}),   # 0.75.0: OBJ from tools/forge/{f}.py\n"
    s=re.sub(rf"^ '{f}':dict\(.*\n",'',s,flags=re.M);s=s.replace("EXTRA={   # guns not rebuilt here: just their sight mounts and muzzle data\n","EXTRA={   # guns not rebuilt here: just their sight mounts and muzzle data\n"+line,1)
    open(p,'w').write(s)
    rp=os.path.join(T,'forge_regions.json');R=json.load(open(rp)) if os.path.exists(rp) else {};R[f]=regions;json.dump(R,open(rp,'w'),indent=1)
    pp=os.path.join(T,'make_0700_paint.py');s=open(pp).read();tag=f"armory21_{f}_forge_"
    if tag not in s:
        s=s.replace("obj_emit('sniperh','body'",f"obj_emit('{f}','body','{tag}body');obj_emit('{f}','view','{tag}view')\nobj_emit('{f}','moving','{tag}moving',force=3);obj_emit('{f}','magazine','{tag}magazine',force=4)\nobj_emit('sniperh','body'",1)
        open(pp,'w').write(s)

FAMS=['desert_eagle','usp','m4a1','ak47','m870','mp5','smg','sniper','mp40','p90','python','glock','scar','ump45','spas12','beretta','uzi','famas','m249','barrett','mac10','galil','aug','saiga12','svd']
def support_table():
    rows=[]
    for f in FAMS:
        p=os.path.join(T,'forge',f+'_meta.json');sp=json.load(open(p)).get('support') if os.path.exists(p) else None
        rows.append('{%sF,%sF}'%(sp[0],sp[1]) if sp else '{0F,0F}')
    J=os.path.join(ROOT,'src/main/java/br/mareeletrica/client/ArmoryRenderer.java');s=open(J).read()
    s=re.sub(r'static final float\[\]\[\] SUPPORT=\{.*?\};','static final float[][] SUPPORT={'+','.join(rows)+'};',s,count=1);open(J,'w').write(s);print('support ok')
if __name__=='__main__':support_table()

def ak_receiver(K,g,x0=-40,x1=0,yb=25.6,yt=36.8,w=2.9,mat='steel',cover='steel',rail=False,rivets=True):
    """AK-pattern stamped receiver: body, ribbed dust cover, trunnion rivets, safety lever, ejection port; returns top of cover"""
    c=K.cube;both=K.both
    c(g,'receptor',[x0,yb,-w],[x1,yt,w],mat)
    c(g,'tampa',[x0+2,yt,-w+.3],[x1-1,yt+1.6,w-.3],cover)
    both(g,'tampa_chanfro',x0+2,yt+.6,x1-1,yt+2.0,w-1.4,w-.2,cover,rot=[45,0,0],origin=[(x0+x1)/2,yt+1.2,w-.8])
    c(g,'tampa_topo',[x0+2,yt+1.6,-w+1.2],[x1-1,yt+2.2,w-1.2],cover)
    for k in range(3):c(g,'tampa_nervura',[x0+6+k*1.2,yt+2.2,-w+1.4],[x0+6.6+k*1.2,yt+2.4,w-1.4],cover)
    c(g,'tampa_botao',[x0-.8,yt+.6,-.8],[x0+2,yt+1.8,.8],'polish')
    c(g,'janela',[x0+16,yt-4.2,w-.02],[x1-6,yt+.2,w+.08],'dark')
    c(g,'seguranca',[x0+4,yt-3.2,w],[x1-4,yt-2.0,w+.4],mat)
    c(g,'seguranca_aba',[x1-6,yt-6.0,w],[x1-4,yt-2.0,w+.4],mat)
    for k in range(5):c(g,'seguranca_entalhe',[x0+16,yt-6+k*.9,w+.02],[x0+16.5,yt-5.6+k*.9,w+.12],'white' if k==0 else 'red') if False else None
    if rivets:
        for x,y in ((x1-2,yb+2),(x1-4,yb+2),(x1-3,yb+4.6),(x0+3,yb+2),(x0+6,yb+2),(x0+12,yb+1.6)):both(g,'rebite',x-.4,y-.4,x+.4,y+.4,w,w+.14,'polish')
    c(g,'trunnion',[x1-5,yb-.6,-w-.05],[x1,yb+5.4,w+.05],mat)
    return yt+2.2
def ak_trigger_group(K,g,x,yb,gmat='steel'):
    K.trigger(g,x,yb,guard=True,gmat=gmat,w=1.1,L=12.6,depth=6.0,curve=False)
    K.cube(g,'reten_carregador',[x+3.8,yb-4.6,-1.2],[x+5.8,yb-.2,1.2],gmat)
