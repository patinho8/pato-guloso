package br.mareeletrica;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Only hydro parts have water states; their part range matches the real structure. */
public class HydroMachinePartBlock extends LargeMachinePartBlock implements net.minecraft.world.level.block.SimpleWaterloggedBlock {
 private static final IntegerProperty SMALL_PART=IntegerProperty.create("part",0,26);
 public HydroMachinePartBlock(Kind kind,Properties properties){super(kind,properties);}
 @Override public IntegerProperty partProperty(){return SMALL_PART;}
 @Override protected boolean holdsWater(){return true;}
 @Override public boolean canPlaceLiquid(net.minecraft.world.entity.player.Player player,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.material.Fluid fluid){
  return HydroWater.canFill(state,fluid);
 }
 @Override public boolean placeLiquid(net.minecraft.world.level.LevelAccessor level,net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState state,net.minecraft.world.level.material.FluidState fluid){
  return HydroWater.fill(level,pos,state,fluid);
 }
 @Override protected net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(net.minecraft.world.level.block.state.BlockState s,net.minecraft.world.level.BlockGetter l,net.minecraft.core.BlockPos p,net.minecraft.world.phys.shapes.CollisionContext c){
  return kind==Kind.GIANT_WHEEL||kind==Kind.CURRENT_TURBINE?net.minecraft.world.phys.shapes.Shapes.empty():super.getCollisionShape(s,l,p,c);
 }
 public static final class Giant extends HydroMachinePartBlock {
  private static final IntegerProperty GIANT_PART=IntegerProperty.create("part",0,342);
  public Giant(Kind kind,Properties properties){super(kind,properties);registerDefaultState(defaultBlockState().setValue(MachineBlock.WIDE,false));}
  @Override public IntegerProperty partProperty(){return GIANT_PART;}
  @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,net.minecraft.world.level.block.state.BlockState> b){super.createBlockStateDefinition(b);b.add(MachineBlock.WIDE);}
 }
}
