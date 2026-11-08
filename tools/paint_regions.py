"""0.70.0: which paintable region (0 corpo, 1 cano, 2 coronha, 3 ferrolho, 4 carregador, 5 mira) each face/element of a gun belongs to."""
import re
CANO=re.compile(r'barrel|muzzle|bore|brake|flash|flute|jacket|gas_tube|gas_block|gas_regulator|cleaning_rod|front_sight|mag_tube|suppress|heat_shield|shield_hole|nut|delta_ring|underlug|ejector|compensator|comp_|choke|crown|bead|thread|hider|vent_rib|rib_post|front_post|front_wing|front_ear|front_hood|fsb_|front_ring|mag_cap|barrel_clamp')
CORONHA=re.compile(r'stock|butt|_pad|^pad|cheek|hinge|thumbhole|wire|rear_plate|strut|monopod|buffer')
def java_region(name):
    n=name.lower()
    if CORONHA.search(n):return 2
    if CANO.search(n):return 1
    return 0
# imported (OBJ) guns: rules on the face centre (model units) and material
def obj_region(tag,mat,z,y):
    if tag=='ak47':
        if mat=='mag':return 4
        if z<-10.4:return 1
        if z>12.3 or (mat=='wood' and z>11.8 and y>6.8):return 2
        return 0
    if tag=='sniper':   # 0.72.0: model scaled .66 -> .82 about the trigger (z 8.4, y 6.3)
        if y>12.4 and -18.2<z<13.5:return 5
        if z<-21.3:return 1
        if z>14.1:return 2
        return 0
    if tag=='sniperh':
        if y>12.0 and -5<z<17.5:return 5
        if z<-4.5:return 1
        if z>17.5:return 2
        return 0
    return 0
