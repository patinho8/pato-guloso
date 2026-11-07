package br.mareeletrica;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Armor charging station (stand on it: charges power armor) and vehicle hangar (repairs and rearms jets, helicopters and tanks parked on it). */
public final class ServiceMachines {
 private ServiceMachines(){}
 public static boolean tick(MachineEntity m){
  if(!(m.getLevel() instanceof ServerLevel level))return false;
  var c=m.getBlockPos().getCenter();
  if(m.kind==Kind.ARMOR_STATION){
   boolean any=false;
   for(var p:level.getEntitiesOfClass(Player.class,new AABB(m.getBlockPos()).inflate(1,2,1),x->x.isAlive()&&!x.isSpectator())){
    var chest=p.getItemBySlot(EquipmentSlot.CHEST);
    if(!(chest.getItem() instanceof PowerArmor.SuitItem))continue;
    int have=PowerArmor.charge(chest);if(have>=PowerArmor.MAX_CHARGE)continue;
    if(!m.clean.pay(600)){m.status=2;return false;}
    PowerArmor.setCharge(chest,have+PowerArmor.MAX_CHARGE/80);   // full in ~40 s
    any=true;
    level.sendParticles(ParticleTypes.ELECTRIC_SPARK,p.getX(),p.getY()+1,p.getZ(),6,.3,.6,.3,.05);
    if((level.getGameTime()/10)%4==0)level.playSound(null,m.getBlockPos(),SoundEvents.BEACON_AMBIENT,SoundSource.BLOCKS,.6F,1.6F);
    p.displayClientMessage(net.minecraft.network.chat.Component.literal("Carregando armadura: "+PowerArmor.charge(chest)*100/PowerArmor.MAX_CHARGE+"%"),true);
   }
   m.status=any?77:76;m.lastRate=any?1200:0;return any;
  }
  // hangar: 3x3 pad around the block (and a bit above, for the big jet)
  boolean any=false;
  for(Entity e:level.getEntities((Entity)null,new AABB(m.getBlockPos()).inflate(2.5,3,2.5),x->x instanceof Vehicles.Serviceable)){
   if(m.clean.stored()<800){m.status=2;return false;}
   if(((Vehicles.Serviceable)e).service()){m.clean.pay(800);any=true;level.sendParticles(ParticleTypes.WAX_OFF,e.getX(),e.getY()+1,e.getZ(),4,1,.5,1,.05);}
  }
  if(any&&(level.getGameTime()/10)%2==0)level.playSound(null,m.getBlockPos(),SoundEvents.ANVIL_USE,SoundSource.BLOCKS,.4F,1.4F);
  m.status=any?79:78;m.lastRate=any?1600:0;return any;
 }
}
