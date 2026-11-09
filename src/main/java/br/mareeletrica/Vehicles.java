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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredItem;

/** Helicopter (hovers, 4 seats, nose gun + rockets) and battle tank (tracks, turret that follows your aim, main cannon + coaxial gun).
 *  The driver's game simulates the movement (like boats); the server owns weapons, damage and ammo. */
public final class Vehicles {
 public static final DeferredItem<PlaceItem> HELI_ITEM=MareEletrica.ITEMS.register("helicopter",()->new PlaceItem(new Item.Properties().stacksTo(1),true));
 public static final DeferredItem<PlaceItem> TANK_ITEM=MareEletrica.ITEMS.register("battle_tank",()->new PlaceItem(new Item.Properties().stacksTo(1),false));
 public static final Supplier<EntityType<Helicopter>> HELI=MareEletrica.ACTORS.register("helicopter",()->
  EntityType.Builder.<Helicopter>of(Helicopter::new,MobCategory.MISC).sized(5.4F,4.0F).clientTrackingRange(16).updateInterval(1).fireImmune().build(MareEletrica.ID+":helicopter"));
 public static final Supplier<EntityType<Tank>> TANK=MareEletrica.ACTORS.register("battle_tank",()->
  EntityType.Builder.<Tank>of(Tank::new,MobCategory.MISC).sized(5.2F,3.6F).clientTrackingRange(16).updateInterval(1).fireImmune().build(MareEletrica.ID+":battle_tank"));
 private Vehicles(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(HELI_ITEM.get());out.accept(TANK_ITEM.get());}

 /** Something the hangar can repair and rearm. Returns true when it did any work this call. */
 public interface Serviceable { boolean service(); }

 public static final class PlaceItem extends Item {
  private final boolean heli;
  public PlaceItem(Properties p,boolean heli){super(p);this.heli=heli;}
  @Override public InteractionResult useOn(UseOnContext c){
   var level=c.getLevel();
   if(!level.isClientSide){
    Base v=heli?new Helicopter(HELI.get(),level):new Tank(TANK.get(),level);var pos=c.getClickedPos().relative(c.getClickedFace());
    v.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);v.setYRot(c.getRotation());v.yRotO=v.getYRot();
    if(!level.noCollision(v,v.getBoundingBox())){if(c.getPlayer()!=null)c.getPlayer().displayClientMessage(Component.literal("Sem espaço aqui."),true);return InteractionResult.FAIL;}
    level.addFreshEntity(v);if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
 }

