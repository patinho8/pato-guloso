"""0.44.0: five more guns (MAC-10, Galil ACE, Steyr AUG, Saiga-12, Dragunov SVD) built from donor geometry of
generate_armory21.py (no CairoSVG needed: the atlases are copied from the donor family and re-tinted)."""
import sys,types,json,copy,shutil
sys.modules['cairosvg']=types.ModuleType('cairosvg')
sys.path.insert(0,__import__('os').path.dirname(__file__))
import generate_armory21 as G
from PIL import Image,ImageEnhance
NEW=[('mac10','MAC-10','uzi',30,'netherite_scrap'),('galil','Galil ACE','ak47',35,'glowstone_dust'),('aug','Steyr AUG','famas',30,'prismarine_shard'),('saiga12','Saiga-12','spas12',8,'blaze_powder'),('svd','Dragunov SVD','barrett',10,'ender_pearl')]
DONOR={n:d for n,_,d,_,_ in NEW}
TINT={'mac10':(.9,.9,.95),'galil':(1.0,.95,.82),'aug':(.85,1.0,.85),'saiga12':(.95,.92,.9),'svd':(1.05,.9,.8)}
for n,t,d,cap,mark in NEW:
    i=G.FAMILIES.index(d)
    G.FAMILIES.append(n);G.TITLES.append(t);G.ZOOMS.append(G.ZOOMS[i]);G.CAPS.append(cap);G.MARKERS.append(mark);G.MUZZLE.append(G.MUZZLE[i]);G.SKINS.append(G.SKINS[i])
orig_geometry=G.geometry;orig_optic=G.optic;orig_supp=G.suppressor
def move(es,dz=0,dy=0,dx=0):
    for e in es:
        e['from']=[e['from'][0]+dx,e['from'][1]+dy,e['from'][2]+dz];e['to']=[e['to'][0]+dx,e['to'][1]+dy,e['to'][2]+dz]
        if 'rotation' in e:o=e['rotation']['origin'];e['rotation']['origin']=[o[0]+dx,o[1]+dy,o[2]+dz]
    return es
def geometry(f):
    if f not in DONOR:return orig_geometry(f)
    P,iron=orig_geometry(DONOR[f]);P=copy.deepcopy(P);b=P['body']
    if f=='mac10':
        # stubby box: no stock, short threaded barrel, front strap loop
        P['body']=[e for e in b if not any(k in e['name'] for k in ('uzi_stock','uzi_butt'))]
        P['body']+= [G.box('mac_strap_loop',7.6,11.2,-2.4,8.4,12.2,-1.6,'metal'),G.box('mac_bolt_slot',7.8,11.2,0,8.2,11.25,8,'rubber')]
    elif f=='galil':
        # polymer furniture instead of wood, folding skeleton stock, carry handle
        for e in b:
            if e['name'] in ('wood_stock','wood_handguard'):
                for face in e['faces'].values():face['uv']=G.box('x',*e['from'],*e['to'],'rubber')['faces']['north']['uv']
        P['body']=[e for e in b if e['name']!='wood_stock']+[G.box('galil_stock_top',7.3,9.4,12,8.7,10.3,22,'metal'),G.box('galil_stock_low',7.3,6.6,12,8.7,7.4,21,'metal'),G.box('galil_butt',6.6,6.0,21,9.4,10.6,22.2,'rubber'),G.box('galil_handle',7.5,12.6,1,8.5,13.3,7,'metal'),G.box('galil_handle_post',7.6,11.8,1.2,8.4,12.6,2,'metal')]
    elif f=='aug':
        # bullpup with an integrated scope tube and a vertical foregrip, green polymer
        P['body']=[e for e in b if not e['name'].startswith('famas_handle') and not e['name'].startswith('famas_bipod')]
        P['body']+=G.cylinder('aug_scope',8,13.4,-2,8,1.1,'paint')+[G.box('aug_scope_mount',7.2,11,-1,8.8,12.4,7,'metal'),G.box('aug_foregrip',7.2,3.4,-6,8.8,6.5,-4.4,'rubber'),G.box('aug_guard',6.4,4.2,-2,9.6,5.0,10,'rubber')]
    elif f=='saiga12':
        # AK-style shotgun: box magazine, polymer stock, thicker barrel
        P['body']=[e for e in b if e['name']!='ak_mag']
        P['body']+=[G.box('saiga_gas_tube',7.4,10.8,-8,8.6,11.6,1,'metal')]
    elif f=='svd':
        # long thin rifle: skeleton (thumbhole) stock, slim handguard, very long barrel with slotted flash hider
        P['body']=[e for e in b if not e['name'].startswith('barrett_bipod') and e['name']!='barrett_monopod']
        P['body']+=[G.box('svd_thumbhole',6.6,6.6,15,9.4,7.6,21,'wood'),G.box('svd_cheek',6.8,10,14,9.2,11.4,19,'wood'),G.box('svd_handguard',6.4,8.2,-10,9.6,10.6,-1,'wood')]
    return P,iron
def optic(f,v):return orig_optic(DONOR.get(f,f),v)
def suppressor(f,v):return orig_supp(DONOR.get(f,f),v)
G.geometry=geometry;G.optic=optic;G.suppressor=suppressor
G.atlas=lambda f,skin:None
# atlases: copy the donor's 13 skins with a slight tint so each gun looks its own
T=G.A/'textures/block'
for n,_,d,_,_ in NEW:
    for skin in range(13):
        src=T/f'armory21_{d}_{skin}.png'
        if not src.exists():continue
        im=Image.open(src).convert('RGB');r,g,b=im.split();k=TINT[n]
        im=Image.merge('RGB',[x.point(lambda v,kk=kk:min(255,int(v*kk))) for x,kk in zip((r,g,b),k)])
        im.convert('P',palette=Image.ADAPTIVE,colors=256).save(T/f'armory21_{n}_{skin}.png')
# run the original main loop only for the new families
names=[n for n,*_ in NEW]
G.FAMILIES_ALL=G.FAMILIES
orig_main_families=list(G.FAMILIES)
import builtins
_enum=builtins.enumerate
def only_new(it,*a,**k):
    lst=list(it)
    if lst==orig_main_families:return [(i,f) for i,f in _enum(lst) if f in names]
    return _enum(lst,*a,**k)
G.enumerate=only_new
G.dump(G.ROOT/'ARTWORK_tmp.json',{})
art=G.ROOT/'ARTWORK_0.21.0.json';keep=art.read_text() if art.exists() else None
G.main()
if keep is not None:art.write_text(keep)
(G.ROOT/'ARTWORK_tmp.json').unlink()
# gun recipes and names
L={l:json.loads((G.A/'lang'/(l+'.json')).read_text()) for l in ('pt_br','en_us')}
for n,t,d,_,mark in NEW:
    for l in L:L[l]['item.mareeletrica.'+n]=t
    G.dump(G.ROOT/f'src/main/resources/data/mareeletrica/recipe/{n}.json',{"type":"minecraft:crafting_shaped","category":"equipment","pattern":["IIB"," MC","  S"],"key":{"I":{"item":"mareeletrica:iron_plate"},"B":{"item":"mareeletrica:reinforced_alloy"},"M":{"item":"mareeletrica:servo_motor"},"C":{"item":"mareeletrica:control_module"},"S":{"item":"minecraft:"+mark}},"result":{"id":"mareeletrica:"+n,"count":1}})
for l,dct in L.items():G.dump(G.A/'lang'/(l+'.json'),dct)
print('new guns done')
