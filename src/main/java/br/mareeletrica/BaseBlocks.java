package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** Runway lights (chase toward where they point), helipad (the panel calls a helicopter to land on it) and the vehicle lift. */
public final class BaseBlocks {
 public static final DeferredBlock<RunwayLight> RUNWAY=MareEletrica.BLOCKS.register("runway_light",()->new RunwayLight(BlockBehaviour.Properties.of().strength(1F).sound(SoundType.GLASS).noOcclusion().lightLevel(s->9)));
 public static final DeferredItem<BlockItem> RUNWAY_ITEM=MareEletrica.ITEMS.register("runway_light",()->new BlockItem(RUNWAY.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Light>> RUNWAY_ENTITY=MareEletrica.ENTITIES.register("runway_light",()->BlockEntityType.Builder.of(Light::new,RUNWAY.get()).build(null));
 public static final DeferredBlock<HelipadBlock> HELIPAD=MareEletrica.BLOCKS.register("helipad",()->new HelipadBlock(BlockBehaviour.Properties.of().strength(3F).sound(SoundType.STONE).noOcclusion().lightLevel(s->6)));
 public static final DeferredItem<BlockItem> HELIPAD_ITEM=MareEletrica.ITEMS.register("helipad",()->new BlockItem(HELIPAD.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Helipad>> HELIPAD_ENTITY=MareEletrica.ENTITIES.register("helipad",()->BlockEntityType.Builder.of(Helipad::new,HELIPAD.get()).build(null));
 public static final DeferredBlock<LiftBlock> LIFT=MareEletrica.BLOCKS.register("vehicle_lift",()->new LiftBlock(BlockBehaviour.Properties.of().strength(5F).sound(SoundType.METAL).noOcclusion(),false));
 public static final DeferredBlock<LiftBlock> LIFT_STOP=MareEletrica.BLOCKS.register("lift_stop",()->new LiftBlock(BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL).noOcclusion(),true));
 public static final DeferredItem<BlockItem> LIFT_ITEM=MareEletrica.ITEMS.register("vehicle_lift",()->new BlockItem(LIFT.get(),new Item.Properties()));
 public static final DeferredItem<BlockItem> LIFT_STOP_ITEM=MareEletrica.ITEMS.register("lift_stop",()->new BlockItem(LIFT_STOP.get(),new Item.Properties()));
 public static final Supplier<BlockEntityType<Lift>> LIFT_ENTITY=MareEletrica.ENTITIES.register("vehicle_lift",()->BlockEntityType.Builder.of(Lift::new,LIFT.get()).build(null));
 public static final Supplier<EntityType<Platform>> PLATFORM=MareEletrica.ACTORS.register("lift_platform",()->
  EntityType.Builder.<Platform>of(Platform::new,MobCategory.MISC).sized(5F,.5F).clientTrackingRange(10).updateInterval(1).build(MareEletrica.ID+":lift_platform"));
 private BaseBlocks(){}
 public static void register(){}

 // ------------------------------------------------------------ runway light
 public static final class RunwayLight extends HorizontalDirectionalBlock implements EntityBlock {
  public static final MapCodec<RunwayLight> CODEC=simpleCodec(RunwayLight::new);
  public RunwayLight(BlockBehaviour.Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  /** The chase runs the way the player faces when placing. */
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection());}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return Block.box(4,0,4,12,3,12);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Light(p,s);}
 }
 public static final class Light extends BlockEntity {public Light(BlockPos p,BlockState s){super(RUNWAY_ENTITY.get(),p,s);}}

 // ------------------------------------------------------------ helipad
 public static final class HelipadBlock extends Block implements EntityBlock,Multiblock.Master {
  public HelipadBlock(BlockBehaviour.Properties p){super(p);}
  static final double[][] BOXES={{-16,0,-16,32,1.5,32}};
  @Override public java.util.List<BlockPos> cells(BlockState s){return Multiblock.cellsOf(Direction.NORTH,BOXES);}
  @Override public VoxelShape cellShape(BlockState s,BlockPos o){return Multiblock.slice(Direction.NORTH,BOXES,o);}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack st){Multiblock.place(l,p,s);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock()))Multiblock.clear(l,p,s);super.onRemove(s,l,p,n,moving);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var st=defaultBlockState();return Multiblock.fits(c,st)?st:null;}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return cellShape(s,BlockPos.ZERO);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Helipad(p,s);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(!l.isClientSide)player.displayClientMessage(Component.literal(callHelicopter((ServerLevel)l,p)),true);
   return InteractionResult.sidedSuccess(l.isClientSide);
  }
 }
 public static final class Helipad extends BlockEntity {public Helipad(BlockPos p,BlockState s){super(HELIPAD_ENTITY.get(),p,s);}}
 /** Sends the nearest empty helicopter (within 200 blocks) to land on the helipad closest to {@code near}. */
 public static String callHelicopter(ServerLevel l,BlockPos near){
  Helipad pad=null;double bd=1e18;
  for(var h:BaseControl.around(l,near,BaseControl.RADIUS,Helipad.class)){double d=h.getBlockPos().distSqr(near);if(d<bd){bd=d;pad=h;}}
  if(pad==null)return "§eNenhum heliponto perto.";
  Vec3 spot=pad.getBlockPos().getCenter().add(0,-.5+2/16.0,0);
  Vehicles.Helicopter best=null;double hd=1e18;
  for(var h:l.getEntitiesOfClass(Vehicles.Helicopter.class,new AABB(pad.getBlockPos()).inflate(200),h->h.getPassengers().isEmpty())){double d=h.distanceToSqr(spot);if(d<hd){hd=d;best=h;}}
  if(best==null)return "§eNenhum helicóptero vazio a até 200 blocos.";
  if(best.fuel()<=0)return "§cO helicóptero mais perto está sem combustível.";
  if(hd<4)return "O helicóptero já está no heliponto.";
  best.autoTarget=spot;
  return "§aHelicóptero a caminho do heliponto ("+Math.round(Math.sqrt(hd))+" m).";
 }

 // ------------------------------------------------------------ vehicle lift
 /** The lift (bottom) and its stops (upper floors). Right-click either one, or power the lift with redstone, to move the platform. */
 public static final class LiftBlock extends Block implements EntityBlock,Multiblock.Master {
  final boolean stop;
  public LiftBlock(BlockBehaviour.Properties p,boolean stop){super(p);this.stop=stop;}
  static final double[][] BOXES={{-16,0,-16,32,6,32},{-16,6,-16,32,12,-14},{-16,6,30,32,12,32},{-16,6,-14,-14,12,30},{30,6,-14,32,12,30}};
  @Override public java.util.List<BlockPos> cells(BlockState s){return stop?java.util.List.of():Multiblock.cellsOf(Direction.NORTH,BOXES);}
  @Override public VoxelShape cellShape(BlockState s,BlockPos o){return Multiblock.slice(Direction.NORTH,BOXES,o);}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,net.minecraft.world.item.ItemStack st){Multiblock.place(l,p,s);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moving){if(!s.is(n.getBlock()))Multiblock.clear(l,p,s);super.onRemove(s,l,p,n,moving);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var st=defaultBlockState();return Multiblock.fits(c,st)?st:null;}

  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return stop?null:new Lift(p,s);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return stop?Block.box(0,0,0,16,4,16):cellShape(s,BlockPos.ZERO);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
   if(l.isClientSide)return InteractionResult.SUCCESS;
   Lift lift=stop?findLift(l,p):l.getBlockEntity(p) instanceof Lift x?x:null;
   if(lift==null){player.displayClientMessage(Component.literal("§eNenhum elevador embaixo desta parada (até 64 blocos)."),true);return InteractionResult.CONSUME;}
   player.displayClientMessage(Component.literal(lift.call((ServerLevel)l,stop?p:null)),true);
   return InteractionResult.CONSUME;
  }
  @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos from,boolean moving){
   if(!stop&&!l.isClientSide&&l.hasNeighborSignal(p)&&l.getBlockEntity(p) instanceof Lift x&&l.getGameTime()-x.lastSignal>20){x.lastSignal=l.getGameTime();x.call((ServerLevel)l,null);}
  }
  static Lift findLift(Level l,BlockPos stop){for(int i=1;i<=64;i++)if(l.getBlockEntity(stop.below(i)) instanceof Lift x)return x;return null;}
 }
 public static final class Lift extends BlockEntity {
  UUID platform;long lastSignal;
  public Lift(BlockPos p,BlockState s){super(LIFT_ENTITY.get(),p,s);}
  double bottom(){return worldPosition.getY()+12/16.0;}
  Platform platform(ServerLevel l){
   if(platform!=null&&l.getEntity(platform) instanceof Platform pl&&pl.isAlive())return pl;
   var pl=new Platform(PLATFORM.get(),l);pl.setPos(worldPosition.getX()+.5,bottom(),worldPosition.getZ()+.5);pl.home=worldPosition;pl.targetY=bottom();
   l.addFreshEntity(pl);platform=pl.getUUID();setChanged();return pl;
  }
  /** Moves the platform to a stop (or, from the bottom block, to the next stop up / back down). */
  String call(ServerLevel l,BlockPos to){
   var pl=platform(l);
   double target;
   if(to!=null)target=to.getY()+4/16.0;
   else if(pl.getY()>bottom()+.5)target=bottom();
   else{BlockPos stop=null;for(int i=2;i<=64;i++){var q=worldPosition.above(i);if(l.getBlockState(q).is(LIFT_STOP.get())){stop=q;break;}}
    if(stop==null)return "§eColoque uma Parada de elevador em cima (mesma coluna) para ele subir.";target=stop.getY()+4/16.0;}
   pl.targetY=target;l.playSound(null,worldPosition,SoundEvents.PISTON_EXTEND,SoundSource.BLOCKS,1F,.5F);
   return target>pl.getY()?"Elevador subindo…":"Elevador descendo…";
  }
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);if(platform!=null)t.putUUID("Platform",platform);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);platform=t.hasUUID("Platform")?t.getUUID("Platform"):null;}
 }
 /** 5x5 platform for people: it carries players, mobs and items up and down the shaft (keep the 5x5 shaft clear). */
 public static final class Platform extends Entity {
  public double targetY;BlockPos home;public static final double SPEED=.15;
  public Platform(EntityType<? extends Platform> t,Level l){super(t,l);noPhysics=true;}
  @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
  @Override public boolean canBeCollidedWith(){return true;}
  @Override public boolean isPickable(){return !isRemoved();}
  @Override public boolean isPushable(){return false;}
  @Override public boolean isNoGravity(){return true;}
  @Override public InteractionResult interact(Player p,InteractionHand h){
   if(!level().isClientSide&&home!=null&&level().getBlockEntity(home) instanceof Lift lift)p.displayClientMessage(Component.literal(lift.call((ServerLevel)level(),null)),true);
   return InteractionResult.sidedSuccess(level().isClientSide);
  }
  @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float a){return false;}
  @Override public void tick(){
   super.tick();
   if(level().isClientSide)return;
   if(home==null||!(level().getBlockEntity(home) instanceof Lift)){discard();return;}
   double dy=targetY-getY();if(Math.abs(dy)<.001)return;
   double step=Math.signum(dy)*Math.min(SPEED,Math.abs(dy));
   var box=getBoundingBox();
   // a lift for people (and mobs, items): vehicles do not ride it
   var riders=level().getEntities(this,new AABB(box.minX,box.maxY-.2,box.minZ,box.maxX,box.maxY+4,box.maxZ),e->!(e instanceof Platform)&&!(e instanceof Vehicles.Base)&&!(e instanceof Jets.JetEntity)&&!e.isPassenger()&&e.getY()>=box.maxY-.3);
   setPos(getX(),getY()+step,getZ());
   for(var e:riders){
    double y=getBoundingBox().maxY;
    // players: only the height is set, so they can still walk around on the platform while it moves
    if(e instanceof ServerPlayer sp)sp.connection.teleport(0,Math.max(sp.getY()+step,y),0,0,0,java.util.EnumSet.of(RelativeMovement.X,RelativeMovement.Z,RelativeMovement.Y_ROT,RelativeMovement.X_ROT));
    else e.setPos(e.getX(),Math.max(e.getY()+step,y),e.getZ());
    e.fallDistance=0;e.setOnGround(true);
   }
   if(tickCount%10==0)level().playSound(null,blockPosition(),SoundEvents.MINECART_RIDING,SoundSource.BLOCKS,.4F,.6F);
   if(Math.abs(targetY-getY())<.001)level().playSound(null,blockPosition(),SoundEvents.PISTON_CONTRACT,SoundSource.BLOCKS,1F,.6F);
  }
  @Override protected void addAdditionalSaveData(CompoundTag t){t.putDouble("TargetY",targetY);if(home!=null)t.putLong("Home",home.asLong());}
  @Override protected void readAdditionalSaveData(CompoundTag t){targetY=t.getDouble("TargetY");home=t.contains("Home")?BlockPos.of(t.getLong("Home")):null;}
 }
}