 /** Shared: health, damage, driver input, smooth movement for other players, pick-up. */
 public abstract static class Base extends Entity implements Serviceable {
  protected static final EntityDataAccessor<Float> HEALTH=SynchedEntityData.defineId(Base.class,EntityDataSerializers.FLOAT);
  protected static final EntityDataAccessor<Integer> AMMO=SynchedEntityData.defineId(Base.class,EntityDataSerializers.INT);
  protected static final EntityDataAccessor<Integer> FIRED=SynchedEntityData.defineId(Base.class,EntityDataSerializers.INT);
  /** 0.65.1: a knocked-out tank is a burning wreck (ticks since it was knocked out, -1 = fine): no driving, no guns, explodes after 3 min. */
  protected static final EntityDataAccessor<Integer> WRECK=SynchedEntityData.defineId(Base.class,EntityDataSerializers.INT);
  public static final int WRECK_TICKS=3600;
  public boolean wrecked(){return entityData.get(WRECK)>=0;}
  /** Vehicles that become a burning wreck instead of blowing up at once. */
  protected boolean wrecks(){return false;}
  /** Driver input (set on the driver's client before the tick). */
  public float inForward,inTurn,inLift;public boolean inFire,inAlt;
  /** Server: trigger state reported by the driver. */
  public boolean fireHeld,altHeld;private long inputAt;
  protected int gunCooldown,altCooldown,serviceCount;private int lerpSteps;private double lx,ly,lz;private float lyr;
  /** Yaw change this tick, applied to the riders so they turn with the vehicle. */
  public float deltaYaw;
  /** Horizontal distance moved last tick (works for the driver, the server and other players alike). */
  public double moved;private Vec3 lastPos;
  protected Base(EntityType<?> t,Level l){super(t,l);}
  protected abstract float baseHealth();
  protected abstract int baseAmmo();
  public abstract int maxFuel();
  protected static final EntityDataAccessor<Integer> UPG=SynchedEntityData.defineId(Base.class,EntityDataSerializers.INT);
  protected static final EntityDataAccessor<Integer> FUEL=SynchedEntityData.defineId(Base.class,EntityDataSerializers.INT);
  public static final String[] PAINTS={"Verde oliva","Deserto","Floresta","Neve","Preto","Urbano","Marinha"};
  public static final String[] UPGRADES={"Blindagem","Motor","Munição"};
  public int paint(){return entityData.get(UPG)&15;}
  public void setPaint(int p){entityData.set(UPG,(entityData.get(UPG)&~15)|Math.floorMod(p,PAINTS.length));}
  /** Upgrade level 0..3: 0 = armour, 1 = engine, 2 = ammo. */
  public int upgrade(int which){return entityData.get(UPG)>>(4+which*4)&15;}
  public boolean addUpgrade(int which){int lv=upgrade(which);if(lv>=3)return false;int u=entityData.get(UPG);u=(u&~(15<<(4+which*4)))|((lv+1)<<(4+which*4));entityData.set(UPG,u);if(which==0)entityData.set(HEALTH,health()+baseHealth()*.25F);return true;}
  public float maxHealth(){return baseHealth()*(1+.25F*upgrade(0));}
  public int maxAmmo(){return Math.round(baseAmmo()*(1+.5F*upgrade(2)));}
  public float speedMul(){return 1+.12F*upgrade(1);}
  public int fuel(){return entityData.get(FUEL);}
  public void addFuel(int n){entityData.set(FUEL,Mth.clamp(fuel()+n,0,maxFuel()));}
  /** Autopilot target (garage remote / base panel), null = none. */
  public Vec3 autoTarget;
  protected abstract void autopilot();
  public abstract Item ammoItem();
  public abstract ItemStack pick();
  protected abstract void drive();
  protected abstract void weapons(Player driver);
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(HEALTH,baseHealth());b.define(AMMO,0);b.define(FIRED,-100);b.define(UPG,0);b.define(FUEL,maxFuel());b.define(WRECK,-1);}
  public float health(){return entityData.get(HEALTH);}
  public int ammo(){return entityData.get(AMMO);}
  public int firedTick(){return entityData.get(FIRED);}
  @Override public LivingEntity getControllingPassenger(){return getFirstPassenger() instanceof Player p?p:null;}
  @Override public boolean isPickable(){return !isRemoved();}
  @Override public boolean canBeCollidedWith(){return false;}
  @Override public boolean isPushable(){return false;}
  @Override public ItemStack getPickResult(){return pick();}
  @Override public net.minecraft.world.phys.AABB getBoundingBoxForCulling(){return getBoundingBox().inflate(3,2,3);}
  @Override public void lerpTo(double x,double y,double z,float yr,float xr,int steps){lx=x;ly=y;lz=z;lyr=yr;lerpSteps=Math.max(steps,3);}
  @Override public Vec3 getDismountLocationForPassenger(LivingEntity e){var s=super.getDismountLocationForPassenger(e);return s.y<getY()?new Vec3(s.x,getY()+getBbHeight(),s.z):s;}
  public void input(ServerPlayer p,boolean fire,boolean alt){if(getControllingPassenger()!=p)return;fireHeld=fire;altHeld=alt;inputAt=level().getGameTime();}
  @Override public InteractionResult interact(Player p,InteractionHand hand){
   var held=p.getItemInHand(hand);
   if(level().isClientSide)return InteractionResult.SUCCESS;
   if(held.is(ammoItem())){int n=Math.min(held.getCount(),maxAmmo()-ammo());if(n>0){entityData.set(AMMO,ammo()+n);if(!p.isCreative())held.shrink(n);level().playSound(null,blockPosition(),SoundEvents.ARMOR_EQUIP_IRON.value(),SoundSource.NEUTRAL,1F,.8F);}
    p.displayClientMessage(Component.literal("Munição: "+ammo()+"/"+maxAmmo()),true);return InteractionResult.CONSUME;}
   if(held.getItem() instanceof Garage.KitItem k){
    if(k.kind<0){if(fuel()>=maxFuel()){p.displayClientMessage(Component.literal("Tanque cheio."),true);return InteractionResult.CONSUME;}addFuel(maxFuel()/3);if(!p.isCreative())held.shrink(1);level().playSound(null,blockPosition(),SoundEvents.BUCKET_EMPTY,SoundSource.NEUTRAL,1F,.8F);p.displayClientMessage(Component.literal("Combustível "+fuel()*100/maxFuel()+"%"),true);return InteractionResult.CONSUME;}
    if(addUpgrade(k.kind)){if(!p.isCreative())held.shrink(1);level().playSound(null,blockPosition(),SoundEvents.ANVIL_USE,SoundSource.NEUTRAL,1F,1.2F);p.displayClientMessage(Component.literal("§a"+UPGRADES[k.kind]+" nível "+upgrade(k.kind)+"/3"),true);}
    else p.displayClientMessage(Component.literal(UPGRADES[k.kind]+" já está no máximo."),true);
    return InteractionResult.CONSUME;
   }
   if(p.isShiftKeyDown()&&held.isEmpty()&&getPassengers().isEmpty()&&onGround()){
    var s=pick();if(!p.getInventory().add(s))p.drop(s,false);
    if(ammo()>0){var a=new ItemStack(ammoItem(),Math.min(ammo(),64));if(!p.getInventory().add(a))p.drop(a,false);}
    discard();return InteractionResult.CONSUME;
   }
   if(!p.isShiftKeyDown()&&canAddPassenger(p)){p.startRiding(this);if(getControllingPassenger()==p)p.displayClientMessage(Component.literal(help()),true);}
   return InteractionResult.CONSUME;
  }
  protected abstract String help();
  @Override public boolean hurt(DamageSource s,float amount){
   if(s.is(net.minecraft.tags.DamageTypeTags.IS_FIRE))return false;   // 0.81.1: vehicles do not catch fire
   if(level().isClientSide||isRemoved()||isInvulnerableTo(s))return false;
   if(s.getEntity()!=null&&hasPassenger(s.getEntity()))return false;
   if(s.getEntity() instanceof Player pl&&pl.isCreative()&&s.getDirectEntity()==pl){ejectPassengers();discard();return true;}
   if(wrecked())return true;   // already a burning wreck: the 3-minute fuse keeps going
   boolean melee=s.getDirectEntity() instanceof Player;
   float dmg=melee?amount*.25F:amount;
   entityData.set(HEALTH,health()-dmg);
   level().playSound(null,blockPosition(),SoundEvents.ANVIL_LAND,SoundSource.NEUTRAL,.35F,1.6F);
   if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.CRIT,getX(),getY()+1,getZ(),6,1,.5,1,.2);
   if(health()<=0){
    if(wrecks()){entityData.set(WRECK,0);entityData.set(HEALTH,0F);
     level().explode(this,null,null,getX(),getY()+1,getZ(),2.5F,false,Level.ExplosionInteraction.NONE,ParticleTypes.EXPLOSION,ParticleTypes.EXPLOSION_EMITTER,SoundEvents.GENERIC_EXPLODE);
     knockedOut();}
    else destroy();}
   return true;
  }
  /** just knocked out (wrecks() vehicles): the tank warns its crew; the helicopter throws them out and starts falling */
  protected void knockedOut(){for(var r:getPassengers())if(r instanceof Player pl)pl.displayClientMessage(Component.literal("§4§lTANQUE DESTRUÍDO! §cSaia rápido — ele explode em 3 minutos."),false);}
  protected void destroy(){
   if(level().isClientSide||isRemoved())return;
   ejectPassengers();
   level().explode(this,getX(),getY()+1,getZ(),4F,Level.ExplosionInteraction.TNT);
   if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+1,getZ(),60,1.5,1,1.5,.05);
   discard();
  }
  /** Hangar: +8 health per call, then refills a bit of ammo every few calls. */
  @Override public boolean service(){
   if(wrecked())return false;   // a wreck can't be repaired
   if(health()<maxHealth()){entityData.set(HEALTH,Math.min(maxHealth(),health()+8));return true;}
   if(fuel()<maxFuel()){addFuel(maxFuel()/40);return true;}
   if(ammo()<maxAmmo()&&++serviceCount%4==0){entityData.set(AMMO,ammo()+1);return true;}
   return false;
  }
  @Override public void tick(){
   clearFire();
   float yawBefore=getYRot();
   super.tick();
   if(level().isClientSide&&!isControlledByLocalInstance()){
    if(lerpSteps>0){setPos(getX()+(lx-getX())/lerpSteps,getY()+(ly-getY())/lerpSteps,getZ()+(lz-getZ())/lerpSteps);setYRot(getYRot()+Mth.wrapDegrees(lyr-getYRot())/lerpSteps);lerpSteps--;}
   }else if(isControlledByLocalInstance()){
    if(getControllingPassenger()==null){inForward=inTurn=inLift=0;if(autoTarget!=null&&!level().isClientSide)autopilot();}
    if(wrecked()){inForward=inTurn=0;inLift=Math.min(inLift,0);}   // the wreck doesn't move
    if(fuel()<=0){inForward=0;inLift=Math.min(inLift,0);}
    drive();
   }
   deltaYaw=Mth.wrapDegrees(getYRot()-yawBefore);
   if(lastPos!=null)moved=new Vec3(getX()-lastPos.x,0,getZ()-lastPos.z).length();lastPos=position();
   if(level().isClientSide){effects();return;}
   if(gunCooldown>0)gunCooldown--;if(altCooldown>0)altCooldown--;
   var d=getControllingPassenger();
   if(d==null||level().getGameTime()-inputAt>10){fireHeld=altHeld=false;}
   if(wrecked()){fireHeld=altHeld=false;wreckTick();return;}
   if(d instanceof Player p)weapons(p);
   if(health()<maxHealth()*.3F&&tickCount%4==0&&level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+getBbHeight(),getZ(),1,.3,.1,.3,.02);
  }
  protected void effects(){}
  /** Server: thick black smoke and fire pour out; secondary pops; at 3 minutes the ammunition goes up. */
  private void wreckTick(){
   int w=entityData.get(WRECK)+1;entityData.set(WRECK,w);
   if(!(level() instanceof ServerLevel sl))return;
   double top=getY()+getBbHeight();
   if(tickCount%2==0)sl.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,getX(),top,getZ(),1,.4,.1,.4,.02);
   if(tickCount%3==0)sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),top-.3,getZ(),3,.8,.2,.8,.03);
   if(tickCount%4==0)sl.sendParticles(ParticleTypes.FLAME,getX(),getY()+getBbHeight()*.7,getZ(),4,.9,.3,.9,.02);
   if(tickCount%20==0)level().playSound(null,blockPosition(),SoundEvents.FIRE_AMBIENT,SoundSource.NEUTRAL,1.5F,.7F);
   if(random.nextInt(160)==0){sl.sendParticles(ParticleTypes.EXPLOSION,getX(),top,getZ(),1,.6,.3,.6,0);level().playSound(null,blockPosition(),SoundEvents.FIREWORK_ROCKET_BLAST,SoundSource.NEUTRAL,1.2F,.6F);}   // rounds cooking off
   int left=WRECK_TICKS-w;
   if(left==600||left==200)for(var p:sl.players())if(p.distanceToSqr(this)<40*40)p.displayClientMessage(Component.literal("§cO tanque destruído vai explodir em "+left/20+" s!"),true);
   if(left<=200&&left%20==0)level().playSound(null,blockPosition(),SoundEvents.NOTE_BLOCK_BASS.value(),SoundSource.NEUTRAL,1.5F,.5F+(200-left)/400F);
   if(w>=WRECK_TICKS){
    ejectPassengers();
    level().explode(this,null,null,getX(),getY()+1,getZ(),7F,true,Level.ExplosionInteraction.TNT,ParticleTypes.EXPLOSION,ParticleTypes.EXPLOSION_EMITTER,SoundEvents.GENERIC_EXPLODE);
    sl.sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY()+1,getZ(),120,2.5,1.5,2.5,.08);sl.sendParticles(ParticleTypes.FLAME,getX(),getY()+1,getZ(),80,2,1,2,.15);
    level().playSound(null,blockPosition(),MareEletrica.C4_BLAST.get(),SoundSource.NEUTRAL,1.5F,.8F);
    discard();
   }
  }
  /** Riders turn with the vehicle. */
  protected void turnRider(Entity rider){if(deltaYaw!=0){rider.setYRot(rider.getYRot()+deltaYaw);rider.setYHeadRot(rider.getYHeadRot()+deltaYaw);}}
  protected void fired(){entityData.set(FIRED,tickCount);}
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putFloat("Health",health());t.putInt("Ammo",ammo());t.putInt("Upg",entityData.get(UPG));t.putInt("Fuel",fuel());t.putInt("Wreck",entityData.get(WRECK));}
  @Override protected void readAdditionalSaveData(CompoundTag t){entityData.set(UPG,t.getInt("Upg"));if(t.contains("Health"))entityData.set(HEALTH,t.getFloat("Health"));entityData.set(AMMO,t.getInt("Ammo"));entityData.set(FUEL,t.contains("Fuel")?t.getInt("Fuel"):maxFuel());entityData.set(WRECK,t.contains("Wreck")?t.getInt("Wreck"):-1);}
  /** Steer toward a point: returns the yaw error in degrees (positive = target to the right). */
  protected float headingError(Vec3 target){float want=(float)Math.toDegrees(Math.atan2(-(target.x-getX()),target.z-getZ()));return Mth.wrapDegrees(want-getYRot());}
 }

 // ======================================================================= helicopter
 public static final class Helicopter extends Base {
  /** Rotor speed 0..1 (spins up when a pilot gets in, winds down when empty). */
  public float rotor,rotorO,rotorAngle,rotorAngleO;
  public Helicopter(EntityType<? extends Helicopter> t,Level l){super(t,l);}
  @Override protected float baseHealth(){return 80F;}
  @Override protected int baseAmmo(){return 16;}
  @Override public int maxFuel(){return 6000;}
  @Override public Item ammoItem(){return Barrage.ROCKET.get();}
  @Override public ItemStack pick(){return new ItemStack(HELI_ITEM.get());}
  @Override public boolean isNoGravity(){return true;}
  @Override protected boolean canAddPassenger(Entity e){return getPassengers().size()<4&&!wrecked();}
  /** 0.66.0: shot down → it doesn't blow up in the air: the crew bails out with parachutes and it spins down smoking until it hits the ground */
  @Override protected boolean wrecks(){return true;}
  @Override protected void knockedOut(){
   for(var r:java.util.List.copyOf(getPassengers())){r.stopRiding();
    if(r instanceof net.minecraft.server.level.ServerPlayer sp){sp.teleportTo(sp.getX(),sp.getY()+1.5,sp.getZ());sp.setDeltaMovement(getDeltaMovement().add(random.nextGaussian()*.4,.6,random.nextGaussian()*.4));sp.hurtMarked=true;
     br.mareeletrica.EjectSeat.arm(sp);sp.displayClientMessage(Component.literal("§4§lHELICÓPTERO ABATIDO! §cVocê pulou fora — o paraquedas abre sozinho."),false);}}
   level().playSound(null,blockPosition(),MareEletrica.SHELL_WHISTLE.get(),SoundSource.NEUTRAL,1.2F,.6F);
  }
  @Override protected String help(){return "Espaço sobe • Ctrl desce • W/S frente/ré • A/D gira • clique: metralhadora • botão direito: foguete • Shift sai";}
  private static final Vec3[] SEATS={new Vec3(.75,.56,1.07),new Vec3(-.75,.56,1.07),new Vec3(.75,.56,-.6),new Vec3(-.75,.56,-.6)};
  @Override protected void positionRider(Entity rider,Entity.MoveFunction move){
   if(!hasPassenger(rider))return;int i=Math.min(getPassengers().indexOf(rider),SEATS.length-1);
   Vec3 s=SEATS[i].yRot((float)Math.toRadians(-getYRot()));
   move.accept(rider,getX()+s.x,getY()+s.y,getZ()+s.z);turnRider(rider);
  }
  int fallTicks;
  @Override protected void drive(){
   if(wrecked()){   // shot down: the tail rotor is gone — it spins and drops, then blows up when it hits something
    fallTicks++;setYRot(getYRot()+Math.min(18F,6F+fallTicks*.4F));
    Vec3 v=getDeltaMovement();v=new Vec3(v.x*.96,Math.max(v.y-.05,-1.4),v.z*.96);
    setDeltaMovement(v);move(MoverType.SELF,v);
    if(!level().isClientSide){
     if(level() instanceof ServerLevel sl&&fallTicks%10==0)sl.sendParticles(ParticleTypes.EXPLOSION,getX(),getY()+2,getZ(),1,1.2,.5,1.2,0);
     if(onGround()||verticalCollision||horizontalCollision||isInWater()||fallTicks>900)destroy();
    }
    return;
   }
   boolean piloted=getControllingPassenger()!=null||autoTarget!=null;
   float yaw=getYRot()+inTurn*(onGround()?1.2F:3.2F)*rotor;
   setYRot(yaw);
   Vec3 fwd=Vec3.directionFromRotation(0,yaw);
   Vec3 v=getDeltaMovement();
   double h=.92,vert=.88;
   v=new Vec3(v.x*h,v.y*vert,v.z*h);
   v=v.add(fwd.scale(inForward*.05*rotor*speedMul()));
   // lift: rotor at full speed holds it up; Space climbs, Ctrl sinks, nothing = hover
   v=v.add(0,-.06+.06*rotor+inLift*.055*rotor,0);
   if(!piloted&&!onGround())v=v.add(0,-.01,0);   // empty: autorotation, comes down slowly
   double max=1.15*speedMul(),hs=Math.sqrt(v.x*v.x+v.z*v.z);if(hs>max){v=new Vec3(v.x/hs*max,v.y,v.z/hs*max);}
   v=new Vec3(v.x,Mth.clamp(v.y,-.8,.6),v.z);
   if(onGround()&&v.y<0)v=new Vec3(v.x*.6,0,v.z*.6);
   setDeltaMovement(v);
   double speedBefore=v.horizontalDistance();
   move(MoverType.SELF,v);
   if(!level().isClientSide&&horizontalCollision&&speedBefore>.75)hurt(damageSources().flyIntoWall(),(float)(speedBefore*25));
  }
  @Override public void tick(){
   rotorO=rotor;rotorAngleO=rotorAngle;
   boolean piloted=(getControllingPassenger()!=null||autoTarget!=null)&&fuel()>0;
   rotor=Mth.clamp(rotor+(piloted?.012F:-.006F),0,1);
   rotorAngle+=rotor*55F;
   super.tick();
   if(!level().isClientSide&&rotor>.5F&&!onGround()&&fuel()>0)entityData.set(FUEL,fuel()-1);
   if(!level().isClientSide&&rotor>.2F&&tickCount%8==0)level().playSound(null,blockPosition(),SoundEvents.MINECART_RIDING,SoundSource.NEUTRAL,.35F+rotor*.5F,.45F+rotor*.25F);
  }
  /** Flies to the target: climbs to a safe height, crosses, then comes straight down and lands. */
  @Override protected void autopilot(){
   Vec3 t=autoTarget;double flat=new Vec3(t.x-getX(),0,t.z-getZ()).length();
   double cruise=Math.max(t.y+14,getY());
   float err=headingError(t);
   inTurn=flat>3?Mth.clamp(err/25F,-1,1):0;
   inForward=flat>3&&Math.abs(err)<35?(float)Mth.clamp(flat/12,.25,1):0;
   if(flat>4)inLift=getY()<cruise-1?1:getY()>cruise+2?-.5F:0;
   else inLift=onGround()?0:-.6F;
   if(flat<=4&&onGround()&&rotor>.3F){autoTarget=null;level().playSound(null,blockPosition(),SoundEvents.NOTE_BLOCK_CHIME.value(),SoundSource.NEUTRAL,1F,1.4F);}
  }
  @Override protected void effects(){
   if(rotor>.5F&&tickCount%2==0){   // downwash: dust under the rotor when low
    var floor=level().getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,blockPosition());
    if(getY()-floor.getY()<8)for(int i=0;i<4;i++){double a=random.nextDouble()*Math.PI*2;level().addParticle(ParticleTypes.POOF,getX()+Math.cos(a)*5.5,floor.getY()+.1,getZ()+Math.sin(a)*5.5,Math.cos(a)*.25,.02,Math.sin(a)*.25);}
   }
  }
  @Override protected void weapons(Player p){
   Vec3 look=p.getLookAngle();
   if(fireHeld&&gunCooldown==0){
    gunCooldown=2;Vec3 from=position().add(0,.35,0).add(Vec3.directionFromRotation(0,getYRot()).scale(5.3));
    Tracers.fire(level(),this,p,from,look.add(random.nextGaussian()*.012,random.nextGaussian()*.012,random.nextGaussian()*.012),6F,6F,getDeltaMovement());
    level().playSound(null,from.x,from.y,from.z,MareEletrica.JET_GUN.get(),SoundSource.NEUTRAL,1.2F,1.2F+random.nextFloat()*.15F);fired();
   }
   if(altHeld&&altCooldown==0){
    altCooldown=14;
    if(ammo()<=0&&!p.isCreative()){p.displayClientMessage(Component.literal("§cSem foguetes — clique no helicóptero com foguetes de barragem"),true);altCooldown=30;return;}
    if(!p.isCreative())entityData.set(AMMO,ammo()-1);
    boolean left=(ammo()&1)==0;Vec3 side=Vec3.directionFromRotation(0,getYRot()-90).scale(left?2.5:-2.5);
    Vec3 from=position().add(0,1.2,0).add(side).add(Vec3.directionFromRotation(0,getYRot()).scale(1.7));
    var r=new Barrage.RocketEntity(Barrage.ROCKET_ENTITY.get(),level());r.setPos(from.x,from.y,from.z);r.setItem(new ItemStack(Barrage.ROCKET.get()));r.setOwner(p);
    r.setDeltaMovement(look.scale(3.2).add(getDeltaMovement()));level().addFreshEntity(r);
    level().playSound(null,from.x,from.y,from.z,MareEletrica.LAUNCHER_FIRE.get(),SoundSource.NEUTRAL,1.2F,1.2F);
    if(level() instanceof ServerLevel sl)sl.sendParticles(ParticleTypes.FLAME,from.x,from.y,from.z,6,.1,.1,.1,.05);
   }
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);}
 }

 // ======================================================================= tank
 public static final class Tank extends Base {
  private static final EntityDataAccessor<Float> TURRET=SynchedEntityData.defineId(Tank.class,EntityDataSerializers.FLOAT),BARREL=SynchedEntityData.defineId(Tank.class,EntityDataSerializers.FLOAT);
  public float turretR,turretO,barrelR,barrelO,trackPhase,trackPhaseO;private float speed;
  public Tank(EntityType<? extends Tank> t,Level l){super(t,l);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(TURRET,0F);b.define(BARREL,0F);}
  @Override protected float baseHealth(){return 240F;}
  @Override protected boolean wrecks(){return true;}
  @Override protected int baseAmmo(){return 24;}
  @Override public int maxFuel(){return 8000;}
  @Override public Item ammoItem(){return MareEletrica.CANNONBALL.get();}
  @Override public ItemStack pick(){return new ItemStack(TANK_ITEM.get());}
  @Override public float maxUpStep(){return 1.1F;}
  @Override protected boolean canAddPassenger(Entity e){return getPassengers().size()<2;}
  @Override protected String help(){return "W/S anda • A/D vira • mouse mira a torre • clique: metralhadora • botão direito: canhão • Shift sai";}
  /** World yaw of the turret and the barrel elevation (degrees, negative = up). */
  public float turretYaw(){return entityData.get(TURRET);}
  public float barrelPitch(){return entityData.get(BARREL);}
  @Override protected void positionRider(Entity rider,Entity.MoveFunction move){
   if(!hasPassenger(rider))return;
   boolean commander=getPassengers().indexOf(rider)==0;
   Vec3 s=(commander?new Vec3(.87,2.6,-.73):new Vec3(-.8,1.53,2.8)).yRot((float)Math.toRadians(-(commander?turretYaw():getYRot())));
   move.accept(rider,getX()+s.x,getY()+s.y,getZ()+s.z);turnRider(rider);
  }
  @Override protected void drive(){
   boolean driven=getControllingPassenger()!=null;
   float target=inForward*.30F*speedMul();
   speed+=Mth.clamp(target-speed,-.02F,.016F);
   if(!driven)speed*=.8F;
   float turn=inTurn*(Math.abs(speed)<.02F?2.2F:1.7F)*(speed<-.01F?-1:1);
   setYRot(getYRot()+turn);
   Vec3 fwd=Vec3.directionFromRotation(0,getYRot());
   Vec3 v=getDeltaMovement();
   double vy=onGround()?-.04:Math.max(-1.5,v.y-.08);
   v=new Vec3(fwd.x*speed,vy,fwd.z*speed);
   setDeltaMovement(v);
   move(MoverType.SELF,v);
   if(horizontalCollision&&Math.abs(speed)>.05F)speed*=.5F;
   trackPhase+=speed*2.2F+Math.abs(turn)*.03F;
  }
  @Override public void tick(){
   turretO=turretR;barrelO=barrelR;trackPhaseO=trackPhase;
   super.tick();
   // turret follows the commander's aim at a limited speed
   if(!level().isClientSide){
    float ty=turretYaw(),tp=barrelPitch();
    if(wrecked()){}   // frozen turret
    else if(getControllingPassenger() instanceof Player p){ty+=Mth.clamp(Mth.wrapDegrees(p.getYRot()-ty),-3.5F,3.5F);tp+=Mth.clamp(Mth.clamp(p.getXRot(),-20,8)-tp,-2F,2F);}
    else ty+=deltaYaw;
    // the turret only turns 90 degrees to each side of the hull (no full spin)
    ty=getYRot()+Mth.clamp(Mth.wrapDegrees(ty-getYRot()),-90F,90F);
    entityData.set(TURRET,ty);entityData.set(BARREL,tp);
    // running over things
    if(moved>.12)for(var e:level().getEntities(this,getBoundingBox().inflate(.2,0,.2),x->x instanceof LivingEntity&&!hasPassenger(x)&&x.isAlive()))
     if(e.invulnerableTime==0){e.hurt(getControllingPassenger() instanceof LivingEntity d?damageSources().mobAttack(d):damageSources().generic(),8F);var push=e.position().subtract(position()).normalize();e.push(push.x*.8,.3,push.z*.8);e.hurtMarked=true;}
   }else{
    float ty=turretYaw();turretR+=Mth.wrapDegrees(ty-turretR)*.5F;barrelR+=(barrelPitch()-barrelR)*.5F;
   }
   if(!level().isClientSide&&moved>.02&&fuel()>0)entityData.set(FUEL,fuel()-1);
   if(!level().isClientSide&&moved>.05&&tickCount%6==0)level().playSound(null,blockPosition(),SoundEvents.IRON_GOLEM_STEP,SoundSource.NEUTRAL,.7F,.5F);
  }
  /** Drives toward the target and stops 6 blocks before it. */
  @Override protected void autopilot(){
   Vec3 t=autoTarget;double flat=new Vec3(t.x-getX(),0,t.z-getZ()).length();
   if(flat<6){inForward=inTurn=0;autoTarget=null;return;}
   float err=headingError(t);
   inTurn=Mth.clamp(err/20F,-1,1);inForward=Math.abs(err)<40?1:0;
   if(horizontalCollision&&tickCount%40==0)autoTarget=null;   // stuck: give up
  }
  @Override protected void effects(){
   if(moved>.08&&tickCount%2==0){var st=level().getBlockState(blockPosition().below());if(!st.isAir())level().addParticle(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,st),getX()+random.nextGaussian(),getY()+.1,getZ()+random.nextGaussian(),0,.1,0);}
   if(tickCount%5==0){Vec3 ex=position().add(Vec3.directionFromRotation(0,getYRot()).scale(-3.2)).add(0,2.27,0);level().addParticle(ParticleTypes.SMOKE,ex.x,ex.y,ex.z,0,.05,0);}
  }
  public Vec3 barrelDir(){return Vec3.directionFromRotation(barrelPitch(),turretYaw());}
  @Override protected void weapons(Player p){
   Vec3 pivot=position().add(0,2.63,0).add(Vec3.directionFromRotation(0,turretYaw()).scale(-.27));
   if(fireHeld&&gunCooldown==0){
    gunCooldown=3;Vec3 from=pivot.add(barrelDir().scale(2.4)).add(Vec3.directionFromRotation(0,turretYaw()+90).scale(.6));
    Tracers.fire(level(),this,p,from,barrelDir().add(random.nextGaussian()*.01,random.nextGaussian()*.01,random.nextGaussian()*.01),5.5F,6F,null);
    level().playSound(null,from.x,from.y,from.z,MareEletrica.JET_GUN.get(),SoundSource.NEUTRAL,1.2F,1.3F);
   }
   if(altHeld&&altCooldown==0){
    altCooldown=60;
    if(ammo()<=0&&!p.isCreative()){p.displayClientMessage(Component.literal("§cSem munição — clique no tanque com balas de canhão"),true);altCooldown=20;return;}
    if(!p.isCreative())entityData.set(AMMO,ammo()-1);
    Vec3 dir=barrelDir();Vec3 from=pivot.add(dir.scale(4.5));
    var ball=new CannonballEntity(MareEletrica.SHELL.get(),level());ball.setPos(from.x,from.y,from.z);ball.setOwner(p);ball.shoot(dir.x,dir.y,dir.z,4.2F,.2F);level().addFreshEntity(ball);
    level().playSound(null,from.x,from.y,from.z,MareEletrica.C4_BLAST.get(),SoundSource.NEUTRAL,1.2F,1.3F);
    if(level() instanceof ServerLevel sl){sl.sendParticles(ParticleTypes.EXPLOSION,from.x,from.y,from.z,1,0,0,0,0);sl.sendParticles(ParticleTypes.LARGE_SMOKE,from.x,from.y,from.z,25,.5,.3,.5,.06);}
    fired();
   }
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putFloat("Turret",turretYaw());}
  @Override protected void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);entityData.set(TURRET,t.getFloat("Turret"));}
 }

 // ======================================================================= network
 public record Input(boolean fire,boolean alt) implements CustomPacketPayload {
  public static final Type<Input> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"vehicle_input"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Input> CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,Input::fire,ByteBufCodecs.BOOL,Input::alt,Input::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
 public static final class Net {
  @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
   e.registrar("13").playToServer(Input.TYPE,Input.CODEC,(p,c)->{if(c.player() instanceof ServerPlayer sp&&sp.getVehicle() instanceof Base b)b.input(sp,p.fire(),p.alt());});
  }
 }
}
