package br.mareeletrica;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.89.0: Ferris wheel — a ticket booth block that raises a 16-block wheel with 8 coloured gondolas that stay level while it turns.
 *  Click: board the gondola at the bottom • Shift+click: stop / start • the lights chase around the rim at night. */
public final class FerrisWheel {
 private FerrisWheel(){}
 public static final double R=7.0,HUB=R+3.5;        // radius and hub height above the booth
 public static final int CABINS=8;
 public static final float SPEED=.30F;              // degrees per tick (one turn in 60 s)
 public static final DeferredBlock<BoothBlock> BLOCK=MareEletrica.BLOCKS.register("ferris_wheel",()->new BoothBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(3F).sound(SoundType.METAL).noOcclusion().lightLevel(s->10)));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("ferris_wheel",()->new BlockItem(BLOCK.get(),new Item.Properties().stacksTo(1)){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§dRoda-gigante§7 de 16 blocos com 8 cabines."));
   t.add(Component.literal("§7Clique: entrar na cabine de baixo • Shift+clique: parar/ligar"));
   t.add(Component.literal("§7Precisa de espaço livre: 18 de altura, 8 para cada lado."));}});
 public static final Supplier<BlockEntityType<Wheel>> ENTITY=MareEletrica.ENTITIES.register("ferris_wheel",()->BlockEntityType.Builder.of(Wheel::new,BLOCK.get()).build(null));
 public static final Supplier<EntityType<Cabin>> SEAT=MareEletrica.ACTORS.register("ferris_seat",()->EntityType.Builder.<Cabin>of(Cabin::new,MobCategory.MISC).sized(.6F,.4F).noSave().clientTrackingRange(16).updateInterval(1).build(MareEletrica.ID+":ferris_seat"));
 public static void register(){}

 /** horizontal unit vector in the plane of the wheel (the wheel faces the player who placed it) */
 public static Vec3 side(Direction f){var c=f.getClockWise();return new Vec3(c.getStepX(),0,c.getStepZ());}
 public static Vec3 hub(BlockPos p){return Vec3.atBottomCenterOf(p).add(0,HUB,0);}
 /** where gondola i hangs from the rim at wheel angle a (degrees) */
 public static Vec3 pivot(BlockPos p,Direction f,float a,int i){
  double t=Math.toRadians(a+i*360.0/CABINS);return hub(p).add(side(f).scale(Math.cos(t)*R)).add(0,Math.sin(t)*R,0);}

 public static final class BoothBlock extends HorizontalDirectionalBlock implements EntityBlock {
  static final VoxelShape[] SHAPES=new VoxelShape[4];
  static{for(var d:Direction.Plane.HORIZONTAL)SHAPES[d.get2DDataValue()]=Furniture.shape(new double[][]{{0,0,0,16,2,16},{2,2,3,14,14,13},{1,14,2,15,16,14}},d);}
  public BoothBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec(){return com.mojang.serialization.MapCodec.unit(this);}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPES[s.getValue(FACING).get2DDataValue()];}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new Wheel(p,s);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return t==ENTITY.get()?(lv,p,st,be)->((Wheel)be).tick():null;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player pl,BlockHitResult h){
   if(l.isClientSide)return InteractionResult.SUCCESS;
   if(!(l.getBlockEntity(p) instanceof Wheel w))return InteractionResult.PASS;
   if(pl.isShiftKeyDown()){w.running=!w.running;w.sync();
    l.playSound(null,p,w.running?SoundEvents.NOTE_BLOCK_CHIME.value():SoundEvents.NOTE_BLOCK_BASS.value(),SoundSource.BLOCKS,1F,w.running?1.2F:.6F);
    pl.displayClientMessage(Component.literal(w.running?"§d🎡 Roda-gigante ligada!":"§7🎡 Roda-gigante parada."),true);return InteractionResult.CONSUME;}
   if(pl.isPassenger())return InteractionResult.PASS;
   // the free gondola closest to the bottom
   int best=-1;double low=1e9;
   for(int i=0;i<CABINS;i++){if(w.taken(i))continue;double y=pivot(p,s.getValue(FACING),w.angle,i).y;if(y<low){low=y;best=i;}}
   if(best<0){pl.displayClientMessage(Component.literal("§7Todas as cabines estão ocupadas."),true);return InteractionResult.CONSUME;}
   var c=new Cabin(SEAT.get(),l);c.wheel=p;c.index=best;c.place();l.addFreshEntity(c);pl.startRiding(c);pl.setYRot(s.getValue(FACING).toYRot());pl.setYHeadRot(pl.getYRot());
   l.playSound(null,p,SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,.6F,1.4F);
   pl.displayClientMessage(Component.literal("§d🎡 Boa viagem! §7(Shift para descer)"),true);
   return InteractionResult.CONSUME;}
 }

 public static final class Wheel extends BlockEntity {
  public float angle,angleO;public boolean running=true;int syncT;
  public Wheel(BlockPos p,BlockState s){super(ENTITY.get(),p,s);}
  public Direction facing(){var s=getBlockState();return s.hasProperty(HorizontalDirectionalBlock.FACING)?s.getValue(HorizontalDirectionalBlock.FACING):Direction.NORTH;}
  public float angle(float pt){return Mth.lerp(pt,angleO,angle);}
  void tick(){
   angleO=angle;if(running)angle+=SPEED;
   if(angle>=360){angle-=360;angleO-=360;}
   if(level!=null&&!level.isClientSide){
    if(++syncT>=200){syncT=0;sync();}   // keep clients in step
    if(running&&level.getGameTime()%140==0)level.playSound(null,worldPosition,SoundEvents.NOTE_BLOCK_CHIME.value(),SoundSource.BLOCKS,.5F,.8F+level.random.nextFloat()*.6F);}}
  boolean taken(int i){return level!=null&&!level.getEntitiesOfClass(Cabin.class,new net.minecraft.world.phys.AABB(worldPosition).inflate(R+3),c->c.index==i&&c.wheel!=null&&c.wheel.equals(worldPosition)).isEmpty();}
  void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);t.putFloat("Angle",angle);t.putBoolean("Running",running);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);float a=t.getFloat("Angle");
   if(level==null||Math.abs(Mth.wrapDegrees(a-angle))>3){angle=a;angleO=a;}running=t.getBoolean("Running");}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();saveAdditional(t,r);return t;}
  @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 }

 /** invisible seat inside a gondola; follows the wheel on both sides so the ride is smooth */
 public static final class Cabin extends Entity {
  static final net.minecraft.network.syncher.EntityDataAccessor<BlockPos> WHEEL=net.minecraft.network.syncher.SynchedEntityData.defineId(Cabin.class,net.minecraft.network.syncher.EntityDataSerializers.BLOCK_POS);
  static final net.minecraft.network.syncher.EntityDataAccessor<Integer> INDEX=net.minecraft.network.syncher.SynchedEntityData.defineId(Cabin.class,net.minecraft.network.syncher.EntityDataSerializers.INT);
  BlockPos wheel;int index;
  public Cabin(EntityType<? extends Cabin> t,Level l){super(t,l);noPhysics=true;}
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){b.define(WHEEL,BlockPos.ZERO);b.define(INDEX,0);}
  Wheel wheel(){if(level().isClientSide){wheel=entityData.get(WHEEL);index=entityData.get(INDEX);}
   return wheel!=null&&level().getBlockEntity(wheel) instanceof Wheel w?w:null;}
  void place(){var w=wheel();if(w==null)return;var f=w.facing().getOpposite();var p=pivot(wheel,w.facing(),w.angle,index).add(f.getStepX()*.3,-2.75,f.getStepZ()*.3);setPos(p.x,p.y,p.z);setYRot(w.facing().toYRot());
   if(!level().isClientSide){entityData.set(WHEEL,wheel);entityData.set(INDEX,index);}}
  @Override public void tick(){
   super.tick();var w=wheel();
   if(w==null){if(!level().isClientSide)discard();return;}
   place();
   if(!level().isClientSide&&getPassengers().isEmpty())discard();}
  @Override protected Vec3 getPassengerAttachmentPoint(Entity e,EntityDimensions d,float s){return Vec3.ZERO;}
  @Override public Vec3 getDismountLocationForPassenger(LivingEntity e){
   var w=wheel();if(w==null)return position();
   // getting off high up: drop down beside the booth instead of falling
   if(getY()>wheel.getY()+2.5)return Vec3.atBottomCenterOf(wheel.relative(w.facing(),2));
   return position().add(0,.6,0);}
  @Override protected void readAdditionalSaveData(CompoundTag t){}
  @Override protected void addAdditionalSaveData(CompoundTag t){}
 }
}
