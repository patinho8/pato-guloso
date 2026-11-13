"""0.90.0: big versions of ordinary machines. Scales each machine's block model by its size (2 or 3),
cuts it into per-cell part models (like the other multiblocks) and writes the blockstates:
  <id>.json       : expanded=false -> classic model, expanded=true -> cell 0
  <id>_part.json  : facing/part/active -> <id>_part_<n>[_active]
Must match Kind.bigSize()."""
import json, math, os, copy
A='src/main/resources/assets/mareeletrica/'
SIZE={**{k:2 for k in "electric_furnace crusher healer magnet electric_cooker recycler stone_generator oxygen_station harvester planter auto_fisher ore_scanner charging_station xp_collector automatic_turret component_press electric_sawmill cryogenic_freezer electric_repairer disenchanter animal_feeder automatic_shearer fire_suppressor component_assembler material_compactor bucket_filler effect_cleanser food_station apiary_extractor core_medium power_controller".split()},
      **{k:3 for k in "weather_controller core_large armor_charging_station vehicle_hangar construction_projector terrain_leveler auto_crank".split()}}
FACES={'north':(2,0),'south':(2,1),'west':(0,0),'east':(0,1),'down':(1,0),'up':(1,1)}   # axis, 0=min side 1=max side
Y={'north':0,'east':90,'south':180,'west':270}
def load(name):
    d=json.load(open(A+f'models/block/{name}.json'))
    if d.get('parent','').startswith('mareeletrica:block/'):
        par=load(d['parent'].split('/')[-1]);t=dict(par.get('textures',{}));t.update(d.get('textures',{}))
        m=dict(par);m.update({k:v for k,v in d.items() if k not in('parent','textures')});m['textures']=t;m.pop('parent',None)
        if 'elements' not in d:m['elements']=par.get('elements',[])
        return m
    return d
def cut(model,s,part):
    ox,oy,oz=part%s,part//(s*s),part//s%s;o=(ox*16,oy*16,oz*16)
    out=[]
    for e in model.get('elements',[]):
        f=[v*s for v in e['from']];t=[v*s for v in e['to']]
        lo=[max(f[i],o[i]) for i in range(3)];hi=[min(t[i],o[i]+16) for i in range(3)]
        if any(hi[i]-lo[i]<=1e-6 for i in range(3)):continue
        n={'from':[round(lo[i]-o[i],4) for i in range(3)],'to':[round(hi[i]-o[i],4) for i in range(3)],'faces':{}}
        if 'rotation' in e:
            r=copy.deepcopy(e['rotation']);r['origin']=[round(r['origin'][i]*s-o[i],4) for i in range(3)];n['rotation']=r
        if 'shade' in e:n['shade']=e['shade']
        for fn,fd in e['faces'].items():
            ax,side=FACES[fn]
            # drop faces that sit on a cut plane (they were inside the original element)
            if side==0 and lo[ax]>f[ax]+1e-6:continue
            if side==1 and hi[ax]<t[ax]-1e-6:continue
            fd=dict(fd);fd.pop('cullface',None);n['faces'][fn]=fd
        if n['faces']:out.append(n)
    return out
count=0
for mid,s in SIZE.items():
    bs=json.load(open(A+f'blockstates/{mid}.json'))
    base=load(mid);active=load(mid+'_active') if os.path.exists(A+f'models/block/{mid}_active.json') else base
    for part in range(s**3):
        for suffix,m in(('',base),('_active',active)):
            els=cut(m,s,part)
            d={'ambientocclusion':m.get('ambientocclusion',True),'textures':m['textures'],'elements':els}
            if not els:d['elements']=[]
            json.dump(d,open(A+f'models/block/{mid}_part_{part}{suffix}.json','w'));count+=1
    # controller blockstate
    v={}
    for k,val in bs['variants'].items():
        if 'expanded=' in k:continue
        props=dict(x.split('=') for x in k.split(','))
        def key(extra):d=dict(props);d.update(extra);return ','.join(f'{a}={d[a]}' for a in sorted(d))
        v[key({'expanded':'false'})]=val
        y=Y[props['facing']];m={'model':f'mareeletrica:block/{mid}_part_0'+('_active' if props.get('active')=='true' else '')}
        if y:m['y']=y
        v[key({'expanded':'true'})]=m
    json.dump({'variants':v},open(A+f'blockstates/{mid}.json','w'))
    pv={}
    for f,y in Y.items():
        for part in range(s**3):
            for act in('false','true'):
                m={'model':f'mareeletrica:block/{mid}_part_{part}'+('_active' if act=='true' else '')}
                if y:m['y']=y
                pv[f'active={act},facing={f},part={part}']=m
    json.dump({'variants':pv},open(A+f'blockstates/{mid}_part.json','w'))
print('models',count)
