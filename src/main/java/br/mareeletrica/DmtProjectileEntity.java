package br.mareeletrica;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
public final class DmtProjectileEntity extends ThrowableItemProjectile {
 private static final EntityDataAccessor<Boolean> SPEAR=SynchedEntityData.defineId(DmtProjectileEntity.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<Boolean> SNIPER=SynchedEntityData.defineId(DmtProjectileEntity.class,EntityDataSerializers.BOOLEAN);
 private boolean revolver,smg,sweepingSniper;
 private float shotDamage=-1,shotRange=240,travelled;private boolean pellet;
 public void setFirearm(float damage,int range,boolean pellet,Item ammo){shotDamage=Math.clamp(damage,0,60);shotRange=Math.clamp(range,1,256);this.pellet=pellet;setItem(new net.minecraft.world.item.ItemStack(ammo));}
 public float firearmDamage(){return shotDamage;}
 public boolean isPellet(){return pellet;}
 private final java.util.Set<java.util.UUID> struck=new java.util.HashSet<>();
 private final java.util.Set<net.minecraft.core.BlockPos> crossed=new java.util.HashSet<>();
 public boolean canStrike(Entity entity){return !struck.contains(entity.getUUID())&&canHitEntity(entity);}
 public void setSmg(boolean value){smg=value;if(value)setItem(new net.minecraft.world.item.ItemStack(MareEletrica.SMG_ROUND.get()));}
 public void setRevolver(boolean value){revolver=value;if(value)setItem(new net.minecraft.world.item.ItemStack(MareEletrica.REVOLVER_ROUND.get()));}
 private int fuse=-1;private java.util.UUID attached;private Vec3 offset=Vec3.ZERO;
 public DmtProjectileEntity(EntityType<? extends DmtProjectileEntity> type,Level level){super(type,level);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(SPEAR,false);b.define(SNIPER,false);}
 public void setSpear(boolean value){entityData.set(SPEAR,value);setItem(new net.minecraft.world.item.ItemStack(value?MareEletrica.THUNDER_SPEAR.get():MareEletrica.DMT_CARTRIDGE.get()));}
 public void setSniper(boolean value){entityData.set(SNIPER,value);if(value)setItem(new net.minecraft.world.item.ItemStack(MareEletrica.SNIPER_ROUND.get()));}
 public boolean isSniper(){return entityData.get(SNIPER);}
 public boolean isSpear(){return entityData.get(SPEAR);}
 // Called while Entity is constructing its data table: never read entityData here.
 @Override protected Item getDefaultItem(){return MareEletrica.DMT_CARTRIDGE.get();}
 @Override protected double getDefaultGravity(){return isSpear()?.018:isSniper()?.015:0;}
 @Override protected boolean canHitEntity(Entity e){return !sweepingSniper&&fuse<0&&super.canHitEntity(e);}
 @Override protected void onHit(HitResult result){if(level().isClientSide||fuse>=0||sweepingSniper)return;if(result instanceof EntityHitResult hit){var target=hit.getEntity();if(revolver||smg||shotDamage>=0)target.invulnerableTime=0;target.hurt(damageSources().thrown(this,getOwner()),shotDamage>=0?shotDamage*(pellet?Math.max(.2F,1F-(travelled+(float)position().distanceTo(hit.getLocation()))/shotRange):1F):isSpear()?12:isSniper()?SniperBallistics.damage(target):smg?3F:revolver?12F:18F);if(isSniper()){struck.add(target.getUUID());if(struck.size()<64&&SniperBallistics.pierceable(target))return;}if(isSpear()){attached=target.getUUID();offset=result.getLocation().subtract(target.position());}}
  if(isSpear()){setPos(result.getLocation());setDeltaMovement(Vec3.ZERO);setNoGravity(true);fuse=30;}else discard();
 }
 public long lastServerTick=Long.MIN_VALUE;
 @Override public void tick(){Vec3 previousPosition=position();if(!level().isClientSide)lastServerTick=level().getGameTime();if(fuse>=0){setDeltaMovement(Vec3.ZERO);setNoGravity(true);}if(isSniper()&&!level().isClientSide){for(int i=0;i<16;i++){var hit=SniperBallistics.trace(this,crossed);if(hit.getType()==HitResult.Type.MISS)break;
 boolean cancelled=net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this,hit);
 if(!cancelled){hitTargetOrDeflectSelf(hit);if(isRemoved())return;}
 if(hit instanceof EntityHitResult eh){if(cancelled)struck.add(eh.getEntity().getUUID());else if(!struck.contains(eh.getEntity().getUUID()))break;}else break;
 }sweepingSniper=true;try{super.tick();}finally{sweepingSniper=false;}}else super.tick();if(shotDamage>=0){travelled+=(float)position().distanceTo(previousPosition);if(travelled>=shotRange)discard();}if(level() instanceof ServerLevel server){if(fuse>=0){if(attached!=null){var e=server.getEntity(attached);if(e!=null&&e.isAlive())setPos(e.position().add(offset));}server.sendParticles(ParticleTypes.ELECTRIC_SPARK,getX(),getY(),getZ(),2,.1,.1,.1,0);if(--fuse<=0){server.explode(this,getX(),getY(),getZ(),3.8F,Level.ExplosionInteraction.TNT);discard();}}else if(tickCount>(isSpear()?160:isSniper()?40:20))discard();}else if(isSpear()&&getDeltaMovement().lengthSqr()>.01){Vec3 back=getDeltaMovement().normalize().scale(-.8);for(int i=0;i<3;i++)level().addParticle(ParticleTypes.SMOKE,getX()+back.x,getY()+back.y,getZ()+back.z,back.x*.12,back.y*.12,back.z*.12);level().addParticle(ParticleTypes.FLAME,getX()+back.x,getY()+back.y,getZ()+back.z,back.x*.04,back.y*.04,back.z*.04);}}
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);tag.putFloat("ShotDamage",shotDamage);tag.putFloat("ShotRange",shotRange);tag.putFloat("Travelled",travelled);tag.putBoolean("Pellet",pellet);tag.putBoolean("Revolver",revolver);tag.putBoolean("Smg",smg);var blocks=new net.minecraft.nbt.ListTag();for(var pos:crossed)blocks.add(net.minecraft.nbt.LongTag.valueOf(pos.asLong()));tag.put("Crossed",blocks);var victims=new net.minecraft.nbt.ListTag();for(var id:struck)victims.add(net.minecraft.nbt.StringTag.valueOf(id.toString()));tag.put("Struck",victims);tag.putBoolean("Spear",isSpear());tag.putBoolean("Sniper",isSniper());tag.putInt("Fuse",fuse);if(attached!=null)tag.putUUID("Attached",attached);tag.putDouble("OX",offset.x);tag.putDouble("OY",offset.y);tag.putDouble("OZ",offset.z);}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);var savedAmmunition=getItem().copy();shotDamage=tag.contains("ShotDamage")?Math.clamp(tag.getFloat("ShotDamage"),-1,60):-1;shotRange=Math.clamp(tag.getFloat("ShotRange"),1,256);travelled=Math.max(0,tag.getFloat("Travelled"));pellet=tag.getBoolean("Pellet");setRevolver(tag.getBoolean("Revolver"));setSpear(tag.getBoolean("Spear"));setSniper(tag.getBoolean("Sniper"));setSmg(tag.getBoolean("Smg"));if(shotDamage>=0)setItem(savedAmmunition);struck.clear();for(var v:tag.getList("Struck",net.minecraft.nbt.Tag.TAG_STRING)){if(struck.size()>=64)break;try{struck.add(java.util.UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}}crossed.clear();for(var value:tag.getList("Crossed",net.minecraft.nbt.Tag.TAG_LONG))if(crossed.size()<4)crossed.add(net.minecraft.core.BlockPos.of(((net.minecraft.nbt.LongTag)value).getAsLong()));fuse=tag.getInt("Fuse");attached=tag.hasUUID("Attached")?tag.getUUID("Attached"):null;offset=new Vec3(tag.getDouble("OX"),tag.getDouble("OY"),tag.getDouble("OZ"));}
}
