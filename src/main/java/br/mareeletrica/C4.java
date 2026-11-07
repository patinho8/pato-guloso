package br.mareeletrica;

import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** C4: a placed block with a programmable timer (real minutes/hours or Minecraft days). Explodes and breaks blocks. */
@EventBusSubscriber(modid=MareEletrica.ID,bus=EventBusSubscriber.Bus.MOD)
public final class C4 {
 public static final DeferredBlock<C4Block> BLOCK=MareEletrica.BLOCKS.register("c4",()->new C4Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(2.5F).noOcclusion().sound(SoundType.METAL)));
 public static final DeferredItem<BlockItem> ITEM=MareEletrica.ITEMS.register("c4",()->new BlockItem(BLOCK.get(),new Item.Properties().stacksTo(4)));
 public static final Supplier<BlockEntityType<C4Entity>> ENTITY=MareEletrica.ENTITIES.register("c4",()->BlockEntityType.Builder.of(C4Entity::new,BLOCK.get()).build(null));
 /** Set by the client to open the timer screen. */
 public static Consumer<BlockPos> openScreen=p->{};
 private C4(){}
 public static void register(){}

 public static String format(long ticks){
  if(ticks>=24000)return (ticks/24000)+" dia(s) e "+Math.round((ticks%24000)/1000F)+" h do Minecraft";
  long s=ticks/20;return String.format("%02d:%02d",s/60,s%60);
 }

 public static final class C4Block extends BaseEntityBlock {
  public static final DirectionProperty FACING=BlockStateProperties.FACING;
  public static final BooleanProperty ARMED=BooleanProperty.create("armed");
  public static final MapCodec<C4Block> CODEC=simpleCodec(C4Block::new);
  private static final VoxelShape UP=Block.box(1,0,3,15,6,13),DOWN=Block.box(1,10,3,15,16,13),NORTH=Block.box(1,3,10,15,13,16),SOUTH=Block.box(1,3,0,15,13,6),EAST=Block.box(0,3,1,6,13,15),WEST=Block.box(10,3,1,16,13,15);
  public C4Block(BlockBehaviour.Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.UP).setValue(ARMED,false));}
  @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,ARMED);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){var s=defaultBlockState().setValue(FACING,c.getClickedFace());return s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
  @Override protected boolean canSurvive(BlockState state,net.minecraft.world.level.LevelReader level,BlockPos pos){var support=pos.relative(state.getValue(FACING).getOpposite());return level.getBlockState(support).isFaceSturdy(level,support,state.getValue(FACING));}
  @Override protected BlockState updateShape(BlockState state,Direction dir,BlockState neighbor,net.minecraft.world.level.LevelAccessor level,BlockPos pos,BlockPos neighborPos){return dir==state.getValue(FACING).getOpposite()&&!state.canSurvive(level,pos)?net.minecraft.world.level.block.Blocks.AIR.defaultBlockState():super.updateShape(state,dir,neighbor,level,pos,neighborPos);}
  @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
  @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext ctx){return switch(state.getValue(FACING)){case UP->UP;case DOWN->DOWN;case NORTH->NORTH;case SOUTH->SOUTH;case EAST->EAST;case WEST->WEST;};}
  @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new C4Entity(pos,state);}
  @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide?null:createTickerHelper(type,ENTITY.get(),C4Entity::tick);}
  @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
   if(!state.getValue(ARMED)){if(level.isClientSide)openScreen.accept(pos);return InteractionResult.sidedSuccess(level.isClientSide);}
   if(!level.isClientSide&&level.getBlockEntity(pos) instanceof C4Entity c4){
    if(player.getMainHandItem().is(Items.SHEARS)){c4.disarm();level.setBlock(pos,state.setValue(ARMED,false),3);level.playSound(null,pos,SoundEvents.SHEEP_SHEAR,SoundSource.BLOCKS,1F,1F);player.displayClientMessage(Component.literal("C4 desarmada."),true);}
    else player.displayClientMessage(Component.literal("C4 armada — explode em "+format(c4.remaining())+". Corte o fio com uma tesoura para desarmar."),true);
   }
   return InteractionResult.sidedSuccess(level.isClientSide);
  }
 }

 public static final class C4Entity extends BlockEntity {
  private long at=-1;
  public C4Entity(BlockPos pos,BlockState state){super(ENTITY.get(),pos,state);}
  public void arm(long ticks){at=level.getGameTime()+ticks;sync();}
  public void disarm(){at=-1;sync();}
  private void sync(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
  /** Client copy of the timer, for the red countdown on the C4's screen. */
  public long at(){return at;}
  @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){var t=new CompoundTag();t.putLong("At",at);return t;}
  @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
  public long remaining(){return at<0||level==null?-1:Math.max(0,at-level.getGameTime());}
  public static void tick(Level level,BlockPos pos,BlockState state,C4Entity be){
   if(be.at<0)return;
   long left=be.at-level.getGameTime();
   if(left<=0){be.at=-1;level.removeBlock(pos,false);double x=pos.getX()+.5,y=pos.getY()+.5,z=pos.getZ()+.5;
    level.explode(null,null,null,x,y,z,13F,false,Level.ExplosionInteraction.TNT,net.minecraft.core.particles.ParticleTypes.EXPLOSION,net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,MareEletrica.SILENT);
    level.playSound(null,pos,MareEletrica.C4_BLAST.get(),net.minecraft.sounds.SoundSource.BLOCKS,2.5F,.85F);
    if(level instanceof net.minecraft.server.level.ServerLevel sl){sl.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,x,y+1,z,6,2.5,1.5,2.5,0);sl.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,x,y+1,z,160,3.5,2,3.5,.08);sl.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,x,y,z,90,1,.6,1,.45);}return;}
   long interval=left>1200?100:left>200?40:left>100?20:left>40?10:5;
   if(level.getGameTime()%interval==0)level.playSound(null,pos,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.9F,left>200?1.4F:1.9F);
  }
  @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);tag.putLong("At",at);}
  @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);at=tag.contains("At")?tag.getLong("At"):-1;}
 }

 /** Client asks the server to arm the C4 at pos with amount x unit (0 = minutes, 1 = hours, 2 = Minecraft days, 3 = seconds). */
 public record Arm(BlockPos pos,int amount,int unit) implements CustomPacketPayload {
  public static final Type<Arm> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(MareEletrica.ID,"c4_arm"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Arm> CODEC=StreamCodec.composite(BlockPos.STREAM_CODEC,Arm::pos,ByteBufCodecs.VAR_INT,Arm::amount,ByteBufCodecs.VAR_INT,Arm::unit,Arm::new);
  @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 @SubscribeEvent public static void packets(RegisterPayloadHandlersEvent e){
  e.registrar("5").playToServer(Arm.TYPE,Arm.CODEC,(p,c)->{
   if(!(c.player() instanceof ServerPlayer player))return;
   var level=player.level();var pos=p.pos();var state=level.getBlockState(pos);
   if(player.distanceToSqr(pos.getCenter())>64||!(state.getBlock() instanceof C4Block)||state.getValue(C4Block.ARMED))return;
   long ticks=(long)Mth.clamp(p.amount(),1,9999)*(p.unit()==0?1200L:p.unit()==1?72000L:p.unit()==3?20L:24000L);
   if(level.getBlockEntity(pos) instanceof C4Entity be){
    be.arm(ticks);level.setBlock(pos,state.setValue(C4Block.ARMED,true),3);
    level.playSound(null,pos,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,1F,.8F);
    player.displayClientMessage(Component.literal("C4 armada — explode em "+format(ticks)+"."),true);
   }
  });
 }
}
