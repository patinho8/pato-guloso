package br.mareeletrica;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.*;
import net.neoforged.neoforge.event.level.BlockEvent;

/** One real source block per transfer; never loads chunks or replaces solid blocks. */
public final class WaterDuctBlock extends Block implements SimpleWaterloggedBlock {
 public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
 public static final DirectionProperty FACING=BlockStateProperties.FACING;
 public static final Map<Direction,BooleanProperty> CONNECTIONS=new EnumMap<>(Direction.class);
 static {for(Direction d:Direction.values())CONNECTIONS.put(d,BooleanProperty.create(d.getName()));}
 public final int port; // 0 pipe, 1 blue intake, 2 orange outlet
 public WaterDuctBlock(Properties p,int port){super(p);this.port=port;BlockState s=stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(WATERLOGGED,false);for(var v:CONNECTIONS.values())s=s.setValue(v,false);registerDefaultState(s);}
 protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,WATERLOGGED);for(var v:CONNECTIONS.values())b.add(v);}
 public BlockState getStateForPlacement(BlockPlaceContext c){BlockState s=defaultBlockState().setValue(FACING,c.getNearestLookingDirection().getOpposite()).setValue(WATERLOGGED,c.getLevel().getFluidState(c.getClickedPos()).is(net.minecraft.tags.FluidTags.WATER));for(Direction d:Direction.values())s=s.setValue(CONNECTIONS.get(d),c.getLevel().getBlockState(c.getClickedPos().relative(d)).getBlock() instanceof WaterDuctBlock);return s;}
 protected BlockState updateShape(BlockState s,Direction d,BlockState neighbor,LevelAccessor l,BlockPos p,BlockPos np){if(s.getValue(WATERLOGGED))l.scheduleTick(p,Fluids.WATER,Fluids.WATER.getTickDelay(l));return s.setValue(CONNECTIONS.get(d),neighbor.getBlock() instanceof WaterDuctBlock);}
 protected net.minecraft.world.level.material.FluidState getFluidState(BlockState s){return s.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(s);}
 protected void onPlace(BlockState s,Level l,BlockPos p,BlockState old,boolean moving){if(port==1&&!l.isClientSide)l.scheduleTick(p,this,20);}
 protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(port==0)return InteractionResult.PASS;if(!l.isClientSide&&player.mayBuild()){Direction face=hit.getDirection();l.setBlock(p,s.setValue(FACING,face),3);player.displayClientMessage(net.minecraft.network.chat.Component.literal((port==1?"Entrada azul: ":"Saída laranja: ")+face.getName()),true);}return InteractionResult.sidedSuccess(l.isClientSide);}
 protected VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){VoxelShape shape=box(4,4,4,12,12,12);for(Direction d:Direction.values())if(s.getValue(CONNECTIONS.get(d))||port!=0&&d==s.getValue(FACING))shape=Shapes.or(shape,switch(d){case NORTH->box(4,4,0,12,12,8);case SOUTH->box(4,4,8,12,12,16);case EAST->box(8,4,4,16,12,12);case WEST->box(0,4,4,8,12,12);case UP->box(4,8,4,12,16,12);case DOWN->box(4,0,4,12,8,12);});return shape;}
 protected void tick(BlockState s,ServerLevel l,BlockPos p,RandomSource random){if(port!=1)return;l.scheduleTick(p,this,20);if(l.hasNeighborSignal(p)||l.dimensionType().ultraWarm())return;transfer(l,p,s);}
 public static boolean transfer(ServerLevel level,BlockPos intake,BlockState state){
  BlockPos source=intake.relative(state.getValue(FACING));if(!level.hasChunkAt(source))return false;
  boolean artificial=WaterOrigin.artificial(level,source);BlockState before=level.getBlockState(source);if(!before.is(Blocks.WATER)||!before.getFluidState().isSource())return false;
  var queue=new ArrayDeque<BlockPos>();Set<BlockPos> visited=new HashSet<>();queue.add(intake);int scans=0;
  while(!queue.isEmpty()&&scans++<768&&visited.size()<256){BlockPos pos=queue.removeFirst();if(!level.hasChunkAt(pos)||pos.distManhattan(intake)>64||!visited.add(pos))continue;BlockState duct=level.getBlockState(pos);if(!(duct.getBlock() instanceof WaterDuctBlock b))continue;
   if(b.port==2&&!level.hasNeighborSignal(pos)){
    BlockPos mouth=pos.relative(duct.getValue(FACING)),dest=FaucetFill.deposit(level,mouth);if(dest!=null&&!dest.equals(source)&&level.hasChunkAt(dest)&&!level.isOutsideBuildHeight(dest)&&level.getWorldBorder().isWithinBounds(dest)&&level.getBlockEntity(dest)==null){
     var actor=FakePlayerFactory.getMinecraft(level);if(!level.mayInteract(actor,source)||!level.mayInteract(actor,dest))continue;
     if(NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level,source,before,actor)).isCanceled())continue;
     if(!level.getBlockState(source).equals(before))return false;
     var snapshot=BlockSnapshot.create(level.dimension(),level,dest);
     if(!level.setBlock(dest,MareEletrica.PRESSURIZED_WATER_BLOCK.get().defaultBlockState(),3))continue;
     if(net.neoforged.neoforge.event.EventHooks.onBlockPlace(actor,snapshot,duct.getValue(FACING))){snapshot.restore(3);continue;}
     // removeBlock preserves a block's fluid; a water source must explicitly become air.
     if(!level.getBlockState(source).equals(before)||!level.setBlock(source,Blocks.AIR.defaultBlockState(),3)){snapshot.restore(3);return false;}
     if(artificial)WaterOrigin.mark(level,dest);else WaterOrigin.clear(level,dest);FaucetFill.stream(level,mouth,duct.getValue(FACING));level.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,dest.getX()+.5,dest.getY()+.8,dest.getZ()+.5,4,.2,.1,.2,0);return true;
    }
   }
   for(Direction d:Direction.values())queue.addLast(pos.relative(d));
  }return false;
 }
}
