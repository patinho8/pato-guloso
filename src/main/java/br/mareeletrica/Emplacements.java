package br.mareeletrica;

import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** Crewed weapons: a heavy machine-gun turret and a big 3x3 field cannon. Sit on them, aim with the mouse, click to fire.
 *  The cannon can also be linked to the aim remote (Shift + right-click it with the remote). */
public final class Emplacements {
 public static final DeferredItem<PlaceItem> TURRET_ITEM=MareEletrica.ITEMS.register("gun_turret",()->new PlaceItem(new Item.Properties().stacksTo(1),true));
 public static final DeferredItem<PlaceItem> CANNON_ITEM=MareEletrica.ITEMS.register("field_cannon",()->new PlaceItem(new Item.Properties().stacksTo(1),false));
 public static final Supplier<EntityType<Mounted>> TURRET=MareEletrica.ACTORS.register("gun_turret",()->
  EntityType.Builder.<Mounted>of((t,l)->new Mounted(t,l,true),MobCategory.MISC).sized(1.8F,2.1F).clientTrackingRange(10).updateInterval(2).build(MareEletrica.ID+":gun_turret"));
 public static final Supplier<EntityType<Mounted>> CANNON=MareEletrica.ACTORS.register("field_cannon",()->
  EntityType.Builder.<Mounted>of((t,l)->new Mounted(t,l,false),MobCategory.MISC).sized(2.6F,2.4F).clientTrackingRange(10).updateInterval(2).build(MareEletrica.ID+":field_cannon"));
 private Emplacements(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(TURRET_ITEM.get());out.accept(CANNON_ITEM.get());}

 public static final class PlaceItem extends Item {
  private final boolean turret;
  public PlaceItem(Properties p,boolean turret){super(p);this.turret=turret;}
  @Override public InteractionResult useOn(UseOnContext c){
   var level=c.getLevel();
   if(!level.isClientSide){
    var m=new Mounted(turret?TURRET.get():CANNON.get(),level,turret);var pos=c.getClickedPos().relative(c.getClickedFace());
    m.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);m.setYRot(c.getRotation());m.aimYaw=c.getRotation();
    if(!level.noCollision(m,m.getBoundingBox())){if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("Sem espaço aqui."),true);return InteractionResult.FAIL;}
    level.addFreshEntity(m);if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
 }

