package br.mareeletrica;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredItem;

/** Remote-controlled drone (flown through its camera), the drone bomb it carries, and the remote. */
@EventBusSubscriber(modid=MareEletrica.ID)
public final class Drones {
 public static final DeferredItem<DroneItem> DRONE=MareEletrica.ITEMS.register("drone",()->new DroneItem(new Item.Properties().stacksTo(1)));
 public static final DeferredItem<Item> BOMB=MareEletrica.ITEMS.register("drone_bomb",()->new Item(new Item.Properties().stacksTo(16)));
 public static final DeferredItem<DroneRemoteItem> REMOTE=MareEletrica.ITEMS.register("drone_remote",()->new DroneRemoteItem(new Item.Properties().stacksTo(1)));
 public static final Supplier<EntityType<DroneEntity>> DRONE_ENTITY=MareEletrica.ACTORS.register("drone",()->
  EntityType.Builder.<DroneEntity>of(DroneEntity::new,MobCategory.MISC).sized(.9F,.35F).eyeHeight(.08F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":drone"));
 public static final Supplier<EntityType<BombEntity>> BOMB_ENTITY=MareEletrica.ACTORS.register("drone_bomb",()->
  EntityType.Builder.<BombEntity>of(BombEntity::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":drone_bomb"));
 public static final double RANGE=Double.MAX_VALUE;   // no distance limit any more: the camera loads the chunks around the drone
 public static final int VIEW_DISTANCE=6,LOAD_RADIUS=6;
 private static final net.minecraft.server.level.TicketType<UUID> VIEW=net.minecraft.server.level.TicketType.create("mare_drone_view",java.util.Comparator.comparing(UUID::toString));
 private static final Map<UUID,net.minecraft.world.level.ChunkPos> CENTER=new HashMap<>();
 public static final int MAX_BOMBS=10,BATTERY_MAX=6000;   // 6000 ticks = 5 min of normal flight; turbo drains 3x
 private Drones(){}
 public static void register(){}
 public static void creative(CreativeModeTab.Output out){out.accept(DRONE.get());out.accept(BOMB.get());out.accept(REMOTE.get());}

