package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.69.0: furniture — sofa, armchair and chair you can sit on, table, lamp, a TV with channels, a fridge and a double bed. */
public final class Furniture {
 private Furniture(){}
 static BlockBehaviour.Properties wood(){return BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion();}
 static BlockBehaviour.Properties cloth(MapColor c){return BlockBehaviour.Properties.of().mapColor(c).strength(1F).sound(SoundType.WOOL).noOcclusion();}
 static BlockBehaviour.Properties metal(){return BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(2F).sound(SoundType.METAL).noOcclusion();}
 public static final DeferredBlock<SeatBlock> SOFA=MareEletrica.BLOCKS.register("sofa",()->new SeatBlock(cloth(MapColor.COLOR_GRAY),.42,new double[][]{{0,0,0,16,8,16},{0,8,12,16,16,16},{0,8,0,2,12,12},{14,8,0,16,12,12}}));
 public static final DeferredBlock<SeatBlock> ARMCHAIR=MareEletrica.BLOCKS.register("armchair",()->new SeatBlock(cloth(MapColor.COLOR_RED),.42,new double[][]{{1,0,1,15,8,15},{1,8,12,15,16,15},{1,8,1,3,12,12},{13,8,1,15,12,12}}));
 public static final DeferredBlock<SeatBlock> CHAIR=MareEletrica.BLOCKS.register("chair",()->new SeatBlock(wood(),.45,new double[][]{{2,0,2,14,8,14},{2,8,12,14,19,14}}));
 public static final DeferredBlock<ShapeBlock> TABLE=MareEletrica.BLOCKS.register("table",()->new ShapeBlock(wood(),new double[][]{{0,13,0,16,16,16},{1,0,1,3,13,3},{13,0,1,15,13,3},{1,0,13,3,13,15},{13,0,13,15,13,15}}));
 public static final DeferredBlock<LampBlock> LAMP=MareEletrica.BLOCKS.register("floor_lamp",()->new LampBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(.8F).sound(SoundType.WOOD).noOcclusion().lightLevel(s->s.getValue(LampBlock.LIT)?15:0)));
 public static final DeferredBlock<TvBlock> TV=MareEletrica.BLOCKS.register("tv",()->new TvBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(1.5F).sound(SoundType.METAL).noOcclusion().lightLevel(s->s.getValue(TvBlock.ON)?6:0)));
 public static final DeferredBlock<FridgeBlock> FRIDGE=MareEletrica.BLOCKS.register("fridge",()->new FridgeBlock(metal()));
 public static final DeferredBlock<DoubleBed> BED=MareEletrica.BLOCKS.register("double_bed",()->new DoubleBed(net.minecraft.world.item.DyeColor.BLUE,BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(.4F).sound(SoundType.WOOD).noOcclusion()));
 public static final Supplier<BlockEntityType<TvEntity>> TV_ENTITY=MareEletrica.ENTITIES.register("tv",()->BlockEntityType.Builder.of(TvEntity::new,TV.get()).build(null));
 public static final Supplier<BlockEntityType<FridgeEntity>> FRIDGE_ENTITY=MareEletrica.ENTITIES.register("fridge",()->BlockEntityType.Builder.of(FridgeEntity::new,FRIDGE.get()).build(null));
 public static final Supplier<BlockEntityType<BedEntity2>> BED_ENTITY=MareEletrica.ENTITIES.register("double_bed",()->BlockEntityType.Builder.of(BedEntity2::new,BED.get()).build(null));
 public static final Supplier<EntityType<Seat>> SEAT=MareEletrica.ACTORS.register("seat",()->EntityType.Builder.<Seat>of(Seat::new,MobCategory.MISC).sized(.01F,.01F).noSave().clientTrackingRange(8).build(MareEletrica.ID+":seat"));
 static DeferredItem<BlockItem> item(DeferredBlock<? extends Block> b,String tip){return MareEletrica.ITEMS.register(b.getId().getPath(),()->new BlockItem(b.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){if(!tip.isEmpty())t.add(Component.literal("§7"+tip));}});}
 public static final DeferredItem<BlockItem> SOFA_ITEM=item(SOFA,"Clique para sentar. Coloque vários lado a lado."),ARMCHAIR_ITEM=item(ARMCHAIR,"Clique para sentar."),CHAIR_ITEM=item(CHAIR,"Clique para sentar."),
  TABLE_ITEM=item(TABLE,""),LAMP_ITEM=item(LAMP,"Clique: liga/desliga."),TV_ITEM=item(TV,"Clique: troca de canal • Shift+clique: liga/desliga"),FRIDGE_ITEM=item(FRIDGE,"Guarda 27 itens. 2 blocos de altura."),
  BED_ITEM=item(BED,"Cama larga para dormir (2 blocos de comprimento).");
 public static void creative(java.util.function.Consumer<ItemStack> out){for(var i:List.of(SOFA_ITEM,ARMCHAIR_ITEM,CHAIR_ITEM,TABLE_ITEM,LAMP_ITEM,TV_ITEM,FRIDGE_ITEM,BED_ITEM))out.accept(new ItemStack(i.get()));}
 public static void register(){}

 /** shapes are written for a block facing north; rotated for the others */
 static VoxelShape shape(double[][] boxes,Direction f){VoxelShape s=Shapes.empty();for(var b:boxes)s=Shapes.or(s,Multiblock.box(f,b[0],b[1],b[2],b[3],b[4],b[5]));return s;}

 public static class ShapeBlock extends HorizontalDirectionalBlock {
  final VoxelShape[] shapes=new VoxelShape[4];
  public ShapeBlock(Properties p,double[][] boxes){super(p);for(var d:Direction.Plane.HORIZONTAL)shapes[d.get2DDataValue()]=shape(boxes,d);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return MapCodec.unit(this);}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return shapes[s.getValue(FACING).get2DDataValue()];}
 }
 public static final class SeatBlock extends ShapeBlock {
  final double height;
  public SeatBlock(Properties p,double h,double[][] boxes){super(p,boxes);height=h;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   if(p.isShiftKeyDown()||p.isPassenger())return InteractionResult.PASS;
   if(!l.isClientSide){if(!l.getEntitiesOfClass(Seat.class,new net.minecraft.world.phys.AABB(pos)).isEmpty())return InteractionResult.SUCCESS;
    var seat=new Seat(SEAT.get(),l);var f=s.getValue(FACING);seat.moveTo(pos.getX()+.5-f.getStepX()*.06,pos.getY()+height,pos.getZ()+.5-f.getStepZ()*.06,f.toYRot(),0);l.addFreshEntity(seat);p.startRiding(seat);}
   return InteractionResult.sidedSuccess(l.isClientSide);}
 }
 public static final class Seat extends Entity {
  public Seat(EntityType<? extends Seat> t,Level l){super(t,l);noPhysics=true;}
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
  @Override public void tick(){super.tick();if(!level().isClientSide&&(getPassengers().isEmpty()||!(level().getBlockState(blockPosition()).getBlock() instanceof SeatBlock)))discard();}
  @Override protected Vec3 getPassengerAttachmentPoint(Entity e,EntityDimensions d,float s){return Vec3.ZERO;}
  @Override protected void positionRider(Entity r,Entity.MoveFunction m){super.positionRider(r,m);if(r instanceof net.minecraft.world.entity.LivingEntity le){float y=getYRot();le.setYBodyRot(y);float d=net.minecraft.util.Mth.wrapDegrees(le.getYRot()-y);le.setYRot(y+net.minecraft.util.Mth.clamp(d,-100,100));le.setYHeadRot(le.getYRot());}}
  @Override public Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity e){return Vec3.atBottomCenterOf(blockPosition().above());}
  @Override protected void readAdditionalSaveData(CompoundTag t){}
  @Override protected void addAdditionalSaveData(CompoundTag t){}
 }
 public static final class LampBlock extends ShapeBlock {
  public static final BooleanProperty LIT=BlockStateProperties.LIT;
  public LampBlock(Properties p){super(p,new double[][]{{4,0,4,12,1.5,12},{7.4,1.5,7.4,8.6,14,8.6},{3,12,3,13,19,13}});registerDefaultState(defaultBlockState().setValue(LIT,true));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,LIT);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   l.setBlock(pos,s.cycle(LIT),3);l.playSound(null,pos,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,.5F,s.getValue(LIT)?.7F:1.2F);return InteractionResult.sidedSuccess(l.isClientSide);}
 }
 // ---------------- TV ----------------
 public static final String[] CHANNELS={"Desenho do Pato","Surfe ao vivo","Futebol","Jornal da Maré","Fliperama TV","Chuvisco"};
 public static final class TvBlock extends ShapeBlock implements EntityBlock {
  public static final BooleanProperty ON=BlockStateProperties.POWERED;public static final IntegerProperty CHANNEL=IntegerProperty.create("channel",0,5);
  public TvBlock(Properties p){super(p,new double[][]{{0,3,6.5,16,14,9.5},{6,0,6,10,1,10},{7,1,7.5,9,3,8.5}});registerDefaultState(defaultBlockState().setValue(ON,true).setValue(CHANNEL,0));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ON,CHANNEL);}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TvEntity(p,s);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   if(!l.isClientSide){
    if(p.isShiftKeyDown()){l.setBlock(pos,s.cycle(ON),3);p.displayClientMessage(Component.literal(s.getValue(ON)?"§7📺 TV desligada":"§a📺 TV ligada — canal "+(s.getValue(CHANNEL)+1)+": "+CHANNELS[s.getValue(CHANNEL)]),true);}
    else{int c=(s.getValue(CHANNEL)+1)%CHANNELS.length;l.setBlock(pos,s.setValue(CHANNEL,c).setValue(ON,true),3);p.displayClientMessage(Component.literal("§b📺 Canal "+(c+1)+": §f"+CHANNELS[c]),true);}
    l.playSound(null,pos,SoundEvents.STONE_BUTTON_CLICK_ON,SoundSource.BLOCKS,.4F,1.6F);}
   return InteractionResult.sidedSuccess(l.isClientSide);}
 }
 public static final class TvEntity extends BlockEntity {public TvEntity(BlockPos p,BlockState s){super(TV_ENTITY.get(),p,s);}}
 // ---------------- fridge (2 tall, 27 slots) ----------------
 public static final class FridgeBlock extends ShapeBlock implements EntityBlock {
  public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
  public FridgeBlock(Properties p){super(p,new double[][]{{1,0,1,15,16,15}});registerDefaultState(defaultBlockState().setValue(HALF,DoubleBlockHalf.LOWER));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,HALF);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var p=c.getClickedPos();if(p.getY()>=c.getLevel().getMaxBuildHeight()-1||!c.getLevel().getBlockState(p.above()).canBeReplaced(c))return null;return super.getStateForPlacement(c).setValue(HALF,DoubleBlockHalf.LOWER);}
  @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity who,ItemStack st){l.setBlock(p.above(),s.setValue(HALF,DoubleBlockHalf.UPPER),3);}
  @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos p,BlockPos np){
   boolean lower=s.getValue(HALF)==DoubleBlockHalf.LOWER;if(d==(lower?Direction.UP:Direction.DOWN)&&!(n.is(this)&&n.getValue(HALF)!=s.getValue(HALF)))return Blocks.AIR.defaultBlockState();return s;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(HALF)==DoubleBlockHalf.LOWER?new FridgeEntity(p,s):null;}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos pos,Player p,BlockHitResult hit){
   if(s.getValue(HALF)==DoubleBlockHalf.UPPER)pos=pos.below();
   if(!l.isClientSide&&l.getBlockEntity(pos) instanceof FridgeEntity f){p.openMenu(new SimpleMenuProvider((id,inv,pl)->ChestMenu.threeRows(id,inv,f),Component.literal("Geladeira")));l.playSound(null,pos,SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.6F,1.4F);}
   return InteractionResult.sidedSuccess(l.isClientSide);}
  @Override protected void onRemove(BlockState s,Level l,BlockPos p,BlockState n,boolean moved){if(!s.is(n.getBlock())&&l.getBlockEntity(p) instanceof FridgeEntity f)Containers.dropContents(l,p,f);super.onRemove(s,l,p,n,moved);}
 }
 public static final class FridgeEntity extends BlockEntity implements net.minecraft.world.Container {
  final net.minecraft.core.NonNullList<ItemStack> items=net.minecraft.core.NonNullList.withSize(27,ItemStack.EMPTY);
  public FridgeEntity(BlockPos p,BlockState s){super(FRIDGE_ENTITY.get(),p,s);}
  @Override public int getContainerSize(){return 27;}
  @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
  @Override public ItemStack getItem(int i){return items.get(i);}
  @Override public ItemStack removeItem(int i,int n){var r=net.minecraft.world.ContainerHelper.removeItem(items,i,n);setChanged();return r;}
  @Override public ItemStack removeItemNoUpdate(int i){return net.minecraft.world.ContainerHelper.takeItem(items,i);}
  @Override public void setItem(int i,ItemStack s){items.set(i,s);setChanged();}
  @Override public boolean stillValid(Player p){return p.distanceToSqr(Vec3.atCenterOf(worldPosition))<64;}
  @Override public void clearContent(){items.clear();}
  @Override public void stopOpen(Player p){if(level!=null)level.playSound(null,worldPosition,SoundEvents.IRON_DOOR_CLOSE,SoundSource.BLOCKS,.6F,1.4F);}
  @Override protected void saveAdditional(CompoundTag t,HolderLookup.Provider r){super.saveAdditional(t,r);net.minecraft.world.ContainerHelper.saveAllItems(t,items,r);}
  @Override protected void loadAdditional(CompoundTag t,HolderLookup.Provider r){super.loadAdditional(t,r);items.clear();net.minecraft.world.ContainerHelper.loadAllItems(t,items,r);}
 }
 // ---------------- double bed: a real (vanilla) bed with a wide model ----------------
 public static final class DoubleBed extends BedBlock {
  public DoubleBed(net.minecraft.world.item.DyeColor c,Properties p){super(c,p);}
  @Override protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
  @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new BedEntity2(p,s);}
 }
 public static final class BedEntity2 extends BlockEntity {public BedEntity2(BlockPos p,BlockState s){super(BED_ENTITY.get(),p,s);}}
}
