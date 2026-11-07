package br.mareeletrica;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.effect.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/** Powered guardian: only hostile entities are eligible for every weapon. */
public final class ExterminatorEntity extends PathfinderMob {
 private static final EntityDataAccessor<Boolean> GIANT=SynchedEntityData.defineId(ExterminatorEntity.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<Integer> WEAPON=SynchedEntityData.defineId(ExterminatorEntity.class,EntityDataSerializers.INT);
 public BlockPos home=BlockPos.ZERO;private int cooldown;public long giantUntil;private int shotPhase;public float animation;
 public ExterminatorEntity(EntityType<? extends ExterminatorEntity> type,Level level){super(type,level);setPersistenceRequired();}
 public static void attributes(EntityAttributeCreationEvent e){e.put(MareEletrica.EXTERMINATOR.get(),createMobAttributes().add(Attributes.MAX_HEALTH,400).add(Attributes.ARMOR,20).add(Attributes.MOVEMENT_SPEED,.38).add(Attributes.FOLLOW_RANGE,64).add(Attributes.STEP_HEIGHT,1.5).add(Attributes.KNOCKBACK_RESISTANCE,1).build());}
 protected void registerGoals(){}
 protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(GIANT,false);b.define(WEAPON,0);}
 public boolean giant(){return entityData.get(GIANT);}public int weapon(){return entityData.get(WEAPON);}
 public void enlarge(){giantUntil=level().getGameTime()+6000;entityData.set(GIANT,true);refreshDimensions();setHealth(getMaxHealth());getNavigation().stop();}
 @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key.equals(GIANT))refreshDimensions();}
 @Override public EntityDimensions getDefaultDimensions(Pose pose){return giant()?EntityDimensions.scalable(5,5):super.getDefaultDimensions(pose);}
 @Override public boolean causeFallDamage(float distance,float multiplier,net.minecraft.world.damagesource.DamageSource source){return false;}
 private MachineEntity base(){return level().hasChunkAt(home)&&level().getBlockEntity(home) instanceof MachineEntity m&&m.kind==Kind.EXTERMINATOR?m:null;}
 public static boolean hostile(Entity e){return e instanceof LivingEntity l&&e instanceof Enemy&&l.isAlive();}
 public static boolean baseTick(MachineEntity m){
  if(!(m.getLevel() instanceof ServerLevel level))return false;
  if(m.roverId==null){
   if(m.clean.stored()<10000){m.status=2;return false;}
   Direction front=m.getBlockState().getValue(MachineBlock.FACING);BlockPos pos=m.getBlockPos().relative(front,4).relative(front.getClockWise(),2);
   if(!level.hasChunkAt(pos)){m.status=30;return false;}
   ExterminatorEntity e=new ExterminatorEntity(MareEletrica.EXTERMINATOR.get(),level);e.home=m.getBlockPos();e.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);
   if(!level.noCollision(e)){m.status=41;return false;}
   if(!level.addFreshEntity(e))return false;m.roverId=e.getUUID();m.roverPosition=e.blockPosition();m.clean.pay(10000);m.setChanged();
  }
  if(!(level.getEntity(m.roverId) instanceof ExterminatorEntity e)){m.status=30;return false;}
  int cost=e.giant()?0:1000;if(!m.clean.pay(cost)){m.status=2;return false;}m.lastRate=cost*2;m.status=1;m.roverPosition=e.blockPosition();m.setChanged();return true;
 }
 public static boolean enlarge(MachineEntity m,Player player){
  if(!(m.getLevel() instanceof ServerLevel l)||m.roverId==null||!(l.getEntity(m.roverId) instanceof ExterminatorEntity e))return false;
  if(e.giant())return true;var gas=m.gasInventory.getStackInSlot(0);
  if(!gas.is(MareEletrica.GAS.get())||gas.getCount()<20){player.displayClientMessage(net.minecraft.network.chat.Component.literal("Coloque 20 gás na base para transformar."),true);return true;}
  if(e.getY()+5>l.getMaxBuildHeight()){player.displayClientMessage(net.minecraft.network.chat.Component.literal("É preciso espaço até o limite de altura para o devorador."),true);return true;}
  if(!l.noCollision(e,new AABB(e.getX()-2.5,e.getY(),e.getZ()-2.5,e.getX()+2.5,e.getY()+5,e.getZ()+2.5))){player.displayClientMessage(net.minecraft.network.chat.Component.literal("Libere uma área de 5 × 5 × 5 para transformar."),true);return true;}
  var empty=m.inventory.getStackInSlot(0);if(!empty.isEmpty()&&(!empty.is(MareEletrica.EMPTY_GAS.get())||empty.getCount()>44)){player.displayClientMessage(net.minecraft.network.chat.Component.literal("Retire os cilindros vazios da saída."),true);return true;}m.gasInventory.extractItem(0,20,false);m.inventory.setStackInSlot(0,new net.minecraft.world.item.ItemStack(MareEletrica.EMPTY_GAS.get(),empty.getCount()+20));e.enlarge();m.setChanged();l.sendParticles(ParticleTypes.ELECTRIC_SPARK,e.getX(),e.getY()+6,e.getZ(),150,5,5,5,.3);return true;
 }
 @Override public void tick(){
  super.tick();animation+=1;if(!(level() instanceof ServerLevel l))return;MachineEntity m=base();
  if(m==null){if(l.hasChunkAt(home))discard();return;}if(!getUUID().equals(m.roverId)){discard();return;}
  if(giant()&&giantUntil<=l.getGameTime()){m.enabled=false;m.syncAim();l.sendParticles(ParticleTypes.CLOUD,getX(),getY()+2.5,getZ(),40,2,2,2,.1);discard();return;}
  if(!m.enabled||m.status!=1||l.hasNeighborSignal(home)){getNavigation().stop();return;}
  if(cooldown>0)cooldown--;
  if(giant()){
   LivingEntity prey=l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(36),ExterminatorEntity::hostile).stream().filter(e->e.position().distanceToSqr(home.getCenter())<=96*96).min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
   if(prey==null){entityData.set(WEAPON,0);if(distanceToSqr(home.getCenter())>256)getNavigation().moveTo(home.getX()+2.5,home.getY(),home.getZ()-6,1.2);else getNavigation().stop();return;}
   Vec3 delta=prey.position().subtract(position());setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));yBodyRot=yHeadRot=getYRot();
   if(tickCount%10==0)getNavigation().moveTo(prey,1.35);entityData.set(WEAPON,distanceToSqr(prey)<36?4:0);
   if(getBoundingBox().inflate(.8).intersects(prey.getBoundingBox())&&hasLineOfSight(prey)&&cooldown==0){getNavigation().stop();cooldown=16;l.sendParticles(ParticleTypes.SWEEP_ATTACK,prey.getX(),prey.getY()+.8,prey.getZ(),4,.5,.5,.5,0);prey.hurt(damageSources().mobAttack(this),Math.max(60,prey.getMaxHealth()*1.5F));if(!prey.isAlive()){heal(12);l.sendParticles(ParticleTypes.CLOUD,prey.getX(),prey.getY()+.5,prey.getZ(),20,.6,.4,.6,.04);}}
   return;
  }
  LivingEntity target=l.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(32),ExterminatorEntity::hostile).stream().filter(e->e.position().distanceToSqr(home.getCenter())<=64*64).min(java.util.Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
  if(target==null){if(distanceToSqr(home.getCenter())>144)getNavigation().moveTo(home.getX()+2.5,home.getY(),home.getZ()-4,1);else getNavigation().stop();return;}
  Vec3 aim=target.position().subtract(position());setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));yBodyRot=yHeadRot=getYRot();
  int weapon=target instanceof EnderMan?3:target.fireImmune()?2:target.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)?1:0;entityData.set(WEAPON,weapon);
  double distance=distanceToSqr(target);
  if(distance>12){if(tickCount%10==0)getNavigation().moveTo(target,1.1);if(tickCount%40==0&&distance<256&&hasLineOfSight(target)){Vec3 dash=new Vec3(aim.x,0,aim.z).normalize().scale(1.4);setDeltaMovement(dash.x,.18,dash.z);hurtMarked=true;l.sendParticles(ParticleTypes.CLOUD,getX(),getY()+.5,getZ(),12,.5,.2,.5,.05);}}
  if(cooldown>0||!hasLineOfSight(target))return;
  if(weapon==3&&distance>16)return;
  if(distance<=16){target.hurt(damageSources().mobAttack(this),24);l.sendParticles(ParticleTypes.SWEEP_ATTACK,target.getX(),target.getY()+1,target.getZ(),2,.4,.4,.4,0);}
  else{RoverBulletEntity shot=new RoverBulletEntity(MareEletrica.BULLET.get(),l);shot.setOwner(this);shot.roverId=getId();Vec3 direction=target.getEyePosition().subtract(getEyePosition()).normalize();Vec3 muzzle=getEyePosition().add(direction.scale(1.8));shot.setPos(muzzle.x,muzzle.y,muzzle.z);shot.setDeltaMovement(direction.scale(3.5));l.addFreshEntity(shot);beam(l,target,weapon==1?ParticleTypes.FLAME:weapon==2?ParticleTypes.SNOWFLAKE:ParticleTypes.ELECTRIC_SPARK);}
  cooldown=distance<=16?10:(++shotPhase%3==0?6:7);
 }
 public void hit(LivingEntity target){if(!hostile(target))return;if(weapon()==1)target.igniteForSeconds(5);if(weapon()==2)target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,2));}
 private void beam(ServerLevel l,LivingEntity target,net.minecraft.core.particles.SimpleParticleType particle){Vec3 start=getEyePosition();if(giant())start=position().add(0,24,0);Vec3 delta=target.getEyePosition().subtract(start);for(int i=0;i<12;i++){Vec3 v=start.add(delta.scale(i/12.0));l.sendParticles(particle,v.x,v.y,v.z,1,0,0,0,0);}}
 @Override public void remove(RemovalReason r){if(!level().isClientSide&&(r==RemovalReason.KILLED||r==RemovalReason.DISCARDED)){var m=base();if(m!=null&&getUUID().equals(m.roverId)){m.roverId=null;m.setChanged();}}super.remove(r);}
 @Override public boolean isPushable(){return !giant()&&super.isPushable();}
 @Override public void push(Entity e){if(!giant())super.push(e);}
 @Override protected void doPush(Entity e){if(!giant())super.doPush(e);}
 @Override protected void pushEntities(){if(!giant())super.pushEntities();}
 @Override public void push(double x,double y,double z){if(!giant())super.push(x,y,z);}
 public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putLong("Home",home.asLong());t.putBoolean("Giant",giant());t.putLong("GiantUntil",giantUntil);}
 public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);home=BlockPos.of(t.getLong("Home"));entityData.set(GIANT,t.getBoolean("Giant"));giantUntil=t.contains("GiantUntil")?Math.min(t.getLong("GiantUntil"),level().getGameTime()+6000):level().getGameTime()+6000;refreshDimensions();}
}
