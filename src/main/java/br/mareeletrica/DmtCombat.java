package br.mareeletrica;

import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

/** Fictional maneuver equipment; actions and ammunition are validated on the server. */
public final class DmtCombat {
 private DmtCombat(){}
 public static String name(int mode){return switch(mode){case 1->"Lâminas";case 2->"Arma de gás";case 3->"Lanças";default->"Guardado";};}
 public static int mode(Player p){var gear=DmtItem.equipped(p);if(gear.isEmpty()||!DmtItem.enabled(p)||SniperItem.firearm(p.getMainHandItem()))return 0;int n=gear.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getInt("DmtWeapon");return n>=1&&n<=3&&!DmtLoadout.of(p).getStackInSlot(n-1).isEmpty()?n:0;}
 public static ItemStack visual(Player p,int hand){int mode=mode(p);if(mode==0||hand==1&&mode!=1)return ItemStack.EMPTY;return DmtLoadout.of(p).getStackInSlot(mode-1);}
 public static void select(Player p,int mode){if(DmtItem.equipped(p).isEmpty()||mode<0||mode>3)return;if(mode>0&&DmtLoadout.of(p).getStackInSlot(mode-1).isEmpty()){p.displayClientMessage(Component.literal("Coloque esse equipamento no painel do DMT."),true);return;}var gear=DmtItem.equipped(p);var tag=gear.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();tag.putInt("DmtWeapon",mode);gear.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));if(p instanceof ServerPlayer server)DmtEquipment.sync(server);}
 public static void cycle(Player p){int start=mode(p);for(int i=1;i<=4;i++){int next=(start+i)%4;if(next==0||!DmtLoadout.of(p).getStackInSlot(next-1).isEmpty()){select(p,next);return;}}}
 public static boolean pullable(LivingEntity mob){return mob instanceof Mob&&mob.isAlive()&&mob.getBbWidth()*mob.getBbHeight()<=3.5F&&mob.getMaxHealth()<=100;}
 public static boolean pullEquipped(Player p){return !DmtLoadout.of(p).getStackInSlot(4).isEmpty();}
 public static void attack(ServerPlayer p){
  if(p.containerMenu!=p.inventoryMenu||!p.isAlive()||p.isSpectator()||RemoteSessions.active(p))return;int mode=mode(p);if(mode==0)return;var session=DmtSystem.ensure(p);long now=p.level().getGameTime();if(now<session.attackAfter)return;var kit=DmtLoadout.of(p);var weapon=kit.getStackInSlot(mode-1);
  if(mode==1){session.attackAfter=now+8;Vec3 eye=p.getEyePosition(),look=p.getLookAngle();var candidates=p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(4.5),e->e!=p&&e.isAlive()&&!e.isAlliedTo(p)&&(!(e instanceof Player other)||p.canHarmPlayer(other))&&p.hasLineOfSight(e));candidates.sort(Comparator.comparingDouble(p::distanceToSqr));int hits=0;for(var target:candidates){Vec3 to=target.getBoundingBox().getCenter().subtract(eye);if(to.length()>4.8||to.normalize().dot(look)<.45)continue;if(target.hurt(p.damageSources().playerAttack(p),14F+(float)Math.min(6,p.getDeltaMovement().length()*4))){target.knockback(.35,p.getX()-target.getX(),p.getZ()-target.getZ());hits++;}if(hits==3)break;}p.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK,p.getX()+look.x*1.6,p.getEyeY()-.3,p.getZ()+look.z*1.6,2,.25,.1,.25,0);if(hits>0&&!p.isCreative()){weapon.setDamageValue(weapon.getDamageValue()+1);if(weapon.getDamageValue()>=weapon.getMaxDamage())weapon.shrink(1);kit.persist();}p.level().playSound(null,p.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,.8F,1.2F);p.swing(InteractionHand.MAIN_HAND,true);return;}
  if(mode==2){var tag=weapon.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();int rounds=tag.getInt("Rounds");if(rounds<=0){int ammo=-1;for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(MareEletrica.DMT_CARTRIDGE.get())){ammo=i;break;}if(ammo<0&&!p.isCreative()){p.displayClientMessage(Component.literal("Faltam cartuchos de gás no inventário."),true);session.attackAfter=now+15;return;}if(!p.isCreative())p.getInventory().getItem(ammo).shrink(1);tag.putInt("Rounds",2);weapon.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));kit.persist();session.attackAfter=now+30;p.level().playSound(null,p.blockPosition(),SoundEvents.CROSSBOW_LOADING_END.value(),SoundSource.PLAYERS,.9F,.8F);return;}if(DmtItem.fuel(DmtItem.equipped(p))<12&&!p.isCreative()){p.displayClientMessage(Component.literal("Recarregue o gás do DMT."),true);return;}if(!p.isCreative()){tag.putInt("Rounds",rounds-1);weapon.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));kit.persist();DmtItem.fuel(DmtItem.equipped(p),DmtItem.fuel(DmtItem.equipped(p))-12);}session.attackAfter=now+12;{var bullet=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),p.level());bullet.setOwner(p);bullet.setPos(p.getEyePosition().add(p.getLookAngle().scale(.65)));bullet.shoot(p.getLookAngle().x,p.getLookAngle().y,p.getLookAngle().z,12F,0F);p.level().addFreshEntity(bullet);}p.setDeltaMovement(p.getDeltaMovement().add(p.getLookAngle().scale(-.13)));p.hurtMarked=true;p.level().playSound(null,p.blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),SoundSource.PLAYERS,.6F,1.8F);return;}
  session.attackAfter=now+24;var spear=new DmtProjectileEntity(MareEletrica.DMT_PROJECTILE.get(),p.level());spear.setSpear(true);spear.setOwner(p);spear.setPos(p.getEyePosition().add(p.getLookAngle().scale(.8)));spear.shoot(p.getLookAngle().x,p.getLookAngle().y,p.getLookAngle().z,2.8F,.15F);p.level().addFreshEntity(spear);if(!p.isCreative())kit.extractItem(2,1,false);p.level().playSound(null,p.blockPosition(),SoundEvents.FIREWORK_ROCKET_LAUNCH,SoundSource.PLAYERS,1F,.7F);p.swing(InteractionHand.MAIN_HAND,true);
 }
}