 public static final class Mounted extends Entity {
  private static final EntityDataAccessor<Float> AIM_YAW=SynchedEntityData.defineId(Mounted.class,EntityDataSerializers.FLOAT),AIM_PITCH=SynchedEntityData.defineId(Mounted.class,EntityDataSerializers.FLOAT);
  private static final EntityDataAccessor<Integer> AMMO=SynchedEntityData.defineId(Mounted.class,EntityDataSerializers.INT),FIRED=SynchedEntityData.defineId(Mounted.class,EntityDataSerializers.INT);
  public final boolean turret;public float aimYaw,aimPitch,yawO,pitchO,yawR,pitchR;public boolean trigger;private int cooldown;private float health;private long triggerAt;
  public Mounted(EntityType<? extends Mounted> t,Level l,boolean turret){super(t,l);this.turret=turret;health=turret?40:80;}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(AIM_YAW,0F);b.define(AIM_PITCH,0F);b.define(AMMO,0);b.define(FIRED,0);}
  public int ammo(){return entityData.get(AMMO);}
  public int firedTick(){return entityData.get(FIRED);}
  public int maxAmmo(){return turret?1000:32;}
  public Item ammoItem(){return turret?Arsenal.RIFLE_ROUND.get():MareEletrica.CANNONBALL.get();}
  @Override public LivingEntity getControllingPassenger(){return null;}   // it does not move: the rider only aims
  @Override protected boolean canAddPassenger(Entity e){return getPassengers().isEmpty();}
  @Override public boolean isPickable(){return !isRemoved();}
  @Override public boolean canBeCollidedWith(){return true;}
  @Override public boolean isPushable(){return false;}
  @Override public ItemStack getPickResult(){return new ItemStack(turret?TURRET_ITEM.get():CANNON_ITEM.get());}
  @Override protected void positionRider(Entity rider,Entity.MoveFunction move){
   if(!hasPassenger(rider))return;
   // the seat turns with the gun: it sits right behind the breech, facing where the gun points
   float yaw=level().isClientSide?yawR:aimYaw;double r=Math.toRadians(yaw);Vec3 back=new Vec3(Math.sin(r),0,-Math.cos(r)).scale(turret?1.25:1.55);
   move.accept(rider,getX()+back.x,getY()+(turret?.2:.38),getZ()+back.z);
  }
  @Override public InteractionResult interact(Player p,InteractionHand hand){
   var held=p.getItemInHand(hand);
   if(level().isClientSide)return InteractionResult.SUCCESS;
   if(Batteries.AmmoCrate.holds(held,ammoItem())){int n=Batteries.AmmoCrate.feed(held,ammoItem(),maxAmmo()-ammo(),p);if(n>0){entityData.set(AMMO,ammo()+n);level().playSound(null,blockPosition(),SoundEvents.ARMOR_EQUIP_IRON.value(),SoundSource.NEUTRAL,1F,.8F);}
    p.displayClientMessage(Component.literal("Munição: "+ammo()+"/"+maxAmmo()),true);return InteractionResult.CONSUME;}
   if(held.is(MareEletrica.AIM_REMOTE.get())&&!turret&&p.isShiftKeyDown()){AimRemoteItem.linkCannon(held,this,p);return InteractionResult.CONSUME;}
   if(p.isShiftKeyDown()&&held.isEmpty()&&getPassengers().isEmpty()){
    var s=new ItemStack(turret?TURRET_ITEM.get():CANNON_ITEM.get());if(!p.getInventory().add(s))p.drop(s,false);
    if(ammo()>0){var a=Batteries.AmmoCrate.of(ammoItem(),ammo());if(!p.getInventory().add(a))p.drop(a,false);}   // all of it comes back, in a crate
    discard();return InteractionResult.CONSUME;
   }
   if(getPassengers().isEmpty()){p.startRiding(this);p.displayClientMessage(Component.literal((turret?"Metralhadora":"Canhão")+": mire com o mouse • clique esquerdo atira • Shift sai • munição "+ammo()+"/"+maxAmmo()+" (clique com "+(turret?"balas de fuzil":"balas de canhão")+" para carregar)"),true);}
   return InteractionResult.CONSUME;
  }
  @Override public boolean hurt(DamageSource s,float amount){
   if(level().isClientSide||isRemoved()||isInvulnerableTo(s))return false;
   if(s.getEntity()!=null&&hasPassenger(s.getEntity()))return false;
   health-=amount;level().playSound(null,blockPosition(),SoundEvents.ANVIL_LAND,SoundSource.NEUTRAL,.3F,1.6F);
   if(health<=0||s.getEntity() instanceof Player pl&&pl.isCreative()){ejectPassengers();if(!(s.getEntity() instanceof Player pl2&&pl2.isCreative())){spawnAtLocation(getPickResult());if(ammo()>0)spawnAtLocation(Batteries.AmmoCrate.of(ammoItem(),ammo()));}discard();}
   return true;
  }
  /** Aim from outside (the aim remote). */
  public void aim(float yaw,float pitch){aimYaw=yaw;aimPitch=Mth.clamp(pitch,turret?-35:-60,turret?25:5);entityData.set(AIM_YAW,aimYaw);entityData.set(AIM_PITCH,aimPitch);}
  @Override public void tick(){
   super.tick();
   if(!onGround()){setDeltaMovement(getDeltaMovement().add(0,-.08,0));move(MoverType.SELF,getDeltaMovement());}else setDeltaMovement(Vec3.ZERO);
   if(level().isClientSide){
    yawO=yawR;pitchO=pitchR;float ty=entityData.get(AIM_YAW),tp=entityData.get(AIM_PITCH);
    float sp=turret?20F:4F;yawR+=Mth.clamp(Mth.wrapDegrees(ty-yawR),-sp,sp);pitchR+=Mth.clamp(tp-pitchR,-sp,sp);
    return;
   }
   if(getFirstPassenger() instanceof Player p){float sp=turret?20F:4F;aim(aimYaw+Mth.clamp(Mth.wrapDegrees(p.getYRot()-aimYaw),-sp,sp),p.getXRot());}
   if(cooldown>0)cooldown--;
   if(trigger&&level().getGameTime()-triggerAt>6)trigger=false;
   if(trigger&&cooldown==0)fire(getFirstPassenger() instanceof Player p?p:null);
  }
  /** Muzzle position: in front of the pivot along the aim. */
  public Vec3 aimDir(){return Vec3.directionFromRotation(aimPitch,aimYaw);}
  public Vec3 muzzle(){return position().add(0,turret?1.15:1.5,0).add(aimDir().scale(turret?2.2:2.6));}
  public boolean fire(Player shooter){
   if(level().isClientSide||cooldown>0)return false;
   boolean free=shooter!=null&&shooter.isCreative();
   if(ammo()<=0&&!free){if(shooter!=null)shooter.displayClientMessage(Component.literal("§cSem munição — clique com "+(turret?"balas de fuzil":"balas de canhão")),true);cooldown=20;return false;}
   if(!free)entityData.set(AMMO,ammo()-1);
   entityData.set(FIRED,tickCount);
   var from=muzzle();var dir=aimDir();var sl=(ServerLevel)level();
   if(turret){
    cooldown=2;
    dir=dir.add(random.nextGaussian()*.012,random.nextGaussian()*.012,random.nextGaussian()*.012).normalize();
    Tracers.fire(level(),this,shooter,from,dir,5.5F,7F,null);
    sl.sendParticles(ParticleTypes.FLAME,from.x,from.y,from.z,1,0,0,0,0);sl.sendParticles(ParticleTypes.SMOKE,from.x,from.y,from.z,2,.05,.05,.05,.01);
    level().playSound(null,from.x,from.y,from.z,MareEletrica.JET_GUN.get(),SoundSource.NEUTRAL,1.2F,1.1F+random.nextFloat()*.15F);
   }else{
    cooldown=50;
    var ball=new CannonballEntity(MareEletrica.SHELL.get(),level());ball.setPos(from.x,from.y,from.z);ball.setOwner(shooter);ball.shoot(dir.x,dir.y,dir.z,3.6F,.4F);level().addFreshEntity(ball);
    sl.sendParticles(ParticleTypes.EXPLOSION,from.x,from.y,from.z,2,.2,.2,.2,0);sl.sendParticles(ParticleTypes.LARGE_SMOKE,from.x,from.y,from.z,30,.6,.4,.6,.06);
    level().playSound(null,from.x,from.y,from.z,MareEletrica.C4_BLAST.get(),SoundSource.NEUTRAL,1.2F,1.5F);
    var push=dir.scale(-.25);if(getFirstPassenger()!=null)getFirstPassenger().hurtMarked=true;
   }
   return true;
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putInt("Ammo",ammo());t.putFloat("Health",health);t.putFloat("AimYaw",aimYaw);t.putFloat("AimPitch",aimPitch);}
  @Override protected void readAdditionalSaveData(CompoundTag t){entityData.set(AMMO,t.getInt("Ammo"));if(t.contains("Health"))health=t.getFloat("Health");aim(t.getFloat("AimYaw"),t.getFloat("AimPitch"));}
 }

 /** Rider holds the attack button: the client says so a few times a second. */
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  public record Trigger(boolean down) implements CustomPacketPayload {
   public static final Type<Trigger> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"emplacement_trigger"));
   public static final StreamCodec<RegistryFriendlyByteBuf,Trigger> CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,Trigger::down,Trigger::new);
   @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
  }
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
   e.registrar("11").playToServer(Trigger.TYPE,Trigger.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer sp&&sp.getVehicle() instanceof Mounted m){m.trigger=p.down();m.triggerAt=m.level().getGameTime();}});
  }
 }
}
