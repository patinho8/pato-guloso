package br.mareeletrica;

import java.util.function.Supplier;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

/** Real flying bullets (with a glowing tracer) for the machine-gun turret and the fighter jet's cannons. */
public final class Tracers {
 public static final Supplier<EntityType<Round>> ROUND=MareEletrica.ACTORS.register("tracer_round",()->
  EntityType.Builder.<Round>of(Round::new,MobCategory.MISC).sized(.2F,.2F).clientTrackingRange(16).updateInterval(1).build(MareEletrica.ID+":tracer_round"));
 private Tracers(){}
 public static void register(){}

 /** Fires one round from {@code from} along {@code dir}; {@code carrier} (the turret or the jet) is never hit by its own bullets. */
 public static Round fire(Level level,Entity carrier,LivingEntity shooter,Vec3 from,Vec3 dir,float speed,float damage,Vec3 inherit){
  var r=new Round(ROUND.get(),level);r.setOwner(shooter!=null?shooter:carrier);r.carrier=carrier==null?null:carrier.getUUID();r.damage=damage;
  r.setPos(from.x,from.y,from.z);
  Vec3 v=dir.normalize().scale(speed).add(inherit==null?Vec3.ZERO:inherit);
  r.setDeltaMovement(v);r.setYRot((float)(Math.toDegrees(Math.atan2(v.x,v.z))));r.setXRot((float)Math.toDegrees(Math.atan2(v.y,v.horizontalDistance())));r.yRotO=r.getYRot();r.xRotO=r.getXRot();
  level.addFreshEntity(r);return r;
 }

 public static final class Round extends ThrowableProjectile {
  java.util.UUID carrier;float damage=6F;
  public Round(EntityType<? extends Round> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
  @Override protected double getDefaultGravity(){return .012;}
  @Override public boolean shouldRenderAtSqrDistance(double d){return d<256*256;}
  @Override protected boolean canHitEntity(Entity e){
   if(carrier!=null&&(e.getUUID().equals(carrier)||e.getVehicle()!=null&&e.getVehicle().getUUID().equals(carrier)))return false;
   return super.canHitEntity(e);
  }
  @Override protected void onHitEntity(EntityHitResult hit){
   if(level().isClientSide)return;
   var e=hit.getEntity();e.invulnerableTime=0;
   var owner=getOwner();
   e.hurt(damageSources().mobProjectile(this,owner instanceof LivingEntity le?le:null),damage);
  }
  @Override protected void onHitBlock(BlockHitResult hit){
   super.onHitBlock(hit);
   if(level() instanceof ServerLevel sl){var st=level().getBlockState(hit.getBlockPos());var at=hit.getLocation();
    if(!st.isAir())sl.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,st),at.x,at.y,at.z,5,.08,.08,.08,.1);
    sl.sendParticles(ParticleTypes.SMOKE,at.x,at.y,at.z,2,.05,.05,.05,.01);}
  }
  @Override protected void onHit(HitResult r){super.onHit(r);if(!level().isClientSide)discard();}
  @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>60)discard();}
  @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putFloat("Damage",damage);}
  @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);damage=t.getFloat("Damage");}
 }
}
