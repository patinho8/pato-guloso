"""0.58: landmine, portable missile launcher, airstrike flare."""
import json,os
ROOT=os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A=os.path.join(ROOT,'src/main/resources/assets/mareeletrica');D=os.path.join(ROOT,'src/main/resources/data/mareeletrica')
T={"olive":"mareeletrica:block/veh_olive","dark":"mareeletrica:block/base_dark","metal":"mareeletrica:block/veh_metal","steel":"mareeletrica:block/base_steel",
   "red":"mareeletrica:block/lamp_red","green":"mareeletrica:block/lamp_green","yellow":"mareeletrica:block/lamp_yellow","hazard":"mareeletrica:block/veh_hazard",
   "glass":"mareeletrica:block/veh_glass","dirt":"minecraft:block/coarse_dirt","particle":"mareeletrica:block/veh_olive"}
def b(f,t,tex,rot=None):
    e={"from":f,"to":t,"faces":{d:{"texture":"#"+tex,"uv":[0,0,16,16]} for d in ["down","up","north","south","west","east"]}}
    if rot:e["rotation"]={"origin":rot[0],"axis":rot[1],"angle":rot[2]}
    return e
def save(kind,name,els,disp=None,tex=T):
    m={"textures":tex,"elements":els}
    if disp:m["display"]=disp
    json.dump(m,open(os.path.join(A,'models',kind,name+'.json'),'w'),separators=(',',':'))
mine=[b([3,0,3],[13,1,13],'olive'),b([4,0,2],[12,1,14],'olive'),b([2,0,4],[14,1,12],'olive'),b([5,1,5],[11,1.5,11],'dark'),b([7.5,1.5,7.5],[8.5,2,8.5],'metal')]
save('block','landmine_off',mine+[b([7.6,1.6,7.6],[8.4,1.9,8.4],'green')])
save('block','landmine_on',mine+[b([7.6,1.6,7.6],[8.4,1.9,8.4],'red')])
json.dump({"variants":{"armed=false,triggered=false":{"model":"mareeletrica:block/landmine_off"},"armed=false,triggered=true":{"model":"mareeletrica:block/landmine_off"},"armed=true,triggered=false":{"model":"mareeletrica:block/landmine_on"},"armed=true,triggered=true":{"model":"mareeletrica:block/landmine_on"}}},open(os.path.join(A,'blockstates/landmine.json'),'w'))
json.dump({"parent":"mareeletrica:block/landmine_off","display":{"gui":{"rotation":[30,45,0],"translation":[0,3,0],"scale":[1,1,1]},"ground":{"scale":[.6,.6,.6]},"fixed":{"rotation":[90,0,0],"scale":[1,1,1]},"thirdperson_righthand":{"rotation":[75,45,0],"translation":[0,2.5,0],"scale":[.5,.5,.5]},"firstperson_righthand":{"rotation":[0,45,0],"translation":[0,3,0],"scale":[.6,.6,.6]}}},open(os.path.join(A,'models/item/landmine.json'),'w'))
H={"gui":{"rotation":[20,-135,0],"translation":[0,0,0],"scale":[.75,.75,.75]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"fixed":{"rotation":[0,90,0],"scale":[.8,.8,.8]},
 "thirdperson_righthand":{"rotation":[0,0,0],"translation":[0,4,2],"scale":[.85,.85,.85]},"thirdperson_lefthand":{"rotation":[0,0,0],"translation":[0,4,2],"scale":[.85,.85,.85]},
 "firstperson_righthand":{"rotation":[0,0,0],"translation":[4,3,-3],"scale":[.5,.5,.5]},"firstperson_lefthand":{"rotation":[0,0,0],"translation":[4,3,-3],"scale":[.5,.5,.5]}}
# launcher: long tube along z, sight on the side, grip below
launcher=[b([6,8,-8],[10,12,24],'olive'),b([5.5,7.5,-8.5],[10.5,12.5,-7],'dark'),b([5.5,7.5,23],[10.5,12.5,24.5],'dark'),
 b([10,10,2],[13,13,8],'dark'),b([10.5,10.5,1.8],[12.5,12.5,2],'glass'),b([7,4,6],[9,8,8],'dark'),b([7,4,12],[9,8,14],'dark'),b([6.5,12,10],[9.5,13,12],'hazard')]
save('item','missile_launcher_portable',launcher,H)
flare=[b([6.5,0,6.5],[9.5,10,9.5],'red'),b([6,10,6],[10,11,10],'dark'),b([7.5,11,7.5],[8.5,13,8.5],'metal'),b([6.4,4,6.4],[9.6,6,9.6],'yellow')]
save('item','airstrike_flare',flare,{"gui":{"rotation":[25,-30,0],"scale":[1.2,1.2,1.2]},"ground":{"translation":[0,2,0],"scale":[.5,.5,.5]},"thirdperson_righthand":{"translation":[0,3,1],"scale":[.6,.6,.6]},"firstperson_righthand":{"rotation":[0,-25,0],"translation":[1,3.5,-1],"scale":[.6,.6,.6]},"fixed":{"scale":[1,1,1]}})
json.dump({"type":"minecraft:block","pools":[{"rolls":1,"entries":[{"type":"minecraft:item","name":"mareeletrica:landmine"}]}]},open(os.path.join(D,'loot_table/blocks/landmine.json'),'w'))
json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":[" P ","ITI"],"key":{"P":{"item":"minecraft:stone_pressure_plate"},"I":{"item":"minecraft:iron_ingot"},"T":{"item":"minecraft:tnt"}},"result":{"id":"mareeletrica:landmine","count":2}},open(os.path.join(D,'recipe/landmine.json'),'w'))
json.dump({"type":"minecraft:crafting_shaped","category":"misc","pattern":["III","GRS","  I"],"key":{"I":{"item":"minecraft:iron_ingot"},"G":{"item":"minecraft:glass_pane"},"R":{"item":"minecraft:redstone"},"S":{"item":"minecraft:stick"}},"result":{"id":"mareeletrica:missile_launcher_portable","count":1}},open(os.path.join(D,'recipe/missile_launcher_portable.json'),'w'))
json.dump({"type":"minecraft:crafting_shapeless","category":"misc","ingredients":[{"item":"minecraft:red_dye"},{"item":"minecraft:gunpowder"},{"item":"minecraft:tnt"},{"item":"minecraft:redstone_torch"}],"result":{"id":"mareeletrica:airstrike_flare","count":1}},open(os.path.join(D,'recipe/airstrike_flare.json'),'w'))
for lang in('pt_br','en_us'):
    p=os.path.join(A,'lang',lang+'.json');j=json.load(open(p))
    pt=lang=='pt_br'
    j['block.mareeletrica.landmine']='Mina Terrestre' if pt else 'Landmine'
    j['item.mareeletrica.missile_launcher_portable']='Lança-Mísseis Portátil' if pt else 'Portable Missile Launcher'
    j['item.mareeletrica.airstrike_flare']='Sinalizador de Ataque Aéreo' if pt else 'Airstrike Flare'
    j['entity.mareeletrica.airstrike_marker']='Fumaça de Ataque Aéreo' if pt else 'Airstrike Smoke'
    j['entity.mareeletrica.airstrike_bomb']='Bomba' if pt else 'Bomb'
    json.dump(j,open(p,'w'),ensure_ascii=False,indent=1)
t=os.path.join(ROOT,'src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json');j=json.load(open(t))
if 'mareeletrica:landmine' not in j['values']:j['values'].append('mareeletrica:landmine')
json.dump(j,open(t,'w'),indent=1)
print('war2 ok')