 // ---------------- sessions: which player is flying which drone ----------------
 private static final Map<UUID,UUID> PILOT_TO_DRONE=new HashMap<>(),DRONE_TO_PILOT=new HashMap<>();
 public static void start(ServerPlayer player,DroneEntity drone){
  end(player);PILOT_TO_DRONE.put(player.getUUID(),drone.getUUID());DRONE_TO_PILOT.put(drone.getUUID(),player.getUUID());
  PacketDistributor.sendToPlayer(player,new DroneNet.Session(drone.getId()));
  player.displayClientMessage(Component.literal("Drone conectado. Ctrl = turbo, B = soltar bomba, X = sair. Sem limite de distância."),true);
 }
 public static void end(ServerPlayer player){
  var drone=PILOT_TO_DRONE.remove(player.getUUID());
  if(drone!=null){DRONE_TO_PILOT.remove(drone);PacketDistributor.sendToPlayer(player,new DroneNet.Session(-1));}
  releaseView(player);
 }
 public static void end(DroneEntity drone){
  var player=DRONE_TO_PILOT.remove(drone.getUUID());
  if(player!=null){PILOT_TO_DRONE.remove(player);var p=drone.level().getServer()!=null?drone.level().getServer().getPlayerList().getPlayer(player):null;if(p!=null){PacketDistributor.sendToPlayer(p,new DroneNet.Session(-1));releaseView(p);}}
 }
 public static DroneEntity droneOf(ServerPlayer player){
  var id=PILOT_TO_DRONE.get(player.getUUID());if(id==null)return null;
  for(ServerLevel l:player.server.getAllLevels()){var e=l.getEntity(id);if(e instanceof DroneEntity d)return d;}
  return null;
 }
 public static ServerPlayer pilot(DroneEntity drone){
  var id=DRONE_TO_PILOT.get(drone.getUUID());
  return id==null||drone.level().getServer()==null?null:drone.level().getServer().getPlayerList().getPlayer(id);
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){var d=PILOT_TO_DRONE.remove(p.getUUID());if(d!=null)DRONE_TO_PILOT.remove(d);releaseView(p);}}
 // ---------------- remote camera: the server sends the pilot the chunks and entities around the drone ----------------
 public static boolean viewing(ServerPlayer p){return CENTER.containsKey(p.getUUID());}
 public static net.minecraft.world.level.ChunkPos viewChunk(ServerPlayer p){var c=CENTER.get(p.getUUID());return c==null?p.chunkPosition():c;}
 public static Vec3 viewPosition(ServerPlayer p){var d=droneOf(p);return d==null||d.level()!=p.level()?p.position():d.position();}
 private static void moveView(ServerPlayer p,DroneEntity d){
  var next=d.chunkPosition();var old=CENTER.get(p.getUUID());if(next.equals(old))return;
  var src=((ServerLevel)d.level()).getChunkSource();src.addRegionTicket(VIEW,next,LOAD_RADIUS,p.getUUID());
  if(old!=null)src.removeRegionTicket(VIEW,old,LOAD_RADIUS,p.getUUID());
  CENTER.put(p.getUUID(),next);RemoteSessions.refreshView(p);
 }
 private static void releaseView(ServerPlayer p){
  var old=CENTER.remove(p.getUUID());if(old==null)return;
  p.serverLevel().getChunkSource().removeRegionTicket(VIEW,old,LOAD_RADIUS,p.getUUID());RemoteSessions.refreshView(p);
 }
 @SubscribeEvent public static void shutdown(net.neoforged.neoforge.event.server.ServerStoppedEvent e){PILOT_TO_DRONE.clear();DRONE_TO_PILOT.clear();CENTER.clear();}

 // ---------------- items ----------------
 public static final class DroneItem extends Item {
  public DroneItem(Properties p){super(p);}
  public static net.minecraft.nbt.CompoundTag data(ItemStack s){var c=s.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);return c==null?new net.minecraft.nbt.CompoundTag():c.copyTag();}
  public static int battery(ItemStack s){var t=data(s);return t.contains("Battery")?t.getInt("Battery"):BATTERY_MAX;}
  public static int bombs(ItemStack s){return data(s).getInt("Bombs");}
  public static ItemStack make(int battery,int bombs){var s=new ItemStack(DRONE.get());var t=new net.minecraft.nbt.CompoundTag();t.putInt("Battery",battery);t.putInt("Bombs",bombs);s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(t));return s;}
  @Override public boolean isBarVisible(ItemStack s){return battery(s)<BATTERY_MAX;}
  @Override public int getBarWidth(ItemStack s){return Math.round(13F*battery(s)/BATTERY_MAX);}
  @Override public int getBarColor(ItemStack s){return battery(s)>BATTERY_MAX/4?0x5BE07C:0xE05B5B;}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<Component> tips,net.minecraft.world.item.TooltipFlag f){
   tips.add(Component.literal("Bateria "+battery(s)*100/BATTERY_MAX+"%  •  Bombas "+bombs(s)+"/"+MAX_BOMBS));
   tips.add(Component.literal("Clique direito no chão: colocar • Redstone no drone: recarregar"));
  }
  @Override public InteractionResult useOn(UseOnContext c){
   var level=c.getLevel();
   if(!level.isClientSide){
    var drone=new DroneEntity(DRONE_ENTITY.get(),level);
    var pos=c.getClickedPos().relative(c.getClickedFace());
    drone.setPos(pos.getX()+.5,pos.getY()+.05,pos.getZ()+.5);drone.setYRot(c.getRotation());
    drone.setBattery(battery(c.getItemInHand()));drone.setBombs(bombs(c.getItemInHand()));
    if(c.getPlayer()!=null)drone.setOwner(c.getPlayer().getUUID());
    level.addFreshEntity(drone);
    if(c.getPlayer()==null||!c.getPlayer().isCreative())c.getItemInHand().shrink(1);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
 }
 public static final class DroneRemoteItem extends Item {
  public DroneRemoteItem(Properties p){super(p);}
  /** Each remote flies only the drone it was paired with. */
  public static UUID linked(ItemStack s){var t=DroneItem.data(s);return t.hasUUID("Drone")?t.getUUID("Drone"):null;}
  public static void link(ItemStack s,DroneEntity d){var t=DroneItem.data(s);t.putUUID("Drone",d.getUUID());s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(t));}
  @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<Component> tips,net.minecraft.world.item.TooltipFlag f){
   var id=linked(s);tips.add(Component.literal(id==null?"Sem drone — Shift + clique no drone para conectar":"Conectado ao drone "+id.toString().substring(0,8)));
  }
  @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
   var stack=player.getItemInHand(hand);
   if(!level.isClientSide&&player instanceof ServerPlayer sp){
    if(droneOf(sp)!=null){end(sp);sp.displayClientMessage(Component.literal("Voo encerrado."),true);}
    else{
     var link=linked(stack);
     if(link==null)sp.displayClientMessage(Component.literal("Controle sem drone. Shift + clique direito no drone com este controle para conectar."),true);
     else if(!(sp.serverLevel().getEntity(link) instanceof DroneEntity d)||d.crashing())sp.displayClientMessage(Component.literal("O drone conectado não foi encontrado (destruído ou muito longe/descarregado)."),true);
     else start(sp,d);
    }
    sp.getCooldowns().addCooldown(this,10);
   }
   return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
  }
 }

 // ---------------- the drone ----------------
 public static final class DroneEntity extends Entity {
  private static final EntityDataAccessor<Integer> BOMBS=SynchedEntityData.defineId(DroneEntity.class,EntityDataSerializers.INT),BATTERY=SynchedEntityData.defineId(DroneEntity.class,EntityDataSerializers.INT);
  private static final EntityDataAccessor<Boolean> CRASHING=SynchedEntityData.defineId(DroneEntity.class,EntityDataSerializers.BOOLEAN),POWERED=SynchedEntityData.defineId(DroneEntity.class,EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Integer> HATCH=SynchedEntityData.defineId(DroneEntity.class,EntityDataSerializers.INT);
  /** Delivery version (courier): no bombs, carries up to 9 stacks of cargo instead; CARGO = how many stacks are aboard. */
  private static final EntityDataAccessor<Boolean> COURIER=SynchedEntityData.defineId(DroneEntity.class,EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Integer> CARGO=SynchedEntityData.defineId(DroneEntity.class,EntityDataSerializers.INT);
  public static final int CARGO_MAX=9;
  private final java.util.List<ItemStack> cargo=new java.util.ArrayList<>();
  public boolean courier(){return entityData.get(COURIER);}
  public void setCourier(boolean c){entityData.set(COURIER,c);}
  public int cargoStacks(){return entityData.get(CARGO);}
  /** Bomb-bay doors: how open they are (0 closed .. 1 open), animated on both sides from the synced timer. */
  public float hatch,hatchO;
  private UUID owner;private float health=8F;private int crashTicks;private boolean warned;
  public float inForward,inStrafe,inYaw,inPitch;public boolean inUp,inDown,inBoost;public long inputAt=-100;
  public DroneEntity(EntityType<? extends DroneEntity> type,Level level){super(type,level);}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(BOMBS,0);b.define(BATTERY,BATTERY_MAX);b.define(CRASHING,false);b.define(POWERED,false);b.define(HATCH,0);b.define(COURIER,false);b.define(CARGO,0);}
  public boolean hatchOpen(){return entityData.get(HATCH)>0;}
  public int bombs(){return entityData.get(BOMBS);}
  public void setBombs(int n){entityData.set(BOMBS,Mth.clamp(n,0,MAX_BOMBS));}
  public boolean hasBomb(){return bombs()>0;}
  public int battery(){return entityData.get(BATTERY);}
  public void setBattery(int n){entityData.set(BATTERY,Mth.clamp(n,0,BATTERY_MAX));}
  public boolean crashing(){return entityData.get(CRASHING);}
  /** True while a pilot is flying it (rotors spin fast). */
  public boolean powered(){return entityData.get(POWERED);}
  public void setOwner(UUID id){owner=id;}
  public boolean isOwnedBy(Player p){return owner==null||owner.equals(p.getUUID());}
  @Override public boolean isPickable(){return !isRemoved();}
  @Override public boolean isPushable(){return false;}
  @Override public boolean canBeCollidedWith(){return false;}
  @Override public boolean isNoGravity(){return true;}
  @Override public ItemStack getPickResult(){return new ItemStack(courier()?Delivery.DRONE_ITEM.get():DRONE.get());}
  /** Courier, action key: over a delivery station or with cargo aboard, unload; otherwise pick up items from the ground (or a station) below. */
  public void cargoAction(ServerPlayer pilot){
   var l=(ServerLevel)level();
   net.minecraft.core.BlockPos below=null;for(int i=0;i<4;i++){var p=blockPosition().below(i);if(l.getBlockEntity(p) instanceof Delivery.Station){below=p;break;}if(!l.getBlockState(p).isAir())break;}
   if(!cargo.isEmpty()){
    if(below!=null&&l.getBlockEntity(below) instanceof Delivery.Station st){st.receive(new java.util.ArrayList<>(cargo));pilot.displayClientMessage(Component.literal("§aCarga entregue na estação ("+cargo.size()+" pilhas)."),true);}
    else{for(var c:cargo){var it=new net.minecraft.world.entity.item.ItemEntity(l,getX(),getY()-.3,getZ(),c);it.setDeltaMovement(getDeltaMovement().scale(.5).add(0,-.1,0));l.addFreshEntity(it);}pilot.displayClientMessage(Component.literal("Carga solta ("+cargo.size()+" pilhas)."),true);}
    cargo.clear();entityData.set(CARGO,0);entityData.set(HATCH,20);l.playSound(null,blockPosition(),SoundEvents.ITEM_PICKUP,SoundSource.NEUTRAL,1F,.7F);return;
   }
   if(below!=null&&l.getBlockEntity(below) instanceof Delivery.Station st){
    for(int i=1;i<st.getContainerSize()&&cargo.size()<CARGO_MAX;i++){var it=st.getItem(i);if(!it.isEmpty()){cargo.add(it.copy());st.setItem(i,ItemStack.EMPTY);}}
    st.setChanged();
   }else{
    var box=getBoundingBox().inflate(1.5,0,1.5).expandTowards(0,-4,0);
    for(var it:l.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box,x->x.isAlive()&&!x.hasPickUpDelay())){if(cargo.size()>=CARGO_MAX)break;cargo.add(it.getItem().copy());it.discard();}
   }
   entityData.set(CARGO,cargo.size());
   pilot.displayClientMessage(Component.literal(cargo.isEmpty()?"§7Nada para pegar aqui embaixo (desça perto dos itens ou de uma estação).":"§aCarga pega: "+cargo.size()+"/"+CARGO_MAX+" pilhas"),true);
   if(!cargo.isEmpty())l.playSound(null,blockPosition(),SoundEvents.ITEM_PICKUP,SoundSource.NEUTRAL,1F,1.2F);
  }
  void dropCargo(){for(var c:cargo)net.minecraft.world.Containers.dropItemStack(level(),getX(),getY(),getZ(),c);cargo.clear();entityData.set(CARGO,0);}
  @Override public boolean hurt(DamageSource source,float amount){
   if(level().isClientSide||isRemoved()||crashing())return false;
   health-=amount;
   level().playSound(null,blockPosition(),SoundEvents.CHAIN_HIT,SoundSource.NEUTRAL,1F,.6F);
   ((ServerLevel)level()).sendParticles(ParticleTypes.CRIT,getX(),getY()+.2,getZ(),6,.3,.1,.3,.1);
   if(health<=0){entityData.set(CRASHING,true);entityData.set(POWERED,false);Drones.end(this);setDeltaMovement(getDeltaMovement().add((random.nextFloat()-.5F)*.2,.05,(random.nextFloat()-.5F)*.2));}
   return true;
  }
  /** Client-predicted flight (like boats): the pilot's own game moves the drone every tick and tells the server where it is,
   *  so the camera never waits for the network. Everyone else just sees the normal smoothed position. */
  public static int localPilotedId=-1;
  public boolean pilotedHere(){return level().isClientSide&&getId()==localPilotedId;}
  /** Shared flight physics; returns true while the rotors are driving it. */
  private boolean fly(boolean flying){
   Vec3 v=getDeltaMovement();
   if(flying){
    setYRot(inYaw);setXRot(Mth.clamp(inPitch,-80,80));
    double yaw=Math.toRadians(inYaw),top=inBoost?.78:.42;
    Vec3 fwd=new Vec3(-Math.sin(yaw),0,Math.cos(yaw)),right=new Vec3(-Math.cos(yaw),0,-Math.sin(yaw));
    Vec3 want=fwd.scale(inForward).add(right.scale(-inStrafe));
    if(want.lengthSqr()>1)want=want.normalize();
    want=want.scale(top).add(0,(inUp?.32:0)-(inDown?.32:0),0);
    v=v.add(want.subtract(v).scale(want.lengthSqr()>1e-4?.14:.08));      // smooth acceleration, gentle braking
    if(!inUp&&!inDown)v=new Vec3(v.x,v.y*.85+Math.sin(tickCount*.15)*.004,v.z);   // light hover bob
   }else v=new Vec3(v.x*.88,onGround()?0:Math.max(v.y-.02,-.12),v.z*.88);   // no pilot / no battery: settle down
   setDeltaMovement(v);
   move(MoverType.SELF,v);
   if(horizontalCollision)setDeltaMovement(getDeltaMovement().multiply(-.25,1,-.25));
   return flying;
  }
  /** Server: accept the pilot's position when it is plausible. */
  public void clientPosition(double x,double y,double z){
   if(crashing())return;
   if(new Vec3(x,y,z).distanceToSqr(position())<6.25){setDeltaMovement(new Vec3(x-getX(),y-getY(),z-getZ()));setPos(x,y,z);clientMovedAt=level().getGameTime();}
  }
  private long clientMovedAt=-100;
  @Override public void lerpTo(double x,double y,double z,float yRot,float xRot,int steps){
   if(pilotedHere()&&new Vec3(x,y,z).distanceToSqr(position())<16)return;   // our own prediction is newer; only take big corrections
   super.lerpTo(x,y,z,yRot,xRot,steps);
  }
  @Override public void tick(){
   super.tick();
   hatchO=hatch;hatch=Mth.clamp(hatch+(hatchOpen()?.25F:-.12F),0F,1F);
   if(!level().isClientSide&&entityData.get(HATCH)>0)entityData.set(HATCH,entityData.get(HATCH)-1);
   if(level().isClientSide){
    if(pilotedHere()&&!crashing())fly(battery()>0);
    return;
   }
   if(crashing()){
    crashTicks++;setDeltaMovement(getDeltaMovement().add(0,-.06,0).scale(.98));move(MoverType.SELF,getDeltaMovement());setYRot(getYRot()+25F);
    ((ServerLevel)level()).sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY(),getZ(),3,.2,.1,.2,.01);
    if(onGround()||horizontalCollision||verticalCollision||crashTicks>80)explode();
    return;
   }
   var pilot=Drones.pilot(this);
   if(pilot!=null&&(pilot.isRemoved()||pilot.level()!=level()||!pilot.isAlive())){Drones.end(this);pilot=null;}
   if(pilot!=null)moveView(pilot,this);
   boolean flying=pilot!=null&&level().getGameTime()-inputAt<10;
   if(flying&&battery()<=0){
    if(!warned){pilot.displayClientMessage(Component.literal("Bateria do drone acabou! Ele está pousando."),true);warned=true;}
    flying=false;
   }
   entityData.set(POWERED,flying);
   if(flying){
    warned=false;setYRot(inYaw);setXRot(Mth.clamp(inPitch,-80,80));
    boolean moving=Math.abs(inForward)+Math.abs(inStrafe)>.01||inUp||inDown;
    setBattery(battery()-(inBoost&&moving?3:1));
    if(level().getGameTime()-clientMovedAt>4)fly(true);   // the pilot's game stopped sending positions: fly it here
   }else fly(false);
   releaseBombs();
   if(entityData.get(HATCH)==1)level().playSound(null,blockPosition(),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.NEUTRAL,.8F,1.3F);
   if(flying&&tickCount%16==0)level().playSound(null,blockPosition(),SoundEvents.BEE_LOOP,SoundSource.NEUTRAL,.22F,inBoost?1.9F:1.5F);
  }
  public void explode(){if(level().isClientSide)return;
   if(courier()){dropCargo();((ServerLevel)level()).sendParticles(ParticleTypes.LARGE_SMOKE,getX(),getY(),getZ(),12,.3,.2,.3,.02);level().playSound(null,blockPosition(),SoundEvents.ITEM_BREAK,SoundSource.NEUTRAL,1F,.6F);
    var it=new ItemStack(Delivery.DRONE_ITEM.get());net.minecraft.world.Containers.dropItemStack(level(),getX(),getY(),getZ(),it);discard();return;}
   level().explode(this,getX(),getY(),getZ(),2F+bombs()*1.5F,Level.ExplosionInteraction.TNT);discard();}
  public void dropBomb(){
   if(level().isClientSide||!hasBomb()||crashing())return;
   if(!hatchOpen())level().playSound(null,blockPosition(),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.NEUTRAL,1F,1.3F);
   entityData.set(HATCH,24);pendingDrops++;
  }
  private int pendingDrops,dropDelay;
  private void releaseBombs(){
   if(pendingDrops<=0||!hasBomb()){pendingDrops=0;return;}
   if(hatch<.8F||--dropDelay>0)return;
   dropDelay=4;pendingDrops--;setBombs(bombs()-1);entityData.set(HATCH,Math.max(entityData.get(HATCH),14));
   var b=new BombEntity(BOMB_ENTITY.get(),level());b.setItem(new ItemStack(BOMB.get()));b.setOwner(this);
   b.setPos(getX(),getY()-.25,getZ());b.setDeltaMovement(getDeltaMovement().scale(.9).add(0,-.05,0));level().addFreshEntity(b);
   level().playSound(null,blockPosition(),SoundEvents.PISTON_CONTRACT,SoundSource.NEUTRAL,.6F,1.6F);
  }
  @Override public InteractionResult interact(Player player,InteractionHand hand){
   var held=player.getItemInHand(hand);
   if(!level().isClientSide){
    if(held.is(REMOTE.get())&&player.isShiftKeyDown()){
     if(!isOwnedBy(player))player.displayClientMessage(Component.literal("Este drone não é seu."),true);
     else{DroneRemoteItem.link(held,this);level().playSound(null,blockPosition(),SoundEvents.NOTE_BLOCK_PLING.value(),SoundSource.NEUTRAL,.8F,1.8F);player.displayClientMessage(Component.literal("Controle conectado a este drone."),true);}
    }else if(held.is(BOMB.get())&&courier()){player.displayClientMessage(Component.literal("O drone de entrega leva carga, não bombas."),true);
    }else if(held.is(BOMB.get())){
     if(bombs()>=MAX_BOMBS)player.displayClientMessage(Component.literal("O drone já carrega "+MAX_BOMBS+" bombas."),true);
     else{setBombs(bombs()+1);if(!player.isCreative())held.shrink(1);level().playSound(null,blockPosition(),SoundEvents.IRON_TRAPDOOR_OPEN,SoundSource.NEUTRAL,1F,1.2F);player.displayClientMessage(Component.literal("Bombas: "+bombs()+"/"+MAX_BOMBS),true);}
    }else if(held.is(net.minecraft.world.item.Items.REDSTONE)||held.is(net.minecraft.world.item.Items.REDSTONE_BLOCK)){
     if(battery()>=BATTERY_MAX)player.displayClientMessage(Component.literal("Bateria cheia."),true);
     else{setBattery(battery()+(held.is(net.minecraft.world.item.Items.REDSTONE_BLOCK)?BATTERY_MAX:BATTERY_MAX/5));if(!player.isCreative())held.shrink(1);level().playSound(null,blockPosition(),SoundEvents.BEACON_POWER_SELECT,SoundSource.NEUTRAL,.6F,1.8F);player.displayClientMessage(Component.literal("Bateria "+battery()*100/BATTERY_MAX+"%"),true);}
    }else if(player.isShiftKeyDown()&&held.isEmpty()&&isOwnedBy(player)){
     var s=courier()?Delivery.courierItem(battery()):DroneItem.make(battery(),bombs());if(!player.getInventory().add(s))player.drop(s,false);
     dropCargo();
     Drones.end(this);discard();
    }
   }
   return InteractionResult.sidedSuccess(level().isClientSide);
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putBoolean("Courier",courier());var cl=new net.minecraft.nbt.ListTag();for(var c:cargo)cl.add(c.save(registryAccess()));t.put("Cargo",cl);if(owner!=null)t.putUUID("Owner",owner);t.putFloat("Health",health);t.putInt("Bombs",bombs());t.putInt("Battery",battery());}
  @Override protected void readAdditionalSaveData(CompoundTag t){setCourier(t.getBoolean("Courier"));cargo.clear();for(var x:t.getList("Cargo",10))ItemStack.parse(registryAccess(),x).ifPresent(cargo::add);entityData.set(CARGO,cargo.size());if(t.hasUUID("Owner"))owner=t.getUUID("Owner");health=t.contains("Health")?t.getFloat("Health"):8F;setBombs(t.contains("Bombs")?t.getInt("Bombs"):(t.getBoolean("Bomb")?1:0));setBattery(t.contains("Battery")?t.getInt("Battery"):BATTERY_MAX);}
 }

 // ---------------- the bomb: falls and explodes on contact, breaking blocks ----------------
 public static final class BombEntity extends ThrowableItemProjectile {
  public BombEntity(EntityType<? extends BombEntity> type,Level level){super(type,level);}
  @Override protected Item getDefaultItem(){return BOMB.get();}
  @Override protected double getDefaultGravity(){return .07;}
  @Override protected void onHit(HitResult result){
   super.onHit(result);
   if(!level().isClientSide){level().explode(this,getX(),getY(),getZ(),5.2F,Level.ExplosionInteraction.TNT);discard();}
  }
 }
}
