package br.mareeletrica;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.91.0: replaces the old water duct / intake / outlet. A water pump sucks from the water source in front of it and
 *  pushes it through thick pipes; every open end of the line pours the water out. */
public final class BigPipe {
 private BigPipe(){}
 static final Map<Direction,BooleanProperty> SIDES=new EnumMap<>(Direction.class);
 static{for(Direction d:Direction.values())SIDES.put(d,BooleanProperty.create(d.getName()));}
 public static final BooleanProperty STRAIGHT=BooleanProperty.create("straight");
 public static final DeferredBlock<PipeBlock> PIPE=MareEletrica.BLOCKS.register("big_pipe",()->new PipeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(2.5F).sound(SoundType.METAL).noOcclusion()));
 public static final DeferredBlock<PumpBlock> PUMP=MareEletrica.BLOCKS.register("water_pump",()->new PumpBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLUE).strength(3F).sound(SoundType.METAL).noOcclusion()));
 public static final DeferredItem<BlockItem> PIPE_ITEM=MareEletrica.ITEMS.register("big_pipe",()->new BlockItem(PIPE.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Liga na Bomba d'Água. Toda ponta aberta do cano despeja a água."));}});
 public static final DeferredItem<BlockItem> PUMP_ITEM=MareEletrica.ITEMS.register("water_pump",()->new BlockItem(PUMP.get(),new Item.Properties()){
  @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){
   t.add(Component.literal("§7Coloque encostada na água: puxa até 3 blocos de água por segundo"));
   t.add(Component.literal("§7e manda pelos Canos Grandes. Clique numa face para escolher o lado da sucção."));
   t.add(Component.literal("§7Sinal de redstone desliga."));}});
 public static void register(){}

 static boolean links(BlockState s){return s.getBlock() instanceof PipeBlock||s.getBlock() instanceof PumpBlock;}
 /** a pump only connects on the sides that are not its suction side */
 static boolean linkable(BlockState n,Direction toward){return n.getBlock() instanceof PipeBlock||n.getBlock() instanceof PumpBlock&&n.getValue(PumpBlock.FACING)!=toward.getOpposite();}

 public static final class PipeBlock extends Block {
  static final VoxelShape CORE=box(3,3,3,13,13,13);
  static final Map<Direction,VoxelShape> ARM=new EnumMap<>(Direction.class);
  static{ARM.put(Direction.NORTH,box(3,3,0,13,13,3));ARM.put(Direction.SOUTH,box(3,3,13,13,13,16));ARM.put(Direction.WEST,box(0,3,3,3,13,13));ARM.put(Direction.EAST,box(13,3,3,16,13,13));ARM.put(Direction.DOWN,box(3,0,3,13,3,13));ARM.put(Direction.UP,box(3,13,3,13,16,13));}
  public PipeBlock(Properties p){super(p);BlockState s=stateDefinition.any().setValue(STRAIGHT,false);for(var v:SIDES.values())s=s.setValue(v,false);registerDefaultState(s);}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(STRAIGHT);for(var v:SIDES.values())b.add(v);}
  public BlockState connect(BlockState s,LevelAccessor l,BlockPos p){for(Direction d:Direction.values())s=s.setValue(SIDES.get(d),linkable(l.getBlockState(p.relative(d)),d));return straight(s);}
  static BlockState straight(BlockState s){int n=0;for(var v:SIDES.values())if(s.getValue(v))n++;
   boolean st=n==2&&(on(s,Direction.NORTH)&&on(s,Direction.SOUTH)||on(s,Direction.EAST)&&on(s,Direction.WEST)||on(s,Direction.UP)&&on(s,Direction.DOWN));return s.setValue(STRAIGHT,st);}
  static boolean on(BlockState s,Direction d){return s.getValue(SIDES.get(d));}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){return connect(defaultBlockState(),c.getLevel(),c.getClickedPos());}
  @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos p,BlockPos np){return straight(s.setValue(SIDES.get(d),linkable(n,d)));}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){VoxelShape v=CORE;for(Direction d:Direction.values())if(on(s,d))v=Shapes.or(v,ARM.get(d));return v;}
  /** the open end of a line: exactly one connection, the water comes out on the other side */
  static Direction mouth(BlockState s){Direction only=null;for(Direction d:Direction.values())if(on(s,d)){if(only!=null)return null;only=d;}return only==null?null:only.getOpposite();}
 }

 public static final class PumpBlock extends Block {
  public static final DirectionProperty FACING=BlockStateProperties.FACING;   // suction side
  static final VoxelShape SHAPE=Shapes.or(box(1,0,1,15,2,15),box(2,2,2,14,13,14),box(4,13,4,12,16,12));
  public PumpBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){
   // face the water if there is some next to it, otherwise the block that was clicked
   for(Direction d:Direction.values())if(d!=Direction.UP&&c.getLevel().getFluidState(c.getClickedPos().relative(d)).isSource()&&c.getLevel().getFluidState(c.getClickedPos().relative(d)).is(net.minecraft.tags.FluidTags.WATER))return defaultBlockState().setValue(FACING,d);
   return defaultBlockState().setValue(FACING,c.getClickedFace().getOpposite());}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
  @Override protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(!l.isClientSide)l.scheduleTick(p,this,20);}
  @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player pl,BlockHitResult hit){
   if(!l.isClientSide&&pl.mayBuild()){Direction face=hit.getDirection();l.setBlock(p,s.setValue(FACING,face),3);
    pl.displayClientMessage(Component.literal("§bBomba d'Água: §7suga pelo lado "+switch(face){case NORTH->"norte";case SOUTH->"sul";case EAST->"leste";case WEST->"oeste";case UP->"de cima";default->"de baixo";}),true);}
   return InteractionResult.sidedSuccess(l.isClientSide);}
  @Override protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){
   l.scheduleTick(p,this,20);if(l.hasNeighborSignal(p)||l.dimensionType().ultraWarm())return;
   int moved=0;for(int i=0;i<3;i++)if(transfer(l,p,s))moved++;else break;
   if(moved>0&&r.nextInt(3)==0)l.playSound(null,p,SoundEvents.BUCKET_FILL,SoundSource.BLOCKS,.5F,.6F+r.nextFloat()*.2F);}
 }

 /** move one source block of water from in front of the pump to the first open pipe end that can take it */
 static boolean transfer(ServerLevel level,BlockPos pump,BlockState state){
  Direction suck=state.getValue(PumpBlock.FACING);
  BlockPos source=pump.relative(suck);if(!level.hasChunkAt(source))return false;
  boolean artificial=WaterOrigin.artificial(level,source);BlockState before=level.getBlockState(source);
  if(!before.is(Blocks.WATER)||!before.getFluidState().isSource())return false;
  var queue=new ArrayDeque<BlockPos>();Set<BlockPos> seen=new HashSet<>();seen.add(pump);
  for(Direction d:Direction.values())if(d!=suck)queue.add(pump.relative(d));
  while(!queue.isEmpty()&&seen.size()<512){
   BlockPos pos=queue.removeFirst();if(!seen.add(pos)||!level.hasChunkAt(pos)||pos.distManhattan(pump)>96)continue;
   BlockState pipe=level.getBlockState(pos);if(!(pipe.getBlock() instanceof PipeBlock))continue;
   Direction out=PipeBlock.mouth(pipe);
   if(out!=null){
    BlockPos mouth=pos.relative(out),dest=FaucetFill.deposit(level,mouth);
    if(dest!=null&&!dest.equals(source)&&level.hasChunkAt(dest)&&!level.isOutsideBuildHeight(dest)&&level.getWorldBorder().isWithinBounds(dest)&&level.getBlockEntity(dest)==null){
     var actor=FakePlayerFactory.getMinecraft(level);
     if(level.mayInteract(actor,source)&&level.mayInteract(actor,dest)&&!NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,source,before,actor)).isCanceled()){
      if(!level.getBlockState(source).equals(before))return false;
      var snapshot=BlockSnapshot.create(level.dimension(),level,dest);
      if(level.setBlock(dest,MareEletrica.PRESSURIZED_WATER_BLOCK.get().defaultBlockState(),3)){
       if(net.neoforged.neoforge.event.EventHooks.onBlockPlace(actor,snapshot,out)){snapshot.restore(3);}
       else{
        if(!level.getBlockState(source).equals(before)||!level.setBlock(source,Blocks.AIR.defaultBlockState(),3)){snapshot.restore(3);return false;}
        if(artificial)WaterOrigin.mark(level,dest);else WaterOrigin.clear(level,dest);
        FaucetFill.stream(level,mouth,out);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,mouth.getX()+.5,mouth.getY()+.5,mouth.getZ()+.5,12,.25,.25,.25,.15);
        return true;}}}}}
   for(Direction d:Direction.values())if(PipeBlock.on(pipe,d))queue.addLast(pos.relative(d));
  }
  return false;
 }
}
