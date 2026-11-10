package br.mareeletrica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** 0.83.0: a Counter-Strike style bot for the Bomb mode. Walks to its objective, shoots enemies it can see, plants or defuses the bomb. */
public class BombBot extends PathfinderMob {
 public static final EntityDataAccessor<Integer> TEAM=SynchedEntityData.defineId(BombBot.class,EntityDataSerializers.INT);   // 0 = TR, 1 = CT
 public static final EntityDataAccessor<Integer> SKIN=SynchedEntityData.defineId(BombBot.class,EntityDataSerializers.INT);
 public static final EntityDataAccessor<Integer> WORK=SynchedEntityData.defineId(BombBot.class,EntityDataSerializers.INT);   // >0 while planting/defusing (ticks done)
 public BlockPos terminal=BlockPos.ZERO;
 public BlockPos goal;          // where it is heading
 public boolean carrier;        // TR with the bomb
 int shotCd,seen,repath,stuck;LivingEntity target;Vec3 lastPos=Vec3.ZERO;
 public String botName="Bot";

 public BombBot(EntityType<? extends BombBot> type,Level level){super(type,level);setPersistenceRequired();}
 public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.30).add(Attributes.FOLLOW_RANGE,64).add(Attributes.STEP_HEIGHT,1.1);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(TEAM,0);b.define(SKIN,0);b.define(WORK,0);}
 public int team(){return entityData.get(TEAM);}
 public boolean ct(){return team()==1;}
 @Override protected void registerGoals(){}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public boolean causeFallDamage(float d,float m,DamageSource s){return super.causeFallDamage(d,m,s);}

 BombMode.Terminal match(){return level().hasChunkAt(terminal)&&level().getBlockEntity(terminal) instanceof BombMode.Terminal t?t:null;}

 @Override public boolean hurt(DamageSource s,float amount){
  if(s.getEntity()!=null&&BombMode.sameTeam(s.getEntity(),this))return false;   // no friendly fire
  return super.hurt(s,amount);
 }
 @Override public void die(DamageSource s){
  var m=match();if(m!=null&&!level().isClientSide)m.killed(this,s.getEntity());
  super.die(s);
 }
 @Override protected void dropCustomDeathLoot(ServerLevel l,DamageSource s,boolean b){}
 @Override protected void dropEquipment(){}

 @Override public void aiStep(){
  super.aiStep();
  if(level().isClientSide||!isAlive())return;
  var m=match();if(m==null||!m.running()){discard();return;}
  if(m.frozen()){getNavigation().stop();return;}
  if(shotCd>0)shotCd--;
  // ---- look for an enemy
  if(tickCount%5==0){target=null;double best=48*48;
   for(var e:level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(48),x->x.isAlive()&&BombMode.enemies(this,x))){double d=distanceToSqr(e);if(d<best&&hasLineOfSight(e)){best=d;target=e;}}}
  if(target!=null&&(!target.isAlive()||!hasLineOfSight(target)))target=null;
  int work=entityData.get(WORK);
  if(target!=null&&work==0){
   seen++;getLookControl().setLookAt(target,60,60);lookAtNow(target);
   if(distanceToSqr(target)>14*14)getNavigation().moveTo(target,1.0);else getNavigation().stop();
   if(seen>8&&shotCd<=0)shoot(target);
   return;
  }
  seen=0;
  // ---- objective
  var bomb=m.bombPos();
  if(ct()&&bomb!=null){
   double d=distanceToSqr(Vec3.atCenterOf(bomb));
   if(d<2.6*2.6){getNavigation().stop();entityData.set(WORK,work+1);lookAtNow(Vec3.atCenterOf(bomb));if(work%10==0)swing(net.minecraft.world.InteractionHand.MAIN_HAND);
    if(work+1>=(getMainHandItem().isEmpty()?200:100)){m.defusedBy(this);entityData.set(WORK,0);}
    return;}
   entityData.set(WORK,0);goal=bomb;
  }else if(!ct()&&carrier&&bomb==null&&m.inSite(blockPosition())!=0){
   getNavigation().stop();entityData.set(WORK,work+1);if(work%10==0)swing(net.minecraft.world.InteractionHand.MAIN_HAND);
   if(work+1>=60){m.plant(this,blockPosition());carrier=false;entityData.set(WORK,0);}
   return;
  }else entityData.set(WORK,0);
  if(goal==null||blockPosition().distSqr(goal)<9)goal=m.nextGoal(this);
  if(goal!=null&&(--repath<=0||getNavigation().isDone())){repath=40;getNavigation().moveTo(goal.getX()+.5,goal.getY(),goal.getZ()+.5,1.0);}
  // unstick
  if(tickCount%40==0){if(position().distanceToSqr(lastPos)<.3&&goal!=null){stuck++;if(stuck>2){goal=m.nextGoal(this);stuck=0;}}else stuck=0;lastPos=position();}
 }
 void lookAtNow(Entity e){lookAtNow(e.getEyePosition());}
 void lookAtNow(Vec3 v){var d=v.subtract(getEyePosition());float yaw=(float)(Math.toDegrees(Math.atan2(d.z,d.x))-90);float pitch=(float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z)));setYRot(yaw);yHeadRot=yaw;yBodyRot=yaw;setXRot(pitch);}

 /** hitscan shot with some spread; damage and rate by weapon */
 void shoot(LivingEntity t){
  var f=WeaponFamily.of(getMainHandItem());boolean rifle=f!=null&&!f.pistol();
  float dmg=f==WeaponFamily.AK47?5.5F:f==WeaponFamily.M4A1?4.5F:f!=null&&f.pistol()?3.5F:4F;
  shotCd=rifle?4:9;
  var eye=getEyePosition();var aim=t.getBoundingBox().getCenter().add(0,t.getBbHeight()*.15,0);
  double dist=eye.distanceTo(aim);float spread=(float)(.035+dist*.0016)*(t instanceof Player p&&p.isCrouching()?1.2F:1F)*(getDeltaMovement().horizontalDistanceSqr()>.003?1.6F:1F);
  var dir=aim.subtract(eye).normalize().add(random.nextGaussian()*spread,random.nextGaussian()*spread,random.nextGaussian()*spread).normalize();
  var end=eye.add(dir.scale(64));
  var block=level().clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));
  if(block.getType()!=HitResult.Type.MISS)end=block.getLocation();
  var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(level(),this,eye,end,getBoundingBox().expandTowards(dir.scale(64)).inflate(1),x->x instanceof LivingEntity&&x!=this&&x.isAlive()&&!x.isSpectator(),0F);
  if(hit!=null&&hit.getEntity() instanceof LivingEntity le&&!BombMode.sameTeam(le,this)){
   boolean head=hit.getLocation().y>le.getEyeY()-.25;
   le.invulnerableTime=0;le.hurt(damageSources().mobAttack(this),head?dmg*2.2F:dmg);end=hit.getLocation();}
  level().playSound(null,blockPosition(),rifle?MareEletrica.SMG_SHOT.get():MareEletrica.REVOLVER_SHOT.get(),SoundSource.HOSTILE,2.5F,rifle?.85F+random.nextFloat()*.1F:1.1F);
  if(level() instanceof ServerLevel sl){var muzzle=eye.add(dir.scale(.8)).add(0,-.25,0);sl.sendParticles(ParticleTypes.SMALL_FLAME,muzzle.x,muzzle.y,muzzle.z,1,0,0,0,0);
   var step=end.subtract(muzzle);int n=(int)Math.min(20,step.length()/2);for(int i=1;i<=n;i++){var p=muzzle.add(step.scale(i/(double)n));sl.sendParticles(ParticleTypes.CRIT,p.x,p.y,p.z,1,0,0,0,0);}
   if(block.getType()==HitResult.Type.BLOCK&&end==block.getLocation())sl.sendParticles(ParticleTypes.SMOKE,end.x,end.y,end.z,2,.03,.03,.03,.01);}
  swing(net.minecraft.world.InteractionHand.MAIN_HAND);
 }

 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putLong("Terminal",terminal.asLong());t.putInt("Team",team());t.putString("BotName",botName);t.putBoolean("Carrier",carrier);t.putInt("Skin",entityData.get(SKIN));}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);terminal=BlockPos.of(t.getLong("Terminal"));entityData.set(TEAM,t.getInt("Team"));if(t.contains("BotName"))botName=t.getString("BotName");carrier=t.getBoolean("Carrier");entityData.set(SKIN,t.getInt("Skin"));}
}
